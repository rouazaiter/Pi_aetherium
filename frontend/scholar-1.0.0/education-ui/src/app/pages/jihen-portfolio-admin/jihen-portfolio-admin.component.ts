// Jihen Portfolio Admin

import { CommonModule } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { JihenPortfolioAdminModels } from './jihen-portfolio-admin.models';
import { JihenPortfolioAdminService } from './jihen-portfolio-admin.service';

type SidebarItem = {
  label: string;
  tab?: JihenPortfolioAdminModels.AdminTab;
};

type StatCard = {
  label: string;
  icon: string;
  value: number;
  growth: number | null;
};

type ActivityView = {
  label: string;
  portfolios: number;
  projects: number;
  collections: number;
};

type TrendingSkillView = {
  rank: number;
  name: string;
  usageCount: number;
  progress: number;
};

type RecentItemView = {
  id: number | null;
  title: string;
  type: string;
  owner: string;
  visibility: string;
  status: string;
  views: number;
  createdAt: string | null;
};

type ProjectRowView = {
  id: number;
  title: string;
  description: string;
  thumbnailUrl: string;
  owner: string;
  portfolio: string;
  visibility: JihenPortfolioAdminModels.ProjectVisibility;
  status: JihenPortfolioAdminModels.ModerationStatus;
  views: number;
  likes: number;
  skills: string[];
  createdAt: string | null;
};

@Component({
  selector: 'app-jihen-portfolio-admin',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './jihen-portfolio-admin.component.html',
  styleUrl: './jihen-portfolio-admin.component.scss',
})
export class JihenPortfolioAdminComponent implements OnInit {
  private readonly api = inject(JihenPortfolioAdminService);
  private readonly router = inject(Router);

  protected activeTab: JihenPortfolioAdminModels.AdminTab = 'overview';
  protected readonly sidebarItems: SidebarItem[] = [
    { label: 'Overview', tab: 'overview' },
    { label: 'Portfolios' },
    { label: 'Projects', tab: 'projects' },
    { label: 'Collections' },
    { label: 'Skills' },
    { label: 'Moderation' },
    { label: 'Analytics' },
    { label: 'CV & AI Usage' },
    { label: 'Search Insights' },
    { label: 'Settings' },
  ];

  protected overviewLoading = true;
  protected projectsLoading = false;
  protected overviewError = '';
  protected projectsError = '';

  protected overviewSearch = '';
  protected activityRange: JihenPortfolioAdminModels.ActivityRange = '30d';
  protected overview: JihenPortfolioAdminModels.OverviewResponse | null = null;
  protected activityRows: ActivityView[] = [];
  protected trendingSkills: TrendingSkillView[] = [];
  protected recentItems: RecentItemView[] = [];

  protected projectSearch = '';
  protected selectedVisibility = '';
  protected selectedStatus = '';
  protected selectedDateRange = 'all';
  protected selectedSort = 'createdAt,desc';
  protected projectPage = 0;
  protected projectSize = 10;
  protected projectRows: ProjectRowView[] = [];
  protected projectTotal = 0;
  protected projectTotalPages = 0;
  protected openActionMenuId: number | null = null;

  protected blockModalOpen = false;
  protected blockReason = '';
  protected blockTarget: ProjectRowView | null = null;
  protected actionLoadingId: number | null = null;

  ngOnInit(): void {
    this.loadOverview();
    this.loadProjects();
  }

  protected selectTab(tab: JihenPortfolioAdminModels.AdminTab | undefined): void {
    if (!tab) {
      return;
    }

    this.activeTab = tab;
    if (tab === 'projects' && this.projectRows.length === 0 && !this.projectsLoading) {
      this.loadProjects();
    }
  }

  protected loadOverview(): void {
    this.overviewLoading = true;
    this.overviewError = '';

    forkJoin({
      overview: this.api.getOverview(),
      activity: this.api.getActivity(this.activityRange),
      trending: this.api.getTrendingSkills(),
      recentItems: this.api.getRecentItems(),
    })
      .pipe(
        catchError(() => {
          this.overviewError = 'Admin dashboard could not be loaded.';
          this.overviewLoading = false;
          return of(null);
        }),
      )
      .subscribe((result) => {
        if (!result) {
          return;
        }

        this.overview = result.overview;
        this.activityRows = this.normalizeActivity(result.activity);
        this.trendingSkills = this.normalizeTrendingSkills(result.trending);
        this.recentItems = this.normalizeRecentItems(result.recentItems);
        this.overviewLoading = false;
      });
  }

