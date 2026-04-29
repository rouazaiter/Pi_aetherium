import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  recommendations?: CertCard[];
  isTyping?: boolean;
}

export interface CertCard {
  id: number;
  title: string;
  category: string;
  difficulty: string;
  price: number;
  description: string;
  coverImageUrl: string | null;
}

export interface ChatResponse {
  reply: string;
  recommendations: CertCard[];
}

@Injectable({ providedIn: 'root' })
export class ChatAssistantService {
  private readonly API = 'http://localhost:8089/api/chat/assistant';

  constructor(private http: HttpClient) {}

  send(messages: { role: string; content: string }[]): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(this.API, { messages });
  }
}
