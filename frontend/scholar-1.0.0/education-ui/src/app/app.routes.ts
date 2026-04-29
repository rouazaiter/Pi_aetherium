import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { frontofficeGuard } from './core/guards/frontoffice.guard';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { JihenCvComponent } from './pages/jihen-cv/jihen-cv.component';
import { JihenPortfolioComponent } from './pages/jihen-portfolio/jihen-portfolio.component';
import { PortfolioMentorComponent } from './pages/portfolio-mentor/portfolio-mentor.component';
import { ResetPasswordComponent } from './pages/reset-password/reset-password.component';
import { RoomListComponent } from './components/room-session/room-list/room-list.component';
import { RoomSessionComponent } from './components/room-session/room-session/room-session.component';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
  { path: 'rooms', component: RoomListComponent },
  { path: 'rooms/:roomId', component: RoomSessionComponent },
  {
    path: 'login',
    loadComponent: () => import('./pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./pages/register/register.component').then((m) => m.RegisterComponent),
  },
  {
    path: 'forgot-password',
    component: ForgotPasswordComponent,
  },
  {
    path: 'reset-password',
    component: ResetPasswordComponent,
  },
  {
    path: 'profile',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/profile/profile.component').then((m) => m.ProfileComponent),
  },
  {
    path: 'subscriptions',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/subscriptions/subscriptions.component').then((m) => m.SubscriptionsComponent),
  },
  {
    path: 'friends',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/friends/friends.component').then((m) => m.FriendsComponent),
  },
  {
    path: 'social-hub',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/social-hub/social-hub.component').then((m) => m.SocialHubComponent),
  },
  {
    path: 'reclamation',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/reclamation/reclamation.component').then((m) => m.ReclamationComponent),
  },
  {
    path: 'admin/reclamations',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./pages/admin-reclamations/admin-reclamations.component').then((m) => m.AdminReclamationsComponent),
  },
  {
    path: 'explore',
    canActivate: [authGuard],
    loadComponent: () => import('./pages/explore/explore.component').then((m) => m.ExploreComponent),
  },
  {
    path: 'explore/portfolios/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/explore-portfolio-detail/explore-portfolio-detail.component').then((m) => m.ExplorePortfolioDetailComponent),
  },
  {
    path: 'explore/projects/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/explore-project-detail/explore-project-detail.component').then((m) => m.ExploreProjectDetailComponent),
  },
  {
    path: 'explore/collections/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/explore-collection-detail/explore-collection-detail.component').then((m) => m.ExploreCollectionDetailComponent),
  },
  {
    path: 'jihen-portfolio-3d/:id',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/jihen-portfolio-3d/jihen-portfolio-3d.component').then((m) => m.JihenPortfolio3dComponent),
  },
  {
    path: 'jihen-portfolio',
    canActivate: [authGuard],
    component: JihenPortfolioComponent,
  },
  {
    path: 'portfolio-mentor',
    canActivate: [authGuard],
    component: PortfolioMentorComponent,
  },
  {
    path: 'cv',
    canActivate: [authGuard],
    component: JihenCvComponent,
  },
  {
    path: 'admin-jihen-portfolio',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./pages/jihen-portfolio-admin/jihen-portfolio-admin.component').then((m) => m.JihenPortfolioAdminComponent),
  },

  // ── SkillHub Certification routes (isolated under /skillhub) ────────────
  {
    path: 'skillhub',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./components/certification/layout/layout.component').then(
        (m) => m.LayoutComponent
      ),
    children: [
      // Default: users → store, admins → dashboard (frontofficeGuard handles the redirect)
      { path: '', redirectTo: 'store', pathMatch: 'full' },

      // ── Frontoffice — regular users only (admins are redirected to /skillhub/dashboard) ──
      {
        path: 'store',
        canActivate: [frontofficeGuard],
        loadChildren: () =>
          import('./components/certification/store/store.module').then(
            (m) => m.StoreModule
          ),
      },
      {
        path: 'verify',
        canActivate: [frontofficeGuard],
        loadChildren: () =>
          import('./components/certification/verify/verify.module').then(
            (m) => m.VerifyModule
          ),
      },

      // ── Backoffice — admin only ───────────────────────────────────────
      {
        path: 'dashboard',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./components/certification/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'certifications',
        canActivate: [adminGuard],
        loadChildren: () =>
          import('./components/certification/certifications/certifications.module').then(
            (m) => m.CertificationsModule
          ),
      },
    ],
  },
  // Showcase is public (no sidebar) — stays at root level
  {
    path: 'showcase',
    loadChildren: () =>
      import('./components/certification/showcase/showcase.module').then(
        (m) => m.ShowcaseModule
      ),
  },

  { path: '**', redirectTo: '' },
];
