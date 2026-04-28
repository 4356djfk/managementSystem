import { createRouter, createWebHistory } from 'vue-router'

import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/',
    redirect: '/dashboard',
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: {
      guestOnly: true,
    },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/auth/RegisterView.vue'),
    meta: {
      guestOnly: true,
    },
  },
  {
    path: '/dashboard',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: {
      requiresAuth: true,
    },
    children: [
      {
        path: '',
        name: 'dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
      },
      {
        path: 'templates',
        name: 'project-templates',
        component: () => import('@/views/templates/TemplatesView.vue'),
        meta: {
          allowedRoles: ['SYS_ADMIN'],
        },
      },
      {
        path: 'users',
        name: 'users',
        component: () => import('@/views/users/UsersView.vue'),
        meta: {
          allowedRoles: ['SYS_ADMIN'],
        },
      },
      {
        path: 'calendar',
        name: 'my-calendar',
        component: () => import('@/views/calendar/MyCalendarView.vue'),
        meta: {
          allowedRoles: ['USER'],
        },
      },
      {
        path: 'search',
        name: 'global-search',
        component: () => import('@/views/search/SearchView.vue'),
        meta: {
          allowedRoles: ['USER'],
        },
      },
      {
        path: 'audit',
        name: 'audit-logs',
        component: () => import('@/views/audit/AuditLogsView.vue'),
        meta: {
          allowedRoles: ['SYS_ADMIN'],
        },
      },
      {
        path: 'notifications',
        name: 'notifications',
        component: () => import('@/views/notifications/NotificationsView.vue'),
      },
      {
        path: 'profile',
        name: 'profile',
        component: () => import('@/views/profile/ProfileView.vue'),
      },
    ],
  },
  {
    path: '/editor/:projectId',
    name: 'project-editor',
    component: () => import('@/views/editor/ProjectEditorView.vue'),
    meta: {
      requiresAuth: true,
      allowedRoles: ['USER'],
    },
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()

  if (!authStore.initialized) {
    await authStore.bootstrap()
  }

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      name: 'login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return {
      name: 'dashboard',
    }
  }

  const currentRoleCodes = Array.isArray(authStore.user?.roleCodes || authStore.user?.roles)
    ? (authStore.user?.roleCodes || authStore.user?.roles)
    : []
  const allowedRoles = to.matched.flatMap((record) => (
    Array.isArray(record.meta?.allowedRoles) ? record.meta.allowedRoles : []
  ))

  if (allowedRoles.length && !allowedRoles.some((role) => currentRoleCodes.includes(role))) {
    return {
      name: 'dashboard',
    }
  }

  return true
})

export default router
