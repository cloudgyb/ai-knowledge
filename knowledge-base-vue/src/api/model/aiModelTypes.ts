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
export interface AiModelConfig {
    id?: number;
    modelId?: number;
    temperature?: number;      // 模型温度
    lexical?: number;          // 词汇属性
    talk?: number;             // 话题属性
    repeat?: number;           // 重复属性
    tokens?: number;           // 最大回复的token数
    timeout?: number;          // 等待AI响应的最长时间，单位为秒
}

/**
 * AI 模型
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
    isCustom?: boolean;
    createTime?: Date | string;
    updateTime?: Date | string;
    config: AiModelConfig;
    provider?: AiModelProvider;
}

export interface AiModelSearchForm {
    modelName?: string;
    modelType?: string;
    providerId?: number;
    status?: number;
}