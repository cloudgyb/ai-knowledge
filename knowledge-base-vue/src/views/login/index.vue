<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1 class="system-title">基于 AI RAG 的在线知识库</h1>
        <p class="system-subtitle">欢迎登录</p>
      </div>

      <a-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @finish="handleLogin"
      >
        <a-form-item name="username">
          <a-input
              v-model:value="loginForm.username"
              placeholder="请输入用户名"
              size="large"
          >
            <template #prefix>
              <UserOutlined class="site-form-item-icon"/>
            </template>
          </a-input>
        </a-form-item>

        <a-form-item name="password">
          <a-input-password
              v-model:value="loginForm.password"
              placeholder="请输入密码"
              size="large"
          >
            <template #prefix>
              <LockOutlined class="site-form-item-icon"/>
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item name="captchaCode">
          <a-row :gutter="8">
            <a-col :span="14">
              <a-input
                  v-model:value="loginForm.captchaCode"
                  placeholder="请输入验证码"
                  size="large"
              >
                <template #prefix>
                  <SafetyOutlined class="site-form-item-icon"/>
                </template>
              </a-input>
            </a-col>
            <a-col :span="10">
              <div class="captcha-image" @click="refreshCaptcha">
                <img v-if="captchaImage" :src="captchaImage" alt="验证码"/>
                <span v-else class="captcha-loading">加载中...</span>
              </div>
            </a-col>
          </a-row>
        </a-form-item>

        <a-form-item>
          <a-button
              type="primary"
              html-type="submit"
              size="large"
              class="login-button"
              :loading="loading"
          >
            {{ loading ? '登录中...' : '登 录' }}
          </a-button>
        </a-form-item>
      </a-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import {useRouter} from 'vue-router'
import {message} from 'ant-design-vue'
import type {FormInstance} from 'ant-design-vue'
import {UserOutlined, LockOutlined, SafetyOutlined} from '@ant-design/icons-vue'
import {authApi} from '@/api/auth'
import type {LoginForm} from '@/api/model/authTypes'
import {useUserStore} from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const captchaImage = ref('')

// 登录表单
const loginForm = reactive<LoginForm>({
  username: '',
  password: '',
  captchaCode: '',
  uuid: ''
})

// 表单验证规则
const rules = {
  username: [
    {required: true, message: '请输入用户名', trigger: 'blur'},
    {min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur'}
  ],
  password: [
    {required: true, message: '请输入密码', trigger: 'blur'}
  ],
  captchaCode: [
    {required: true, message: '请输入验证码', trigger: 'blur'}
  ]
}

// 获取验证码
const getCaptcha = async () => {
  try {
    const res = await authApi.getCaptcha()
    if (res.code === '200' && res.data) {
      captchaImage.value = res.data.captchaCodeImage
      loginForm.uuid = res.data.uuid
    }
  } catch (error: any) {
    console.error('获取验证码失败:', error)
    message.error(error.message || '获取验证码失败')
  }
}

// 刷新验证码
const refreshCaptcha = () => {
  loginForm.captchaCode = ''
  getCaptcha()
}

// 登录处理
const handleLogin = async () => {
  try {
    loading.value = true
    
    const res = await authApi.login(loginForm)
    
    if (res.code === '200' && res.data) {
      const loginResult = res.data
      
      // 检查登录结果码
      if (loginResult.code === 0) {
        // 登录成功
        message.success('登录成功')
        
        // 保存用户信息（这里假设用户名就是登录的用户名，实际项目中应该从后端获取完整用户信息）
        userStore.setUserInfo({
          id: 0, // 实际项目中应该从后端获取
          username: loginForm.username,
          nickname: loginForm.username
        }, loginResult.token)
        
        // 跳转到首页
        setTimeout(() => {
          router.push('/ai-model')
        }, 500)
      } else if (loginResult.code === -1) {
        // 用户名或密码错误
        message.error(loginResult.reason || '用户名或密码错误')
        refreshCaptcha()
      } else if (loginResult.code === -2) {
        // 验证码错误
        message.error(loginResult.reason || '验证码错误')
        refreshCaptcha()
      } else {
        message.error(loginResult.reason || '登录失败')
        refreshCaptcha()
      }
    }
  } catch (error: any) {
    console.error('登录失败:', error)
    message.error(error.message || '登录失败，请重试')
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

// 页面加载时获取验证码
onMounted(() => {
  getCaptcha()
})
</script>

<style scoped lang="less">
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.system-title {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  margin: 0 0 10px 0;
}

.system-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0;
}

.login-form {
  .site-form-item-icon {
    color: rgba(0, 0, 0, 0.25);
  }
}

.captcha-image {
  height: 40px;
  cursor: pointer;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  .captcha-loading {
    font-size: 12px;
    color: #999;
  }
  
  &:hover {
    border-color: #1890ff;
  }
}

.login-button {
  width: 100%;
}
</style>
