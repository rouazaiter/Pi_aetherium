import { Injectable, NgZone, inject } from '@angular/core';
import { Subject } from 'rxjs';

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
  }
}

/**
 * Un seul appel à google.accounts.id.initialize() par chargement de page (exigence Google).
 * Les pages login / inscription ne doivent appeler que renderButton + s’abonner à credential$.
 */
@Injectable({ providedIn: 'root' })
export class GoogleIdentityService {
  private readonly zone = inject(NgZone);
  private readonly credentialSubject = new Subject<string>();
  readonly credential$ = this.credentialSubject.asObservable();

  private initDone = false;
  private clientId = '';

  loadScript(): Promise<void> {
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

  /**
   * Initialise Google Identity une seule fois pour ce client_id.
   */
  ensureInitialized(clientId: string): void {
    const id = clientId?.trim();
    if (!id || !window.google?.accounts?.id) {
      return;
    }
    if (this.initDone && this.clientId === id) {
      return;
    }
    this.clientId = id;
    this.initDone = true;
    window.google.accounts.id.initialize({
      client_id: id,
      callback: (resp: { credential?: string }) => {
        const c = resp?.credential;
        if (c) {
          this.zone.run(() => this.credentialSubject.next(c));
        }
      },
      auto_select: false,
      itp_support: true,
      // Évite le flux FedCM du bouton (souvent 403 si l’origine n’est pas encore bien enregistrée côté Google).
      use_fedcm_for_button: false,
    });
  }

  renderButton(host: HTMLElement, options: Record<string, unknown>): void {
    if (!host || !window.google?.accounts?.id) {
      return;
    }
    host.replaceChildren();
    window.google.accounts.id.renderButton(host, options);
  }
}
