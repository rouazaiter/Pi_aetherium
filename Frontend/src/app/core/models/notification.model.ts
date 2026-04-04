export interface RealtimeNotification {
  type: string;
  message: string;
  serviceRequestId: number | null;
  applicationId: number | null;
  createdAt: string;
}
