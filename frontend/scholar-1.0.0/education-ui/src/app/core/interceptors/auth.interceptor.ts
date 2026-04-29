import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/** Only attach our JWT to this app's Spring API — never to third-party hosts (Giphy, OAuth, etc.). */
function shouldAttachJwt(url: string): boolean {
  if (url.includes('/api/auth/')) {
    return false;
  }
  const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
  if (!base) {
    return url.startsWith('/api/');
  }
  return url.startsWith(`${base}/api/`);
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  if (!token || !shouldAttachJwt(req.url)) {
    return next(req);
  }
  return next(
    req.clone({
      setHeaders: { Authorization: `Bearer ${token}` },
    }),
  );
};
