import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { ApiService } from '../../core/services/api.service';
import { Post, Comment, PostStatus, PostVisibility } from '../../core/models/blog.models';
import { ModerationService } from '../../core/services/moderation.service';
import { UrgencyService } from '../../core/services/urgency.service';
import { ToastService } from '../../core/services/toast.service';
import { AiSummaryService, FailureExplanation } from '../../core/services/ai-summary.service';
import { BookmarkService } from '../../core/services/bookmark.service';
import { NavbarComponent } from '../../shared/components/navbar/navbar.component';
import { FooterComponent } from '../../shared/components/footer/footer.component';
import { MicButtonComponent } from '../../shared/components/mic-button/mic-button.component';
import { PostTooltipDirective } from '../../shared/directives/post-tooltip.directive';

@Component({
  selector: 'app-blog',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, NavbarComponent, FooterComponent, MicButtonComponent, PostTooltipDirective],
  templateUrl: './blog.component.html',
  styleUrls: ['./blog.component.scss']
})
export class BlogComponent implements OnInit {

  readonly CURRENT_USER = 1;

  @ViewChild('modalBox') modalBox!: ElementRef<HTMLDivElement>;

  // Reading progress (0–100)
  readingProgress = 0;

  onModalScroll(event: Event): void {
    const el = event.target as HTMLDivElement;
    const scrolled = el.scrollTop;
    const total = el.scrollHeight - el.clientHeight;
    this.readingProgress = total > 0 ? Math.round((scrolled / total) * 100) : 0;
  }

  posts: Post[] = [];
  loading = true;

  // Search & filter
  searchQuery = '';
  filter: PostStatus | 'ALL' = 'ALL';

  // Pagination
  currentPage = 1;
  readonly pageSize = 6;

  // Detail modal
  selectedPost: Post | null = null;
  comments: Comment[] = [];
  likeCount = 0;
  liked = false;
  newComment = '';
  loadingComments = false;

  // Lightbox
  lightboxOpen = false;
  lightboxSrc = '';

  openLightbox(src: string): void { this.lightboxSrc = src; this.lightboxOpen = true; }
  closeLightbox(): void { this.lightboxOpen = false; }

