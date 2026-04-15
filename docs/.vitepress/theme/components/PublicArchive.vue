<script setup lang="ts">
import { computed } from 'vue'
import { blogCatalog } from '../data/blogCatalog'

const groupedArchive = computed(() => {
  const groups = new Map<string, typeof blogCatalog>()

  blogCatalog.forEach((entry) => {
    const year = entry.date.slice(0, 4)
    if (!groups.has(year)) {
      groups.set(year, [])
    }
    groups.get(year)!.push(entry)
  })

  return Array.from(groups.entries()).map(([year, entries]) => ({ year, entries }))
})

function formatDate(date: string) {
  return new Intl.DateTimeFormat('en', {
    month: 'short',
    day: 'numeric'
  }).format(new Date(date))
}
</script>

<template>
  <div class="public-archive">
    <header class="public-archive__header">
      <h1>Archive</h1>
      <p>按时间回看所有公开写作与项目记录。</p>
    </header>

    <section v-for="group in groupedArchive" :key="group.year" class="public-archive__group">
      <h2>{{ group.year }}</h2>
      <article v-for="entry in group.entries" :key="entry.href" class="public-archive__item">
        <span>{{ formatDate(entry.date) }}</span>
        <a :href="entry.href">{{ entry.title }}</a>
      </article>
    </section>
  </div>
</template>

<style scoped>
.public-archive {
  width: min(100%, 760px);
  margin: 0 auto;
  display: grid;
  gap: 28px;
}

.public-archive__header h1,
.public-archive__group h2 {
  margin: 0;
  color: var(--vp-c-text-1);
}

.public-archive__header h1 {
  font-size: 38px;
}

.public-archive__header p {
  margin: 10px 0 0;
  color: var(--vp-c-text-2);
  line-height: 1.8;
}

.public-archive__group {
  display: grid;
  gap: 12px;
}

.public-archive__group h2 {
  font-size: 22px;
}

.public-archive__item {
  display: grid;
  grid-template-columns: 90px minmax(0, 1fr);
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid var(--vp-c-divider);
}

.public-archive__item span {
  color: var(--blog-accent-muted);
  font-size: 14px;
}

.public-archive__item a {
  color: var(--vp-c-text-1);
  text-decoration: none;
  line-height: 1.7;
}

.public-archive__item a:hover {
  text-decoration: underline;
}
</style>
