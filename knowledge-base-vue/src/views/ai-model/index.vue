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
      <div v-if="loading"
           style="text-align: center; position: absolute;z-index: 999;background: rgba(0,0,0,0);top: 60px;left: 50%">
        <a-spin/>
      </div>
      <a-empty v-if="aiModelList.length === 0 && !loading" :image="simpleImage"/>
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
                      :page-size-options="pagination.pageSizeOptions"
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
      <a-tabs v-model:activeKey="activeTab">
        <!-- 基本配置 Tab -->
        <a-tab-pane key="basic" tab="基本配置">
          <a-form
              ref="formRef"
              :model="formData"
              :rules="formRules"
              layout="vertical"
          >
            <a-form-item label="名称" name="customName">
              <a-input v-model:value="formData.customName" placeholder="请输入名称"/>
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
            <a-form-item label="模型名称" name="modelName" v-if="formData.isCustom">
              <a-input v-model:value="formData.modelName"/>
            </a-form-item>
            <a-form-item label="模型接口地址" name="modelUrl">
              <a-input v-model:value="formData.modelUrl" placeholder="请输入模型接口地址"/>
            </a-form-item>
            <a-form-item label="模型接口Key" name="modelApiKey">
              <a-input-password v-model:value="formData.modelApiKey" placeholder="请输入模型接口key"/>
            </a-form-item>
            <a-form-item label="是否启用" name="status">
              <a-radio-group v-model:value="formData.status">
                <a-radio :value="1">启用</a-radio>
                <a-radio :value="0">禁用</a-radio>
              </a-radio-group>
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <!-- 参数配置 Tab - 仅语言模型显示 -->
        <a-tab-pane key="params" tab="参数配置" v-if="isLanguageModel">
          <a-form
              :model="formData.config"
              layout="vertical"
          >
            <a-form-item label="模型温度 (temperature)">
              <a-slider 
                v-model:value="formData.config.temperature" 
                :min="0" 
                :max="1" 
                :step="0.1"
                :marks="{0: '0', 0.5: '0.5', 1: '1'}"
              />
              <div class="param-description">
                控制生成文本的随机性。值越高，输出越随机；值越低，输出越确定。
              </div>
            </a-form-item>
            
            <a-form-item label="词汇多样性 (lexical)">
              <a-slider 
                v-model:value="formData.config.lexical" 
                :min="0" 
                :max="1" 
                :step="0.1"
                :marks="{0: '0', 0.5: '0.5', 1: '1'}"
              />
              <div class="param-description">
                控制词汇的丰富程度。值越高，用词越多样；值越低，用词越简单。
              </div>
            </a-form-item>
            
            <a-form-item label="话题发散度 (talk)">
              <a-slider 
                v-model:value="formData.config.talk" 
                :min="0" 
                :max="1" 
                :step="0.1"
                :marks="{0: '0', 0.5: '0.5', 1: '1'}"
              />
              <div class="param-description">
                控制话题的发散程度。值越高，话题越发散；值越低，话题越集中。
              </div>
            </a-form-item>
            
            <a-form-item label="重复惩罚 (repeat)">
              <a-slider 
                v-model:value="formData.config.repeat" 
                :min="0" 
                :max="1" 
                :step="0.1"
                :marks="{0: '0', 0.5: '0.5', 1: '1'}"
              />
              <div class="param-description">
                控制内容重复的惩罚程度。值越高，越避免重复；值越低，允许更多重复。
              </div>
            </a-form-item>
            
            <a-form-item label="最大Token数 (tokens)">
              <a-input-number 
                v-model:value="formData.config.tokens" 
                :min="1" 
                :max="32768"
                :step="1"
                style="width: 100%"
              />
              <div class="param-description">
                模型单次回复的最大token数量。建议根据实际需求设置，过大会增加响应时间。
              </div>
            </a-form-item>
            
            <a-form-item label="超时时间 (timeout)">
              <a-input-number 
                v-model:value="formData.config.timeout" 
                :min="1" 
                :max="300"
                :step="1"
                style="width: 100%"
                addon-after="秒"
              />
              <div class="param-description">
                等待AI响应的最长时间。超过此时间将自动终止请求。
              </div>
            </a-form-item>
          </a-form>
        </a-tab-pane>
      </a-tabs>
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
import {Empty} from 'ant-design-vue';
import {modelApi} from '@/api/model'
import type {AiModel, AiModelProvider, AiModelSearchForm, SysAiModel} from '@/api/model/aiModelTypes'
import {rsaEncrypt} from '@/utils/rsa'

const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE;
// 模型类型 Select 下拉选项
const aiModelTypeOptions = ref<SelectProps['options']>([])
// 搜索表单
const searchForm = ref<AiModelSearchForm>({modelName: '', modelType: ''})
// 模型列表
const aiModelList = ref<AiModel[]>([])

