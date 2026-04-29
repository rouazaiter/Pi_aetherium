import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FeedbackInsights, FeedbackService } from './feedback.service';

export interface ScoreBucket  { range: string; count: number; }
export interface CertStat     { name: string; value: number; }
export interface MonthlyTrend { month: string; avgScore: number; count: number; }
export interface QuestionDiff { questionText: string; correctRate: number; totalAttempts: number; }

export interface AnalyticsData {
  totalEnrollments: number;
  completedExams:   number;
  passedExams:      number;
  failedExams:      number;
  overallPassRate:  number;
  averageScore:     number;
  passCount:        number;
  failCount:        number;
  scoreDistribution:    ScoreBucket[];
  avgScorePerCert:      CertStat[];
  monthlyTrend:         MonthlyTrend[];
  questionDifficulty:   QuestionDiff[];
  mostFailedQuestions:  QuestionDiff[];
  enrollmentsPerCert:   CertStat[];
  availableCategories:  string[];
}

export interface AnalyticsFilters {
  certificationId?: number | null;
  difficulty?:      string;
  category?:        string;
  dateFrom?:        string;
  dateTo?:          string;
}

export { FeedbackInsights };

@Injectable({ providedIn: 'root' })
export class AnalyticsService {
  private readonly url = 'http://localhost:8089/api/analytics';

  constructor(private http: HttpClient, private feedbackService: FeedbackService) {}

  getAnalytics(filters: AnalyticsFilters = {}): Observable<AnalyticsData> {
    let params = new HttpParams();
    if (filters.certificationId) params = params.set('certificationId', filters.certificationId);
    if (filters.difficulty)      params = params.set('difficulty',      filters.difficulty);
    if (filters.category)        params = params.set('category',        filters.category);
    if (filters.dateFrom)        params = params.set('dateFrom',        filters.dateFrom);
    if (filters.dateTo)          params = params.set('dateTo',          filters.dateTo);
    return this.http.get<AnalyticsData>(this.url, { params });
  }

  getFeedbackInsights(certificationId?: number): Observable<FeedbackInsights[]> {
    return this.feedbackService.getInsights(certificationId);
  }
}
