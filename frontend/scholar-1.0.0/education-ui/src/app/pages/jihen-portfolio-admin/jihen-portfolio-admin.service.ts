// Jihen Portfolio Admin

import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { JihenPortfolioAdminModels } from './jihen-portfolio-admin.models';

@Injectable({ providedIn: 'root' })
export class JihenPortfolioAdminService {
  private readonly http = inject(HttpClient);

  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${normalizedPath}` : normalizedPath;
  }

  getOverview(): Observable<JihenPortfolioAdminModels.OverviewResponse> {
    return this.http.get<JihenPortfolioAdminModels.OverviewResponse>(this.api('/api/admin-jihen/portfolio/overview'));
  }

  getActivity(range: JihenPortfolioAdminModels.ActivityRange): Observable<JihenPortfolioAdminModels.ActivityPoint[]> {
    const params = new HttpParams().set('range', range);
    return this.http.get<JihenPortfolioAdminModels.ActivityPoint[]>(this.api('/api/admin-jihen/portfolio/analytics/activity'), { params });
  }

  getTrendingSkills(): Observable<JihenPortfolioAdminModels.TrendingSkillItem[]> {
    return this.http.get<JihenPortfolioAdminModels.TrendingSkillItem[]>(this.api('/api/admin-jihen/portfolio/analytics/trending-skills'));
  }

  getRecentItems(): Observable<JihenPortfolioAdminModels.RecentItem[]> {
    return this.http.get<JihenPortfolioAdminModels.RecentItem[]>(this.api('/api/admin-jihen/portfolio/recent-items'));
  }

  getProjects(query: JihenPortfolioAdminModels.ProjectQuery): Observable<JihenPortfolioAdminModels.ProjectsPageResponse> {
    let params = new HttpParams();
    if (query.q?.trim()) {
      params = params.set('q', query.q.trim());
    }
    if (query.visibility?.trim()) {
      params = params.set('visibility', query.visibility.trim());
    }
    if (query.moderationStatus?.trim()) {
      params = params.set('moderationStatus', query.moderationStatus.trim());
    }
    if (typeof query.page === 'number') {
      params = params.set('page', String(query.page));
    }
    if (typeof query.size === 'number') {
      params = params.set('size', String(query.size));
    }
    if (query.sort?.trim()) {
      params = params.set('sort', query.sort.trim());
    }

    return this.http.get<JihenPortfolioAdminModels.ProjectsPageResponse>(this.api('/api/admin-jihen/portfolio/projects'), { params });
  }

  updateProjectVisibility(projectId: number, body: JihenPortfolioAdminModels.UpdateVisibilityRequest): Observable<unknown> {
    return this.http.patch(this.api(`/api/admin-jihen/portfolio/projects/${projectId}/visibility`), body);
  }

  updateProjectModeration(projectId: number, body: JihenPortfolioAdminModels.UpdateModerationRequest): Observable<unknown> {
    return this.http.patch(this.api(`/api/admin-jihen/portfolio/projects/${projectId}/moderation`), body);
  }
}
