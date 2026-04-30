import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

declare global {
  interface Window {
    SockJS: any;
  }
}

export interface ChatMessage {
  id?: number;
  senderId: number;
  senderName: string;
  content: string;
  timestamp?: Date;
}

export interface RoomEvent {
  type: 'USER_JOINED' | 'USER_LEFT' | 'ROOM_ENDED' | 'ACCESS_LOCK_UPDATED';
  userId?: number;
  userName?: string;
  roomId?: number;
  workspaceAccessBlocked?: boolean;
}

export interface WhiteboardAction {
  type: 'draw' | 'clear' | 'undo';
  data?: any;
}

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {
  private stompClient: Client | null = null;
  private connected = false;
  private activeRoomId: number | null = null;
  private connectionSubject = new Subject<boolean>();
  private chatSubject = new Subject<ChatMessage>();
  private roomEventSubject = new Subject<RoomEvent>();
  private whiteboardSubject = new Subject<WhiteboardAction>();

  connectionStatus$ = this.connectionSubject.asObservable();
  chatMessages$ = this.chatSubject.asObservable();
  roomEvents$ = this.roomEventSubject.asObservable();
  whiteboardActions$ = this.whiteboardSubject.asObservable();

  connect(roomId: number): void {
    if (this.connected && this.activeRoomId === roomId) {
      return;
    }

    if (this.stompClient) {
      this.disconnect();
    }

    this.activeRoomId = roomId;
    this.stompClient = new Client({
      webSocketFactory: () => {
        const SockJSCtor = window.SockJS || SockJS;
        return new SockJSCtor('/ws');
      },
      reconnectDelay: 3000
    });

    this.stompClient.onConnect = () => {
      this.connected = true;
      this.connectionSubject.next(true);

      this.stompClient?.subscribe(`/topic/room/${roomId}`, (message: Message) => {
        const event = JSON.parse(message.body);
        this.roomEventSubject.next(event);
      });

      this.stompClient?.subscribe(`/topic/room/${roomId}/chat`, (message: Message) => {
        const chatMessage = JSON.parse(message.body);
        this.chatSubject.next(chatMessage);
      });

      this.stompClient?.subscribe(`/topic/room/${roomId}/whiteboard`, (message: Message) => {
        const action = JSON.parse(message.body);
        this.whiteboardSubject.next(action);
      });
    };

    this.stompClient.onStompError = () => {
      this.connected = false;
      this.connectionSubject.next(false);
    };

    this.stompClient.onWebSocketError = () => {
      this.connected = false;
      this.connectionSubject.next(false);
    };

    this.stompClient.onDisconnect = () => {
      this.connected = false;
      this.connectionSubject.next(false);
    };

    this.stompClient.activate();
  }

  disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
      this.connected = false;
      this.activeRoomId = null;
      this.connectionSubject.next(false);
    }
  }

  sendChatMessage(roomId: number, message: ChatMessage): void {
    this.stompClient?.publish({
      destination: `/app/room/${roomId}/chat`,
      body: JSON.stringify(message)
    });
  }

  sendWhiteboardAction(roomId: number, action: WhiteboardAction): void {
    this.stompClient?.publish({
      destination: `/app/room/${roomId}/whiteboard`,
      body: JSON.stringify(action)
    });
  }

  isConnected(): boolean {
    return this.connected;
  }
}