import { Component, ElementRef, HostListener } from '@angular/core';
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
  isOpen = false;

  constructor(
    private elementRef: ElementRef,
    private notificationService: NotificationRealtimeService,
    private currentUserService: CurrentUserService
  ) {
    this.notifications$ = this.notificationService.notifications$;
    this.unreadCount$ = this.notificationService.notifications$.pipe(map(list => list.length));
    this.currentUser$ = this.currentUserService.currentUser$;
  }

  toggle(event?: MouseEvent): void {
    event?.stopPropagation();
    this.isOpen = !this.isOpen;
  }

  close(): void {
    this.isOpen = false;
  }

  clear(): void {
    this.notificationService.clear();
    this.close();
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent): void {
    if (!this.elementRef.nativeElement.contains(event.target)) {
      this.close();
    }
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    this.close();
  }
}
