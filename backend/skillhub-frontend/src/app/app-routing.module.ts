import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadChildren: () => import('./modules/home/home.module').then(m => m.HomeModule)
  },
  {
    path: 'blog',
    loadChildren: () => import('./modules/blog/blog.module').then(m => m.BlogModule)
  },
  {
    path: 'discussions',
    loadChildren: () => import('./modules/discussions/discussions.module').then(m => m.DiscussionsModule)
  },
  {
    path: 'library',
    loadChildren: () => import('./modules/library/library.module').then(m => m.LibraryModule)
  },
  {
    path: 'admin',
    loadChildren: () => import('./modules/admin/admin.module').then(m => m.AdminModule)
  },
  {
    path: 'knowledge-base',
    loadChildren: () => import('./modules/knowledge-base/knowledge-base.module').then(m => m.KnowledgeBaseModule)
  },
  { path: '**', redirectTo: '' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {}
