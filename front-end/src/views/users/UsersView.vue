<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'

import UserAvatar from '@/components/ui/UserAvatar.vue'
import { getUsers } from '@/api/users'

const loading = ref(false)
const keyword = ref('')
const roleFilter = ref('')
const statusFilter = ref('')
const users = ref([])
const total = ref(0)

const roleOptions = [
  { label: '系统管理员', value: 'SYS_ADMIN' },
  { label: '业务用户', value: 'USER' },
]

const statusOptions = [
  { label: '正常', value: 'ACTIVE' },
  { label: '未激活', value: 'INACTIVE' },
  { label: '已锁定', value: 'LOCKED' },
]

const summaryCards = computed(() => {
  const list = users.value

  return [
    {
      label: '用户总数',
      value: total.value || list.length,
      hint: '系统内全部账号',
      tone: 'primary',
    },
    {
      label: '系统管理员',
      value: list.filter((item) => normalizeRoles(item.roles).includes('SYS_ADMIN')).length,
      hint: '负责系统级配置与审计',
      tone: 'admin',
    },
    {
      label: '业务用户',
      value: list.filter((item) => normalizeRoles(item.roles).includes('USER')).length,
      hint: '参与项目业务协作',
      tone: 'business',
    },
    {
      label: '正常账号',
      value: list.filter((item) => item.status === 'ACTIVE').length,
      hint: '当前可正常登录',
      tone: 'success',
    },
  ]
})

const activeFilterLabels = computed(() => {
  const labels = []
  if (roleFilter.value) labels.push(formatRoleLabel(roleFilter.value))
  if (statusFilter.value) labels.push(formatStatusLabel(statusFilter.value))
  if (keyword.value.trim()) labels.push(`关键词：${keyword.value.trim()}`)
  return labels
})

const tableSummaryText = computed(() => {
  const count = total.value || users.value.length
  if (!activeFilterLabels.value.length) {
    return `当前展示 ${count} 个系统账号`
  }
  return `按筛选条件展示 ${count} 个系统账号`
})

function normalizeListResult(result) {
  if (Array.isArray(result)) {
    return {
      list: result,
      total: result.length,
    }
  }

  return {
    list: Array.isArray(result?.list) ? result.list : [],
    total: Number(result?.total || 0),
  }
}

function normalizeRoles(roles) {
  return Array.isArray(roles) ? roles : []
}

function formatRoleLabel(role) {
  const map = {
    SYS_ADMIN: '系统管理员',
    USER: '业务用户',
  }
  return map[role] || role || '-'
}

function roleTagClass(role) {
  const map = {
    SYS_ADMIN: 'role-tag-admin',
    USER: 'role-tag-user',
  }
  return map[role] || 'role-tag-neutral'
}

function formatStatusLabel(status) {
  const map = {
    ACTIVE: '正常',
    INACTIVE: '未激活',
    LOCKED: '已锁定',
  }
  return map[status] || status || '-'
}

function statusClass(status) {
  const map = {
    ACTIVE: 'status-active',
    INACTIVE: 'status-inactive',
    LOCKED: 'status-locked',
  }
  return map[status] || 'status-neutral'
}

function formatLastLogin(value) {
  if (!value) return '暂无记录'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '暂无记录'

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  }).format(date)
}

