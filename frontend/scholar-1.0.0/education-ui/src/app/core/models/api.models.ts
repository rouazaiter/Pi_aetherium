export type SocialProvider = 'GOOGLE' | 'FACEBOOK';

export interface SocialLoginRequest {
  provider: SocialProvider;
  token: string;
}

export type Role = 'user' | 'admin';

export type SubscriptionPlan = 'FREE' | 'STANDARD' | 'PREMIUM';

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
  expirationDate: string;
  billingDate?: string | null;
}

export interface SubscriptionResponse {
  id: number;
  dateOfSubscription: string | null;
  subscriptionPlan: SubscriptionPlan;
  expirationDate: string | null;
  billingDate: string | null;
}

export interface AddFriendRequest {
  username: string;
}

export interface FriendResponse {
  id: number;
  username: string;
  firstName: string | null;
  lastName: string | null;
}

export interface ApiErrorBody {
  error?: string;
  detail?: string;
  errors?: Record<string, string>;
}
