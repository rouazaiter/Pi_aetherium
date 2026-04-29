import {
  Component, OnInit, OnDestroy, HostListener, NgZone
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EnrollmentService } from '../../services/enrollment.service';
import { CertificationDetail, QuestionResponse } from '../../models/certification.model';
import { AnswerDTO } from '../../models/enrollment.model';
import { ConfirmModalService } from '../../shared/confirm-modal/confirm-modal.service';
import { ToastService } from '../../shared/toast.service';
import { NgIf, NgFor } from '@angular/common';
import { RoomCheckComponent } from '../room-check/room-check.component';
import { FormsModule } from '@angular/forms';

@Component({
    selector: 'app-store-exam',
    templateUrl: './store-exam.component.html',
    styleUrls: ['./store-exam.component.scss'],
    standalone: true,
    imports: [NgIf, RoomCheckComponent, NgFor, FormsModule]
})
export class StoreExamComponent implements OnInit, OnDestroy {

  // ── Data ──────────────────────────────────────────────────────────────────
  cert: CertificationDetail | null = null;
  questions: QuestionResponse[] = [];
  answers: Map<number, string> = new Map();
  skipped: Set<number> = new Set();
  currentIndex = 0;
  loading = true;
  submitting = false;
  error = '';
  certId!: number;
  userName = '';

  // Room check gate
  roomCheckPassed = false;

  // ── Continuous proctoring ─────────────────────────────────────────────────
  private proctoringInterval: any = null;
  private proctoringStream: MediaStream | null = null;
  proctoringViolations = 0;
  private lastAnswerTime = Date.now();
  private inactivityCheckInterval: any = null;

  // ── Timer ─────────────────────────────────────────────────────────────────
  timeLeft = 0;
  private timerInterval: any = null;
  get timerMinutes(): number { return Math.floor(this.timeLeft / 60); }
  get timerSeconds(): number { return this.timeLeft % 60; }
  get timerDisplay(): string {
    return `${String(this.timerMinutes).padStart(2, '0')}:${String(this.timerSeconds).padStart(2, '0')}`;
  }
  get timerWarning(): boolean  { return this.timeLeft <= 300 && this.timeLeft > 60; }
  get timerCritical(): boolean { return this.timeLeft <= 60; }

