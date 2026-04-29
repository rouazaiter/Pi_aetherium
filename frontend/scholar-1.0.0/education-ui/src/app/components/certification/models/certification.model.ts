export type Difficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED';
export type CertStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type QuestionType = 'FILL_BLANK' | 'MATCH' | 'CODE' | 'EXPLAIN' | 'WRITE'
  | 'MCQ' | 'MULTI_SELECT' | 'SCENARIO' | 'ORDERING' | 'DRAG_DROP';

export interface MatchPair {
  left: string;
  right: string;
}

export interface QuestionCreate {
  type: QuestionType;
  questionText: string;
  points: number;
  orderIndex: number;
  expectedAnswer?: string;
  codeLanguage?: string;
  matchPairs?: MatchPair[];
}

export interface ExamCreate {
  title: string;
  timeLimit: number;
  passingScore: number;
  questions: QuestionCreate[];
}

export interface CertificationCreate {
  title: string;
  description: string;
  category: string;
  difficulty: Difficulty;
  status: CertStatus;
  price: number;
  validFrom: string | null;
  expiresAt: string | null;
  durationMinutes: number;
  passingScore: number;
  exams: ExamCreate[];
  coverImageUrl?: string | null;
}

export interface Certification {
  id: number;
  title: string;
  description: string;
  category: string;
  difficulty: Difficulty;
  status: CertStatus;
  price: number;
  validFrom: string;
  expiresAt: string;
  durationMinutes: number;
  passingScore: number;
  createdAt: string;
  updatedAt: string;
  coverImageUrl: string | null;
}

export interface ChoiceResponse {
  id: number;
  matchLeft: string;
  matchRight: string;
  text: string;
  isCorrect: boolean;
}

export interface QuestionResponse {
  id: number;
  type: QuestionType;
  questionText: string;
  expectedAnswer: string;
  codeLanguage: string | null;
  points: number;
  orderIndex: number;
  choices: ChoiceResponse[];
  options: string[] | null;   // MCQ / MULTI_SELECT / SCENARIO / ORDERING
}

export interface ExamResponse {
  id: number;
  title: string;
  timeLimit: number;
  passingScore: number;
  questions: QuestionResponse[];
}

export interface CertificationDetail extends Certification {
  exams: ExamResponse[];
}

export interface LlmGenerateRequest {
  topic: string;
  description: string;
  difficulty: Difficulty;
  numberOfQuestions: number;
  timeLimitMinutes: number;
}

export interface QuestionUpdateRequest {
  type: QuestionType;
  questionText: string;
  expectedAnswer: string | null;
  codeLanguage: string | null;
  points: number;
  orderIndex: number;
  matchPairs: MatchPair[] | null;
  options: string[] | null;
}

