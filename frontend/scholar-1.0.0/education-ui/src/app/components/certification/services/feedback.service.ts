import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export type DifficultyRating = 'EASY' | 'BALANCED' | 'HARD';
export type TimeRating        = 'TOO_SHORT' | 'ADEQUATE' | 'TOO_LONG';
export type RelevanceRating   = 'YES' | 'PARTIALLY' | 'NO';

export interface FeedbackRequest {
  enrollmentId:     number;
  difficultyRating: DifficultyRating;
  timeRating:       TimeRating;
  relevanceRating:  RelevanceRating;
  comment?:         string;
}

export interface FeedbackInsights {
  certificationId:    number;
  certificationTitle: string;
  totalFeedbacks:     number;
  difficultyBreakdown: Record<string, number>;
  dominantDifficulty:  string;
  timeBreakdown:       Record<string, number>;
  dominantTime:        string;
  relevanceBreakdown:  Record<string, number>;
  dominantRelevance:   string;
  flaggedTooHard:      boolean;
  flaggedTooEasy:      boolean;
  flaggedNotRelevant:  boolean;
  flaggedTimeTooShort: boolean;
  avgScore:            number;
}

@Injectable({ providedIn: 'root' })
export class FeedbackService {
  private readonly base = 'http://localhost:8089/api/feedback';

  constructor(private http: HttpClient) {}

  submit(req: FeedbackRequest): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(this.base, req);
  }

  checkSubmitted(enrollmentId: number): Observable<{ submitted: boolean }> {
    return this.http.get<{ submitted: boolean }>(`${this.base}/check/${enrollmentId}`);
  }

  getInsights(certificationId?: number): Observable<FeedbackInsights[]> {
    let params = new HttpParams();
    if (certificationId) params = params.set('certificationId', certificationId);
    return this.http.get<FeedbackInsights[]>(`${this.base}/insights`, { params });
  }
}
