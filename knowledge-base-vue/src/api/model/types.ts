export interface ApiResponse<T> {
    code: string
    message: string
    data: T
}

export interface AiModel {
    id: number
    name: string
    type: string
    providerId: number
    providerName?: string
    config?: Record<string, any>
    status: 'active' | 'inactive'
    createTime?: string
    updateTime?: string
}

export interface AiModelProvider {
    id: number
    providerName: string
    logoUrl?: string
    description?: string
}
