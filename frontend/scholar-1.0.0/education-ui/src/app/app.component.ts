import { Component, effect, HostListener, inject, OnDestroy, OnInit, signal, untracked } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { environment } from '../environments/environment';
import { WelcomeDialogComponent } from './components/welcome-dialog/welcome-dialog.component';
import { resolvePresetProfilePicture } from './core/data/preset-avatars';
import { AuthService } from './core/services/auth.service';
import { SocialGraphService } from './core/services/social-graph.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, WelcomeDialogComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component\.css',
})
export class AppComponent implements OnInit, OnDestroy {
  protected readonly auth = inject(AuthService);
  private readonly socialGraph = inject(SocialGraphService);
  private readonly router = inject(Router);
  private navSub?: Subscription;

  /** Menu mobile ouvert. */
  readonly navOpen = signal(false);
  /** Dropdown utilisateur ouvert (desktop/mobile). */
  readonly userMenuOpen = signal(false);

  /** Photo de profil dans la barre : repasse Ã  false si lâ€™URL change. */
  protected readonly navAvatarImgError = signal(false);
  readonly mentorshipPendingCount = signal(0);

  private readonly _resetNavAvatarOnAuthPictureChange = effect(() => {
    const a = this.auth.auth();
    const _ = `${a?.userId ?? ''}|${a?.profilePicture ?? ''}`;
    untracked(() => this.navAvatarImgError.set(false));
  });

  ngOnInit(): void {
    this.navSub = this.router.events.pipe(filter((e) => e instanceof NavigationEnd)).subscribe(() => {
      this.navOpen.set(false);
      this.refreshMentorshipBadge();
    });
    this.refreshMentorshipBadge();
  }

  ngOnDestroy(): void {
    this.navSub?.unsubscribe();
  }

  toggleNav(): void {
    this.userMenuOpen.set(false);
    this.navOpen.update((v) => !v);
  }

  closeNav(): void {
    this.navOpen.set(false);
    this.userMenuOpen.set(false);
  }

  toggleUserMenu(): void {
    this.userMenuOpen.update((v) => !v);
  }

  closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.userMenuOpen()) {
      return;
    }
    const target = event.target;
    if (!(target instanceof Element)) {
      this.closeUserMenu();
      return;
    }
    if (!target.closest('.app-nav__user-menu-wrap')) {
      this.closeUserMenu();
    }
  }

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    this.closeUserMenu();
  }

  logout(): void {
    this.auth.logout();
    this.mentorshipPendingCount.set(0);
  }

  protected refreshMentorshipBadge(): void {
    if (!this.auth.isLoggedIn()) {
      this.mentorshipPendingCount.set(0);
      return;
    }
    this.socialGraph.incomingMentorshipRequests().subscribe({
      next: (rows) => this.mentorshipPendingCount.set(rows.length),
      error: () => this.mentorshipPendingCount.set(0),
    });
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

