<template>
  <div class="assistant-page">
    <a-row class="chat-container" :gutter="2">
      <!-- 左侧对话列表 -->
      <a-col flex="260px" style="height: 100%">
        <a-card style="height: 100%">
          <template #title>
            <a-flex vertical align="center" justify="center" gap="small">
              <div>AI 聊天小助理</div>
              <a-button block type="primary" ghost @click="handleNewChat">
                开启新对话
              </a-button>
            </a-flex>
          </template>
          <div class="conversation-box" ref="conversationBoxRef" @scroll="handleScroll">
            <a-menu v-model:selectedKeys="selectedKeys" style="width: 256px" mode="vertical" @click="handleClick">
              <a-menu-item v-for="c in conversationList" :key="c.id" :title="c.title+'-'+c.lastActiveTime">
                {{ c.title }}
                <a-dropdown @click.stop>
                  <a-button type="primary" ghost style="position: absolute;right: 5px;top: 5px">
                    <template #icon>
                      <ellipsis-outlined/>
                    </template>
                  </a-button>
                  <template #overlay>
                    <a-menu>
                      <a-menu-item key="0">
                        <a-button type="text" size="small" @click="handleRenameConversation(c)">
                          <template #icon>
                            <edit-outlined/>
                          </template>
                          重命名
                        </a-button>
                      </a-menu-item>
                      <a-menu-item key="2">
                        <a-button type="text" size="small">
                          <template #icon>
                            <pushpin-outlined/>
                          </template>
                          置顶
                        </a-button>
                      </a-menu-item>
                      <a-menu-item key="1">
                        <a-button danger type="text" size="small" @click="handleDeleteConversation(c.id)">
                          <template #icon>
                            <delete-outlined/>
                          </template>
                          删除对话
                        </a-button>
                      </a-menu-item>
                    </a-menu>
                  </template>
                </a-dropdown>
              </a-menu-item>
            </a-menu>
          </div>
          <a-button @click="handleClearAllChat" style="position: absolute;bottom: 10px;left: 20px;right: 20px">
            清空会话
          </a-button>
        </a-card>
      </a-col>
      <!-- 右侧聊天区域 -->
      <a-col flex="auto" style="height: 100%">
        <ai-model-select @change="handleAiModelSelect" style="position: absolute;top: 10px;z-index: 999"/>
        <router-view/>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import {onMounted, provide, ref} from 'vue'
import {EllipsisOutlined, DeleteOutlined, EditOutlined, PushpinOutlined} from '@ant-design/icons-vue'
import {message, type MenuProps} from 'ant-design-vue'
import type {Conversation} from "@/api/model/chatTypes";
import {useRouter, useRoute} from "vue-router";
import {chatApi} from "@/api/chat";
import AiModelSelect from "@/components/ai/AiModelSelect.vue";
import {useInputMsgStore} from "@/stores/userInputMsg";

const inputMsgStore = useInputMsgStore()

const router = useRouter()
const route = useRoute()

const conversationList = ref<Conversation[]>([])
const conversationBoxRef = ref<HTMLElement>()

// 分页相关状态
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const loading = ref(false)
const hasMore = ref(true)

const handleNewChat = () => {
  router.push('/assistant')
  selectedKeys.value = []
}
const handleClick: MenuProps['onClick'] = menuInfo => {
  console.log('click ', menuInfo);
  router.push(`/assistant/chat/${menuInfo.key}`)
};

const handleClearAllChat = () => {
  message.success('已清空所有会话')
}

const selectedKeys = ref<any[]>([]);

/**
 * 对话侧边栏列表-选中当前对话
 */
function selectConversation() {
  let path = route.path;
  if (path.startsWith('/assistant/chat/')) {
    const cid = route.params.cid + ''
    selectedKeys.value = [cid]
  }
}

