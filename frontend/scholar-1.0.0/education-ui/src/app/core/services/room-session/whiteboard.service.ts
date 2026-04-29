import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface WhiteboardRoom {
  uuid: string;
  room?: any;
}

export interface RoomTokenResponse {
  roomToken: string;
  expireAt: number;
}

export interface WhiteboardConfig {
  appIdentifier: string;
  region?: string;
}

export interface FastboardModule {
  createFastboard: (options: any) => Promise<any>;
  mount: (app: any, div: HTMLElement) => any;
}

@Injectable({
  providedIn: 'root'
})
export class WhiteboardService {
  private apiUrl = '/api/whiteboard';
  private fastboardLoadPromise: Promise<FastboardModule> | null = null;

  constructor(private http: HttpClient) {}

  getConfig(): Observable<WhiteboardConfig> {
    return this.http.get<WhiteboardConfig>(`${this.apiUrl}/config`);
  }

  /**
   * Creates a new whiteboard room
   * @param name Optional room name
   */
  createRoom(name?: string): Observable<WhiteboardRoom> {
    return this.http.post<WhiteboardRoom>(`${this.apiUrl}/create-room`, { name: name || `Room-${Date.now()}` });
  }

  /**
   * Gets a room token for joining a whiteboard room
   * @param uuid Room UUID
   * @param role 'writer' | 'reader' | 'admin'
   * @param lifespanMs Token lifespan in milliseconds (default 1 hour)
   */
  getRoomToken(
    uuid: string, 
    role: 'writer' | 'reader' | 'admin' = 'writer', 
    lifespanMs: number = 3600000
  ): Observable<RoomTokenResponse> {
    return this.http.post<RoomTokenResponse>(`${this.apiUrl}/room-token`, {
      uuid,
      role,
      lifespan: lifespanMs
    });
  }

  /**
   * Refreshes an existing room token
   * @param uuid Room UUID
   */
  refreshRoomToken(uuid: string): Observable<RoomTokenResponse> {
    return this.http.post<RoomTokenResponse>(`${this.apiUrl}/refresh-room-token`, { uuid });
  }

  /**
   * Gets the mapped whiteboard UUID for an app room
   * @param appRoomId The application's room ID
   */
  getMappedRoom(appRoomId: number): Observable<{ uuid: string }> {
    return this.http.get<{ uuid: string }>(`${this.apiUrl}/map/${appRoomId}`);
  }

  /**
   * Maps an app room to a whiteboard room
   * @param appRoomId The application's room ID
   * @param uuid Whiteboard room UUID
   */
  mapRoom(appRoomId: number, uuid: string): Observable<{ success: boolean; uuid: string }> {
    return this.http.post<{ success: boolean; uuid: string }>(`${this.apiUrl}/map/${appRoomId}`, { uuid });
  }

  loadFastboard(): Promise<FastboardModule> {
    if (this.fastboardLoadPromise) {
      return this.fastboardLoadPromise;
    }

    this.fastboardLoadPromise = this.resolveFastboard();
    return this.fastboardLoadPromise;
  }

  private async resolveFastboard(): Promise<FastboardModule> {
    const fastboardModule: any = await import('@netless/fastboard');
    const createFastboard = fastboardModule?.createFastboard;
    const mount = fastboardModule?.mount;

    if (typeof createFastboard !== 'function' || typeof mount !== 'function') {
      throw new Error('Fastboard SDK introuvable dans le bundle frontend.');
    }

    return { createFastboard, mount };
  }
}