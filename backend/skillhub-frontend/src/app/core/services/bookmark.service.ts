import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Post } from '../models/blog.models';

export interface BookmarkedPost extends Post {
  bookmarkedAt: string;
  topic: string;
  progress: number; // 0-100 reading progress
}

export interface LearningPath {
  topic: string;
  posts: BookmarkedPost[];
  completedCount: number;
}

const STORAGE_KEY = 'skillhub_bookmarks';

// Topic detection keywords
const TOPIC_MAP: Record<string, string[]> = {
  'Spring Boot':    ['spring', 'boot', 'springboot', 'jpa', 'hibernate', 'maven', 'gradle'],
  'Angular':        ['angular', 'component', 'module', 'directive', 'ngmodule', 'rxjs', 'typescript'],
  'Security':       ['security', 'jwt', 'auth', 'oauth', 'cors', 'breach', 'hacked', 'vulnerability'],
  'Database':       ['database', 'mysql', 'sql', 'query', 'schema', 'migration', 'jpa', 'repository'],
  'DevOps':         ['docker', 'kubernetes', 'deploy', 'ci', 'cd', 'pipeline', 'server', 'nginx'],
  'Debugging':      ['error', 'bug', 'crash', 'exception', 'fix', 'debug', 'stack', 'trace', 'null'],
  'Architecture':   ['microservice', 'architecture', 'design', 'pattern', 'rest', 'api', 'service'],
  'Performance':    ['performance', 'slow', 'optimize', 'cache', 'memory', 'timeout', 'latency'],
  'General':        []
};

@Injectable({ providedIn: 'root' })
export class BookmarkService {

  private bookmarks$ = new BehaviorSubject<BookmarkedPost[]>(this.load());

  get bookmarks(): BookmarkedPost[] { return this.bookmarks$.value; }
  get bookmarks$obs() { return this.bookmarks$.asObservable(); }

  isBookmarked(postId: number): boolean {
    return this.bookmarks.some(b => b.id === postId);
  }

  toggle(post: Post): boolean {
    if (this.isBookmarked(post.id)) {
      this.remove(post.id);
      return false;
    } else {
      this.add(post);
      return true;
    }
  }

  add(post: Post): void {
    const bookmarked: BookmarkedPost = {
      ...post,
      bookmarkedAt: new Date().toISOString(),
      topic: this.detectTopic(post.title + ' ' + post.content),
      progress: 0
    };
    const updated = [bookmarked, ...this.bookmarks.filter(b => b.id !== post.id)];
    this.save(updated);
  }

  remove(postId: number): void {
    this.save(this.bookmarks.filter(b => b.id !== postId));
  }

  updateProgress(postId: number, progress: number): void {
    const updated = this.bookmarks.map(b =>
      b.id === postId ? { ...b, progress: Math.min(100, Math.round(progress)) } : b
    );
    this.save(updated);
  }

  get learningPaths(): LearningPath[] {
    const grouped: Record<string, BookmarkedPost[]> = {};
    for (const b of this.bookmarks) {
      if (!grouped[b.topic]) grouped[b.topic] = [];
      grouped[b.topic].push(b);
    }
    return Object.entries(grouped).map(([topic, posts]) => ({
      topic,
      posts,
      completedCount: posts.filter(p => p.progress >= 80).length
    })).sort((a, b) => b.posts.length - a.posts.length);
  }

  get totalCount(): number { return this.bookmarks.length; }
  get completedCount(): number { return this.bookmarks.filter(b => b.progress >= 80).length; }

  detectTopic(text: string): string {
    const lower = text.toLowerCase();
    for (const [topic, keywords] of Object.entries(TOPIC_MAP)) {
      if (keywords.some(kw => lower.includes(kw))) return topic;
    }
    return 'General';
  }

  private save(bookmarks: BookmarkedPost[]): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(bookmarks));
    this.bookmarks$.next(bookmarks);
  }

  private load(): BookmarkedPost[] {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || '[]');
    } catch { return []; }
  }
}
