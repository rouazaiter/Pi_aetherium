import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface RoomSession {
  id: number;
  name: string;
  hostUserId: number;
  status: 'ACTIVE' | 'ENDED';
  startTime: Date;
  endTime?: Date;
  agoraChannelName: string;
  agoraToken?: string;
}

export interface Participant {
  id: number;
  userId: number;
  userName: string;
  role: 'HOST' | 'PARTICIPANT';
  joinedAt: Date;
  leftAt?: Date;
}

export interface ChatMessage {
  id: number;
  senderId: number;
  senderName: string;
  content: string;
  timestamp: Date;
}

export interface Recording {
  id: number;
  fileName: string;
  filePath: string;
  fileSize: number;
  contentType: string;
  type: 'VIDEO' | 'WHITEBOARD' | 'IDE';
  durationSeconds?: number;
  createdAt: Date;
}

@Injectable({
  providedIn: 'root'
})
export class RoomSessionService {
  private api(path: string): string {
    const base = (environment.apiUrl ?? '').trim().replace(/\/$/, '');
    const p = path.startsWith('/') ? path : `/${path}`;
    return base ? `${base}${p}` : p;
  }

  constructor(private http: HttpClient) {}

  createRoom(name: string, hostUserId: number): Observable<RoomSession> {
    return this.http.post<RoomSession>(this.api('/api/rooms'), { name, hostUserId });
  }

  getRoom(id: number): Observable<RoomSession> {
    return this.http.get<RoomSession>(this.api(`/api/rooms/${id}`));
  }

  getActiveRooms(): Observable<RoomSession[]> {
    return this.http.get<RoomSession[]>(this.api('/api/rooms/active'));
  }

  joinRoom(roomId: number, userId: number, userName: string): Observable<Participant> {
    return this.http.post<Participant>(this.api(`/api/rooms/${roomId}/join`), { userId, userName });
  }

  leaveRoom(roomId: number, userId: number): Observable<any> {
    return this.http.post(this.api(`/api/rooms/${roomId}/leave`), { userId });
  }

  endRoom(roomId: number, userId: number): Observable<any> {
    return this.http.post(this.api(`/api/rooms/${roomId}/end`), { userId });
  }

  getParticipants(roomId: number): Observable<Participant[]> {
    return this.http.get<Participant[]>(this.api(`/api/rooms/${roomId}/participants`));
  }

  getMessages(roomId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(this.api(`/api/rooms/${roomId}/messages`));
  }

  sendMessage(roomId: number, senderId: number, senderName: string, content: string): Observable<any> {
    return this.http.post(this.api(`/api/rooms/${roomId}/messages`), { senderId, senderName, content });
  }

  getAgoraToken(roomId: number, userId: number): Observable<{ token: string; appId: string; channelName: string }> {
    return this.http.post<{ token: string; appId: string; channelName: string }>(this.api(`/api/rooms/${roomId}/token`), { userId });
  }

  uploadRecording(roomId: number, file: File, type: string): Observable<Recording> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return this.http.post<Recording>(this.api(`/api/rooms/${roomId}/recordings`), formData);
  }

  getRecordings(roomId: number): Observable<Recording[]> {
    return this.http.get<Recording[]>(this.api(`/api/rooms/${roomId}/recordings`));
  }
}