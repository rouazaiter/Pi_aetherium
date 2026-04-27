import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';
import type { PortfolioMentorChatResponse, PortfolioMentorReplyMode } from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { PortfolioMentorService } from '../../core/services/portfolio-mentor.service';
import { messageFromHttpError } from '../../core/util/http-error';

type MentorSectionState<T> = {
  loading: boolean;
  error: string;
  data: T | null;
};

type MentorSummaryCard = {
  title: string;
  value: string;
  numericScore: number | null;
  badge: string;
  explanation: string;
  progress: number | null;
};

type MentorInsightItem = {
  title: string;
  category: string;
  description: string;
  score: number;
  priority: string;
};

type MentorMoveItem = {
  priority: number;
  title: string;
  category: string;
  description: string;
  impact: string;
};

type MentorMiniMetric = {
  title: string;
  score: number;
  badge: string;
  progress: number;
};

type MentorCoherenceView = {
  score: number;
  status: string;
  explanation: string;
  recommendations: string[];
  metrics: MentorMiniMetric[];
};

type MentorChatMessage = {
  role: 'user' | 'assistant';
  content: string;
  notes?: string[];
  rewrites?: string[];
};

type MentorQuickPrompt = {
  label: string;
  message: string;
  target: string;
  replyMode: PortfolioMentorReplyMode;
};

type MentorSidebarItem = {
  label: string;
  route: string;
  icon: 'home' | 'portfolio' | 'projects' | 'cv' | 'mentor' | 'skills' | 'analytics' | 'settings';
  exact?: boolean;
};

