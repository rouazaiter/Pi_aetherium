import {
  Component, OnInit, OnDestroy, ViewChild, ElementRef,
  AfterViewChecked, ChangeDetectorRef
} from '@angular/core';
import { Router } from '@angular/router';
import { ChatAssistantService, ChatMessage, CertCard } from './chat-assistant.service';
import { NgIf, NgFor, DecimalPipe, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface QuickAction {
  label: string;
  icon: string;
  message: string;
}

const STORAGE_KEY = 'skillhub_chat_history';

@Component({
    selector: 'app-chat-assistant',
    templateUrl: './chat-assistant.component.html',
    styleUrls: ['./chat-assistant.component.scss'],
    standalone: true,
    imports: [NgIf, NgFor, FormsModule, DecimalPipe, DatePipe]
})
export class ChatAssistantComponent implements OnInit, OnDestroy, AfterViewChecked {

  @ViewChild('messagesEnd') messagesEnd!: ElementRef;
  @ViewChild('inputEl')     inputEl!: ElementRef<HTMLTextAreaElement>;

  isOpen    = false;
  isTyping  = false;
  inputText = '';
  messages: ChatMessage[] = [];
  private shouldScroll = false;

  readonly quickActions: QuickAction[] = [
    { label: 'Recommend for me',       icon: '🎯', message: 'Can you recommend a certification based on my goals?' },
    { label: 'Compare certifications', icon: '⚖️', message: 'I want to compare two certifications.' },
    { label: 'Beginner path',          icon: '🌱', message: 'Show me the best beginner learning path.' },
    { label: 'Advanced path',          icon: '🔥', message: 'What are the most advanced certifications available?' },
  ];

  private readonly WELCOME: ChatMessage = {
    role: 'assistant',
    content: `Hi! I'm your **SkillHub Certification Advisor** 👋\n\nI can help you:\n• Find the right certification for your goals\n• Compare certifications\n• Build a learning path\n• Understand what each cert covers\n\nWhat are you looking to achieve?`,
    timestamp: new Date()
  };

  constructor(
    private chatService: ChatAssistantService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loadHistory();
  }

  ngOnDestroy(): void {}

  ngAfterViewChecked(): void {
    if (this.shouldScroll) {
      this.scrollToBottom();
      this.shouldScroll = false;
    }
  }

  // ── Persistence ───────────────────────────────────────────────────────────

  private loadHistory(): void {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed: ChatMessage[] = JSON.parse(raw);
        // Restore Date objects (JSON serialises them as strings)
        this.messages = parsed.map(m => ({ ...m, timestamp: new Date(m.timestamp) }));
        return;
      }
    } catch { /* corrupt storage — fall through to welcome */ }
    this.messages = [{ ...this.WELCOME }];
  }

  private saveHistory(): void {
    try {
      // Keep last 60 messages to avoid bloating localStorage
      const toSave = this.messages.slice(-60);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(toSave));
    } catch { /* storage full — ignore */ }
  }

  clearHistory(): void {
    localStorage.removeItem(STORAGE_KEY);
    this.messages = [{ ...this.WELCOME, timestamp: new Date() }];
    this.shouldScroll = true;
  }

  // ── Open / close ──────────────────────────────────────────────────────────

  toggle(): void {
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      this.shouldScroll = true;
      setTimeout(() => this.inputEl?.nativeElement?.focus(), 150);
    }
  }

  close(): void { this.isOpen = false; }

  // ── Send message ──────────────────────────────────────────────────────────

  sendQuickAction(action: QuickAction): void {
    this.sendMessage(action.message);
  }

  onKeydown(e: KeyboardEvent): void {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      this.send();
    }
  }

  send(): void {
    const text = this.inputText.trim();
    if (!text || this.isTyping) return;
    this.sendMessage(text);
    this.inputText = '';
  }

  private sendMessage(text: string): void {
    this.messages.push({ role: 'user', content: text, timestamp: new Date() });
    this.shouldScroll = true;
    this.isTyping = true;
    this.saveHistory();

    const history = this.messages
      .filter(m => !m.isTyping)
      .map(m => ({ role: m.role, content: m.content }));

    this.chatService.send(history).subscribe({
      next: res => {
        this.isTyping = false;
        this.messages.push({
          role: 'assistant',
          content: res.reply,
          timestamp: new Date(),
          recommendations: res.recommendations?.length ? res.recommendations : undefined
        });
        this.shouldScroll = true;
        this.saveHistory();
        this.cdr.detectChanges();
      },
      error: () => {
        this.isTyping = false;
        this.messages.push({
          role: 'assistant',
          content: 'Sorry, I had trouble connecting. Please try again.',
          timestamp: new Date()
        });
        this.shouldScroll = true;
        this.saveHistory();
        this.cdr.detectChanges();
      }
    });
  }

  // ── Navigation ────────────────────────────────────────────────────────────

  openCert(id: number): void {
    this.close();
    this.router.navigate(['/skillhub/store', id]);
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private scrollToBottom(): void {
    try {
      this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' });
    } catch {}
  }

  formatContent(text: string): string {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/^• /gm, '<span class="bullet">•</span> ')
      .replace(/\n/g, '<br>');
  }

  difficultyColor(d: string): string {
    return { BEGINNER: '#10b981', INTERMEDIATE: '#f59e0b', ADVANCED: '#ef4444' }[d] ?? '#6366f1';
  }

  difficultyBg(d: string): string {
    return this.difficultyColor(d) + '18';
  }

  get unreadCount(): number { return 0; }
}
