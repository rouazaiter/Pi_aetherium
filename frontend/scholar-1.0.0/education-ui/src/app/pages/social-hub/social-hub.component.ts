import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import type { QuizLeaderboardEntryResponse, StudyGroupMemberResponse, StudyGroupResponse } from '../../core/models/api.models';
import { SocialGraphService } from '../../core/services/social-graph.service';
import { messageFromHttpError } from '../../core/util/http-error';

@Component({
  selector: 'app-social-hub',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule, DatePipe],
  templateUrl: './social-hub.component.html',
  styleUrl: './social-hub.component.scss',
})
export class SocialHubComponent implements OnInit {
  private readonly quizSize = 10;
  private readonly maxImageWidth = 960;
  private readonly maxImageHeight = 960;
  private readonly fb = new FormBuilder();
  private readonly socialApi = inject(SocialGraphService);

  readonly createGroupForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    topic: ['', [Validators.required, Validators.minLength(2)]],
    description: ['', [Validators.required, Validators.minLength(10)]],
    imageUrl: [''],
  });

  readonly aiQuizTopics = ['Java', 'Spring', 'Angular', 'DevOps', 'Cybersecurity', 'Cloud'];
  readonly itTopics = ['Cybersecurity', 'AI', 'Cloud', 'DevOps', 'Data Science', 'Web Development'];

  readonly activePage = signal<'discover' | 'group-room' | 'quiz' | 'goals'>('discover');
  readonly selectedGroup = signal<HubGroup | null>(null);
  readonly selectedAiTopic = signal('');
  readonly currentQuestionIndex = signal(0);
  readonly selectedAnswers = signal<Record<number, string>>({});
  readonly showQuizResult = signal(false);
  readonly selectedGroupImagePreview = signal<string>('');
  loadingGroups = false;

  readonly suggestedGroups = signal<HubGroup[]>([]);

  readonly groupMembers = signal<GroupMember[]>([]);

  readonly leaderboard = signal<QuizLeaderboardEntryResponse[]>([]);

  readonly quizQuestions = signal<QuizQuestion[]>(this.generateAiQuiz('Java'));

  readonly personalGoals = signal<PersonalGoal[]>([
    { id: 1, title: 'Master React Hooks', progress: 70, status: 'priority' },
    { id: 2, title: 'SQL Optimization', progress: 45, status: 'in-progress' },
    { id: 3, title: 'Business', progress: 20, status: 'in-progress' },
    { id: 4, title: 'Public Speaking', progress: 85, status: 'in-progress' },
  ]);
  readonly newGoalTitle = signal('');
  readonly newGoalProgress = signal(50);
  readonly editingGoalId = signal<number | null>(null);
  readonly editGoalTitle = signal('');
  readonly editGoalProgress = signal(0);

  error = '';
  membersLoading = false;
  createGroupError = '';
  createGroupLoading = false;
  aiQuizLoading = false;
  aiQuizError = '';

  ngOnInit(): void {
    this.refreshGroups();
  }

  readonly currentQuestion = computed(() => this.quizQuestions()[this.currentQuestionIndex()]);
  readonly quizProgress = computed(() =>
    Math.round(((this.currentQuestionIndex() + 1) / Math.max(1, this.quizQuestions().length)) * 100),
  );
  readonly quizScore = computed(() => {
    const answers = this.selectedAnswers();
    return this.quizQuestions().filter((q, idx) => answers[idx] === q.correct).length;
  });

  setPage(page: 'discover' | 'group-room' | 'quiz' | 'goals'): void {
    this.activePage.set(page);
  }

  onGroupImagePicked(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;
    this.createGroupError = '';

    const contentType = (file.type || '').toLowerCase();
    if (contentType.includes('heic') || contentType.includes('heif')) {
      this.createGroupError = 'HEIC/HEIF is not supported here. Please choose JPG, PNG, or WEBP.';
      this.createGroupForm.controls.imageUrl.setValue('');
      this.selectedGroupImagePreview.set('');
      return;
    }

    this.optimizeImageToDataUrl(file)
      .then((dataUrl) => {
        this.createGroupForm.controls.imageUrl.setValue(dataUrl);
        this.selectedGroupImagePreview.set(dataUrl);
      })
      .catch(() => {
        this.createGroupError = 'Unable to read this image. Please choose another file.';
      });
  }

  createGroup(): void {
    this.createGroupError = '';
    if (this.createGroupForm.invalid) {
      this.createGroupForm.markAllAsTouched();
      const name = this.createGroupForm.controls.name.value?.trim() ?? '';
      const topic = this.createGroupForm.controls.topic.value?.trim() ?? '';
      const description = this.createGroupForm.controls.description.value?.trim() ?? '';
      if (name.length < 2) {
        this.createGroupError = 'Group name must contain at least 2 characters.';
      } else if (topic.length < 2) {
        this.createGroupError = 'Topic must contain at least 2 characters.';
      } else if (description.length < 10) {
        this.createGroupError = 'Description must contain at least 10 characters.';
      } else {
        this.createGroupError = 'Please complete all required fields correctly.';
      }
      return;
    }
    const raw = this.createGroupForm.getRawValue();
    this.createGroupLoading = true;
    this.socialApi
      .createGroup({
        name: raw.name.trim(),
        topic: raw.topic.trim(),
        description: raw.description.trim(),
        imageUrl: raw.imageUrl || null,
      })
      .subscribe({
        next: () => {
          this.createGroupLoading = false;
          this.createGroupForm.reset({ name: '', topic: '', description: '', imageUrl: '' });
          this.selectedGroupImagePreview.set('');
          this.createGroupError = '';
          this.refreshGroups();
        },
        error: (err) => {
          this.createGroupLoading = false;
          this.createGroupError = messageFromHttpError(err, 'Unable to create study group.');
        },
      });
  }

  openGroup(group: HubGroup): void {
    this.selectedGroup.set(group);
    this.activePage.set('group-room');
    this.loadGroupMembers(group.id);
    this.loadQuizLeaderboard(group.id);
  }

  generateQuizByAi(): void {
    const group = this.selectedGroup();
    if (!group) {
      this.aiQuizError = 'Please select a group first.';
      return;
    }

    const topic = this.selectedAiTopic().trim() || group.topic || 'IT';
    this.aiQuizError = '';
    this.aiQuizLoading = true;

    this.socialApi.launchQuiz(group.id, { topic }).subscribe({
      next: (rows) => {
        this.aiQuizLoading = false;
        const mapped = rows.slice(0, this.quizSize).map((q) => ({
          question: q.question,
          options: [q.optionA, q.optionB, q.optionC, q.optionD],
          correct: this.resolveCorrectAnswerText(q),
        }));
        if (mapped.length === 0) {
          this.aiQuizError = 'AI did not return questions. Try another topic.';
          return;
        }
        this.quizQuestions.set(mapped);
        this.currentQuestionIndex.set(0);
        this.selectedAnswers.set({});
        this.showQuizResult.set(false);
        this.activePage.set('quiz');
      },
      error: (err) => {
        this.aiQuizLoading = false;
        this.aiQuizError = messageFromHttpError(err, 'AI quiz generation failed. Try again.');
      },
    });
  }

  proposeQuiz(): void {
    const topic = this.selectedAiTopic().trim() || this.selectedGroup()?.topic || 'Custom IT';
    this.quizQuestions.set(this.generateAiQuiz(topic));
    this.currentQuestionIndex.set(0);
    this.selectedAnswers.set({});
    this.showQuizResult.set(false);
    this.activePage.set('quiz');
  }

  answerCurrent(option: string): void {
    const idx = this.currentQuestionIndex();
    this.selectedAnswers.update((prev) => ({ ...prev, [idx]: option }));
  }

  nextQuestion(): void {
    const idx = this.currentQuestionIndex();
    if (idx < this.quizQuestions().length - 1) {
      this.currentQuestionIndex.set(idx + 1);
      return;
    }
    this.showQuizResult.set(true);
  }

  restartQuiz(): void {
    const topic = this.selectedAiTopic().trim() || this.selectedGroup()?.topic || 'IT';
    this.quizQuestions.set(this.generateAiQuiz(topic));
    this.currentQuestionIndex.set(0);
    this.selectedAnswers.set({});
    this.showQuizResult.set(false);
  }

  viewProfile(member: GroupMember): void {
    this.error = `Open profile for @${member.username} from the Friends/Profile page.`;
  }

  addPersonalGoal(): void {
    const title = this.newGoalTitle().trim();
    if (title.length < 2) {
      return;
    }
    const progress = this.normalizeProgress(this.newGoalProgress());
    const next: PersonalGoal = {
      id: Date.now(),
      title,
      progress,
      status: progress >= 70 ? 'priority' : 'in-progress',
    };
    this.personalGoals.update((rows) => [next, ...rows]);
    this.newGoalTitle.set('');
    this.newGoalProgress.set(50);
  }

  startGoalEdit(goal: PersonalGoal): void {
    this.editingGoalId.set(goal.id);
    this.editGoalTitle.set(goal.title);
    this.editGoalProgress.set(goal.progress);
  }

  cancelGoalEdit(): void {
    this.editingGoalId.set(null);
    this.editGoalTitle.set('');
    this.editGoalProgress.set(0);
  }

  saveGoalEdit(goalId: number): void {
    const title = this.editGoalTitle().trim();
    if (title.length < 2) {
      return;
    }
    const progress = this.normalizeProgress(this.editGoalProgress());
    this.personalGoals.update((rows) =>
      rows.map((g) =>
        g.id === goalId
          ? {
              ...g,
              title,
              progress,
              status: progress >= 70 ? 'priority' : 'in-progress',
            }
          : g,
      ),
    );
    this.cancelGoalEdit();
  }

  deleteGoal(goalId: number): void {
    this.personalGoals.update((rows) => rows.filter((g) => g.id !== goalId));
    if (this.editingGoalId() === goalId) {
      this.cancelGoalEdit();
    }
  }

  displayUserLabel(username: string): string {
    const match = this.groupMembers().find((m) => m.username.toLowerCase() === (username || '').toLowerCase());
    if (match?.name) {
      return match.name;
    }
    return this.friendlyUsername(username);
  }

  private loadGroupMembers(groupId: number): void {
    this.membersLoading = true;
    this.socialApi.groupMembers(groupId).subscribe({
      next: (rows) => {
        this.membersLoading = false;
        this.groupMembers.set(rows.map((row) => this.toGroupMember(row)));
      },
      error: (err) => {
        this.membersLoading = false;
        this.groupMembers.set([]);
        this.error = messageFromHttpError(err, 'Unable to load real group members for this group.');
      },
    });
  }

  private loadQuizLeaderboard(groupId: number): void {
    this.socialApi.groupQuizLeaderboard(groupId).subscribe({
      next: (rows) => {
        this.leaderboard.set(rows);
      },
      error: () => {
        // Keep the UI clean if leaderboard cannot be fetched.
        this.leaderboard.set([]);
      },
    });
  }

  private refreshGroups(): void {
    this.loadingGroups = true;
    this.socialApi.allGroups().subscribe({
      next: (rows) => {
        this.loadingGroups = false;
        this.suggestedGroups.set(rows.map((row) => this.toHubGroup(row)));
        const firstGroup = rows[0];
        if (firstGroup) {
          this.selectedGroup.set(this.toHubGroup(firstGroup));
          this.loadGroupMembers(firstGroup.id);
          this.loadQuizLeaderboard(firstGroup.id);
        } else {
          this.selectedGroup.set(null);
          this.groupMembers.set([]);
          this.leaderboard.set([]);
        }
      },
      error: (err) => {
        this.loadingGroups = false;
        this.error = messageFromHttpError(err, 'Unable to load public groups.');
      },
    });
  }

  private toHubGroup(row: StudyGroupResponse): HubGroup {
    return {
      id: row.id,
      name: row.name,
      topic: row.topic ?? 'IT',
      description: row.description ?? 'No description',
      members: row.memberCount,
      imageUrl: row.imageUrl ?? this.defaultGroupImage(row.topic),
      owner: row.ownerUsername,
      activeNow: false,
    };
  }

  private defaultGroupImage(topic?: string | null): string {
    const normalized = (topic ?? '').toLowerCase();
    if (normalized.includes('security')) {
      return 'https://images.unsplash.com/photo-1563013544-824ae1b704d3?auto=format&fit=crop&w=900&q=80';
    }
    if (normalized.includes('data') || normalized.includes('ai')) {
      return 'https://images.unsplash.com/photo-1526379095098-d400fd0bf935?auto=format&fit=crop&w=900&q=80';
    }
    if (normalized.includes('cloud') || normalized.includes('devops')) {
      return 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=900&q=80';
    }
    return 'https://images.unsplash.com/photo-1518773553398-650c184e0bb3?auto=format&fit=crop&w=900&q=80';
  }

  private optimizeImageToDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onerror = () => reject(new Error('read_failed'));
      reader.onload = () => {
        const source = String(reader.result ?? '');
        if (!source) {
          reject(new Error('empty_data'));
          return;
        }
        const img = new Image();
        img.onerror = () => reject(new Error('decode_failed'));
        img.onload = () => {
          const ratio = Math.min(
            1,
            this.maxImageWidth / Math.max(1, img.width),
            this.maxImageHeight / Math.max(1, img.height),
          );
          const width = Math.max(1, Math.round(img.width * ratio));
          const height = Math.max(1, Math.round(img.height * ratio));
          const canvas = document.createElement('canvas');
          canvas.width = width;
          canvas.height = height;
          const ctx = canvas.getContext('2d');
          if (!ctx) {
            reject(new Error('canvas_failed'));
            return;
          }
          ctx.drawImage(img, 0, 0, width, height);
          const optimized = canvas.toDataURL('image/jpeg', 0.82);
          resolve(optimized || source);
        };
        img.src = source;
      };
      reader.readAsDataURL(file);
    });
  }

  private resolveCorrectAnswerText(q: {
    correctOption?: 'A' | 'B' | 'C' | 'D' | null;
    optionA: string;
    optionB: string;
    optionC: string;
    optionD: string;
  }): string {
    switch (q.correctOption) {
      case 'A':
        return q.optionA;
      case 'B':
        return q.optionB;
      case 'C':
        return q.optionC;
      case 'D':
        return q.optionD;
      default:
        return q.optionA;
    }
  }

  private toGroupMember(row: StudyGroupMemberResponse): GroupMember {
    const first = row.firstName?.trim();
    const last = row.lastName?.trim();
    const displayName = [first, last].filter(Boolean).join(' ').trim() || this.friendlyUsername(row.username);
    return {
      id: row.userId,
      username: row.username,
      name: displayName,
      role: row.role,
      points: row.score ?? 0,
      avatar: this.computeAvatar(displayName),
      profilePicture: row.profilePicture,
    };
  }

  private friendlyUsername(value: string): string {
    const raw = (value ?? '').trim();
    if (!raw) {
      return 'User';
    }
    if (raw.includes('@')) {
      const local = raw.split('@')[0]?.trim();
      return local || raw;
    }
    return raw;
  }

  private computeAvatar(name: string): string {
    const parts = name
      .trim()
      .split(/\s+/)
      .filter(Boolean);
    if (parts.length === 0) {
      return 'U';
    }
    if (parts.length === 1) {
      return parts[0].slice(0, 2).toUpperCase();
    }
    return `${parts[0][0] ?? ''}${parts[1][0] ?? ''}`.toUpperCase();
  }

  private generateAiQuiz(topic: string): QuizQuestion[] {
    const pool: QuizQuestion[] = [
      {
        question: `Which practice best secures an ${topic} application in production?`,
        image: 'https://images.unsplash.com/photo-1518773553398-650c184e0bb3?auto=format&fit=crop&w=1200&q=80',
        options: ['Hardcoded credentials', 'Role-based access control', 'Disabling logs', 'Public admin endpoints'],
        correct: 'Role-based access control',
      },
      {
        question: 'Which metric is most useful for monitoring backend stability?',
        options: ['Color palette usage', 'CPU + memory + error rate', 'Font size', 'Logo clicks'],
        correct: 'CPU + memory + error rate',
      },
      {
        question: 'What is the best strategy for one-question-at-a-time exam UX?',
        options: ['All questions in one page', 'Random page reloads', 'Single question with persistent state', 'No progress indicator'],
        correct: 'Single question with persistent state',
      },
      {
        question: `In ${topic}, what improves system reliability the most?`,
        options: ['No logging', 'Health checks and alerts', 'Single environment only', 'Removing tests'],
        correct: 'Health checks and alerts',
      },
      {
        question: 'Which is the safest authentication practice?',
        options: ['Shared admin account', 'JWT/session with expiration', 'Plain-text passwords', 'No auth on internal APIs'],
        correct: 'JWT/session with expiration',
      },
      {
        question: 'What should be done before deploying a new release?',
        options: ['Skip tests for speed', 'Run tests and monitor rollout', 'Deploy directly to production always', 'Disable observability'],
        correct: 'Run tests and monitor rollout',
      },
      {
        question: `For ${topic}, which data policy is best?`,
        options: ['Collect everything forever', 'Data minimization + retention rules', 'No encryption', 'Public backups'],
        correct: 'Data minimization + retention rules',
      },
      {
        question: 'What is most important for team collaboration?',
        options: ['No code reviews', 'Clear PR reviews and documentation', 'Random task ownership', 'No issue tracking'],
        correct: 'Clear PR reviews and documentation',
      },
      {
        question: 'How to reduce outages in production?',
        options: ['Manual hotfixes only', 'CI/CD with staged rollout', 'Direct DB edits', 'Disable rollback'],
        correct: 'CI/CD with staged rollout',
      },
      {
        question: `Which approach is best for scaling ${topic} workloads?`,
        options: ['Vertical forever only', 'Measure bottlenecks then scale', 'Ignore monitoring', 'Single-thread everything'],
        correct: 'Measure bottlenecks then scale',
      },
      {
        question: 'What gives better API quality?',
        options: ['No versioning', 'Contract validation and tests', 'No error handling', 'No rate limiting'],
        correct: 'Contract validation and tests',
      },
      {
        question: 'How to improve app performance safely?',
        options: ['Premature optimization', 'Profile first, optimize hot paths', 'Disable caching', 'Ignore bundle size'],
        correct: 'Profile first, optimize hot paths',
      },
      {
        question: 'What is the best security baseline?',
        options: ['Open all ports', 'Least privilege and secret management', 'Hardcoded API keys', 'No audit trail'],
        correct: 'Least privilege and secret management',
      },
      {
        question: 'Which testing mix is most effective?',
        options: ['Only manual tests', 'Unit + integration + e2e critical paths', 'No tests in CI', 'Only UI tests'],
        correct: 'Unit + integration + e2e critical paths',
      },
      {
        question: `In a ${topic} project, what helps maintainability?`,
        options: ['Huge components', 'Modular architecture and clear boundaries', 'No linting', 'Copy-paste patterns'],
        correct: 'Modular architecture and clear boundaries',
      },
    ];

    return this.shuffle(pool).slice(0, this.quizSize);
  }

  private shuffle<T>(rows: T[]): T[] {
    const out = [...rows];
    for (let i = out.length - 1; i > 0; i -= 1) {
      const j = Math.floor(Math.random() * (i + 1));
      [out[i], out[j]] = [out[j], out[i]];
    }
    return out;
  }

  private normalizeProgress(value: number): number {
    if (Number.isNaN(value)) {
      return 0;
    }
    return Math.max(0, Math.min(100, Math.round(value)));
  }
}

interface HubGroup {
  id: number;
  name: string;
  topic: string;
  description: string;
  members: number;
  imageUrl: string;
  owner: string;
  activeNow: boolean;
}

interface GroupMember {
  id: number;
  username: string;
  name: string;
  role: string;
  points: number;
  avatar: string;
  profilePicture?: string | null;
}

interface QuizQuestion {
  question: string;
  options: string[];
  correct: string;
  image?: string;
}

interface PersonalGoal {
  id: number;
  title: string;
  progress: number;
  status: 'priority' | 'in-progress';
}

