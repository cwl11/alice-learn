import http from './http'
import type {
  ApiResult,
  Essay,
  EssayDetail,
  EssayReview,
  LoginResponse,
  PageResult,
  ReadingPassage,
  SubmitAnswersResponse,
  UserProfile,
  WordItem,
  WritingTask,
} from '../types'

export const authApi = {
  register(data: { username: string; password: string; email: string; targetScore?: number }) {
    return http.post<unknown, ApiResult<void>>('/auth/register', data)
  },
  login(data: { username: string; password: string }) {
    return http.post<unknown, ApiResult<LoginResponse>>('/auth/login', data)
  },
  me() {
    return http.get<unknown, ApiResult<UserProfile>>('/auth/me')
  },
}

export const writingApi = {
  tasks(params: { page?: number; size?: number; taskType?: string }) {
    return http.get<unknown, ApiResult<PageResult<WritingTask>>>('/writing/tasks', { params })
  },
  task(id: number) {
    return http.get<unknown, ApiResult<WritingTask>>(`/writing/tasks/${id}`)
  },
  saveEssay(data: { taskId: number; content: string; submit: boolean }) {
    return http.post<unknown, ApiResult<Essay>>('/writing/essays', data)
  },
  essay(id: number) {
    return http.get<unknown, ApiResult<EssayDetail>>(`/writing/essays/${id}`)
  },
  review(id: number) {
    return http.post<unknown, ApiResult<EssayReview>>(`/writing/essays/${id}/review`)
  },
}

export const meApi = {
  essays(params: { page?: number; size?: number }) {
    return http.get<unknown, ApiResult<PageResult<Essay>>>('/me/essays', { params })
  },
}

export const vocabApi = {
  words(params: { page?: number; size?: number; category?: string; keyword?: string }) {
    return http.get<unknown, ApiResult<PageResult<WordItem>>>('/vocab/words', { params })
  },
  notebook(params: { page?: number; size?: number }) {
    return http.get<unknown, ApiResult<PageResult<WordItem>>>('/vocab/notebook', { params })
  },
  add(wordId: number) {
    return http.post<unknown, ApiResult<void>>('/vocab/notebook', { wordId })
  },
  remove(wordId: number) {
    return http.delete<unknown, ApiResult<void>>(`/vocab/notebook/${wordId}`)
  },
  reviewCards() {
    return http.get<unknown, ApiResult<WordItem[]>>('/vocab/review')
  },
  review(wordId: number, remembered: boolean) {
    return http.post<unknown, ApiResult<WordItem>>('/vocab/review', { wordId, remembered })
  },
}

export const readingApi = {
  passages(params: { page?: number; size?: number }) {
    return http.get<unknown, ApiResult<PageResult<ReadingPassage>>>('/reading/passages', { params })
  },
  passage(id: number) {
    return http.get<unknown, ApiResult<ReadingPassage>>(`/reading/passages/${id}`)
  },
  submit(answers: { questionId: number; answer: string }[]) {
    return http.post<unknown, ApiResult<SubmitAnswersResponse>>('/reading/answers', { answers })
  },
}
