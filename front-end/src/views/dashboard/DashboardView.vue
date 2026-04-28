<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

import { getDashboardHome } from '@/api/dashboard'
import {
  createProject,
  createProjectFromTemplate,
  deleteProject,
  getProjectDetail,
  getProjects,
  getProjectTemplates,
  initDemoProject,
} from '@/api/projects'
import { getProjectTimesheets } from '@/api/editor'
import { getProjectTasks } from '@/api/tasks'
import { useAuthStore } from '@/stores/auth'
import { recordRecentProject } from '@/utils/recentProjects'

const router = useRouter()
const authStore = useAuthStore()

const keyword = ref('')
const loading = ref(false)
const taskLoading = ref(false)
const timesheetLoading = ref(false)
const projects = ref([])
const memberTaskMap = ref({})
const memberTimesheetMap = ref({})
const templateDialogVisible = ref(false)
const templateLoading = ref(false)
const templateCreating = ref(false)
const templates = ref([])
const selectedTemplateId = ref('')
const templateProjectName = ref('')
const projectDetailDialogVisible = ref(false)
const projectDetailLoading = ref(false)
const selectedProjectDetail = ref(null)
const dashboardHome = ref(null)

const adminPrimaryActions = [
  {
    label: '用户管理',
    hint: '维护系统用户账号、角色和访问边界。',
    routeName: 'users',
    category: '系统',
    cta: '进入用户管理',
  },
  {
    label: '模板中心',
    hint: '维护标准项目模板，供业务用户建项使用。',
    routeName: 'project-templates',
    category: '配置',
    cta: '进入模板中心',
  },
  {
    label: '审计日志',
    hint: '查看系统级操作记录，追踪变更来源。',
    routeName: 'audit-logs',
    category: '审计',
    cta: '查看审计日志',
  },
]

const adminSecondaryActions = [
  {
    label: '通知中心',
    hint: '查看系统消息与提醒。',
    routeName: 'notifications',
  },
  {
    label: '个人资料',
    hint: '维护个人信息与账号设置。',
    routeName: 'profile',
  },
]

const adminGuardrails = [
  '系统管理员不自动拥有项目所有者权限。',
  '项目创建、成员协作、任务推进只属于业务用户职责。',
  '如果账号同时具备 USER 角色，才会显示项目工作台。',
]

const currentUser = computed(() => authStore.user || null)
const currentUserId = computed(() => currentUser.value?.id ? String(currentUser.value.id) : '')
const currentRoleCodes = computed(() => {
  const raw = currentUser.value?.roleCodes || currentUser.value?.roles
  return Array.isArray(raw) ? raw : []
})
const hasBusinessRole = computed(() => currentRoleCodes.value.includes('USER'))
const isSysAdmin = computed(() => currentRoleCodes.value.includes('SYS_ADMIN'))
const showBusinessWorkspace = computed(() => hasBusinessRole.value)
const showAdminWorkspace = computed(() => !hasBusinessRole.value && isSysAdmin.value)

const selectedTemplate = computed(
  () => templates.value.find((item) => String(item.id) === String(selectedTemplateId.value)) || null,
)

const filteredProjects = computed(() => {
  const search = keyword.value.trim().toLowerCase()
  if (!search) return projects.value

  return projects.value.filter((project) =>
    [project.name, project.projectCode, project.description, project.status, project.lifeCycleModel, project.ownerName]
      .filter(Boolean)
      .some((field) => String(field).toLowerCase().includes(search)),
  )
})

const activeProjects = computed(() =>
  filteredProjects.value.filter((project) => project.status !== 'CLOSED'),
)

const closedProjects = computed(() =>
  filteredProjects.value.filter((project) => project.status === 'CLOSED'),
)

const showQuickActions = computed(() => hasBusinessRole.value && !keyword.value.trim())
const activeProjectsEmptyHint = computed(() => (
  hasBusinessRole.value ? '可以从上面的入口开始新建项目。' : '当前账号未参与任何项目。'
))

const memberTasks = computed(() =>
  Object.entries(memberTaskMap.value).flatMap(([projectId, list]) =>
    list.map((item) => ({ ...item, projectId })),
  ),
)

const memberTimesheets = computed(() =>
  Object.entries(memberTimesheetMap.value).flatMap(([projectId, list]) =>
    list.map((item) => ({ ...item, projectId })),
  ),
)

const todayValue = computed(() => new Date().toISOString().slice(0, 10))

const myFocusTasks = computed(() => memberTasks.value
  .filter((item) => item.status !== 'DONE')
  .sort((left, right) => {
    if (Number(isOverdueTask(right)) !== Number(isOverdueTask(left))) {
      return Number(isOverdueTask(right)) - Number(isOverdueTask(left))
    }
    return taskTimeValue(left.plannedEndDate) - taskTimeValue(right.plannedEndDate)
  })
  .slice(0, 6))

