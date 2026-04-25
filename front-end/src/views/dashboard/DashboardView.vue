<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'

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

const currentUser = computed(() => authStore.user || null)
const currentUserId = computed(() => currentUser.value?.id ? String(currentUser.value.id) : '')
const currentRoleCodes = computed(() => {
  const raw = currentUser.value?.roleCodes || currentUser.value?.roles
  return Array.isArray(raw) ? raw : []
})

const canManageProjects = computed(() =>
  currentRoleCodes.value.includes('SYS_ADMIN') || currentRoleCodes.value.includes('PROJECT_MANAGER'),
)

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

const showQuickActions = computed(() => canManageProjects.value && !keyword.value.trim())

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

const myFocusTasks = computed(() => {
  if (canManageProjects.value) return []
  return memberTasks.value
    .filter((item) => item.status !== 'DONE')
    .sort((left, right) => {
      if (Number(isOverdueTask(right)) !== Number(isOverdueTask(left))) {
        return Number(isOverdueTask(right)) - Number(isOverdueTask(left))
      }
      return taskTimeValue(left.plannedEndDate) - taskTimeValue(right.plannedEndDate)
    })
    .slice(0, 6)
})

const todayTimesheets = computed(() =>
  memberTimesheets.value.filter((item) => String(item.workDate || '') === todayValue.value),
)

const todayTimesheetHours = computed(() =>
  todayTimesheets.value.reduce((sum, item) => sum + Number(item.hours || 0), 0),
)

const todayTimesheetTaskIds = computed(() =>
  new Set(todayTimesheets.value.map((item) => String(item.taskId || ''))),
)

const missingTimesheetTasks = computed(() => {
  if (canManageProjects.value) return []
  return myFocusTasks.value
    .filter((item) => !todayTimesheetTaskIds.value.has(String(item.id)))
    .slice(0, 4)
})

const memberProjectCards = computed(() => activeProjects.value.map((project) => {
  const taskBucket = memberTaskMap.value[String(project.id)] || []
  return {
    ...project,
    myTaskCount: taskBucket.length,
    myTodoCount: taskBucket.filter((item) => item.status !== 'DONE').length,
    myOverdueCount: taskBucket.filter(isOverdueTask).length,
  }
}))

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
    DONE: '已完成',
    BLOCKED: '闃诲',
  }
  return map[status] || status || '-'
}

function formatTaskDeadline(value) {
  if (!value) return '未设置截止时间'
  return String(value).replace('T', ' ').slice(0, 16)
}

function formatMetricHours(value) {
  return `${Number(value || 0).toFixed(1)}h`
}

function formatProjectSummary(project) {
  return project.description || `${project.lifeCycleModel || '未设置模型'} / 负责人 ${project.ownerName || '-'}`
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
  return projects.value.find((item) => String(item.id) === String(projectId))?.name || `椤圭洰${projectId}`
}

async function loadProjects() {
  loading.value = true
  try {
    const result = await getProjects({ page: 1, pageSize: 100 })
    projects.value = Array.isArray(result?.list) ? result.list : []
  } catch (error) {
    ElMessage.error(error.message || '椤圭洰鍒楄〃鍔犺浇澶辫触')
  } finally {
    loading.value = false
  }
}

async function loadMemberTasks() {
  if (canManageProjects.value || !currentUserId.value || !projects.value.length) {
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
    ElMessage.error(error.message || '鎴愬憳浠诲姟鍔犺浇澶辫触')
  } finally {
    taskLoading.value = false
  }
}

async function loadMemberTimesheets() {
  if (canManageProjects.value || !currentUserId.value || !projects.value.length) {
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
    ElMessage.error(error.message || '鎴愬憳宸ユ椂鍔犺浇澶辫触')
  } finally {
    timesheetLoading.value = false
  }
}

function openProject(project, options = {}) {
  const query = {}
  if (options.taskId != null && options.taskId !== '') {
    query.taskId = String(options.taskId)
  }
  router.push({
    name: 'project-editor',
    params: { projectId: String(project.id) },
    query,
  })
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
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除项目失败')
    }
  } finally {
    loading.value = false
  }
}

async function createBlankProject() {
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
    openProject(project)
  } catch (error) {
    ElMessage.error(error.message || '项目创建失败')
  }
}

async function openTemplateDialog() {
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
      templateId: Number(selectedTemplateId.value),
      name: templateProjectName.value.trim(),
    })
    templateDialogVisible.value = false
    ElMessage.success('项目已按模板创建')
    await loadProjects()
    openProject(project)
  } catch (error) {
    ElMessage.error(error.message || '模板建项失败')
  } finally {
    templateCreating.value = false
  }
}

async function createDemoProject() {
  loading.value = true
  try {
    const project = await initDemoProject()
    ElMessage.success('演示项目已初始化')
    await loadProjects()
    openProject(project)
  } catch (error) {
    ElMessage.error(error.message || '演示项目初始化失败')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  await loadProjects()
})
</script>

