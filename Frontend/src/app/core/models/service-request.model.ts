export type ServiceRequestStatus = 'OPEN' | 'CLOSED' | 'EXPIRED';

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface ServiceRequest {
  id: number;
  name: string;
  description: string;
  status: ServiceRequestStatus;
  files?: string;
  createdAt: string;
  updatedAt: string;
  expiringDate?: string;
  creator: User;
}
