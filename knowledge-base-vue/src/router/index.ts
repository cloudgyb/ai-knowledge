import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/ai-model',
    children: [
      {
        path: '/ai-model',
        name: 'AiModel',
        component: () => import('@/views/ai-model/index.vue'),
        meta: { title: 'AI 模型管理', icon: 'api' }
      },
      {
        path: '/knowledge-base',
        name: 'KnowledgeBase',
        component: () => import('@/views/knowledge-base/index.vue'),
        meta: { title: '知识库管理', icon: 'folder' }
      },
      {
        path: '/assistant',
        name: 'Assistant',
        component: () => import('@/views/assistant/index.vue'),
        meta: { title: 'AI 小助理', icon: 'message' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
