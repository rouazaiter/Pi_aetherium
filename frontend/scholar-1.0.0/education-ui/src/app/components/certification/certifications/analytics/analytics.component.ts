import {
  Component, OnInit, OnDestroy, AfterViewInit,
  ViewChild, ElementRef, ChangeDetectorRef
} from '@angular/core';
import { Chart, registerables, ChartOptions } from 'chart.js';
import { CertificationService } from '../../services/certification.service';
import { AnalyticsService, AnalyticsData, AnalyticsFilters, FeedbackInsights } from '../../services/analytics.service';
import { Certification } from '../../models/certification.model';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';

Chart.register(...registerables);

// ── Shared chart defaults ─────────────────────────────────────────────────────
const FONT = "'Inter', 'Helvetica Neue', sans-serif";
Chart.defaults.font.family = FONT;
Chart.defaults.color = '#64748b';

const P = {
  indigo:  '#6366f1', indigoL: '#818cf8', indigoA: 'rgba(99,102,241,',
  emerald: '#10b981', emeraldA:'rgba(16,185,129,',
  rose:    '#ef4444', roseA:   'rgba(239,68,68,',
  amber:   '#f59e0b', amberA:  'rgba(245,158,11,',
  sky:     '#0ea5e9', skyA:    'rgba(14,165,233,',
  violet:  '#8b5cf6', violetA: 'rgba(139,92,246,',
  slate:   '#64748b',
};

const MULTI_COLORS = [
  P.indigo, P.emerald, P.amber, P.sky, P.violet, P.rose,
  '#f97316','#14b8a6','#a855f7','#ec4899','#84cc16','#06b6d4'
];

function multiAlpha(i: number, a = 0.85): string {
  const c = MULTI_COLORS[i % MULTI_COLORS.length];
  // convert hex to rgba
  const r = parseInt(c.slice(1,3),16), g = parseInt(c.slice(3,5),16), b = parseInt(c.slice(5,7),16);
  return `rgba(${r},${g},${b},${a})`;
}

const BASE_OPTIONS: Partial<ChartOptions> = {
  responsive: true,
  maintainAspectRatio: false,
  animation: { duration: 600, easing: 'easeOutQuart' },
  plugins: {
    legend: { labels: { font: { family: FONT, size: 12 }, padding: 16, usePointStyle: true } },
    tooltip: {
      backgroundColor: '#0f172a',
      titleFont: { family: FONT, size: 12, weight: 'bold' },
      bodyFont:  { family: FONT, size: 12 },
      padding: 10, cornerRadius: 8, displayColors: true
    }
  }
};

@Component({
    selector: 'app-analytics',
    templateUrl: './analytics.component.html',
    styleUrls: ['./analytics.component.scss'],
    standalone: true,
    imports: [NgIf, NgFor, FormsModule]
})
export class AnalyticsComponent implements OnInit, OnDestroy, AfterViewInit {

  @ViewChild('passFailChart')    passFailRef!:    ElementRef<HTMLCanvasElement>;
  @ViewChild('scoreDistChart')   scoreDistRef!:   ElementRef<HTMLCanvasElement>;
  @ViewChild('avgScoreChart')    avgScoreRef!:    ElementRef<HTMLCanvasElement>;
  @ViewChild('trendChart')       trendRef!:       ElementRef<HTMLCanvasElement>;
  @ViewChild('difficultyChart')  difficultyRef!:  ElementRef<HTMLCanvasElement>;
  @ViewChild('mostFailedChart')  mostFailedRef!:  ElementRef<HTMLCanvasElement>;
  @ViewChild('enrollmentsChart') enrollmentsRef!: ElementRef<HTMLCanvasElement>;

  // ── State ─────────────────────────────────────────────────────────────────
  data: AnalyticsData | null = null;
  certifications: Certification[] = [];
  loading = true;
  error   = '';

  // ── Feedback insights ─────────────────────────────────────────────────────
  feedbackInsights: FeedbackInsights[] = [];
  loadingFeedback = false;

  // ── Filters ───────────────────────────────────────────────────────────────
  filters: AnalyticsFilters = {};
  showFilters = true;

