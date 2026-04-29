import { Component, OnInit, ElementRef, ViewChild, HostListener } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault } from '@angular/common';

export interface VerificationResult {
  certificateId: string;
  valid: boolean;
  holderName: string | null;
  certificationTitle: string | null;
  category: string | null;
  difficulty: string | null;
  score: number;
  passed: boolean;
  issuedAt: string | null;
  issuedBy: string | null;
  issuerTitle: string | null;
  platform: string | null;
}

@Component({
    selector: 'app-verify',
    templateUrl: './verify.component.html',
    styleUrls: ['./verify.component.scss'],
    standalone: true,
    imports: [NgIf, NgSwitch, NgSwitchCase, NgSwitchDefault]
})
export class VerifyComponent implements OnInit {

  @ViewChild('fileInput') fileInputRef!: ElementRef<HTMLInputElement>;

  // Upload state
  dragOver    = false;
  uploading   = false;
  uploadError = '';
  fileName    = '';

  // Result state
  result:   VerificationResult | null = null;
  searched  = false;

  // Extracted cert ID (shown after upload)
  extractedId = '';

  private readonly API = 'http://localhost:8089/api';

  constructor(
    private http: HttpClient,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Support QR code deep-link: /verify?id=SKH-000017
    const id = this.route.snapshot.queryParamMap.get('id');
    if (id) {
      this.extractedId = id;
      this.verifyById(id);
    }
  }

  // ── Drag & drop ───────────────────────────────────────────────────────────

  onDragOver(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.dragOver = true;
  }

  onDragLeave(e: DragEvent): void {
    e.preventDefault();
    this.dragOver = false;
  }

  onDrop(e: DragEvent): void {
    e.preventDefault();
    e.stopPropagation();
    this.dragOver = false;
    const file = e.dataTransfer?.files?.[0];
    if (file) this.processFile(file);
  }

  onFileSelected(e: Event): void {
    const input = e.target as HTMLInputElement;
    const file  = input.files?.[0];
    if (file) this.processFile(file);
    input.value = ''; // reset so same file can be re-selected
  }

  openFilePicker(): void {
    this.fileInputRef.nativeElement.click();
  }

  // ── File processing ───────────────────────────────────────────────────────

  private processFile(file: File): void {
    if (file.type !== 'application/pdf') {
      this.uploadError = 'Please upload a PDF file. Only PDF certificates are supported.';
      return;
    }
    if (file.size > 10 * 1024 * 1024) {
      this.uploadError = 'File is too large. Maximum size is 10 MB.';
      return;
    }

    this.fileName    = file.name;
    this.uploadError = '';
    this.result      = null;
    this.searched    = false;
    this.uploading   = true;

    const formData = new FormData();
    formData.append('file', file);

    this.http.post<VerificationResult>(`${this.API}/verify/upload`, formData)
      .subscribe({
        next: res => {
          this.result      = res;
          this.extractedId = res.certificateId;
          this.searched    = true;
          this.uploading   = false;
          // Update URL for shareability
          this.router.navigate([], {
            queryParams: { id: res.certificateId },
            queryParamsHandling: 'merge',
            replaceUrl: true
          });
        },
        error: err => {
          const msg = err?.error?.message || err?.statusText || 'Unknown error';
          this.uploadError = `Verification failed: ${msg}. Please try again.`;
          this.uploading   = false;
          this.searched    = false;
        }
      });
  }

  // ── Direct ID verification (QR code deep-link) ────────────────────────────

  private verifyById(id: string): void {
    this.uploading = true;
    const params   = new HttpParams().set('id', id);
    this.http.get<VerificationResult>(`${this.API}/verify`, { params })
      .subscribe({
        next: res => {
          this.result    = res;
          this.searched  = true;
          this.uploading = false;
        },
        error: () => {
          this.uploadError = 'Could not reach the verification server.';
          this.uploading   = false;
        }
      });
  }

  // ── Reset ─────────────────────────────────────────────────────────────────

  reset(): void {
    this.result      = null;
    this.searched    = false;
    this.fileName    = '';
    this.uploadError = '';
    this.extractedId = '';
    this.router.navigate([], { queryParams: {}, replaceUrl: true });
  }

  // ── Computed ──────────────────────────────────────────────────────────────

  get scorePercent(): number { return Math.round(this.result?.score ?? 0); }

  get formattedDate(): string {
    if (!this.result?.issuedAt) return '—';
    return new Date(this.result.issuedAt).toLocaleDateString('en-US', {
      year: 'numeric', month: 'long', day: 'numeric'
    });
  }

  get difficultyColor(): string {
    const map: Record<string, string> = {
      BEGINNER: '#10b981', INTERMEDIATE: '#f59e0b', ADVANCED: '#ef4444'
    };
    return map[this.result?.difficulty ?? ''] ?? '#64748b';
  }

  get difficultyBg(): string {
    return this.difficultyColor + '20';
  }
}
