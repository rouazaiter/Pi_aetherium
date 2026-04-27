import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription, catchError, forkJoin, of, switchMap } from 'rxjs';
import type {
  CvAiChatRequest,
  CvAiChatResponse,
  CvAiImproveMaxLength,
  CvAiImproveRequest,
  CvAiImproveTone,
  CvJobMatchResponse,
  CvJobMatchTone,
  CvDraftApiResponse,
  CvDraftDto,
  CvDraftSectionDto,
  CvDraftUpdateRequest,
  CvEducationDto,
  CvExperienceDto,
  CvLanguageDto,
  CvPreviewProfileDto,
  CvPreviewProjectDto,
  CvPreviewResponse,
  CvPreviewSkillDto,
  CvPreviewSkillGroupDto,
  CvProfileResponse,
  UpdateCvProfileRequest,
} from '../../core/models/api.models';
import { JihenCvService } from '../../core/services/jihen-cv.service';
import { messageFromHttpError } from '../../core/util/http-error';

type CvBuilderTemplate = 'ATS_MINIMAL' | 'MODERN' | 'ELEGANT' | 'CREATIVE';
type CvSectionKey = 'PROFILE' | 'SUMMARY' | 'SKILLS' | 'PROJECTS' | 'EXPERIENCE' | 'EDUCATION' | 'LANGUAGES';
type DraftSectionType = 'PROFILE' | 'SKILLS' | 'PROJECTS' | 'EXPERIENCE' | 'EDUCATION' | 'LANGUAGES';

type EditorProfile = {
  fullName: string;
  headline: string;
  email: string;
  phone: string;
  location: string;
  githubUrl: string;
  linkedinUrl: string;
  summary: string;
};

type EditorSkillGroup = {
  category: string;
  skills: CvPreviewSkillDto[];
};

type EditorProject = {
  id: number;
  title: string;
  description: string;
  projectUrl: string;
  imageUrl: string;
  collectionName: string;
  skills: CvPreviewSkillDto[];
  visible: boolean;
};

type EditorExperience = {
  company: string;
  role: string;
  location: string;
  startDate: string;
  endDate: string;
  current: boolean;
  summary: string;
};

type EditorEducation = {
  school: string;
  degree: string;
  fieldOfStudy: string;
  location: string;
  startDate: string;
  endDate: string;
  current: boolean;
  description: string;
};

type EditorLanguage = {
  name: string;
  proficiency: string;
};

type EditorSectionMeta = {
  key: DraftSectionType;
  title: string;
  orderIndex: number;
  visible: boolean;
};

type SuggestionTarget =
  | { kind: 'summary' }
  | { kind: 'project'; projectId: number };

type CvSuggestionState = {
  target: SuggestionTarget;
  title: string;
  original: string;
  suggestion: string;
  loading: boolean;
  error: string;
};

type CvAssistantMessage = {
  role: 'user' | 'assistant';
  content: string;
  score?: number | null;
  suggestedActions?: string[];
};

type CvJobMatchForm = {
  targetJobTitle: string;
  jobDescription: string;
  tone: CvJobMatchTone;
};

@Component({
  selector: 'app-jihen-cv',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './jihen-cv.component.html',
  styleUrl: './jihen-cv.component.scss',
})
export class JihenCvComponent implements OnInit, OnDestroy {
  private readonly cvApi = inject(JihenCvService);
  private chatRequestSub: Subscription | null = null;
  private chatTimeoutHandle: ReturnType<typeof setTimeout> | null = null;

  protected loadingWorkspace = true;
  protected workspaceError = '';
  protected generatingDraft = false;
  protected savingDraft = false;
  protected saveMessage = '';
  protected saveError = '';
  protected aiActionError = '';
  protected chatLoading = false;
  protected chatError = '';
  protected chatTimeoutMessage = '';
  protected pendingChatMessage = '';
  protected pendingChatContext: CvAiChatRequest | null = null;
  protected hasDraft = false;

  protected currentDraftId: number | null = null;
  protected currentDraftSource: CvDraftDto | null = null;
  protected rawDraftSections: CvDraftSectionDto[] = [];
  protected profileResponse: CvProfileResponse | null = null;
  protected previewResponse: CvPreviewResponse | null = null;

  protected selectedTemplate: CvBuilderTemplate = 'ATS_MINIMAL';
  protected profile: EditorProfile = this.buildEmptyProfile();
  protected skillGroups: EditorSkillGroup[] = [];
  protected projects: EditorProject[] = [];
  protected experience: EditorExperience[] = [];
  protected education: EditorEducation[] = [];
  protected languages: EditorLanguage[] = [];

  protected readonly sectionMeta: Record<DraftSectionType, EditorSectionMeta> = {
    PROFILE: { key: 'PROFILE', title: 'Profile', orderIndex: 0, visible: true },
    SKILLS: { key: 'SKILLS', title: 'Skills', orderIndex: 1, visible: true },
    PROJECTS: { key: 'PROJECTS', title: 'Projects', orderIndex: 2, visible: true },
    EXPERIENCE: { key: 'EXPERIENCE', title: 'Experience', orderIndex: 3, visible: true },
    EDUCATION: { key: 'EDUCATION', title: 'Education', orderIndex: 4, visible: true },
    LANGUAGES: { key: 'LANGUAGES', title: 'Languages', orderIndex: 5, visible: true },
  };

