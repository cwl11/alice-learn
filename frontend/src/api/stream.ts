/**
 * 以 SSE 方式请求 AI 范文。
 * - 普通 `data:` 事件：正文片段，逐块回调
 * - `event: error`：后端业务错误（如未配置 Key、DeepSeek 报错），抛出携带原始信息的 Error
 * - HTTP 非 2xx：尝试解析 JSON 的 message（401/403 等由 Security 直接返回 JSON）
 */
export async function streamEssaySample(
  essayId: number,
  onChunk: (text: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const token = localStorage.getItem('token')
  const response = await fetch(`/api/writing/essays/${essayId}/sample`, {
    method: 'POST',
    headers: {
      Authorization: token ? `Bearer ${token}` : '',
      Accept: 'text/event-stream',
    },
    signal,
  })
  if (!response.ok || !response.body) {
    let message = `范文生成失败（HTTP ${response.status}）`
    try {
      const payload = await response.json()
      if (payload?.message) message = payload.message
    } catch {
      // 非 JSON 响应，保留默认文案
    }
    throw new Error(message)
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  let received = false

  const handleEvent = (raw: string) => {
    let eventName = 'message'
    const dataLines: string[] = []
    for (const line of raw.split('\n')) {
      if (line.startsWith('event:')) eventName = line.slice(6).trim()
      // Spring 的 SseEmitter 写的是 "data:" 紧跟内容、不带空格；模型的 token 常以空格开头，
      // 这里只去掉 "data:" 本身，绝不能再吃掉一个空格，否则单词会粘在一起。
      else if (line.startsWith('data:')) dataLines.push(line.slice(5))
    }
    const data = dataLines.join('\n')
    if (eventName === 'error') {
      throw new Error(data || 'AI 服务返回错误')
    }
    if (data && data !== '[DONE]') {
      received = true
      onChunk(data)
    }
  }

  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n\n')
    buffer = parts.pop() ?? ''
    for (const part of parts) handleEvent(part)
  }
  if (buffer.trim()) handleEvent(buffer)
  if (!received) {
    throw new Error('AI 没有返回内容，请稍后重试')
  }
}
