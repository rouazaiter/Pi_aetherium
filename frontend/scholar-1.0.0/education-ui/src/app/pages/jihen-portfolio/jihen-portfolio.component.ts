import { HttpErrorResponse } from '@angular/common/http';
import { Component, HostListener, OnInit, inject } from '@angular/core';
import { FormArray, FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription, forkJoin, of, switchMap } from 'rxjs';
import type {
  ApiErrorBody,
  CvAiChatRequest,
  CvAiChatResponse,
  CreateSkillRequest,
  CreateCollectionRequest,
  CreateProjectRequest,
  CvAiImproveRequest,
  CvAiImproveMaxLength,
  CvAiImproveTone,
  CvDraftApiResponse,
  CvDraftDto,
  CvDraftSectionDto,
  CvDraftUpdateRequest,
  CvPreviewProfileDto,
  CvPreviewProjectDto,
  CvPreviewSkillDto,
  CvPreviewSkillGroupDto,
  PortfolioCollectionDto,
  PortfolioProjectDto,
  PortfolioResponse,
  PortfolioUpsertRequest,
  PortfolioVisibility,
  SkillSummaryDto,
  UploadProjectMediaResponse,
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { JihenCvService } from '../../core/services/jihen-cv.service';
import { JihenPortfolioService } from '../../core/services/jihen-portfolio.service';
import { messageFromHttpError } from '../../core/util/http-error';

type JihenPortfolioStatIcon = 'globe' | 'eye' | 'stack' | 'folder';
type JihenPortfolioInfoIcon = 'calendar' | 'clock';
type JihenPortfolioFormMode = 'create' | 'edit';
type JihenProjectModalMode = 'create' | 'edit';
type JihenProjectModalOrigin = 'default' | 'collection';
type JihenSidebarIcon = 'home' | 'portfolio' | 'projects' | 'collections' | 'skills' | 'cv' | 'skillchat';
type JihenActiveSection = 'portfolio' | 'projects' | 'collections' | 'skills' | 'cv' | 'skillchat';
type JihenProjectSortOption = 'newest' | 'oldest' | 'name-asc' | 'name-desc';
type JihenCollectionSortOption = 'newest' | 'oldest' | 'name-asc' | 'name-desc';
type JihenActionMenuType = 'project' | 'collection' | 'skill';
type JihenDeleteTargetType = 'project' | 'collection' | 'skill';
type JihenCvTemplate = 'ats-minimal' | 'developer-minimal' | 'default';
type JihenCvAiTargetType = 'PROFILE_SUMMARY' | 'PROFILE_HEADLINE' | 'PROJECT_DESCRIPTION' | 'EXPERIENCE_SUMMARY' | 'EDUCATION_DESCRIPTION';
type JihenCvAiTarget = { type: JihenCvAiTargetType; index: number | null };
type JihenCvAssistantMessage = {
  role: 'user' | 'assistant';
  content: string;
  score?: number | null;
  suggestedActions?: string[];
};

type JihenPortfolioStat = {
  jihenLabel: string;
  jihenValue: string;
  jihenIcon: JihenPortfolioStatIcon;
  jihenAccent?: 'success' | 'warning' | 'muted';
};

type JihenPortfolioInfoItem = {
  jihenLabel: string;
  jihenValue: string;
  jihenIcon: JihenPortfolioInfoIcon;
};

type JihenPortfolioViewModel = {
  jihenTitle: string;
  jihenBio: string;
  jihenCoverImage: string;
  jihenAvatarImage: string;
  jihenJob: string;
  jihenGithubUrl: string;
  jihenLinkedinUrl: string;
  jihenOpenToWork: boolean;
  jihenAvailableForFreelance: boolean;
  jihenVisibility: PortfolioVisibility;
  jihenLocation: string;
  jihenMemberSince: string;
  jihenUpdatedAt: string;
  jihenOwnerUsername: string;
};

type JihenProjectMediaType = 'IMAGE' | 'VIDEO';

type JihenProjectMediaItem = {
  mediaUrl: string;
  mediaType: JihenProjectMediaType;
  orderIndex: number;
};

type JihenPortfolioProjectView = {
  jihenId: number;
  jihenTitle: string;
  jihenDescription: string;
  jihenProjectUrl: string;
  jihenPinned: boolean;
  jihenSkillIds: number[];
  jihenSkills: string[];
  jihenVisibility: PortfolioVisibility;
  jihenCoverImage: string;
  jihenCoverMediaType: JihenProjectMediaType | null;
  jihenMedia: JihenProjectMediaItem[];
  jihenCreatedAt: string | null;
  jihenUpdatedAt: string | null;
  jihenCreatedAtLabel: string;
  jihenUpdatedAtLabel: string;
};

type JihenPortfolioCollectionView = {
  jihenId: number;
  jihenSortIndex: number;
  jihenName: string;
  jihenDescription: string;
  jihenProjectCount: number;
  jihenVisibility: PortfolioVisibility;
  jihenProjectSummaries: string[];
  jihenCreatedAtLabel: string;
  jihenUpdatedAtLabel: string;
};

type JihenSkillCategoryGroup = {
  jihenCategoryKey: string;
  jihenCategoryLabel: string;
  jihenSkills: SkillSummaryDto[];
};

type JihenShareChannel = {
  jihenLabel: string;
  jihenTone: 'linkedin' | 'facebook' | 'instagram' | 'whatsapp' | 'mail' | 'gmail';
};

type JihenSidebarItem = {
  jihenKey: JihenSidebarIcon;
  jihenLabel: string;
  jihenSection: JihenActiveSection;
  jihenIcon: JihenSidebarIcon;
};

type JihenProjectSkillFilterOption = {
  jihenValue: string;
  jihenLabel: string;
};

type JihenOpenActionMenu = {
  jihenType: JihenActionMenuType;
  jihenId: number;
};

type JihenPendingDelete = {
  jihenType: JihenDeleteTargetType;
  jihenId: number;
};

type JihenCvSkillGroupView = {
  category: string;
  skills: CvPreviewSkillDto[];
};

type JihenCvProjectView = {
  id: number;
  title: string;
  description: string;
  imageUrl: string;
  projectUrl: string;
  collectionName: string;
  skills: CvPreviewSkillDto[];
};

type JihenCvExtraItemView = {
  heading: string;
  subheading: string;
  meta: string;
  body: string;
  tags: string[];
  lines: string[];
};

type JihenCvExtraSectionView = {
  type: string;
  title: string;
  items: JihenCvExtraItemView[];
};

type JihenEditableCvSectionType = 'PROFILE' | 'SKILLS' | 'EXPERIENCE' | 'EDUCATION' | 'LANGUAGES' | 'PROJECTS';

type JihenEditableCvProfileContent = {
  fullName: string;
  headline: string;
  email: string;
  phone: string;
  location: string;
  summary: string;
  githubUrl: string;
  linkedinUrl: string;
};

type JihenEditableCvSkillGroup = {
  category: string;
  skills: CvPreviewSkillDto[];
};

type JihenEditableCvExperienceItem = {
  company: string;
  role: string;
  location: string;
  startDate: string;
  endDate: string;
  current: boolean;
  summary: string;
};

type JihenEditableCvEducationItem = {
  school: string;
  degree: string;
  fieldOfStudy: string;
  location: string;
  startDate: string;
  endDate: string;
  current: boolean;
  description: string;
};

type JihenEditableCvLanguageItem = {
  name: string;
  proficiency: string;
};

type JihenEditableCvProjectItem = {
  title: string;
  description: string;
  projectUrl: string;
  imageUrl: string;
  collectionName: string;
  skills: CvPreviewSkillDto[];
};

type JihenEditableCvSection<T = unknown> = {
  type: JihenEditableCvSectionType;
  title: string;
  orderIndex: number;
  visible: boolean;
  content: T;
};

type JihenEditableCvDraft = {
  theme: string;
  settings: unknown;
  sections: Array<JihenEditableCvSection>;
  rawSections: CvDraftSectionDto[];
};

type JihenCvViewModel = {
  profile: CvPreviewProfileDto;
  profileTitle: string;
  skillsTitle: string;
  projectsTitle: string;
  skillsByCategory: JihenCvSkillGroupView[];
  projects: JihenCvProjectView[];
  extraSections: JihenCvExtraSectionView[];
  template: JihenCvTemplate;
};

@Component({
  selector: 'app-jihen-portfolio',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule],
  templateUrl: './jihen-portfolio.component.html',
  styleUrl: './jihen-portfolio.component.scss',
})
export class JihenPortfolioComponent implements OnInit {
  private readonly jihenFormBuilder = inject(FormBuilder);
  private readonly jihenPortfolioApi = inject(JihenPortfolioService);
  private readonly jihenCvApi = inject(JihenCvService);
  private readonly jihenAuth = inject(AuthService);
  private readonly jihenRouter = inject(Router);

  readonly jihenDefaultAvatarImage =
    'data:image/svg+xml;base64,PHN2ZyB4bWxucz0naHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmcnIHZpZXdCb3g9JzAgMCAyNDAgMjQwJz4KICA8ZGVmcz4KICAgIDxsaW5lYXJHcmFkaWVudCBpZD0nYmcnIHgxPScwJyB5MT0nMCcgeDI9JzEnIHkyPScxJz4KICAgICAgPHN0b3Agb2Zmc2V0PScwJScgc3RvcC1jb2xvcj0nI2Y2ZjFmZicvPgogICAgICA8c3RvcCBvZmZzZXQ9JzEwMCUnIHN0b3AtY29sb3I9JyNlY2U0ZmYnLz4KICAgIDwvbGluZWFyR3JhZGllbnQ+CiAgICA8bGluZWFyR3JhZGllbnQgaWQ9J3NoaXJ0JyB4MT0nMCcgeTE9JzAnIHgyPScxJyB5Mj0nMSc+CiAgICAgIDxzdG9wIG9mZnNldD0nMCUnIHN0b3AtY29sb3I9JyNmZmZmZmYnLz4KICAgICAgPHN0b3Agb2Zmc2V0PScxMDAlJyBzdG9wLWNvbG9yPScjZjFlOGZmJy8+CiAgICA8L2xpbmVhckdyYWRpZW50PgogIDwvZGVmcz4KICA8cmVjdCB3aWR0aD0nMjQwJyBoZWlnaHQ9JzI0MCcgcng9JzEyMCcgZmlsbD0ndXJsKCNiZyknLz4KICA8Y2lyY2xlIGN4PScxMjAnIGN5PSc5Nicgcj0nNDQnIGZpbGw9JyNmM2M4YWQnLz4KICA8cGF0aCBkPSdNNzIgOTdjMC00MCAyMy02NiA1OC02NiAzOSAwIDYxIDI3IDYxIDYzIDAgMTgtNiAzNC0xNyA0Ni0xMS0xMi0yMC0yOS0yMi00NC0yNyAyLTQ3LTMtNjMtMTQtNiAxNS0xMSAyOC0xNyA0MS0xLTcgMC0xNSAwLTI2WicgZmlsbD0nIzI2MTkzNScvPgogIDxwYXRoIGQ9J004NCAyMTJjOC0zMyAyNy00OSA1Ni00OSAzMSAwIDUwIDE2IDU4IDQ5SDg0WicgZmlsbD0ndXJsKCNzaGlydCknLz4KICA8cGF0aCBkPSdNMTA3IDE0NmgyNWMwIDEyIDkgMjEgMjAgMjctOCA3LTIwIDExLTMzIDExLTE0IDAtMjYtNC0zNC0xMiAxMy01IDIyLTE1IDIyLTI2WicgZmlsbD0nI2VmYzJhNCcvPgogIDxjaXJjbGUgY3g9JzEwNCcgY3k9Jzk2JyByPSc1JyBmaWxsPScjMWMxNTMwJy8+CiAgPGNpcmNsZSBjeD0nMTM5JyBjeT0nOTYnIHI9JzUnIGZpbGw9JyMxYzE1MzAnLz4KICA8cGF0aCBkPSdNMTEwIDEyMGM4IDYgMTcgNiAyNSAwJyBmaWxsPSdub25lJyBzdHJva2U9JyM5ZDVjNTQnIHN0cm9rZS1saW5lY2FwPSdyb3VuZCcgc3Ryb2tlLXdpZHRoPSc0Jy8+Cjwvc3ZnPg==';
  readonly jihenVisibilityOptions: PortfolioVisibility[] = ['PUBLIC', 'FRIENDS_ONLY', 'PRIVATE'];
  readonly jihenShareChannels: JihenShareChannel[] = [
    { jihenLabel: 'LinkedIn', jihenTone: 'linkedin' },
    { jihenLabel: 'Facebook', jihenTone: 'facebook' },
    { jihenLabel: 'Instagram', jihenTone: 'instagram' },
    { jihenLabel: 'WhatsApp', jihenTone: 'whatsapp' },
    { jihenLabel: 'Mail', jihenTone: 'mail' },
    { jihenLabel: 'Gmail', jihenTone: 'gmail' },
  ];
  readonly jihenAcceptedImageMimeTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif'];
  readonly jihenAcceptedVideoMimeTypes = ['video/mp4', 'video/webm', 'video/quicktime', 'video/ogg'];
  readonly jihenEditableCvSectionTypes: JihenEditableCvSectionType[] = ['PROFILE', 'SKILLS', 'EXPERIENCE', 'EDUCATION', 'LANGUAGES', 'PROJECTS'];
  readonly jihenSidebarItems: JihenSidebarItem[] = [
    { jihenKey: 'home', jihenLabel: 'Home', jihenSection: 'portfolio', jihenIcon: 'home' },
    { jihenKey: 'portfolio', jihenLabel: 'Portfolio', jihenSection: 'portfolio', jihenIcon: 'portfolio' },
    { jihenKey: 'projects', jihenLabel: 'Projects', jihenSection: 'projects', jihenIcon: 'projects' },
    { jihenKey: 'collections', jihenLabel: 'Collections', jihenSection: 'collections', jihenIcon: 'collections' },
    { jihenKey: 'skills', jihenLabel: 'Skills', jihenSection: 'skills', jihenIcon: 'skills' },
    { jihenKey: 'cv', jihenLabel: 'Generate CV', jihenSection: 'cv', jihenIcon: 'cv' },
    { jihenKey: 'skillchat', jihenLabel: 'SkillChat', jihenSection: 'skillchat', jihenIcon: 'skillchat' },
  ];
  readonly jihenProjectPlaceholders = [
    'data:image/svg+xml;base64,PHN2ZyB4bWxucz0naHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmcnIHZpZXdCb3g9JzAgMCAzMjAgMTkwJz4KICA8ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9J2JnJyB4MT0nMCcgeTE9JzAnIHgyPScxJyB5Mj0nMSc+PHN0b3Agb2Zmc2V0PScwJScgc3RvcC1jb2xvcj0nIzZkNTdmNycvPjxzdG9wIG9mZnNldD0nMTAwJScgc3RvcC1jb2xvcj0nIzQyMjRjOCcvPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPgogIDxyZWN0IHdpZHRoPSczMjAnIGhlaWdodD0nMTkwJyByeD0nMjYnIGZpbGw9J3VybCgjYmcpJy8+CiAgPHJlY3QgeD0nNTYnIHk9JzQyJyB3aWR0aD0nMTMyJyBoZWlnaHQ9Jzg4JyByeD0nMTAnIGZpbGw9JyMxYTE1NTgnIG9wYWNpdHk9Jy4zOCcvPgogIDxyZWN0IHg9JzY4JyB5PSc1NCcgd2lkdGg9JzEwOCcgaGVpZ2h0PSc2NCcgcng9JzgnIGZpbGw9JyNmZmZmZmYnIG9wYWNpdHk9Jy4xMicvPgogIDxyZWN0IHg9Jzg0JyB5PSc2Nycgd2lkdGg9JzU4JyBoZWlnaHQ9JzEwJyByeD0nNScgZmlsbD0nI2NhYmRmZicvPgogIDxyZWN0IHg9Jzg0JyB5PSc4Nicgd2lkdGg9Jzc1JyBoZWlnaHQ9JzgnIHJ4PSc0JyBmaWxsPScjZmZmZmZmJyBvcGFjaXR5PScuNjUnLz4KICA8cmVjdCB4PSc4NCcgeT0nMTAxJyB3aWR0aD0nNDknIGhlaWdodD0nOCcgcng9JzQnIGZpbGw9JyNmZmZmZmYnIG9wYWNpdHk9Jy40Jy8+CiAgPHBhdGggZD0nTTQ4IDE0MmgxNTBsMjAgMThINzJsLTI0LTE4WicgZmlsbD0nIzI0MTg3NScgb3BhY2l0eT0nLjcnLz4KICA8Y2lyY2xlIGN4PScyNjAnIGN5PSc1NScgcj0nMTgnIGZpbGw9JyNmZmZmZmYnIG9wYWNpdHk9Jy4xMicvPgogIDxjaXJjbGUgY3g9JzIzOCcgY3k9JzEzMCcgcj0nMjQnIGZpbGw9JyNmZmZmZmYnIG9wYWNpdHk9Jy4wOCcvPgo8L3N2Zz4=',
    'data:image/svg+xml;base64,PHN2ZyB4bWxucz0naHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmcnIHZpZXdCb3g9JzAgMCAzMjAgMTkwJz4KICA8ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9J2JnJyB4MT0nMCcgeTE9JzAnIHgyPScxJyB5Mj0nMSc+PHN0b3Agb2Zmc2V0PScwJScgc3RvcC1jb2xvcj0nI2ZmN2U4NycvPjxzdG9wIG9mZnNldD0nMTAwJScgc3RvcC1jb2xvcj0nIzdiNGRmZicvPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPgogIDxyZWN0IHdpZHRoPSczMjAnIGhlaWdodD0nMTkwJyByeD0nMjYnIGZpbGw9J3VybCgjYmcpJy8+CiAgPHJlY3QgeD0nMTE4JyB5PScyMicgd2lkdGg9Jzg0JyBoZWlnaHQ9JzE0Nicgcng9JzE4JyBmaWxsPScjZmZmZmZmJyBvcGFjaXR5PScuOTInLz4KICA8cmVjdCB4PScxMjgnIHk9JzQ0JyB3aWR0aD0nNjQnIGhlaWdodD0nMTInIHJ4PSc2JyBmaWxsPScjZDhkMGZmJy8+CiAgPHJlY3QgeD0nMTM2JyB5PSc2OCcgd2lkdGg9JzQ5JyBoZWlnaHQ9JzEwJyByeD0nNScgZmlsbD0nIzdkNTlmNycgb3BhY2l0eT0nLjgnLz4KICA8cmVjdCB4PScxMzYnIHk9Jzg4JyB3aWR0aD0nNDknIGhlaWdodD0nMTAnIHJ4PSc1JyBmaWxsPScjZjA5ZWMyJyBvcGFjaXR5PScuODUnLz4KICA8cmVjdCB4PScxMzYnIHk9JzEwOCcgd2lkdGg9JzQ5JyBoZWlnaHQ9JzEwJyByeD0nNScgZmlsbD0nIzdmZDNmZicgb3BhY2l0eT0nLjg1Jy8+CiAgPHJlY3QgeD0nMTM2JyB5PScxMjknIHdpZHRoPSczNicgaGVpZ2h0PSc4JyByeD0nNCcgZmlsbD0nI2Q1ZDVmNScvPgogIDxjaXJjbGUgY3g9JzcyJyBjeT0nNTgnIHI9JzE4JyBmaWxsPScjZmZmZmZmJyBvcGFjaXR5PScuMTgnLz4KICA8Y2lyY2xlIGN4PScyNDUnIGN5PScxMzYnIHI9JzI4JyBmaWxsPScjZmZmZmZmJyBvcGFjaXR5PScuMTInLz4KPC9zdmc+',
    'data:image/svg+xml;base64,PHN2ZyB4bWxucz0naHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmcnIHZpZXdCb3g9JzAgMCAzMjAgMTkwJz4KICA8ZGVmcz48bGluZWFyR3JhZGllbnQgaWQ9J2JnJyB4MT0nMCcgeTE9JzAnIHgyPScxJyB5Mj0nMSc+PHN0b3Agb2Zmc2V0PScwJScgc3RvcC1jb2xvcj0nIzExMTczNScvPjxzdG9wIG9mZnNldD0nMTAwJScgc3RvcC1jb2xvcj0nIzIwMmM2YScvPjwvbGluZWFyR3JhZGllbnQ+PC9kZWZzPgogIDxyZWN0IHdpZHRoPSczMjAnIGhlaWdodD0nMTkwJyByeD0nMjYnIGZpbGw9J3VybCgjYmcpJy8+CiAgPHJlY3QgeD0nMjgnIHk9JzIyJyB3aWR0aD0nNjInIGhlaWdodD0nMTQ2JyByeD0nMTInIGZpbGw9JyMwYjEwMjgnIG9wYWNpdHk9Jy42MicvPgogIDxyZWN0IHg9JzEwOCcgeT0nMjYnIHdpZHRoPScxODQnIGhlaWdodD0nNDYnIHJ4PScxNCcgZmlsbD0nIzEwMTgzZCcgb3BhY2l0eT0nLjknLz4KICA8cmVjdCB4PScxMDgnIHk9Jzg0JyB3aWR0aD0nODQnIGhlaWdodD0nMzYnIHJ4PScxMicgZmlsbD0nIzExMWE0OCcgb3BhY2l0eT0nLjk1Jy8+CiAgPHJlY3QgeD0nMjA4JyB5PSc4NCcgd2lkdGg9Jzg0JyBoZWlnaHQ9JzM2JyByeD0nMTInIGZpbGw9JyMxMTFhNDgnIG9wYWNpdHk9Jy45NScvPgogIDxyZWN0IHg9JzEwOCcgeT0nMTMyJyB3aWR0aD0nMTg0JyBoZWlnaHQ9JzI2JyByeD0nMTInIGZpbGw9JyMxMDE4M2QnIG9wYWNpdHk9Jy45NScvPgogIDxjaXJjbGUgY3g9JzE0OCcgY3k9JzQ5JyByPScxMScgZmlsbD0nIzZlNWFmNycvPgogIDxjaXJjbGUgY3g9JzE5MycgY3k9JzQ5JyByPScxMScgZmlsbD0nIzM1ZDdiNicvPgogIDxjaXJjbGUgY3g9JzIzOCcgY3k9JzQ5JyByPScxMScgZmlsbD0nI2ZmOWQ1YycvPgogIDxwYXRoIGQ9J00xMjQgMTQ2YzEyLTEzIDIyLTEyIDMyIDBzMjIgMTIgMzQtNCAyMS0xOCAzNCAwIDIzIDE3IDM0LTMnIGZpbGw9J25vbmUnIHN0cm9rZT0nIzZlNWFmNycgc3Ryb2tlLWxpbmVjYXA9J3JvdW5kJyBzdHJva2Utd2lkdGg9JzQnLz4KPC9zdmc+',
  ];