  protected sectionOpen: Record<CvSectionKey, boolean> = {
    PROFILE: true,
    SUMMARY: true,
    SKILLS: false,
    PROJECTS: true,
    EXPERIENCE: false,
    EDUCATION: false,
    LANGUAGES: false,
  };

  protected suggestionState: CvSuggestionState | null = null;
  protected chatInput = '';
  protected jobMatchModalOpen = false;
  protected jobMatchSubmitting = false;
  protected jobMatchApplying = false;
  protected jobMatchError = '';
  protected jobMatchDraftId: number | null = null;
  protected jobMatchResult: CvJobMatchResponse | null = null;
  protected jobMatchForm: CvJobMatchForm = this.buildEmptyJobMatchForm();
  protected readonly jobMatchToneOptions: CvJobMatchTone[] = ['ATS_PROFESSIONAL', 'CONFIDENT', 'CONCISE', 'INTERNSHIP', 'SENIOR'];
  protected assistantMessages: CvAssistantMessage[] = [
    {
      role: 'assistant',
      content: 'I can help improve your summary, project wording, and overall CV readiness.',
    },
  ];

  ngOnInit(): void {
    this.loadWorkspace();
  }

  ngOnDestroy(): void {
    this.clearChatRequestState(false);
  }

  protected get visibleProjects(): EditorProject[] {
    return this.projects.filter((project) => project.visible);
  }

  protected get hasPreviewSummary(): boolean {
    return this.profile.summary.trim().length > 0;
  }

  protected get canSaveDraft(): boolean {
    return this.hasDraft && !this.savingDraft && this.currentDraftId !== null;
  }

  protected get canDownloadPdf(): boolean {
    return this.hasDraft;
  }

  protected get canCreateJobMatchedCv(): boolean {
    return this.hasDraft && !this.loadingWorkspace && !this.jobMatchSubmitting && !this.jobMatchApplying;
  }

  protected get canSubmitJobMatch(): boolean {
    return (
      !this.jobMatchSubmitting &&
      this.jobMatchForm.targetJobTitle.trim().length > 0 &&
      this.jobMatchForm.jobDescription.trim().length > 0
    );
  }

  protected get hasJobMatchResult(): boolean {
    return this.jobMatchResult !== null;
  }

  protected get previewTemplateLabel(): string {
    switch (this.selectedTemplate) {
      case 'MODERN':
        return 'Modern';
      case 'ELEGANT':
        return 'Elegant';
      case 'CREATIVE':
        return 'Creative';
      default:
        return 'ATS Minimal';
    }
  }

  protected trackBySkillGroup(_: number, group: EditorSkillGroup): string {
    return group.category;
  }

  protected trackBySkill(_: number, skill: CvPreviewSkillDto): number {
    return skill.id;
  }

  protected trackByProject(_: number, project: EditorProject): number {
    return project.id;
  }

  protected trackByIndex(index: number): number {
    return index;
  }

  protected toggleSection(section: CvSectionKey): void {
    this.sectionOpen[section] = !this.sectionOpen[section];
  }

  protected generateDraft(): void {
    if (this.generatingDraft) {
      return;
    }

    this.generatingDraft = true;
    this.workspaceError = '';
    this.saveError = '';

    this.cvApi.generateMyCvDraft().subscribe({
      next: (response) => {
        this.generatingDraft = false;
        const draft = this.extractDraft(response);
        this.hasDraft = true;
        this.applyDraftToEditor(draft, this.previewResponse, this.profileResponse);
        this.saveMessage = 'CV draft generated from your portfolio.';
      },
      error: (err) => {
        this.generatingDraft = false;
        this.workspaceError = messageFromHttpError(err, 'Unable to generate a CV draft.');
      },
    });
  }

  protected saveDraft(): void {
    if (!this.currentDraftId || this.savingDraft) {
      return;
    }

    this.persistCurrentCv(this.currentDraftId, 'Draft saved successfully.');
  }

  protected downloadPdf(): void {
    window.print();
  }

  protected openJobMatchModal(): void {
    this.jobMatchModalOpen = true;
    this.jobMatchError = '';
    this.jobMatchResult = null;
    this.jobMatchDraftId = null;
    this.jobMatchSubmitting = false;
    this.jobMatchApplying = false;
    this.jobMatchForm = {
      targetJobTitle: this.profile.headline.trim() || '',
      jobDescription: '',
      tone: 'ATS_PROFESSIONAL',
    };
  }

  protected closeJobMatchModal(): void {
    if (this.jobMatchSubmitting || this.jobMatchApplying) {
      return;
    }

    this.jobMatchModalOpen = false;
    this.jobMatchError = '';
    this.jobMatchResult = null;
    this.jobMatchDraftId = null;
    this.jobMatchForm = this.buildEmptyJobMatchForm();
  }

