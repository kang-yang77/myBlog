<script setup lang="ts">
import { computed } from 'vue'
import { formatDateTime, useBlogStudioStore } from '../composables/useBlogStudio'

const { state, metrics, publishedArticles, timeline, activeBackground, getCategoryPath } = useBlogStudioStore()

const featuredArticle = computed(() => publishedArticles.value[0] ?? state.articles[0] ?? null)
const sideArticles = computed(() => publishedArticles.value.slice(1, 4))
const runwayEntries = computed(() => timeline.value.slice(0, 6))
const categoryBands = computed(() =>
  state.categories.slice(0, 5).map((category) => {
    const publishedCount = state.articles.filter(
      (article) => article.categoryId === category.id && article.status === 'published'
    ).length
    const latest = state.articles.find((article) => article.categoryId === category.id)

    return {
      ...category,
      publishedCount,
      latestTitle: latest?.title || '等待新的内容投递'
    }
  })
)
const workflowDeck = computed(() => [
  {
    label: '草稿箱',
    count: state.articles.filter((article) => article.status === 'draft').length,
    note: '先打磨，再决定是否推进入口。'
  },
  {
    label: '待发布',
    count: metrics.value.pending,
    note: '进入待发布队列，检查结构与视觉。'
  },
  {
    label: '已发布',
    count: metrics.value.published,
    note: '正式出现在看板、首页和时间线里。'
  },
  {
    label: '隐藏 / 下架',
    count: state.articles.filter((article) => ['hidden', 'offline'].includes(article.status)).length,
    note: '保留记录，但从公开视野中撤出。'
  }
])

function featureStyle(image?: string) {
  const source = image || activeBackground.value?.imageUrl
  return source
    ? {
        backgroundImage: `linear-gradient(135deg, rgba(7, 17, 28, 0.9), rgba(16, 31, 45, 0.55)), url("${source}")`
      }
    : {
        background:
          'linear-gradient(135deg, rgba(9, 18, 31, 0.92), rgba(22, 41, 58, 0.82))'
      }
}
</script>

<template>
  <div class="landing-shell">
    <section class="landing-hero">
      <div class="landing-hero__copy">
        <p class="landing-kicker">ExitZero Dispatch</p>
        <h1>把个人博客做成一间有节奏感的私人编辑部。</h1>
        <p class="landing-summary">
          这里不是一份普通文档站，而是一套围绕发布流、视觉策展、分类标签和时间轨迹组织起来的个人出版系统。
        </p>

        <div class="landing-actions">
          <a class="landing-action landing-action--primary" href="/studio/index">进入写作中枢</a>
          <a class="landing-action landing-action--ghost" href="/journal/index">查看出版看板</a>
        </div>

        <div class="landing-metrics">
          <article class="landing-metric">
            <strong>{{ metrics.total }}</strong>
            <span>内容条目</span>
          </article>
          <article class="landing-metric">
            <strong>{{ metrics.pending }}</strong>
            <span>待发布信号</span>
          </article>
          <article class="landing-metric">
            <strong>{{ state.categories.length }}</strong>
            <span>分类层级</span>
          </article>
          <article class="landing-metric">
            <strong>{{ activeBackground?.name }}</strong>
            <span>当前视觉主题</span>
          </article>
        </div>
      </div>

      <div class="landing-hero__deck">
        <article v-if="featuredArticle" class="landing-feature" :style="featureStyle(featuredArticle.bannerImage || featuredArticle.coverImage || featuredArticle.backgroundImage)">
          <div class="landing-feature__meta">
            <StatusBadge :status="featuredArticle.status" />
            <span>{{ getCategoryPath(featuredArticle.categoryId) }}</span>
            <span>{{ formatDateTime(featuredArticle.publishedAt) }}</span>
          </div>
          <h2>{{ featuredArticle.title }}</h2>
          <p>{{ featuredArticle.summary }}</p>
        </article>

        <div class="landing-side-notes">
          <article v-for="entry in sideArticles" :key="entry.id" class="landing-note">
            <p class="landing-note__label">Recent Drop</p>
            <strong>{{ entry.title }}</strong>
            <span>{{ getCategoryPath(entry.categoryId) }}</span>
          </article>
        </div>
      </div>
    </section>

    <section class="landing-strip">
      <div class="landing-strip__headline">
        <p class="landing-kicker">Taxonomy</p>
        <h2>分类是目录，标签是视角，背景是语气。</h2>
      </div>
      <div class="landing-band-list">
        <article v-for="band in categoryBands" :key="band.id" class="landing-band">
          <div class="landing-band__top">
            <strong>{{ band.name }}</strong>
            <span>{{ band.publishedCount }} 篇已发布</span>
          </div>
          <p>{{ band.description }}</p>
          <small>{{ band.latestTitle }}</small>
        </article>
      </div>
    </section>

    <section class="landing-grid">
      <article class="landing-panel">
        <div class="landing-panel__header">
          <p class="landing-kicker">Publishing Loop</p>
          <h2>文章不是静态文件，而是一条有状态的发布链。</h2>
        </div>
        <div class="landing-workflow">
          <article v-for="item in workflowDeck" :key="item.label" class="landing-workflow__card">
            <span>{{ item.label }}</span>
            <strong>{{ item.count }}</strong>
            <p>{{ item.note }}</p>
          </article>
        </div>
      </article>

      <article class="landing-panel">
        <div class="landing-panel__header">
          <p class="landing-kicker">Runway</p>
          <h2>最近的发布节奏</h2>
        </div>
        <div class="landing-runway">
          <article v-for="entry in runwayEntries" :key="entry.id" class="landing-runway__item">
            <div>
              <span>{{ entry.label }}</span>
              <strong>{{ entry.title }}</strong>
            </div>
            <small>{{ formatDateTime(entry.at) }}</small>
          </article>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.landing-shell {
  display: grid;
  gap: 26px;
}

