import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LeaderboardResponse } from '../models/leaderboard.model';
import { ServiceRequestCategory } from '../models/service-request.model';

@Injectable({ providedIn: 'root' })
export class LeaderboardService {
  private base = '/skillhub/api/leaderboard';

  constructor(private http: HttpClient) {}

  getApplicants(days: number, limit: number, category?: ServiceRequestCategory): Observable<LeaderboardResponse> {
    const params = this.buildParams(days, limit, category);
    return this.http.get<LeaderboardResponse>(`${this.base}/applicants`, { params });
  }

  getCreators(days: number, limit: number, category?: ServiceRequestCategory): Observable<LeaderboardResponse> {
    const params = this.buildParams(days, limit, category);
    return this.http.get<LeaderboardResponse>(`${this.base}/creators`, { params });
  }

  private buildParams(days: number, limit: number, category?: ServiceRequestCategory): HttpParams {
    let params = new HttpParams()
      .set('days', String(days))
      .set('limit', String(limit));

    if (category) {
      params = params.set('category', category);
    }

    return params;
  }
}
