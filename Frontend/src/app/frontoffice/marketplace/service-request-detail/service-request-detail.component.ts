import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ApplicationService } from '../../../core/services/application.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { Application, ApplicationStatus } from '../../../core/models/application.model';
import { CurrentUserService } from '../../../core/auth/current-user.service';
import { MeetingReservation, MeetingSchedulerService } from '../../../core/services/meeting-scheduler.service';
import { catchError, forkJoin, map, of } from 'rxjs';

@Component({
  selector: 'app-service-request-detail',
  templateUrl: './service-request-detail.component.html',
  styleUrls: ['./service-request-detail.component.css']
})
export class ServiceRequestDetailComponent implements OnInit {
  serviceRequest?: ServiceRequest;
  applications: Application[] = [];
  loading = false;
  error = '';
  currentUserId = 0;
  meetingByApplication: Record<number, MeetingReservation> = {};

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private srService: ServiceRequestService,
    private appService: ApplicationService,
    private currentUserService: CurrentUserService,
    private meetingSchedulerService: MeetingSchedulerService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);

    this.currentUserService.currentUser$.subscribe(user => {
      if (user.id <= 0) {
        return;
      }

      this.currentUserId = user.id;
      this.loading = true;

      this.srService.getById(id).subscribe({
        next: (sr) => {
          // If this is not my request, redirect
          if (sr.creator.id !== this.currentUserId) {
            this.router.navigate(['/marketplace']);
            return;
          }
          this.serviceRequest = sr;
          this.loadApplications(id);
        },
        error: () => {
          this.error = 'Request not found.';
          this.loading = false;
        }
      });
    });
  }

  loadApplications(srId: number): void {
    this.appService.getByServiceRequest(srId, this.currentUserId).subscribe({
      next: (apps) => {
        this.applications = apps;
        this.reloadMeetingReservations();
        this.loading = false;
      },
      error: () => {
        this.error = 'Error loading applications.';
        this.loading = false;
      }
    });
  }

  updateStatus(appId: number, status: ApplicationStatus): void {
    this.appService.updateStatus(appId, this.currentUserId, status).subscribe({
      next: () => {
        const app = this.applications.find(a => a.id === appId);
        if (app) app.status = status;
        // If accepted, reload the request (its status becomes CLOSED)
        if (status === 'ACCEPTED') {
          this.srService.getById(this.serviceRequest!.id).subscribe(sr => this.serviceRequest = sr);
        }
      },
      error: (err) => this.error = err?.error?.message || 'Error.'
    });
  }

  getMeetingReservation(applicationId: number): MeetingReservation | null {
    return this.meetingByApplication[applicationId] ?? null;
  }

  confirmMeeting(applicationId: number): void {
    this.meetingSchedulerService.updateBookingStatus(applicationId, this.currentUserId, 'CONFIRMED').subscribe({
      next: (reservation) => {
        this.meetingByApplication[applicationId] = reservation;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Unable to confirm meeting.';
      }
    });
  }

  declineMeeting(applicationId: number): void {
    this.meetingSchedulerService.updateBookingStatus(applicationId, this.currentUserId, 'DECLINED').subscribe({
      next: (reservation) => {
        this.meetingByApplication[applicationId] = reservation;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Unable to decline meeting.';
      }
    });
  }

  private reloadMeetingReservations(): void {
    if (this.applications.length === 0) {
      this.meetingByApplication = {};
      return;
    }

    forkJoin(
      this.applications.map(app =>
        this.meetingSchedulerService.getBookingByApplication(app.id, this.currentUserId).pipe(
          map(reservation => ({ applicationId: app.id, reservation })),
          catchError(() => of({ applicationId: app.id, reservation: null }))
        )
      )
    ).subscribe(results => {
      const mapping: Record<number, MeetingReservation> = {};
      results.forEach(item => {
        if (item.reservation) {
          mapping[item.applicationId] = item.reservation;
        }
      });
      this.meetingByApplication = mapping;
    });
  }
}
