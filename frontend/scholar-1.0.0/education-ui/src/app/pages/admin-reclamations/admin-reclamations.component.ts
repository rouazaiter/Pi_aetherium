import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import type { ReclamationResponse, ReclamationStatus } from '../../core/models/api.models';
import { ReclamationService } from '../../core/services/reclamation.service';
import { messageFromHttpError } from '../../core/util/http-error';

type RowDraft = {
  status: 'IN_REVIEW' | 'RESOLVED';
  adminResponse: string;
  saving: boolean;
  error: string | null;
};

@Component({
  selector: 'app-admin-reclamations',
  standalone: true,
  imports: [DatePipe, FormsModule],
  templateUrl: './admin-reclamations.component.html',
  styleUrl: './admin-reclamations.component.scss',
})
export class AdminReclamationsComponent implements OnInit {
  private readonly api = inject(ReclamationService);

  readonly items = signal<ReclamationResponse[]>([]);
  readonly loadError = signal<string | null>(null);
  /** id -> draft */
  readonly drafts = signal<Record<number, RowDraft>>({});

  ngOnInit(): void {
    this.reload();
  }

  statusLabel(s: ReclamationStatus): string {
    switch (s) {
      case 'PENDING':
        return 'En attente';
      case 'IN_REVIEW':
        return 'En traitement';
      case 'RESOLVED':
        return 'Clôturée';
      default:
        return s;
    }
  }

  patchDraft(id: number, patch: Partial<RowDraft>): void {
    const cur = this.drafts()[id];
    if (!cur) {
      return;
    }
    this.drafts.set({ ...this.drafts(), [id]: { ...cur, ...patch } });
  }

  reload(): void {
    this.loadError.set(null);
    this.api.adminList().subscribe({
      next: (rows) => {
        this.items.set(rows);
        const nextDrafts: Record<number, RowDraft> = {};
        for (const r of rows) {
          nextDrafts[r.id] = {
            status: r.status === 'RESOLVED' ? 'RESOLVED' : 'IN_REVIEW',
            adminResponse: r.adminResponse ?? '',
            saving: false,
            error: null,
          };
        }
        this.drafts.set(nextDrafts);
      },
      error: (err) => this.loadError.set(messageFromHttpError(err, 'Chargement impossible.')),
    });
  }

  save(r: ReclamationResponse): void {
    const d = this.drafts()[r.id];
    if (!d || d.saving) {
      return;
    }
    this.patchDraft(r.id, { error: null, saving: true });
    this.api
      .adminUpdate(r.id, { status: d.status, adminResponse: d.adminResponse.trim() || null })
      .pipe(
        finalize(() => {
          const after = this.drafts()[r.id];
          if (after) {
            this.patchDraft(r.id, { saving: false });
          }
        }),
      )
      .subscribe({
        next: () => this.reload(),
        error: (err) =>
          this.patchDraft(r.id, {
            error: messageFromHttpError(err, 'Mise à jour impossible.'),
          }),
      });
  }
}
