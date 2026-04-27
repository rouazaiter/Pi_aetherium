import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  CvAiChatRequest,
  CvAiChatResponse,
  CvAiImproveRequest,
  CvAiImproveResponse,
  CvJobMatchRequest,
  CvJobMatchResponse,
  CvDraftApiResponse,
  CvDraftUpdateRequest,
  CvPreviewResponse,
  CvProfileResponse,
  UpdateCvProfileRequest,
} from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class JihenCvService {
  private readonly http = inject(HttpClient);

  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const p = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${p}` : p;
  }

  getMyCvPreview(): Observable<CvPreviewResponse> {
    return this.http.get<CvPreviewResponse>(this.api('/api/cv/me/preview'));
  }

  getMyCvProfile(): Observable<CvProfileResponse> {
    return this.http.get<CvProfileResponse>(this.api('/api/cv/me/profile'));
  }

  updateMyCvProfile(body: UpdateCvProfileRequest): Observable<CvProfileResponse> {
    return this.http.put<CvProfileResponse>(this.api('/api/cv/me/profile'), body);
  }

  getLatestMyCvDraft(): Observable<CvDraftApiResponse> {
    return this.http.get<CvDraftApiResponse>(this.api('/api/cv/me/drafts/latest'));
  }

  generateMyCvDraft(): Observable<CvDraftApiResponse> {
    return this.http.post<CvDraftApiResponse>(this.api('/api/cv/me/drafts/generate'), {});
  }

  updateMyCvDraft(draftId: number, body: CvDraftUpdateRequest): Observable<CvDraftApiResponse> {
    return this.http.put<CvDraftApiResponse>(this.api(`/api/cv/me/drafts/${draftId}`), body);
  }

  updateLatestMyCvDraft(body: CvDraftUpdateRequest): Observable<CvDraftApiResponse> {
    return this.http.put<CvDraftApiResponse>(this.api('/api/cv/me/drafts/latest'), body);
  }

  improveMyCvText(body: CvAiImproveRequest): Observable<CvAiImproveResponse> {
    return this.http.post<CvAiImproveResponse>(this.api('/api/cv/me/ai/improve'), body);
  }

  chatAboutMyCv(body: CvAiChatRequest): Observable<CvAiChatResponse> {
    return this.http.post<CvAiChatResponse>(this.api('/api/cv/me/ai/chat'), body);
  }

  createJobMatchedCv(body: CvJobMatchRequest): Observable<CvJobMatchResponse> {
    return this.http.post<CvJobMatchResponse>(this.api('/api/cv/me/ai/job-match'), body);
  }
}
