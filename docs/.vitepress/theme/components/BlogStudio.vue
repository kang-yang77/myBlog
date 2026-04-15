<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import BlogShowcase from './BlogShowcase.vue'
import StatusBadge from './StatusBadge.vue'
import {
  ARTICLE_STATUS_META,
  formatDateTime,
  slugify,
  type ArticleStatus,
  useBlogStudioStore
} from '../composables/useBlogStudio'
import { estimateReadMinutes, renderMarkdown } from '../utils/markdown'

const {
  state,
  articles,
  flattenedCategoryList,
  metrics,
  autosaveStatus,
  lastSavedAt,
  activeBackground,
  createArticle,
  transitionArticle,
  upsertCategory,
  createTag,
  createBackground,
  setActiveBackground,
  getCategoryById,
  getCategoryPath,
  getArticleById,
  getAvailableTransitions
} = useBlogStudioStore()

const selectedArticleId = ref(articles.value[0]?.id || createArticle().id)
const filterStatus = ref<'all' | ArticleStatus>('all')
const keyword = ref('')
const transitionMessage = ref('')

const categoryDraft = reactive({
  id: '',
  name: '',
  slug: '',
  parentId: '',
  description: '',
  bannerImage: '',
  backgroundImage: ''
})

const tagDraft = reactive({
  name: '',
  slug: '',
  color: '#81c7ff'
})

const backgroundDraft = reactive({
  name: '',
  imageUrl: '',
  accentColor: '#81c7ff',
  overlay:
    'linear-gradient(135deg, rgba(8, 18, 31, 0.92), rgba(24, 39, 58, 0.75))',
  motionClass: 'aurora'
})

const currentArticle = computed(() => getArticleById(selectedArticleId.value) || null)
const currentCategory = computed(() =>
  currentArticle.value ? getCategoryById(currentArticle.value.categoryId) : null
)

const filteredArticles = computed(() =>
  articles.value.filter((article) => {
    const matchesStatus = filterStatus.value === 'all' || article.status === filterStatus.value
    const value = keyword.value.trim().toLowerCase()
    const matchesKeyword =
      !value ||
      article.title.toLowerCase().includes(value) ||
      article.summary.toLowerCase().includes(value)

    return matchesStatus && matchesKeyword
  })
)

const previewHtml = computed(() =>
  renderMarkdown(currentArticle.value?.content || '# 请先创建或选择文章')
)

const readMinutes = computed(() =>
  estimateReadMinutes(currentArticle.value?.content || '')
)

const autosaveText = computed(() => {
  if (autosaveStatus.value === 'saving') {
    return '草稿正在同步到本地工作台'
  }

  if (lastSavedAt.value) {
    return `最近一次草稿落点 ${formatDateTime(lastSavedAt.value)}`
  }

  return '编辑后会自动写入本地草稿箱'
})

const categoryOptions = computed(() =>
  flattenedCategoryList.value.map((category) => ({
    ...category,
    label: `${'· '.repeat(category.depth)}${category.name}`
  }))
)

const availableTransitions = computed(() =>
  currentArticle.value ? getAvailableTransitions(currentArticle.value.status) : []
)

const queueMetrics = computed(() =>
  (Object.keys(ARTICLE_STATUS_META) as ArticleStatus[]).map((status) => ({
    status,
    label: ARTICLE_STATUS_META[status].label,
    count: state.articles.filter((article) => article.status === status).length
  }))
)

const storySurfaceStyle = computed(() => {
  const article = currentArticle.value
  const category = currentCategory.value
  const source =
    article?.backgroundImage ||
    article?.bannerImage ||
    article?.coverImage ||
    category?.backgroundImage ||
    category?.bannerImage ||
    activeBackground.value?.imageUrl

  return source
    ? {
        backgroundImage: `linear-gradient(135deg, rgba(6, 12, 21, 0.94), rgba(16, 28, 42, 0.52)), url("${source}")`
      }
    : {
        background:
          'linear-gradient(135deg, rgba(8, 17, 28, 0.96), rgba(18, 33, 49, 0.84))'
      }
})

