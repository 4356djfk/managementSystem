<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { getAuditLogs } from '@/api/ops'

const router = useRouter()

const moduleOptions = [
  { label: '全部模块', value: '' },
  { label: '认证', value: 'AUTH' },
  { label: '用户', value: 'USER' },
  { label: '项目模板', value: 'PROJECT_TEMPLATE' },
  { label: '通知', value: 'NOTIFICATION' },
  { label: '导出', value: 'EXPORT' },
  { label: '导入', value: 'IMPORT' },
  { label: '附件', value: 'ATTACHMENT' },
  { label: '编辑器偏好', value: 'EDITOR_PREFERENCE' },
  { label: '项目成员', value: 'PROJECT_MEMBER' },
  { label: '项目章程', value: 'PROJECT_CHARTER' },
  { label: '工作分解', value: 'WBS' },
  { label: '需求', value: 'REQUIREMENT' },
  { label: '里程碑', value: 'MILESTONE' },
  { label: '任务', value: 'TASK' },
  { label: '风险', value: 'RISK' },
  { label: '成本', value: 'COST' },
  { label: '工时', value: 'TIMESHEET' },
  { label: '变更', value: 'CHANGE' },
  { label: '范围基线', value: 'SCOPE_BASELINE' },
  { label: '验收', value: 'ACCEPTANCE' },
  { label: '归档', value: 'ARCHIVE' },
  { label: '经验教训', value: 'LESSON' },
  { label: '报表', value: 'REPORT' },
  { label: '日历', value: 'CALENDAR' },
  { label: '结项', value: 'CLOSURE' },
  { label: '项目', value: 'PROJECT' },
  { label: '系统', value: 'SYSTEM' },
]

const loading = ref(false)
const logs = ref([])
const form = reactive({
  module: '',
  startTime: '',
  endTime: '',
})

function formatDateTime(value) {
  if (!value) return '-'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '-'

  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
  }).format(date)
}

function formatModuleLabel(value) {
  if (!value) return '-'
  return moduleOptions.find((item) => item.value === String(value))?.label || value
}

async function loadLogs() {
  loading.value = true
  try {
    const pageResult = await getAuditLogs({
      module: form.module,
      startTime: form.startTime || null,
      endTime: form.endTime || null,
      page: 1,
      pageSize: 200,
    })
    logs.value = Array.isArray(pageResult?.list) ? pageResult.list : []
  } catch (error) {
    ElMessage.error(error.message || '审计日志加载失败')
  } finally {
    loading.value = false
  }
}

function openProject(item) {
  if (!item?.projectId) {
    ElMessage.info('该日志没有关联项目')
    return
  }

  router.push({
    name: 'project-editor',
    params: { projectId: String(item.projectId) },
  })
}
</script>

<template>
  <section class="audit-view surface-card">
    <header class="page-header">
      <div>
        <div class="page-eyebrow">审计日志</div>
        <h1>操作审计</h1>
        <p>先开放按模块和时间范围查询的基础日志视图，便于快速追踪关键操作。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="loadLogs">查询</el-button>
      </div>
    </header>

    <section class="filter-panel">
      <el-select v-model="form.module" placeholder="选择模块" style="width: 220px">
        <el-option
          v-for="item in moduleOptions"
          :key="item.value || 'all'"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="开始时间" />
      <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="结束时间" />
      <el-button type="primary" :loading="loading" @click="loadLogs">查询</el-button>
    </section>

    <div v-if="!logs.length" class="state-block">
      <strong>暂无审计日志</strong>
      <span>可以设置筛选条件后开始查询。</span>
    </div>

    <div v-else class="table-shell">
      <el-table :data="logs" v-loading="loading" empty-text="暂无审计日志">
        <el-table-column label="模块" width="120">
          <template #default="{ row }">
            {{ formatModuleLabel(row.moduleName) }}
          </template>
        </el-table-column>
        <el-table-column prop="action" label="动作" width="140" />
        <el-table-column label="操作人" min-width="140">
          <template #default="{ row }">
            {{ row.operatorName || row.operatorId || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="请求" min-width="240">
          <template #default="{ row }">
            {{ row.requestMethod || '-' }} {{ row.requestPath || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="结果" width="120">
          <template #default="{ row }">
            {{ row.resultCode ?? '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP" width="150" />
        <el-table-column label="时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="项目" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.projectId" link type="primary" @click="openProject(row)">
              #{{ row.projectId }}
            </el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </section>
</template>

<style scoped>
.audit-view {
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

.filter-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
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

.table-shell {
  overflow: hidden;
  border: 1px solid #d4d4d8;
  background: #ffffff;
}

@media (max-width: 640px) {
  .audit-view {
    padding: 18px 16px 24px;
  }

  .page-header h1 {
    font-size: 1.6rem;
  }
}
</style>
