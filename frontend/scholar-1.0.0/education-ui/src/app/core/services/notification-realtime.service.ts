import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Client, IMessage } from '@stomp/stompjs';
import { BehaviorSubject, Subscription } from 'rxjs';
import { CurrentUserService } from '../auth/current-user.service';
import { RealtimeNotification } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationRealtimeService implements OnDestroy {
  private stompClient: Client | null = null;
  private userSub: Subscription;
  private activeUserId: number | null = null;

  private readonly notificationsSubject = new BehaviorSubject<RealtimeNotification[]>([]);
  readonly notifications$ = this.notificationsSubject.asObservable();

  constructor(
    private currentUserService: CurrentUserService,
    private http: HttpClient
  ) {
    this.userSub = this.currentUserService.currentUser$.subscribe(user => {
      if (user.id <= 0) {
        this.activeUserId = null;
        this.notificationsSubject.next([]);
        this.disconnect();
        return;
      }

      this.activeUserId = user.id;
      this.loadHistory(user.id);
    });
  }

  clear(): void {
    this.notificationsSubject.next([]);
  }

  ngOnDestroy(): void {
    this.userSub.unsubscribe();
    this.disconnect();
  }

  private loadHistory(userId: number): void {
    this.http.get<RealtimeNotification[]>(`/skillhub/api/notifications/user/${userId}?limit=30`).subscribe({
      next: (history) => {
        if (this.activeUserId !== userId) {
          return;
        }

        this.notificationsSubject.next(this.normalizeNotifications(history ?? []));
        void this.connectForUser(userId);
      },
      error: () => {
        if (this.activeUserId !== userId) {
          return;
        }

        void this.connectForUser(userId);
      }
    });
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
    this.notificationsSubject.next(this.normalizeNotifications([parsed, ...current]));
  }

  private normalizeNotifications(notifications: RealtimeNotification[]): RealtimeNotification[] {
    const seen = new Set<string>();
    const ordered = [...notifications].sort((a, b) => {
      const left = new Date(a.createdAt || 0).getTime();
      const right = new Date(b.createdAt || 0).getTime();
      return right - left;
    });

    const deduped: RealtimeNotification[] = [];

    for (const notification of ordered) {
      const key = [
        notification.type,
        notification.message,
        notification.createdAt,
        notification.serviceRequestId ?? '',
        notification.applicationId ?? ''
      ].join('|');

      if (seen.has(key)) {
        continue;
      }

      seen.add(key);
      deduped.push(notification);

      if (deduped.length >= 30) {
        break;
      }
    }

    return deduped;
  }
}
