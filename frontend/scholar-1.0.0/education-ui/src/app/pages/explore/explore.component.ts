import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, catchError, debounceTime, distinctUntilChanged, map, of, switchMap, takeUntil, tap } from 'rxjs';
import type {
  ExploreCollectionCardDto,
  ExploreOptionDto,
  ExplorePortfolioCardDto,
  ExploreProjectCardDto,
  ExploreSort,
  ExploreVisibilityFilter,
  PortfolioVisibility,
  SkillSummaryDto,
} from '../../core/models/api.models';
import { ExploreService, type ExploreFilters } from '../../core/services/explore.service';

type ExploreTab = 'portfolios' | 'projects' | 'collections';
type ExploreViewMode = 'grid' | 'list';
type ExploreVisibilityUi = 'PUBLIC' | 'FRIENDS_ONLY' | 'FRIENDS_PROJECTS_ONLY' | 'PUBLIC_AND_FRIENDS';

type FamilyCard = {
  value: string;
  label: string;
  icon: string;
};

type VisibilityCard = {
  value: ExploreVisibilityUi;
  title: string;
  description: string;
  icon: string;
};

type ExploreResultState =
  | { kind: 'portfolios'; rows: ExplorePortfolioCardDto[]; fallbackProjects: ExploreProjectCardDto[] }
  | { kind: 'projects'; rows: ExploreProjectCardDto[] }
  | { kind: 'collections'; rows: ExploreCollectionCardDto[] }
  | { kind: 'error'; tab: ExploreTab };

@Component({
  selector: 'app-explore',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './explore.component.html',
  styleUrl: './explore.component.scss',
})
export class ExploreComponent implements OnInit, OnDestroy {
  private readonly api = inject(ExploreService);
  private readonly router = inject(Router);
  private skillsTimer: ReturnType<typeof setTimeout> | null = null;
  private readonly destroy$ = new Subject<void>();
  private readonly searchInput$ = new Subject<string>();
  private readonly resultsTrigger$ = new Subject<void>();

  activeTab: ExploreTab = 'projects';
  hasUserSelectedTab = false;
  viewMode: ExploreViewMode = 'grid';

  loadingFilters = true;
  loadingSkills = false;
  loadingResults = false;
  filterError = '';
  resultsError = '';

  searchQuery = '';
  jobTitle = '';
  selectedFamily = '';
  selectedCategory = '';
  selectedVisibility: ExploreVisibilityUi = 'PUBLIC_AND_FRIENDS';
  selectedSort: ExploreSort = 'RELEVANCE';
  skillSearchTerm = '';
  showMorePopularSearches = false;

  families: ExploreOptionDto[] = [];
  categories: ExploreOptionDto[] = [];
  skillSuggestions: SkillSummaryDto[] = [];
  selectedSkills: SkillSummaryDto[] = [];
  portfolioResults: ExplorePortfolioCardDto[] = [];
  projectResults: ExploreProjectCardDto[] = [];
  collectionResults: ExploreCollectionCardDto[] = [];
  portfolioHelperMessage = '';

  readonly popularSearches = [
    'Backend Developer',
    'Full Stack',
    'DevOps Engineer',
    'Data Scientist',
    'UI/UX Designer',
    'Machine Learning',
    'Cloud Engineer',
  ];

  readonly extraPopularSearches = ['Security Engineer', 'Product Designer', 'Python Developer'];

  readonly sortOptions: Array<{ value: ExploreSort; label: string }> = [
    { value: 'RELEVANCE', label: 'Most relevant' },
    { value: 'NEWEST', label: 'Most recent' },
    { value: 'MOST_VIEWED', label: 'Most viewed' },
    { value: 'MOST_LIKED', label: 'Most liked' },
  ];

  readonly visibilityOptions: VisibilityCard[] = [
    { value: 'PUBLIC', title: 'Public', description: 'Public portfolios and projects', icon: 'PB' },
    { value: 'FRIENDS_ONLY', title: 'Friends', description: 'Portfolios shared by your friends', icon: 'FR' },
    { value: 'FRIENDS_PROJECTS_ONLY', title: 'Friends Projects', description: 'Projects shared by your friends', icon: 'FP' },
    { value: 'PUBLIC_AND_FRIENDS', title: 'Public + Friends', description: 'Public content plus friend-only content', icon: 'PF' },
  ];

  ngOnInit(): void {
    this.setupResultsStream();
    this.loadFilters();
    this.triggerResultsLoad();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();

    if (this.skillsTimer) {
      clearTimeout(this.skillsTimer);
    }
  }

