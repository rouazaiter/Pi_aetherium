import { CommonModule, DatePipe, Location } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';
import type { ExploreProjectDetailDto, PortfolioVisibility, ProjectMediaDto, ProjectMediaType, SkillSummaryDto } from '../../core/models/api.models';
import { ExploreService } from '../../core/services/explore.service';

type ProjectMediaView = {
  url: string;
  type: ProjectMediaType;
};

@Component({
  selector: 'app-explore-project-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, DatePipe],
  templateUrl: './explore-project-detail.component.html',
  styleUrl: './explore-project-detail.component.scss',
})
export class ExploreProjectDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly api = inject(ExploreService);

  loading = true;
  errorMessage = '';
  detail: ExploreProjectDetailDto | null = null;
  activeMediaIndex = 0;

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map((params) => Number(params.get('id'))),
        switchMap((projectId) => {
          if (!Number.isFinite(projectId) || projectId <= 0) {
            this.loading = false;
            this.errorMessage = 'Project not available.';
            return of(null);
          }

          this.loading = true;
          this.errorMessage = '';
          return this.api.getProjectDetail(projectId).pipe(
            catchError((error) => {
              this.loading = false;
              this.errorMessage = error?.status === 403 ? 'You do not have permission to view this.' : 'Project not available.';
              return of(null);
            }),
          );
        }),
      )
      .subscribe((detail) => {
        this.detail = detail;
        this.activeMediaIndex = 0;
        this.loading = false;
      });
  }

  backToExplore(): void {
    this.location.back();
  }

  openOwnerPortfolio(): void {
    const portfolioId = this.detail?.portfolioId ?? this.detail?.project?.id ?? this.detail?.portfolio?.id;
    if (!portfolioId) {
      return;
    }
    this.router.navigate(['/explore/portfolios', portfolioId]);
  }

  selectMedia(index: number): void {
    this.activeMediaIndex = index;
  }

  visibilityBadge(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'Friends' : value === 'PUBLIC' ? 'Public' : 'Private';
  }

  visibilityBadgeClass(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'detail-badge detail-badge--friends' : 'detail-badge';
  }

  title(): string {
    return this.detail?.title?.trim() || this.detail?.project?.title?.trim() || 'Untitled project';
  }

  ownerName(): string {
    return (
      this.detail?.ownerName?.trim()
      || this.detail?.ownerDisplayName?.trim()
      || this.detail?.ownerUsername?.trim()
      || this.detail?.owner?.username?.trim()
      || 'Unknown owner'
    );
  }

  ownerJob(): string {
    return this.detail?.ownerJob?.trim() || this.detail?.portfolio?.job?.trim() || 'Portfolio owner';
  }

  profileImage(): string {
    return this.detail?.ownerAvatarUrl?.trim() || this.detail?.ownerProfileImage?.trim() || '';
  }

  shortDescription(): string {
    return this.rawDescription() || 'This project does not have a description yet.';
  }

  fullDescription(): string {
    return this.rawDescription() || 'This project does not have a description yet.';
  }

  projectUrl(): string {
    return this.detail?.projectUrl?.trim() || this.detail?.project?.projectUrl?.trim() || '';
  }

  skills(): SkillSummaryDto[] {
    return this.detail?.skills ?? this.detail?.topSkills ?? this.detail?.project?.skills ?? [];
  }

  mediaItems(): ProjectMediaView[] {
    const directMedia = this.detail?.media ?? this.detail?.project?.media ?? [];
    const normalized = directMedia
      .map((item) => this.normalizeMedia(item))
      .filter((item): item is ProjectMediaView => Boolean(item));

    if (normalized.length > 0) {
      return normalized;
    }

    const fallbackUrl = this.detail?.mediaUrl?.trim();
    const fallbackType = this.detail?.mediaType ?? 'IMAGE';
    return fallbackUrl ? [{ url: fallbackUrl, type: fallbackType }] : [];
  }

  activeMedia(): ProjectMediaView | null {
    const items = this.mediaItems();
    if (items.length === 0) {
      return null;
    }
    return items[Math.min(this.activeMediaIndex, items.length - 1)] ?? null;
  }

  views(): number | string {
    return this.detail?.views ?? '—';
  }

  likes(): number | string {
    return this.detail?.likes ?? '—';
  }

  createdAt(): string | null {
    return this.detail?.createdAt ?? this.detail?.project?.createdAt ?? null;
  }

  updatedAt(): string | null {
    return this.detail?.updatedAt ?? this.detail?.project?.updatedAt ?? null;
  }

  skillCount(): number {
    return this.skills().length;
  }

  mediaCount(): number {
    return this.mediaItems().length;
  }

  private rawDescription(): string {
    return this.detail?.description?.trim() || this.detail?.project?.description?.trim() || '';
  }

  private normalizeMedia(item: ProjectMediaDto | null | undefined): ProjectMediaView | null {
    const url = item?.mediaUrl?.trim();
    if (!url) {
      return null;
    }

    const type = item?.mediaType ?? 'IMAGE';

    return {
      url,
      type,
    };
  }
}
