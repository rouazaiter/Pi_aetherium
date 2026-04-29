import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { EnrollmentService } from '../../services/enrollment.service';
import { Certification, Difficulty } from '../../models/certification.model';
import { FormsModule } from '@angular/forms';
import { NgIf, NgFor, SlicePipe, DecimalPipe } from '@angular/common';
import { ChatAssistantComponent } from '../chat-assistant/chat-assistant.component';

type PriceFilter = 'all' | 'free' | 'paid';
type SortOption  = 'default' | 'price-asc' | 'price-desc' | 'title-asc';

@Component({
    selector: 'app-store-catalog',
    templateUrl: './store-catalog.component.html',
    styleUrls: ['./store-catalog.component.scss'],
    standalone: true,
    imports: [FormsModule, NgIf, NgFor, ChatAssistantComponent, SlicePipe, DecimalPipe]
})
export class StoreCatalogComponent implements OnInit, OnDestroy {

  // ── Data ──────────────────────────────────────────────────────────────────
  all: Certification[] = [];
  filtered: Certification[] = [];
  paged: Certification[] = [];
  loading = true;
  error = '';
  skeletonRows = Array(9);

  // ── Search ────────────────────────────────────────────────────────────────
  searchQuery = '';
  private search$ = new Subject<string>();
  private sub = new Subscription();

  // ── Filters ───────────────────────────────────────────────────────────────
  difficultyFilter: Difficulty | 'all' = 'all';
  priceFilter: PriceFilter = 'all';
  categoryFilter = 'all';
  sortBy: SortOption = 'default';

  readonly difficulties: Array<Difficulty | 'all'> = ['all', 'BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  get categories(): string[] {
    const cats = [...new Set(this.all.map(c => c.category).filter(Boolean))].sort();
    return ['all', ...cats];
  }

  // ── Pagination ────────────────────────────────────────────────────────────
  readonly PAGE_SIZE = 9;
  page = 1;
  get totalPages(): number { return Math.max(1, Math.ceil(this.filtered.length / this.PAGE_SIZE)); }
  get pageNumbers(): number[] {
    // Show max 5 page numbers with ellipsis logic
    const total = this.totalPages;
    if (total <= 7) return Array.from({ length: total }, (_, i) => i + 1);
    const pages: number[] = [];
    if (this.page <= 4) {
      for (let i = 1; i <= 5; i++) pages.push(i);
      pages.push(-1, total);
    } else if (this.page >= total - 3) {
      pages.push(1, -1);
      for (let i = total - 4; i <= total; i++) pages.push(i);
    } else {
      pages.push(1, -1, this.page - 1, this.page, this.page + 1, -2, total);
    }
    return pages;
  }

  constructor(private enrollService: EnrollmentService, private router: Router) {}

  ngOnInit(): void {
    this.enrollService.getStore().subscribe({
      next: data => {
        this.all = data;
        this.applyAll();
        this.loading = false;
      },
      error: () => { this.error = 'Failed to load certifications'; this.loading = false; }
    });

    this.sub.add(
      this.search$.pipe(debounceTime(300), distinctUntilChanged())
        .subscribe(() => this.applyAll())
    );
  }

  ngOnDestroy(): void { this.sub.unsubscribe(); }

  // ── Search input ──────────────────────────────────────────────────────────
  onSearchInput(value: string): void { this.search$.next(value); }
  clearSearch(): void { this.searchQuery = ''; this.search$.next(''); }

  // ── Filter/sort change ────────────────────────────────────────────────────
  onFilterChange(): void { this.applyAll(); }

  resetFilters(): void {
    this.searchQuery = '';
    this.difficultyFilter = 'all';
    this.priceFilter = 'all';
    this.categoryFilter = 'all';
    this.sortBy = 'default';
    this.applyAll();
  }

  get hasActiveFilters(): boolean {
    return this.searchQuery.trim() !== '' ||
           this.difficultyFilter !== 'all' ||
           this.priceFilter !== 'all' ||
           this.categoryFilter !== 'all' ||
           this.sortBy !== 'default';
  }

  // ── Core filter + sort + paginate ─────────────────────────────────────────
  private applyAll(): void {
    const term = this.searchQuery.toLowerCase().trim();

    let result = this.all.filter(c => {
      // Search
      if (term && !c.title.toLowerCase().includes(term) &&
          !(c.category ?? '').toLowerCase().includes(term) &&
          !(c.description ?? '').toLowerCase().includes(term)) return false;
      // Difficulty
      if (this.difficultyFilter !== 'all' && c.difficulty !== this.difficultyFilter) return false;
      // Price
      if (this.priceFilter === 'free' && c.price > 0) return false;
      if (this.priceFilter === 'paid' && c.price === 0) return false;
      // Category
      if (this.categoryFilter !== 'all' && c.category !== this.categoryFilter) return false;
      return true;
    });

    // Sort
    switch (this.sortBy) {
      case 'price-asc':  result.sort((a, b) => a.price - b.price); break;
      case 'price-desc': result.sort((a, b) => b.price - a.price); break;
      case 'title-asc':  result.sort((a, b) => a.title.localeCompare(b.title)); break;
    }

    this.filtered = result;
    this.page = 1;
    this.applyPage();
  }

  private applyPage(): void {
    const start = (this.page - 1) * this.PAGE_SIZE;
    this.paged = this.filtered.slice(start, start + this.PAGE_SIZE);
  }

  goPage(p: number): void {
    if (p < 1 || p > this.totalPages || p === this.page) return;
    this.page = p;
    this.applyPage();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────
  viewCert(id: number): void { this.router.navigate(['/store', id]); }

  difficultyClass(d: string): string {
    return { BEGINNER: 'badge-success', INTERMEDIATE: 'badge-warning', ADVANCED: 'badge-danger' }[d] ?? 'badge-secondary';
  }

  difficultyLabel(d: string): string {
    return { BEGINNER: '🟢 Beginner', INTERMEDIATE: '🟡 Intermediate', ADVANCED: '🔴 Advanced' }[d] ?? d;
  }

  difficultyEmoji(d: string): string {
    return { BEGINNER: '🌱', INTERMEDIATE: '⚡', ADVANCED: '🔥' }[d] ?? '🎓';
  }
}
