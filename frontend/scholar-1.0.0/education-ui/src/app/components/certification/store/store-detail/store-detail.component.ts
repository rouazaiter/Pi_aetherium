import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { CertificationDetail } from '../../models/certification.model';
import { EnrollmentDTO } from '../../models/enrollment.model';
import { NgIf, NgFor, DecimalPipe } from '@angular/common';

@Component({
    selector: 'app-store-detail',
    templateUrl: './store-detail.component.html',
    styleUrls: ['./store-detail.component.scss'],
    standalone: true,
    imports: [NgIf, RouterLink, NgFor, DecimalPipe]
})
export class StoreDetailComponent implements OnInit {
  cert: CertificationDetail | null = null;
  enrollment: EnrollmentDTO | null = null;
  loading = true;
  error = '';
  userName = '';
  showNameModal = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enrollService: EnrollmentService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.userName = this.enrollService.getUser();
    this.enrollService.getStoreDetail(id).subscribe({
      next: data => {
        this.cert = data;
        this.loading = false;
        if (this.userName) this.checkEnrollment();
      },
      error: () => { this.error = 'Certification not found'; this.loading = false; }
    });
  }

  checkEnrollment(): void {
    if (!this.cert || !this.userName) return;
    this.enrollService.getEnrollment(this.cert.id, this.userName).subscribe({
      next: e  => this.enrollment = e,
      error: () => this.enrollment = null
    });
  }

  get totalQuestions(): number {
    return this.cert?.exams.reduce((s, e) => s + e.questions.length, 0) ?? 0;
  }

  get timeLimit(): number {
    return this.cert?.exams[0]?.timeLimit ?? this.cert?.durationMinutes ?? 60;
  }

  get passingScore(): number {
    return this.cert?.exams[0]?.passingScore ?? this.cert?.passingScore ?? 70;
  }

  // Derived skill tags from category + difficulty
  get skillTags(): string[] {
    const cat = (this.cert?.category ?? '').toLowerCase();
    const base: string[] = [];
    if (cat.includes('java'))       base.push('OOP', 'Collections', 'Streams', 'JDBC');
    if (cat.includes('spring'))     base.push('REST APIs', 'Dependency Injection', 'JPA', 'Security');
    if (cat.includes('python'))     base.push('Data Structures', 'OOP', 'File I/O', 'Libraries');
    if (cat.includes('javascript')) base.push('ES6+', 'Async/Await', 'DOM', 'Modules');
    if (cat.includes('react'))      base.push('Components', 'Hooks', 'State', 'Routing');
    if (cat.includes('angular'))    base.push('Components', 'Services', 'RxJS', 'Forms');
    if (cat.includes('sql'))        base.push('Queries', 'Joins', 'Indexes', 'Transactions');
    if (cat.includes('cloud'))      base.push('IaaS', 'PaaS', 'Networking', 'Security');
    if (base.length === 0)          base.push('Core Concepts', 'Best Practices', 'Problem Solving', 'Applied Skills');
    return base;
  }

  // Estimated success rate based on passing score
  get estimatedSuccessRate(): number {
    const ps = this.passingScore;
    if (ps <= 60) return 78;
    if (ps <= 70) return 65;
    if (ps <= 80) return 52;
    return 38;
  }

  // Who this is for
  get targetAudience(): string[] {
    const d = this.cert?.difficulty ?? 'BEGINNER';
    if (d === 'BEGINNER')     return ['Students and career starters', 'Self-taught developers', 'Professionals switching to tech'];
    if (d === 'INTERMEDIATE') return ['Developers with 1–3 years experience', 'Professionals seeking validation', 'Team leads and senior engineers'];
    return ['Senior engineers and architects', 'Tech leads and CTOs', 'Professionals targeting top-tier roles'];
  }

  get prerequisites(): string[] {
    const d = this.cert?.difficulty ?? 'BEGINNER';
    if (d === 'BEGINNER')     return ['Basic programming knowledge', 'Familiarity with the technology', 'No prior certification required'];
    if (d === 'INTERMEDIATE') return ['1+ year of hands-on experience', 'Understanding of core concepts', 'Completed beginner-level projects'];
    return ['3+ years of professional experience', 'Deep understanding of architecture patterns', 'Prior intermediate certification recommended'];
  }

  // Exam rules
  get examRules(): { icon: string; text: string }[] {
    return [
      { icon: '⏱', text: `${this.timeLimit}-minute time limit — auto-submitted when time expires` },
      { icon: '🔒', text: 'Secure exam mode — tab switching and window changes are monitored' },
      { icon: '📷', text: 'Camera verification required before the exam begins' },
      { icon: '✅', text: `Pass with ${this.passingScore}% or higher to earn your certificate` },
      { icon: '📄', text: 'Certificate issued instantly upon passing' },
    ];
  }

  // CTA actions
  clickEnroll(): void {
    if (!this.userName) { this.showNameModal = true; return; }
    this.router.navigate(['/store', this.cert!.id, 'checkout']);
  }

  confirmName(name: string): void {
    if (!name.trim()) return;
    this.userName = name.trim();
    this.enrollService.setUser(this.userName);
    this.showNameModal = false;
    this.router.navigate(['/store', this.cert!.id, 'checkout']);
  }

  startExam():     void { this.router.navigate(['/store', this.cert!.id, 'exam']); }
  startPractice(): void { this.router.navigate(['/store', this.cert!.id, 'practice']); }
  viewResult():    void { this.router.navigate(['/store', this.cert!.id, 'result']); }

  get difficultyConfig(): { label: string; color: string; bg: string; icon: string } {
    const map: Record<string, any> = {
      BEGINNER:     { label: 'Beginner',     color: '#059669', bg: '#d1fae5', icon: '🌱' },
      INTERMEDIATE: { label: 'Intermediate', color: '#d97706', bg: '#fef3c7', icon: '⚡' },
      ADVANCED:     { label: 'Advanced',     color: '#dc2626', bg: '#fee2e2', icon: '🔥' },
    };
    return map[this.cert?.difficulty ?? 'BEGINNER'] ?? map['BEGINNER'];
  }
}