  get familyCards(): FamilyCard[] {
    const backendMap = new Map(this.families.map((family) => [family.value, family.label]));
    const orderedValues = ['BACKEND', 'FRONTEND', 'FULL_STACK', 'DEVOPS_CLOUD', 'DATA_AI', 'DESIGN_CREATIVE', 'SECURITY', 'GENERAL'];

    return [
      { value: '', label: 'All', icon: 'grid' },
      ...orderedValues.map((value) => ({
        value,
        label: backendMap.get(value) || this.familyFallbackLabel(value),
        icon: this.familyIcon(value),
      })),
    ];
  }

  get visiblePopularSearches(): string[] {
    return this.showMorePopularSearches ? [...this.popularSearches, ...this.extraPopularSearches] : this.popularSearches;
  }

  get resultsCount(): number {
    switch (this.activeTab) {
      case 'portfolios':
        return this.portfolioResults.length;
      case 'collections':
        return this.collectionResults.length;
      default:
        return this.projectResults.length;
    }
  }

  get resultsLabel(): string {
    switch (this.activeTab) {
      case 'portfolios':
        return 'portfolios';
      case 'collections':
        return 'collections';
      default:
        return 'projects';
    }
  }

  setActiveTab(tab: ExploreTab): void {
    this.hasUserSelectedTab = true;
    if (this.activeTab === tab) {
      this.logTabState();
      return;
    }

    this.activeTab = tab;
    this.portfolioHelperMessage = '';
    this.logTabState();
    this.triggerResultsLoad();
  }

  setViewMode(mode: ExploreViewMode): void {
    this.viewMode = mode;
  }

  selectVisibility(value: ExploreVisibilityUi): void {
    this.selectedVisibility = value;
  }

  applyFilters(): void {
    this.triggerResultsLoad();
  }

  resetAll(): void {
    this.searchQuery = '';
    this.jobTitle = '';
    this.selectedFamily = '';
    this.selectedCategory = '';
    this.selectedVisibility = 'PUBLIC_AND_FRIENDS';
    this.selectedSort = 'RELEVANCE';
    this.skillSearchTerm = '';
    this.skillSuggestions = [];
    this.selectedSkills = [];
    this.showMorePopularSearches = false;
    this.triggerResultsLoad();
  }

  selectFamily(value: string): void {
    this.selectedFamily = value;
  }

  toggleMorePopularSearches(): void {
    this.showMorePopularSearches = !this.showMorePopularSearches;
  }

  runMainSearch(): void {
    this.triggerResultsLoad();
  }

  usePopularSearch(value: string): void {
    this.searchQuery = value;
    this.jobTitle = value;
    this.triggerResultsLoad();
  }

  onSortChange(): void {
    this.triggerResultsLoad();
  }

  onSearchQueryInput(value: string): void {
    this.searchQuery = value;
    this.searchInput$.next(value.trim());
  }

  onSkillSearchInput(): void {
    if (this.skillsTimer) {
      clearTimeout(this.skillsTimer);
    }

    const term = this.skillSearchTerm.trim();
    if (term.length < 2) {
      this.loadingSkills = false;
      this.skillSuggestions = [];
      return;
    }

    this.loadingSkills = true;
    this.skillsTimer = setTimeout(() => {
      this.api.searchSkills(term).subscribe({
        next: (skills) => {
          const selectedIds = new Set(this.selectedSkills.map((skill) => skill.id));
          this.loadingSkills = false;
          this.skillSuggestions = skills.filter((skill) => !selectedIds.has(skill.id)).slice(0, 8);
        },
        error: () => {
          this.loadingSkills = false;
          this.skillSuggestions = [];
        },
      });
    }, 220);
  }

  addSkill(skill: SkillSummaryDto): void {
    if (this.selectedSkills.some((item) => item.id === skill.id)) {
      return;
    }

    this.selectedSkills = [...this.selectedSkills, skill].sort((left, right) => left.name.localeCompare(right.name));
    this.skillSearchTerm = '';
    this.skillSuggestions = [];
  }

  removeSkill(skillId: number): void {
    this.selectedSkills = this.selectedSkills.filter((skill) => skill.id !== skillId);
  }

  focusSkillSearch(input: HTMLInputElement): void {
    input.focus();
  }

  visibilityBadge(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'Friends' : value === 'PUBLIC' ? 'Public' : 'Private';
  }

  visibilityBadgeClass(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'explore-badge explore-badge--friends' : 'explore-badge';
  }

  initials(name: string | null | undefined, fallback: string | null | undefined): string {
    const source = (name?.trim() || fallback?.trim() || 'S').split(/\s+/).filter(Boolean);
    if (source.length === 1) {
      return source[0].slice(0, 2).toUpperCase();
    }
    return source.slice(0, 2).map((part) => part.charAt(0)).join('').toUpperCase();
  }

  portfolioLocation(card: ExplorePortfolioCardDto): string {
    return card.location?.trim() || 'Remote';
  }

