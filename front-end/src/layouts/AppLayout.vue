<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { storeToRefs } from 'pinia'
import { useRoute, useRouter } from 'vue-router'

import UserAvatar from '@/components/ui/UserAvatar.vue'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notifications'
import { loadRecentProjects, RECENT_PROJECTS_UPDATED_EVENT, recordRecentProject } from '@/utils/recentProjects'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const { unreadCount } = storeToRefs(notificationStore)

const navItems = [
  { label: '首页', routeName: 'dashboard' },
  { label: '模板', routeName: 'project-templates', adminOnly: true },
  { label: '用户管理', routeName: 'users', adminOnly: true },
  { label: '日历', routeName: 'my-calendar', businessOnly: true },
  { label: '搜索', routeName: 'global-search', businessOnly: true },
  { label: '审计', routeName: 'audit-logs', adminOnly: true },
  { label: '通知', routeName: 'notifications' },
]

const recentProjects = ref(loadRecentProjects())

const notificationUnreadCountLabel = computed(() => (
  unreadCount.value > 99 ? '99+' : String(unreadCount.value || '')
))

const activeRouteName = computed(() => route.name)
const currentUser = computed(() => authStore.user || null)
const currentUserRoleCodes = computed(() => {
  const raw = currentUser.value?.roleCodes || currentUser.value?.roles
  return Array.isArray(raw) ? raw : []
})
const hasBusinessRole = computed(() => currentUserRoleCodes.value.includes('USER'))
const isSysAdmin = computed(() => currentUserRoleCodes.value.includes('SYS_ADMIN'))
const currentUserDisplayName = computed(() => (
  currentUser.value?.realName || currentUser.value?.username || '未登录'
))
const sidebarRecentProjects = computed(() => recentProjects.value.slice(0, 3))
const visibleNavItems = computed(() => navItems.filter((item) => {
  if (item.adminOnly && !isSysAdmin.value) return false
  if (item.businessOnly && !hasBusinessRole.value) return false
  return true
}))

function syncRecentProjects() {
  recentProjects.value = loadRecentProjects()
}

function handleRecentProjectsUpdated(event) {
  recentProjects.value = Array.isArray(event?.detail) ? event.detail : loadRecentProjects()
}

function goTo(routeName) {
  if (activeRouteName.value !== routeName) {
    router.push({ name: routeName })
  }
}

function openRecentProject(project) {
  if (!hasBusinessRole.value) {
    return
  }

  recordRecentProject(project)
  router.push({
    name: 'project-editor',
    params: { projectId: project.projectId },
  })
}