watch(articles, (nextArticles) => {
  if (!nextArticles.length) {
    const article = createArticle()
    selectedArticleId.value = article.id
    return
  }

  if (!nextArticles.some((article) => article.id === selectedArticleId.value)) {
    selectedArticleId.value = nextArticles[0].id
  }
})

function handleCreateArticle() {
  const article = createArticle()
  selectedArticleId.value = article.id
  transitionMessage.value = ''
}

function generateSlug() {
  if (!currentArticle.value) {
    return
  }

  currentArticle.value.slug = slugify(currentArticle.value.title)
}

function toggleTag(tagId: string) {
  if (!currentArticle.value) {
    return
  }

  if (currentArticle.value.tagIds.includes(tagId)) {
    currentArticle.value.tagIds = currentArticle.value.tagIds.filter((item) => item !== tagId)
    return
  }

  currentArticle.value.tagIds = [...currentArticle.value.tagIds, tagId]
}

function handleTransition(nextStatus: ArticleStatus) {
  if (!currentArticle.value) {
    return
  }

  const result = transitionArticle(currentArticle.value.id, nextStatus)
  transitionMessage.value = result.message
}

function syncCategoryDraft(categoryId: string) {
  const category = getCategoryById(categoryId)

  if (!category) {
    categoryDraft.id = ''
    categoryDraft.name = ''
    categoryDraft.slug = ''
    categoryDraft.parentId = ''
    categoryDraft.description = ''
    categoryDraft.bannerImage = ''
    categoryDraft.backgroundImage = ''
    return
  }

  categoryDraft.id = category.id
  categoryDraft.name = category.name
  categoryDraft.slug = category.slug
  categoryDraft.parentId = category.parentId || ''
  categoryDraft.description = category.description
  categoryDraft.bannerImage = category.bannerImage
  categoryDraft.backgroundImage = category.backgroundImage
}

function saveCategoryDraft() {
  if (!categoryDraft.name.trim()) {
    return
  }

  upsertCategory({
    id: categoryDraft.id || undefined,
    name: categoryDraft.name,
    slug: categoryDraft.slug || slugify(categoryDraft.name),
    parentId: categoryDraft.parentId || null,
    description: categoryDraft.description,
    bannerImage: categoryDraft.bannerImage,
    backgroundImage: categoryDraft.backgroundImage
  })

  syncCategoryDraft('')
}

function handleCreateTag() {
  if (!tagDraft.name.trim()) {
    return
  }

  createTag({
    name: tagDraft.name,
    slug: tagDraft.slug || slugify(tagDraft.name),
    color: tagDraft.color
  })

  tagDraft.name = ''
  tagDraft.slug = ''
  tagDraft.color = '#81c7ff'
}

function handleCreateBackground() {
  createBackground(backgroundDraft)
  backgroundDraft.name = ''
  backgroundDraft.imageUrl = ''
  backgroundDraft.accentColor = '#81c7ff'
}

function updateImageField(field: 'coverImage' | 'bannerImage' | 'backgroundImage', event: Event) {
  const target = event.target as HTMLInputElement | null
  const file = target?.files?.[0]

  if (!file || !currentArticle.value) {
    return
  }

  if (file.size > 800 * 1024) {
    transitionMessage.value = '图片建议控制在 800KB 以内，避免本地草稿存储超限。'
    target.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    currentArticle.value![field] = String(reader.result || '')
  }
  reader.readAsDataURL(file)
  target.value = ''
}

function handleCategorySelect(event: Event) {
  const target = event.target as HTMLSelectElement | null
  syncCategoryDraft(target?.value || '')
}
</script>

