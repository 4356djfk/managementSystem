<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

import {
  createProjectTemplate,
  getProjectTemplateDetail,
  getProjectTemplates,
  updateProjectTemplate,
} from '@/api/projects'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const detailLoading = ref(false)
const dialogVisible = ref(false)
const editingTemplateId = ref(null)
const templates = ref([])
const currentRoleCodes = computed(() => {
  const raw = authStore.user?.roleCodes || authStore.user?.roles
  return Array.isArray(raw) ? raw : []
})
const canManageTemplates = computed(() => currentRoleCodes.value.includes('SYS_ADMIN'))

const filters = reactive({
  keyword: '',
  type: '',
  status: '',
})

const templateForm = reactive({
  name: '',
  type: 'AGILE',
  description: '',
  status: 'ENABLED',
  isSystem: false,
})

const typeOptions = [
  { label: '敏捷开发', value: 'AGILE' },
  { label: '瀑布开发', value: 'WATERFALL' },
  { label: '迭代开发', value: 'ITERATIVE' },
  { label: '混合模式', value: 'HYBRID' },
]

const statusOptions = [
  { label: '启用', value: 'ENABLED' },
  { label: '停用', value: 'DISABLED' },
]

const filteredTemplates = computed(() => {
  const keyword = filters.keyword.trim().toLowerCase()
  if (!keyword) return templates.value

  return templates.value.filter((item) =>
    [item.name, item.type, item.description]
      .filter(Boolean)
      .some((field) => String(field).toLowerCase().includes(keyword)),
  )
})

const dialogTitle = computed(() =>
  editingTemplateId.value ? '编辑项目模板' : '新建项目模板',
)

function formatTemplateType(value) {
  return typeOptions.find((item) => item.value === value)?.label || value || '-'
}

function formatTemplateStatus(value) {
  return statusOptions.find((item) => item.value === value)?.label || value || '-'
}

function resetTemplateForm() {
  templateForm.name = ''
  templateForm.type = 'AGILE'
  templateForm.description = ''
  templateForm.status = 'ENABLED'
  templateForm.isSystem = false
  editingTemplateId.value = null
}

async function loadTemplates() {
  loading.value = true
  try {
    const result = await getProjectTemplates({
      page: 1,
      pageSize: 200,
      type: filters.type,
      status: filters.status,
    })
    templates.value = Array.isArray(result?.list) ? result.list : []
  } catch (error) {
    ElMessage.error(error.message || '项目模板加载失败')
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  if (!canManageTemplates.value) {
    ElMessage.warning('当前账号只能查看模板')
    return
  }
  resetTemplateForm()
  dialogVisible.value = true
}

async function openEditDialog(item) {
  if (!canManageTemplates.value) {
    ElMessage.warning('当前账号只能查看模板')
    return
  }
  dialogVisible.value = true
  detailLoading.value = true
  resetTemplateForm()

  try {
    const detail = await getProjectTemplateDetail(item.id)
    editingTemplateId.value = detail.id
    templateForm.name = detail.name || ''
    templateForm.type = detail.type || 'AGILE'
    templateForm.description = detail.description || ''
    templateForm.status = detail.status || 'ENABLED'
    templateForm.isSystem = Number(detail.isSystem || 0) === 1
  } catch (error) {
    dialogVisible.value = false
    ElMessage.error(error.message || '模板详情加载失败')
  } finally {
    detailLoading.value = false
  }
}

async function submitTemplate() {
  if (!canManageTemplates.value) {
    ElMessage.warning('当前账号只能查看模板')
    return
  }
  if (!templateForm.name.trim()) {
    ElMessage.warning('请输入模板名称')
    return
  }

  saving.value = true
  try {
    const payload = {
      name: templateForm.name.trim(),
      type: templateForm.type,
      description: templateForm.description.trim(),
      status: templateForm.status,
      isSystem: templateForm.isSystem ? 1 : 0,
    }

    if (editingTemplateId.value) {
      await updateProjectTemplate(editingTemplateId.value, payload)
      ElMessage.success('模板已更新')
    } else {
      await createProjectTemplate(payload)
      ElMessage.success('模板已创建')
    }

    dialogVisible.value = false
    await loadTemplates()
  } catch (error) {
    ElMessage.error(error.message || '模板保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(loadTemplates)
</script>

<template>
  <section class="templates-view surface-card">
    <header class="page-header">
      <div>
        <div class="page-eyebrow">模板中心</div>
        <h1>项目模板管理</h1>
        <p>先开放模板查询、创建和编辑，便于后续规范化建项。</p>
      </div>
      <div class="header-actions">
        <el-button :loading="loading" @click="loadTemplates">刷新</el-button>
        <el-button v-if="canManageTemplates" type="primary" @click="openCreateDialog">新建模板</el-button>
      </div>
    </header>

    <section class="filter-panel">
      <label class="search-box" for="template-keyword">
        <span class="search-icon" aria-hidden="true" />
        <input
          id="template-keyword"
          v-model="filters.keyword"
          type="text"
          placeholder="搜索模板名称、类型或说明"
        />
      </label>
      <el-select v-model="filters.type" clearable placeholder="生命周期类型" style="width: 180px" @change="loadTemplates">
        <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="filters.status" clearable placeholder="状态" style="width: 150px" @change="loadTemplates">
        <el-option v-for="item in statusOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
    </section>

    <div class="table-shell">
      <el-table :data="filteredTemplates" v-loading="loading" empty-text="暂无模板数据">
        <el-table-column prop="name" label="模板名称" min-width="180" />
        <el-table-column label="类型" width="140">
          <template #default="{ row }">
            {{ formatTemplateType(row.type) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <span class="status-pill" :class="row.status === 'ENABLED' ? 'enabled' : 'disabled'">
              {{ formatTemplateStatus(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="系统模板" width="120">
          <template #default="{ row }">
            {{ Number(row.isSystem || 0) === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="260" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button v-if="canManageTemplates" link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="720px">
      <div v-if="detailLoading" class="state-block">
        <strong>正在加载模板详情</strong>
        <span>请稍候。</span>
      </div>
      <el-form v-else label-width="98px">
        <el-form-item label="模板名称">
          <el-input v-model="templateForm.name" placeholder="例如 默认敏捷模板" />
        </el-form-item>
        <el-form-item label="生命周期">
          <el-select v-model="templateForm.type" style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="模板状态">
          <el-segmented v-model="templateForm.status" :options="statusOptions" />
        </el-form-item>
        <el-form-item label="系统模板">
          <el-switch v-model="templateForm.isSystem" inline-prompt active-text="是" inactive-text="否" />
        </el-form-item>
        <el-form-item label="模板说明">
          <el-input v-model="templateForm.description" type="textarea" :rows="4" placeholder="描述该模板适用的交付场景和工作方式" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitTemplate">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.templates-view {
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

.table-shell {
  overflow: hidden;
  border: 1px solid #d4d4d8;
  background: #ffffff;
}

.status-pill {
  display: inline-flex;
  min-width: 58px;
  justify-content: center;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 700;
}

.status-pill.enabled {
  color: #166534;
  background: #dcfce7;
}

.status-pill.disabled {
  color: #9f1239;
  background: #ffe4e6;
}

.state-block {
  display: grid;
  gap: 6px;
  padding: 24px 20px;
  border: 1px solid #d4d4d8;
  background: #ffffff;
}

.state-block strong {
  font-size: 1rem;
}

.state-block span {
  color: #64748b;
}

@media (max-width: 900px) {
  .page-header {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .templates-view {
    padding: 18px 16px 24px;
  }

  .page-header h1 {
    font-size: 1.6rem;
  }
}
</style>