function formatRecentProjectTime(value) {
  if (!value) return '最近打开时间未知'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '最近打开时间未知'

  const formatter = new Intl.DateTimeFormat('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })

  return `最近打开 ${formatter.format(date)}`
}

async function handleLogout() {
  await authStore.logoutAction()
  notificationStore.reset()
  router.replace('/login')
}

async function handleUserCommand(command) {
  if (command === 'logout') {
    await handleLogout()
  }
}

onMounted(() => {
  syncRecentProjects()
  notificationStore.startPolling()
  window.addEventListener(RECENT_PROJECTS_UPDATED_EVENT, handleRecentProjectsUpdated)
  window.addEventListener('storage', syncRecentProjects)
})

onBeforeUnmount(() => {
  notificationStore.stopPolling()
  window.removeEventListener(RECENT_PROJECTS_UPDATED_EVENT, handleRecentProjectsUpdated)
  window.removeEventListener('storage', syncRecentProjects)
})
</script>

<template>
  <div class="app-layout">
    <aside class="sidebar">
      <div class="brand-block">
        <strong>软件项目管理</strong>
      </div>

      <nav class="nav-list" aria-label="主导航">
        <button
          v-for="item in visibleNavItems"
          :key="item.routeName"
          type="button"
          class="nav-item"
          :class="{ active: activeRouteName === item.routeName }"
          @click="goTo(item.routeName)"
        >
          <span class="nav-item-label">{{ item.label }}</span>
          <span
            v-if="item.routeName === 'notifications' && unreadCount"
            class="nav-badge"
          >
            {{ notificationUnreadCountLabel }}
          </span>
        </button>
      </nav>

      <div class="sidebar-spacer" />

      <section
        v-if="hasBusinessRole"
        class="recent-section"
        aria-label="最近项目"
      >
        <div class="recent-section-label">最近项目</div>

        <div v-if="sidebarRecentProjects.length" class="recent-project-list">
          <template
            v-for="(project, index) in sidebarRecentProjects"
            :key="project.projectId"
          >
            <button
              type="button"
              class="recent-project-item"
              @click="openRecentProject(project)"
            >
              <span class="recent-project-copy">
                <strong>{{ project.name }}</strong>
                <span>{{ formatRecentProjectTime(project.lastOpenedAt) }}</span>
              </span>
              <span class="recent-project-arrow" aria-hidden="true">›</span>
            </button>
            <el-divider
              v-if="index < sidebarRecentProjects.length - 1"
              class="recent-project-divider"
            />
          </template>
        </div>

        <div v-else class="recent-empty">
          最近打开的项目会显示在这里
        </div>
      </section>

      <section
        v-else-if="isSysAdmin"
        class="recent-section system-scope"
        aria-label="系统职责"
      >
        <div class="recent-section-label">系统职责</div>
        <div class="system-scope-card">
          <strong>当前账号为系统管理员</strong>
          <span>负责用户、模板、审计等系统级管理，不直接参与项目业务。</span>
        </div>
      </section>

      <section
        v-else
        class="recent-section system-scope"
        aria-label="账号提示"
      >
        <div class="recent-section-label">账号提示</div>
        <div class="system-scope-card">
          <strong>当前账号没有项目角色</strong>
          <span>如需进入项目工作台，请为该账号分配业务角色。</span>
        </div>
      </section>

      <div class="sidebar-bottom">
        <div
          v-if="currentUser"
          class="user-entry"
          :class="{ active: activeRouteName === 'profile' }"
        >
          <button type="button" class="user-entry-main" @click="goTo('profile')">
            <UserAvatar
              :avatar-url="currentUser.avatarUrl"
              :username="currentUser.username"
              :display-name="currentUserDisplayName"
              :size="42"
            />
            <strong>{{ currentUserDisplayName }}</strong>
          </button>

          <el-dropdown trigger="click" @command="handleUserCommand">
            <button type="button" class="user-menu-trigger" aria-label="用户菜单" @click.stop>
              <span class="menu-dots" aria-hidden="true">
                <span />
                <span />
                <span />
              </span>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
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
  grid-template-columns: 332px minmax(0, 1fr);
  gap: 0;
  padding: 0;
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  display: grid;
  grid-template-rows: auto auto 1fr auto auto;
  gap: 0;
  padding: 18px 0 0;
  color: #eff6ff;
  background: #2f6fe4;
  border-right: 1px solid rgba(15, 23, 42, 0.08);
  box-shadow: none;
}

.brand-block {
  padding: 0 18px 14px;
}

.brand-block strong {
  display: block;
  font-size: 1.12rem;
  line-height: 1.2;
  font-weight: 700;
  color: #ffffff;
  white-space: nowrap;
}

.nav-list {
  display: grid;
  gap: 0;
  padding-top: 4px;
}

.nav-item {
  width: 100%;
  min-height: 58px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 0 18px;
  border: 0;
  border-radius: 0;
  color: rgba(255, 255, 255, 0.9);
  background: transparent;
  text-align: left;
  font-size: 1.08rem;
  font-weight: 700;
  cursor: pointer;
  transition: background 0.18s ease;
}

.nav-item-label {
  min-width: 0;
}

.nav-badge {
  min-width: 26px;
  height: 26px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 8px;
  border-radius: 999px;
  color: #1d4ed8;
  background: #ffffff;
  font-size: 0.78rem;
  font-weight: 800;
  line-height: 1;
}

