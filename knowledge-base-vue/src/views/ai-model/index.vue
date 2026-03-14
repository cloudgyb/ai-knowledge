<template>
  <div class="ai-model-page">
    <!-- 搜索栏 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="模型名称">
          <a-input v-model:value="searchForm.modelName" placeholder="请输入模型名称" style="width: 200px" allow-clear/>
        </a-form-item>
        <a-form-item label="模型类型">
          <a-select :options="aiModelTypeOptions" v-model:value="searchForm.modelType" placeholder="请选择模型类型"
                    style="width: 150px" allow-clear/>
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
        <a-form-item style="position:absolute; right: 0">
          <a-button type="primary" @click="handleAdd">
            <template #icon>
              <PlusOutlined/>
            </template>
            新增 AI 模型
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>
    <!-- 模型列表 -->
    <a-card :bordered="false">
      <a-row :gutter="[16, 16]">
        <a-col v-for="model in aiModelList" :key="model.id" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card size="small" hoverable class="model-card">
            <template #title>
              <a-typography-title :level="5" style="margin: 0;display:flex;align-items: center">
                <img :src="model.provider?.logoUrl" :alt="model.provider?.providerCompany"
                     :title="model.provider?.providerCompany"
                     style="width: 40px">
                {{ model.customName }}
              </a-typography-title>
            </template>
            <template #extra>
              <div style="display: flex;width:40px;align-items: center;justify-content: space-between">
                <a-popconfirm
                    title="确定删除该模型吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDelete(model.id)"
                >
                  <DeleteOutlined key="delete" style="color: #ff4d4f"/>
                </a-popconfirm>
                <edit-outlined key="edit" @click="handleEdit(model)" style="color: #1677ff"/>
              </div>
            </template>
            <a-card-meta @click="handleEdit(model)">
              <template #description>
                <div class="status-info">
                  类型：
                  <a-tag color="blue">{{ getModelTypeName(model.modelType) }}</a-tag>
                </div>
                <div class="status-info">AI供应商：{{ model.provider?.providerName || '未知' }}</div>
                <div class="status-info">
                  状态：
                  <a-badge :status="model.status === 1 ? 'success' : 'default'">
                    <template #text>
                      <span class="status-info">{{ model.status === 1 ? '已启用' : '已禁用' }}</span>
                    </template>
                  </a-badge>
                </div>
              </template>
            </a-card-meta>
          </a-card>
        </a-col>
      </a-row>

      <!-- 分页 -->
      <div class="pagination-container">
        <a-pagination size="small"
                      v-model:current="pagination.current"
                      v-model:page-size="pagination.pageSize"
                      :total="pagination.total"
                      show-quick-jumper
                      show-size-changer
                      :show-total="pagination.showTotal"
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
            :options="aiModelTypeOptions"
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
          <a-card hoverable class="provider-card" @click="selectProvider(provider)">
            <template #cover>
              <div class="provider-logo">
                <img v-if="provider.logoUrl" :src="provider.logoUrl" :alt="provider.providerName"
                     style="max-width: 80px; max-height: 80px;width: 80px; height: 80px;"/>
                <ApiOutlined v-else style="font-size: 48px; color: #1890ff"/>
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
        <a-form-item label="模型名称" name="customName">
          <a-input v-model:value="formData.customName" placeholder="请输入模型名称"/>
        </a-form-item>
        <a-form-item label="模型类型" name="modelType">
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
        <a-form-item label="AI 模型" name="modelId">
          <a-select v-model:value="formData.modelId" @change="handleCurrentSysAiModelChange">
            <a-select-option v-for="aiModel in currentProviderAiModelsTyped" :key="aiModel.id" :value="aiModel.id">
              {{ aiModel.modelName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模型名称" name="modelName" style="display: none">
          <a-input v-model:value="formData.modelName"/>
        </a-form-item>
        <a-form-item label="模型接口地址" name="modelUrl">
          <a-input v-model:value="formData.modelUrl" placeholder="请输入模型接口地址"/>
        </a-form-item>
        <a-form-item label="模型接口Key" name="modelApiKey">
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
import {ref, reactive, onMounted, computed, type UnwrapRef} from 'vue'
import {message, type PaginationProps, type SelectProps} from 'ant-design-vue'
import {
  SearchOutlined,
  RedoOutlined,
  PlusOutlined,
  DeleteOutlined,
  ApiOutlined,
  EditOutlined
} from '@ant-design/icons-vue'
import {modelApi} from '@/api/model'
import type {AiModel, AiModelProvider, AiModelSearchForm, SysAiModel} from '@/api/model/aiModelTypes'
// 模型类型 Select 下拉选项
const aiModelTypeOptions = ref<SelectProps['options']>([])
// 搜索表单
const searchForm = ref<AiModelSearchForm>({modelName: '', modelType: ''})
// 模型列表
const aiModelList = ref<AiModel[]>([])

// 分页
const pagination = reactive<PaginationProps>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total) => `共 ${total} 个`
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
  providerId: undefined,
  modelId: undefined,
  modelType: undefined,
  modelName: undefined,
  modelUrl: '',
  modelApiKey: '',
  modelApiSecret: '',
  status: 1,
  config: {}
})

