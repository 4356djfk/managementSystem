<script setup>
const model = defineModel({
  type: String,
  default: '',
})

const props = defineProps({
  id: {
    type: String,
    default: '',
  },
  label: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: 'text',
  },
  placeholder: {
    type: String,
    default: '',
  },
  autocomplete: {
    type: String,
    default: '',
  },
  error: {
    type: String,
    default: '',
  },
})
</script>

<template>
  <label class="field">
    <span v-if="props.label" class="field-label">{{ props.label }}</span>
    <input
      :id="props.id"
      v-model.trim="model"
      class="field-input"
      :class="{ invalid: props.error }"
      :type="props.type"
      :placeholder="props.placeholder"
      :autocomplete="props.autocomplete"
    >
    <span v-if="props.error" class="field-error">{{ props.error }}</span>
  </label>
</template>

<style scoped>
.field {
  display: grid;
  gap: 10px;
}

.field-label {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--foreground);
}

.field-input {
  width: 100%;
  min-height: 54px;
  padding: 0 16px;
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 14px;
  outline: none;
  background: rgba(248, 250, 252, 0.88);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.field-input::placeholder {
  color: rgba(100, 116, 139, 0.7);
}

.field-input:hover {
  border-color: rgba(148, 163, 184, 0.78);
}

.field-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px rgba(0, 82, 255, 0.12);
}

.field-input.invalid {
  border-color: rgba(220, 38, 38, 0.6);
  box-shadow: 0 0 0 4px rgba(220, 38, 38, 0.08);
}

.field-error {
  color: var(--danger);
  font-size: 0.84rem;
}
</style>
