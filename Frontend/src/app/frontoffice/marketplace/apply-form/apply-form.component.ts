import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ApplicationService } from '../../../core/services/application.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { CurrentUserService } from '../../../core/auth/current-user.service';
import { MeetingSchedulerService } from '../../../core/services/meeting-scheduler.service';

@Component({
  selector: 'app-apply-form',
  templateUrl: './apply-form.component.html',
  styleUrls: ['./apply-form.component.css']
})
export class ApplyFormComponent implements OnInit {
  serviceRequest?: ServiceRequest;
  form!: FormGroup;
  loading = false;
  error = '';
  success = '';
  alreadyApplied = false;
  currentUserId = 0;
  availableSlots: string[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private srService: ServiceRequestService,
    private appService: ApplicationService,
    private currentUserService: CurrentUserService,
    private meetingSchedulerService: MeetingSchedulerService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);

    this.form = this.fb.group({
      message: ['', [Validators.required, Validators.maxLength(2000)]],
      meetingSlot: ['', Validators.required]
    });

    this.currentUserService.currentUser$.subscribe(user => {
      if (user.id <= 0) {
        return;
      }

      this.currentUserId = user.id;

      // Load the request
      this.srService.getById(id).subscribe({
        next: (sr) => {
          // If this is my own request, do not allow applying
          if (sr.creator.id === this.currentUserId) {
            this.router.navigate(['/marketplace']);
            return;
          }
          this.serviceRequest = sr;

          this.meetingSchedulerService.getConfig(sr.id).subscribe({
            next: (schedulingConfig) => {
              this.availableSlots = schedulingConfig.availableSlots ?? [];
            }
          });

          // Check whether user already applied
          this.appService.hasApplied(id, this.currentUserId).subscribe({
            next: (res) => this.alreadyApplied = res.hasApplied
          });
        },
        error: () => this.router.navigate(['/marketplace'])
      });
    });
  }

  onSubmit(): void {
    if (this.form.invalid || !this.serviceRequest) return;
    if (!this.validateMeetingChoice()) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.appService.apply(this.currentUserId, this.serviceRequest.id, this.form.value.message).subscribe({
      next: (application) => {
        this.meetingSchedulerService.reserveSlot(
          application.id,
          this.currentUserId,
          'SLOTS',
          this.form.value.meetingSlot
        ).subscribe({
          next: () => {
            this.success = 'Application submitted successfully.';
            this.alreadyApplied = true;
            this.loading = false;
          },
          error: (err) => {
            this.error = err?.error?.message || 'Application submitted, but meeting reservation failed.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.error = err?.error?.message || 'An error occurred while submitting your application.';
        this.loading = false;
      }
    });
  }

  hasSchedulingOptions(): boolean {
    return this.availableSlots.length > 0;
  }

  private validateMeetingChoice(): boolean {
    if (!this.availableSlots.length) {
      this.error = 'No available slots are configured for this request yet.';
      return false;
    }

    if (!this.form.value.meetingSlot) {
      this.error = 'Please choose one available meeting slot.';
      return false;
    }

    return true;
  }
}
