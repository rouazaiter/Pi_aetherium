import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { FriendProfileResponse, FriendRequestResponse, FriendResponse, FriendSearchResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class FriendService {
  private readonly http = inject(HttpClient);

  list(): Observable<FriendResponse[]> {
    return this.http.get<FriendResponse[]>(`${environment.apiUrl}/api/friends`);
  }

  search(query: string): Observable<FriendSearchResponse[]> {
    return this.http.get<FriendSearchResponse[]>(`${environment.apiUrl}/api/friends/search`, {
      params: { query },
    });
  }

  discover(): Observable<FriendSearchResponse[]> {
    return this.http.get<FriendSearchResponse[]>(`${environment.apiUrl}/api/friends/discover`);
  }

  listIncomingRequests(): Observable<FriendRequestResponse[]> {
    return this.http.get<FriendRequestResponse[]>(`${environment.apiUrl}/api/friends/requests/incoming`);
  }

  sendRequest(userId: number): Observable<FriendRequestResponse> {
    return this.http.post<FriendRequestResponse>(`${environment.apiUrl}/api/friends/requests`, { userId });
  }

  acceptRequest(requestId: number): Observable<FriendResponse> {
    return this.http.post<FriendResponse>(`${environment.apiUrl}/api/friends/requests/${requestId}/accept`, {});
  }

  declineRequest(requestId: number): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/api/friends/requests/${requestId}/decline`, {});
  }

  remove(friendId: number): Observable<void> {
    return this.http.delete<void>(`${environment.apiUrl}/api/friends/${friendId}`);
  }

  profile(friendId: number): Observable<FriendProfileResponse> {
    return this.http.get<FriendProfileResponse>(`${environment.apiUrl}/api/friends/${friendId}/profile`);
  }
}
