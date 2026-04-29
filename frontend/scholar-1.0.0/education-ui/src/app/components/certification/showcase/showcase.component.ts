import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NgIf, NgFor } from '@angular/common';

interface CertData {
  certificateId: string;
  valid: boolean;
  holderName: string | null;
  certificationTitle: string | null;
  category: string | null;
  difficulty: string | null;
  score: number;
  passed: boolean;
  issuedAt: string | null;
  issuedBy: string | null;
  issuerTitle: string | null;
  platform: string | null;
}

@Component({
    selector: 'app-showcase',
    templateUrl: './showcase.component.html',
    styleUrls: ['./showcase.component.scss'],
    standalone: true,
    imports: [NgIf, NgFor]
})
export class ShowcaseComponent implements OnInit {

  cert: CertData | null = null;
  loading = true;
  error   = '';
  copied  = false;

  private readonly API = 'http://172.16.0.107:8080/api';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.queryParamMap.get('id');
    if (!id) { this.error = 'No certificate ID provided.'; this.loading = false; return; }

    const params = new HttpParams().set('id', id);
    this.http.get<CertData>(`${this.API}/verify`, { params }).subscribe({
      next: data => { this.cert = data; this.loading = false; },
      error: ()   => { this.error = 'Could not load certificate data.'; this.loading = false; }
    });
  }

  // ── Computed ──────────────────────────────────────────────────────────────

  get scorePercent(): number { return Math.round(this.cert?.score ?? 0); }

  get circumference(): number { return 2 * Math.PI * 44; }

  get dashOffset(): number {
    return this.circumference * (1 - this.scorePercent / 100);
  }

  get formattedDate(): string {
    if (!this.cert?.issuedAt) return '';
    return new Date(this.cert.issuedAt).toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric'
    });
  }

  get difficultyEmoji(): string {
    return { BEGINNER: '🌱', INTERMEDIATE: '⚡', ADVANCED: '🔥' }[this.cert?.difficulty ?? ''] ?? '🎓';
  }

  get difficultyColor(): string {
    return { BEGINNER: '#10b981', INTERMEDIATE: '#f59e0b', ADVANCED: '#ef4444' }[this.cert?.difficulty ?? ''] ?? '#6366f1';
  }

  get skillTags(): string[] {
    const cat = (this.cert?.category ?? '').toLowerCase();
    if (cat.includes('java'))       return ['OOP', 'Collections', 'Streams', 'JDBC', 'Generics'];
    if (cat.includes('spring'))     return ['REST APIs', 'Dependency Injection', 'JPA', 'Security', 'MVC'];
    if (cat.includes('python'))     return ['Data Structures', 'OOP', 'File I/O', 'Libraries', 'Algorithms'];
    if (cat.includes('javascript')) return ['ES6+', 'Async/Await', 'DOM', 'Modules', 'Closures'];
    if (cat.includes('react'))      return ['Components', 'Hooks', 'State', 'Routing', 'Context'];
    if (cat.includes('angular'))    return ['Components', 'Services', 'RxJS', 'Forms', 'Routing'];
    if (cat.includes('sql'))        return ['Queries', 'Joins', 'Indexes', 'Transactions', 'Aggregation'];
    if (cat.includes('php'))        return ['Syntax', 'OOP', 'Forms', 'Sessions', 'MySQL'];
    return ['Core Concepts', 'Best Practices', 'Problem Solving', 'Applied Skills', 'Architecture'];
  }

  get shareUrl(): string {
    return window.location.href;
  }

  get shareText(): string {
    const name  = this.cert?.holderName ?? 'I';
    const title = this.cert?.certificationTitle ?? 'a certification';
    const score = this.scorePercent;
    return `🎓 ${name} just earned the "${title}" certification on SkillHub with a score of ${score}%! Verified achievement. #SkillHub #Certification #Learning`;
  }

  // ── Share actions ─────────────────────────────────────────────────────────

  shareWhatsApp(): void {
    const text = encodeURIComponent(this.shareText + '\n\n' + this.shareUrl);
    window.open(`https://wa.me/?text=${text}`, '_blank');
  }

  shareLinkedIn(): void {
    const url  = encodeURIComponent(this.shareUrl);
    const text = encodeURIComponent(this.shareText);
    window.open(`https://www.linkedin.com/sharing/share-offsite/?url=${url}&summary=${text}`, '_blank');
  }

  copyLink(): void {
    navigator.clipboard.writeText(this.shareUrl).then(() => {
      this.copied = true;
      setTimeout(() => this.copied = false, 2500);
    });
  }
}
