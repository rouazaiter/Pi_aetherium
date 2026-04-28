import { Injectable, signal } from '@angular/core';

const PENDING_WELCOME_KEY = 'skillhub_pending_welcome_username';

/**
 * Modale de bienvenue après connexion / inscription ; déclenchée depuis la page profil.
 */
@Injectable({ providedIn: 'root' })
export class WelcomeDialogService {
  readonly visible = signal(false);
  readonly userName = signal('');

  showWelcome(username: string): void {
    const name = username?.trim() || 'there';
    this.userName.set(name);
    this.visible.set(true);
  }

  /** À appeler après auth réussie avant navigation vers `/profile`. */
  queueWelcomeForProfile(username: string): void {
    const name = username?.trim() || 'there';
    sessionStorage.setItem(PENDING_WELCOME_KEY, name);
  }

  /** À appeler depuis `ProfileComponent` : affiche la modale si une arrivée depuis auth est en attente. */
  tryShowPendingWelcome(): void {
    const name = sessionStorage.getItem(PENDING_WELCOME_KEY);
    if (!name) {
      return;
    }
    sessionStorage.removeItem(PENDING_WELCOME_KEY);
    this.showWelcome(name);
  }

  close(): void {
    this.visible.set(false);
  }
}
