/**
 * 复制文本到剪贴板
 * 优先使用 Clipboard API，失败时回退到 execCommand（兼容非安全上下文/旧浏览器）
 */
export async function copyToClipboard(text: string): Promise<void> {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text)
      return
    }
    throw new Error('Clipboard API 不可用')
  } catch {
    // 回退方案：通过隐藏 textarea + execCommand 复制
    const textarea = document.createElement('textarea')
    textarea.value = text
    textarea.style.position = 'fixed'
    textarea.style.opacity = '0'
    textarea.style.left = '-9999px'
    document.body.appendChild(textarea)
    textarea.focus()
    textarea.select()
    const ok = document.execCommand('copy')
    document.body.removeChild(textarea)
    if (!ok) {
      throw new Error('复制失败')
    }
  }
}
