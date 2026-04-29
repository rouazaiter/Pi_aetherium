import {
  Component, Input, Output, EventEmitter, OnInit
} from '@angular/core';
import {
  FeedbackService, DifficultyRating, TimeRating, RelevanceRating
} from '../../services/feedback.service';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';

type Step = 1 | 2 | 3 | 4;

interface Option<T> {
  value: T;
  label: string;
  emoji: string;
  desc:  string;
}

@Component({
    selector: 'app-feedback-modal',
    templateUrl: './feedback-modal.component.html',
    styleUrls: ['./feedback-modal.component.scss'],
    standalone: true,
    imports: [NgIf, NgFor, FormsModule]
})
export class FeedbackModalComponent implements OnInit {

  @Input()  enrollmentId!: number;
  @Input()  certTitle     = '';
  @Input()  score         = 0;
  @Output() closed        = new EventEmitter<void>();
  @Output() submitted     = new EventEmitter<void>();

  step: Step = 1;
  submitting = false;
  error      = '';

  difficulty: DifficultyRating | null = null;
  time:       TimeRating        | null = null;
  relevance:  RelevanceRating   | null = null;
  comment     = '';

  readonly difficultyOptions: Option<DifficultyRating>[] = [
    { value: 'EASY',     emoji: '😌', label: 'Easy',     desc: 'Most questions felt straightforward' },
    { value: 'BALANCED', emoji: '⚖️', label: 'Balanced', desc: 'Good mix of easy and challenging' },
    { value: 'HARD',     emoji: '🔥', label: 'Hard',     desc: 'Many questions were very challenging' },
  ];

  readonly timeOptions: Option<TimeRating>[] = [
    { value: 'TOO_SHORT', emoji: '⏩', label: 'Too Short', desc: 'I ran out of time' },
    { value: 'ADEQUATE',  emoji: '✅', label: 'Adequate',  desc: 'Time was just right' },
    { value: 'TOO_LONG',  emoji: '⏳', label: 'Too Long',  desc: 'I had a lot of time left' },
  ];

  readonly relevanceOptions: Option<RelevanceRating>[] = [
    { value: 'YES',       emoji: '🎯', label: 'Yes',       desc: 'Questions matched the skills perfectly' },
    { value: 'PARTIALLY', emoji: '🔄', label: 'Partially', desc: 'Some questions felt off-topic' },
    { value: 'NO',        emoji: '❌', label: 'No',        desc: 'Questions didn\'t match the skills' },
  ];

  constructor(private feedbackService: FeedbackService) {}

  ngOnInit(): void {}

  // ── Navigation ────────────────────────────────────────────────────────────

  get canProceed(): boolean {
    if (this.step === 1) return this.difficulty !== null;
    if (this.step === 2) return this.time !== null;
    if (this.step === 3) return this.relevance !== null;
    return true;
  }

  next(): void {
    if (!this.canProceed) return;
    if (this.step < 4) this.step = (this.step + 1) as Step;
  }

  back(): void {
    if (this.step > 1) this.step = (this.step - 1) as Step;
  }

  skip(): void { this.closed.emit(); }

  // ── Submit ────────────────────────────────────────────────────────────────

  submit(): void {
    if (!this.difficulty || !this.time || !this.relevance) return;
    this.submitting = true;
    this.error = '';

    this.feedbackService.submit({
      enrollmentId:     this.enrollmentId,
      difficultyRating: this.difficulty,
      timeRating:       this.time,
      relevanceRating:  this.relevance,
      comment:          this.comment.trim() || undefined
    }).subscribe({
      next: () => {
        this.submitting = false;
        this.step = 4; // thank-you step
      },
      error: e => {
        this.submitting = false;
        this.error = e?.error?.message || 'Failed to submit feedback. Please try again.';
      }
    });
  }

  done(): void { this.submitted.emit(); }

  // ── Helpers ───────────────────────────────────────────────────────────────

  get progressPercent(): number {
    return ((this.step - 1) / 3) * 100;
  }

  get stepLabel(): string {
    const labels = ['', 'Difficulty', 'Time', 'Relevance', 'Done'];
    return labels[this.step];
  }
}
