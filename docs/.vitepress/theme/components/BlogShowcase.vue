<script setup lang="ts">
import { computed } from 'vue'
import StatusBadge from './StatusBadge.vue'
import { formatDateTime, useBlogStudioStore } from '../composables/useBlogStudio'

const props = defineProps<{
  compact?: boolean
}>()

const {
  state,
  publishedArticles,
  tagMap,
  activeBackground,
  setActiveBackground,
  getCategoryPath
} = useBlogStudioStore()

const leadArticle = computed(() => publishedArticles.value[0] ?? null)
const storyCards = computed(() =>
  props.compact ? publishedArticles.value.slice(1, 4) : publishedArticles.value.slice(1, 7)
)
const categoryBoards = computed(() =>
  state.categories.map((category) => {
    const entries = publishedArticles.value.filter((article) => article.categoryId === category.id)
    return {
      ...category,
      count: entries.length,
      latestTitle: entries[0]?.title || '等待该分类的第一篇正式发布'
    }
  })
)

function articleCover(image?: string) {
  const fallback = activeBackground.value?.imageUrl
  const source = image || fallback
  return source
    ? {
        backgroundImage: `linear-gradient(135deg, rgba(5, 12, 21, 0.92), rgba(12, 25, 40, 0.58)), url("${source}")`
      }
    : {
        background:
          'linear-gradient(135deg, rgba(8, 16, 26, 0.95), rgba(20, 35, 50, 0.8))'
      }
}

function tagStyle(tagId: string) {
  const color = tagMap.value[tagId]?.color || '#81c7ff'
  return {
    '--tag-color': color
  }
}
</script>

<template>
  <div class="journal-shell">
    <section class="journal-hero">
      <article
        v-if="leadArticle"
        class="journal-feature"
        :style="articleCover(leadArticle.bannerImage || leadArticle.coverImage || leadArticle.backgroundImage)"
      >
        <div class="journal-feature__meta">
          <StatusBadge :status="leadArticle.status" />
          <span>{{ getCategoryPath(leadArticle.categoryId) }}</span>
          <span>{{ formatDateTime(leadArticle.publishedAt) }}</span>
        </div>
        <h2>{{ leadArticle.title }}</h2>
        <p>{{ leadArticle.summary }}</p>
        <div class="journal-feature__tags">
          <span
            v-for="tagId in leadArticle.tagIds"
            :key="tagId"
            class="journal-tag"
            :style="tagStyle(tagId)"
          >
            {{ tagMap[tagId]?.name || '标签' }}
          </span>
        </div>
      </article>

      <aside class="journal-side">
        <article class="journal-note">
          <p class="journal-kicker">Publishing Note</p>
          <h3>这不是文章列表，而是一次有策展感的内容呈现。</h3>
          <p>
            已发布内容会优先读取文章自己的 Banner、Cover 或背景图；没有设置时，再回退到全局视觉主题。
          </p>
        </article>

        <article class="journal-note">
          <p class="journal-kicker">Theme Switch</p>
          <div class="journal-backgrounds">
            <button
              v-for="background in state.settings.backgrounds"
              :key="background.id"
              class="journal-background"
              :class="{ 'journal-background--active': state.settings.activeBackgroundId === background.id }"
              :style="{ background: background.overlay }"
              @click="setActiveBackground(background.id)"
            >
              <strong>{{ background.name }}</strong>
              <span>{{ background.motionClass }}</span>
            </button>
          </div>
        </article>
      </aside>
    </section>

    <section class="journal-grid">
      <article
        v-for="article in storyCards"
        :key="article.id"
        class="journal-card"
        :style="articleCover(article.bannerImage || article.coverImage || article.backgroundImage)"
      >
        <div class="journal-card__meta">
          <span>{{ getCategoryPath(article.categoryId) }}</span>
          <span>{{ formatDateTime(article.publishedAt) }}</span>
        </div>
        <h3>{{ article.title }}</h3>
        <p>{{ article.summary }}</p>
        <div class="journal-card__tags">
          <span
            v-for="tagId in article.tagIds"
            :key="tagId"
            class="journal-tag"
            :style="tagStyle(tagId)"
          >
            {{ tagMap[tagId]?.name || '标签' }}
          </span>
        </div>
      </article>
    </section>

    <section v-if="!compact" class="journal-taxonomy">
      <div class="journal-taxonomy__header">
        <div>
          <p class="journal-kicker">Taxonomy Board</p>
          <h2>分类决定入口，标签决定读法。</h2>
        </div>
        <p class="journal-copy">
          这层不是机械归档，而是让你的博客内容在“主题、情境和更新节奏”三个维度上都有结构。
        </p>
      </div>

      <div class="journal-taxonomy__grid">
        <article v-for="category in categoryBoards" :key="category.id" class="journal-taxonomy__card">
          <div class="journal-taxonomy__top">
            <strong>{{ category.name }}</strong>
            <span>{{ category.count }} 篇</span>
          </div>
          <p>{{ category.description }}</p>
          <small>{{ category.latestTitle }}</small>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.journal-shell {
  display: grid;
  gap: 24px;
}

