export type SocialProvider = 'GOOGLE' | 'FACEBOOK';

export interface SocialLoginRequest {
  provider: SocialProvider;
  token: string;
}

export type Role = 'user' | 'admin';

export type SubscriptionPlan = 'STANDARD' | 'PREMIUM';
export type SubscriptionStatus = 'ACTIVE' | 'CANCELLED' | 'EXPIRED';

export interface AuthResponse {
  token: string;
  userId: number;
  username: string;
  email: string;
  role: Role;
  profilePicture?: string | null;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface MessageResponse {
  message: string;
}

export interface SignUpRequest {
  username: string;
  email: string;
  password: string;
  dateOfBirth?: string | null;
  firstName?: string | null;
  lastName?: string | null;
  interests?: string[];
  description?: string | null;
  recuperationEmail?: string | null;
}

export interface ProfileResponse {
  id: number;
  firstName: string | null;
  lastName: string | null;
  interests: string[];
  description: string | null;
  profilePicture: string | null;
  lastPasswordChanged: string | null;
  recuperationEmail: string | null;
  twoFactorEnabled: boolean;
  activeStatusVisible: boolean;
}

export interface ProfileUpdateRequest {
  firstName?: string | null;
  lastName?: string | null;
  interests?: string[];
  description?: string | null;
  profilePicture?: string | null;
  recuperationEmail?: string | null;
  twoFactorEnabled?: boolean | null;
  activeStatusVisible?: boolean | null;
}

export interface LoginActivityResponse {
  loggedAt: string;
  ipAddress: string | null;
  userAgent: string | null;
}

export interface SubscriptionRequest {
  subscriptionPlan: SubscriptionPlan;
  dateOfSubscription?: string | null;
  expirationDate?: string | null;
  billingDate?: string | null;
  autoRenew?: boolean | null;
}

export interface SubscriptionResponse {
  id: number;
  dateOfSubscription: string | null;
  subscriptionPlan: SubscriptionPlan;
  status: SubscriptionStatus;
  expirationDate: string | null;
  billingDate: string | null;
  autoRenew: boolean;
}

export interface SubscriptionPlanResponse {
  plan: SubscriptionPlan;
  monthlyPrice: number;
  durationDays: number;
  trialDays: number;
  features: string[];
}

export interface FriendResponse {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
  profilePicture: string | null;
  activeNow: boolean | null;
  lastActiveAt: string | null;
}

export type FriendRelation = 'NONE' | 'FRIEND' | 'REQUEST_SENT' | 'REQUEST_RECEIVED';

export interface FriendSearchResponse {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
  profilePicture: string | null;
  relation: FriendRelation;
  activeNow: boolean | null;
  lastActiveAt: string | null;
}

export interface FriendProfileResponse {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
  description: string | null;
  interests: string[];
  profilePicture: string | null;
  activeNow: boolean | null;
  lastActiveAt: string | null;
}

export type FriendRequestStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export interface FriendRequestResponse {
  id: number;
  sender: FriendResponse;
  receiver: FriendResponse;
  status: FriendRequestStatus;
  createdAt: string;
}

export interface ConversationResponse {
  id: number;
  otherUserId: number;
  otherUsername: string;
  otherFirstName: string | null;
  otherLastName: string | null;
  otherActiveNow: boolean | null;
  otherLastActiveAt: string | null;
  lastMessage: string;
  lastMessageAt: string | null;
  unreadCount: number;
}

export interface DirectMessageResponse {
  id: number;
  senderId: number;
  senderUsername: string;
  content: string;
  voiceUrl: string | null;
  sentAt: string;
  reactionEmoji: string | null;
  replyToMessageId: number | null;
  replyToSnippet: string | null;
  deleted: boolean;
}

export interface ConversationSummaryResponse {
  conversationId: number;
  analyzedTextMessages: number;
  summary: string;
}

export type GroupMemberRole = 'OWNER' | 'ADMIN' | 'MEMBER';
export type GoalVisibility = 'PRIVATE' | 'FRIENDS' | 'GROUP' | 'PUBLIC';
export type GoalStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';
export type MentorshipStatus = 'PENDING' | 'ACTIVE' | 'DECLINED';
export type FeedEventType =
  | 'GROUP_JOINED'
  | 'GOAL_CREATED'
  | 'GOAL_PROGRESS'
  | 'GOAL_COMPLETED'
  | 'CHALLENGE_CREATED'
  | 'CHALLENGE_COMPLETED'
  | 'QUIZ_CREATED'
  | 'QUIZ_ANSWERED'
  | 'MENTORSHIP_ACCEPTED'
  | 'STREAK_UPDATED';

export interface StudyGroupResponse {
  id: number;
  name: string;
  description: string | null;
  topic: string | null;
  imageUrl: string | null;
  createdAt: string;
  ownerUsername: string;
  memberCount: number;
  myRole: GroupMemberRole | null;
}

export interface StudyGroupMemberResponse {
  userId: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
  profilePicture: string | null;
  role: GroupMemberRole;
  score: number;
}

export interface CreateStudyGroupRequest {
  name: string;
  description?: string | null;
  topic?: string | null;
  imageUrl?: string | null;
}

export interface GoalResponse {
  id: number;
  title: string;
  topic: string | null;
  targetValue: number;
  currentValue: number;
  completionPercent: number;
  deadline: string | null;
  visibility: GoalVisibility;
  status: GoalStatus;
  ownerUsername: string;
  groupId: number | null;
  updatedAt: string;
}

export interface CreateGoalRequest {
  title: string;
  topic?: string | null;
  targetValue: number;
  deadline?: string | null;
  visibility: GoalVisibility;
  groupId?: number | null;
}

export interface ChallengeResponse {
  id: number;
  groupId: number;
  title: string;
  description: string | null;
  topic: string | null;
  targetValue: number;
  startDate: string;
  endDate: string;
  createdBy: string;
  createdAt: string;
  myProgress: number;
  myPoints: number;
  myCompleted: boolean;
}

export type QuizOption = 'A' | 'B' | 'C' | 'D';

export interface GroupQuizResponse {
  id: number;
  groupId: number;
  question: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  createdBy: string;
  createdAt: string;
  answerCount: number;
  optionACount: number;
  optionBCount: number;
  optionCCount: number;
  optionDCount: number;
  mySelectedOption: QuizOption | null;
  myCorrect: boolean;
}

export interface CreateGroupQuizRequest {
  groupId: number;
  question: string;
  optionA: string;
  optionB: string;
  optionC: string;
  optionD: string;
  correctOption: QuizOption;
}

export interface LaunchGroupQuizRequest {
  topic?: string | null;
}

export interface QuizLeaderboardEntryResponse {
  rank: number;
  userId: number;
  username: string;
  quizzesAnswered: number;
  correctAnswers: number;
  totalScore: number;
  accuracyPercent: number;
}

export interface CreateChallengeRequest {
  groupId: number;
  title: string;
  description?: string | null;
  topic?: string | null;
  targetValue: number;
  startDate: string;
  endDate: string;
}

export interface MentorshipResponse {
  id: number;
  mentorId: number;
  mentorUsername: string;
  menteeId: number;
  menteeUsername: string;
  status: MentorshipStatus;
  requestedAt: string;
  acceptedAt: string | null;
}

export interface SocialUserOptionResponse {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
}

export interface ChallengeLeaderboardEntryResponse {
  rank: number;
  userId: number;
  username: string;
  progressValue: number;
  points: number;
  completed: boolean;
}

export interface StreakResponse {
  currentStreak: number;
  bestStreak: number;
  lastActivityDate: string | null;
}

export interface FeedEventResponse {
  id: number;
  type: FeedEventType;
  actorUsername: string;
  message: string;
  topic: string | null;
  groupId: number | null;
  goalId: number | null;
  challengeId: number | null;
  createdAt: string;
  rankingScore: number;
}

export type PortfolioVisibility = 'PUBLIC' | 'FRIENDS_ONLY' | 'PRIVATE';

export interface SkillSummaryDto {
  id: number;
  name: string;
  category?: string | null;
}

export interface CreateSkillRequest {
  name: string;
  category: string;
}

export interface PortfolioOwnerDto {
  id: number;
  username: string;
  email?: string | null;
}

export interface PortfolioProfileDto {
  firstName?: string | null;
  lastName?: string | null;
  description?: string | null;
  profilePicture?: string | null;
  location?: string | null;
  city?: string | null;
  country?: string | null;
}

export interface PortfolioDto {
  id: number;
  title?: string | null;
  bio?: string | null;
  coverImage?: string | null;
  job?: string | null;
  githubUrl?: string | null;
  linkedinUrl?: string | null;
  openToWork?: boolean | null;
  availableForFreelance?: boolean | null;
  visibility: PortfolioVisibility;
  createdAt?: string | null;
  updatedAt?: string | null;
  skills?: SkillSummaryDto[];
}

export interface PortfolioProjectDto {
  id: number;
  title?: string | null;
  name?: string | null;
  description?: string | null;
  projectUrl?: string | null;
  pinned?: boolean | null;
  coverImage?: string | null;
  visibility: PortfolioVisibility;
  totalLikes?: number | null;
  skills?: SkillSummaryDto[];
  skillNames?: string[];
  media?: ProjectMediaDto[];
  createdAt?: string | null;
  updatedAt?: string | null;
}

export type ProjectMediaType = 'IMAGE' | 'VIDEO';

export interface ProjectMediaRequest {
  mediaUrl: string;
  mediaType: ProjectMediaType;
  orderIndex: number;
}

export interface ProjectMediaDto extends ProjectMediaRequest {
  id?: number;
}

export interface UploadProjectMediaResponse {
  mediaUrl: string;
  mediaType: ProjectMediaType;
  filename: string;
}

export interface CreateProjectRequest {
  title: string;
  description: string;
  projectUrl: string;
  pinned: boolean;
  visibility: PortfolioVisibility;
  skillIds: number[];
  media: ProjectMediaRequest[];
}

export interface CreateCollectionRequest {
  name: string;
  description: string;
  visibility: PortfolioVisibility;
}

export interface PortfolioCollectionDto {
  id: number;
  name?: string | null;
  title?: string | null;
  description?: string | null;
  visibility: PortfolioVisibility;
  projectCount?: number | null;
  projects?: Array<{ id: number }>;
}

export interface PortfolioResponse {
  portfolio: PortfolioDto;
  owner: PortfolioOwnerDto;
  profile?: PortfolioProfileDto | null;
  projects: PortfolioProjectDto[];
  collections: PortfolioCollectionDto[];
}

export interface PortfolioUpsertRequest {
  title?: string | null;
  bio?: string | null;
  coverImage?: string | null;
  job?: string | null;
  githubUrl?: string | null;
  linkedinUrl?: string | null;
  openToWork?: boolean | null;
  availableForFreelance?: boolean | null;
  visibility?: PortfolioVisibility | null;
  skillIds?: number[];
}

export interface ApiErrorBody {
  error?: string;
  detail?: string;
  errors?: Record<string, string>;
}

export type ReclamationStatus = 'PENDING' | 'IN_REVIEW' | 'RESOLVED';

export interface ReclamationResponse {
  id: number;
  submitterUsername?: string | null;
  subject: string;
  description: string;
  status: ReclamationStatus;
  adminResponse: string | null;
  createdAt: string;
  updatedAt: string;
  reviewedAt: string | null;
}

export interface CreateReclamationRequest {
  subject: string;
  description: string;
}

export interface AdminReclamationUpdateRequest {
  status: 'IN_REVIEW' | 'RESOLVED';
  adminResponse?: string | null;
}

export type AccountStatus = 'ACTIVE' | 'SUSPENDED' | 'PENDING_VERIFICATION';

export interface AdminDashboardSummaryResponse {
  totalUsers: number;
  totalStudents: number;
  totalAdmins: number;
  totalPortfolios: number;
  totalCvProfiles: number;
  totalCvDrafts: number;
  totalReclamations: number;
  totalSubscriptions: number;
  activeSubscriptions: number;
  pendingReclamations: number;
  pendingAdminActions: number;
}

export interface AdminMonthlyActivityPointResponse {
  label: string;
  users: number;
  portfolios: number;
  cvDrafts: number;
  reclamations: number;
  subscriptions: number;
}

export interface AdminStatusCountResponse {
  status: string;
  count: number;
}

export interface AdminRecentActivityResponse {
  type: string;
  title: string;
  subtitle: string | null;
  status: string | null;
  createdAt: string | null;
}

export interface AdminDashboardActivityResponse {
  monthlyActivity: AdminMonthlyActivityPointResponse[];
  reclamationsByStatus: AdminStatusCountResponse[];
  recentActivity: AdminRecentActivityResponse[];
}

export interface AdminPageResponse<T> {
  items: T[];
  total: number;
  page: number;
  size: number;
  totalPages: number;
}

export interface AdminUserSummaryResponse {
  id: number;
  username: string;
  email: string;
  firstName: string | null;
  lastName: string | null;
  role: Role;
  accountStatus: AccountStatus;
  createdAt: string | null;
  lastLogin: string | null;
}

export interface AdminUserDetailResponse extends AdminUserSummaryResponse {
  portfolioCount: number;
  cvProfileCount: number;
  cvDraftCount: number;
  reclamationCount: number;
  subscriptionCount: number;
}

export interface AdminUserStatusUpdateRequest {
  active: boolean;
}

export interface AdminPortfolioSummaryResponse {
  id: number;
  title: string;
  ownerUsername: string | null;
  ownerName: string | null;
  visibility: PortfolioVisibility | null;
  moderationStatus: string | null;
  projectCount: number;
  totalViews: number;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface AdminCvSummaryResponse {
  userId: number;
  profileId: number | null;
  username: string;
  fullName: string | null;
  headline: string | null;
  visibility: PortfolioVisibility | null;
  preferredTemplate: string | null;
  draftCount: number;
  profileUpdatedAt: string | null;
  latestDraftUpdatedAt: string | null;
}

export interface AdminSubscriptionSummaryResponse {
  id: number;
  userId: number | null;
  username: string | null;
  email: string | null;
  subscriptionPlan: SubscriptionPlan | null;
  status: SubscriptionStatus | null;
  dateOfSubscription: string | null;
  expirationDate: string | null;
  billingDate: string | null;
  autoRenew: boolean;
}

export interface CvPreviewSkillDto {
  id: number;
  name: string;
  category?: string | null;
}

export interface CvPreviewSkillGroupDto {
  category?: string | null;
  skills?: CvPreviewSkillDto[];
}

export interface CvPreviewProjectDto {
  id: number;
  title?: string | null;
  description?: string | null;
  projectUrl?: string | null;
  visibility?: PortfolioVisibility | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  imageUrl?: string | null;
  collectionName?: string | null;
  skills?: CvPreviewSkillDto[];
}

export interface CvPreviewProfileDto {
  fullName?: string | null;
  email?: string | null;
  phone?: string | null;
  headline?: string | null;
  summary?: string | null;
  location?: string | null;
  profilePicture?: string | null;
  githubUrl?: string | null;
  linkedinUrl?: string | null;
  linkedInUrl?: string | null;
  preferredTemplate?: string | null;
  language?: string | null;
  visibility?: PortfolioVisibility | null;
}

export interface CvEducationDto {
  school?: string | null;
  degree?: string | null;
  fieldOfStudy?: string | null;
  location?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  current?: boolean | null;
  description?: string | null;
}

export interface CvExperienceDto {
  company?: string | null;
  role?: string | null;
  location?: string | null;
  startDate?: string | null;
  endDate?: string | null;
  current?: boolean | null;
  summary?: string | null;
}

export interface CvLanguageDto {
  name?: string | null;
  proficiency?: string | null;
}

export interface CvPreviewMetaDto {
  [key: string]: unknown;
}

export interface CvPreviewResponse {
  profile?: CvPreviewProfileDto | null;
  skillsByCategory?: CvPreviewSkillGroupDto[];
  projects?: CvPreviewProjectDto[];
  education?: CvEducationDto[];
  experience?: CvExperienceDto[];
  languages?: CvLanguageDto[];
  meta?: CvPreviewMetaDto | null;
}

export interface CvProfileResponse {
  id?: number | null;
  headline?: string | null;
  summary?: string | null;
  professionalSummary?: string | null;
  phone?: string | null;
  location?: string | null;
  preferredTemplate?: string | null;
  language?: string | null;
  visibility?: PortfolioVisibility | null;
  selectedProjectIds?: number[];
  education?: CvEducationDto[];
  experience?: CvExperienceDto[];
  languages?: CvLanguageDto[];
}

export interface UpdateCvProfileRequest {
  headline?: string | null;
  summary?: string | null;
  professionalSummary?: string | null;
  phone?: string | null;
  location?: string | null;
  preferredTemplate?: string | null;
  language?: string | null;
  visibility?: PortfolioVisibility | null;
  selectedProjectIds?: number[];
  education?: CvEducationDto[];
  experience?: CvExperienceDto[];
  languages?: CvLanguageDto[];
}

export type CvDraftSectionType = 'SKILLS' | 'PROJECTS' | 'PROFILE' | 'EXPERIENCE' | 'EDUCATION' | 'LANGUAGES' | string;

export interface CvDraftSectionDto {
  type?: CvDraftSectionType | null;
  title?: string | null;
  orderIndex?: number | null;
  visible?: boolean | null;
  content?: unknown;
}

export interface CvDraftDto {
  id?: number | null;
  theme?: string | null;
  preferredTemplate?: string | null;
  language?: string | null;
  settings?: unknown;
  sections?: CvDraftSectionDto[];
}

export interface CvDraftUpdateRequest {
  theme?: string | null;
  settings?: unknown;
  sections?: CvDraftSectionDto[];
}

export type CvAiImproveTone = 'ATS_PROFESSIONAL' | 'SHORT' | 'TECHNICAL' | 'SIMPLE';
export type CvAiImproveMaxLength = 'SHORT' | 'MEDIUM';

export interface CvAiImproveRequest {
  topic: string;
  sectionType: string;
  field: string;
  text: string;
  targetTone: CvAiImproveTone;
  maxLength: CvAiImproveMaxLength;
  context: Record<string, unknown>;
}

export interface CvAiImproveResponse {
  suggestion: string;
}

export interface CvAiChatRequest {
  message: string;
  draftId: number | null;
  contextMode: 'CURRENT_CV';
}

export interface CvAiChatResponse {
  reply: string;
  score?: number | null;
  suggestedActions?: string[] | null;
}

export type CvJobMatchTone = 'ATS_PROFESSIONAL' | 'CONFIDENT' | 'CONCISE' | 'INTERNSHIP' | 'SENIOR';

export interface CvJobMatchRequest {
  draftId: number;
  targetJobTitle: string;
  jobDescription: string;
  tone: CvJobMatchTone;
  language: string;
}

export interface CvJobMatchProjectSuggestion {
  projectId: number;
  originalDescription?: string | null;
  improvedDescription?: string | null;
}

export interface CvJobMatchResponse {
  targetJobTitle?: string | null;
  extractedKeywords?: string[] | null;
  matchingKeywords?: string[] | null;
  missingKeywords?: string[] | null;
  improvedSummary?: string | null;
  improvedProjects?: CvJobMatchProjectSuggestion[] | null;
  recommendations?: string[] | null;
}

export type CvDraftApiResponse = CvDraftDto & {
  draft?: CvDraftDto | null;
};

export type PortfolioMentorReplyMode = 'ADVICE' | 'REWRITE' | 'EXPLAIN';

export interface PortfolioMentorChatRequest {
  message: string;
  target: string;
  replyMode: PortfolioMentorReplyMode;
}

export interface PortfolioMentorChatResponse {
  mainMessage?: string | null;
  notes?: string[] | null;
  rewrites?: string[] | null;
  rawResponse?: string | null;
  suggestedActions?: string[] | null;
}

export type ExploreVisibilityFilter = 'PUBLIC' | 'FRIENDS' | 'PUBLIC_AND_FRIENDS';
export type ExploreSort = 'RELEVANCE' | 'NEWEST' | 'MOST_VIEWED' | 'MOST_LIKED';

export interface ExploreOptionDto {
  value: string;
  label: string;
}

export interface ExplorePortfolioCardDto {
  portfolioId: number;
  ownerId: number;
  ownerUsername: string;
  displayName: string;
  profileImage?: string | null;
  location?: string | null;
  portfolioTitle?: string | null;
  bio?: string | null;
  jobTitle?: string | null;
  family?: string | null;
  visibility: PortfolioVisibility;
  projectCount: number;
  totalViews?: number | null;
  totalLikes?: number | null;
  githubUrl?: string | null;
  linkedinUrl?: string | null;
  topSkills: SkillSummaryDto[];
}

export interface ExploreProjectCardDto {
  projectId: number;
  portfolioId: number;
  ownerId: number;
  ownerUsername: string;
  ownerDisplayName: string;
  ownerProfileImage?: string | null;
  title?: string | null;
  description?: string | null;
  projectUrl?: string | null;
  mediaUrl?: string | null;
  mediaType?: ProjectMediaType | null;
  family?: string | null;
  visibility: PortfolioVisibility;
  views?: number | null;
  likes?: number | null;
  topSkills: SkillSummaryDto[];
}

export interface ExploreCollectionCardDto {
  collectionId: number;
  portfolioId?: number | null;
  ownerId?: number | null;
  ownerUsername?: string | null;
  ownerDisplayName?: string | null;
  ownerProfileImage?: string | null;
  name?: string | null;
  title?: string | null;
  description?: string | null;
  mediaUrl?: string | null;
  mediaType?: ProjectMediaType | null;
  family?: string | null;
  visibility: PortfolioVisibility;
  projectCount?: number | null;
  views?: number | null;
  likes?: number | null;
  topSkills?: SkillSummaryDto[] | null;
  skills?: SkillSummaryDto[] | null;
}

export interface ExploreSkillGroupDto {
  category?: string | null;
  skills?: SkillSummaryDto[] | null;
}

export interface ExplorePortfolioDetailDto {
  portfolioId?: number | null;
  id?: number | null;
  ownerId?: number | null;
  ownerUsername?: string | null;
  displayName?: string | null;
  fullName?: string | null;
  profileImage?: string | null;
  portfolioTitle?: string | null;
  title?: string | null;
  jobTitle?: string | null;
  headline?: string | null;
  bio?: string | null;
  about?: string | null;
  location?: string | null;
  city?: string | null;
  country?: string | null;
  githubUrl?: string | null;
  linkedinUrl?: string | null;
  visibility?: PortfolioVisibility | null;
  openToWork?: boolean | null;
  availableForFreelance?: boolean | null;
  freelance?: boolean | null;
  totalViews?: number | null;
  views?: number | null;
  projectCount?: number | null;
  collectionCount?: number | null;
  skillsByCategory?: ExploreSkillGroupDto[] | null;
  skills?: SkillSummaryDto[] | null;
  projects?: ExploreProjectCardDto[] | null;
  collections?: ExploreCollectionCardDto[] | null;
  owner?: PortfolioOwnerDto | null;
  profile?: PortfolioProfileDto | null;
  portfolio?: PortfolioDto | null;
}

export interface ExploreProjectDetailDto {
  projectId?: number | null;
  id?: number | null;
  portfolioId?: number | null;
  ownerId?: number | null;
  ownerName?: string | null;
  ownerUsername?: string | null;
  ownerDisplayName?: string | null;
  ownerAvatarUrl?: string | null;
  ownerProfileImage?: string | null;
  ownerJob?: string | null;
  title?: string | null;
  description?: string | null;
  projectUrl?: string | null;
  mediaUrl?: string | null;
  mediaType?: ProjectMediaType | null;
  media?: ProjectMediaDto[] | null;
  visibility?: PortfolioVisibility | null;
  views?: number | null;
  likes?: number | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  topSkills?: SkillSummaryDto[] | null;
  skills?: SkillSummaryDto[] | null;
  owner?: PortfolioOwnerDto | null;
  portfolio?: PortfolioDto | null;
  project?: PortfolioProjectDto | null;
}

export interface ExploreCollectionDetailDto {
  collectionId?: number | null;
  id?: number | null;
  portfolioId?: number | null;
  ownerId?: number | null;
  ownerUsername?: string | null;
  ownerDisplayName?: string | null;
  ownerProfileImage?: string | null;
  name?: string | null;
  title?: string | null;
  description?: string | null;
  visibility?: PortfolioVisibility | null;
  projectCount?: number | null;
  mediaUrl?: string | null;
  mediaType?: ProjectMediaType | null;
  skills?: SkillSummaryDto[] | null;
  topSkills?: SkillSummaryDto[] | null;
  projects?: ExploreProjectCardDto[] | null;
  owner?: PortfolioOwnerDto | null;
  portfolio?: PortfolioDto | null;
}
