# AI 知识库管理系统 - 前端

基于 Vue 3 + TypeScript + Ant Design Vue 的 AI 知识库管理系统前端。

## 技术栈

- Vue 3 - 渐进式 JavaScript 框架
- TypeScript - JavaScript 的超集
- Vite - 下一代前端构建工具
- Ant Design Vue 4.x - 企业级 UI 组件库
- Pinia - Vue 官方状态管理库
- Vue Router 4 - 官方路由管理器
- Axios - HTTP 客户端

## 功能模块

### 1. AI 模型管理
- AI 模型列表展示（卡片形式）
- 按模型名称和类型搜索
- 新增 AI 模型（选择供应商）
- 编辑 AI 模型
- 删除 AI 模型（鼠标移入显示删除按钮）

### 2. 知识库管理
- 知识库列表展示（卡片形式）
- 按知识库名称和类型搜索
- 创建知识库
- 编辑知识库
- 删除知识库（鼠标移入显示删除按钮）

### 3. AI 小助理
- 聊天对话界面
- 可选择多个知识库
- SSE 流式输出
- 全选/清空知识库选择

## 项目结构

```
knowledge-base-vue/
├── src/
│   ├── api/                  # API 接口定义
│   │   ├── model.ts         # AI 模型相关接口
│   │   ├── knowledgeBase.ts # 知识库相关接口
│   │   └── chat.ts          # 聊天相关接口
│   ├── layouts/             # 布局组件
│   │   └── MainLayout.vue   # 主布局（顶部菜单 + 左侧边栏）
│   ├── router/              # 路由配置
│   │   └── index.ts
│   ├── stores/              # Pinia 状态管理
│   │   └── user.ts         # 用户信息 store
│   ├── utils/               # 工具函数
│   │   └── request.ts      # Axios 请求封装
│   ├── views/               # 页面组件
│   │   ├── ai-model/       # AI 模型管理页面
│   │   ├── knowledge-base/ # 知识库管理页面
│   │   └── assistant/      # AI 小助理页面
│   ├── App.vue             # 根组件
│   └── main.ts             # 入口文件
└── package.json
```

## 开发指南

### 环境要求

- Node.js >= 18.0.0
- npm >= 9.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:3000

### 构建生产版本

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

### 代码检查

```bash
npm run lint
```

## 后端接口

项目默认代理到后端服务 `http://localhost:8080`，在 `vite.config.ts` 中配置。

主要接口：

### AI 模型管理
- GET `/api/ai/model/list` - 获取模型列表
- GET `/api/ai/model/detail/:id` - 获取模型详情
- POST `/api/ai/model/add` - 新增模型
- POST `/api/ai/model/update` - 更新模型
- POST `/api/ai/model/delete` - 删除模型
- GET `/api/sys/ai/model/providers` - 获取供应商列表

### 知识库管理
- GET `/api/kb/list` - 获取知识库列表
- GET `/api/kb/detail/:id` - 获取知识库详情
- POST `/api/kb/add` - 新增知识库
- POST `/api/kb/update` - 更新知识库
- POST `/api/kb/delete` - 删除知识库

### AI 聊天
- POST `/api/kb/chat` - SSE 流式聊天接口

## UI 设计说明

- 采用 Ant Design Vue 原生组件和样式，无自定义 CSS
- 顶部菜单：系统名称 + 用户名下拉菜单
- 左侧边栏：三个主要功能模块导航
- 主体内容：响应式卡片布局

## 注意事项

1. 首次运行前需要先安装依赖
2. 确保后端服务已启动（默认端口 8080）
3. 聊天功能使用 SSE 流式输出，需要后端支持
4. 用户信息目前为模拟数据，实际项目中需对接登录接口
