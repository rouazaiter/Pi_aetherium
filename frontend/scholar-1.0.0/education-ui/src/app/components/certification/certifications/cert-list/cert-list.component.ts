import { Component, OnInit, OnDestroy, ViewEncapsulation } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { CertificationService } from '../../services/certification.service';
import { Certification, CertificationCreate, Difficulty, CertStatus } from '../../models/certification.model';
import { ToastService } from '../../shared/toast.service';
import { ConfirmModalService } from '../../shared/confirm-modal/confirm-modal.service';
import { FormsModule } from '@angular/forms';
import { NgIf, NgFor, NgClass, DecimalPipe } from '@angular/common';

type SortField = 'title' | 'category' | 'difficulty' | 'status' | 'price' | 'createdAt';
type SortDir   = 'asc' | 'desc';
type StatusFilter     = 'all' | 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
type DifficultyFilter = 'all' | 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';

@Component({
    selector: 'app-cert-list',
    templateUrl: './cert-list.component.html',
    styleUrls: ['./cert-list.component.scss'],
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [FormsModule, NgIf, NgFor, NgClass, DecimalPipe]
})
export class CertListComponent implements OnInit, OnDestroy {

  // ── Data ──────────────────────────────────────────────────────────────
  all: Certification[] = [];
  filtered: Certification[] = [];
  paged: Certification[] = [];

  // ── State ─────────────────────────────────────────────────────────────
  loading = true;
  skeletonRows = Array(6);

  // ── Search ────────────────────────────────────────────────────────────
  searchQuery = '';
  private search$ = new Subject<string>();

  // ── Filters ───────────────────────────────────────────────────────────
  statusFilter: StatusFilter = 'all';
  difficultyFilter: DifficultyFilter = 'all';
  categoryFilter = 'all';
  get availableCategories(): string[] {
    const cats = [...new Set(this.all.map(c => c.category).filter(Boolean))].sort();
    return cats;
  }
  get hasActiveFilters(): boolean {
    return this.searchQuery.trim() !== '' ||
           this.statusFilter !== 'all' ||
           this.difficultyFilter !== 'all' ||
           this.categoryFilter !== 'all';
  }

  // ── Sort ──────────────────────────────────────────────────────────────
  sortField: SortField = 'createdAt';
  sortDir: SortDir = 'desc';

  // ── Pagination ────────────────────────────────────────────────────────
  page = 1;
  pageSize = 8;
  get totalPages(): number { return Math.max(1, Math.ceil(this.filtered.length / this.pageSize)); }
  get pageNumbers(): number[] {
    const total = this.totalPages;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const p = this.page;
    const set = new Set([1, total, p - 1, p, p + 1].filter(n => n >= 1 && n <= total));
    return Array.from(set).sort((a, b) => a - b);
  }
  isEllipsisBefore(page: number, idx: number, pages: number[]): boolean {
    return idx > 0 && page - pages[idx - 1] > 1;
  }

  // ── Modal: create/edit ────────────────────────────────────────────────
  showFormModal = false;
  editingId: number | null = null;
  formSaving = false;
  formError = '';
  form: CertificationCreate = this.emptyForm();

