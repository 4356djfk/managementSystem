<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  avatarUrl: {
    type: String,
    default: '',
  },
  username: {
    type: String,
    default: '',
  },
  displayName: {
    type: String,
    default: '',
  },
  size: {
    type: Number,
    default: 40,
  },
  shape: {
    type: String,
    default: 'circle',
  },
  appearance: {
    type: String,
    default: 'gradient',
  },
})

const imageFailed = ref(false)

watch(
  () => props.avatarUrl,
  () => {
    imageFailed.value = false
  },
)

const avatarText = computed(() => {
  const source = `${props.username || props.displayName || ''}`.trim()
  if (!source) return '?'
  const firstChar = Array.from(source)[0] || '?'
  return /[a-z]/i.test(firstChar) ? firstChar.toUpperCase() : firstChar
})

const avatarStyle = computed(() => {
  const palette = [
    ['#1d4ed8', '#3b82f6'],
    ['#0f766e', '#14b8a6'],
    ['#b45309', '#f59e0b'],
    ['#be185d', '#ec4899'],
    ['#6d28d9', '#8b5cf6'],
  ]
  const key = `${props.username || props.displayName || 'user'}`
  const index = Array.from(key).reduce((sum, char) => sum + char.charCodeAt(0), 0) % palette.length
  const [start, end] = palette[index]

  return {
    width: `${props.size}px`,
    height: `${props.size}px`,
    fontSize: `${Math.max(14, Math.round(props.size * 0.4))}px`,
    '--avatar-start': start,
    '--avatar-end': end,
  }
})

const showImage = computed(() => Boolean(props.avatarUrl) && !imageFailed.value)
</script>

<template>
  <div
    class="user-avatar"
    :class="{
      'is-square': shape === 'square',
      'is-flat': appearance === 'flat',
    }"
    :style="avatarStyle"
    :title="displayName || username || '用户头像'"
  >
    <img
      v-if="showImage"
      :src="avatarUrl"
      :alt="displayName || username || '用户头像'"
      @error="imageFailed = true"
    />
    <span v-else>{{ avatarText }}</span>
  </div>
</template>

<style scoped>
.user-avatar {
  flex: 0 0 auto;
  display: grid;
  place-items: center;
  border-radius: 999px;
  overflow: hidden;
  color: #fff;
  font-weight: 800;
  letter-spacing: 0.02em;
  background: linear-gradient(135deg, var(--avatar-start), var(--avatar-end));
  box-shadow: 0 10px 20px rgba(15, 23, 42, 0.14);
}

.user-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.user-avatar span {
  line-height: 1;
}

.user-avatar.is-square {
  border-radius: 0;
}

.user-avatar.is-flat {
  color: #1f2937;
  background: #e5e7eb;
  box-shadow: none;
}
</style>
