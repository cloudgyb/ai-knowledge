<template>
  <div class="assistant-page">
    <!-- 右侧聊天区域 -->
    <a-card :bordered="false" class="chat-card">
      <!-- 聊天记录 -->
      <div ref="chatMessagesRef" class="chat-messages">
        <div v-for="(message, index) in chatMessages" :key="index" :class="['message', message.role]">
          <div class="message-content">
            <div class="message-bubble">
              <article class="vue-markdown-wrapper">
                <div v-if="message.role === 'user'" style="padding: 5px 5px">
                  <pre style="margin: 0;font-weight: bold">{{ message.content }}</pre>
                </div>
                <Markdown
                    v-else-if="!message.isLoading && message.content"
                    :content="message.content"/>
                <StreamMarkdown v-else-if="message.isLoading"
                                :content="message.content"
                                :streaming="message.isLoading"/>
              </article>
              <!-- AI 回复操作栏：复制 / 导出 Markdown 内容 -->
              <div v-if="message.role === 'assistant' && !message.isLoading && message.content"
                   class="message-actions">
                <a-button
                    size="small"
                    type="text"
                    class="action-btn"
                    :title="copiedIndex === index ? '已复制' : '复制 Markdown 内容'"
                    @click="handleCopyMarkdown(message, index)">
                  <CheckOutlined v-if="copiedIndex === index" class="copied-icon"/>
                  <CopyOutlined v-else/>
                  <span>{{ copiedIndex === index ? '已复制' : '复制' }}</span>
                </a-button>
                <a-button
                    size="small"
                    type="text"
                    class="action-btn"
                    title="导出 Markdown 文件"
                    @click="handleExportMarkdown(message)">
                  <DownloadOutlined/>
                  <span>导出</span>
                </a-button>
              </div>
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
          <a-select placeholder="选择知识库"
                    v-model:value="selectedKB"
                    show-search
                    :filter-option="kbSelectOptionFilter"
                    :options="kbSelectOptions"
                    style="width: 100px">
          </a-select>
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
  </div>
</template>

<script setup lang="ts">
import {ref, onMounted, nextTick, watch, inject, type Ref} from 'vue'
import {message, type SelectProps} from 'ant-design-vue'
import {SendOutlined, CopyOutlined, CheckOutlined, DownloadOutlined} from '@ant-design/icons-vue'
import {knowledgeBaseApi} from '@/api/knowledgeBase'
import {chatApi} from '@/api/chat'
import type {KnowledgeBase} from '@/api/knowledgeBase'
import {useRoute} from "vue-router";
import {useInputMsgStore} from "@/stores/userInputMsg";
import Markdown from '@/components/Markdown/index.vue'
import StreamMarkdown from '@/components/Markdown/StreamMarkdown.vue'
import {createEventSource} from '@/utils/request'
import {copyToClipboard} from '@/utils/clipboard'
import 'highlight.js/styles/github.min.css'
import '@/assets/styles/markdown.css'

const inputMsgStore = useInputMsgStore()

const loadConversations = inject('loadConversations') as () => void

const route = useRoute()
const selectedKeys = inject('selectedKeys') as Ref<string[]>
// 监听路由切换
watch(() => route.params.cid, (cid, preCid) => {
  console.log("当前对话id：" + cid)
  console.log("上一个对话id：" + preCid)
  init()
})
// 知识库列表
const knowledgeBaseList = ref<KnowledgeBase[]>([])
// 选择的知识库列表
const kbSelectOptions = ref<SelectProps["options"]>([])
const selectedKB = ref<number>()
// 加载知识库列表
const loadKnowledgeBases = async () => {
  try {
    const res = await knowledgeBaseApi.getList({
      pageNum: 1,
      pageSize: 100
    })
    if (res.code === '200') {
      knowledgeBaseList.value = res.data.records || []
      kbSelectOptions.value = knowledgeBaseList.value.map(kb => ({
        value: kb.id,
        label: kb.name
      }))
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  }
}
const kbSelectOptionFilter = (inputValue: string, option: any) => {
  return option.label.toLowerCase().includes(inputValue.toLowerCase())
}


// 聊天消息
interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  isLoading?: boolean
}

const currentCid = ref('')
const chatMessages = ref<ChatMessage[]>([])
const inputMessage = ref('')
const isStreaming = ref(false)
// 记录刚复制过的消息索引，用于按钮反馈
const copiedIndex = ref(-1)

