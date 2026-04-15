import { computed, reactive, ref, watch } from 'vue'
import { inBrowser } from 'vitepress'

export type ArticleStatus = 'draft' | 'pending' | 'published' | 'hidden' | 'offline'
export type ArticleHistoryType =
  | 'created'
  | 'submitted'
  | 'published'
  | 'hidden'
  | 'offline'
  | 'restored'
export type SiteEventType = 'system' | 'release' | 'content'

export interface ArticleHistoryEntry {
  id: string
  type: ArticleHistoryType
  at: string
  note: string
}

export interface BlogArticle {
  id: string
  title: string
  slug: string
  summary: string
  content: string
  coverImage: string
  bannerImage: string
  backgroundImage: string
  categoryId: string
  tagIds: string[]
  status: ArticleStatus
  createdAt: string
  updatedAt: string
  publishedAt: string | null
  draftSavedAt: string | null
  history: ArticleHistoryEntry[]
}

export interface BlogCategory {
  id: string
  name: string
  slug: string
  parentId: string | null
  description: string
  bannerImage: string
  backgroundImage: string
}

export interface BlogTag {
  id: string
  name: string
  slug: string
  color: string
}

export interface BackgroundAsset {
  id: string
  name: string
  imageUrl: string
  overlay: string
  accentColor: string
  motionClass: string
}

export interface SiteSettings {
  siteTitle: string
  tagline: string
  activeBackgroundId: string
  backgrounds: BackgroundAsset[]
}

export interface SiteTimelineEvent {
  id: string
  type: SiteEventType
  title: string
  description: string
  at: string
}

interface BlogStudioState {
  articles: BlogArticle[]
  categories: BlogCategory[]
  tags: BlogTag[]
  settings: SiteSettings
  siteTimeline: SiteTimelineEvent[]
}

interface TimelineEntry {
  id: string
  kind: 'article' | 'site'
  at: string
  label: string
  title: string
  description: string
  articleId?: string
  categoryId?: string
  status?: ArticleStatus
}

const STORAGE_KEY = 'exitzero-blog-studio-v2'
const SAVE_DELAY = 650

export const ARTICLE_STATUS_META: Record<ArticleStatus, { label: string; tone: string }> = {
  draft: { label: '草稿箱', tone: 'neutral' },
  pending: { label: '待发布', tone: 'warning' },
  published: { label: '已发布', tone: 'success' },
  hidden: { label: '已隐藏', tone: 'muted' },
  offline: { label: '已下架', tone: 'danger' }
}

const allowedTransitions: Record<ArticleStatus, ArticleStatus[]> = {
  draft: ['pending'],
  pending: ['draft', 'published'],
  published: ['hidden', 'offline'],
  hidden: ['published', 'offline'],
  offline: ['draft', 'published']
}

function createId(prefix: string): string {
  return `${prefix}-${Math.random().toString(36).slice(2, 8)}${Date.now().toString(36).slice(-4)}`
}

function nowIso(): string {
  return new Date().toISOString()
}

export function formatDateTime(value: string | null): string {
  if (!value) {
    return '未设置'
  }

  return new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(value))
}

export function slugify(input: string): string {
  const slug = input
    .trim()
    .toLowerCase()
    .replace(/[^\w\u4e00-\u9fa5-]+/g, '-')
    .replace(/-+/g, '-')
    .replace(/^-|-$/g, '')

  return slug || `untitled-${Date.now().toString(36)}`
}

function createArticleHistory(type: ArticleHistoryType, note: string, at = nowIso()): ArticleHistoryEntry {
  return {
    id: createId('history'),
    type,
    at,
    note
  }
}

