<template>
  <div class="stream-markdown">
    <!-- 渲染内容 -->
    <div class="markdown-content" v-html="safeHtml"></div>
    
    <!-- 流式指示器 -->
    <span v-if="isStreaming" class="streaming-cursor">▍</span>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import DOMPurify from 'dompurify'
import { renderer } from './renderer'
// 节流函数
function throttle<T extends (...args: any[]) => any>(
    fn: T,
    delay: number
): (...args: Parameters<T>) => void {
  let lastTime = 0
  return (...args: Parameters<T>) => {
    const now = Date.now()
    if (now - lastTime >= delay) {
      lastTime = now
      fn(...args)
    }
  }
}
interface Props {
  /** 流式内容 */
  content: string
  /** 是否正在流式传输 */
  streaming?: boolean
  /** 搜索结果 */
  webSearchResults?: Array<{
    id: string
    content: string
    sourceUrl: string
  }>
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  streaming: false,
  webSearchResults: () => []
})

const emit = defineEmits<{
  (e: 'update:content', value: string): void
}>()

// 显示内容（节流更新，避免频繁渲染）
const displayContent = ref('')
const isStreaming = ref(false)

// 节流更新显示内容
const updateDisplay = throttle(() => {
  displayContent.value = props.content
}, 50) // 50ms 节流，20fps 足够流畅

// 监听内容变化
watch(
  () => props.content,
  () => {
    updateDisplay()
    isStreaming.value = props.streaming
  },
  { immediate: true }
)

// 监听流式结束
watch(
  () => props.streaming,
  (streaming) => {
    if (!streaming) {
      isStreaming.value = false
      displayContent.value = props.content // 流结束时确保显示全部内容
    }
  }
)

// 安全 HTML
const safeHtml = computed(() => {
  if (!displayContent.value) return ''
  const html = renderer.render(displayContent.value, props.webSearchResults)
  return DOMPurify.sanitize(html, {
    ADD_TAGS: ['button'],
    ADD_ATTR: ['onclick', 'class', 'target', 'rel']
  })
})

// 暴露追加内容的方法
defineExpose({
  appendContent: (chunk: string) => {
    emit('update:content', props.content + chunk)
  }
})
</script>

<style scoped>
.stream-markdown {
  line-height: 1.7;
  word-wrap: break-word;
  font-size: 15px;
}

.streaming-cursor {
  display: inline-block;
  animation: blink 1s infinite;
  color: #0366d6;
  font-weight: bold;
  margin-left: 2px;
}

@keyframes blink {
  0%, 50% {
    opacity: 1;
  }
  51%, 100% {
    opacity: 0;
  }
}

.markdown-content {
  display: inline;
}

.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.25;
}

.markdown-content :deep(h1) {
  font-size: 2em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #eaecef;
}

.markdown-content :deep(h2) {
  font-size: 1.5em;
  padding-bottom: 0.3em;
  border-bottom: 1px solid #eaecef;
}

.markdown-content :deep(h3) {
  font-size: 1.25em;
}

.markdown-content :deep(p) {
  margin: 16px 0;
  display: inline;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 2em;
  margin: 16px 0;
}

.markdown-content :deep(li) {
  margin: 4px 0;
}

.markdown-content :deep(blockquote) {
  margin: 16px 0;
  padding: 0 1em;
  color: #6a737d;
  border-left: 4px solid #dfe2e5;
}

.markdown-content :deep(code) {
  padding: 0.2em 0.4em;
  margin: 0;
  font-size: 85%;
  background-color: rgba(27, 31, 35, 0.05);
  border-radius: 3px;
  font-family: 'Fira Code', 'Consolas', monospace;
}

.markdown-content :deep(pre) {
  margin: 16px 0;
  padding: 0 !important;
  overflow: auto;
  border-radius: 8px;
}

.markdown-content :deep(pre code) {
  padding: 20px !important;
  margin: 0 !important;
  background-color: transparent;
  font-size: 13px;
}

.markdown-content :deep(a) {
  color: #0366d6;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
}

.markdown-content :deep(hr) {
  margin: 24px 0;
  border: 0;
  border-top: 1px solid #dfe2e5;
}
</style>
