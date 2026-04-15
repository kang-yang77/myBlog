<script setup lang="ts">
import { computed } from 'vue'
import { blogCatalog } from '../data/blogCatalog'

const recentPosts = computed(() => blogCatalog)

function formatDate(date: string) {
  return new Intl.DateTimeFormat('en', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(new Date(date))
}
</script>

<template>
  <div class="public-home">
    <section class="public-home__intro">
      <p class="public-home__eyebrow">Personal blog · Backend engineering · AI applications</p>
      <div class="public-home__identity">
        <h1 class="public-home__welcome">👋 Welcome to Yang Kang's Blog</h1>
        <p class="public-home__bio">
          Hi, this is Yang Kang. I write about backend engineering, AI applications, algorithms, and the process of turning a personal blog into a long-term body of work.
        </p>
        <div class="public-home__links" aria-label="social links">
          <a href="https://github.com/kang-yang77" target="_blank" rel="noreferrer">GitHub</a>
        </div>
      </div>
    </section>

    <section class="public-home__list">
      <article v-for="post in recentPosts" :key="post.href" class="public-post">
        <h2>
          <a :href="post.href">{{ post.title }}</a>
        </h2>
        <p>{{ post.summary }}</p>
        <div class="public-post__meta">
          <span>Date: {{ formatDate(post.date) }}</span>
          <span>Estimated Reading Time: {{ post.readingTime }}</span>
          <span>Author: Yang Kang</span>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.public-home {
  width: min(100%, var(--blog-home-width));
  margin: 0 auto;
  display: grid;
  gap: 48px;
}

.public-home__intro,
.public-home__list {
  display: grid;
  gap: 20px;
}

.public-home__eyebrow {
  margin: 0;
  color: var(--blog-accent-muted);
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.public-home__identity {
  display: grid;
  gap: 16px;
  max-width: 760px;
}

.public-home__welcome {
  margin: 0;
  font-family: var(--vp-font-family-base);
  color: var(--vp-c-text-1);
  font-size: clamp(34px, 4.8vw, 54px);
  font-weight: 800;
  line-height: 1.08;
}

.public-home__bio {
  margin: 0;
  color: var(--vp-c-text-2);
  line-height: 1.78;
  font-size: clamp(16px, 1.45vw, 18px);
}

.public-home__links {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.public-home__links a {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 76px;
  padding: 7px 12px;
  border-radius: 999px;
  border: 1px solid var(--blog-card-border);
  background: var(--blog-card-bg);
  color: var(--vp-c-text-1);
  font-size: 13px;
  font-weight: 600;
  text-decoration: none;
  transition: background 0.2s ease, border-color 0.2s ease;
}

.public-home__links a:hover {
  background: var(--blog-card-bg-hover);
}

.public-home__list {
  gap: 28px;
}

.public-post {
  display: grid;
  gap: 16px;
  padding: 30px 34px;
  border-radius: 18px;
  border: 1px solid var(--blog-card-border);
  background: var(--blog-card-bg);
  transition: background 0.2s ease, border-color 0.2s ease, transform 0.2s ease;
}

.public-post:hover {
  background: var(--blog-card-bg-hover);
  transform: translateY(-1px);
}

.public-post h2 {
  margin: 0;
  font-family: var(--vp-font-family-base);
  color: var(--vp-c-text-1);
  font-size: clamp(20px, 1.9vw, 25px);
  font-weight: 800;
  line-height: 1.22;
}

.public-post h2 a {
  color: inherit;
  text-decoration: none;
}

.public-post h2 a:hover {
  text-decoration: underline;
  text-underline-offset: 3px;
}

.public-post p {
  margin: 0;
  color: var(--vp-c-text-2);
  line-height: 1.76;
  font-size: 16px;
}

.public-post__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 18px;
  color: var(--blog-accent-muted);
  font-size: 14px;
}

@media (max-width: 640px) {
  .public-home {
    gap: 36px;
  }

  .public-post {
    padding: 22px 20px;
  }

  .public-home__welcome {
    font-size: 34px;
  }

  .public-home__bio {
    font-size: 16px;
  }

  .public-post p {
    font-size: 15px;
  }
}
</style>
