import { CommonModule, Location } from '@angular/common';
import { Component, HostListener, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
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
  label: string;
  projectIndex?: number;
  collectionIndex?: number;
};

@Component({
  selector: 'app-jihen-portfolio-3d',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './jihen-portfolio-3d.component.html',
  styleUrl: './jihen-portfolio-3d.component.scss',
})
export class JihenPortfolio3dComponent implements OnInit, OnDestroy {
  private static readonly TOUR_STEP_DURATION_MS = 5000;
  private static readonly TOUR_TRANSITION_MS = 900;

  private readonly route = inject(ActivatedRoute);
  private readonly location = inject(Location);
  private readonly api = inject(ExploreService);

  protected loading = true;
  protected errorMessage = '';
  protected detail: ExplorePortfolioDetailDto | null = null;
  protected collections: ExploreCollectionCardDto[] = [];
  protected projects: ExploreProjectCardDto[] = [];
  protected groupedSkills: JihenSkillGroupView[] = [];

  protected tourMode = false;
  protected tourRunning = false;
  protected tourPaused = false;
  protected tourProgressIndex = 0;
  protected activeSection: Jihen3dSection = 'hero';
  protected activeProjectIndex = 0;
  protected activeCollectionIndex = 0;
  protected transitioning = false;
  protected transitionFromStep: TourStep | null = null;
  protected transitionToStep: TourStep | null = null;

  protected sceneRotateX = -5;
  protected sceneRotateY = 8;

  private tourTimer: ReturnType<typeof window.setTimeout> | null = null;
  private transitionTimer: ReturnType<typeof window.setTimeout> | null = null;