function createEmptyArticle(): BlogArticle {
  const createdAt = nowIso()
  return {
    id: createId('article'),
    title: '未命名文章',
    slug: `untitled-${Date.now().toString(36)}`,
    summary: '一句话概括这篇内容，让首页卡片与 SEO 摘要更清晰。',
    content: `# 新文章\n\n从这里开始写作。\n\n## 这一版支持什么\n- Markdown 实时预览\n- 自动保存草稿\n- 封面图 / Banner / 背景图\n- 状态机流转\n`,
    coverImage: '',
    bannerImage: '',
    backgroundImage: '',
    categoryId: 'cat-blog-building',
    tagIds: ['tag-vitepress'],
    status: 'draft',
    createdAt,
    updatedAt: createdAt,
    publishedAt: null,
    draftSavedAt: createdAt,
    history: [createArticleHistory('created', '创建文章草稿', createdAt)]
  }
}

function createDefaultState(): BlogStudioState {
  return {
    articles: [
      {
        id: 'article-blog-studio',
        title: '从静态站到博客工作台',
        slug: 'blog-studio-kickoff',
        summary: '把 VitePress 站点升级成具备发布流、时间轴和视觉配置的个人博客控制台。',
        content: `# 从静态站到博客工作台\n\n这次改造的目标，是让原来的文档博客具备真正的内容运营能力。\n\n## 已完成的能力\n- Markdown 编辑与预览\n- 草稿自动保存\n- 封面图、Banner 与背景图字段\n- 状态流转：草稿 -> 待发布 -> 已发布 -> 隐藏 / 下架\n- 全局动态背景配置\n`,
        coverImage: '/images/fig1.png',
        bannerImage: '',
        backgroundImage: '',
        categoryId: 'cat-blog-building',
        tagIds: ['tag-vitepress', 'tag-vue'],
        status: 'published',
        createdAt: '2026-04-02T08:30:00.000Z',
        updatedAt: '2026-04-10T15:20:00.000Z',
        publishedAt: '2026-04-10T15:20:00.000Z',
        draftSavedAt: '2026-04-09T13:10:00.000Z',
        history: [
          createArticleHistory('created', '启动站点升级计划', '2026-04-02T08:30:00.000Z'),
          createArticleHistory('submitted', '提交到待发布队列', '2026-04-09T09:00:00.000Z'),
          createArticleHistory('published', '正式发布到博客首页', '2026-04-10T15:20:00.000Z')
        ]
      },
      {
        id: 'article-ai-log',
        title: 'AI 自动化内容流的第一篇实验',
        slug: 'ai-content-automation-log',
        summary: '记录如何把标签体系、时间线和内容运营串起来，让文章发布更像产品迭代。',
        content: `# AI 自动化内容流的第一篇实验\n\n> 文章不仅是内容，更是一次面向时间轴的迭代记录。\n\n## 规划\n1. 先完成分类与标签结构\n2. 再建立发布前审核状态\n3. 最后串联时间轴与首页呈现\n`,
        coverImage: '',
        bannerImage: '',
        backgroundImage: '',
        categoryId: 'cat-ai-practice',
        tagIds: ['tag-ai', 'tag-workflow'],
        status: 'pending',
        createdAt: '2026-04-11T03:10:00.000Z',
        updatedAt: '2026-04-12T12:45:00.000Z',
        publishedAt: null,
        draftSavedAt: '2026-04-12T12:45:00.000Z',
        history: [
          createArticleHistory('created', '新建 AI 自动化专题文章', '2026-04-11T03:10:00.000Z'),
          createArticleHistory('submitted', '进入待发布流程，等待最终检查', '2026-04-12T12:45:00.000Z')
        ]
      }
    ],
    categories: [
      {
        id: 'cat-engineering',
        name: '工程实践',
        slug: 'engineering',
        parentId: null,
        description: '围绕系统设计、部署、架构演进的主分类。',
        bannerImage: '',
        backgroundImage: ''
      },
      {
        id: 'cat-blog-building',
        name: '博客搭建',
        slug: 'blog-building',
        parentId: 'cat-engineering',
        description: '记录博客从视觉、交互到内容工作台的演进。',
        bannerImage: '',
        backgroundImage: ''
      },
      {
        id: 'cat-ai-practice',
        name: 'AI 实战',
        slug: 'ai-practice',
        parentId: 'cat-engineering',
        description: 'LLM、Agent 与自动化内容工作流的实验场。',
        bannerImage: '',
        backgroundImage: ''
      },
      {
        id: 'cat-algorithm',
        name: '算法沉淀',
        slug: 'algorithm',
        parentId: null,
        description: '题解、模板与方法论沉淀。',
        bannerImage: '',
        backgroundImage: ''
      }
    ],
    tags: [
      { id: 'tag-vitepress', name: 'VitePress', slug: 'vitepress', color: '#3a7cff' },
      { id: 'tag-vue', name: 'Vue 3', slug: 'vue-3', color: '#42b883' },
      { id: 'tag-ai', name: 'AI', slug: 'ai', color: '#ff7a59' },
      { id: 'tag-workflow', name: 'Workflow', slug: 'workflow', color: '#8f5cff' }
    ],
    settings: {
      siteTitle: 'ExitZero Studio',
      tagline: '让博客像产品一样持续发布、持续演进。',
      activeBackgroundId: 'bg-aurora',
      backgrounds: [
        {
          id: 'bg-aurora',
          name: 'Aurora Flow',
          imageUrl: '',
          overlay: 'radial-gradient(circle at 20% 20%, rgba(58,124,255,0.45), transparent 42%), radial-gradient(circle at 80% 10%, rgba(255,122,89,0.35), transparent 36%), linear-gradient(135deg, rgba(8,18,34,0.92), rgba(18,31,53,0.8))',
          accentColor: '#3a7cff',
          motionClass: 'aurora'
        },
        {
          id: 'bg-grid',
          name: 'Grid Dawn',
          imageUrl: '/images/fig1.png',
          overlay: 'linear-gradient(120deg, rgba(7,20,37,0.9), rgba(16,44,67,0.78))',
          accentColor: '#41d1ff',
          motionClass: 'grid'
        },
        {
          id: 'bg-ember',
          name: 'Ember Notes',
          imageUrl: '',
          overlay: 'radial-gradient(circle at 25% 15%, rgba(255,181,71,0.25), transparent 30%), radial-gradient(circle at 82% 25%, rgba(255,122,89,0.32), transparent 28%), linear-gradient(135deg, rgba(31,20,15,0.92), rgba(63,31,22,0.78))',
          accentColor: '#ff7a59',
          motionClass: 'ember'
        }
      ]
    },
    siteTimeline: [
      {
        id: 'site-1',
        type: 'system',
        title: '站点启动重构',
        description: '从纯文档博客转向内容工作台，开始补齐发布流与时间轴能力。',
        at: '2026-04-01T08:00:00.000Z'
      },
      {
        id: 'site-2',
        type: 'content',
        title: '分类与标签层完成',
        description: '支持多级分类与多对多标签映射，为后续首页聚合与检索打基础。',
        at: '2026-04-08T10:20:00.000Z'
      },
      {
        id: 'site-3',
        type: 'release',
        title: '发布工作台上线',
        description: '编辑器、自动保存、封面上传和状态机在同一页联动。',
        at: '2026-04-10T14:00:00.000Z'
      }
    ]
  }
}

