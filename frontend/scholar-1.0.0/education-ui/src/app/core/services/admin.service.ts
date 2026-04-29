import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type {
  AdminCvSummaryResponse,
  AdminDashboardActivityResponse,
  AdminDashboardSummaryResponse,
  AdminPageResponse,
  AdminPortfolioSummaryResponse,
  AdminSubscriptionSummaryResponse,
  AdminUserDetailResponse,
  AdminUserStatusUpdateRequest,
  AdminUserSummaryResponse,
} from '../models/api.models';

type AdminListQuery = {
  q?: string;
  page?: number;
  size?: number;
  status?: string;
  role?: string;
  visibility?: string;
  moderationStatus?: string;
};

@Injectable({ providedIn: 'root' })
export class AdminService {
  private readonly http = inject(HttpClient);

  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const normalizedPath = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${normalizedPath}` : normalizedPath;
  }

  getDashboardSummary(): Observable<AdminDashboardSummaryResponse> {
    return this.http.get<AdminDashboardSummaryResponse>(this.api('/api/admin/dashboard/summary'));
  }

  getDashboardActivity(): Observable<AdminDashboardActivityResponse> {
    return this.http.get<AdminDashboardActivityResponse>(this.api('/api/admin/dashboard/activity'));
  }

  listUsers(query: AdminListQuery): Observable<AdminPageResponse<AdminUserSummaryResponse>> {
    return this.http.get<AdminPageResponse<AdminUserSummaryResponse>>(this.api('/api/admin/users'), {
      params: this.buildParams(query),
    });
  }

  getUser(userId: number): Observable<AdminUserDetailResponse> {
    return this.http.get<AdminUserDetailResponse>(this.api(`/api/admin/users/${userId}`));
  }

  updateUserStatus(userId: number, body: AdminUserStatusUpdateRequest): Observable<AdminUserSummaryResponse> {
    return this.http.patch<AdminUserSummaryResponse>(this.api(`/api/admin/users/${userId}/status`), body);
  }

  listPortfolios(query: AdminListQuery): Observable<AdminPageResponse<AdminPortfolioSummaryResponse>> {
    return this.http.get<AdminPageResponse<AdminPortfolioSummaryResponse>>(this.api('/api/admin/portfolios'), {
      params: this.buildParams(query),
    });
  }

  listCvs(query: AdminListQuery): Observable<AdminPageResponse<AdminCvSummaryResponse>> {
    return this.http.get<AdminPageResponse<AdminCvSummaryResponse>>(this.api('/api/admin/cvs'), {
      params: this.buildParams(query),
    });
  }

  listSubscriptions(query: AdminListQuery): Observable<AdminPageResponse<AdminSubscriptionSummaryResponse>> {
    return this.http.get<AdminPageResponse<AdminSubscriptionSummaryResponse>>(this.api('/api/admin/subscriptions'), {
      params: this.buildParams(query),
    });
  }

  private buildParams(query: AdminListQuery): HttpParams {
    let params = new HttpParams();
    if (query.q?.trim()) {
      params = params.set('q', query.q.trim());
    }
    if (typeof query.page === 'number') {
      params = params.set('page', String(query.page));
    }
    if (typeof query.size === 'number') {
      params = params.set('size', String(query.size));
    }
    if (query.status?.trim()) {
      params = params.set('status', query.status.trim());
    }
    if (query.role?.trim()) {
      params = params.set('role', query.role.trim());
    }
    if (query.visibility?.trim()) {
      params = params.set('visibility', query.visibility.trim());
    }
    if (query.moderationStatus?.trim()) {
      params = params.set('moderationStatus', query.moderationStatus.trim());
    }
    return params;
  }
}
