export type EnrollmentStatus = 'ENROLLED' | 'COMPLETED';

export interface EnrollmentDTO {
  id: number;
  userIdentifier: string;
  fullName: string | null;
  certificationId: number;
  certificationTitle: string;
  amountPaid: number;
  status: EnrollmentStatus;
  score: number | null;
  passed: boolean | null;
  enrolledAt: string;
  completedAt: string | null;
  isVerified: boolean;
  attemptCount: number;
  maxAttempts: number;
  lastAttemptAt: string | null;
}

export interface ExamResultDTO {
  enrollmentId: number;
  score: number;
  passed: boolean;
  passingScore: number;
  totalQuestions: number;
  answeredQuestions: number;
}

export interface AnswerDTO {
  questionId: number;
  answer: string;
}

export interface SubmitExamRequest {
  userIdentifier: string;
  answers: AnswerDTO[];
}
