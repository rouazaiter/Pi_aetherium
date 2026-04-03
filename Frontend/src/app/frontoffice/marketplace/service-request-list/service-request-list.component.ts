import { Component, OnInit } from '@angular/core';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { CURRENT_USER_ID } from '../../../core/auth/current-user';

@Component({
  selector: 'app-service-request-list',
  templateUrl: './service-request-list.component.html'
})
export class ServiceRequestListComponent implements OnInit {
  myRequests: ServiceRequest[] = [];    // demandes que J'ai créées
  otherRequests: ServiceRequest[] = []; // demandes des autres
  loading = false;
  error = '';
  currentUserId = CURRENT_USER_ID;

  constructor(private srService: ServiceRequestService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.srService.getAll().subscribe({
      next: (data) => {
        // séparation selon le créateur
        this.myRequests = data.filter(r => r.creator.id === this.currentUserId);
        this.otherRequests = data.filter(r => r.creator.id !== this.currentUserId);
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement.';
        this.loading = false;
      }
    });
  }
}
