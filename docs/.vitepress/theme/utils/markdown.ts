function escapeHtml(input: string): string {
  return input
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;')
}

function renderInline(input: string): string {
  const codeTokens: string[] = []

  const withPlaceholders = escapeHtml(input)
    .replace(/`([^`]+)`/g, (_, code: string) => {
      const token = `@@CODE_${codeTokens.length}@@`
      codeTokens.push(`<code>${code}</code>`)
      return token
    })
    .replace(/!\[([^\]]*)\]\(([^)\s]+)\)/g, '<img alt="$1" src="$2" />')
    .replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, '<a href="$2" target="_blank" rel="noreferrer">$1</a>')
    .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
    .replace(/__([^_]+)__/g, '<strong>$1</strong>')
    .replace(/~~([^~]+)~~/g, '<del>$1</del>')
    .replace(/(^|[^\*])\*([^*]+)\*(?!\*)/g, '$1<em>$2</em>')
    .replace(/(^|[^_])_([^_]+)_(?!_)/g, '$1<em>$2</em>')

  return codeTokens.reduce(
    (html, block, index) => html.replace(`@@CODE_${index}@@`, block),
    withPlaceholders
  )
}

function flushParagraph(buffer: string[], html: string[]) {
  if (!buffer.length) {
    return
  }

  html.push(`<p>${renderInline(buffer.join(' '))}</p>`)
  buffer.length = 0
}

function flushList(tag: 'ul' | 'ol' | null, items: string[], html: string[]) {
  if (!tag || !items.length) {
    return
  }

  html.push(`<${tag}>${items.map((item) => `<li>${renderInline(item)}</li>`).join('')}</${tag}>`)
  items.length = 0
}

function flushQuote(buffer: string[], html: string[]) {
  if (!buffer.length) {
    return
  }

  const content = buffer
    .filter((item) => item.trim().length > 0)
    .map((item) => `<p>${renderInline(item)}</p>`)
    .join('')

  html.push(`<blockquote>${content}</blockquote>`)
  buffer.length = 0
}

export function renderMarkdown(markdown: string): string {
  const lines = markdown.replace(/\r\n/g, '\n').split('\n')
  const html: string[] = []
  const paragraphBuffer: string[] = []
  const quoteBuffer: string[] = []
  const listBuffer: string[] = []

  let listTag: 'ul' | 'ol' | null = null
  let inCodeBlock = false
  let codeFence = '```'
  let codeLanguage = ''
  let codeLines: string[] = []

  const flushBlocks = () => {
    flushParagraph(paragraphBuffer, html)
    flushList(listTag, listBuffer, html)
    flushQuote(quoteBuffer, html)
    listTag = null
  }

  for (const line of lines) {
    const trimmed = line.trim()

    if (trimmed.startsWith('```') || trimmed.startsWith('~~~')) {
      if (inCodeBlock) {
        html.push(
          `<pre><code class="language-${escapeHtml(codeLanguage)}">${escapeHtml(codeLines.join('\n'))}</code></pre>`
        )
        inCodeBlock = false
        codeFence = '```'
        codeLanguage = ''
        codeLines = []
      } else {
        flushBlocks()
        inCodeBlock = true
        codeFence = trimmed.startsWith('~~~') ? '~~~' : '```'
        codeLanguage = trimmed.slice(codeFence.length).trim()
      }
      continue
    }

    if (inCodeBlock) {
      codeLines.push(line)
      continue
    }

    if (!trimmed) {
      flushBlocks()
      continue
    }

    const heading = line.match(/^(#{1,6})\s+(.*)$/)
    if (heading) {
      flushBlocks()
      const level = heading[1].length
      html.push(`<h${level}>${renderInline(heading[2])}</h${level}>`)
      continue
    }

    if (/^(-{3,}|\*{3,})$/.test(trimmed)) {
      flushBlocks()
      html.push('<hr />')
      continue
    }

    const quote = line.match(/^>\s?(.*)$/)
    if (quote) {
      flushParagraph(paragraphBuffer, html)
      flushList(listTag, listBuffer, html)
      listTag = null
      quoteBuffer.push(quote[1])
      continue
    }

    const unordered = line.match(/^[-*+]\s+(.*)$/)
    if (unordered) {
      flushParagraph(paragraphBuffer, html)
      flushQuote(quoteBuffer, html)
      if (listTag && listTag !== 'ul') {
        flushList(listTag, listBuffer, html)
      }
      listTag = 'ul'
      listBuffer.push(unordered[1])
      continue
    }

    const ordered = line.match(/^\d+\.\s+(.*)$/)
    if (ordered) {
      flushParagraph(paragraphBuffer, html)
      flushQuote(quoteBuffer, html)
      if (listTag && listTag !== 'ol') {
        flushList(listTag, listBuffer, html)
      }
      listTag = 'ol'
      listBuffer.push(ordered[1])
      continue
    }

    flushQuote(quoteBuffer, html)
    flushList(listTag, listBuffer, html)
    listTag = null
    paragraphBuffer.push(trimmed)
  }

  flushBlocks()

  if (inCodeBlock) {
    html.push(
      `<pre><code class="language-${escapeHtml(codeLanguage)}">${escapeHtml(codeLines.join('\n'))}</code></pre>`
    )
  }

  return html.join('\n')
}

export function estimateReadMinutes(markdown: string): number {
  const text = markdown
    .replace(/```[\s\S]*?```/g, ' ')
    .replace(/!\[[^\]]*]\(([^)\s]+)\)/g, ' ')
    .replace(/\[[^\]]+]\(([^)\s]+)\)/g, ' ')
    .replace(/[#>*`~-]/g, ' ')

  const words = text.split(/\s+/).filter(Boolean).length
  return Math.max(1, Math.ceil(words / 220))
}
