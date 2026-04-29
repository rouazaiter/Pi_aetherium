import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CertificationService } from '../../services/certification.service';
import { ToastService } from '../../shared/toast.service';
import {
  CertificationCreate, Difficulty, CertStatus,
  QuestionCreate, QuestionType, LlmGenerateRequest
} from '../../models/certification.model';
import { NgIf, NgFor } from '@angular/common';
import { FormsModule } from '@angular/forms';

export type CreationMode = 'manual' | 'llm' | 'pdf';

@Component({
    selector: 'app-cert-form',
    templateUrl: './cert-form.component.html',
    styleUrls: ['./cert-form.component.scss'],
    standalone: true,
    imports: [RouterLink, NgIf, FormsModule, NgFor]
})
export class CertFormComponent implements OnInit {
  isEdit = false;
  certId: number | null = null;
  mode: CreationMode = 'manual';
  loading = false;
  error = '';

  form: CertificationCreate = {
    title: '', description: '', category: '',
    difficulty: 'INTERMEDIATE', status: 'DRAFT',
    price: 0, validFrom: null, expiresAt: null,
    durationMinutes: 60, passingScore: 70,
    exams: [{ title: 'Main Exam', timeLimit: 60, passingScore: 70, questions: [] }],
    coverImageUrl: null
  };

  // Cover image
  generatingImage = false;
  imagePreviewUrl: string | null = null;
  imageLoaded = false;

  llmReq: LlmGenerateRequest = {
    topic: '', description: '', difficulty: 'INTERMEDIATE',
    numberOfQuestions: 5, timeLimitMinutes: 60
  };

  selectedFile: File | null = null;

  difficulties: Difficulty[] = ['BEGINNER', 'INTERMEDIATE', 'ADVANCED'];
  statuses: CertStatus[] = ['DRAFT', 'PUBLISHED', 'ARCHIVED'];
  questionTypes: QuestionType[] = ['FILL_BLANK', 'MATCH', 'CODE', 'EXPLAIN', 'WRITE'];

  constructor(
    private certService: CertificationService,
    private router: Router,
    private route: ActivatedRoute,
    private toast: ToastService
  ) {}

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    this.certId = idParam ? Number(idParam) : null;
    this.isEdit = !!this.certId && !isNaN(this.certId);

    const modeParam = this.route.snapshot.queryParamMap.get('mode');
    if (modeParam === 'llm' || modeParam === 'pdf') {
      this.mode = modeParam;
    }

