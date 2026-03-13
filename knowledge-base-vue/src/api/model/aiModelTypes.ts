/**
 * AI 模型提供商
 */
export interface AiModelProvider {
    id: number
    providerName: string
    providerCode: string
    providerCompany: string
    logoUrl?: string
    description?: string
}

/**
 * 系统支持的 AI 模型
 */
export interface SysAiModel {
    id?: number;
    modelName?: string;
    modelType?: string;
    modelTypeName?: string;
    modelUrl?: string;
    providerId?: number;
    createTime?: Date | string;
    updateTime?: Date | string;
    deleted?: number;
}


/**
 * AI 模型配置
 */
export interface AiModel {
    id?: number;
    customName?: string;
    modelId?: number;
    modelName?: string;
    modelType?: string;
    modelUrl?: string;
    modelApiKey?: string;
    modelApiSecret?: string;
    providerId?: number;
    createUserId?: number;
    status?: number;
    createTime?: Date | string;
    updateTime?: Date | string;
    config?: {};
    provider?: AiModelProvider;
}

export interface AiModelSearchForm {
    modelName?: string;
    modelType?: string;
    providerId?: number;
    status?: number;
}