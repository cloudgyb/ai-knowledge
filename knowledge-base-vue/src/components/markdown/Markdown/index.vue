<template>
  <div class="markdown-renderer" ref="containerRef">
    <div 
      class="markdown-content"
      ref="contentRef"
      v-html="sanitizedHtml"
    ></div>
    <div v-if="isStreaming" class="streaming-indicator">
      <span class="dot"></span>
      <span class="dot"></span>
      <span class="dot"></span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted, onUnmounted, h, createApp } from 'vue'
import DOMPurify from 'dompurify'
import { createMarkdownRenderer, renderMarkdown } from '@/utils/markdown'
import type { WebSearchResult } from '@/utils/types'
import CodeBlock from '@/components/markdown/CodeBlock/index.vue'

interface Props {
  content: string
  isStreaming?: boolean
  webSearchResults?: WebSearchResult[]
}

const props = withDefaults(defineProps<Props>(), {
  isStreaming: false,
  webSearchResults: () => []
})

const contentRef = ref<HTMLElement | null>(null)
const containerRef = ref<HTMLElement | null>(null)
const renderer = createMarkdownRenderer()

// Placeholder marker for code blocks
const CODE_BLOCK_MARKER_START = '___CODE_BLOCK_MARKER___'
const CODE_BLOCK_MARKER_END = '___END_CODE_BLOCK___'
const CODE_BLOCK_REGEX = /___CODE_BLOCK_MARKER___(\d+)___END_CODE_BLOCK___/g
let codeBlocksData: Array<{ code: string; language: string }> = []

// Track mounted apps to avoid re-mounting
const mountedApps = new Map<string, any>()

// Extract code blocks and replace with markers
function extractCodeBlocks(html: string): { html: string; codeBlocks: Array<{ code: string; language: string }> } {
  const codeBlocks: Array<{ code: string; language: string }> = []
  
  // Match <pre class="code-block-placeholder" data-lang="xxx"><code>...</code></pre> blocks
  const preRegex = /<pre[^>]*class="code-block-placeholder"[^>]*data-lang="(\w+)"[^>]*>([\s\S]*?)<\/pre>/gi
  let index = 0
  
  const processedHtml = html.replace(preRegex, (match, lang, content) => {
    const language = lang || 'plaintext'
    
    // Extract code content (remove inner <code> tags)
    let code = content
      .replace(/<code[^>]*>/gi, '')
      .replace(/<\/code>/gi, '')
    
    // Decode HTML entities to get original code
    code = decodeHtmlEntities(code)
    
    codeBlocks.push({ code, language })
    return `${CODE_BLOCK_MARKER_START}${index++}${CODE_BLOCK_MARKER_END}`
  })
  
  return { html: processedHtml, codeBlocks }
}

// Decode HTML entities
function decodeHtmlEntities(text: string): string {
  const entities: Record<string, string> = {
    '&amp;': '&',
    '&lt;': '<',
    '&gt;': '>',
    '&quot;': '"',
    '&#39;': "'",
    '&nbsp;': ' '
  }
  return text.replace(/&[^;]+;/g, (match) => entities[match] || match)
}

// Sanitize and prepare HTML
const sanitizedHtml = computed(() => {
  if (!props.content) return ''
  
  // For streaming content, add a small buffer to avoid rendering incomplete syntax
  let contentToRender = props.content
  
  // If streaming and content is very short, don't render markdown yet
  if (props.isStreaming && contentToRender.length < 3) {
    // Just escape and return as plain text
    return contentToRender
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#039;')
  }
  
  const html = renderMarkdown(contentToRender, renderer, props.webSearchResults, props.isStreaming)
  
  // Extract code blocks first
  const { html: processedHtml, codeBlocks } = extractCodeBlocks(html)
  codeBlocksData = codeBlocks
  
  // Sanitize HTML (allow necessary tags and attributes)
  const safeHtml = DOMPurify.sanitize(processedHtml, {
    ALLOWED_TAGS: [
      'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
      'p', 'br', 'hr',
      'ul', 'ol', 'li', 'dl', 'dt', 'dd',
      'blockquote', 'pre', 'code',
      'a', 'strong', 'em', 'del', 's', 'b', 'i', 'u', 'mark', 'sup', 'sub',
      'table', 'thead', 'tbody', 'tfoot', 'tr', 'th', 'td',
      'img', 'div', 'span',
      'section', 'article',
      'input'
    ],
    ALLOWED_ATTR: [
      'href', 'target', 'rel',
      'src', 'alt', 'title', 'width', 'height',
      'class', 'id',
      'type', 'checked', 'disabled',
      'data-lang', 'data-line', 'data-folded'
    ],
    ALLOW_DATA_ATTR: false,
    ADD_ATTR: ['target']
  })
  
  return safeHtml
})

