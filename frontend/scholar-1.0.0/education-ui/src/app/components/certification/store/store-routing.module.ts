import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StoreCatalogComponent } from './store-catalog/store-catalog.component';
import { StoreDetailComponent } from './store-detail/store-detail.component';
import { StoreExamComponent } from './store-exam/store-exam.component';
import { StoreResultComponent } from './store-result/store-result.component';
import { CheckoutComponent } from './checkout/checkout.component';
import { MockExamComponent } from './mock-exam/mock-exam.component';
import { MyEnrollmentsComponent } from './my-enrollments/my-enrollments.component';

const routes: Routes = [
  { path: '',              component: StoreCatalogComponent },
  { path: 'my',           component: MyEnrollmentsComponent },
  { path: ':id',           component: StoreDetailComponent },
  { path: ':id/checkout',  component: CheckoutComponent },
  { path: ':id/practice',  component: MockExamComponent },
  { path: ':id/exam',      component: StoreExamComponent },
  { path: ':id/result',    component: StoreResultComponent }
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule]
})
export class StoreRoutingModule {}
