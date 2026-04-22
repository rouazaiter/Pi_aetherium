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
}

export interface ProfileUpdateRequest {
  firstName?: string | null;
  lastName?: string | null;
  interests?: string[];
  description?: string | null;
  profilePicture?: string | null;
  recuperationEmail?: string | null;
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
}

export type FriendRelation = 'NONE' | 'FRIEND' | 'REQUEST_SENT' | 'REQUEST_RECEIVED';

export interface FriendSearchResponse {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
  relation: FriendRelation;
}

export type FriendRequestStatus = 'PENDING' | 'ACCEPTED' | 'DECLINED';

export interface FriendRequestResponse {
  id: number;
  sender: FriendResponse;
  receiver: FriendResponse;
  status: FriendRequestStatus;
  createdAt: string;
}

export interface ApiErrorBody {
  error?: string;
  detail?: string;
  errors?: Record<string, string>;
}