.landing-hero {
  display: grid;
  gap: 22px;
  grid-template-columns: minmax(0, 1.08fr) minmax(0, 0.92fr);
  align-items: stretch;
}

.landing-hero__copy,
.landing-hero__deck,
.landing-panel,
.landing-strip {
  padding: 32px;
  border-radius: 30px;
  border: 1px solid rgba(239, 226, 197, 0.14);
  background: linear-gradient(145deg, rgba(9, 18, 30, 0.86), rgba(14, 27, 42, 0.58));
  box-shadow: 0 28px 80px rgba(1, 6, 15, 0.32);
  backdrop-filter: blur(18px);
}

.landing-kicker {
  margin: 0 0 12px;
  color: rgba(244, 214, 164, 0.9);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.26em;
  text-transform: uppercase;
}

.landing-hero__copy h1,
.landing-panel h2,
.landing-strip h2 {
  margin: 0;
  color: #fbf5ea;
  font-size: clamp(34px, 4.6vw, 64px);
  line-height: 0.96;
}

.landing-summary {
  max-width: 620px;
  margin: 18px 0 0;
  color: rgba(244, 240, 230, 0.76);
  font-size: 16px;
  line-height: 1.9;
}

.landing-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 28px;
}

.landing-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 18px;
  border-radius: 999px;
  font-weight: 700;
  text-decoration: none;
  transition: transform 0.2s ease, background 0.2s ease;
}

.landing-action:hover {
  transform: translateY(-1px);
}

.landing-action--primary {
  color: #08131f;
  background: linear-gradient(135deg, #f4d6a4, #ffab7a);
}

.landing-action--ghost {
  color: #fbf5ea;
  border: 1px solid rgba(239, 226, 197, 0.16);
  background: rgba(15, 29, 44, 0.45);
}

.landing-metrics {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 30px;
}

.landing-metric {
  display: grid;
  gap: 6px;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(5, 10, 19, 0.34);
}

.landing-metric strong {
  color: #fbf5ea;
  font-size: 26px;
  line-height: 1;
}

.landing-metric span {
  color: rgba(244, 240, 230, 0.68);
  font-size: 13px;
}

.landing-hero__deck {
  display: grid;
  gap: 14px;
}

.landing-feature {
  position: relative;
  display: grid;
  gap: 14px;
  min-height: 310px;
  padding: 24px;
  border-radius: 24px;
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.landing-feature::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 62%;
  background: linear-gradient(to top, rgba(7, 14, 24, 0.96), transparent);
}

.landing-feature > * {
  position: relative;
  z-index: 1;
}

.landing-feature__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  color: rgba(244, 240, 230, 0.72);
  font-size: 12px;
}

