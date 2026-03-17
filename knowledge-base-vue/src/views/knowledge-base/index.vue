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
          <a-select v-model:value="searchForm.type" :options="kbTypeSelectOptions" placeholder="请选择知识库类型"
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
            新增知识库
          </a-button>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 知识库列表 -->
    <a-card :bordered="false">
      <a-row :gutter="[16, 16]">
        <a-col v-for="kb in knowledgeBaseList" :key="kb.id" :xs="24" :sm="12" :md="8" :lg="6">
          <a-card hoverable class="kb-card" size="small">
            <template #extra>
              <div style="width: 40px;display: flex;justify-content: space-between">
                <a-popconfirm
                    title="确定删除该知识库吗？"
                    ok-text="确定"
                    cancel-text="取消"
                    @confirm="handleDelete(kb.id)"
                >
                  <DeleteOutlined key="delete" style="color: #ff4d4f" title="删除知识库"/>
                </a-popconfirm>
                <edit-outlined key="edit" @click="handleEdit(kb)" style="color: #1677ff"
                               title="编辑知识库基本信息"></edit-outlined>
              </div>
            </template>
            <template #title>
              <a-typography-title :level="5" style="margin: 0;display:flex;align-items: center">
                <BookTwoTone style="padding-right: 10px;font-size: 30px"/>
                {{ kb.name }}
              </a-typography-title>
            </template>
            <div @click="showDocModel(kb)">
              <div class="status-info">
                状态：
                <a-badge :status="kb.status === 1 ? 'success' : 'default'">
                  <template #text>
                    <span class="status-info">{{ kb.status === 1 ? '已启用' : '已禁用' }}</span>
                  </template>
                </a-badge>
              </div>
              <div>
                类型：
                <a-tag color="green">{{
                    kbTypeSelectOptions?.filter(item => item.value === kb.type)?.at(0)?.label
                  }}
                </a-tag>
              </div>
              <div>
                AI 向量模型：
                <a-tag color="green">{{ kb.aiVectorModel.customName }}</a-tag>
              </div>
              <div v-if="kb.remark">
                描述：{{ kb.remark }}
              </div>
              <div>
                更新时间：{{ formatTime(kb.updateTime || kb.createTime) }}
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
      <!-- 分页 -->
      <div class="pagination-container">
        <a-pagination size="small"
                      v-model:current="pagination.current"
                      v-model:page-size="pagination.pageSize"
                      :total="pagination.total"
                      show-size-changer
                      show-quick-jumper
                      :show-total="pagination.showTotal"
                      @change="handlePageChange"
                      @show-size-change="handlePageSizeChange"
        />
      </div>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal v-model:open="modalVisible"
             :title="modalTitle"
             width="800px"
             @ok="handleSubmit"
             :confirm-loading="submitLoading"
    >
      <a-form ref="formRef" :model="formData" :rules="formRules" layout="vertical">
        <a-form-item label="知识库名称" name="name">
          <a-input v-model:value="formData.name" placeholder="请输入知识库名称"/>
        </a-form-item>
        <a-form-item label="知识库类型" name="type">
          <a-select v-model:value="formData.type" :options="kbTypeSelectOptions" placeholder="请选择知识库类型">
          </a-select>
        </a-form-item>
        <a-form-item label="AI向量模型" name="aiVectorModelId">
          <a-select v-model:value="formData.aiVectorModelId" placeholder="请选择知识库类型"
                    :loading="aiVectorModelLoading" :disabled="aiVectorModelLoading">
            <a-select-option v-for="model in AiVectorModels" :key="model.id" :value="model.id">
              {{ model.modelName }} <span style="color: #b1b1b1">({{ model.provider?.providerName }})</span>
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="是否启用" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
        <a-form-item label="知识库描述" name="remark">
          <a-textarea v-model:value="formData.remark" placeholder="请输入知识库描述" :rows="4"/>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal :open="docModelVisible" width="100%" wrap-class-name="full-modal" title="知识库文档管理"
             @cancel="()=> {docModelVisible = false}">
      <template #footer>
        <a-button @click="()=> {docModelVisible = false}" type="primary">关闭</a-button>
      </template>
      <a-card>
        <a-tabs v-model:activeKey="docManageTabActiveKey" tabPosition="left">
          <a-tab-pane key="1" tab="文档管理">
            <a-card :bordered="false">
              <a-form ref="docSearchFormRef" :model="docSearchFormData" layout="inline">
                <a-form-item label="文档名称" name="title">
                  <a-input v-model:value="docSearchFormData.title" placeholder="请输入文档名称" style="width: 200px"/>
                </a-form-item>
                <a-form-item>
                  <a-button type="primary" @click="handleDocSearch">
                    <template #icon>
                      <SearchOutlined/>
                    </template>
                    搜索
                  </a-button>
                </a-form-item>
                <a-form-item style="position: absolute;right: 0">
                  <a-button type="primary" @click="showUploadDocModel">
                    <template #icon>
                      <UploadOutlined/>
                    </template>
                    上传文档
                  </a-button>
                </a-form-item>
              </a-form>
            </a-card>
            <a-card>
              <a-empty v-if="docList.length === 0"/>
              <a-row :gutter="[16, 16]" style="max-height: 410px;overflow-y: auto">
                <a-col v-for="doc in docList" :key="doc.id" :xs="24" :sm="12" :md="8" :lg="6">
                  <a-card hoverable class="kb-card" size="small">
                    <template #title>
                      <a-typography-title :level="5"
                                          style="margin: 0;display:flex;align-items: center">
                        <BookTwoTone style="padding-right: 10px;font-size: 30px"/>
                        <a-typography-text :content="doc.title||doc.filename"
                                           :title="doc.title||doc.filename"
                                           :ellipsis="true" style="width: 200px"/>
                      </a-typography-title>
                    </template>
                    <template #extra>
                      <div style="width: 40px;display: flex;justify-content: space-between">
                        <a-popconfirm
                            title="确定删除该文档吗？"
                            ok-text="确定"
                            cancel-text="取消"
                            @confirm="handleDocDelete(doc.id)"
                        >
                          <DeleteOutlined key="delete" style="color: #ff4d4f" title="删除文档"/>
                        </a-popconfirm>
                        <edit-outlined key="edit" @click="handleDocEdit(doc)" style="color: #1677ff"
                                       title="编辑文档基本信息"></edit-outlined>
                      </div>
                    </template>
                    <div>
                      <span>更新时间：{{ formatTime(doc.updateTime || doc.createTime) }}</span><br>
                      <a-tag color="blue">{{ doc.fileType }}</a-tag>
                      <a-tag :color="DocStatus[doc.status].color">
                        {{ DocStatus[doc.status].text || '未知' }}
                      </a-tag>
                    </div>
                  </a-card>
                </a-col>
              </a-row>
              <template #actions>
                <div class="pagination-container">
                  <a-pagination size="small"
                                v-model:current="docListPagination.current"
                                v-model:page-size="docListPagination.pageSize"
                                :total="docListPagination.total"
                                show-size-changer
                                show-quick-jumper
                                :show-total="docListPagination.showTotal"
                                @change="handleDocPageSizeChange"
                  />
                </div>
              </template>
            </a-card>
          </a-tab-pane>
          <a-tab-pane key="2" tab="命中测试" force-render>Content of Tab Pane 2</a-tab-pane>
        </a-tabs>
      </a-card>
    </a-modal>
    <a-modal title="上传文档" :open="uploadDocModelVisible" width="600px" @ok="handleUploadDocSubmit"
             @cancel="()=>{uploadDocModelVisible = false}">
      <a-form ref="uploadDocFormRef" :model="uploadDocFormData" :rules="uploadDocFormRules" layout="vertical">
        <a-form-item label="文档标题" name="title">
          <a-input v-model:value="uploadDocFormData.title" placeholder="输入文档标题"></a-input>
        </a-form-item>
        <a-form-item label="上传文件" name="fileList">
          <a-upload-dragger name="fileList" :file-list="uploadDocFormData.fileList" :multiple="true"
                            :before-upload="beforeDocUpload" @remove="handleDocRemove"
                            accept=".txt,.doc,.docx,.md,.pdf,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document">
            <p class="ant-upload-drag-icon">
              <InboxOutlined/>
            </p>
            <p class="ant-upload-text">点击或拖拽上传</p>
            <p class="ant-upload-hint">支持单个或批量上传</p>
            <p class="ant-upload-hint">支持 txt,md,pdf,word(doc, docx)文件上传</p>
          </a-upload-dragger>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import {ref, reactive, onMounted} from 'vue'