@Component({
  selector: 'app-portfolio-mentor',
  standalone: true,
  imports: [FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './portfolio-mentor.component.html',
  styleUrl: './portfolio-mentor.component.scss',
})
export class PortfolioMentorComponent implements OnInit, OnDestroy {
  private readonly mentorApi = inject(PortfolioMentorService);
  private readonly auth = inject(AuthService);
  private summaryResponse: Record<string, unknown> | null = null;
  private coherenceResponse: Record<string, unknown> | null = null;
  private chatRequestSub: Subscription | null = null;
  private chatTimeoutHandle: ReturnType<typeof setTimeout> | null = null;

  protected readonly quickPrompts: MentorQuickPrompt[] = [
    { label: 'Improve my portfolio', message: 'Improve my portfolio', target: 'general', replyMode: 'ADVICE' },
    { label: 'What’s missing?', message: 'What is missing from my portfolio?', target: 'general', replyMode: 'ADVICE' },
    { label: 'Rewrite my bio', message: 'Rewrite my bio to make it stronger', target: 'bio', replyMode: 'REWRITE' },
    {
      label: 'Check coherence',
      message: 'Do my title, bio, and projects tell the same story?',
      target: 'general',
      replyMode: 'EXPLAIN',
    },
  ];

  protected readonly sidebarItems: MentorSidebarItem[] = [
    { label: 'Dashboard', route: '/', icon: 'home', exact: true },
    { label: 'My Portfolio', route: '/jihen-portfolio', icon: 'portfolio' },
    { label: 'Projects', route: '/jihen-portfolio', icon: 'projects' },
    { label: 'CV Builder', route: '/cv', icon: 'cv' },
    { label: 'Portfolio Mentor', route: '/portfolio-mentor', icon: 'mentor' },
    { label: 'Skills', route: '/jihen-portfolio', icon: 'skills' },
    { label: 'Analytics', route: '/portfolio-mentor', icon: 'analytics' },
    { label: 'Settings', route: '/profile', icon: 'settings' },
  ];

  protected readonly summaryState: MentorSectionState<MentorSummaryCard[]> = { loading: true, error: '', data: null };
  protected readonly strengthsState: MentorSectionState<{ strengths: MentorInsightItem[]; gaps: MentorInsightItem[] }> =
    { loading: true, error: '', data: null };
  protected readonly movesState: MentorSectionState<{
    core: MentorMoveItem[];
    adjacent: MentorMoveItem[];
    expansion: MentorMoveItem[];
  }> = { loading: true, error: '', data: null };
  protected readonly coherenceState: MentorSectionState<MentorCoherenceView> = { loading: true, error: '', data: null };

  protected chatMessages: MentorChatMessage[] = [];
  protected chatInput = '';
  protected chatSending = false;
  protected chatError = '';
  protected chatTimeoutMessage = '';
  protected pendingChatMessage = '';
  protected pendingTarget = 'general';
  protected pendingReplyMode: PortfolioMentorReplyMode = 'ADVICE';

  ngOnInit(): void {
    this.loadSummary();
    this.loadStrengthsGaps();
    this.loadNextBestMoves();
    this.loadCoherence();
  }

  ngOnDestroy(): void {
    this.clearChatRequestState(false);
  }

  protected userName(): string {
    return this.auth.auth()?.username?.trim() || '';
  }

  protected greeting(): string {
    const userName = this.userName();
    return userName ? `Hi ${userName} 👋` : 'Hi there 👋';
  }

  protected assistantWelcomeMessage(): string {
    const dominantFamily = this.readString(this.summaryResponse ?? {}, ['dominantFamily']);
    if (dominantFamily) {
      return `I reviewed your portfolio. You have a strong ${dominantFamily.toLowerCase()} foundation. What would you like to improve first?`;
    }
    return 'I reviewed your portfolio. Ask me anything or pick a quick action to improve your portfolio.';
  }

  protected topStrengths(): MentorInsightItem[] {
    return (this.strengthsState.data?.strengths ?? []).slice(0, 3);
  }

  protected topGaps(): MentorInsightItem[] {
    return (this.strengthsState.data?.gaps ?? []).slice(0, 3);
  }

  protected allMoves(): MentorMoveItem[] {
    const moves = this.movesState.data;
    if (!moves) {
      return [];
    }

    return [...moves.core, ...moves.adjacent, ...moves.expansion].sort((left, right) => left.priority - right.priority);
  }

  protected topMoves(): MentorMoveItem[] {
    return this.allMoves().slice(0, 4);
  }

  protected insightSummary(): { intro: string; gaps: string; nextStep: string } | null {
    const dominantFamily = this.readString(this.summaryResponse ?? {}, ['dominantFamily']);
    const strongestSignals = this.readStringList(this.summaryResponse ?? {}, ['strongestSignals']);
    const topGaps = this.topGaps().map((item) => item.title);
    const nextMove = this.topMoves()[0]?.title ?? '';

    if (!dominantFamily && strongestSignals.length === 0 && topGaps.length === 0 && !nextMove) {
      return null;
    }

    const introParts = [];
    if (dominantFamily) {
      introParts.push(`You have a strong ${dominantFamily.toLowerCase()} foundation`);
    }
    if (strongestSignals.length > 0) {
      introParts.push(strongestSignals[0].toLowerCase());
    }

    return {
      intro: introParts.length > 0 ? `${introParts.join(' and ')}.` : '',
      gaps: topGaps.length > 0 ? `The main gaps are ${topGaps.join(', ')}.` : '',
      nextStep: nextMove ? `Next step: ${nextMove}.` : '',
    };
  }

  protected strengthCount(): number {
    return this.strengthsState.data?.strengths.length ?? 0;
  }

  protected gapCount(): number {
    return this.strengthsState.data?.gaps.length ?? 0;
  }

  protected moveCount(): number {
    return this.allMoves().length;
  }

  protected scoreTone(score: number): 'strong' | 'medium' | 'critical' {
    if (score >= 75) {
      return 'strong';
    }
    if (score >= 50) {
      return 'medium';
    }
    return 'critical';
  }

  protected insightItemIcon(item: MentorInsightItem, positive: boolean): string {
    const label = `${item.category} ${item.description} ${item.title}`.toLowerCase();
    if (label.includes('test') || label.includes('quality')) {
      return positive ? 'check_circle' : 'warning';
    }
    if (label.includes('deploy') || label.includes('ci/cd')) {
      return positive ? 'rocket_launch' : 'pending';
    }
    if (label.includes('doc')) {
      return positive ? 'description' : 'edit_note';
    }
    if (label.includes('database')) {
      return 'database';
    }
    if (label.includes('project')) {
      return 'inventory_2';
    }
    return positive ? 'verified' : 'error';
  }

  protected loadSummary(): void {
    this.summaryState.loading = true;
    this.summaryState.error = '';
    this.mentorApi.getProfileStrength().subscribe({
      next: (response) => {
        this.summaryState.loading = false;
        this.summaryResponse = this.asRecord(response);
        this.summaryState.data = this.normalizeSummary(this.summaryResponse, this.coherenceResponse);
      },
      error: (err) => {
        this.summaryState.loading = false;
        this.summaryState.error = messageFromHttpError(err, 'Unable to load the summary insights.');
      },
    });
  }

  protected loadStrengthsGaps(): void {
    this.strengthsState.loading = true;
    this.strengthsState.error = '';
    this.mentorApi.getStrengthsGaps().subscribe({
      next: (response) => {
        this.strengthsState.loading = false;
        this.strengthsState.data = this.normalizeStrengthsGaps(response);
      },
      error: (err) => {
        this.strengthsState.loading = false;
        this.strengthsState.error = messageFromHttpError(err, 'Unable to load strengths and gaps.');
      },
    });
  }

  protected loadNextBestMoves(): void {
    this.movesState.loading = true;
    this.movesState.error = '';
    this.mentorApi.getNextBestMoves().subscribe({
      next: (response) => {
        this.movesState.loading = false;
        this.movesState.data = this.normalizeMoves(response);
      },
      error: (err) => {
        this.movesState.loading = false;
        this.movesState.error = messageFromHttpError(err, 'Unable to load your next best moves.');
      },
    });
  }

  protected loadCoherence(): void {
    this.coherenceState.loading = true;
    this.coherenceState.error = '';
    this.mentorApi.getCoherence().subscribe({
      next: (response) => {
        this.coherenceState.loading = false;
        this.coherenceResponse = this.asRecord(response);
        this.coherenceState.data = this.normalizeCoherence(this.coherenceResponse);
        if (this.summaryResponse) {
          this.summaryState.data = this.normalizeSummary(this.summaryResponse, this.coherenceResponse);
        }
      },
      error: (err) => {
        this.coherenceState.loading = false;
        this.coherenceState.error = messageFromHttpError(err, 'Unable to load profile coherence.');
      },
    });
  }

  protected sendQuickPrompt(prompt: MentorQuickPrompt): void {
    this.chatInput = prompt.message;
    this.submitChat(prompt.target, prompt.replyMode);
  }

  protected cancelChat(): void {
    this.clearChatRequestState(true);
    this.chatError = 'AI request canceled. You can edit your message and try again.';
  }

  protected retryChat(): void {
    if (!this.pendingChatMessage.trim()) {
      return;
    }

    this.submitChat(this.pendingTarget, this.pendingReplyMode);
  }

  protected submitChat(target = 'general', replyMode: PortfolioMentorReplyMode = 'ADVICE'): void {
    const message = this.chatInput.trim() || this.pendingChatMessage.trim();
    if (!message || this.chatSending) {
      return;
    }

    this.chatError = '';
    this.chatTimeoutMessage = '';
    this.pendingChatMessage = message;
    this.pendingTarget = target;
    this.pendingReplyMode = replyMode;
    this.chatSending = true;
    this.startChatTimeout('This is taking longer than usual. You can wait or try again.');

    this.chatRequestSub?.unsubscribe();
    this.chatRequestSub = this.mentorApi.chat({ message, target, replyMode }).subscribe({
      next: (response) => {
        const assistantMessage = this.normalizeChatMessage(response);
        const nextMessages: MentorChatMessage[] = [...this.chatMessages, { role: 'user', content: message }];
        this.chatMessages = assistantMessage ? [...nextMessages, assistantMessage] : nextMessages;
        this.chatInput = '';
        this.clearChatRequestState(false);
      },
      error: (err) => {
        this.chatSending = false;
        this.clearChatTimeout();
        this.chatError = messageFromHttpError(err, 'Unable to reach Portfolio Mentor right now.');
      },
    });
  }

  protected trackByTitle(_: number, item: { title: string }): string {
    return item.title;
  }

  protected trackByLabel(_: number, item: { label: string }): string {
    return item.label;
  }

  private normalizeSummary(summary: Record<string, unknown>, coherence: Record<string, unknown> | null): MentorSummaryCard[] {
    const profileStrengthScore = this.readNumber(summary, ['profileStrengthScore']);
    const dominantFamily = this.readString(summary, ['dominantFamily']);
    const dnaType = this.readString(summary, ['dnaType']);
    const maturityLevel = this.readString(summary, ['maturityLevel']);
    const marketReadiness = this.readString(summary, ['marketReadiness']);
    const strategicPriorities = this.readStringList(summary, ['strategicPriorities']);

    const cards: MentorSummaryCard[] = [];

    if (profileStrengthScore !== null || maturityLevel || marketReadiness) {
      const score = profileStrengthScore === null ? null : this.clampScore(profileStrengthScore);
      cards.push({
        title: 'Profile Strength',
        value: score === null ? '' : String(score),
        numericScore: score,
        badge: maturityLevel,
        explanation: marketReadiness,
        progress: score,
      });
    }

    if (coherence) {
      const totalScore = this.readNumber(coherence, ['totalScore']);
      const mismatchDetected = this.readBoolean(coherence, ['mismatchDetected']);
      const status = this.readString(coherence, ['status']);
      if (totalScore !== null || status) {
        const score = totalScore === null ? null : this.clampScore(totalScore);
        cards.push({
          title: 'Coherence Score',
          value: score === null ? '' : String(score),
          numericScore: score,
          badge: status,
          explanation: mismatchDetected
            ? 'Your portfolio has some inconsistencies between title, bio, skills, and projects.'
            : 'Your portfolio tells a clear and consistent professional story.',
          progress: score,
        });
      }
    }

    if (dominantFamily || dnaType || strategicPriorities.length > 0 || marketReadiness) {
      cards.push({
        title: 'Dominant Direction',
        value: dominantFamily,
        numericScore: null,
        badge: dnaType,
        explanation: strategicPriorities[0] || marketReadiness,
        progress: null,
      });
    }

    return cards;
  }

  private normalizeStrengthsGaps(response: unknown): { strengths: MentorInsightItem[]; gaps: MentorInsightItem[] } {
    const root = this.asRecord(response);
    return {
      strengths: this.normalizeInsightItems(root['strengths'] ?? root['currentStrengths'] ?? root['current_strengths'], true),
      gaps: this.normalizeInsightItems(root['gaps'] ?? root['importantGaps'] ?? root['important_gaps'], false),
    };
  }

  private normalizeInsightItems(value: unknown, positive: boolean): MentorInsightItem[] {
    if (!Array.isArray(value)) {
      return [];
    }

    return value
      .map((entry) => {
      const item = this.asRecord(entry);
      const title = this.readString(item, ['label']);
      if (!title) {
        return null;
      }

      const rawScore = this.readNumber(item, ['score']);
      const score = rawScore === null ? 0 : this.clampScore(rawScore);
      return {
        title,
        category: this.readString(item, ['category']),
        description: this.readString(item, ['type', 'category']),
        score,
        priority: this.readString(item, ['priority']),
      };
      })
      .filter((item): item is MentorInsightItem => Boolean(item));
  }

  private normalizeMoves(response: unknown): {
    core: MentorMoveItem[];
    adjacent: MentorMoveItem[];
    expansion: MentorMoveItem[];
  } {
    const root = this.asRecord(response);
    return {
      core: this.normalizeMoveItems(root['coreMoves'] ?? root['core_moves'] ?? root['core']),
      adjacent: this.normalizeMoveItems(root['adjacentMoves'] ?? root['adjacent_moves'] ?? root['adjacent']),
      expansion: this.normalizeMoveItems(root['expansionMoves'] ?? root['expansion_moves'] ?? root['expansion']),
    };
  }

  private normalizeMoveItems(value: unknown): MentorMoveItem[] {
    if (!Array.isArray(value)) {
      return [];
    }

    return value
      .map((entry) => {
      const item = this.asRecord(entry);
      const title = this.readString(item, ['label']);
      const priority = this.readNumber(item, ['priority']);
      if (!title || priority === null) {
        return null;
      }

      return {
        priority,
        title,
        category: this.readString(item, ['category']),
        description: this.readString(item, ['reason']),
        impact: this.readString(item, ['type']),
      };
      })
      .filter((item): item is MentorMoveItem => Boolean(item));
  }

  private normalizeCoherence(root: Record<string, unknown>): MentorCoherenceView {
    const totalScore = this.readNumber(root, ['totalScore']);
    const mismatchDetected = this.readBoolean(root, ['mismatchDetected']);
    const recommendations = this.readStringList(root, ['recommendations']);
    const score = totalScore === null ? 0 : this.clampScore(totalScore);
    return {
      score,
      status: this.readString(root, ['status']),
      explanation: mismatchDetected
        ? 'Your portfolio has some inconsistencies between title, bio, skills, and projects.'
        : 'Your portfolio tells a clear and consistent professional story.',
      recommendations,
      metrics: [
        this.normalizeMiniMetric(this.readNumber(root, ['skillAlignmentScore']), 'Skill Consistency'),
        this.normalizeMiniMetric(this.readNumber(root, ['titleAlignmentScore']), 'Title Alignment'),
        this.normalizeMiniMetric(this.readNumber(root, ['bioAlignmentScore']), 'Bio Alignment'),
        this.normalizeMiniMetric(this.readNumber(root, ['projectProofScore']), 'Project Proof'),
      ],
    };
  }

  private normalizeMiniMetric(scoreValue: number | null, title: string): MentorMiniMetric {
    const score = scoreValue === null ? 0 : this.clampScore(scoreValue);
    return {
      title,
      score,
      badge: this.scoreToStatus(score),
      progress: score,
    };
  }

  private normalizeChatMessage(response: PortfolioMentorChatResponse | null | undefined): MentorChatMessage | null {
    const content = response?.mainMessage?.trim() || response?.rawResponse?.trim() || '';
    const notes = (response?.notes ?? []).map((note) => note.trim()).filter(Boolean);
    const rewrites = (response?.rewrites ?? []).map((rewrite) => rewrite.trim()).filter(Boolean);

    if (!content && notes.length === 0 && rewrites.length === 0) {
      return null;
    }

    return {
      role: 'assistant',
      content,
      notes: notes.length > 0 ? notes : undefined,
      rewrites: rewrites.length > 0 ? rewrites : undefined,
    };
  }

  private scoreToStatus(score: number): string {
    if (score >= 80) {
      return 'Strong';
    }
    if (score >= 60) {
      return 'Promising';
    }
    if (score >= 40) {
      return 'Needs Work';
    }
    return 'Critical';
  }

  private clampScore(value: number): number {
    return Math.max(0, Math.min(100, Math.round(value)));
  }

  private asRecord(value: unknown): Record<string, unknown> {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
      return {};
    }
    return value as Record<string, unknown>;
  }

  private readBoolean(source: Record<string, unknown>, keys: string[]): boolean {
    for (const key of keys) {
      if (typeof source[key] === 'boolean') {
        return source[key] as boolean;
      }
    }
    return false;
  }

  private readString(source: Record<string, unknown>, keys: string[]): string {
    for (const key of keys) {
      const value = source[key];
      if (typeof value === 'string' && value.trim()) {
        return value.trim();
      }
    }
    return '';
  }

  private readNumber(source: Record<string, unknown>, keys: string[]): number | null {
    for (const key of keys) {
      const value = source[key];
      if (typeof value === 'number' && Number.isFinite(value)) {
        return value;
      }
      if (typeof value === 'string' && value.trim()) {
        const parsed = Number(value);
        if (Number.isFinite(parsed)) {
          return parsed;
        }
      }
    }
    return null;
  }

  private readStringList(source: Record<string, unknown>, keys: string[]): string[] {
    for (const key of keys) {
      const value = source[key];
      if (!Array.isArray(value)) {
        continue;
      }

      return value
        .map((entry) => (typeof entry === 'string' ? entry.trim() : ''))
        .filter(Boolean);
    }
    return [];
  }

  private startChatTimeout(message: string): void {
    this.clearChatTimeout();
    this.chatTimeoutHandle = setTimeout(() => {
      if (this.chatSending) {
        this.chatTimeoutMessage = message;
      }
    }, 30000);
  }

  private clearChatTimeout(): void {
    if (this.chatTimeoutHandle) {
      clearTimeout(this.chatTimeoutHandle);
      this.chatTimeoutHandle = null;
    }
  }

  private clearChatRequestState(keepPendingMessage: boolean): void {
    this.chatRequestSub?.unsubscribe();
    this.chatRequestSub = null;
    this.clearChatTimeout();
    this.chatSending = false;
    this.chatTimeoutMessage = '';
    if (!keepPendingMessage) {
      this.pendingChatMessage = '';
    }
  }
}