<template>
  <div class="atelier-shell">
    <section class="atelier-header">
      <div class="atelier-header__copy">
        <p class="atelier-kicker">Editorial Command Center</p>
        <h2>写作、审核、视觉编排与发布时间，在一个中枢里被组织。</h2>
        <p class="atelier-summary">
          这不是普通 CMS 后台。它更像一张私人出版控制台，让你同时管理内容节奏、页面气质和文章生命周期。
        </p>
      </div>

      <div class="atelier-header__signals">
        <article class="atelier-signal">
          <span>当前草稿同步</span>
          <strong>{{ autosaveText }}</strong>
        </article>
        <article class="atelier-signal">
          <span>已发布内容</span>
          <strong>{{ metrics.published }} 篇</strong>
        </article>
        <article class="atelier-signal">
          <span>当前全局视觉</span>
          <strong>{{ activeBackground?.name }}</strong>
        </article>
      </div>
    </section>

    <section class="atelier-grid">
      <aside class="atelier-column atelier-column--queue">
        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Queue</p>
              <h3>发布队列</h3>
            </div>
            <button class="atelier-button atelier-button--primary" @click="handleCreateArticle">
              新建文章
            </button>
          </div>

          <div class="atelier-metrics">
            <article v-for="item in queueMetrics" :key="item.status" class="atelier-metric">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }}</strong>
            </article>
          </div>

          <div class="atelier-filter">
            <select v-model="filterStatus" class="atelier-input">
              <option value="all">全部状态</option>
              <option v-for="(meta, key) in ARTICLE_STATUS_META" :key="key" :value="key">
                {{ meta.label }}
              </option>
            </select>
            <input v-model="keyword" class="atelier-input" type="search" placeholder="搜索标题或摘要" />
          </div>

          <div class="atelier-list">
            <button
              v-for="article in filteredArticles"
              :key="article.id"
              class="atelier-queue-card"
              :class="{ 'atelier-queue-card--active': selectedArticleId === article.id }"
              @click="selectedArticleId = article.id"
            >
              <div class="atelier-queue-card__meta">
                <StatusBadge :status="article.status" />
                <span>{{ formatDateTime(article.updatedAt) }}</span>
              </div>
              <strong>{{ article.title }}</strong>
              <p>{{ article.summary }}</p>
              <small>{{ getCategoryPath(article.categoryId) }}</small>
            </button>
          </div>
        </section>
      </aside>

      <main v-if="currentArticle" class="atelier-column atelier-column--editor">
        <section class="atelier-storyboard" :style="storySurfaceStyle">
          <div class="atelier-storyboard__meta">
            <StatusBadge :status="currentArticle.status" />
            <span>{{ getCategoryPath(currentArticle.categoryId) }}</span>
            <span>{{ readMinutes }} 分钟阅读</span>
          </div>
          <h3>{{ currentArticle.title }}</h3>
          <p>{{ currentArticle.summary }}</p>
        </section>

        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Story Setup</p>
              <h3>文章设定</h3>
            </div>
            <button class="atelier-button atelier-button--ghost" @click="generateSlug">
              生成 slug
            </button>
          </div>

          <div class="atelier-form-grid atelier-form-grid--wide">
            <input v-model="currentArticle.title" class="atelier-input" placeholder="文章标题" />
            <input v-model="currentArticle.slug" class="atelier-input" placeholder="文章 slug" />
            <textarea
              v-model="currentArticle.summary"
              class="atelier-input atelier-textarea"
              rows="3"
              placeholder="摘要 / SEO 描述"
            />
            <select v-model="currentArticle.categoryId" class="atelier-input">
              <option v-for="category in categoryOptions" :key="category.id" :value="category.id">
                {{ category.label }}
              </option>
            </select>
          </div>

          <div class="atelier-tag-cloud">
            <button
              v-for="tag in state.tags"
              :key="tag.id"
              class="atelier-tag-chip"
              :class="{ 'atelier-tag-chip--active': currentArticle.tagIds.includes(tag.id) }"
              :style="{ '--tag-color': tag.color }"
              @click="toggleTag(tag.id)"
            >
              {{ tag.name }}
            </button>
          </div>
        </section>

        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Writing Deck</p>
              <h3>写作与预览</h3>
            </div>
          </div>

          <div class="atelier-editor-grid">
            <textarea
              v-model="currentArticle.content"
              class="atelier-markdown"
              spellcheck="false"
              placeholder="# 开始写作"
            />
            <div class="atelier-preview vp-doc">
              <div class="atelier-preview__intro">
                <span>{{ currentArticle.slug }}</span>
                <strong>{{ currentArticle.title }}</strong>
              </div>
              <div v-html="previewHtml" />
            </div>
          </div>
        </section>

        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Publish Preview</p>
              <h3>出版橱窗预演</h3>
            </div>
          </div>
          <BlogShowcase compact />
        </section>
      </main>

      <aside v-if="currentArticle" class="atelier-column atelier-column--ops">
        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Release Console</p>
              <h3>状态机与时间记录</h3>
            </div>
          </div>

          <div class="atelier-transition-row">
            <button
              v-for="status in availableTransitions"
              :key="status"
              class="atelier-button atelier-button--primary"
              @click="handleTransition(status)"
            >
              {{
                status === 'pending'
                  ? '提交待发布'
                  : status === 'published'
                    ? '立即发布'
                    : status === 'hidden'
                      ? '隐藏文章'
                      : status === 'offline'
                        ? '下架文章'
                        : '退回草稿'
              }}
            </button>
          </div>

          <p v-if="transitionMessage" class="atelier-inline-message">
            {{ transitionMessage }}
          </p>

          <div class="atelier-history">
            <article v-for="record in currentArticle.history" :key="record.id" class="atelier-history__item">
              <div>
                <strong>{{ record.note }}</strong>
                <p>{{ formatDateTime(record.at) }}</p>
              </div>
              <StatusBadge :status="currentArticle.status" />
            </article>
          </div>
        </section>

        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Media Desk</p>
              <h3>封面与背景</h3>
            </div>
          </div>

          <div class="atelier-upload-grid">
            <label class="atelier-upload">
              <span>封面图</span>
              <input type="file" accept="image/*" @change="updateImageField('coverImage', $event)" />
            </label>
            <label class="atelier-upload">
              <span>Banner</span>
              <input type="file" accept="image/*" @change="updateImageField('bannerImage', $event)" />
            </label>
            <label class="atelier-upload">
              <span>背景图</span>
              <input type="file" accept="image/*" @change="updateImageField('backgroundImage', $event)" />
            </label>
          </div>

          <div class="atelier-form-grid">
            <input v-model="currentArticle.coverImage" class="atelier-input" placeholder="封面图 URL / Base64" />
            <input v-model="currentArticle.bannerImage" class="atelier-input" placeholder="Banner URL / Base64" />
            <input v-model="currentArticle.backgroundImage" class="atelier-input" placeholder="背景图 URL / Base64" />
          </div>

          <div class="atelier-media-preview">
            <figure v-if="currentArticle.coverImage" class="atelier-media-preview__item">
              <img :src="currentArticle.coverImage" alt="cover preview" />
              <figcaption>Cover</figcaption>
            </figure>
            <figure v-if="currentArticle.bannerImage" class="atelier-media-preview__item">
              <img :src="currentArticle.bannerImage" alt="banner preview" />
              <figcaption>Banner</figcaption>
            </figure>
            <figure v-if="currentArticle.backgroundImage" class="atelier-media-preview__item">
              <img :src="currentArticle.backgroundImage" alt="background preview" />
              <figcaption>Background</figcaption>
            </figure>
          </div>
        </section>

        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Taxonomy Lab</p>
              <h3>分类与标签</h3>
            </div>
            <select class="atelier-input atelier-input--compact" @change="handleCategorySelect">
              <option value="">新建分类</option>
              <option v-for="category in categoryOptions" :key="category.id" :value="category.id">
                {{ category.label }}
              </option>
            </select>
          </div>

          <div class="atelier-form-grid">
            <input v-model="categoryDraft.name" class="atelier-input" placeholder="分类名称" />
            <input v-model="categoryDraft.slug" class="atelier-input" placeholder="分类 slug" />
            <select v-model="categoryDraft.parentId" class="atelier-input">
              <option value="">顶级分类</option>
              <option
                v-for="category in categoryOptions.filter((item) => item.id !== categoryDraft.id)"
                :key="category.id"
                :value="category.id"
              >
                {{ category.label }}
              </option>
            </select>
            <textarea
              v-model="categoryDraft.description"
              class="atelier-input atelier-textarea"
              rows="3"
              placeholder="分类描述"
            />
            <input v-model="categoryDraft.bannerImage" class="atelier-input" placeholder="分类 Banner" />
            <input v-model="categoryDraft.backgroundImage" class="atelier-input" placeholder="分类背景图" />
          </div>
          <button class="atelier-button atelier-button--ghost" @click="saveCategoryDraft">
            保存分类
          </button>

          <div class="atelier-divider" />

          <div class="atelier-form-grid atelier-form-grid--two">
            <input v-model="tagDraft.name" class="atelier-input" placeholder="标签名称" />
            <input v-model="tagDraft.slug" class="atelier-input" placeholder="标签 slug" />
            <input v-model="tagDraft.color" class="atelier-input" type="color" />
            <button class="atelier-button atelier-button--ghost" @click="handleCreateTag">
              新增标签
            </button>
          </div>
        </section>

        <section class="atelier-panel">
          <div class="atelier-panel__header">
            <div>
              <p class="atelier-kicker">Visual Deck</p>
              <h3>全局背景主题</h3>
            </div>
          </div>

          <div class="atelier-form-grid atelier-form-grid--two">
            <input v-model="backgroundDraft.name" class="atelier-input" placeholder="背景名称" />
            <input v-model="backgroundDraft.imageUrl" class="atelier-input" placeholder="背景图 URL" />
            <input v-model="backgroundDraft.accentColor" class="atelier-input" type="color" />
            <select v-model="backgroundDraft.motionClass" class="atelier-input">
              <option value="aurora">Aurora</option>
              <option value="grid">Grid</option>
              <option value="ember">Ember</option>
            </select>
            <textarea
              v-model="backgroundDraft.overlay"
              class="atelier-input atelier-textarea"
              rows="3"
              placeholder="CSS 渐变叠层"
            />
          </div>
          <button class="atelier-button atelier-button--ghost" @click="handleCreateBackground">
            新增并启用主题
          </button>

          <div class="atelier-background-list">
            <button
              v-for="background in state.settings.backgrounds"
              :key="background.id"
              class="atelier-background-card"
              :class="{ 'atelier-background-card--active': state.settings.activeBackgroundId === background.id }"
              :style="{ background: background.overlay }"
              @click="setActiveBackground(background.id)"
            >
              <strong>{{ background.name }}</strong>
              <span>{{ background.motionClass }}</span>
            </button>
          </div>
        </section>
      </aside>
    </section>
  </div>
