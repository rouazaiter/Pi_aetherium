import { Component, inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import {
  confirmPasswordMatchesValidator,
  passwordPolicyValidator,
} from '../../core/validators/register.validators';
import { AuthService } from '../../core/services/auth.service';
import { messageFromHttpError } from '../../core/util/http-error';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.component.html',
  styleUrl: './reset-password.component.scss',
})
export class ResetPasswordComponent implements OnInit, OnDestroy {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private querySub: Subscription | null = null;

  token = '';

  readonly form = this.fb.nonNullable.group(
    {
      password: [
        '',
        [Validators.required, Validators.minLength(8), Validators.maxLength(128), passwordPolicyValidator],
      ],
      confirmPassword: ['', Validators.required],
    },
    { validators: [confirmPasswordMatchesValidator] },
  );

  errorMsg = '';
  loading = false;

  ngOnInit(): void {
    this.querySub = this.route.queryParamMap.subscribe((params) => {
      this.token = (params.get('token') ?? '').trim();
    });
  }

  ngOnDestroy(): void {
    this.querySub?.unsubscribe();
  }

  submit(): void {
    this.errorMsg = '';
    if (!this.token) {
      this.errorMsg = 'This reset link is invalid or incomplete.';
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.auth.resetPassword(this.token, this.form.controls.password.value).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigate(['/login'], {
          queryParams: { reset: 'ok' },
        });
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = messageFromHttpError(err, 'Could not reset password.');
      },
    });
  }
}

