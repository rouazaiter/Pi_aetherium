import { Component, OnInit } from '@angular/core';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { CurrentUserService } from '../../../core/auth/current-user.service';
import { LeaderboardService } from '../../../core/services/leaderboard.service';
import { LeaderboardEntry } from '../../../core/models/leaderboard.model';
import { catchError, forkJoin, of } from 'rxjs';

interface PopularServiceStat {
  name: string;
  count: number;
}

@Component({
  selector: 'app-service-request-list',
  templateUrl: './service-request-list.component.html',
  styleUrls: ['./service-request-list.component.css']
})
export class ServiceRequestListComponent implements OnInit {
  myRequests: ServiceRequest[] = [];
  otherRequests: ServiceRequest[] = [];
  filteredRequests: ServiceRequest[] = [];
  activeFilter: string = 'All';
  loading = false;
  error = '';
  currentUserId = 0;
  popularServices: PopularServiceStat[] = [];
  topApplicantOfWeek?: LeaderboardEntry;
  topCreatorOfWeek?: LeaderboardEntry;

  constructor(
    private srService: ServiceRequestService,
    private currentUserService: CurrentUserService,
    private leaderboardService: LeaderboardService
  ) {}

  ngOnInit(): void {
    this.currentUserService.currentUser$.subscribe(user => {
      if (user.id <= 0) {
        return;
      }
      this.currentUserId = user.id;
      this.load();
    });
    this.loadWeeklyHighlights();
  }

  load(): void {
    this.loading = true;
    this.srService.getAll(this.currentUserId).subscribe({
      next: (data) => {
        this.myRequests = data.filter(r => r.creator.id === this.currentUserId);
        this.otherRequests = data.filter(r => r.creator.id !== this.currentUserId);
        this.filteredRequests = this.otherRequests;
        this.popularServices = this.buildPopularServices(data);
        this.loading = false;
      },
      error: () => {
        this.error = 'Error loading requests.';
        this.loading = false;
      }
    });
  }

  filterBy(status: string): void {
    this.activeFilter = status;
    if (status === 'All') {
      this.filteredRequests = this.otherRequests;
    } else if (status === 'CLOSED') {
      this.filteredRequests = this.otherRequests.filter(r => r.status === 'CLOSED' || r.status === 'EXPIRED');
    } else {
      this.filteredRequests = this.otherRequests.filter(r => r.status === status);
    }
  }

  private loadWeeklyHighlights(): void {
    forkJoin({
      applicants: this.leaderboardService.getApplicants(7, 1).pipe(catchError(() => of({ entries: [] } as any))),
      creators: this.leaderboardService.getCreators(7, 1).pipe(catchError(() => of({ entries: [] } as any)))
    }).subscribe(({ applicants, creators }) => {
      this.topApplicantOfWeek = applicants.entries?.[0];
      this.topCreatorOfWeek = creators.entries?.[0];
    });
  }

  private buildPopularServices(requests: ServiceRequest[]): PopularServiceStat[] {
    const counts = new Map<string, PopularServiceStat>();

    for (const request of requests) {
      const displayName = (request.name || '').trim();
      if (!displayName) {
        continue;
      }

      const key = displayName.toLowerCase();
      const existing = counts.get(key);

      if (existing) {
        existing.count += 1;
      } else {
        counts.set(key, { name: displayName, count: 1 });
      }
    }

    return Array.from(counts.values())
      .sort((a, b) => b.count - a.count)
      .slice(0, 4);
  }
}