// Convert code block markers to actual CodeBlock components
function replaceCodeBlocks() {
  if (!contentRef.value) return
  
  const container = contentRef.value
  
  // Collect indices of code blocks that need to be mounted
  const indicesToMount = new Set<string>()
  
  // Find all code block markers in text nodes
  const walker = document.createTreeWalker(
    container,
    NodeFilter.SHOW_TEXT,
    null
  )
  
  const textNodes: Text[] = []
  let node: Text | null
  while ((node = walker.nextNode() as Text | null)) {
    if (node.textContent?.includes(CODE_BLOCK_MARKER_START)) {
      textNodes.push(node)
    }
  }
  
  // Process each text node with markers
  for (const textNode of textNodes) {
    const parent = textNode.parentNode
    if (!parent) continue
    
    const text = textNode.textContent || ''
    const parts = text.split(CODE_BLOCK_REGEX)
    
    // Find or create container for replacement
    const fragment = document.createDocumentFragment()
    
    parts.forEach((part, i) => {
      if (i % 2 === 1) {
        // This is an index
        const index = parseInt(part, 10)
        if (index < codeBlocksData.length) {
          const { code, language } = codeBlocksData[index]
          const indexStr = String(index)
          indicesToMount.add(indexStr)
          
          // Check if already mounted by looking for existing wrapper with same index
          const existingWrapper = container.querySelector(`.code-block-wrapper[data-code-index="${indexStr}"]`)
          
          if (existingWrapper && existingWrapper.parentElement) {
            // Already exists and is in DOM, move it to current position
            fragment.appendChild(existingWrapper)
          } else {
            // Create a new container div for the Vue component
            const containerDiv = document.createElement('div')
            containerDiv.className = 'code-block-wrapper'
            containerDiv.dataset.codeIndex = indexStr
            
            fragment.appendChild(containerDiv)
            
            // Mount Vue component into container
            mountCodeBlock(containerDiv, code, language, indexStr)
          }
        }
      } else if (part) {
        fragment.appendChild(document.createTextNode(part))
      }
    })
    
    parent.replaceChild(fragment, textNode)
  }
  
  // Cleanup old apps that are no longer needed
  cleanupOldApps(indicesToMount)
}

// Mount CodeBlock component into a DOM element
function mountCodeBlock(container: HTMLElement, code: string, language: string, index: string) {
  // Clear container
  container.innerHTML = ''
  
  // Create a temporary wrapper
  const wrapper = document.createElement('div')
  container.appendChild(wrapper)
  
  // Create and mount Vue app
  const app = createApp({
    render() {
      return h(CodeBlock, { code, language })
    }
  })
  
  app.mount(wrapper)
  
  // Track the mounted app
  mountedApps.set(index, app)
}

// Cleanup old apps that are no longer needed
function cleanupOldApps(currentIndices: Set<string>) {
  mountedApps.forEach((app, index) => {
    if (!currentIndices.has(index)) {
      try {
        app.unmount()
      } catch (e) {
        console.warn('Failed to unmount app:', e)
      }
      mountedApps.delete(index)
    }
  })
}

// Watch for content changes and replace code blocks
watch(() => props.content, async () => {
  await nextTick()
  // Use requestAnimationFrame for smoother updates
  requestAnimationFrame(() => {
    replaceCodeBlocks()
  })
})

// Initial mount
onMounted(() => {
  requestAnimationFrame(() => {
    replaceCodeBlocks()
  })
})

onUnmounted(() => {
  // Unmount all tracked apps
  mountedApps.forEach(app => {
    try {
      app.unmount()
    } catch (e) {
      console.warn('Failed to unmount app:', e)
    }
  })
  mountedApps.clear()
})
</script>

<style scoped>
.markdown-renderer {
  position: relative;
  width: 100%;
}

.markdown-content {
  font-size: 15px;
  line-height: 1.7;
  color: #374151;
  word-break: break-word;
  overflow-wrap: break-word;
}

/* Inherit global markdown styles */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin-top: 1.5em;
  margin-bottom: 0.5em;
  font-weight: 600;
  line-height: 1.3;
  color: #111827;
}

