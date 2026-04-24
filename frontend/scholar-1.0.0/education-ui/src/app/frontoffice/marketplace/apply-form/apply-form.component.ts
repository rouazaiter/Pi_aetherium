import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ServiceRequestService } from '../../../core/services/service-request.service';
import { ApplicationService } from '../../../core/services/application.service';
import { ServiceRequest } from '../../../core/models/service-request.model';
import { CurrentUserService } from '../../../core/auth/current-user.service';
import { MeetingSchedulerService } from '../../../core/services/meeting-scheduler.service';
import { AiCoachService } from '../../../core/services/ai-coach.service';
import { AiCoachPreviewResponse } from '../../../core/models/ai-coach.model';
import { catchError, forkJoin, of } from 'rxjs';
import { LeaderboardService } from '../../../core/services/leaderboard.service';

interface RecommendedServiceRequest extends ServiceRequest {
  recommendationScore: number;
  recommendationReason: string;
}

const RECOMMENDATION_STOP_WORDS = new Set([
  'a', 'an', 'the', 'and', 'or', 'for', 'to', 'in', 'on', 'of', 'with', 'at', 'by', 'from', 'is', 'are', 'be',
  'de', 'la', 'le', 'les', 'des', 'du', 'un', 'une', 'et', 'ou', 'pour', 'dans', 'sur', 'avec', 'par', 'est', 'sont'
]);

