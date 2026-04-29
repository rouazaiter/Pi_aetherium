import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { EnrollmentDTO } from '../../models/enrollment.model';
import { NgIf, NgFor, DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

type StatusFilter = 'all' | 'ENROLLED' | 'COMPLETED';

@Component({
    selector: 'app-my-enrollments',
    templateUrl: './my-enrollments.component.html',
    styleUrls: ['./my-enrollments.component.scss'],
    standalone: true,
    imports: [NgIf, FormsModule, NgFor, DecimalPipe, DatePipe]
})
export class MyEnrollmentsComponent implements OnInit {

  enrollments: EnrollmentDTO[] = [];
  filtered: EnrollmentDTO[] = [];
  loading = true;
  error = '';
  userName = '';
  statusFilter: StatusFilter = 'all';

  // Identity prompt
  emailInput = '';
  showEmailPrompt = true;

  constructor(
    private enrollService: EnrollmentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const stored = this.enrollService.getUser();
    if (stored) {
      this.userName = stored;
      this.showEmailPrompt = false;
      this.load();
    }
  }

  confirmEmail(): void {
    if (!this.emailInput.trim()) return;
    this.userName = this.emailInput.trim();
    this.enrollService.setUser(this.userName);
    this.showEmailPrompt = false;
    this.load();
  }

  load(): void {
    this.loading = true;
    this.enrollService.getMyEnrollments(this.userName).subscribe({
      next: data => {
        this.enrollments = data.sort((a, b) =>
          new Date(b.enrolledAt).getTime() - new Date(a.enrolledAt).getTime()
        );
        this.applyFilter();
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load enrollments'; this.loading = false; }
    });
  }

  applyFilter(): void {
    this.filtered = this.statusFilter === 'all'
      ? [...this.enrollments]
      : this.enrollments.filter(e => e.status === this.statusFilter);
  }

  onFilterChange(): void { this.applyFilter(); }

  // ── Stats ─────────────────────────────────────────────────────────────────
  get totalCount():    number { return this.enrollments.length; }
  get enrolledCount(): number { return this.enrollments.filter(e => e.status === 'ENROLLED').length; }
  get completedCount():number { return this.enrollments.filter(e => e.status === 'COMPLETED').length; }
  get passedCount():   number { return this.enrollments.filter(e => e.passed === true).length; }

  // ── Actions ───────────────────────────────────────────────────────────────
  goToExam(e: EnrollmentDTO):     void { this.router.navigate(['/store', e.certificationId, 'exam']); }
  goToPractice(e: EnrollmentDTO): void { this.router.navigate(['/store', e.certificationId, 'practice']); }
  goToResult(e: EnrollmentDTO):   void { this.router.navigate(['/store', e.certificationId, 'result']); }
  goToCert(e: EnrollmentDTO):     void { this.router.navigate(['/store', e.certificationId]); }
  goToStore():                    void { this.router.navigate(['/store']); }

  scoreColor(score: number | null): string {
    if (score === null) return '#94a3b8';
    if (score >= 80) return '#10b981';
    if (score >= 70) return '#f59e0b';
    return '#ef4444';
  }

  circumference = 2 * Math.PI * 28;
  dashOffset(score: number | null): number {
    return this.circumference * (1 - (score ?? 0) / 100);
  }
}
