import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';

export interface RoomSession {
  id: number;
  name: string;
  hostUserId: number;
  hostUserName?: string;
  workspaceAccessBlocked?: boolean;
  participantCount?: number;
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

export interface JoinRoomByNameResponse {
  roomId: number;
  roomName: string;
  hostUserId: number;
  hostUserName?: string;
  workspaceAccessBlocked?: boolean;
}

export interface MyRoomsByRoleResponse {
  hostRooms: RoomSession[];
  participantRooms: RoomSession[];
}

@Injectable({
  providedIn: 'root'
})
export class RoomSessionService {
  private readonly apiUrl = `${environment.apiUrl}/api/rooms`;

  constructor(private http: HttpClient) {}

  createRoom(name: string, hostUserId: number): Observable<RoomSession> {
    console.log('API call:', this.apiUrl, { name, hostUserId });
    return this.http.post<RoomSession>(this.apiUrl, { name, hostUserId });
  }

  getRoom(id: number): Observable<RoomSession> {
    return this.http.get<RoomSession>(`${this.apiUrl}/${id}`);
  }

  getActiveRooms(): Observable<RoomSession[]> {
    return this.http.get<RoomSession[]>(`${this.apiUrl}/active`);
  }

  getMyActiveRoomsByRole(userId: number): Observable<MyRoomsByRoleResponse> {
    return this.http.get<MyRoomsByRoleResponse>(`${this.apiUrl}/active/by-user/${userId}`);
  }

  joinRoom(roomId: number, userId: number, userName: string): Observable<Participant> {
    return this.http.post<Participant>(`${this.apiUrl}/${roomId}/join`, { userId, userName });
  }

  joinRoomByName(roomName: string, userId: number, userName: string): Observable<JoinRoomByNameResponse> {
    return this.http.post<JoinRoomByNameResponse>(`${this.apiUrl}/join-by-name`, { roomName, userId, userName });
  }

  setWorkspaceAccess(roomId: number, userId: number, blocked: boolean): Observable<{ roomId: number; workspaceAccessBlocked: boolean }> {
    return this.http.post<{ roomId: number; workspaceAccessBlocked: boolean }>(
      `${this.apiUrl}/${roomId}/workspace-access`,
      { userId, blocked }
    );
  }

  leaveRoom(roomId: number, userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${roomId}/leave`, { userId });
  }

  endRoom(roomId: number, userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/${roomId}/end`, { userId });
  }

  getParticipants(roomId: number): Observable<Participant[]> {
    return this.http.get<Participant[]>(`${this.apiUrl}/${roomId}/participants`);
  }

  getMessages(roomId: number): Observable<ChatMessage[]> {
    return this.http.get<ChatMessage[]>(`${this.apiUrl}/${roomId}/messages`);
  }

  sendMessage(roomId: number, senderId: number, senderName: string, content: string): Observable<ChatMessage> {
    return this.http.post<ChatMessage>(`${this.apiUrl}/${roomId}/messages`, { senderId, senderName, content });
  }

  getAgoraToken(roomId: number, userId: number): Observable<{ token: string; appId: string; channelName: string }> {
    return this.http.post<{ token: string; appId: string; channelName: string }>(`${this.apiUrl}/${roomId}/token`, { userId });
  }

  uploadRecording(roomId: number, file: File, type: string): Observable<Recording> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);
    return this.http.post<Recording>(`${this.apiUrl}/${roomId}/recordings`, formData);
  }

  getRecordings(roomId: number): Observable<Recording[]> {
    return this.http.get<Recording[]>(`${this.apiUrl}/${roomId}/recordings`);
  }
}
