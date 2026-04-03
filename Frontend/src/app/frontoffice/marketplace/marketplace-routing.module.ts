import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ServiceRequestListComponent } from './service-request-list/service-request-list.component';
import { ServiceRequestFormComponent } from './service-request-form/service-request-form.component';
import { ServiceRequestDetailComponent } from './service-request-detail/service-request-detail.component';
import { ApplyFormComponent } from './apply-form/apply-form.component';

const routes: Routes = [
  // liste principale (tout le monde)
  { path: '', component: ServiceRequestListComponent },

  // créer une nouvelle demande
  { path: 'new', component: ServiceRequestFormComponent },

  // modifier MA demande
  { path: 'edit/:id', component: ServiceRequestFormComponent },

  // détails de MA demande + candidatures reçues (créateur uniquement)
  { path: 'my/:id', component: ServiceRequestDetailComponent },

  // détails d'une demande + formulaire candidature (candidat uniquement)
  { path: 'apply/:id', component: ApplyFormComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class MarketplaceRoutingModule {}