function cloneState(state: BlogStudioState): BlogStudioState {
  return JSON.parse(JSON.stringify(state)) as BlogStudioState
}

function normalizeState(raw: Partial<BlogStudioState> | null | undefined): BlogStudioState {
  const defaults = createDefaultState()

  if (!raw) {
    return defaults
  }

  return {
    articles: Array.isArray(raw.articles)
      ? raw.articles.map((item) => ({
          ...createEmptyArticle(),
          ...item,
          history: Array.isArray(item.history) ? item.history : []
        }))
      : defaults.articles,
    categories: Array.isArray(raw.categories)
      ? raw.categories.map((item) => ({
          id: item.id || createId('cat'),
          name: item.name || '未命名分类',
          slug: item.slug || slugify(item.name || 'category'),
          parentId: item.parentId ?? null,
          description: item.description || '',
          bannerImage: item.bannerImage || '',
          backgroundImage: item.backgroundImage || ''
        }))
      : defaults.categories,
    tags: Array.isArray(raw.tags)
      ? raw.tags.map((item) => ({
          id: item.id || createId('tag'),
          name: item.name || '未命名标签',
          slug: item.slug || slugify(item.name || 'tag'),
          color: item.color || '#3a7cff'
        }))
      : defaults.tags,
    settings: {
      siteTitle: raw.settings?.siteTitle || defaults.settings.siteTitle,
      tagline: raw.settings?.tagline || defaults.settings.tagline,
      activeBackgroundId: raw.settings?.activeBackgroundId || defaults.settings.activeBackgroundId,
      backgrounds:
        raw.settings?.backgrounds?.map((item) => ({
          id: item.id || createId('bg'),
          name: item.name || '未命名背景',
          imageUrl: item.imageUrl || '',
          overlay: item.overlay || defaults.settings.backgrounds[0].overlay,
          accentColor: item.accentColor || '#3a7cff',
          motionClass: item.motionClass || 'aurora'
        })) || defaults.settings.backgrounds
    },
    siteTimeline: Array.isArray(raw.siteTimeline)
      ? raw.siteTimeline.map((item) => ({
          id: item.id || createId('site'),
          type: item.type || 'system',
          title: item.title || '系统事件',
          description: item.description || '',
          at: item.at || nowIso()
        }))
      : defaults.siteTimeline
  }
}