const loadConversations = async (isLoadMore = false) => {
  // 如果没有更多数据或正在加载中，则不继续加载
  if (loading.value) return
  
  try {
    loading.value = true
    const currentPage = isLoadMore ? pageNum.value : 1
    const res = await chatApi.getConversations(currentPage, pageSize.value)
    if (res.code === '200') {
      const newRecords = res.data.records || []
      
      if (isLoadMore) {
        // 加载更多时追加数据
        conversationList.value = [...conversationList.value, ...newRecords]
      } else {
        // 首次加载或刷新时替换数据
        conversationList.value = newRecords
      }
      
      // 更新总数和页码
      total.value = res.data.total || 0
      
      // 判断是否还有更多数据
      hasMore.value = conversationList.value.length < total.value
      
      // 如果成功加载，页码+1
      if (newRecords.length > 0) {
        pageNum.value = currentPage + 1
      }
      
      selectConversation()
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  } finally {
    loading.value = false
  }
}
const handleDeleteConversation = async (id: string) => {
  try {
    const res = await chatApi.deleteConversation(id)
    if (res.code === '200') {
      message.success('删除成功')
      // 重置分页状态
      pageNum.value = 1
      hasMore.value = true
      await loadConversations()
      const path = route.path;
      if (path.startsWith('/assistant/chat/')) {
        const cid = route.params.cid + ''
        if (cid === id) {
          if (conversationList.value.length > 0) {
            selectedKeys.value = [conversationList.value[0].id]
            await router.push('/assistant/chat/' + conversationList.value[0].id)
          } else {
            selectedKeys.value = []
            await router.push('/assistant')
          }
        }
      }
    }
  } catch (error) {
    message.error('删除失败')
  }
}
const handleRenameConversation = async (conversation: Conversation) => {
  const newTitle = prompt('请输入新的标题', conversation.title)
  if (newTitle) {
    const res = await chatApi.updateConversation({id: conversation.id, title: newTitle})
    if (res.code === '200') {
      message.success('修改成功')
      conversation.title = newTitle
    }
  }
}
const handleAiModelSelect = (value: number) => {
  console.log('handleAiModelSelect', value)
  inputMsgStore.setAiModelId(value)
}
// 处理滚动事件
const handleScroll = async (e: Event) => {
  const target = e.target as HTMLElement
  const scrollTop = target.scrollTop
  const scrollHeight = target.scrollHeight
  const clientHeight = target.clientHeight
  
  // 当滚动到距离底部 50px 时触发加载
  if (scrollHeight - scrollTop - clientHeight < 50) {
    await loadConversations(true)
  }
}

const selectKeys = (key: string) => {
  debugger
  selectedKeys.value = [key]
}
onMounted(() => {
  loadConversations()
  let path = route.path;
  if (path.startsWith('/assistant/chat/')) {
    selectedKeys.value = [route.params.cid]
  }
})
provide('conversationList', conversationList)
provide('loadConversations', loadConversations)
provide('selectedKeys', selectedKeys)
provide('selectKeys', selectKeys)
</script>

<style scoped>

.assistant-page {
  padding: 0;
  height: calc(100vh - 128px);
}

.chat-container {
  height: 100%;
}

.conversation-box {
  max-height: calc(100vh - 300px);
  overflow-y: auto;
}

.conversation-box::-webkit-scrollbar {
  width: 6px; /* 垂直滚动条宽度 */
  height: 10px; /* 水平滚动条高度 */
  background-color: transparent; /* 关键：避免轨道溢出背景 */
}

.conversation-box:hover::-webkit-scrollbar {
  background-color: #f5f5f5;
}

.conversation-box::-webkit-scrollbar-thumb {
  border-radius: 8px;
  background-color: transparent;
}

.conversation-box:hover::-webkit-scrollbar-thumb {
  background-color: #ccc;
}

:deep(.ant-menu-vertical .ant-menu-item) {
  padding-inline: 30px;
}

:deep(.ant-menu-item .ant-menu-submenu-title ) {
  text-overflow: inherit;
}

:deep(.ant-menu-title-content>button) {
  border-color: transparent;
  border-radius: 50%;
}

:deep(.ant-dropdown-button) {
  display: inline-flex;
  align-items: center;
  justify-content: flex-end;
  width: 100%;
}

:deep(.ant-dropdown-button > button) {
  display: none;
}

:deep(.ant-dropdown-button > button.ant-btn-icon-only) {
  display: inline-block;
  border-radius: 50%;
  border: none;
  box-shadow: none;
}
</style>
