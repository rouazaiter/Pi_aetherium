export type ApplicationStatus = 'PENDING' | 'ACCEPTED' | 'REJECTED';

export interface Application {
  id: number;
  message: string;
  status: ApplicationStatus;
  appliedAt: string;
  applicant: {
    id: number;
    username: string;
    email: string;
  };
}
