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
    <!-- 选择 AI模型提供商弹窗 -->
    <a-modal
        v-model:open="providerSelectVisible"
        title="选择 AI模型提供商"
        width="800px"
        :footer="null"
    >
      <div class="provider-toolbar">
        <a-select
            v-model:value="selectedProviderType"
            :options="aiModelTypeObjs"
            placeholder="选择模型类型"
            style="width: 200px"
            allow-clear
            @change="handleProviderTypeChange"
        >
        </a-select>
      </div>
      <a-row :gutter="[16, 16]">
        <a-col
            v-for="provider in allProviders"
            :key="provider.id"
            :xs="24"
            :sm="12"
            :md="8"
            :lg="6"
        >
          <a-card
              hoverable
              class="provider-card"
              @click="selectProvider(provider)"
          >
            <template #cover>
              <div class="provider-logo">
                <img
                    v-if="provider.logoUrl"
                    :src="provider.logoUrl"
                    :alt="provider.providerName"
                    style="max-width: 80px; max-height: 80px;width: 80px; height: 80px;"
                />
                <ApiOutlined
                    v-else
                    style="font-size: 48px; color: #1890ff"
                />
              </div>
            </template>
            <a-card-meta>
              <template #title>
                <a-typography-title :level="5" style="margin: 0">{{ provider.providerName }}</a-typography-title>
              </template>
              <template #description>
                <div class="provider-desc">{{ provider.providerCompany || '未知' }}</div>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>
      </a-row>
    </a-modal>
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
          <a-input v-model:value="formData.customName" placeholder="请输入模型名称"/>
        </a-form-item>
        <a-form-item label="模型类型" name="type">
          <a-select v-model:value="formData.modelType" :options="currentProviderAiModelTypeObjs"
                    @change="handleCurrentAiModelChange"/>
        </a-form-item>
        <a-form-item label="AI 供应商" name="providerId">
          <a-select v-model:value="formData.providerId" disabled>
            <a-select-option v-for="provider in allProviders" :key="provider.id" :value="provider.id">
              <img :src="provider.logoUrl" alt="logo" style="width: 20px; height: 20px; margin-right: 8px;">
              {{ provider.providerName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="AI 模型" name="model">
          <a-select v-model:value="formData.modelId" @change="handleCurrentSysAiModelChange">
            <a-select-option v-for="aiModel in currentProviderAiModelsTyped" :key="aiModel.id" :value="aiModel.id">
              {{ aiModel.modelName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模型接口地址" name="api">
          <a-input v-model:value="formData.modelUrl" placeholder="请输入模型接口地址"/>
        </a-form-item>
        <a-form-item label="模型接口Key" name="key">
          <a-input v-model:value="formData.modelApiKey" placeholder="请输入模型接口key"/>
        </a-form-item>
        <a-form-item label="是否启用" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted, computed} from 'vue'
import {message} from 'ant-design-vue'
import {
  SearchOutlined,
  RedoOutlined,
  PlusOutlined,
  DeleteOutlined,
  ApiOutlined
} from '@ant-design/icons-vue'
import {modelApi} from '@/api/model'
import type {AiModel, AiModelProvider, SysAiModel} from '@/api/model/types'

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

const providerSelectVisible = ref(false)
const allProviders = ref<AiModelProvider[]>([])
const selectedProviderType = ref<string | undefined>(undefined)

// 弹窗
const modalVisible = ref(false)
const modalTitle = ref('')
const submitLoading = ref(false)

// 表单
const formRef = ref()
const formData = ref<AiModel>({
  customName: '',
  modelType: undefined,
  providerId: undefined,
  modelName: undefined,
  modelUrl: '',
  modelApiKey: '',
  modelApiSecret: '',
  modelId: undefined,
  status: 0
})

const formRules = {
  name: [{required: true, message: '请输入模型名称', trigger: 'blur'}],
  type: [{required: true, message: '请选择模型类型', trigger: 'change'}],
  providerId: [{required: true, message: '请选择 AI 供应商', trigger: 'change'}]
}

// 供应商列表
const providers = ref<AiModelProvider[]>([])

// 加载所有提供商（用于弹窗展示）
const loadAllProviders = async () => {
  try {
    // 根据选中的类型调用后端接口筛选
    const res = await modelApi.getProviders(selectedProviderType.value || '')
    console.log(res)
    if (res.code === '200') {
      allProviders.value = res.data || []
    }
  } catch (error) {
    console.error('加载所有供应商列表失败:', error)
  }
}

// 处理模型类型变化 - 重新加载提供商列表
const handleProviderTypeChange = async () => {
  await loadAllProviders()
}

// 选择提供商
const selectProvider = (provider: AiModelProvider) => {
  formData.value.providerId = provider.id
  providerSelectVisible.value = false

  loadSysAiModels(provider.id)

  formData.value.modelType = undefined
  formData.value.customName = ''
  formData.value.modelUrl = ''
  formData.value.modelApiKey = ''
  formData.value.modelId = undefined

  // 延迟打开新增弹窗
  setTimeout(() => {
    modalVisible.value = true
  }, 100)
}

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

const currentProviderAiModels = ref<SysAiModel[]>([])

const currentProviderAiModelsTyped = ref<SysAiModel[]>([])

const currentProviderAiModelTypeObjs = computed(() => {
  let map = new Map(currentProviderAiModels.value.map(item => [item.modelType, item]))
  return Array.from(map.values()).map(item => {
    return {
      value: item.modelType,
      label: item.modelTypeName,
      key: item.modelType
    }
  })
})

const handleCurrentAiModelChange = async (modelType: string) => {
  currentProviderAiModelsTyped.value = currentProviderAiModels.value.filter(item => item.modelType === modelType)
  const sysAiModel = currentProviderAiModelsTyped.value[0];
  formData.value.modelId = sysAiModel.id
  formData.value.modelUrl = sysAiModel.modelUrl
}

const handleCurrentSysAiModelChange = async (sysAiModelId: number) => {
  const sysAiModel = currentProviderAiModelsTyped.value.find(item => item.id === sysAiModelId)
  formData.value.modelUrl = sysAiModel?.modelUrl
}
const loadSysAiModels = async (providerId: number) => {
  try {
    const res = await modelApi.getSysAiModels(providerId)
    console.log(res)
    if (res.code === '200') {
      currentProviderAiModels.value = res.data || []
    }
  } catch (error) {
    console.error('加载 AI模型列表失败:', error)
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

// 新增 - 先显示提供商选择弹窗
const handleAdd = async () => {
  modalTitle.value = '新增 AI模型'
  providers.value = []
  // 加载所有提供商
  await loadAllProviders()
  // 显示提供商选择弹窗
  providerSelectVisible.value = true
}

// 编辑 - 直接打开编辑弹窗
const handleEdit = (model: AiModel) => {
  modalTitle.value = '编辑 AI 模型'
  formData.value = {
    ...model
  }
  formData.value.modelType = undefined
  formData.value.customName = ''
  formData.value.modelUrl = ''
  formData.value.modelApiKey = ''
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
      ...formData.value
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

.provider-toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

.provider-card {
  cursor: pointer;
  transition: all 0.3s;
}

.provider-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.provider-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100px;
  background: linear-gradient(135deg, #667eea 0%, #ffffff 100%);
  color: #fff;
}

.provider-desc {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.65);
  margin-top: 8px;
}
</style>
