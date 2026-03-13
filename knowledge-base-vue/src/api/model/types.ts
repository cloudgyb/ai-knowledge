export interface ApiResponse<T> {
    code: string
    message: string
    data: T
}

export interface ApiResponsePagination<T> {
    total: number
    current: number
    size: number
    page: number
    records: T[]
}