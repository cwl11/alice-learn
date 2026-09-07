import type { EssayStatus } from '../types'

export function formatTime(value?: string | null): string {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

export function formatDuration(seconds?: number | null): string {
  if (seconds == null) return '-'
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return m ? `${m} 分 ${s} 秒` : `${s} 秒`
}

const STATUS_TEXT: Record<EssayStatus, string> = {
  DRAFT: '草稿',
  SUBMITTED: '已提交',
  REVIEWED: '已批改',
}

const STATUS_TYPE: Record<EssayStatus, 'info' | 'warning' | 'success'> = {
  DRAFT: 'info',
  SUBMITTED: 'warning',
  REVIEWED: 'success',
}

export function statusText(status: EssayStatus | string): string {
  return STATUS_TEXT[status as EssayStatus] ?? status
}

export function statusType(status: EssayStatus | string): 'info' | 'warning' | 'success' {
  return STATUS_TYPE[status as EssayStatus] ?? 'info'
}
