import request from '@/utils/request'
import type {ApiResponse, ApiResponsePagination} from "@/api/model/types";
import type {KnowledgeBaseDoc} from "@/api/model/knowledgeBaseTypes";

export const kbDocApi = {
    uploadDoc(formData: FormData): Promise<ApiResponse<any>> {
        return request.post('/kb/doc/add', formData)
    },
    // 获取 AI 模型供应商列表
    // 获取 AI 模型列表
    getList(params?: {
        kbId: number;
        title?: string;
        pageNum?: number;
        pageSize?: number
    }): Promise<ApiResponse<ApiResponsePagination<KnowledgeBaseDoc>>> {
        return request.get('/kb/doc/list', {params})
    },

    // 更新 AI 模型
    update(data: any): Promise<ApiResponse<any>> {
        return request.put('/ai/model/update', data)
    },

    // 删除 AI 模型
    delete(id: number|undefined): Promise<ApiResponse<any>> {
        return request.delete('/kb/doc', {params: {id}})
    }
}
