import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

export interface WebSearchResult {
    id: string
    content: string
    sourceUrl: string
}
// HTML 转义
function escapeHtml(html: string): string {
    return html
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;')
}

// 语法高亮函数 - 输出带复制按钮的 <pre><code>
// 使用 HTML 实体，避免 JS Unicode 转义问题
const COPY_ICON = '&#128203;'  // 📋
const CHECK_ICON = '&#9989;'  // ✅


function highlightCode(str: string, lang: string): string {
    const language = lang.trim().toLowerCase()
    const copyBtn = `<button class="copy-btn" onclick="window.copyCodeBlock(this)" title="复制代码">${COPY_ICON}</button>`

    if (str && hljs.getLanguage(language)) {
        const codeContent = hljs.highlight(str, {language, ignoreIllegals: true}).value
        return `<pre class="code-block" data-lang="${language}">${copyBtn}<code>${codeContent}</code></pre>`
    } else {
        return `<pre class="code-block" data-lang="${language || 'text'}">${copyBtn}<code>${escapeHtml(str)}</code></pre>`
    }
}

// 全局复制函数 - 通过 pre 元素内的 code 文本复制
if (typeof window !== 'undefined') {
    ;(window as any).copyCodeBlock = async (btn: HTMLElement): Promise<void> => {
        const pre = btn.closest('pre')
        if (!pre) return
        const code = pre.querySelector('code')
        if (!code) return
        const text = code.innerText || code.textContent || ''
        try {
            await navigator.clipboard.writeText(text)
            btn.innerHTML = CHECK_ICON
            btn.style.color = '#1a7f37'
            setTimeout(() => {
                btn.innerHTML = COPY_ICON
                btn.style.color = ''
            }, 2000)
        } catch (_err) {
            const textarea = document.createElement('textarea')
            textarea.value = text
            textarea.style.position = 'fixed'
            textarea.style.opacity = '0'
            document.body.appendChild(textarea)
            textarea.select()
            document.execCommand('copy')
            document.body.removeChild(textarea)
        }
    }
}

export interface CodeBlockData {
    lang: string
    code: string
}

// 解析代码块 HTML，提取语言和代码
export function parseCodeBlockHtml(html: string): { html: string, codeBlocks: CodeBlockData[] } {
    const codeBlocks: CodeBlockData[] = []

    // 匹配所有 <pre class="code-block" data-lang="...">...</pre>
    const processedHtml = html.replace(
        /<pre class="code-block" data-lang="([^"]*)">([\s\S]*?)<\/pre>/g,
        (match, lang, codeContent) => {
            // 提取 code 标签内的内容
            const codeMatch = codeContent.match(/<code>([\s\S]*?)<\/code>/)
            if (codeMatch) {
                // 解码 HTML 实体
                const code = codeMatch[1]
                    .replace(/&amp;/g, '&')
                    .replace(/&lt;/g, '<')
                    .replace(/&gt;/g, '>')
                    .replace(/&quot;/g, '"')
                    .replace(/&#39;/g, "'")
                    .replace(/&#x27;/g, "'")

                const blockId = `cb_${codeBlocks.length}`
                codeBlocks.push({lang, code})
                return `<div class="code-block-placeholder" data-block-id="${blockId}"></div>`
            }
            return match
        }
    )

    return {html: processedHtml, codeBlocks}
}

// 替换占位符为 Vue 组件标签
export function replaceCodeBlockPlaceholders(
    html: string,
    codeBlocks: CodeBlockData[]
): string {
    return html.replace(
        /<div class="code-block-placeholder" data-block-id="cb_(\d+)"><\/div>/g,
        (match, index) => {
            const block = codeBlocks[parseInt(index)]
            if (block) {
                // 转义代码用于属性
                const encodedCode = encodeURIComponent(block.code)
                return `<code-block-placeholder lang="${block.lang}" :code="decodeURIComponent('${encodedCode}')"></code-block-placeholder>`
            }
            return match
        }
    )
}

// ========== MarkdownRenderer 类 ==========

export class MarkdownRenderer {
    private readonly md: MarkdownIt

    constructor() {
        this.md = new MarkdownIt({
            html: true,
            breaks: true,
            langPrefix: 'language-',
            typographer: true,
            highlight: highlightCode
        })

        // 应用自定义链接规则 - 新标签打开
        this.applyLinkOpenRules()
    }

    // 链接安全处理
    private applyLinkOpenRules(): void {
        this.md.renderer.rules.link_open = (tokens, idx) => {
            tokens[idx].attrSet('target', '_blank')
            tokens[idx].attrSet('rel', 'noopener noreferrer')
            return this.md.renderer.renderToken(tokens, idx, {})
        }
    }

    render(content: string, additionalWebSearchResults: WebSearchResult[] = []): string {
        if (!content) return ''

        let contentToRender = content

        // 添加脚注
        if (additionalWebSearchResults?.length) {
            const footnotes = this.convertToMarkdownFootnotes(additionalWebSearchResults)
            contentToRender = `${contentToRender}${footnotes}`
        }

        return this.md.render(contentToRender)
    }

    private convertToMarkdownFootnotes(results: WebSearchResult[]): string {
        if (!results?.length) return ''

        const footnotes = results.map(({id, content, sourceUrl}) => {
            const truncatedContent = content?.trim().substring(0, 200) || ''
            return `[^${id}]: [${truncatedContent}](${sourceUrl || '#'})`
        })

        return `\n\n${footnotes.join('\n\n')}\n\n`
    }
}

// 创建默认实例
export const renderer = new MarkdownRenderer()
