import { Injectable } from '@angular/core';
import { HttpClient, HttpParams, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  Certification, CertificationCreate, CertificationDetail,
  LlmGenerateRequest, QuestionResponse, QuestionUpdateRequest
} from '../models/certification.model';

@Injectable({ providedIn: 'root' })
export class CertificationService {
  private base = 'http://localhost:8089/api/certifications';
  private questionsBase = 'http://localhost:8089/api/questions';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Certification[]> {
    return this.http.get<Certification[]>(this.base).pipe(catchError(this.handleError));
  }

  getById(id: number): Observable<Certification> {
    return this.http.get<Certification>(`${this.base}/${id}`).pipe(catchError(this.handleError));
  }

  getDetail(id: number): Observable<CertificationDetail> {
    return this.http.get<CertificationDetail>(`${this.base}/${id}/detail`).pipe(catchError(this.handleError));
  }

  search(title: string): Observable<Certification[]> {
    return this.http.get<Certification[]>(`${this.base}/search`, {
      params: new HttpParams().set('title', title)
    }).pipe(catchError(this.handleError));
  }

  create(dto: CertificationCreate): Observable<Certification> {
    return this.http.post<Certification>(this.base, dto).pipe(catchError(this.handleError));
  }

  update(id: number, dto: CertificationCreate): Observable<Certification> {
    return this.http.put<Certification>(`${this.base}/${id}`, dto).pipe(catchError(this.handleError));
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(catchError(this.handleError));
  }

  generateImage(id: number): Observable<{ coverImageUrl: string }> {
    return this.http.post<{ coverImageUrl: string }>(`${this.base}/${id}/generate-image`, {})
      .pipe(catchError(this.handleError));
  }

  generateFromLlm(req: LlmGenerateRequest): Observable<Certification> {
    return this.http.post<Certification>(`${this.base}/generate`, req).pipe(catchError(this.handleError));
  }

  importFromPdf(file: File): Observable<Certification> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<Certification>(`${this.base}/import-pdf`, form).pipe(catchError(this.handleError));
  }

  // ── Question CRUD ──────────────────────────────────────────────────────
  updateQuestion(id: number, dto: QuestionUpdateRequest): Observable<QuestionResponse> {
    return this.http.put<QuestionResponse>(`${this.questionsBase}/${id}`, dto).pipe(catchError(this.handleError));
  }

  deleteQuestion(id: number): Observable<void> {
    return this.http.delete<void>(`${this.questionsBase}/${id}`).pipe(catchError(this.handleError));
  }

  // ── Error handler ──────────────────────────────────────────────────────
  private handleError(err: HttpErrorResponse): Observable<never> {
    // Try to extract the most useful message in order of preference
    const msg =
      err.error?.message ||          // our Spring { "message": "..." } body
      err.error?.error ||            // Spring default error field
      (typeof err.error === 'string' ? err.error : null) ||
      err.message ||
      `Server error ${err.status}`;
    return throwError(() => new Error(msg));
  }
}
