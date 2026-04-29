import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { KnowledgeBaseArticle } from '../../core/models/blog.models';
import { AiSummaryService, WhyItWorksExplanation } from '../../core/services/ai-summary.service';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../shared/components/footer/footer.component';

@Component({
  selector: 'app-knowledge-base',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent, FooterComponent],
  templateUrl: './knowledge-base.component.html',
  styleUrls: ['./knowledge-base.component.scss']
})
export class KnowledgeBaseComponent implements OnInit {

  articles: KnowledgeBaseArticle[] = [];
  loading = true;
  searchQuery = '';
  selectedArticle: KnowledgeBaseArticle | null = null;
  activeTag = '';

  // Why This Works
  whyItWorks: WhyItWorksExplanation | null = null;
  whyLoading = false;
  showWhy = false;

  // Follow-up chat
  chatMessages: { role: string; text: string }[] = [];
  chatInput = '';
  chatLoading = false;

  constructor(private api: ApiService, private router: Router, private aiSummary: AiSummaryService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getKnowledgeBase().subscribe({
      next: a => { this.articles = a; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  onSearch(): void {
    if (!this.searchQuery.trim()) { this.load(); return; }
    this.loading = true;
    this.api.searchKnowledgeBase(this.searchQuery).subscribe({
      next: a => { this.articles = a; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  filterByTag(tag: string): void {
    this.activeTag = tag;
    this.searchQuery = tag;
    this.onSearch();
  }

  openArticle(article: KnowledgeBaseArticle): void {
    this.api.getKBArticle(article.id).subscribe(a => {
      this.selectedArticle = a;
      this.whyItWorks = null;
      this.showWhy = false;
      this.chatMessages = [];
      this.chatInput = '';
    });
  }

  closeArticle(): void { this.selectedArticle = null; this.whyItWorks = null; this.showWhy = false; this.chatMessages = []; }

  explainWhyItWorks(): void {
    if (!this.selectedArticle) return;
    this.whyLoading = true;
    this.showWhy = true;
    this.aiSummary.getWhyItWorks(
      this.selectedArticle.id,
      this.selectedArticle.question,
      this.selectedArticle.answer
    ).subscribe({
      next: result => { this.whyItWorks = result; this.whyLoading = false; },
      error: () => { this.whyLoading = false; }
    });
  }

  sendFollowUp(): void {
    if (!this.chatInput.trim() || !this.selectedArticle || this.chatLoading) return;
    const question = this.chatInput.trim();
    this.chatMessages.push({ role: 'user', text: question });
    this.chatInput = '';
    this.chatLoading = true;
    this.aiSummary.askFollowUp(
      question,
      this.selectedArticle.question,
      this.selectedArticle.answer,
      this.chatMessages
    ).subscribe({
      next: answer => { this.chatMessages.push({ role: 'ai', text: answer }); this.chatLoading = false; },
      error: () => { this.chatMessages.push({ role: 'ai', text: 'Something went wrong. Try again.' }); this.chatLoading = false; }
    });
  }

  get allTags(): string[] {
    const tags = this.articles.flatMap(a => (a.tags || '').split(',').map(t => t.trim()).filter(Boolean));
    return [...new Set(tags)];
  }

  navigateToDiscussion(): void {
    this.closeArticle();
    this.router.navigate(['/discussions']);
  }

  parseTags(tags: string): string[] {
    return (tags || '').split(',').map(t => t.trim()).filter(Boolean);
  }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }
}
