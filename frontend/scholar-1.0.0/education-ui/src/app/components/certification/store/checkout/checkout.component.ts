import { Component, OnInit, OnDestroy, ElementRef, ViewChild, AfterViewInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { ToastService } from '../../shared/toast.service';
import { CertificationDetail } from '../../models/certification.model';
import { loadStripe, Stripe, StripeElements, StripePaymentElement } from '@stripe/stripe-js';
import { NgIf, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';

type Step = 'payment_ready' | 'payment' | 'verify' | 'processing' | 'done';

const STRIPE_PK = 'pk_test_51TP7oSEUQLDnJdgCUyw9qPhJ096VRn16pQbfyzyr7piAEvHC060Rqr1dKjc72aVtTfUO6YacLt3VLt1lq3y6kzkQ00BjlA2jnY';

@Component({
    selector: 'app-checkout',
    templateUrl: './checkout.component.html',
    styleUrls: ['./checkout.component.scss'],
    standalone: true,
    imports: [NgIf, FormsModule, DecimalPipe]
})
export class CheckoutComponent implements OnInit, OnDestroy {

  @ViewChild('paymentElement') paymentElementRef!: ElementRef;

  certId!: number;
  cert: CertificationDetail | null = null;
  loading = true;
  error = '';

  step: Step = 'payment_ready';
  userIdentifier = '';
  fullName = '';
  phoneNumber = '';
  processing = false;

  // Stripe
  private stripe: Stripe | null = null;
  private elements: StripeElements | null = null;
  private paymentElement: StripePaymentElement | null = null;
  private paymentIntentId = '';
  stripeLoading = false;

  // Email verification (for free certs)
  verificationCode = '';
  resendCooldown = 0;
  private cooldownInterval: any;

  constructor(
    public router: Router,
    private route: ActivatedRoute,
    private enrollService: EnrollmentService,
    private toast: ToastService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.certId = Number(this.route.snapshot.paramMap.get('id'));

    // Auto-populate from Pi_aetherium session — no manual entry needed
    const session = this.authService.auth();
    this.userIdentifier = session?.email ?? '';
    this.fullName       = session?.username ?? '';

    this.enrollService.getStoreDetail(this.certId).subscribe({
      next: data => {
        this.cert = data;
        this.loading = false;
        // Skip the email step — user is already authenticated
        if (this.userIdentifier) {
          this.step = 'payment_ready'; // will be handled in initiatePayment
          this.checkExistingEnrollment();
        }
      },
      error: () => this.router.navigate(['/skillhub/store'])
    });
  }

  private checkExistingEnrollment(): void {
    this.processing = true;
    this.enrollService.getEnrollment(this.certId, this.userIdentifier).subscribe({
      next: e => {
        this.processing = false;
        if (e?.isVerified) {
          this.toast.info('You are already enrolled!');
          this.router.navigate(['/skillhub/store', this.certId]);
          return;
        }
        this.initiatePayment();
      },
      error: () => { this.processing = false; this.initiatePayment(); }
    });
  }

  ngOnDestroy(): void {
    this.paymentElement?.unmount();
    if (this.cooldownInterval) clearInterval(this.cooldownInterval);
  }

  get totalQuestions(): number {
    return this.cert?.exams.reduce((s, e) => s + e.questions.length, 0) ?? 0;
  }

  // ── Step 1 is now skipped — user comes from session ──────────────────────
  // confirmEmail() is no longer needed; checkExistingEnrollment() handles it

  isValidPhone(p: string): boolean {
    return /^\+[1-9]\d{6,14}$/.test(p.replace(/\s/g, ''));
  }

  // ── Step 2: Payment ───────────────────────────────────────────────────────

  private initiatePayment(): void {
    if (!this.cert) return;

    // Free cert — send verification email then go to verify step
    if (this.cert.price === 0) {
      this.processing = true;
      this.enrollService.confirmPaymentFree(this.certId, this.userIdentifier, this.fullName, this.phoneNumber).subscribe({
        next: () => {
          this.processing = false;
          this.step = 'verify';
          this.startCooldown();
          this.toast.success(`Verification code sent to ${this.userIdentifier}`);
        },
        error: e => { this.processing = false; this.step = 'payment_ready'; this.error = e.message || 'Failed to send code'; }
      });
      return;
    }

    // Paid cert — create Stripe PaymentIntent then mount Elements
    this.stripeLoading = true;
    this.step = 'payment';

    this.enrollService.createPaymentIntent(this.certId, this.userIdentifier).subscribe({
      next: async (res) => {
        this.paymentIntentId = res.paymentIntentId;
        await this.mountStripeElements(res.clientSecret);
        this.stripeLoading = false;
      },
      error: e => {
        this.stripeLoading = false;
        this.step = 'payment_ready';
        this.error = e.message || 'Failed to initialize payment';
      }
    });
  }

  private async mountStripeElements(clientSecret: string): Promise<void> {
    this.stripe = await loadStripe(STRIPE_PK);
    if (!this.stripe) { this.error = 'Stripe failed to load'; return; }

    this.elements = this.stripe.elements({
      clientSecret,
      appearance: {
        theme: 'stripe',
        variables: {
          colorPrimary: '#4f46e5',
          colorBackground: '#ffffff',
          colorText: '#0f172a',
          colorDanger: '#ef4444',
          fontFamily: 'Inter, system-ui, sans-serif',
          borderRadius: '9px',
          spacingUnit: '4px'
        }
      }
    });

    this.paymentElement = this.elements.create('payment', {
      layout: 'tabs'
    });

    // Wait for DOM then mount
    setTimeout(() => {
      const el = document.getElementById('stripe-payment-element');
      if (el && this.paymentElement) {
        this.paymentElement.mount(el);
      }
      // Remove the Stripe floating badge injected into <body>
      this.removeStripeBadge();
    }, 500);
  }

  // ── Step 3: Confirm Stripe payment → send email code ─────────────────────

  async pay(): Promise<void> {
    if (!this.stripe || !this.elements) return;
    this.processing = true;
    this.error = '';

    // confirmPayment with redirect:'if_required' — no return_url needed for card payments
    // This avoids Stripe injecting a redirect link/banner
    const result = await this.stripe.confirmPayment({
      elements: this.elements,
      redirect: 'if_required'
    });
    if (result.error) {
      // Show Stripe's own error message (card declined, etc.)
      this.error = result.error.message || 'Payment failed. Please check your card details.';
      this.processing = false;
      return;
    }

    // Verify the paymentIntent actually succeeded before calling backend
    const pi = result.paymentIntent;
    if (!pi || pi.status !== 'succeeded') {
      this.error = `Payment not completed (status: ${pi?.status ?? 'unknown'}). Please try again.`;
      this.processing = false;
      return;
    }

    // Payment succeeded — tell backend to verify with Stripe and send email code
    this.enrollService.confirmStripePayment(
      this.certId, this.userIdentifier, pi.id, this.fullName, this.phoneNumber
    ).subscribe({
      next: () => {
        this.processing = false;
        this.step = 'verify';
        this.startCooldown();
        this.removeStripeBadge();
        this.toast.success(`Payment confirmed! Check ${this.userIdentifier} for your verification code.`);
      },
      error: e => {
        this.processing = false;
        this.step = 'payment';
        this.error = e.message || 'Payment confirmation failed. Please contact support.';
      }
    });
  }

  // ── Step 4: Verify email code (paid + free) ───────────────────────────────

  verifyCode(): void {
    if (this.verificationCode.length !== 6) { this.error = 'Enter the 6-digit code'; return; }
    this.error = ''; this.processing = true;
    this.enrollService.verifyCode(this.certId, this.userIdentifier, this.verificationCode).subscribe({
      next: () => {
        this.processing = false;
        this.step = 'done';
        this.removeStripeBadge();
        this.toast.success('Certification unlocked! 🎉');
      },
      error: e => { this.processing = false; this.error = e.message || 'Invalid or expired code'; }
    });
  }

  resendCode(): void {
    if (this.resendCooldown > 0) return;
    this.error = '';
    this.enrollService.confirmPayment(this.certId, this.userIdentifier).subscribe({
      next: () => { this.toast.success('New code sent!'); this.startCooldown(); },
      error: e => { this.error = e.message; }
    });
  }

  private startCooldown(): void {
    this.resendCooldown = 60;
    this.cooldownInterval = setInterval(() => {
      this.resendCooldown--;
      if (this.resendCooldown <= 0) clearInterval(this.cooldownInterval);
    }, 1000);
  }

  onCodeInput(e: Event): void {
    const v = (e.target as HTMLInputElement).value.replace(/\D/g, '').substring(0, 6);
    this.verificationCode = v;
    (e.target as HTMLInputElement).value = v;
  }

  startExam(): void { this.router.navigate(['/skillhub/store', this.certId, 'exam']); }
  goBack():    void { this.router.navigate(['/skillhub/store', this.certId]); }
  goToStore(): void { this.router.navigate(['/skillhub/store']); }

  /** Remove only the Stripe floating badge — NOT the payment element iframes */
  private removeStripeBadge(): void {
    const removeBadge = () => {
      // Only remove the metrics controller and cookie-settings iframes
      document.querySelectorAll<HTMLIFrameElement>('body > iframe').forEach(iframe => {
        const name = iframe.name || '';
        const src  = iframe.src  || '';
        if (
          name.startsWith('__privateStripeMetricsController') ||
          src.includes('stripe.com/cookie-settings')
        ) {
          iframe.remove();
        }
      });

      // Remove the fixed bottom-right badge wrapper div
      document.querySelectorAll<HTMLElement>('body > div').forEach(div => {
        const style = div.getAttribute('style') || '';
        const isFixed       = style.includes('position: fixed') || style.includes('position:fixed');
        const isBottomRight = style.includes('bottom') && style.includes('right');
        if (isFixed && isBottomRight) {
          const inner = div.querySelector('iframe');
          if (inner && (inner.src || '').includes('stripe')) {
            div.remove();
          }
        }
      });
    };

    // Run immediately and after delays to catch async injections
    removeBadge();
    setTimeout(removeBadge, 500);
    setTimeout(removeBadge, 1500);
    setTimeout(removeBadge, 3000);

    // Also watch for Stripe re-injecting the badge after step changes
    const observer = new MutationObserver(() => removeBadge());
    observer.observe(document.body, { childList: true, subtree: false });
    // Stop observing after 10s to avoid memory leaks
    setTimeout(() => observer.disconnect(), 10000);
  }
}
