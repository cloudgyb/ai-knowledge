<template>
  <a-layout class="layout">
    <!-- 顶部菜单 -->
    <a-layout-header class="header">
      <div class="header-content">
        <div class="logo">
          <h1 class="system-title">{{AppConfig.title}}</h1>
        </div>
        <div class="user-menu">
          <a-dropdown v-if="userStore.userInfo">
            <span class="user-info">
              <a-avatar size="small">{{ userStore.userInfo.username?.[0]?.toUpperCase() }}</a-avatar>
              <span class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username }}</span>
              <DownOutlined/>
            </span>
            <template #overlay>
              <a-menu>
                <a-menu-item key="my-knowledge">
                  <template #icon>
                    <UserOutlined/>
                  </template>
                  我的知识库
                </a-menu-item>
                <a-menu-item key="account-settings">
                  <template #icon>
                    <SettingOutlined/>
                  </template>
                  账户设置
                </a-menu-item>
                <a-menu-divider/>
                <a-menu-item key="logout" @click="handleLogout">
                  <template #icon>
                    <LogoutOutlined/>
                  </template>
                  退出
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </div>
      </div>
    </a-layout-header>

    <a-layout>
      <!-- 左侧边栏 -->
      <a-layout-sider v-model:selectedKeys="selectedKeys" theme="light" width="200" style="background: #fff">
        <a-menu
            v-model:selectedKeys="selectedKeys"
            mode="inline"
            :items="menuItems"
            @select="handleMenuSelect"
        />
      </a-layout-sider>

      <!-- 主体内容 -->
      <a-layout-content class="content">
        <router-view/>
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import {ref, h, onMounted} from 'vue'
import {useRouter, useRoute} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {
  ApiOutlined,
  FolderOutlined,
  MessageOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  DownOutlined
} from '@ant-design/icons-vue'
import {authApi} from '@/api/auth'
import {message} from 'ant-design-vue'
import {AppConfig} from "@/utils/env.ts";

const router = useRouter()
const userStore = useUserStore()
const route = useRoute()

// 初始化用户信息（从 localStorage 恢复）
onMounted(() => {
  userStore.initUserInfo()
})

const selectedKeys = ref<string[]>(['ai-model'])

const menuItems = ref([
  {
    key: 'ai-model',
    icon: () => h(ApiOutlined),
    label: 'AI 模型管理'
  },
  {
    key: 'knowledge-base',
    icon: () => h(FolderOutlined),
    label: '知识库管理'
  },
  {
    key: 'assistant',
    icon: () => h(MessageOutlined),
    label: 'AI 小助理'
  }
])

const handleMenuSelect = ({key}: { key: string }) => {
  router.push(`/${key}`)
}

const handleLogout = async () => {
  try {
    // 调用登出接口
    await authApi.logout()
    message.success('退出成功')
  } catch (error) {
    console.error('登出失败:', error)
  } finally {
    // 清除本地用户信息
    userStore.clearUserInfo()
    // 跳转到登录页
    router.push('/login')
  }
}

onMounted(() => {
  //处理页面刷新后的菜单选中
  let path = route.path
  let key = menuItems.value.find(item => path.startsWith("/" + item.key))?.key
  selectedKeys.value = [key || menuItems.value[0].key]
})
</script>

<style scoped>
.layout {
  min-height: 100vh;
}

.header {
  background: #fff;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
}

.logo {
  display: flex;
  align-items: center;
}

.system-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1890ff;
}

.user-menu {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  margin-right: 4px;
}

.content {
  margin: 16px;
  padding: 16px;
  background: #fff;
  border-radius: 4px;
}
</style>
