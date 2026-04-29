<template>
  <div class="code-block-wrapper" :class="{ collapsed: isCollapsed && canCollapse }">
    <!-- 代码块头部 -->
    <div class="code-header">
      <div class="code-header-left">
        <span class="lang-label">{{ displayLanguage }}</span>
      </div>
      <div class="code-header-right">
        <button class="header-btn copy-btn" @click="handleCopy" :title="isCopied ? '已复制' : '复制代码'">
          <span v-if="isCopied" class="copy-success">✓</span>
          <span v-else>📋</span>
        </button>
        <button v-if="canCollapse" class="header-btn collapse-btn" @click="toggleCollapse"
                :title="isCollapsed ? '展开' : '收起'">
          <span class="collapse-icon" :class="{ collapsed: isCollapsed }">▼</span>
        </button>
      </div>
    </div>

    <!-- 代码内容 -->
    <div class="code-content-wrapper" ref="codeContainerRef">
      <div class="code-content" :class="{ 'content-collapsed': isCollapsed && canCollapse }"
           v-html="highlightedCode"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref, computed, onMounted, onUnmounted} from 'vue'
import hljs from 'highlight.js'

// 防抖函数
function debounce<T extends (...args: any[]) => any>(
    fn: T,
    delay: number
): (...args: Parameters<T>) => void {
  let timeoutId: ReturnType<typeof setTimeout> | null = null
  return (...args: Parameters<T>) => {
    if (timeoutId) clearTimeout(timeoutId)
    timeoutId = setTimeout(() => fn(...args), delay)
  }
}

// 语言名称映射
const languageMap: Record<string, string> = {
  js: 'javascript',
  ts: 'typescript',
  py: 'python',
  rb: 'ruby',
  sh: 'bash',
  shell: 'bash',
  zsh: 'bash',
  yml: 'yaml',
  md: 'markdown',
  rs: 'rust',
  ml: 'ocaml',
  kt: 'kotlin',
  cs: 'csharp',
  'c++': 'cpp'
}

// 获取语言显示名称
function getDisplayLanguage(lang: string): string {
  const lowerLang = lang.trim().toLowerCase()
  return languageMap[lowerLang] || lowerLang
}

interface Props {
  code: string
  language: string
  /** 是否默认折叠 */
  defaultCollapsed?: boolean
  /** 折叠高度阈值 (px) */
  collapseHeight?: number
}

const props = withDefaults(defineProps<Props>(), {
  code: '',
  language: 'text',
  defaultCollapsed: false,
  collapseHeight: 350
})

const isCopied = ref(false)
const isCollapsed = ref(props.defaultCollapsed)
const canCollapse = ref(false)
const codeContainerRef = ref<HTMLElement | null>(null)
let resizeObserver: ResizeObserver | null = null

// 显示语言
const displayLanguage = computed(() => getDisplayLanguage(props.language))

// 高亮代码
const highlightedCode = computed(() => {
  const lang = props.language.trim().toLowerCase()
  if (props.code && hljs.getLanguage(lang)) {
    return hljs.highlight(props.code, {language: lang, ignoreIllegals: true}).value
  }
  return props.code
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
})

// 切换折叠
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 检查是否需要折叠
const checkContainerHeight = (element: HTMLElement | undefined | null) => {
  return element ? element.scrollHeight >= props.collapseHeight : false
}

const debouncedHeightCheck = debounce(() => {
  canCollapse.value = checkContainerHeight(codeContainerRef.value)
}, 180)

// 初始化 ResizeObserver
const initResizeObserver = () => {
  if (typeof ResizeObserver === 'undefined') {
    window.addEventListener('resize', debouncedHeightCheck)
    return
  }

  resizeObserver = new ResizeObserver(debouncedHeightCheck)
  if (codeContainerRef.value) {
    resizeObserver.observe(codeContainerRef.value)
  }
}

// 复制代码
const handleCopy = async () => {
  try {
    await window.copyToClipboard(props.code)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
  }
}

onMounted(() => {
  debouncedHeightCheck()
  initResizeObserver()
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  } else {
    window.removeEventListener('resize', debouncedHeightCheck)
  }
})
</script>

<style scoped>
.code-block-wrapper {
  margin: 16px 0;
  border-radius: 8px;
  overflow: hidden;
  background-color: #f6f8fa;
  border: 1px solid #e1e4e8;
  contain: content;
  content-visibility: auto;
  contain-intrinsic-size: 320px 180px;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f0f2f5;
  border-bottom: 1px solid #d0d7de;
}

.code-header-left {
  display: flex;
  align-items: center;
}

.lang-label {
  font-size: 12px;
  color: #24292f;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', monospace;
  font-weight: 500;
}

.code-header-right {
  display: flex;
  align-items: center;
  gap: 4px;
}

.header-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 28px;
  border: 1px solid #d0d7de;
  background-color: #ffffff;
  color: #57606a;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s ease;
}

.header-btn:hover {
  background-color: #f6f8fa;
  border-color: #afb8c1;
  color: #24292f;
}

.header-btn:active {
  background-color: #eaeef2;
}

.icon-copy {
  font-size: 14px;
  line-height: 1;
}

.collapse-icon {
  font-size: 11px;
  line-height: 1;
  transition: transform 0.2s ease;
}

.collapse-icon.collapsed {
  transform: rotate(180deg);
}

.copy-success {
  color: #1a7f37;
  font-size: 14px;
}

.code-content-wrapper {
  position: relative;
  overflow: hidden;
}

.code-content {
  padding: 16px;
  overflow-x: auto;
  font-size: 14px;
  line-height: 1.5;
  font-family: 'Fira Code', 'Consolas', monospace;
  background-color: #f6f8fa;
}

.code-content code {
  color: #24292e;
}

/* 折叠样式 */
.code-block-wrapper.collapsed .code-content-wrapper {
  max-height: 200px;
}

.content-collapsed {
  max-height: 200px;
  overflow: hidden;
}

/* highlight.js 样式覆盖 */
.code-content :deep(.hljs-keyword) {
  color: #d73a49;
}

.code-content :deep(.hljs-string) {
  color: #032f62;
}

.code-content :deep(.hljs-number) {
  color: #005cc5;
}

.code-content :deep(.hljs-function) {
  color: #6f42c1;
}

.code-content :deep(.hljs-comment) {
  color: #6a737d;
}

.code-content :deep(.hljs-class) {
  color: #22863a;
}

.code-content :deep(.hljs-variable) {
  color: #e36209;
}

.code-content :deep(.hljs-built_in) {
  color: #005cc5;
}

.code-content :deep(.hljs-attr) {
  color: #6f42c1;
}

.code-content :deep(.hljs-tag) {
  color: #22863a;
}

.code-content :deep(.hljs-name) {
  color: #22863a;
}

.code-content :deep(.hljs-selector-class) {
  color: #6f42c1;
}

.code-content :deep(.hljs-selector-id) {
  color: #6f42c1;
}

.code-content :deep(.hljs-attribute) {
  color: #6f42c1;
}
</style>
