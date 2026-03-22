<template>
  <div class="assistant-page">
    <a-row class="chat-container" :gutter="2">
      <!-- 左侧知识库选择 -->
      <a-col :span="6" class="sidebar" style="height: 100%">
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
            <a-space direction="vertical">
              <div v-for="c in conversationList" :key="c.id" class="kb-item">
                {{ c.title }}
              </div>
            </a-space>
          </div>
          <a-button @click="handleClearAllChat" style="position: absolute;bottom: 10px;left: 20px;right: 20px">
            清空会话
          </a-button>
        </a-card>
      </a-col>
      <!-- 右侧聊天区域 -->
      <a-col :span="18" style="width:100%; height: 100%">
          <router-view/>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import {onMounted, ref, provide} from 'vue'
import {message} from 'ant-design-vue'
import type {Conversation} from "@/api/model/chatTypes";
import {useRouter} from "vue-router";

const router = useRouter()
const conversationList = ref<Conversation[]>([])

const handleNewChat = () => {
  router.push('/assistant')
}

const handleClearAllChat = () => {
  message.success('已清空所有会话')
}

onMounted(() => {
  conversationList.value.push(
      {
        id: '1',
        title: '测试会话',
        last_active: '2023-08-01 12:00:00'
      }
      , {
        id: '2',
        title: '测试会话2',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '3',
        title: '测试会话3',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '4',
        title: '测试会话4',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '5',
        title: '测试会话5',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '6',
        title: '测试会话6',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '7',
        title: '测试会话7',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '8',
        title: '测试会话8',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '7',
        title: '测试会话7',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '8',
        title: '测试会话8',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '7',
        title: '测试会话7',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '8',
        title: '测试会话8',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '7',
        title: '测试会话7',
        last_active: '2023-08-01 12:00:00'
      },
      {
        id: '8',
        title: '测试会话8',
        last_active: '2023-08-01 12:00:00'
      }
  )
})
provide('conversationList', conversationList)
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
</style>
