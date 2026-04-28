import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { SubscriptionRequest, SubscriptionResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class SubscriptionService {
  private readonly http = inject(HttpClient);

  listMine(): Observable<SubscriptionResponse[]> {
    return this.http.get<SubscriptionResponse[]>(`${environment.apiUrl}/api/subscriptions/me`);
  }

  create(body: SubscriptionRequest): Observable<SubscriptionResponse> {
    return this.http.post<SubscriptionResponse>(`${environment.apiUrl}/api/subscriptions`, body);
  }
}