// 分页
const pagination = reactive<PaginationProps>({
  current: 1,
  pageSize: 12,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  pageSizeOptions: ['12', '24', '36'],
  showTotal: (total) => `共 ${total} 个`
})

const providerSelectVisible = ref(false)
const allProviders = ref<AiModelProvider[]>([])
const selectedProviderType = ref<string | undefined>(undefined)

// 弹窗
const modalVisible = ref(false)
const modalTitle = ref('')
const submitLoading = ref(false)
const activeTab = ref('basic')
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
  isCustom: false,
  status: 1,
  config: {
    temperature: 0.7,
    lexical: 0.5,
    talk: 0.5,
    repeat: 0.5,
    tokens: 10240,
    timeout: 30
  }
})

const formRules = {
  customName: [{required: true, message: '请输入名称', trigger: 'blur'},
    {max: 20, message: '名称长度不能超过20个字符', trigger: 'blur'}],
  modelName: [{required: true, message: '请输入模型名称', trigger: 'blur'},
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
  formData.value.config = {
    temperature: 0.7,
    lexical: 0.5,
    talk: 0.5,
    repeat: 0.5,
    tokens: 10240,
    timeout: 30
  }

  // 延迟打开新增弹窗
  setTimeout(() => {
    modalVisible.value = true
    activeTab.value = 'basic'
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

// 判断是否为语言模型
const isLanguageModel = computed(() => {
  return formData.value.modelType === 'LANG'
})

const handleCurrentAiModelChange = async (modelType: string) => {
  currentProviderAiModelsTyped.value = currentProviderAiModels.value.filter(item => item.modelType === modelType)
  const sysAiModel = currentProviderAiModelsTyped.value[0];
  formData.value.modelId = sysAiModel.id
  formData.value.modelUrl = sysAiModel.modelUrl
  if(sysAiModel?.modelName !== '自定义') {
    formData.value.modelName = sysAiModel?.modelName
    formData.value.isCustom = false
  } else {
    formData.value.modelName = ''
    formData.value.isCustom = true
  }
  
  // 如果切换到非语言模型，自动切换回基本配置 Tab
  if (modelType !== 'LANG' && activeTab.value === 'params') {
    activeTab.value = 'basic'
  }
}

const handleCurrentSysAiModelChange = async (sysAiModelId: number) => {
  const sysAiModel = currentProviderAiModelsTyped.value.find(item => item.id === sysAiModelId)
  formData.value.modelId = sysAiModel?.id
  formData.value.modelUrl = sysAiModel?.modelUrl
  if(sysAiModel?.modelName !== '自定义') {
    formData.value.modelName = sysAiModel?.modelName
    formData.value.isCustom = false
  } else {
    formData.value.modelName = ''
    formData.value.isCustom = true
  }
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
const loading = ref<boolean>(false)
// 加载数据
const loadModels = async () => {
  try {
    loading.value = true
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
  } finally {
    loading.value = false
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
  activeTab.value = 'basic'
  await loadAllProviders()
  await loadSysAiModels(model.provider?.id || 0)
  currentProviderAiModelsTyped.value = currentProviderAiModels.value.filter(item => item.modelType === model.modelType)
  formData.value = {
    ...model,
    config: model.config || {
      temperature: 0.7,
      lexical: 0.5,
      talk: 0.5,
      repeat: 0.5,
      tokens: 10240,
      timeout: 30
    }
  }
  formData.value.modelId = currentProviderAiModels.value.filter(item => item.modelName === model.modelName).at(0)?.id
  formData.value.isCustom = false
  if(!formData.value.modelId) {
    formData.value.modelId = currentProviderAiModels.value.filter(item => item.modelName === '自定义').at(0)?.id
    formData.value.isCustom = true
  }
  
  // 如果不是语言模型，确保在基本配置 Tab
  if (model.modelType !== 'LANG') {
    activeTab.value = 'basic'
  }
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
      debugger
      const aiModel = aiModelList.value.find(item => item.id === formData.value.id);
      if (aiModel?.modelApiKey !== submitData.modelApiKey) { // api key 有修改，加密
        submitData.modelApiKey = rsaEncrypt(submitData.modelApiKey + "")
      }
      if (aiModel?.modelApiSecret !== submitData.modelApiSecret) { // api secret 有修改，加密
        submitData.modelApiSecret = rsaEncrypt(submitData.modelApiSecret + "")
      }
      await modelApi.update(submitData)
      message.success('更新成功')
    } else {
      submitData.modelApiKey = rsaEncrypt(submitData.modelApiKey + "")
      submitData.modelApiSecret = rsaEncrypt(submitData.modelApiSecret + "")
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

.param-description {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  margin-top: 4px;
  line-height: 1.5;
}

:deep(.ant-tabs-content) {
  padding-top: 16px;
}

</style>