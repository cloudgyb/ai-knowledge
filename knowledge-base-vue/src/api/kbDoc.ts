import request from '@/utils/request'
import type {ApiResponse, ApiResponsePagination} from "@/api/model/types";
import type {AiModel, AiModelProvider, SysAiModel} from "@/api/model/aiModelTypes";
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

    // 获取 AI 模型详情
    getDetail(id: number) {
        return request.get(`/ai/model/detail/${id}`)
    },

    // 新增 AI 模型
    add(data: any): Promise<ApiResponse<any>> {
        return request.post('/ai/model', data)
    },

    // 更新 AI 模型
    update(data: any): Promise<ApiResponse<any>> {
        return request.put('/ai/model/update', data)
    },

    // 删除 AI 模型
    delete(id: number): Promise<ApiResponse<any>> {
        return request.delete('/ai/model', {params: {id}})
    },

    // 获取 AI 模型供应商列表
    getProviders(modelType?: string): Promise<ApiResponse<AiModelProvider[]>> {
        return request.get('/sys/ai/model/providers', {params: {modelType}})
    },
    // 获取系统内置的 AI 模型列表
    getSysAiModels(providerId: number): Promise<ApiResponse<SysAiModel[]>> {
        return request.get('/sys/ai/model/list', {params: {providerId}})
    }
}