  jihenHasPortfolio = false;
  jihenLoadingPortfolio = true;
  jihenLoadingSkills = false;
  jihenLoadingSkillCategories = false;
  jihenLoadingSkillSearch = false;
  jihenLoadingCvDraft = false;
  jihenIsSubmittingPortfolio = false;
  jihenIsSavingSkills = false;
  jihenIsGeneratingCvDraft = false;
  jihenIsSavingCvDraft = false;
  jihenIsSubmittingProject = false;
  jihenIsSubmittingCollection = false;
  jihenIsDeletingItem = false;
  jihenIsUploadingProjectMedia = false;
  jihenIsEditModalOpen = false;
  jihenIsSkillModalOpen = false;
  jihenIsShareModalOpen = false;
  jihenIsProjectModalOpen = false;
  jihenIsCollectionModalOpen = false;
  jihenIsDeleteConfirmModalOpen = false;
  jihenIsCvAssistantOpen = false;
  jihenIsSidebarExpanded = false;
  jihenFormSubmitted = false;
  jihenProjectFormSubmitted = false;
  jihenCollectionFormSubmitted = false;
  jihenLinkCopied = false;
  jihenHasCvDraft = false;
  jihenCvMode: 'preview' | 'edit' = 'preview';
  jihenActiveSidebarItemKey: JihenSidebarIcon = 'portfolio';
  jihenActiveSection: JihenActiveSection = 'portfolio';
  jihenProjectSearchTerm = '';
  jihenPortfolioSkillSearchTerm = '';
  jihenProjectSort: JihenProjectSortOption = 'newest';
  jihenProjectVisibilityFilter: 'ALL' | PortfolioVisibility = 'ALL';
  jihenProjectSkillFilter = 'ALL';
  jihenSkillSearchTerm = '';
  jihenSkillModalSelectedCategory = '';
  jihenCollectionSearchTerm = '';
  jihenCollectionSort: JihenCollectionSortOption = 'newest';
  jihenCollectionVisibilityFilter: 'ALL' | PortfolioVisibility = 'ALL';
  jihenOpenActionMenu: JihenOpenActionMenu | null = null;
  jihenLoadError = '';
  jihenSkillsError = '';
  jihenSkillCategoriesError = '';
  jihenSkillsSaveError = '';
  jihenSkillsSaveSuccess = '';
  jihenCvDraftError = '';
  jihenCvGenerateError = '';
  jihenCvGenerateSuccess = '';
  jihenCvSaveError = '';
  jihenCvSaveSuccess = '';
  jihenCreatingSkillContext: 'portfolio-form' | 'project-form' | 'skills-modal' | null = null;
  jihenPortfolioSkillCreateError = '';
  jihenProjectSkillCreateError = '';
  jihenSkillModalCreateError = '';
  jihenFormError = '';
  jihenProjectFormError = '';
  jihenCollectionFormError = '';
  jihenModalMode: JihenPortfolioFormMode = 'edit';
  jihenProjectModalMode: JihenProjectModalMode = 'create';
  jihenProjectModalOrigin: JihenProjectModalOrigin = 'default';
  jihenEditingProjectId: number | null = null;
  jihenEditCoverPreview = '';
  jihenEditCoverFileName = '';
  jihenProjectExternalMediaUrl = '';
  jihenProjectExternalMediaType: JihenProjectMediaType = 'IMAGE';
  jihenProjectMediaError = '';
  jihenProjectMediaUploadLabel = '';
  jihenProjectActionErrors: Record<number, string> = {};
  jihenCollectionActionErrors: Record<number, string> = {};
  jihenProjectActionFeedback: Record<number, string> = {};
  jihenCollectionActionFeedback: Record<number, string> = {};
  jihenSkillActionFeedback: Record<number, string> = {};
  jihenCollectionPlaceholderMessage = '';
  jihenCollectionProjectSuccessMessage = '';
  jihenDeleteConfirmError = '';
  jihenPendingDelete: JihenPendingDelete | null = null;

  jihenPortfolio: JihenPortfolioViewModel = this.jihenBuildEmptyPortfolioModel();
  jihenSkillCategories: string[] = [];
  jihenAvailableSkills: SkillSummaryDto[] = [];
  jihenSkillSearchResults: SkillSummaryDto[] = [];
  jihenSelectedSkills: SkillSummaryDto[] = [];
  jihenSelectedPortfolioSkillIds: number[] = [];
  jihenSkillModalInitialSkillIds: number[] = [];
  jihenPortfolioSkillDraftName = '';
  jihenPortfolioSkillDraftCategory = '';
  jihenProjectSkillDraftName = '';
  jihenProjectSkillDraftCategory = '';
  jihenSkillModalDraftName = '';
  jihenSkillModalDraftCategory = '';
  jihenProjects: JihenPortfolioProjectView[] = [];
  jihenCollections: JihenPortfolioCollectionView[] = [];
  jihenCvDraft: JihenCvViewModel = this.jihenBuildEmptyCvView();
  jihenCvDraftSource: CvDraftDto | null = null;
  jihenEditableCvDraft: JihenEditableCvDraft = this.jihenBuildEmptyEditableCvDraft();
  jihenIsCvAiModalOpen = false;
  jihenIsCvAssistantLoading = false;
  jihenCvAiLoadingTarget: JihenCvAiTarget | null = null;
  jihenCvAiTarget: JihenCvAiTarget | null = null;
  jihenCvAiOriginalText = '';
  jihenCvAiSuggestedText = '';
  jihenCvAiError = '';
  jihenCvAiTone: CvAiImproveTone = 'ATS_PROFESSIONAL';
  jihenCvAssistantInput = '';
  jihenCvAssistantError = '';
  jihenCvAssistantMessages: JihenCvAssistantMessage[] = [];
  jihenCvAccordionState: Record<JihenEditableCvSectionType, boolean> = {
    PROFILE: true,
    SKILLS: true,
    EXPERIENCE: false,
    EDUCATION: false,
    LANGUAGES: false,
    PROJECTS: true,
  };

  readonly jihenPortfolioForm = this.jihenFormBuilder.nonNullable.group({
    title: ['', Validators.required],
    bio: [''],
    coverImage: [''],
    job: ['', Validators.required],
    githubUrl: [''],
    linkedinUrl: [''],
    openToWork: [true],
    availableForFreelance: [false],
    visibility: ['PUBLIC' as PortfolioVisibility, Validators.required],
    skillIds: this.jihenFormBuilder.nonNullable.control<number[]>([]),
  });

  readonly jihenProjectForm = this.jihenFormBuilder.nonNullable.group({
    title: ['', Validators.required],
    description: [''],
    projectUrl: [''],
    visibility: ['PUBLIC' as PortfolioVisibility, Validators.required],
    pinned: [false],
    skillIds: this.jihenFormBuilder.nonNullable.control<number[]>([]),
    media: this.jihenFormBuilder.array([]),
  });

  readonly jihenCollectionForm = this.jihenFormBuilder.nonNullable.group({
    name: ['', Validators.required],
    description: [''],
    visibility: ['PUBLIC' as PortfolioVisibility, Validators.required],
    projectIds: this.jihenFormBuilder.nonNullable.control<number[]>([]),
  });
  readonly jihenCvAiToneOptions: Array<{ value: CvAiImproveTone; label: string }> = [
    { value: 'ATS_PROFESSIONAL', label: 'ATS Professional' },
    { value: 'SHORT', label: 'Shorter' },
    { value: 'TECHNICAL', label: 'More Technical' },
    { value: 'SIMPLE', label: 'Simpler' },
  ];
  readonly jihenCvAssistantPrompts: string[] = [
    'Improve my CV',
    'Score my CV',
    'Make it ATS-friendly',
    'What should I remove?',
    'Rewrite my project descriptions',
    'Make my summary stronger',
  ];

  private jihenSkillSearchDebounceId: ReturnType<typeof setTimeout> | null = null;
  private jihenCvAiRequestSub: Subscription | null = null;
  private jihenCvAssistantRequestSub: Subscription | null = null;

  ngOnInit(): void {
    this.jihenLoadSkillCategories();
    this.jihenLoadSkills();
    this.jihenLoadPortfolio();
  }

  get jihenStats(): JihenPortfolioStat[] {
    return [
      {
        jihenLabel: 'Visibilit\u00e9',
        jihenValue: this.jihenPortfolio.jihenVisibility,
        jihenIcon: 'globe',
        jihenAccent:
          this.jihenPortfolio.jihenVisibility === 'PUBLIC'
            ? 'success'
            : this.jihenPortfolio.jihenVisibility === 'FRIENDS_ONLY'
              ? 'warning'
              : 'muted',
      },
      { jihenLabel: 'Vues du profil', jihenValue: '128', jihenIcon: 'eye' },
      { jihenLabel: 'Projets', jihenValue: String(this.jihenProjects.length), jihenIcon: 'stack' },
      { jihenLabel: 'Collections', jihenValue: String(this.jihenCollections.length), jihenIcon: 'folder' },
    ];
  }

  get jihenAboutItems(): JihenPortfolioInfoItem[] {
    return [
      { jihenLabel: 'Membre depuis', jihenValue: this.jihenPortfolio.jihenMemberSince, jihenIcon: 'calendar' },
      { jihenLabel: 'Derni\u00e8re mise \u00e0 jour', jihenValue: this.jihenPortfolio.jihenUpdatedAt, jihenIcon: 'clock' },
    ];
  }

  get jihenCvHasSummary(): boolean {
    return Boolean(this.jihenCvDraft.profile.summary?.trim());
  }

  get jihenCvHasSkills(): boolean {
    return this.jihenCvDraft.skillsByCategory.length > 0;
  }

  get jihenCvHasProjects(): boolean {
    return this.jihenCvDraft.projects.length > 0;
  }

  get jihenCvHasExtraSections(): boolean {
    return this.jihenCvDraft.extraSections.length > 0;
  }

  get jihenIsEditingCv(): boolean {
    return this.jihenCvMode === 'edit';
  }

  get jihenCvIsAtsMinimal(): boolean {
    return this.jihenCvDraft.template === 'ats-minimal';
  }

  get jihenEditableCvIsAtsMinimal(): boolean {
    return this.jihenEditableCvDraft.theme.trim().toUpperCase() === 'ATS_MINIMAL';
  }

  get jihenPreferredCvTemplateLabel(): string {
    if (this.jihenCvDraft.template === 'ats-minimal') {
      return 'ATS Minimal';
    }

    return this.jihenCvDraft.template === 'developer-minimal' ? 'Developer Minimal' : 'Default';
  }

  get jihenEditableProfileSection(): JihenEditableCvSection<JihenEditableCvProfileContent> {
    return this.jihenGetEditableCvSection<JihenEditableCvProfileContent>('PROFILE');
  }

  get jihenEditableSkillsSection(): JihenEditableCvSection<JihenEditableCvSkillGroup[]> {
    return this.jihenGetEditableCvSection<JihenEditableCvSkillGroup[]>('SKILLS');
  }

  get jihenEditableExperienceSection(): JihenEditableCvSection<JihenEditableCvExperienceItem[]> {
    return this.jihenGetEditableCvSection<JihenEditableCvExperienceItem[]>('EXPERIENCE');
  }

  get jihenEditableEducationSection(): JihenEditableCvSection<JihenEditableCvEducationItem[]> {
    return this.jihenGetEditableCvSection<JihenEditableCvEducationItem[]>('EDUCATION');
  }

  get jihenEditableLanguagesSection(): JihenEditableCvSection<JihenEditableCvLanguageItem[]> {
    return this.jihenGetEditableCvSection<JihenEditableCvLanguageItem[]>('LANGUAGES');
  }

  get jihenEditableProjectsSection(): JihenEditableCvSection<JihenEditableCvProjectItem[]> {
    return this.jihenGetEditableCvSection<JihenEditableCvProjectItem[]>('PROJECTS');
  }

  isJihenCvAccordionOpen(jihenType: JihenEditableCvSectionType): boolean {
    return this.jihenCvAccordionState[jihenType];
  }

  toggleJihenCvAccordion(jihenType: JihenEditableCvSectionType): void {
    this.jihenCvAccordionState[jihenType] = !this.jihenCvAccordionState[jihenType];
  }

  isJihenCvAiLoading(jihenType: JihenCvAiTargetType, jihenIndex: number | null = null): boolean {
    return this.jihenCvAiLoadingTarget?.type === jihenType && this.jihenCvAiLoadingTarget.index === jihenIndex;
  }

  get jihenCvAiBusy(): boolean {
    return this.jihenCvAiLoadingTarget !== null;
  }

  get jihenCanApplyCvAiSuggestion(): boolean {
    return !this.jihenCvAiBusy && this.jihenCvAiSuggestedText.trim().length > 0;
  }

  get jihenCanSendCvAssistantMessage(): boolean {
    return !this.jihenIsCvAssistantLoading && this.jihenCvAssistantInput.trim().length > 0;
  }

  get jihenCurrentCvDraftId(): number | null {
    return this.jihenCvDraftSource?.id ?? null;
  }

  openJihenCvAssistant(): void {
    this.jihenIsCvAssistantOpen = true;
    this.jihenCvAssistantError = '';

    if (this.jihenCvAssistantMessages.length === 0) {
      this.jihenCvAssistantMessages = [
        {
          role: 'assistant',
          content: 'Ask how to improve your CV. I can review clarity, ATS readiness, structure, and wording.',
        },
      ];
    }
  }

  closeJihenCvAssistant(): void {
    this.jihenCvAssistantRequestSub?.unsubscribe();
    this.jihenIsCvAssistantLoading = false;
    this.jihenIsCvAssistantOpen = false;
    this.jihenCvAssistantError = '';
  }

  sendJihenCvAssistantPrompt(prompt: string): void {
    this.jihenCvAssistantInput = prompt;
    this.sendJihenCvAssistantMessage();
  }

  sendJihenCvAssistantMessage(): void {
    const message = this.jihenCvAssistantInput.trim();
    if (!message || this.jihenIsCvAssistantLoading) {
      return;
    }

    const payload: CvAiChatRequest = {
      message,
      draftId: this.jihenCurrentCvDraftId,
      contextMode: 'CURRENT_CV',
    };

    this.openJihenCvAssistant();
    this.jihenCvAssistantError = '';
    this.jihenIsCvAssistantLoading = true;
    this.jihenCvAssistantInput = '';
    this.jihenCvAssistantMessages = [...this.jihenCvAssistantMessages, { role: 'user', content: message }];
    this.jihenCvAssistantRequestSub?.unsubscribe();

    this.jihenCvAssistantRequestSub = this.jihenCvApi.chatAboutMyCv(payload).subscribe({
      next: (response) => {
        this.jihenIsCvAssistantLoading = false;
        this.jihenCvAssistantMessages = [...this.jihenCvAssistantMessages, this.jihenBuildCvAssistantReply(response)];
      },
      error: (err) => {
        this.jihenIsCvAssistantLoading = false;
        this.jihenCvAssistantError = this.jihenBuildCvAssistantErrorMessage(err);
      },
    });
  }

  requestJihenCvAiSuggestion(jihenType: JihenCvAiTargetType, jihenIndex: number | null = null): void {
    if (this.jihenCvAiBusy) {
      return;
    }

    const original = this.jihenReadCvAiTargetText(jihenType, jihenIndex);
    if (!original.trim()) {
      return;
    }

    const target: JihenCvAiTarget = { type: jihenType, index: jihenIndex };

    this.jihenCvAiRequestSub?.unsubscribe();
    this.jihenIsCvAiModalOpen = true;
    this.jihenCvAiTarget = target;
    this.jihenCvAiOriginalText = original;
    this.jihenCvAiSuggestedText = '';
    this.jihenCvAiError = '';
    this.jihenCvAiLoadingTarget = target;

    const payload = this.jihenBuildCvAiImproveRequest(target, original);
    console.log('AI improve payload', payload);

    this.jihenCvAiRequestSub = this.jihenCvApi.improveMyCvText(payload).subscribe({
      next: (response) => {
        console.log('AI improve response', response);
        this.jihenCvAiLoadingTarget = null;
        this.jihenCvAiSuggestedText = (response.suggestion ?? '').trim();
        this.jihenCvAiError = this.jihenCvAiSuggestedText ? '' : 'AI request failed. The service returned an empty suggestion.';
      },
      error: (err) => {
        console.error('CV AI error', err);
        this.jihenCvAiLoadingTarget = null;
        this.jihenCvAiSuggestedText = '';
        this.jihenCvAiError = this.jihenBuildCvAiErrorMessage(err);
      },
    });
  }

  closeJihenCvAiModal(): void {
    if (this.jihenCvAiBusy) {
      this.jihenCvAiRequestSub?.unsubscribe();
      this.jihenCvAiLoadingTarget = null;
    }

    this.jihenIsCvAiModalOpen = false;
    this.jihenCvAiTarget = null;
    this.jihenCvAiOriginalText = '';
    this.jihenCvAiSuggestedText = '';
    this.jihenCvAiError = '';
  }

  retryJihenCvAiSuggestion(): void {
    if (!this.jihenCvAiTarget) {
      return;
    }

    this.requestJihenCvAiSuggestion(this.jihenCvAiTarget.type, this.jihenCvAiTarget.index);
  }

  applyJihenCvAiSuggestion(): void {
    if (!this.jihenCvAiTarget || !this.jihenCvAiSuggestedText.trim()) {
      return;
    }

    this.jihenWriteCvAiTargetText(this.jihenCvAiTarget.type, this.jihenCvAiTarget.index, this.jihenCvAiSuggestedText);
    this.closeJihenCvAiModal();
  }

  formatJihenCvAiTargetLabel(target: JihenCvAiTarget): string {
    switch (target.type) {
      case 'PROFILE_HEADLINE':
        return 'Headline';
      case 'PROFILE_SUMMARY':
        return 'Profile summary';
      case 'PROJECT_DESCRIPTION':
        return `Project ${typeof target.index === 'number' ? target.index + 1 : ''} description`.trim();
      case 'EXPERIENCE_SUMMARY':
        return `Experience ${typeof target.index === 'number' ? target.index + 1 : ''} summary`.trim();
      case 'EDUCATION_DESCRIPTION':
        return `Education ${typeof target.index === 'number' ? target.index + 1 : ''} description`.trim();
      default:
        return 'CV text';
    }
  }

  private jihenReadCvAiTargetText(jihenType: JihenCvAiTargetType, jihenIndex: number | null): string {
    switch (jihenType) {
      case 'PROFILE_HEADLINE':
        return this.jihenEditableProfileSection.content.headline ?? '';
      case 'PROFILE_SUMMARY':
        return this.jihenEditableProfileSection.content.summary ?? '';
      case 'PROJECT_DESCRIPTION':
        return typeof jihenIndex === 'number' ? this.jihenEditableProjectsSection.content[jihenIndex]?.description ?? '' : '';
      case 'EXPERIENCE_SUMMARY':
        return typeof jihenIndex === 'number' ? this.jihenEditableExperienceSection.content[jihenIndex]?.summary ?? '' : '';
      case 'EDUCATION_DESCRIPTION':
        return typeof jihenIndex === 'number' ? this.jihenEditableEducationSection.content[jihenIndex]?.description ?? '' : '';
      default:
        return '';
    }
  }