  protected submitJobMatch(): void {
    if (!this.canSubmitJobMatch) {
      return;
    }

    this.jobMatchSubmitting = true;
    this.jobMatchError = '';
    this.jobMatchResult = null;
    this.jobMatchDraftId = null;

    this.cvApi
      .getLatestMyCvDraft()
      .pipe(
        switchMap((draftResponse) => {
          const latestDraft = this.extractDraft(draftResponse);
          const draftId = latestDraft.id ?? null;

          if (!draftId) {
            throw new Error('missing_draft_id');
          }

          this.jobMatchDraftId = draftId;
          return this.cvApi.createJobMatchedCv({
            draftId,
            targetJobTitle: this.jobMatchForm.targetJobTitle.trim(),
            jobDescription: this.jobMatchForm.jobDescription.trim(),
            tone: this.jobMatchForm.tone,
            language: this.resolveJobMatchLanguage(),
          });
        }),
      )
      .subscribe({
        next: (response) => {
          this.jobMatchSubmitting = false;
          this.jobMatchResult = response;
        },
        error: () => {
          this.jobMatchSubmitting = false;
          this.jobMatchError = 'Could not generate a job-matched CV. Please try again.';
        },
      });
  }

  protected retryJobMatch(): void {
    this.submitJobMatch();
  }

  protected applyJobMatchChanges(): void {
    if (!this.jobMatchResult || !this.jobMatchDraftId || this.jobMatchApplying) {
      return;
    }

    const improvedSummary = (this.jobMatchResult.improvedSummary ?? '').trim();
    if (improvedSummary) {
      this.profile.summary = improvedSummary;
    }

    for (const improvedProject of this.jobMatchResult.improvedProjects ?? []) {
      const improvedDescription = (improvedProject.improvedDescription ?? '').trim();
      if (!improvedDescription) {
        continue;
      }

      const project = this.projects.find((candidate) => candidate.id === improvedProject.projectId);
      if (project) {
        project.description = improvedDescription;
      }
    }

    this.jobMatchApplying = true;
    this.jobMatchError = '';

    this.persistCurrentCv(
      this.jobMatchDraftId,
      `Job-matched CV ready for ${this.jobMatchResult.targetJobTitle?.trim() || this.jobMatchForm.targetJobTitle.trim()}.`,
      () => {
        this.jobMatchApplying = false;
        this.closeJobMatchModal();
      },
      () => {
        this.jobMatchApplying = false;
        this.jobMatchError = 'Could not generate a job-matched CV. Please try again.';
      },
    );
  }

  protected selectTemplate(template: CvBuilderTemplate): void {
    this.selectedTemplate = template;
    this.saveMessage = '';

    if (this.hasDraft && this.currentDraftId && !this.savingDraft) {
      this.persistCurrentCv(this.currentDraftId, `${this.previewTemplateLabel} template applied.`);
    }
  }

  protected requestSummaryImprove(tone: CvAiImproveTone = 'ATS_PROFESSIONAL', maxLength: CvAiImproveMaxLength = 'MEDIUM'): void {
    const text = this.profile.summary.trim();
    if (!text) {
      this.aiActionError = 'Add a summary first so AI can improve it.';
      return;
    }

    this.aiActionError = '';
    this.suggestionState = {
      target: { kind: 'summary' },
      title: 'Summary suggestion',
      original: text,
      suggestion: '',
      loading: true,
      error: '',
    };

    const payload: CvAiImproveRequest = {
      topic: 'PROFILE_SUMMARY',
      sectionType: 'PROFILE',
      field: 'summary',
      text,
      targetTone: tone,
      maxLength,
      context: this.buildAiContext(),
    };

    this.cvApi.improveMyCvText(payload).subscribe({
      next: (response) => {
        if (!this.suggestionState || this.suggestionState.target.kind !== 'summary') {
          return;
        }
        this.suggestionState.loading = false;
        this.suggestionState.suggestion = (response.suggestion ?? '').trim();
        this.suggestionState.error = this.suggestionState.suggestion ? '' : 'The AI returned an empty suggestion.';
      },
      error: (err) => {
        if (!this.suggestionState || this.suggestionState.target.kind !== 'summary') {
          return;
        }
        this.suggestionState.loading = false;
        this.suggestionState.error = messageFromHttpError(err, 'Unable to improve your summary right now.');
      },
    });
  }

  protected requestProjectImprove(projectId: number): void {
    const project = this.projects.find((candidate) => candidate.id === projectId);
    if (!project) {
      return;
    }

    const text = project.description.trim();
    if (!text) {
      this.aiActionError = 'This project needs a description before AI can improve it.';
      return;
    }

    this.aiActionError = '';
    this.suggestionState = {
      target: { kind: 'project', projectId },
      title: `${project.title} suggestion`,
      original: text,
      suggestion: '',
      loading: true,
      error: '',
    };

    const payload: CvAiImproveRequest = {
      topic: 'PROJECT_DESCRIPTION',
      sectionType: 'PROJECTS',
      field: 'description',
      text,
      targetTone: 'ATS_PROFESSIONAL',
      maxLength: 'MEDIUM',
      context: {
        ...this.buildAiContext(),
        project: {
          id: project.id,
          title: project.title,
          skills: project.skills.map((skill) => skill.name),
        },
      },
    };

    this.cvApi.improveMyCvText(payload).subscribe({
      next: (response) => {
        if (!this.suggestionState || this.suggestionState.target.kind !== 'project' || this.suggestionState.target.projectId !== projectId) {
          return;
        }
        this.suggestionState.loading = false;
        this.suggestionState.suggestion = (response.suggestion ?? '').trim();
        this.suggestionState.error = this.suggestionState.suggestion ? '' : 'The AI returned an empty suggestion.';
      },
      error: (err) => {
        if (!this.suggestionState || this.suggestionState.target.kind !== 'project' || this.suggestionState.target.projectId !== projectId) {
          return;
        }
        this.suggestionState.loading = false;
        this.suggestionState.error = messageFromHttpError(err, 'Unable to improve this project description right now.');
      },
    });
  }