  readonly difficulties = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];

  get availableCategories(): string[] {
    return this.data?.availableCategories ?? [];
  }

  // Quick date presets
  readonly datePresets = [
    { label: 'Last 7 days',  days: 7  },
    { label: 'Last 30 days', days: 30 },
    { label: 'Last 90 days', days: 90 },
    { label: 'This year',    days: 365 },
  ];
  activePreset: number | null = null;

  private charts: Chart[] = [];

  constructor(
    private analyticsService: AnalyticsService,
    private certService: CertificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.certService.getAll().subscribe({ next: c => this.certifications = c, error: () => {} });
    this.loadData();
    this.loadFeedbackInsights();
  }

  ngAfterViewInit(): void {}
  ngOnDestroy(): void { this.destroyCharts(); }

  // ── Filters ───────────────────────────────────────────────────────────────

  applyPreset(days: number, idx: number): void {
    this.activePreset = idx;
    const to   = new Date();
    const from = new Date();
    from.setDate(from.getDate() - days);
    this.filters.dateFrom = from.toISOString().split('T')[0];
    this.filters.dateTo   = to.toISOString().split('T')[0];
    this.loadData();
  }

  clearFilters(): void {
    this.filters = {};
    this.activePreset = null;
    this.loadData();
  }

  get hasActiveFilters(): boolean {
    return !!(this.filters.certificationId || this.filters.difficulty ||
              this.filters.category || this.filters.dateFrom || this.filters.dateTo);
  }

  onFilterChange(): void {
    this.activePreset = null;
    this.loadData();
    this.loadFeedbackInsights();
  }

  // ── Load ──────────────────────────────────────────────────────────────────

  loadData(): void {
    this.loading = true;
    this.error   = '';
    this.analyticsService.getAnalytics(this.filters).subscribe({
      next: d => {
        this.data    = d;
        this.loading = false;
        this.cdr.detectChanges();
        setTimeout(() => this.buildCharts(), 60);
      },
      error: () => { this.error = 'Failed to load analytics'; this.loading = false; }
    });
  }

  loadFeedbackInsights(): void {
    this.loadingFeedback = true;
    this.analyticsService.getFeedbackInsights(
      this.filters.certificationId ?? undefined
    ).subscribe({
      next: insights => { this.feedbackInsights = insights; this.loadingFeedback = false; },
      error: () => { this.loadingFeedback = false; }
    });
  }

  // ── Feedback helpers ──────────────────────────────────────────────────────

  difficultyEmoji(d: string): string {
    return { EASY: '😌', BALANCED: '⚖️', HARD: '🔥' }[d] ?? '❓';
  }
  timeEmoji(t: string): string {
    return { TOO_SHORT: '⏩', ADEQUATE: '✅', TOO_LONG: '⏳' }[t] ?? '❓';
  }
  relevanceEmoji(r: string): string {
    return { YES: '🎯', PARTIALLY: '🔄', NO: '❌' }[r] ?? '❓';
  }
  pctOf(map: Record<string, number>, key: string, total: number): number {
    if (!total) return 0;
    return Math.round(((map[key] ?? 0) / total) * 100);
  }
  flagCount(ins: FeedbackInsights): number {
    return [ins.flaggedTooHard, ins.flaggedTooEasy, ins.flaggedNotRelevant, ins.flaggedTimeTooShort]
      .filter(Boolean).length;
  }

  // ── Charts ────────────────────────────────────────────────────────────────

  private destroyCharts(): void {
    this.charts.forEach(c => c.destroy());
    this.charts = [];
  }

  private buildCharts(): void {
    this.destroyCharts();
    if (!this.data) return;
    const d = this.data;

    // ── 1. Pass / Fail Doughnut ───────────────────────────────────────────
    if (this.passFailRef && d.completedExams > 0) {
      this.charts.push(new Chart(this.passFailRef.nativeElement, {
        type: 'doughnut',
        data: {
          labels: ['Passed', 'Failed'],
          datasets: [{
            data: [d.passCount, d.failCount],
            backgroundColor: [P.emerald, P.rose],
            borderWidth: 3, borderColor: '#fff',
            hoverOffset: 10
          }]
        },
        options: {
          ...BASE_OPTIONS as any,
          cutout: '72%',
          plugins: {
            ...BASE_OPTIONS.plugins,
            legend: { position: 'bottom', labels: { padding: 20, font: { size: 13 } } },
            tooltip: {
              ...BASE_OPTIONS.plugins!.tooltip,
              callbacks: {
                label: (ctx: any) => {
                  const pct = Math.round(ctx.parsed / d.completedExams * 100);
                  return `  ${ctx.label}: ${ctx.parsed} (${pct}%)`;
                }
              }
            }
          }
        }
      }));
    }

    // ── 2. Score Distribution ─────────────────────────────────────────────
    if (this.scoreDistRef) {
      const colors = d.scoreDistribution.map((_, i) =>
        i >= 7 ? P.emeraldA + '0.85)' : i >= 5 ? P.amberA + '0.85)' : P.roseA + '0.85)');
      this.charts.push(new Chart(this.scoreDistRef.nativeElement, {
        type: 'bar',
        data: {
          labels: d.scoreDistribution.map(b => b.range),
          datasets: [{
            label: 'Students',
            data: d.scoreDistribution.map(b => b.count),
            backgroundColor: colors,
            borderRadius: 7, borderSkipped: false
          }]
        },
        options: {
          ...BASE_OPTIONS as any,
          plugins: { ...BASE_OPTIONS.plugins, legend: { display: false } },
          scales: {
            x: { grid: { display: false }, border: { display: false },
                 title: { display: true, text: 'Score Range', font: { size: 11 } } },
            y: { beginAtZero: true, ticks: { stepSize: 1 }, border: { display: false },
                 grid: { color: '#f1f5f9' },
                 title: { display: true, text: 'Students', font: { size: 11 } } }
          }
        }
      }));
    }

    // ── 3. Avg Score per Cert (horizontal bar) ────────────────────────────
    if (this.avgScoreRef && d.avgScorePerCert.length > 0) {
      const vals = d.avgScorePerCert.map(c => c.value);
      this.charts.push(new Chart(this.avgScoreRef.nativeElement, {
        type: 'bar',
        data: {
          labels: d.avgScorePerCert.map(c => c.name),
          datasets: [{
            label: 'Avg Score (%)',
            data: vals,
            backgroundColor: vals.map(v =>
              v >= 70 ? P.emeraldA + '0.8)' : v >= 50 ? P.amberA + '0.8)' : P.roseA + '0.8)'),
            borderColor: vals.map(v =>
              v >= 70 ? P.emerald : v >= 50 ? P.amber : P.rose),
            borderWidth: 1.5, borderRadius: 6
          }]
        },
        options: {
          ...BASE_OPTIONS as any,
          indexAxis: 'y',
          plugins: { ...BASE_OPTIONS.plugins, legend: { display: false },
            tooltip: { ...BASE_OPTIONS.plugins!.tooltip,
              callbacks: { label: (ctx: any) => `  ${ctx.parsed.x}%` } }
          },
          scales: {
            x: { min: 0, max: 100, border: { display: false }, grid: { color: '#f1f5f9' },
                 title: { display: true, text: 'Average Score (%)', font: { size: 11 } } },
            y: { grid: { display: false }, border: { display: false },
                 ticks: { font: { size: 11 } } }
          }
        }
      }));
    }

    // ── 4. Monthly Trend (line) ───────────────────────────────────────────
    if (this.trendRef && d.monthlyTrend.length > 0) {
      this.charts.push(new Chart(this.trendRef.nativeElement, {
        type: 'line',
        data: {
          labels: d.monthlyTrend.map(t => t.month),
          datasets: [
            {
              label: 'Avg Score (%)',
              data: d.monthlyTrend.map(t => t.avgScore),
              borderColor: P.indigo, backgroundColor: P.indigoA + '0.12)',
              fill: true, tension: 0.45,
              pointRadius: 5, pointHoverRadius: 8,
              pointBackgroundColor: P.indigo, pointBorderColor: '#fff', pointBorderWidth: 2,
              yAxisID: 'y'
            },
            {
              label: 'Exams Taken',
              data: d.monthlyTrend.map(t => t.count),
              borderColor: P.emerald, backgroundColor: 'transparent',
              borderDash: [6, 4], tension: 0.4,
              pointRadius: 4, pointHoverRadius: 7,
              pointBackgroundColor: P.emerald, pointBorderColor: '#fff', pointBorderWidth: 2,
              yAxisID: 'y1'
            }
          ]
        },
        options: {
          ...BASE_OPTIONS as any,
          plugins: { ...BASE_OPTIONS.plugins, legend: { position: 'top' } },
          scales: {
            x: { grid: { display: false }, border: { display: false } },
            y:  { min: 0, max: 100, border: { display: false }, grid: { color: '#f1f5f9' },
                  title: { display: true, text: 'Avg Score (%)', font: { size: 11 } } },
            y1: { position: 'right', beginAtZero: true, border: { display: false },
                  grid: { drawOnChartArea: false },
                  title: { display: true, text: 'Exams', font: { size: 11 } } }
          }
        }
      }));
    }

    // ── 5. Question Difficulty ────────────────────────────────────────────
    if (this.difficultyRef && d.questionDifficulty.length > 0) {
      const top = d.questionDifficulty.slice(0, 12);
      this.charts.push(new Chart(this.difficultyRef.nativeElement, {
        type: 'bar',
        data: {
          labels: top.map(q => q.questionText),
          datasets: [{
            label: 'Correct Rate (%)',
            data: top.map(q => q.correctRate),
            backgroundColor: top.map(q =>
              q.correctRate >= 70 ? P.emeraldA + '0.8)' :
              q.correctRate >= 40 ? P.amberA  + '0.8)' : P.roseA + '0.8)'),
            borderRadius: 5
          }]
        },
        options: {
          ...BASE_OPTIONS as any,
          indexAxis: 'y',
          plugins: { ...BASE_OPTIONS.plugins, legend: { display: false },
            tooltip: { ...BASE_OPTIONS.plugins!.tooltip,
              callbacks: { label: (ctx: any) =>
                `  ${ctx.parsed.x}% correct · ${top[ctx.dataIndex].totalAttempts} attempts` }
            }
          },
          scales: {
            x: { min: 0, max: 100, border: { display: false }, grid: { color: '#f1f5f9' },
                 title: { display: true, text: 'Correct Rate (%)', font: { size: 11 } } },
            y: { grid: { display: false }, border: { display: false },
                 ticks: { font: { size: 11 } } }
          }
        }
      }));
    }

    // ── 6. Most Failed Questions ──────────────────────────────────────────
    if (this.mostFailedRef && d.mostFailedQuestions.length > 0) {
      const top = d.mostFailedQuestions.slice(0, 10);
      this.charts.push(new Chart(this.mostFailedRef.nativeElement, {
        type: 'bar',
        data: {
          labels: top.map(q => q.questionText),
          datasets: [{
            label: 'Fail Rate (%)',
            data: top.map(q => Math.round((100 - q.correctRate) * 10) / 10),
            backgroundColor: top.map((_, i) => `rgba(239,68,68,${0.5 + i * 0.05})`),
            borderColor: P.rose, borderWidth: 1, borderRadius: 5
          }]
        },
        options: {
          ...BASE_OPTIONS as any,
          indexAxis: 'y',
          plugins: { ...BASE_OPTIONS.plugins, legend: { display: false } },
          scales: {
            x: { min: 0, max: 100, border: { display: false }, grid: { color: '#f1f5f9' },
                 title: { display: true, text: 'Fail Rate (%)', font: { size: 11 } } },
            y: { grid: { display: false }, border: { display: false },
                 ticks: { font: { size: 11 } } }
          }
        }
      }));
    }

    // ── 7. Enrollments per Cert ───────────────────────────────────────────
    if (this.enrollmentsRef && d.enrollmentsPerCert.length > 0) {
      this.charts.push(new Chart(this.enrollmentsRef.nativeElement, {
        type: 'bar',
        data: {
          labels: d.enrollmentsPerCert.map(c => c.name),
          datasets: [{
            label: 'Enrollments',
            data: d.enrollmentsPerCert.map(c => c.value),
            backgroundColor: d.enrollmentsPerCert.map((_, i) => multiAlpha(i)),
            borderRadius: 7, borderSkipped: false
          }]
        },
        options: {
          ...BASE_OPTIONS as any,
          plugins: { ...BASE_OPTIONS.plugins, legend: { display: false } },
          scales: {
            x: { grid: { display: false }, border: { display: false },
                 ticks: { font: { size: 11 } } },
            y: { beginAtZero: true, ticks: { stepSize: 1 }, border: { display: false },
                 grid: { color: '#f1f5f9' } }
          }
        }
      }));
    }
  }

  // ── Computed ──────────────────────────────────────────────────────────────
  get passRateDisplay(): string { return this.data ? this.data.overallPassRate.toFixed(1) + '%' : '—'; }
  get avgScoreDisplay(): string { return this.data ? this.data.averageScore.toFixed(1) + '%' : '—'; }

  getCertName(id: number | null | undefined): string {
    if (!id) return '';
    return this.certifications.find(c => c.id === id)?.title ?? String(id);
  }

  get kpiTrend(): { pass: string; score: string } {
    // Simple trend indicator based on pass rate
    const pr = this.data?.overallPassRate ?? 0;
    return {
      pass:  pr >= 70 ? '↑ Good' : pr >= 50 ? '→ Average' : '↓ Low',
      score: (this.data?.averageScore ?? 0) >= 70 ? '↑ Good' : '→ Average'
    };
  }
}
