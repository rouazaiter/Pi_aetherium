import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CertificationsRoutingModule } from './certifications-routing.module';
import { CertListComponent } from './cert-list/cert-list.component';
import { CertDetailComponent } from './cert-detail/cert-detail.component';
import { CertFormComponent } from './cert-form/cert-form.component';
import { AnalyticsComponent } from './analytics/analytics.component';


@NgModule({
    imports: [CommonModule, FormsModule, CertificationsRoutingModule, CertListComponent, CertDetailComponent, CertFormComponent, AnalyticsComponent]
})
export class CertificationsModule {}