async function loadUsers() {
  loading.value = true
  try {
    const result = await getUsers({
      keyword: keyword.value.trim(),
      role: roleFilter.value,
      status: statusFilter.value,
      page: 1,
      pageSize: 200,
    })
    const normalized = normalizeListResult(result)
    users.value = normalized.list
    total.value = normalized.total
  } catch (error) {
    ElMessage.error(error.message || '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  keyword.value = ''
  roleFilter.value = ''
  statusFilter.value = ''
  loadUsers()
}

onMounted(loadUsers)
</script>

<template>
  <section class="users-view surface-card">
    <header class="page-header">
      <div>
        <div class="page-eyebrow">用户中心</div>
        <h1>用户管理</h1>
        <p>展示系统内全部用户账号。项目内的成员协作关系由项目创建者在项目内部维护，不在这里按项目拆分。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="loadUsers">刷新</el-button>
      </div>
    </header>

    <section class="summary-grid">
      <article
        v-for="card in summaryCards"
        :key="card.label"
        class="summary-card"
        :class="`summary-${card.tone}`"
      >
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
        <small>{{ card.hint }}</small>
      </article>
    </section>

    <section class="filter-panel">
      <label class="search-box" for="user-keyword">
        <span class="search-icon" aria-hidden="true" />
        <input
          id="user-keyword"
          v-model="keyword"
          type="text"
          placeholder="搜索用户名、姓名、邮箱或手机号"
          @keydown.enter.prevent="loadUsers"
        />
      </label>
      <el-select v-model="roleFilter" clearable placeholder="全部角色" style="width: 180px">
        <el-option
          v-for="item in roleOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select v-model="statusFilter" clearable placeholder="全部状态" style="width: 160px">
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <div class="filter-actions">
        <el-button type="primary" :loading="loading" @click="loadUsers">查询</el-button>
        <el-button @click="resetFilters">重置</el-button>
      </div>
    </section>

    <section class="table-shell">
      <div class="table-head">
        <div class="table-head-copy">
          <strong>账号列表</strong>
          <span>{{ tableSummaryText }}</span>
        </div>
        <div v-if="activeFilterLabels.length" class="table-head-filters">
          <span
            v-for="label in activeFilterLabels"
            :key="label"
            class="filter-chip"
          >
            {{ label }}
          </span>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="users"
        height="100%"
        empty-text="暂无用户数据"
      >
        <el-table-column label="用户信息" min-width="280">
          <template #default="{ row }">
            <div class="user-cell">
              <UserAvatar
                :username="row.username"
                :display-name="row.realName"
                :size="42"
              />
              <div class="user-copy">
                <strong>{{ row.realName || row.username || `用户${row.id}` }}</strong>
                <span>@{{ row.username || '-' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="角色" min-width="220">
          <template #default="{ row }">
            <div class="role-tags">
              <span
                v-for="role in normalizeRoles(row.roles)"
                :key="`${row.id}-${role}`"
                class="role-tag"
                :class="roleTagClass(role)"
              >
                {{ formatRoleLabel(role) }}
              </span>
              <span v-if="!normalizeRoles(row.roles).length" class="role-tag role-tag-empty">
                未分配
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <span class="status-pill" :class="statusClass(row.status)">
              {{ formatStatusLabel(row.status) }}
            </span>
          </template>
        </el-table-column>

        <el-table-column label="联系方式" min-width="260">
          <template #default="{ row }">
            <div class="contact-stack">
              <span>{{ row.email || '未填写邮箱' }}</span>
              <span>{{ row.phone || '未填写手机号' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="最近登录" min-width="180">
          <template #default="{ row }">
            <span class="meta-text">{{ formatLastLogin(row.lastLoginAt) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </section>
  </section>
</template>

<style scoped>
.users-view {
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
  color: #0f172a;
}

.page-header p {
  margin: 0;
  max-width: 760px;
  color: #64748b;
  line-height: 1.65;
}

.header-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  display: grid;
  gap: 6px;
  padding: 18px 16px;
  border: 1px solid #d4d4d8;
  background: #ffffff;
}

.summary-card span {
  color: #64748b;
  font-size: 0.78rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.summary-card strong {
  color: #0f172a;
  font-size: 1.56rem;
  line-height: 1.1;
}

.summary-card small {
  color: #94a3b8;
  font-size: 0.82rem;
  line-height: 1.5;
}

.summary-primary {
  border-top: 3px solid #2563eb;
}

.summary-admin {
  border-top: 3px solid #1d4ed8;
}

.summary-business {
  border-top: 3px solid #0f766e;
}

.summary-success {
  border-top: 3px solid #15803d;
}

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-box {
  width: min(440px, 100%);
  height: 46px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
  border: 1px solid #a3a3a3;
  background: #fff;
}

.search-icon {
  position: relative;
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
}

.search-icon::before {
  content: '';
  position: absolute;
  inset: 0;
  border: 2px solid #2563eb;
  border-radius: 999px;
}

.search-icon::after {
  content: '';
  position: absolute;
  right: -4px;
  bottom: -1px;
  width: 10px;
  height: 2px;
  background: #2563eb;
  transform: rotate(45deg);
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--foreground);
}

.filter-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-left: auto;
}

.table-shell {
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
  border: 1px solid #d4d4d8;
  background: #ffffff;
}

.table-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #ffffff;
}

.table-head-copy {
  display: grid;
  gap: 4px;
}

.table-head-copy strong {
  color: #0f172a;
  font-size: 1rem;
  font-weight: 600;
}

.table-head-copy span {
  color: #64748b;
  font-size: 0.84rem;
}

.table-head-filters {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.filter-chip {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  color: #1d4ed8;
  background: #eff6ff;
  font-size: 0.78rem;
  font-weight: 600;
}

.user-cell {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.user-copy {
  display: grid;
  gap: 4px;
  min-width: 0;
}

.user-copy strong {
  color: #111827;
  font-size: 14px;
  line-height: 1.3;
}

.user-copy span,
.meta-text {
  color: #667085;
  font-size: 12px;
  line-height: 1.45;
}

.role-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-tag {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  font-size: 12px;
  font-weight: 700;
}

.role-tag-admin {
  color: #1d4ed8;
  background: #dbeafe;
}

.role-tag-user {
  color: #0f766e;
  background: #ccfbf1;
}

.role-tag-neutral,
.role-tag-empty {
  color: #667085;
  background: #f3f4f6;
}

.status-pill {
  display: inline-flex;
  min-width: 64px;
  justify-content: center;
  padding: 3px 10px;
  font-size: 0.78rem;
  font-weight: 700;
}

.status-active {
  color: #166534;
  background: #dcfce7;
}

.status-inactive {
  color: #92400e;
  background: #fef3c7;
}

.status-locked {
  color: #9f1239;
  background: #ffe4e6;
}

.status-neutral {
  color: #475467;
  background: #f3f4f6;
}

.contact-stack {
  display: grid;
  gap: 4px;
}

.contact-stack span {
  color: #667085;
  font-size: 12px;
  line-height: 1.45;
}

.users-view :deep(.el-button),
.users-view :deep(.el-input__wrapper),
.users-view :deep(.el-select__wrapper) {
  border-radius: 0;
}

.users-view :deep(.el-table) {
  --el-table-border-color: #e5e7eb;
}

.users-view :deep(.el-table th.el-table__cell) {
  background: #f8fafc;
  color: #475467;
  font-weight: 600;
}

.users-view :deep(.el-table td.el-table__cell) {
  padding-top: 14px;
  padding-bottom: 14px;
}

.users-view :deep(.el-table__row:hover > td.el-table__cell) {
  background: #f8fbff;
}

@media (max-width: 1100px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .page-header,
  .table-head {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-actions {
    margin-left: 0;
  }

  .table-head-filters {
    justify-content: flex-start;
  }
}

@media (max-width: 640px) {
  .users-view {
    padding: 18px 16px 24px;
  }

  .page-header h1 {
    font-size: 1.6rem;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .search-box {
    width: 100%;
  }

  .filter-actions {
    width: 100%;
  }

  .filter-actions :deep(.el-button) {
    flex: 1 1 0;
  }
}
</style>
