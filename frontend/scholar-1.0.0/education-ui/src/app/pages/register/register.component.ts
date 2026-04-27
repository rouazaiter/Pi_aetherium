import { TitleCasePipe } from '@angular/common';
import { animate, style, transition, trigger } from '@angular/animations';
import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { SignInProvidersComponent } from '../../components/sign-in-providers/sign-in-providers.component';
import {
  analyzePassword,
  confirmPasswordMatchesValidator,
  distinctRecoveryEmailValidator,
  isPasswordPolicySatisfied,
  passwordPolicyValidator,
  usernamePatternValidator,
} from '../../core/validators/register.validators';
import { AuthService } from '../../core/services/auth.service';
import { WelcomeDialogService } from '../../core/services/welcome-dialog.service';
import { messageFromHttpError } from '../../core/util/http-error';
@Component({
  selector: 'app-register',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, TitleCasePipe, SignInProvidersComponent],
  templateUrl: './register.component.html',
  styleUrl: './register.component\.css',
  animations: [
    trigger('wizardPane', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(22px) scale(0.99)' }),
        animate(
          '420ms cubic-bezier(0.22, 1, 0.36, 1)',
          style({ opacity: 1, transform: 'none' }),
        ),
      ]),
      transition(':leave', [
        animate(
          '240ms cubic-bezier(0.4, 0, 1, 1)',
          style({ opacity: 0, transform: 'translateY(-12px) scale(0.99)' }),
        ),
      ]),
    ]),
  ],
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly welcomeDialog = inject(WelcomeDialogService);

  readonly form = this.fb.nonNullable.group(
    {
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(64), usernamePatternValidator]],
      email: ['', [Validators.required, Validators.email]],
      password: [
        '',
        [Validators.required, Validators.minLength(8), Validators.maxLength(128), passwordPolicyValidator],
      ],
      confirmPassword: ['', Validators.required],
      dateOfBirth: [''],
      firstName: ['', Validators.required],
      lastName: [''],
      interestsText: [''],
      description: [''],
      recuperationEmail: ['', Validators.email],
      acceptTerms: [false, Validators.requiredTrue],
      /** Champ leurre : doit rester vide (anti-bots basiques). */
      companyWebsite: [''],
    },
    {
      validators: [confirmPasswordMatchesValidator, distinctRecoveryEmailValidator],
    },
  );

  errorMsg = '';
  loading = false;
  showPassword = false;
  showConfirmPassword = false;
  /** Affiche le formulaire e-mail (Ã©cran initial = OAuth uniquement, style Fiverr). */
  showEmailForm = false;

  /** Ã‰tapes du parcours e-mail (1â€“6). */
  emailWizardStep = 1;
  readonly emailWizardTotalSteps = 6;
  readonly wizardStepIndices: readonly number[] = [1, 2, 3, 4, 5, 6];

  private readonly wizardStepLabels = [
    'Votre identitÃ©',
    'Votre e-mail',
    'Votre nom dâ€™utilisateur',
    'SÃ©curisez votre compte',
    'Personnalisez votre profil',
    'Presque terminÃ©',
  ] as const;

  get wizardStepTitle(): string {
    return this.wizardStepLabels[this.emailWizardStep - 1] ?? '';
  }

  wizardStepProgressPercent(): number {
    return (this.emailWizardStep / this.emailWizardTotalSteps) * 100;
  }

  openEmailForm(): void {
    this.showEmailForm = true;
    this.emailWizardStep = 1;
    setTimeout(() => document.getElementById('register-email-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' }), 0);
  }

  prevWizard(): void {
    if (this.emailWizardStep > 1) {
      this.emailWizardStep -= 1;
    }
  }

  continueWizard(): void {
    this.errorMsg = '';
    const markTouched = (...names: string[]) => {
      for (const n of names) {
        this.form.get(n)?.markAsTouched();
      }
    };

    switch (this.emailWizardStep) {
      case 1:
        markTouched('firstName');
        if (this.form.controls.firstName.invalid) {
          this.errorMsg = 'Indiquez votre prÃ©nom pour continuer.';
          return;
        }
        break;
      case 2:
        markTouched('email');
        if (this.form.controls.email.invalid) {
          this.errorMsg = 'Une adresse e-mail valide est obligatoire.';
          return;
        }
        break;
      case 3:
        markTouched('username');
        if (this.form.controls.username.invalid) {
          this.errorMsg =
            'Choisissez un nom dâ€™utilisateur (3â€“64 caractÃ¨res : lettres, chiffres, . _ - uniquement).';
          return;
        }
        break;
      case 4:
        markTouched('password', 'confirmPassword');
        this.form.updateValueAndValidity();
        if (
          this.form.controls.password.invalid ||
          this.form.controls.confirmPassword.invalid ||
          this.form.errors?.['passwordMismatch']
        ) {
          if (!this.form.controls.confirmPassword.value?.trim()) {
            this.errorMsg = 'Renseignez aussi Â« Confirmer le mot de passe Â» (mÃªme mot de passe deux fois).';
          } else if (this.form.errors?.['passwordMismatch']) {
            this.errorMsg = 'Les deux mots de passe doivent Ãªtre identiques.';
          } else if (this.form.controls.password.errors?.['passwordPolicy']) {
            this.errorMsg =
              'Le mot de passe doit respecter toutes les rÃ¨gles listÃ©es : 8 caractÃ¨res minimum, majuscule, minuscule, chiffre et symbole.';
          } else {
            this.errorMsg = 'VÃ©rifiez le mot de passe et sa confirmation.';
          }
          return;
        }
        break;
      case 5:
        markTouched('recuperationEmail');
        this.form.updateValueAndValidity();
        if (this.form.controls.recuperationEmail.invalid || this.form.errors?.['recoverySameAsLogin']) {
          if (this.form.errors?.['recoverySameAsLogin']) {
            this.errorMsg = 'Lâ€™e-mail de rÃ©cupÃ©ration doit Ãªtre diffÃ©rent de lâ€™e-mail de connexion.';
          } else {
            this.errorMsg = 'Corrigez le format de lâ€™e-mail de rÃ©cupÃ©ration ou laissez le champ vide.';
          }
          return;
        }
        break;
      default:
        return;
    }

    if (this.emailWizardStep < this.emailWizardTotalSteps) {
      this.emailWizardStep += 1;
    }
  }

  passwordChecks() {
    return analyzePassword(this.form.controls.password.value);
  }

  passwordStrengthPercent(): number {
    const c = this.passwordChecks();
    const n = [c.minLength, c.hasLower, c.hasUpper, c.hasDigit, c.hasSpecial].filter(Boolean).length;
    return (n / 5) * 100;
  }

  passwordStrengthLabel(): 'faible' | 'moyen' | 'fort' {
    const c = this.passwordChecks();
    if (!this.form.controls.password.value) {
      return 'faible';
    }
    if (isPasswordPolicySatisfied(c)) {
      return 'fort';
    }
    const n = [c.minLength, c.hasLower, c.hasUpper, c.hasDigit, c.hasSpecial].filter(Boolean).length;
    return n >= 3 ? 'moyen' : 'faible';
  }

  submit(): void {
    this.errorMsg = '';
    if (this.showEmailForm && this.emailWizardStep !== this.emailWizardTotalSteps) {
      return;
    }
    if (this.form.controls.companyWebsite.value?.trim()) {
      this.errorMsg = 'Inscription impossible. RÃ©essayez plus tard.';
      return;
    }
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    const interests = v.interestsText
      .split(',')
      .map((s) => s.trim())
      .filter(Boolean);
    const body = {
      username: v.username.trim(),
      email: v.email.trim(),
      password: v.password,
      dateOfBirth: v.dateOfBirth || null,
      firstName: v.firstName?.trim() || null,
      lastName: v.lastName?.trim() || null,
      interests,
      description: v.description?.trim() || null,
      recuperationEmail: v.recuperationEmail?.trim() || null,
    };
    this.loading = true;
    this.auth.register(body).subscribe({
      next: (res) => {
        this.loading = false;
        this.welcomeDialog.queueWelcomeForProfile(res.username);
        void this.router.navigateByUrl('/profile');
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = messageFromHttpError(err, 'Inscription impossible.');
      },
    });
  }
}