</template>

<style scoped>
.atelier-shell {
  display: grid;
  gap: 24px;
}

.atelier-header,
.atelier-panel,
.atelier-storyboard {
  border: 1px solid rgba(239, 226, 197, 0.14);
  box-shadow: 0 28px 80px rgba(1, 6, 15, 0.28);
  backdrop-filter: blur(18px);
}

.atelier-header {
  display: grid;
  gap: 18px;
  grid-template-columns: minmax(0, 1.06fr) minmax(320px, 0.94fr);
  padding: 30px;
  border-radius: 32px;
  background: linear-gradient(145deg, rgba(9, 18, 30, 0.88), rgba(14, 27, 42, 0.58));
}

.atelier-kicker {
  margin: 0 0 10px;
  color: rgba(244, 214, 164, 0.92);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.atelier-header h2,
.atelier-panel h3,
.atelier-storyboard h3 {
  margin: 0;
  color: #fbf5ea;
  line-height: 1.02;
}

.atelier-header h2 {
  font-size: clamp(34px, 4.4vw, 52px);
}

.atelier-summary {
  margin: 16px 0 0;
  max-width: 720px;
  color: rgba(244, 240, 230, 0.76);
  line-height: 1.9;
}

.atelier-header__signals {
  display: grid;
  gap: 12px;
}

.atelier-signal {
  display: grid;
  gap: 6px;
  padding: 16px 18px;
  border-radius: 20px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(6, 12, 21, 0.42);
}

.atelier-signal span {
  color: rgba(129, 199, 255, 0.84);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.atelier-signal strong {
  color: #fbf5ea;
  font-size: 16px;
  line-height: 1.5;
}

.atelier-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: minmax(300px, 0.72fr) minmax(0, 1.18fr) minmax(320px, 0.86fr);
  align-items: start;
}

.atelier-column {
  display: grid;
  gap: 18px;
}

.atelier-panel {
  display: grid;
  gap: 18px;
  padding: 24px;
  border-radius: 28px;
  background: linear-gradient(145deg, rgba(9, 18, 30, 0.88), rgba(14, 27, 42, 0.58));
}

.atelier-panel__header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
}

