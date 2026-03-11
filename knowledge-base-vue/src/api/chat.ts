import request from '@/utils/request'

export interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

export const chatApi = {
  // SSE 流式聊天
  chat(params: { message: string; knowledgeBaseIds?: number[] }) {
    return request.post('/kb/chat', params, {
      responseType: 'text'
    })
  }
}