  protected applySuggestion(): void {
    if (!this.suggestionState || !this.suggestionState.suggestion.trim()) {
      return;
    }

    if (this.suggestionState.target.kind === 'summary') {
      this.profile.summary = this.suggestionState.suggestion;
    } else {
      const projectId = this.suggestionState.target.projectId;
      const project = this.projects.find((candidate) => candidate.id === projectId);
      if (project) {
        project.description = this.suggestionState.suggestion;
      }
    }

    this.cancelSuggestion();
  }

  protected cancelSuggestion(): void {
    this.suggestionState = null;
  }

  protected sendAssistantPrompt(message: string): void {
    this.chatInput = message;
    this.submitAssistantChat();
  }

  protected cancelAssistantChat(): void {
    this.clearChatRequestState(true);
    this.chatError = 'AI request canceled. You can edit your message and try again.';
  }

  protected retryAssistantChat(): void {
    if (!this.pendingChatMessage.trim()) {
      return;
    }

    this.submitAssistantChat();
  }

  protected submitAssistantChat(): void {
    const message = this.chatInput.trim() || this.pendingChatMessage.trim();
    if (!message || this.chatLoading) {
      return;
    }

    const payload: CvAiChatRequest = {
      message,
      draftId: this.currentDraftId,
      contextMode: 'CURRENT_CV',
    };

    this.chatError = '';
    this.chatTimeoutMessage = '';
    this.chatLoading = true;
    this.pendingChatMessage = message;
    this.pendingChatContext = payload;
    this.startChatTimeout('This is taking longer than usual. You can wait or try again.');

    this.chatRequestSub?.unsubscribe();
    this.chatRequestSub = this.cvApi.chatAboutMyCv(payload).subscribe({
      next: (response) => {
        this.assistantMessages = [...this.assistantMessages, { role: 'user', content: message }, this.buildAssistantReply(response)];
        this.chatInput = '';
        this.clearChatRequestState(false);
      },
      error: (err) => {
        this.chatLoading = false;
        this.clearChatTimeout();
        this.chatError = messageFromHttpError(err, 'Unable to reach the AI CV assistant right now.');
      },
    });
  }

  protected addExperience(): void {
    this.experience = [...this.experience, this.buildEmptyExperience()];
  }

  protected addEducation(): void {
    this.education = [...this.education, this.buildEmptyEducation()];
  }

  protected addLanguage(): void {
    this.languages = [...this.languages, this.buildEmptyLanguage()];
  }

  protected removeExperience(index: number): void {
    this.experience = this.experience.filter((_, itemIndex) => itemIndex !== index);
  }

  protected removeEducation(index: number): void {
    this.education = this.education.filter((_, itemIndex) => itemIndex !== index);
  }

  protected removeLanguage(index: number): void {
    this.languages = this.languages.filter((_, itemIndex) => itemIndex !== index);
  }