  private jihenWriteCvAiTargetText(jihenType: JihenCvAiTargetType, jihenIndex: number | null, value: string): void {
    switch (jihenType) {
      case 'PROFILE_HEADLINE':
        this.jihenEditableProfileSection.content.headline = value;
        return;
      case 'PROFILE_SUMMARY':
        this.jihenEditableProfileSection.content.summary = value;
        return;
      case 'PROJECT_DESCRIPTION':
        if (typeof jihenIndex === 'number' && this.jihenEditableProjectsSection.content[jihenIndex]) {
          this.jihenEditableProjectsSection.content[jihenIndex].description = value;
        }
        return;
      case 'EXPERIENCE_SUMMARY':
        if (typeof jihenIndex === 'number' && this.jihenEditableExperienceSection.content[jihenIndex]) {
          this.jihenEditableExperienceSection.content[jihenIndex].summary = value;
        }
        return;
      case 'EDUCATION_DESCRIPTION':
        if (typeof jihenIndex === 'number' && this.jihenEditableEducationSection.content[jihenIndex]) {
          this.jihenEditableEducationSection.content[jihenIndex].description = value;
        }
        return;
      default:
        return;
    }
  }

  private jihenBuildCvAiImproveRequest(target: JihenCvAiTarget, text: string): CvAiImproveRequest {
    return {
      topic: this.formatJihenCvAiTargetLabel(target),
      sectionType: this.jihenBuildCvAiSectionType(target),
      field: this.jihenBuildCvAiFieldName(target),
      text: text.trim(),
      targetTone: this.jihenCvAiTone,
      maxLength: this.jihenBuildCvAiMaxLength(target),
      context: this.jihenBuildCvAiContext(target),
    };
  }

  private jihenBuildCvAiContext(target: JihenCvAiTarget): Record<string, unknown> {
    return {
      language: 'English',
      targetLabel: this.formatJihenCvAiTargetLabel(target),
      theme: this.jihenEditableCvDraft.theme,
      profile: {
        fullName: this.jihenEditableProfileSection.content.fullName,
        headline: this.jihenEditableProfileSection.content.headline,
        summary: this.jihenEditableProfileSection.content.summary,
        location: this.jihenEditableProfileSection.content.location,
      },
      project: typeof target.index === 'number' ? this.jihenEditableProjectsSection.content[target.index] ?? null : null,
      experience: typeof target.index === 'number' ? this.jihenEditableExperienceSection.content[target.index] ?? null : null,
      education: typeof target.index === 'number' ? this.jihenEditableEducationSection.content[target.index] ?? null : null,
    };
  }

  private jihenBuildCvAiSectionType(target: JihenCvAiTarget): string {
    switch (target.type) {
      case 'PROFILE_HEADLINE':
      case 'PROFILE_SUMMARY':
        return 'PROFILE';
      case 'PROJECT_DESCRIPTION':
        return 'PROJECTS';
      case 'EXPERIENCE_SUMMARY':
        return 'EXPERIENCE';
      case 'EDUCATION_DESCRIPTION':
        return 'EDUCATION';
      default:
        return 'PROFILE';
    }
  }

  private jihenBuildCvAiFieldName(target: JihenCvAiTarget): string {
    switch (target.type) {
      case 'PROFILE_HEADLINE':
        return 'headline';
      case 'PROFILE_SUMMARY':
        return 'summary';
      case 'PROJECT_DESCRIPTION':
        return 'description';
      case 'EXPERIENCE_SUMMARY':
        return 'summary';
      case 'EDUCATION_DESCRIPTION':
        return 'description';
      default:
        return 'text';
    }
  }

  private jihenBuildCvAiMaxLength(target: JihenCvAiTarget): CvAiImproveMaxLength {
    switch (target.type) {
      case 'PROFILE_HEADLINE':
      case 'PROJECT_DESCRIPTION':
        return 'SHORT';
      case 'PROFILE_SUMMARY':
      case 'EXPERIENCE_SUMMARY':
      case 'EDUCATION_DESCRIPTION':
        return 'MEDIUM';
      default:
        return 'MEDIUM';
    }
  }

  private jihenBuildCvAiErrorMessage(err: unknown): string {
    if (this.jihenIsCvAiUnavailableError(err)) {
      return 'AI unavailable';
    }

    return messageFromHttpError(err, 'AI request failed. Unable to generate a suggestion.');
  }

  private jihenIsCvAiUnavailableError(err: unknown): boolean {
    if (!(err instanceof HttpErrorResponse)) {
      return false;
    }

    if (err.status === 0 || err.status === 502 || err.status === 503 || err.status === 504) {
      return true;
    }

    const body = err.error as ApiErrorBody | string | null;
    const rawMessage =
      typeof body === 'string'
        ? body
        : [body?.error, body?.detail, err.message]
            .filter((value): value is string => typeof value === 'string' && value.trim().length > 0)
            .join(' ');

    return /ollama|connection refused|failed to connect|service unavailable|timed out|unavailable/i.test(rawMessage);
  }

  private jihenBuildCvAssistantReply(response: CvAiChatResponse): JihenCvAssistantMessage {
    return {
      role: 'assistant',
      content: (response.reply ?? '').trim() || 'Could not get AI response.',
      score: response.score ?? null,
      suggestedActions: (response.suggestedActions ?? []).filter(
        (action): action is string => typeof action === 'string' && action.trim().length > 0,
      ),
    };
  }

  private jihenBuildCvAssistantErrorMessage(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 404) {
        return 'Generate a CV draft first.';
      }

