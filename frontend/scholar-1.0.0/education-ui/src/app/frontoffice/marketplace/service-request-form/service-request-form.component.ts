import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { CurrentUserService } from '../../../core/auth/current-user.service';
import { MeetingSchedulerService } from '../../../core/services/meeting-scheduler.service';

@Component({
  selector: 'app-service-request-form',
  templateUrl: './service-request-form.component.html',
  styleUrls: ['./service-request-form.component.css']
})
export class ServiceRequestFormComponent implements OnInit {
  readonly categories = [
    'Software Development',
    'Networks and Systems',
    'Cybersecurity',
    'Data / Artificial Intelligence',
    'Cloud Computing'
  ];

  form!: FormGroup;
  selectedFile: File | null = null;
  existingFileUrl = '';
  slotInput = '';
  selectedSlots: string[] = [];
  loading = false;
  error = '';
  success = '';
  isEdit = false;
  requestId?: number;
  currentUserId = 0;

  constructor(
    private fb: FormBuilder,
    private srService: ServiceRequestService,
    private route: ActivatedRoute,
    private router: Router,
    private currentUserService: CurrentUserService,
    private meetingSchedulerService: MeetingSchedulerService
  ) {}

  ngOnInit(): void {
    this.currentUserService.currentUser$.subscribe(user => {
      if (user.id <= 0) {
        return;
      }
      this.currentUserId = user.id;
    });

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      category: ['', Validators.required],
      description: ['', [Validators.required, Validators.maxLength(2000)]],
      price: [null, [Validators.required, Validators.min(1)]],
      expiringDate: [null, Validators.required],
      calendlyLink: ['', [Validators.required, Validators.maxLength(300), Validators.pattern(/^https?:\/\/.+/i)]]
    });

    this.requestId = this.route.snapshot.params['id'];
    if (this.requestId) {
      this.isEdit = true;
      this.srService.getById(this.requestId, this.currentUserId).subscribe({
        next: (sr) => {
          this.form.patchValue({
            name: sr.name,
            category: sr.category,
            description: sr.description,
            price: sr.price,
            expiringDate: sr.expiringDate ? sr.expiringDate.substring(0, 16) : null,
            calendlyLink: ''
          });
          this.existingFileUrl = sr.files || '';

          this.meetingSchedulerService.getConfig(sr.id).subscribe({
            next: (schedulingConfig) => {
              this.selectedSlots = (schedulingConfig.availableSlots ?? []).slice();
              this.form.patchValue({
                calendlyLink: schedulingConfig.calendlyLink ?? ''
              });
            }
          });
        }
      });
    }
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    if (!this.selectedFile && !this.existingFileUrl) {
      this.error = 'Please upload a file. This field is required.';
      return;
    }

    if (this.selectedSlots.length === 0) {
      this.error = 'Please add at least one available meeting slot from the calendar.';
      return;
    }

    if (!this.validateExpirationAgainstSlots()) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    const formData = new FormData();
    const d = new Date(this.form.value.expiringDate);
    const formatted = d.toISOString().replace('Z', '');

    const payload = {
      name: this.form.value.name,
      category: this.form.value.category,
      description: this.form.value.description,
      expiringDate: formatted,
      price: Number(this.form.value.price)
    };

    formData.append('payload', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
    if (this.selectedFile) {
      formData.append('file', this.selectedFile);
    }

    const request$ = this.isEdit
      ? this.srService.update(this.requestId!, this.currentUserId, formData)
      : this.srService.create(this.currentUserId, formData);

    request$.subscribe({
      next: (savedRequest) => {
        const targetRequestId = this.isEdit ? this.requestId! : savedRequest.id;
        this.meetingSchedulerService.saveConfig(
          targetRequestId,
          this.currentUserId,
          this.form.value.calendlyLink || '',
          this.selectedSlots
        ).subscribe({
          next: () => {
            if (this.isEdit) {
              this.success = 'Request updated.';
              this.loading = false;
              this.router.navigate(['/marketplace']);
              return;
            }

            this.success = 'Request published successfully.';
            this.loading = false;
            this.router.navigate(['/marketplace']);
          },
          error: (err) => {
            this.error = err?.error?.message || 'Request saved, but meeting configuration failed.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.error = err?.error?.message || 'An error occurred.';
        this.loading = false;
      }
    });
  }

  addSlotFromCalendar(): void {
    const rawSlot = (this.slotInput || '').trim();
    if (!rawSlot) {
      return;
    }

    const normalized = this.normalizeSlot(rawSlot);
    if (!this.selectedSlots.includes(normalized)) {
      this.selectedSlots = [...this.selectedSlots, normalized].sort((a, b) => a.localeCompare(b));
    }

    this.slotInput = '';
  }

  onSlotInputChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.slotInput = input.value ?? '';
  }

  removeSlot(slot: string): void {
    this.selectedSlots = this.selectedSlots.filter(item => item !== slot);
  }

  private normalizeSlot(rawSlot: string): string {
    // Keep a consistent human-readable format while preserving local date/time.
    return rawSlot.replace('T', ' ');
  }

  private validateExpirationAgainstSlots(): boolean {
    const expiringDateRaw = this.form.value.expiringDate;
    if (!expiringDateRaw) {
      this.error = 'Expiration date is required.';
      return false;
    }

    const expirationDate = new Date(expiringDateRaw);
    if (Number.isNaN(expirationDate.getTime())) {
      this.error = 'Invalid expiration date.';
      return false;
    }

    for (const slot of this.selectedSlots) {
      const slotDate = new Date(slot.replace(' ', 'T'));
      if (Number.isNaN(slotDate.getTime())) {
        this.error = `Invalid slot format: ${slot}`;
        return false;
      }

      if (expirationDate <= slotDate) {
        this.error = 'Expiration date must be later than all selected slots.';
        return false;
      }
    }

    return true;
  }
}