.landing-feature h2 {
  margin: 0;
  color: #fbf5ea;
  font-size: clamp(28px, 4vw, 40px);
  line-height: 1.02;
}

.landing-feature p {
  margin: 0;
  max-width: 85%;
  color: rgba(244, 240, 230, 0.82);
  line-height: 1.8;
}

.landing-side-notes {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.landing-note {
  display: grid;
  gap: 8px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(7, 14, 24, 0.5);
  border: 1px solid rgba(239, 226, 197, 0.12);
}

.landing-note__label {
  margin: 0;
  color: rgba(129, 199, 255, 0.84);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
}

.landing-note strong {
  color: #fbf5ea;
  font-size: 16px;
  line-height: 1.4;
}

.landing-note span {
  color: rgba(244, 240, 230, 0.66);
  font-size: 12px;
}

.landing-strip__headline {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-end;
}

.landing-strip__headline h2 {
  max-width: 700px;
  font-size: clamp(28px, 3.5vw, 42px);
}

.landing-band-list {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  margin-top: 20px;
}

.landing-band {
  display: grid;
  gap: 10px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: linear-gradient(180deg, rgba(12, 21, 33, 0.78), rgba(6, 12, 20, 0.48));
}

.landing-band__top {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  align-items: baseline;
}

.landing-band strong {
  color: #fbf5ea;
  font-size: 18px;
}

.landing-band span,
.landing-band small {
  color: rgba(244, 240, 230, 0.64);
  font-size: 12px;
}

.landing-band p {
  margin: 0;
  color: rgba(244, 240, 230, 0.8);
  line-height: 1.75;
}

.landing-grid {
  display: grid;
  gap: 22px;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 0.95fr);
}

.landing-panel__header h2 {
  font-size: clamp(28px, 3.6vw, 42px);
}

.landing-workflow {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  margin-top: 20px;
}

.landing-workflow__card {
  display: grid;
  gap: 8px;
  padding: 18px;
  border-radius: 20px;
  background: rgba(7, 14, 24, 0.52);
  border: 1px solid rgba(239, 226, 197, 0.12);
}

.landing-workflow__card span {
  color: rgba(129, 199, 255, 0.82);
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.landing-workflow__card strong {
  color: #fbf5ea;
  font-size: 34px;
}

.landing-workflow__card p {
  margin: 0;
  color: rgba(244, 240, 230, 0.72);
  line-height: 1.75;
}

.landing-runway {
  display: grid;
  gap: 12px;
  margin-top: 20px;
}

.landing-runway__item {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 16px 18px;
  border-radius: 18px;
  background: rgba(7, 14, 24, 0.48);
  border: 1px solid rgba(239, 226, 197, 0.12);
}

.landing-runway__item span {
  display: block;
  color: rgba(244, 214, 164, 0.86);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.landing-runway__item strong {
  color: #fbf5ea;
  font-size: 16px;
  line-height: 1.5;
}

.landing-runway__item small {
  color: rgba(244, 240, 230, 0.66);
  white-space: nowrap;
}

@media (max-width: 1080px) {
  .landing-hero,
  .landing-grid,
  .landing-side-notes,
  .landing-band-list {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .landing-hero__copy,
  .landing-hero__deck,
  .landing-panel,
  .landing-strip {
    padding: 22px;
  }

  .landing-metrics,
  .landing-workflow {
    grid-template-columns: 1fr;
  }

  .landing-feature p {
    max-width: none;
  }

  .landing-strip__headline {
    display: grid;
  }
}
</style>
