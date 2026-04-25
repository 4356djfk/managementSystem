import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { getCurrentUser, login, logout, register } from '@/api/auth'

const TOKEN_KEY = 'pm_auth_token'
const USER_KEY = 'pm_auth_user'
const SESSION_EXPIRED_REDIRECT_FLAG = 'pm_session_expired_redirecting'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(parseStoredUser())
  const initialized = ref(false)
  const loading = ref(false)

  const isAuthenticated = computed(() => Boolean(token.value))
  const displayName = computed(() => user.value?.realName || user.value?.username || '未登录')

  function persistSession(nextToken, nextUser) {
    token.value = nextToken || ''
    user.value = nextUser || null

    if (token.value) {
      localStorage.setItem(TOKEN_KEY, token.value)
    } else {
      localStorage.removeItem(TOKEN_KEY)
    }

    if (user.value) {
      localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    } else {
      localStorage.removeItem(USER_KEY)
    }

    if (token.value) {
      sessionStorage.removeItem(SESSION_EXPIRED_REDIRECT_FLAG)
    }
  }

  async function bootstrap() {
    if (initialized.value) {
      return
    }

    if (!token.value) {
      initialized.value = true
      return
    }

    try {
      const profile = await getCurrentUser()
      persistSession(token.value, profile)
    } catch (error) {
      persistSession('', null)
    } finally {
      initialized.value = true
    }
  }

  async function loginAction(payload) {
    loading.value = true

    try {
      const result = await login(payload)
      persistSession(result.token, result.user)
      initialized.value = true
      return result
    } finally {
      loading.value = false
    }
  }

  async function registerAction(payload) {
    loading.value = true

    try {
      const result = await register(payload)
      return result
    } finally {
      loading.value = false
    }
  }

  async function logoutAction() {
    try {
      if (token.value) {
        await logout()
      }
    } finally {
      persistSession('', null)
      initialized.value = true
      sessionStorage.removeItem(SESSION_EXPIRED_REDIRECT_FLAG)
    }
  }

  return {
    token,
    user,
    initialized,
    loading,
    isAuthenticated,
    displayName,
    bootstrap,
    loginAction,
    registerAction,
    logoutAction,
  }
})

function parseStoredUser() {
  const raw = localStorage.getItem(USER_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    localStorage.removeItem(USER_KEY)
    return null
  }
}