    if (this.isEdit && this.certId) {
      this.certService.getById(this.certId).subscribe({
        next: c => {
          this.form = {
            title: c.title, description: c.description, category: c.category,
            difficulty: c.difficulty, status: c.status, price: c.price,
            validFrom: c.validFrom ? c.validFrom.substring(0, 10) : null,
            expiresAt: c.expiresAt ? c.expiresAt.substring(0, 10) : null,
            durationMinutes: c.durationMinutes, passingScore: c.passingScore,
            exams: [], coverImageUrl: c.coverImageUrl ?? null
          };
          this.imagePreviewUrl = c.coverImageUrl ?? null;
        }
      });
    }
  }

  get questions(): QuestionCreate[] {
    return this.form.exams[0]?.questions ?? [];
  }

  addQuestion(): void {
    this.questions.push({
      type: 'EXPLAIN', questionText: '', points: 10,
      orderIndex: this.questions.length + 1,
      expectedAnswer: '', codeLanguage: '', matchPairs: []
    });
  }

  removeQuestion(i: number): void {
    this.questions.splice(i, 1);
  }

  addMatchPair(q: QuestionCreate): void {
    if (!q.matchPairs) q.matchPairs = [];
    q.matchPairs.push({ left: '', right: '' });
  }

  removeMatchPair(q: QuestionCreate, i: number): void {
    q.matchPairs?.splice(i, 1);
  }

  trackByIndex(i: number): number { return i; }

  // ── Cover image generation ────────────────────────────────────────────────
  generateCoverImage(): void {
    if (!this.form.title.trim()) {
      this.toast.error('Enter a title first to generate an image');
      return;
    }

    this.generatingImage = true;
    this.imageLoaded     = false;
    this.imagePreviewUrl = null;
    this.form.coverImageUrl = null;

    const topic = this.form.title.trim();
    const cat   = (this.form.category || 'technology').trim();
    const diff  = this.form.difficulty || 'INTERMEDIATE';

    const colorScheme = diff === 'BEGINNER' ? 'teal green neon'
                      : diff === 'ADVANCED' ? 'red purple neon'
                      : 'blue cyan neon';

    const prompt  = `${topic} ${cat} 3D render dark background ${colorScheme} cinematic no text`;
    const encoded = encodeURIComponent(prompt);
    const seed    = Math.abs(topic.split('').reduce((a, c) => a + c.charCodeAt(0), 0) % 99999);
    const url     = `https://image.pollinations.ai/prompt/${encoded}?width=800&height=450&seed=${seed}&nologo=true`;

    this.toast.info('🎨 Generating image — please wait ~15 seconds…');

    // Use a hidden Image object to load — avoids CORS issues with HttpClient blob fetch
    const img = new Image();
    img.crossOrigin = 'anonymous';

    // Timeout after 40s
    const timeout = setTimeout(() => {
      img.onload = null;
      img.onerror = null;
      this.generatingImage = false;
      this.toast.error('❌ Image took too long. Try again.');
    }, 40000);

    img.onload = () => {
      clearTimeout(timeout);
      this.imagePreviewUrl    = url;
      this.form.coverImageUrl = url;
      this.imageLoaded        = true;
      this.generatingImage    = false;
      this.toast.success('✅ Cover image generated!');
    };

    img.onerror = () => {
      clearTimeout(timeout);
      // Retry once without crossOrigin (some CDNs block it)
      const img2 = new Image();
      img2.onload = () => {
        this.imagePreviewUrl    = url;
        this.form.coverImageUrl = url;
        this.imageLoaded        = true;
        this.generatingImage    = false;
        this.toast.success('✅ Cover image generated!');
      };
      img2.onerror = () => {
        this.generatingImage = false;
        this.toast.error('❌ Image generation failed. Check your internet connection and try again.');
      };
      img2.src = url;
    };

    img.src = url;
  }

  onImageError(): void {
    this.imageLoaded = false;
    this.toast.error('Image failed to load. Try generating again.');
  }

  removeCoverImage(): void {
    this.form.coverImageUrl = null;
    this.imagePreviewUrl = null;
    this.imageLoaded = false;
  }

  submitManual(): void {    if (!this.form.title.trim()) { this.error = 'Title is required'; return; }
    this.loading = true; this.error = '';
    const obs = this.isEdit && this.certId
      ? this.certService.update(this.certId, this.form)
      : this.certService.create(this.form);
    obs.subscribe({
      next: cert => {
        this.toast.success(this.isEdit ? 'Certification updated' : 'Certification created');
        this.router.navigate(['/skillhub/certifications', cert.id]);
      },
      error: e => { this.error = e.message; this.loading = false; }
    });
  }

  submitLlm(): void {
    if (!this.llmReq.topic.trim()) { this.error = 'Topic is required'; return; }
    if (this.llmReq.numberOfQuestions < 5 || this.llmReq.numberOfQuestions > 75) {
      this.error = 'Number of questions must be between 5 and 75'; return;
    }
    this.loading = true; this.error = '';
    this.certService.generateFromLlm(this.llmReq).subscribe({
      next: cert => {
        this.toast.success(`"${cert.title}" generated successfully with ${this.llmReq.numberOfQuestions} questions`);
        this.router.navigate(['/skillhub/certifications', cert.id]);
      },
      error: e => {
        this.error = e.message;
        this.toast.error('Generation failed: ' + e.message);
        this.loading = false;
      }
    });
  }

  onFileChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  submitPdf(): void {
    if (!this.selectedFile) { this.error = 'Please select a PDF file'; return; }
    this.loading = true; this.error = '';
    this.certService.importFromPdf(this.selectedFile).subscribe({
      next: cert => {
        this.toast.success(`"${cert.title}" imported successfully`);
        this.router.navigate(['/skillhub/certifications', cert.id]);
      },
      error: e => {
        this.error = e.message;
        this.toast.error('Import failed: ' + e.message);
        this.loading = false;
      }
    });
  }
}
