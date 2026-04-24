export type LeaderboardType = 'APPLICANTS' | 'CREATORS';

export type LeaderboardCategory =
  | 'Software Development'
  | 'Networks and Systems'
  | 'Cybersecurity'
  | 'Data / Artificial Intelligence'
  | 'Cloud Computing';

export interface LeaderboardEntry {
  userId: number;
  username: string;
  email: string;
  totalCount: number;
  successCount: number;
  pendingCount: number;
  failedCount: number;
  successRate: number;
  realScore: number;
  aiQualityScore: number;
  score: number;
  rank: number;
  badge: string;
}

export interface LeaderboardResponse {
  type: LeaderboardType;
  category?: LeaderboardCategory | null;
  days: number;
  limit: number;
  generatedAt: string;
  entries: LeaderboardEntry[];
}
