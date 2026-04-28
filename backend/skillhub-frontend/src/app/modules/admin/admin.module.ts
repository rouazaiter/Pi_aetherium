import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { SharedModule } from '../../shared/shared.module';

import { AdminLayoutComponent } from './admin-layout.component';
import { AdminDashboardComponent } from './admin-dashboard.component';
import { AdminUsersComponent } from './admin-users.component';
import { AdminPostsComponent } from './admin-posts.component';
import { AdminDiscussionsComponent } from './admin-discussions.component';
import { AdminReportsComponent } from './admin-reports.component';
import { AdminKbComponent } from './admin-kb.component';

const routes: Routes = [
  {
    path: '',
    component: AdminLayoutComponent,
    children: [
      { path: '', component: AdminDashboardComponent },
      { path: 'users', component: AdminUsersComponent },
      { path: 'posts', component: AdminPostsComponent },
      { path: 'discussions', component: AdminDiscussionsComponent },
      { path: 'reports', component: AdminReportsComponent },
      { path: 'knowledge-base', component: AdminKbComponent },
    ]
  }
];

@NgModule({
  declarations: [
    AdminLayoutComponent,
    AdminDashboardComponent,
    AdminUsersComponent,
    AdminPostsComponent,
    AdminDiscussionsComponent,
    AdminReportsComponent,
    AdminKbComponent,
  ],
  imports: [SharedModule, RouterModule.forChild(routes)]
})
export class AdminModule {}
