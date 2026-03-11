# 快速启动指南

## 项目已成功创建并启动！🎉

### 当前状态
- ✅ 项目初始化完成
- ✅ 依赖安装完成
- ✅ 开发服务器已启动
- ✅ 访问地址：http://localhost:3000

### 项目功能

#### 1. AI 模型管理 (/ai-model)
- 📋 卡片式展示 AI 模型列表
- 🔍 支持按名称和类型搜索
- ➕ 新增 AI 模型（可选择供应商）
- ✏️ 编辑 AI 模型
- 🗑️ 删除 AI 模型（鼠标移入卡片显示删除按钮）

#### 2. 知识库管理 (/knowledge-base)
- 📋 卡片式展示知识库列表
- 🔍 支持按名称和类型搜索
- ➕ 创建知识库
- ✏️ 编辑知识库
- 🗑️ 删除知识库（鼠标移入卡片显示删除按钮）

#### 3. AI 小助理 (/assistant)
- 💬 聊天对话界面
- 📚 可选择多个知识库
- ⚡ SSE 流式输出
- ✓ 全选/清空知识库选择

### UI 布局
- 📌 顶部菜单：左侧显示系统名称，右侧显示用户名下拉菜单
- 📌 左侧边栏：三个功能模块导航
- 📌 主体内容：响应式卡片布局

### 技术栈
- Vue 3 + TypeScript
- Ant Design Vue 4.x
- Vite 6.x
- Pinia（状态管理）
- Vue Router（路由）
- Axios（HTTP 请求）

### 注意事项

⚠️ **后端服务需要启动**
- 前端会代理到后端服务：http://localhost:8080
- 如果后端未启动，接口调用会失败（控制台会显示 proxy error）

⚠️ **SSE 流式聊天**
- 需要后端支持 SSE（Server-Sent Events）
- 确保后端有 `/kb/chat` 接口

⚠️ **用户信息**
- 当前使用模拟的用户数据（admin/管理员）
- 实际项目中需要对接登录接口

### 常用命令

```bash
# 安装依赖（已完成）
npm install

# 启动开发服务器（已启动）
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码检查
npm run lint
```

### 下一步

1. **启动后端服务**（如果还未启动）
   ```bash
   cd ../knowledge-base-server
   # 启动后端服务
   ```

2. **测试功能**
   - 打开浏览器访问 http://localhost:3000
   - 依次测试三个功能模块

3. **开发调试**
   - 修改 src 目录下的文件会自动热更新
   - 查看浏览器开发者工具进行调试

### 项目结构
```
knowledge-base-vue/
├── src/
│   ├── api/              # API 接口
│   ├── layouts/          # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   ├── utils/            # 工具函数
│   └── views/            # 页面组件
├── package.json
├── vite.config.ts
└── tsconfig.json
```

祝开发愉快！🚀
