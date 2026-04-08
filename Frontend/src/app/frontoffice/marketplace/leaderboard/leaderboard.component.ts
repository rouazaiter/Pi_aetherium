import { Component, OnInit } from '@angular/core';
import { LeaderboardEntry, LeaderboardType } from '../../../core/models/leaderboard.model';
import { LeaderboardService } from '../../../core/services/leaderboard.service';

@Component({
  selector: 'app-leaderboard',
  templateUrl: './leaderboard.component.html',
  styleUrls: ['./leaderboard.component.css']
})
export class LeaderboardComponent implements OnInit {
  leaderboardType: LeaderboardType = 'APPLICANTS';
  days = 30;
  limit = 20;

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

    const req = this.leaderboardType === 'APPLICANTS'
      ? this.leaderboardService.getApplicants(this.days, this.limit)
      : this.leaderboardService.getCreators(this.days, this.limit);

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
}