  // ── Secure mode ───────────────────────────────────────────────────────────
  securityViolations = 0;
  showSecurityWarning = false;
  securityMessage = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private enrollService: EnrollmentService,
    private confirm: ConfirmModalService,
    private toast: ToastService,
    private ngZone: NgZone
  ) {}

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.certId = Number(this.route.snapshot.paramMap.get('id'));
    this.userName = this.enrollService.getUser();
    if (!this.userName) { this.router.navigate(['/store', this.certId]); return; }

    this.enrollService.getEnrollment(this.certId, this.userName).subscribe({
      next: enrollment => {
        if (!enrollment) { this.router.navigate(['/store', this.certId]); return; }
        if (enrollment.status === 'COMPLETED') {
          this.router.navigate(['/store', this.certId, 'result']); return;
        }
        this.loadExam();
      },
      error: () => this.router.navigate(['/store', this.certId])
    });
  }

  ngOnDestroy(): void {
    this.stopTimer();
    this.removeSecurityListeners();
    this.stopContinuousProctoring();
    this.stopInactivityCheck();
  }

  loadExam(): void {
    this.enrollService.getStoreDetail(this.certId).subscribe({
      next: cert => {
        this.cert = cert;
        this.questions = cert.exams.flatMap(e => e.questions)
          .sort((a, b) => (a.orderIndex ?? 0) - (b.orderIndex ?? 0));
        this.loading = false;
        const timeLimitMinutes = cert.exams[0]?.timeLimit ?? cert.durationMinutes ?? 60;
        this.timeLeft = timeLimitMinutes * 60;
      },
      error: () => { this.error = 'Failed to load exam'; this.loading = false; }
    });
  }

  /** Called by the room-check component when the room is verified clear. */
  onRoomApproved(): void {
    this.roomCheckPassed = true;
    this.lastAnswerTime = Date.now();
    this.startTimer();
    // No fullscreen — just attach security listeners and start proctoring
    this.attachSecurityListeners();
    this.startContinuousProctoring();
    this.startInactivityCheck();
  }

  // ── Continuous proctoring (every 5s) ──────────────────────────────────────

  private async startContinuousProctoring(): Promise<void> {
    try {
      this.proctoringStream = await navigator.mediaDevices.getUserMedia({
        video: { width: 320, height: 240, facingMode: 'user' }
      });

      // Show live feed in the proctor bar widget
      setTimeout(() => {
        const widget = document.getElementById('proctor-video') as HTMLVideoElement;
        if (widget && this.proctoringStream) {
          widget.srcObject = this.proctoringStream;
          widget.muted = true;
          widget.play().catch(() => {});
        }
      }, 200);

      // Use the visible proctor-video element for detection
      // (more reliable than a hidden video element)
      const cocoSsd = await import('@tensorflow-models/coco-ssd');
      await import('@tensorflow/tfjs');
      const model = await cocoSsd.load({ base: 'lite_mobilenet_v2' });

      // Off-screen canvas for frozen/lighting checks
      const offCanvas = document.createElement('canvas');
      offCanvas.width = 80;
      offCanvas.height = 60;
      const offCtx = offCanvas.getContext('2d');
      let prevGray: Uint8Array | null = null;

      // Grace counter: allow up to 2 consecutive missed detections before flagging
      let missedPersonFrames = 0;
      const MISSED_THRESHOLD = 2;

      this.proctoringInterval = setInterval(async () => {
        if (this.submitting) return;

        // Get the visible video element for detection
        const videoEl = document.getElementById('proctor-video') as HTMLVideoElement;
        if (!videoEl || videoEl.readyState < 2 || videoEl.paused) return;

        try {
          // ── Frozen/lighting check ──────────────────────────────────────
          if (offCtx) {
            offCtx.drawImage(videoEl, 0, 0, 80, 60);
            const frame = offCtx.getImageData(0, 0, 80, 60);
            const gray = new Uint8Array(80 * 60);
            let totalBrightness = 0;
            for (let i = 0; i < gray.length; i++) {
              const r = frame.data[i * 4], g = frame.data[i * 4 + 1], b = frame.data[i * 4 + 2];
              gray[i] = Math.round(0.299 * r + 0.587 * g + 0.114 * b);
              totalBrightness += gray[i];
            }
            const brightness = totalBrightness / gray.length;

            let frozen = false;
            if (prevGray) {
              let diff = 0;
              for (let i = 0; i < gray.length; i++) diff += Math.abs(gray[i] - prevGray[i]);
              frozen = (diff / gray.length) < 0.5;
            }
            prevGray = gray;

            if (brightness < 30) {
              this.ngZone.run(() => this.handleSecurityViolation('Proctoring: Room too dark or camera covered'));
              return;
            }
            if (frozen) {
              this.ngZone.run(() => this.handleSecurityViolation('Proctoring: Camera appears frozen or covered'));
              return;
            }
          }

          // ── Person detection — use lower threshold for better sensitivity ──
          const predictions = await model.detect(videoEl);
          const people = predictions.filter(p => p.class === 'person' && p.score > 0.45);

          if (people.length === 0) {
            missedPersonFrames++;
            if (missedPersonFrames >= MISSED_THRESHOLD) {
              missedPersonFrames = 0;
              this.ngZone.run(() => this.handleSecurityViolation('Proctoring: No person detected — please stay in frame'));
            }
            return;
          }

          // Person detected — reset miss counter
          missedPersonFrames = 0;

          if (people.length > 1) {
            this.ngZone.run(() => {
              this.proctoringViolations++;
              if (this.proctoringViolations === 1) {
                this.handleSecurityViolation(`Proctoring: ${people.length} people detected in camera`);
              } else {
                this.autoSubmit(`Exam auto-submitted: Multiple people detected by proctoring camera.`);
              }
            });
            return;
          }

          // ── Partial out-of-frame check ─────────────────────────────────
          const person = people[0];
          const [bx, by, bw, bh] = person.bbox;
          const vw = videoEl.videoWidth  || 320;
          const vh = videoEl.videoHeight || 240;
          const leftCut   = Math.max(0, -bx);
          const topCut    = Math.max(0, -by);
          const rightCut  = Math.max(0, (bx + bw) - vw);
          const bottomCut = Math.max(0, (by + bh) - vh);
          const totalCut  = leftCut + topCut + rightCut + bottomCut;
          const bboxArea  = bw * bh;
          if (bboxArea > 0 && totalCut / bboxArea > 0.20) {
            this.ngZone.run(() => this.handleSecurityViolation('Proctoring: You are partially out of frame — stay centered'));
          }

        } catch { /* skip frame on error */ }
      }, 5000);

    } catch {
      console.warn('[Proctoring] Could not start camera check');
    }
  }

  private stopContinuousProctoring(): void {
    if (this.proctoringInterval) { clearInterval(this.proctoringInterval); this.proctoringInterval = null; }
    if (this.proctoringStream)   { this.proctoringStream.getTracks().forEach(t => t.stop()); this.proctoringStream = null; }
  }

  // ── Inactivity detection ──────────────────────────────────────────────────

  private startInactivityCheck(): void {
    this.inactivityCheckInterval = setInterval(() => {
      const elapsed = (Date.now() - this.lastAnswerTime) / 1000 / 60; // minutes
      if (elapsed > 10) {
        this.ngZone.run(() => {
          this.handleSecurityViolation('Proctoring: No activity for 10+ minutes');
        });
      }
    }, 60000); // check every minute
  }

  private stopInactivityCheck(): void {
    if (this.inactivityCheckInterval) { clearInterval(this.inactivityCheckInterval); this.inactivityCheckInterval = null; }
  }

  // ── Timer ─────────────────────────────────────────────────────────────────

  private startTimer(): void {
    this.timerInterval = setInterval(() => {
      this.ngZone.run(() => {
        if (this.timeLeft <= 0) {
          this.stopTimer();
          this.autoSubmit('Time is up! Your exam has been submitted automatically.');
        } else {
          this.timeLeft--;
          if (this.timeLeft === 300) this.toast.info('⏱ 5 minutes remaining!');
          if (this.timeLeft === 60)  this.toast.error('⚠️ 1 minute remaining!');
        }
      });
    }, 1000);
  }

  private stopTimer(): void {
    if (this.timerInterval) { clearInterval(this.timerInterval); this.timerInterval = null; }
  }

  // ── Secure mode ───────────────────────────────────────────────────────────

  // Deduplication: ignore events within 2s of the last violation
  private lastViolationAt = 0;
  private readonly VIOLATION_COOLDOWN_MS = 2000;

  private attachSecurityListeners(): void {
    document.addEventListener('visibilitychange', this.onVisibilityChange);
    window.addEventListener('blur', this.onWindowBlur);
  }

  private removeSecurityListeners(): void {
    document.removeEventListener('visibilitychange', this.onVisibilityChange);
    window.removeEventListener('blur', this.onWindowBlur);
  }

  private onVisibilityChange = (): void => {
    if (document.hidden) {
      this.ngZone.run(() => this.handleSecurityViolation('Tab switch / window minimized detected'));
    }
  };

  private onWindowBlur = (): void => {
    // Only fire if visibilitychange didn't already fire within the cooldown window
    // (they both fire on tab switch — we only want to count it once)
    this.ngZone.run(() => this.handleSecurityViolation('Window lost focus (possible alt-tab)'));
  };

  private handleSecurityViolation(reason: string): void {
    if (this.submitting || this.loading) return;

    // Deduplicate: if another violation was recorded within the cooldown, skip
    const now = Date.now();
    if (now - this.lastViolationAt < this.VIOLATION_COOLDOWN_MS) return;
    this.lastViolationAt = now;

    this.securityViolations++;

    if (this.securityViolations === 1) {
      // First violation → show warning modal
      this.securityMessage = `⚠️ ${reason}.\n\nThis is your first and only warning. If you switch tabs or leave the exam again, your exam will be auto-submitted immediately.`;
      this.showSecurityWarning = true;
      this.toast.error(`⚠️ Warning: ${reason}`);
    } else {
      // Second violation → auto-submit
      this.showSecurityWarning = false;
      this.autoSubmit(`Exam auto-submitted: ${reason} (second violation).`);
    }
  }

  dismissSecurityWarning(): void {
    this.showSecurityWarning = false;
    // Reset the deduplication timer so the next violation is counted fresh
    this.lastViolationAt = 0;
  }

  private autoSubmit(reason: string): void {
    if (this.submitting) return;
    this.stopTimer();
    this.removeSecurityListeners();
    this.stopContinuousProctoring();
    this.stopInactivityCheck();
    this.toast.error(reason);
    this.doSubmit();
  }

  // ── Navigation ────────────────────────────────────────────────────────────

  get current(): QuestionResponse { return this.questions[this.currentIndex]; }
  get progress(): number { return this.questions.length ? ((this.currentIndex + 1) / this.questions.length) * 100 : 0; }
  get answeredCount(): number { return this.questions.filter(q => this.hasAnswerFor(q.id)).length; }
  get skippedCount(): number { return this.skipped.size; }

  hasAnswerFor(qId: number): boolean {
    const q = this.questions.find(q => q.id === qId);
    const raw = this.getAnswer(qId);
    if (q?.type === 'DRAG_DROP' || q?.type === 'MATCH') {
      if (!q.choices || q.choices.length === 0) return false;
      try {
        const parsed = JSON.parse(raw || '{}');
        return q.choices.every(c => String(parsed[c.id] ?? '').trim().length > 0);
      } catch { return false; }
    }
    return raw.trim().length > 0;
  }

  isSkipped(qId: number): boolean { return this.skipped.has(qId); }

  next(): void { if (this.currentIndex < this.questions.length - 1) this.currentIndex++; }
  prev(): void { if (this.currentIndex > 0) this.currentIndex--; }
  goTo(i: number): void { this.currentIndex = i; }

  skip(): void {
    this.skipped.add(this.current.id);
    const next = this.questions.findIndex(
      (q, i) => i > this.currentIndex && !this.hasAnswerFor(q.id) && !this.skipped.has(q.id)
    );
    if (next !== -1) this.currentIndex = next;
    else if (this.currentIndex < this.questions.length - 1) this.currentIndex++;
  }

  unskip(): void { this.skipped.delete(this.current.id); }

  // ── Answers ───────────────────────────────────────────────────────────────

  setAnswer(value: string): void {
    this.answers.set(this.current.id, value);
    this.skipped.delete(this.current.id);
    this.lastAnswerTime = Date.now(); // reset inactivity timer
  }

  getAnswer(qId: number): string { return this.answers.get(qId) ?? ''; }

  toggleMultiSelect(qId: number, option: string): void {
    const selected = this.getAnswer(qId) ? this.getAnswer(qId).split(',').filter(s => s) : [];
    const letter = option.charAt(0);
    const idx = selected.indexOf(letter);
    if (idx === -1) selected.push(letter); else selected.splice(idx, 1);
    this.answers.set(qId, selected.sort().join(','));
    this.skipped.delete(qId);
    this.lastAnswerTime = Date.now();
  }

  isMultiSelected(qId: number, option: string): boolean {
    return this.getAnswer(qId).split(',').includes(option.charAt(0));
  }

  moveOrderItem(qId: number, options: string[], fromIdx: number, dir: -1 | 1): void {
    const toIdx = fromIdx + dir;
    if (toIdx < 0 || toIdx >= options.length) return;
    const order = this.getOrderArray(qId, options);
    [order[fromIdx], order[toIdx]] = [order[toIdx], order[fromIdx]];
    this.answers.set(qId, order.join(','));
    this.skipped.delete(qId);
    this.lastAnswerTime = Date.now();
  }

  getOrderArray(qId: number, options: string[]): number[] {
    const saved = this.getAnswer(qId);
    if (saved) return saved.split(',').map(Number);
    return options.map((_, i) => i);
  }

  getOrderedOptions(qId: number, options: string[]): string[] {
    return this.getOrderArray(qId, options).map(i => options[i]);
  }

  // ── Match / Drag-drop ─────────────────────────────────────────────────────

  getMatchOptionsExam(q: QuestionResponse): string[] {
    if (!q.choices || q.choices.length === 0) return [];
    return this.shuffleOnce(q.id, q.choices.map(c => c.matchRight));
  }

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

  setMatchAnswerExam(qId: number, choiceId: number, value: string): void {
    let current: Record<number, string> = {};
    try { current = JSON.parse(this.getAnswer(qId) || '{}'); } catch {}
    current[choiceId] = value;
    this.answers.set(qId, JSON.stringify(current));
    this.skipped.delete(qId);
    this.lastAnswerTime = Date.now();
  }

  isMatchOptionUsedExam(qId: number, value: string, excludeChoiceId: number): boolean {
    try {
      const parsed = JSON.parse(this.getAnswer(qId) || '{}');
      return Object.entries(parsed).some(([k, v]) => Number(k) !== excludeChoiceId && v === value);
    } catch { return false; }
  }

  // ── Submit ────────────────────────────────────────────────────────────────

  async submit(): Promise<void> {
    const unanswered = this.questions.length - this.answeredCount;
    const ok = await this.confirm.confirm({
      title: 'Submit Exam',
      message: unanswered > 0
        ? `You have ${unanswered} unanswered question${unanswered > 1 ? 's' : ''}. Submit anyway? This cannot be undone.`
        : `You answered all ${this.questions.length} questions. Ready to submit?`,
      confirmLabel: 'Submit Exam',
      cancelLabel: 'Keep Reviewing',
      variant: unanswered > 0 ? 'warning' : 'info'
    });
    if (!ok) return;
    this.stopTimer();
    this.removeSecurityListeners();
    this.stopContinuousProctoring();
    this.stopInactivityCheck();
    this.doSubmit();
  }

  private doSubmit(): void {
    this.submitting = true;
    const answerList: AnswerDTO[] = this.questions.map(q => ({
      questionId: q.id,
      answer: this.answers.get(q.id) ?? ''
    }));
    this.enrollService.submitExam(this.certId, { userIdentifier: this.userName, answers: answerList }).subscribe({
      next: () => this.router.navigate(['/store', this.certId, 'result']),
      error: e => {
        this.error = e.message ?? 'Submission failed';
        this.toast.error(this.error);
        this.submitting = false;
      }
    });
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  typeLabel(type: string): string {
    const map: Record<string, string> = {
      MCQ: 'Multiple Choice', MULTI_SELECT: 'Multi-Select', SCENARIO: 'Scenario',
      CODE: 'Code Analysis', ORDERING: 'Ordering', DRAG_DROP: 'Drag & Drop',
      MATCH: 'Matching', FILL_BLANK: 'Fill in Blank', EXPLAIN: 'Explain', WRITE: 'Written'
    };
    return map[type] ?? type;
  }

  goBack(): void { this.router.navigate(['/store', this.certId]); }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.showSecurityWarning) this.dismissSecurityWarning();
  }
}
