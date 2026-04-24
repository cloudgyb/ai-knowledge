import MarkdownIt from 'markdown-it'
import type Token from 'markdown-it/lib/token.mjs'
import hljs from 'highlight.js'
import markdownItFootnote from 'markdown-it-footnote'
import markdownItContainer from 'markdown-it-container'
import type { WebSearchResult } from './types'

// Language display names mapping
export const languageMap: Record<string, string> = {
  javascript: 'JavaScript',
  typescript: 'TypeScript',
  python: 'Python',
  java: 'Java',
  cpp: 'C++',
  csharp: 'C#',
  go: 'Go',
  rust: 'Rust',
  ruby: 'Ruby',
  php: 'PHP',
  swift: 'Swift',
  kotlin: 'Kotlin',
  scala: 'Scala',
  html: 'HTML',
  css: 'CSS',
  sql: 'SQL',
  bash: 'Bash',
  shell: 'Shell',
  powershell: 'PowerShell',
  json: 'JSON',
  xml: 'XML',
  yaml: 'YAML',
  markdown: 'Markdown',
  plaintext: 'Plain Text',
  text: 'Plain Text',
}

// Escape HTML special characters
export function escapeHtml(str: string): string {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;')
}

// Truncate content for footnotes
export function truncateContent(content: string, maxLength = 100): string {
  if (content.length <= maxLength) return content
  return content.slice(0, maxLength) + '...'
}

// Convert search results to markdown footnotes
export function convertToMarkdownFootnotes(results: WebSearchResult[]): string {
  if (!results?.length) return ''
  
  const footnotes = results.map(({ id, content, sourceUrl }) => {
    const truncatedContent = truncateContent(content?.trim() || '')
    return `[^${id}]: [${truncatedContent}](${sourceUrl || '#'})`
  })
  
  return `\n\n${footnotes.join('\n\n')}\n\n`
}

// Highlight code using highlight.js - Just return placeholder with language info
function highlightCode(str: string, lang: string): string {
  const language = lang?.trim().toLowerCase() || 'plaintext'
  // Don't highlight here, just mark the code block with language
  // The actual highlighting will be done by CodeBlock component
  return `<pre class="code-block-placeholder" data-lang="${language}"><code>${escapeHtml(str)}</code></pre>`
}

// Configure link open rules (security)
function configureLinkOpenRules(md: MarkdownIt): void {
  md.renderer.rules.link_open = (tokens: Token[], id: number) => {
    const token = tokens[id]
    token.attrSet('target', '_blank')
    token.attrSet('rel', 'noopener noreferrer')
    return md.renderer.renderToken(tokens, id, {})
  }
}

// Configure footnote rules
function configureFootnoteRules(md: MarkdownIt, results: WebSearchResult[] = []): void {
  md.renderer.rules.footnote_ref = (tokens: Token[], id: number) => {
    const n = Number(tokens[id].meta.id + 1).toString()
    const data = results?.find((t) => t.id === n)
    
    if (data?.sourceUrl) {
      return `<sup class="footnote-ref"><a href="${data.sourceUrl}" target="_blank" rel="noopener noreferrer">[${n}]</a></sup>`
    }
    return `<sup class="footnote-ref">[${n}]</sup>`
  }

  md.renderer.rules.footnote_block_open = () => `
    <section class="footnotes">
    <h2 class="footnotes-title">参考文献</h2>
    <ol class="footnotes-list">
  `

  md.renderer.rules.footnote_block_close = () => `</ol></section>`
}

