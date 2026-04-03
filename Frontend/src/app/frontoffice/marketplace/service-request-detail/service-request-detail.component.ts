import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ApplicationService } from '../../../core/services/application.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { Application, ApplicationStatus } from '../../../core/models/application.model';
import { CURRENT_USER_ID } from '../../../core/auth/current-user';

@Component({
  selector: 'app-service-request-detail',
  templateUrl: './service-request-detail.component.html'
})
export class ServiceRequestDetailComponent implements OnInit {
  serviceRequest?: ServiceRequest;
  applications: Application[] = [];
  loading = false;
  error = '';
  currentUserId = CURRENT_USER_ID;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private srService: ServiceRequestService,
    private appService: ApplicationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);
    this.loading = true;

    this.srService.getById(id).subscribe({
      next: (sr) => {
        // si ce n'est pas MA demande → rediriger
        if (sr.creator.id !== this.currentUserId) {
          this.router.navigate(['/marketplace']);
          return;
        }
        this.serviceRequest = sr;
        this.loadApplications(id);
      },
      error: () => {
        this.error = 'Demande introuvable.';
        this.loading = false;
      }
    });
  }

  loadApplications(srId: number): void {
    this.appService.getByServiceRequest(srId, this.currentUserId).subscribe({
      next: (apps) => {
        this.applications = apps;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement des candidatures.';
        this.loading = false;
      }
    });
  }

  updateStatus(appId: number, status: ApplicationStatus): void {
    this.appService.updateStatus(appId, this.currentUserId, status).subscribe({
      next: () => {
        const app = this.applications.find(a => a.id === appId);
        if (app) app.status = status;
        // si accepté → recharger la demande (son statut passe à CLOSED)
        if (status === 'ACCEPTED') {
          this.srService.getById(this.serviceRequest!.id).subscribe(sr => this.serviceRequest = sr);
        }
      },
      error: (err) => this.error = err?.error?.message || 'Erreur.'
    });
  }
}
