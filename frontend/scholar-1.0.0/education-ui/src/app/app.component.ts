import { Component, effect, HostListener, inject, OnDestroy, OnInit, signal, untracked } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, Subscription } from 'rxjs';
import { environment } from '../environments/environment';
import { WelcomeDialogComponent } from './components/welcome-dialog/welcome-dialog.component';
import { resolvePresetProfilePicture } from './core/data/preset-avatars';
import { AuthService } from './core/services/auth.service';
import { JihenPortfolioService } from './core/services/jihen-portfolio.service';
import { SocialGraphService } from './core/services/social-graph.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, WelcomeDialogComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
})
export class AppComponent implements OnInit, OnDestroy {
  protected readonly auth = inject(AuthService);
  private readonly socialGraph = inject(SocialGraphService);
  private readonly portfolioService = inject(JihenPortfolioService);
  private readonly router = inject(Router);
  private navSub?: Subscription;
  private portfolioRouteSub?: Subscription;
  private lastPortfolioOwnerId: number | null = null;

  /** Menu mobile ouvert. */
  readonly navOpen = signal(false);
  /** Dropdown portfolio ouvert (desktop/mobile). */
  readonly portfolioMenuOpen = signal(false);
  /** Dropdown utilisateur ouvert (desktop/mobile). */
  readonly userMenuOpen = signal(false);
  readonly myPortfolio3dLink = signal<string | null>(null);

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
      this.portfolioMenuOpen.set(false);
      this.userMenuOpen.set(false);
      this.refreshMentorshipBadge();
      this.refreshPortfolio3dLink();
    });
    this.refreshMentorshipBadge();
    this.refreshPortfolio3dLink();
  }

  ngOnDestroy(): void {
    this.navSub?.unsubscribe();
    this.portfolioRouteSub?.unsubscribe();
  }

  toggleNav(): void {
    this.portfolioMenuOpen.set(false);
    this.userMenuOpen.set(false);
    this.navOpen.update((v) => !v);
  }

  closeNav(): void {
    this.navOpen.set(false);
    this.portfolioMenuOpen.set(false);
    this.userMenuOpen.set(false);
  }

  togglePortfolioMenu(): void {
    this.userMenuOpen.set(false);
    this.portfolioMenuOpen.update((v) => !v);
  }

  closePortfolioMenu(): void {
    this.portfolioMenuOpen.set(false);
  }

  toggleUserMenu(): void {
    this.portfolioMenuOpen.set(false);
    this.userMenuOpen.update((v) => !v);
  }

  closeUserMenu(): void {
    this.userMenuOpen.set(false);
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.userMenuOpen() && !this.portfolioMenuOpen()) {
      return;
    }
    const target = event.target;
    if (!(target instanceof Element)) {
      this.closePortfolioMenu();
      this.closeUserMenu();
      return;
    }
    if (this.portfolioMenuOpen() && !target.closest('.app-nav__dropdown')) {
      this.closePortfolioMenu();
    }
    if (!target.closest('.app-nav__user-menu-wrap')) {
      this.closeUserMenu();
    }
  }

  @HostListener('document:keydown.escape')
  onEscapeKey(): void {
    this.closePortfolioMenu();
    this.closeUserMenu();
  }

  logout(): void {
    this.auth.logout();
    this.mentorshipPendingCount.set(0);
    this.lastPortfolioOwnerId = null;
    this.myPortfolio3dLink.set(null);
  }

  protected isPortfolioMenuActive(): boolean {
    const url = this.router.url;
    return url.startsWith('/jihen-portfolio') || url.startsWith('/portfolio-mentor') || url.startsWith('/explore') || url.startsWith('/cv');
  }

  protected platformName(): string {
    return 'SkillHub';
  }

  protected isImmersiveRoute(): boolean {
    return this.router.url.startsWith('/jihen-portfolio-3d/') || this.router.url.startsWith('/cv');
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

  private refreshPortfolio3dLink(): void {
    if (!this.auth.isLoggedIn()) {
      this.portfolioRouteSub?.unsubscribe();
      this.lastPortfolioOwnerId = null;
      this.myPortfolio3dLink.set(null);
      return;
    }

    const ownerId = this.auth.auth()?.userId ?? null;
    if (ownerId && ownerId === this.lastPortfolioOwnerId && this.myPortfolio3dLink()) {
      return;
    }

    this.portfolioRouteSub?.unsubscribe();
    this.lastPortfolioOwnerId = ownerId;
    this.portfolioRouteSub = this.portfolioService.getMyPortfolio().subscribe({
      next: (response) => {
        const portfolioId = response.portfolio?.id;
        this.myPortfolio3dLink.set(portfolioId ? `/jihen-portfolio-3d/${portfolioId}` : null);
      },
      error: () => {
        this.myPortfolio3dLink.set(null);
      },
    });
  }
}

