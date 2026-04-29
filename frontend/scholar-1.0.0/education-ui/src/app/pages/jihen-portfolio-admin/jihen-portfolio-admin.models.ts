// Jihen Portfolio Admin

export namespace JihenPortfolioAdminModels {
  export type AdminTab = 'overview' | 'projects';
  export type ActivityRange = '7d' | '30d' | '90d';
  export type ProjectVisibility = 'PUBLIC' | 'FRIENDS_ONLY' | 'PRIVATE';
  export type ModerationStatus = 'ACTIVE' | 'UNDER_REVIEW' | 'BLOCKED';

  export interface OverviewResponse {
    totalPortfolios?: number | null;
    totalProjects?: number | null;
    totalCollections?: number | null;
    totalSkills?: number | null;
    usersWithPortfolio?: number | null;
    publicPortfolios?: number | null;
    friendsOnlyPortfolios?: number | null;
    privatePortfolios?: number | null;
    portfolioGrowthPct?: number | null;
    projectGrowthPct?: number | null;
    collectionGrowthPct?: number | null;
    skillGrowthPct?: number | null;
    usersWithPortfolioGrowthPct?: number | null;
    growth?: Record<string, number | null | undefined> | null;
  }

  export interface ActivityPoint {
    label?: string | null;
    date?: string | null;
    day?: string | null;
    portfolios?: number | null;
    projects?: number | null;
    collections?: number | null;
  }

  export interface TrendingSkillItem {
    skillName?: string | null;
    name?: string | null;
    usageCount?: number | null;
    count?: number | null;
  }

  export interface RecentItem {
    id?: number | null;
    itemId?: number | null;
    title?: string | null;
    name?: string | null;
    type?: string | null;
    itemType?: string | null;
    owner?: string | null;
    ownerName?: string | null;
    ownerUsername?: string | null;
    visibility?: ProjectVisibility | null;
    status?: ModerationStatus | string | null;
    moderationStatus?: ModerationStatus | string | null;
    views?: number | null;
    createdAt?: string | null;
  }

  export interface SkillChip {
    id?: number | null;
    name?: string | null;
  }

  export interface AdminProjectItem {
    id?: number | null;
    projectId?: number | null;
    title?: string | null;
    description?: string | null;
    thumbnailUrl?: string | null;
    mediaUrl?: string | null;
    owner?: string | null;
    ownerName?: string | null;
    ownerUsername?: string | null;
    portfolio?: string | null;
    portfolioTitle?: string | null;
    visibility?: ProjectVisibility | null;
    status?: ModerationStatus | string | null;
    moderationStatus?: ModerationStatus | string | null;
    views?: number | null;
    likes?: number | null;
    skills?: SkillChip[] | null;
    topSkills?: SkillChip[] | null;
    createdAt?: string | null;
  }

  export interface ProjectsPageResponse {
    items?: AdminProjectItem[] | null;
    total?: number | null;
    page?: number | null;
    size?: number | null;
    totalPages?: number | null;
  }

  export interface ProjectQuery {
    q?: string;
    visibility?: string;
    moderationStatus?: string;
    page?: number;
    size?: number;
    sort?: string;
  }

  export interface UpdateVisibilityRequest {
    visibility: ProjectVisibility;
  }

  export interface UpdateModerationRequest {
    status: ModerationStatus;
    reason: string | null;
  }
}
