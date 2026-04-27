import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  NgZone,
  OnDestroy,
  ViewChild,
  inject,
  output,
} from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../core/services/auth.service';
import { GoogleIdentityService } from '../../core/services/google-identity.service';
import { WelcomeDialogService } from '../../core/services/welcome-dialog.service';
import { messageFromHttpError } from '../../core/util/http-error';

declare global {
  interface Window {
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
  selector: 'app-sign-in-providers',
  standalone: true,
  templateUrl: './sign-in-providers.component.html',
  styleUrl: './sign-in-providers.component\.css',
})
export class SignInProvidersComponent implements AfterViewInit, OnDestroy {
  /** `signup` â†’ libellÃ© Google Â« Sign up Â» ; mÃªme API backend (crÃ©ation de compte si besoin). */
  @Input() intent: 'signin' | 'signup' = 'signin';

  /** Sur lâ€™inscription : masquer le bouton e-mail (le formulaire est dÃ©jÃ  en dessous). */
  @Input() oauthOnly = false;

  /** Google puis Facebook empilÃ©s en pleine largeur (ex. page inscription type split). */
  @Input() stacked = false;

  @ViewChild('googleHost') googleHost?: ElementRef<HTMLDivElement>;

  readonly emailContinue = output<void>();

  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly welcomeDialog = inject(WelcomeDialogService);
  private readonly zone = inject(NgZone);
  private readonly googleIdentity = inject(GoogleIdentityService);

  private googleSub: Subscription | null = null;

  readonly hasGoogle = Boolean(environment.googleClientId?.trim());
  readonly hasFacebook = Boolean(environment.facebookAppId?.trim());

  errorMsg = '';
  oauthLoading = false;
  fbReady = false;

  ngAfterViewInit(): void {
    const g = environment.googleClientId?.trim();
    if (g) {
      this.googleSub = this.googleIdentity.credential$.subscribe((credential) => {
        this.sendSocial('GOOGLE', credential);
      });
      this.googleIdentity
        .loadScript()
        .then(() => {
          this.zone.run(() =>
            setTimeout(() => {
              this.googleIdentity.ensureInitialized(g);
              const el = this.googleHost?.nativeElement;
              if (el) {
                this.googleIdentity.renderButton(el, {
                  type: 'standard',
                  theme: 'outline',
                  size: 'large',
                  text: this.intent === 'signup' ? 'signup_with' : 'continue_with',
                  shape: 'rectangular',
                  width: 360,
                  locale: 'en',
                });
              }
            }, 0),
          );
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
    this.googleSub?.unsubscribe();
    this.googleSub = null;
    try {
      window.google?.accounts?.id?.cancelOneTap?.();
    } catch {
      /* ignore */
    }
  }

  onEmailContinue(): void {
    this.emailContinue.emit();
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
            this.errorMsg = 'Connexion Facebook annulÃ©e ou refusÃ©e.';
          }
        });
      },
      { scope: 'public_profile,email' },
    );
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
    this.oauthLoading = true;
    this.auth.socialLogin(provider, token).subscribe({
      next: (res) => {
        this.oauthLoading = false;
        this.welcomeDialog.queueWelcomeForProfile(res.username);
        void this.router.navigateByUrl('/profile');
      },
      error: (err) => {
        this.oauthLoading = false;
        this.errorMsg = messageFromHttpError(err, 'Connexion impossible.');
      },
    });
  }
}

