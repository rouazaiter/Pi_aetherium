import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-admin-discussions',
  templateUrl: './admin-discussions.component.html',
  styleUrls: ['./admin-discussions.component.scss']
})
export class AdminDiscussionsComponent implements OnInit {
  discussions: any[] = [];
  loading = true;
  search = '';

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getDiscussions(1).subscribe({
      next: d => { this.discussions = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get filtered(): any[] {
    if (!this.search.trim()) return this.discussions;
    const q = this.search.toLowerCase();
    return this.discussions.filter(d => d.theme.toLowerCase().includes(q) || d.creator?.username?.toLowerCase().includes(q));
  }

  deleteDiscussion(disc: any): void {
    if (!confirm(`Delete discussion "${disc.theme}"?`)) return;
    this.api.deleteDiscussion(disc.id, disc.creator?.id ?? 1).subscribe({
      next: () => { this.discussions = this.discussions.filter(d => d.id !== disc.id); this.toast.success('Discussion deleted'); },
      error: () => this.toast.error('Failed to delete discussion')
    });
  }
}