const state = reactive<BlogStudioState>(createDefaultState())
const hydrationReady = ref(false)
const autosaveStatus = ref<'idle' | 'saving' | 'saved'>('idle')
const lastSavedAt = ref<string | null>(null)

let saveTimer: ReturnType<typeof setTimeout> | null = null
let watchInitialized = false

function replaceState(nextState: BlogStudioState) {
  state.articles.splice(0, state.articles.length, ...nextState.articles)
  state.categories.splice(0, state.categories.length, ...nextState.categories)
  state.tags.splice(0, state.tags.length, ...nextState.tags)
  state.siteTimeline.splice(0, state.siteTimeline.length, ...nextState.siteTimeline)
  state.settings.siteTitle = nextState.settings.siteTitle
  state.settings.tagline = nextState.settings.tagline
  state.settings.activeBackgroundId = nextState.settings.activeBackgroundId
  state.settings.backgrounds.splice(0, state.settings.backgrounds.length, ...nextState.settings.backgrounds)
}

function schedulePersist() {
  if (!inBrowser || !hydrationReady.value) {
    return
  }

  autosaveStatus.value = 'saving'

  if (saveTimer) {
    clearTimeout(saveTimer)
  }

  saveTimer = window.setTimeout(() => {
    const snapshot = cloneState(state)
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(snapshot))
    autosaveStatus.value = 'saved'
    lastSavedAt.value = nowIso()
  }, SAVE_DELAY)
}

function initPersistence() {
  if (watchInitialized) {
    return
  }

  watch(state, schedulePersist, { deep: true })
  watchInitialized = true
}

function hydrateState() {
  initPersistence()

  if (!inBrowser || hydrationReady.value) {
    return
  }

  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (raw) {
    try {
      replaceState(normalizeState(JSON.parse(raw)))
    } catch {
      replaceState(createDefaultState())
    }
  }

  hydrationReady.value = true
}

function pushSiteEvent(type: SiteEventType, title: string, description: string) {
  state.siteTimeline.unshift({
    id: createId('site'),
    type,
    title,
    description,
    at: nowIso()
  })
}

function getArticleById(articleId: string): BlogArticle | undefined {
  return state.articles.find((article) => article.id === articleId)
}

function getCategoryById(categoryId: string | null): BlogCategory | undefined {
  return state.categories.find((category) => category.id === categoryId)
}

function createArticle() {
  const article = createEmptyArticle()
  state.articles.unshift(article)
  pushSiteEvent('content', '创建新草稿', `《${article.title}》进入草稿箱。`)
  return article
}