import {message, type SelectProps, type UploadProps} from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  SearchOutlined,
  RedoOutlined,
  PlusOutlined,
  DeleteOutlined,
  BookTwoTone,
  EditOutlined,
  UploadOutlined,
  InboxOutlined
} from '@ant-design/icons-vue'
import {knowledgeBaseApi} from '@/api/knowledgeBase'
import type {KnowledgeBase} from '@/api/knowledgeBase'
import {modelApi} from '@/api/model'
import type {AiModel} from "@/api/model/aiModelTypes";
import {kbDocApi} from '@/api/kbDoc'
import {DocStatus, type KnowledgeBaseDoc} from "@/api/model/knowledgeBaseTypes";

// 搜索表单
const searchForm = ref({
  name: '',
  type: ''
})

const kbTypeSelectOptions = ref<SelectProps["options"]>([
  {
    value: 'KNOWLEDGE_BASE',
    label: '知识库'
  },
  {
    value: 'MEMORY_BASE',
    label: '记忆库'
  }
])

// 知识库列表
const knowledgeBaseList = ref<KnowledgeBase[]>([])

// 分页
const pagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`
})

// 弹窗
const modalVisible = ref(false)
const modalTitle = ref('')
const submitLoading = ref(false)

// 表单
const formRef = ref()
const formData = ref<any>({
  id: undefined,
  name: '',
  type: '',
  status: 1,
  aiVectorModelId: '',
  remark: '',
})

const formRules = {
  name: [{required: true, message: '请输入知识库名称', trigger: 'blur'},
    {max: 20, message: '知识库名称长度不能超过20个字符', trigger: 'blur'}],
  type: [{required: true, message: '请选择知识库类型', trigger: 'change'}],
  aiVectorModelId: [{required: true, message: '请选择AI向量模型', trigger: 'change'}],
  status: [{required: true, message: '请选择知识库状态', trigger: 'change'}],
  remark: [{required: false, message: '请输入知识库描述', trigger: 'blur'}]
}
// 加载AI向量模型
const AiVectorModels = ref<AiModel[]>([{}])
const loadAiVectorModels = async () => {
  try {
    const res = await modelApi.getList(
        {type: 'VECTOR', pageNum: 1, pageSize: 10000}
    )
    if (res.code === '200') {
      AiVectorModels.value = res.data.records
    }
  } catch (error) {
    console.error('加载模型列表失败:', error)
  }
}

// 加载数据
const loadKnowledgeBases = async () => {
  try {
    const res = await knowledgeBaseApi.getList({
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
      type: searchForm.value.type,
      name: searchForm.value.name
    })
    if (res.code === '200') {
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
const handleAdd = async () => {
  modalTitle.value = '创建知识库'
  formData.value = {
    id: undefined,
    name: '',
    type: kbTypeSelectOptions.value?.at(0)?.value,
    status: 1,
    remark: ''
  }
  await loadAiVectorModels()
  modalVisible.value = true
}
const aiVectorModelLoading = ref<boolean>(false)
// 编辑
const handleEdit = async (kb: KnowledgeBase) => {
  modalTitle.value = '编辑知识库'
  formData.value = {...kb}
  modalVisible.value = true
  aiVectorModelLoading.value = true
  formData.value.aiVectorModelId = undefined
  try {
    await loadAiVectorModels()
  } finally {
    aiVectorModelLoading.value = false
  }
  formData.value.aiVectorModelId = kb.aiVectorModelId
}

// 删除
const handleDelete = async (id: number) => {
  try {
    const res = await knowledgeBaseApi.delete(id)
    if (res.code === '200') {
      message.success('删除成功')
      await loadKnowledgeBases()
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
    await loadKnowledgeBases()
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
// 文档管理开始
const docModelVisible = ref<boolean>(false)
const showDocModel = (kb: KnowledgeBase) => {
  docModelVisible.value = true
  uploadDocFormData.value.kbId = kb.id
  loadDocList()
}
const docManageTabActiveKey = ref<string>('1')
const docSearchFormRef = ref()
const docSearchFormData = ref<any>({
  title: ''
})
const uploadDocModelTitle = ref<string>('上传文档')
const uploadDocModelVisible = ref<boolean>(false)
const showUploadDocModel = () => {
  uploadDocModelVisible.value = true
  uploadDocFormData.value.title = ''
  uploadDocFormData.value.fileList = []
}

const docList = ref<KnowledgeBaseDoc[]>([])
const docListPagination = reactive({
  current: 1,
  pageSize: 10,
  total: 0,
  showTotal: (total: number) => `共 ${total} 条`
})
const loadDocList = async () => {
  try {
    const res = await kbDocApi.getList({
      kbId: uploadDocFormData.value.kbId,
      title: docSearchFormData.value.title,
      pageNum: docListPagination.current,
      pageSize: docListPagination.pageSize
    })
    if (res.code === '200') {
      docList.value = res.data.records || []
      docListPagination.total = res.data.total || 0
    }
  } catch (error) {
    console.error('加载文档列表失败:', error)
  }
}
const handleDocPageSizeChange = (current: number, size: number) => {
  docListPagination.current = current
  docListPagination.pageSize = size
  loadDocList()
}
const uploadDocFormRef = ref()
const uploadDocFormData = ref<any>({
  id: undefined,
  kbId: undefined,
  title: '',
  fileList: []
})
const handleDocSearch = async () => {
  await loadDocList()
}
const handleDocDelete = async (id: number | undefined) => {
  try {
    const res = await kbDocApi.delete(id)
    if (res.code === '200') {
      message.success('删除成功')
      await loadDocList()
    }
  } catch (error) {
    console.error('删除失败:', error)
    message.error('删除失败')
  }
}
const uploadDocSubmitLoading = ref<boolean>(false)
const uploadDocFormRules = {
  title: [{required: true, message: '请输入文档标题', trigger: 'blur'},
    {max: 20, message: '文档标题长度不能超过20个字符', trigger: 'blur'}],
  fileList: [{required: true, message: '请选择文件', trigger: 'change'},
    {
      validator: (rule: any, value: any) => {
        for (let file of value) {
          if (file.size > 10 * 1024 * 1024) {
            return Promise.reject(new Error('文件不能超过10M'))
          }
        }
        return Promise.resolve()
      }, trigger: 'change'
    }]
}
const handleDocRemove: UploadProps['onRemove'] = file => {
  const index = uploadDocFormData.value.fileList.indexOf(file);
  const newFileList = uploadDocFormData.value.fileList.slice();
  newFileList.splice(index, 1);
  uploadDocFormData.value.fileList = newFileList;
};

const beforeDocUpload: UploadProps['beforeUpload'] = file => {
  uploadDocFormData.value.fileList = [...(uploadDocFormData.value.fileList || []), file];
  return false
};
const handleUploadDocSubmit = async () => {
  try {
    await uploadDocFormRef.value.validateFields()
  } catch (error: any) {
    console.log(error)
    message.error(error.errorFields[0].errors[0] || '验证失败')
    return
  }
  const formData = new FormData();
  for (let key in uploadDocFormData.value) {
    if (key && key !== 'fileList' && uploadDocFormData.value[key]) {
      formData.append(key, uploadDocFormData.value[key]);
    }
  }
  uploadDocFormData.value.fileList.forEach((file: UploadProps['fileList'][]) => {
    formData.append('files', file as any);
  });
  try {
    const res = await kbDocApi.uploadDoc(formData)
    if (res.code === '200') {
      message.success('上传成功')
      await loadDocList()
    }
  } catch (error: any) {
    message.error(error.message || '上传文档失败')
  }
  uploadDocModelVisible.value = false
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
  border: #ccc 1px solid;
  box-shadow: 2px 2px 4px rgba(0, 0, 0, 0.15);
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
<style lang="less">
.full-modal {
  .ant-modal {
    max-width: 100%;
    top: 0;
    padding-bottom: 0;
    margin: 0;
  }

  .ant-modal-content {
    display: flex;
    flex-direction: column;
    height: calc(100vh);
  }

  .ant-modal-body {
    flex: 1;
  }
}
</style>