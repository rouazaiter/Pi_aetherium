import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Frontoffice guard — allows only regular (non-admin) users.
 * Admins are redirected to the backoffice dashboard.
 */
export const frontofficeGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isLoggedIn()) {
    void router.navigateByUrl('/login');
    return false;
  }

  if (auth.auth()?.role === 'admin') {
    // Admins belong in the backoffice, not the store
    void router.navigateByUrl('/skillhub/dashboard');
    return false;
  }

  return true;
};
