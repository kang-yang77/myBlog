<script setup lang="ts">
import { computed } from 'vue'
import { useBlogStudioStore } from '../composables/useBlogStudio'

const { activeBackground } = useBlogStudioStore()

const backgroundStyle = computed(() => {
  const background = activeBackground.value

  return {
    '--studio-overlay': background?.overlay || 'linear-gradient(135deg, rgba(9,18,33,0.92), rgba(19,35,59,0.78))',
    '--studio-image': background?.imageUrl ? `url("${background.imageUrl}")` : 'none',
    '--studio-accent': background?.accentColor || '#3a7cff'
  }
})

const motionClass = computed(() => `studio-bg--${activeBackground.value?.motionClass || 'aurora'}`)
</script>

<template>
  <div class="studio-dynamic-bg" :class="motionClass" :style="backgroundStyle">
    <div class="studio-dynamic-bg__mesh" />
    <div class="studio-dynamic-bg__orb studio-dynamic-bg__orb--one" />
    <div class="studio-dynamic-bg__orb studio-dynamic-bg__orb--two" />
    <div class="studio-dynamic-bg__grain" />
  </div>
</template>
