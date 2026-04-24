// Web search result for footnotes
export interface WebSearchResult {
  id: string | number
  content: string
  sourceUrl?: string
  title?: string
}

// Markdown renderer options
export interface MarkdownRendererOptions {
  webSearchResults?: WebSearchResult[]
  highlightOptions?: HighlightOptions
  lineNumbers?: boolean
}

// Highlight options
export interface HighlightOptions {
  theme?: string
  languages?: string[]
}

// Chat message interface
export interface ChatMessage {
  id: string
  role: 'user' | 'assistant' | 'system'
  content: string
  timestamp: number
  isStreaming?: boolean
  webSearchResults?: WebSearchResult[]
}

// AST node types for HTML parsing
export interface ASTNode {
  type: 'tag' | 'text' | 'comment' | 'directive'
  name?: string
  data?: string
  children?: ASTNode[]
  attribs?: Record<string, string>
}
