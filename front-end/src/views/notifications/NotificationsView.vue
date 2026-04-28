<script setup>
import { onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { useNotificationStore } from '@/stores/notifications'

const router = useRouter()
const notificationStore = useNotificationStore()
const { loading, markingAll, notifications, unreadCount } = storeToRefs(notificationStore)

function formatDateTime(value) {
  if (!value) return '时间未知'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '时间未知'

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}

function formatBizType(value) {
  const map = {
    PROJECT: '项目',
    TASK: '任务',
    RISK: '风险',
    CHANGE: '变更',
    CHANGE_REQUEST: '变更',
    COST: '成本',
    TIMESHEET: '工时',
    REPORT: '报表',
    EXPORT_TASK: '导出',
    PROJECT_MEMBER: '成员',
    MILESTONE: '里程碑',
    ACCEPTANCE_ITEM: '验收',
    NOTIFICATION: '通知',
  }
  return map[value] || value || '系统'
}

async function loadNotifications() {
  try {
    await notificationStore.fetchNotifications()
  } catch (error) {
    ElMessage.error(error.message || '通知加载失败')
  }
}

async function handleMarkRead(notification) {
  if (!notification?.id || notification.readStatus === 'READ') return

  try {
    await notificationStore.markReadAction(notification.id)
  } catch (error) {
    ElMessage.error(error.message || '通知已读失败')
  }
}

async function handleMarkAllRead() {
  if (!unreadCount.value) return

  try {
    await notificationStore.markAllReadAction()
    ElMessage.success('通知已全部标记为已读')
  } catch (error) {
    ElMessage.error(error.message || '批量已读失败')
  }
}

async function openNotification(notification) {
  await handleMarkRead(notification)

  if (notification?.projectId) {
    router.push({
      name: 'project-editor',
      params: { projectId: String(notification.projectId) },
    })
    return
  }

  ElMessage.info('该通知没有关联项目')
}

onMounted(loadNotifications)
</script>

<template>
  <section class="notifications-view surface-card">
    <header class="page-header">
      <div>
        <div class="page-eyebrow">通知中心</div>
        <h1>站内通知</h1>
        <p>查看系统提醒、变更结果和待办更新。</p>
      </div>
      <div class="header-actions">
        <div class="unread-pill">
          未读 {{ unreadCount }}
        </div>
        <el-button :loading="loading" @click="loadNotifications">刷新</el-button>
        <el-button type="primary" :disabled="!unreadCount" :loading="markingAll" @click="handleMarkAllRead">
          全部已读
        </el-button>
      </div>
    </header>

    <div v-if="loading" class="state-block">
      <strong>正在加载通知</strong>
      <span>请稍候。</span>
    </div>

    <div v-else-if="!notifications.length" class="state-block">
      <strong>暂无通知</strong>
      <span>后续的审批、提醒和状态更新会显示在这里。</span>
    </div>

    <div v-else class="notification-list">
      <article
        v-for="item in notifications"
        :key="item.id"
        class="notification-card"
        :class="{ unread: item.readStatus !== 'READ' }"
      >
        <div class="notification-main" @click="openNotification(item)">
          <div class="notification-head">
            <div class="notification-title-row">
              <strong>{{ item.title || '系统通知' }}</strong>
              <span class="biz-tag">{{ formatBizType(item.bizType) }}</span>
            </div>
            <span class="time-label">{{ formatDateTime(item.createdAt) }}</span>
          </div>
          <p>{{ item.content || '暂无内容' }}</p>
          <div class="notification-meta">
            <span v-if="item.projectId">关联项目 #{{ item.projectId }}</span>
            <span>{{ item.readStatus === 'READ' ? '已读' : '未读' }}</span>
          </div>
        </div>
        <div class="notification-actions">
          <el-button
            v-if="item.readStatus !== 'READ'"
            size="small"
            type="primary"
            plain
            @click="handleMarkRead(item)"
          >
            标记已读
          </el-button>
          <el-button
            size="small"
            :type="item.projectId ? 'primary' : 'default'"
            @click="openNotification(item)"
          >
            {{ item.projectId ? '打开项目' : '查看' }}
          </el-button>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.notifications-view {
  min-height: 100vh;
  display: grid;
  align-content: start;
  gap: 20px;
  padding: 24px;
  border-radius: 0;
  background: #f3f3f3;
  border: 0;
  box-shadow: none;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
}

.page-eyebrow {
  color: #2563eb;
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 6px 0 10px;
  font-size: 2rem;
  line-height: 1.08;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: flex-end;
  align-items: center;
}

.unread-pill {
  min-width: 84px;
  padding: 9px 14px;
  border-radius: 999px;
  color: #1d4ed8;
  background: #dbeafe;
  font-weight: 700;
  text-align: center;
}

.state-block {
  display: grid;
  gap: 6px;
  padding: 26px 24px;
  border: 1px solid #d4d4d8;
  background: #ffffff;
}

.state-block strong {
  font-size: 1rem;
}

.state-block span {
  color: #64748b;
}

.notification-list {
  display: grid;
  gap: 14px;
}

.notification-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  padding: 18px 20px;
  border: 1px solid #d4d4d8;
  background: #ffffff;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.notification-card.unread {
  border-color: #93c5fd;
  box-shadow: 0 0 0 1px rgba(37, 99, 235, 0.08);
}

.notification-main {
  display: grid;
  gap: 10px;
  cursor: pointer;
}

.notification-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.notification-title-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.notification-title-row strong {
  font-size: 1rem;
}

.biz-tag {
  padding: 2px 8px;
  border-radius: 999px;
  color: #475569;
  background: #e2e8f0;
  font-size: 0.75rem;
  font-weight: 700;
}

.time-label {
  color: #64748b;
  font-size: 0.82rem;
  white-space: nowrap;
}

.notification-main p {
  margin: 0;
  color: #334155;
  line-height: 1.65;
}

.notification-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  color: #64748b;
  font-size: 0.85rem;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
  justify-content: center;
}

@media (max-width: 900px) {
  .page-header,
  .notification-card {
    grid-template-columns: 1fr;
  }

  .notification-actions {
    flex-direction: row;
    justify-content: flex-start;
    flex-wrap: wrap;
  }
}

@media (max-width: 640px) {
  .notifications-view {
    padding: 18px 16px 24px;
  }

  .page-header h1 {
    font-size: 1.6rem;
  }
}
</style>