  protected formatSkillCategory(category: string | null | undefined): string {
    const value = (category ?? '').trim();
    if (!value) {
      return 'General';
    }

    return value
      .toLowerCase()
      .split('_')
      .filter(Boolean)
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  protected projectHasMissingDescription(project: EditorProject): boolean {
    return !project.description.trim();
  }

  protected visibleProjectCount(): number {
    return this.visibleProjects.length;
  }

  protected currentSuggestion(): CvSuggestionState | null {
    return this.suggestionState;
  }

  protected isProjectSuggestion(projectId: number): boolean {
    return this.suggestionState?.target.kind === 'project' && this.suggestionState.target.projectId === projectId;
  }

  protected skillNames(skills: CvPreviewSkillDto[]): string {
    return skills.map((skill) => skill.name).filter(Boolean).join(', ');
  }

  protected skillNamesWithDots(skills: CvPreviewSkillDto[]): string {
    return skills.map((skill) => skill.name).filter(Boolean).join(' • ');
  }

  protected projectTitleById(projectId: number): string {
    return this.projects.find((candidate) => candidate.id === projectId)?.title || `Project #${projectId}`;
  }

  private loadWorkspace(): void {
    this.loadingWorkspace = true;
    this.workspaceError = '';

    forkJoin({
      preview: this.cvApi.getMyCvPreview().pipe(catchError(() => of(null))),
      profile: this.cvApi.getMyCvProfile().pipe(catchError(() => of(null))),
      draft: this.cvApi.getLatestMyCvDraft().pipe(
        catchError((err: unknown) => {
          if (err instanceof HttpErrorResponse && err.status === 404) {
            return of(null);
          }
          throw err;
        }),
      ),
    }).subscribe({
      next: ({ preview, profile, draft }) => {
        this.loadingWorkspace = false;
        this.previewResponse = preview;
        this.profileResponse = profile;

        if (draft) {
          this.hasDraft = true;
          this.applyDraftToEditor(this.extractDraft(draft), preview, profile);
          return;
        }

        this.hasDraft = false;
        this.currentDraftId = null;
        this.currentDraftSource = null;
        this.rawDraftSections = [];
        this.selectedTemplate = this.resolveTemplate('', preview?.profile?.preferredTemplate ?? profile?.preferredTemplate ?? '');
      },
      error: (err) => {
        this.loadingWorkspace = false;
        this.workspaceError = messageFromHttpError(err, 'Unable to load your CV workspace.');
      },
    });
  }

  private applyDraftToEditor(draft: CvDraftDto, preview: CvPreviewResponse | null, profile: CvProfileResponse | null): void {
    this.currentDraftSource = draft;
    this.currentDraftId = draft.id ?? null;
    this.rawDraftSections = [];
    this.selectedTemplate = this.resolveTemplate(draft.theme ?? '', preview?.profile?.preferredTemplate ?? profile?.preferredTemplate ?? '');

    const draftSections = draft.sections ?? [];
    const profileSection = this.findDraftSection(draftSections, 'PROFILE');
    const skillsSection = this.findDraftSection(draftSections, 'SKILLS');
    const projectsSection = this.findDraftSection(draftSections, 'PROJECTS');
    const experienceSection = this.findDraftSection(draftSections, 'EXPERIENCE');
    const educationSection = this.findDraftSection(draftSections, 'EDUCATION');
    const languagesSection = this.findDraftSection(draftSections, 'LANGUAGES');

    const knownTypes = new Set<DraftSectionType>(['PROFILE', 'SKILLS', 'PROJECTS', 'EXPERIENCE', 'EDUCATION', 'LANGUAGES']);
    this.rawDraftSections = draftSections
      .filter((section) => !knownTypes.has(((section.type ?? '').trim().toUpperCase() as DraftSectionType)))
      .map((section) => this.clone(section));

    this.applySectionMeta('PROFILE', profileSection);
    this.applySectionMeta('SKILLS', skillsSection);
    this.applySectionMeta('PROJECTS', projectsSection);
    this.applySectionMeta('EXPERIENCE', experienceSection);
    this.applySectionMeta('EDUCATION', educationSection);
    this.applySectionMeta('LANGUAGES', languagesSection);

    const profileContent = this.normalizeObject(profileSection?.content);
    const previewProfile = preview?.profile ?? {};

    this.profile = {
      fullName: this.readString(profileContent, ['fullName', 'name']) || previewProfile.fullName?.trim() || 'Your Name',
      headline:
        this.readString(profileContent, ['headline', 'jobTitle', 'role', 'title']) ||
        profile?.headline?.trim() ||
        previewProfile.headline?.trim() ||
        '',
      email: this.readString(profileContent, ['email']) || previewProfile.email?.trim() || '',
      phone:
        this.readString(profileContent, ['phone', 'phoneNumber', 'mobile']) ||
        profile?.phone?.trim() ||
        previewProfile.phone?.trim() ||
        '',
      location:
        this.readString(profileContent, ['location', 'city', 'country']) ||
        profile?.location?.trim() ||
        previewProfile.location?.trim() ||
        '',
      githubUrl: this.readString(profileContent, ['githubUrl', 'github']) || previewProfile.githubUrl?.trim() || '',
      linkedinUrl:
        this.readString(profileContent, ['linkedinUrl', 'linkedInUrl', 'linkedin']) ||
        previewProfile.linkedInUrl?.trim() ||
        previewProfile.linkedinUrl?.trim() ||
        '',
      summary:
        this.readString(profileContent, ['summary', 'about', 'bio', 'description']) ||
        profile?.summary?.trim() ||
        previewProfile.summary?.trim() ||
        '',
    };

    this.skillGroups = this.mapSkillGroups(skillsSection?.content, preview?.skillsByCategory ?? []);
    this.projects = this.mapProjects(
      projectsSection?.content,
      preview?.projects ?? [],
      profile?.selectedProjectIds ?? [],
    );
    this.experience = this.mapExperience(experienceSection?.content, profile?.experience ?? preview?.experience ?? []);
    this.education = this.mapEducation(educationSection?.content, profile?.education ?? preview?.education ?? []);
    this.languages = this.mapLanguages(languagesSection?.content, profile?.languages ?? preview?.languages ?? []);
  }

  private applySectionMeta(sectionKey: DraftSectionType, section: CvDraftSectionDto | null): void {
    if (!section) {
      return;
    }

    this.sectionMeta[sectionKey] = {
      ...this.sectionMeta[sectionKey],
      title: section.title?.trim() || this.sectionMeta[sectionKey].title,
      orderIndex: section.orderIndex ?? this.sectionMeta[sectionKey].orderIndex,
      visible: section.visible !== false,
    };
  }

  private mapSkillGroups(content: unknown, previewGroups: CvPreviewSkillGroupDto[]): EditorSkillGroup[] {
    if (Array.isArray(content)) {
      return content
        .map((group, index) => {
          const groupData = this.normalizeObject(group);
          const skills = Array.isArray(groupData['skills'])
            ? groupData['skills']
                .map((skill, skillIndex) => this.mapSkill(skill, skillIndex + 1))
                .filter((skill): skill is CvPreviewSkillDto => Boolean(skill))
            : [];

          return {
            category: this.formatSkillCategory(this.readString(groupData, ['category']) || `group_${index + 1}`),
            skills,
          };
        })
        .filter((group) => group.skills.length > 0);
    }

    return previewGroups
      .map((group) => ({
        category: this.formatSkillCategory(group.category),
        skills: (group.skills ?? []).filter((skill) => Boolean(skill?.name?.trim())),
      }))
      .filter((group) => group.skills.length > 0);
  }

  private mapProjects(content: unknown, previewProjects: CvPreviewProjectDto[], selectedIds: number[]): EditorProject[] {
    if (Array.isArray(content)) {
      return content
        .map((project, index) => {
          const projectData = this.normalizeObject(project);
          const title = this.readString(projectData, ['title', 'name']);
          const previewProject = previewProjects[index];

          return {
            id: this.readNumber(projectData, ['id']) ?? previewProject?.id ?? index + 1,
            title: title || previewProject?.title?.trim() || '',
            description: this.readString(projectData, ['description', 'summary']),
            projectUrl: this.readString(projectData, ['projectUrl', 'url']) || previewProject?.projectUrl?.trim() || '',
            imageUrl: this.readString(projectData, ['imageUrl']) || previewProject?.imageUrl?.trim() || '',
            collectionName: this.readString(projectData, ['collectionName']) || previewProject?.collectionName?.trim() || '',
            skills: Array.isArray(projectData['skills'])
              ? projectData['skills']
                  .map((skill, skillIndex) => this.mapSkill(skill, skillIndex + 1))
                  .filter((skill): skill is CvPreviewSkillDto => Boolean(skill))
              : (previewProject?.skills ?? []).filter((skill) => Boolean(skill?.name?.trim())),
            visible: true,
          };
        })
        .filter((project) => Boolean(project.title));
    }

    const selectedIdSet = new Set(selectedIds);
    return previewProjects.map((project) => ({
      id: project.id,
      title: project.title?.trim() || '',
      description: project.description?.trim() || '',
      projectUrl: project.projectUrl?.trim() || '',
      imageUrl: project.imageUrl?.trim() || '',
      collectionName: project.collectionName?.trim() || '',
      skills: (project.skills ?? []).filter((skill) => Boolean(skill?.name?.trim())),
      visible: selectedIdSet.size === 0 || selectedIdSet.has(project.id),
    }));
  }

  private mapExperience(content: unknown, fallback: CvExperienceDto[]): EditorExperience[] {
    const source = Array.isArray(content) ? content : fallback;
    return source.map((item) => {
      const data = this.normalizeObject(item);
      return {
        company: this.readString(data, ['company']),
        role: this.readString(data, ['role', 'position']),
        location: this.readString(data, ['location']),
        startDate: this.readString(data, ['startDate']),
        endDate: this.readString(data, ['endDate']),
        current: this.readBoolean(data, ['current']),
        summary: this.readString(data, ['summary', 'description']),
      };
    });
  }

  private mapEducation(content: unknown, fallback: CvEducationDto[]): EditorEducation[] {
    const source = Array.isArray(content) ? content : fallback;
    return source.map((item) => {
      const data = this.normalizeObject(item);
      return {
        school: this.readString(data, ['school', 'institution', 'name']),
        degree: this.readString(data, ['degree']),
        fieldOfStudy: this.readString(data, ['fieldOfStudy', 'field']),
        location: this.readString(data, ['location']),
        startDate: this.readString(data, ['startDate']),
        endDate: this.readString(data, ['endDate']),
        current: this.readBoolean(data, ['current']),
        description: this.readString(data, ['description', 'summary']),
      };
    });
  }

  private mapLanguages(content: unknown, fallback: CvLanguageDto[]): EditorLanguage[] {
    const source = Array.isArray(content) ? content : fallback;
    return source.map((item) => {
      const data = this.normalizeObject(item);
      return {
        name: this.readString(data, ['name', 'language']),
        proficiency: this.readString(data, ['proficiency', 'level']),
      };
    });
  }

  private buildDraftUpdateRequest(): CvDraftUpdateRequest {
    const sections: CvDraftSectionDto[] = [
      this.serializeProfileSection(),
      this.serializeSkillsSection(),
      this.serializeProjectsSection(),
      this.serializeExperienceSection(),
      this.serializeEducationSection(),
      this.serializeLanguagesSection(),
      ...this.rawDraftSections.map((section) => this.clone(section)),
    ].sort((left, right) => (left.orderIndex ?? 0) - (right.orderIndex ?? 0));

    return {
      theme: this.selectedTemplate,
      settings: this.clone(this.currentDraftSource?.settings ?? null),
      sections,
    };
  }

  private buildProfileUpdateRequest(): UpdateCvProfileRequest {
    return {
      headline: this.profile.headline.trim() || null,
      summary: this.profile.summary.trim() || null,
      professionalSummary: this.profile.summary.trim() || null,
      phone: this.profile.phone.trim() || null,
      location: this.profile.location.trim() || null,
      preferredTemplate: this.profileResponse?.preferredTemplate?.trim() || null,
      language: this.profileResponse?.language?.trim() || null,
      visibility: this.profileResponse?.visibility ?? null,
      selectedProjectIds: this.visibleProjects.map((project) => project.id),
      education: this.education.map((item) => ({
        school: item.school.trim() || null,
        degree: item.degree.trim() || null,
        fieldOfStudy: item.fieldOfStudy.trim() || null,
        location: item.location.trim() || null,
        startDate: item.startDate.trim() || null,
        endDate: item.current ? null : item.endDate.trim() || null,
        current: item.current,
        description: item.description.trim() || null,
      })),
      experience: this.experience.map((item) => ({
        company: item.company.trim() || null,
        role: item.role.trim() || null,
        location: item.location.trim() || null,
        startDate: item.startDate.trim() || null,
        endDate: item.current ? null : item.endDate.trim() || null,
        current: item.current,
        summary: item.summary.trim() || null,
      })),
      languages: this.languages.map((item) => ({
        name: item.name.trim() || null,
        proficiency: item.proficiency.trim() || null,
      })),
    };
  }

  private serializeProfileSection(): CvDraftSectionDto {
    const meta = this.sectionMeta.PROFILE;
    return {
      type: 'PROFILE',
      title: meta.title.trim(),
      orderIndex: meta.orderIndex,
      visible: meta.visible,
      content: {
        fullName: this.profile.fullName.trim() || null,
        headline: this.profile.headline.trim() || null,
        email: this.profile.email.trim() || null,
        phone: this.profile.phone.trim() || null,
        location: this.profile.location.trim() || null,
        summary: this.profile.summary.trim() || null,
        githubUrl: this.profile.githubUrl.trim() || null,
        linkedinUrl: this.profile.linkedinUrl.trim() || null,
        linkedInUrl: this.profile.linkedinUrl.trim() || null,
      },
    };
  }

  private serializeSkillsSection(): CvDraftSectionDto {
    const meta = this.sectionMeta.SKILLS;
    return {
      type: 'SKILLS',
      title: meta.title.trim(),
      orderIndex: meta.orderIndex,
      visible: meta.visible,
      content: this.skillGroups.map((group) => ({
        category: group.category,
        skills: group.skills.map((skill) => ({
          id: skill.id,
          name: skill.name,
          category: skill.category ?? null,
        })),
      })),
    };
  }

  private serializeProjectsSection(): CvDraftSectionDto {
    const meta = this.sectionMeta.PROJECTS;
    return {
      type: 'PROJECTS',
      title: meta.title.trim(),
      orderIndex: meta.orderIndex,
      visible: meta.visible,
      content: this.visibleProjects.map((project) => ({
        id: project.id,
        title: project.title.trim() || null,
        description: project.description.trim() || null,
        projectUrl: project.projectUrl.trim() || null,
        imageUrl: project.imageUrl.trim() || null,
        collectionName: project.collectionName.trim() || null,
        skills: project.skills.map((skill) => ({
          id: skill.id,
          name: skill.name,
          category: skill.category ?? null,
        })),
      })),
    };
  }

  private serializeExperienceSection(): CvDraftSectionDto {
    const meta = this.sectionMeta.EXPERIENCE;
    return {
      type: 'EXPERIENCE',
      title: meta.title.trim(),
      orderIndex: meta.orderIndex,
      visible: meta.visible,
      content: this.experience.map((item) => ({
        company: item.company.trim() || null,
        role: item.role.trim() || null,
        location: item.location.trim() || null,
        startDate: item.startDate.trim() || null,
        endDate: item.current ? null : item.endDate.trim() || null,
        current: item.current,
        summary: item.summary.trim() || null,
      })),
    };
  }

  private serializeEducationSection(): CvDraftSectionDto {
    const meta = this.sectionMeta.EDUCATION;
    return {
      type: 'EDUCATION',
      title: meta.title.trim(),
      orderIndex: meta.orderIndex,
      visible: meta.visible,
      content: this.education.map((item) => ({
        school: item.school.trim() || null,
        degree: item.degree.trim() || null,
        fieldOfStudy: item.fieldOfStudy.trim() || null,
        location: item.location.trim() || null,
        startDate: item.startDate.trim() || null,
        endDate: item.current ? null : item.endDate.trim() || null,
        current: item.current,
        description: item.description.trim() || null,
      })),
    };
  }

  private serializeLanguagesSection(): CvDraftSectionDto {
    const meta = this.sectionMeta.LANGUAGES;
    return {
      type: 'LANGUAGES',
      title: meta.title.trim(),
      orderIndex: meta.orderIndex,
      visible: meta.visible,
      content: this.languages.map((item) => ({
        name: item.name.trim() || null,
        proficiency: item.proficiency.trim() || null,
      })),
    };
  }

  private buildAiContext(): Record<string, unknown> {
    return {
      theme: this.selectedTemplate,
      profile: {
        fullName: this.profile.fullName,
        headline: this.profile.headline,
        location: this.profile.location,
      },
      visibleProjects: this.visibleProjects.map((project) => ({
        id: project.id,
        title: project.title,
        description: project.description,
      })),
    };
  }

  private buildAssistantReply(response: CvAiChatResponse): CvAssistantMessage {
    return {
      role: 'assistant',
      content: response.reply?.trim() || 'The assistant returned an empty reply.',
      score: response.score ?? null,
      suggestedActions: (response.suggestedActions ?? []).filter(Boolean),
    };
  }

  private buildSaveErrorMessage(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 404) {
        return 'Load or generate a CV draft first.';
      }
      if (err.status === 400) {
        return messageFromHttpError(err, 'Validation error while saving your CV draft.');
      }
    }

