import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { MarketplaceRoutingModule } from './marketplace-routing.module';
import { ServiceRequestListComponent } from './service-request-list/service-request-list.component';
import { ServiceRequestFormComponent } from './service-request-form/service-request-form.component';
import { ServiceRequestDetailComponent } from './service-request-detail/service-request-detail.component';
import { ApplyFormComponent } from './apply-form/apply-form.component';
import { LeaderboardComponent } from './leaderboard/leaderboard.component';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
  declarations: [
    ServiceRequestListComponent,
    ServiceRequestFormComponent,
    ServiceRequestDetailComponent,
    ApplyFormComponent,
    LeaderboardComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MarketplaceRoutingModule,
    SharedModule
  ]
})
export class MarketplaceModule {}
