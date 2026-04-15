<template>
  <div class="assistant-page">
    <a-row class="chat-container" :gutter="2">
      <!-- 左侧知识库选择 -->
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
          <div class="conversation-box">
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

const router = useRouter()
const route = useRoute()

const conversationList = ref<Conversation[]>([])

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
  /*if (conversationList.value.length > 0) {
    selectedKeys.value = [conversationList.value[0].id]
  }*/
  let path = route.path;
  if (path.startsWith('/assistant/chat/')) {
    const cid = route.params.cid + ''
    selectedKeys.value = [cid]
  }
}

const loadConversations = async () => {
  try {
    const res = await chatApi.getConversations()
    if (res.code === '200') {
      conversationList.value = res.data.records
      selectConversation()
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}
const handleDeleteConversation = async (id: string) => {
  try {
    const res = await chatApi.deleteConversation(id)
    if (res.code === '200') {
      message.success('删除成功')
      await loadConversations()
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
