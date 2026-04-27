import { CommonModule, Location } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import type {
  ExploreCollectionCardDto,
  ExplorePortfolioDetailDto,
  ExploreProjectCardDto,
  PortfolioVisibility,
  SkillSummaryDto,
} from '../../core/models/api.models';
import { ExploreService } from '../../core/services/explore.service';

type SkillGroupView = {
  category: string;
  skills: SkillSummaryDto[];
};

type PortfolioSection = 'overview' | 'projects' | 'collections' | 'skills' | 'activity';

@Component({
  selector: 'app-explore-portfolio-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './explore-portfolio-detail.component.html',
  styleUrl: './explore-portfolio-detail.component.scss',
})
export class ExplorePortfolioDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly api = inject(ExploreService);

  loading = true;
  errorMessage = '';
  activeSection: PortfolioSection = 'overview';
  detail: ExplorePortfolioDetailDto | null = null;
  portfolio: ExplorePortfolioDetailDto['portfolio'] | null = null;
  groupedSkills: SkillGroupView[] = [];
  visibleProjects: ExploreProjectCardDto[] = [];
  visibleCollections: ExploreCollectionCardDto[] = [];
  private readonly loggedProjectMediaIds = new Set<number>();

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map((params) => Number(params.get('id'))),
        switchMap((portfolioId) => {
          if (!Number.isFinite(portfolioId) || portfolioId <= 0) {
            this.errorMessage = 'Portfolio not available.';
            this.loading = false;
            return of(null);
          }

          this.loading = true;
          this.errorMessage = '';

          return forkJoin({
            detail: this.api.getPortfolioDetail(portfolioId),
            collections: this.api.getPortfolioCollections(portfolioId).pipe(catchError(() => of([]))),
          }).pipe(
            catchError((error) => {
              this.errorMessage = this.resolveErrorMessage(error?.status);
              this.loading = false;
              return of(null);
            }),
          );
        }),
      )
      .subscribe((result) => {
        if (!result) {
          return;
        }

        this.detail = result.detail;
        this.portfolio = result.detail.portfolio ?? null;
        this.groupedSkills = this.normalizeSkillGroups(result.detail);
        this.visibleProjects = this.normalizeProjects(result.detail);
        this.visibleCollections = this.normalizeCollections(result.detail, result.collections);
        console.log('Loaded portfolio detail:', this.portfolio);
        console.log('Portfolio id for 3D:', this.portfolio?.id);
        this.loading = false;
      });
  }

  backToExplore(): void {
    this.location.back();
  }

  selectSection(section: PortfolioSection): void {
    this.activeSection = section;
  }

  open3dPortfolio(): void {
    const portfolioId = this.portfolio?.id;
    if (!portfolioId) {
      return;
    }
    this.router.navigate(['/jihen-portfolio-3d', portfolioId]);
  }

  openProject(projectId: number | null | undefined): void {
    if (!projectId) {
      return;
    }
    this.router.navigate(['/explore/projects', projectId]);
  }

  openCollection(collectionId: number | null | undefined): void {
    if (!collectionId) {
      return;
    }
    this.router.navigate(['/explore/collections', collectionId]);
  }

  onProjectKeydown(event: KeyboardEvent, projectId: number | null | undefined): void {
    if (event.key !== 'Enter' && event.key !== ' ') {
      return;
    }
    event.preventDefault();
    this.openProject(projectId);
  }

  onCollectionKeydown(event: KeyboardEvent, collectionId: number | null | undefined): void {
    if (event.key !== 'Enter' && event.key !== ' ') {
      return;
    }
    event.preventDefault();
    this.openCollection(collectionId);
  }

  visibilityBadge(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'Friends' : value === 'PUBLIC' ? 'Public' : 'Private';
  }

  visibilityBadgeClass(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'detail-badge detail-badge--friends' : 'detail-badge';
  }

  displayName(): string {
    const detail = this.detail;
    if (!detail) {
      return '';
    }

    return (
      detail.displayName?.trim()
      || detail.fullName?.trim()
      || [detail.profile?.firstName, detail.profile?.lastName].filter(Boolean).join(' ').trim()
      || detail.ownerUsername?.trim()
      || detail.owner?.username?.trim()
      || 'Scholar user'
    );
  }

  username(): string {
    return this.detail?.ownerUsername?.trim() || this.detail?.owner?.username?.trim() || '';
  }

  headline(): string {
    const detail = this.detail;
    const rawHeadline =
      detail?.portfolio?.job?.trim()
      || detail?.jobTitle?.trim()
      || detail?.headline?.trim()
      || detail?.portfolioTitle?.trim()
      || detail?.title?.trim()
      || '';

    if (!rawHeadline || rawHeadline.toLowerCase() === this.displayName().trim().toLowerCase()) {
      return 'Software Engineer';
    }

    return rawHeadline;
  }

  locationText(): string {
    const detail = this.detail;
    if (!detail) {
      return 'Remote';
    }

    const primary = detail.location?.trim() || detail.profile?.location?.trim();
    if (primary) {
      return primary;
    }

    const cityCountry = [detail.city || detail.profile?.city, detail.country || detail.profile?.country].filter(Boolean).join(', ').trim();
    return cityCountry || 'Remote';
  }

  aboutText(): string {
    const detail = this.detail;
    return detail?.about?.trim() || detail?.bio?.trim() || detail?.portfolio?.bio?.trim() || detail?.profile?.description?.trim() || '';
  }

  profileImage(): string {
    const detailWithMedia = this.detail as (ExplorePortfolioDetailDto & { profileImageUrl?: string | null }) | null;
    const profileWithMedia = this.detail?.profile as ({
      profileImageUrl?: string | null;
      avatarUrl?: string | null;
      profilePicture?: string | null;
      user?: { profileImageUrl?: string | null } | null;
    } & object) | null | undefined;
    return (
      detailWithMedia?.profileImageUrl?.trim()
      || profileWithMedia?.profileImageUrl?.trim()
      || profileWithMedia?.avatarUrl?.trim()
      || profileWithMedia?.user?.profileImageUrl?.trim()
      || detailWithMedia?.profileImage?.trim()
      || profileWithMedia?.profilePicture?.trim()
      || this.detail?.portfolio?.coverImage?.trim()
      || ''
    );
  }

  githubUrl(): string {
    return this.detail?.githubUrl?.trim() || this.detail?.portfolio?.githubUrl?.trim() || '';
  }

  linkedinUrl(): string {
    return this.detail?.linkedinUrl?.trim() || this.detail?.portfolio?.linkedinUrl?.trim() || '';
  }

  emailAddress(): string {
    return this.detail?.owner?.email?.trim() || '';
  }

  emailHref(): string {
    const email = this.emailAddress();
    return email ? `mailto:${email}` : '';
  }

  totalViews(): number {
    return Number(this.detail?.totalViews ?? this.detail?.views ?? 0);
  }

  projectCount(): number {
    return Number(this.detail?.projectCount ?? this.visibleProjects.length);
  }

  collectionCount(): number {
    return Number(this.detail?.collectionCount ?? this.visibleCollections.length);
  }

  totalLikes(): number {
    return this.visibleProjects.reduce((sum, project) => sum + Number(project.likes ?? 0), 0);
  }

  isOpenToWork(): boolean {
    return Boolean(this.detail?.openToWork ?? this.detail?.portfolio?.openToWork);
  }

  isFreelance(): boolean {
    return Boolean(this.detail?.availableForFreelance ?? this.detail?.freelance ?? this.detail?.portfolio?.availableForFreelance);
  }

  featuredProject(): ExploreProjectCardDto | null {
    return this.visibleProjects.find((project) => this.isPinnedProject(project)) ?? this.visibleProjects[0] ?? null;
  }

  remainingProjects(): ExploreProjectCardDto[] {
    const featured = this.featuredProject();
    return this.visibleProjects.filter((project) => project !== featured);
  }

  projectSkills(project: ExploreProjectCardDto): SkillSummaryDto[] {
    return (project.topSkills ?? []).slice(0, 4);
  }

  projectImage(project: ExploreProjectCardDto): string {
    this.logProjectMedia(project);
    if (this.projectVideo(project)) {
      return '';
    }

    const projectWithMedia = project as ExploreProjectCardDto & {
      imageUrl?: string | null;
      thumbnailUrl?: string | null;
      mediaUrl?: string | null;
    };

    return projectWithMedia.imageUrl?.trim() || projectWithMedia.thumbnailUrl?.trim() || projectWithMedia.mediaUrl?.trim() || '';
  }

  projectVideo(project: ExploreProjectCardDto): string {
    this.logProjectMedia(project);
    const projectWithMedia = project as ExploreProjectCardDto & {
      videoUrl?: string | null;
      mediaUrl?: string | null;
      mediaType?: string | null;
    };

    if (projectWithMedia.videoUrl?.trim()) {
      return projectWithMedia.videoUrl.trim();
    }

    if (projectWithMedia.mediaType === 'VIDEO' && projectWithMedia.mediaUrl?.trim()) {
      return projectWithMedia.mediaUrl.trim();
    }

    return '';
  }

  projectDescription(project: ExploreProjectCardDto): string {
    return project.description?.trim() || 'No project description available.';
  }

  projectViews(project: ExploreProjectCardDto): number | null {
    return project.views ?? null;
  }

  projectLikes(project: ExploreProjectCardDto): number | null {
    return project.likes ?? null;
  }

  collectionTitle(collection: ExploreCollectionCardDto): string {
    return collection.name?.trim() || collection.title?.trim() || 'Untitled collection';
  }

  collectionDescription(collection: ExploreCollectionCardDto): string {
    return collection.description?.trim() || 'No collection description available.';
  }

  collectionSkills(collection: ExploreCollectionCardDto): SkillSummaryDto[] {
    return (collection.topSkills ?? collection.skills ?? []).slice(0, 4);
  }

  collectionPreviewImage(collection: ExploreCollectionCardDto): string {
    return collection.mediaUrl?.trim() || '';
  }

  collectionProjectCount(collection: ExploreCollectionCardDto): number {
    return Number(collection.projectCount ?? 0);
  }

  topSkillChips(limit = 6): SkillSummaryDto[] {
    return this.groupedSkills.flatMap((group) => group.skills).slice(0, limit);
  }

  highlightItems(): string[] {
    const skillNames = this.groupedSkills.flatMap((group) => group.skills.map((skill) => skill.name)).filter(Boolean);
    const highlights = [
      this.pickHighlight(skillNames, ['API', 'REST', 'SPRING', 'NODE', 'BACKEND'], 'API design'),
      this.pickHighlight(skillNames, ['ARCHITECTURE', 'SYSTEM', 'MICROSERVICE'], 'Clean architecture'),
      this.pickHighlight(skillNames, ['DOCKER', 'CI/CD', 'AWS', 'KUBERNETES', 'DEVOPS'], 'Delivery and DevOps'),
    ].filter((item): item is string => Boolean(item)).filter((item, index, array) => array.indexOf(item) === index);

    return highlights.length > 0 ? highlights : ['Problem solving', 'Product thinking', 'Cross-functional collaboration'];
  }

  availabilityItems(): string[] {
    const items: string[] = [];
    if (this.isOpenToWork()) {
      items.push('Open to work');
    }
    if (this.isFreelance()) {
      items.push('Available for freelance');
    }
    if (this.locationText().toLowerCase().includes('remote')) {
      items.push('Remote friendly');
    }
    return items.length > 0 ? items : ['Availability not specified'];
  }

  activityItems(): Array<{ title: string; meta: string }> {
    const items: Array<{ title: string; meta: string }> = [];

    for (const project of this.visibleProjects.slice(0, 4)) {
      items.push({
        title: project.title?.trim() || 'Project published',
        meta: `${Number(project.views ?? 0)} views • ${Number(project.likes ?? 0)} likes`,
      });
    }

    for (const collection of this.visibleCollections.slice(0, 2)) {
      items.push({
        title: this.collectionTitle(collection),
        meta: `${this.collectionProjectCount(collection)} projects`,
      });
    }

    return items;
  }

  private normalizeSkillGroups(detail: ExplorePortfolioDetailDto): SkillGroupView[] {
    const groups = detail.skillsByCategory ?? [];
    if (groups.length > 0) {
      return groups
        .map((group) => ({
          category: this.formatCategoryName(group.category?.trim() || 'General'),
          skills: group.skills ?? [],
        }))
        .filter((group) => group.skills.length > 0);
    }

    const flatSkills = detail.skills ?? detail.portfolio?.skills ?? [];
    const mapByCategory = new Map<string, SkillSummaryDto[]>();
    for (const skill of flatSkills) {
      const category = this.formatCategoryName(skill.category?.trim() || 'General');
      const bucket = mapByCategory.get(category) ?? [];
      bucket.push(skill);
      mapByCategory.set(category, bucket);
    }

    return Array.from(mapByCategory.entries()).map(([category, skills]) => ({ category, skills }));
  }

  private normalizeProjects(detail: ExplorePortfolioDetailDto): ExploreProjectCardDto[] {
    const directProjects = detail.projects ?? [];
    return directProjects;
  }

  private normalizeCollections(detail: ExplorePortfolioDetailDto, fromCollectionsEndpoint: ExploreCollectionCardDto[]): ExploreCollectionCardDto[] {
    if (fromCollectionsEndpoint.length > 0) {
      return fromCollectionsEndpoint;
    }

    return detail.collections ?? [];
  }

  private formatCategoryName(value: string): string {
    return value
      .replace(/[_-]+/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }

  private isPinnedProject(project: ExploreProjectCardDto): boolean {
    const pinned = (project as ExploreProjectCardDto & { pinned?: boolean | null }).pinned;
    return pinned === true;
  }

  private logProjectMedia(project: ExploreProjectCardDto): void {
    const projectId = Number(project.projectId ?? 0);
    if (!projectId || this.loggedProjectMediaIds.has(projectId)) {
      return;
    }

    this.loggedProjectMediaIds.add(projectId);
    console.log('Project media', project);
  }

  private pickHighlight(skillNames: string[], matches: string[], fallback: string): string | null {
    return skillNames.some((skillName) => matches.some((match) => skillName.toUpperCase().includes(match))) ? fallback : null;
  }

  private resolveErrorMessage(status: number | undefined): string {
    return status === 403 ? 'You do not have permission to view this.' : 'Portfolio not available.';
  }
}
