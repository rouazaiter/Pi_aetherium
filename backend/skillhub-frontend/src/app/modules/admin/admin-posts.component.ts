import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/services/api.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-admin-posts',
  templateUrl: './admin-posts.component.html',
  styleUrls: ['./admin-posts.component.scss']
})
export class AdminPostsComponent implements OnInit {
  posts: any[] = [];
  loading = true;
  search = '';

  constructor(private api: ApiService, private toast: ToastService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getPosts().subscribe({
      next: p => { this.posts = p; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get filtered(): any[] {
    if (!this.search.trim()) return this.posts;
    const q = this.search.toLowerCase();
    return this.posts.filter(p => p.title.toLowerCase().includes(q) || p.author?.username?.toLowerCase().includes(q));
  }

  deletePost(post: any): void {
    if (!confirm(`Delete post "${post.title}"?`)) return;
    this.api.deletePost(post.id, post.author?.id ?? 1).subscribe({
      next: () => { this.posts = this.posts.filter(p => p.id !== post.id); this.toast.success('Post deleted'); },
      error: () => this.toast.error('Failed to delete post')
    });
  }
}
