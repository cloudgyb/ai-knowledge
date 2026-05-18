import {createRouter, createWebHistory} from 'vue-router'
import type {RouteRecordRaw} from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: {title: '登录', requiresAuth: false}
    },
    {
        path: '/',
        component: MainLayout,
        redirect: '/ai-model',
        meta: {requiresAuth: true},
        children: [
            {
                path: '/ai-model',
                name: 'AiModel',
                component: () => import('@/views/ai-model/index.vue'),
                meta: {title: 'AI 模型管理', icon: 'api'}
            },
            {
                path: '/knowledge-base',
                name: 'KnowledgeBase',
                component: () => import('@/views/knowledge-base/index.vue'),
                meta: {title: '知识库管理', icon: 'folder'}
            },
            {
                path: '/assistant',
                name: 'Assistant',
                component: () => import('@/views/assistant/index.vue'),
                children: [
                    {
                        path: '',
                        name: 'NewChat',
                        component: () => import('@/views/assistant/NewChat.vue'),
                        meta: {title: '新建对话'}
                    },
                    {
                        path: '/assistant/chat/:cid',
                        name: 'AssistantChat',
                        component: () => import('@/views/assistant/chat.vue'),
                        meta: {title: 'AI 小助理'}
                    }
                ],
                meta: {title: 'AI 小助理', icon: 'message'}
            }
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = (to.meta.title as string) || '基于 AI RAG 的在线知识库'
    
    // 检查是否需要认证
    const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
    const token = localStorage.getItem('token')
    
    if (requiresAuth && !token) {
        // 需要认证但没有 token，跳转到登录页
        next('/login')
    } else if (to.path === '/login' && token) {
        // 已登录但访问登录页，跳转到首页
        next('/ai-model')
    } else {
        next()
    }
})

export default router