<template>
  <section class="start-view surface-card">
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


    <section v-if="showQuickActions" class="project-grid action-grid">
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
    </section>

    <section class="project-grid">
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
                  <el-dropdown-item v-if="canManageProjects" command="delete">删除项目</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <span>{{ project.description || `${project.lifeCycleModel || '未设置模型'} / 负责人 ${project.ownerName || '-'}` }}</span>
        </div>
      </button>
    </section>


    <section v-if="closedProjects.length" class="closed-section">
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

    <div v-if="!loading && !filteredProjects.length && !showQuickActions" class="empty-result">
      <strong>没有找到匹配项目</strong>
      <span>可以修改搜索词继续查找。</span>
    </div>

    <el-dialog v-model="templateDialogVisible" title="从模板创建项目" width="720px">
      <div v-if="templateLoading" class="empty-result">
        <strong>正在加载模板</strong>
        <span>请稍候。</span>
      </div>
      <el-form v-else label-width="96px">
        <el-form-item label="椤圭洰妯℃澘">
          <el-select
            v-model="selectedTemplateId"
            placeholder="璇烽€夋嫨妯℃澘"
            style="width: 100%"
            @change="templateProjectName = selectedTemplate?.name ? `${selectedTemplate.name}-鍓湰` : ''"
          >
            <el-option
              v-for="template in templates"
              :key="template.id"
              :label="template.name"
              :value="String(template.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="椤圭洰鍚嶇О">
          <el-input v-model="templateProjectName" placeholder="璇疯緭鍏ユ柊椤圭洰鍚嶇О" />
        </el-form-item>
        <el-form-item label="妯℃澘璇存槑">
          <div class="template-description">{{ selectedTemplate?.description || '褰撳墠妯℃澘鏆傛棤璇存槑' }}</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="templateDialogVisible = false">鍙栨秷</el-button>
        <el-button type="primary" :loading="templateCreating" @click="createByTemplate">鍒涘缓</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="projectDetailDialogVisible" title="项目属性" width="720px">
      <div v-if="projectDetailLoading" class="empty-result">
        <strong>正在加载项目属性</strong>
        <span>请稍候。</span>
      </div>
      <div v-else-if="selectedProjectDetail" class="detail-grid">
        <div class="detail-item">
          <span>椤圭洰鍚嶇О</span>
          <strong>{{ selectedProjectDetail.name || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>椤圭洰缂栫爜</span>
          <strong>{{ selectedProjectDetail.projectCode || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>状态</span>
          <strong>{{ formatProjectTag(selectedProjectDetail) }}</strong>
        </div>
        <div class="detail-item">
          <span>鐢熷懡鍛ㄦ湡</span>
          <strong>{{ selectedProjectDetail.lifeCycleModel || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>开始日期</span>
          <strong>{{ selectedProjectDetail.startDate || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>缁撴潫鏃ユ湡</span>
          <strong>{{ selectedProjectDetail.endDate || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>负责人</span>
          <strong>{{ selectedProjectDetail.ownerName || selectedProjectDetail.ownerId || '-' }}</strong>
        </div>
        <div class="detail-item">
          <span>椤圭洰璇存槑</span>
          <strong>{{ selectedProjectDetail.description || '鏆傛棤璇存槑' }}</strong>
        </div>
      </div>
      <template #footer>
        <el-button @click="projectDetailDialogVisible = false">鍏抽棴</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.start-view {
  min-height: 100vh;
  display: grid;
  align-content: start;
  gap: 18px;
  padding: 24px;
  border-radius: 0;
  background: #f3f3f3;
  border: 0;
  box-shadow: none;
}

.search-row {
  display: flex;
  align-items: center;
}

.search-box {
  width: min(540px, 100%);
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

.member-strip {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.member-strip-item {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border: 1px solid #d4d4d4;
  background: #fff;
}

.member-strip-item strong {
  color: #222;
  font-size: 14px;
}

.member-strip-item span {
  color: #667085;
  font-size: 13px;
}

.project-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px 14px;
}

.action-grid {
  margin-bottom: 4px;
}

.compact-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
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

.blank-visual {
  background: #fff;
}

.action-visual {
  background: #fff;
}

.closed-visual {
  height: 188px;
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

.tag-chip {
  color: #404040;
  background: #e5e5e5;
}

.muted-chip {
  color: #404040;
  background: #e5e5e5;
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

.member-project-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 2px;
}

.member-project-stats span {
  display: inline;
  -webkit-line-clamp: unset;
  font-size: 12px;
  color: #475467;
}

.member-project-stats .warning {
  color: #b42318;
}

.member-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
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
  .project-grid,
  .compact-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .member-strip,
  .member-detail-grid,
  .detail-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .project-grid,
  .compact-grid {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .start-view {
    min-height: auto;
    padding: 16px;
  }

  .member-strip,
  .member-detail-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .project-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .compact-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .search-box {
    width: 100%;
    height: 44px;
  }
}
</style>
