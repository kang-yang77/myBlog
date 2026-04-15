<script setup lang="ts">
import { computed } from 'vue'
import { ARTICLE_STATUS_META, type ArticleStatus } from '../composables/useBlogStudio'

const props = defineProps<{
  status: ArticleStatus
}>()

const meta = computed(() => ARTICLE_STATUS_META[props.status])
</script>

<template>
  <span class="studio-status-badge" :data-tone="meta.tone">
    {{ meta.label }}
  </span>
</template>
