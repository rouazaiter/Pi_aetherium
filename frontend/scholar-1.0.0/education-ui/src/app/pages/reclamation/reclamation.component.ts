import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import type { ReclamationResponse, ReclamationStatus } from '../../core/models/api.models';
import { ReclamationService } from '../../core/services/reclamation.service';
import { messageFromHttpError } from '../../core/util/http-error';

@Component({
  selector: 'app-reclamation',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe, RouterLink],
  templateUrl: './reclamation.component.html',
  styleUrl: './reclamation.component.scss',
})
export class ReclamationComponent implements OnInit {
  private readonly api = inject(ReclamationService);
  private readonly fb = inject(FormBuilder);

  readonly submitting = signal(false);
  readonly loadError = signal<string | null>(null);
  readonly submitError = signal<string | null>(null);
  readonly submitOk = signal(false);
  readonly items = signal<ReclamationResponse[]>([]);

  readonly form = this.fb.nonNullable.group({
    subject: ['', [Validators.required, Validators.maxLength(255)]],
    description: ['', [Validators.required, Validators.maxLength(16000)]],
  });

  ngOnInit(): void {
    this.refreshList();
  }

  statusLabel(s: ReclamationStatus): string {
    switch (s) {
      case 'PENDING':
        return 'En attente';
      case 'IN_REVIEW':
        return 'En cours de traitement';
      case 'RESOLVED':
        return 'Clôturée';
      default:
        return s;
    }
  }

  refreshList(): void {
    this.loadError.set(null);
    this.api.mine().subscribe({
      next: (rows) => this.items.set(rows),
      error: (err) => this.loadError.set(messageFromHttpError(err, 'Impossible de charger vos réclamations.')),
    });
  }

  submit(): void {
    this.submitError.set(null);
    this.submitOk.set(false);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const v = this.form.getRawValue();
    this.submitting.set(true);
    this.api
      .create({ subject: v.subject.trim(), description: v.description.trim() })
      .pipe(finalize(() => this.submitting.set(false)))
      .subscribe({
        next: () => {
          this.submitOk.set(true);
          this.form.reset();
          this.refreshList();
        },
        error: (err) => this.submitError.set(messageFromHttpError(err, 'Envoi impossible.')),
      });
  }
}