.markdown-content :deep(h1) { font-size: 1.75em; }
.markdown-content :deep(h2) { font-size: 1.5em; }
.markdown-content :deep(h3) { font-size: 1.25em; }
.markdown-content :deep(h4) { font-size: 1.1em; }
.markdown-content :deep(h5) { font-size: 1em; }
.markdown-content :deep(h6) { font-size: 0.9em; color: #6b7280; }

.markdown-content :deep(p) {
  margin: 1em 0;
}

.markdown-content :deep(a) {
  color: #2563eb;
  text-decoration: none;
  transition: color 0.2s;
}

.markdown-content :deep(a:hover) {
  color: #1d4ed8;
  text-decoration: underline;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 1em 0;
  padding-left: 1.5em;
}

.markdown-content :deep(li) {
  margin: 0.5em 0;
}

.markdown-content :deep(li > ul),
.markdown-content :deep(li > ol) {
  margin: 0.25em 0;
}

.markdown-content :deep(blockquote) {
  margin: 1em 0;
  padding: 0.5em 1em;
  border-left: 4px solid #d1d5db;
  background: #f9fafb;
  color: #4b5563;
  border-radius: 0 4px 4px 0;
}

.markdown-content :deep(blockquote p) {
  margin: 0.5em 0;
}

.markdown-content :deep(code:not(.code-block-wrapper code)) {
  padding: 0.2em 0.4em;
  margin: 0 0.2em;
  font-size: 0.875em;
  font-family: 'Fira Code', 'Consolas', monospace;
  background: #f3f4f6;
  border-radius: 4px;
  color: #dc2626;
}

.markdown-content :deep(.code-block-wrapper) {
  margin: 1em 0;
}

.markdown-content :deep(table) {
  width: 100%;
  margin: 1em 0;
  border-collapse: collapse;
  font-size: 0.9em;
  overflow-x: auto;
  display: block;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  padding: 0.75em 1em;
  border: 1px solid #e5e7eb;
  text-align: left;
}

.markdown-content :deep(th) {
  background: #f9fafb;
  font-weight: 600;
  color: #374151;
}

.markdown-content :deep(tr:nth-child(even)) {
  background: #f9fafb;
}

.markdown-content :deep(hr) {
  margin: 2em 0;
  border: none;
  border-top: 1px solid #e5e7eb;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 1em 0;
}

.markdown-content :deep(strong) {
  font-weight: 600;
  color: #111827;
}

.markdown-content :deep(em) {
  font-style: italic;
}

.markdown-content :deep(.footnote-ref) {
  margin: 0 0.2em;
  font-size: 0.75em;
  vertical-align: super;
}

.markdown-content :deep(.footnote-ref a) {
  color: #2563eb;
  font-weight: 500;
}

.markdown-content :deep(.footnotes) {
  margin-top: 3em;
  padding-top: 1em;
  border-top: 1px solid #e5e7eb;
  font-size: 0.875em;
  color: #6b7280;
}

.markdown-content :deep(.footnotes-title) {
  font-size: 1em;
  font-weight: 600;
  color: #374151;
  margin-bottom: 0.5em;
}

.markdown-content :deep(.custom-container) {
  margin: 1em 0;
  padding: 1em;
  border-radius: 8px;
  border-left: 4px solid;
}

.markdown-content :deep(.custom-container.tip) {
  background: #ecfdf5;
  border-color: #10b981;
}

.markdown-content :deep(.custom-container.warning) {
  background: #fffbeb;
  border-color: #f59e0b;
}

.markdown-content :deep(.custom-container.info) {
  background: #eff6ff;
  border-color: #3b82f6;
}

.markdown-content :deep(.custom-container.danger) {
  background: #fef2f2;
  border-color: #ef4444;
}

/* Streaming indicator */
.streaming-indicator {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
}

.streaming-indicator .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #666;
  animation: pulse 1.4s infinite ease-in-out;
}

.streaming-indicator .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.streaming-indicator .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes pulse {
  0%, 80%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

/* Scrollbar */
.markdown-content :deep(::-webkit-scrollbar) {
  width: 8px;
  height: 8px;
}

.markdown-content :deep(::-webkit-scrollbar-track) {
  background: #f1f1f1;
  border-radius: 4px;
}

.markdown-content :deep(::-webkit-scrollbar-thumb) {
  background: #c1c1c1;
  border-radius: 4px;
}

.markdown-content :deep(::-webkit-scrollbar-thumb:hover) {
  background: #a1a1a1;
}

/* Code block wrapper styling (applied after mount) */
.markdown-content :deep(.code-block-wrapper .code-block-wrapper) {
  margin: 0;
}
</style>
