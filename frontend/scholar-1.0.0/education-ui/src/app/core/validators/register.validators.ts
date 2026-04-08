import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

/** Caractères spéciaux acceptés pour le mot de passe */
const SPECIAL_RE = /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/;

export interface PasswordPolicyChecks {
  minLength: boolean;
  hasLower: boolean;
  hasUpper: boolean;
  hasDigit: boolean;
  hasSpecial: boolean;
}

export function analyzePassword(password: string): PasswordPolicyChecks {
  const pw = password ?? '';
  return {
    minLength: pw.length >= 8 && pw.length <= 128,
    hasLower: /[a-z]/.test(pw),
    hasUpper: /[A-Z]/.test(pw),
    hasDigit: /\d/.test(pw),
    hasSpecial: SPECIAL_RE.test(pw),
  };
}

export function isPasswordPolicySatisfied(checks: PasswordPolicyChecks): boolean {
  return (
    checks.minLength && checks.hasLower && checks.hasUpper && checks.hasDigit && checks.hasSpecial
  );
}

/** Mot de passe vide : laisser `required` gérer ; sinon toutes les règles doivent être remplies. */
export function passwordPolicyValidator(control: AbstractControl): ValidationErrors | null {
  const pw = (control.value as string) ?? '';
  if (!pw) {
    return null;
  }
  const checks = analyzePassword(pw);
  return isPasswordPolicySatisfied(checks) ? null : { passwordPolicy: checks };
}

/** Nom d’utilisateur : lettres, chiffres, point, tiret, underscore — pas d’espaces. */
export const USERNAME_PATTERN = /^[a-zA-Z0-9._-]{3,64}$/;

export function usernamePatternValidator(control: AbstractControl): ValidationErrors | null {
  const v = (control.value as string) ?? '';
  if (!v) {
    return null;
  }
  return USERNAME_PATTERN.test(v.trim()) ? null : { usernamePattern: true };
}

export const confirmPasswordMatchesValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const password = group.get('password')?.value as string | undefined;
  const confirm = group.get('confirmPassword')?.value as string | undefined;
  if (confirm === undefined || confirm === '') {
    return null;
  }
  return password === confirm ? null : { passwordMismatch: true };
};

/** E-mail principal et e-mail de récupération ne doivent pas être identiques si les deux sont renseignés. */
export const distinctRecoveryEmailValidator: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const email = (group.get('email')?.value as string)?.trim().toLowerCase() ?? '';
  const rec = (group.get('recuperationEmail')?.value as string)?.trim().toLowerCase() ?? '';
  if (!rec || !email) {
    return null;
  }
  return email === rec ? { recoverySameAsLogin: true } : null;
};
