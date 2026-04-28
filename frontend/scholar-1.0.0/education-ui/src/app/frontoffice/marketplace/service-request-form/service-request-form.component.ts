import { Component, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { CurrentUserService } from '../../../core/auth/current-user.service';
import { MeetingSchedulerService } from '../../../core/services/meeting-scheduler.service';

@Component({
  selector: 'app-service-request-form',
  templateUrl: './service-request-form.component.html',
  styleUrls: ['./service-request-form.component.scss']
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
      duration: [null, [Validators.required, Validators.min(1)]],
      expiringDate: [null, Validators.required],
      calendlyLink: ['', [Validators.required, Validators.maxLength(300), Validators.pattern(/^https?:\/\/.+/i)]],
      availableSlots: this.fb.array([this.fb.control('')])
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
            duration: null,
            expiringDate: sr.expiringDate ? this.formatDateTimeForInput(sr.expiringDate) : null,
            calendlyLink: ''
          });
          this.existingFileUrl = sr.files || '';

          this.meetingSchedulerService.getConfig(sr.id).subscribe({
            next: (schedulingConfig) => {
              const slots = schedulingConfig.availableSlots ?? [];
              const slotControls = slots.length
                ? slots.map(slot => this.fb.control(this.formatDateTimeForInput(slot)))
                : [this.fb.control('')];
              this.form.setControl('availableSlots', this.fb.array(slotControls));
              this.form.patchValue({
                duration: schedulingConfig.durationMinutes ?? null,
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

    if (!this.validateExpirationDate()) {
      return;
    }

    this.loading = true;
    this.error = '';
    this.success = '';

    const formData = new FormData();
    const parsedDate = this.parseDateTime(this.form.value.expiringDate);
    if (!parsedDate) {
      this.error = 'Invalid expiration date. Use YYYY-MM-DD HH:mm or YYYY-MM-DDTHH:mm format.';
      this.loading = false;
      return;
    }

    const formatted = parsedDate.toISOString().replace('Z', '');

    const payload = {
      name: this.form.value.name,
      category: this.form.value.category,
      description: this.form.value.description,
      expiringDate: formatted,
      price: Number(this.form.value.price),
      durationMinutes: Number(this.form.value.duration)
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
          Number(this.form.value.duration),
          this.parseAvailableSlots()
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

  private validateExpirationDate(): boolean {
    const expiringDateRaw = this.form.value.expiringDate;
    if (!expiringDateRaw) {
      this.error = 'Expiration date is required.';
      return false;
    }

    const expirationDate = this.parseDateTime(expiringDateRaw);
    if (!expirationDate) {
      this.error = 'Invalid expiration date. Use YYYY-MM-DD HH:mm or YYYY-MM-DDTHH:mm format.';
      return false;
    }

    return true;
  }

  private parseDateTime(value: string): Date | null {
    if (!value) {
      return null;
    }

    const normalized = value.trim().replace('T', ' ');
    const match = normalized.match(/^([0-9]{4})-([0-9]{2})-([0-9]{2})\s+([0-9]{2}):([0-9]{2})(?::([0-9]{2}))?$/);
    if (!match) {
      return null;
    }

    const year = Number(match[1]);
    const month = Number(match[2]) - 1;
    const day = Number(match[3]);
    const hour = Number(match[4]);
    const minute = Number(match[5]);
    const second = Number(match[6] ?? '0');

    const date = new Date(year, month, day, hour, minute, second);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private formatDateTimeForInput(value: string): string {
    const normalized = value.trim().replace('T', ' ');
    const match = normalized.match(/^([0-9]{4}-[0-9]{2}-[0-9]{2})\s+([0-9]{2}:[0-9]{2})/);
    return match ? `${match[1]} ${match[2]}` : normalized;
  }

  get availableSlotsFormArray(): FormArray {
    return this.form.get('availableSlots') as FormArray;
  }

  addAvailableSlot(): void {
    this.availableSlotsFormArray.push(this.fb.control(''));
  }

  removeAvailableSlot(index: number): void {
    if (this.availableSlotsFormArray.length > 1) {
      this.availableSlotsFormArray.removeAt(index);
    } else {
      this.availableSlotsFormArray.at(0).setValue('');
    }
  }

  private parseAvailableSlots(): string[] {
    return this.availableSlotsFormArray.controls
      .map(control => (control.value || '').toString().trim().replace('T', ' '))
      .filter(value => value.length > 0);
  }
}
