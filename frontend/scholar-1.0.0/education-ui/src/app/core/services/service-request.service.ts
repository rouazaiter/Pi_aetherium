import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ServiceRequest,
  ServiceRequestPayload,
  ServiceRequestStatus
} from '../models/service-request.model';

@Injectable({ providedIn: 'root' })
export class ServiceRequestService {
  private base = '/skillhub/api/service-requests';

  constructor(private http: HttpClient) {}

  getAll(viewerId: number): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.base}/requests`, {
      params: new HttpParams().set('viewerId', viewerId)
    });
  }

  getById(id: number, viewerId: number): Observable<ServiceRequest> {
    return this.http.get<ServiceRequest>(`${this.base}/request/${id}`, {
      params: new HttpParams().set('viewerId', viewerId)
    });
  }

  getByStatus(status: ServiceRequestStatus, viewerId: number): Observable<ServiceRequest[]> {
    return this.http.get<ServiceRequest[]>(`${this.base}/requestbystatus/${status}`, {
      params: new HttpParams().set('viewerId', viewerId)
    });
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