  protected onActivityRangeChange(range: JihenPortfolioAdminModels.ActivityRange): void {
    this.activityRange = range;
    this.loadOverview();
  }

  protected applyProjectFilters(): void {
    this.projectPage = 0;
    this.loadProjects();
  }

  protected resetProjectFilters(): void {
    this.projectSearch = '';
    this.selectedVisibility = '';
    this.selectedStatus = '';
    this.selectedDateRange = 'all';
    this.selectedSort = 'createdAt,desc';
    this.projectPage = 0;
    this.loadProjects();
  }

  protected changeProjectPage(page: number): void {
    if (page < 0 || page >= this.projectTotalPages || page === this.projectPage) {
      return;
    }

    this.projectPage = page;
    this.loadProjects();
  }

  protected toggleActionMenu(projectId: number): void {
    this.openActionMenuId = this.openActionMenuId === projectId ? null : projectId;
  }

  protected openProject(projectId: number): void {
    this.router.navigate(['/explore/projects', projectId]);
  }

  protected setProjectVisibility(project: ProjectRowView, visibility: JihenPortfolioAdminModels.ProjectVisibility): void {
    this.openActionMenuId = null;
    this.actionLoadingId = project.id;

    this.api.updateProjectVisibility(project.id, { visibility }).subscribe({
      next: () => {
        this.projectRows = this.projectRows.map((row) => (row.id === project.id ? { ...row, visibility } : row));
        this.actionLoadingId = null;
      },
      error: () => {
        this.projectsError = 'Projects could not be loaded.';
        this.actionLoadingId = null;
      },
    });
  }

  protected setModeration(project: ProjectRowView, status: JihenPortfolioAdminModels.ModerationStatus): void {
    this.openActionMenuId = null;

    if (status === 'BLOCKED') {
      this.blockTarget = project;
      this.blockReason = '';
      this.blockModalOpen = true;
      return;
    }

    const reason = status === 'UNDER_REVIEW' ? 'Needs review' : null;
    this.actionLoadingId = project.id;

    this.api.updateProjectModeration(project.id, { status, reason }).subscribe({
      next: () => {
        this.projectRows = this.projectRows.map((row) => (row.id === project.id ? { ...row, status } : row));
        this.actionLoadingId = null;
      },
      error: () => {
        this.projectsError = 'Projects could not be loaded.';
        this.actionLoadingId = null;
      },
    });
  }

  protected closeBlockModal(): void {
    this.blockModalOpen = false;
    this.blockReason = '';
    this.blockTarget = null;
  }

  protected confirmBlockProject(): void {
    if (!this.blockTarget) {
      return;
    }

    this.actionLoadingId = this.blockTarget.id;
    this.api.updateProjectModeration(this.blockTarget.id, { status: 'BLOCKED', reason: this.blockReason.trim() || 'Blocked by admin' }).subscribe({
      next: () => {
        const targetId = this.blockTarget?.id;
        this.projectRows = this.projectRows.map((row) => (row.id === targetId ? { ...row, status: 'BLOCKED' } : row));
        this.actionLoadingId = null;
        this.closeBlockModal();
      },
      error: () => {
        this.projectsError = 'Projects could not be loaded.';
        this.actionLoadingId = null;
      },
    });
  }

  protected exportProjectsCsv(): void {
    const rows = this.filteredProjectRows();
    const header = ['Project', 'Owner', 'Portfolio', 'Visibility', 'Status', 'Views', 'Likes', 'Skills', 'Created At'];
    const body = rows.map((row) => [
      row.title,
      row.owner,
      row.portfolio,
      row.visibility,
      row.status,
      String(row.views),
      String(row.likes),
      row.skills.join(' | '),
      row.createdAt ?? '',
    ]);

    const csvContent = [header, ...body]
      .map((line) => line.map((value) => `"${String(value ?? '').replace(/"/g, '""')}"`).join(','))
      .join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.href = url;
    link.download = 'jihen-portfolio-projects.csv';
    link.click();
    URL.revokeObjectURL(url);
  }

  protected statCards(): StatCard[] {
    const overview = this.overview;
    if (!overview) {
      return [];
    }

    return [
      { label: 'Total Portfolios', icon: 'PF', value: Number(overview.totalPortfolios ?? 0), growth: this.resolveGrowth(overview, 'portfolioGrowthPct') },
      { label: 'Total Projects', icon: 'PR', value: Number(overview.totalProjects ?? 0), growth: this.resolveGrowth(overview, 'projectGrowthPct') },
      { label: 'Total Collections', icon: 'CL', value: Number(overview.totalCollections ?? 0), growth: this.resolveGrowth(overview, 'collectionGrowthPct') },
      { label: 'Total Skills', icon: 'SK', value: Number(overview.totalSkills ?? 0), growth: this.resolveGrowth(overview, 'skillGrowthPct') },
      { label: 'Users with Portfolio', icon: 'US', value: Number(overview.usersWithPortfolio ?? 0), growth: this.resolveGrowth(overview, 'usersWithPortfolioGrowthPct') },
    ];
  }

