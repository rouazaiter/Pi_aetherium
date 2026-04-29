import { Routes } from '@angular/router';
import { adminGuard } from './core/guards/admin.guard';
import { authGuard } from './core/guards/auth.guard';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './pages/reset-password/reset-password.component';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./pages/home/home.component').then((m) => m.HomeComponent),
  },
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

  // ── SkillHub Certification routes (isolated under /skillhub) ────────────
  // These load inside their own LayoutComponent (sidebar) — completely
  // separate from app.component's navbar/footer. No merge conflicts.
  {
    path: 'skillhub',
    loadComponent: () =>
      import('./components/certification/layout/layout.component').then(
        (m) => m.LayoutComponent
      ),
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./components/certification/dashboard/dashboard.component').then(
            (m) => m.DashboardComponent
          ),
      },
      {
        path: 'certifications',
        loadChildren: () =>
          import('./components/certification/certifications/certifications.module').then(
            (m) => m.CertificationsModule
          ),
      },
      {
        path: 'store',
        loadChildren: () =>
          import('./components/certification/store/store.module').then(
            (m) => m.StoreModule
          ),
      },
      {
        path: 'verify',
        loadChildren: () =>
          import('./components/certification/verify/verify.module').then(
            (m) => m.VerifyModule
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
