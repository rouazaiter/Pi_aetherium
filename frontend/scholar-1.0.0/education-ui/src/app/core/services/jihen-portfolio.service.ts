import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  CreateSkillRequest,
  CreateCollectionRequest,
  CreateProjectRequest,
  PortfolioCollectionDto,
  PortfolioProjectDto,
  PortfolioResponse,
  PortfolioUpsertRequest,
  SkillSummaryDto,
  UploadProjectMediaResponse,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class JihenPortfolioService {
  private readonly http = inject(HttpClient);

  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const p = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${p}` : p;
  }

  getMyPortfolio(): Observable<PortfolioResponse> {
    return this.http.get<PortfolioResponse>(this.api('/api/portfolio/me'));
  }

  createMyPortfolio(body: PortfolioUpsertRequest): Observable<PortfolioResponse> {
    return this.http.post<PortfolioResponse>(this.api('/api/portfolio/me'), body);
  }

  updateMyPortfolio(body: PortfolioUpsertRequest): Observable<PortfolioResponse> {
    return this.http.put<PortfolioResponse>(this.api('/api/portfolio/me'), body);
  }

  createProject(body: CreateProjectRequest): Observable<PortfolioProjectDto> {
    return this.http.post<PortfolioProjectDto>(this.api('/api/portfolio/me/projects'), body);
  }

  createCollection(body: CreateCollectionRequest): Observable<PortfolioCollectionDto> {
    return this.http.post<PortfolioCollectionDto>(this.api('/api/portfolio/me/collections'), body);
  }

  addProjectToCollection(collectionId: number, projectId: number): Observable<void> {
    return this.http.post<void>(this.api(`/api/portfolio/me/collections/${collectionId}/projects/${projectId}`), {});
  }

  uploadProjectMedia(file: File): Observable<UploadProjectMediaResponse> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<UploadProjectMediaResponse>(this.api('/api/portfolio/me/projects/media/upload'), formData);
  }

  getSkills(query?: string): Observable<SkillSummaryDto[]> {
    let params = new HttpParams();
    const q = query?.trim();
    if (q) {
      params = params.set('q', q);
    }
    return this.http.get<SkillSummaryDto[]>(this.api('/api/portfolio/skills'), { params });
  }

  getSkillCategories(): Observable<string[]> {
    return this.http.get<string[]>(this.api('/api/portfolio/skills/categories'));
  }

  createSkill(body: CreateSkillRequest): Observable<SkillSummaryDto> {
    return this.http.post<SkillSummaryDto>(this.api('/api/portfolio/skills'), body);
  }
}
