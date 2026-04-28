import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { Discussion, DiscussionMessage, Reaction, ReactionType } from '../../core/models/blog.models';
import { ModerationService } from '../../core/services/moderation.service';
import { ToastService } from '../../core/services/toast.service';
import { AiSummaryService, DiscussionSummary } from '../../core/services/ai-summary.service';

interface MessageVM extends DiscussionMessage {
  replies: DiscussionMessage[];
  reactions: Reaction[];
  reactionMap: Record<string, number>;
  showReplies: boolean;
}

@Component({
  selector: 'app-discussions',
  templateUrl: './discussions.component.html',
  styleUrls: ['./discussions.component.scss']
})
export class DiscussionsComponent implements OnInit {

  readonly CURRENT_USER = 1;
  readonly REACTIONS: { emoji: string; type: ReactionType }[] = [
    { emoji: '', type: 'LIKE' }, { emoji: '', type: 'LOVE' },
    { emoji: '', type: 'LAUGH' }, { emoji: '', type: 'WOW' },
    { emoji: '', type: 'SAD' }, { emoji: '', type: 'ANGRY' }
  ];

  discussions: Discussion[] = [];
  loading = true;
  searchQuery = '';
  currentPage = 1;
  readonly pageSize = 6;

  get filteredDiscussions(): Discussion[] {
    if (!this.searchQuery.trim()) return this.discussions;
    const q = this.searchQuery.toLowerCase();
    return this.discussions.filter(d =>
      d.theme.toLowerCase().includes(q) ||
      (d.creator?.username?.toLowerCase().includes(q) ?? false)
    );
  }

  get totalPages(): number { return Math.ceil(this.filteredDiscussions.length / this.pageSize) || 1; }

  get paginatedDiscussions(): Discussion[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredDiscussions.slice(start, start + this.pageSize);
  }

  get pages(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }

