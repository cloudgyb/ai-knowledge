export interface ApiResponse<T> {
    code: string
    message: string
    data: T
}

/**
 * AI 模型配置
 */
export interface AiModel {
    /**
     * 模型 ID
     */
    id?: number;

    /**
     * 自定义名称
     */
    customName?: string;

    modelId?: number;

    /**
     * 模型名称
     */
    modelName?: string;

    /**
     * 模型类型
     */
    modelType?: string;

    /**
     * 模型 API URL
     */
    modelUrl?: string;

    /**
     * 模型 API Key
     */
    modelApiKey?: string;

    /**
     * 模型 API 密钥
     */
    modelApiSecret?: string;

    /**
     * 模型提供商 ID
     */
    providerId?: number;

    /**
     * 创建者 ID
     */
    createUserId?: number;

    /**
     * 模型状态 (0-未激活，1-已激活)
     */
    status?: number;

    /**
     * 创建时间
     */
    createTime?: Date | string;

    /**
     * 更新时间
     */
    updateTime?: Date | string;
}

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
    /**
     * 模型 ID
     */
    id?: number;

    /**
     * 模型名称
     */
    modelName?: string;

    /**
     * 模型类型
     */
    modelType?: string;

    modelTypeName?: string;

    modelUrl?: string;

    /**
     * 模型提供商 ID
     */
    providerId?: number;

    /**
     * 创建时间
     */
    createTime?: Date | string;

    /**
     * 更新时间
     */
    updateTime?: Date | string;

    /**
     * 是否删除 (0-未删除，1-已删除)
     */
    deleted?: number;
}
