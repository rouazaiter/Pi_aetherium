import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import type { ConversationResponse, ConversationSummaryResponse, DirectMessageResponse } from '../models/api.models';

@Injectable({ providedIn: 'root' })
export class MessageService {
  private readonly http = inject(HttpClient);

  conversations(): Observable<ConversationResponse[]> {
    return this.http.get<ConversationResponse[]>(`${environment.apiUrl}/api/messages/conversations`);
  }

  messages(conversationId: number): Observable<DirectMessageResponse[]> {
    return this.http.get<DirectMessageResponse[]>(`${environment.apiUrl}/api/messages/conversations/${conversationId}`);
  }

  summarizeConversation(conversationId: number): Observable<ConversationSummaryResponse> {
    return this.http.get<ConversationSummaryResponse>(
      `${environment.apiUrl}/api/messages/conversations/${conversationId}/summary`,
    );
  }

  ensureConversation(recipientId: number): Observable<ConversationResponse> {
    return this.http.post<ConversationResponse>(`${environment.apiUrl}/api/messages/conversations/ensure`, {
      recipientId,
    });
  }

  send(recipientId: number, content: string, replyToMessageId?: number | null): Observable<DirectMessageResponse> {
    return this.http.post<DirectMessageResponse>(`${environment.apiUrl}/api/messages/send`, {
      recipientId,
      content,
      replyToMessageId: replyToMessageId ?? null,
    });
  }

  sendVoice(file: Blob, recipientId: number, replyToMessageId?: number | null): Observable<DirectMessageResponse> {
    const formData = new FormData();
    formData.append('file', file, 'voice-message.webm');
    formData.append('recipientId', String(recipientId));
    if (replyToMessageId != null) {
      formData.append('replyToMessageId', String(replyToMessageId));
    }
    return this.http.post<DirectMessageResponse>(`${environment.apiUrl}/api/messages/voice`, formData);
  }

  react(messageId: number, emoji: string): Observable<DirectMessageResponse> {
    return this.http.post<DirectMessageResponse>(`${environment.apiUrl}/api/messages/${messageId}/reaction`, {
      emoji,
    });
  }

  deleteMessage(messageId: number, scope: 'ME' | 'EVERYONE'): Observable<void> {
    return this.http.post<void>(`${environment.apiUrl}/api/messages/${messageId}/delete`, {
      scope,
    });
  }

  stream(token: string): EventSource {
    return new EventSource(`${environment.apiUrl}/api/messages/stream?access_token=${encodeURIComponent(token)}`);
  }
}
