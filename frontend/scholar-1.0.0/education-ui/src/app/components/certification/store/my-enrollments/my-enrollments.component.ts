import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { EnrollmentDTO } from '../../models/enrollment.model';
import { NgIf, NgFor, DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../../core/services/auth.service';

type StatusFilter  = 'all' | 'ENROLLED' | 'COMPLETED';
type ResultFilter  = 'all' | 'passed' | 'failed';
type SortOption    = 'date_desc' | 'date_asc' | 'score_desc' | 'score_asc' | 'title_asc';

const PAGE_SIZE = 6;

@Component({
  selector: 'app-my-enrollments',
  templateUrl: './my-enrollments.component.html',
  styleUrls: ['./my-enrollments.component.scss'],
  standalone: true,
  imports: [NgIf, FormsModule, NgFor, DecimalPipe, DatePipe]
})
export class MyEnrollmentsComponent implements OnInit {

  // ── Raw data ──────────────────────────────────────────────────────────────
  enrollments: EnrollmentDTO[] = [];
  loading = true;
  error = '';
  userName = '';

  // ── Filters ───────────────────────────────────────────────────────────────
  searchQuery   = '';
  statusFilter: StatusFilter = 'all';
  resultFilter: ResultFilter = 'all';
  sortBy: SortOption = 'date_desc';

  // ── Pagination ────────────────────────────────────────────────────────────
  currentPage = 1;
  pageSize    = PAGE_SIZE;

  // ── Derived ───────────────────────────────────────────────────────────────
  filtered: EnrollmentDTO[] = [];
  paginated: EnrollmentDTO[] = [];

  constructor(
    private enrollService: EnrollmentService,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.userName = this.authService.auth()?.email ?? '';
    if (!this.userName) { this.router.navigate(['/login']); return; }
    this.load();
  }

  load(): void {
    this.loading = true;
    this.enrollService.getMyEnrollments(this.userName).subscribe({
      next: data => {
        this.enrollments = data;
        this.applyAll();
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load enrollments'; this.loading = false; }
    });
  }

  // ── Master filter + sort + paginate ──────────────────────────────────────
  applyAll(): void {
    const q = this.searchQuery.trim().toLowerCase();

    let result = this.enrollments.filter(e => {
      // Search
      if (q && !e.certificationTitle.toLowerCase().includes(q)) return false;
      // Status
      if (this.statusFilter !== 'all' && e.status !== this.statusFilter) return false;
      // Result
      if (this.resultFilter === 'passed' && e.passed !== true)  return false;
      if (this.resultFilter === 'failed' && e.passed !== false) return false;
      return true;
    });

    // Sort
    result = [...result].sort((a, b) => {
      switch (this.sortBy) {
        case 'date_asc':   return new Date(a.enrolledAt).getTime() - new Date(b.enrolledAt).getTime();
        case 'score_desc': return (b.score ?? -1) - (a.score ?? -1);
        case 'score_asc':  return (a.score ?? 101) - (b.score ?? 101);
        case 'title_asc':  return a.certificationTitle.localeCompare(b.certificationTitle);
        default:           return new Date(b.enrolledAt).getTime() - new Date(a.enrolledAt).getTime();
      }
    });

    this.filtered = result;
    this.currentPage = 1;
    this.updatePage();
  }

  updatePage(): void {
    const start = (this.currentPage - 1) * this.pageSize;
    this.paginated = this.filtered.slice(start, start + this.pageSize);
  }

  // ── Filter event handlers ─────────────────────────────────────────────────
  onSearchChange():  void { this.applyAll(); }
  onFilterChange():  void { this.applyAll(); }
  clearSearch():     void { this.searchQuery = ''; this.applyAll(); }
  resetFilters():    void {
    this.searchQuery = '';
    this.statusFilter = 'all';
    this.resultFilter = 'all';
    this.sortBy = 'date_desc';
    this.applyAll();
  }

  // ── Pagination helpers ────────────────────────────────────────────────────
  get totalPages(): number { return Math.ceil(this.filtered.length / this.pageSize); }
  get pages(): number[] {
    const total = this.totalPages;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    // Show first, last, current ±1, with ellipsis
    const p = this.currentPage;
    const set = new Set([1, total, p - 1, p, p + 1].filter(n => n >= 1 && n <= total));
    return Array.from(set).sort((a, b) => a - b);
  }
  isEllipsisBefore(page: number, idx: number): boolean {
    return idx > 0 && page - this.pages[idx - 1] > 1;
  }
  goToPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.currentPage = p;
    this.updatePage();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // ── Stats ─────────────────────────────────────────────────────────────────
  get totalCount():    number { return this.enrollments.length; }
  get enrolledCount(): number { return this.enrollments.filter(e => e.status === 'ENROLLED').length; }
  get completedCount():number { return this.enrollments.filter(e => e.status === 'COMPLETED').length; }
  get passedCount():   number { return this.enrollments.filter(e => e.passed === true).length; }
  get filteredCount(): number { return this.filtered.length; }
  get hasActiveFilters(): boolean {
    return this.searchQuery.trim() !== '' || this.statusFilter !== 'all' || this.resultFilter !== 'all';
  }

  // ── Actions ───────────────────────────────────────────────────────────────
  goToExam(e: EnrollmentDTO):     void { this.router.navigate(['/skillhub/store', e.certificationId, 'exam']); }
  goToPractice(e: EnrollmentDTO): void { this.router.navigate(['/skillhub/store', e.certificationId, 'practice']); }
  goToResult(e: EnrollmentDTO):   void { this.router.navigate(['/skillhub/store', e.certificationId, 'result']); }
  goToCert(e: EnrollmentDTO):     void { this.router.navigate(['/skillhub/store', e.certificationId]); }
  goToStore():                    void { this.router.navigate(['/skillhub/store']); }

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
