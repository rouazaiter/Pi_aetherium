import { Component, inject, OnInit } from '@angular/core';
import { animate, style, transition, trigger } from '@angular/animations';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SignInProvidersComponent } from '../../components/sign-in-providers/sign-in-providers.component';
import { AuthService } from '../../core/services/auth.service';
import { WelcomeDialogService } from '../../core/services/welcome-dialog.service';
import { messageFromHttpError } from '../../core/util/http-error';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, SignInProvidersComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
  animations: [
    trigger('shellEnter', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(32px) scale(0.97)' }),
        animate(
          '680ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ opacity: 1, transform: 'none' }),
        ),
      ]),
    ]),
    trigger('fadeSlideUp', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(20px)' }),
        animate(
          '480ms 100ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ opacity: 1, transform: 'none' }),
        ),
      ]),
    ]),
    trigger('alertEnter', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(-8px) scale(0.98)' }),
        animate('320ms ease-out', style({ opacity: 1, transform: 'none' })),
      ]),
    ]),
  ],
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly welcomeDialog = inject(WelcomeDialogService);

  /** After clicking â€œContinue with email or usernameâ€. */
  showEmailForm = false;

  /** Toggle password visibility on the email form. */
  showPassword = false;

  readonly form = this.fb.nonNullable.group({
    usernameOrEmail: ['', Validators.required],
    password: ['', Validators.required],
  });

  errorMsg = '';
  infoMsg = '';
  loading = false;

  ngOnInit(): void {
    const reset = this.route.snapshot.queryParamMap.get('reset');
    if (reset === 'ok') {
      this.infoMsg = 'Your password was updated. Sign in with your new password.';
      void this.router.navigate([], {
        relativeTo: this.route,
        queryParams: { reset: null },
        queryParamsHandling: 'merge',
        replaceUrl: true,
      });
    }
  }

  submit(): void {
    this.errorMsg = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.auth.login(this.form.getRawValue()).subscribe({
      next: (res) => {
        this.loading = false;
        this.welcomeDialog.queueWelcomeForProfile(res.username);
        void this.router.navigateByUrl('/profile');
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = messageFromHttpError(err, 'Sign-in failed.');
      },
    });
  }
}

