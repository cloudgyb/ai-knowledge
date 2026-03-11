import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig } from 'axios'
import { AppConfig } from './env'

const request: AxiosInstance = axios.create({
  baseURL: AppConfig.apiBaseUrl,
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    // 可以在这里添加 token 等认证信息
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default request
