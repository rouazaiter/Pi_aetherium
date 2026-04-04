import { Component } from '@angular/core';
import { map } from 'rxjs';
import { CurrentUserService } from '../../core/auth/current-user.service';
import { NotificationRealtimeService } from '../../core/services/notification-realtime.service';

@Component({
  selector: 'app-notification-bar',
  templateUrl: './notification-bar.component.html',
  styleUrls: ['./notification-bar.component.css']
})
export class NotificationBarComponent {
  notifications$;
  unreadCount$;
  currentUser$;

  constructor(
    private notificationService: NotificationRealtimeService,
    private currentUserService: CurrentUserService
  ) {
    this.notifications$ = this.notificationService.notifications$;
    this.unreadCount$ = this.notificationService.notifications$.pipe(map(list => list.length));
    this.currentUser$ = this.currentUserService.currentUser$;
  }

  clear(): void {
    this.notificationService.clear();
  }
}