  // Change cover image from detail modal
  onDetailImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length || !this.selectedPost) return;
    const file = input.files[0];
    if (file.size > 2 * 1024 * 1024) { this.toast.error('Image must be smaller than 2MB'); input.value = ''; return; }
    const reader = new FileReader();
    reader.onload = () => {
      const base64 = reader.result as string;
      this.api.updatePost(this.selectedPost!.id, { ...this.selectedPost!, coverImage: base64 }, this.CURRENT_USER)
        .subscribe(updated => {
          this.selectedPost = updated;
          this.loadPosts();
        });
    };
    reader.readAsDataURL(file);
  }

  removeDetailCoverImage(): void {
    if (!this.selectedPost) return;
    this.api.updatePost(this.selectedPost.id, { ...this.selectedPost, coverImage: '' }, this.CURRENT_USER)
      .subscribe(updated => { this.selectedPost = updated; this.loadPosts(); });
  }

  // Comment sentiment filter & sort
  sentimentFilter: 'ALL' | 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL' = 'ALL';
  commentSort: 'DATE' | 'BEST' = 'DATE';

  // Comment editing
  editingCommentId: number | null = null;
  editingCommentText = '';

  get filteredComments(): Comment[] {
    let result = this.sentimentFilter === 'ALL'
      ? [...this.comments]
      : this.comments.filter(c => c.sentiment === this.sentimentFilter);
    if (this.commentSort === 'BEST') {
      const order: Record<string, number> = { POSITIVE: 0, NEUTRAL: 1, NEGATIVE: 2 };
      result.sort((a, b) => (order[a.sentiment ?? 'NEUTRAL'] ?? 1) - (order[b.sentiment ?? 'NEUTRAL'] ?? 1));
    }
    return result;
  }

  get topComment(): Comment | null {
    const positives = this.comments.filter(c => c.sentiment === 'POSITIVE');
    return positives.length > 0 ? positives[0] : null;
  }

  sentimentEmoji(s: string): string {
    if (s === 'POSITIVE') return '😊';
    if (s === 'NEGATIVE') return '😡';
    return '😐';
  }

  sentimentLabel(s: string): string {
    if (s === 'POSITIVE') return 'Positive';
    if (s === 'NEGATIVE') return 'Negative';
    return 'Neutral';
  }

  get positiveCount(): number { return this.comments.filter(c => c.sentiment === 'POSITIVE').length; }
  get negativeCount(): number { return this.comments.filter(c => c.sentiment === 'NEGATIVE').length; }
  get neutralCount(): number { return this.comments.filter(c => c.sentiment === 'NEUTRAL').length; }

  // Create/Edit modal
  showPostForm = false;
  editingPost: Post | null = null;
  coverImagePreview: string | null = null;
  postForm = {
    title: '', slug: '', content: '',
    status: 'PUBLISHED' as PostStatus,
    visibility: 'PUBLIC' as PostVisibility,
    coverImage: '' as string | undefined
  };

  moderationWarning = '';

  // Report
  showReportModal = false;
  reportTarget: { type: 'POST' | 'COMMENT'; id: number; label: string } | null = null;
  reportReason = 'SPAM';
  reportDetails = '';
  reportSubmitted = false;
  readonly reportReasons = [
    { value: 'SPAM', label: '🚫 Spam' },
    { value: 'INAPPROPRIATE', label: '🔞 Inappropriate content' },
    { value: 'MISINFORMATION', label: '❌ Misinformation' },
    { value: 'HARASSMENT', label: '😡 Harassment' },
    { value: 'COPYRIGHT', label: '©️ Copyright violation' },
    { value: 'OTHER', label: '📝 Other' }
  ];

  openReport(type: 'POST' | 'COMMENT', id: number, label: string, event: Event): void {
    event.stopPropagation();
    this.reportTarget = { type, id, label };
    this.reportReason = 'SPAM';
    this.reportDetails = '';
    this.reportSubmitted = false;
    this.showReportModal = true;
  }

  submitReport(): void {
    if (!this.reportTarget) return;
    this.api.submitReport(
      this.CURRENT_USER, this.reportTarget.type,
      this.reportTarget.id, this.reportReason, this.reportDetails
    ).subscribe({
      next: () => {
        this.reportSubmitted = true;
        this.toast.success('Report submitted. Our team will review it shortly.');
        setTimeout(() => this.showReportModal = false, 2000);
      },
      error: (err: any) => this.toast.error(err.error?.message || 'Failed to submit report')
    });
  }

  // Live session modal
  showLiveSession = false;
  liveSessionPost: Post | null = null;

  constructor(private api: ApiService, private moderation: ModerationService, private urgencyService: UrgencyService, private toast: ToastService, private aiSummary: AiSummaryService, public bookmarks: BookmarkService) {}

  ngOnInit(): void { this.loadPosts(); }

  loadPosts(): void {
    this.loading = true;
    this.api.getPosts(this.CURRENT_USER).subscribe({
      next: posts => { this.posts = posts; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  // ── Search + filter + pagination ─────────────────────────────────────────

  get filteredPosts(): Post[] {
    let result = this.filter === 'ALL' ? this.posts : this.posts.filter(p => p.status === this.filter);
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(p =>
        p.title.toLowerCase().includes(q) ||
        p.content.toLowerCase().includes(q) ||
        (p.author?.username?.toLowerCase().includes(q) ?? false)
      );
    }
    return result;
  }

  get totalPages(): number { return Math.ceil(this.filteredPosts.length / this.pageSize) || 1; }

  get paginatedPosts(): Post[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPosts.slice(start, start + this.pageSize);
  }

  get pages(): number[] { return Array.from({ length: this.totalPages }, (_, i) => i + 1); }

  onSearch(): void { this.currentPage = 1; }
  onFilterChange(f: PostStatus | 'ALL'): void { this.filter = f; this.currentPage = 1; }
  goToPage(p: number): void { if (p >= 1 && p <= this.totalPages) this.currentPage = p; }

  // ── Post detail ───────────────────────────────────────────────────────────

  openPost(post: Post): void {
    this.selectedPost = post;
    this.liked = false;
    this.loadComments(post.id);
    this.api.getPostLikeCount(post.id).subscribe(r => this.likeCount = r.count);
  }

  closePost(): void { this.selectedPost = null; this.readingProgress = 0; }

  loadComments(postId: number): void {
    this.loadingComments = true;
    this.api.getComments(postId).subscribe({
      next: c => { this.comments = c; this.loadingComments = false; },
      error: () => { this.loadingComments = false; }
    });
  }

  toggleLike(): void {
    if (!this.selectedPost) return;
    const action = this.liked
      ? this.api.unlikePost(this.selectedPost.id, this.CURRENT_USER)
      : this.api.likePost(this.selectedPost.id, this.CURRENT_USER);
    action.subscribe(() => {
      this.liked = !this.liked;
      this.likeCount += this.liked ? 1 : -1;
    });
  }

  // ── Comments ──────────────────────────────────────────────────────────────

  // Moderation violation tracking (client-side counter mirrors backend)
  private violationCount = 0;

  private handleModerationViolation(detectedWords: string[], context: string): void {
    this.violationCount++;
    if (this.violationCount >= 5) {
      this.moderationWarning = `🚨 Final warning: You have repeatedly used inappropriate language. A warning email has been sent to your registered email address.`;
      this.toast.error('Warning email sent to your account.');
    } else if (this.violationCount >= 3) {
      this.moderationWarning = `⚠️ ${context} contains inappropriate language: "${detectedWords.join('", "')}". Warning ${this.violationCount}/5 — at 5 violations a warning email will be sent.`;
    } else {
      this.moderationWarning = `⚠️ Inappropriate language detected: "${detectedWords.join('", "')}". Please revise. (${this.violationCount}/5 warnings)`;
    }
  }

  submitComment(): void {
    if (!this.newComment.trim() || !this.selectedPost) return;
    // Always send to backend — it handles moderation, violation tracking, and email
    this.api.addComment(this.selectedPost.id, this.CURRENT_USER, this.newComment).subscribe({
      next: () => {
        this.newComment = '';
        this.moderationWarning = '';
        this.loadComments(this.selectedPost!.id);
        this.toast.success('Comment posted');
      },
      error: (err: any) => {
        const detectedWords: string[] = err.error?.detectedWords ?? [];
        this.handleModerationViolation(detectedWords, 'Your comment');
      }
    });
  }

  startEditComment(c: Comment): void { this.editingCommentId = c.id; this.editingCommentText = c.content; }
  cancelEditComment(): void { this.editingCommentId = null; this.editingCommentText = ''; }

  saveEditComment(commentId: number): void {
    if (!this.editingCommentText.trim()) return;
    this.api.updateComment(commentId, this.editingCommentText, this.CURRENT_USER).subscribe({
      next: () => {
        this.editingCommentId = null;
        this.moderationWarning = '';
        this.loadComments(this.selectedPost!.id);
      },
      error: (err: any) => {
        const detectedWords: string[] = err.error?.detectedWords ?? [];
        this.handleModerationViolation(detectedWords, 'Your comment');
      }
    });
  }

  deleteComment(commentId: number): void {
    if (!this.selectedPost) return;
    this.api.deleteComment(commentId, this.CURRENT_USER).subscribe(() => {
      this.loadComments(this.selectedPost!.id);
    });
  }

  // ── Create / Edit post ────────────────────────────────────────────────────

  openCreatePost(): void {
    this.editingPost = null;
    this.coverImagePreview = null;
    this.postForm = { title: '', slug: '', content: '', status: 'PUBLISHED', visibility: 'PUBLIC', coverImage: '' };
    this.showPostForm = true;
  }

  openEditPost(post: Post, event: Event): void {
    event.stopPropagation();
    this.editingPost = post;
    this.coverImagePreview = post.coverImage || null;
    this.postForm = {
      title: post.title, slug: post.slug, content: post.content,
      status: post.status, visibility: post.visibility,
      coverImage: post.coverImage || ''
    };
    this.showPostForm = true;
  }

  onImageSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    const file = input.files[0];
    if (file.size > 2 * 1024 * 1024) { this.toast.error('Image must be smaller than 2MB'); input.value = ''; return; }
    const reader = new FileReader();
    reader.onload = () => {
      this.coverImagePreview = reader.result as string;
      this.postForm.coverImage = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  removeCoverImage(): void { this.coverImagePreview = null; this.postForm.coverImage = ''; }

  submitPost(): void {
    if (!this.postForm.title || !this.postForm.slug || !this.postForm.content) return;
    const check = this.moderation.check(`${this.postForm.title} ${this.postForm.content}`);
    if (check.flagged) {
      this.moderationWarning = `⚠️ Inappropriate content detected: "${check.detectedWords.join('", "')}". Please revise your post.`;
      return;
    }
    this.moderationWarning = '';
    const action = this.editingPost
      ? this.api.updatePost(this.editingPost.id, this.postForm, this.CURRENT_USER)
      : this.api.createPost(this.postForm, this.CURRENT_USER);
    action.subscribe(() => { this.showPostForm = false; this.loadPosts(); this.toast.success(this.editingPost ? 'Post updated successfully' : 'Post published successfully'); });
  }

  deletePost(post: Post, event: Event): void {
    event.stopPropagation();
    if (!confirm('Delete this post?')) return;
    this.api.deletePost(post.id, this.CURRENT_USER).subscribe(() => {
      this.loadPosts();
      this.toast.success('Post deleted');
    });
  }

  // Speech-to-text
  speechLang = 'en-US';
  readonly speechLangs = [
    { code: 'en-US', label: '🇺🇸 English' },
    { code: 'fr-FR', label: '🇫🇷 French' },
    { code: 'ar-SA', label: '🇸🇦 Arabic' }
  ];

  appendToTitle(text: string): void {
    this.postForm.title += (this.postForm.title ? ' ' : '') + text;
    this.autoSlug();
  }

  appendToContent(text: string): void {
    this.postForm.content += (this.postForm.content ? ' ' : '') + text;
  }

  appendToComment(text: string): void {
    this.newComment += (this.newComment ? ' ' : '') + text;
  }

  get liveUrgency() {
    return this.urgencyService.detect(this.postForm.title, this.postForm.content);
  }

  autoSlug(): void {
    this.postForm.slug = this.postForm.title.toLowerCase()
      .replace(/\s+/g, '-').replace(/[^a-z0-9-]/g, '');
  }

  // "Why This Failed" explanation
  failureExplanation: FailureExplanation | null = null;
  failureLoading = false;
  showFailurePanel = false;

  explainFailure(): void {
    if (!this.selectedPost) return;
    this.failureLoading = true;
    this.showFailurePanel = true;
    this.failureExplanation = null;
    this.aiSummary.getFailureExplanation(
      this.selectedPost.id, this.selectedPost.title, this.selectedPost.content
    ).subscribe({
      next: result => { this.failureExplanation = result; this.failureLoading = false; },
      error: () => { this.failureLoading = false; this.toast.error('AI explanation failed. Try again.'); }
    });
  }

  closeFailurePanel(): void { this.showFailurePanel = false; this.failureExplanation = null; }

  toggleBookmark(post: Post, event: Event): void {
    event.stopPropagation();
    const added = this.bookmarks.toggle(post);
    this.toast.success(added ? '🔖 Saved to My Library' : 'Removed from library');
  }

  // ── Related posts ─────────────────────────────────────────────────────────

  private readonly STOP_WORDS = new Set([
    'the','a','an','is','it','in','on','at','to','for','of','and','or','but',
    'with','from','this','that','are','was','were','be','been','have','has',
    'had','do','does','did','will','would','could','should','may','might',
    'i','you','we','they','he','she','its','our','your','their','my',
    'how','what','why','when','where','which','who','not','no','so','if'
  ]);

  private tokenize(text: string): Set<string> {
    return new Set(
      text.toLowerCase()
        .replace(/[^a-z0-9\s]/g, ' ')
        .split(/\s+/)
        .filter(w => w.length > 3 && !this.STOP_WORDS.has(w))
    );
  }

  private similarity(a: Post, b: Post): number {
    const setA = this.tokenize(`${a.title} ${a.content}`);
    const setB = this.tokenize(`${b.title} ${b.content}`);
    let common = 0;
    setA.forEach(w => { if (setB.has(w)) common++; });
    return common / (Math.sqrt(setA.size) * Math.sqrt(setB.size) || 1);
  }

  get relatedPosts(): Post[] {
    if (!this.selectedPost) return [];
    return this.posts
      .filter(p => p.id !== this.selectedPost!.id)
      .map(p => ({ post: p, score: this.similarity(this.selectedPost!, p) }))
      .filter(x => x.score > 0)
      .sort((a, b) => b.score - a.score)
      .slice(0, 3)
      .map(x => x.post);
  }

  // ── Urgency ───────────────────────────────────────────────────────────────

  get urgentPosts(): Post[] {
    return this.posts.filter(p => p.urgencyLevel === 'CRITICAL' || p.urgencyLevel === 'HIGH');
  }

  urgencyIcon(level: string): string {
    if (level === 'CRITICAL') return '🚨';
    if (level === 'HIGH') return '⚠️';
    return '';
  }

  urgencyLabel(level: string): string {
    if (level === 'CRITICAL') return 'CRITICAL';
    if (level === 'HIGH') return 'HIGH PRIORITY';
    return '';
  }

  requestLiveSession(post: Post, event: Event): void {
    event.stopPropagation();
    this.liveSessionPost = post;
    this.showLiveSession = true;
  }

  closeLiveSession(): void { this.showLiveSession = false; this.liveSessionPost = null; }

  // ── Helpers ───────────────────────────────────────────────────────────────

  truncate(text: string, n = 100): string {
    return text?.length > n ? text.substring(0, n) + '...' : text;
  }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }

  coverImg(post: Post): string {
    if (post.coverImage) return post.coverImage;
    const imgs = ['course-01', 'course-02', 'course-03', 'course-04', 'course-05', 'course-06'];
    return `${imgs[post.id % imgs.length]}.jpg`;
  }
}
