import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoomSessionService, ChatMessage } from '../../../core/services/room-session/room-session.service';
import { WebSocketService, ChatMessage as WsChatMessage } from '../../../core/services/room-session/websocket.service';

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
  private destroy$ = new Subject<void>();
  private shouldScroll = false;

  constructor(
    private roomSessionService: RoomSessionService,
    private websocketService: WebSocketService
  ) {}

  ngOnInit(): void {
    this.loadMessages();
    this.subscribeToChatMessages();
  }

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  sendMessage(): void {
    if (!this.newMessage.trim()) return;

    const message: WsChatMessage = {
      senderId: this.userId,
      senderName: this.userName,
      content: this.newMessage
    };

    this.websocketService.sendChatMessage(this.roomId, message);
    this.newMessage = '';
  }

  formatTime(timestamp: Date): string {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  }

  private loadMessages(): void {
    this.roomSessionService.getMessages(this.roomId).subscribe(messages => {
      this.messages = messages;
      this.shouldScroll = true;
    });
  }

  private subscribeToChatMessages(): void {
    this.websocketService.chatMessages$
      .pipe(takeUntil(this.destroy$))
      .subscribe(message => {
        this.messages.push({
          id: message.id || Date.now(),
          senderId: message.senderId,
          senderName: message.senderName,
          content: message.content,
          timestamp: message.timestamp || new Date()
        });
        this.shouldScroll = true;
      });
  }

  private scrollToBottom(): void {
    if (this.chatMessagesEl) {
      const el = this.chatMessagesEl.nativeElement;
      el.scrollTop = el.scrollHeight;
    }
  }
}