  portfolioHeading(card: ExplorePortfolioCardDto): string {
    return card.jobTitle?.trim() || card.portfolioTitle?.trim() || 'Developer';
  }

  portfolioConnections(card: ExplorePortfolioCardDto): number | null {
    return card.totalViews ?? null;
  }

  portfolioSkills(card: ExplorePortfolioCardDto): SkillSummaryDto[] {
    return (card.topSkills ?? []).slice(0, 4);
  }

  portfolioExtraSkills(card: ExplorePortfolioCardDto): number {
    return Math.max((card.topSkills ?? []).length - 4, 0);
  }

  projectSkills(card: ExploreProjectCardDto): SkillSummaryDto[] {
    return (card.topSkills ?? []).slice(0, 3);
  }

  collectionName(card: ExploreCollectionCardDto): string {
    return card.name?.trim() || card.title?.trim() || 'Untitled collection';
  }

  collectionOwner(card: ExploreCollectionCardDto): string {
    return card.ownerDisplayName?.trim() || card.ownerUsername?.trim() || 'Unknown owner';
  }

  collectionSkills(card: ExploreCollectionCardDto): SkillSummaryDto[] {
    return (card.topSkills ?? card.skills ?? []).slice(0, 3);
  }

  openPortfolio(card: ExplorePortfolioCardDto): void {
    const portfolioId = this.portfolioId(card);
    if (!portfolioId) {
      console.warn('Missing item id for navigation');
      return;
    }

    this.router.navigate(['/explore/portfolios', portfolioId]);
  }

  openProject(card: ExploreProjectCardDto): void {
    const projectId = this.projectId(card);
    if (!projectId) {
      console.warn('Missing item id for navigation');
      return;
    }

    this.router.navigate(['/explore/projects', projectId]);
  }

  openCollection(card: ExploreCollectionCardDto): void {
    const collectionId = this.collectionId(card);
    if (!collectionId) {
      console.warn('Missing item id for navigation');
      return;
    }

    this.router.navigate(['/explore/collections', collectionId]);
  }

  onCardKeydown(event: KeyboardEvent, kind: ExploreTab, card: ExplorePortfolioCardDto | ExploreProjectCardDto | ExploreCollectionCardDto): void {
    if (event.key !== 'Enter' && event.key !== ' ') {
      return;
    }

    event.preventDefault();

    if (kind === 'portfolios') {
      this.openPortfolio(card as ExplorePortfolioCardDto);
      return;
    }

    if (kind === 'collections') {
      this.openCollection(card as ExploreCollectionCardDto);
      return;
    }

    this.openProject(card as ExploreProjectCardDto);
  }

  discoverPeople(): void {
    this.hasUserSelectedTab = true;
    this.activeTab = 'portfolios';
    this.triggerResultsLoad();
  }

  private loadFilters(): void {
    this.loadingFilters = true;
    this.filterError = '';

    let pending = 2;
    const finish = () => {
      pending -= 1;
      if (pending <= 0) {
        this.loadingFilters = false;
      }
    };

    this.api.getFamilies().subscribe({
      next: (rows) => {
        this.families = rows;
        finish();
      },
      error: () => {
        this.families = [];
        this.filterError = 'Explore results could not be loaded.';
        finish();
      },
    });

    this.api.getSkillCategories().subscribe({
      next: (rows) => {
        this.categories = rows;
        finish();
      },
      error: () => {
        this.categories = [];
        this.filterError = 'Explore results could not be loaded.';
        finish();
      },
    });
  }

  private setupResultsStream(): void {
    this.searchInput$.pipe(debounceTime(300), distinctUntilChanged(), takeUntil(this.destroy$)).subscribe(() => this.triggerResultsLoad());

    this.resultsTrigger$
      .pipe(
        tap(() => {
          this.loadingResults = true;
          this.resultsError = '';
        }),
        switchMap(() => this.loadResults$()),
        takeUntil(this.destroy$),
      )
      .subscribe((result) => {
        if (result.kind === 'error') {
          this.loadingResults = false;
          if (result.tab === 'portfolios') {
            this.portfolioResults = [];
          } else if (result.tab === 'collections') {
            this.collectionResults = [];
          } else {
            this.projectResults = [];
          }
          this.resultsError = 'Explore results could not be loaded.';
          return;
        }

        if (result.kind === 'portfolios') {
          this.portfolioResults = result.rows;
          this.portfolioHelperMessage = result.rows.length < 3 ? 'Not many portfolios yet — explore projects instead 👇' : '';
          if (!this.hasUserSelectedTab && result.rows.length === 0 && result.fallbackProjects.length > 0) {
            this.activeTab = 'projects';
            this.projectResults = result.fallbackProjects;
            this.portfolioHelperMessage = '';
          }
          this.logTabState();
          this.loadingResults = false;
          return;
        }

        if (result.kind === 'collections') {
          this.collectionResults = result.rows;
          this.logTabState();
          this.loadingResults = false;
          return;
        }

        this.projectResults = result.rows;
        this.logTabState();
        this.loadingResults = false;
      });
  }

