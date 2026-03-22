<template>
  <a-card style="width:100%; height: 100%">
    <!-- 输入区域 -->
    <a-flex vertical align="center" justify="center" gap="small" style="height: 80%">
      <a-typography-title :level="2" style="display: flex;justify-content: center;gap: 10px">
        <MessageTwoTone/>
        <span>今天有什么可以帮你的？</span>
      </a-typography-title>
      <a-textarea
          v-model:value="conversationAddForm.text"
          placeholder="请输入您的问题..."
          :auto-size="{ minRows: 3, maxRows: 6 }"
          @pressEnter="handleSendMessage"
          style="width: 700px;border-radius: 20px;"
      />
      <a-flex align="space-between" justify="space-between" style="width: 700px">
        <a-select
            v-model:value="conversationAddForm.kbId"
            show-search
            placeholder="选择知识库"
            style="width: 200px"
            :options="kbSelectOptions"
            :filter-option="kbSelectOptionFilter"
        >
          <template #suffixIcon>
            <FolderTwoTone/>
          </template>
        </a-select>
        <a-button type="primary" :disabled="sendBtnDisabled"
                  :loading="isStreaming"
                  @click="handleSendMessage" style="width: 120px;color: #fff"
                  :style="{background: sendBtnDisabled ? '#6da3ef' : ''}">
          <template #icon>
            <SendOutlined/>
          </template>
          发送
        </a-button>
      </a-flex>
    </a-flex>
  </a-card>
</template>
<script setup lang="ts">

import {onMounted, ref, inject, type Ref, computed} from "vue";
import {FolderTwoTone, MessageTwoTone, SendOutlined} from "@ant-design/icons-vue";
import {type KnowledgeBase, knowledgeBaseApi} from "@/api/knowledgeBase";
import {message, type SelectProps} from "ant-design-vue";
import {useRouter} from "vue-router";
import type {Conversation} from "@/api/model/chatTypes";

const router = useRouter()
// 获取父路由提供的 conversationList
const conversationList = inject('conversationList') as Ref<Conversation[]> || ref([])

const conversationAddForm = ref({
  text: '',
  kbId: undefined
})
// 知识库列表
const knowledgeBaseList = ref<KnowledgeBase[]>([])
// 选择的知识库列表
const kbSelectOptions = ref<SelectProps["options"]>([])


const isStreaming = ref(false)
const sendBtnDisabled = computed(() => {
  return !conversationAddForm.value.text.trim()
})

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

// 发送消息
const handleSendMessage = async () => {
  if (!conversationAddForm.value.text.trim()) {
    message.warning('请输入消息内容')
    return
  }

  if (isStreaming.value) {
    message.warning('请等待!')
    return
  }

  // 开始流式请求
  isStreaming.value = true

  try {
    const params = new URLSearchParams({
      cid: new Date().getTime().toString(),
      text: conversationAddForm.value.text,
      kbId: conversationAddForm.value.kbId || '',
    })
    conversationList.value.push({
      id: params.get('cid') || '',
      title: `测试会话` + params.get('cid'),
      last_active: new Date().toISOString()
    })
    // todo 调用后端接口创建新的对话
    // todo 进入聊天路由页面
    await router.push('/assistant/chat/' + params.get('cid'))
  } catch (error) {
    console.error('发送消息失败:', error)
    message.error('发送消息失败，请稍后再试')
  } finally {
    isStreaming.value = false
  }
}

onMounted(() => {
  loadKnowledgeBases()
})

</script>
<style scoped>
:deep(.ant-card-body) {
  height: 100%;
}
</style>