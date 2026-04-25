<script setup>
defineProps({
  text: {
    type: String,
    required: true,
  },
})
</script>

<template>
  <div class="section-badge">
    <span class="badge-dot pulse-dot" />
    <span class="mono-label">{{ text }}</span>
  </div>
</template>

<style scoped>
.section-badge {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  border-radius: 999px;
  border: 1px solid rgba(0, 82, 255, 0.24);
  background: rgba(0, 82, 255, 0.05);
  color: var(--accent);
}

.badge-dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--accent), var(--accent-secondary));
  box-shadow: 0 0 0 6px rgba(0, 82, 255, 0.08);
}

.mono-label {
  font-size: 0.74rem;
  font-weight: 500;
}
</style>
