<script setup>
const props = defineProps({
  type: {
    type: String,
    default: 'button',
  },
  variant: {
    type: String,
    default: 'primary',
  },
  block: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
})
</script>

<template>
  <button
    :type="props.type"
    class="base-button"
    :class="[props.variant, { block: props.block }]"
    :disabled="props.disabled || props.loading"
  >
    <span v-if="props.loading" class="spinner" />
    <slot />
  </button>
</template>

<style scoped>
.base-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  min-height: 52px;
  padding: 0 22px;
  border: 1px solid transparent;
  border-radius: 14px;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease, background 0.2s ease;
  font-weight: 600;
  font-size: 0.96rem;
}

.base-button:hover {
  transform: translateY(-2px);
}

.base-button:active {
  transform: translateY(0) scale(0.98);
}

.base-button:disabled {
  cursor: not-allowed;
  opacity: 0.7;
  transform: none;
}

.base-button.block {
  width: 100%;
}

.base-button.primary {
  color: var(--accent-foreground);
  background: linear-gradient(135deg, var(--accent), var(--accent-secondary));
  box-shadow: var(--shadow-accent);
}

.base-button.primary:hover {
  filter: brightness(1.06);
  box-shadow: 0 18px 44px rgba(0, 82, 255, 0.3);
}

.base-button.secondary {
  color: var(--foreground);
  background: rgba(255, 255, 255, 0.7);
  border-color: var(--border);
  box-shadow: var(--shadow-sm);
}

.base-button.secondary:hover {
  border-color: rgba(0, 82, 255, 0.24);
  box-shadow: var(--shadow-md);
}

.base-button.ghost {
  color: var(--muted-foreground);
  background: transparent;
}

.base-button.ghost:hover {
  color: var(--foreground);
  background: rgba(241, 245, 249, 0.72);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.38);
  border-top-color: rgba(255, 255, 255, 1);
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

.secondary .spinner,
.ghost .spinner {
  border-color: rgba(100, 116, 139, 0.28);
  border-top-color: var(--accent);
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
