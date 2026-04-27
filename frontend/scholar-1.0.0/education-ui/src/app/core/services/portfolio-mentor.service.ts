import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { PortfolioMentorChatRequest, PortfolioMentorChatResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class PortfolioMentorService {
  private readonly http = inject(HttpClient);

  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const p = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${p}` : p;
  }

  getProfileStrength(): Observable<unknown> {
    return this.http.get<unknown>(this.api('/api/portfolio-ai/me/profile-strength'));
  }

  getStrengthsGaps(): Observable<unknown> {
    return this.http.get<unknown>(this.api('/api/portfolio-ai/me/strengths-gaps'));
  }

  getNextBestMoves(): Observable<unknown> {
    return this.http.get<unknown>(this.api('/api/portfolio-ai/me/next-best-moves'));
  }

  getCoherence(): Observable<unknown> {
    return this.http.get<unknown>(this.api('/api/portfolio-ai/me/coherence'));
  }

  chat(body: PortfolioMentorChatRequest): Observable<PortfolioMentorChatResponse> {
    return this.http.post<PortfolioMentorChatResponse>(this.api('/api/portfolio-ai/me/mentor-chat'), body);
  }
}
