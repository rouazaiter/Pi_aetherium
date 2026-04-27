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
