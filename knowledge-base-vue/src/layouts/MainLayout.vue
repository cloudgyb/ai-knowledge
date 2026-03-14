<template>
  <a-layout class="layout">
    <!-- 顶部菜单 -->
    <a-layout-header class="header">
      <div class="header-content">
        <div class="logo">
          <h1 class="system-title">AI 知识库管理系统</h1>
        </div>
        <div class="user-menu">
          <a-dropdown>
            <span class="user-info">
              <a-avatar size="small">{{ userStore.userInfo?.username?.[0]?.toUpperCase() }}</a-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <DownOutlined />
            </span>
            <template #overlay>
              <a-menu>
                <a-menu-item key="my-knowledge">
                  <template #icon><UserOutlined /></template>
                  我的知识库
                </a-menu-item>
                <a-menu-item key="account-settings">
                  <template #icon><SettingOutlined /></template>
                  账户设置
                </a-menu-item>
                <a-menu-divider />
                <a-menu-item key="logout" @click="handleLogout">
                  <template #icon><LogoutOutlined /></template>
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
        <router-view />
      </a-layout-content>
    </a-layout>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, h } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  ApiOutlined,
  FolderOutlined,
  MessageOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  DownOutlined
} from '@ant-design/icons-vue'

const router = useRouter()
const userStore = useUserStore()

// 模拟用户信息（实际项目中应该从登录接口获取）
if (!userStore.userInfo) {
  userStore.setUserInfo({
    id: 1,
    username: 'admin',
    nickname: '管理员'
  })
}

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

const handleMenuSelect = ({ key }: { key: string }) => {
  router.push(`/${key}`)
}

const handleLogout = () => {
  // 退出逻辑，可以清除用户信息等
  console.log('退出登录')
  // 实际项目中可以在这里调用登出接口或跳转到登录页
}
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
