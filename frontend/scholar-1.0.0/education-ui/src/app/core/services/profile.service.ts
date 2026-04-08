import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { MessageResponse, ProfileResponse, ProfileUpdateRequest } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class ProfileService {
  private readonly http = inject(HttpClient);

  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const p = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${p}` : p;
  }

  getMe(): Observable<ProfileResponse> {
    return this.http.get<ProfileResponse>(this.api('/api/profile/me'));
  }

  updateMe(body: ProfileUpdateRequest): Observable<ProfileResponse> {
    return this.http.put<ProfileResponse>(this.api('/api/profile/me'), body);
  }

  sendProfileVerificationCode(): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.api('/api/profile/verification/send-code'), {});
  }

  verifyProfileAccessCode(code: string): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(this.api('/api/profile/verification/verify'), { code });
  }

  uploadProfilePhoto(file: File): Observable<ProfileResponse> {
    const body = new FormData();
    body.append('file', file, file.name);
    return this.http.post<ProfileResponse>(this.api('/api/profile/me/photo'), body);
  }
}
