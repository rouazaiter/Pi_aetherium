import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { EnrollmentService } from '../../services/enrollment.service';
import { FeedbackService } from '../../services/feedback.service';
import { EnrollmentDTO } from '../../models/enrollment.model';
import { NgIf, CurrencyPipe, DatePipe } from '@angular/common';
import { FeedbackModalComponent } from '../feedback-modal/feedback-modal.component';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-store-result',
    templateUrl: './store-result.component.html',
    styleUrls: ['./store-result.component.scss'],
    standalone: true,
    imports: [NgIf, RouterLink, FeedbackModalComponent, FormsModule, CurrencyPipe, DatePipe]
})
export class StoreResultComponent implements OnInit, OnDestroy {
  enrollment: EnrollmentDTO | null = null;
  loading = true;
  error = '';
  certId!: number;

  // Certificate inline preview
  certPdfUrl: SafeResourceUrl | null = null;
  loadingCert = false;
  showCertPreview = false;

  // LinkedIn post
  showLinkedInModal = false;
  linkedInPost = '';
  loadingPost = false;
  postCopied = false;

  // Feedback modal
  showFeedbackModal = false;
  feedbackAlreadySubmitted = false;

  // Cooldown state
  cooldownEnd: Date | null = null;
  cooldownDisplay = '';
  private cooldownInterval: any = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enrollService: EnrollmentService,
    private feedbackService: FeedbackService,
    private sanitizer: DomSanitizer
  ) {}

  ngOnInit(): void {
    this.certId = Number(this.route.snapshot.paramMap.get('id'));
    const user = this.enrollService.getUser();
    if (!user) { this.router.navigate(['/store', this.certId]); return; }

    this.enrollService.getEnrollment(this.certId, user).subscribe({
      next: e => {
        this.enrollment = e;
        this.loading = false;
        if (e?.passed) {
          this.loadCertPreview();
          this.checkFeedbackStatus();
        }
        // If failed and has a lastAttemptAt, compute cooldown
        if (e && !e.passed && e.lastAttemptAt) {
          this.initCooldown(e.lastAttemptAt);
        }
      },
      error: () => { this.error = 'Result not found'; this.loading = false; }
    });
  }

  private initCooldown(lastAttemptAt: string): void {
    const end = new Date(new Date(lastAttemptAt).getTime() + 24 * 60 * 60 * 1000);
    if (new Date() >= end) return; // cooldown already expired
    this.cooldownEnd = end;
    this.updateCooldownDisplay();
    this.cooldownInterval = setInterval(() => {
      this.updateCooldownDisplay();
      if (this.cooldownEnd && new Date() >= this.cooldownEnd) {
        clearInterval(this.cooldownInterval);
        this.cooldownEnd = null;
        this.cooldownDisplay = '';
      }
    }, 1000);
  }

  private updateCooldownDisplay(): void {
    if (!this.cooldownEnd) return;
    const diff = this.cooldownEnd.getTime() - Date.now();
    if (diff <= 0) { this.cooldownDisplay = ''; return; }
    const h = Math.floor(diff / 3600000);
    const m = Math.floor((diff % 3600000) / 60000);
    const s = Math.floor((diff % 60000) / 1000);
    this.cooldownDisplay =
      `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
  }

  get isCooldownActive(): boolean {
    return !!this.cooldownEnd && new Date() < this.cooldownEnd;
  }

  private checkFeedbackStatus(): void {    if (!this.enrollment) return;
    this.feedbackService.checkSubmitted(this.enrollment.id).subscribe({
      next: res => {
        this.feedbackAlreadySubmitted = res.submitted;
        // Auto-show feedback modal after 1.5s if not yet submitted
        if (!res.submitted) {
          setTimeout(() => { this.showFeedbackModal = true; }, 1500);
        }
      },
      error: () => {} // silently ignore
    });
  }

  openFeedbackModal(): void  { this.showFeedbackModal = true; }
  closeFeedbackModal(): void { this.showFeedbackModal = false; }
  onFeedbackSubmitted(): void {
    this.showFeedbackModal = false;
    this.feedbackAlreadySubmitted = true;
  }

  ngOnDestroy(): void {
    if (this.cooldownInterval) clearInterval(this.cooldownInterval);
  }

  retryExam(): void {
    if (!this.enrollment || this.isCooldownActive) return;
    const user = this.enrollService.getUser();
    this.enrollService.retryExam(this.certId, user).subscribe({
      next: () => this.router.navigate(['/store', this.certId, 'exam']),
      error: e => {
        const msg: string = e?.error?.message || '';
        // Parse cooldown error: "COOLDOWN:<isoDate>:<human message>"
        if (msg.startsWith('COOLDOWN:')) {
          const parts = msg.split(':');
          const isoDate = parts[1] + ':' + parts[2] + ':' + parts[3]; // reassemble ISO
          this.initCooldown(isoDate);
          this.error = '';
        } else {
          this.error = msg || 'Could not retry exam';
        }
      }
    });
  }

  get certIdFormatted(): string {
    if (!this.enrollment) return '';
    return 'SKH-' + String(this.enrollment.id).padStart(6, '0');
  }
  get scorePercent(): number { return Math.round(this.enrollment?.score ?? 0); }
  get circumference(): number { return 2 * Math.PI * 54; }
  get dashOffset(): number { return this.circumference * (1 - this.scorePercent / 100); }

  loadCertPreview(): void {
    if (!this.enrollment) return;
    this.loadingCert = true;
    this.enrollService.downloadCertificate(this.enrollment.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        this.certPdfUrl = this.sanitizer.bypassSecurityTrustResourceUrl(url);
        this.showCertPreview = true;
        this.loadingCert = false;
      },
      error: () => { this.loadingCert = false; }
    });
  }

  downloadCertificate(): void {
    if (!this.enrollment) return;
    this.enrollService.downloadCertificate(this.enrollment.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `SkillHub-Certificate-${this.enrollment?.certificationTitle}.pdf`;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => { this.error = 'Failed to download certificate'; }
    });
  }

  // ── LinkedIn ──────────────────────────────────────────────────────────────

  openLinkedInModal(): void {
    if (!this.enrollment) return;
    this.showLinkedInModal = true;
    if (!this.linkedInPost) {
      this.loadingPost = true;
      this.enrollService.generateLinkedInPost(this.enrollment.id).subscribe({
        next: res => { this.linkedInPost = res.post; this.loadingPost = false; },
        error: () => {
          this.linkedInPost = this.buildFallbackPost();
          this.loadingPost = false;
        }
      });
    }
  }

  closeLinkedInModal(): void {
    this.showLinkedInModal = false;
    this.postCopied = false;
  }

  copyPost(): void {
    navigator.clipboard.writeText(this.linkedInPost).then(() => {
      this.postCopied = true;
      setTimeout(() => this.postCopied = false, 2500);
    });
  }

  shareOnLinkedIn(): void {
    // Copy post to clipboard then open LinkedIn share
    navigator.clipboard.writeText(this.linkedInPost).catch(() => {});
    const url = encodeURIComponent('https://skillhub.io');
    const text = encodeURIComponent(this.linkedInPost.substring(0, 700));
    window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${url}&summary=${text}`, '_blank');
  }

  private buildFallbackPost(): string {
    const name  = this.enrollment?.fullName || this.enrollment?.userIdentifier || 'I';
    const title = this.enrollment?.certificationTitle || 'a certification';
    const score = Math.round(this.enrollment?.score ?? 0);
    return `🎓 Excited to share that I just earned the "${title}" certification on SkillHub with a score of ${score}%!\n\nThis journey pushed me to deepen my knowledge and sharpen my skills. Continuous learning is the foundation of growth — and this is just the beginning.\n\nIf you're looking to validate your expertise and stand out, check out SkillHub at https://skillhub.io\n\n#Certification #SkillHub #Learning #ProfessionalDevelopment #Upskilling #Growth`;
  }
}
