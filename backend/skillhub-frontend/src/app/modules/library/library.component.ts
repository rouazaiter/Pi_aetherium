import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { BookmarkService, BookmarkedPost, LearningPath } from '../../core/services/bookmark.service';
import { ApiService } from '../../core/services/api.service';
import { Comment } from '../../core/models/blog.models';

@Component({
  selector: 'app-library',
  templateUrl: './library.component.html',
  styleUrls: ['./library.component.scss']
})
export class LibraryComponent implements OnInit, OnDestroy {

  readonly Math = Math;
  readonly CURRENT_USER = 1;

  bookmarks: BookmarkedPost[] = [];
  learningPaths: LearningPath[] = [];
  view: 'paths' | 'all' = 'paths';
  searchQuery = '';
  selectedTopic = '';

  // Post reader modal
  selectedPost: BookmarkedPost | null = null;
  fullContent = '';
  loadingContent = false;
  readingProgress = 0;

  // Comments & likes
  comments: Comment[] = [];
  likeCount = 0;
  liked = false;
  newComment = '';
  loadingComments = false;

  private sub!: Subscription;

  constructor(public bookmarkService: BookmarkService, private router: Router, private api: ApiService) {}

  ngOnInit(): void {
    this.sub = this.bookmarkService.bookmarks$obs.subscribe(() => {
      this.bookmarks = this.bookmarkService.bookmarks;
      this.learningPaths = this.bookmarkService.learningPaths;
    });
  }

  openPost(post: BookmarkedPost): void {
    this.selectedPost = post;
    this.readingProgress = post.progress;
    this.fullContent = '';
    this.comments = [];
    this.likeCount = 0;
    this.liked = false;
    this.newComment = '';
    this.loadingContent = true;
    this.loadingComments = true;

    this.api.getPost(post.id).subscribe({
      next: p => { this.fullContent = p.content; this.loadingContent = false; },
      error: () => { this.fullContent = post.content || 'Content not available.'; this.loadingContent = false; }
    });

    this.api.getComments(post.id).subscribe({
      next: c => { this.comments = c; this.loadingComments = false; },
      error: () => { this.comments = []; this.loadingComments = false; }
    });

    this.api.getPostLikeCount(post.id).subscribe(r => this.likeCount = r.count);
  }

  closePost(): void {
    if (this.selectedPost) {
      this.bookmarkService.updateProgress(this.selectedPost.id, this.readingProgress);
    }
    this.selectedPost = null;
    this.fullContent = '';
    this.comments = [];
  }

  onContentScroll(event: Event): void {
    const el = event.target as HTMLDivElement;
    const total = el.scrollHeight - el.clientHeight;
    if (total > 0) {
      this.readingProgress = Math.round((el.scrollTop / total) * 100);
      if (this.selectedPost) {
        this.bookmarkService.updateProgress(this.selectedPost.id, this.readingProgress);
      }
    }
  }

  markComplete(): void {
    if (this.selectedPost) {
      this.bookmarkService.updateProgress(this.selectedPost.id, 100);
      this.readingProgress = 100;
    }
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

  submitComment(): void {
    if (!this.newComment.trim() || !this.selectedPost) return;
    this.api.addComment(this.selectedPost.id, this.CURRENT_USER, this.newComment).subscribe(() => {
      this.newComment = '';
      this.api.getComments(this.selectedPost!.id).subscribe(c => this.comments = c);
    });
  }

  sentimentEmoji(s: string): string {
    if (s === 'POSITIVE') return '😊';
    if (s === 'NEGATIVE') return '😡';
    return '😐';
  }

  get filteredBookmarks(): BookmarkedPost[] {
    let result = this.bookmarks;
    if (this.selectedTopic) result = result.filter(b => b.topic === this.selectedTopic);
    if (this.searchQuery.trim()) {
      const q = this.searchQuery.toLowerCase();
      result = result.filter(b => b.title.toLowerCase().includes(q) || b.topic.toLowerCase().includes(q));
    }
    return result;
  }

  get allTopics(): string[] { return [...new Set(this.bookmarks.map(b => b.topic))]; }

  get totalProgress(): number {
    if (!this.bookmarks.length) return 0;
    return Math.round(this.bookmarks.reduce((sum, b) => sum + b.progress, 0) / this.bookmarks.length);
  }

  remove(postId: number): void { this.bookmarkService.remove(postId); }

  topicIcon(topic: string): string {
    const icons: Record<string, string> = {
      'Spring Boot': '🍃', 'Angular': '🔴', 'Security': '🔒',
      'Database': '🗄️', 'DevOps': '⚙️', 'Debugging': '🐛',
      'Architecture': '🏗️', 'Performance': '⚡', 'General': '📄'
    };
    return icons[topic] || '📄';
  }

  progressColor(p: number): string {
    if (p >= 80) return '#4caf50';
    if (p >= 40) return '#ff9800';
    return '#7a6ad8';
  }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
  }

  ngOnDestroy(): void { this.sub?.unsubscribe(); }
}
