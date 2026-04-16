import { Injectable, OnDestroy } from '@angular/core';
import { Client, IMessage } from '@stomp/stompjs';
import { BehaviorSubject, Subscription } from 'rxjs';
import { CurrentUserService } from '../auth/current-user.service';
import { RealtimeNotification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationRealtimeService implements OnDestroy {
  private stompClient: Client | null = null;
  private userSub: Subscription;

  private readonly notificationsSubject = new BehaviorSubject<RealtimeNotification[]>([]);
  readonly notifications$ = this.notificationsSubject.asObservable();

  constructor(private currentUserService: CurrentUserService) {
    this.userSub = this.currentUserService.currentUser$.subscribe(user => {
      if (user.id <= 0) {
        this.disconnect();
        return;
      }
      this.connectForUser(user.id);
    });
  }

  clear(): void {
    this.notificationsSubject.next([]);
  }

  ngOnDestroy(): void {
    this.userSub.unsubscribe();
    this.disconnect();
  }

  private async connectForUser(userId: number): Promise<void> {
    this.disconnect();

    const { default: SockJS } = await import('sockjs-client');

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8089/skillhub/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        this.stompClient?.subscribe(`/topic/notifications/user/${userId}`, (message: IMessage) => {
          this.handleIncomingMessage(message.body);
        });
      }
    });

    this.stompClient.activate();
  }

  private disconnect(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }

  private handleIncomingMessage(rawBody: string): void {
    const parsed = JSON.parse(rawBody) as RealtimeNotification;
    const current = this.notificationsSubject.value;
    this.notificationsSubject.next([parsed, ...current].slice(0, 30));
  }
}