@Component({
  selector: 'app-apply-form',
  templateUrl: './apply-form.component.html',
  styleUrls: ['./apply-form.component.css']
})
export class ApplyFormComponent implements OnInit {
  serviceRequest?: ServiceRequest;
  form!: FormGroup;
  loading = false;
  error = '';
  success = '';
  alreadyApplied = false;
  currentUserId = 0;
  availableSlots: string[] = [];
  aiLoading = false;
  aiError = '';
  aiPreview?: AiCoachPreviewResponse;
  aiOriginalText = '';
  recommendationsLoading = false;
  recommendationsError = '';
  recommendedRequests: RecommendedServiceRequest[] = [];

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private srService: ServiceRequestService,
    private appService: ApplicationService,
    private currentUserService: CurrentUserService,
    private meetingSchedulerService: MeetingSchedulerService,
    private aiCoachService: AiCoachService,
    private leaderboardService: LeaderboardService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.params['id']);

    this.form = this.fb.group({
      message: ['', [Validators.required, Validators.maxLength(2000)]],
      meetingSlot: ['', Validators.required],
      calendlyEventUrl: ['', [Validators.required, Validators.maxLength(500), Validators.pattern(/^https?:\/\/.+/i)]]
    });

    this.currentUserService.currentUser$.subscribe(user => {
      if (user.id <= 0) {
        return;
      }

      this.currentUserId = user.id;

      // Load the request
      this.srService.getById(id, this.currentUserId).subscribe({
        next: (sr) => {
          // If this is my own request, do not allow applying
          if (sr.creator.id === this.currentUserId) {
            this.router.navigate(['/marketplace']);
            return;
          }
          this.serviceRequest = sr;
          this.loadRecommendations(sr);

          this.meetingSchedulerService.getConfig(sr.id).subscribe({
            next: (schedulingConfig) => {
              this.availableSlots = schedulingConfig.availableSlots ?? [];
            }
          });

          // Check whether user already applied
          this.appService.hasApplied(id, this.currentUserId).subscribe({
            next: (res) => this.alreadyApplied = res.hasApplied
          });
        },
        error: () => this.router.navigate(['/marketplace'])
      });
    });
  }

  onSubmit(): void {
    if (this.form.invalid || !this.serviceRequest) return;
    if (!this.validateMeetingChoice()) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.appService.apply(this.currentUserId, this.serviceRequest.id, this.form.value.message).subscribe({
      next: (application) => {
        this.meetingSchedulerService.reserveSlot(
          application.id,
          this.currentUserId,
          'SLOTS',
          this.form.value.meetingSlot,
          this.form.value.calendlyEventUrl
        ).subscribe({
          next: () => {
            this.success = 'Application submitted successfully.';
            this.alreadyApplied = true;
            this.loading = false;
          },
          error: (err) => {
            this.error = err?.error?.message || 'Application submitted, but meeting reservation failed.';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.error = err?.error?.message || 'An error occurred while submitting your application.';
        this.loading = false;
      }
    });
  }

  onImproveWithAi(): void {
    this.aiError = '';
    this.aiPreview = undefined;

    if (!this.serviceRequest) {
      return;
    }

    const currentMessage = (this.form.get('message')?.value || '').trim();
    if (!currentMessage) {
      this.aiError = 'Please write your message first, then use AI improvement.';
      this.form.get('message')?.markAsTouched();
      return;
    }

    this.aiLoading = true;

    this.aiCoachService.preview({
      serviceRequestId: this.serviceRequest.id,
      originalText: currentMessage,
      tone: 'professional',
      language: 'en'
    }).subscribe({
      next: (preview) => {
        this.aiOriginalText = currentMessage;
        this.aiPreview = preview;
        this.aiLoading = false;
      },
      error: (err) => {
        this.aiError = err?.error?.message || 'AI improvement is currently unavailable.';
        this.aiLoading = false;
      }
    });
  }

  applyAiSuggestion(): void {
    if (!this.aiPreview) {
      return;
    }
    this.form.patchValue({ message: this.aiPreview.improvedText });
    this.form.get('message')?.markAsDirty();
    this.form.get('message')?.markAsTouched();
  }

  clearAiPreview(): void {
    this.aiPreview = undefined;
    this.aiOriginalText = '';
    this.aiError = '';
  }

  trackByRequestId(_: number, request: RecommendedServiceRequest): number {
    return request.id;
  }

  hasSchedulingOptions(): boolean {
    return this.availableSlots.length > 0;
  }

  private loadRecommendations(currentRequest: ServiceRequest): void {
    this.recommendationsLoading = true;
    this.recommendationsError = '';

    forkJoin({
      requests: this.srService.getAll(this.currentUserId).pipe(catchError(() => of([] as ServiceRequest[]))),
      creators: this.leaderboardService.getCreators(90, 50, currentRequest.category).pipe(
        catchError(() => of({ entries: [] } as any))
      )
    }).subscribe({
      next: ({ requests, creators }) => {
        const creatorEntries = creators.entries ?? [];
        const topScore = creatorEntries.reduce((highest: number, entry: { score: number }) => Math.max(highest, entry.score || 0), 0);
        const creatorScoreById = new Map<number, { score: number; rank: number }>();

        for (const entry of creatorEntries) {
          creatorScoreById.set(entry.userId, { score: entry.score, rank: entry.rank });
        }

        this.recommendedRequests = requests
          .filter(request => request.id !== currentRequest.id)
          .filter(request => request.status === 'OPEN')
          .filter(request => request.category === currentRequest.category)
          .filter(request => request.creator?.id !== this.currentUserId)
          .map(request => {
            const creatorStats = creatorScoreById.get(request.creator?.id ?? -1);
            const creatorScore = creatorStats && topScore > 0 ? creatorStats.score / topScore : 0;
            const contentScore = this.computeContentSimilarity(currentRequest, request);
            const recencyScore = this.computeRecencyScore(request.createdAt);
            const recommendationScore = this.round2((creatorScore * 0.55) + (contentScore * 0.3) + (recencyScore * 0.15));

            const reasonParts: string[] = [];
            if (creatorStats) {
              reasonParts.push(`creator ranked #${creatorStats.rank}`);
            }
            if (contentScore >= 0.35) {
              reasonParts.push('strong content overlap');
            }
            if (recencyScore >= 0.6) {
              reasonParts.push('recent posting');
            }

            return {
              ...request,
              recommendationScore,
              recommendationReason: reasonParts.length > 0 ? reasonParts.join(' • ') : 'same category match'
            };
          })
          .sort((a, b) => b.recommendationScore - a.recommendationScore)
          .slice(0, 3);

        this.recommendationsLoading = false;
      },
      error: () => {
        this.recommendationsError = 'Recommendations are temporarily unavailable.';
        this.recommendationsLoading = false;
      }
    });
  }

  private computeContentSimilarity(source: ServiceRequest, target: ServiceRequest): number {
    const sourceTokens = this.extractKeywords(`${source.name} ${source.description || ''}`);
    const targetTokens = this.extractKeywords(`${target.name} ${target.description || ''}`);

    if (!sourceTokens.length || !targetTokens.length) {
      return 0;
    }

    const targetSet = new Set(targetTokens);
    let intersection = 0;

    for (const token of sourceTokens) {
      if (targetSet.has(token)) {
        intersection++;
      }
    }

    const union = new Set([...sourceTokens, ...targetTokens]).size;
    return union === 0 ? 0 : intersection / union;
  }

  private computeRecencyScore(createdAt: string): number {
    const createdDate = new Date(createdAt).getTime();
    if (Number.isNaN(createdDate)) {
      return 0;
    }

    const ageInDays = (Date.now() - createdDate) / (1000 * 60 * 60 * 24);
    return Math.max(0, 1 - Math.min(ageInDays, 90) / 90);
  }

  private extractKeywords(text: string): string[] {
    return (text || '')
      .toLowerCase()
      .split(/[^a-z0-9]+/)
      .map(token => token.trim())
      .filter(token => token.length > 2 && !RECOMMENDATION_STOP_WORDS.has(token));
  }

  private round2(value: number): number {
    return Math.round(value * 100) / 100;
  }

  private validateMeetingChoice(): boolean {
    if (!this.availableSlots.length) {
      this.error = 'No available slots are configured for this request yet.';
      return false;
    }

    if (!this.form.value.meetingSlot) {
      this.error = 'Please choose one available meeting slot.';
      return false;
    }

    if (!this.form.value.calendlyEventUrl?.trim()) {
      this.error = 'Please provide your Calendly link.';
      return false;
    }

    return true;
  }
}
