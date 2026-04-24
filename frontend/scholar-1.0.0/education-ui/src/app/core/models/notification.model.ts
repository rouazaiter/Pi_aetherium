export interface RealtimeNotification {
  type: string;
  message: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  suggestedAction: string;
  generatedByAi: boolean;
  serviceRequestId: number | null;
  applicationId: number | null;
  createdAt: string;
}
