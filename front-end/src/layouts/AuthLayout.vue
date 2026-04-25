<script setup>
defineProps({
  brandText: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    default: '',
  },
  description: {
    type: String,
    default: '',
  },
  imageSrc: {
    type: String,
    default: '',
  },
  imageAlt: {
    type: String,
    default: '',
  },
  showCopy: {
    type: Boolean,
    default: true,
  },
})
</script>

<template>
  <div class="auth-screen">
    <section class="auth-visual">
      <div v-if="brandText" class="visual-brand">{{ brandText }}</div>
      <div class="visual-stage" aria-hidden="true">
        <img v-if="imageSrc" class="visual-image" :src="imageSrc" :alt="imageAlt" />
      </div>
    </section>

    <section class="auth-side">
      <div class="auth-panel">
        <div v-if="showCopy && (title || description)" class="auth-copy">
          <h1>{{ title }}</h1>
          <p>{{ description }}</p>
        </div>
        <div class="auth-body">
          <slot />
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.auth-screen {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 42%;
  background: #ffffff;
  overflow: hidden;
}

.auth-visual {
  position: relative;
  padding: 28px 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.visual-brand {
  position: absolute;
  top: 28px;
  left: 32px;
  color: #0f172a;
  font-size: 1.36rem;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.visual-stage {
  width: 100%;
  min-height: min(100vh - 56px, 720px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #ffffff;
}

.visual-image {
  width: min(76%, 620px);
  height: auto;
  max-height: 72vh;
  object-fit: contain;
  object-position: center;
}

.auth-side {
  min-height: 100vh;
  background: #ffffff;
  border-left: 1px solid rgba(226, 232, 240, 0.88);
  display: flex;
  align-items: center;
  justify-content: center;
}

.auth-panel {
  width: min(380px, calc(100% - 72px));
  max-height: calc(100vh - 88px);
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-copy {
  flex: 0 0 auto;
  display: grid;
  gap: 8px;
  margin-bottom: 22px;
}

.auth-copy h1 {
  margin: 0;
  font-size: 1.55rem;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.auth-copy p {
  margin: 0;
  color: var(--muted-foreground);
  line-height: 1.7;
  font-size: 0.92rem;
}

.auth-body {
  flex: 0 1 auto;
}

@media (max-width: 980px) {
  .auth-screen {
    grid-template-columns: 1fr;
  }

  .auth-visual {
    padding: 20px;
  }

  .visual-brand {
    top: 20px;
    left: 20px;
    font-size: 1.18rem;
  }

  .visual-stage {
    height: 300px;
    min-height: 300px;
  }

  .visual-image {
    width: min(72%, 360px);
    max-height: 66%;
  }

  .auth-side,
  .auth-panel {
    min-height: auto;
  }

  .auth-panel {
    width: min(420px, calc(100% - 44px));
    max-height: none;
    padding: 28px 0 24px;
  }
}
</style>
