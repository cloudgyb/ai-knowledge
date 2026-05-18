import request from '@/utils/request'
import type { ApiResponse } from "@/api/model/types"
import type { CaptchaCode, LoginForm, LoginResult } from "@/api/model/authTypes"

export const authApi = {
    // 获取验证码
    getCaptcha(): Promise<ApiResponse<CaptchaCode>> {
        return request.get('/captcha')
    },

    // 用户登录
    login(data: LoginForm): Promise<ApiResponse<LoginResult>> {
        return request.post('/login', data)
    },

    // 用户登出
    logout(): Promise<ApiResponse<void>> {
        return request.post('/logout')
    }
}
