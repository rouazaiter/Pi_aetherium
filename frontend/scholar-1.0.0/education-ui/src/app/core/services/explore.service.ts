import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  ExploreCollectionCardDto,
  ExploreCollectionDetailDto,
  ExploreOptionDto,
  ExplorePortfolioDetailDto,
  ExplorePortfolioCardDto,
  ExploreProjectDetailDto,
  ExploreProjectCardDto,
  ExploreSort,
  ExploreVisibilityFilter,
  SkillSummaryDto,
} from '../models/api.models';

export type ExploreFilters = {
  q?: string;
  jobTitle?: string;
  family?: string;
  category?: string;
  skillIds?: number[];
  visibility?: ExploreVisibilityFilter;
  sort?: ExploreSort;
};

@Injectable({ providedIn: 'root' })
export class ExploreService {
  private readonly http = inject(HttpClient);

  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const p = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${p}` : p;
  }

  getFamilies(): Observable<ExploreOptionDto[]> {
    return this.http.get<ExploreOptionDto[]>(this.api('/api/explore/families'));
  }

  getSkillCategories(): Observable<ExploreOptionDto[]> {
    return this.http.get<ExploreOptionDto[]>(this.api('/api/explore/skill-categories'));
  }

  searchSkills(query?: string): Observable<SkillSummaryDto[]> {
    let params = new HttpParams();
    const q = query?.trim();
    if (q) {
      params = params.set('q', q);
    }
    return this.http.get<SkillSummaryDto[]>(this.api('/api/explore/skills'), { params });
  }

  searchPortfolios(query: ExploreFilters): Observable<ExplorePortfolioCardDto[]> {
    return this.http.get<ExplorePortfolioCardDto[]>(this.api('/api/explore/portfolios'), {
      params: this.buildParams(query),
    });
  }

  searchProjects(query: ExploreFilters): Observable<ExploreProjectCardDto[]> {
    return this.http.get<ExploreProjectCardDto[]>(this.api('/api/explore/projects'), {
      params: this.buildParams(query),
    });
  }

  searchCollections(query: ExploreFilters): Observable<ExploreCollectionCardDto[]> {
    return this.http.get<ExploreCollectionCardDto[]>(this.api('/api/explore/collections'), {
      params: this.buildParams(query),
    });
  }

  getPortfolioDetail(portfolioId: number): Observable<ExplorePortfolioDetailDto> {
    return this.http.get<ExplorePortfolioDetailDto>(this.api(`/api/explore/portfolios/${portfolioId}`));
  }

  getProjectDetail(projectId: number): Observable<ExploreProjectDetailDto> {
    return this.http.get<ExploreProjectDetailDto>(this.api(`/api/explore/projects/${projectId}`));
  }

  getCollectionDetail(collectionId: number): Observable<ExploreCollectionDetailDto> {
    return this.http.get<ExploreCollectionDetailDto>(this.api(`/api/explore/collections/${collectionId}`));
  }

  getPortfolioCollections(portfolioId: number): Observable<ExploreCollectionCardDto[]> {
    return this.http.get<ExploreCollectionCardDto[]>(this.api(`/api/explore/portfolios/${portfolioId}/collections`));
  }

  private buildParams(query: ExploreFilters): HttpParams {
    let params = new HttpParams();
    if (query.family?.trim()) {
      params = params.set('family', query.family.trim());
    }
    if (query.category?.trim()) {
      params = params.set('category', query.category.trim());
    }
    if (query.q?.trim()) {
      params = params.set('q', query.q.trim());
    }
    if (query.jobTitle?.trim()) {
      params = params.set('jobTitle', query.jobTitle.trim());
    }
    if (query.visibility?.trim()) {
      params = params.set('visibility', query.visibility.trim());
    }
    if (query.sort?.trim()) {
      params = params.set('sort', query.sort.trim());
    }
    for (const skillId of query.skillIds ?? []) {
      params = params.append('skillIds', String(skillId));
    }
    return params;
  }
}
