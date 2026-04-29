import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { EnrollmentDTO, ExamResultDTO, SubmitExamRequest } from '../models/enrollment.model';
import { Certification, CertificationDetail } from '../models/certification.model';

@Injectable({ providedIn: 'root' })
export class EnrollmentService {
  private base = 'http://localhost:8089/api/store';

  constructor(private http: HttpClient) {}

  getStore(): Observable<Certification[]> {
    return this.http.get<Certification[]>(this.base);
  }

  getStoreDetail(id: number): Observable<CertificationDetail> {
    return this.http.get<CertificationDetail>(`${this.base}/${id}`);
  }

  enroll(certId: number, userIdentifier: string, fullName?: string): Observable<EnrollmentDTO> {
    return this.http.post<EnrollmentDTO>(`${this.base}/${certId}/enroll`, { userIdentifier, fullName: fullName ?? null });
  }

  confirmPayment(certId: number, userIdentifier: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${certId}/confirm-payment`, { userIdentifier, fullName: null, phoneNumber: null });
  }

  confirmPaymentFree(certId: number, userIdentifier: string, fullName: string, phoneNumber: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${certId}/confirm-payment`, {
      userIdentifier, fullName, phoneNumber: phoneNumber || null
    });
  }

  createPaymentIntent(certId: number, userIdentifier: string): Observable<{ clientSecret: string; paymentIntentId: string; amount: number }> {
    return this.http.post<{ clientSecret: string; paymentIntentId: string; amount: number }>(
      `${this.base}/${certId}/create-payment-intent`, { userIdentifier }
    );
  }

  confirmStripePayment(certId: number, userIdentifier: string, paymentIntentId: string, fullName: string, phoneNumber: string): Observable<EnrollmentDTO> {
    return this.http.post<EnrollmentDTO>(
      `${this.base}/${certId}/confirm-stripe-payment`,
      { userIdentifier, paymentIntentId, fullName, phoneNumber: phoneNumber || null }
    );
  }

  verifyCode(certId: number, userIdentifier: string, code: string): Observable<void> {
    return this.http.post<void>(`${this.base}/${certId}/verify-code`, { userIdentifier, code });
  }

  getEnrollment(certId: number, userIdentifier: string): Observable<EnrollmentDTO> {
    return this.http.get<EnrollmentDTO>(`${this.base}/${certId}/enrollment`, {
      params: new HttpParams().set('userIdentifier', userIdentifier)
    });
  }

  downloadCertificate(enrollmentId: number): Observable<Blob> {
    return this.http.get(`http://localhost:8089/api/enrollments/${enrollmentId}/certificate`, {
      responseType: 'blob'
    });
  }

  generateLinkedInPost(enrollmentId: number): Observable<{ post: string }> {
    return this.http.get<{ post: string }>(`http://localhost:8089/api/enrollments/${enrollmentId}/linkedin-post`);
  }

  retryExam(certId: number, userIdentifier: string): Observable<any> {
    return this.http.post(`http://localhost:8089/api/store/${certId}/retry`, { userIdentifier });
  }

  getPracticeQuestions(certId: number, count: number): Observable<any[]> {
    const params = new HttpParams().set('count', count);
    return this.http.get<any[]>(`http://localhost:8089/api/store/${certId}/practice-questions`, { params });
  }

  submitExam(certId: number, req: SubmitExamRequest): Observable<ExamResultDTO> {
    return this.http.post<ExamResultDTO>(`${this.base}/${certId}/submit`, req);
  }

  getMyEnrollments(userIdentifier: string): Observable<EnrollmentDTO[]> {
    return this.http.get<EnrollmentDTO[]>('http://localhost:8089/api/enrollments/my', {
      params: new HttpParams().set('userIdentifier', userIdentifier)
    });
  }

  // Simple local storage for user identity (no auth)
  getUser(): string {
    return localStorage.getItem('skillhub_user') ?? '';
  }

  setUser(name: string): void {
    localStorage.setItem('skillhub_user', name);
  }
}
