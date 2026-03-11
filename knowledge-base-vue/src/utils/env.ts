/**
 * 环境变量配置工具
 */

export const AppConfig = {
  /** 应用标题 */
  title: import.meta.env.VITE_APP_TITLE || 'AI 知识库管理系统',
  
  /** API 基础 URL */
  apiBaseUrl: import.meta.env.VITE_API_BASE_URL || '/api',
  
  /** 应用版本 */
  version: import.meta.env.VITE_APP_VERSION || '1.0.0',
  
  /** 应用环境 */
  env: import.meta.env.VITE_APP_ENV || 'development',
  
  /** 是否为开发环境 */
  isDev: import.meta.env.VITE_APP_ENV === 'development',
  
  /** 是否为生产环境 */
  isProd: import.meta.env.VITE_APP_ENV === 'production',
  
  /** 是否为测试环境 */
  isTest: import.meta.env.VITE_APP_ENV === 'test'
} as const

// 导出类型
export type AppConfigType = typeof AppConfig
