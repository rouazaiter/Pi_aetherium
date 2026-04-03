import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceRequest, ServiceRequestStatus } from '../models/service-request.model';

@Injectable({ providedIn: 'root' })
export class ServiceRequestService {
  private base = '/skillhub/api/service-requests';

  constructor(private http: HttpClient) {}

  getAll(): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.base}/requests`);
  }

  getById(id: number): Observable<ServiceRequest> {
    return this.http.get<ServiceRequest>(`${this.base}/request/${id}`);
  }

  getByStatus(status: ServiceRequestStatus): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.base}/requestbystatus/${status}`);
  }

  getByUser(userId: number): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.base}/requestbyuser/${userId}`);
  }

  create(creatorId: number, formData: FormData): Observable<ServiceRequest> {
    return this.http.post<ServiceRequest>(`${this.base}/addservice/${creatorId}`, formData);
  }

  update(id: number, requesterId: number, formData: FormData): Observable<ServiceRequest> {
    return this.http.put<ServiceRequest>(`${this.base}/modifyrequest/${id}/${requesterId}`, formData);
  }

  delete(id: number, requesterId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/removerequest/${id}/${requesterId}`);
  }
}