const todayTimesheets = computed(() =>
  memberTimesheets.value.filter((item) => String(item.workDate || '') === todayValue.value),
)

const todayTimesheetHours = computed(() =>
  todayTimesheets.value.reduce((sum, item) => sum + Number(item.hours || 0), 0),
)

const todayTimesheetTaskIds = computed(() =>
  new Set(todayTimesheets.value.map((item) => String(item.taskId || ''))),
)

const missingTimesheetTasks = computed(() =>
  myFocusTasks.value
    .filter((item) => !todayTimesheetTaskIds.value.has(String(item.id)))
    .slice(0, 4))

const summaryCards = computed(() => [
  {
    label: '项目总数',
    value: formatSummaryNumber(dashboardHome.value?.projectCount ?? projects.value.length),
    hint: '全部项目',
  },
  {
    label: '进行中项目',
    value: formatSummaryNumber(dashboardHome.value?.inProgressProjectCount ?? activeProjects.value.length),
    hint: '当前活跃项目',
  },
  {
    label: '开放风险',
    value: formatSummaryNumber(dashboardHome.value?.openRiskCount ?? 0),
    hint: '待跟踪',
  },
  {
    label: '待审批变更',
    value: formatSummaryNumber(dashboardHome.value?.pendingChangeCount ?? 0),
    hint: '需要处理',
  },
  {
    label: '计划成本',
    value: formatCurrency(dashboardHome.value?.plannedCost ?? 0),
    hint: '预算汇总',
  },
  {
    label: '实际成本',
    value: formatCurrency(dashboardHome.value?.actualCost ?? 0),
    hint: '已发生成本',
  },
])

const upcomingMilestones = computed(() =>
  Array.isArray(dashboardHome.value?.upcomingMilestones)
    ? dashboardHome.value.upcomingMilestones.slice(0, 5)
    : [],
)

function formatProjectTag(project) {
  const map = {
    PLANNING: '规划中',
    IN_PROGRESS: '进行中',
    MONITORING: '监控中',
    CLOSED: '已关闭',
  }
  return map[project.status] || project.status || '项目'
}

function formatTaskStatus(status) {
  const map = {
    TODO: '未开始',
    IN_PROGRESS: '进行中',
    PENDING_REVIEW: '待验收',
    DONE: '已完成',
    BLOCKED: '阻塞',
  }
  return map[status] || status || '-'
}

