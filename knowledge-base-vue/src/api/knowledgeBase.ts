import request from '@/utils/request'

export interface KnowledgeBase {
  id: number
  name: string
  type: string
  description?: string
  createTime?: string
  updateTime?: string
}

export const knowledgeBaseApi = {
  // 获取知识库列表
  getList(params?: { pageNum?: number; pageSize?: number; name?: string }) {
    return request.get('/kb/list', { params })
  },

  // 获取知识库详情
  getDetail(id: number) {
    return request.get(`/kb/detail/${id}`)
  },

  // 新增知识库
  add(data: any) {
    return request.post('/kb/add', data)
  },

  // 更新知识库
  update(data: any) {
    return request.post('/kb/update', data)
  },

  // 删除知识库
  delete(id: number) {
    return request.post('/kb/delete', null, { params: { id } })
  }
}
