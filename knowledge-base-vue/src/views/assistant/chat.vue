<template>
  <div class="assistant-page">
    <a-row class="chat-container" :gutter="16">
      <!-- 左侧知识库选择 -->
      <a-col :span="6" class="sidebar">
        <a-card title="选择知识库" :bordered="false" class="knowledge-card">
          <a-checkbox-group v-model:value="selectedKnowledgeBases" style="width: 100%">
            <a-space direction="vertical" style="width: 100%">
              <div
                  v-for="kb in knowledgeBaseList"
                  :key="kb.id"
                  class="kb-item"
              >
                <a-checkbox :value="kb.id">
                  {{ kb.name }}
                </a-checkbox>
              </div>
            </a-space>
          </a-checkbox-group>

          <a-divider/>

          <a-button type="primary" block @click="handleSelectAll">
            全选
          </a-button>
          <a-button block style="margin-top: 8px" @click="handleClearAll">
            清空
          </a-button>
        </a-card>
      </a-col>

      <!-- 右侧聊天区域 -->
      <a-col :span="18" class="chat-area">
        <a-card :bordered="false" class="chat-card">
          <!-- 聊天记录 -->
          <div ref="chatMessagesRef" class="chat-messages">
            <div
                v-for="(message, index) in chatMessages"
                :key="index"
                :class="['message', message.role]"
            >
              <div class="message-avatar">
                <a-avatar v-if="message.role === 'user'" style="background-color: #1890ff">
                  <UserOutlined/>
                </a-avatar>
                <a-avatar v-else style="background-color: #52c41a">
                  <RobotOutlined/>
                </a-avatar>
                <div v-if="message.isLoading" class="message assistant streaming">
                  <div class="message-content">
                    <div class="message-bubble">
                      <a-spin :indicator="loadingIndicator"/>
                      <span style="margin-left: 8px">思考中...</span>
                    </div>
                  </div>
                </div>
              </div>
              <div class="message-content">
                <div class="message-bubble">
                  <AgentMarkdown :content="message.content"/>
                </div>
              </div>
            </div>
          </div>

          <!-- 输入区域 -->
          <div class="chat-input">
            <a-textarea
                v-model:value="inputMessage"
                placeholder="请输入您的问题..."
                :auto-size="{ minRows: 3, maxRows: 6 }"
                @pressEnter="handleSendMessage"
                :disabled="isStreaming"
            />
            <div class="input-actions">
              <a-button
                  type="primary"
                  :loading="isStreaming"
                  @click="handleSendMessage"
                  style="width: 120px"
              >
                <template #icon>
                  <SendOutlined/>
                </template>
                发送
              </a-button>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, nextTick, watch, inject, type Ref} from 'vue'
import {message} from 'ant-design-vue'
import {UserOutlined, RobotOutlined, SendOutlined, LoadingOutlined} from '@ant-design/icons-vue'
import {knowledgeBaseApi} from '@/api/knowledgeBase'
import type {KnowledgeBase} from '@/api/knowledgeBase'
import {h} from 'vue'
import {AgentMarkdown} from 'agent-markdown-vue';
import 'x-markdown-vue/style'
import {useRoute} from "vue-router";

const route = useRoute()
const selectedKeys = inject('selectedKeys') as Ref<string[]>
// 监听路由切换
watch(() => route.params.cid, (cid, preCid) => {
  console.log("当前对话id：" + cid)
  console.log("上一个对话id：" + preCid)
})
// 加载图标
const loadingIndicator = () => h(LoadingOutlined, {spin: true})

// 知识库列表
const knowledgeBaseList = ref<KnowledgeBase[]>([])
const selectedKnowledgeBases = ref<number[]>([])

// 聊天消息
interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  isLoading?: boolean
}

const chatMessages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)

// 聊天记录滚动容器
const chatMessagesRef = ref<HTMLElement>()

