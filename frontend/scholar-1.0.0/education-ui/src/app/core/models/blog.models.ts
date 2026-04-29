export interface User {
  id: number;
  username: string;
  email: string;
}

export type PostStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type PostVisibility = 'PUBLIC' | 'PRIVATE' | 'FRIENDS_ONLY' | 'RESTRICTED';
export type DiscussionStatus = 'ACTIVE' | 'ARCHIVED' | 'DELETED';
export type ReactionType = 'LIKE' | 'LOVE' | 'LAUGH' | 'WOW' | 'SAD' | 'ANGRY';

export interface Post {
  id: number;
  title: string;
  slug: string;
  content: string;
  coverImage?: string;
  status: PostStatus;
  visibility: PostVisibility;
  urgencyLevel: 'NONE' | 'LOW' | 'HIGH' | 'CRITICAL';
  author: User;
  createdAt: string;
  updatedAt: string;
}

export interface Comment {
  id: number;
  content: string;
  author: User;
  sentiment?: 'POSITIVE' | 'NEGATIVE' | 'NEUTRAL';
  createdAt: string;
  updatedAt: string;
}

export interface Discussion {
  id: number;
  theme: string;
  status: DiscussionStatus;
  creator: User;
  solvedMessageId?: number;
  createdAt: string;
}

export interface DiscussionMessage {
  id: number;
  content: string;
  sender: User;
  replies?: DiscussionMessage[];
  reactions?: Reaction[];
  createdAt: string;
}

export interface Reaction {
  id: number;
  type: ReactionType;
  user: User;
  createdAt: string;
}

export interface LikeCount { count: number; }

export interface KnowledgeBaseArticle {
  id: number;
  question: string;
  answer: string;
  tags: string;
  views: number;
  author: User;
  createdAt: string;
  sourceDiscussion?: Discussion;
}
