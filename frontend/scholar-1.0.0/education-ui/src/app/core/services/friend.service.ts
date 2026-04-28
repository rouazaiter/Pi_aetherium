import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { AddFriendRequest, FriendResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class FriendService {
  private readonly http = inject(HttpClient);

  list(): Observable<FriendResponse[]> {
    return this.http.get<FriendResponse[]>(`${environment.apiUrl}/api/friends`);
  }

  add(body: AddFriendRequest): Observable<FriendResponse> {
    return this.http.post<FriendResponse>(`${environment.apiUrl}/api/friends`, body);
  }

  remove(friendId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/api/friends/${friendId}`);
  }
}
