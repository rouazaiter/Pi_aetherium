import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { Client, Message } from '@stomp/stompjs';

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
  type: 'USER_JOINED' | 'USER_LEFT' | 'ROOM_ENDED';
  userId?: number;
  userName?: string;
  roomId?: number;
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
  private connectionSubject = new Subject<boolean>();
  private chatSubject = new Subject<ChatMessage>();
  private roomEventSubject = new Subject<RoomEvent>();
  private whiteboardSubject = new Subject<WhiteboardAction>();

  connectionStatus$ = this.connectionSubject.asObservable();
  chatMessages$ = this.chatSubject.asObservable();
  roomEvents$ = this.roomEventSubject.asObservable();
  whiteboardActions$ = this.whiteboardSubject.asObservable();

  connect(roomId: number): void {
    if (this.connected) {
      return;
    }

    const socket = new window.SockJS('/ws');
    this.stompClient = new Client({
      webSocketFactory: () => socket
    });

    this.stompClient.onConnect = (frame: any) => {
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