export interface AiCoachPreviewRequest {
  serviceRequestId: number;
  originalText: string;
  tone?: string;
  language?: string;
}

export interface AiCoachPreviewResponse {
  improvedText: string;
  relevanceScore: number;
  clarityScore: number;
  missingPoints: string[];
  suggestions: string[];
  changesSummary: string;
  generatedByAi: boolean;
}

export interface AiCoachApplyRequest {
  improvedText: string;
}