// 加载知识库列表
const loadKnowledgeBases = async () => {
  try {
    const res = await knowledgeBaseApi.getList({
      pageNum: 1,
      pageSize: 100
    })
    if (res.code === '200') {
      knowledgeBaseList.value = res.data.records || []
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  }
}

// 全选
const handleSelectAll = () => {
  selectedKnowledgeBases.value = knowledgeBaseList.value.map(kb => kb.id)
}

// 清空
const handleClearAll = () => {
  selectedKnowledgeBases.value = []
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

// 发送消息
const handleSendMessage = async () => {
  if (!inputMessage.value.trim()) {
    message.warning('请输入消息内容')
    return
  }

  if (isStreaming.value) {
    message.warning('请等待当前回复完成')
    return
  }

  // 添加用户消息
  chatMessages.value.push({
    role: 'user',
    content: inputMessage.value.trim()
  })

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''
  await scrollToBottom()

  // 添加 AI 助手占位消息
  const assistantMessageIndex = chatMessages.value.length
  chatMessages.value.push({
    role: 'assistant',
    content: '',
    isLoading: true
  })
  await scrollToBottom()

  // 开始流式请求
  isStreaming.value = true

  try {
    // 使用 EventSource 进行 SSE 流式请求
    const params = new URLSearchParams({
      t: new Date().getTime().toString(),
      text: userMessage,
      kbId: selectedKnowledgeBases.value.join(',')
    })

    const eventSource = new EventSource(`/api/ai/chat/connect?${params.toString()}`)

    let accumulatedContent = ''

    eventSource.onmessage = (event) => {
      const data = event.data

      // 检查是否是结束标记
      if (data === '[DONE]') {
        eventSource.close()
        isStreaming.value = false

        // 移除加载状态
        if (chatMessages.value[assistantMessageIndex]) {
          chatMessages.value[assistantMessageIndex].isLoading = false
        }
        return
      }

      console.log(data)

      // 累加内容
      accumulatedContent += data
      chatMessages.value[assistantMessageIndex].content += data

      scrollToBottom()
    }

    eventSource.onerror = (e: Event) => {
      console.log(e)
      eventSource.close()
      isStreaming.value = false

      // 如果内容为空，显示错误提示
      if (!accumulatedContent) {
        chatMessages.value[assistantMessageIndex].content += '抱歉，获取回复失败，请稍后再试。\n'
      }
      if (chatMessages.value[assistantMessageIndex]) {
        chatMessages.value[assistantMessageIndex].isLoading = false
      }
      scrollToBottom()
    }
  } catch (error) {
    console.error('发送消息失败:', error)
    message.error('发送消息失败，请稍后再试')
    isStreaming.value = false
    chatMessages.value[assistantMessageIndex].isLoading = false
    // 移除占位消息
    chatMessages.value.pop()
  }
}

onMounted(() => {
  console.log("chat onMounted")
  let path = route.path;
  console.log("当前路径：" + path)
  if (path.startsWith('/assistant/chat/')) {
    selectedKeys.value = [route.params.cid + '']
  }
  loadKnowledgeBases()
})
</script>

<style scoped>
.assistant-page {
  padding: 0;
  height: calc(100vh - 128px);
}

.chat-container {
  height: 100%;
}

.sidebar {
  height: 100%;
  overflow-y: auto;
}

.knowledge-card {
  height: 100%;
}

.kb-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.kb-item:last-child {
  border-bottom: none;
}

.chat-area {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 4px;
  margin-bottom: 16px;
}

.message {
  display: flex;
  margin-bottom: 16px;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-avatar {
  flex-shrink: 0;
  margin: 0 8px;
}

.message.user .message-avatar {
  order: 2;
}

.message.assistant .message-avatar {
  order: 1;
}

.message-content {
  max-width: 70%;
  order: 1;
}

.message.user .message-content {
  order: 1;
}

.message.assistant .message-content {
  order: 2;
}

.message-bubble {
  padding: 12px 16px;
  border-radius: 8px;
  word-wrap: break-word;
  white-space: pre-wrap;
}

.message.user .message-bubble {
  background: #1890ff;
  color: #fff;
}

.message.assistant .message-bubble {
  background: #fff;
  color: rgba(0, 0, 0, 0.85);
}

.chat-input {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 8px;
}

/* 滚动条样式 */
.chat-messages::-webkit-scrollbar {
  width: 6px;
}

.chat-messages::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}
</style>
