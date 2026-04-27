import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { ForgotPasswordComponent } from './pages/forgot-password/forgot-password.component';
import { JihenCvComponent } from './pages/jihen-cv/jihen-cv.component';
import { JihenPortfolioComponent } from './pages/jihen-portfolio/jihen-portfolio.component';
import { PortfolioMentorComponent } from './pages/portfolio-mentor/portfolio-mentor.component';
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
  { path: '**', redirectTo: '' },
];