function updateArticle(articleId: string, patch: Partial<BlogArticle>) {
  const article = getArticleById(articleId)
  if (!article) {
    return
  }

  Object.assign(article, patch)
  article.updatedAt = nowIso()
  article.draftSavedAt = nowIso()
}

function canTransition(currentStatus: ArticleStatus, nextStatus: ArticleStatus) {
  return allowedTransitions[currentStatus].includes(nextStatus)
}

function getAvailableTransitions(status: ArticleStatus): ArticleStatus[] {
  return allowedTransitions[status]
}

function transitionArticle(articleId: string, nextStatus: ArticleStatus) {
  const article = getArticleById(articleId)
  if (!article) {
    return { ok: false, message: '文章不存在。' }
  }

  if (!canTransition(article.status, nextStatus)) {
    return { ok: false, message: `当前状态不允许流转到 ${ARTICLE_STATUS_META[nextStatus].label}。` }
  }

  const historyMap: Record<ArticleStatus, ArticleHistoryType> = {
    draft: 'restored',
    pending: 'submitted',
    published: 'published',
    hidden: 'hidden',
    offline: 'offline'
  }

  const actionText: Record<ArticleStatus, string> = {
    draft: '退回草稿箱',
    pending: '提交待发布',
    published: '正式发布',
    hidden: '已隐藏内容',
    offline: '已下架内容'
  }

  const eventTime = nowIso()
  article.status = nextStatus
  article.updatedAt = eventTime

  if (nextStatus === 'published') {
    article.publishedAt = eventTime
  }

  article.history.unshift(createArticleHistory(historyMap[nextStatus], actionText[nextStatus], eventTime))
  pushSiteEvent(
    nextStatus === 'published' ? 'release' : 'content',
    `${ARTICLE_STATUS_META[nextStatus].label} · ${article.title}`,
    `文章《${article.title}》已完成 ${actionText[nextStatus]}。`
  )

  return { ok: true, message: actionText[nextStatus] }
}

function upsertCategory(category: Partial<BlogCategory>) {
  const existing = category.id ? getCategoryById(category.id) : undefined

  if (existing) {
    existing.name = category.name || existing.name
    existing.slug = category.slug || slugify(existing.name)
    existing.parentId = category.parentId ?? existing.parentId
    existing.description = category.description || ''
    existing.bannerImage = category.bannerImage || ''
    existing.backgroundImage = category.backgroundImage || ''
    pushSiteEvent('system', '更新分类配置', `分类「${existing.name}」已更新层级或视觉字段。`)
    return existing
  }

  const nextCategory: BlogCategory = {
    id: createId('cat'),
    name: category.name || '未命名分类',
    slug: category.slug || slugify(category.name || 'category'),
    parentId: category.parentId || null,
    description: category.description || '',
    bannerImage: category.bannerImage || '',
    backgroundImage: category.backgroundImage || ''
  }

  state.categories.push(nextCategory)
  pushSiteEvent('system', '新增分类', `新增分类「${nextCategory.name}」，支持多级内容管理。`)
  return nextCategory
}

function createTag(tag: Partial<BlogTag>) {
  const nextTag: BlogTag = {
    id: createId('tag'),
    name: tag.name || '未命名标签',
    slug: tag.slug || slugify(tag.name || 'tag'),
    color: tag.color || '#3a7cff'
  }

  state.tags.unshift(nextTag)
  pushSiteEvent('system', '新增标签', `标签「${nextTag.name}」已加入内容维度。`)
  return nextTag
}

function createBackground(background: Partial<BackgroundAsset>) {
  const nextBackground: BackgroundAsset = {
    id: createId('bg'),
    name: background.name || '自定义背景',
    imageUrl: background.imageUrl || '',
    overlay:
      background.overlay ||
      'linear-gradient(135deg, rgba(11,23,41,0.9), rgba(25,43,69,0.78))',
    accentColor: background.accentColor || '#3a7cff',
    motionClass: background.motionClass || 'aurora'
  }

  state.settings.backgrounds.unshift(nextBackground)
  state.settings.activeBackgroundId = nextBackground.id
  pushSiteEvent('system', '新增全局背景', `背景「${nextBackground.name}」已设为当前站点视觉。`)
  return nextBackground
}

