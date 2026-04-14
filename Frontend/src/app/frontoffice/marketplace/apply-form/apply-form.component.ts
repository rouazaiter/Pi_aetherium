import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
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
  currentUserId = 1;
  calendlyLink = '';
  availableSlots: string[] = [];
  calendlyEmbedUrl?: SafeResourceUrl;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private sanitizer: DomSanitizer,
    private srService: ServiceRequestService,
    private appService: ApplicationService,
    private currentUserService: CurrentUserService,
    private meetingSchedulerService: MeetingSchedulerService
  ) {}

  ngOnInit(): void {
    this.currentUserId = this.currentUserService.currentUser.id;
    this.currentUserService.currentUser$.subscribe(user => this.currentUserId = user.id);

    const id = Number(this.route.snapshot.params['id']);

    this.form = this.fb.group({
      message: ['', [Validators.required, Validators.maxLength(2000)]],
      meetingMode: [''],
      hasCalendlyAccount: [''],
      meetingSlot: [''],
      calendlySelectionNote: ['', Validators.maxLength(500)],
      calendlyEventUrl: ['', Validators.maxLength(500)]
    });

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
            this.calendlyLink = schedulingConfig.calendlyLink ?? '';
            this.availableSlots = schedulingConfig.availableSlots ?? [];

            if (this.calendlyLink) {
              this.calendlyEmbedUrl = this.sanitizer.bypassSecurityTrustResourceUrl(
                this.buildCalendlyEmbedUrl(this.calendlyLink)
              );
            }

            if (this.availableSlots.length > 0) {
              this.form.patchValue({ meetingMode: 'SLOTS' });
            } else if (this.calendlyLink) {
              this.form.patchValue({ meetingMode: 'CALENDLY' });
            }
          }
        });

        // Check whether user already applied
        this.appService.hasApplied(id, this.currentUserId).subscribe({
          next: (res) => this.alreadyApplied = res.hasApplied
        });
      },
      error: () => this.router.navigate(['/marketplace'])
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
        const mode = this.form.value.meetingMode;
        if (!this.hasSchedulingOptions()) {
          this.success = 'Application submitted successfully.';
          this.alreadyApplied = true;
          this.loading = false;
          return;
        }

        const slot = mode === 'SLOTS' ? this.form.value.meetingSlot : this.form.value.calendlySelectionNote;
        const eventUrl = mode === 'CALENDLY' ? this.form.value.calendlyEventUrl : undefined;

        this.meetingSchedulerService.reserveSlot(application.id, this.currentUserId, mode, slot, eventUrl).subscribe({
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
    return this.availableSlots.length > 0 || !!this.calendlyLink;
  }

  isMeetingMode(mode: 'SLOTS' | 'CALENDLY'): boolean {
    return this.form.value.meetingMode === mode;
  }

  setMeetingMode(mode: 'SLOTS' | 'CALENDLY'): void {
    this.form.patchValue({ meetingMode: mode });
  }

  setCalendlyAccountReady(isReady: boolean): void {
    this.form.patchValue({ hasCalendlyAccount: isReady ? 'YES' : 'NO' });
  }

  private validateMeetingChoice(): boolean {
    if (!this.hasSchedulingOptions()) {
      return true;
    }

    const mode = this.form.value.meetingMode;
    if (!mode) {
      this.error = 'Please choose a meeting method.';
      return false;
    }

    if (mode === 'SLOTS') {
      if (!this.form.value.meetingSlot) {
        this.error = 'Please choose one available meeting slot.';
        return false;
      }
      return true;
    }

    if (!this.calendlyLink) {
      this.error = 'Calendly is not configured for this request.';
      return false;
    }

    if (this.form.value.hasCalendlyAccount !== 'YES') {
      this.error = 'Create your Calendly account first, then come back and confirm you are ready.';
      return false;
    }

    if (!this.form.value.calendlySelectionNote?.trim()) {
      this.error = 'After picking a date in Calendly, add the selected date/time note.';
      return false;
    }

    return true;
  }

  private buildCalendlyEmbedUrl(rawUrl: string): string {
    const cleanUrl = rawUrl.trim();
    const separator = cleanUrl.includes('?') ? '&' : '?';
    return `${cleanUrl}${separator}hide_landing_page_details=1&hide_gdpr_banner=1`;
  }
}