  protected recentItemsForView(): RecentItemView[] {
    const query = this.overviewSearch.trim().toLowerCase();
    if (!query) {
      return this.recentItems;
    }

    return this.recentItems.filter((item) =>
      [item.title, item.type, item.owner, item.status].some((value) => value.toLowerCase().includes(query)),
    );
  }

  protected donutStyle(): string {
    const total = this.totalVisibilityCount();
    if (total <= 0) {
      return 'conic-gradient(#efe8ff 0deg 360deg)';
    }

    const publicValue = Number(this.overview?.publicPortfolios ?? 0);
    const friendsValue = Number(this.overview?.friendsOnlyPortfolios ?? 0);
    const privateValue = Number(this.overview?.privatePortfolios ?? 0);

    const publicDeg = (publicValue / total) * 360;
    const friendsDeg = (friendsValue / total) * 360;
    const privateDeg = 360 - publicDeg - friendsDeg;

    return `conic-gradient(#22c55e 0deg ${publicDeg}deg, #7c5cff ${publicDeg}deg ${publicDeg + friendsDeg}deg, #ff9f1c ${publicDeg + friendsDeg}deg ${publicDeg + friendsDeg + privateDeg}deg)`;
  }

  protected totalVisibilityCount(): number {
    return Number(this.overview?.publicPortfolios ?? 0) + Number(this.overview?.friendsOnlyPortfolios ?? 0) + Number(this.overview?.privatePortfolios ?? 0);
  }

