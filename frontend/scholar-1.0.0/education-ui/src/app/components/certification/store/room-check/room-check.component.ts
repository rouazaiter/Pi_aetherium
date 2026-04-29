import {
  Component, OnInit, OnDestroy, ViewChild,
  ElementRef, Output, EventEmitter, NgZone, ChangeDetectorRef
} from '@angular/core';
import * as cocoSsd from '@tensorflow-models/coco-ssd';
import '@tensorflow/tfjs';
import { NgIf, NgFor } from '@angular/common';

export type CheckState =
  | 'loading_model' | 'loading_camera'
  | 'scan_360' | 'scan_failed'
  | 'confirm_candidate' | 'approved' | 'error';

/** A compact color histogram (8 bins per channel = 512 bins total) */
type Histogram = Float32Array;

@Component({
    selector: 'app-room-check',
    templateUrl: './room-check.component.html',
    styleUrls: ['./room-check.component.scss'],
    standalone: true,
    imports: [NgIf, NgFor]
})
export class RoomCheckComponent implements OnInit, OnDestroy {

  @ViewChild('videoEl',  { static: true }) videoRef!:  ElementRef<HTMLVideoElement>;
  @ViewChild('canvasEl', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  @Output() approved  = new EventEmitter<void>();
  @Output() cancelled = new EventEmitter<void>();

  state: CheckState = 'loading_model';
  statusMessage = '';
  personCount = 0;

  // ── Scan phase ────────────────────────────────────────────────────────────
  scanProgress = 0;
  /**
   * We require the user to show N *distinctly different* background scenes.
   * A scene is "distinct" when its histogram differs from ALL previously
   * accepted scenes by more than HIST_DIFF_THRESHOLD.
   */
  readonly SCENES_REQUIRED = 6;
  readonly HIST_DIFF_THRESHOLD = 0.18; // cosine distance threshold
  private acceptedScenes: Histogram[] = [];

  // Motion (used only for UI feedback — NOT for gating progress)
  motionLevel  = 0;   // 0-100
  isMoving     = false;

  // Confirm phase
  confirmSeconds = 0;
  readonly CONFIRM_REQUIRED = 3;

  // Lighting
  brightnessOk = true;

  private model: cocoSsd.ObjectDetection | null = null;
  private stream: MediaStream | null = null;
  private detectionInterval: any = null;
  private confirmStart: number | null = null;

  // Off-screen canvas for analysis
  private prevGray: Uint8Array | null = null;
  private offCanvas: HTMLCanvasElement | null = null;
  private offCtx: CanvasRenderingContext2D | null = null;
  private readonly OFF_W = 80;
  private readonly OFF_H = 60;

  // Throttle scene acceptance: only accept a new scene every 800ms
  private lastSceneAcceptedAt = 0;

  constructor(private ngZone: NgZone, private cdr: ChangeDetectorRef) {}

  ngOnInit():    void { this.init(); }
  ngOnDestroy(): void { this.stopEverything(); }
  retry():       void { this.init(); }

  // ── Init ──────────────────────────────────────────────────────────────────

  private async init(): Promise<void> {
    this.stopEverything();
    this.resetState();

    try {
      this.state = 'loading_model';
      this.statusMessage = 'Loading AI model…';
      this.cdr.detectChanges();
      this.model = await cocoSsd.load({ base: 'lite_mobilenet_v2' });
    } catch {
      this.state = 'error';
      this.statusMessage = 'Failed to load AI model. Check your connection and try again.';
      return;
    }

    try {
      this.state = 'loading_camera';
      this.statusMessage = 'Opening camera…';
      this.cdr.detectChanges();
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { width: { ideal: 640 }, height: { ideal: 480 }, facingMode: 'user' }
      });
    } catch (e: any) {
      this.state = 'error';
      this.statusMessage = e.name === 'NotAllowedError'
        ? '🚫 Camera access denied. Please allow camera access and try again.'
        : '📷 Could not open camera: ' + (e.message || 'Unknown error');
      return;
    }

    this.offCanvas = document.createElement('canvas');
    this.offCanvas.width  = this.OFF_W;
    this.offCanvas.height = this.OFF_H;
    this.offCtx = this.offCanvas.getContext('2d');

