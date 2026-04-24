import { Component, OnInit } from '@angular/core';
import { LeaderboardCategory, LeaderboardEntry, LeaderboardType } from '../../../core/models/leaderboard.model';
import { LeaderboardService } from '../../../core/services/leaderboard.service';

@Component({
  selector: 'app-leaderboard',
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.css']
})
export class LeaderboardComponent implements OnInit {
  leaderboardType: LeaderboardType = 'APPLICANTS';
  category: LeaderboardCategory | 'ALL' = 'ALL';
  days = 30;
  limit = 20;

  readonly categories: Array<{ label: string; value: LeaderboardCategory | 'ALL' }> = [
    { label: 'All categories', value: 'ALL' },
    { label: 'Software Development', value: 'Software Development' },
    { label: 'Networks and Systems', value: 'Networks and Systems' },
    { label: 'Cybersecurity', value: 'Cybersecurity' },
    { label: 'Data / Artificial Intelligence', value: 'Data / Artificial Intelligence' },
    { label: 'Cloud Computing', value: 'Cloud Computing' }
  ];

  loading = false;
  error = '';
  entries: LeaderboardEntry[] = [];
  generatedAt = '';

  constructor(private leaderboardService: LeaderboardService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.error = '';

    const category = this.category === 'ALL' ? undefined : this.category;

    const req = this.leaderboardType === 'APPLICANTS'
      ? this.leaderboardService.getApplicants(this.days, this.limit, category)
      : this.leaderboardService.getCreators(this.days, this.limit, category);

    req.subscribe({
      next: (res) => {
        this.entries = res.entries ?? [];
        this.generatedAt = res.generatedAt;
        this.loading = false;
      },
      error: () => {
        this.error = 'Unable to load leaderboard for now.';
        this.loading = false;
      }
    });
  }

  onTypeChange(value: string): void {
    this.leaderboardType = value as LeaderboardType;
    this.load();
  }

  onDaysChange(value: string): void {
    this.days = Number(value) || 30;
    this.load();
  }

  onLimitChange(value: string): void {
    this.limit = Number(value) || 20;
    this.load();
  }

  onCategoryChange(value: string): void {
    this.category = value as LeaderboardCategory | 'ALL';
    this.load();
  }
}
