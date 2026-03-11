import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface UserInfo {
  id: number
  username: string
  nickname?: string
  avatar?: string
}

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)

  // 模拟用户登录，实际项目中应该调用登录接口
  function setUserInfo(info: UserInfo) {
    userInfo.value = info
  }

  function clearUserInfo() {
    userInfo.value = null
  }

  return {
    userInfo,
    setUserInfo,
    clearUserInfo
  }
})
