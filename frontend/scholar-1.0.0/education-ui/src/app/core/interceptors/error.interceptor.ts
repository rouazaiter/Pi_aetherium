import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { catchError, throwError } from 'rxjs';

export const errorInterceptorFn: HttpInterceptorFn = (req, next) => {
  // Skip interceptor for external APIs
  if (!req.url.includes('localhost') && !req.url.includes('127.0.0.1')) {
    return next(req);
  }
  return next(req).pipe(
    catchError((err: HttpErrorResponse) => {
      let message = 'An error occurred';
      if (err.status === 0) {
        message = 'Cannot reach the server. Make sure Spring Boot is running on port 8080.';
      } else if (err.error?.message) {
        message = err.error.message;
      } else {
        message = `Error ${err.status}: ${err.statusText}`;
      }
      console.error('[SkillHub API Error]', message, err);
      return throwError(() => new Error(message));
    })
  );
};