function formatTaskDeadline(value) {
  if (!value) return '未设置截止时间'
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatSummaryNumber(value) {
  return new Intl.NumberFormat('zh-CN').format(Number(value || 0))
}

function formatCurrency(value) {
  return `¥${new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 0,
    maximumFractionDigits: 2,
  }).format(Number(value || 0))}`
}

function formatMetricHours(value) {
  return `${Number(value || 0).toFixed(1)}h`
}

function formatMilestoneDate(value) {
  if (!value) return '未安排时间'
  return String(value).replace('T', ' ').slice(0, 16)
}

function taskTimeValue(value) {
  if (!value) return Number.MAX_SAFE_INTEGER
  const timestamp = new Date(value).getTime()
  return Number.isFinite(timestamp) ? timestamp : Number.MAX_SAFE_INTEGER
}

function isOverdueTask(task) {
  if (!task || task.status === 'DONE' || !task.plannedEndDate) return false
  return taskTimeValue(task.plannedEndDate) < Date.now()
}

function findProjectName(projectId) {
  return projects.value.find((item) => String(item.id) === String(projectId))?.name || `项目${projectId}`
}

function openRoute(routeName) {
  router.push({ name: routeName })
}

async function loadProjects() {
  loading.value = true
  try {
    const result = await getProjects({ page: 1, pageSize: 100 })
    projects.value = Array.isArray(result?.list) ? result.list : []
  } catch (error) {
    ElMessage.error(error.message || '项目列表加载失败')
  } finally {
    loading.value = false
  }
}

async function loadDashboardSummary() {
  try {
    dashboardHome.value = await getDashboardHome()
  } catch (error) {
    ElMessage.error(error.message || '首页概览加载失败')
  }
}

async function loadMemberTasks() {
  if (!currentUserId.value || !projects.value.length) {
    memberTaskMap.value = {}
    return
  }

  taskLoading.value = true
  try {
    const entries = await Promise.all(
      projects.value
        .filter((project) => project.status !== 'CLOSED')
        .map(async (project) => {
          const result = await getProjectTasks(project.id, {
            assigneeId: currentUserId.value,
            page: 1,
            pageSize: 200,
          })
          return [String(project.id), Array.isArray(result?.list) ? result.list : []]
        }),
    )
    memberTaskMap.value = Object.fromEntries(entries)
  } catch (error) {
    ElMessage.error(error.message || '成员任务加载失败')
  } finally {
    taskLoading.value = false
  }
}

async function loadMemberTimesheets() {
  if (!currentUserId.value || !projects.value.length) {
    memberTimesheetMap.value = {}
    return
  }

  timesheetLoading.value = true
  try {
    const entries = await Promise.all(
      projects.value
        .filter((project) => project.status !== 'CLOSED')
        .map(async (project) => {
          const result = await getProjectTimesheets(project.id, {
            userId: currentUserId.value,
            startDate: todayValue.value,
            endDate: todayValue.value,
            page: 1,
            pageSize: 200,
          })
          return [String(project.id), Array.isArray(result?.list) ? result.list : []]
        }),
    )
    memberTimesheetMap.value = Object.fromEntries(entries)
  } catch (error) {
    ElMessage.error(error.message || '成员工时加载失败')
  } finally {
    timesheetLoading.value = false
  }
}

async function refreshWorkspaceSummary() {
  if (!hasBusinessRole.value) {
    memberTaskMap.value = {}
    memberTimesheetMap.value = {}
    dashboardHome.value = null
    return
  }

  await Promise.all([
    loadDashboardSummary(),
    loadMemberTasks(),
    loadMemberTimesheets(),
  ])
}

async function loadWorkspace() {
  if (!hasBusinessRole.value) {
    keyword.value = ''
    projects.value = []
    memberTaskMap.value = {}
    memberTimesheetMap.value = {}
    dashboardHome.value = null
    return
  }

  await loadProjects()
  await refreshWorkspaceSummary()
}

function openProject(project, options = {}) {
  const query = {}
  if (options.taskId != null && options.taskId !== '') {
    query.taskId = String(options.taskId)
  }
  recordRecentProject({
    projectId: project.id,
    name: project.name,
  })
  router.push({
    name: 'project-editor',
    params: { projectId: String(project.id) },
    query,
  })
}

function canDeleteProject(project) {
  if (!project?.ownerId || !currentUserId.value) return false
  return String(project.ownerId) === currentUserId.value
}

async function viewProjectProperties(project) {
  projectDetailLoading.value = true
  projectDetailDialogVisible.value = true
  try {
    selectedProjectDetail.value = await getProjectDetail(project.id)
  } catch (error) {
    projectDetailDialogVisible.value = false
    ElMessage.error(error.message || '项目属性加载失败')
  } finally {
    projectDetailLoading.value = false
  }
}

async function removeProject(project) {
  try {
    await ElMessageBox.confirm(
      `确认删除项目“${project.name}”吗？删除后无法恢复。`,
      '删除项目',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProject(project.id)
    ElMessage.success('项目已删除')
    await loadProjects()
    await refreshWorkspaceSummary()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除项目失败')
    }
  } finally {
    loading.value = false
  }
}

async function createBlankProject() {
  if (!hasBusinessRole.value) {
    ElMessage.warning('当前账号不能创建项目')
    return
  }

  let projectName = ''
  try {
    const result = await ElMessageBox.prompt('请输入项目名称', '新建项目', {
      confirmButtonText: '创建',
      cancelButtonText: '取消',
      inputValue: '新项目',
      inputPlaceholder: '例如 客户门户二期',
      inputValidator: (value) => {
        if (!value || !value.trim()) return '项目名称不能为空'
        return true
      },
    })
    projectName = result.value.trim()
  } catch {
    return
  }

  try {
    const project = await createProject({
      name: projectName,
      description: '',
      lifeCycleModel: 'AGILE',
    })
    ElMessage.success('项目已创建')
    await loadProjects()
    await refreshWorkspaceSummary()
    openProject(project)
  } catch (error) {
    ElMessage.error(error.message || '项目创建失败')
  }
}

async function openTemplateDialog() {
  if (!hasBusinessRole.value) {
    ElMessage.warning('当前账号不能创建项目')
    return
  }

  templateDialogVisible.value = true
  templateLoading.value = true
  try {
    const result = await getProjectTemplates({ page: 1, pageSize: 100 })
    templates.value = Array.isArray(result?.list) ? result.list : []
    selectedTemplateId.value = templates.value[0]?.id ? String(templates.value[0].id) : ''
    templateProjectName.value = selectedTemplate.value?.name ? `${selectedTemplate.value.name}-副本` : ''
  } catch (error) {
    ElMessage.error(error.message || '项目模板加载失败')
  } finally {
    templateLoading.value = false
  }
}

async function createByTemplate() {
  if (!selectedTemplateId.value) {
    ElMessage.warning('请先选择模板')
    return
  }
  if (!templateProjectName.value.trim()) {
    ElMessage.warning('请输入项目名称')
    return
  }

  templateCreating.value = true
  try {
    const project = await createProjectFromTemplate({
      templateId: String(selectedTemplateId.value).trim(),
      name: templateProjectName.value.trim(),
    })
    templateDialogVisible.value = false
    ElMessage.success('项目已按模板创建')
    await loadProjects()
    await refreshWorkspaceSummary()
    openProject(project)
  } catch (error) {
    ElMessage.error(error.message || '模板建项失败')
  } finally {
    templateCreating.value = false
  }
}

async function createDemoProject() {
  if (!hasBusinessRole.value) {
    ElMessage.warning('当前账号不能创建项目')
    return
  }

  loading.value = true
  try {
    const project = await initDemoProject()
    ElMessage.success('演示项目已初始化')
    await loadProjects()
    await refreshWorkspaceSummary()
    openProject(project)
  } catch (error) {
    ElMessage.error(error.message || '演示项目初始化失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadWorkspace)
</script>

<template>
  <section class="start-view surface-card">
    <div v-if="showBusinessWorkspace" class="dashboard-layout">
      <div class="dashboard-main">
        <header class="search-row">
          <label class="search-box" for="project-search">
            <span class="search-icon" aria-hidden="true" />
            <input
              id="project-search"
              v-model="keyword"
              type="text"
              placeholder="搜索项目名称、编码、负责人或描述"
            />
          </label>
        </header>

        <section class="summary-grid">
          <article
            v-for="card in summaryCards"
            :key="card.label"
            class="summary-card"
          >
            <span>{{ card.label }}</span>
            <strong>{{ card.value }}</strong>
            <small>{{ card.hint }}</small>
          </article>
        </section>

        <section v-if="showQuickActions" class="project-section">
          <div class="section-title">
            <strong>开始项目</strong>
            <span>3 个入口</span>
          </div>
          <div class="project-grid action-grid">
            <button type="button" class="project-card" @click="createBlankProject">
              <div class="project-visual blank-visual">
                <div class="visual-head">
                  <span class="tag-chip">新建</span>
                </div>
                <div class="visual-board" />
              </div>
              <div class="project-info">
                <strong>新建项目</strong>
                <span>从空白项目开始规划</span>
              </div>
            </button>

            <button type="button" class="project-card" @click="openTemplateDialog">
              <div class="project-visual blank-visual action-visual">
                <div class="visual-head">
                  <span class="tag-chip">模板</span>
                </div>
                <div class="visual-board" />
              </div>
              <div class="project-info">
                <strong>从模板创建</strong>
                <span>复制现有模板快速开始</span>
              </div>
            </button>

            <button type="button" class="project-card" @click="createDemoProject">
              <div class="project-visual blank-visual action-visual">
                <div class="visual-head">
                  <span class="tag-chip">演示</span>
                </div>
                <div class="visual-board" />
              </div>
              <div class="project-info">
                <strong>初始化演示项目</strong>
                <span>生成一套可直接浏览的示例数据</span>
              </div>
            </button>
          </div>
        </section>

        <section class="project-section">
          <div class="section-title">
            <strong>进行中的项目</strong>
            <span>{{ activeProjects.length }} 个</span>
          </div>
          <div v-if="activeProjects.length" class="project-grid">
            <button
              v-for="project in activeProjects"
              :key="project.id"
              type="button"
              class="project-card"
              @click="openProject(project)"
            >
              <div class="project-visual">
                <div class="visual-head">
                  <span class="code-chip">{{ project.projectCode || `PRJ-${project.id}` }}</span>
                  <span class="tag-chip">{{ formatProjectTag(project) }}</span>
                </div>
                <div class="visual-body">
                  <span />
                  <span />
                  <span />
                </div>
              </div>
              <div class="project-info">
                <div class="project-info-head">
                  <strong>{{ project.name }}</strong>
                  <el-dropdown
                    trigger="click"
                    @command="(command) => {
                      if (command === 'open') openProject(project)
                      if (command === 'properties') viewProjectProperties(project)
                      if (command === 'delete') removeProject(project)
                    }"
                  >
                    <button type="button" class="project-more" aria-label="更多操作" @click.stop>
                      <span class="project-more-dots" aria-hidden="true">
                        <span />
                        <span />
                        <span />
                      </span>
                    </button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="open">打开项目</el-dropdown-item>
                        <el-dropdown-item command="properties">查看属性</el-dropdown-item>
                        <el-dropdown-item v-if="canDeleteProject(project)" command="delete">删除项目</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
                <span>{{ project.description || `${project.lifeCycleModel || '未设置模型'} / 负责人 ${project.ownerName || '-'}` }}</span>
              </div>
            </button>
          </div>
          <div v-else-if="!loading" class="empty-result">
            <strong>暂无进行中的项目</strong>
            <span>{{ activeProjectsEmptyHint }}</span>
          </div>
        </section>

        <section v-if="closedProjects.length" class="project-section closed-section">
          <div class="section-title">
            <strong>已关闭项目</strong>
            <span>{{ closedProjects.length }} 个</span>
          </div>
          <div class="project-grid compact-grid">
            <button
              v-for="project in closedProjects"
              :key="`closed-${project.id}`"
              type="button"
              class="project-card compact-card"
              @click="openProject(project)"
            >
              <div class="project-visual">
                <div class="visual-head">
                  <span class="code-chip">{{ project.projectCode || `PRJ-${project.id}` }}</span>
                  <span class="tag-chip">{{ formatProjectTag(project) }}</span>
                </div>
                <div class="visual-board visual-board-closed">已关闭</div>
              </div>
              <div class="project-info compact-info">
                <strong>{{ project.name }}</strong>
                <span>{{ project.description || `${project.lifeCycleModel || '未设置模型'} / 负责人 ${project.ownerName || '-'}` }}</span>
              </div>
            </button>
          </div>
        </section>

        <div v-if="!loading && !filteredProjects.length && !showQuickActions && keyword.trim()" class="empty-result">
          <strong>没有找到匹配项目</strong>
          <span>可以修改搜索词继续查找。</span>
        </div>
      </div>

      <aside class="dashboard-side">
        <article class="mini-panel">
          <div class="mini-panel-head">
            <strong>我的待办</strong>
            <span>{{ myFocusTasks.length }} 条</span>
          </div>
          <div v-if="taskLoading" class="mini-empty">正在加载任务...</div>
          <div v-else-if="myFocusTasks.length" class="mini-list">
            <button
              v-for="task in myFocusTasks"
              :key="task.id"
              type="button"
              class="mini-list-item"
              @click="openProject({ id: task.projectId, name: findProjectName(task.projectId) }, { taskId: task.id })"
            >
              <strong>{{ task.name }}</strong>
              <span>{{ findProjectName(task.projectId) }} / {{ formatTaskStatus(task.status) }}</span>
              <span>{{ formatTaskDeadline(task.plannedEndDate) }}</span>
            </button>
          </div>
          <div v-else class="mini-empty">没有需要跟进的任务。</div>
        </article>

        <article class="mini-panel">
          <div class="mini-panel-head">
            <strong>今日工时</strong>
            <span>{{ formatMetricHours(todayTimesheetHours) }}</span>
          </div>
          <div v-if="timesheetLoading" class="mini-empty">正在加载工时...</div>
          <div v-else class="mini-list">
            <div class="mini-list-item static-item">
              <strong>今日已登记 {{ formatMetricHours(todayTimesheetHours) }}</strong>
              <span>已登记任务 {{ todayTimesheets.length }} 条</span>
              <span v-if="missingTimesheetTasks.length">还有 {{ missingTimesheetTasks.length }} 条待补工时任务</span>
              <span v-else>今日待办任务已完成工时覆盖</span>
            </div>
            <button
              v-for="task in missingTimesheetTasks"
              :key="`missing-${task.id}`"
              type="button"
              class="mini-list-item"
              @click="openProject({ id: task.projectId, name: findProjectName(task.projectId) }, { taskId: task.id })"
            >
              <strong>{{ task.name }}</strong>
              <span>{{ findProjectName(task.projectId) }}</span>
              <span>{{ formatTaskDeadline(task.plannedEndDate) }}</span>
            </button>
          </div>
        </article>

        <article class="mini-panel">
          <div class="mini-panel-head">
            <strong>即将到来的里程碑</strong>
            <span>{{ upcomingMilestones.length }} 条</span>
          </div>
          <div v-if="upcomingMilestones.length" class="mini-list">
            <div
              v-for="item in upcomingMilestones"
              :key="`milestone-${item.id}`"
              class="mini-list-item static-item"
            >
              <strong>{{ item.name }}</strong>
              <span>{{ item.ownerName ? `负责人 ${item.ownerName}` : '未设置负责人' }}</span>
              <span>{{ formatMilestoneDate(item.plannedDate) }}</span>
            </div>
          </div>
          <div v-else class="mini-empty">暂无临近里程碑。</div>
        </article>
      </aside>
    </div>

    <div v-else-if="showAdminWorkspace" class="admin-dashboard">
      <header class="admin-hero">
        <div class="admin-hero-copy">
          <span class="admin-eyebrow">系统工作台</span>
          <h1>管理员首页</h1>
          <p>当前账号只负责系统管理，不直接进入项目执行面。请从这里处理用户、模板和审计相关工作。</p>
        </div>
        <div class="admin-role-badge">SYS_ADMIN</div>
      </header>

      <section class="admin-section">
        <div class="section-title">
          <strong>核心系统入口</strong>
          <span>{{ adminPrimaryActions.length }} 个模块</span>
        </div>
        <div class="admin-action-grid">
          <button
            v-for="action in adminPrimaryActions"
            :key="action.routeName"
            type="button"
            class="admin-action-card"
            @click="openRoute(action.routeName)"
          >
            <span class="admin-action-tag">{{ action.category }}</span>
            <strong>{{ action.label }}</strong>
            <p>{{ action.hint }}</p>
            <span class="admin-action-link">{{ action.cta }}</span>
          </button>
        </div>
      </section>

      <section class="admin-secondary-grid">
        <article class="admin-note-card">
          <div class="section-title">
            <strong>权限边界</strong>
            <span>系统职责与项目职责分离</span>
          </div>
          <ul class="admin-note-list">
            <li v-for="note in adminGuardrails" :key="note">{{ note }}</li>
          </ul>
        </article>

        <article class="admin-note-card">
          <div class="section-title">
            <strong>常用页面</strong>
            <span>保留个人和消息相关能力</span>
          </div>
          <div class="admin-support-grid">
            <button
              v-for="action in adminSecondaryActions"
              :key="action.routeName"
              type="button"
              class="admin-support-card"
              @click="openRoute(action.routeName)"
            >
              <strong>{{ action.label }}</strong>
              <span>{{ action.hint }}</span>
            </button>
          </div>
        </article>
      </section>
    </div>

    <div v-else class="admin-dashboard empty-account-dashboard">
      <header class="admin-hero">
        <div class="admin-hero-copy">
          <span class="admin-eyebrow">账号提示</span>
          <h1>当前账号暂无可用工作台</h1>
          <p>该账号既没有业务角色，也不是系统管理员，因此不能进入项目或系统管理页面。</p>
        </div>
      </header>

      <article class="admin-note-card">
        <div class="section-title">
          <strong>建议处理</strong>
          <span>请联系系统管理员分配角色</span>
        </div>
        <ul class="admin-note-list">
          <li>如需参与项目，请分配 USER 角色。</li>
          <li>如需负责系统管理，请分配 SYS_ADMIN 角色。</li>
        </ul>
      </article>
    </div>

    <el-dialog v-model="templateDialogVisible" title="从模板创建项目" width="720px">
      <div v-if="templateLoading" class="empty-result">
        <strong>正在加载模板</strong>
        <span>请稍候。</span>
      </div>
      <el-form v-else label-width="96px">
        <el-form-item label="项目模板">
          <el-select
            v-model="selectedTemplateId"
            placeholder="请选择模板"
            style="width: 100%"
            @change="templateProjectName = selectedTemplate?.name ? `${selectedTemplate.name}-副本` : ''"
          >
            <el-option
              v-for="template in templates"
              :key="template.id"
              :label="template.name"
              :value="String(template.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="项目名称">
          <el-input v-model="templateProjectName" placeholder="请输入新项目名称" />
        </el-form-item>
        <el-form-item label="模板说明">
          <div class="template-description">{{ selectedTemplate?.description || '当前模板暂无说明' }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="templateCreating" @click="createByTemplate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="projectDetailDialogVisible" title="项目属性" width="720px">
      <div v-if="projectDetailLoading" class="empty-result">
        <strong>正在加载项目属性</strong>
        <span>请稍候。</span>
      </div>
      <div v-else-if="selectedProjectDetail" class="detail-grid">
        <div class="detail-item">
          <span>项目名称</span>
          <strong>{{ selectedProjectDetail.name || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>项目编码</span>
          <strong>{{ selectedProjectDetail.projectCode || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>状态</span>
          <strong>{{ formatProjectTag(selectedProjectDetail) }}</strong>
        </div>
        <div class="detail-item">
          <span>生命周期</span>
          <strong>{{ selectedProjectDetail.lifeCycleModel || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>开始日期</span>
          <strong>{{ selectedProjectDetail.startDate || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>结束日期</span>
          <strong>{{ selectedProjectDetail.endDate || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>负责人</span>
          <strong>{{ selectedProjectDetail.ownerName || selectedProjectDetail.ownerId || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>项目说明</span>
          <strong>{{ selectedProjectDetail.description || '暂无说明' }}</strong>
        </div>
      </div>
      <template #footer>
        <el-button @click="projectDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.start-view {
  min-height: 100vh;
  height: 100vh;
  padding: 0;
  border-radius: 0;
  background: #f3f3f3;
  border: 0;
  box-shadow: none;
  overflow: hidden;
}

.dashboard-layout {
  min-height: 100vh;
  height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
  align-items: stretch;
}

.dashboard-main {
  min-width: 0;
  min-height: 0;
  display: grid;
  align-content: start;
  gap: 18px;
  padding: 24px;
  overflow-y: auto;
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.dashboard-main::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.dashboard-side {
  display: grid;
  min-height: 100%;
  gap: 16px;
  align-content: start;
  align-self: stretch;
  padding: 24px 18px;
  background: #fff;
  border-left: 1px solid #d4d4d4;
  overflow: hidden;
}

.dashboard-side .mini-panel {
  padding: 0 0 14px;
  border: 0;
  border-bottom: 1px solid #e5e7eb;
  background: transparent;
}

.dashboard-side .mini-panel:last-child {
  padding-bottom: 0;
  border-bottom: 0;
}

.search-row {
  display: flex;
  align-items: center;
}

.search-box {
  width: min(640px, 100%);
  height: 48px;
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
  font-size: 14px;
  color: var(--foreground);
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.summary-card {
  display: grid;
  gap: 6px;
  padding: 16px 14px;
  border: 1px solid #d4d4d4;
  background: #fff;
}

.summary-card span {
  color: #667085;
  font-size: 12px;
}

.summary-card strong {
  color: #111827;
  font-size: 1.24rem;
  line-height: 1.2;
}

.summary-card small {
  color: #94a3b8;
  font-size: 12px;
}

.project-section,
.admin-section {
  display: grid;
  gap: 12px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px 14px;
}

.action-grid {
  margin-bottom: 4px;
}

.compact-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.project-card {
  display: grid;
  grid-template-rows: 188px auto;
  gap: 8px;
  padding: 0;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.compact-card {
  grid-template-rows: 188px auto;
}

.project-visual {
  height: 188px;
  padding: 12px;
  border: 1px solid #a3a3a3;
  background: #fff;
}

.blank-visual,
.action-visual,
.closed-visual {
  background: #fff;
}

.project-card:hover .project-visual,
.project-card:hover .compact-info,
.mini-list-item:hover {
  border-color: #8a8a8a;
  background: #e8ebf0;
}

.action-grid .project-card:hover .project-visual {
  background: #eceff3;
  border-color: #8a8a8a;
}

.action-grid .project-card:hover .visual-board {
  background: #eceff3;
}

.visual-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.visual-body {
  height: calc(100% - 28px);
  display: grid;
  align-content: center;
  gap: 8px;
}

.visual-body span {
  display: block;
  height: 8px;
  background: #d4d4d4;
}

.visual-body span:nth-child(1) { width: 86%; }
.visual-body span:nth-child(2) { width: 70%; }
.visual-body span:nth-child(3) { width: 48%; }

.visual-board {
  height: calc(100% - 28px);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #556274;
  font-size: 16px;
  letter-spacing: 1px;
  background: #fff;
}

.visual-board-closed {
  height: calc(100% - 28px);
  font-size: 0;
  color: transparent;
  background:
    linear-gradient(#d4d4d4, #d4d4d4) left center / 86% 8px no-repeat,
    linear-gradient(#d4d4d4, #d4d4d4) left calc(50% + 16px) / 70% 8px no-repeat,
    linear-gradient(#d4d4d4, #d4d4d4) left calc(50% + 32px) / 48% 8px no-repeat;
}

.code-chip,
.tag-chip {
  display: inline-flex;
  align-items: center;
  min-height: 24px;
  padding: 0 9px;
  font-size: 12px;
  font-weight: 700;
}

.code-chip {
  color: #2563eb;
  background: #e5e5e5;
}

.tag-chip,
.muted-chip {
  color: #404040;
  background: #e5e5e5;
}

.muted-chip {
  min-height: 24px;
  padding: 0 9px;
  font-size: 12px;
}

.project-info {
  display: grid;
  align-content: start;
  gap: 3px;
  min-height: 60px;
  padding: 0 2px;
}

.compact-info {
  padding: 0 2px;
  border: 0;
  background: transparent;
}

.project-info-head {
  display: flex;
  align-items: start;
  justify-content: space-between;
  gap: 8px;
}

.project-info strong {
  font-size: 14px;
  line-height: 1.35;
  color: #1f2937;
}

.project-info span {
  color: #667085;
  font-size: 12px;
  line-height: 1.45;
  display: -webkit-box;
  overflow: hidden;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.project-more {
  width: 24px;
  height: 24px;
  border: 1px solid #cfcfcf;
  background: #fff;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.project-more:hover {
  border-color: #2563eb;
}

.project-more-dots {
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.project-more-dots span {
  width: 3px;
  height: 3px;
  border-radius: 999px;
  background: #666;
}

.project-more:hover .project-more-dots span {
  background: #2563eb;
}

.mini-panel {
  display: grid;
  gap: 10px;
  padding: 14px;
  border: 1px solid #d4d4d4;
  background: #fff;
}

.mini-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.mini-panel-head strong {
  font-size: 14px;
  color: #1f2937;
}

.mini-panel-head span,
.mini-empty {
  color: #667085;
  font-size: 13px;
}

.mini-list {
  display: grid;
  gap: 8px;
}

.mini-list-item {
  display: grid;
  gap: 3px;
  padding: 10px 12px;
  border: 1px solid #d9dfeb;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.mini-list-item strong {
  color: #1f2937;
  font-size: 13px;
}

.mini-list-item span {
  color: #667085;
  font-size: 12px;
}

.closed-section {
  display: grid;
  gap: 12px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.section-title strong {
  color: #1f2937;
  font-size: 14px;
  font-weight: 600;
}

.section-title span {
  color: #667085;
  font-size: 12px;
}

.empty-result {
  display: grid;
  gap: 6px;
  padding: 16px;
  border: 1px solid #d9dfeb;
  background: #fff;
}

.empty-result strong {
  color: #1f2937;
}

.empty-result span {
  color: #667085;
}

.admin-dashboard {
  min-height: 100vh;
  height: 100vh;
  display: grid;
  align-content: start;
  gap: 20px;
  padding: 24px;
  overflow-y: auto;
  background:
    radial-gradient(circle at top right, rgba(47, 111, 228, 0.08), transparent 28%),
    linear-gradient(180deg, #fafcff 0%, #f3f6fb 100%);
}

.admin-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 16px;
  padding: 24px;
  border: 1px solid #dbe4f0;
  background: rgba(255, 255, 255, 0.92);
}

.admin-hero-copy {
  display: grid;
  gap: 8px;
}

.admin-eyebrow {
  color: #2f6fe4;
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.admin-hero h1 {
  margin: 0;
  color: #17314f;
  font-size: 1.9rem;
  line-height: 1.08;
}

.admin-hero p {
  margin: 0;
  max-width: 760px;
  color: #5d718c;
  font-size: 0.98rem;
  line-height: 1.7;
}

.admin-role-badge {
  min-width: 128px;
  align-self: start;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 12px 18px;
  color: #2f6fe4;
  background: #eaf2ff;
  border: 1px solid #cfe0ff;
  font-size: 0.84rem;
  font-weight: 700;
  letter-spacing: 0.06em;
}

.admin-action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.admin-action-card {
  display: grid;
  gap: 12px;
  align-content: start;
  min-height: 220px;
  padding: 18px;
  border: 1px solid #d7e2f2;
  background: rgba(255, 255, 255, 0.96);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.admin-action-card:hover {
  border-color: #9cbaf2;
  background: #ffffff;
  transform: translateY(-2px);
  box-shadow: 0 18px 28px rgba(23, 49, 79, 0.08);
}

.admin-action-tag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: fit-content;
  min-height: 24px;
  padding: 0 10px;
  color: #45617d;
  background: #eef3fa;
  font-size: 0.74rem;
  font-weight: 700;
}

.admin-action-card strong {
  color: #17314f;
  font-size: 1.18rem;
  line-height: 1.25;
}

.admin-action-card p {
  margin: 0;
  color: #5d718c;
  font-size: 0.92rem;
  line-height: 1.65;
}

.admin-action-link {
  margin-top: auto;
  color: #2f6fe4;
  font-size: 0.88rem;
  font-weight: 700;
}

.admin-secondary-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 1fr);
  gap: 14px;
}

.admin-note-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  border: 1px solid #d7e2f2;
  background: rgba(255, 255, 255, 0.96);
}

.admin-note-list {
  margin: 0;
  padding-left: 18px;
  color: #5d718c;
  line-height: 1.8;
}

.admin-note-list li + li {
  margin-top: 6px;
}

.admin-support-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.admin-support-card {
  display: grid;
  gap: 6px;
  padding: 14px;
  border: 1px solid #dbe4f0;
  background: #f8fbff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease;
}

.admin-support-card:hover {
  border-color: #9cbaf2;
  background: #ffffff;
}

.admin-support-card strong {
  color: #17314f;
  font-size: 0.98rem;
}

.admin-support-card span {
  color: #5d718c;
  font-size: 0.86rem;
  line-height: 1.6;
}

.empty-account-dashboard {
  grid-template-columns: 1fr;
}

.template-description {
  min-height: 72px;
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d4d4d4;
  background: #fafafa;
  color: #555;
  line-height: 1.6;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.detail-item {
  display: grid;
  gap: 8px;
  padding: 14px;
  border: 1px solid #ddd;
  background: #fff;
}

.detail-item span {
  color: #666;
  font-size: 13px;
}

.detail-item strong {
  color: #222;
  font-size: 14px;
  line-height: 1.6;
}

@media (max-width: 1180px) {
  .dashboard-layout {
    grid-template-columns: 1fr;
    min-height: auto;
    height: auto;
  }

  .dashboard-side {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    padding: 20px 24px 24px;
    border-left: 0;
    border-top: 1px solid #d4d4d4;
    min-height: auto;
    overflow: visible;
  }

  .summary-grid,
  .project-grid,
  .compact-grid,
  .admin-action-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .admin-secondary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 980px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-side,
  .detail-grid,
  .admin-support-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .project-grid,
  .compact-grid,
  .admin-action-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .admin-hero {
    grid-template-columns: 1fr;
  }

  .admin-role-badge {
    min-width: 0;
    width: fit-content;
  }
}

@media (max-width: 640px) {
  .start-view,
  .admin-dashboard {
    min-height: auto;
    height: auto;
    overflow: visible;
  }

  .dashboard-layout {
    height: auto;
  }

  .dashboard-main {
    overflow: visible;
  }

  .dashboard-main,
  .dashboard-side,
  .admin-dashboard {
    padding: 16px;
  }

  .summary-grid,
  .dashboard-side,
  .detail-grid,
  .project-grid,
  .compact-grid,
  .admin-action-grid,
  .admin-support-grid {
    grid-template-columns: 1fr;
  }

  .search-box {
    width: 100%;
    height: 44px;
  }

  .admin-hero,
  .admin-note-card,
  .admin-action-card {
    padding: 16px;
  }
}
</style>
