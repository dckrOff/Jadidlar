export interface Jadid {
  id: string;
  name: string;
  nameUz: string;
  birthYear: number;
  deathYear: number;
  shortDescription: string;
  fullDescription: string;
  imageUrl: string;
  orderIndex: number;
}

export interface Book {
  id: string;
  title: string;
  authorId: string;
  authorName: string;
  publishYear: number;
  description: string;
  coverImageUrl: string;
  pdfUrl: string;
  rating: number;
  isFavorite: boolean;
  orderIndex: number;
}

export interface Question {
  id: string;
  questionText: string;
  answers: string[];
  correctAnswerIndex: number;
}

export interface Test {
  id: string;
  title: string;
  description: string;
  questionCount: number;
  timeLimit: number;
  questions: Question[];
}

export interface TestResult {
  id: string;
  testId: string;
  userId: string;
  score: number;
  totalQuestions: number;
  timeSpent: number;
  completedAt: Date;
  answers: {
    questionId: string;
    selectedAnswerIndex: number;
    isCorrect: boolean;
  }[];
}
