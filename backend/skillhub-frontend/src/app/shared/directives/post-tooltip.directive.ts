import {
  Directive, Input, HostListener, OnDestroy, ElementRef, Renderer2
} from '@angular/core';
import { Post } from '../../core/models/blog.models';
import { AiSummaryService } from '../../core/services/ai-summary.service';
import { Subscription } from 'rxjs';

@Directive({ selector: '[appPostTooltip]' })
export class PostTooltipDirective implements OnDestroy {

  @Input('appPostTooltip') post!: Post;

  private tooltipEl: HTMLElement | null = null;
  private showTimer: any;
  private hideTimer: any;
  private sub: Subscription | null = null;

  constructor(
    private el: ElementRef,
    private renderer: Renderer2,
    private aiSummary: AiSummaryService
  ) {}

  @HostListener('mouseenter') onEnter() {
    clearTimeout(this.hideTimer);
    this.showTimer = setTimeout(() => this.show(), 400);
  }

  @HostListener('mouseleave') onLeave() {
    clearTimeout(this.showTimer);
    this.hideTimer = setTimeout(() => this.hide(), 200);
  }

  private show(): void {
    this.hide();
    this.tooltipEl = this.renderer.createElement('div');
    this.tooltipEl!.className = 'post-summary-tooltip';
    this.render('⏳ Generating AI summary...');
    this.position();
    requestAnimationFrame(() => {
      if (this.tooltipEl) this.tooltipEl.style.opacity = '1';
    });

    // Fetch AI summary
    this.sub = this.aiSummary.getSummary(this.post.id, this.post.content).subscribe(summary => {
      if (this.tooltipEl) this.render(summary);
    });
  }

  private render(summary: string): void {
    if (!this.tooltipEl) return;
    const readTime = this.readingTime(this.post.content);
    const sentimentEmoji = this.sentimentEmoji();
    const urgencyBadge = this.urgencyBadge();
    const isLoading = summary.startsWith('⏳');

    this.tooltipEl.innerHTML = `
      <div class="pst-header">
        <span class="pst-author">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 12c2.7 0 4.8-2.1 4.8-4.8S14.7 2.4 12 2.4 7.2 4.5 7.2 7.2 9.3 12 12 12zm0 2.4c-3.2 0-9.6 1.6-9.6 4.8v2.4h19.2v-2.4c0-3.2-6.4-4.8-9.6-4.8z"/>
          </svg>
          ${this.post.author?.username || 'Unknown'}
        </span>
        <span class="pst-meta">${readTime} · ${sentimentEmoji}</span>
        ${urgencyBadge}
      </div>
      <div class="pst-divider"></div>
      <p class="pst-label">🤖 AI Summary</p>
      <p class="pst-summary ${isLoading ? 'loading' : ''}">${summary}</p>
      <div class="pst-footer">
        <span class="pst-status ${this.post.status}">${this.post.status}</span>
        <span class="pst-visibility">${this.visibilityIcon(this.post.visibility)} ${this.post.visibility}</span>
      </div>
    `;
  }

  private hide(): void {
    this.sub?.unsubscribe();
    if (this.tooltipEl) {
      this.renderer.removeChild(document.body, this.tooltipEl);
      this.tooltipEl = null;
    }
  }

  private position(): void {
    if (!this.tooltipEl) return;
    const rect = this.el.nativeElement.getBoundingClientRect();
    const tip = this.tooltipEl;
    const tipWidth = 320;

    tip.style.position = 'fixed';
    tip.style.zIndex = '9999';
    tip.style.opacity = '0';
    tip.style.transition = 'opacity 0.2s ease';
    tip.style.width = `${tipWidth}px`;
    tip.style.transform = 'translateY(-100%)';

    let left = rect.left + rect.width / 2 - tipWidth / 2;
    if (left < 8) left = 8;
    if (left + tipWidth > window.innerWidth - 8) left = window.innerWidth - tipWidth - 8;

    tip.style.left = `${left}px`;
    tip.style.top = `${rect.top - 10}px`;

    this.renderer.appendChild(document.body, tip);
  }

  private readingTime(content: string): string {
    const words = (content || '').split(/\s+/).length;
    return `${Math.max(1, Math.round(words / 200))} min read`;
  }

  private sentimentEmoji(): string {
    const s = (this.post as any).sentiment;
    if (s === 'POSITIVE') return '😊';
    if (s === 'NEGATIVE') return '😡';
    return '😐';
  }

  private urgencyBadge(): string {
    if (this.post.urgencyLevel === 'CRITICAL') return `<span class="pst-urgency critical">🚨 CRITICAL</span>`;
    if (this.post.urgencyLevel === 'HIGH') return `<span class="pst-urgency high">⚠️ HIGH</span>`;
    return '';
  }

  private visibilityIcon(v: string): string {
    const icons: Record<string, string> = { PUBLIC: '🌍', PRIVATE: '🔒', FRIENDS_ONLY: '👥', RESTRICTED: '🔐' };
    return icons[v] || '🌍';
  }

  ngOnDestroy(): void {
    clearTimeout(this.showTimer);
    clearTimeout(this.hideTimer);
    this.hide();
  }
}