const formRules = {
  customName: [{required: true, message: '请输入模型名称', trigger: 'blur'},
    {max: 20, message: '模型名称长度不能超过20个字符', trigger: 'blur'}],
  modelType: [{required: true, message: '请选择模型类型', trigger: 'change'}],
  providerId: [{required: true, message: '请选择 AI 供应商', trigger: 'change'}],
  modelId: [{required: true, message: '请选择模型', trigger: 'change'}],
  modelUrl: [{required: true, message: '请输入模型接口地址', trigger: 'blur'}],
  modelApiKey: [{required: true, message: '请输入模型接口Key', trigger: 'blur'}],
  status: [{required: true, message: '请选择是否启用', trigger: 'change'}]
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
  providerSelectVisible.value = false
  loadSysAiModels(provider.id)

  formData.value.id = undefined
  formData.value.providerId = provider.id
  formData.value.customName = ''
  formData.value.modelId = undefined
  formData.value.modelType = undefined
  formData.value.modelName = undefined
  formData.value.modelUrl = ''
  formData.value.modelApiKey = ''

  // 延迟打开新增弹窗
  setTimeout(() => {
    modalVisible.value = true
  }, 100)
}

const loadAiModelTypeOptions = async () => {
  try {
    const res = await modelApi.getAiModelTypes()
    if (res.code === '200') {
      aiModelTypeOptions.value = res.data || []
    }
  } catch (error) {
    message.error('加载模型类型选项失败')
    console.error('加载模型类型选项失败:', error)
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
  formData.value.modelName = sysAiModel.modelName
}

const handleCurrentSysAiModelChange = async (sysAiModelId: number) => {
  const sysAiModel = currentProviderAiModelsTyped.value.find(item => item.id === sysAiModelId)
  formData.value.modelId = sysAiModel?.id
  formData.value.modelUrl = sysAiModel?.modelUrl
  formData.value.modelName = sysAiModel?.modelName
}
const loadSysAiModels = async (providerId: number) => {
  try {
    const res = await modelApi.getSysAiModels(providerId)
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
      name: searchForm.value.modelName,
      type: searchForm.value.modelType,
      pageNum: pagination.current,
      pageSize: pagination.pageSize
    })
    if (res.code === '200') {
      aiModelList.value = res.data.records || []
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
    modelName: '',
    modelType: ''
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
const handleEdit = async (model: AiModel) => {
  modalTitle.value = '编辑 AI 模型'
  modalVisible.value = true
  await loadAllProviders()
  await loadSysAiModels(model.provider?.id || 0)
  currentProviderAiModelsTyped.value = currentProviderAiModels.value.filter(item => item.modelType === model.modelType)
  formData.value = {
    ...model
  }
  formData.value.modelId = currentProviderAiModels.value.filter(item => item.modelName === model.modelName).at(0)?.id
  //debugger
}

// 删除
const handleDelete = async (id: number | undefined) => {
  try {
    if (!id) {
      return
    }
    const res = await modelApi.delete(id)
    if (res.code === '200') {
      message.success('删除成功')
      await loadModels()
    }
  } catch (error: any) {
    message.error(error.message || '删除失败')
  }
}

// 提交添加
const handleSubmit = async () => {
  try {
    try {
      await formRef.value.validate()
    } catch (error) {
      message.error('请填写必填项')
      return
    }
    submitLoading.value = true

    const submitData = {
      ...formData.value
    }

    if (formData.value.id) {
      //debugger
      await modelApi.update(submitData)
      message.success('更新成功')
    } else {
      await modelApi.add(submitData)
      message.success('添加成功')
    }

    modalVisible.value = false
    await loadModels()
  } catch (error: any) {
    if (formData.value.id) {
      message.error(error.message || '更新失败')
    } else {
      message.error(error.message || '添加失败')
    }
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

const getModelTypeName = (modelType: UnwrapRef<AiModel["modelType"]> | undefined) => {
  const modelTypeObj = aiModelTypeOptions.value?.find(item => item.value === modelType)
  return modelTypeObj?.label || ''
}

onMounted(() => {
  loadAiModelTypeOptions()
  loadModels()
})
</script>

<style scoped>
.ai-model-page {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;
}

.model-card {
  height: 100%;
  border: #ccc 1px solid;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.15);
}

:deep(.ant-card-meta-detail), :deep(.ant-card-meta-title) {
  overflow: visible !important;
  margin: 0;
}

:deep(.ant-card-meta-title) {
  height: 30px;
}

:deep(.ant-card-body) {
  padding-bottom: 10px;
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
