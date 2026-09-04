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
    throw new Error('范文生成失败')
  }
  const reader = response.body.getReader()
  const decoder = new TextDecoder()
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const parts = buffer.split('\n\n')
    buffer = parts.pop() ?? ''
    for (const part of parts) {
      const lines = part.split('\n').filter((line) => line.startsWith('data:'))
      const data = lines.map((line) => line.replace(/^data:?\s?/, '')).join('\n')
      if (data && data !== '[DONE]') {
        onChunk(data)
      }
    }
  }
}