// Create and configure markdown-it instance
export function createMarkdownRenderer(): MarkdownIt {
  const md = new MarkdownIt({
    html: true,
    breaks: true,
    langPrefix: 'language-',
    typographer: true,
    highlight: highlightCode
  })

  // Use plugins
  md.use(markdownItFootnote)

  // Configure custom containers
  md.use(markdownItContainer, 'tip', {
    renderToken: (tokens: Token[], idx: number) => {
      if (tokens[idx].nesting === 1) {
        const title = tokens[idx].info.trim().slice(3).trim() || '提示'
        return `<div class="custom-container tip"><p class="custom-container-title">💡 ${title}</p>\n`
      }
      return '</div>\n'
    }
  })

  md.use(markdownItContainer, 'warning', {
    renderToken: (tokens: Token[], idx: number) => {
      if (tokens[idx].nesting === 1) {
        const title = tokens[idx].info.trim().slice(7).trim() || '警告'
        return `<div class="custom-container warning"><p class="custom-container-title">⚠️ ${title}</p>\n`
      }
      return '</div>\n'
    }
  })

  md.use(markdownItContainer, 'info', {
    renderToken: (tokens: Token[], idx: number) => {
      if (tokens[idx].nesting === 1) {
        const title = tokens[idx].info.trim().slice(4).trim() || '信息'
        return `<div class="custom-container info"><p class="custom-container-title">ℹ️ ${title}</p>\n`
      }
      return '</div>\n'
    }
  })

  md.use(markdownItContainer, 'danger', {
    renderToken: (tokens: Token[], idx: number) => {
      if (tokens[idx].nesting === 1) {
        const title = tokens[idx].info.trim().slice(6).trim() || '危险'
        return `<div class="custom-container danger"><p class="custom-container-title">🚨 ${title}</p>\n`
      }
      return '</div>\n'
    }
  })

  // Apply security rules
  configureLinkOpenRules(md)

  return md
}

// Preprocess streaming markdown content to handle incomplete syntax
function preprocessStreamingContent(content: string): string {
  if (!content) return content
  
  let processed = content
  
  // Ensure proper line breaks for better parsing
  // Add newline at the end if missing to help MarkdownIt parse properly
  if (!processed.endsWith('\n')) {
    processed += '\n'
  }
  
  // Handle incomplete code blocks: if there's an opening ``` without closing
  const codeBlockMatches = processed.match(/```/g)
  if (codeBlockMatches && codeBlockMatches.length % 2 !== 0) {
    // Odd number of code block markers, add closing marker
    processed += '```\n'
  }
  
  // Handle incomplete headings by ensuring they have content or are treated as text
  // This regex finds # at the end of a line without content after it
  processed = processed.replace(/^(#{1,6})\s*$/gm, '$1 &nbsp;')
  
  // Handle incomplete list items - add non-breaking space to make them valid
  processed = processed.replace(/^([\-\*+])\s*$/gm, '$1 &nbsp;')
  
  // Handle incomplete bold/italic markers
  // If there's an odd number of ** or *, we need to handle it
  const doubleAsteriskCount = (processed.match(/\*\*/g) || []).length
  if (doubleAsteriskCount % 2 !== 0) {
    // Remove the last unpaired **
    processed = processed.replace(/\*\*([^*]*)$/, '$1')
  } else {
    // Check for single *
    const singleAsteriskPattern = /(?<!\*)\*(?!\*)/g
    const singleMatches = processed.match(singleAsteriskPattern)
    if (singleMatches && singleMatches.length % 2 !== 0) {
      // Remove the last unpaired *
      processed = processed.replace(/(?<!\*)\*(?!\*)([^*]*)$/, '$1')
    }
  }
  
  // Handle incomplete links [text](url
  const incompleteLinks = processed.match(/\[[^\]]*\]\([^)]*$/g)
  if (incompleteLinks) {
    processed = processed.replace(/(\[[^\]]*\]\([^)]*)$/, '$1)')
  }
  
  // Handle incomplete images ![alt](url
  const incompleteImages = processed.match(/!\[[^\]]*\]\([^)]*$/g)
  if (incompleteImages) {
    processed = processed.replace(/(!\[[^\]]*\]\([^)]*)$/, '$1)')
  }
  
  return processed
}

// Main render function
export function renderMarkdown(
  content: string,
  renderer: MarkdownIt,
  webSearchResults: WebSearchResult[] = [],
  isStreaming: boolean = false
): string {
  let contentToRender = content

  if (!contentToRender) {
    return ''
  }

  if (typeof contentToRender !== 'string') {
    contentToRender = String(contentToRender)
  }

  // Preprocess content for streaming to handle incomplete syntax
  if (isStreaming) {
    contentToRender = preprocessStreamingContent(contentToRender)
  }

  // Add footnotes if search results exist
  if (webSearchResults?.length) {
    const footnotes = convertToMarkdownFootnotes(webSearchResults)
    contentToRender = `${contentToRender}${footnotes}`
    configureFootnoteRules(renderer, webSearchResults)
  }

  return renderer.render(contentToRender)
}

// Get language display name
export function getLanguageDisplayName(lang: string): string {
  const normalizedLang = lang?.trim().toLowerCase() || 'plaintext'
  return languageMap[normalizedLang] || normalizedLang
}
