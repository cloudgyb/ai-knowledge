// 验证码相关类型
export interface CaptchaCode {
    captchaCodeImage: string  // base64格式的验证码图片
    uuid: string              // 验证码唯一标识
}

// 登录表单类型
export interface LoginForm {
    username: string          // 用户名
    password: string          // 密码
    captchaCode: string       // 验证码
    uuid: string              // 验证码UUID
}

// 登录结果类型
export interface LoginResult {
    token: string             // 登录令牌
    reason: string            // 登录结果说明
    code: number              // 结果码：0-成功，-1-用户名或密码错误，-2-验证码错误
}

// 用户信息类型
export interface UserInfo {
    id: number
    username: string
    nickname?: string
    avatar?: string
}