  protected readonly sections: Array<{ key: Jihen3dSection; label: string; description: string }> = [
    { key: 'hero', label: 'Hero / Profile', description: 'Identity, role, and first impression.' },
    { key: 'about', label: 'About', description: 'A concise story about the portfolio owner.' },
    { key: 'skills', label: 'Skills', description: 'Core stacks and strongest tools.' },
    { key: 'projects', label: 'Projects', description: 'Projects appear one by one in full focus.' },
    { key: 'collections', label: 'Collections', description: 'Collections appear one by one in sequence.' },
    { key: 'contact', label: 'Contact', description: 'Final call to connect and collaborate.' },
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
              this.errorMessage = error?.status === 403 ? 'You do not have permission to view this.' : 'Portfolio not available.';
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
        this.collections = result.collections.length > 0 ? result.collections : (result.detail.collections ?? []);
        this.projects = result.detail.projects ?? [];
        this.groupedSkills = this.normalizeSkillGroups(result.detail);
        this.loading = false;
        this.applyTourStep();
      });
  }

  ngOnDestroy(): void {
    this.clearTourTimer();
    this.clearTransitionTimer();
  }

  protected backToPortfolio(): void {
    this.location.back();
  }

  protected startTour(): void {
    this.tourMode = true;
    this.tourRunning = true;
    this.tourPaused = false;
    this.tourProgressIndex = 0;
    this.applyTourStep();
    this.scheduleNextStep();
  }

  protected pauseTour(): void {
    this.tourPaused = true;
    this.clearTourTimer();
  }

  protected resumeTour(): void {
    if (!this.tourMode) {
      this.startTour();
      return;
    }

    this.tourRunning = true;
    this.tourPaused = false;
    this.scheduleNextStep();
  }

  protected resetTour(): void {
    this.clearTourTimer();
    this.clearTransitionTimer();
    this.tourMode = true;
    this.tourRunning = false;
    this.tourPaused = false;
    this.transitioning = false;
    this.transitionFromStep = null;
    this.transitionToStep = null;
    this.tourProgressIndex = 0;
    this.applyTourStep();
  }

  protected exitTour(): void {
    this.clearTourTimer();
    this.clearTransitionTimer();
    this.tourMode = false;
    this.tourRunning = false;
    this.tourPaused = false;
    this.transitioning = false;
    this.transitionFromStep = null;
    this.transitionToStep = null;
    this.tourProgressIndex = 0;
    this.activeSection = 'hero';
    this.activeProjectIndex = 0;
    this.activeCollectionIndex = 0;
    this.resetScene();
  }

  protected nextTourStep(): void {
    if (this.transitioning) {
      return;
    }

    const steps = this.tourSteps();
    if (steps.length === 0) {
      return;
    }

    if (this.tourProgressIndex >= steps.length - 1) {
      this.pauseTour();
      return;
    }

    this.beginTransitionToIndex(this.tourProgressIndex + 1);
  }

  protected previousTourStep(): void {
    if (this.transitioning) {
      return;
    }

    const steps = this.tourSteps();
    if (steps.length === 0) {
      return;
    }

    this.clearTourTimer();
    this.beginTransitionToIndex(Math.max(this.tourProgressIndex - 1, 0));
  }

  protected goToStep(index: number): void {
    if (this.transitioning) {
      return;
    }

    const steps = this.tourSteps();
    if (index < 0 || index >= steps.length) {
      return;
    }

    this.tourMode = true;
    this.clearTourTimer();
    if (index === this.tourProgressIndex) {
      return;
    }

    this.beginTransitionToIndex(index);
  }

  protected selectSection(section: Jihen3dSection): void {
    const index = this.tourSteps().findIndex((step) => step.section === section);
    if (index === -1) {
      return;
    }

    this.goToStep(index);
  }

  protected onSceneMove(event: MouseEvent): void {
    if (!this.tourMode) {
      return;
    }

    const element = event.currentTarget as HTMLElement | null;
    if (!element) {
      return;
    }

    const rect = element.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width - 0.5;
    const y = (event.clientY - rect.top) / rect.height - 0.5;

    this.sceneRotateY = 8 + x * 8;
    this.sceneRotateX = -5 - y * 7;
  }

  protected resetScene(): void {
    this.sceneRotateX = -5;
    this.sceneRotateY = 8;
  }

  @HostListener('window:keydown.escape')
  protected onEscape(): void {
    if (this.tourMode) {
      this.exitTour();
      return;
    }

    this.backToPortfolio();
  }

  protected displayName(): string {
    const detail = this.detail;
    if (!detail) {
      return '';
    }

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
    if (!detail) {
      return 'Remote';
    }

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

  protected aboutLead(): string {
    const text = this.aboutText();
    return text.length > 240 ? `${text.slice(0, 240).trim()}...` : text;
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

  protected previewSkills(): SkillSummaryDto[] {
    return this.groupedSkills.flatMap((group) => group.skills).slice(0, 8);
  }

  protected activeProject(): ExploreProjectCardDto | null {
    return this.projects[this.activeProjectIndex] ?? this.projects[0] ?? null;
  }

  protected activeCollection(): ExploreCollectionCardDto | null {
    return this.collections[this.activeCollectionIndex] ?? this.collections[0] ?? null;
  }

  protected projectForStep(step: TourStep | null): ExploreProjectCardDto | null {
    if (!step || step.section !== 'projects') {
      return null;
    }

    const index = typeof step.projectIndex === 'number' ? step.projectIndex : 0;
    return this.projects[index] ?? this.projects[0] ?? null;
  }

  protected collectionForStep(step: TourStep | null): ExploreCollectionCardDto | null {
    if (!step || step.section !== 'collections') {
      return null;
    }

    const index = typeof step.collectionIndex === 'number' ? step.collectionIndex : 0;
    return this.collections[index] ?? this.collections[0] ?? null;
  }

  protected projectImage(project: ExploreProjectCardDto | null): string {
    if (!project) {
      return '';
    }

    const projectWithMedia = project as ExploreProjectCardDto & {
      imageUrl?: string | null;
      thumbnailUrl?: string | null;
      mediaUrl?: string | null;
    };

    return projectWithMedia.thumbnailUrl?.trim() || projectWithMedia.imageUrl?.trim() || projectWithMedia.mediaUrl?.trim() || '';
  }

  protected projectVideo(project: ExploreProjectCardDto | null): string {
    if (!project) {
      return '';
    }

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
    if (!project) {
      return [];
    }

    const projectWithSkillNames = project as ExploreProjectCardDto & { skillNames?: string[] | null };
    const skills = (project.topSkills ?? []).map((skill) => skill.name?.trim()).filter((name): name is string => Boolean(name));
    if (skills.length > 0) {
      return skills.slice(0, 5);
    }

    return (projectWithSkillNames.skillNames ?? []).filter(Boolean).slice(0, 5);
  }

  protected shortProjectSummary(project: ExploreProjectCardDto | null): string {
    const text = project?.description?.trim() || '';
    if (!text) {
      return 'Immersive project showcase.';
    }

    return text.length > 110 ? `${text.slice(0, 110).trim()}...` : text;
  }

  protected collectionPreviewImage(collection: ExploreCollectionCardDto | null): string {
    if (!collection) {
      return '';
    }

    const collectionWithProjects = collection as ExploreCollectionCardDto & {
      imageUrl?: string | null;
      thumbnailUrl?: string | null;
      mediaUrl?: string | null;
      projects?: Array<
        ExploreProjectCardDto & {
          thumbnailUrl?: string | null;
          imageUrl?: string | null;
          mediaUrl?: string | null;
        }
      >;
    };

    if (collectionWithProjects.thumbnailUrl?.trim()) {
      return collectionWithProjects.thumbnailUrl.trim();
    }
    if (collectionWithProjects.imageUrl?.trim()) {
      return collectionWithProjects.imageUrl.trim();
    }
    if (collectionWithProjects.mediaUrl?.trim()) {
      return collectionWithProjects.mediaUrl.trim();
    }

    const firstProject = collectionWithProjects.projects?.[0];
    return firstProject?.thumbnailUrl?.trim() || firstProject?.imageUrl?.trim() || firstProject?.mediaUrl?.trim() || '';
  }

  protected collectionProjectCount(collection: ExploreCollectionCardDto | null): number {
    if (!collection) {
      return 0;
    }

    const withProjects = collection as ExploreCollectionCardDto & {
      projectCount?: number | null;
      projects?: unknown[];
    };

    return withProjects.projectCount ?? withProjects.projects?.length ?? 0;
  }

  protected collectionKeywords(collection: ExploreCollectionCardDto | null): string[] {
    if (!collection) {
      return [];
    }

    const withProjects = collection as ExploreCollectionCardDto & {
      skills?: SkillSummaryDto[] | null;
      topSkills?: SkillSummaryDto[] | null;
      projects?: Array<ExploreProjectCardDto & { topSkills?: SkillSummaryDto[] | null }>;
    };

    const keywords = new Set<string>();

    for (const skill of withProjects.topSkills ?? withProjects.skills ?? []) {
      if (skill.name?.trim()) {
        keywords.add(skill.name.trim());
      }
    }

    for (const project of withProjects.projects ?? []) {
      for (const skill of project.topSkills ?? []) {
        if (skill.name?.trim()) {
          keywords.add(skill.name.trim());
        }
      }
    }

    return Array.from(keywords).slice(0, 5);
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

  protected skillKey(skill: SkillSummaryDto): string {
    const skillWithSlug = skill as SkillSummaryDto & { slug?: string | null; iconKey?: string | null };
    return (skillWithSlug.iconKey?.trim() || skillWithSlug.slug?.trim() || skill.name?.trim() || '')
      .toLowerCase()
      .replace(/[^a-z0-9]/g, '');
  }

  protected tourSteps(): TourStep[] {
    const projectSteps: TourStep[] = this.projects.length
      ? this.projects.map((project, index) => ({
          section: 'projects',
          label: project.title?.trim() || `Project ${index + 1}`,
          projectIndex: index,
        }))
      : [{ section: 'projects', label: 'Projects' }];

    const collectionSteps: TourStep[] = this.collections.length
      ? this.collections.map((collection, index) => ({
          section: 'collections',
          label: collection.name?.trim() || collection.title?.trim() || `Collection ${index + 1}`,
          collectionIndex: index,
        }))
      : [{ section: 'collections', label: 'Collections' }];

    return [
      { section: 'hero', label: 'Hero / Profile' },
      { section: 'about', label: 'About' },
      { section: 'skills', label: 'Skills' },
      ...projectSteps,
      ...collectionSteps,
      { section: 'contact', label: 'Contact' },
    ];
  }

  protected currentStep(): TourStep | null {
    return this.tourSteps()[this.tourProgressIndex] ?? null;
  }

  protected renderedPrimaryStep(): TourStep | null {
    return this.transitioning ? this.transitionFromStep : this.currentStep();
  }

  protected renderedIncomingStep(): TourStep | null {
    return this.transitioning ? this.transitionToStep : null;
  }

  protected currentStepLabel(): string {
    return this.currentStep()?.label || 'Hero / Profile';
  }

  protected currentStepNumber(): number {
    return this.tourProgressIndex + 1;
  }

  protected totalSteps(): number {
    return this.tourSteps().length;
  }

  protected currentSectionMeta(): string {
    switch (this.activeSection) {
      case 'hero':
        return 'Immersive first impression';
      case 'about':
        return 'Personal story and positioning';
      case 'skills':
        return 'Core technologies and expertise';
      case 'projects':
        return `Project ${this.activeProjectIndex + 1} of ${Math.max(this.projects.length, 1)}`;
      case 'collections':
        return `Collection ${this.activeCollectionIndex + 1} of ${Math.max(this.collections.length, 1)}`;
      case 'contact':
        return 'How to connect';
    }
  }

  protected sectionCardCount(section: Jihen3dSection): number {
    if (section === 'projects') {
      return Math.max(this.projects.length, 1);
    }
    if (section === 'collections') {
      return Math.max(this.collections.length, 1);
    }
    return 1;
  }

  protected previewFloorLabel(): string {
    return `${this.projects.length} projects • ${this.collections.length} collections • ${this.previewSkills().length} spotlight skills`;
  }

  protected totalTourTimeSeconds(): number {
    return this.totalSteps() * 5;
  }

  protected previewDurationLabel(section: Jihen3dSection): string {
    if (section === 'projects' || section === 'collections') {
      return '5 seconds each';
    }

    return '5 seconds';
  }

  private scheduleNextStep(): void {
    this.clearTourTimer();
    if (!this.tourRunning || this.tourPaused) {
      return;
    }

    this.tourTimer = window.setTimeout(() => {
      this.nextTourStep();
    }, JihenPortfolio3dComponent.TOUR_STEP_DURATION_MS);
  }

  private beginTransitionToIndex(nextIndex: number): void {
    const steps = this.tourSteps();
    const nextStep = steps[nextIndex];
    const currentStep = this.currentStep();

    if (!nextStep || !currentStep) {
      return;
    }

    this.transitioning = true;
    this.transitionFromStep = { ...currentStep };
    this.transitionToStep = { ...nextStep };
    this.primeStepState(nextStep);
    this.clearTransitionTimer();

    this.transitionTimer = window.setTimeout(() => {
      this.tourProgressIndex = nextIndex;
      this.applyTourStep();
      this.transitioning = false;
      this.transitionFromStep = null;
      this.transitionToStep = null;
      this.clearTransitionTimer();

      if (this.tourRunning && !this.tourPaused) {
        this.scheduleNextStep();
      }
    }, JihenPortfolio3dComponent.TOUR_TRANSITION_MS);
  }

  private applyTourStep(): void {
    const step = this.currentStep();
    if (!step) {
      return;
    }

    this.activeSection = step.section;

    if (typeof step.projectIndex === 'number') {
      this.activeProjectIndex = step.projectIndex;
    }

    if (typeof step.collectionIndex === 'number') {
      this.activeCollectionIndex = step.collectionIndex;
    }
  }

  private clearTourTimer(): void {
    if (this.tourTimer) {
      window.clearTimeout(this.tourTimer);
      this.tourTimer = null;
    }
  }

  private clearTransitionTimer(): void {
    if (this.transitionTimer) {
      window.clearTimeout(this.transitionTimer);
      this.transitionTimer = null;
    }
  }

  private primeStepState(step: TourStep): void {
    if (typeof step.projectIndex === 'number') {
      this.activeProjectIndex = step.projectIndex;
    }

    if (typeof step.collectionIndex === 'number') {
      this.activeCollectionIndex = step.collectionIndex;
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