  private triggerResultsLoad(): void {
    this.resultsTrigger$.next();
  }

  private loadResults$() {
    const filters: ExploreFilters = {
      q: this.searchQuery.trim() || undefined,
      jobTitle: this.jobTitle.trim() || undefined,
      family: this.selectedFamily || undefined,
      category: this.selectedCategory || undefined,
      skillIds: this.selectedSkills.map((skill) => skill.id),
      visibility: this.resolveVisibilityForActiveTab(),
      sort: this.selectedSort,
    };

    if (this.activeTab === 'portfolios') {
      return this.api.searchPortfolios(filters).pipe(
        switchMap((rows) => {
          if (!this.hasUserSelectedTab && rows.length === 0) {
            const fallbackFilters: ExploreFilters = {
              ...filters,
              visibility: this.resolveVisibilityForTab('projects'),
            };

            return this.api.searchProjects(fallbackFilters).pipe(
              map((fallbackProjects) => ({
                kind: 'portfolios' as const,
                rows,
                fallbackProjects,
              })),
            );
          }

          return of({
            kind: 'portfolios' as const,
            rows,
            fallbackProjects: [] as ExploreProjectCardDto[],
          });
        }),
        catchError(() => of({ kind: 'error' as const, tab: 'portfolios' as const })),
      );
    }

    if (this.activeTab === 'collections') {
      return this.api.searchCollections(filters).pipe(
        map((rows) => ({
          kind: 'collections' as const,
          rows,
        })),
        catchError(() => of({ kind: 'error' as const, tab: 'collections' as const })),
      );
    }

    return this.api.searchProjects(filters).pipe(
      map((rows) => ({
        kind: 'projects' as const,
        rows,
      })),
      catchError(() => of({ kind: 'error' as const, tab: 'projects' as const })),
    );
  }

  private resolveVisibilityForActiveTab(): ExploreVisibilityFilter {
    return this.resolveVisibilityForTab(this.activeTab);
  }

  private resolveVisibilityForTab(tab: ExploreTab): ExploreVisibilityFilter {
    switch (this.selectedVisibility) {
      case 'PUBLIC':
        return 'PUBLIC';
      case 'FRIENDS_ONLY':
        return tab === 'portfolios' ? 'FRIENDS' : 'PUBLIC_AND_FRIENDS';
      case 'FRIENDS_PROJECTS_ONLY':
        return tab === 'projects' || tab === 'collections' ? 'FRIENDS' : 'PUBLIC_AND_FRIENDS';
      default:
        return 'PUBLIC_AND_FRIENDS';
    }
  }

  private familyFallbackLabel(value: string): string {
    return (
      {
        BACKEND: 'Backend',
        FRONTEND: 'Frontend',
        FULL_STACK: 'Full Stack',
        DEVOPS_CLOUD: 'DevOps',
        DATA_AI: 'Data / AI',
        DESIGN_CREATIVE: 'Design',
        SECURITY: 'Security',
        GENERAL: 'Other',
      }[value] || value
    );
  }

  private familyIcon(value: string): string {
    return (
      {
        BACKEND: '</>',
        FRONTEND: 'UI',
        FULL_STACK: 'FS',
        DEVOPS_CLOUD: 'OPS',
        DATA_AI: 'AI',
        DESIGN_CREATIVE: 'DES',
        SECURITY: 'SEC',
        GENERAL: '...',
      }[value] || '.'
    );
  }

  private portfolioId(card: ExplorePortfolioCardDto): number | null {
    const value = (card as ExplorePortfolioCardDto & { id?: number | null }).portfolioId ?? (card as ExplorePortfolioCardDto & { id?: number | null }).id;
    return typeof value === 'number' ? value : null;
  }

  private projectId(card: ExploreProjectCardDto): number | null {
    const value = (card as ExploreProjectCardDto & { id?: number | null }).projectId ?? (card as ExploreProjectCardDto & { id?: number | null }).id;
    return typeof value === 'number' ? value : null;
  }

  private collectionId(card: ExploreCollectionCardDto): number | null {
    const value = (card as ExploreCollectionCardDto & { id?: number | null }).collectionId ?? (card as ExploreCollectionCardDto & { id?: number | null }).id;
    return typeof value === 'number' ? value : null;
  }

  private logTabState(): void {
    console.log('activeTab', this.activeTab);
    console.log('portfolio count', this.portfolioResults.length);
    console.log('project count', this.projectResults.length);
  }
}
