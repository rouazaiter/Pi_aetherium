import { AfterViewInit, Component, ElementRef, Input, NgZone, OnDestroy, ViewChild, inject } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { messageFromHttpError } from '../../core/util/http-error';

declare global {
  interface Window {
    google?: {
      accounts: {
        id: {
          initialize: (cfg: Record<string, unknown>) => void;
          renderButton: (el: HTMLElement, opts: Record<string, unknown>) => void;
          cancelOneTap?: () => void;
        };
      };
    };
    FB?: {
      init: (cfg: Record<string, unknown>) => void;
      login: (
        cb: (r: { authResponse?: { accessToken: string } }) => void,
        opts?: Record<string, unknown>,
      ) => void;
    };
    fbAsyncInit?: () => void;
  }
}

@Component({
  selector: 'app-social-login',
  standalone: true,
  templateUrl: './social-login.component.html',
  styleUrl: './social-login.component.scss',
})
export class SocialLoginComponent implements AfterViewInit, OnDestroy {
  /** Affiche un titre au-dessus du bouton Google (ex. page connexion). */
  @Input() showGoogleHeading = false;

  /** Sans bordure du haut (quand le bloc est placé en premier sur la page). */
  @Input() flushTop = false;

  @ViewChild('googleHost') googleHost?: ElementRef<HTMLDivElement>;

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly zone = inject(NgZone);

  readonly hasGoogle = Boolean(environment.googleClientId?.trim());
  readonly hasFacebook = Boolean(environment.facebookAppId?.trim());

  errorMsg = '';
  loading = false;
  fbReady = false;

  ngAfterViewInit(): void {
    if (!this.hasGoogle && !this.hasFacebook) {
      return;
    }
    const g = environment.googleClientId?.trim();
    if (g) {
      this.loadGoogleScript()
        .then(() => {
          this.zone.run(() => {
            setTimeout(() => this.renderGoogleButton(g), 0);
          });
        })
        .catch(() => this.zone.run(() => (this.errorMsg = 'Impossible de charger Google Sign-In.')));
    }
    const f = environment.facebookAppId?.trim();
    if (f) {
      this.loadFacebookSdk(f)
        .then(() => this.zone.run(() => (this.fbReady = true)))
        .catch(() => this.zone.run(() => (this.errorMsg = 'Impossible de charger le SDK Facebook.')));
    }
  }

  ngOnDestroy(): void {
    try {
      window.google?.accounts?.id?.cancelOneTap?.();
    } catch {
      /* ignore */
    }
  }

  facebookLogin(): void {
    if (!window.FB) {
      return;
    }
    this.errorMsg = '';
    window.FB.login(
      (response) => {
        this.zone.run(() => {
          const token = response.authResponse?.accessToken;
          if (token) {
            this.sendSocial('FACEBOOK', token);
          } else {
            this.errorMsg = 'Connexion Facebook annulée ou refusée.';
          }
        });
      },
      { scope: 'public_profile,email' },
    );
  }

  private loadGoogleScript(): Promise<void> {
    if (window.google?.accounts?.id) {
      return Promise.resolve();
    }
    return new Promise((resolve, reject) => {
      const src = 'https://accounts.google.com/gsi/client';
      const existing = document.querySelector(`script[src="${src}"]`);
      if (existing) {
        const el = existing as HTMLScriptElement;
        if (window.google?.accounts?.id) {
          resolve();
          return;
        }
        el.addEventListener('load', () => resolve(), { once: true });
        el.addEventListener('error', () => reject(), { once: true });
        return;
      }
      const s = document.createElement('script');
      s.src = src;
      s.async = true;
      s.defer = true;
      s.onload = () => resolve();
      s.onerror = () => reject();
      document.head.appendChild(s);
    });
  }

  private renderGoogleButton(clientId: string): void {
    const el = this.googleHost?.nativeElement;
    if (!el || !window.google?.accounts?.id) {
      return;
    }
    window.google.accounts.id.initialize({
      client_id: clientId,
      callback: (resp: { credential?: string }) =>
        this.zone.run(() => {
          const c = resp?.credential;
          if (c) {
            this.sendSocial('GOOGLE', c);
          }
        }),
      auto_select: false,
      itp_support: true,
    });
    window.google.accounts.id.renderButton(el, {
      type: 'standard',
      theme: 'outline',
      size: 'large',
      text: 'signin_with',
      shape: 'rectangular',
      width: 320,
      locale: 'fr',
    });
  }

  private loadFacebookSdk(appId: string): Promise<void> {
    return new Promise((resolve, reject) => {
      const init = () => {
        try {
          window.FB!.init({
            appId,
            cookie: true,
            xfbml: false,
            version: 'v19.0',
          });
          resolve();
        } catch {
          reject();
        }
      };

      if (window.FB) {
        init();
        return;
      }

      window.fbAsyncInit = init;

      if (document.getElementById('facebook-jssdk')) {
        const start = Date.now();
        const iv = setInterval(() => {
          if (window.FB) {
            clearInterval(iv);
            init();
          } else if (Date.now() - start > 15000) {
            clearInterval(iv);
            reject();
          }
        }, 50);
        return;
      }

      const fjs = document.getElementsByTagName('script')[0];
      const js = document.createElement('script');
      js.id = 'facebook-jssdk';
      js.async = true;
      js.defer = true;
      js.src = 'https://connect.facebook.net/en_US/sdk.js';
      js.onerror = () => reject();
      fjs.parentNode?.insertBefore(js, fjs);
    });
  }

  private sendSocial(provider: 'GOOGLE' | 'FACEBOOK', token: string): void {
    this.errorMsg = '';
    this.loading = true;
    this.auth.socialLogin(provider, token).subscribe({
      next: () => {
        this.loading = false;
        void this.router.navigateByUrl('/profile');
      },
      error: (err) => {
        this.loading = false;
        this.errorMsg = messageFromHttpError(err, 'Connexion impossible.');
      },
    });
  }
}
