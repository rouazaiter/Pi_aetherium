import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { CertListComponent } from './cert-list/cert-list.component';
import { CertDetailComponent } from './cert-detail/cert-detail.component';
import { CertFormComponent } from './cert-form/cert-form.component';
import { AnalyticsComponent } from './analytics/analytics.component';

const routes: Routes = [
  { path: '',           component: CertListComponent },
  { path: 'analytics',  component: AnalyticsComponent },
  { path: 'create',     component: CertFormComponent },
  { path: ':id',        component: CertDetailComponent },
  { path: ':id/edit',   component: CertFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class CertificationsRoutingModule {}
