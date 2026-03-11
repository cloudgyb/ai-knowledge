<template>
  <div class="knowledge-base-page">
    <!-- 搜索栏 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="知识库名称">
          <a-input
            v-model:value="searchForm.name"
            placeholder="请输入知识库名称"
            style="width: 200px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="知识库类型">
          <a-select
            v-model:value="searchForm.type"
            placeholder="请选择知识库类型"
            style="width: 150px"
            allow-clear
          >
            <a-select-option value="text">文本知识库</a-select-option>
            <a-select-option value="file">文件知识库</a-select-option>
            <a-select-option value="url">URL 知识库</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><RedoOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作栏 -->
    <a-card class="toolbar-card" :bordered="false">
      <a-button type="primary" @click="handleAdd">
        <template #icon><PlusOutlined /></template>
        创建知识库
      </a-button>
    </a-card>

    <!-- 知识库列表 -->
    <a-card :bordered="false">
      <a-row :gutter="[16, 16]">
        <a-col
          v-for="kb in knowledgeBaseList"
          :key="kb.id"
          :xs="24"
          :sm="12"
          :md="8"
          :lg="6"
        >
          <a-card hoverable class="kb-card">
            <template #actions>
              <a-popconfirm
                title="确定删除该知识库吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDelete(kb.id)"
              >
                <DeleteOutlined key="delete" style="color: #ff4d4f" />
              </a-popconfirm>
            </template>
            <template #cover>
              <div class="kb-cover">
                <FolderOutlined style="font-size: 48px; color: #fff" />
              </div>
            </template>
            <a-card-meta>
              <template #title>
                <a-typography-title :level="5" style="margin: 0">{{ kb.name }}</a-typography-title>
              </template>
              <template #description>
                <a-tag color="green">{{ kb.type }}</a-tag>
                <div class="description-info" v-if="kb.description">
                  {{ kb.description }}
                </div>
                <div class="time-info">
                  更新时间：{{ formatTime(kb.updateTime) }}
                </div>
              </template>
            </a-card-meta>
            <div class="card-footer">
              <a-button type="link" @click="handleEdit(kb)">编辑</a-button>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <!-- 分页 -->
      <div class="pagination-container">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          show-size-changer
          show-quick-jumper
          @change="handlePageChange"
          @show-size-change="handlePageSizeChange"
        />
      </div>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="modalVisible"
      :title="modalTitle"
      width="800px"
      @ok="handleSubmit"
      :confirm-loading="submitLoading"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        layout="vertical"
      >
        <a-form-item label="知识库名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入知识库名称" />
        </a-form-item>
        <a-form-item label="知识库类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择知识库类型">
            <a-select-option value="text">文本知识库</a-select-option>
            <a-select-option value="file">文件知识库</a-select-option>
            <a-select-option value="url">URL 知识库</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入知识库描述"
            :rows="4"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  RedoOutlined,
  PlusOutlined,
  DeleteOutlined,
  FolderOutlined
} from '@ant-design/icons-vue'
import { knowledgeBaseApi } from '@/api/knowledgeBase'
import type { KnowledgeBase } from '@/api/knowledgeBase'

// 搜索表单
const searchForm = ref({
  name: '',
  type: ''
})

// 知识库列表
const knowledgeBaseList = ref<KnowledgeBase[]>([])

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0
})

// 弹窗
const modalVisible = ref(false)
const modalTitle = ref('')
const submitLoading = ref(false)

// 表单
const formRef = ref()
const formData = ref<any>({
  name: '',
  type: '',
  description: ''
})

const formRules = {
  name: [{ required: true, message: '请输入知识库名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择知识库类型', trigger: 'change' }]
}

// 加载数据
const loadKnowledgeBases = async () => {
  try {
    const res = await knowledgeBaseApi.getList({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      name: searchForm.value.name
    })
    if (res.code === 200) {
      knowledgeBaseList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载知识库列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadKnowledgeBases()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    name: '',
    type: ''
  }
  pagination.current = 1
  loadKnowledgeBases()
}

// 新增
const handleAdd = () => {
  modalTitle.value = '创建知识库'
  formData.value = {
    name: '',
    type: 'text',
    description: ''
  }
  modalVisible.value = true
}

// 编辑
const handleEdit = (kb: KnowledgeBase) => {
  modalTitle.value = '编辑知识库'
  formData.value = { ...kb }
  modalVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  try {
    const res = await knowledgeBaseApi.delete(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadKnowledgeBases()
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}

// 提交
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    
    if (formData.value.id) {
      await knowledgeBaseApi.update(formData.value)
      message.success('更新成功')
    } else {
      await knowledgeBaseApi.add(formData.value)
      message.success('创建成功')
    }
    
    modalVisible.value = false
    loadKnowledgeBases()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.current = page
  loadKnowledgeBases()
}

const handlePageSizeChange = (current: number, size: number) => {
  pagination.current = current
  pagination.pageSize = size
  loadKnowledgeBases()
}

// 格式化时间
const formatTime = (time?: string) => {
  if (!time) return '-'
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss')
}

onMounted(() => {
  loadKnowledgeBases()
})
</script>

<style scoped>
.knowledge-base-page {
  padding: 0;
}

.search-card,
.toolbar-card {
  margin-bottom: 16px;
}

.kb-card {
  height: 100%;
}

.kb-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  background: linear-gradient(135deg, #56ab2f 0%, #a8e063 100%);
  color: #fff;
}

.description-info {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.time-info {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.card-footer {
  margin-top: 16px;
  text-align: center;
}

.pagination-container {
  margin-top: 24px;
  text-align: right;
}
</style>
