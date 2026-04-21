export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';
export type PaymentStatus = 'PENDING' | 'PAID' | 'FAILED' | 'CANCELED';

export interface Application {
  id: number;
  message: string;
  status: ApplicationStatus;
  paymentStatus: PaymentStatus;
  appliedAt: string;
  applicant: {
    id: number;
    username: string;
    email: string;
  };
}
