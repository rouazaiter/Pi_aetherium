import { CommonModule, Location } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, map, of, switchMap } from 'rxjs';
import type {
  ExploreCollectionDetailDto,
  ExploreProjectCardDto,
  PortfolioVisibility,
  SkillSummaryDto,
} from '../../core/models/api.models';
import { ExploreService } from '../../core/services/explore.service';

@Component({
  selector: 'app-explore-collection-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './explore-collection-detail.component.html',
  styleUrl: './explore-collection-detail.component.scss',
})
export class ExploreCollectionDetailComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly location = inject(Location);
  private readonly api = inject(ExploreService);

  loading = true;
  errorMessage = '';
  detail: ExploreCollectionDetailDto | null = null;

  ngOnInit(): void {
    this.route.paramMap
      .pipe(
        map((params) => Number(params.get('id'))),
        switchMap((collectionId) => {
          if (!Number.isFinite(collectionId) || collectionId <= 0) {
            this.loading = false;
            this.errorMessage = 'Collection not available.';
            return of(null);
          }

          this.loading = true;
          this.errorMessage = '';
          return this.api.getCollectionDetail(collectionId).pipe(
            catchError((error) => {
              this.loading = false;
              this.errorMessage = error?.status === 403 ? 'You do not have permission to view this.' : 'Collection not available.';
              return of(null);
            }),
          );
        }),
      )
      .subscribe((detail) => {
        this.detail = detail;
        this.loading = false;
      });
  }

  backToExplore(): void {
    this.location.back();
  }

  openProject(projectId: number | null | undefined): void {
    if (!projectId) {
      return;
    }
    this.router.navigate(['/explore/projects', projectId]);
  }

  openOwnerPortfolio(): void {
    const portfolioId = this.detail?.portfolioId ?? this.detail?.portfolio?.id;
    if (!portfolioId) {
      return;
    }
    this.router.navigate(['/explore/portfolios', portfolioId]);
  }

  visibilityBadge(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'Friends' : value === 'PUBLIC' ? 'Public' : 'Private';
  }

  visibilityBadgeClass(value: PortfolioVisibility | null | undefined): string {
    return value === 'FRIENDS_ONLY' ? 'detail-badge detail-badge--friends' : 'detail-badge';
  }

  title(): string {
    return this.detail?.name?.trim() || this.detail?.title?.trim() || 'Untitled collection';
  }

  ownerName(): string {
    return this.detail?.ownerDisplayName?.trim() || this.detail?.ownerUsername?.trim() || this.detail?.owner?.username?.trim() || 'Unknown owner';
  }

  profileImage(): string {
    return this.detail?.ownerProfileImage?.trim() || '';
  }

  skills(): SkillSummaryDto[] {
    return this.detail?.skills ?? this.detail?.topSkills ?? [];
  }

  projects(): ExploreProjectCardDto[] {
    return this.detail?.projects ?? [];
  }
}