  onSearch(): void { this.currentPage = 1; }
  goToPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.currentPage = p; }

  activeDiscussion: Discussion | null = null;
  messages: MessageVM[] = [];
  loadingMessages = false;
  newMessage = '';
  moderationWarning = '';

  replyingTo: MessageVM | null = null;
  replyText = '';

  showCreateForm = false;
  newTheme = '';

  editingDiscussion: Discussion | null = null;
  editDiscussionTheme = '';

  editingMessageId: number | null = null;
  editingMessageText = '';

  aiSummaryResult: DiscussionSummary | null = null;
  aiSummaryLoading = false;
  showAiSummary = false;

  constructor(
    private api: ApiService,
    private moderation: ModerationService,
    private toast: ToastService,
    private aiSummary: AiSummaryService
  ) {}

  ngOnInit(): void { this.loadDiscussions(); }

  loadDiscussions(): void {
    this.loading = true;
    this.api.getDiscussions(this.CURRENT_USER).subscribe({
      next: d => { this.discussions = d; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  openDiscussion(disc: Discussion): void {
    this.activeDiscussion = disc;
    this.loadMessages(disc.id);
  }

  closeDiscussion(): void { this.activeDiscussion = null; }

  loadMessages(discId: number): void {
    this.loadingMessages = true;
    this.api.getMessages(discId).subscribe({
      next: async msgs => {
        const enriched: MessageVM[] = [];
        for (const m of msgs) {
          const replies = await this.api.getReplies(discId, m.id).toPromise() ?? [];
          const reactions = await this.api.getReactions(m.id).toPromise() ?? [];
          const reactionMap: Record<string, number> = {};
          reactions.forEach(r => { reactionMap[r.type] = (reactionMap[r.type] || 0) + 1; });
          enriched.push({ ...m, replies, reactions, reactionMap, showReplies: false });
        }
        this.messages = enriched;
        this.loadingMessages = false;
      },
      error: () => { this.loadingMessages = false; }
    });
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.activeDiscussion) return;
    const check = this.moderation.check(this.newMessage);
    if (check.flagged) {
      this.moderationWarning = `Inappropriate language detected: "${check.detectedWords.join('", "')}". Please revise.`;
      return;
    }
    this.moderationWarning = '';
    this.api.sendMessage(this.activeDiscussion.id, this.CURRENT_USER, this.newMessage).subscribe(() => {
      this.newMessage = '';
      this.loadMessages(this.activeDiscussion!.id);
      this.toast.success('Message sent');
    });
  }

  appendToMessage(text: string): void {
    this.newMessage += (this.newMessage ? ' ' : '') + text;
  }

  openReply(msg: MessageVM): void { this.replyingTo = msg; this.replyText = ''; }

  submitReply(): void {
    if (!this.replyText.trim() || !this.replyingTo || !this.activeDiscussion) return;
    const check = this.moderation.check(this.replyText);
    if (check.flagged) {
      this.moderationWarning = `Inappropriate language detected: "${check.detectedWords.join('", "')}". Please revise.`;
      return;
    }
    this.moderationWarning = '';
    this.api.sendMessage(this.activeDiscussion.id, this.CURRENT_USER, this.replyText, this.replyingTo.id).subscribe(() => {
      this.replyingTo = null;
      this.loadMessages(this.activeDiscussion!.id);
    });
  }

  markSolved(msg: MessageVM): void {
    if (!this.activeDiscussion) return;
    if (!confirm('Mark this message as the accepted solution?')) return;
    this.api.markDiscussionSolved(this.activeDiscussion.id, msg.id, this.CURRENT_USER).subscribe({
      next: () => {
        this.activeDiscussion!.status = 'ARCHIVED';
        this.activeDiscussion!.solvedMessageId = msg.id;
        this.loadDiscussions();
        this.toast.success('Discussion marked as solved. Knowledge Base article created!');
      },
      error: (err: any) => this.toast.error(err.error?.message || 'Failed to mark as solved')
    });
  }

  deleteMessage(msg: MessageVM): void {
    if (!this.activeDiscussion) return;
    this.api.deleteMessage(this.activeDiscussion.id, msg.id, this.CURRENT_USER).subscribe(() => {
      this.loadMessages(this.activeDiscussion!.id);
    });
  }

  react(msg: MessageVM, type: ReactionType): void {
    this.api.reactToMessage(msg.id, this.CURRENT_USER, type).subscribe(() => {
      this.loadMessages(this.activeDiscussion!.id);
    });
  }

  openEditDiscussion(disc: Discussion, event: Event): void {
    event.stopPropagation();
    this.editingDiscussion = disc;
    this.editDiscussionTheme = disc.theme;
  }

  saveEditDiscussion(): void {
    if (!this.editDiscussionTheme.trim() || !this.editingDiscussion) return;
    this.api.updateDiscussion(this.editingDiscussion.id, this.editDiscussionTheme, this.CURRENT_USER).subscribe({
      next: () => {
        this.editingDiscussion = null;
        this.loadDiscussions();
        this.toast.success('Discussion updated');
      },
      error: (err: any) => this.toast.error(err.error?.message || 'Failed to update discussion')
    });
  }

  openEditMessage(msg: MessageVM): void {
    this.editingMessageId = msg.id;
    this.editingMessageText = msg.content;
  }

  saveEditMessage(): void {
    if (!this.editingMessageText.trim() || !this.activeDiscussion || this.editingMessageId === null) return;
    this.api.updateMessage(this.activeDiscussion.id, this.editingMessageId, this.editingMessageText, this.CURRENT_USER).subscribe({
      next: () => {
        this.editingMessageId = null;
        this.loadMessages(this.activeDiscussion!.id);
        this.toast.success('Message updated');
      },
      error: (err: any) => this.toast.error(err.error?.message || 'Failed to update message')
    });
  }

  createDiscussion(): void {
    if (!this.newTheme.trim()) return;
    this.api.createDiscussion(this.newTheme, this.CURRENT_USER).subscribe(() => {
      this.newTheme = '';
      this.showCreateForm = false;
      this.loadDiscussions();
      this.toast.success('Discussion created');
    });
  }

  deleteDiscussion(disc: Discussion, event: Event): void {
    event.stopPropagation();
    if (!confirm('Delete this discussion?')) return;
    this.api.deleteDiscussion(disc.id, this.CURRENT_USER).subscribe(() => {
      this.loadDiscussions();
      this.toast.success('Discussion deleted');
    });
  }

  generateSummary(): void {
    if (!this.activeDiscussion || this.messages.length === 0) return;
    this.aiSummaryLoading = true;
    this.showAiSummary = true;
    this.aiSummaryResult = null;
    const texts = this.messages.map(m => m.content);
    this.aiSummary.getDiscussionSummary(this.activeDiscussion.id, this.activeDiscussion.theme, texts).subscribe({
      next: (result: DiscussionSummary) => { this.aiSummaryResult = result; this.aiSummaryLoading = false; },
      error: () => { this.aiSummaryLoading = false; this.toast.error('AI summary failed. Try again.'); }
    });
  }

  closeAiSummary(): void { this.showAiSummary = false; this.aiSummaryResult = null; }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }
}
