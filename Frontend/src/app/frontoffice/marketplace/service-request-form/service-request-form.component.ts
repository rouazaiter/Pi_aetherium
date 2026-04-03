import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';

// TODO: remplacer par l'id de l'utilisateur connecté (auth service)
const CURRENT_USER_ID = 1;

@Component({
  selector: 'app-service-request-form',
  templateUrl: './service-request-form.component.html'
})
export class ServiceRequestFormComponent implements OnInit {
  form!: FormGroup;
  selectedFile: File | null = null;
  loading = false;
  error = '';
  success = '';
  isEdit = false;
  requestId?: number;

  constructor(
    private fb: FormBuilder,
    private srService: ServiceRequestService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(100)]],
      description: ['', Validators.maxLength(2000)],
      expiringDate: [null]
    });

    this.requestId = this.route.snapshot.params['id'];
    if (this.requestId) {
      this.isEdit = true;
      this.srService.getById(this.requestId).subscribe({
        next: (sr) => {
          this.form.patchValue({
            name: sr.name,
            description: sr.description,
            expiringDate: sr.expiringDate ? sr.expiringDate.substring(0, 16) : null
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
    this.loading = true;
    this.error = '';
    this.success = '';

    const formData = new FormData();
    formData.append('name', this.form.value.name);
    if (this.form.value.description) formData.append('description', this.form.value.description);
    if (this.form.value.expiringDate) {
      // Spring @DateTimeFormat(iso = DATE_TIME) attend "yyyy-MM-ddTHH:mm:ss"
      const d = new Date(this.form.value.expiringDate);
      const formatted = d.toISOString().replace('Z', '');
      formData.append('expiringDate', formatted);
    }
    if (this.selectedFile) formData.append('file', this.selectedFile);

    const request$ = this.isEdit
      ? this.srService.update(this.requestId!, CURRENT_USER_ID, formData)
      : this.srService.create(CURRENT_USER_ID, formData);

    request$.subscribe({
      next: () => {
        this.success = this.isEdit ? 'Demande mise à jour.' : 'Demande publiée avec succès.';
        this.loading = false;
        setTimeout(() => this.router.navigate(['/marketplace']), 1500);
      },
      error: (err) => {
        this.error = err?.error?.message || 'Une erreur est survenue.';
        this.loading = false;
      }
    });
  }
}
