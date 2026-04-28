import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { getNotifications, markAllNotificationsRead, markNotificationRead } from '@/api/ops'

const NOTIFICATION_POLL_INTERVAL = 30000

export const useNotificationStore = defineStore('notifications', () => {
  const notifications = ref([])
  const loading = ref(false)
  const markingAll = ref(false)
  const initialized = ref(false)
  let pollTimer = null

  const unreadCount = computed(() =>
    notifications.value.filter((item) => item.readStatus !== 'READ').length,
  )

  async function fetchNotifications(options = {}) {
    const { silent = false } = options

    if (!localStorage.getItem('pm_auth_token')) {
      notifications.value = []
      initialized.value = true
      return []
    }

    if (!silent) {
      loading.value = true
    }

    try {
      const result = await getNotifications()
      notifications.value = Array.isArray(result?.list) ? result.list : []
      initialized.value = true
      return notifications.value
    } catch (error) {
      if (!silent) {
        throw error
      }
      return notifications.value
    } finally {
      if (!silent) {
        loading.value = false
      }
    }
  }

  async function markReadAction(id) {
    if (!id) return
    await markNotificationRead(id)
    notifications.value = notifications.value.map((item) => (
      item.id === id
        ? {
            ...item,
            readStatus: 'READ',
            readAt: item.readAt || new Date().toISOString(),
          }
        : item
    ))
  }

  async function markAllReadAction() {
    if (!unreadCount.value) return
    markingAll.value = true
    try {
      await markAllNotificationsRead()
      const now = new Date().toISOString()
      notifications.value = notifications.value.map((item) => ({
        ...item,
        readStatus: 'READ',
        readAt: item.readAt || now,
      }))
    } finally {
      markingAll.value = false
    }
  }

  function startPolling() {
    if (pollTimer) {
      return
    }
    fetchNotifications({ silent: true })
    pollTimer = window.setInterval(() => {
      fetchNotifications({ silent: true })
    }, NOTIFICATION_POLL_INTERVAL)
  }

  function stopPolling() {
    if (!pollTimer) {
      return
    }
    window.clearInterval(pollTimer)
    pollTimer = null
  }

  function reset() {
    notifications.value = []
    loading.value = false
    markingAll.value = false
    initialized.value = false
    stopPolling()
  }

  return {
    notifications,
    loading,
    markingAll,
    initialized,
    unreadCount,
    fetchNotifications,
    markReadAction,
    markAllReadAction,
    startPolling,
    stopPolling,
    reset,
  }
})
