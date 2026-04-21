export type ServiceRequestStatus = 'OPEN' | 'CLOSED' | 'EXPIRED';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELED';
export type ServiceRequestCategory =
  | 'Software Development'
  | 'Networks and Systems'
  | 'Cybersecurity'
  | 'Data / Artificial Intelligence'
  | 'Cloud Computing';

export interface User {
  id: number;
  username: string;
  email: string;
}

export interface ServiceRequest {
  id: number;
  name: string;
  category: ServiceRequestCategory;
  description: string;
  status: ServiceRequestStatus;
  paymentStatus: PaymentStatus;
  price: number;
  files?: string;
  createdAt: string;
  updatedAt: string;
  expiringDate?: string;
  creator: User;
}

export interface ServiceRequestPayload {
  name: string;
  category: ServiceRequestCategory;
  description: string;
  expiringDate: string;
  price: number;
}

export interface CheckoutSessionResponse {
  sessionId: string;
  checkoutUrl: string;
}
