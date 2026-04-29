import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-reports.component.html',
  styleUrls: ['./admin-reports.component.scss']
})
export class AdminReportsComponent implements OnInit {

  reports: any[] = [];
  loading = true;
  filter: 'ALL' | 'PENDING' | 'REVIEWED' | 'ACTION_TAKEN' | 'DISMISSED' = 'PENDING';

  readonly statusOptions = ['PENDING', 'REVIEWED', 'ACTION_TAKEN', 'DISMISSED'];

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    const obs = this.filter === 'PENDING' ? this.api.getPendingReports() : this.api.getAllReports();
    obs.subscribe({
      next: r => { this.reports = r; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get filteredReports(): any[] {
    if (this.filter === 'ALL') return this.reports;
    return this.reports.filter(r => r.status === this.filter);
  }

  updateStatus(report: any, status: string): void {
    this.api.updateReportStatus(report.id, status).subscribe(updated => {
      report.status = updated.status;
      report.reviewedAt = updated.reviewedAt;
      const labels: Record<string, string> = {
        REVIEWED: 'Marked as reviewed', ACTION_TAKEN: 'Action taken', DISMISSED: 'Report dismissed'
      };
      this.toast.success(labels[status] || 'Status updated');
    });
  }

  statusClass(status: string): string {
    const map: Record<string, string> = {
      PENDING: 'status-pending', REVIEWED: 'status-reviewed',
      ACTION_TAKEN: 'status-action', DISMISSED: 'status-dismissed'
    };
    return map[status] || '';
  }

  reasonIcon(reason: string): string {
    const map: Record<string, string> = {
      SPAM: '🚫', INAPPROPRIATE: '🔞', MISINFORMATION: '❌',
      HARASSMENT: '😡', COPYRIGHT: '©️', OTHER: '📝'
    };
    return map[reason] || '📝';
  }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
  }
}