  protected linePath(key: 'portfolios' | 'projects' | 'collections'): string {
    const points = this.activityRows;
    if (points.length === 0) {
      return '';
    }

    const width = 640;
    const height = 220;
    const padding = 24;
    const maxValue = Math.max(1, ...points.flatMap((point) => [point.portfolios, point.projects, point.collections]));
    return points
      .map((point, index) => {
        const x = padding + (index * (width - padding * 2)) / Math.max(points.length - 1, 1);
        const value = point[key];
        const y = height - padding - ((value / maxValue) * (height - padding * 2));
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
  }

  protected chartPointX(index: number): number {
    const width = 640;
    const padding = 24;
    return padding + (index * (width - padding * 2)) / Math.max(this.activityRows.length - 1, 1);
  }

  protected chartPointY(value: number): number {
    const height = 220;
    const padding = 24;
    const maxValue = Math.max(1, ...this.activityRows.flatMap((point) => [point.portfolios, point.projects, point.collections]));
    return height - padding - ((value / maxValue) * (height - padding * 2));
  }

  protected visibilityTone(value: string): string {
    return value === 'PUBLIC' ? 'tone-public' : value === 'FRIENDS_ONLY' ? 'tone-friends' : 'tone-private';
  }

  protected moderationTone(value: string): string {
    return value === 'ACTIVE' ? 'tone-active' : value === 'UNDER_REVIEW' ? 'tone-review' : 'tone-blocked';
  }

  protected projectSummary(): string {
    if (this.projectTotal === 0) {
      return 'Showing 0 projects';
    }

    const start = this.projectPage * this.projectSize + 1;
    const end = Math.min((this.projectPage + 1) * this.projectSize, this.projectTotal);
    return `Showing ${start} to ${end} of ${this.projectTotal} projects`;
  }

  protected filteredProjectRows(): ProjectRowView[] {
    if (this.selectedDateRange === 'all') {
      return this.projectRows;
    }

    const days = this.selectedDateRange === '7d' ? 7 : this.selectedDateRange === '30d' ? 30 : 90;
    const cutoff = new Date();
    cutoff.setDate(cutoff.getDate() - days);

    return this.projectRows.filter((row) => !row.createdAt || new Date(row.createdAt) >= cutoff);
  }

  private loadProjects(): void {
    this.projectsLoading = true;
    this.projectsError = '';
    this.openActionMenuId = null;

    this.api
      .getProjects({
        q: this.projectSearch.trim() || undefined,
        visibility: this.selectedVisibility || undefined,
        moderationStatus: this.selectedStatus || undefined,
        page: this.projectPage,
        size: this.projectSize,
        sort: this.selectedSort,
      })
      .pipe(
        catchError(() => {
          this.projectsError = 'Projects could not be loaded.';
          this.projectsLoading = false;
          return of(null);
        }),
      )
      .subscribe((result) => {
        if (!result) {
          return;
        }

        this.projectRows = this.normalizeProjects(result.items ?? []);
        this.projectTotal = Number(result.total ?? 0);
        this.projectPage = Number(result.page ?? this.projectPage);
        this.projectSize = Number(result.size ?? this.projectSize);
        this.projectTotalPages = Number(result.totalPages ?? 0);
        this.projectsLoading = false;
      });
  }

  private normalizeActivity(rows: JihenPortfolioAdminModels.ActivityPoint[]): ActivityView[] {
    return (rows ?? []).map((row, index) => ({
      label: row.label?.trim() || row.day?.trim() || row.date?.slice(5) || `Day ${index + 1}`,
      portfolios: Number(row.portfolios ?? 0),
      projects: Number(row.projects ?? 0),
      collections: Number(row.collections ?? 0),
    }));
  }

  private normalizeTrendingSkills(rows: JihenPortfolioAdminModels.TrendingSkillItem[]): TrendingSkillView[] {
    const normalized = (rows ?? [])
      .map((row, index) => ({
        rank: index + 1,
        name: row.skillName?.trim() || row.name?.trim() || `Skill ${index + 1}`,
        usageCount: Number(row.usageCount ?? row.count ?? 0),
        progress: 0,
      }))
      .slice(0, 5);

    const maxValue = Math.max(1, ...normalized.map((item) => item.usageCount));
    return normalized.map((item) => ({ ...item, progress: Math.max(12, Math.round((item.usageCount / maxValue) * 100)) }));
  }

  private normalizeRecentItems(rows: JihenPortfolioAdminModels.RecentItem[]): RecentItemView[] {
    return (rows ?? []).map((row) => ({
      id: row.id ?? row.itemId ?? null,
      title: row.title?.trim() || row.name?.trim() || 'Untitled item',
      type: row.type?.trim() || row.itemType?.trim() || 'Item',
      owner: row.owner?.trim() || row.ownerName?.trim() || row.ownerUsername?.trim() || 'Unknown owner',
      visibility: (row.visibility ?? 'PRIVATE') as string,
      status: String(row.moderationStatus ?? row.status ?? 'ACTIVE'),
      views: Number(row.views ?? 0),
      createdAt: row.createdAt ?? null,
    }));
  }

  private normalizeProjects(rows: JihenPortfolioAdminModels.AdminProjectItem[]): ProjectRowView[] {
    return (rows ?? []).map((row, index) => ({
      id: Number(row.projectId ?? row.id ?? index + 1),
      title: row.title?.trim() || 'Untitled project',
      description: row.description?.trim() || 'No project description available.',
      thumbnailUrl: row.thumbnailUrl?.trim() || row.mediaUrl?.trim() || '',
      owner: row.owner?.trim() || row.ownerName?.trim() || row.ownerUsername?.trim() || 'Unknown owner',
      portfolio: row.portfolio?.trim() || row.portfolioTitle?.trim() || 'Untitled portfolio',
      visibility: this.normalizeVisibility(row.visibility),
      status: this.normalizeModeration(row.moderationStatus ?? row.status),
      views: Number(row.views ?? 0),
      likes: Number(row.likes ?? 0),
      skills: (row.skills ?? row.topSkills ?? []).map((skill) => skill.name?.trim()).filter((value): value is string => Boolean(value)).slice(0, 4),
      createdAt: row.createdAt ?? null,
    }));
  }

  private normalizeVisibility(value: string | null | undefined): JihenPortfolioAdminModels.ProjectVisibility {
    return value === 'PUBLIC' || value === 'FRIENDS_ONLY' ? value : 'PRIVATE';
  }

  private normalizeModeration(value: string | null | undefined): JihenPortfolioAdminModels.ModerationStatus {
    return value === 'ACTIVE' || value === 'UNDER_REVIEW' ? value : 'BLOCKED';
  }

  private resolveGrowth(overview: JihenPortfolioAdminModels.OverviewResponse, field: keyof JihenPortfolioAdminModels.OverviewResponse): number | null {
    const directValue = overview[field];
    if (typeof directValue === 'number') {
      return directValue;
    }

    const growthKey = String(field).replace(/Pct$/, '').replace(/[A-Z]/g, (char) => `_${char.toLowerCase()}`);
    const nestedValue = overview.growth?.[growthKey] ?? overview.growth?.[String(field)];
    return typeof nestedValue === 'number' ? nestedValue : null;
  }
}
