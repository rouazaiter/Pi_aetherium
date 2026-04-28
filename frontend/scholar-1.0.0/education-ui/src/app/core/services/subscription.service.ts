import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { SubscriptionPlanResponse, SubscriptionRequest, SubscriptionResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private readonly http = inject(HttpClient);

  listPlans(): Observable<SubscriptionPlanResponse[]> {
    return this.http.get<SubscriptionPlanResponse[]>(`${environment.apiUrl}/api/subscriptions/plans`);
  }

  listMine(): Observable<SubscriptionResponse[]> {
    return this.http.get<SubscriptionResponse[]>(`${environment.apiUrl}/api/subscriptions/me`);
  }

  create(body: SubscriptionRequest): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${environment.apiUrl}/api/subscriptions`, body);
  }

  downloadInvoice(subscriptionId: number): Observable<Blob> {
    return this.http.get(`${environment.apiUrl}/api/subscriptions/${subscriptionId}/invoice`, {
      responseType: 'blob',
    });
  }
}