    return messageFromHttpError(err, 'Unable to save your CV draft.');
  }

  private resolveTemplate(theme: string, preferredTemplate: string): CvBuilderTemplate {
    const themeKey = theme.trim().toUpperCase();
    if (themeKey === 'MODERN' || themeKey === 'ELEGANT' || themeKey === 'CREATIVE' || themeKey === 'ATS_MINIMAL') {
      return themeKey;
    }
    return preferredTemplate.trim().toLowerCase() === 'developer-minimal' ? 'MODERN' : 'ATS_MINIMAL';
  }

  private extractDraft(response: CvDraftApiResponse): CvDraftDto {
    return response.draft ?? response;
  }

  private persistCurrentCv(
    draftId: number,
    successMessage: string,
    onSuccess?: () => void,
    onError?: (err: unknown) => void,
  ): void {
    this.savingDraft = true;
    this.saveError = '';
    this.saveMessage = '';

    forkJoin({
      draft: this.cvApi.updateMyCvDraft(draftId, this.buildDraftUpdateRequest()),
      profile: this.cvApi.updateMyCvProfile(this.buildProfileUpdateRequest()),
    }).subscribe({
      next: ({ draft, profile }) => {
        this.savingDraft = false;
        this.profileResponse = profile;
        this.currentDraftSource = this.extractDraft(draft);
        this.currentDraftId = this.currentDraftSource.id ?? draftId;
        this.saveMessage = successMessage;
        onSuccess?.();
      },
      error: (err) => {
        this.savingDraft = false;
        this.saveError = this.buildSaveErrorMessage(err);
        onError?.(err);
      },
    });
  }

  private findDraftSection(sections: CvDraftSectionDto[], type: DraftSectionType): CvDraftSectionDto | null {
    return (
      [...sections]
        .filter((section) => (section.type ?? '').trim().toUpperCase() === type)
        .sort((left, right) => (left.orderIndex ?? 0) - (right.orderIndex ?? 0))[0] ?? null
    );
  }

  private mapSkill(value: unknown, fallbackId: number): CvPreviewSkillDto | null {
    if (typeof value === 'string') {
      const name = value.trim();
      return name ? { id: fallbackId, name, category: null } : null;
    }

    const data = this.normalizeObject(value);
    const name = this.readString(data, ['name']);
    if (!name) {
      return null;
    }

    return {
      id: this.readNumber(data, ['id']) ?? fallbackId,
      name,
      category: this.readString(data, ['category']) || null,
    };
  }

  private buildEmptyProfile(): EditorProfile {
    return {
      fullName: '',
      headline: '',
      email: '',
      phone: '',
      location: '',
      githubUrl: '',
      linkedinUrl: '',
      summary: '',
    };
  }

  private buildEmptyExperience(): EditorExperience {
    return {
      company: '',
      role: '',
      location: '',
      startDate: '',
      endDate: '',
      current: false,
      summary: '',
    };
  }

  private buildEmptyEducation(): EditorEducation {
    return {
      school: '',
      degree: '',
      fieldOfStudy: '',
      location: '',
      startDate: '',
      endDate: '',
      current: false,
      description: '',
    };
  }

  private buildEmptyLanguage(): EditorLanguage {
    return {
      name: '',
      proficiency: '',
    };
  }

  private buildEmptyJobMatchForm(): CvJobMatchForm {
    return {
      targetJobTitle: '',
      jobDescription: '',
      tone: 'ATS_PROFESSIONAL',
    };
  }

  private resolveJobMatchLanguage(): string {
    return this.profileResponse?.language?.trim() || 'EN';
  }

  private startChatTimeout(message: string): void {
    this.clearChatTimeout();
    this.chatTimeoutHandle = setTimeout(() => {
      if (this.chatLoading) {
        this.chatTimeoutMessage = message;
      }
    }, 30000);
  }

  private clearChatTimeout(): void {
    if (this.chatTimeoutHandle) {
      clearTimeout(this.chatTimeoutHandle);
      this.chatTimeoutHandle = null;
    }
  }

  private clearChatRequestState(keepInput: boolean): void {
    this.chatRequestSub?.unsubscribe();
    this.chatRequestSub = null;
    this.clearChatTimeout();
    this.chatLoading = false;
    this.chatTimeoutMessage = '';
    this.pendingChatContext = null;
    this.pendingChatMessage = keepInput ? this.pendingChatMessage : '';
  }

  private clone<T>(value: T): T {
    return JSON.parse(JSON.stringify(value)) as T;
  }

  private normalizeObject(value: unknown): Record<string, unknown> {
    if (!value || typeof value !== 'object' || Array.isArray(value)) {
      return {};
    }
    return value as Record<string, unknown>;
  }

  private readString(source: Record<string, unknown>, keys: string[]): string {
    for (const key of keys) {
      const value = source[key];
      if (typeof value === 'string' && value.trim()) {
        return value.trim();
      }
    }
    return '';
  }

  private readNumber(source: Record<string, unknown>, keys: string[]): number | null {
    for (const key of keys) {
      const value = source[key];
      if (typeof value === 'number' && Number.isFinite(value)) {
        return value;
      }
      if (typeof value === 'string' && value.trim()) {
        const parsed = Number(value);
        if (Number.isFinite(parsed)) {
          return parsed;
        }
      }
    }
    return null;
  }

  private readBoolean(source: Record<string, unknown>, keys: string[]): boolean {
    for (const key of keys) {
      if (typeof source[key] === 'boolean') {
        return source[key] as boolean;
      }
    }
    return false;
  }
}
