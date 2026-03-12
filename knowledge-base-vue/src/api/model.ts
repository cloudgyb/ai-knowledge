import request from '@/utils/request'
import type {AiModelProvider, ApiResponse, SysAiModel} from "@/api/model/types";

export const modelApi = {
    getAiModelTypes(): Promise<ApiResponse<any>> {
        return request.get('/ai/model/types')
    },
    // 获取 AI 模型供应商列表
    // 获取 AI 模型列表
    getList(params?: { name?: string; type?: string }) {
        return request.get('/ai/model/list', {params})
    },

    // 获取 AI 模型详情
    getDetail(id: number) {
        return request.get(`/ai/model/detail/${id}`)
    },

    // 新增 AI 模型
    add(data: any) {
        return request.post('/ai/model/add', data)
    },

    // 更新 AI 模型
    update(data: any) {
        return request.post('/ai/model/update', data)
    },

    // 删除 AI 模型
    delete(id: number) {
        return request.post('/ai/model/delete', null, {params: {id}})
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
