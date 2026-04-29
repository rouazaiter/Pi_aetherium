import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GradeRequest {
  questionType: string;
  questionText: string;
  expectedAnswer: string | null;
  userAnswer: string;
  codeLanguage: string | null;
}

export interface GradeResponse {
  correct: boolean;
  score: number;       // 0–100
  feedback: string;
  modelAnswer: string;
}

@Injectable({ providedIn: 'root' })
export class GradingService {
  private readonly url = 'http://localhost:8089/api/grading/evaluate';

  constructor(private http: HttpClient) {}

  evaluate(req: GradeRequest): Observable<GradeResponse> {
    return this.http.post<GradeResponse>(this.url, req);
  }
}
