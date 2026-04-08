import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  AuthResponse,
  LoginRequest,
  MessageResponse,
  SignUpRequest,
  SocialLoginRequest,
} from '../models/api.models';

const TOKEN_KEY = 'education_platform_token';
const AUTH_KEY = 'education_platform_auth';

/** Base API URL: absolute (e.g. http://localhost:8081) or empty → same-origin `/api/...` with dev proxy. */
function authUrl(suffix: string): string {
  const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
  const path = suffix.startsWith('/') ? suffix : `/${suffix}`;
  return base ? `${base}${path}` : path;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly authSignal = signal<AuthResponse | null>(this.readStoredAuth());

  readonly auth = this.authSignal.asReadonly();
  readonly isLoggedIn = computed(() => !!this.authSignal()?.token);
  readonly token = computed(() => this.authSignal()?.token ?? null);

  login(body: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(authUrl('/api/auth/login'), body)
      .pipe(tap((res) => this.persist(res)));
  }

  register(body: SignUpRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(authUrl('/api/auth/register'), body)
      .pipe(tap((res) => this.persist(res)));
  }

  socialLogin(provider: SocialLoginRequest['provider'], token: string): Observable<AuthResponse> {
    const body: SocialLoginRequest = { provider, token };
    return this.http
      .post<AuthResponse>(authUrl('/api/auth/social'), body)
      .pipe(tap((res) => this.persist(res)));
  }

  requestPasswordReset(email: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(authUrl('/api/auth/forgot-password'), { email });
  }

  resetPassword(token: string, newPassword: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(authUrl('/api/auth/reset-password'), {
      token,
      newPassword,
    });
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(AUTH_KEY);
    this.authSignal.set(null);
    void this.router.navigateByUrl('/');
  }

  /** Met à jour la photo en session (ex. après édition du profil) sans refaire un login. */
  patchSessionProfilePicture(url: string | null | undefined): void {
    const cur = this.authSignal();
    if (!cur) {
      return;
    }
    const next: AuthResponse = {
      ...cur,
      profilePicture: url ?? null,
    };
    localStorage.setItem(AUTH_KEY, JSON.stringify(next));
    this.authSignal.set(next);
  }

  private persist(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(AUTH_KEY, JSON.stringify(res));
    this.authSignal.set(res);
  }

  private readStoredAuth(): AuthResponse | null {
    const raw = localStorage.getItem(AUTH_KEY);
    const t = localStorage.getItem(TOKEN_KEY);
    if (!raw || !t) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as AuthResponse;
      if (parsed.token !== t) {
        return null;
      }
      return parsed;
    } catch {
      return null;
    }
  }
}
