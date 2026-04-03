import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Application, ApplicationStatus } from '../models/application.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private base = '/skillhub/api/applications';

  constructor(private http: HttpClient) {}

  apply(applicantId: number, serviceRequestId: number, message: string): Observable<Application> {
    return this.http.post<Application>(
      `${this.base}/add-application/${applicantId}/${serviceRequestId}`,
      { message }
    );
  }

  getByUser(applicantId: number): Observable<Application[]> {
    return this.http.get<Application[]>(`${this.base}/retrieve-by-user/${applicantId}`);
  }

  getByServiceRequest(serviceRequestId: number, requesterId: number): Observable<Application[]> {
    return this.http.get<Application[]>(
      `${this.base}/retrieve-by-service-request/${serviceRequestId}/${requesterId}`
    );
  }

  getByServiceRequestAndStatus(
    serviceRequestId: number,
    status: ApplicationStatus,
    requesterId: number
  ): Observable<Application[]> {
    return this.http.get<Application[]>(
      `${this.base}/retrieve-by-service-request-status/${serviceRequestId}/${status}/${requesterId}`
    );
  }

  updateStatus(applicationId: number, requesterId: number, status: ApplicationStatus): Observable<Application> {
    return this.http.patch<Application>(
      `${this.base}/modify-status/${applicationId}/${requesterId}/${status}`,
      {}
    );
  }

  hasApplied(serviceRequestId: number, applicantId: number): Observable<{ hasApplied: boolean }> {
    return this.http.get<{ hasApplied: boolean }>(
      `${this.base}/has-applied/${serviceRequestId}/${applicantId}`
    );
  }
}
