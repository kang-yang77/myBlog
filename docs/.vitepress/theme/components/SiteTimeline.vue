<script setup lang="ts">
import { computed, ref } from 'vue'
import { formatDateTime, useBlogStudioStore } from '../composables/useBlogStudio'

const { timeline, getCategoryPath } = useBlogStudioStore()

const filter = ref<'all' | 'article' | 'site'>('all')

const filteredEntries = computed(() =>
  filter.value === 'all'
    ? timeline.value
    : timeline.value.filter((entry) => entry.kind === filter.value)
)

const groupedEntries = computed(() => {
  const groups = new Map<string, { key: string; label: string; entries: typeof filteredEntries.value }>()

  filteredEntries.value.forEach((entry) => {
    const date = new Date(entry.at)
    const key = `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
    const label = new Intl.DateTimeFormat('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: '2-digit',
      weekday: 'short'
    }).format(date)

    if (!groups.has(key)) {
      groups.set(key, { key, label, entries: [] })
    }

    groups.get(key)!.entries.push(entry)
  })

  return Array.from(groups.values())
})

const summaryCards = computed(() => [
  {
    label: '全部动态',
    value: timeline.value.length,
    description: '系统事件与文章流转统一聚合。'
  },
  {
    label: '文章节点',
    value: timeline.value.filter((entry) => entry.kind === 'article').length,
    description: '草稿、待发布、发布、隐藏与下架记录。'
  },
  {
    label: '系统节点',
    value: timeline.value.filter((entry) => entry.kind === 'site').length,
    description: '视觉、结构和平台能力的迭代轨迹。'
  }
])

function setFilter(mode: 'all' | 'article' | 'site') {
  filter.value = mode
}

function tone(entryLabel: string) {
  if (entryLabel.includes('发布')) {
    return 'success'
  }
  if (entryLabel.includes('隐藏') || entryLabel.includes('下架')) {
    return 'danger'
  }
  if (entryLabel.includes('待发布')) {
    return 'warning'
  }
  return 'neutral'
}

function sourceLabel(kind: 'article' | 'site') {
  return kind === 'article' ? 'ARTICLE' : 'SYSTEM'
}
</script>

<template>
  <div class="atlas-shell">
    <section class="atlas-header">
      <div class="atlas-header__copy">
        <p class="atlas-kicker">Evolution Log</p>
        <h2>建站历程、内容发布与系统更新，在一条轨道里发生。</h2>
        <p class="atlas-summary">
          时间轴不仅记录“什么时候发文”，还记录“为什么会有这一次迭代”。这会让博客更像持续演化的作品，而不是静态仓库。
        </p>
      </div>

      <div class="atlas-header__controls">
        <div class="atlas-filters">
          <button
            v-for="mode in ['all', 'site', 'article']"
            :key="mode"
            class="atlas-filter"
            :class="{ 'atlas-filter--active': filter === mode }"
            @click="setFilter(mode)"
          >
            {{
              mode === 'all'
                ? '全部'
                : mode === 'site'
                  ? '系统'
                  : '文章'
            }}
          </button>
        </div>

        <div class="atlas-summary-cards">
          <article v-for="item in summaryCards" :key="item.label" class="atlas-summary-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
            <p>{{ item.description }}</p>
          </article>
        </div>
      </div>
    </section>

    <section class="atlas-runway">
      <div v-for="group in groupedEntries" :key="group.key" class="atlas-day">
        <div class="atlas-day__label">
          <span>{{ group.label }}</span>
        </div>

        <div class="atlas-day__stack">
          <article
            v-for="entry in group.entries"
            :key="entry.id"
            class="atlas-card"
            :data-tone="tone(entry.label)"
          >
            <div class="atlas-card__meta">
              <span class="atlas-pill">{{ sourceLabel(entry.kind) }}</span>
              <span>{{ entry.label }}</span>
              <span>{{ formatDateTime(entry.at) }}</span>
              <span v-if="entry.categoryId">{{ getCategoryPath(entry.categoryId) }}</span>
            </div>
            <h3>{{ entry.title }}</h3>
            <p>{{ entry.description }}</p>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.atlas-shell {
  display: grid;
  gap: 24px;
}

.atlas-header,
.atlas-runway {
  padding: 30px;
  border-radius: 32px;
  border: 1px solid rgba(239, 226, 197, 0.14);
  background: linear-gradient(145deg, rgba(9, 18, 30, 0.88), rgba(14, 27, 42, 0.58));
  box-shadow: 0 28px 80px rgba(1, 6, 15, 0.28);
  backdrop-filter: blur(18px);
}

.atlas-header {
  display: grid;
  gap: 20px;
  grid-template-columns: minmax(0, 1.08fr) minmax(320px, 0.92fr);
}

.atlas-kicker {
  margin: 0 0 12px;
  color: rgba(244, 214, 164, 0.9);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.24em;
  text-transform: uppercase;
}

.atlas-header h2 {
  margin: 0;
  color: #fbf5ea;
  font-size: clamp(32px, 4.2vw, 52px);
  line-height: 0.98;
}

.atlas-summary {
  margin: 16px 0 0;
  max-width: 700px;
  color: rgba(244, 240, 230, 0.76);
  line-height: 1.9;
}

.atlas-header__controls {
  display: grid;
  gap: 16px;
  align-content: start;
}

.atlas-filters {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.atlas-filter {
  padding: 10px 16px;
  border-radius: 999px;
  border: 1px solid rgba(239, 226, 197, 0.14);
  color: rgba(244, 240, 230, 0.72);
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  background: rgba(7, 14, 24, 0.44);
}

.atlas-filter--active {
  color: #08131f;
  background: linear-gradient(135deg, #f4d6a4, #ffab7a);
  border-color: transparent;
}

.atlas-summary-cards {
  display: grid;
  gap: 12px;
}

.atlas-summary-card {
  display: grid;
  gap: 6px;
  padding: 16px 18px;
  border-radius: 20px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(7, 14, 24, 0.42);
}

.atlas-summary-card span {
  color: rgba(129, 199, 255, 0.86);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.atlas-summary-card strong {
  color: #fbf5ea;
  font-size: 30px;
}

.atlas-summary-card p {
  margin: 0;
  color: rgba(244, 240, 230, 0.68);
  line-height: 1.75;
}

.atlas-runway {
  display: grid;
  gap: 22px;
}

.atlas-day {
  display: grid;
  gap: 16px;
  grid-template-columns: 220px minmax(0, 1fr);
  align-items: start;
}

.atlas-day__label {
  position: sticky;
  top: 92px;
  padding-top: 6px;
}

.atlas-day__label span {
  display: inline-flex;
  align-items: center;
  padding: 10px 14px;
  border-radius: 999px;
  color: #fbf5ea;
  font-size: 13px;
  font-weight: 700;
  border: 1px solid rgba(239, 226, 197, 0.14);
  background: rgba(7, 14, 24, 0.5);
}

.atlas-day__stack {
  display: grid;
  gap: 14px;
}

.atlas-card {
  display: grid;
  gap: 12px;
  padding: 20px 22px;
  border-radius: 24px;
  border: 1px solid rgba(239, 226, 197, 0.12);
  background: rgba(6, 12, 21, 0.56);
}

.atlas-card[data-tone='success'] {
  border-color: rgba(138, 223, 164, 0.22);
}

.atlas-card[data-tone='warning'] {
  border-color: rgba(244, 214, 164, 0.22);
}

.atlas-card[data-tone='danger'] {
  border-color: rgba(255, 179, 155, 0.2);
}

.atlas-card__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  color: rgba(244, 240, 230, 0.66);
  font-size: 12px;
}

.atlas-pill {
  padding: 6px 10px;
  border-radius: 999px;
  color: rgba(129, 199, 255, 0.92);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  background: rgba(129, 199, 255, 0.12);
  border: 1px solid rgba(129, 199, 255, 0.16);
}

.atlas-card h3 {
  margin: 0;
  color: #fbf5ea;
  font-size: 24px;
  line-height: 1.08;
}

.atlas-card p {
  margin: 0;
  color: rgba(244, 240, 230, 0.78);
  line-height: 1.85;
}

@media (max-width: 1100px) {
  .atlas-header,
  .atlas-day {
    grid-template-columns: 1fr;
  }

  .atlas-day__label {
    position: static;
  }
}

@media (max-width: 760px) {
  .atlas-header,
  .atlas-runway {
    padding: 22px;
  }
}
</style>
