<template>
  <div class="ai-model-page">
    <!-- 搜索栏 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="模型名称">
          <a-input
              v-model:value="searchForm.name"
              placeholder="请输入模型名称"
              style="width: 200px"
              allow-clear
          />
        </a-form-item>
        <a-form-item label="模型类型">
          <a-select
              :options="aiModelTypeObjs"
              v-model:value="searchForm.type"
              placeholder="请选择模型类型"
              style="width: 150px"
              allow-clear
          >
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon>
                <SearchOutlined/>
              </template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon>
                <RedoOutlined/>
              </template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作栏 -->
    <a-card class="toolbar-card" :bordered="false">
      <a-button type="primary" @click="handleAdd">
        <template #icon>
          <PlusOutlined/>
        </template>
        新增 AI 模型
      </a-button>
    </a-card>

    <!-- 模型列表 -->
    <a-card :bordered="false">
      <a-row :gutter="[16, 16]">
        <a-col
            v-for="model in modelList"
            :key="model.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
        >
          <a-card hoverable class="model-card">
            <template #actions>
              <a-popconfirm
                  title="确定删除该模型吗？"
                  ok-text="确定"
                  cancel-text="取消"
                  @confirm="handleDelete(model.id)"
              >
                <DeleteOutlined key="delete" style="color: #ff4d4f"/>
              </a-popconfirm>
            </template>
            <template #cover>
              <div class="model-cover">
                <ApiOutlined style="font-size: 48px; color: #1890ff"/>
              </div>
            </template>
            <a-card-meta>
              <template #title>
                <a-typography-title :level="5" style="margin: 0">{{ model.name }}</a-typography-title>
              </template>
              <template #description>
                <a-tag color="blue">{{ model.type }}</a-tag>
                <div class="provider-info">供应商：{{ model.providerName || '未知' }}</div>
                <div class="status-info">
                  状态：
                  <a-badge :status="model.status === 'active' ? 'success' : 'default'"
                           :text="model.status === 'active' ? '已启用' : '已禁用'"/>
                </div>
              </template>
            </a-card-meta>
            <div class="card-footer">
              <a-button type="link" @click="handleEdit(model)">编辑</a-button>
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
        <a-form-item label="模型名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入模型名称"/>
        </a-form-item>
        <a-form-item label="模型类型" name="type">
          <a-select v-model:value="formData.type" placeholder="请选择模型类型">
            <a-select-option value="chat">对话模型</a-select-option>
            <a-select-option value="embedding">嵌入模型</a-select-option>
            <a-select-option value="image">图像模型</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="AI 供应商" name="providerId">
          <a-select
              v-model:value="formData.providerId"
              placeholder="请选择 AI 供应商"
              @focus="loadProviders"
          >
            <a-select-option
                v-for="provider in providers"
                :key="provider.id"
                :value="provider.id"
            >
              {{ provider.providerName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio value="active">启用</a-radio>
            <a-radio value="inactive">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="配置信息" name="config">
          <a-textarea
              v-model:value="formData.configStr"
              placeholder="请输入 JSON 格式的配置信息"
              :rows="6"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import {message} from 'ant-design-vue'
import {
  SearchOutlined,
  RedoOutlined,
  PlusOutlined,
  DeleteOutlined,
  ApiOutlined
} from '@ant-design/icons-vue'
import {modelApi} from '@/api/model'
import type {AiModel, AiModelProvider} from '@/api/model/types'

const aiModelTypeObjs = ref([])
// 搜索表单
const searchForm = ref({
  name: '',
  type: ''
})

// 模型列表
const modelList = ref<AiModel[]>([])

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
  providerId: undefined,
  status: 'active',
  configStr: ''
})

const formRules = {
  name: [{required: true, message: '请输入模型名称', trigger: 'blur'}],
  type: [{required: true, message: '请选择模型类型', trigger: 'change'}],
  providerId: [{required: true, message: '请选择 AI 供应商', trigger: 'change'}]
}

// 供应商列表
const providers = ref<AiModelProvider[]>([])

const loadAiModelTypes = async () => {
  try {
    const res = await modelApi.getAiModelTypes()
    console.log(res)
    if (res.code === '200') {
      aiModelTypeObjs.value = res.data || []
    }
  } catch (error) {
    console.error('加载模型类型列表失败:', error)
  }
}

// 加载数据
const loadModels = async () => {
  try {
    const res = await modelApi.getList({
      name: searchForm.value.name,
      type: searchForm.value.type
    })
    if (res.code === '200') {
      modelList.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

// 加载供应商
const loadProviders = async () => {
  if (providers.value.length > 0) return

  try {
    const res = await modelApi.getProviders(formData.value.type)
    console.log(res)
    if (res.code === '200') {
      providers.value = res.data || []
    }
  } catch (error) {
    console.error('加载供应商列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.current = 1
  loadModels()
}

// 重置
const handleReset = () => {
  searchForm.value = {
    name: '',
    type: ''
  }
  pagination.current = 1
  loadModels()
}

// 新增
const handleAdd = () => {
  modalTitle.value = '新增 AI 模型'
  formData.value = {
    name: '',
    type: '',
    providerId: undefined,
    status: 'active',
    configStr: ''
  }
  providers.value = []
  modalVisible.value = true
}

// 编辑
const handleEdit = (model: AiModel) => {
  modalTitle.value = '编辑 AI 模型'
  formData.value = {
    ...model,
    configStr: model.config ? JSON.stringify(model.config, null, 2) : ''
  }
  modalVisible.value = true
}

// 删除
const handleDelete = async (id: number) => {
  try {
    const res = await modelApi.delete(id)
    if (res.code === 200) {
      message.success('删除成功')
      loadModels()
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

    const submitData = {
      ...formData.value,
      config: formData.value.configStr ? JSON.parse(formData.value.configStr) : {}
    }

    if (formData.value.id) {
      await modelApi.update(submitData)
      message.success('更新成功')
    } else {
      await modelApi.add(submitData)
      message.success('添加成功')
    }

    modalVisible.value = false
    loadModels()
  } catch (error: any) {
    if (error.message?.includes('JSON')) {
      message.error('配置信息必须是有效的 JSON 格式')
    }
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

// 分页变化
const handlePageChange = (page: number) => {
  pagination.current = page
  loadModels()
}

const handlePageSizeChange = (current: number, size: number) => {
  pagination.current = current
  pagination.pageSize = size
  loadModels()
}

onMounted(() => {
  loadAiModelTypes()
  loadModels()
})
</script>

<style scoped>
.ai-model-page {
  padding: 0;
}

.search-card,
.toolbar-card {
  margin-bottom: 16px;
}

.model-card {
  height: 100%;
}

.model-cover {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.provider-info,
.status-info {
  margin-top: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
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
