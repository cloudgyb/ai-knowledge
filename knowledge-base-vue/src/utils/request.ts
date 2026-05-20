import axios, {type AxiosError} from 'axios'
import type {AxiosInstance} from 'axios'
import {AppConfig} from './env'
import type {ApiResponse} from "@/api/model/types";

const request: AxiosInstance = axios.create({
    baseURL: AppConfig.apiBaseUrl,
    timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
    (config) => {
        // 从 localStorage 获取 token
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['token'] = token
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// 响应拦截器
request.interceptors.response.use(
    (response) => {
        const code: number = parseInt(response.data.code || '200');
        if (code > 399 && code <= 499) {
            throw new Error(response.data.message || '请求错误！')
        }
        if (code > 499) {
            throw new Error(response.data.message || '服务内部错误')
        }
        return response.data
    },
    (error: AxiosError<ApiResponse<any>>) => {
        // 处理 401 未授权错误
        if (error.response?.status === 401) {
            // 清除本地 token 和用户信息
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            // 跳转到登录页
            window.location.href = '/login'
        }
        return Promise.reject(new Error(error.response?.data?.message || '请求错误！'))
    }
)

export default request
