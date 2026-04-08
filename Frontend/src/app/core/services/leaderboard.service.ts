import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { LeaderboardResponse } from '../models/leaderboard.model';

@Injectable({ providedIn: 'root' })
export class LeaderboardService {
  private base = '/skillhub/api/leaderboard';

  constructor(private http: HttpClient) {}

  getApplicants(days: number, limit: number): Observable<LeaderboardResponse> {
    const params = this.buildParams(days, limit);
    return this.http.get<LeaderboardResponse>(`${this.base}/applicants`, { params });
  }

  getCreators(days: number, limit: number): Observable<LeaderboardResponse> {
    const params = this.buildParams(days, limit);
    return this.http.get<LeaderboardResponse>(`${this.base}/creators`, { params });
  }

  private buildParams(days: number, limit: number): HttpParams {
    return new HttpParams()
      .set('days', String(days))
      .set('limit', String(limit));
  }
}
