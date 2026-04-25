<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const navItems = [{ label: '开始', routeName: 'dashboard' }]

const activeRouteName = computed(() => route.name)

function goTo(routeName) {
  if (activeRouteName.value !== routeName) {
    router.push({ name: routeName })
  }
}

async function handleLogout() {
  await authStore.logoutAction()
  router.replace('/login')
}
</script>

<template>
  <div class="app-layout">
    <aside class="sidebar surface-card">
      <div class="brand-block">
        <div class="brand-mark">项</div>
        <div class="brand-copy">
          <strong>软件项目管理系统</strong>
        </div>
      </div>

      <nav class="nav-list" aria-label="主导航">
        <button
          v-for="item in navItems"
          :key="item.routeName"
          type="button"
          class="nav-item"
          :class="{ active: activeRouteName === item.routeName }"
          @click="goTo(item.routeName)"
        >
          {{ item.label }}
        </button>
      </nav>

      <div class="sidebar-spacer" />

      <button type="button" class="logout-button" @click="handleLogout">退出登录</button>
    </aside>

    <main class="content-shell">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: 0;
  padding: 0;
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  display: grid;
  grid-template-rows: auto auto 1fr auto;
  gap: 20px;
  padding: 24px 16px;
  border-radius: 0;
  background: #2563eb;
  border: 0;
  box-shadow: none;
}

.brand-block {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border-radius: 16px;
  color: #eff6ff;
  font-size: 1.08rem;
  font-weight: 800;
  background: rgba(255, 255, 255, 0.16);
}

.brand-copy {
  display: flex;
  align-items: center;
}

.brand-copy strong {
  font-size: 1rem;
  color: #ffffff;
}

.nav-list {
  display: grid;
  gap: 10px;
}

.nav-item,
.logout-button {
  min-height: 50px;
  padding: 0 16px;
  border: 0;
  border-radius: 0;
  color: rgba(255, 255, 255, 0.9);
  background: transparent;
  text-align: left;
  font-size: 0.98rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.18s ease;
}

.nav-item:hover,
.logout-button:hover {
  background: rgba(29, 78, 216, 0.55);
}

.nav-item.active {
  color: #ffffff;
  background: #1d4ed8;
}

.sidebar-spacer {
  min-height: 0;
}

.content-shell {
  min-width: 0;
  min-height: 100vh;
}

@media (max-width: 900px) {
  .app-layout {
    grid-template-columns: 1fr;
  }

  .sidebar {
    position: static;
    height: auto;
    grid-template-rows: auto auto auto auto;
  }

  .content-shell {
    min-height: auto;
  }
}
</style>
