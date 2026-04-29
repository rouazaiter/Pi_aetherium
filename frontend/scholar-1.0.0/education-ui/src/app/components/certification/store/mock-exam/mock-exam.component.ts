import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { GradingService, GradeResponse } from '../../services/grading.service';
import { CertificationDetail, QuestionResponse } from '../../models/certification.model';
import { NgIf, NgFor, SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface QuestionResult {
  questionId: number;
  given: string;
  correct: string;
  isCorrect: boolean;
  feedback?: string;    // LLM feedback for open-ended questions
  llmScore?: number;    // 0-100 from LLM
  llmGraded?: boolean;  // true if graded by LLM
}

@Component({
    selector: 'app-mock-exam',
    templateUrl: './mock-exam.component.html',
    styleUrls: ['./mock-exam.component.scss'],
    standalone: true,
    imports: [NgIf, NgFor, FormsModule, SlicePipe]
})
export class MockExamComponent implements OnInit, OnDestroy {

  cert: CertificationDetail | null = null;
  questions: QuestionResponse[] = [];

  /** Current answer being typed/selected — not locked yet */
  answers: Map<number, string> = new Map();

  /** Questions that have been checked (locked) */
  checked: Set<number> = new Set();

  /** Per-question result after checking */
  results: Map<number, QuestionResult> = new Map();

  currentIndex = 0;
  loading = true;
  generatingQuestions = false;  // separate state for LLM generation
  error = '';
  certId!: number;

  // Session summary
  submitted = false;
  grading = false;  // true while waiting for LLM grading response

  // Timer — relaxed, no auto-submit
  timeLeft = 0;
  private timerInterval: any;
  get timerDisplay(): string {
    const m = Math.floor(this.timeLeft / 60);
    const s = this.timeLeft % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }
  get timerWarning(): boolean  { return this.timeLeft <= 300 && this.timeLeft > 60; }
  get timerCritical(): boolean { return this.timeLeft <= 60; }

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enrollService: EnrollmentService,
    private gradingService: GradingService
  ) {}

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.certId = Number(this.route.snapshot.paramMap.get('id'));
    const user = this.enrollService.getUser();
    if (!user) { this.router.navigate(['/skillhub/store', this.certId]); return; }

    this.enrollService.getStoreDetail(this.certId).subscribe({
      next: cert => {
        this.cert = cert;
        // Relaxed timer: 1.5× real limit, capped at 90 min
        const realMin = cert.exams[0]?.timeLimit ?? cert.durationMinutes ?? 60;
        this.timeLeft = Math.min(Math.round(realMin * 1.5), 90) * 60;
        // Keep loading=true until questions are ready
        this.loadPracticeQuestions();
      },
      error: () => { this.error = 'Failed to load practice exam'; this.loading = false; }
    });
  }

  private loadPracticeQuestions(): void {
    this.generatingQuestions = true;
    // Match the real exam: same number of questions, same time limit
    const realCount = this.cert?.exams.flatMap(e => e.questions).length ?? 10;
    const realMin   = this.cert?.exams[0]?.timeLimit ?? this.cert?.durationMinutes ?? 60;

    this.enrollService.getPracticeQuestions(this.certId, realCount).subscribe({
      next: (qs: QuestionResponse[]) => {
        this.questions = qs;
        this.generatingQuestions = false;
        this.loading = false;
        // Use exact real exam time limit
        this.timeLeft = realMin * 60;
        this.startTimer();
      },
      error: () => {
        // Fallback: use shuffled real questions if LLM fails
        if (this.cert) {
          const all = this.cert.exams.flatMap(e => e.questions)
            .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
          this.questions = this.shuffle(all).slice(0, realCount);
          this.timeLeft = realMin * 60;
          this.startTimer();
        }
        this.generatingQuestions = false;
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void { this.stopTimer(); }

  // ── Timer ─────────────────────────────────────────────────────────────────

  private startTimer(): void {
    this.timerInterval = setInterval(() => {
      if (this.timeLeft > 0) this.timeLeft--;
      // No auto-submit — just stop the clock
      else this.stopTimer();
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerInterval) { clearInterval(this.timerInterval); this.timerInterval = null; }
  }

  // ── Navigation ────────────────────────────────────────────────────────────

  get current(): QuestionResponse { return this.questions[this.currentIndex]; }
  get progress(): number {
    return this.questions.length ? ((this.currentIndex + 1) / this.questions.length) * 100 : 0;
  }
  get realQuestionCount(): number {
    return this.cert?.exams.flatMap(e => e.questions).length ?? 10;
  }
  get checkedCount(): number { return this.checked.size; }
  get correctCount(): number { return [...this.results.values()].filter(r => r.isCorrect).length; }

  isChecked(qId: number): boolean { return this.checked.has(qId); }
  getResult(qId: number): QuestionResult | undefined { return this.results.get(qId); }

  goTo(i: number): void {
    // Free navigation in warm-up — no restrictions
    if (i >= 0 && i < this.questions.length) this.currentIndex = i;
  }

  next(): void {
    if (this.currentIndex < this.questions.length - 1) this.currentIndex++;
  }

  prev(): void {
    if (this.currentIndex > 0) this.currentIndex--;
  }

  // ── Answers (before checking) ─────────────────────────────────────────────

  setAnswer(value: string): void {
    if (this.isChecked(this.current.id)) return; // locked after check
    this.answers.set(this.current.id, value);
  }

  getAnswer(qId: number): string { return this.answers.get(qId) ?? ''; }

  toggleMultiSelect(qId: number, option: string): void {
    if (this.isChecked(qId)) return;
    const selected = this.getAnswer(qId) ? this.getAnswer(qId).split(',').filter(s => s) : [];
    const letter = option.charAt(0);
    const idx = selected.indexOf(letter);
    if (idx === -1) selected.push(letter); else selected.splice(idx, 1);
    this.answers.set(qId, selected.sort().join(','));
  }

  isMultiSelected(qId: number, option: string): boolean {
    return this.getAnswer(qId).split(',').includes(option.charAt(0));
  }

  moveOrderItem(qId: number, options: string[], fromIdx: number, dir: -1 | 1): void {
    if (this.isChecked(qId)) return;
    const toIdx = fromIdx + dir;
    if (toIdx < 0 || toIdx >= options.length) return;
    const order = this.getOrderArray(qId, options);
    [order[fromIdx], order[toIdx]] = [order[toIdx], order[fromIdx]];
    this.answers.set(qId, order.join(','));
  }

  getOrderArray(qId: number, options: string[]): number[] {
    const saved = this.getAnswer(qId);
    if (saved) return saved.split(',').map(Number);
    return options.map((_, i) => i);
  }

  getOrderedOptions(qId: number, options: string[]): string[] {
    return this.getOrderArray(qId, options).map(i => options[i]);
  }

  // ── Check answer ──────────────────────────────────────────────────────────

  /** Types that need LLM grading (open-ended, no single correct string) */
  private readonly LLM_GRADED_TYPES = new Set(['EXPLAIN', 'WRITE', 'CODE']);

  checkAnswer(): void {
    const q = this.current;
    const given = this.getAnswer(q.id);

    if (this.LLM_GRADED_TYPES.has(q.type)) {
      // CODE with options = MCQ-style → grade locally, not via LLM
      if (q.type === 'CODE' && q.options && q.options.length > 0) {
        const correct = q.expectedAnswer ?? '';
        const isCorrect = given.trim().toUpperCase() === correct.trim().toUpperCase();
        this.checked.add(q.id);
        this.results.set(q.id, { questionId: q.id, given, correct, isCorrect, llmGraded: false });
        return;
      }

      // ── LLM grading path ──────────────────────────────────────────────────
      this.grading = true;
      this.gradingService.evaluate({
        questionType:   q.type,
        questionText:   q.questionText,
        expectedAnswer: q.expectedAnswer ?? null,
        userAnswer:     given,
        codeLanguage:   q.codeLanguage ?? null
      }).subscribe({
        next: (res: GradeResponse) => {
          this.grading = false;
          this.checked.add(q.id);
          this.results.set(q.id, {
            questionId: q.id,
            given,
            correct:    res.modelAnswer ?? q.expectedAnswer ?? '',
            isCorrect:  res.correct,
            feedback:   res.feedback,
            llmScore:   res.score,
            llmGraded:  true
          });
        },
        error: () => {
          // Fallback: if LLM grading fails, mark as pending review
          this.grading = false;
          this.checked.add(q.id);
          this.results.set(q.id, {
            questionId: q.id,
            given,
            correct:    q.expectedAnswer ?? '',
            isCorrect:  false,
            feedback:   'Could not grade automatically — please review the model answer.',
            llmGraded:  true
          });
        }
      });
    } else {
      // ── Local grading path (MCQ, FILL_BLANK, ORDERING, etc.) ─────────────
      const correct = q.expectedAnswer ?? '';
      const isCorrect = this.evaluateLocally(given, correct, q);
      this.checked.add(q.id);
      this.results.set(q.id, {
        questionId: q.id,
        given,
        correct,
        isCorrect,
        llmGraded: false
      });
    }
  }

  private evaluateLocally(given: string, correct: string, q: QuestionResponse): boolean {
    if (!given.trim()) return false;
    const type = q.type;
    if (type === 'MCQ' || type === 'SCENARIO') {
      return given.trim().toUpperCase() === correct.trim().toUpperCase();
    }
    if (type === 'MULTI_SELECT') {
      const g = given.split(',').map(s => s.trim()).sort().join(',');
      const c = correct.split(',').map(s => s.trim()).sort().join(',');
      return g === c;
    }
    if (type === 'FILL_BLANK') {
      return given.trim().toLowerCase() === correct.trim().toLowerCase();
    }
    if (type === 'ORDERING') {
      return given.trim() === correct.trim();
    }
    // MATCH / DRAG_DROP — check each pair against the correct matchRight
    if (type === 'MATCH' || type === 'DRAG_DROP') {
      if (!q.choices || q.choices.length === 0) return false;
      try {
        const parsed = JSON.parse(given || '{}');
        // All pairs must be correct
        return q.choices.every(c => {
          const selected = String(parsed[c.id] ?? '').trim().toLowerCase();
          return selected === c.matchRight.trim().toLowerCase();
        });
      } catch { return false; }
    }
    return false;
  }

  // Option state helpers (after checking)
  isOptionCorrect(option: string, q: QuestionResponse): boolean {
    if (!this.isChecked(q.id) || !q.expectedAnswer) return false;
    return q.expectedAnswer.split(',').map(s => s.trim()).includes(option.charAt(0));
  }

  isOptionWrong(option: string, q: QuestionResponse): boolean {
    if (!this.isChecked(q.id)) return false;
    const given = this.getAnswer(q.id);
    const letter = option.charAt(0);
    return given.split(',').includes(letter) && !this.isOptionCorrect(option, q);
  }

  // ── Finish session ────────────────────────────────────────────────────────

  finishMock(): void {
    this.stopTimer();
    this.submitted = true;
  }

  get mockScore(): number {
    if (!this.checkedCount) return 0;
    return Math.round((this.correctCount / this.questions.length) * 100);
  }

  get mockPassed(): boolean {
    return this.mockScore >= (this.cert?.passingScore ?? 70);
  }

  goToRealExam(): void { this.router.navigate(['/skillhub/store', this.certId, 'exam']); }
  goBack():       void { this.router.navigate(['/skillhub/store', this.certId]); }

  retryMock(): void {
    this.submitted = false;
    this.answers.clear();
    this.checked.clear();
    this.results.clear();
    this.currentIndex = 0;
    this.loading = true;
    this.stopTimer();
    // Reload fresh practice questions
    this.loadPracticeQuestions();
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  private shuffle<T>(arr: T[]): T[] {
    const a = [...arr];
    for (let i = a.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }

  get hasAnswer(): boolean {
    const q = this.current;
    const raw = this.getAnswer(q.id);
    if (q.type === 'DRAG_DROP' || q.type === 'MATCH') {
      if (!q.choices || q.choices.length === 0) return false;
      try {
        const parsed = JSON.parse(raw || '{}');
        // Require every pair to have a selection
        return q.choices.every(c => String(parsed[c.id] ?? '').trim().length > 0);
      } catch { return false; }
    }
    return raw.trim().length > 0;
  }

  // ── Match / Drag-drop helpers ─────────────────────────────────────────────

  /** Returns the shuffled right-side options for a MATCH/DRAG_DROP question */
  getMatchOptions(q: QuestionResponse): string[] {
    if (!q.choices || q.choices.length === 0) return [];
    // Shuffle the right-side values so user can't just pick in order
    const rights = q.choices.map(c => c.matchRight);
    return this.shuffleOnce(q.id, rights);
  }

  /** Stable shuffle per question id — same order every render */
  private shuffleOnce(seed: number, arr: string[]): string[] {
    const a = [...arr];
    let s = seed;
    for (let i = a.length - 1; i > 0; i--) {
      s = (s * 1664525 + 1013904223) & 0xffffffff;
      const j = Math.abs(s) % (i + 1);
      [a[i], a[j]] = [a[j], a[i]];
    }
    return a;
  }

  getMatchValue(qId: number, choiceId: number): string {
    try {
      const parsed = JSON.parse(this.getAnswer(qId) || '{}');
      return parsed[choiceId] ?? '';
    } catch { return ''; }
  }

  setMatchAnswer(qId: number, choiceId: number, value: string): void {
    if (this.isChecked(qId)) return;
    let current: Record<number, string> = {};
    try { current = JSON.parse(this.getAnswer(qId) || '{}'); } catch {}
    current[choiceId] = value;
    this.answers.set(qId, JSON.stringify(current));
  }

  /** True if a right-side option is already selected for another pair */
  isMatchOptionUsed(qId: number, value: string, excludeChoiceId: number): boolean {
    try {
      const parsed = JSON.parse(this.getAnswer(qId) || '{}');
      return Object.entries(parsed).some(
        ([k, v]) => Number(k) !== excludeChoiceId && v === value
      );
    } catch { return false; }
  }

  /** After checking: is this pair correct? */
  isPairCorrect(q: QuestionResponse, choiceId: number): boolean {
    if (!this.isChecked(q.id)) return false;
    const choice = q.choices.find(c => c.id === choiceId);
    if (!choice) return false;
    const selected = this.getMatchValue(q.id, choiceId);
    return selected.trim().toLowerCase() === choice.matchRight.trim().toLowerCase();
  }

  buildMatchAnswer(q: QuestionResponse, value: string, choiceId: number): string {
    let current: Record<number, string> = {};
    try { current = JSON.parse(this.answers.get(q.id) ?? '{}'); } catch {}
    current[choiceId] = value;
    return JSON.stringify(current);
  }

  getPrevId(i: number): number {
    return i > 0 ? this.questions[i - 1].id : -1;
  }

  typeLabel(type: string): string {
    const map: Record<string, string> = {
      MCQ: 'Multiple Choice', MULTI_SELECT: 'Multi-Select', SCENARIO: 'Scenario',
      CODE: 'Code Analysis', ORDERING: 'Ordering', DRAG_DROP: 'Drag & Drop',
      MATCH: 'Matching', FILL_BLANK: 'Fill in Blank', EXPLAIN: 'Explain', WRITE: 'Written'
    };
    return map[type] ?? type;
  }
}
