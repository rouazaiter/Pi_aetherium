import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoomSessionService, ChatMessage } from '../../../core/services/room-session/room-session.service';
import { WebSocketService } from '../../../core/services/room-session/websocket.service';

@Component({
  selector: 'app-chat-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-panel.component.html',
  styleUrls: ['./chat-panel.component.scss']
})
export class ChatPanelComponent implements OnInit, OnDestroy, AfterViewChecked {
  @Input() roomId: number = 0;
  @Input() userId: number = 0;
  @Input() userName: string = '';

  @ViewChild('chatMessages') chatMessagesEl!: ElementRef;

  messages: ChatMessage[] = [];
  newMessage = '';
  isConnected = false;
  isSending = false;
  recentMessageId: number | null = null;
  typingHintVisible = false;
  chatError = '';
  private destroy$ = new Subject<void>();
  private shouldScroll = false;
  private typingHintTimeout: ReturnType<typeof setTimeout> | null = null;
  private refreshTimer: ReturnType<typeof setInterval> | null = null;

  constructor(
    private roomSessionService: RoomSessionService,
    private websocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.loadMessages();
    this.startMessageRefresh();
    this.subscribeToChatMessages();
    this.subscribeToRoomEvents();
    this.subscribeToConnectionStatus();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  ngOnDestroy(): void {
    if (this.typingHintTimeout) {
      clearTimeout(this.typingHintTimeout);
    }
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer);
      this.refreshTimer = null;
    }
    this.destroy$.next();
    this.destroy$.complete();
  }

  sendMessage(): void {
    const content = this.newMessage.trim();
    if (!content || this.isSending) return;
    if (!Number.isFinite(this.userId) || this.userId <= 0) {
      this.chatError = 'Invalid user session. Please refresh and sign in again.';
      return;
    }

    this.chatError = '';
    this.isSending = true;
    this.roomSessionService.sendMessage(this.roomId, this.userId, this.userName, content).subscribe({
      next: (savedMessage) => {
        this.upsertMessage(
          {
            id: savedMessage.id,
            senderId: savedMessage.senderId,
            senderName: savedMessage.senderName,
            content: savedMessage.content,
            timestamp: savedMessage.timestamp || new Date()
          },
          true
        );
        this.newMessage = '';
        this.isSending = false;
      },
      error: (err) => {
        const serverMessage = err?.error?.error;
        this.chatError = serverMessage || 'Unable to send the message. Please retry.';
        this.isSending = false;
      }
    });
  }

  formatTime(timestamp: Date): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  trackMessage(index: number, msg: ChatMessage): string {
    return `${msg.id ?? 'x'}-${msg.senderId}-${msg.timestamp}-${index}`;
  }

  onMessageInput(): void {
    this.typingHintVisible = true;
    if (this.typingHintTimeout) {
      clearTimeout(this.typingHintTimeout);
    }
    this.typingHintTimeout = setTimeout(() => {
      this.typingHintVisible = false;
      this.typingHintTimeout = null;
    }, 900);
  }

  onMessageKeydown(event: KeyboardEvent): void {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.sendMessage();
    }
  }

  private loadMessages(): void {
    this.roomSessionService.getMessages(this.roomId).subscribe(messages => {
      this.messages = this.sortMessages(messages).map(message => ({
        ...message,
        timestamp: message.timestamp || new Date()
      }));
      this.shouldScroll = true;
    });
  }

  private startMessageRefresh(): void {
    this.refreshTimer = setInterval(() => {
      this.roomSessionService.getMessages(this.roomId).subscribe(messages => {
        const nextMessages = this.sortMessages(messages).map(message => ({
          ...message,
          timestamp: message.timestamp || new Date()
        }));
        if (nextMessages.length !== this.messages.length) {
          this.messages = nextMessages;
          this.shouldScroll = true;
        }
      });
    }, 4000);
  }

  private subscribeToChatMessages(): void {
    this.websocketService.chatMessages$
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        const incomingMessage = {
          id: message.id || Date.now(),
          senderId: message.senderId,
          senderName: message.senderName,
          content: message.content,
          timestamp: message.timestamp || new Date()
        };
        this.upsertMessage(incomingMessage, true);
      });
  }

  private subscribeToRoomEvents(): void {
    this.websocketService.roomEvents$
      .pipe(takeUntil(this.destroy$))
      .subscribe(event => {
        if (event.type === 'USER_JOINED' && event.userName && event.userId !== this.userId) {
          this.messages.push({
            id: Date.now(),
            senderId: 0,
            senderName: 'System',
            content: `${event.userName} joined the session`,
            timestamp: new Date()
          });
          this.shouldScroll = true;
        }
        if (event.type === 'USER_LEFT' && event.userId && event.userId !== this.userId) {
          this.messages.push({
            id: Date.now(),
            senderId: 0,
            senderName: 'System',
            content: `Participant #${event.userId} left the session`,
            timestamp: new Date()
          });
          this.shouldScroll = true;
        }
      });
  }

  private subscribeToConnectionStatus(): void {
    this.websocketService.connectionStatus$
      .pipe(takeUntil(this.destroy$))
      .subscribe(connected => {
        this.isConnected = connected;
      });
  }

  private hasMessage(message: ChatMessage): boolean {
    return this.messages.some(msg =>
      msg.id === message.id ||
      (msg.senderId === message.senderId &&
        msg.content === message.content &&
        new Date(msg.timestamp).getTime() === new Date(message.timestamp).getTime())
    );
  }

  private sortMessages(messages: ChatMessage[]): ChatMessage[] {
    return [...messages].sort((a, b) => new Date(a.timestamp).getTime() - new Date(b.timestamp).getTime());
  }

  private upsertMessage(message: ChatMessage, markRecent: boolean): void {
    if (this.hasMessage(message)) {
      return;
    }
    this.messages.push(message);
    this.messages = this.sortMessages(this.messages);
    if (markRecent) {
      this.recentMessageId = message.id ?? null;
      setTimeout(() => {
        this.recentMessageId = null;
      }, 450);
    }
    this.shouldScroll = true;
  }

  private scrollToBottom(): void {
    if (this.chatMessagesEl) {
      const el = this.chatMessagesEl.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }
}