    await this.attachStream();
  }

  private attachStream(): Promise<void> {
    return new Promise(resolve => {
      const tryAttach = (n = 0) => {
        const video = this.videoRef?.nativeElement;
        if (!video) {
          if (n > 30) { this.state = 'error'; this.statusMessage = 'Video element not found.'; resolve(); return; }
          setTimeout(() => tryAttach(n + 1), 50);
          return;
        }
        video.srcObject = this.stream;
        video.muted = true;
        video.playsInline = true;
        video.onloadedmetadata = () => {
          video.play().then(() => {
            this.ngZone.run(() => {
              this.state = 'scan_360';
              this.statusMessage = 'Slowly pan your camera to show every corner of the room.';
              this.cdr.detectChanges();
              this.startDetection();
              resolve();
            });
          }).catch(() => { this.state = 'error'; this.statusMessage = 'Could not start video.'; resolve(); });
        };
      };
      setTimeout(() => tryAttach(), 0);
    });
  }

  // ── Detection loop ────────────────────────────────────────────────────────

  private startDetection(): void {
    this.detectionInterval = setInterval(() => this.detect(), 400);
  }

  private async detect(): Promise<void> {
    if (!this.model || !this.videoRef?.nativeElement) return;
    const video = this.videoRef.nativeElement;
    if (video.readyState < 2 || video.paused) return;

    try {
      const predictions = await this.model.detect(video);
      const people = predictions.filter(p => p.class === 'person' && p.score > 0.45);
      const { motion, brightness, frozen } = this.analyseFrame(video, people);

      this.ngZone.run(() => {
        this.personCount  = people.length;
        this.motionLevel  = motion;
        this.isMoving     = motion > 8;
        this.brightnessOk = brightness > 30 && !frozen;

        this.drawDetections(predictions, people.length);

        if (this.state === 'scan_360')           this.handleScan(people.length, frozen, brightness, video, people);
        else if (this.state === 'confirm_candidate') this.handleConfirm(people.length, frozen, brightness);
      });
    } catch { /* skip frame */ }
  }

  // ── Scan handler ──────────────────────────────────────────────────────────

  private handleScan(
    count: number,
    frozen: boolean,
    brightness: number,
    video: HTMLVideoElement,
    people: cocoSsd.DetectedObject[]
  ): void {
    if (count > 1) {
      this.state = 'scan_failed';
      this.statusMessage = `⚠️ ${count} people detected. Only the candidate should be present.`;
      return;
    }
    if (frozen)         { this.statusMessage = '⚠️ Camera appears frozen or covered. Please check your camera.'; return; }
    if (brightness < 30){ this.statusMessage = '💡 Room is too dark. Please improve lighting.'; return; }

    // ── Scene-based 360° tracking ──────────────────────────────────────────
    // Extract background histogram (exclude person bounding boxes)
    const now = Date.now();
    if (now - this.lastSceneAcceptedAt > 800) {
      const hist = this.computeBackgroundHistogram(video, people);
      if (hist && this.isDistinctScene(hist)) {
        this.acceptedScenes.push(hist);
        this.lastSceneAcceptedAt = now;
      }
    }

    this.scanProgress = Math.min((this.acceptedScenes.length / this.SCENES_REQUIRED) * 100, 100);

    if (this.acceptedScenes.length >= this.SCENES_REQUIRED) {
      this.state = 'confirm_candidate';
      this.confirmStart = null;
      this.confirmSeconds = 0;
      this.statusMessage = 'Room scan complete! Now face the camera directly.';
    } else {
      const remaining = this.SCENES_REQUIRED - this.acceptedScenes.length;
      this.statusMessage = this.isMoving
        ? `Scanning… ${remaining} more distinct area${remaining > 1 ? 's' : ''} needed — keep panning`
        : `⏸ Pan the camera! Rotate it to show different parts of the room.`;
    }
  }

  // ── Confirm handler ───────────────────────────────────────────────────────

  private handleConfirm(count: number, frozen: boolean, brightness: number): void {
    if (frozen)          { this.confirmStart = null; this.statusMessage = '⚠️ Camera frozen. Please check your camera.'; return; }
    if (brightness < 30) { this.confirmStart = null; this.statusMessage = '💡 Too dark. Please improve lighting.'; return; }

    if (count === 1) {
      if (!this.confirmStart) this.confirmStart = Date.now();
      const elapsed = (Date.now() - this.confirmStart) / 1000;
      this.confirmSeconds = Math.min(Math.floor(elapsed), this.CONFIRM_REQUIRED);
      if (elapsed >= this.CONFIRM_REQUIRED) {
        this.state = 'approved';
        this.statusMessage = '✅ Identity confirmed! Starting exam…';
        this.stopEverything();
        setTimeout(() => this.approved.emit(), 900);
      } else {
        this.statusMessage = `Confirming candidate… hold still (${this.confirmSeconds}/${this.CONFIRM_REQUIRED}s)`;
      }
    } else if (count === 0) {
      this.confirmStart = null; this.confirmSeconds = 0;
      this.statusMessage = 'No one detected — please face the camera directly.';
    } else {
      this.confirmStart = null; this.confirmSeconds = 0;
      this.statusMessage = `⚠️ ${count} people detected — only the candidate should be visible.`;
    }
  }

  // ── Histogram-based scene detection ──────────────────────────────────────

  /**
   * Compute a color histogram of the background (excluding person bounding boxes).
   * Uses 8 bins per channel (R, G, B) → 512-element histogram, L2-normalized.
   */
  private computeBackgroundHistogram(
    video: HTMLVideoElement,
    people: cocoSsd.DetectedObject[]
  ): Histogram | null {
    if (!this.offCtx || !this.offCanvas) return null;

    this.offCtx.drawImage(video, 0, 0, this.OFF_W, this.OFF_H);
    const frame = this.offCtx.getImageData(0, 0, this.OFF_W, this.OFF_H);

    // Scale person bboxes to off-screen canvas size
    const vw = video.videoWidth  || 640;
    const vh = video.videoHeight || 480;
    const scaleX = this.OFF_W / vw;
    const scaleY = this.OFF_H / vh;

    const personMask = new Uint8Array(this.OFF_W * this.OFF_H); // 1 = person pixel
    for (const p of people) {
      const [bx, by, bw, bh] = p.bbox;
      const x0 = Math.max(0, Math.floor(bx * scaleX));
      const y0 = Math.max(0, Math.floor(by * scaleY));
      const x1 = Math.min(this.OFF_W, Math.ceil((bx + bw) * scaleX));
      const y1 = Math.min(this.OFF_H, Math.ceil((by + bh) * scaleY));
      for (let y = y0; y < y1; y++)
        for (let x = x0; x < x1; x++)
          personMask[y * this.OFF_W + x] = 1;
    }

    const BINS = 8;
    const hist = new Float32Array(BINS * BINS * BINS);
    let pixelCount = 0;

    for (let i = 0; i < this.OFF_W * this.OFF_H; i++) {
      if (personMask[i]) continue; // skip person pixels
      const r = Math.floor(frame.data[i * 4]     / 32); // 0-7
      const g = Math.floor(frame.data[i * 4 + 1] / 32);
      const b = Math.floor(frame.data[i * 4 + 2] / 32);
      hist[r * BINS * BINS + g * BINS + b]++;
      pixelCount++;
    }

    if (pixelCount === 0) return null;

    // L2 normalize
    let norm = 0;
    for (let i = 0; i < hist.length; i++) norm += hist[i] * hist[i];
    norm = Math.sqrt(norm);
    if (norm > 0) for (let i = 0; i < hist.length; i++) hist[i] /= norm;

    return hist;
  }

  /**
   * Returns true if `hist` is sufficiently different from all accepted scenes.
   * Uses cosine distance: dist = 1 - dot(a, b)  (both are L2-normalized).
   */
  private isDistinctScene(hist: Histogram): boolean {
    for (const accepted of this.acceptedScenes) {
      let dot = 0;
      for (let i = 0; i < hist.length; i++) dot += hist[i] * accepted[i];
      const dist = 1 - dot; // cosine distance
      if (dist < this.HIST_DIFF_THRESHOLD) return false; // too similar
    }
    return true;
  }

  // ── Frame analysis (motion + brightness + frozen) ─────────────────────────

  private analyseFrame(
    video: HTMLVideoElement,
    _people: cocoSsd.DetectedObject[]
  ): { motion: number; brightness: number; frozen: boolean } {
    if (!this.offCtx || !this.offCanvas) return { motion: 0, brightness: 50, frozen: false };

    this.offCtx.drawImage(video, 0, 0, this.OFF_W, this.OFF_H);
    const frame = this.offCtx.getImageData(0, 0, this.OFF_W, this.OFF_H);
    const gray  = new Uint8Array(this.OFF_W * this.OFF_H);

    let totalBrightness = 0;
    for (let i = 0; i < gray.length; i++) {
      const r = frame.data[i * 4], g = frame.data[i * 4 + 1], b = frame.data[i * 4 + 2];
      gray[i] = Math.round(0.299 * r + 0.587 * g + 0.114 * b);
      totalBrightness += gray[i];
    }
    const brightness = totalBrightness / gray.length;

    let changed = 0;
    let frozen  = false;
    if (this.prevGray) {
      let diff = 0;
      for (let i = 0; i < gray.length; i++) diff += Math.abs(gray[i] - this.prevGray[i]);
      const avgDiff = diff / gray.length;
      changed = Math.min(avgDiff * 5, 100);
      frozen  = avgDiff < 0.5;
    }
    this.prevGray = gray;

    return { motion: changed, brightness, frozen };
  }

  // ── Canvas overlay ────────────────────────────────────────────────────────

  private drawDetections(predictions: cocoSsd.DetectedObject[], count: number): void {
    const canvas = this.canvasRef?.nativeElement;
    const video  = this.videoRef?.nativeElement;
    if (!canvas || !video) return;
    const vw = video.videoWidth  || 640;
    const vh = video.videoHeight || 480;
    canvas.width  = vw;
    canvas.height = vh;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.clearRect(0, 0, vw, vh);

    for (const pred of predictions) {
      if (pred.class !== 'person' || pred.score < 0.45) continue;
      const [x, y, w, h] = pred.bbox;
      const isExtra = count > 1;

      // ── Mirror the bounding box X coordinate to match the mirrored video ──
      // The video is CSS-mirrored (scaleX(-1)), so the box x in model space
      // maps to (vw - x - w) in display space.
      const mx = vw - x - w; // mirrored left edge

      ctx.strokeStyle = isExtra ? '#ef4444' : '#10b981';
      ctx.lineWidth   = 3;
      ctx.strokeRect(mx, y, w, h);
      ctx.fillStyle   = isExtra ? 'rgba(239,68,68,0.1)' : 'rgba(16,185,129,0.1)';
      ctx.fillRect(mx, y, w, h);

      // ── Draw label in normal (unmirrored) text ────────────────────────────
      const label = isExtra
        ? `⚠ extra ${Math.round(pred.score * 100)}%`
        : `✓ candidate ${Math.round(pred.score * 100)}%`;
      ctx.font = 'bold 13px Inter, sans-serif';
      const tw = ctx.measureText(label).width + 12;
      // Label background
      ctx.fillStyle = isExtra ? '#ef4444' : '#10b981';
      ctx.fillRect(mx, y - 24, tw, 24);
      // Label text — drawn normally (no transform needed since we compute mx directly)
      ctx.fillStyle = '#fff';
      ctx.fillText(label, mx + 6, y - 7);
    }

    if (!this.brightnessOk) {
      ctx.fillStyle = 'rgba(0,0,0,0.5)';
      ctx.fillRect(0, 0, vw, vh);
      ctx.fillStyle = '#fbbf24';
      ctx.font = 'bold 18px Inter, sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText('💡 Too dark — please improve lighting', vw / 2, vh / 2);
      ctx.textAlign = 'left';
    }
  }

  // ── Cleanup ───────────────────────────────────────────────────────────────

  private stopEverything(): void {
    if (this.detectionInterval) { clearInterval(this.detectionInterval); this.detectionInterval = null; }
    if (this.stream) { this.stream.getTracks().forEach(t => t.stop()); this.stream = null; }
  }

  private resetState(): void {
    this.scanProgress    = 0;
    this.acceptedScenes  = [];
    this.lastSceneAcceptedAt = 0;
    this.confirmSeconds  = 0;
    this.personCount     = 0;
    this.motionLevel     = 0;
    this.isMoving        = false;
    this.brightnessOk    = true;
    this.prevGray        = null;
    this.confirmStart    = null;
  }

  cancel(): void { this.stopEverything(); this.cancelled.emit(); }

  get confirmProgressPercent(): number { return (this.confirmSeconds / this.CONFIRM_REQUIRED) * 100; }
  get scenesFound(): number { return this.acceptedScenes.length; }
}
