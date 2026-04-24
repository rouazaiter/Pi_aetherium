import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ServiceRequestListComponent } from './service-request-list/service-request-list.component';
import { ServiceRequestFormComponent } from './service-request-form/service-request-form.component';
import { ServiceRequestDetailComponent } from './service-request-detail/service-request-detail.component';
import { ApplyFormComponent } from './apply-form/apply-form.component';
import { LeaderboardComponent } from './leaderboard/leaderboard.component';

const routes: Routes = [
  // Main list (everyone)
  { path: '', component: ServiceRequestListComponent },

  // Create a new request
  { path: 'new', component: ServiceRequestFormComponent },

  // Edit MY request
  { path: 'edit/:id', component: ServiceRequestFormComponent },

  // Details of MY request + received applications (creator only)
  { path: 'my/:id', component: ServiceRequestDetailComponent },

  // Request details + application form (candidate only)
  { path: 'apply/:id', component: ApplyFormComponent },

  // Advanced leaderboard
  { path: 'leaderboard', component: LeaderboardComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MarketplaceRoutingModule {}
