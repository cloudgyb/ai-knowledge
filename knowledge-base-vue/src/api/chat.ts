import request from '@/utils/request'
import type {ApiResponse, ApiResponsePagination} from "@/api/model/types";
import type {ChatConversationMsgVO, Conversation} from "@/api/model/chatTypes";

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
    },
    // 获取会话列表
    getConversations(pageNum: number = 1, pageSize: number = 10): Promise<ApiResponse<ApiResponsePagination<Conversation>>> {
        const params = {pageNum, pageSize}
        return request.get('/ai/chat/c', {params})
    },
    addConversation(data: any): Promise<ApiResponse<string>> {
        return request.post('/ai/chat/c', data, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
    },
    deleteConversation(id: string): Promise<ApiResponse<any>> {
        return request.delete('/ai/chat/c', {params: {id}})
    },
    updateConversation(c: { id: string; title: string }): Promise<ApiResponse<any>> {
        return request.put('/ai/chat/c', c, {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
    },
    getConversationMessages(id: string): Promise<ApiResponse<ChatConversationMsgVO[]>> {
        return request.get('/ai/chat/c/history', {params: {id}})
    },
}
