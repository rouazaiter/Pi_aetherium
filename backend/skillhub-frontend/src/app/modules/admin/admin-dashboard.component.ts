import { Component, OnInit, AfterViewInit, ElementRef, ViewChild } from '@angular/core';
import { ApiService } from '../../core/services/api.service';

declare const Chart: any;

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss']
})
export class AdminDashboardComponent implements OnInit, AfterViewInit {

  @ViewChild('postStatusChart') postStatusRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('reportStatusChart') reportStatusRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('overviewChart') overviewRef!: ElementRef<HTMLCanvasElement>;
  @ViewChild('discussionStatusChart') discStatusRef!: ElementRef<HTMLCanvasElement>;

  stats = { users: 0, posts: 0, discussions: 0, pendingReports: 0, kbArticles: 0 };
  loading = true;

  private allPosts: any[] = [];
  private allReports: any[] = [];
  private allDiscussions: any[] = [];

  constructor(private api: ApiService) {}

  ngOnInit(): void {
    Promise.all([
      this.api.getUsers().toPromise(),
      this.api.getPosts().toPromise(),
      this.api.getDiscussions(1).toPromise(),
      this.api.getAllReports().toPromise(),
      this.api.getKnowledgeBase().toPromise()
    ]).then(([users, posts, discussions, reports, kb]) => {
      this.allPosts = posts ?? [];
      this.allReports = reports ?? [];
      this.allDiscussions = discussions ?? [];
      this.stats.users = users?.length ?? 0;
      this.stats.posts = this.allPosts.length;
      this.stats.discussions = this.allDiscussions.length;
      this.stats.pendingReports = this.allReports.filter(r => r.status === 'PENDING').length;
      this.stats.kbArticles = kb?.length ?? 0;
      this.loading = false;
      setTimeout(() => this.buildCharts(), 50);
    }).catch(() => { this.loading = false; });
  }

  ngAfterViewInit(): void {}

  private buildCharts(): void {
    this.buildPostStatusChart();
    this.buildReportStatusChart();
    this.buildOverviewChart();
    this.buildDiscussionStatusChart();
  }

  private buildPostStatusChart(): void {
    const counts: Record<string, number> = {};
    this.allPosts.forEach(p => { counts[p.status] = (counts[p.status] || 0) + 1; });
    new Chart(this.postStatusRef.nativeElement, {
      type: 'pie',
      data: {
        labels: Object.keys(counts),
        datasets: [{ data: Object.values(counts), backgroundColor: ['#7a6ad8', '#4caf50', '#ff9800', '#f44336'] }]
      },
      options: { plugins: { legend: { position: 'bottom' } }, responsive: true }
    });
  }

  private buildReportStatusChart(): void {
    const counts: Record<string, number> = {};
    this.allReports.forEach(r => { counts[r.status] = (counts[r.status] || 0) + 1; });
    new Chart(this.reportStatusRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: Object.keys(counts),
        datasets: [{ data: Object.values(counts), backgroundColor: ['#ff9800', '#2196f3', '#4caf50', '#9e9e9e'] }]
      },
      options: { plugins: { legend: { position: 'bottom' } }, responsive: true }
    });
  }

  private buildOverviewChart(): void {
    new Chart(this.overviewRef.nativeElement, {
      type: 'bar',
      data: {
        labels: ['Users', 'Posts', 'Discussions', 'Reports', 'KB Articles'],
        datasets: [{
          label: 'Count',
          data: [this.stats.users, this.stats.posts, this.stats.discussions, this.allReports.length, this.stats.kbArticles],
          backgroundColor: ['#7a6ad8', '#4caf50', '#2196f3', '#ff9800', '#e91e63'],
          borderRadius: 6
        }]
      },
      options: {
        plugins: { legend: { display: false } },
        responsive: true,
        scales: { y: { beginAtZero: true, ticks: { stepSize: 1 } } }
      }
    });
  }

  private buildDiscussionStatusChart(): void {
    const counts: Record<string, number> = {};
    this.allDiscussions.forEach(d => { counts[d.status] = (counts[d.status] || 0) + 1; });
    new Chart(this.discStatusRef.nativeElement, {
      type: 'doughnut',
      data: {
        labels: Object.keys(counts),
        datasets: [{ data: Object.values(counts), backgroundColor: ['#2196f3', '#9c27b0', '#4caf50'] }]
      },
      options: { plugins: { legend: { position: 'bottom' } }, responsive: true }
    });
  }
}
