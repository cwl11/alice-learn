export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
}

export interface UserProfile {
  id: number
  username: string
  email: string
  targetScore?: number
  createdAt: string
}

export interface WritingTask {
  id: number
  taskType: string
  title: string
  description: string
  imageUrl?: string
  difficulty: string
}

export interface Essay {
  id: number
  taskId: number
  content: string
  wordCount: number
  status: string
  createdAt: string
}

export interface FeedbackItem {
  original: string
  suggestion: string
  reason: string
}

export interface EssayReview {
  id: number
  overallScore: number
  taScore: number
  ccScore: number
  lrScore: number
  graScore: number
  comment?: string
  sampleEssay?: string
  feedback: FeedbackItem[]
  createdAt: string
}

export interface EssayDetail {
  id: number
  taskId: number
  taskTitle: string
  taskType: string
  taskDescription: string
  content: string
  wordCount: number
  status: string
  createdAt: string
  review?: EssayReview
}

export interface WordItem {
  id: number
  word: string
  phonetic: string
  meaning: string
  example: string
  category: string
  inNotebook: boolean
  familiarity?: number
}

export interface ReadingPassage {
  id: number
  title: string
  content: string
  difficulty: string
  questions?: ReadingQuestion[]
}

export interface ReadingQuestion {
  id: number
  questionType: string
  question: string
  options: string[]
  answer?: string
  explanation?: string
}

export interface SubmitAnswersResponse {
  total: number
  correctCount: number
  items: {
    questionId: number
    userAnswer: string
    correctAnswer: string
    correct: boolean
    explanation: string
  }[]
}