// 复制 AI 回复的 Markdown 内容
const handleCopyMarkdown = async (msg: ChatMessage, index: number) => {
  try {
    await copyToClipboard(msg.content)
    copiedIndex.value = index
    message.success('Markdown 内容已复制')
    setTimeout(() => {
      if (copiedIndex.value === index) {
        copiedIndex.value = -1
      }
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
    message.error('复制失败，请重试')
  }
}

// 导出 AI 回复的 Markdown 内容为 .md 文件
const handleExportMarkdown = (msg: ChatMessage) => {
  try {
    if (!msg.content) {
      message.warning('暂无内容可导出')
      return
    }
    // 生成带时间戳的文件名
    const now = new Date()
    const pad = (n: number) => n.toString().padStart(2, '0')
    const timestamp = `${now.getFullYear()}${pad(now.getMonth() + 1)}${pad(now.getDate())}_${pad(now.getHours())}${pad(now.getMinutes())}${pad(now.getSeconds())}`
    const filename = `AI回复_${timestamp}.md`

    const blob = new Blob([msg.content], {type: 'text/markdown;charset=utf-8'})
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = filename
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    message.success('Markdown 文件已导出')
  } catch (err) {
    console.error('导出失败:', err)
    message.error('导出失败，请重试')
  }
}

// 聊天记录滚动容器
const chatMessagesRef = ref<HTMLElement>()

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
      cid: currentCid.value,
      text: userMessage,
      kbId: selectedKB.value ? selectedKB.value.toString() : '',
      mid: inputMsgStore.aiModelId + ""
    })

    const eventSource = createEventSource(`/api/ai/chat/connect?${params.toString()}`)

    let accumulatedContent = ''
    eventSource.addEventListener('content', (event) => {
          let data = event.data
          // 累加内容
          accumulatedContent += data
          chatMessages.value[assistantMessageIndex].content += data
          scrollToBottom()
        }
    )

    eventSource.addEventListener('close', (e) => {
      console.log("close", e)
      eventSource.close()
      isStreaming.value = false
      // 移除加载状态
      if (chatMessages.value[assistantMessageIndex]) {
        chatMessages.value[assistantMessageIndex].isLoading = false
      }
    })

    eventSource.addEventListener('title', (e) => {
      console.log("title", e)
      loadConversations()
    })

    eventSource.addEventListener('lastActiveTime', (e) => {
      console.log("lastActiveTime", e)
      loadConversations()
    })

    eventSource.onerror = (e: any) => {
      console.log(e)
      eventSource.close()
      isStreaming.value = false

      // 如果内容为空，显示错误提示
      if (!accumulatedContent) {
        let msg = '抱歉，获取回复失败，请稍后再试。\n'
        if (e.data) {
          msg = e.data
          message.error(msg)
        }
        chatMessages.value[assistantMessageIndex].content += msg
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
/**
 * 加载对话
 * @param cid 对话 id
 */
const loadConversationMsgs = async (cid: string) => {
  const res = await chatApi.getConversationMessages(cid)
  if (res.code === '200') {
    const data = res.data;
    const msgs: ChatMessage[] = []
    data.forEach(msg => {
      msgs.push({
        role: 'user',
        content: msg.userInputMsg,
        isLoading: false
      })
      msgs.push({
        role: 'assistant',
        content: msg.aiReplyMsg,
        isLoading: false
      })
    })
    chatMessages.value = msgs
    await scrollToBottom()
  }
}
const init = async () => {
  let path = route.path;
  if (path.startsWith('/assistant/chat/')) {
    const cid = route.params.cid + '';
    currentCid.value = cid
    selectedKeys.value = [cid]
    await loadConversationMsgs(cid)
    if (inputMsgStore.kbId) {
      selectedKB.value = inputMsgStore.kbId
    }
    inputMessage.value = inputMsgStore.inputMsg?.trim()
    inputMsgStore.clear()
    if (inputMessage.value && inputMessage.value.trim() !== '') {
      await handleSendMessage()
    }
  }
  await loadKnowledgeBases()
}
onMounted(() => {
  console.log("chat onMounted")
  init()
})
</script>

<style scoped>
.assistant-page {
  margin: 0 auto;
  padding: 0;
  width: 900px;
  height: calc(100vh - 128px);
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
  border-radius: 4px;
  margin-bottom: 16px;
  max-height: calc(100vh - 320px);
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

.message-content {
  max-width: 100%;
  order: 1;
}

.message.user .message-content {
  order: 1;
}

.message.assistant .message-content {
  order: 2;
}

.message-bubble {
  padding: 5px 10px;
  border-radius: 8px;

}

.message.user .message-bubble {
  background: #f5f5f5;
  font-size: 1rem;
  color: rgba(0, 0, 0, 0.85);
}

.message.assistant .message-bubble {
  background: #fff;
  color: rgba(0, 0, 0, 0.85);
}

/* AI 回复操作栏 */
.message-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 2px;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.message.assistant .message-bubble:hover .message-actions {
  opacity: 1;
}

.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #57606a;
  padding: 0 6px;
  height: 24px;
  border-radius: 4px;
}

.action-btn:hover {
  color: #1890ff;
  background-color: rgba(24, 144, 255, 0.08);
}

.copied-icon {
  color: #52c41a;
}

.chat-input {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
  position: fixed;
  width: 750px;
  bottom: 40px;
}

.input-actions {
  display: flex;
  justify-content: space-between;
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
