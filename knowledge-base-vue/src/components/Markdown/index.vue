<template>
  <div class="markdown-content">
    <template v-for="(segment, index) in contentSegments" :key="index">
      <CodeBlock
        v-if="segment.isCode"
        :code="segment.code!"
        :language="segment.lang!"
      />
      <span v-else-if="segment.content" v-html="segment.content"></span>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import DOMPurify from 'dompurify'
import { renderer, parseCodeBlockHtml } from './renderer'
import CodeBlock from '../CodeBlock.vue'

interface Props {
  content: string
  webSearchResults?: Array<{
    id: string
    content: string
    sourceUrl: string
  }>
}

const props = withDefaults(defineProps<Props>(), {
  content: '',
  webSearchResults: () => []
})

interface ContentSegment {
  isCode: boolean
  content?: string
  code?: string
  lang?: string
}

// 解析并分割内容为代码块和HTML片段
const contentSegments = computed((): ContentSegment[] => {
  if (!props.content) return []
  
  const html = renderer.render(props.content, props.webSearchResults)
  const { codeBlocks } = parseCodeBlockHtml(html)
  
  const segments: ContentSegment[] = []
  let lastIndex = 0
  
  // 用占位符位置分割
  const placeholderRegex = /<code-block-placeholder[^>]*><\/code-block-placeholder>/g
  let match
  
  while ((match = placeholderRegex.exec(html)) !== null) {
    // 添加占位符之前的HTML内容
    if (match.index > lastIndex) {
      const htmlContent = html.slice(lastIndex, match.index)
      if (htmlContent.trim()) {
        segments.push({
          isCode: false,
          content: DOMPurify.sanitize(htmlContent, {
            ADD_TAGS: ['button'],
            ADD_ATTR: ['onclick', 'class', 'target', 'rel']
          })
        })
      }
    }
    
    // 找到对应的代码块
    const placeholderIndex = segments.filter(s => s.isCode).length
    if (placeholderIndex < codeBlocks.length) {
      const block = codeBlocks[placeholderIndex]
      segments.push({
        isCode: true,
        code: block.code,
        lang: block.lang
      })
    }
    
    lastIndex = match.index + match[0].length
  }
  
  // 添加剩余的HTML内容
  if (lastIndex < html.length) {
    const remainingHtml = html.slice(lastIndex)
    if (remainingHtml.trim()) {
      segments.push({
        isCode: false,
        content: DOMPurify.sanitize(remainingHtml, {
          ADD_TAGS: ['button'],
          ADD_ATTR: ['onclick', 'class', 'target', 'rel']
        })
      })
    }
  }
  
  return segments
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.7;
  word-wrap: break-word;
  font-size: 15px;
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
  background-color: rgba(0, 0, 0, 0.05);
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

.markdown-content :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  padding: 8px 16px;
  border: 1px solid #dfe2e5;
}

.markdown-content :deep(th) {
  background-color: #f6f8fa;
  font-weight: 600;
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
