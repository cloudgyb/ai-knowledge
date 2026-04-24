<template>
  <div class="code-block-wrapper" :data-lang="language">
    <!-- Header -->
    <div class="code-header">
      <div class="code-header-left">
        <span class="lang-icon" v-html="languageIcon"></span>
        <span class="lang-label">{{ displayLanguage }}</span>
      </div>
      <div class="code-header-right">
        <button
          v-if="showCollapseButton"
          class="collapse-btn"
          @click="toggleCollapse"
          :title="isCollapsed ? '展开代码' : '收起代码'"
        >
          <span v-html="isCollapsed ? expandIcon : collapseIcon"></span>
        </button>
        <button class="copy-btn" @click="handleCopyCode" title="复制代码">
          <span v-html="isCopied ? copiedIcon : copyIcon"></span>
          <span class="copy-text">{{ isCopied ? '已复制' : '复制' }}</span>
        </button>
      </div>
    </div>

    <!-- Code Container -->
    <div 
      class="code-container" 
      :class="{ collapsed: isCollapsed }"
      ref="codeContainerRef"
    >
      <pre class="hljs" :class="`language-${language}`"><code ref="codeRef" v-html="highlightedCode"></code></pre>
    </div>

    <!-- Collapse Overlay -->
    <div v-if="isCollapsed" class="collapse-overlay" @click="toggleCollapse">
      <span>点击展开 {{ displayLanguage }} 代码 ({{ lineCount }} 行)</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import hljs from 'highlight.js'
import { escapeHtml, getLanguageDisplayName } from '@/utils/markdown'

interface Props {
  code: string
  language?: string
  showLineNumbers?: boolean
  maxCollapsedLines?: number
}

const props = withDefaults(defineProps<Props>(), {
  language: 'plaintext',
  showLineNumbers: false,
  maxCollapsedLines: 10
})

const isCopied = ref(false)
const isCollapsed = ref(false)
const showCollapseButton = ref(false)
const codeContainerRef = ref<HTMLElement | null>(null)
const codeRef = ref<HTMLElement | null>(null)

// Icons
const copyIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="9" y="9" width="13" height="13" rx="2" ry="2"></rect><path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"></path></svg>`
const copiedIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>`
const expandIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="6 9 12 15 18 9"></polyline></svg>`
const collapseIcon = `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="18 15 12 9 6 15"></polyline></svg>`

// Language icons (simplified)
const languageIcons: Record<string, string> = {
  javascript: '📜',
  typescript: '📘',
  python: '🐍',
  java: '☕',
  cpp: '⚙️',
  csharp: '🔷',
  go: '🐹',
  rust: '🦀',
  html: '🌐',
  css: '🎨',
  sql: '🗃️',
  bash: '💻',
  json: '📋',
  markdown: '📝',
}

// Computed properties
const highlightedCode = computed(() => {
  const lang = props.language?.trim().toLowerCase() || 'plaintext'
  
  if (props.code && hljs.getLanguage(lang)) {
    try {
      return hljs.highlight(props.code, { language: lang, ignoreIllegals: true }).value
    } catch {
      return escapeHtml(props.code)
    }
  }
  
  return escapeHtml(props.code)
})

const displayLanguage = computed(() => getLanguageDisplayName(props.language))

const languageIcon = computed(() => languageIcons[props.language?.toLowerCase()] || '📄')

const lineCount = computed(() => {
  return props.code.split('\n').length
})

// Methods
const handleCopyCode = async () => {
  try {
    await navigator.clipboard.writeText(props.code)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  } catch (err) {
    console.error('Failed to copy code:', err)
    // Fallback for older browsers
    const textArea = document.createElement('textarea')
    textArea.value = props.code
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  }
}

const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// Debounce utility
function debounce<T extends (...args: unknown[]) => void>(fn: T, delay: number): T {
  let timeoutId: ReturnType<typeof setTimeout>
  return ((...args: unknown[]) => {
    clearTimeout(timeoutId)
    timeoutId = setTimeout(() => fn(...args), delay)
  }) as T
}

// Check if code should be collapsible
const checkContainerHeight = () => {
  if (codeContainerRef.value) {
    // Code block with more than 15 lines can be collapsed
    showCollapseButton.value = lineCount.value > 15
  }
}

const debouncedHeightCheck = debounce(checkContainerHeight, 180)

// ResizeObserver for responsive collapse
let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  checkContainerHeight()
  
  if (typeof ResizeObserver !== 'undefined' && codeContainerRef.value) {
    resizeObserver = new ResizeObserver(debouncedHeightCheck)
    resizeObserver.observe(codeContainerRef.value)
  }
})

onUnmounted(() => {
  if (resizeObserver) {
    resizeObserver.disconnect()
  }
})

// Re-check when code changes
watch(() => props.code, debouncedHeightCheck)
</script>

<style scoped>
.code-block-wrapper {
  position: relative;
  margin: 1em 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  contain: content;
  content-visibility: auto;
  contain-intrinsic-size: 320px 180px;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5em 1em;
  background: #ffffff;
  border-bottom: 1px solid #e9ecef;
}

.code-header-left {
  display: flex;
  align-items: center;
  gap: 0.5em;
}

.lang-icon {
  font-size: 1em;
}

.lang-label {
  color: #6c757d;
  font-size: 0.85em;
  font-family: 'Fira Code', monospace;
  text-transform: lowercase;
}

.code-header-right {
  display: flex;
  align-items: center;
  gap: 0.5em;
}

.code-header-right button {
  display: flex;
  align-items: center;
  gap: 0.3em;
  padding: 0.3em 0.6em;
  background: transparent;
  border: 1px solid #dee2e6;
  border-radius: 4px;
  color: #6c757d;
  font-size: 0.75em;
  cursor: pointer;
  transition: all 0.2s;
}

.code-header-right button:hover {
  background: #e9ecef;
  color: #495057;
}

.copy-text {
  font-family: system-ui, sans-serif;
}

.code-container {
  max-height: none;
  transition: max-height 0.3s ease;
}

.code-container.collapsed {
  max-height: 200px;
  overflow: hidden;
}

.code-container pre {
  margin: 0;
  padding: 1em;
  border-radius: 0;
  overflow-x: auto;
  background: #f8f9fa;
}

.code-container code {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 0.875em;
  line-height: 1.6;
  color: #212529;
}

/* Scrollbar for light theme */
.code-container::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

.code-container::-webkit-scrollbar-track {
  background: #f8f9fa;
}

.code-container::-webkit-scrollbar-thumb {
  background: #ced4da;
  border-radius: 4px;
}

.code-container::-webkit-scrollbar-thumb:hover {
  background: #adb5bd;
}

.collapse-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 2em 1em 1em;
  background: linear-gradient(transparent, #f8f9fa 70%);
  text-align: center;
  cursor: pointer;
  color: #6c757d;
  font-size: 0.85em;
  transition: color 0.2s;
}

.collapse-overlay:hover {
  color: #495057;
}
</style>