.journal-hero,
.journal-taxonomy {
  display: grid;
  gap: 18px;
}

.journal-hero {
  grid-template-columns: minmax(0, 1.18fr) minmax(320px, 0.82fr);
}

.journal-feature,
.journal-note,
.journal-taxonomy,
.journal-card {
  border: 1px solid rgba(239, 226, 197, 0.14);
  box-shadow: 0 28px 80px rgba(1, 6, 15, 0.28);
  backdrop-filter: blur(18px);
}

.journal-feature {
  position: relative;
  display: grid;
  gap: 16px;
  min-height: 420px;
  padding: 28px;
  border-radius: 30px;
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.journal-feature::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 62%;
  background: linear-gradient(to top, rgba(5, 11, 19, 0.96), transparent);
}

.journal-feature > * {
  position: relative;
  z-index: 1;
}

.journal-feature__meta,
.journal-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  color: rgba(244, 240, 230, 0.72);
  font-size: 12px;
}

.journal-feature h2,
.journal-taxonomy__header h2 {
  margin: 0;
  color: #fbf5ea;
  font-size: clamp(30px, 4vw, 46px);
  line-height: 0.98;
}

.journal-feature p,
.journal-card p,
.journal-note p,
.journal-copy,
.journal-taxonomy__card p {
  margin: 0;
  color: rgba(244, 240, 230, 0.8);
  line-height: 1.85;
}

.journal-feature__tags,
.journal-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.journal-side {
  display: grid;
  gap: 18px;
}

.journal-note {
  display: grid;
  gap: 12px;
  padding: 26px;
  border-radius: 28px;
  background: linear-gradient(145deg, rgba(10, 18, 29, 0.88), rgba(15, 26, 39, 0.62));
}

.journal-kicker {
  margin: 0;
  color: rgba(244, 214, 164, 0.9);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.journal-note h3 {
  margin: 0;
  color: #fbf5ea;
  font-size: 28px;
  line-height: 1.08;
}

.journal-backgrounds {
  display: grid;
  gap: 12px;
}

.journal-background {
  display: grid;
  gap: 4px;
  padding: 16px;
  border: 1px solid rgba(239, 226, 197, 0.14);
  border-radius: 20px;
  color: #fbf5ea;
  text-align: left;
  transition: transform 0.2s ease, border-color 0.2s ease;
}

.journal-background:hover {
  transform: translateY(-1px);
}

.journal-background strong {
  font-size: 16px;
}

.journal-background span {
  color: rgba(244, 240, 230, 0.72);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.12em;
}

.journal-background--active {
  border-color: rgba(129, 199, 255, 0.34);
  box-shadow: 0 0 0 3px rgba(129, 199, 255, 0.12);
}

.journal-grid {
  display: grid;
  gap: 18px;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
}

.journal-card {
  position: relative;
  display: grid;
  gap: 14px;
  min-height: 250px;
  padding: 22px;
  border-radius: 26px;
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.journal-card::after {
  content: '';
  position: absolute;
  inset: auto 0 0;
  height: 58%;
  background: linear-gradient(to top, rgba(5, 11, 19, 0.95), transparent);
}

.journal-card > * {
  position: relative;
  z-index: 1;
}

.journal-card h3,
.journal-taxonomy__card strong {
  margin: 0;
  color: #fbf5ea;
  font-size: 22px;
  line-height: 1.12;
}

.journal-tag {
  padding: 7px 12px;
  border-radius: 999px;
  color: #fbf5ea;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  background: color-mix(in srgb, var(--tag-color), transparent 42%);
  border: 1px solid color-mix(in srgb, var(--tag-color), transparent 52%);
}

.journal-taxonomy {
  padding: 30px;
  border-radius: 30px;
  background: linear-gradient(145deg, rgba(9, 17, 27, 0.88), rgba(14, 27, 41, 0.62));
}

.journal-taxonomy__header {
  display: grid;
  gap: 12px;
  grid-template-columns: minmax(0, 1fr) minmax(260px, 420px);
  align-items: end;
}

.journal-copy {
  color: rgba(244, 240, 230, 0.72);
}

.journal-taxonomy__grid {
  display: grid;
  gap: 14px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  margin-top: 18px;
}

.journal-taxonomy__card {
  display: grid;
  gap: 10px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(6, 12, 21, 0.44);
}

.journal-taxonomy__top {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  align-items: baseline;
}

.journal-taxonomy__card span,
.journal-taxonomy__card small {
  color: rgba(244, 240, 230, 0.64);
  font-size: 12px;
}

@media (max-width: 1100px) {
  .journal-hero,
  .journal-taxonomy__header {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 760px) {
  .journal-feature,
  .journal-note,
  .journal-taxonomy,
  .journal-card {
    padding: 22px;
  }
}
</style>