.nav-item:hover {
  background: rgba(24, 59, 145, 0.45);
}

.nav-item.active {
  color: #ffffff;
  background: #1f56c2;
}

.nav-item.active .nav-badge {
  color: #1f56c2;
  background: #dbeafe;
}

.recent-section {
  display: grid;
  gap: 0;
  padding: 12px 0 28px;
}

.recent-section-label {
  padding: 10px 18px 8px;
  color: rgba(219, 234, 254, 0.66);
  font-size: 0.82rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.recent-project-list {
  display: grid;
  gap: 0;
}

.recent-project-item {
  width: 100%;
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  min-height: 60px;
  padding: 12px 18px;
  border: 0;
  color: #ffffff;
  background: transparent;
  text-align: left;
  cursor: pointer;
  transition: background 0.18s ease;
}

.recent-project-item:hover {
  background: rgba(24, 59, 145, 0.26);
}

.recent-project-copy {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.recent-project-copy strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 0.96rem;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.92);
}

.recent-project-copy span {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: rgba(219, 234, 254, 0.68);
  font-size: 0.78rem;
  font-weight: 500;
}

.recent-project-arrow {
  color: rgba(255, 255, 255, 0.62);
  font-size: 1.02rem;
  font-weight: 600;
  line-height: 1;
}

.recent-empty {
  display: grid;
  align-items: center;
  min-height: 64px;
  padding: 0 18px;
  color: rgba(219, 234, 254, 0.66);
  font-size: 0.76rem;
  background: transparent;
}

.system-scope {
  align-content: start;
}

.system-scope-card {
  display: grid;
  gap: 8px;
  margin: 0 18px;
  padding: 16px;
  color: #ffffff;
  background: rgba(24, 59, 145, 0.35);
  border: 1px solid rgba(255, 255, 255, 0.14);
}

.system-scope-card strong {
  font-size: 0.94rem;
  font-weight: 700;
}

.system-scope-card span {
  color: rgba(219, 234, 254, 0.82);
  font-size: 0.8rem;
  line-height: 1.6;
}

.recent-project-divider {
  margin: 0;
  border-top-color: rgba(255, 255, 255, 0.12);
}

.recent-project-list :deep(.el-divider--horizontal) {
  margin: 0;
  border-top-color: rgba(255, 255, 255, 0.12);
}

.sidebar-spacer {
  min-height: 0;
}

.sidebar-bottom {
  display: grid;
  gap: 0;
}

.user-entry {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: stretch;
  gap: 0;
  min-height: 64px;
  color: #1f2937;
  background: #ffffff;
}

.user-entry-main {
  min-width: 0;
  min-height: 64px;
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border: 0;
  color: inherit;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.user-entry-main strong {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 1.02rem;
  font-weight: 700;
}

.user-menu-trigger {
  width: 52px;
  height: 64px;
  display: grid;
  place-items: center;
  border: 0;
  border-left: 1px solid #e5e7eb;
  color: #475467;
  background: #ffffff;
  cursor: pointer;
  transition: background 0.18s ease;
}

.user-menu-trigger:hover {
  background: #f8fafc;
}

.menu-dots {
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

.menu-dots span {
  width: 4px;
  height: 4px;
  border-radius: 999px;
  background: currentColor;
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
    grid-template-rows: auto auto auto auto auto;
  }

  .content-shell {
    min-height: auto;
  }
}

@media (max-width: 640px) {
  .sidebar {
    padding: 14px 0 0;
  }

  .brand-block {
    padding: 0 14px 12px;
  }

  .nav-item {
    min-height: 46px;
  }

  .recent-section-label,
  .recent-project-item,
  .recent-empty,
  .system-scope-card {
    padding-left: 14px;
    padding-right: 14px;
  }

  .system-scope-card {
    margin: 0 14px;
  }

  .user-entry-main {
    padding-left: 14px;
    padding-right: 14px;
  }
}
</style>
