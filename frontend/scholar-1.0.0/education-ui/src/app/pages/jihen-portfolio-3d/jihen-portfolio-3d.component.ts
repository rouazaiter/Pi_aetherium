import { CommonModule, Location } from '@angular/common';
import { Component, HostListener, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import type {
  ExploreCollectionCardDto,
  ExplorePortfolioDetailDto,
  ExploreProjectCardDto,
  SkillSummaryDto,
} from '../../core/models/api.models';
import { ExploreService } from '../../core/services/explore.service';

type Jihen3dSection = 'hero' | 'about' | 'skills' | 'projects' | 'collections' | 'contact';

type JihenSkillGroupView = {
  category: string;
  skills: SkillSummaryDto[];
};

type TourStep = {
  section: Jihen3dSection;
  projectIndex?: number;
  collectionIndex?: number;
};

@Component({
  selector: 'app-jihen-portfolio-3d',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './jihen-portfolio-3d.component.html',
  styleUrl: './jihen-portfolio-3d.component.scss',
})
export class JihenPortfolio3dComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly api = inject(ExploreService);

  protected loading = true;
  protected errorMessage = '';
  protected detail: ExplorePortfolioDetailDto | null = null;
  protected collections: ExploreCollectionCardDto[] = [];
  protected projects: ExploreProjectCardDto[] = [];
  protected groupedSkills: JihenSkillGroupView[] = [];

  protected activeSection: Jihen3dSection = 'hero';
  protected activeProjectIndex = 0;
  protected activeCollectionIndex = 0;

  protected sceneRotateX = -7;
  protected sceneRotateY = 10;
  protected sceneDepth = 0;

  protected profileOpen = false;
  protected tourRunning = false;
  protected tourPaused = false;
  protected tourProgressIndex = 0;

  private tourTimer: ReturnType<typeof window.setInterval> | null = null;

  readonly sections: Array<{ key: Jihen3dSection; label: string }> = [
    { key: 'hero', label: 'Hero' },
    { key: 'about', label: 'About' },
    { key: 'skills', label: 'Skills' },
    { key: 'projects', label: 'Projects' },
    { key: 'collections', label: 'Collections' },
    { key: 'contact', label: 'Contact' },
  ];

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map((params) => Number(params.get('id'))),
        switchMap((portfolioId) => {
          if (!Number.isFinite(portfolioId) || portfolioId <= 0) {
            this.loading = false;
            this.errorMessage = 'Portfolio not available.';
            return of(null);
          }

          this.loading = true;
          this.errorMessage = '';

          return forkJoin({
            detail: this.api.getPortfolioDetail(portfolioId),
            collections: this.api.getPortfolioCollections(portfolioId).pipe(catchError(() => of([]))),
          }).pipe(
            catchError((error) => {
              this.loading = false;
              this.errorMessage =
                error?.status === 403
                  ? 'You do not have permission to view this.'
                  : 'Portfolio not available.';
              return of(null);
            }),
          );
        }),
      )
      .subscribe((result) => {
        if (!result) return;

        this.detail = result.detail;
        this.collections = result.collections.length > 0 ? result.collections : (result.detail.collections ?? []);
        this.projects = result.detail.projects ?? [];
        this.groupedSkills = this.normalizeSkillGroups(result.detail);
        this.loading = false;
      });
  }

  ngOnDestroy(): void {
    this.clearTourTimer();
  }

  protected backToPortfolio(): void {
    this.location.back();
  }

  protected selectSection(section: Jihen3dSection): void {
    this.stopTour(false);
    this.activeSection = section;
    this.sceneDepth = this.sectionDepth(section);
  }

  protected openProfile(): void {
    this.profileOpen = true;
  }

  protected closeProfile(): void {
    this.profileOpen = false;
  }

  protected startTour(): void {
    this.tourRunning = true;
    this.tourPaused = false;
    this.tourProgressIndex = 0;
    this.applyTourStep();
    this.clearTourTimer();

    this.tourTimer = window.setInterval(() => {
      if (!this.tourPaused) {
        this.nextTourStep();
      }
    }, 6500);
  }

  protected pauseTour(): void {
    this.tourPaused = true;
  }

  protected resumeTour(): void {
    this.tourPaused = false;
  }

  protected stopTour(reset = true): void {
    this.tourRunning = false;
    this.tourPaused = false;
    this.clearTourTimer();

    if (reset) {
      this.tourProgressIndex = 0;
      this.activeSection = 'hero';
      this.activeProjectIndex = 0;
      this.activeCollectionIndex = 0;
      this.sceneDepth = 0;
    }
  }

  protected nextTourStep(): void {
    const steps = this.tourSteps();
    if (steps.length === 0) return;

    this.tourProgressIndex = (this.tourProgressIndex + 1) % steps.length;
    this.applyTourStep();
  }

  protected previousTourStep(): void {
    const steps = this.tourSteps();
    if (steps.length === 0) return;

    this.tourProgressIndex = this.tourProgressIndex === 0 ? steps.length - 1 : this.tourProgressIndex - 1;
    this.applyTourStep();
  }

  protected onSceneMove(event: MouseEvent): void {
    const element = event.currentTarget as HTMLElement | null;
    if (!element) return;

    const rect = element.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width - 0.5;
    const y = (event.clientY - rect.top) / rect.height - 0.5;

    this.sceneRotateY = 10 + x * 9;
    this.sceneRotateX = -7 - y * 7;
  }

  protected resetScene(): void {
    this.sceneRotateX = -7;
    this.sceneRotateY = 10;
  }

  @HostListener('window:keydown.escape')
  protected onEscape(): void {
    if (this.profileOpen) {
      this.closeProfile();
      return;
    }

    this.backToPortfolio();
  }

  protected displayName(): string {
    const detail = this.detail;
    if (!detail) return '';

    return (
      detail.displayName?.trim() ||
      detail.fullName?.trim() ||
      [detail.profile?.firstName, detail.profile?.lastName].filter(Boolean).join(' ').trim() ||
      detail.ownerUsername?.trim() ||
      detail.owner?.username?.trim() ||
      'Scholar user'
    );
  }

  protected initials(): string {
    return (
      this.displayName()
        .split(/\s+/)
        .filter(Boolean)
        .slice(0, 2)
        .map((part) => part.charAt(0))
        .join('')
        .toUpperCase() || 'SC'
    );
  }

  protected headline(): string {
    const detail = this.detail;
    return (
      detail?.portfolio?.job?.trim() ||
      detail?.jobTitle?.trim() ||
      detail?.headline?.trim() ||
      detail?.portfolioTitle?.trim() ||
      'Software Engineer'
    );
  }

  protected locationText(): string {
    const detail = this.detail;
    if (!detail) return 'Remote';

    return (
      detail.location?.trim() ||
      detail.profile?.location?.trim() ||
      [detail.city || detail.profile?.city, detail.country || detail.profile?.country].filter(Boolean).join(', ') ||
      'Remote'
    );
  }

  protected profileImage(): string {
    const detailWithMedia = this.detail as (ExplorePortfolioDetailDto & {
      profileImageUrl?: string | null;
      profileImage?: string | null;
    }) | null;

    const profileWithMedia = this.detail?.profile as ({
      profileImageUrl?: string | null;
      avatarUrl?: string | null;
      profilePicture?: string | null;
      user?: { profileImageUrl?: string | null } | null;
    } & object) | null | undefined;

    return (
      detailWithMedia?.profileImageUrl?.trim() ||
      profileWithMedia?.profileImageUrl?.trim() ||
      profileWithMedia?.avatarUrl?.trim() ||
      profileWithMedia?.user?.profileImageUrl?.trim() ||
      detailWithMedia?.profileImage?.trim() ||
      profileWithMedia?.profilePicture?.trim() ||
      ''
    );
  }

  protected aboutText(): string {
    return (
      this.detail?.about?.trim() ||
      this.detail?.bio?.trim() ||
      this.detail?.portfolio?.bio?.trim() ||
      this.detail?.profile?.description?.trim() ||
      'No story added yet.'
    );
  }

  protected githubUrl(): string {
    return this.detail?.githubUrl?.trim() || this.detail?.portfolio?.githubUrl?.trim() || '';
  }

  protected linkedinUrl(): string {
    return this.detail?.linkedinUrl?.trim() || this.detail?.portfolio?.linkedinUrl?.trim() || '';
  }

  protected emailHref(): string {
    const email = this.detail?.owner?.email?.trim() || '';
    return email ? `mailto:${email}` : '';
  }

  protected topSkills(): SkillSummaryDto[] {
    return this.groupedSkills.flatMap((group) => group.skills).slice(0, 12);
  }

  protected activeProject(): ExploreProjectCardDto | null {
    return this.projects[this.activeProjectIndex] ?? this.projects[0] ?? null;
  }

  protected activeCollection(): ExploreCollectionCardDto | null {
    return this.collections[this.activeCollectionIndex] ?? this.collections[0] ?? null;
  }

  protected projectImage(project: ExploreProjectCardDto | null): string {
    if (!project) return '';

    const projectWithMedia = project as ExploreProjectCardDto & {
      imageUrl?: string | null;
      thumbnailUrl?: string | null;
      mediaUrl?: string | null;
    };

    return (
      projectWithMedia.thumbnailUrl?.trim() ||
      projectWithMedia.imageUrl?.trim() ||
      projectWithMedia.mediaUrl?.trim() ||
      ''
    );
  }

  protected projectVideo(project: ExploreProjectCardDto | null): string {
    if (!project) return '';

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

  protected projectKeywords(project: ExploreProjectCardDto | null): string[] {
    if (!project) return [];

    const skills = (project.topSkills ?? []).map((skill) => skill.name).filter(Boolean);
    if (skills.length > 0) return skills.slice(0, 5);

    return (project.title || '')
      .split(/\s+/)
      .filter((word) => word.length > 2)
      .slice(0, 5);
  }

  protected shortProjectSummary(project: ExploreProjectCardDto | null): string {
    const text = project?.description?.trim() || '';
    if (!text) return 'Visual project showcase.';

    return text.length > 90 ? `${text.slice(0, 90).trim()}...` : text;
  }

  protected collectionPreviewImage(collection: ExploreCollectionCardDto | null): string {
    if (!collection) return '';

    const collectionWithProjects = collection as ExploreCollectionCardDto & {
      imageUrl?: string | null;
      thumbnailUrl?: string | null;
      projects?: Array<ExploreProjectCardDto & {
        thumbnailUrl?: string | null;
        imageUrl?: string | null;
        mediaUrl?: string | null;
      }>;
    };

    if (collectionWithProjects.thumbnailUrl?.trim()) return collectionWithProjects.thumbnailUrl.trim();
    if (collectionWithProjects.imageUrl?.trim()) return collectionWithProjects.imageUrl.trim();

    const firstProject = collectionWithProjects.projects?.[0];
    return (
      firstProject?.thumbnailUrl?.trim() ||
      firstProject?.imageUrl?.trim() ||
      firstProject?.mediaUrl?.trim() ||
      ''
    );
  }

  protected collectionProjectCount(collection: ExploreCollectionCardDto | null): number {
    if (!collection) return 0;

    const withProjects = collection as ExploreCollectionCardDto & {
      projectCount?: number | null;
      projects?: unknown[];
    };

    return withProjects.projectCount ?? withProjects.projects?.length ?? 0;
  }

  protected collectionKeywords(collection: ExploreCollectionCardDto | null): string[] {
    if (!collection) return [];

    const withProjects = collection as ExploreCollectionCardDto & {
      projects?: Array<ExploreProjectCardDto & { topSkills?: SkillSummaryDto[] }>;
    };

    const names = new Set<string>();

    for (const project of withProjects.projects ?? []) {
      for (const skill of project.topSkills ?? []) {
        if (skill.name) names.add(skill.name);
      }
    }

    return Array.from(names).slice(0, 5);
  }

  protected skillLogo(skill: SkillSummaryDto): string {
    const key = this.skillKey(skill);
    const map: Record<string, string> = {
      angular: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/angularjs/angularjs-original.svg',
      react: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/react/react-original.svg',
      java: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg',
      python: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/python/python-original.svg',
      mongodb: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mongodb/mongodb-original.svg',
      mysql: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg',
      springboot: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg',
      spring: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg',
      git: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/git/git-original.svg',
      postman: 'https://www.vectorlogo.zone/logos/getpostman/getpostman-icon.svg',
      nodejs: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/nodejs/nodejs-original.svg',
      typescript: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg',
      docker: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg',
      kubernetes: 'https://cdn.jsdelivr.net/gh/devicons/devicon/icons/kubernetes/kubernetes-plain.svg',
      openstack: 'https://www.vectorlogo.zone/logos/openstack/openstack-icon.svg',
    };

    return map[key] || '';
  }

  protected skillInitial(skill: SkillSummaryDto): string {
    return (skill.name || '?').charAt(0).toUpperCase();
  }

 protected projectKey(project: ExploreProjectCardDto): string | number {
  return project.projectId ?? project.title ?? Math.random();
}

protected collectionKey(collection: ExploreCollectionCardDto): string | number {
  return collection.collectionId ?? collection.name ?? Math.random();
}

  protected skillKey(skill: SkillSummaryDto): string {
    const skillWithSlug = skill as SkillSummaryDto & { slug?: string | null; iconKey?: string | null };
    return (
      skillWithSlug.iconKey?.trim() ||
      skillWithSlug.slug?.trim() ||
      skill.name?.trim() ||
      ''
    )
      .toLowerCase()
      .replace(/[^a-z0-9]/g, '');
  }

  private applyTourStep(): void {
    const step = this.tourSteps()[this.tourProgressIndex];
    if (!step) return;

    this.activeSection = step.section;
    this.sceneDepth = this.sectionDepth(step.section);

    if (typeof step.projectIndex === 'number') {
      this.activeProjectIndex = step.projectIndex;
    }

    if (typeof step.collectionIndex === 'number') {
      this.activeCollectionIndex = step.collectionIndex;
    }
  }

  private tourSteps(): TourStep[] {
    const projectSteps: TourStep[] = this.projects.length
      ? this.projects.slice(0, 6).map((_, index) => ({ section: 'projects', projectIndex: index }))
      : [{ section: 'projects' }];

    const collectionSteps: TourStep[] = this.collections.length
      ? this.collections.slice(0, 5).map((_, index) => ({ section: 'collections', collectionIndex: index }))
      : [{ section: 'collections' }];

    return [
      { section: 'hero' },
      { section: 'about' },
      { section: 'skills' },
      ...projectSteps,
      ...collectionSteps,
      { section: 'contact' },
    ];
  }

  private sectionDepth(section: Jihen3dSection): number {
    const depthMap: Record<Jihen3dSection, number> = {
      hero: 0,
      about: -18,
      skills: -36,
      projects: -54,
      collections: -72,
      contact: -90,
    };

    return depthMap[section];
  }

  private clearTourTimer(): void {
    if (this.tourTimer) {
      window.clearInterval(this.tourTimer);
      this.tourTimer = null;
    }
  }

  private normalizeSkillGroups(detail: ExplorePortfolioDetailDto): JihenSkillGroupView[] {
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

  private formatCategoryName(value: string): string {
    return value
      .replace(/[_-]+/g, ' ')
      .toLowerCase()
      .replace(/\b\w/g, (char) => char.toUpperCase());
  }
}