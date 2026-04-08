import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { messageFromHttpError } from '../../core/util/http-error';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
  });

  successMsg = '';
  errorMsg = '';
  loading = false;

  submit(): void {
    this.successMsg = '';
    this.errorMsg = '';
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading = true;
    this.auth.requestPasswordReset(this.form.controls.email.value.trim()).subscribe({
      next: (res) => {
        this.loading = false;
        this.successMsg = res.message;
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = messageFromHttpError(err, 'Request failed.');
      },
    });
  }
}