      if (err.status === 503) {
        return 'AI service is unavailable. Make sure Ollama is running.';
      }
    }

    return 'Could not get AI response.';
  }

  get jihenShareUrl(): string {
    return `http://localhost:4200/portfolio/${this.jihenSlugify(this.jihenPortfolio.jihenOwnerUsername || this.jihenPortfolio.jihenTitle)}`;
  }

  get jihenHeroCoverImage(): string {
    if (this.jihenIsEditModalOpen && this.jihenEditCoverPreview.trim()) {
      return this.jihenEditCoverPreview.trim();
    }
    return this.jihenPortfolio.jihenCoverImage.trim();
  }

  get jihenHeroHasCoverImage(): boolean {
    return Boolean(this.jihenHeroCoverImage);
  }

  get jihenHeroBackgroundImage(): string | null {
    return this.jihenBuildCoverBackgroundImage(this.jihenHeroCoverImage);
  }

  get jihenEditCoverBackgroundImage(): string | null {
    return this.jihenBuildCoverBackgroundImage(this.jihenEditCoverPreview);
  }

  get jihenHasEditCoverPreview(): boolean {
    return Boolean(this.jihenEditCoverPreview.trim());
  }

  get jihenProjectSkillGroups(): JihenSkillCategoryGroup[] {
    return this.jihenGroupSkillsByCategory(this.jihenAvailableSkills);
  }

  get jihenProjectMediaArray(): FormArray {
    return this.jihenProjectForm.get('media') as FormArray;
  }

  get jihenProjectVisibilityWarning(): string {
    return this.isJihenProjectVisibilityAllowed(this.jihenProjectForm.controls.visibility.value)
      ? ''
      : 'Veuillez choisir une visibilit\u00e9 compatible avec votre portfolio.';
  }

  get jihenCollectionVisibilityWarning(): string {
    return this.isJihenProjectVisibilityAllowed(this.jihenCollectionForm.controls.visibility.value)
      ? ''
      : 'Veuillez choisir une visibilit\u00e9 compatible avec votre portfolio.';
  }

  get jihenDeleteConfirmMessage(): string {
    switch (this.jihenPendingDelete?.jihenType) {
      case 'project':
        return '\u00cates-vous s\u00fbre de vouloir supprimer ce projet ?';
      case 'collection':
        return '\u00cates-vous s\u00fbre de vouloir supprimer cette collection ?';
      case 'skill':
        return '\u00cates-vous s\u00fbre de vouloir supprimer cette comp\u00e9tence ?';
      default:
        return '\u00cates-vous s\u00fbre de vouloir supprimer cet \u00e9l\u00e9ment ?';
    }
  }

  get jihenFeaturedProjects(): JihenPortfolioProjectView[] {
    return [...this.jihenProjects]
      .sort((a, b) => Number(b.jihenPinned) - Number(a.jihenPinned) || this.jihenProjectTimestamp(b) - this.jihenProjectTimestamp(a))
      .slice(0, 3);
  }

  get jihenSelectedPortfolioSkills(): SkillSummaryDto[] {
    return this.jihenResolveSkillsByIds(this.jihenSelectedPortfolioSkillIds);
  }

  get jihenSelectedPortfolioSkillGroups(): JihenSkillCategoryGroup[] {
    const jihenSearchTerm = this.jihenNormalizeForMatching(this.jihenPortfolioSkillSearchTerm);
    const jihenFilteredSkills = this.jihenSelectedPortfolioSkills.filter((jihenSkill) => {
      if (!jihenSearchTerm) {
        return true;
      }

      return this.jihenNormalizeForMatching(jihenSkill.name).includes(jihenSearchTerm);
    });

    return this.jihenGroupSkillsByCategory(jihenFilteredSkills);
  }

  get jihenFilteredSkillCatalogGroups(): JihenSkillCategoryGroup[] {
    return this.jihenGroupSkillsByCategory(this.jihenSkillSearchTerm.trim() ? this.jihenSkillSearchResults : this.jihenAvailableSkills);
  }

  get jihenSkillModalVisibleSkills(): SkillSummaryDto[] {
    if (!this.jihenSkillModalSelectedCategory) {
      return [];
    }

    const jihenSkillSource = this.jihenSkillSearchTerm.trim() ? this.jihenSkillSearchResults : this.jihenAvailableSkills;
    return jihenSkillSource
      .filter((jihenSkill) => (jihenSkill.category ?? '').trim() === this.jihenSkillModalSelectedCategory)
      .sort((a, b) => a.name.localeCompare(b.name, 'fr'));
  }

  get jihenHasPortfolioSkillChanges(): boolean {
    return !this.jihenHaveSameSkillIds(this.jihenSelectedPortfolioSkillIds, this.jihenSelectedSkills.map((jihenSkill) => jihenSkill.id));
  }

  get jihenFeaturedCollections(): JihenPortfolioCollectionView[] {
    return this.jihenCollections.slice(0, 2);
  }

  get jihenFilteredCollections(): JihenPortfolioCollectionView[] {
    const jihenSearchTerm = this.jihenNormalizeForMatching(this.jihenCollectionSearchTerm);

    const jihenFilteredCollections = this.jihenCollections.filter((jihenCollection) => {
      const jihenMatchesSearch =
        !jihenSearchTerm ||
        this.jihenNormalizeForMatching(`${jihenCollection.jihenName} ${jihenCollection.jihenDescription}`).includes(jihenSearchTerm);

      const jihenMatchesVisibility =
        this.jihenCollectionVisibilityFilter === 'ALL' ||
        jihenCollection.jihenVisibility === this.jihenCollectionVisibilityFilter;

      return jihenMatchesSearch && jihenMatchesVisibility;
    });

    return jihenFilteredCollections.sort((a, b) => {
      switch (this.jihenCollectionSort) {
        case 'oldest':
          return a.jihenSortIndex - b.jihenSortIndex;
        case 'name-asc':
          return a.jihenName.localeCompare(b.jihenName, 'fr');
        case 'name-desc':
          return b.jihenName.localeCompare(a.jihenName, 'fr');
        default:
          return b.jihenSortIndex - a.jihenSortIndex;
      }
    });
  }

  @HostListener('document:click', ['$event'])
  onJihenDocumentClick(event: MouseEvent): void {
    const jihenTarget = event.target as HTMLElement | null;
    if (jihenTarget?.closest('.jihen-action-menu-wrap')) {
      return;
    }

    this.closeJihenActionMenu();
  }

  get jihenProjectSkillFilterOptions(): JihenProjectSkillFilterOption[] {
    const jihenSkillOptions = new Map<string, JihenProjectSkillFilterOption>();

    this.jihenAvailableSkills.forEach((jihenSkill) => {
      jihenSkillOptions.set(`skill:${jihenSkill.id}`, {
        jihenValue: `skill:${jihenSkill.id}`,
        jihenLabel: jihenSkill.name,
      });
    });

    this.jihenProjects.forEach((jihenProject) => {
      jihenProject.jihenSkills.forEach((jihenSkillName, index) => {
        const jihenSkillId = jihenProject.jihenSkillIds[index];
        const jihenValue = jihenSkillId ? `skill:${jihenSkillId}` : `name:${this.jihenNormalizeForMatching(jihenSkillName)}`;
        if (!jihenSkillOptions.has(jihenValue)) {
          jihenSkillOptions.set(jihenValue, {
            jihenValue,
            jihenLabel: jihenSkillName,
          });
        }
      });
    });

    return Array.from(jihenSkillOptions.values()).sort((a, b) => a.jihenLabel.localeCompare(b.jihenLabel, 'fr'));
  }

  get jihenFilteredProjects(): JihenPortfolioProjectView[] {
    const jihenSearchTerm = this.jihenNormalizeForMatching(this.jihenProjectSearchTerm);

    const jihenFilteredProjects = this.jihenProjects.filter((jihenProject) => {
      const jihenMatchesSearch =
        !jihenSearchTerm ||
        this.jihenNormalizeForMatching(`${jihenProject.jihenTitle} ${jihenProject.jihenDescription}`).includes(jihenSearchTerm);

      const jihenMatchesVisibility =
        this.jihenProjectVisibilityFilter === 'ALL' || jihenProject.jihenVisibility === this.jihenProjectVisibilityFilter;

      const jihenMatchesSkill =
        this.jihenProjectSkillFilter === 'ALL' || this.jihenProjectMatchesSkillFilter(jihenProject, this.jihenProjectSkillFilter);

      return jihenMatchesSearch && jihenMatchesVisibility && jihenMatchesSkill;
    });

    return jihenFilteredProjects.sort((a, b) => {
      switch (this.jihenProjectSort) {
        case 'oldest':
          return this.jihenProjectTimestamp(a) - this.jihenProjectTimestamp(b);
        case 'name-asc':
          return a.jihenTitle.localeCompare(b.jihenTitle, 'fr');
        case 'name-desc':
          return b.jihenTitle.localeCompare(a.jihenTitle, 'fr');
        default:
          return this.jihenProjectTimestamp(b) - this.jihenProjectTimestamp(a);
      }
    });
  }

  trackJihenCvSkillGroup(_: number, group: JihenCvSkillGroupView): string {
    return group.category;
  }

  trackJihenCvProject(_: number, project: JihenCvProjectView): number {
    return project.id;
  }

  trackJihenCvSkill(_: number, skill: CvPreviewSkillDto): number {
    return skill.id;
  }

  trackJihenCvExtraSection(_: number, section: JihenCvExtraSectionView): string {
    return section.type;
  }

  trackJihenCvExtraItem(index: number, item: JihenCvExtraItemView): string {
    return `${item.heading}-${item.subheading}-${index}`;
  }

  trackJihenEditableExperienceItem(index: number, item: JihenEditableCvExperienceItem): string {
    return `${item.company}-${item.role}-${index}`;
  }

  trackJihenEditableEducationItem(index: number, item: JihenEditableCvEducationItem): string {
    return `${item.school}-${item.degree}-${index}`;
  }

  trackJihenEditableLanguageItem(index: number, item: JihenEditableCvLanguageItem): string {
    return `${item.name}-${item.proficiency}-${index}`;
  }

  trackJihenEditableProjectItem(index: number, item: JihenEditableCvProjectItem): string {
    return `${item.title}-${index}`;
  }

  formatJihenCvSkillCategory(category: string | null | undefined): string {
    const jihenValue = (category ?? '').trim();
    if (!jihenValue) {
      return 'General';
    }

    return jihenValue
      .toLowerCase()
      .split('_')
      .filter(Boolean)
      .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  formatJihenCvSkillNames(skills: CvPreviewSkillDto[]): string {
    return skills
      .map((skill) => skill.name.trim())
      .filter(Boolean)
      .join(', ');
  }

  openJihenCreateModal(): void {
    this.jihenModalMode = 'create';
    this.jihenFormError = '';
    this.jihenFormSubmitted = false;

    const jihenSuggestedTitle = this.jihenAuth.auth()?.username?.trim() ?? '';
    this.jihenPortfolioForm.reset({
      title: jihenSuggestedTitle,
      bio: '',
      coverImage: '',
      job: '',
      githubUrl: '',
      linkedinUrl: '',
      openToWork: true,
      availableForFreelance: false,
      visibility: 'PUBLIC',
      skillIds: [],
    });

    this.jihenEditCoverPreview = '';
    this.jihenEditCoverFileName = '';
    this.jihenPortfolioSkillCreateError = '';
    this.jihenPortfolioSkillDraftName = '';
    this.jihenPortfolioSkillDraftCategory = '';
    this.jihenIsEditModalOpen = true;
  }

  openJihenEditModal(): void {
    this.jihenModalMode = 'edit';
    this.jihenFormError = '';
    this.jihenFormSubmitted = false;

    const jihenCurrentCoverImage = this.jihenPortfolio.jihenCoverImage.trim();
    this.jihenPortfolioForm.reset({
      title: this.jihenPortfolio.jihenTitle,
      bio: this.jihenPortfolio.jihenBio,
      coverImage: jihenCurrentCoverImage,
      job: this.jihenPortfolio.jihenJob,
      githubUrl: this.jihenPortfolio.jihenGithubUrl,
      linkedinUrl: this.jihenPortfolio.jihenLinkedinUrl,
      openToWork: this.jihenPortfolio.jihenOpenToWork,
      availableForFreelance: this.jihenPortfolio.jihenAvailableForFreelance,
      visibility: this.jihenPortfolio.jihenVisibility,
      skillIds: this.jihenSelectedSkills.map((jihenSkill) => jihenSkill.id),
    });

    this.jihenEditCoverPreview = jihenCurrentCoverImage;
    this.jihenEditCoverFileName = '';
    this.jihenPortfolioSkillCreateError = '';
    this.jihenPortfolioSkillDraftName = '';
    this.jihenPortfolioSkillDraftCategory = '';
    this.jihenIsEditModalOpen = true;
  }

  closeJihenEditModal(): void {
    this.jihenIsEditModalOpen = false;
    this.jihenFormSubmitted = false;
    this.jihenFormError = '';
    this.jihenPortfolioSkillCreateError = '';
  }

  onJihenCoverImageUrlInput(): void {
    const jihenCoverImageValue = this.jihenPortfolioForm.controls.coverImage.value.trim();
    this.jihenEditCoverPreview = jihenCoverImageValue;
    this.jihenEditCoverFileName = '';
  }

  onJihenCoverImageFileSelected(event: Event): void {
    const jihenInput = event.target as HTMLInputElement;
    const jihenFile = jihenInput.files?.[0];
    if (!jihenFile) {
      return;
    }

    const jihenReader = new FileReader();
    jihenReader.onload = () => {
      if (typeof jihenReader.result !== 'string') {
        return;
      }

      this.jihenEditCoverPreview = jihenReader.result;
      this.jihenEditCoverFileName = jihenFile.name;
      this.jihenPortfolioForm.controls.coverImage.setValue('');
      jihenInput.value = '';
    };
    jihenReader.readAsDataURL(jihenFile);
  }

  hasJihenSkillSelected(jihenSkillId: number): boolean {
    return this.jihenPortfolioForm.controls.skillIds.value.includes(jihenSkillId);
  }

  hasJihenPortfolioSkillSelected(jihenSkillId: number): boolean {
    return this.jihenSelectedPortfolioSkillIds.includes(jihenSkillId);
  }

  toggleJihenSkill(jihenSkillId: number): void {
    const jihenCurrentSkillIds = this.jihenPortfolioForm.controls.skillIds.value;
    const jihenNextSkillIds = jihenCurrentSkillIds.includes(jihenSkillId)
      ? jihenCurrentSkillIds.filter((id) => id !== jihenSkillId)
      : [...jihenCurrentSkillIds, jihenSkillId];
    this.jihenPortfolioForm.controls.skillIds.setValue(jihenNextSkillIds);
  }

  addOrCreateJihenPortfolioFormSkill(): void {
    this.jihenPortfolioSkillCreateError = '';
    this.jihenCreateOrReuseSkill(
      this.jihenPortfolioSkillDraftName,
      this.jihenPortfolioSkillDraftCategory,
      'portfolio-form',
      (jihenSkillId) => {
        const jihenCurrentSkillIds = this.jihenPortfolioForm.controls.skillIds.value;
        this.jihenPortfolioForm.controls.skillIds.setValue(this.jihenAppendUniqueSkillId(jihenCurrentSkillIds, jihenSkillId));
      },
      () => {
        this.jihenPortfolioSkillDraftName = '';
        this.jihenPortfolioSkillDraftCategory = '';
      },
      (jihenMessage) => {
        this.jihenPortfolioSkillCreateError = jihenMessage;
      },
    );
  }

  toggleJihenPortfolioSkillSelection(jihenSkillId: number): void {
    const jihenCurrentSkillIds = this.jihenSelectedPortfolioSkillIds;
    this.jihenSelectedPortfolioSkillIds = jihenCurrentSkillIds.includes(jihenSkillId)
      ? jihenCurrentSkillIds.filter((id) => id !== jihenSkillId)
      : [...jihenCurrentSkillIds, jihenSkillId];
    this.jihenSkillsSaveError = '';
    this.jihenSkillsSaveSuccess = '';
  }

  openJihenSkillModal(): void {
    this.jihenSkillModalInitialSkillIds = [...this.jihenSelectedPortfolioSkillIds];
    this.onJihenSkillSearchInput('');
    this.jihenSkillsSaveError = '';
    this.jihenSkillModalCreateError = '';
    this.jihenSkillModalDraftName = '';
    this.jihenSkillModalDraftCategory = '';
    this.jihenSkillModalSelectedCategory = '';
    this.jihenIsSkillModalOpen = true;
  }

  closeJihenSkillModal(restoreInitialSelection = true): void {
    if (this.jihenIsSavingSkills) {
      return;
    }

    if (restoreInitialSelection) {
      this.jihenSelectedPortfolioSkillIds = [...this.jihenSkillModalInitialSkillIds];
    }

    this.onJihenSkillSearchInput('');
    this.jihenSkillModalCreateError = '';
    this.jihenSkillModalSelectedCategory = '';
    this.jihenIsSkillModalOpen = false;
  }

  selectJihenSkillModalCategory(jihenCategory: string): void {
    this.jihenSkillModalSelectedCategory = jihenCategory;
    this.jihenSkillModalDraftCategory = jihenCategory;
    this.jihenSkillModalCreateError = '';
  }

  onJihenSkillSearchInput(jihenValue: string): void {
    this.jihenSkillSearchTerm = jihenValue;
    this.jihenSkillModalCreateError = '';

    if (this.jihenSkillSearchDebounceId) {
      globalThis.clearTimeout(this.jihenSkillSearchDebounceId);
      this.jihenSkillSearchDebounceId = null;
    }

    const jihenQuery = jihenValue.trim();
    if (!jihenQuery) {
      this.jihenLoadingSkillSearch = false;
      this.jihenSkillSearchResults = [];
      return;
    }

    this.jihenLoadingSkillSearch = true;
    this.jihenSkillSearchDebounceId = globalThis.setTimeout(() => {
      this.jihenPortfolioApi.getSkills(jihenQuery).subscribe({
        next: (jihenSkills) => {
          this.jihenLoadingSkillSearch = false;
          this.jihenSkillSearchResults = jihenSkills;
        },
        error: (err) => {
          this.jihenLoadingSkillSearch = false;
          this.jihenSkillSearchResults = [];
          this.jihenSkillsError = messageFromHttpError(err, 'Impossible de charger les compétences.');
        },
      });
    }, 250);
  }

  removeJihenPortfolioSkill(jihenSkillId: number): void {
    if (!this.hasJihenPortfolioSkillSelected(jihenSkillId)) {
      return;
    }

    this.jihenSelectedPortfolioSkillIds = this.jihenSelectedPortfolioSkillIds.filter((id) => id !== jihenSkillId);
    this.jihenSkillsSaveError = '';
    this.jihenSkillsSaveSuccess = '';
  }

  addOrCreateJihenSkillsModalSkill(): void {
    this.jihenSkillModalCreateError = '';
    this.jihenCreateOrReuseSkill(
      this.jihenSkillModalDraftName,
      this.jihenSkillModalDraftCategory,
      'skills-modal',
      (jihenSkillId) => {
        this.jihenSelectedPortfolioSkillIds = this.jihenAppendUniqueSkillId(this.jihenSelectedPortfolioSkillIds, jihenSkillId);
      },
      () => {
        this.jihenSkillModalDraftName = '';
        this.jihenSkillModalDraftCategory = '';
      },
      (jihenMessage) => {
        this.jihenSkillModalCreateError = jihenMessage;
      },
    );
  }

  openJihenShareModal(): void {
    this.jihenLinkCopied = false;
    this.jihenIsShareModalOpen = true;
  }

  expandJihenSidebar(): void {
    this.jihenIsSidebarExpanded = true;
  }

  collapseJihenSidebar(): void {
    this.jihenIsSidebarExpanded = false;
  }

  activateJihenSidebarItem(jihenSidebarItem: JihenSidebarItem): void {
    if (jihenSidebarItem.jihenSection === 'cv') {
      void this.jihenRouter.navigate(['/cv']);
      return;
    }

    this.jihenActiveSidebarItemKey = jihenSidebarItem.jihenKey;
    this.jihenActiveSection = jihenSidebarItem.jihenSection;
  }

  showJihenPortfolioDashboard(jihenSidebarKey: 'home' | 'portfolio' = 'portfolio'): void {
    this.jihenActiveSidebarItemKey = jihenSidebarKey;
    this.jihenActiveSection = 'portfolio';
  }

  showJihenProjectsView(): void {
    this.jihenActiveSidebarItemKey = 'projects';
    this.jihenActiveSection = 'projects';
  }

  showJihenCollectionsView(): void {
    this.jihenActiveSidebarItemKey = 'collections';
    this.jihenActiveSection = 'collections';
  }

  showJihenSkillsView(): void {
    this.jihenActiveSidebarItemKey = 'skills';
    this.jihenActiveSection = 'skills';
  }

  generateJihenCvDraft(): void {
    if (this.jihenIsGeneratingCvDraft) {
      return;
    }

    this.jihenCvGenerateError = '';
    this.jihenCvGenerateSuccess = '';
    this.jihenIsGeneratingCvDraft = true;

    this.jihenCvApi.generateMyCvDraft().subscribe({
      next: (jihenResponse) => {
        this.jihenIsGeneratingCvDraft = false;
        const jihenDraft = this.jihenExtractCvDraft(jihenResponse);
        if ((jihenDraft.sections ?? []).length > 0) {
          this.jihenApplyCvDraftResponse(jihenResponse);
        } else {
          this.jihenLoadLatestCvDraft();
        }
        this.jihenCvDraftError = '';
        this.jihenCvGenerateSuccess = 'CV draft generated successfully.';
        this.jihenCvSaveError = '';
        this.jihenCvMode = 'preview';
      },
      error: (err) => {
        this.jihenIsGeneratingCvDraft = false;
        this.jihenCvGenerateError = messageFromHttpError(err, 'Unable to generate your CV draft.');
      },
    });
  }

  openJihenCvEditor(): void {
    if (!this.jihenCvDraftSource) {
      return;
    }

    this.closeJihenCvAiModal();
    this.jihenEditableCvDraft = this.jihenBuildEditableCvDraft(this.jihenCvDraftSource);
    this.jihenCvSaveError = '';
    this.jihenCvSaveSuccess = '';
    this.jihenCvMode = 'edit';
  }

  cancelJihenCvEditor(): void {
    this.closeJihenCvAiModal();
    this.jihenCvMode = 'preview';
    this.jihenCvSaveError = '';
    if (this.jihenCvDraftSource) {
      this.jihenEditableCvDraft = this.jihenBuildEditableCvDraft(this.jihenCvDraftSource);
    }
  }

  saveJihenCvDraft(): void {
    const jihenDraftId = this.jihenCurrentCvDraftId;
    if (this.jihenIsSavingCvDraft || !jihenDraftId) {
      return;
    }

    this.closeJihenCvAiModal();
    this.jihenCvSaveError = '';
    this.jihenCvSaveSuccess = '';
    this.jihenIsSavingCvDraft = true;

    this.jihenCvApi.updateMyCvDraft(jihenDraftId, this.jihenBuildCvDraftUpdateRequest()).subscribe({
      next: () => {
        this.jihenIsSavingCvDraft = false;
        this.jihenCvMode = 'preview';
        this.jihenCvSaveSuccess = 'CV draft saved successfully.';
        this.jihenLoadLatestCvDraft();
      },
      error: (err) => {
        this.jihenIsSavingCvDraft = false;
        this.jihenCvSaveError = this.jihenBuildCvSaveErrorMessage(err);
      },
    });
  }

  addJihenCvExperienceItem(): void {
    this.jihenEditableExperienceSection.content = [
      ...this.jihenEditableExperienceSection.content,
      this.jihenBuildEmptyCvExperienceItem(),
    ];
  }

  removeJihenCvExperienceItem(index: number): void {
    this.jihenEditableExperienceSection.content = this.jihenEditableExperienceSection.content.filter((_, itemIndex) => itemIndex !== index);
  }

  addJihenCvEducationItem(): void {
    this.jihenEditableEducationSection.content = [
      ...this.jihenEditableEducationSection.content,
      this.jihenBuildEmptyCvEducationItem(),
    ];
  }

  removeJihenCvEducationItem(index: number): void {
    this.jihenEditableEducationSection.content = this.jihenEditableEducationSection.content.filter((_, itemIndex) => itemIndex !== index);
  }

  addJihenCvLanguageItem(): void {
    this.jihenEditableLanguagesSection.content = [
      ...this.jihenEditableLanguagesSection.content,
      this.jihenBuildEmptyCvLanguageItem(),
    ];
  }

  removeJihenCvLanguageItem(index: number): void {
    this.jihenEditableLanguagesSection.content = this.jihenEditableLanguagesSection.content.filter((_, itemIndex) => itemIndex !== index);
  }

  addJihenCvProjectItem(): void {
    this.jihenEditableProjectsSection.content = [
      ...this.jihenEditableProjectsSection.content,
      this.jihenBuildEmptyCvProjectItem(),
    ];
  }

  removeJihenCvProjectItem(index: number): void {
    this.jihenEditableProjectsSection.content = this.jihenEditableProjectsSection.content.filter((_, itemIndex) => itemIndex !== index);
  }

  clearJihenProjectFilters(): void {
    this.jihenProjectSearchTerm = '';
    this.jihenProjectSort = 'newest';
    this.jihenProjectVisibilityFilter = 'ALL';
    this.jihenProjectSkillFilter = 'ALL';
  }

  clearJihenCollectionFilters(): void {
    this.jihenCollectionSearchTerm = '';
    this.jihenCollectionSort = 'newest';
    this.jihenCollectionVisibilityFilter = 'ALL';
  }

  getJihenVisibilityActionTitle(jihenVisibility: PortfolioVisibility): string {
    return jihenVisibility === 'PRIVATE' ? 'Rendre visible' : 'Rendre priv\u00e9';
  }

  getJihenPinActionTitle(jihenPinned: boolean): string {
    return jihenPinned ? 'D\u00e9s\u00e9pingler' : '\u00c9pingler';
  }

  isJihenActionMenuOpen(jihenType: JihenActionMenuType, jihenId: number): boolean {
    return this.jihenOpenActionMenu?.jihenType === jihenType && this.jihenOpenActionMenu.jihenId === jihenId;
  }

  toggleJihenActionMenu(jihenType: JihenActionMenuType, jihenId: number, event: Event): void {
    event.stopPropagation();
    if (this.isJihenActionMenuOpen(jihenType, jihenId)) {
      this.jihenOpenActionMenu = null;
      return;
    }

    this.jihenOpenActionMenu = { jihenType, jihenId };
  }

  closeJihenActionMenu(): void {
    this.jihenOpenActionMenu = null;
  }

  openJihenDeleteConfirmModal(jihenType: JihenDeleteTargetType, jihenId: number): void {
    this.closeJihenActionMenu();
    this.jihenPendingDelete = { jihenType, jihenId };
    this.jihenDeleteConfirmError = '';
    this.jihenIsDeletingItem = false;
    this.jihenIsDeleteConfirmModalOpen = true;
  }

  closeJihenDeleteConfirmModal(): void {
    if (this.jihenIsDeletingItem) {
      return;
    }

    this.jihenIsDeleteConfirmModalOpen = false;
    this.jihenDeleteConfirmError = '';
    this.jihenPendingDelete = null;
  }

  confirmJihenDelete(): void {
    if (!this.jihenPendingDelete) {
      return;
    }

    this.jihenDeleteConfirmError = '';
    this.jihenIsDeletingItem = true;

    try {
      switch (this.jihenPendingDelete.jihenType) {
        case 'project':
          this.jihenDeleteProjectLocally(this.jihenPendingDelete.jihenId);
          break;
        case 'collection':
          this.jihenDeleteCollectionLocally(this.jihenPendingDelete.jihenId);
          break;
        case 'skill':
          this.jihenDeleteSkillLocally(this.jihenPendingDelete.jihenId);
          break;
      }

      this.jihenIsDeletingItem = false;
      this.jihenIsDeleteConfirmModalOpen = false;
      this.jihenPendingDelete = null;
    } catch {
      this.jihenIsDeletingItem = false;
      this.jihenDeleteConfirmError = 'Impossible de supprimer cet \u00e9l\u00e9ment.';
    }
  }

  async copyJihenProjectLink(jihenProjectId: number): Promise<void> {
    await this.copyJihenActionLink(`/jihen-portfolio/projects/${jihenProjectId}`, 'project', jihenProjectId);
  }

  async copyJihenCollectionLink(jihenCollectionId: number): Promise<void> {
    await this.copyJihenActionLink(`/jihen-portfolio/collections/${jihenCollectionId}`, 'collection', jihenCollectionId);
  }

  openJihenProjectEditModal(jihenProjectId: number): void {
    const jihenProject = this.jihenProjects.find((candidate) => candidate.jihenId === jihenProjectId);
    if (!jihenProject) {
      return;
    }

    this.closeJihenActionMenu();
    this.jihenProjectModalMode = 'edit';
    this.jihenEditingProjectId = jihenProjectId;
    this.jihenProjectFormSubmitted = false;
    this.jihenProjectFormError = '';
    this.jihenProjectForm.reset({
      title: jihenProject.jihenTitle,
      description: jihenProject.jihenDescription,
      projectUrl: jihenProject.jihenProjectUrl,
      visibility: jihenProject.jihenVisibility,
      pinned: jihenProject.jihenPinned,
      skillIds: [...jihenProject.jihenSkillIds],
    });
    this.jihenProjectMediaArray.clear();
    jihenProject.jihenMedia
      .slice()
      .sort((a, b) => a.orderIndex - b.orderIndex)
      .forEach((jihenMediaItem) => {
        this.jihenProjectMediaArray.push(
          this.jihenCreateProjectMediaGroup(jihenMediaItem.mediaType, {
            mediaUrl: jihenMediaItem.mediaUrl,
            orderIndex: jihenMediaItem.orderIndex,
          }),
        );
      });
    this.jihenProjectExternalMediaUrl = '';
    this.jihenProjectExternalMediaType = 'IMAGE';
    this.jihenProjectMediaError = '';
    this.jihenProjectMediaUploadLabel = '';
    this.jihenIsUploadingProjectMedia = false;
    this.jihenProjectSkillCreateError = '';
    this.jihenProjectSkillDraftName = '';
    this.jihenProjectSkillDraftCategory = '';
    this.jihenIsProjectModalOpen = true;
  }

  deleteJihenProject(jihenProjectId: number): void {
    this.openJihenDeleteConfirmModal('project', jihenProjectId);
  }

  editJihenCollection(jihenCollectionId: number): void {
    this.closeJihenActionMenu();
    this.jihenSetActionFeedback(
      'collection',
      jihenCollectionId,
      'Modification des collections bient\u00f4t disponible.',
    );
  }

  deleteJihenCollection(jihenCollectionId: number): void {
    this.openJihenDeleteConfirmModal('collection', jihenCollectionId);
  }

  editJihenSkill(jihenSkillId: number): void {
    this.closeJihenActionMenu();
    this.jihenSetActionFeedback(
      'skill',
      jihenSkillId,
      'Modification des comp\u00e9tences bient\u00f4t disponible.',
    );
  }

  deleteJihenSkill(jihenSkillId: number): void {
    this.openJihenDeleteConfirmModal('skill', jihenSkillId);
  }

  private jihenDeleteProjectLocally(jihenProjectId: number): void {
    this.jihenProjects = this.jihenProjects.filter((candidate) => candidate.jihenId !== jihenProjectId);
    this.jihenSetActionFeedback('project', jihenProjectId, 'Projet supprimé localement.');
    // TODO: Replace with DELETE /api/portfolio/me/projects/{projectId} when the endpoint is wired in JihenPortfolioService.
  }

  private jihenDeleteCollectionLocally(jihenCollectionId: number): void {
    this.jihenCollections = this.jihenCollections.filter((candidate) => candidate.jihenId !== jihenCollectionId);
    this.jihenSetActionFeedback('collection', jihenCollectionId, 'Collection supprimée localement.');
    // TODO: Replace with DELETE /api/portfolio/me/collections/{collectionId} when the endpoint is wired in JihenPortfolioService.
  }

  private jihenDeleteSkillLocally(jihenSkillId: number): void {
    this.jihenSetActionFeedback('skill', jihenSkillId, 'Suppression des compétences bientôt disponible.');
  }

  toggleJihenProjectVisibility(jihenProjectId: number): void {
    const jihenProject = this.jihenProjects.find((candidate) => candidate.jihenId === jihenProjectId);
    if (!jihenProject) {
      return;
    }

    this.jihenProjectActionErrors[jihenProjectId] = '';
    const jihenPreviousVisibility = jihenProject.jihenVisibility;
    jihenProject.jihenVisibility = this.jihenBuildNextChildVisibility(jihenProject.jihenVisibility);
    this.jihenProjects = [...this.jihenProjects];

    try {
      // TODO: Replace this local update with PUT /api/portfolio/me/projects/{projectId} when the endpoint is wired in JihenPortfolioService.
    } catch {
      jihenProject.jihenVisibility = jihenPreviousVisibility;
      this.jihenProjects = [...this.jihenProjects];
      this.jihenProjectActionErrors[jihenProjectId] =
        'La visibilit\u00e9 ne peut pas d\u00e9passer celle du portfolio.';
    }
  }

  toggleJihenProjectPinned(jihenProjectId: number): void {
    const jihenProject = this.jihenProjects.find((candidate) => candidate.jihenId === jihenProjectId);
    if (!jihenProject) {
      return;
    }

    this.jihenProjectActionErrors[jihenProjectId] = '';
    const jihenPreviousPinned = jihenProject.jihenPinned;
    jihenProject.jihenPinned = !jihenProject.jihenPinned;
    this.jihenProjects = [...this.jihenProjects];

    try {
      // TODO: Replace this local update with PUT /api/portfolio/me/projects/{projectId} when the endpoint is wired in JihenPortfolioService.
    } catch {
      jihenProject.jihenPinned = jihenPreviousPinned;
      this.jihenProjects = [...this.jihenProjects];
      this.jihenProjectActionErrors[jihenProjectId] = 'Impossible de mettre \u00e0 jour ce projet.';
    }
  }

  toggleJihenCollectionVisibility(jihenCollectionId: number): void {
    const jihenCollection = this.jihenCollections.find((candidate) => candidate.jihenId === jihenCollectionId);
    if (!jihenCollection) {
      return;
    }

    this.jihenCollectionActionErrors[jihenCollectionId] = '';
    const jihenPreviousVisibility = jihenCollection.jihenVisibility;
    jihenCollection.jihenVisibility = this.jihenBuildNextChildVisibility(jihenCollection.jihenVisibility);
    this.jihenCollections = [...this.jihenCollections];

    try {
      // TODO: Replace this local update with PUT /api/portfolio/me/collections/{collectionId} when the endpoint is wired in JihenPortfolioService.
    } catch {
      jihenCollection.jihenVisibility = jihenPreviousVisibility;
      this.jihenCollections = [...this.jihenCollections];
      this.jihenCollectionActionErrors[jihenCollectionId] =
        'La visibilit\u00e9 ne peut pas d\u00e9passer celle du portfolio.';
    }
  }

  openJihenProjectModal(): void {
    this.jihenProjectModalMode = 'create';
    this.jihenProjectModalOrigin = 'default';
    this.jihenEditingProjectId = null;
    this.jihenProjectFormSubmitted = false;
    this.jihenProjectFormError = '';
    this.jihenProjectForm.reset({
      title: '',
      description: '',
      projectUrl: '',
      visibility: this.jihenPortfolio.jihenVisibility,
      pinned: false,
      skillIds: [],
    });
    this.jihenProjectMediaArray.clear();
    this.jihenProjectExternalMediaUrl = '';
    this.jihenProjectExternalMediaType = 'IMAGE';
    this.jihenProjectMediaError = '';
    this.jihenProjectMediaUploadLabel = '';
    this.jihenIsUploadingProjectMedia = false;
    this.jihenProjectSkillCreateError = '';
    this.jihenProjectSkillDraftName = '';
    this.jihenProjectSkillDraftCategory = '';
    this.jihenIsProjectModalOpen = true;
  }

  openJihenProjectModalForCollection(): void {
    this.jihenCollectionPlaceholderMessage = '';
    this.jihenCollectionProjectSuccessMessage = '';
    this.jihenProjectModalOrigin = 'collection';
    this.jihenIsCollectionModalOpen = false;
    this.jihenProjectModalMode = 'create';
    this.jihenEditingProjectId = null;
    this.jihenProjectFormSubmitted = false;
    this.jihenProjectFormError = '';
    this.jihenProjectForm.reset({
      title: '',
      description: '',
      projectUrl: '',
      visibility: this.jihenPortfolio.jihenVisibility,
      pinned: false,
      skillIds: [],
    });
    this.jihenProjectMediaArray.clear();
    this.jihenProjectExternalMediaUrl = '';
    this.jihenProjectExternalMediaType = 'IMAGE';
    this.jihenProjectMediaError = '';
    this.jihenProjectMediaUploadLabel = '';
    this.jihenIsUploadingProjectMedia = false;
    this.jihenProjectSkillCreateError = '';
    this.jihenProjectSkillDraftName = '';
    this.jihenProjectSkillDraftCategory = '';
    this.jihenIsProjectModalOpen = true;
  }

  openJihenCollectionModal(): void {
    this.jihenCollectionFormSubmitted = false;
    this.jihenCollectionFormError = '';
    this.jihenCollectionPlaceholderMessage = '';
    this.jihenCollectionProjectSuccessMessage = '';
    this.jihenCollectionForm.reset({
      name: '',
      description: '',
      visibility: this.jihenPortfolio.jihenVisibility,
      projectIds: [],
    });
    this.jihenIsCollectionModalOpen = true;
  }

  closeJihenProjectModal(): void {
    const jihenShouldReturnToCollection = this.jihenProjectModalOrigin === 'collection';
    this.jihenIsProjectModalOpen = false;
    this.jihenProjectModalMode = 'create';
    this.jihenProjectModalOrigin = 'default';
    this.jihenEditingProjectId = null;
    this.jihenProjectFormSubmitted = false;
    this.jihenProjectFormError = '';
    this.jihenIsSubmittingProject = false;
    this.jihenIsUploadingProjectMedia = false;
    this.jihenProjectMediaError = '';
    this.jihenProjectMediaUploadLabel = '';
    this.jihenProjectSkillCreateError = '';
    if (jihenShouldReturnToCollection) {
      this.jihenIsCollectionModalOpen = true;
    }
  }

  closeJihenCollectionModal(): void {
    this.jihenIsCollectionModalOpen = false;
    this.jihenCollectionFormSubmitted = false;
    this.jihenCollectionFormError = '';
    this.jihenIsSubmittingCollection = false;
    this.jihenCollectionPlaceholderMessage = '';
  }

  closeJihenShareModal(): void {
    this.jihenIsShareModalOpen = false;
    this.jihenLinkCopied = false;
  }

  hasJihenProjectSkillSelected(jihenSkillId: number): boolean {
    return this.jihenProjectForm.controls.skillIds.value.includes(jihenSkillId);
  }

  hasJihenCollectionProjectSelected(jihenProjectId: number): boolean {
    return this.jihenCollectionForm.controls.projectIds.value.includes(jihenProjectId);
  }

  toggleJihenProjectSkill(jihenSkillId: number): void {
    const jihenCurrentSkillIds = this.jihenProjectForm.controls.skillIds.value;
    const jihenNextSkillIds = jihenCurrentSkillIds.includes(jihenSkillId)
      ? jihenCurrentSkillIds.filter((id) => id !== jihenSkillId)
      : [...jihenCurrentSkillIds, jihenSkillId];
    this.jihenProjectForm.controls.skillIds.setValue(jihenNextSkillIds);
  }

  addOrCreateJihenProjectSkill(): void {
    this.jihenProjectSkillCreateError = '';
    this.jihenCreateOrReuseSkill(
      this.jihenProjectSkillDraftName,
      this.jihenProjectSkillDraftCategory,
      'project-form',
      (jihenSkillId) => {
        const jihenCurrentSkillIds = this.jihenProjectForm.controls.skillIds.value;
        this.jihenProjectForm.controls.skillIds.setValue(this.jihenAppendUniqueSkillId(jihenCurrentSkillIds, jihenSkillId));
      },
      () => {
        this.jihenProjectSkillDraftName = '';
        this.jihenProjectSkillDraftCategory = '';
      },
      (jihenMessage) => {
        this.jihenProjectSkillCreateError = jihenMessage;
      },
    );
  }

  toggleJihenCollectionProject(jihenProjectId: number): void {
    const jihenCurrentProjectIds = this.jihenCollectionForm.controls.projectIds.value;
    const jihenNextProjectIds = jihenCurrentProjectIds.includes(jihenProjectId)
      ? jihenCurrentProjectIds.filter((id) => id !== jihenProjectId)
      : [...jihenCurrentProjectIds, jihenProjectId];
    this.jihenCollectionForm.controls.projectIds.setValue(jihenNextProjectIds);
  }

  onJihenProjectVisibilityChange(): void {
    const jihenSelectedVisibility = this.jihenProjectForm.controls.visibility.value;
    if (this.isJihenProjectVisibilityAllowed(jihenSelectedVisibility)) {
      return;
    }

    this.jihenProjectForm.controls.visibility.setValue(this.jihenPortfolio.jihenVisibility);
  }

  onJihenCollectionVisibilityChange(): void {
    const jihenSelectedVisibility = this.jihenCollectionForm.controls.visibility.value;
    if (this.isJihenProjectVisibilityAllowed(jihenSelectedVisibility)) {
      return;
    }

    this.jihenCollectionForm.controls.visibility.setValue(this.jihenPortfolio.jihenVisibility);
  }

  openJihenCollectionProjectPlaceholder(): void {
    this.openJihenProjectModalForCollection();
  }

  addJihenExternalProjectMedia(): void {
    const jihenMediaUrl = this.jihenProjectExternalMediaUrl.trim();
    if (!jihenMediaUrl) {
      this.jihenProjectMediaError = 'Ajoutez une URL de media.';
      return;
    }

    this.jihenProjectMediaError = '';
    this.jihenAppendProjectMediaItem(jihenMediaUrl, this.jihenProjectExternalMediaType);
    this.jihenProjectExternalMediaUrl = '';
    this.jihenProjectExternalMediaType = 'IMAGE';
  }

  onJihenProjectImageFileSelected(event: Event): void {
    this.jihenHandleProjectMediaUpload(event, 'IMAGE');
  }

  onJihenProjectVideoFileSelected(event: Event): void {
    this.jihenHandleProjectMediaUpload(event, 'VIDEO');
  }

  removeJihenProjectMedia(jihenIndex: number): void {
    this.jihenProjectMediaArray.removeAt(jihenIndex);
    this.jihenProjectMediaArray.controls.forEach((jihenMediaControl, index) => {
      jihenMediaControl.get('orderIndex')?.setValue(index);
    });
  }

  getJihenProjectMediaType(jihenIndex: number): JihenProjectMediaType {
    return (this.jihenProjectMediaArray.at(jihenIndex).get('mediaType')?.value as JihenProjectMediaType) ?? 'IMAGE';
  }

  getJihenProjectMediaUrl(jihenIndex: number): string {
    return String(this.jihenProjectMediaArray.at(jihenIndex).get('mediaUrl')?.value ?? '').trim();
  }

  async copyJihenShareLink(): Promise<void> {
    try {
      if (globalThis.navigator?.clipboard?.writeText) {
        await globalThis.navigator.clipboard.writeText(this.jihenShareUrl);
      } else {
        const jihenTextArea = document.createElement('textarea');
        jihenTextArea.value = this.jihenShareUrl;
        jihenTextArea.setAttribute('readonly', '');
        jihenTextArea.style.position = 'absolute';
        jihenTextArea.style.left = '-9999px';
        document.body.appendChild(jihenTextArea);
        jihenTextArea.select();
        document.execCommand('copy');
        document.body.removeChild(jihenTextArea);
      }
      this.jihenLinkCopied = true;
    } catch {
      this.jihenLinkCopied = false;
    }
  }

  saveJihenPortfolioSkills(jihenCloseModalOnSuccess = false): void {
    this.jihenSkillsSaveError = '';
    this.jihenSkillsSaveSuccess = '';
    this.jihenIsSavingSkills = true;

    this.jihenPortfolioApi
      .updateMyPortfolio({
        skillIds: [...this.jihenSelectedPortfolioSkillIds],
      })
      .subscribe({
        next: () => {
          this.jihenIsSavingSkills = false;
          this.jihenSkillsSaveSuccess = 'Compétences du portfolio mises à jour.';
          this.jihenSkillModalInitialSkillIds = [...this.jihenSelectedPortfolioSkillIds];
          if (jihenCloseModalOnSuccess) {
            this.closeJihenSkillModal(false);
          }
          this.jihenLoadPortfolio();
        },
        error: (err) => {
          this.jihenIsSavingSkills = false;
          this.jihenSkillsSaveError = this.jihenBuildSkillsUpdateErrorMessage(err);
        },
      });
  }

  submitJihenProject(): void {
    this.jihenProjectFormSubmitted = true;
    this.jihenProjectFormError = '';

    if (this.jihenProjectForm.invalid) {
      this.jihenProjectForm.markAllAsTouched();
      return;
    }

    if (this.jihenProjectModalMode === 'edit' && this.jihenEditingProjectId !== null) {
      this.jihenSubmitEditedProject(this.jihenEditingProjectId);
      return;
    }

    const jihenRequest = this.jihenBuildCreateProjectRequest();
    this.jihenIsSubmittingProject = true;

    this.jihenPortfolioApi.createProject(jihenRequest).subscribe({
      next: (jihenProject) => {
        this.jihenIsSubmittingProject = false;
        if (this.jihenProjectModalOrigin === 'collection') {
          this.jihenHandleCollectionProjectCreated(jihenProject);
          return;
        }
        this.closeJihenProjectModal();
        this.jihenLoadPortfolio();
      },
      error: (err) => {
        this.jihenIsSubmittingProject = false;
        this.jihenProjectFormError = this.jihenBuildProjectCreateErrorMessage(err);
      },
    });
  }

  submitJihenCollection(): void {
    this.jihenCollectionFormSubmitted = true;
    this.jihenCollectionFormError = '';

    if (this.jihenCollectionForm.invalid) {
      this.jihenCollectionForm.markAllAsTouched();
      return;
    }

    const jihenRequest = this.jihenBuildCreateCollectionRequest();
    const jihenSelectedProjectIds = [...this.jihenCollectionForm.controls.projectIds.value];
    this.jihenIsSubmittingCollection = true;

    this.jihenPortfolioApi
      .createCollection(jihenRequest)
      .pipe(
        switchMap((jihenCollection) => {
          if (jihenSelectedProjectIds.length === 0) {
            return of(jihenCollection);
          }

          return forkJoin(
            jihenSelectedProjectIds.map((jihenProjectId) =>
              this.jihenPortfolioApi.addProjectToCollection(jihenCollection.id, jihenProjectId),
            ),
          ).pipe(switchMap(() => of(jihenCollection)));
        }),
      )
      .subscribe({
        next: () => {
          this.jihenIsSubmittingCollection = false;
          this.closeJihenCollectionModal();
          this.jihenLoadPortfolio();
        },
        error: (err) => {
          this.jihenIsSubmittingCollection = false;
          this.jihenCollectionFormError = this.jihenBuildCollectionCreateErrorMessage(err);
        },
      });
  }

  submitJihenPortfolio(): void {
    this.jihenFormSubmitted = true;
    this.jihenFormError = '';
    if (this.jihenPortfolioForm.invalid) {
      this.jihenPortfolioForm.markAllAsTouched();
      return;
    }

    const jihenRequest = this.jihenBuildPortfolioRequest();
    this.jihenIsSubmittingPortfolio = true;

    const jihenRequest$ =
      this.jihenModalMode === 'create'
        ? this.jihenPortfolioApi.createMyPortfolio(jihenRequest)
        : this.jihenPortfolioApi.updateMyPortfolio(jihenRequest);

    jihenRequest$.subscribe({
      next: (jihenResponse) => {
        this.jihenIsSubmittingPortfolio = false;
        this.jihenApplyPortfolioResponse(jihenResponse);
        this.closeJihenEditModal();
      },
      error: (err) => {
        this.jihenIsSubmittingPortfolio = false;
        if (err instanceof HttpErrorResponse && this.jihenModalMode === 'edit' && err.status === 404) {
          this.jihenHasPortfolio = false;
          this.closeJihenEditModal();
        }
        if (err instanceof HttpErrorResponse && err.status === 409) {
          this.jihenLoadPortfolio();
        }
        this.jihenFormError = messageFromHttpError(
          err,
          this.jihenModalMode === 'create'
            ? 'Creation du portfolio impossible.'
            : 'Mise a jour du portfolio impossible.',
        );
      },
    });
  }

  onJihenImageError(event: Event): void {
    (event.target as HTMLImageElement).src = this.jihenDefaultAvatarImage;
  }

  onJihenCvProjectImageError(event: Event): void {
    const jihenImage = event.target as HTMLImageElement;
    jihenImage.onerror = null;
    jihenImage.style.display = 'none';
  }

  private jihenLoadLatestCvDraft(): void {
    this.jihenLoadingCvDraft = true;
    this.jihenCvDraftError = '';
    this.jihenCvGenerateError = '';

    this.jihenCvApi.getLatestMyCvDraft().subscribe({
      next: (jihenResponse) => {
        this.jihenLoadingCvDraft = false;
        this.jihenApplyCvDraftResponse(jihenResponse);
      },
      error: (err) => {
        this.jihenLoadingCvDraft = false;
        if (err instanceof HttpErrorResponse && err.status === 404) {
          this.jihenHasCvDraft = false;
          this.jihenCvMode = 'preview';
          this.jihenCvDraftError = '';
          this.jihenCvDraft = this.jihenBuildEmptyCvView();
          this.jihenCvDraftSource = null;
          this.jihenEditableCvDraft = this.jihenBuildEmptyEditableCvDraft();
          return;
        }

        this.jihenHasCvDraft = false;
        this.jihenCvDraftError = messageFromHttpError(err, 'Unable to load your latest CV draft.');
        this.jihenCvDraft = this.jihenBuildEmptyCvView();
        this.jihenCvDraftSource = null;
        this.jihenEditableCvDraft = this.jihenBuildEmptyEditableCvDraft();
      },
    });
  }

  private jihenLoadPortfolio(): void {
    this.jihenLoadingPortfolio = true;
    this.jihenLoadError = '';

    this.jihenPortfolioApi.getMyPortfolio().subscribe({
      next: (jihenResponse) => {
        this.jihenLoadingPortfolio = false;
        this.jihenApplyPortfolioResponse(jihenResponse);
      },
      error: (err) => {
        this.jihenLoadingPortfolio = false;
        if (err instanceof HttpErrorResponse && err.status === 404) {
          this.jihenHasPortfolio = false;
          this.jihenPortfolio = this.jihenBuildEmptyPortfolioModel();
          this.jihenSelectedSkills = [];
          this.jihenSelectedPortfolioSkillIds = [];
          this.jihenProjects = [];
          this.jihenCollections = [];
          return;
        }
        this.jihenLoadError = messageFromHttpError(err, 'Impossible de charger le portfolio.');
      },
    });
  }

  private jihenLoadSkills(): void {
    this.jihenLoadingSkills = true;
    this.jihenSkillsError = '';

    this.jihenPortfolioApi.getSkills().subscribe({
      next: (jihenSkills) => {
        this.jihenLoadingSkills = false;
        this.jihenAvailableSkills = jihenSkills;
        this.jihenSkillSearchResults = [];
        this.jihenSyncSelectedSkills();
      },
      error: (err) => {
        this.jihenLoadingSkills = false;
        this.jihenSkillsError = messageFromHttpError(err, 'Impossible de charger les comp\u00e9tences.');
      },
    });
  }

  private jihenLoadSkillCategories(): void {
    this.jihenLoadingSkillCategories = true;
    this.jihenSkillCategoriesError = '';

    this.jihenPortfolioApi.getSkillCategories().subscribe({
      next: (jihenCategories) => {
        this.jihenLoadingSkillCategories = false;
        this.jihenSkillCategories = [...jihenCategories];
      },
      error: (err) => {
        this.jihenLoadingSkillCategories = false;
        this.jihenSkillCategoriesError = messageFromHttpError(err, 'Impossible de charger les catégories.');
      },
    });
  }

  private jihenApplyPortfolioResponse(jihenResponse: PortfolioResponse): void {
    const jihenPortfolioDto = jihenResponse.portfolio;
    const jihenProfile = jihenResponse.profile ?? null;
    const jihenSkills = jihenPortfolioDto.skills ?? [];

    this.jihenHasPortfolio = true;
    this.jihenPortfolio = {
      jihenTitle: (jihenPortfolioDto.title ?? '').trim() || this.jihenBuildDisplayName(jihenResponse) || 'Mon portfolio',
      jihenBio: (jihenPortfolioDto.bio ?? jihenProfile?.description ?? '').trim(),
      jihenCoverImage: (jihenPortfolioDto.coverImage ?? '').trim(),
      jihenAvatarImage: (jihenProfile?.profilePicture ?? '').trim() || this.jihenDefaultAvatarImage,
      jihenJob: (jihenPortfolioDto.job ?? '').trim(),
      jihenGithubUrl: (jihenPortfolioDto.githubUrl ?? '').trim(),
      jihenLinkedinUrl: (jihenPortfolioDto.linkedinUrl ?? '').trim(),
      jihenOpenToWork: Boolean(jihenPortfolioDto.openToWork),
      jihenAvailableForFreelance: Boolean(jihenPortfolioDto.availableForFreelance),
      jihenVisibility: jihenPortfolioDto.visibility,
      jihenLocation: this.jihenBuildLocation(jihenProfile),
      jihenMemberSince: this.jihenFormatMonthYear(jihenPortfolioDto.createdAt),
      jihenUpdatedAt: this.jihenFormatUpdatedAt(jihenPortfolioDto.updatedAt),
      jihenOwnerUsername: (jihenResponse.owner?.username ?? '').trim(),
    };
    this.jihenSelectedSkills = jihenSkills;
    this.jihenSelectedPortfolioSkillIds = jihenSkills.map((jihenSkill) => jihenSkill.id);
    this.jihenProjects = this.jihenMapProjects(jihenResponse.projects);
    this.jihenCollections = this.jihenMapCollections(jihenResponse.collections, this.jihenProjects);
    this.jihenPortfolioForm.controls.skillIds.setValue(jihenSkills.map((jihenSkill) => jihenSkill.id));
    this.jihenSyncSelectedSkills();
  }

  private jihenApplyCvDraftResponse(jihenResponse: CvDraftApiResponse): void {
    const jihenDraft = this.jihenExtractCvDraft(jihenResponse);
    console.log('CV draft response', jihenDraft);
    this.jihenCvDraftSource = this.jihenCloneCvDraft(jihenDraft);
    this.jihenCvDraft = this.jihenMapCvDraft(jihenDraft);
    this.jihenEditableCvDraft = this.jihenBuildEditableCvDraft(jihenDraft);
    this.jihenHasCvDraft = true;
  }

  private jihenExtractCvDraft(jihenResponse: CvDraftApiResponse): CvDraftDto {
    return jihenResponse.draft ?? jihenResponse;
  }

  private jihenBuildEditableCvDraft(jihenDraft: CvDraftDto): JihenEditableCvDraft {
    const jihenKnownSectionTypes = new Set(this.jihenEditableCvSectionTypes);
    const jihenRawSections = (jihenDraft.sections ?? [])
      .filter((jihenSection) => !jihenKnownSectionTypes.has(((jihenSection.type ?? '').trim().toUpperCase() as JihenEditableCvSectionType)))
      .map((jihenSection) => this.jihenCloneCvValue(jihenSection) as CvDraftSectionDto);

    return {
      theme: (jihenDraft.theme ?? '').trim() || (this.jihenMapCvDraft(jihenDraft).template === 'ats-minimal' ? 'ATS_MINIMAL' : ''),
      settings: this.jihenCloneCvValue(jihenDraft.settings ?? null),
      sections: [
        this.jihenBuildEditableCvProfileSection(jihenDraft),
        this.jihenBuildEditableCvSkillsSection(jihenDraft),
        this.jihenBuildEditableCvExperienceSection(jihenDraft),
        this.jihenBuildEditableCvEducationSection(jihenDraft),
        this.jihenBuildEditableCvLanguagesSection(jihenDraft),
        this.jihenBuildEditableCvProjectsSection(jihenDraft),
      ],
      rawSections: jihenRawSections,
    };
  }

  private jihenBuildEditableCvProfileSection(jihenDraft: CvDraftDto): JihenEditableCvSection<JihenEditableCvProfileContent> {
    const jihenSection = this.jihenFindCvSection(jihenDraft, 'PROFILE');
    const jihenContent = this.jihenNormalizeCvObject(jihenSection?.content);

    return {
      type: 'PROFILE',
      title: jihenSection?.title?.trim() || 'Profile',
      orderIndex: jihenSection?.orderIndex ?? 0,
      visible: jihenSection?.visible !== false,
      content: {
        fullName: this.jihenReadCvString(jihenContent, ['fullName', 'name']) || this.jihenPortfolio.jihenTitle || '',
        headline: this.jihenReadCvString(jihenContent, ['headline', 'jobTitle', 'role', 'title']) || this.jihenPortfolio.jihenJob || '',
        email: this.jihenReadCvString(jihenContent, ['email']),
        phone: this.jihenReadCvString(jihenContent, ['phone', 'phoneNumber', 'mobile']),
        location: this.jihenReadCvString(jihenContent, ['location', 'city', 'country']) || this.jihenPortfolio.jihenLocation || '',
        summary:
          this.jihenReadCvString(jihenContent, ['summary', 'about', 'bio', 'description']) ||
          (typeof jihenSection?.content === 'string' ? jihenSection.content.trim() : ''),
        githubUrl: this.jihenReadCvString(jihenContent, ['githubUrl', 'github']) || this.jihenPortfolio.jihenGithubUrl || '',
        linkedinUrl: this.jihenReadCvString(jihenContent, ['linkedinUrl', 'linkedInUrl', 'linkedin']) || this.jihenPortfolio.jihenLinkedinUrl || '',
      },
    };
  }

  private jihenBuildEditableCvSkillsSection(jihenDraft: CvDraftDto): JihenEditableCvSection<JihenEditableCvSkillGroup[]> {
    const jihenSection = this.jihenFindCvSection(jihenDraft, 'SKILLS');

    return {
      type: 'SKILLS',
      title: jihenSection?.title?.trim() || 'Skills',
      orderIndex: jihenSection?.orderIndex ?? 1,
      visible: jihenSection?.visible !== false,
      content: this.jihenMapEditableCvSkillGroups(jihenSection?.content),
    };
  }

  private jihenBuildEditableCvExperienceSection(jihenDraft: CvDraftDto): JihenEditableCvSection<JihenEditableCvExperienceItem[]> {
    const jihenSection = this.jihenFindCvSection(jihenDraft, 'EXPERIENCE');

    return {
      type: 'EXPERIENCE',
      title: jihenSection?.title?.trim() || 'Experience',
      orderIndex: jihenSection?.orderIndex ?? 2,
      visible: jihenSection?.visible !== false,
      content: this.jihenMapEditableCvExperienceItems(jihenSection?.content),
    };
  }

  private jihenBuildEditableCvEducationSection(jihenDraft: CvDraftDto): JihenEditableCvSection<JihenEditableCvEducationItem[]> {
    const jihenSection = this.jihenFindCvSection(jihenDraft, 'EDUCATION');

    return {
      type: 'EDUCATION',
      title: jihenSection?.title?.trim() || 'Education',
      orderIndex: jihenSection?.orderIndex ?? 3,
      visible: jihenSection?.visible !== false,
      content: this.jihenMapEditableCvEducationItems(jihenSection?.content),
    };
  }

  private jihenBuildEditableCvLanguagesSection(jihenDraft: CvDraftDto): JihenEditableCvSection<JihenEditableCvLanguageItem[]> {
    const jihenSection = this.jihenFindCvSection(jihenDraft, 'LANGUAGES');

    return {
      type: 'LANGUAGES',
      title: jihenSection?.title?.trim() || 'Languages',
      orderIndex: jihenSection?.orderIndex ?? 4,
      visible: jihenSection?.visible !== false,
      content: this.jihenMapEditableCvLanguageItems(jihenSection?.content),
    };
  }

  private jihenBuildEditableCvProjectsSection(jihenDraft: CvDraftDto): JihenEditableCvSection<JihenEditableCvProjectItem[]> {
    const jihenSection = this.jihenFindCvSection(jihenDraft, 'PROJECTS');

    return {
      type: 'PROJECTS',
      title: jihenSection?.title?.trim() || 'Projects',
      orderIndex: jihenSection?.orderIndex ?? 5,
      visible: jihenSection?.visible !== false,
      content: this.jihenMapEditableCvProjectItems(jihenSection?.content),
    };
  }

  private jihenBuildCvDraftUpdateRequest(): CvDraftUpdateRequest {
    const jihenSections = [
      ...this.jihenEditableCvDraft.sections.map((jihenSection) => this.jihenSerializeEditableCvSection(jihenSection)),
      ...this.jihenEditableCvDraft.rawSections.map((jihenSection) => this.jihenCloneCvValue(jihenSection) as CvDraftSectionDto),
    ].sort((left, right) => (left.orderIndex ?? 0) - (right.orderIndex ?? 0));

    return {
      theme: this.jihenEditableCvDraft.theme.trim() || null,
      settings: this.jihenCloneCvValue(this.jihenEditableCvDraft.settings ?? null),
      sections: jihenSections,
    };
  }

  private jihenSerializeEditableCvSection(jihenSection: JihenEditableCvSection): CvDraftSectionDto {
    switch (jihenSection.type) {
      case 'PROFILE':
        return {
          type: jihenSection.type,
          title: jihenSection.title.trim(),
          orderIndex: jihenSection.orderIndex,
          visible: jihenSection.visible,
          content: this.jihenSerializeEditableCvProfileContent(jihenSection.content as JihenEditableCvProfileContent),
        };
      case 'SKILLS':
        return {
          type: jihenSection.type,
          title: jihenSection.title.trim(),
          orderIndex: jihenSection.orderIndex,
          visible: jihenSection.visible,
          content: this.jihenCloneCvValue(jihenSection.content),
        };
      case 'EXPERIENCE':
        return {
          type: jihenSection.type,
          title: jihenSection.title.trim(),
          orderIndex: jihenSection.orderIndex,
          visible: jihenSection.visible,
          content: (jihenSection.content as JihenEditableCvExperienceItem[]).map((jihenItem) => ({
            company: jihenItem.company.trim() || null,
            role: jihenItem.role.trim() || null,
            location: jihenItem.location.trim() || null,
            startDate: jihenItem.startDate.trim() || null,
            endDate: jihenItem.current ? null : jihenItem.endDate.trim() || null,
            current: jihenItem.current,
            summary: jihenItem.summary.trim() || null,
          })),
        };
      case 'EDUCATION':
        return {
          type: jihenSection.type,
          title: jihenSection.title.trim(),
          orderIndex: jihenSection.orderIndex,
          visible: jihenSection.visible,
          content: (jihenSection.content as JihenEditableCvEducationItem[]).map((jihenItem) => ({
            school: jihenItem.school.trim() || null,
            degree: jihenItem.degree.trim() || null,
            fieldOfStudy: jihenItem.fieldOfStudy.trim() || null,
            location: jihenItem.location.trim() || null,
            startDate: jihenItem.startDate.trim() || null,
            endDate: jihenItem.current ? null : jihenItem.endDate.trim() || null,
            current: jihenItem.current,
            description: jihenItem.description.trim() || null,
          })),
        };
      case 'LANGUAGES':
        return {
          type: jihenSection.type,
          title: jihenSection.title.trim(),
          orderIndex: jihenSection.orderIndex,
          visible: jihenSection.visible,
          content: (jihenSection.content as JihenEditableCvLanguageItem[]).map((jihenItem) => ({
            name: jihenItem.name.trim() || null,
            proficiency: jihenItem.proficiency.trim() || null,
          })),
        };
      default:
        return {
          type: jihenSection.type,
          title: jihenSection.title.trim(),
          orderIndex: jihenSection.orderIndex,
          visible: jihenSection.visible,
          content: (jihenSection.content as JihenEditableCvProjectItem[]).map((jihenItem) => ({
            title: jihenItem.title.trim() || null,
            description: jihenItem.description.trim() || null,
            projectUrl: jihenItem.projectUrl.trim() || null,
            imageUrl: jihenItem.imageUrl.trim() || null,
            collectionName: jihenItem.collectionName.trim() || null,
            skills: jihenItem.skills.map((jihenSkill) => ({
              id: jihenSkill.id,
              name: jihenSkill.name,
              category: jihenSkill.category ?? null,
            })),
          })),
        };
    }
  }

  private jihenSerializeEditableCvProfileContent(jihenContent: JihenEditableCvProfileContent): Record<string, string | null> {
    const jihenLinkedinValue = jihenContent.linkedinUrl.trim() || null;

    return {
      fullName: jihenContent.fullName.trim() || null,
      headline: jihenContent.headline.trim() || null,
      email: jihenContent.email.trim() || null,
      phone: jihenContent.phone.trim() || null,
      location: jihenContent.location.trim() || null,
      summary: jihenContent.summary.trim() || null,
      githubUrl: jihenContent.githubUrl.trim() || null,
      linkedinUrl: jihenLinkedinValue,
      linkedInUrl: jihenLinkedinValue,
    };
  }

  private jihenBuildCvSaveErrorMessage(err: unknown): string {
    if (err instanceof HttpErrorResponse) {
      if (err.status === 404) {
        return 'Generate a CV draft first.';
      }

      if (err.status === 400) {
        return messageFromHttpError(err, 'Validation error.');
      }

      if (err.status === 401 || err.status === 403) {
        return 'Authentication error. Please sign in again.';
      }
    }

    return 'Could not save CV draft.';
  }

  private jihenMapCvDraft(jihenDraft: CvDraftDto): JihenCvViewModel {
    const jihenProfileSection = this.jihenFindCvSection(jihenDraft, 'PROFILE');
    const jihenSkillsSection = this.jihenFindCvSection(jihenDraft, 'SKILLS');
    const jihenProjectsSection = this.jihenFindCvSection(jihenDraft, 'PROJECTS');
    const jihenProfileContent = this.jihenNormalizeCvObject(jihenProfileSection?.content);
    const jihenPreferredTemplate =
      this.jihenReadCvString(jihenProfileContent, ['preferredTemplate']) || (jihenDraft.preferredTemplate ?? '').trim();
    const jihenTheme = this.jihenReadCvString({ theme: jihenDraft.theme }, ['theme']);
    const jihenSummary =
      this.jihenReadCvString(jihenProfileContent, ['summary', 'about', 'bio', 'description']) ||
      (typeof jihenProfileSection?.content === 'string' ? jihenProfileSection.content.trim() : '');

    return {
      profile: {
        fullName:
          this.jihenReadCvString(jihenProfileContent, ['fullName', 'name']) || this.jihenPortfolio.jihenTitle || 'Your Name',
        email: this.jihenReadCvString(jihenProfileContent, ['email']) || null,
        phone: this.jihenReadCvString(jihenProfileContent, ['phone', 'phoneNumber', 'mobile']) || null,
        headline:
          this.jihenReadCvString(jihenProfileContent, ['headline', 'jobTitle', 'role', 'title']) ||
          this.jihenPortfolio.jihenJob ||
          'Professional Headline',
        summary: jihenSummary,
        location: this.jihenReadCvString(jihenProfileContent, ['location', 'city', 'country']) || this.jihenPortfolio.jihenLocation || null,
        githubUrl: this.jihenReadCvString(jihenProfileContent, ['githubUrl', 'github']) || this.jihenPortfolio.jihenGithubUrl || null,
        linkedinUrl: this.jihenReadCvString(jihenProfileContent, ['linkedinUrl', 'linkedin']) || this.jihenPortfolio.jihenLinkedinUrl || null,
        preferredTemplate: jihenPreferredTemplate,
        language: this.jihenReadCvString(jihenProfileContent, ['language']) || (jihenDraft.language ?? '').trim() || null,
      },
      profileTitle: jihenProfileSection?.title?.trim() || 'Profile',
      skillsTitle: jihenSkillsSection?.title?.trim() || 'Skills',
      projectsTitle: jihenProjectsSection?.title?.trim() || 'Projects',
      skillsByCategory: this.jihenMapCvSkillGroups(jihenSkillsSection?.content),
      projects: this.jihenMapCvProjects(jihenProjectsSection?.content),
      extraSections: this.jihenMapCvExtraSections(jihenDraft),
      template: this.jihenResolveCvTemplate(jihenTheme, jihenPreferredTemplate),
    };
  }

  private jihenResolveCvTemplate(jihenTheme: string, jihenPreferredTemplate: string): JihenCvTemplate {
    const jihenThemeKey = jihenTheme.trim().toUpperCase();
    if (jihenThemeKey === 'ATS_MINIMAL') {
      return 'ats-minimal';
    }

    return jihenPreferredTemplate === 'developer-minimal' ? 'developer-minimal' : 'default';
  }

  private jihenFindCvSection(jihenDraft: CvDraftDto, jihenType: string): CvDraftSectionDto | null {
    const jihenSections = [...(jihenDraft.sections ?? [])]
      .filter((jihenSection) => (jihenSection.type ?? '').trim().toUpperCase() === jihenType && jihenSection.visible !== false)
      .sort((left, right) => (left.orderIndex ?? 0) - (right.orderIndex ?? 0));

    return jihenSections[0] ?? null;
  }

  private jihenMapCvSkillGroups(jihenContent: unknown): JihenCvSkillGroupView[] {
    if (!Array.isArray(jihenContent)) {
      return [];
    }

    return jihenContent
      .map((jihenGroup, groupIndex) => {
        const jihenGroupData = this.jihenNormalizeCvObject(jihenGroup);
        const jihenSkills = Array.isArray(jihenGroupData['skills'])
          ? jihenGroupData['skills']
              .map((jihenSkill, skillIndex) => this.jihenMapCvSkill(jihenSkill, skillIndex + 1))
              .filter((jihenSkill): jihenSkill is CvPreviewSkillDto => Boolean(jihenSkill))
          : [];

        return {
          category: this.formatJihenCvSkillCategory(this.jihenReadCvString(jihenGroupData, ['category']) || `group_${groupIndex + 1}`),
          skills: jihenSkills,
        };
      })
      .filter((jihenGroup) => jihenGroup.skills.length > 0);
  }

  private jihenMapCvProjects(jihenContent: unknown): JihenCvProjectView[] {
    if (!Array.isArray(jihenContent)) {
      return [];
    }

    return jihenContent.map((jihenProject, index) => {
      const jihenProjectData = this.jihenNormalizeCvObject(jihenProject);
      const jihenProjectSkills = Array.isArray(jihenProjectData['skills'])
        ? jihenProjectData['skills']
            .map((jihenSkill, skillIndex) => this.jihenMapCvSkill(jihenSkill, skillIndex + 1))
            .filter((jihenSkill): jihenSkill is CvPreviewSkillDto => Boolean(jihenSkill))
        : [];

      return {
        id: this.jihenReadCvNumber(jihenProjectData, ['id']) ?? index + 1,
        title: this.jihenReadCvString(jihenProjectData, ['title', 'name']) || 'Untitled Project',
        description: this.jihenReadCvString(jihenProjectData, ['description', 'summary']) || 'No project summary provided.',
        imageUrl: this.jihenReadCvString(jihenProjectData, ['imageUrl', 'coverImage', 'thumbnailUrl']),
        projectUrl: this.jihenReadCvString(jihenProjectData, ['projectUrl', 'url', 'link']),
        collectionName: this.jihenReadCvString(jihenProjectData, ['collectionName', 'collection']),
        skills: jihenProjectSkills,
      };
    });
  }

  private jihenMapEditableCvSkillGroups(jihenContent: unknown): JihenEditableCvSkillGroup[] {
    return this.jihenMapCvSkillGroups(jihenContent).map((jihenGroup) => ({
      category: jihenGroup.category,
      skills: jihenGroup.skills.map((jihenSkill) => ({
        id: jihenSkill.id,
        name: jihenSkill.name,
        category: jihenSkill.category ?? null,
      })),
    }));
  }

  private jihenMapEditableCvExperienceItems(jihenContent: unknown): JihenEditableCvExperienceItem[] {
    if (!Array.isArray(jihenContent)) {
      return [];
    }

    return jihenContent
      .map((jihenItem) => {
        const jihenItemData = this.jihenNormalizeCvObject(jihenItem);
        return {
          company: this.jihenReadCvString(jihenItemData, ['company', 'name']),
          role: this.jihenReadCvString(jihenItemData, ['role', 'position', 'title']),
          location: this.jihenReadCvString(jihenItemData, ['location']),
          startDate: this.jihenReadCvString(jihenItemData, ['startDate']),
          endDate: this.jihenReadCvString(jihenItemData, ['endDate']),
          current: this.jihenReadCvBoolean(jihenItemData, ['current']),
          summary: this.jihenReadCvString(jihenItemData, ['summary', 'description']),
        };
      })
      .filter((jihenItem) => Object.values(jihenItem).some((jihenValue) => (typeof jihenValue === 'boolean' ? jihenValue : Boolean(jihenValue))));
  }

  private jihenMapEditableCvEducationItems(jihenContent: unknown): JihenEditableCvEducationItem[] {
    if (!Array.isArray(jihenContent)) {
      return [];
    }

    return jihenContent
      .map((jihenItem) => {
        const jihenItemData = this.jihenNormalizeCvObject(jihenItem);
        return {
          school: this.jihenReadCvString(jihenItemData, ['school', 'institution', 'name']),
          degree: this.jihenReadCvString(jihenItemData, ['degree']),
          fieldOfStudy: this.jihenReadCvString(jihenItemData, ['fieldOfStudy', 'field']),
          location: this.jihenReadCvString(jihenItemData, ['location']),
          startDate: this.jihenReadCvString(jihenItemData, ['startDate']),
          endDate: this.jihenReadCvString(jihenItemData, ['endDate']),
          current: this.jihenReadCvBoolean(jihenItemData, ['current']),
          description: this.jihenReadCvString(jihenItemData, ['description', 'summary']),
        };
      })
      .filter((jihenItem) => Object.values(jihenItem).some((jihenValue) => (typeof jihenValue === 'boolean' ? jihenValue : Boolean(jihenValue))));
  }

  private jihenMapEditableCvLanguageItems(jihenContent: unknown): JihenEditableCvLanguageItem[] {
    if (!Array.isArray(jihenContent)) {
      return [];
    }

    return jihenContent
      .map((jihenItem) => {
        const jihenItemData = this.jihenNormalizeCvObject(jihenItem);
        return {
          name: this.jihenReadCvString(jihenItemData, ['name', 'language']),
          proficiency: this.jihenReadCvString(jihenItemData, ['proficiency', 'level']),
        };
      })
      .filter((jihenItem) => Boolean(jihenItem.name || jihenItem.proficiency));
  }

  private jihenMapEditableCvProjectItems(jihenContent: unknown): JihenEditableCvProjectItem[] {
    return this.jihenMapCvProjects(jihenContent).map((jihenProject) => ({
      title: jihenProject.title,
      description: jihenProject.description,
      projectUrl: jihenProject.projectUrl,
      imageUrl: jihenProject.imageUrl,
      collectionName: jihenProject.collectionName,
      skills: jihenProject.skills.map((jihenSkill) => ({
        id: jihenSkill.id,
        name: jihenSkill.name,
        category: jihenSkill.category ?? null,
      })),
    }));
  }

  private jihenMapCvExtraSections(jihenDraft: CvDraftDto): JihenCvExtraSectionView[] {
    const jihenSectionTypes = ['EXPERIENCE', 'EDUCATION', 'LANGUAGES'];

    return jihenSectionTypes
      .map((jihenType) => {
        const jihenSection = this.jihenFindCvSection(jihenDraft, jihenType);
        if (!jihenSection) {
          return null;
        }

        const jihenItems = this.jihenMapCvExtraItems(jihenSection.content, jihenType);
        if (jihenItems.length === 0) {
          return null;
        }

        return {
          type: jihenType,
          title: jihenSection.title?.trim() || this.formatJihenCvSkillCategory(jihenType),
          items: jihenItems,
        };
      })
      .filter((jihenSection): jihenSection is JihenCvExtraSectionView => Boolean(jihenSection));
  }

  private jihenMapCvExtraItems(jihenContent: unknown, jihenSectionType: string): JihenCvExtraItemView[] {
    if (Array.isArray(jihenContent)) {
      return jihenContent
        .map((jihenItem) => this.jihenMapCvExtraItem(jihenItem, jihenSectionType))
        .filter((jihenItem): jihenItem is JihenCvExtraItemView => Boolean(jihenItem));
    }

    const jihenSingleItem = this.jihenMapCvExtraItem(jihenContent, jihenSectionType);
    return jihenSingleItem ? [jihenSingleItem] : [];
  }

  private jihenMapCvExtraItem(jihenItem: unknown, jihenSectionType: string): JihenCvExtraItemView | null {
    if (typeof jihenItem === 'string') {
      const jihenValue = jihenItem.trim();
      return jihenValue
        ? {
            heading: jihenValue,
            subheading: '',
            meta: '',
            body: '',
            tags: [],
            lines: [],
          }
        : null;
    }

    const jihenItemData = this.jihenNormalizeCvObject(jihenItem);
    const jihenTags = this.jihenReadCvStringList(jihenItemData, ['tags', 'technologies']).filter(Boolean);
    const jihenLines = this.jihenReadCvStringList(jihenItemData, ['highlights', 'responsibilities', 'bullets']).filter(Boolean);
    const jihenHeading =
      this.jihenReadCvString(jihenItemData, ['title', 'name', 'company', 'institution', 'school', 'language']) ||
      (jihenSectionType === 'LANGUAGES' ? 'Language' : '');
    const jihenSubheading = this.jihenReadCvString(jihenItemData, ['role', 'position', 'degree', 'level']);
    const jihenMeta = this.jihenBuildCvMeta(jihenItemData);
    const jihenBody =
      this.jihenReadCvString(jihenItemData, ['summary', 'description', 'details', 'content', 'proficiency']) ||
      this.jihenBuildCvFallbackBody(jihenItemData);

    if (!jihenHeading && !jihenSubheading && !jihenMeta && !jihenBody && jihenTags.length === 0 && jihenLines.length === 0) {
      return null;
    }

    return {
      heading: jihenHeading || 'Untitled',
      subheading: jihenSubheading,
      meta: jihenMeta,
      body: jihenBody,
      tags: jihenTags,
      lines: jihenLines,
    };
  }

  private jihenMapCvSkill(jihenSkill: unknown, fallbackId: number): CvPreviewSkillDto | null {
    if (typeof jihenSkill === 'string') {
      const jihenName = jihenSkill.trim();
      return jihenName ? { id: fallbackId, name: jihenName, category: null } : null;
    }

    const jihenSkillData = this.jihenNormalizeCvObject(jihenSkill);
    const jihenName = this.jihenReadCvString(jihenSkillData, ['name']);
    if (!jihenName) {
      return null;
    }

    return {
      id: this.jihenReadCvNumber(jihenSkillData, ['id']) ?? fallbackId,
      name: jihenName,
      category: this.jihenReadCvString(jihenSkillData, ['category']) || null,
    };
  }

  private jihenNormalizeCvObject(jihenValue: unknown): Record<string, unknown> {
    if (!jihenValue || typeof jihenValue !== 'object' || Array.isArray(jihenValue)) {
      return {};
    }

    return jihenValue as Record<string, unknown>;
  }

  private jihenReadCvBoolean(jihenObject: Record<string, unknown>, jihenKeys: string[]): boolean {
    for (const jihenKey of jihenKeys) {
      const jihenValue = jihenObject[jihenKey];
      if (typeof jihenValue === 'boolean') {
        return jihenValue;
      }
    }

    return false;
  }

  private jihenReadCvString(jihenObject: Record<string, unknown>, jihenKeys: string[]): string {
    for (const jihenKey of jihenKeys) {
      const jihenValue = jihenObject[jihenKey];
      if (typeof jihenValue === 'string' && jihenValue.trim()) {
        return jihenValue.trim();
      }
    }

    return '';
  }

  private jihenReadCvNumber(jihenObject: Record<string, unknown>, jihenKeys: string[]): number | null {
    for (const jihenKey of jihenKeys) {
      const jihenValue = jihenObject[jihenKey];
      if (typeof jihenValue === 'number' && Number.isFinite(jihenValue)) {
        return jihenValue;
      }
    }

    return null;
  }

  private jihenReadCvStringList(jihenObject: Record<string, unknown>, jihenKeys: string[]): string[] {
    for (const jihenKey of jihenKeys) {
      const jihenValue = jihenObject[jihenKey];
      if (!Array.isArray(jihenValue)) {
        continue;
      }

      return jihenValue
        .map((jihenEntry) => {
          if (typeof jihenEntry === 'string') {
            return jihenEntry.trim();
          }

          const jihenEntryObject = this.jihenNormalizeCvObject(jihenEntry);
          return this.jihenReadCvString(jihenEntryObject, ['name', 'label', 'title', 'value']);
        })
        .filter(Boolean);
    }

    return [];
  }

  private jihenBuildCvMeta(jihenObject: Record<string, unknown>): string {
    return [
      this.jihenReadCvString(jihenObject, ['dateRange', 'period', 'duration']),
      this.jihenReadCvString(jihenObject, ['location', 'issuer']),
    ]
      .filter(Boolean)
      .join(' · ');
  }

  private jihenBuildCvFallbackBody(jihenObject: Record<string, unknown>): string {
    const jihenIgnoredKeys = new Set([
      'id',
      'title',
      'name',
      'company',
      'institution',
      'school',
      'language',
      'role',
      'position',
      'degree',
      'level',
      'dateRange',
      'period',
      'duration',
      'location',
      'issuer',
      'tags',
      'technologies',
      'highlights',
      'responsibilities',
      'bullets',
      'skills',
    ]);

    return Object.entries(jihenObject)
      .filter(([jihenKey, jihenValue]) => !jihenIgnoredKeys.has(jihenKey) && typeof jihenValue === 'string' && jihenValue.trim())
      .map(([, jihenValue]) => String(jihenValue).trim())
      .join(' · ');
  }

  private jihenGetEditableCvSection<T>(jihenType: JihenEditableCvSectionType): JihenEditableCvSection<T> {
    const jihenSection = this.jihenEditableCvDraft.sections.find((candidate) => candidate.type === jihenType);
    if (jihenSection) {
      return jihenSection as JihenEditableCvSection<T>;
    }

    throw new Error(`Editable CV section missing: ${jihenType}`);
  }

  private jihenCloneCvDraft(jihenDraft: CvDraftDto): CvDraftDto {
    return this.jihenCloneCvValue(jihenDraft) as CvDraftDto;
  }

  private jihenCloneCvValue<T>(jihenValue: T): T {
    return JSON.parse(JSON.stringify(jihenValue)) as T;
  }

  private jihenBuildEmptyEditableCvDraft(): JihenEditableCvDraft {
    return {
      theme: '',
      settings: null,
      sections: [
        {
          type: 'PROFILE',
          title: 'Profile',
          orderIndex: 0,
          visible: true,
          content: {
            fullName: '',
            headline: '',
            email: '',
            phone: '',
            location: '',
            summary: '',
            githubUrl: '',
            linkedinUrl: '',
          },
        },
        { type: 'SKILLS', title: 'Skills', orderIndex: 1, visible: true, content: [] },
        { type: 'EXPERIENCE', title: 'Experience', orderIndex: 2, visible: true, content: [] },
        { type: 'EDUCATION', title: 'Education', orderIndex: 3, visible: true, content: [] },
        { type: 'LANGUAGES', title: 'Languages', orderIndex: 4, visible: true, content: [] },
        { type: 'PROJECTS', title: 'Projects', orderIndex: 5, visible: true, content: [] },
      ],
      rawSections: [],
    };
  }

  private jihenBuildEmptyCvExperienceItem(): JihenEditableCvExperienceItem {
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

  private jihenBuildEmptyCvEducationItem(): JihenEditableCvEducationItem {
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

  private jihenBuildEmptyCvLanguageItem(): JihenEditableCvLanguageItem {
    return {
      name: '',
      proficiency: '',
    };
  }

  private jihenBuildEmptyCvProjectItem(): JihenEditableCvProjectItem {
    return {
      title: '',
      description: '',
      projectUrl: '',
      imageUrl: '',
      collectionName: '',
      skills: [],
    };
  }

  private jihenSyncSelectedSkills(): void {
    const jihenSelectedSkillIds = this.jihenPortfolioForm.controls.skillIds.value;
    if (jihenSelectedSkillIds.length === 0) {
      this.jihenSelectedSkills = [];
      return;
    }

    const jihenSkillMap = new Map<number, SkillSummaryDto>();
    [...this.jihenSelectedSkills, ...this.jihenAvailableSkills].forEach((jihenSkill) => {
      jihenSkillMap.set(jihenSkill.id, jihenSkill);
    });

    const jihenNextSelectedSkills = jihenSelectedSkillIds
      .map((jihenSkillId) => jihenSkillMap.get(jihenSkillId))
      .filter((jihenSkill): jihenSkill is SkillSummaryDto => Boolean(jihenSkill));

    if (jihenNextSelectedSkills.length > 0) {
      this.jihenSelectedSkills = jihenNextSelectedSkills;
    }
  }

  private jihenBuildPortfolioRequest(): PortfolioUpsertRequest {
    const jihenFormValue = this.jihenPortfolioForm.getRawValue();
    return {
      title: jihenFormValue.title.trim(),
      bio: jihenFormValue.bio.trim() || null,
      coverImage: jihenFormValue.coverImage.trim() || null,
      job: jihenFormValue.job.trim(),
      githubUrl: jihenFormValue.githubUrl.trim() || null,
      linkedinUrl: jihenFormValue.linkedinUrl.trim() || null,
      openToWork: jihenFormValue.openToWork,
      availableForFreelance: jihenFormValue.availableForFreelance,
      visibility: jihenFormValue.visibility,
      skillIds: [...jihenFormValue.skillIds],
    };
  }

  private jihenBuildCreateProjectRequest(): CreateProjectRequest {
    const jihenFormValue = this.jihenProjectForm.getRawValue();
    const jihenMedia = this.jihenProjectMediaArray.controls
      .map((jihenMediaControl, index) => {
        const jihenMediaUrl = String(jihenMediaControl.get('mediaUrl')?.value ?? '').trim();
        const jihenRawMediaType = String(jihenMediaControl.get('mediaType')?.value ?? 'IMAGE').toUpperCase();
        const jihenMediaType: JihenProjectMediaType = jihenRawMediaType === 'VIDEO' ? 'VIDEO' : 'IMAGE';
        const jihenOrderIndexValue = Number(jihenMediaControl.get('orderIndex')?.value ?? index);

        return {
          mediaUrl: jihenMediaUrl,
          mediaType: jihenMediaType,
          orderIndex: Number.isFinite(jihenOrderIndexValue) ? jihenOrderIndexValue : index,
        };
      })
      .filter((jihenMediaItem) => Boolean(jihenMediaItem.mediaUrl));

    return {
      title: jihenFormValue.title.trim(),
      description: jihenFormValue.description.trim(),
      projectUrl: jihenFormValue.projectUrl.trim(),
      pinned: jihenFormValue.pinned,
      visibility: jihenFormValue.visibility,
      skillIds: [...jihenFormValue.skillIds],
      media: jihenMedia,
    };
  }

  private jihenBuildCreateCollectionRequest(): CreateCollectionRequest {
    const jihenFormValue = this.jihenCollectionForm.getRawValue();
    return {
      name: jihenFormValue.name.trim(),
      description: jihenFormValue.description.trim(),
      visibility: jihenFormValue.visibility,
    };
  }

  private jihenHandleCollectionProjectCreated(jihenProject: PortfolioProjectDto): void {
    const jihenMappedProject = this.jihenMapProject(jihenProject, this.jihenProjects.length);
    const jihenExistingProjectIndex = this.jihenProjects.findIndex(
      (candidate) => candidate.jihenId === jihenMappedProject.jihenId,
    );

    if (jihenExistingProjectIndex >= 0) {
      this.jihenProjects = this.jihenProjects.map((jihenExistingProject, index) =>
        index === jihenExistingProjectIndex ? jihenMappedProject : jihenExistingProject,
      );
    } else {
      this.jihenProjects = [jihenMappedProject, ...this.jihenProjects];
    }

    const jihenSelectedProjectIds = new Set(this.jihenCollectionForm.controls.projectIds.value);
    jihenSelectedProjectIds.add(jihenMappedProject.jihenId);
    this.jihenCollectionForm.controls.projectIds.setValue(Array.from(jihenSelectedProjectIds));
    this.jihenCollectionProjectSuccessMessage = 'Projet créé et ajouté à la collection.';
    this.closeJihenProjectModal();
  }

  private jihenSubmitEditedProject(jihenProjectId: number): void {
    const jihenProjectIndex = this.jihenProjects.findIndex((candidate) => candidate.jihenId === jihenProjectId);
    if (jihenProjectIndex < 0) {
      this.jihenProjectFormError = 'Projet introuvable.';
      return;
    }

    const jihenExistingProject = this.jihenProjects[jihenProjectIndex];
    const jihenRequest = this.jihenBuildCreateProjectRequest();
    const jihenSkillMap = new Map<number, SkillSummaryDto>();
    [...this.jihenAvailableSkills, ...this.jihenSelectedSkills].forEach((jihenSkill) => {
      jihenSkillMap.set(jihenSkill.id, jihenSkill);
    });

    const jihenMedia = jihenRequest.media.map((jihenMediaItem) => ({
      mediaUrl: jihenMediaItem.mediaUrl,
      mediaType: jihenMediaItem.mediaType,
      orderIndex: jihenMediaItem.orderIndex,
    }));
    const jihenCoverMedia = this.jihenBuildProjectCoverMedia(jihenMedia);

    const jihenUpdatedProject: JihenPortfolioProjectView = {
      ...jihenExistingProject,
      jihenTitle: jihenRequest.title,
      jihenDescription: jihenRequest.description || 'Description du projet indisponible.',
      jihenProjectUrl: jihenRequest.projectUrl,
      jihenPinned: jihenRequest.pinned,
      jihenSkillIds: [...jihenRequest.skillIds],
      jihenSkills: jihenRequest.skillIds
        .map((jihenSkillId) => jihenSkillMap.get(jihenSkillId)?.name)
        .filter((jihenSkillName): jihenSkillName is string => Boolean(jihenSkillName)),
      jihenVisibility: jihenRequest.visibility,
      jihenMedia: jihenMedia,
      jihenCoverImage: jihenCoverMedia?.mediaUrl || jihenExistingProject.jihenCoverImage,
      jihenCoverMediaType: jihenCoverMedia?.mediaType ?? null,
      jihenUpdatedAt: new Date().toISOString(),
      jihenUpdatedAtLabel: this.jihenFormatProjectDate(new Date().toISOString(), 'today'),
    };

    this.jihenProjects = this.jihenProjects.map((jihenProject) =>
      jihenProject.jihenId === jihenProjectId ? jihenUpdatedProject : jihenProject,
    );
    this.jihenIsSubmittingProject = false;
    this.closeJihenProjectModal();
    // TODO: Replace this local edit with PUT /api/portfolio/me/projects/{projectId} when the endpoint is wired in JihenPortfolioService.
  }

  private jihenMapProjects(jihenProjects: PortfolioProjectDto[]): JihenPortfolioProjectView[] {
    return jihenProjects.map((jihenProject, index) => this.jihenMapProject(jihenProject, index));
  }

  private jihenMapProject(jihenProject: PortfolioProjectDto, index: number): JihenPortfolioProjectView {
    const jihenProjectMedia = this.jihenMapProjectMedia(jihenProject);
    const jihenProjectCoverMedia = this.jihenBuildProjectCoverMedia(jihenProjectMedia);
    const jihenCreatedAt = jihenProject.createdAt?.trim() ?? null;
    const jihenUpdatedAt = jihenProject.updatedAt?.trim() ?? null;

    return {
      jihenId: jihenProject.id,
      jihenTitle: (jihenProject.title ?? jihenProject.name ?? '').trim() || `Projet ${index + 1}`,
      jihenDescription: (jihenProject.description ?? '').trim() || 'Description du projet indisponible.',
      jihenProjectUrl: (jihenProject.projectUrl ?? '').trim(),
      jihenPinned: Boolean(jihenProject.pinned),
      jihenSkillIds: jihenProject.skills?.map((jihenSkill) => jihenSkill.id) ?? [],
      jihenSkills: this.jihenMapProjectSkills(jihenProject),
      jihenVisibility: jihenProject.visibility,
      jihenCoverImage:
        jihenProjectCoverMedia?.mediaUrl || this.jihenProjectPlaceholders[index % this.jihenProjectPlaceholders.length],
      jihenCoverMediaType: jihenProjectCoverMedia?.mediaType ?? null,
      jihenMedia: jihenProjectMedia,
      jihenCreatedAt,
      jihenUpdatedAt,
      jihenCreatedAtLabel: this.jihenFormatProjectDate(jihenCreatedAt, 'recent'),
      jihenUpdatedAtLabel: this.jihenFormatProjectDate(jihenUpdatedAt, 'recent'),
    };
  }

  private jihenMapCollections(
    jihenCollections: PortfolioCollectionDto[],
    jihenProjects: JihenPortfolioProjectView[],
  ): JihenPortfolioCollectionView[] {
    const jihenProjectTitleMap = new Map<number, string>();
    jihenProjects.forEach((jihenProject) => {
      jihenProjectTitleMap.set(jihenProject.jihenId, jihenProject.jihenTitle);
    });

    return jihenCollections.map((jihenCollection, index) => ({
      jihenId: jihenCollection.id,
      jihenSortIndex: index,
      jihenName: (jihenCollection.name ?? jihenCollection.title ?? '').trim() || `Collection ${index + 1}`,
      jihenDescription: (jihenCollection.description ?? '').trim() || 'Description de la collection indisponible.',
      jihenProjectCount: jihenCollection.projectCount ?? jihenCollection.projects?.length ?? 0,
      jihenVisibility: jihenCollection.visibility,
      jihenProjectSummaries:
        jihenCollection.projects
          ?.map((jihenProject) => jihenProjectTitleMap.get(jihenProject.id))
          .filter((jihenProjectTitle): jihenProjectTitle is string => Boolean(jihenProjectTitle))
          .slice(0, 3) ?? [],
      jihenCreatedAtLabel: `Collection ${index + 1}`,
      jihenUpdatedAtLabel: `Collection ${index + 1}`,
    }));
  }

  private jihenMapProjectSkills(jihenProject: PortfolioProjectDto): string[] {
    if (jihenProject.skills?.length) {
      return jihenProject.skills.map((jihenSkill) => jihenSkill.name);
    }
    return jihenProject.skillNames ?? [];
  }

  private async copyJihenActionLink(
    jihenPath: string,
    jihenType: JihenActionMenuType,
    jihenId: number,
  ): Promise<void> {
    const jihenOrigin = globalThis.location?.origin ?? 'http://localhost:4200';
    const jihenUrl = `${jihenOrigin}${jihenPath}`;

    try {
      if (globalThis.navigator?.clipboard?.writeText) {
        await globalThis.navigator.clipboard.writeText(jihenUrl);
      } else {
        const jihenTextArea = document.createElement('textarea');
        jihenTextArea.value = jihenUrl;
        jihenTextArea.setAttribute('readonly', '');
        jihenTextArea.style.position = 'absolute';
        jihenTextArea.style.left = '-9999px';
        document.body.appendChild(jihenTextArea);
        jihenTextArea.select();
        document.execCommand('copy');
        document.body.removeChild(jihenTextArea);
      }
      this.jihenSetActionFeedback(jihenType, jihenId, 'Lien copi\u00e9 !');
    } catch {
      this.jihenSetActionFeedback(jihenType, jihenId, 'Impossible de copier le lien.');
    } finally {
      this.closeJihenActionMenu();
    }
  }

  private jihenSetActionFeedback(jihenType: JihenActionMenuType, jihenId: number, jihenMessage: string): void {
    const jihenStore =
      jihenType === 'project'
        ? this.jihenProjectActionFeedback
        : jihenType === 'collection'
          ? this.jihenCollectionActionFeedback
          : this.jihenSkillActionFeedback;

    jihenStore[jihenId] = jihenMessage;
    globalThis.setTimeout(() => {
      if (jihenStore[jihenId] === jihenMessage) {
        delete jihenStore[jihenId];
      }
    }, 2500);
  }

  private jihenGroupSkillsByCategory(jihenSkills: SkillSummaryDto[]): JihenSkillCategoryGroup[] {
    if (jihenSkills.length === 0) {
      return [];
    }

    const jihenGroupedSkills = new Map<string, SkillSummaryDto[]>();

    jihenSkills.forEach((jihenSkill) => {
      const jihenCategoryKey = (jihenSkill.category ?? 'OTHER').trim() || 'OTHER';
      const jihenCategorySkills = jihenGroupedSkills.get(jihenCategoryKey) ?? [];
      jihenCategorySkills.push(jihenSkill);
      jihenGroupedSkills.set(jihenCategoryKey, jihenCategorySkills);
    });

    return Array.from(jihenGroupedSkills.entries())
      .map(([jihenCategoryKey, jihenCategorySkills]) => ({
        jihenCategoryKey,
        jihenCategoryLabel: this.jihenFormatSkillCategory(jihenCategoryKey),
        jihenSkills: [...jihenCategorySkills].sort((a, b) => a.name.localeCompare(b.name, 'fr')),
      }))
      .sort((a, b) => {
        const jihenCategoryOrderA = this.jihenSkillCategories.indexOf(a.jihenCategoryKey);
        const jihenCategoryOrderB = this.jihenSkillCategories.indexOf(b.jihenCategoryKey);
        if (jihenCategoryOrderA >= 0 && jihenCategoryOrderB >= 0) {
          return jihenCategoryOrderA - jihenCategoryOrderB;
        }
        if (jihenCategoryOrderA >= 0) {
          return -1;
        }
        if (jihenCategoryOrderB >= 0) {
          return 1;
        }

        return a.jihenCategoryLabel.localeCompare(b.jihenCategoryLabel, 'fr');
      });
  }

  private jihenBuildDisplayName(jihenResponse: PortfolioResponse): string {
    const jihenFirstName = jihenResponse.profile?.firstName?.trim() ?? '';
    const jihenLastName = jihenResponse.profile?.lastName?.trim() ?? '';
    return [jihenFirstName, jihenLastName].filter(Boolean).join(' ').trim() || (jihenResponse.owner?.username ?? '').trim();
  }

  private jihenBuildLocation(jihenProfile: PortfolioResponse['profile']): string {
    if (!jihenProfile) {
      return 'Bizerte, Tunisie';
    }
    const jihenLocation = jihenProfile.location?.trim();
    if (jihenLocation) {
      return jihenLocation;
    }
    const jihenParts = [jihenProfile.city?.trim() ?? '', jihenProfile.country?.trim() ?? ''].filter(Boolean);
    return jihenParts.join(', ') || 'Bizerte, Tunisie';
  }

  private jihenProjectMatchesSkillFilter(
    jihenProject: JihenPortfolioProjectView,
    jihenSkillFilter: string,
  ): boolean {
    if (jihenSkillFilter.startsWith('skill:')) {
      const jihenSkillId = Number(jihenSkillFilter.replace('skill:', ''));
      return Number.isFinite(jihenSkillId) && jihenProject.jihenSkillIds.includes(jihenSkillId);
    }

    if (jihenSkillFilter.startsWith('name:')) {
      const jihenSkillName = jihenSkillFilter.replace('name:', '');
      return jihenProject.jihenSkills.some(
        (jihenProjectSkill) => this.jihenNormalizeForMatching(jihenProjectSkill) === jihenSkillName,
      );
    }

    return true;
  }

  private jihenProjectTimestamp(jihenProject: JihenPortfolioProjectView): number {
    return this.jihenDateTimestamp(jihenProject.jihenUpdatedAt) || this.jihenDateTimestamp(jihenProject.jihenCreatedAt);
  }

  private jihenDateTimestamp(jihenValue: string | null | undefined): number {
    if (!jihenValue) {
      return 0;
    }

    const jihenDate = new Date(jihenValue);
    return Number.isNaN(jihenDate.getTime()) ? 0 : jihenDate.getTime();
  }

  private jihenFormatMonthYear(jihenValue: string | null | undefined): string {
    if (!jihenValue) {
      return 'Janvier 2024';
    }
    const jihenDate = new Date(jihenValue);
    if (Number.isNaN(jihenDate.getTime())) {
      return 'Janvier 2024';
    }
    return new Intl.DateTimeFormat('fr-FR', { month: 'long', year: 'numeric' }).format(jihenDate);
  }

  private jihenFormatUpdatedAt(jihenValue: string | null | undefined): string {
    if (!jihenValue) {
      return "Aujourd'hui";
    }
    const jihenDate = new Date(jihenValue);
    if (Number.isNaN(jihenDate.getTime())) {
      return "Aujourd'hui";
    }

    const jihenNow = new Date();
    if (
      jihenDate.getFullYear() === jihenNow.getFullYear() &&
      jihenDate.getMonth() === jihenNow.getMonth() &&
      jihenDate.getDate() === jihenNow.getDate()
    ) {
      return "Aujourd'hui";
    }

    return new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: 'long', year: 'numeric' }).format(jihenDate);
  }

  private jihenFormatProjectDate(jihenValue: string | null | undefined, jihenFallback: 'recent' | 'today' = 'recent'): string {
    if (!jihenValue) {
      return jihenFallback === 'today' ? "Aujourd'hui" : 'Recemment';
    }

    const jihenDate = new Date(jihenValue);
    if (Number.isNaN(jihenDate.getTime())) {
      return jihenFallback === 'today' ? "Aujourd'hui" : 'Recemment';
    }

    return new Intl.DateTimeFormat('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' }).format(jihenDate);
  }

  private jihenSlugify(jihenValue: string): string {
    const jihenFallback = 'portfolio';
    const jihenNormalized = jihenValue
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '');
    return jihenNormalized || jihenFallback;
  }

  private jihenBuildCoverBackgroundImage(jihenCoverImage: string): string | null {
    const jihenValue = jihenCoverImage.trim();
    if (!jihenValue) {
      return null;
    }

    const jihenEscapedValue = jihenValue.replace(/"/g, '\\"');
    return `linear-gradient(180deg, rgba(20, 27, 58, 0.18) 0%, rgba(20, 27, 58, 0.6) 100%), url("${jihenEscapedValue}")`;
  }

  private jihenMapProjectMedia(jihenProject: PortfolioProjectDto): JihenProjectMediaItem[] {
    return (
      jihenProject.media?.map((jihenMediaItem, index) => ({
        mediaUrl: (jihenMediaItem.mediaUrl ?? '').trim(),
        mediaType: jihenMediaItem.mediaType === 'VIDEO' ? 'VIDEO' : 'IMAGE',
        orderIndex: Number.isFinite(jihenMediaItem.orderIndex) ? jihenMediaItem.orderIndex : index,
      })) ?? []
    );
  }

  private jihenBuildProjectCoverMedia(jihenProjectMedia: JihenProjectMediaItem[]): JihenProjectMediaItem | null {
    if (jihenProjectMedia.length === 0) {
      return null;
    }

    const jihenSortedMedia = [...jihenProjectMedia]
      .filter((jihenMediaItem) => Boolean(jihenMediaItem.mediaUrl))
      .sort((a, b) => a.orderIndex - b.orderIndex);

    return (
      jihenSortedMedia.find((jihenMediaItem) => jihenMediaItem.mediaType === 'IMAGE') ||
      jihenSortedMedia[0] ||
      null
    );
  }

  private jihenHandleProjectMediaUpload(event: Event, jihenExpectedKind: JihenProjectMediaType): void {
    const jihenInput = event.target as HTMLInputElement;
    const jihenFile = jihenInput.files?.[0];
    if (!jihenFile) {
      return;
    }

    this.jihenProjectMediaError = '';

    const jihenAllowedMimeTypes =
      jihenExpectedKind === 'IMAGE' ? this.jihenAcceptedImageMimeTypes : this.jihenAcceptedVideoMimeTypes;
    if (!jihenAllowedMimeTypes.includes(jihenFile.type)) {
      this.jihenProjectMediaError = 'Type de fichier non pris en charge.';
      jihenInput.value = '';
      return;
    }

    const jihenMaxBytes = jihenExpectedKind === 'IMAGE' ? 10 * 1024 * 1024 : 50 * 1024 * 1024;
    if (jihenFile.size > jihenMaxBytes) {
      this.jihenProjectMediaError =
        jihenExpectedKind === 'IMAGE' ? 'Image trop volumineuse (10 MB max).' : 'Fichier trop volumineux';
      jihenInput.value = '';
      return;
    }

    if (jihenFile.size > 50 * 1024 * 1024) {
      this.jihenProjectMediaError = 'Fichier trop volumineux';
      jihenInput.value = '';
      return;
    }

    this.jihenIsUploadingProjectMedia = true;
    this.jihenProjectMediaUploadLabel = jihenExpectedKind === 'IMAGE' ? 'Upload image' : 'Upload video';

    this.jihenPortfolioApi.uploadProjectMedia(jihenFile).subscribe({
      next: (jihenResponse: UploadProjectMediaResponse) => {
        this.jihenIsUploadingProjectMedia = false;
        this.jihenProjectMediaUploadLabel = '';
        this.jihenAppendProjectMediaItem(jihenResponse.mediaUrl, jihenResponse.mediaType);
        jihenInput.value = '';
      },
      error: (err) => {
        this.jihenIsUploadingProjectMedia = false;
        this.jihenProjectMediaUploadLabel = '';
        console.error('Jihen project media upload failed', {
          status: err instanceof HttpErrorResponse ? err.status : null,
          statusText: err instanceof HttpErrorResponse ? err.statusText : null,
          url: err instanceof HttpErrorResponse ? err.url : null,
          error: err instanceof HttpErrorResponse ? err.error : err,
        });
        this.jihenProjectMediaError = this.jihenBuildProjectMediaUploadErrorMessage(err);
        jihenInput.value = '';
      },
    });
  }

  private jihenAppendProjectMediaItem(jihenMediaUrl: string, jihenMediaType: JihenProjectMediaType): void {
    this.jihenProjectMediaArray.push(
      this.jihenCreateProjectMediaGroup(jihenMediaType, {
        mediaUrl: jihenMediaUrl,
        orderIndex: this.jihenProjectMediaArray.length,
      }),
    );
  }

  private jihenCreateProjectMediaGroup(
    jihenMediaType: JihenProjectMediaType,
    jihenInitialValue?: Partial<JihenProjectMediaItem>,
  ) {
    return this.jihenFormBuilder.nonNullable.group({
      mediaUrl: [jihenInitialValue?.mediaUrl ?? ''],
      mediaType: [jihenMediaType],
      orderIndex: [jihenInitialValue?.orderIndex ?? this.jihenProjectMediaArray.length],
    });
  }

  jihenFormatSkillCategory(jihenCategory: string): string {
    return jihenCategory
      .toLowerCase()
      .split('_')
      .filter(Boolean)
      .map((jihenWord) => jihenWord.charAt(0).toUpperCase() + jihenWord.slice(1))
      .join(' ');
  }

  private jihenBuildProjectCreateErrorMessage(err: unknown): string {
    if (!(err instanceof HttpErrorResponse)) {
      return 'Impossible de cr\u00e9er le projet.';
    }

    if (err.status === 401 || err.status === 403) {
      return 'Vous devez \u00eatre authentifi\u00e9 pour cr\u00e9er un projet.';
    }

    if (err.status === 404) {
      return 'Cr\u00e9ez d\u2019abord un portfolio.';
    }

    const jihenApiMessage = this.jihenExtractApiErrorMessage(err);
    const jihenApiMessageLower = this.jihenNormalizeForMatching(jihenApiMessage);

    if (err.status === 400) {
      if (
        jihenApiMessageLower.includes('visibilit') &&
        (jihenApiMessageLower.includes('portfolio') ||
          jihenApiMessageLower.includes('cannot exceed') ||
          jihenApiMessageLower.includes('exceed') ||
          jihenApiMessageLower.includes('depass'))
      ) {
        return 'Vous ne pouvez pas rendre ce projet plus visible que votre portfolio.';
      }

      if (
        jihenApiMessageLower.includes('skill') &&
        (jihenApiMessageLower.includes('invalid') ||
          jihenApiMessageLower.includes('not found') ||
          jihenApiMessageLower.includes('introuvable') ||
          jihenApiMessageLower.includes('unknown'))
      ) {
        return 'Une ou plusieurs comp\u00e9tences sont introuvables.';
      }

      if (jihenApiMessage) {
        return jihenApiMessage;
      }
    }

    return jihenApiMessage || 'Impossible de cr\u00e9er le projet.';
  }

  private jihenBuildCollectionCreateErrorMessage(err: unknown): string {
    if (!(err instanceof HttpErrorResponse)) {
      return 'Impossible de cr\u00e9er la collection.';
    }

    if (err.status === 401 || err.status === 403) {
      return 'Vous devez \u00eatre authentifi\u00e9 pour cr\u00e9er une collection.';
    }

    if (err.status === 404) {
      return 'Portfolio ou projet introuvable.';
    }

    const jihenApiMessage = this.jihenExtractApiErrorMessage(err);
    const jihenApiMessageLower = this.jihenNormalizeForMatching(jihenApiMessage);

    if (err.status === 409) {
      return 'Ce projet est deja associe a cette collection.';
    }

    if (err.status === 400) {
      if (
        jihenApiMessageLower.includes('visibilit') &&
        (jihenApiMessageLower.includes('portfolio') ||
          jihenApiMessageLower.includes('cannot exceed') ||
          jihenApiMessageLower.includes('exceed') ||
          jihenApiMessageLower.includes('depass'))
      ) {
        return 'La visibilit\u00e9 ne peut pas d\u00e9passer celle du portfolio.';
      }

      return jihenApiMessage || 'Verifiez les informations de la collection.';
    }

    return jihenApiMessage || 'Impossible de cr\u00e9er la collection.';
  }

  private jihenBuildProjectMediaUploadErrorMessage(err: unknown): string {
    if (!(err instanceof HttpErrorResponse)) {
      return "Impossible d'uploader le media.";
    }

    if (err.status === 401 || err.status === 403) {
      return 'Vous devez \u00eatre authentifi\u00e9 pour uploader un m\u00e9dia.';
    }

    if (err.status === 413) {
      return 'Fichier trop volumineux';
    }

    if (err.status === 400) {
      return this.jihenExtractApiErrorMessage(err) || 'Fichier non pris en charge ou invalide.';
    }

    if (err.status >= 500) {
      return "Impossible d'uploader le media.";
    }

    return this.jihenExtractApiErrorMessage(err) || "Impossible d'uploader le media.";
  }

  private jihenCreateOrReuseSkill(
    jihenName: string,
    jihenCategory: string,
    jihenContext: 'portfolio-form' | 'project-form' | 'skills-modal',
    onSelect: (jihenSkillId: number) => void,
    onSuccess: () => void,
    onError: (jihenMessage: string) => void,
  ): void {
    const jihenTrimmedName = jihenName.trim();
    const jihenNormalizedName = this.jihenNormalizeSkillName(jihenTrimmedName);
    if (!jihenNormalizedName) {
      onError('Saisissez un nom de compétence.');
      return;
    }

    const jihenExistingSkill = this.jihenFindAvailableSkillByNormalizedName(jihenNormalizedName);
    if (jihenExistingSkill) {
      onSelect(jihenExistingSkill.id);
      onSuccess();
      return;
    }

    const jihenTrimmedCategory = jihenCategory.trim();
    if (!jihenTrimmedCategory) {
      onError('Ajoutez une catégorie pour créer cette compétence.');
      return;
    }

    this.jihenCreatingSkillContext = jihenContext;

    const jihenRequest: CreateSkillRequest = {
      name: jihenTrimmedName,
      category: jihenTrimmedCategory,
    };

    this.jihenPortfolioApi.createSkill(jihenRequest).subscribe({
      next: (jihenSkill) => {
        this.jihenCreatingSkillContext = null;
        const jihenCreatedSkill = this.jihenUpsertAvailableSkill(jihenSkill);
        onSelect(jihenCreatedSkill.id);
        onSuccess();
      },
      error: (err) => {
        if (err instanceof HttpErrorResponse && err.status === 409) {
          this.jihenRefreshSkillsAfterDuplicateConflict(jihenNormalizedName, onSelect, onSuccess, onError);
          return;
        }

        this.jihenCreatingSkillContext = null;
        onError(this.jihenBuildCreateSkillErrorMessage(err));
      },
    });
  }

  private jihenRefreshSkillsAfterDuplicateConflict(
    jihenNormalizedName: string,
    onSelect: (jihenSkillId: number) => void,
    onSuccess: () => void,
    onError: (jihenMessage: string) => void,
  ): void {
    this.jihenPortfolioApi.getSkills().subscribe({
      next: (jihenSkills) => {
        this.jihenCreatingSkillContext = null;
        this.jihenAvailableSkills = jihenSkills;
        this.jihenSyncSelectedSkills();
        const jihenMatchingSkill = this.jihenFindAvailableSkillByNormalizedName(jihenNormalizedName);
        if (jihenMatchingSkill) {
          onSelect(jihenMatchingSkill.id);
          onSuccess();
          return;
        }

        onError('Cette compétence existe déjà dans le catalogue.');
      },
      error: () => {
        this.jihenCreatingSkillContext = null;
        onError('Cette compétence existe déjà dans le catalogue.');
      },
    });
  }

  private jihenBuildCreateSkillErrorMessage(err: unknown): string {
    if (!(err instanceof HttpErrorResponse)) {
      return 'Impossible de créer cette compétence.';
    }

    if (err.status === 401 || err.status === 403) {
      return 'Vous devez être authentifié pour créer une compétence.';
    }

    if (err.status === 400) {
      return this.jihenExtractApiErrorMessage(err) || 'Vérifiez le nom et la catégorie de la compétence.';
    }

    return this.jihenExtractApiErrorMessage(err) || 'Impossible de créer cette compétence.';
  }

  private jihenBuildSkillsUpdateErrorMessage(err: unknown): string {
    if (!(err instanceof HttpErrorResponse)) {
      return 'Impossible de mettre à jour les compétences.';
    }

    if (err.status === 401 || err.status === 403) {
      return 'Vous devez être authentifié pour mettre à jour les compétences.';
    }

    if (err.status === 404) {
      return 'Créez d’abord un portfolio.';
    }

    const jihenApiMessage = this.jihenExtractApiErrorMessage(err);
    const jihenApiMessageLower = this.jihenNormalizeForMatching(jihenApiMessage);

    if (
      err.status === 400 &&
      jihenApiMessageLower.includes('skill') &&
      (jihenApiMessageLower.includes('invalid') ||
        jihenApiMessageLower.includes('not found') ||
        jihenApiMessageLower.includes('introuvable') ||
        jihenApiMessageLower.includes('unknown'))
    ) {
      return 'Une ou plusieurs compétences sont introuvables.';
    }

    return jihenApiMessage || 'Impossible de mettre à jour les compétences.';
  }

  private jihenExtractApiErrorMessage(err: HttpErrorResponse): string {
    const jihenBody = err.error as ApiErrorBody | string | null;

    if (typeof jihenBody === 'string') {
      return jihenBody.trim();
    }

    if (jihenBody && typeof jihenBody === 'object') {
      if (jihenBody.detail?.trim()) {
        return jihenBody.detail.trim();
      }

      if (jihenBody.error?.trim()) {
        return jihenBody.error.trim();
      }

      if (jihenBody.errors && Object.keys(jihenBody.errors).length > 0) {
        return Object.values(jihenBody.errors)
          .map((jihenMessage) => jihenMessage.trim())
          .filter(Boolean)
          .join(' · ');
      }
    }

    return '';
  }

  private jihenResolveSkillsByIds(jihenSkillIds: number[]): SkillSummaryDto[] {
    if (jihenSkillIds.length === 0) {
      return [];
    }

    const jihenSkillMap = new Map<number, SkillSummaryDto>();
    [...this.jihenAvailableSkills, ...this.jihenSelectedSkills].forEach((jihenSkill) => {
      jihenSkillMap.set(jihenSkill.id, jihenSkill);
    });

    return jihenSkillIds
      .map((jihenSkillId) => jihenSkillMap.get(jihenSkillId))
      .filter((jihenSkill): jihenSkill is SkillSummaryDto => Boolean(jihenSkill));
  }

  private jihenAppendUniqueSkillId(jihenSkillIds: number[], jihenSkillId: number): number[] {
    return jihenSkillIds.includes(jihenSkillId) ? [...jihenSkillIds] : [...jihenSkillIds, jihenSkillId];
  }

  private jihenFindAvailableSkillByNormalizedName(jihenNormalizedName: string): SkillSummaryDto | undefined {
    return this.jihenAvailableSkills.find(
      (jihenSkill) => this.jihenNormalizeSkillName(jihenSkill.name) === jihenNormalizedName,
    );
  }

  private jihenUpsertAvailableSkill(jihenSkill: SkillSummaryDto): SkillSummaryDto {
    this.jihenAvailableSkills = this.jihenUpsertSkillCollection(this.jihenAvailableSkills, jihenSkill);
    if (this.jihenSkillSearchTerm.trim()) {
      this.jihenSkillSearchResults = this.jihenUpsertSkillCollection(this.jihenSkillSearchResults, jihenSkill);
    }

    return this.jihenAvailableSkills.find((candidate) => candidate.id === jihenSkill.id) ?? jihenSkill;
  }

  private jihenNormalizeSkillName(jihenValue: string): string {
    return jihenValue.trim().replace(/\s+/g, ' ').toLowerCase();
  }

  private jihenUpsertSkillCollection(jihenSkills: SkillSummaryDto[], jihenSkill: SkillSummaryDto): SkillSummaryDto[] {
    const jihenNormalizedName = this.jihenNormalizeSkillName(jihenSkill.name);
    const jihenExistingIndex = jihenSkills.findIndex(
      (candidate) => candidate.id === jihenSkill.id || this.jihenNormalizeSkillName(candidate.name) === jihenNormalizedName,
    );

    const jihenNextSkills =
      jihenExistingIndex >= 0
        ? jihenSkills.map((candidate, index) => (index === jihenExistingIndex ? jihenSkill : candidate))
        : [...jihenSkills, jihenSkill];

    return [...jihenNextSkills].sort((a, b) => a.name.localeCompare(b.name, 'fr'));
  }

  private jihenHaveSameSkillIds(jihenLeft: number[], jihenRight: number[]): boolean {
    if (jihenLeft.length !== jihenRight.length) {
      return false;
    }

    const jihenSortedLeft = [...jihenLeft].sort((a, b) => a - b);
    const jihenSortedRight = [...jihenRight].sort((a, b) => a - b);

    return jihenSortedLeft.every((jihenSkillId, index) => jihenSkillId === jihenSortedRight[index]);
  }

  isJihenProjectVisibilityAllowed(jihenVisibility: PortfolioVisibility): boolean {
    return this.jihenVisibilityRank(jihenVisibility) <= this.jihenVisibilityRank(this.jihenPortfolio.jihenVisibility);
  }

  private jihenVisibilityRank(jihenVisibility: PortfolioVisibility): number {
    switch (jihenVisibility) {
      case 'PUBLIC':
        return 3;
      case 'FRIENDS_ONLY':
        return 2;
      default:
        return 1;
    }
  }

  private jihenBuildNextChildVisibility(jihenCurrentVisibility: PortfolioVisibility): PortfolioVisibility {
    if (jihenCurrentVisibility === 'PRIVATE') {
      return this.jihenPortfolio.jihenVisibility;
    }

    return 'PRIVATE';
  }

  private jihenNormalizeForMatching(jihenValue: string): string {
    return jihenValue
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase();
  }

  private jihenBuildEmptyCvView(): JihenCvViewModel {
    const jihenUsername = this.jihenAuth.auth()?.username?.trim() || 'Your Name';

    return {
      profile: {
        fullName: jihenUsername,
        headline: 'Professional Headline',
        summary: '',
        preferredTemplate: 'developer-minimal',
      },
      profileTitle: 'Profile',
      skillsTitle: 'Skills',
      projectsTitle: 'Projects',
      skillsByCategory: [],
      projects: [],
      extraSections: [],
      template: 'developer-minimal',
    };
  }

  private jihenBuildEmptyPortfolioModel(): JihenPortfolioViewModel {
    const jihenUsername = this.jihenAuth.auth()?.username?.trim() ?? 'jihen-ben-fredj';
    return {
      jihenTitle: jihenUsername,
      jihenBio: '',
      jihenCoverImage: '',
      jihenAvatarImage: this.jihenDefaultAvatarImage,
      jihenJob: '',
      jihenGithubUrl: '',
      jihenLinkedinUrl: '',
      jihenOpenToWork: true,
      jihenAvailableForFreelance: false,
      jihenVisibility: 'PUBLIC',
      jihenLocation: 'Bizerte, Tunisie',
      jihenMemberSince: 'Janvier 2024',
      jihenUpdatedAt: "Aujourd'hui",
      jihenOwnerUsername: jihenUsername,
    };
  }
}

