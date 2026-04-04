import { Component, OnInit } from '@angular/core';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { CurrentUserService } from '../../../core/auth/current-user.service';

@Component({
  selector: 'app-service-request-list',
  templateUrl: './service-request-list.component.html'
})
export class ServiceRequestListComponent implements OnInit {
  myRequests: ServiceRequest[] = [];
  otherRequests: ServiceRequest[] = [];
  filteredRequests: ServiceRequest[] = [];
  activeFilter: string = 'All';
  loading = false;
  error = '';
  currentUserId = 1;

  constructor(
    private srService: ServiceRequestService,
    private currentUserService: CurrentUserService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.currentUserService.currentUser.id;
    this.currentUserService.currentUser$.subscribe(user => {
      this.currentUserId = user.id;
      this.load();
    });
    this.load();
  }

  load(): void {
    this.loading = true;
    this.srService.getAll().subscribe({
      next: (data) => {
        this.myRequests = data.filter(r => r.creator.id === this.currentUserId);
        this.otherRequests = data.filter(r => r.creator.id !== this.currentUserId);
        this.filteredRequests = this.otherRequests;
        this.loading = false;
      },
      error: () => {
        this.error = 'Error loading requests.';
        this.loading = false;
      }
    });
  }

  filterBy(status: string): void {
    this.activeFilter = status;
    if (status === 'All') {
      this.filteredRequests = this.otherRequests;
    } else if (status === 'CLOSED') {
      this.filteredRequests = this.otherRequests.filter(r => r.status === 'CLOSED' || r.status === 'EXPIRED');
    } else {
      this.filteredRequests = this.otherRequests.filter(r => r.status === status);
    }
  }
}
