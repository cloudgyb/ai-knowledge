import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserInfo } from '@/api/model/authTypes'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)

  // 初始化用户信息（从 localStorage 恢复）
  function initUserInfo() {
    const storedUserInfo = localStorage.getItem('userInfo')
    if (storedUserInfo) {
      try {
        userInfo.value = JSON.parse(storedUserInfo)
      } catch (e) {
        console.error('解析用户信息失败:', e)
      }
    }
  }

  // 设置用户信息和 token
  function setUserInfo(info: UserInfo, token?: string) {
    userInfo.value = info
    if (token) {
      localStorage.setItem('token', token)
    }
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  // 清除用户信息
  function clearUserInfo() {
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return {
    userInfo,
    initUserInfo,
    setUserInfo,
    clearUserInfo
  }
})
