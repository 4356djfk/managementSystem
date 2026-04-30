import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

const VUE_ECOSYSTEM_PACKAGES = [
  '/vue/',
  '/@vue/',
  '/vue-router/',
  '/pinia/',
]

const ELEMENT_PLUS_ECOSYSTEM_PACKAGES = [
  '/element-plus/',
  '/@element-plus/',
  '/@floating-ui/',
  '/async-validator/',
  '/dayjs/',
  '/lodash/',
  '/lodash-es/',
  '/lodash-unified/',
  '/memoize-one/',
  '/normalize-wheel-es/',
  '/escape-html/',
]

function resolveElementPlusComponentChunkName(id) {
  const marker = '/element-plus/es/components/'
  const markerIndex = id.indexOf(marker)
  if (markerIndex < 0) {
    return null
  }
  const componentPath = id.slice(markerIndex + marker.length)
  const componentName = componentPath.split('/')[0]
  if (!componentName) {
    return 'vendor-ep-core'
  }
  const initial = componentName[0].toLowerCase()
  if (initial <= 'b') return 'vendor-ep-a-b'
  if (initial <= 'd') return 'vendor-ep-c-d'
  if (initial <= 'k') return 'vendor-ep-e-k'
  if (initial <= 'r') return 'vendor-ep-l-r'
  return 'vendor-ep-s-z'
}

function resolveVendorChunkName(id) {
  if (!id.includes('node_modules')) {
    return null
  }
  if (VUE_ECOSYSTEM_PACKAGES.some((token) => id.includes(token))) {
    return 'vendor-vue'
  }
  if (ELEMENT_PLUS_ECOSYSTEM_PACKAGES.some((token) => id.includes(token))) {
    return resolveElementPlusComponentChunkName(id) || 'vendor-ep-core'
  }
  return 'vendor-misc'
}

// https://vite.dev/config/
export default defineConfig(({ command }) => ({
  plugins: [
    vue(),
    command === 'serve' ? vueDevTools() : null,
  ].filter(Boolean),
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          return resolveVendorChunkName(id)
        },
      },
    },
  },
}))