.atelier-button {
  border-radius: 999px;
  padding: 11px 16px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  font-size: 11px;
  transition: transform 0.2s ease, background 0.2s ease;
}

.atelier-button:hover {
  transform: translateY(-1px);
}

.atelier-button--primary {
  color: #08131f;
  background: linear-gradient(135deg, #f4d6a4, #ffab7a);
}

.atelier-button--ghost {
  color: #fbf5ea;
  border: 1px solid rgba(239, 226, 197, 0.16);
  background: rgba(7, 14, 24, 0.44);
}

.atelier-metrics {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.atelier-metric {
  display: grid;
  gap: 4px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(6, 12, 21, 0.42);
}

.atelier-metric span {
  color: rgba(244, 240, 230, 0.68);
  font-size: 12px;
}

.atelier-metric strong {
  color: #fbf5ea;
  font-size: 22px;
}

.atelier-filter,
.atelier-form-grid,
.atelier-upload-grid,
.atelier-tag-cloud,
.atelier-media-preview,
.atelier-history,
.atelier-background-list,
.atelier-transition-row {
  display: grid;
  gap: 12px;
}

.atelier-filter {
  grid-template-columns: 130px minmax(0, 1fr);
}

.atelier-list {
  display: grid;
  gap: 12px;
  max-height: 880px;
  overflow: auto;
  padding-right: 4px;
}

.atelier-queue-card {
  display: grid;
  gap: 10px;
  padding: 18px;
  text-align: left;
  border-radius: 22px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(6, 12, 21, 0.42);
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.atelier-queue-card:hover {
  transform: translateY(-1px);
}

.atelier-queue-card--active {
  border-color: rgba(129, 199, 255, 0.28);
  background: rgba(129, 199, 255, 0.1);
}

.atelier-queue-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
  color: rgba(244, 240, 230, 0.68);
  font-size: 12px;
}

.atelier-queue-card strong,
.atelier-history__item strong,
.atelier-storyboard h3,
.atelier-preview__intro strong {
  color: #fbf5ea;
}

.atelier-queue-card strong {
  font-size: 18px;
  line-height: 1.25;
}

.atelier-queue-card p,
.atelier-history__item p,
.atelier-inline-message,
.atelier-storyboard p {
  margin: 0;
  color: rgba(244, 240, 230, 0.78);
  line-height: 1.8;
}

.atelier-queue-card small,
.atelier-history__item p {
  color: rgba(244, 240, 230, 0.62);
  font-size: 12px;
}

.atelier-storyboard {
  position: relative;
  display: grid;
  gap: 14px;
  min-height: 260px;
  padding: 28px;
  border-radius: 30px;
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.atelier-storyboard::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 56%;
  background: linear-gradient(to top, rgba(6, 12, 21, 0.96), transparent);
}

.atelier-storyboard > * {
  position: relative;
  z-index: 1;
}

.atelier-storyboard__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  color: rgba(244, 240, 230, 0.7);
  font-size: 12px;
}

.atelier-storyboard h3 {
  font-size: clamp(30px, 4.4vw, 44px);
}

.atelier-form-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.atelier-form-grid--wide {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.atelier-form-grid--two {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.atelier-input,
.atelier-markdown {
  width: 100%;
  border-radius: 18px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  padding: 12px 14px;
  color: #fbf5ea;
  background: rgba(6, 12, 21, 0.46);
}

.atelier-input:focus,
.atelier-markdown:focus {
  border-color: rgba(129, 199, 255, 0.3);
  box-shadow: 0 0 0 3px rgba(129, 199, 255, 0.1);
  outline: none;
}

.atelier-input--compact {
  max-width: 180px;
}

.atelier-textarea {
  min-height: 92px;
  resize: vertical;
}

.atelier-tag-cloud {
  display: flex;
  flex-wrap: wrap;
}

.atelier-tag-chip {
  padding: 8px 12px;
  border-radius: 999px;
  color: rgba(244, 240, 230, 0.82);
  font-size: 12px;
  font-weight: 700;
  border: 1px solid color-mix(in srgb, var(--tag-color), transparent 64%);
  background: rgba(6, 12, 21, 0.42);
}

.atelier-tag-chip--active {
  color: #fbf5ea;
  background: color-mix(in srgb, var(--tag-color), transparent 46%);
  border-color: color-mix(in srgb, var(--tag-color), transparent 44%);
}

.atelier-editor-grid {
  display: grid;
  gap: 16px;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
}

.atelier-markdown {
  min-height: 620px;
  resize: vertical;
  line-height: 1.75;
  font-family: 'IBM Plex Mono', 'SFMono-Regular', Menlo, Consolas, monospace;
}

.atelier-preview {
  min-height: 620px;
  overflow: auto;
  padding: 22px;
  border-radius: 24px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(6, 12, 21, 0.52);
}

.atelier-preview__intro {
  display: grid;
  gap: 6px;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid rgba(239, 226, 197, 0.12);
}

.atelier-preview__intro span {
  color: rgba(129, 199, 255, 0.84);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.atelier-preview :deep(img) {
  width: 100%;
  border-radius: 18px;
}

.atelier-transition-row {
  grid-template-columns: repeat(auto-fit, minmax(140px, max-content));
}

.atelier-history__item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(6, 12, 21, 0.42);
}

.atelier-upload-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.atelier-upload {
  display: grid;
  gap: 10px;
  padding: 16px;
  border-radius: 18px;
  border: 1px dashed rgba(239, 226, 197, 0.18);
  color: rgba(244, 240, 230, 0.8);
  background: rgba(6, 12, 21, 0.36);
}

.atelier-upload span {
  color: #fbf5ea;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.atelier-media-preview {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.atelier-media-preview__item {
  display: grid;
  gap: 8px;
  margin: 0;
}

.atelier-media-preview__item img {
  width: 100%;
  height: 128px;
  object-fit: cover;
  border-radius: 18px;
}

.atelier-media-preview__item figcaption {
  color: rgba(244, 240, 230, 0.64);
  font-size: 11px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.atelier-divider {
  height: 1px;
  background: linear-gradient(to right, rgba(239, 226, 197, 0.12), transparent);
}

.atelier-background-list {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.atelier-background-card {
  display: grid;
  gap: 4px;
  padding: 16px;
  border-radius: 20px;
  border: 1px solid rgba(239, 226, 197, 0.14);
  color: #fbf5ea;
  text-align: left;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.atelier-background-card:hover {
  transform: translateY(-1px);
}

.atelier-background-card span {
  color: rgba(244, 240, 230, 0.72);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.atelier-background-card--active {
  border-color: rgba(129, 199, 255, 0.3);
  box-shadow: 0 0 0 3px rgba(129, 199, 255, 0.1);
}

@media (max-width: 1320px) {
  .atelier-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .atelier-header,
  .atelier-editor-grid,
  .atelier-upload-grid,
  .atelier-media-preview,
  .atelier-form-grid,
  .atelier-form-grid--wide,
  .atelier-form-grid--two,
  .atelier-background-list {
    grid-template-columns: 1fr;
  }

  .atelier-header {
    padding: 22px;
  }
}

@media (max-width: 760px) {
  .atelier-panel,
  .atelier-storyboard {
    padding: 20px;
  }

  .atelier-panel__header {
    display: grid;
  }

  .atelier-filter,
  .atelier-metrics {
    grid-template-columns: 1fr;
  }
}
</style>
