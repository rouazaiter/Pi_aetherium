import { Injectable } from '@angular/core';
import { HttpInterceptor, HttpRequest, HttpHandler, HttpEvent, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // Skip interceptor for external APIs
    if (!req.url.includes('localhost') && !req.url.includes('127.0.0.1')) {
      return next.handle(req);
    }
    return next.handle(req).pipe(
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
  }
}
