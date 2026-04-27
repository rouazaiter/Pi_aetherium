import { CommonModule, Location } from '@angular/common';
import { Component, HostListener, OnInit, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import type {
  ExploreCollectionCardDto,
  ExplorePortfolioDetailDto,
  ExploreProjectCardDto,
  SkillSummaryDto,
} from '../../core/models/api.models';
import { ExploreService } from '../../core/services/explore.service';

type Jihen3dSection = 'hero' | 'about' | 'projects' | 'skills' | 'contact';
type JihenSkillGroupView = {
  category: string;
  skills: SkillSummaryDto[];
};

@Component({
  selector: 'app-jihen-portfolio-3d',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './jihen-portfolio-3d.component.html',
  styleUrl: './jihen-portfolio-3d.component.scss',
})
export class JihenPortfolio3dComponent implements OnInit {
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
  protected sceneRotateX = -8;
  protected sceneRotateY = 12;
  protected sceneDepth = 0;

  readonly sections: Array<{ key: Jihen3dSection; label: string }> = [
    { key: 'hero', label: 'Hero' },
    { key: 'about', label: 'About' },
    { key: 'projects', label: 'Projects' },
    { key: 'skills', label: 'Skills' },
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
      });
  }

  protected backToPortfolio(): void {
    this.location.back();
  }

  protected selectSection(section: Jihen3dSection): void {
    this.activeSection = section;
    const index = this.sections.findIndex((item) => item.key === section);
    this.sceneDepth = index * -20;
  }

  protected onSceneMove(event: MouseEvent): void {
    const element = event.currentTarget as HTMLElement | null;
    if (!element) {
      return;
    }

    const rect = element.getBoundingClientRect();
    const x = (event.clientX - rect.left) / rect.width - 0.5;
    const y = (event.clientY - rect.top) / rect.height - 0.5;
    this.sceneRotateY = 12 + x * 10;
    this.sceneRotateX = -8 - y * 8;
  }

  protected resetScene(): void {
    this.sceneRotateX = -8;
    this.sceneRotateY = 12;
  }

  @HostListener('window:keydown.escape')
  protected onEscape(): void {
    this.backToPortfolio();
  }

  protected displayName(): string {
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

  protected initials(): string {
    return this.displayName().split(/\s+/).filter(Boolean).slice(0, 2).map((part) => part.charAt(0)).join('').toUpperCase() || 'SC';
  }

  protected headline(): string {
    const detail = this.detail;
    return detail?.portfolio?.job?.trim() || detail?.jobTitle?.trim() || detail?.headline?.trim() || detail?.portfolioTitle?.trim() || 'Software Engineer';
  }

  protected locationText(): string {
    const detail = this.detail;
    if (!detail) {
      return 'Remote';
    }

    return detail.location?.trim() || detail.profile?.location?.trim() || [detail.city || detail.profile?.city, detail.country || detail.profile?.country].filter(Boolean).join(', ') || 'Remote';
  }

  protected profileImage(): string {
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
      || ''
    );
  }

  protected aboutText(): string {
    return this.detail?.about?.trim() || this.detail?.bio?.trim() || this.detail?.portfolio?.bio?.trim() || this.detail?.profile?.description?.trim() || 'No story added yet.';
  }

  protected projectImage(project: ExploreProjectCardDto): string {
    const projectWithMedia = project as ExploreProjectCardDto & {
      imageUrl?: string | null;
      thumbnailUrl?: string | null;
      mediaUrl?: string | null;
    };

    return projectWithMedia.imageUrl?.trim() || projectWithMedia.thumbnailUrl?.trim() || projectWithMedia.mediaUrl?.trim() || '';
  }

  protected projectVideo(project: ExploreProjectCardDto): string {
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

  protected projectSkills(project: ExploreProjectCardDto): SkillSummaryDto[] {
    return (project.topSkills ?? []).slice(0, 4);
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

  protected topProjects(): ExploreProjectCardDto[] {
    return this.projects.slice(0, 4);
  }

  protected topCollections(): ExploreCollectionCardDto[] {
    return this.collections.slice(0, 3);
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
