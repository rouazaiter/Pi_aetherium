import { Component, effect, inject, OnDestroy, OnInit, signal, untracked } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { environment } from '../environments/environment';
import { WelcomeDialogComponent } from './components/welcome-dialog/welcome-dialog.component';
import { resolvePresetProfilePicture } from './core/data/preset-avatars';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, WelcomeDialogComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit, OnDestroy {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private navSub?: Subscription;

  /** Menu mobile ouvert. */
  readonly navOpen = signal(false);

  /** Photo de profil dans la barre : repasse à false si l’URL change. */
  protected readonly navAvatarImgError = signal(false);

  private readonly _resetNavAvatarOnAuthPictureChange = effect(() => {
    const a = this.auth.auth();
    const _ = `${a?.userId ?? ''}|${a?.profilePicture ?? ''}`;
    untracked(() => this.navAvatarImgError.set(false));
  });

  ngOnInit(): void {
    this.navSub = this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => this.navOpen.set(false));
  }

  ngOnDestroy(): void {
    this.navSub?.unsubscribe();
  }

  toggleNav(): void {
    this.navOpen.update((v) => !v);
  }

  closeNav(): void {
    this.navOpen.set(false);
  }

  logout(): void {
    this.auth.logout();
  }

  protected navAvatarUrl(): string {
    const pic = this.auth.auth()?.profilePicture?.trim();
    if (!pic) {
      return '';
    }
    const preset = resolvePresetProfilePicture(pic);
    if (preset) {
      return preset;
    }
    if (/^https?:\/\//i.test(pic)) {
      return pic;
    }
    if (pic.startsWith('/api/')) {
      const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
      return base ? `${base}${pic}` : pic;
    }
    return '';
  }

  protected navAvatarInitials(): string {
    const u = this.auth.auth()?.username?.trim();
    return u ? u.slice(0, 2).toUpperCase() : '?';
  }
}
