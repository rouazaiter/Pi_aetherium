import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./modules/home/home.component').then(m => m.HomeComponent)
  },
  {
    path: 'blog',
    loadComponent: () => import('./modules/blog/blog.component').then(m => m.BlogComponent)
  },
  {
    path: 'discussions',
    loadComponent: () => import('./modules/discussions/discussions.component').then(m => m.DiscussionsComponent)
  },
  {
    path: 'library',
    loadComponent: () => import('./modules/library/library.component').then(m => m.LibraryComponent)
  },
  {
    path: 'admin',
    loadComponent: () => import('./modules/admin/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { path: '', loadComponent: () => import('./modules/admin/admin-dashboard.component').then(m => m.AdminDashboardComponent) },
      { path: 'users', loadComponent: () => import('./modules/admin/admin-users.component').then(m => m.AdminUsersComponent) },
      { path: 'posts', loadComponent: () => import('./modules/admin/admin-posts.component').then(m => m.AdminPostsComponent) },
      { path: 'discussions', loadComponent: () => import('./modules/admin/admin-discussions.component').then(m => m.AdminDiscussionsComponent) },
      { path: 'reports', loadComponent: () => import('./modules/admin/admin-reports.component').then(m => m.AdminReportsComponent) },
      { path: 'knowledge-base', loadComponent: () => import('./modules/admin/admin-kb.component').then(m => m.AdminKbComponent) },
    ]
  },
  {
    path: 'knowledge-base',
    loadComponent: () => import('./modules/knowledge-base/knowledge-base.component').then(m => m.KnowledgeBaseComponent)
  },
  { path: '**', redirectTo: '' }
];
