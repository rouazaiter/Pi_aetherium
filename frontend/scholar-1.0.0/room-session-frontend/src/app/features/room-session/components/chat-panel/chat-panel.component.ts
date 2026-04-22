import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewChecked } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { RoomSessionService, ChatMessage } from '../../services/room-session.service';
import { WebSocketService, ChatMessage as WsChatMessage } from '../../services/websocket.service';

@Component({
  selector: 'app-chat-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <section class="h-full flex flex-col min-h-0 bg-white">
      <div #chatMessages class="flex-1 overflow-auto p-4 space-y-4 bg-white">
        <div *ngFor="let msg of messages" class="flex flex-col gap-1" [class.items-end]="msg.senderId === userId">
          <div class="flex items-center justify-between w-full">
            <span class="text-body-sm font-bold" [ngClass]="msg.senderId === userId ? 'text-primary' : 'text-slate-900'">{{ msg.senderId === userId ? 'You' : msg.senderName }}</span>
            <span class="text-[10px] text-slate-400">{{ formatTime(msg.timestamp) }}</span>
          </div>
          <div class="p-2 rounded-lg text-body-sm max-w-[90%]" [ngClass]="msg.senderId === userId ? 'bg-primary-container text-white rounded-tr-none' : 'bg-slate-100 text-slate-800 rounded-tl-none'">
            {{ msg.content }}
          </div>
        </div>
      </div>

      <div class="p-4 border-t border-slate-100">
        <div class="relative">
          <textarea
            [(ngModel)]="newMessage"
            (keyup.enter)="sendMessage()"
            placeholder="Type a message..."
            rows="1"
            class="w-full border border-slate-200 bg-white rounded-lg p-2 pr-10 text-body-sm focus:outline-none focus:ring-1 focus:ring-primary resize-none overflow-hidden text-slate-900"></textarea>
          <button type="button" (click)="sendMessage()" class="absolute right-2 top-1/2 -translate-y-1/2 text-primary hover:text-primary-container">
            <span class="material-symbols-outlined">send</span>
          </button>
        </div>
      </div>
    </section>
  `
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