function setActiveBackground(backgroundId: string) {
  state.settings.activeBackgroundId = backgroundId
}

function getCategoryPath(categoryId: string | null): string {
  const segments: string[] = []
  let current = getCategoryById(categoryId)

  while (current) {
    segments.unshift(current.name)
    current = current.parentId ? getCategoryById(current.parentId) : undefined
  }

  return segments.join(' / ') || '未分类'
}

function flattenCategories(
  categories: BlogCategory[],
  parentId: string | null = null,
  depth = 0
): Array<BlogCategory & { depth: number }> {
  return categories
    .filter((item) => item.parentId === parentId)
    .sort((left, right) => left.name.localeCompare(right.name, 'zh-CN'))
    .flatMap((category) => [
      { ...category, depth },
      ...flattenCategories(categories, category.id, depth + 1)
    ])
}

function mapHistoryLabel(type: ArticleHistoryType): string {
  switch (type) {
    case 'created':
      return '草稿创建'
    case 'submitted':
      return '进入待发布'
    case 'published':
      return '正式发布'
    case 'hidden':
      return '内容隐藏'
    case 'offline':
      return '内容下架'
    case 'restored':
      return '恢复草稿'
    default:
      return '内容更新'
  }
}

export function useBlogStudioStore() {
  hydrateState()

  const articleMap = computed(() =>
    Object.fromEntries(state.articles.map((article) => [article.id, article]))
  )

  const tagMap = computed(() => Object.fromEntries(state.tags.map((tag) => [tag.id, tag])))

  const activeBackground = computed(
    () =>
      state.settings.backgrounds.find(
        (background) => background.id === state.settings.activeBackgroundId
      ) || state.settings.backgrounds[0]
  )

  const articles = computed(() =>
    [...state.articles].sort(
      (left, right) =>
        new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime()
    )
  )

  const publishedArticles = computed(() =>
    [...state.articles]
      .filter((article) => article.status === 'published')
      .sort((left, right) => {
        const rightTime = right.publishedAt ? new Date(right.publishedAt).getTime() : 0
        const leftTime = left.publishedAt ? new Date(left.publishedAt).getTime() : 0
        return rightTime - leftTime
      })
  )

  const flattenedCategoryList = computed(() => flattenCategories(state.categories))

  const metrics = computed(() => ({
    total: state.articles.length,
    pending: state.articles.filter((article) => article.status === 'pending').length,
    published: state.articles.filter((article) => article.status === 'published').length,
    hidden: state.articles.filter((article) => article.status === 'hidden').length
  }))

  const timeline = computed<TimelineEntry[]>(() => {
    const siteEntries: TimelineEntry[] = state.siteTimeline.map((item) => ({
      id: item.id,
      kind: 'site',
      at: item.at,
      label: item.type === 'release' ? '站点发布' : item.type === 'content' ? '内容更新' : '系统演进',
      title: item.title,
      description: item.description
    }))

    const articleEntries: TimelineEntry[] = state.articles.flatMap((article) =>
      article.history.map((entry) => ({
        id: entry.id,
        kind: 'article',
        at: entry.at,
        label: mapHistoryLabel(entry.type),
        title: article.title,
        description: entry.note,
        articleId: article.id,
        categoryId: article.categoryId,
        status: article.status
      }))
    )

    return [...siteEntries, ...articleEntries].sort(
      (left, right) => new Date(right.at).getTime() - new Date(left.at).getTime()
    )
  })

  return {
    state,
    articleMap,
    articles,
    publishedArticles,
    flattenedCategoryList,
    tagMap,
    metrics,
    timeline,
    autosaveStatus,
    lastSavedAt,
    activeBackground,
    hydrationReady,
    createArticle,
    updateArticle,
    transitionArticle,
    upsertCategory,
    createTag,
    createBackground,
    setActiveBackground,
    getCategoryById,
    getCategoryPath,
    getArticleById,
    getAvailableTransitions
  }
}
