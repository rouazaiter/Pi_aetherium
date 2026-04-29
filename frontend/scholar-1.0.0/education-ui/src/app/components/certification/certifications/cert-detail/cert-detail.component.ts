import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CertificationService } from '../../services/certification.service';
import {
  CertificationDetail, QuestionResponse,
  QuestionType, QuestionUpdateRequest, MatchPair
} from '../../models/certification.model';
import { ToastService } from '../../shared/toast.service';
import { ConfirmModalService } from '../../shared/confirm-modal/confirm-modal.service';
import { NgIf, NgFor, SlicePipe, CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

// All supported question types
const ALL_TYPES: QuestionType[] = [
  'MCQ', 'MULTI_SELECT', 'SCENARIO', 'CODE',
  'ORDERING', 'DRAG_DROP', 'MATCH',
  'FILL_BLANK', 'EXPLAIN', 'WRITE'
];

const TYPE_LABELS: Record<string, string> = {
  MCQ: 'Multiple Choice', MULTI_SELECT: 'Multi-Select',
  SCENARIO: 'Scenario', CODE: 'Code',
  ORDERING: 'Ordering', DRAG_DROP: 'Drag & Drop', MATCH: 'Match',
  FILL_BLANK: 'Fill Blank', EXPLAIN: 'Explain', WRITE: 'Write'
};

// Types that have A/B/C/D options
const OPTION_TYPES: QuestionType[] = ['MCQ', 'MULTI_SELECT', 'SCENARIO', 'CODE', 'ORDERING'];

@Component({
    selector: 'app-cert-detail',
    templateUrl: './cert-detail.component.html',
    styleUrls: ['./cert-detail.component.scss'],
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [NgIf, NgFor, RouterLink, FormsModule, SlicePipe, CurrencyPipe]
})
export class CertDetailComponent implements OnInit {
  cert: CertificationDetail | null = null;
  loading = true;
  error = '';

  // Edit modal state
  editingQ: QuestionResponse | null = null;
  editForm: {
    type: QuestionType;
    questionText: string;
    expectedAnswer: string;
    codeLanguage: string;
    points: number;
    orderIndex: number;
    matchPairs: MatchPair[];
    options: string[];          // the A/B/C/D option texts
  } = this.blankForm();

  saving    = false;
  editError = '';

  readonly questionTypes = ALL_TYPES;
  readonly typeLabels    = TYPE_LABELS;

  constructor(
    private route: ActivatedRoute,
    private certService: CertificationService,
    private toast: ToastService,
    private confirm: ConfirmModalService
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loading = true;
    this.certService.getDetail(id).subscribe({
      next: data => { this.cert = data; this.loading = false; },
      error: () => { this.error = 'Certification not found'; this.loading = false; }
    });
  }

  get allQuestions(): QuestionResponse[] {
    return this.cert?.exams.flatMap(e => e.questions) ?? [];
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  hasOptions(type: QuestionType): boolean {
    return OPTION_TYPES.includes(type);
  }

  /** Letter prefix for option index: 0→A, 1→B … */
  optionLetter(i: number): string {
    return String.fromCharCode(65 + i);
  }

  /** For MCQ/SCENARIO/CODE: is this option the correct answer? */
  isCorrectSingle(i: number): boolean {
    return this.editForm.expectedAnswer.trim().toUpperCase() === this.optionLetter(i);
  }

  /** For MULTI_SELECT: is this option in the comma-separated answer? */
  isCorrectMulti(i: number): boolean {
    return this.editForm.expectedAnswer
      .split(',').map(s => s.trim().toUpperCase())
      .includes(this.optionLetter(i));
  }

  /** Toggle a letter in/out of the multi-select answer */
  toggleMultiAnswer(i: number): void {
    const letter = this.optionLetter(i);
    const current = this.editForm.expectedAnswer
      .split(',').map(s => s.trim().toUpperCase()).filter(Boolean);
    const idx = current.indexOf(letter);
    if (idx === -1) current.push(letter);
    else current.splice(idx, 1);
    this.editForm.expectedAnswer = current.sort().join(',');
  }

  /** Set single-answer correct option */
  setSingleAnswer(i: number): void {
    this.editForm.expectedAnswer = this.optionLetter(i);
  }

  addOption(): void {
    if (this.editForm.options.length < 8) {
      this.editForm.options.push('');
    }
  }

  removeOption(i: number): void {
    if (this.editForm.options.length > 2) {
      this.editForm.options.splice(i, 1);
    }
  }

  // ── Open / close edit ─────────────────────────────────────────────────────

  openEdit(q: QuestionResponse): void {
    this.editingQ  = q;
    this.editError = '';

    // Parse stored options (JSON array from backend)
    let opts: string[] = [];
    if (q.options && q.options.length > 0) {
      opts = [...q.options];
    }
    // Ensure at least 4 options for option-based types
    if (OPTION_TYPES.includes(q.type as QuestionType)) {
      while (opts.length < 4) opts.push('');
    }

    this.editForm = {
      type:           q.type as QuestionType,
      questionText:   q.questionText,
      expectedAnswer: q.expectedAnswer ?? '',
      codeLanguage:   q.codeLanguage ?? '',
      points:         q.points,
      orderIndex:     q.orderIndex,
      matchPairs:     q.choices.map(c => ({ left: c.matchLeft, right: c.matchRight })),
      options:        opts
    };

    // Ensure at least 4 match pairs
    while (this.editForm.matchPairs.length < 4) {
      this.editForm.matchPairs.push({ left: '', right: '' });
    }
  }

  closeEdit(): void { this.editingQ = null; }

  onTypeChange(): void {
    const t = this.editForm.type;
    // Ensure options array is populated when switching to an option type
    if (OPTION_TYPES.includes(t) && this.editForm.options.length < 4) {
      while (this.editForm.options.length < 4) this.editForm.options.push('');
    }
    // Clear expected answer when switching types
    this.editForm.expectedAnswer = '';
  }

  saveEdit(): void {
    if (!this.editingQ) return;
    this.saving = true;
    this.editError = '';

    const hasOpts = OPTION_TYPES.includes(this.editForm.type);

    const dto: QuestionUpdateRequest = {
      type:           this.editForm.type,
      questionText:   this.editForm.questionText,
      expectedAnswer: this.editForm.expectedAnswer || null,
      codeLanguage:   this.editForm.codeLanguage   || null,
      points:         this.editForm.points,
      orderIndex:     this.editForm.orderIndex,
      matchPairs: (this.editForm.type === 'MATCH' || this.editForm.type === 'DRAG_DROP')
        ? this.editForm.matchPairs.filter(p => p.left && p.right)
        : null,
      options: hasOpts
        ? this.editForm.options.filter(o => o.trim().length > 0)
        : null
    };

    this.certService.updateQuestion(this.editingQ.id, dto).subscribe({
      next: updated => {
        this.cert!.exams.forEach(exam => {
          const idx = exam.questions.findIndex(q => q.id === updated.id);
          if (idx !== -1) exam.questions[idx] = updated;
        });
        this.saving   = false;
        this.editingQ = null;
        this.toast.success('Question saved');
      },
      error: e => { this.editError = e.message; this.saving = false; }
    });
  }

  // ── Delete ────────────────────────────────────────────────────────────────

  async deleteQuestion(q: QuestionResponse): Promise<void> {
    const preview = q.questionText.length > 60
      ? q.questionText.substring(0, 60) + '…' : q.questionText;
    const ok = await this.confirm.delete(preview);
    if (!ok) return;
    this.certService.deleteQuestion(q.id).subscribe({
      next: () => {
        this.cert!.exams.forEach(exam => {
          exam.questions = exam.questions.filter(eq => eq.id !== q.id);
        });
        this.toast.success('Question deleted');
      },
      error: () => this.toast.error('Failed to delete question')
    });
  }

  // ── Display helpers ───────────────────────────────────────────────────────

  difficultyBadge(d: string): string {
    return { BEGINNER: 'badge-info', INTERMEDIATE: 'badge-primary', ADVANCED: 'badge-danger' }[d] ?? 'badge-secondary';
  }

  statusBadge(s: string): string {
    return { DRAFT: 'badge-warning', PUBLISHED: 'badge-success', ARCHIVED: 'badge-secondary' }[s] ?? 'badge-secondary';
  }

  trackByIndex(i: number): number { return i; }

  private blankForm() {
    return {
      type: 'MCQ' as QuestionType,
      questionText: '', expectedAnswer: '', codeLanguage: '',
      points: 2, orderIndex: 1,
      matchPairs: [] as MatchPair[],
      options: ['', '', '', ''] as string[]
    };
  }
}