  readonly difficulties: Difficulty[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  readonly statuses: CertStatus[] = ['DRAFT', 'PUBLISHED', 'ARCHIVED'];

  // AI & PDF
  showAiModal = false;
  aiTopic = '';
  aiProcessing = false;

  private subs = new Subscription();

  constructor(
    private certService: CertificationService,
    private router: Router,
    private toast: ToastService,
    private confirm: ConfirmModalService
  ) {}

  ngOnInit(): void {
    this.load();
    this.subs.add(
      this.search$.pipe(debounceTime(300), distinctUntilChanged())
        .subscribe(q => this.applyFilter(q))
    );
  }

  ngOnDestroy(): void { this.subs.unsubscribe(); }

  // ── Load ──────────────────────────────────────────────────────────────
  load(): void {
    this.loading = true;
    this.certService.getAll().subscribe({
      next: data => {
        this.all = data;
        this.applyFilter(this.searchQuery);
        this.loading = false;
      },
      error: () => { this.toast.error('Failed to load certifications'); this.loading = false; }
    });
  }

  // ── Search ────────────────────────────────────────────────────────────
  onSearchInput(value: string): void { this.search$.next(value); }

  clearSearch(): void { this.searchQuery = ''; this.search$.next(''); }

  private applyFilter(q: string): void {
    const term = q.toLowerCase().trim();
    this.filtered = this.all.filter(c => {
      // Text search
      if (term && !(
        c.title.toLowerCase().includes(term) ||
        c.category?.toLowerCase().includes(term) ||
        c.difficulty?.toLowerCase().includes(term) ||
        c.status?.toLowerCase().includes(term)
      )) return false;
      // Status filter
      if (this.statusFilter !== 'all' && c.status !== this.statusFilter) return false;
      // Difficulty filter
      if (this.difficultyFilter !== 'all' && c.difficulty !== this.difficultyFilter) return false;
      // Category filter
      if (this.categoryFilter !== 'all' && c.category !== this.categoryFilter) return false;
      return true;
    });
    this.applySort();
  }

  onFilterChange(): void { this.applyFilter(this.searchQuery); }

  resetFilters(): void {
    this.searchQuery = '';
    this.statusFilter = 'all';
    this.difficultyFilter = 'all';
    this.categoryFilter = 'all';
    this.applyFilter('');
  }

  // ── Sort ──────────────────────────────────────────────────────────────
  setSort(field: SortField): void {
    if (this.sortField === field) {
      this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field; this.sortDir = 'asc';
    }
    this.applySort();
  }

  private applySort(): void {
    const dir = this.sortDir === 'asc' ? 1 : -1;
    this.filtered.sort((a, b) => {
      const av = (a as any)[this.sortField] ?? '';
      const bv = (b as any)[this.sortField] ?? '';
      return av < bv ? -dir : av > bv ? dir : 0;
    });
    this.page = 1;
    this.applyPage();
  }

  // ── Pagination ────────────────────────────────────────────────────────
  applyPage(): void {
    const start = (this.page - 1) * this.pageSize;
    this.paged = this.filtered.slice(start, start + this.pageSize);
  }

  goPage(p: number): void {
    if (p < 1 || p > this.totalPages) return;
    this.page = p; this.applyPage();
  }

  // ── CRUD ──────────────────────────────────────────────────────────────
  openCreate(): void {
    this.editingId = null;
    this.form = this.emptyForm();
    this.formError = '';
    this.showFormModal = true;
  }

  openEdit(cert: Certification): void {
    this.editingId = cert.id;
    this.form = {
      title: cert.title, description: cert.description, category: cert.category,
      difficulty: cert.difficulty, status: cert.status, price: cert.price,
      validFrom: cert.validFrom ? cert.validFrom.substring(0, 10) : null,
      expiresAt: cert.expiresAt ? cert.expiresAt.substring(0, 10) : null,
      durationMinutes: cert.durationMinutes, passingScore: cert.passingScore, exams: []
    };
    this.formError = '';
    this.showFormModal = true;
  }

  saveForm(): void {
    if (!this.form.title.trim()) { this.formError = 'Title is required'; return; }
    this.formSaving = true; this.formError = '';
    const obs = this.editingId
      ? this.certService.update(this.editingId, this.form)
      : this.certService.create(this.form);
    obs.subscribe({
      next: () => {
        this.toast.success(this.editingId ? 'Certification updated' : 'Certification created');
        this.showFormModal = false;
        this.formSaving = false;
        this.load();
      },
      error: e => { this.formError = e.message || 'Save failed'; this.formSaving = false; }
    });
  }

  async deleteCert(cert: Certification): Promise<void> {
    const ok = await this.confirm.delete(cert.title);
    if (!ok) return;
    this.certService.delete(cert.id).subscribe({
      next: () => { this.toast.success('Certification deleted'); this.load(); },
      error: () => this.toast.error('Failed to delete certification')
    });
  }

  viewDetail(id: number): void { this.router.navigate(['/skillhub/certifications', id]); }

  // ── AI & PDF ───────────────────────────────────────────────────────────
  generateAi(): void {
    if (!this.aiTopic.trim()) return;
    this.aiProcessing = true;
    this.certService.generateFromLlm({
      topic: this.aiTopic,
      description: `Professional certification for ${this.aiTopic}`,
      difficulty: 'INTERMEDIATE',
      numberOfQuestions: 10,
      timeLimitMinutes: 60
    }).subscribe({
      next: (cert) => {
        this.toast.success('AI Certification generated!');
        this.aiProcessing = false;
        this.showAiModal = false;
        this.aiTopic = '';
        this.load();
      },
      error: () => {
        this.toast.error('AI generation failed');
        this.aiProcessing = false;
      }
    });
  }

  onPdfUpload(event: Event): void {
    const file = (event.target as HTMLInputElement).files?.[0];
    if (!file) return;

    this.loading = true;
    this.certService.importFromPdf(file).subscribe({
      next: () => {
        this.toast.success('PDF imported successfully');
        this.load();
      },
      error: () => {
        this.toast.error('Failed to import PDF');
        this.loading = false;
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────
  sortIcon(field: SortField): string {
    if (this.sortField !== field) return '↕';
    return this.sortDir === 'asc' ? '↑' : '↓';
  }

  statusClass(s: string): string {
    return { DRAFT: 'badge-warning', PUBLISHED: 'badge-success', ARCHIVED: 'badge-secondary' }[s] ?? '';
  }

  difficultyClass(d: string): string {
    return { BEGINNER: 'badge-info', INTERMEDIATE: 'badge-primary', ADVANCED: 'badge-danger' }[d] ?? '';
  }

  private emptyForm(): CertificationCreate {
    return {
      title: '', description: '', category: '', difficulty: 'INTERMEDIATE',
      status: 'DRAFT', price: 0, validFrom: null, expiresAt: null,
      durationMinutes: 60, passingScore: 70, exams: []
    };
  }
}
