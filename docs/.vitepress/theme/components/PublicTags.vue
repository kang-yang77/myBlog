<script setup lang="ts">
import { blogCatalog } from '../data/blogCatalog'
</script>

<template>
  <div class="public-tags">
    <header class="public-tags__header">
      <h1>Tags</h1>
      <p>这里直接展示所有文章，只保留标题和描述，阅读会更干净一些。</p>
    </header>

    <section class="public-tags__list">
      <article v-for="entry in blogCatalog" :key="entry.href" class="public-tags__item">
        <a :href="entry.href">{{ entry.title }}</a>
        <p>{{ entry.summary }}</p>
      </article>
    </section>
  </div>
</template>

<style scoped>
.public-tags {
  width: min(100%, 760px);
  margin: 0 auto;
  display: grid;
  gap: 28px;
}

.public-tags__header h1 {
  margin: 0;
  color: var(--vp-c-text-1);
}

.public-tags__header h1 {
  font-size: 38px;
}

.public-tags__header p {
  margin: 10px 0 0;
  color: var(--vp-c-text-2);
  line-height: 1.8;
}

.public-tags__list {
  display: grid;
  gap: 18px;
}

.public-tags__item {
  display: grid;
  gap: 10px;
  padding: 0 0 22px;
  border-bottom: 1px solid var(--vp-c-divider);
}

.public-tags__item a {
  color: var(--vp-c-text-1);
  font-size: 26px;
  font-weight: 700;
  text-decoration: none;
}

.public-tags__item a:hover {
  text-decoration: underline;
}

.public-tags__item p {
  margin: 0;
  color: var(--vp-c-text-2);
  line-height: 1.8;
  font-size: 16px;
}
</style>
