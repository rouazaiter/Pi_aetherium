import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/services/api.service';

@Component({
  selector: 'app-admin-kb',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-kb.component.html',
  styleUrls: ['./admin-kb.component.scss']
})
export class AdminKbComponent implements OnInit {
  articles: any[] = [];
  loading = true;
  search = '';

  constructor(private api: ApiService) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    this.api.getKnowledgeBase().subscribe({
      next: a => { this.articles = a; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }

  get filtered(): any[] {
    if (!this.search.trim()) return this.articles;
    const q = this.search.toLowerCase();
    return this.articles.filter(a => a.title?.toLowerCase().includes(q) || a.summary?.toLowerCase().includes(q));
  }

  formatDate(dt: string): string {
    return new Date(dt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
  }
}
