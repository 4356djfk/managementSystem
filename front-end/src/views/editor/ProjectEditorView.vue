<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import saveIcon from '@/assets/save-icon.svg'
import {
  createProjectArchive,
  createProjectLessonLearned,
  createProjectTimesheet,
  deleteProjectAttachment,
  deleteProjectArchive,
  deleteProjectLessonLearned,
  deleteProjectTimesheet,
  generateProjectReport,
  generateProjectSummaryReport,
  getProjectArchives,
  getProjectCalendar,
  getProjectClosureCheck,
  getProjectLessonsLearned,
  getProjectCriticalPath,
  getProjectReports,
  getProjectScheduleAlerts,
  getProjectTimesheets,
  getProjectTimesheetReport,
  uploadProjectAttachment,
  updateProjectLessonLearned,
  updateProjectTimesheet,
} from '@/api/editor'
import {
  addProjectMember,
  changeProjectStatus,
  closeProject,
  deleteProject,
  getProjectMembers,
  getProjectCharter,
  getProjectDashboard,
  getProjectDetail,
  removeProjectMember,
  saveProjectCharter,
  updateProjectMember,
  updateProject,
} from '@/api/projects'
import { getUsers } from '@/api/users'
import {
  createProjectRequirement,
  createProjectScopeBaseline,
  createProjectWbsNode,
  deleteProjectScopeBaseline,
  getProjectRequirements,
  getProjectScopeBaselines,
  getProjectWbs,
} from '@/api/scope'
import {
  createProjectCostActual,
  createProjectCostBaseline,
  createProjectCostPlan,
  deleteProjectCostActual,
  deleteProjectCostBaseline,
  deleteProjectCostPlan,
  getProjectCostActuals,
  getProjectCostBaselines,
  getProjectCostPlans,
  getProjectEvm,
  updateProjectCostPlan,
} from '@/api/cost'
import {
  createProjectTaskDependency,
  createProjectTaskComment,
  createProjectTask,
  deleteProjectTaskDependency,
  deleteProjectTaskComment,
  deleteProjectTask,
  getProjectTaskDependencies,
  getProjectTaskDetail,
  getProjectTasks,
  updateProjectTaskProgress,
  updateProjectTask,
} from '@/api/tasks'
import {
  createProjectMilestone,
  deleteProjectMilestone,
  getProjectMilestones,
  updateProjectMilestone,
} from '@/api/milestones'
import {
  createProjectRisk,
  deleteProjectRisk,
  getProjectRiskMatrix,
  getProjectRisks,
  updateProjectRisk,
  updateProjectRiskStatus,
} from '@/api/risk'
import {
  createAcceptanceItem,
  createExportTask,
  deleteAcceptanceItem,
  getAcceptanceItems,
  getAuditLogs,
  importExcelFile,
  searchGlobal,
  updateAcceptanceItem,
} from '@/api/ops'
import {
  approveProjectChangeRequest,
  createProjectChangeRequest,
  getProjectChangeRequestDetail,
  getProjectChangeRequestLogs,
  getProjectChangeRequests,
} from '@/api/change'
import { API_BASE_URL } from '@/api/http'

const router = useRouter()
const route = useRoute()

const DAY_WIDTH = 32
const MIN_ROW_COUNT = 18
const DAY_NAMES = ['日', '一', '二', '三', '四', '五', '六']
const EDITABLE_FIELDS = ['mode', 'name', 'duration', 'start', 'finish']
const GANTT_APPEARANCE_STORAGE_KEY = 'pm_global_gantt_appearance'
const WBS_CONFIG_STORAGE_PREFIX = 'pm_project_wbs_config_'
const COST_DRAFT_BINDINGS_STORAGE_PREFIX = 'pm_project_cost_draft_bindings_'
let nextDraftCostPlanId = 1

const projectId = computed(() => {
  const raw = route.params.projectId
  return typeof raw === 'string' && raw.trim() ? raw.trim() : null
})

const allTabs = [
  { key: 'file', label: '文件' },
  { key: 'task', label: '任务' },
  { key: 'project', label: '项目' },
  { key: 'team', label: '团队' },
  { key: 'format', label: '格式' },
  { key: 'report', label: '报表' },
]

const ganttFormats = [
  {
    key: 'classic-blue',
    label: '经典蓝',
    weekend: '#f5f8ff',
    taskColor: '#7fb0ff',
    taskBorder: '#1f5fbf',
    summaryColor: '#5b8def',
    summaryBorder: '#1e4e9f',
    milestoneColor: '#1f5fbf',
    milestoneBorder: '#163f80',
  },
  {
    key: 'forest-green',
    label: '森林绿',
    weekend: '#f4faf5',
    taskColor: '#95d6ad',
    taskBorder: '#2f7d4b',
    summaryColor: '#68b885',
    summaryBorder: '#25643c',
    milestoneColor: '#2f7d4b',
    milestoneBorder: '#1f5634',
  },
  {
    key: 'slate-gray',
    label: '石板灰',
    weekend: '#f6f6f6',
    taskColor: '#d0d6de',
    taskBorder: '#6c7785',
    summaryColor: '#aeb7c3',
    summaryBorder: '#57616e',
    milestoneColor: '#6c7785',
    milestoneBorder: '#4c5562',
  },
]

const ganttColorPalette = [
  '#1f5fbf', '#4d7cff', '#00a6fb', '#00b894', '#27ae60', '#7cb342',
  '#f39c12', '#ff8f3d', '#d35400', '#e74c3c', '#d6336c', '#8e44ad',
  '#6c5ce7', '#546e7a', '#37474f', '#8d6e63', '#00838f', '#5d4037',
]

const ganttStylePalette = {
  task: [
    { key: 'task-azure', label: '蔚蓝', color: '#7fb0ff', border: '#1f5fbf' },
    { key: 'task-teal', label: '青绿', color: '#73d7c4', border: '#138a72' },
    { key: 'task-lime', label: '青柠', color: '#b5dd71', border: '#6c9a1f' },
    { key: 'task-orange', label: '橙色', color: '#f7bb7a', border: '#c96c16' },
    { key: 'task-coral', label: '珊瑚', color: '#f29b8f', border: '#cb5846' },
    { key: 'task-violet', label: '紫罗兰', color: '#b49cff', border: '#6a4fd2' },
    { key: 'task-slate', label: '石板', color: '#c4ccd8', border: '#5c6675' },
  ],
  summary: [
    { key: 'summary-blue', label: '蓝色', color: '#5b8def', border: '#1e4e9f' },
    { key: 'summary-green', label: '绿色', color: '#62be8a', border: '#237048' },
    { key: 'summary-gold', label: '金色', color: '#dcb255', border: '#9e7409' },
    { key: 'summary-red', label: '红色', color: '#f08a8a', border: '#bb4b4b' },
    { key: 'summary-purple', label: '紫色', color: '#9d8cff', border: '#5f4fc8' },
    { key: 'summary-charcoal', label: '炭灰', color: '#8d9aaa', border: '#495360' },
  ],
  milestone: [
    { key: 'milestone-blue', label: '蓝色', color: '#1f5fbf', border: '#163f80' },
    { key: 'milestone-green', label: '绿色', color: '#239b56', border: '#186a3b' },
    { key: 'milestone-gold', label: '金色', color: '#efb840', border: '#b98313' },
    { key: 'milestone-red', label: '红色', color: '#db4f4f', border: '#9f2727' },
    { key: 'milestone-violet', label: '紫色', color: '#7d5fff', border: '#5134c3' },
    { key: 'milestone-dark', label: '深灰', color: '#5f6978', border: '#39414d' },
  ],
}

const ganttShapeOptions = {
  task: [
    { key: 'rounded', label: '圆角条' },
    { key: 'square', label: '方角条' },
    { key: 'capsule', label: '胶囊条' },
  ],
  summary: [
    { key: 'bracket', label: '括号摘要' },
    { key: 'pill', label: '圆角摘要' },
    { key: 'block', label: '块状摘要' },
  ],
  milestone: [
    { key: 'diamond', label: '菱形里程碑' },
    { key: 'square', label: '方块里程碑' },
    { key: 'circle', label: '圆形里程碑' },
  ],
}

const activeTab = ref('task')
const ganttFormat = ref('classic-blue')
const loading = ref(false)
const saving = ref(false)
const hasUnsavedChanges = ref(false)
const isHydratingRows = ref(false)
const isSyncingSummary = ref(false)

const taskRows = ref([])
const loadedTasks = ref([])
const projectMembers = ref([])
const availableUsers = ref([])
const activeCell = ref({ rowId: null, field: '' })
const inputRefs = new Map()
let nextLocalRowId = 1

const projectDetail = ref(null)
const projectDashboard = ref(null)
const projectCharter = ref(null)
const calendarEvents = ref([])
const criticalPathTasks = ref([])
const scheduleAlerts = ref([])
const reportList = ref([])
const timesheetReport = ref(null)
const timesheetRecords = ref([])
const closureCheck = ref(null)
const closureArchives = ref([])
const closureLessons = ref([])
const acceptanceItems = ref([])
const changeRequests = ref([])
const selectedChangeRequest = ref(null)
const changeRequestLogs = ref([])
const projectTaskDependencies = ref([])

const infoDialogVisible = ref(false)
const charterDialogVisible = ref(false)
const dashboardDialogVisible = ref(false)
const projectMembersDialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const taskInfoDialogVisible = ref(false)
const milestoneDialogVisible = ref(false)
const riskDialogVisible = ref(false)
const riskMatrixDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const importDialogVisible = ref(false)
const globalSearchDialogVisible = ref(false)
const auditLogDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const ganttStyleDialogVisible = ref(false)
const wbsDialogVisible = ref(false)
const requirementDialogVisible = ref(false)
const scopeBaselineDialogVisible = ref(false)
const costPlanDialogVisible = ref(false)
const costActualDialogVisible = ref(false)
const costBaselineDialogVisible = ref(false)
const evmDialogVisible = ref(false)
const projectStatusDialogVisible = ref(false)
const timesheetDialogVisible = ref(false)
const projectClosureDialogVisible = ref(false)
const acceptanceDialogVisible = ref(false)
const changeRequestDialogVisible = ref(false)
const closureStep = ref('acceptance')
const taskMineOnly = ref(false)
const selectedBarStyleType = ref('main')
let ganttAppearanceSnapshot = null
let wbsConfigSnapshot = null
const wbsTreeExpandedKeys = ref({})
const activeScopeBaselineId = ref(null)
const scopeBaselineTreeExpandedKeys = ref({})
const taskDetailLoading = ref(false)
const taskDetail = ref(null)
const milestoneLoading = ref(false)
const milestoneList = ref([])
const riskLoading = ref(false)
const riskList = ref([])
const riskMatrix = ref({ levels: [], highCount: 0, criticalCount: 0 })
const opsLoading = ref(false)
const exportTasks = ref([])
const importResult = ref(null)
const globalSearchResults = ref([])
const auditLogs = ref([])
const taskDetailSection = ref('basic')
const pendingRouteTaskId = ref('')
const allTaskDetailSections = [
  { key: 'basic', label: '基础信息' },
  { key: 'progress', label: '进度' },
  { key: 'dependency', label: '依赖' },
  { key: 'risk', label: '风险' },
  { key: 'comment', label: '评论' },
]

const projectMemberForm = reactive({
  memberId: '',
  userId: '',
  projectRole: 'TEAM_MEMBER',
  memberStatus: 'ACTIVE',
})

const taskProgressForm = reactive({
  status: 'TODO',
  progress: 0,
  remark: '',
})

const taskBasicForm = reactive({
  description: '',
  milestoneId: '',
  assigneeId: '',
})

const taskDependencyForm = reactive({
  predecessorTaskId: '',
  dependencyType: 'FS',
})

const taskCommentForm = reactive({
  content: '',
  replyToId: '',
  replyToName: '',
})

const milestoneForm = reactive({
  name: '',
  description: '',
  plannedDate: '',
  status: 'PENDING',
})

const editingMilestoneId = ref(null)
const editingRiskId = ref(null)
const editingTimesheetId = ref(null)
const editingLessonId = ref(null)
const editingAcceptanceId = ref(null)

const timesheetFilterForm = reactive({
  taskId: '',
  startDate: '',
  endDate: '',
})

const timesheetForm = reactive({
  taskId: '',
  workDate: '',
  hours: '',
  description: '',
})

const archiveForm = reactive({
  itemName: '',
  archiveType: 'DOCUMENT',
  attachmentId: '',
  attachmentName: '',
  repositoryUrl: '',
  status: 'ARCHIVED',
})

const lessonForm = reactive({
  category: '过程',
  title: '',
  content: '',
})

const closureSummaryForm = reactive({
  type: 'SUMMARY',
  startDate: '',
  endDate: '',
})

const acceptanceForm = reactive({
  itemName: '',
  itemType: 'DOCUMENT',
  description: '',
  status: 'PENDING',
  ownerId: '',
  attachmentId: '',
  attachmentName: '',
})

const changeRequestFilterForm = reactive({
  status: '',
  priority: '',
})

const changeRequestForm = reactive({
  title: '',
  type: 'SCOPE',
  priority: 'MEDIUM',
  impactAnalysis: '',
  reason: '',
})

const changeApprovalForm = reactive({
  id: '',
  decision: 'APPROVED',
  comment: '',
})

const riskForm = reactive({
  name: '',
  description: '',
  probability: 3,
  impact: 3,
  level: 'MEDIUM',
  responseStrategy: '',
  taskId: '',
  phaseName: '',
})

const riskStatusForm = reactive({
  riskId: '',
  status: 'IDENTIFIED',
  comment: '',
})

const exportForm = reactive({
  module: 'TASK',
  format: 'EXCEL',
})

const importForm = reactive({
  module: 'TASK',
  file: null,
  fileName: '',
})

const searchForm = reactive({
  keyword: '',
  type: '',
})

const auditForm = reactive({
  module: '',
  startTime: '',
  endTime: '',
})

const projectForm = reactive({
  name: '',
  description: '',
  startDate: '',
  endDate: '',
  lifeCycleModel: 'AGILE',
  ownerId: '',
  plannedBudget: '',
})

const charterForm = reactive({
  objective: '',
  scopeSummary: '',
  sponsor: '',
  approver: '',
  stakeholders: '',
  successCriteria: '',
})

const reportGenerateForm = reactive({
  type: 'WEEKLY',
  startDate: '',
  endDate: '',
})

const wbsNodes = ref([])
const requirements = ref([])
const scopeBaselines = ref([])
const costPlans = ref([])
const costActuals = ref([])
const costBaselines = ref([])
const evmMetrics = ref(null)
const editingCostPlanId = ref(null)
const draftCostPlans = ref([])

const wbsForm = reactive({
  name: '',
  code: '',
  parentId: null,
  description: '',
})

const requirementForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
})

const scopeBaselineForm = reactive({
  name: '',
  description: '',
})

const costPlanForm = reactive({
  name: '',
  type: 'LABOR',
  taskId: '',
  phase: '',
  plannedAmount: '',
  currency: 'CNY',
  remark: '',
})

const costActualForm = reactive({
  taskId: '',
  sourceType: 'MANUAL',
  amount: '',
  currency: 'CNY',
  spendDate: '',
  remark: '',
})

const costBaselineForm = reactive({
  name: '',
})

function createDefaultWbsConfig() {
  return {
    prefix: '',
    separator: '.',
    paddingWidth: 1,
  }
}

const wbsConfig = reactive(createDefaultWbsConfig())

function createDefaultGanttAppearance() {
  return {
    mainColor: ganttFormats[0].taskColor,
    mainBorder: ganttFormats[0].taskBorder,
    mainShape: 'rounded',
    mainThickness: 20,
    childColor: '#8fd3a8',
    childBorder: '#2f7d4b',
    childShape: 'capsule',
    childThickness: 16,
    parentColor: '#89aef7',
    parentBorder: '#315fb5',
    parentShape: 'square',
    parentThickness: 24,
    summaryColor: ganttFormats[0].summaryColor,
    summaryBorder: ganttFormats[0].summaryBorder,
    summaryShape: 'bracket',
    summaryThickness: 10,
    milestoneColor: ganttFormats[0].milestoneColor,
    milestoneBorder: ganttFormats[0].milestoneBorder,
    milestoneShape: 'diamond',
    milestoneSize: 16,
  }
}

const ganttAppearance = reactive(createDefaultGanttAppearance())

const ganttBarStyleOptions = [
  { key: 'main', label: '主任务' },
  { key: 'child', label: '子任务' },
  { key: 'parent', label: '父任务' },
  { key: 'summary', label: '摘要任务' },
  { key: 'milestone', label: '里程碑' },
]

const lifeCycleOptions = [
  { label: '敏捷开发', value: 'AGILE' },
  { label: '瀑布开发', value: 'WATERFALL' },
  { label: '迭代开发', value: 'ITERATIVE' },
  { label: '混合模式', value: 'HYBRID' },
]

const projectStatusOptions = [
  { label: '规划中', value: 'PLANNING' },
  { label: '进行中', value: 'IN_PROGRESS' },
  { label: '监控中', value: 'MONITORING' },
  { label: '已关闭', value: 'CLOSED' },
]

const riskLevelOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' },
  { label: '严重', value: 'CRITICAL' },
]

const riskStatusOptions = [
  { label: '已识别', value: 'IDENTIFIED' },
  { label: '已分析', value: 'ANALYZED' },
  { label: '已响应', value: 'RESPONDING' },
  { label: '已关闭', value: 'CLOSED' },
]

const archiveTypeOptions = [
  { label: '文档', value: 'DOCUMENT' },
  { label: '代码', value: 'CODE' },
  { label: '合同', value: 'CONTRACT' },
  { label: '交付物', value: 'DELIVERABLE' },
  { label: '其他', value: 'OTHER' },
]

const acceptanceStatusOptions = [
  { label: '待验收', value: 'PENDING' },
  { label: '验收中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
]

const acceptanceTypeOptions = [
  { label: '文档', value: 'DOCUMENT' },
  { label: '功能', value: 'FEATURE' },
  { label: '测试', value: 'TEST' },
  { label: '交付物', value: 'DELIVERABLE' },
]

const projectMemberRoleOptions = [
  { label: '项目经理', value: 'PROJECT_MANAGER' },
  { label: '团队成员', value: 'TEAM_MEMBER' },
  { label: '只读成员', value: 'READ_ONLY' },
]

const projectMemberStatusOptions = [
  { label: '在项', value: 'ACTIVE' },
  { label: '已移除', value: 'REMOVED' },
]

const changeTypeOptions = [
  { label: '范围变更', value: 'SCOPE' },
  { label: '进度变更', value: 'SCHEDULE' },
  { label: '成本变更', value: 'COST' },
  { label: '质量变更', value: 'QUALITY' },
]

const changePriorityOptions = [
  { label: '高', value: 'HIGH' },
  { label: '中', value: 'MEDIUM' },
  { label: '低', value: 'LOW' },
]

const changeDecisionOptions = [
  { label: '批准', value: 'APPROVED' },
  { label: '驳回', value: 'REJECTED' },
]

const exportModuleOptions = [
  { label: '任务', value: 'TASK' },
  { label: '风险', value: 'RISK' },
  { label: '成本', value: 'COST' },
  { label: '工时', value: 'TIMESHEET' },
  { label: '报表', value: 'REPORT' },
]

const exportFormatOptions = [
  { label: 'Excel', value: 'EXCEL' },
]

const searchTypeOptions = [
  { label: '全部', value: '' },
  { label: '项目', value: 'PROJECT' },
  { label: '任务', value: 'TASK' },
  { label: '风险', value: 'RISK' },
  { label: '经验教训', value: 'LESSON' },
]

const importTemplateConfig = {
  TASK: {
    fileName: '任务导入模板.xlsx',
    headers: ['name', 'description', 'status', 'taskType', 'priority', 'progress', 'plannedStartDate', 'plannedEndDate', 'plannedHours', 'parentTaskId', 'remark'],
    sample: ['需求分析', '梳理业务流程和范围', 'TODO', 'TASK', 'MEDIUM', '0', '2026-04-26 09:00:00', '2026-04-28 18:00:00', '24', '', '手动计划'],
    fields: [
      '必填: name',
      '可选: description, status, taskType, priority, progress',
      '日期支持: yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss',
      'parentTaskId 仅支持已存在任务 ID',
    ],
  },
  RISK: {
    fileName: '风险导入模板.xlsx',
    headers: ['name', 'description', 'probability', 'impact', 'level', 'status', 'responseStrategy', 'taskId', 'phaseName'],
    sample: ['核心人员流失', '关键开发离职导致延期', '4', '5', 'CRITICAL', 'IDENTIFIED', '提前安排备份人员与知识交接', '', '2.1 | 后端开发'],
    fields: [
      '必填: name, probability, impact',
      'level 可不填，系统会按 概率 x 影响 自动计算',
      'status 可选: IDENTIFIED / ANALYZED / RESPONDING / CLOSED',
      'taskId 仅支持当前项目已存在任务 ID',
    ],
  },
}

const selectedProjectStatus = ref('PLANNING')

const todayString = formatDateToValue(new Date())

const currentRibbonGroups = computed(() => {
  if (activeTab.value === 'file') {
    return [
      { title: '文件', actions: [{ key: 'save', label: '保存' }, { key: 'back', label: '返回首页' }] },
    ]
  }

  if (activeTab.value === 'task') {
    return [
      {
        title: '插入',
        actions: [
          { key: 'add-task', label: '任务' },
          { key: 'add-child', label: '子任务' },
          { key: 'add-summary', label: '摘要任务' },
          { key: 'add-milestone', label: '里程碑' },
        ],
      },
      {
        title: '结构',
        actions: [
          { key: 'indent', label: '降级' },
          { key: 'outdent', label: '升级' },
          { key: 'delete', label: '删除' },
          { key: 'task-info', label: '信息' },
        ],
      },
      {
        title: '日程',
        actions: [
          { key: 'calendar', label: '日程' },
          { key: 'alerts', label: '预警' },
          { key: 'my-tasks', label: '我的任务' },
          { key: 'save', label: '保存' },
        ],
      },
    ]
  }

  if (activeTab.value === 'project') {
    return [
      {
        title: '项目基础',
        actions: [
          { key: 'project-dashboard', label: '项目概览' },
          { key: 'project-info', label: '项目信息' },
          { key: 'project-charter', label: '项目章程' },
          { key: 'project-status', label: '状态流转' },
        ],
      },
      {
        title: '范围管理',
        actions: [
          { key: 'project-wbs', label: 'WBS' },
          { key: 'project-requirements', label: '需求' },
          { key: 'project-milestones', label: '里程碑' },
          { key: 'project-scope-baselines', label: '范围基线' },
        ],
      },
      {
        title: '风险与变更',
        actions: [
          { key: 'project-risks', label: '风险登记册' },
          { key: 'project-risk-matrix', label: '风险矩阵' },
          { key: 'project-changes', label: '变更管理' },
        ],
      },
      {
        title: '成本管理',
        actions: [
          { key: 'project-cost-plans', label: '成本计划' },
          { key: 'project-cost-actuals', label: '实际成本' },
          { key: 'project-cost-baselines', label: '成本基线' },
          { key: 'project-evm', label: 'EVM' },
        ],
      },
      {
        title: '收尾与报告',
        actions: [
          { key: 'project-acceptance', label: '验收事项' },
          { key: 'project-closure', label: '项目收尾' },
          { key: 'project-close', label: '关闭项目' },
        ],
      },
      {
        title: '平台能力',
        actions: [
          { key: 'platform-export', label: '导出' },
          { key: 'platform-import', label: 'Excel导入' },
          { key: 'platform-search', label: '全局搜索' },
          { key: 'platform-audit', label: '审计日志' },
        ],
      },
      {
        title: '危险操作',
        emphasis: 'danger',
        actions: [
          { key: 'project-delete', label: '删除项目', variant: 'danger' },
        ],
      },
    ]
  }

  if (activeTab.value === 'team') {
    return [
      {
        title: '团队',
        actions: [
          { key: 'project-members', label: '项目成员' },
          { key: 'project-timesheets', label: '工时管理' },
          { key: 'calendar', label: '日程' },
          { key: 'alerts', label: '预警' },
        ],
      },
    ]
  }

  if (activeTab.value === 'format') {
    return [
      {
        title: '甘特图格式',
        actions: [
          { key: 'open-gantt-style', label: '条形图样式' },
        ],
      },
    ]
  }

  if (activeTab.value === 'report') {
    return [
      {
        title: '报表',
        actions: [
          { key: 'report-open', label: '报表中心' },
          { key: 'report-generate', label: '生成周报' },
        ],
      },
    ]
  }

  return []
})

const currentFormat = computed(
  () => ganttFormats.find((item) => item.key === ganttFormat.value) || ganttFormats[0],
)

const ganttThemeStyle = computed(() => ({
  '--gantt-main-color': ganttAppearance.mainColor,
  '--gantt-main-border': ganttAppearance.mainBorder,
  '--gantt-child-color': ganttAppearance.childColor,
  '--gantt-child-border': ganttAppearance.childBorder,
  '--gantt-parent-color': ganttAppearance.parentColor,
  '--gantt-parent-border': ganttAppearance.parentBorder,
  '--gantt-summary-color': ganttAppearance.summaryColor,
  '--gantt-summary-border': ganttAppearance.summaryBorder,
  '--gantt-milestone-color': ganttAppearance.milestoneColor,
  '--gantt-milestone-border': ganttAppearance.milestoneBorder,
  '--gantt-weekend-color': currentFormat.value.weekend,
}))

function applyGanttFormat(formatKey) {
  const format = ganttFormats.find((item) => item.key === formatKey)
  if (!format) return
  ganttFormat.value = format.key
  ganttAppearance.mainColor = format.taskColor
  ganttAppearance.mainBorder = format.taskBorder
  ganttAppearance.childColor = format.taskColor
  ganttAppearance.childBorder = format.taskBorder
  ganttAppearance.parentColor = format.taskColor
  ganttAppearance.parentBorder = format.taskBorder
  ganttAppearance.summaryColor = format.summaryColor
  ganttAppearance.summaryBorder = format.summaryBorder
  ganttAppearance.milestoneColor = format.milestoneColor
  ganttAppearance.milestoneBorder = format.milestoneBorder
}

function updateGanttElementColor(type, colorKey) {
  const target = ganttStylePalette[type]?.find((item) => item.key === colorKey)
  if (!target) return
  ganttAppearance[`${type}Color`] = target.color
  ganttAppearance[`${type}Border`] = target.border
}

function updateGanttElementShape(type, shapeKey) {
  if (!ganttShapeOptions[type]?.some((item) => item.key === shapeKey)) return
  ganttAppearance[`${type}Shape`] = shapeKey
}

function isActionActive(actionKey) {
  if (actionKey.startsWith('format:')) return ganttFormat.value === actionKey.slice(7)
  if (actionKey === 'open-gantt-style') return ganttStyleDialogVisible.value
  if (actionKey === 'my-tasks') return effectiveTaskMineOnly.value
  return false
}

const memberReadableActionKeys = new Set([
  'back',
  'task-info',
  'calendar',
  'alerts',
  'my-tasks',
  'project-dashboard',
  'project-info',
  'project-charter',
  'project-members',
  'project-status',
  'report-open',
])

const closedReadableActionKeys = new Set([
  'task-info', 'calendar', 'alerts', 'my-tasks', 'back',
  'project-info', 'project-charter', 'project-dashboard', 'project-milestones',
  'project-risks', 'project-risk-matrix', 'project-wbs', 'project-requirements',
  'project-scope-baselines', 'project-cost-plans', 'project-cost-actuals',
  'project-cost-baselines', 'project-evm', 'project-timesheets',
  'project-acceptance', 'project-changes', 'project-closure', 'project-status',
  'report-open', 'report-generate',
])

function canUseAction(actionKey) {
  if (actionKey.startsWith('format:') || actionKey === 'open-gantt-style') {
    return true
  }

  if (actionKey === 'project-timesheets') {
    return Boolean(canUseTimesheet.value)
  }

  if (canManageProject.value) {
    if (!isClosedProject.value) return true
    return closedReadableActionKeys.has(actionKey)
  }

  if (isClosedProject.value) {
    return memberReadableActionKeys.has(actionKey)
  }

  return memberReadableActionKeys.has(actionKey)
}

function isActionDisabled(actionKey) {
  return !canUseAction(actionKey)
}

function getRowBarType(row) {
  if (isMilestoneRow(row)) return 'milestone'
  if (isSummaryRow(row)) return 'summary'
  if (hasChildRows(row)) return row.localParentId ? 'child' : 'parent'
  if (row.localParentId) return 'child'
  return 'main'
}

function getTaskBarVariant(row) {
  if (isSummaryRow(row)) return 'summary'
  if (isMilestoneRow(row)) return 'milestone'

  const depth = Number(row.outlineLevel || 0)
  if (hasChildRows(row)) {
    return depth === 0 ? 'parent-major' : 'parent-nested'
  }
  if (depth === 0) return 'main-root'
  if (depth === 1) return 'child-level-1'
  return 'child-deep'
}

function isTaskBarType(type) {
  return type === 'main' || type === 'child' || type === 'parent'
}

function getStyleOptionFamily(type) {
  return isTaskBarType(type) ? 'task' : type
}

function getBarStyleConfig(type) {
  return {
    color: ganttAppearance[`${type}Color`],
    border: ganttAppearance[`${type}Border`],
    shape: ganttAppearance[`${type}Shape`],
    thickness: ganttAppearance[`${type}Thickness`] ?? (type === 'milestone' ? 16 : 18),
    size: ganttAppearance[`${type}Size`] ?? 16,
  }
}

const currentBarStyleConfig = computed(() => getBarStyleConfig(selectedBarStyleType.value))

function normalizeHexColor(value, fallback = '#1f5fbf') {
  const raw = String(value || '').trim()
  return /^#([0-9a-fA-F]{6})$/.test(raw) ? raw.toLowerCase() : fallback
}

function getBarTop(thickness) {
  return Math.max(4, Math.round((38 - thickness) / 2))
}

function getRepresentativeVariant(type) {
  if (type === 'main') return 'main-root'
  if (type === 'child') return 'child-level-1'
  if (type === 'parent') return 'parent-major'
  return ''
}

function getPreviewBarClass(type) {
  const config = getBarStyleConfig(type)
  return {
    'task-bar': type !== 'summary',
    'summary-bar': type === 'summary',
    [`${type}-bar`]: type !== 'summary',
    [`bar-variant-${getRepresentativeVariant(type)}`]: isTaskBarType(type),
    [`task-shape-${config.shape}`]: isTaskBarType(type),
    [`summary-shape-${config.shape}`]: type === 'summary',
  }
}

function getPreviewBarStyle(type, width = 88) {
  const config = getBarStyleConfig(type)
  if (type === 'milestone') {
    return {
      width: `${config.size}px`,
      height: `${config.size}px`,
      background: config.color,
      borderColor: config.border,
      color: config.border,
    }
  }

  const thickness = Math.max(6, Number(config.thickness) || 16)
  return {
    width: `${width}px`,
    height: `${thickness}px`,
    top: 'auto',
    background: config.color,
    borderColor: config.border,
    color: config.border,
  }
}

function commitBarColor(type, field) {
  const key = `${type}${field}`
  ganttAppearance[key] = normalizeHexColor(ganttAppearance[key], createDefaultGanttAppearance()[key])
}

function cloneGanttAppearance() {
  return JSON.parse(JSON.stringify(ganttAppearance))
}

function applyGanttAppearanceConfig(config) {
  Object.assign(ganttAppearance, createDefaultGanttAppearance(), config || {})
}

function loadGlobalGanttAppearance() {
  try {
    const raw = localStorage.getItem(GANTT_APPEARANCE_STORAGE_KEY)
    if (!raw) {
      applyGanttAppearanceConfig(createDefaultGanttAppearance())
      return
    }
    applyGanttAppearanceConfig(JSON.parse(raw))
  } catch {
    applyGanttAppearanceConfig(createDefaultGanttAppearance())
  }
}

function saveGlobalGanttAppearance() {
  localStorage.setItem(GANTT_APPEARANCE_STORAGE_KEY, JSON.stringify(cloneGanttAppearance()))
}

function cloneWbsConfig() {
  return JSON.parse(JSON.stringify(wbsConfig))
}

function applyWbsConfig(config) {
  const next = { ...createDefaultWbsConfig(), ...(config || {}) }
  wbsConfig.prefix = String(next.prefix || '')
  wbsConfig.separator = String(next.separator || '.').trim() || '.'
  wbsConfig.paddingWidth = Math.max(1, Number(next.paddingWidth) || 1)
}

function loadProjectWbsConfig() {
  try {
    const raw = localStorage.getItem(`${WBS_CONFIG_STORAGE_PREFIX}${projectId.value}`)
    if (!raw) {
      applyWbsConfig(createDefaultWbsConfig())
      return
    }
    applyWbsConfig(JSON.parse(raw))
  } catch {
    applyWbsConfig(createDefaultWbsConfig())
  }
}

function saveProjectWbsConfig() {
  localStorage.setItem(`${WBS_CONFIG_STORAGE_PREFIX}${projectId.value}`, JSON.stringify(cloneWbsConfig()))
}

function formatWbsSegment(index) {
  const width = Math.max(1, Number(wbsConfig.paddingWidth) || 1)
  return width > 1 ? String(index).padStart(width, '0') : String(index)
}

function formatWbsCode(path) {
  const body = path.map((part) => formatWbsSegment(part)).join(wbsConfig.separator)
  if (!body) return ''
  return wbsConfig.prefix ? `${wbsConfig.prefix}${wbsConfig.separator}${body}` : body
}

function createEmptyRow(mode = '手动计划', isPlaceholder = true) {
  return {
    localId: nextLocalRowId++,
    taskId: null,
    backendParentTaskId: null,
    localParentId: null,
    outlineLevel: 0,
    expanded: true,
    mode,
    name: '',
    duration: '',
    start: '',
    finish: '',
    taskType: 'TASK',
    status: 'TODO',
    progress: 0,
    assigneeId: null,
    assigneeName: '',
    sortOrder: 0,
    wbsCode: '',
    isPlaceholder,
  }
}

function mapModeToTaskType(mode) {
  if (String(mode || '').includes('里程碑')) {
    return 'MILESTONE_TASK'
  }
  if (String(mode || '').includes('摘要')) {
    return 'SUB_TASK'
  }
  return 'TASK'
}

function isSummaryRow(row) {
  return String(row.mode || '').includes('摘要')
}

function isMilestoneRow(row) {
  return row.taskType === 'MILESTONE_TASK' || String(row.mode || '').includes('里程碑')
}

function createRowFromTask(task, index) {
  const row = createEmptyRow(task.remark || '手动计划', false)
  row.taskId = task.id != null ? String(task.id) : null
  row.backendParentTaskId = task.parentTaskId != null ? String(task.parentTaskId) : null
  row.name = task.name || ''
  row.start = toDateValue(task.plannedStartDate)
  row.finish = toDateValue(task.plannedEndDate)
  row.duration = formatDuration(task.plannedHours)
  row.progress = Number(task.progress || 0)
  row.status = task.status || 'TODO'
  row.assigneeId = task.assigneeId != null ? String(task.assigneeId) : null
  row.assigneeName = task.assigneeName || ''
  row.taskType = task.taskType || mapModeToTaskType(task.remark || '')
  row.sortOrder = task.sortOrder ?? index + 1

  if (!row.duration && row.start && row.finish) {
    row.duration = String(diffDays(new Date(`${row.start}T00:00:00`), new Date(`${row.finish}T00:00:00`)) + 1)
  }

  return row
}

function hasRowContent(row) {
  return [row.name, row.duration, row.start, row.finish].some((item) => String(item || '').trim() !== '')
}

function ensureMinimumRows() {
  while (taskRows.value.length < MIN_ROW_COUNT) {
    taskRows.value.push(createEmptyRow(taskRows.value.length === 0 ? '手动计划' : '', true))
  }
}

function ensureTrailingEmptyRows() {
  const tail = taskRows.value.slice(-4)
  if (tail.filter(hasRowContent).length >= 2) {
    taskRows.value.push(...Array.from({ length: 6 }, () => createEmptyRow('', true)))
  }
}

function rebuildHierarchyFromBackend(rows) {
  const rowMap = new Map(rows.map((row) => [row.taskId, row]))

  rows.forEach((row) => {
    row.localParentId = null
    row.outlineLevel = 0
    row.expanded = row.expanded ?? true
  })

  rows.forEach((row) => {
    if (row.backendParentTaskId) {
      const parentRow = rowMap.get(String(row.backendParentTaskId))
      if (parentRow) {
        row.localParentId = parentRow.localId
      }
    }
  })

  const localMap = new Map(rows.map((row) => [row.localId, row]))
  const resolveLevel = (row) => {
    if (!row.localParentId) return 0
    const parent = localMap.get(row.localParentId)
    return parent ? resolveLevel(parent) + 1 : 0
  }

  rows.forEach((row) => {
    row.outlineLevel = resolveLevel(row)
  })

  return rows.sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
}

function refreshOutlineLevels() {
  const rowMap = new Map(taskRows.value.map((row) => [row.localId, row]))

  const resolveLevel = (row) => {
    if (!row.localParentId) return 0
    const parent = rowMap.get(row.localParentId)
    return parent ? resolveLevel(parent) + 1 : 0
  }

  taskRows.value.forEach((row, index) => {
    row.outlineLevel = resolveLevel(row)
    row.sortOrder = index + 1
  })

  refreshWbsCodes()
}

function refreshWbsCodes() {
  const childCounters = new Map()
  const pathMap = new Map()

  taskRows.value.forEach((row) => {
    if (isBlankPlaceholderRow(row)) {
      row.wbsCode = ''
      return
    }

    const parentKey = row.localParentId || 'root'
    const nextIndex = (childCounters.get(parentKey) || 0) + 1
    childCounters.set(parentKey, nextIndex)

    const parentPath = row.localParentId ? (pathMap.get(row.localParentId) || []) : []
    const currentPath = [...parentPath, nextIndex]
    pathMap.set(row.localId, currentPath)
    row.wbsCode = formatWbsCode(currentPath)
  })
}

function buildWbsTree() {
  const sourceRows = taskRows.value.filter((row) => !isBlankPlaceholderRow(row))
  const nodeMap = new Map()
  const roots = []

  sourceRows.forEach((row) => {
    nodeMap.set(row.localId, {
      localId: row.localId,
      wbsCode: row.wbsCode,
      name: row.name,
      outlineLevel: row.outlineLevel || 0,
      children: [],
    })
  })

  sourceRows.forEach((row) => {
    const node = nodeMap.get(row.localId)
    if (!node) return
    if (row.localParentId && nodeMap.has(row.localParentId)) {
      nodeMap.get(row.localParentId).children.push(node)
    } else {
      roots.push(node)
    }
  })

  return roots
}

const wbsTreeNodes = computed(() => buildWbsTree())

function isWbsTreeExpanded(localId) {
  return wbsTreeExpandedKeys.value[localId] !== false
}

function toggleWbsTreeNode(localId) {
  wbsTreeExpandedKeys.value = {
    ...wbsTreeExpandedKeys.value,
    [localId]: !isWbsTreeExpanded(localId),
  }
}

function buildScopeBaselineSnapshot() {
  const rows = taskRows.value
    .filter((row) => !isBlankPlaceholderRow(row))
    .map((row) => ({
      localId: row.localId,
      taskId: row.taskId || null,
      backendParentTaskId: row.backendParentTaskId || null,
      parentLocalId: row.localParentId || null,
      name: row.name || '',
      mode: row.mode || '手动计划',
      taskType: row.taskType || mapModeToTaskType(row.mode),
      wbsCode: row.wbsCode || '',
      outlineLevel: row.outlineLevel || 0,
      sortOrder: row.sortOrder || 0,
    }))

  return JSON.stringify({
    snapshotType: 'WBS_ONLY',
    createdAt: new Date().toISOString(),
    wbsConfig: cloneWbsConfig(),
    tasks: rows,
  })
}

function parseScopeBaselineSnapshot(item) {
  try {
    const parsed = JSON.parse(item?.snapshotJson || '{}')
    return Array.isArray(parsed?.tasks) ? parsed : { ...parsed, tasks: [] }
  } catch {
    return { tasks: [] }
  }
}

function buildSnapshotTree(snapshot) {
  const tasks = Array.isArray(snapshot?.tasks) ? snapshot.tasks : []
  const nodeMap = new Map()
  const roots = []

  tasks.forEach((task) => {
    nodeMap.set(task.localId, {
      ...task,
      children: [],
    })
  })

  tasks.forEach((task) => {
    const node = nodeMap.get(task.localId)
    if (!node) return
    if (task.parentLocalId && nodeMap.has(task.parentLocalId)) {
      nodeMap.get(task.parentLocalId).children.push(node)
    } else {
      roots.push(node)
    }
  })

  return roots
}

function isScopeBaselineActive(itemId) {
  return activeScopeBaselineId.value === itemId
}

function toggleScopeBaselinePreview(itemId) {
  activeScopeBaselineId.value = activeScopeBaselineId.value === itemId ? null : itemId
}

function isScopeBaselineTreeExpanded(localId) {
  return scopeBaselineTreeExpandedKeys.value[localId] !== false
}

function toggleScopeBaselineTreeNode(localId) {
  scopeBaselineTreeExpandedKeys.value = {
    ...scopeBaselineTreeExpandedKeys.value,
    [localId]: !isScopeBaselineTreeExpanded(localId),
  }
}

async function restoreScopeBaseline(item) {
  const snapshot = parseScopeBaselineSnapshot(item)
  const tasks = Array.isArray(snapshot?.tasks) ? snapshot.tasks : []

  if (!tasks.length) {
    ElMessage.warning('该基线下没有可恢复的 WBS 快照')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认将当前任务结构回退到基线“${item.baselineName || item.name || item.id}”吗？这会覆盖当前编辑器中的任务/WBS 结构。`,
      '回退范围基线',
      {
        type: 'warning',
        confirmButtonText: '确认回退',
        cancelButtonText: '取消',
      },
    )

    const currentTaskMap = new Map(
      taskRows.value
        .filter((row) => !isBlankPlaceholderRow(row))
        .map((row) => [String(row.taskId || row.localId), row]),
    )

    const localIdMap = new Map()
    const restoredRows = tasks
      .slice()
      .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      .map((task) => {
        const current = currentTaskMap.get(String(task.taskId || task.localId))
        const row = createEmptyRow(task.mode || '手动计划', false)
        row.taskId = task.taskId ? String(task.taskId) : null
        row.backendParentTaskId = task.backendParentTaskId ? String(task.backendParentTaskId) : null
        row.name = task.name || ''
        row.mode = task.mode || current?.mode || '手动计划'
        row.taskType = task.taskType || current?.taskType || mapModeToTaskType(row.mode)
        row.duration = current?.duration || ''
        row.start = current?.start || ''
        row.finish = current?.finish || ''
        row.progress = Number(current?.progress || 0)
        row.sortOrder = task.sortOrder || 0
        row.expanded = true
        localIdMap.set(task.localId, row.localId)
        row._snapshotParentLocalId = task.parentLocalId || null
        return row
      })

    restoredRows.forEach((row) => {
      row.localParentId = row._snapshotParentLocalId ? (localIdMap.get(row._snapshotParentLocalId) || null) : null
      delete row._snapshotParentLocalId
    })

    replaceTaskRows(restoredRows)
    hasUnsavedChanges.value = true
    activeScopeBaselineId.value = item.id
    ElMessage.success('已回退到所选范围基线，当前尚未保存到后端')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '回退范围基线失败')
    }
  }
}

function replaceTaskRows(rows) {
  isHydratingRows.value = true
  taskRows.value = rebuildHierarchyFromBackend(rows)
  refreshWbsCodes()
  ensureMinimumRows()
  ensureTrailingEmptyRows()
  syncSummaryRows()
  activeCell.value = { rowId: null, field: '' }
  nextTick(() => {
    isHydratingRows.value = false
    hasUnsavedChanges.value = false
  })
}

function setInputRef(rowId, field, el) {
  const key = `${rowId}-${field}`
  if (el) inputRefs.set(key, el)
  else inputRefs.delete(key)
}

function selectCell(rowId, field) {
  activeCell.value = { rowId, field }
}

function activateEditableCell(rowId, field) {
  if (!canEditTaskPlan.value) return
  selectCell(rowId, field)
}

function selectTaskRow(row, field = 'name') {
  if (!row?.localId) return
  selectCell(row.localId, field)
}

function isActiveCell(rowId, field) {
  return activeCell.value.rowId === rowId && activeCell.value.field === field
}

function focusCell(rowId, field) {
  activeCell.value = { rowId, field }
  nextTick(() => {
    const input = inputRefs.get(`${rowId}-${field}`)
    input?.focus?.()
    input?.select?.()
  })
}

function focusHorizontalCell(rowId, field, direction) {
  const index = EDITABLE_FIELDS.indexOf(field)
  const nextField = EDITABLE_FIELDS[index + direction]
  if (nextField) focusCell(rowId, nextField)
}

function focusVerticalCell(rowId, field, direction) {
  const rowIndex = taskRows.value.findIndex((row) => row.localId === rowId)
  if (rowIndex < 0) return
  if (direction > 0) ensureTrailingEmptyRows()
  const target = taskRows.value[rowIndex + direction]
  if (target) focusCell(target.localId, field)
}

function focusNextRow(rowId, field) {
  focusVerticalCell(rowId, field, 1)
}

function getSelectedRow() {
  return taskRows.value.find((row) => row.localId === activeCell.value.rowId) || null
}

const selectedTaskRow = computed(() => getSelectedRow())

function findRowByTaskId(taskId) {
  if (taskId == null || taskId === '') return null
  return taskRows.value.find((row) => String(row.taskId || '') === String(taskId)) || null
}

async function clearRouteTaskQuery() {
  const nextQuery = { ...route.query }
  delete nextQuery.taskId
  await router.replace({
    name: 'project-editor',
    params: { projectId: projectId.value },
    query: nextQuery,
  })
}

async function openRouteTaskIfNeeded() {
  if (!pendingRouteTaskId.value) return
  const row = findRowByTaskId(pendingRouteTaskId.value)
  if (!row) return
  const taskId = pendingRouteTaskId.value
  pendingRouteTaskId.value = ''
  await openTaskInfoDialog(row)
  if (String(route.query.taskId || '') === String(taskId)) {
    await clearRouteTaskQuery()
  }
}

function findRowIndex(localId) {
  return taskRows.value.findIndex((row) => row.localId === localId)
}

function findSubtreeEndIndex(startIndex) {
  const baseLevel = taskRows.value[startIndex]?.outlineLevel ?? 0
  let endIndex = startIndex
  for (let index = startIndex + 1; index < taskRows.value.length; index += 1) {
    if ((taskRows.value[index].outlineLevel ?? 0) <= baseLevel) break
    endIndex = index
  }
  return endIndex
}

function isBlankPlaceholderRow(row) {
  return Boolean(row)
    && row.isPlaceholder
    && !row.taskId
    && !hasChildRows(row)
    && !hasRowContent(row)
}

function findFirstBlankPlaceholderIndex() {
  return taskRows.value.findIndex((row) => isBlankPlaceholderRow(row))
}

function insertRowNearSelection(row, asChild = false) {
  const selectedRow = getSelectedRow()
  let insertIndex = taskRows.value.length

  if (selectedRow) {
    const selectedIndex = findRowIndex(selectedRow.localId)
    if (!asChild && isBlankPlaceholderRow(selectedRow)) {
      row.localId = selectedRow.localId
      row.taskId = selectedRow.taskId
      row.backendParentTaskId = selectedRow.backendParentTaskId
      row.localParentId = selectedRow.localParentId
      row.outlineLevel = selectedRow.outlineLevel
      row.sortOrder = selectedRow.sortOrder
      taskRows.value.splice(selectedIndex, 1, row)
      refreshOutlineLevels()
      hasUnsavedChanges.value = true
      nextTick(() => focusCell(row.localId, 'name'))
      return
    }
    insertIndex = findSubtreeEndIndex(selectedIndex) + 1

    if (asChild) {
      row.localParentId = selectedRow.localId
      row.outlineLevel = selectedRow.outlineLevel + 1
      insertIndex = findSubtreeEndIndex(selectedIndex) + 1
    } else {
      row.localParentId = selectedRow.localParentId
      row.outlineLevel = selectedRow.outlineLevel
    }
  } else if (!asChild) {
    const blankIndex = findFirstBlankPlaceholderIndex()
    if (blankIndex >= 0) {
      const blankRow = taskRows.value[blankIndex]
      row.localId = blankRow.localId
      row.taskId = blankRow.taskId
      row.backendParentTaskId = blankRow.backendParentTaskId
      row.localParentId = blankRow.localParentId
      row.outlineLevel = blankRow.outlineLevel
      row.sortOrder = blankRow.sortOrder
      taskRows.value.splice(blankIndex, 1, row)
      refreshOutlineLevels()
      hasUnsavedChanges.value = true
      nextTick(() => focusCell(row.localId, 'name'))
      return
    }
  }

  taskRows.value.splice(insertIndex, 0, row)
  refreshOutlineLevels()
  hasUnsavedChanges.value = true
  nextTick(() => focusCell(row.localId, 'name'))
}

function addTaskRow() {
  insertRowNearSelection(createEmptyRow('手动计划', false))
}

function getNextChildTaskName(parentRow) {
  if (!parentRow) return '子任务1'
  const siblingCount = taskRows.value.filter((row) => row.localParentId === parentRow.localId).length
  return `子任务${siblingCount + 1}`
}

function getNextNamedRowLabel(keyword) {
  const count = taskRows.value.filter((row) => String(row.name || '').startsWith(keyword)).length
  return `${keyword}${count + 1}`
}

function addChildTaskRow() {
  const base = getSelectedRow()
  if (!base) {
    ElMessage.warning('请先选中一条任务')
    return
  }
  if (isBlankPlaceholderRow(base)) {
    ElMessage.warning('请先填写或选中一条有效任务，再添加子任务')
    return
  }
  const row = createEmptyRow('子任务')
  row.isPlaceholder = false
  row.name = getNextChildTaskName(base)
  insertRowNearSelection(row, true)
}

function addSummaryTaskRow() {
  const row = createEmptyRow('摘要任务', false)
  row.name = getNextNamedRowLabel('摘要')
  row.taskType = 'SUB_TASK'
  insertRowNearSelection(row)
}

function addMilestoneRow() {
  const row = createEmptyRow('里程碑', false)
  row.name = getNextNamedRowLabel('里程碑')
  row.duration = '0'
  row.taskType = 'MILESTONE_TASK'
  insertRowNearSelection(row)
}

function indentSelectedRow() {
  const row = getSelectedRow()
  if (!row) {
    ElMessage.warning('请先选中一条任务')
    return
  }
  const rowIndex = findRowIndex(row.localId)
  if (rowIndex <= 0) return

  const targetParentLevel = Number(row.outlineLevel || 0)
  let nextParent = null

  for (let index = rowIndex - 1; index >= 0; index -= 1) {
    const candidate = taskRows.value[index]
    if (isBlankPlaceholderRow(candidate)) continue
    if (Number(candidate.outlineLevel || 0) === targetParentLevel) {
      nextParent = candidate
      break
    }
  }

  if (!nextParent || nextParent.localId === row.localParentId) return
  row.localParentId = nextParent.localId
  refreshOutlineLevels()
}

function outdentSelectedRow() {
  const row = getSelectedRow()
  if (!row || !row.localParentId) return
  const parent = taskRows.value.find((item) => item.localId === row.localParentId)
  row.localParentId = parent?.localParentId || null
  refreshOutlineLevels()
}

function deleteSelectedTaskRow() {
  const row = getSelectedRow()
  if (!row) {
    ElMessage.warning('请先选中要删除的任务')
    return
  }
  const startIndex = findRowIndex(row.localId)
  const endIndex = findSubtreeEndIndex(startIndex)
  taskRows.value.splice(startIndex, endIndex - startIndex + 1)
  refreshOutlineLevels()
  ensureMinimumRows()
  ensureTrailingEmptyRows()
  activeCell.value = { rowId: null, field: '' }
  hasUnsavedChanges.value = true
}

function getDescendantRows(parentLocalId) {
  const parent = taskRows.value.find((row) => row.localId === parentLocalId)
  if (!parent) return []
  const startIndex = findRowIndex(parentLocalId)
  const endIndex = findSubtreeEndIndex(startIndex)
  return taskRows.value.slice(startIndex + 1, endIndex + 1)
}

function hasChildRows(row) {
  return taskRows.value.some((item) => item.localParentId === row.localId)
}

function isHiddenByCollapsedAncestor(row) {
  let currentParentId = row.localParentId
  while (currentParentId) {
    const parent = taskRows.value.find((item) => item.localId === currentParentId)
    if (!parent) {
      return false
    }
    if (parent.expanded === false) {
      return true
    }
    currentParentId = parent.localParentId
  }
  return false
}

function toggleRowExpanded(row) {
  if (!hasChildRows(row)) {
    return
  }
  row.expanded = row.expanded === false
}

function getRowDisplayNumber(row) {
  const index = taskRows.value.findIndex((item) => item.localId === row.localId)
  return index >= 0 ? index + 1 : ''
}

function syncSummaryRows() {
  if (isSyncingSummary.value) return
  isSyncingSummary.value = true

  try {
    for (let index = taskRows.value.length - 1; index >= 0; index -= 1) {
      const row = taskRows.value[index]
      if (!isSummaryRow(row)) continue

      const descendants = getDescendantRows(row.localId).filter((item) => hasRowContent(item) && !isSummaryRow(item))
      if (!descendants.length) continue

      const starts = descendants.map((item) => item.start).filter(Boolean).sort()
      const finishes = descendants.map((item) => item.finish).filter(Boolean).sort()

      if (starts.length) row.start = starts[0]
      if (finishes.length) row.finish = finishes[finishes.length - 1]
      if (row.start && row.finish) {
        row.duration = String(diffDays(new Date(`${row.start}T00:00:00`), new Date(`${row.finish}T00:00:00`)) + 1)
      }
    }
  } finally {
    isSyncingSummary.value = false
  }
}

function getNameCellStyle(row) {
  return {
    paddingLeft: `${(hasChildRows(row) ? 30 : 12) + (row.outlineLevel || 0) * 22}px`,
  }
}

function getNameInputClass(row) {
  return {
    'parent-task-input': hasChildRows(row) && !isSummaryRow(row),
    'summary-input': isSummaryRow(row),
    'milestone-input': isMilestoneRow(row),
  }
}

function toDateValue(value) {
  if (!value) return ''
  return typeof value === 'string' ? value.slice(0, 10) : ''
}

function formatDuration(value) {
  if (value === null || value === undefined || value === '') return ''
  const num = Number(value)
  if (Number.isNaN(num)) return ''
  return Number.isInteger(num) ? String(num) : String(num).replace(/\.?0+$/, '')
}

function parseDuration(value) {
  const raw = String(value || '').trim()
  if (!raw) return null
  const parsed = Number(raw)
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : null
}

function addDays(date, days) {
  const next = new Date(date)
  next.setDate(next.getDate() + days)
  return next
}

function diffDays(startDate, endDate) {
  return Math.floor((endDate.getTime() - startDate.getTime()) / (24 * 60 * 60 * 1000))
}

function formatDateToValue(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function getParentRow(row) {
  if (!row?.localParentId) return null
  return taskRows.value.find((item) => item.localId === row.localParentId) || null
}

function getProjectDateBounds() {
  if (!projectForm.startDate || !projectForm.endDate) return null
  return {
    min: projectForm.startDate,
    max: projectForm.endDate,
  }
}

function getParentDateBounds(row) {
  const parent = getParentRow(row)
  if (!parent || !parent.start || !parent.finish) return null
  return {
    min: parent.start,
    max: parent.finish,
  }
}

function getRowDateBounds(row) {
  const projectBounds = getProjectDateBounds()
  const parentBounds = getParentDateBounds(row)

  if (projectBounds && parentBounds) {
    return {
      min: projectBounds.min > parentBounds.min ? projectBounds.min : parentBounds.min,
      max: projectBounds.max < parentBounds.max ? projectBounds.max : parentBounds.max,
    }
  }

  return parentBounds || projectBounds
}

function clampRowToBounds(row) {
  const bounds = getRowDateBounds(row)
  if (!bounds) return

  if (row.start && row.start < bounds.min) {
    row.start = bounds.min
  }
  if (row.start && row.start > bounds.max) {
    row.start = bounds.max
  }
  if (row.finish && row.finish < bounds.min) {
    row.finish = bounds.min
  }
  if (row.finish && row.finish > bounds.max) {
    row.finish = bounds.max
  }
  if (row.start && row.finish && row.finish < row.start) {
    row.finish = row.start
  }
}

function isDateDisabledByParent(row, date) {
  const bounds = getRowDateBounds(row)
  if (!bounds) return false
  const value = formatDateToValue(date)
  return value < bounds.min || value > bounds.max
}

function syncDurationFromDates(row) {
  if (!row.start || !row.finish) return
  if (row.finish < row.start) row.finish = row.start
  row.duration = String(diffDays(new Date(`${row.start}T00:00:00`), new Date(`${row.finish}T00:00:00`)) + 1)
}

function syncFinishFromDuration(row) {
  const duration = parseDuration(row.duration)
  if (!row.start || duration === null) return
  row.finish = formatDateToValue(addDays(new Date(`${row.start}T00:00:00`), Math.max(duration, 1) - 1))
}

function handleStartInput(row) {
  if (!row.start) return
  if (parseDuration(row.duration) !== null) syncFinishFromDuration(row)
  else if (row.finish) syncDurationFromDates(row)
  clampRowToBounds(row)
  if (row.start && row.finish) syncDurationFromDates(row)
}

function handleFinishInput(row) {
  clampRowToBounds(row)
  if (row.start && row.finish) syncDurationFromDates(row)
}

function handleDurationInput(row) {
  if (parseDuration(row.duration) !== null && row.start) syncFinishFromDuration(row)
  clampRowToBounds(row)
  if (row.start && row.finish) syncDurationFromDates(row)
}

function buildTaskPayload(row, index) {
  const name = String(row.name || '').trim()
  if (!name) {
    throw new Error(`第 ${index + 1} 行任务名称不能为空`)
  }
  if (!row.start || !row.finish) {
    throw new Error(`第 ${index + 1} 行需要填写开始时间和完成时间`)
  }
  if (row.finish < row.start) {
    throw new Error(`第 ${index + 1} 行结束时间不能早于开始时间`)
  }

  const bounds = getRowDateBounds(row)
  if (bounds && (row.start < bounds.min || row.finish > bounds.max)) {
    throw new Error(`第 ${index + 1} 行任务时间必须在允许范围内`)
  }

  const plannedHours = parseDuration(row.duration)
  if (String(row.duration || '').trim() && plannedHours === null) {
    throw new Error(`第 ${index + 1} 行工期必须是数字`)
  }

  return {
    name,
    description: name,
    parentTaskId: null,
    assigneeId: row.assigneeId || null,
    plannedStartDate: `${row.start}T00:00:00`,
    plannedEndDate: `${row.finish}T00:00:00`,
    plannedHours,
      priority: 'MEDIUM',
      taskType: isMilestoneRow(row) ? 'MILESTONE_TASK' : mapModeToTaskType(row.mode),
      status: row.status || 'TODO',
      progress: Number(row.progress || 0),
      sortOrder: index + 1,
      remark: row.mode || '',
    }
  }

async function loadProjectDetailData() {
  const detail = await getProjectDetail(projectId.value)
  projectDetail.value = detail
  projectForm.name = detail.name || ''
  projectForm.description = detail.description || ''
  projectForm.startDate = detail.startDate || ''
  projectForm.endDate = detail.endDate || ''
  projectForm.lifeCycleModel = detail.lifeCycleModel || 'AGILE'
  projectForm.ownerId = detail.ownerId || ''
  projectForm.plannedBudget = detail.plannedBudget ?? ''
  normalizeProjectDates()
}

async function loadProjectMilestonesData() {
  const milestones = await getProjectMilestones(projectId.value)
  milestoneList.value = Array.isArray(milestones) ? milestones : []
}

async function loadProjectRisksData() {
  const pageResult = await getProjectRisks(projectId.value, { page: 1, pageSize: 500 })
  riskList.value = Array.isArray(pageResult?.list) ? pageResult.list : []
}

async function loadProjectRiskMatrixData() {
  const matrix = await getProjectRiskMatrix(projectId.value)
  riskMatrix.value = matrix || { levels: [], highCount: 0, criticalCount: 0 }
}

async function loadProjectTasksData() {
  const [pageResult, dependencies] = await Promise.all([
    getProjectTasks(projectId.value, { page: 1, pageSize: 500 }),
    getProjectTaskDependencies(projectId.value),
  ])
  const tasks = Array.isArray(pageResult?.list) ? pageResult.list : []
  loadedTasks.value = tasks
  projectTaskDependencies.value = Array.isArray(dependencies) ? dependencies : []
  replaceTaskRows(tasks.map((task, index) => createRowFromTask(task, index)))
}

async function loadProjectMembersData() {
  projectMembers.value = normalizeListResult(await getProjectMembers(projectId.value))
}

async function loadAvailableUsersData() {
  const result = await getUsers({ page: 1, pageSize: 200 })
  availableUsers.value = normalizeListResult(result)
}

function resetProjectMemberForm(member = null) {
  projectMemberForm.memberId = member?.id ? String(member.id) : ''
  projectMemberForm.userId = member?.userId ? String(member.userId) : ''
  projectMemberForm.projectRole = member?.projectRole || 'TEAM_MEMBER'
  projectMemberForm.memberStatus = member?.memberStatus || 'ACTIVE'
}

async function saveEditorContent() {
  try {
    saving.value = true
    const filledRows = taskRows.value.filter(hasRowContent)
    const persistedIds = new Set()
    const existingTaskIds = new Set(loadedTasks.value.map((task) => String(task.id)))

    // Phase 1: make sure every filled row exists as a task record first.
    for (const [index, row] of filledRows.entries()) {
      const payload = buildTaskPayload(row, index)
      payload.parentTaskId = null

      if (row.taskId && existingTaskIds.has(String(row.taskId))) {
        try {
          const detail = await updateProjectTask(projectId.value, row.taskId, payload)
          row.taskId = String(detail.id)
        } catch (error) {
          if (!String(error?.message || '').includes('task not found')) {
            throw error
          }
          const detail = await createProjectTask(projectId.value, payload)
          row.taskId = String(detail.id)
          existingTaskIds.add(String(detail.id))
        }
      } else {
        const detail = await createProjectTask(projectId.value, payload)
        row.taskId = String(detail.id)
        existingTaskIds.add(String(detail.id))
      }
    }

    // Phase 2: write parent-child relationships and final ordering after all ids exist.
    for (const [index, row] of filledRows.entries()) {
      const payload = buildTaskPayload(row, index)
      const parentRow = row.localParentId
        ? filledRows.find((item) => item.localId === row.localParentId) || null
        : null
      payload.parentTaskId = parentRow?.taskId ? parentRow.taskId : null

      const detail = await updateProjectTask(projectId.value, row.taskId, payload)
      row.taskId = String(detail.id)
      row.backendParentTaskId = payload.parentTaskId ? String(payload.parentTaskId) : null
      persistedIds.add(String(row.taskId))
    }

      for (const task of loadedTasks.value) {
        const id = String(task.id)
        if (!persistedIds.has(id)) {
          await deleteProjectTask(projectId.value, id)
        }
      }

      await persistDraftCostPlans(filledRows)

      await loadProjectTasksData()
      ElMessage.success('文件内容已保存')
  } catch (error) {
    ElMessage.error(error.message || '文件保存失败')
  } finally {
    saving.value = false
  }
}

function normalizeProjectDates() {
  if (projectForm.startDate && projectForm.startDate < todayString) {
    projectForm.startDate = todayString
  }
  if (projectForm.endDate && projectForm.startDate && projectForm.endDate <= projectForm.startDate) {
    projectForm.endDate = ''
  }
}

function disabledStartDate(date) {
  return date.getTime() < new Date(`${todayString}T00:00:00`).getTime()
}

function disabledEndDate(date) {
  const minDate = projectForm.startDate || todayString
  return date.getTime() <= new Date(`${minDate}T00:00:00`).getTime()
}

async function openProjectInfo() {
  loading.value = true
  try {
    await loadProjectDetailData()
    infoDialogVisible.value = true
  } finally {
    loading.value = false
  }
}

async function openProjectMembersDialog() {
  try {
    loading.value = true
    await loadProjectMembersData()
    if (canManageProject.value) {
      await loadAvailableUsersData()
    } else {
      availableUsers.value = []
    }
    resetProjectMemberForm()
    projectMembersDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '项目成员加载失败')
  } finally {
    loading.value = false
  }
}

async function saveProjectMemberAction() {
  if (!projectMemberForm.memberId && !projectMemberForm.userId) {
    ElMessage.warning('请选择要加入项目的成员')
    return
  }

  try {
    loading.value = true
    if (projectMemberForm.memberId) {
      await updateProjectMember(projectId.value, projectMemberForm.memberId, {
        projectRole: projectMemberForm.projectRole,
        memberStatus: projectMemberForm.memberStatus,
      })
    } else {
      await addProjectMember(projectId.value, {
        userId: projectMemberForm.userId,
        projectRole: projectMemberForm.projectRole,
      })
    }
    await Promise.all([
      loadProjectMembersData(),
      loadAvailableUsersData(),
    ])
    resetProjectMemberForm()
    ElMessage.success('项目成员已保存')
  } catch (error) {
    ElMessage.error(error.message || '项目成员保存失败')
  } finally {
    loading.value = false
  }
}

async function removeProjectMemberAction(item) {
  try {
    await ElMessageBox.confirm(
      `确认移除项目成员“${item.realName || item.username || item.userId}”吗？`,
      '移除项目成员',
      {
        type: 'warning',
        confirmButtonText: '确认移除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await removeProjectMember(projectId.value, item.id)
    await Promise.all([
      loadProjectMembersData(),
      loadAvailableUsersData(),
    ])
    resetProjectMemberForm()
    ElMessage.success('项目成员已移除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '移除项目成员失败')
    }
  } finally {
    loading.value = false
  }
}

async function saveProjectInfo() {
  try {
    normalizeProjectDates()
    loading.value = true
    await updateProject(projectId.value, {
      name: projectForm.name,
      description: projectForm.description,
      startDate: projectForm.startDate || null,
      endDate: projectForm.endDate || null,
      lifeCycleModel: projectForm.lifeCycleModel,
      ownerId: projectForm.ownerId || null,
      plannedBudget: projectForm.plannedBudget === '' ? null : Number(projectForm.plannedBudget),
    })
    await loadProjectDetailData()
    infoDialogVisible.value = false
    ElMessage.success('项目信息已更新')
  } catch (error) {
    ElMessage.error(error.message || '项目信息保存失败')
  } finally {
    loading.value = false
  }
}

async function openProjectCharterDialog() {
  try {
    loading.value = true
    const charter = await getProjectCharter(projectId.value)
    projectCharter.value = charter
    charterForm.objective = charter?.objective || ''
    charterForm.scopeSummary = charter?.scopeSummary || ''
    charterForm.sponsor = charter?.sponsor || ''
    charterForm.approver = charter?.approver || ''
    charterForm.stakeholders = charter?.stakeholders || ''
    charterForm.successCriteria = charter?.successCriteria || ''
    charterDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '项目章程加载失败')
  } finally {
    loading.value = false
  }
}

async function saveCharter() {
  try {
    loading.value = true
    projectCharter.value = await saveProjectCharter(projectId.value, { ...charterForm }, projectCharter.value?.id)
    charterDialogVisible.value = false
    ElMessage.success('项目章程已保存')
  } catch (error) {
    ElMessage.error(error.message || '项目章程保存失败')
  } finally {
    loading.value = false
  }
}

async function openDashboardDialog() {
  try {
    loading.value = true
    projectDashboard.value = await getProjectDashboard(projectId.value)
    dashboardDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '项目概览加载失败')
  } finally {
    loading.value = false
  }
}

async function openScheduleDialog() {
  try {
    loading.value = true
    const [calendar, critical, alerts] = await Promise.all([
      getProjectCalendar(projectId.value),
      getProjectCriticalPath(projectId.value),
      getProjectScheduleAlerts(projectId.value),
    ])
    calendarEvents.value = Array.isArray(calendar) ? calendar : []
    criticalPathTasks.value = Array.isArray(critical) ? critical : []
    scheduleAlerts.value = Array.isArray(alerts) ? alerts : []
    scheduleDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '日程数据加载失败')
  } finally {
    loading.value = false
  }
}

async function openReportDialog() {
  try {
    loading.value = true
    const [dashboard, timesheet, reports] = await Promise.all([
      getProjectDashboard(projectId.value),
      getProjectTimesheetReport(projectId.value),
      getProjectReports(projectId.value),
    ])
    projectDashboard.value = dashboard
    timesheetReport.value = timesheet
    reportList.value = Array.isArray(reports) ? reports : []
    reportDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '报表加载失败')
  } finally {
    loading.value = false
  }
}

function resetWbsForm() {
  wbsForm.name = ''
  wbsForm.code = ''
  wbsForm.parentId = null
  wbsForm.description = ''
}

function resetRequirementForm() {
  requirementForm.title = ''
  requirementForm.description = ''
  requirementForm.priority = 'MEDIUM'
}

function resetScopeBaselineForm() {
  scopeBaselineForm.name = ''
  scopeBaselineForm.description = ''
}

function normalizeListResult(result) {
  if (Array.isArray(result)) return result
  if (Array.isArray(result?.list)) return result.list
  if (Array.isArray(result?.items)) return result.items
  if (Array.isArray(result?.records)) return result.records
  return []
}

function getCostDraftBindingsStorageKey() {
  return `${COST_DRAFT_BINDINGS_STORAGE_PREFIX}${projectId.value}`
}

function loadCostDraftBindings() {
  try {
    const raw = localStorage.getItem(getCostDraftBindingsStorageKey())
    const parsed = raw ? JSON.parse(raw) : {}
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch {
    return {}
  }
}

function saveCostDraftBindings(bindings) {
  localStorage.setItem(getCostDraftBindingsStorageKey(), JSON.stringify(bindings || {}))
}

function rememberCostDraftBinding(planId, taskValue) {
  if (!planId) return
  const bindings = loadCostDraftBindings()
  if (taskValue && String(taskValue).startsWith('draft:')) {
    bindings[String(planId)] = String(taskValue)
  } else {
    delete bindings[String(planId)]
  }
  saveCostDraftBindings(bindings)
}

function clearCostDraftBinding(planId) {
  if (!planId) return
  const bindings = loadCostDraftBindings()
  delete bindings[String(planId)]
  saveCostDraftBindings(bindings)
}

function getRememberedCostDraftBinding(planId) {
  const bindings = loadCostDraftBindings()
  return planId ? bindings[String(planId)] || '' : ''
}

function attachDraftTaskBindings(plans) {
  return normalizeListResult(plans).map((item) => {
    const draftTaskValue = !item.taskId ? getRememberedCostDraftBinding(item.id) : ''
    const draftTaskOption = draftTaskValue
      ? costTaskOptions.value.find((option) => option.value === draftTaskValue) || null
      : null
    return {
      ...item,
      draftTaskValue,
      draftTaskLabel: draftTaskOption?.label || '',
    }
  })
}

function isDraftPlanId(value) {
  return String(value || '').startsWith('draft-plan:')
}

function getDraftTaskLabel(taskValue) {
  return costTaskOptions.value.find((option) => option.value === taskValue)?.label || ''
}

function mergeCostPlans(plans) {
  const backendPlans = attachDraftTaskBindings(plans).map((item) => ({
    ...item,
    isDraftPlan: false,
  }))

  const draftPlans = draftCostPlans.value.map((item) => ({
    ...item,
    id: `draft-plan:${item.localId}`,
    isDraftPlan: true,
    draftTaskLabel: getDraftTaskLabel(item.draftTaskValue) || item.draftTaskLabel || '',
  }))

  return [...draftPlans, ...backendPlans]
}

function upsertDraftCostPlan() {
  const taskValue = String(costPlanForm.taskId || '')
  const draftItem = {
    localId: isDraftPlanId(editingCostPlanId.value)
      ? String(editingCostPlanId.value).slice('draft-plan:'.length)
      : String(nextDraftCostPlanId++),
    name: costPlanForm.name.trim(),
    type: costPlanForm.type,
    draftTaskValue: taskValue,
    draftTaskLabel: getDraftTaskLabel(taskValue),
    phase: costPlanForm.phase.trim() || '',
    plannedAmount: Number(costPlanForm.plannedAmount),
    currency: costPlanForm.currency,
    remark: costPlanForm.remark.trim() || '',
  }

  const index = draftCostPlans.value.findIndex((item) => String(item.localId) === String(draftItem.localId))
  if (index >= 0) {
    draftCostPlans.value.splice(index, 1, draftItem)
  } else {
    draftCostPlans.value.unshift(draftItem)
  }
}

function removeDraftCostPlanById(id) {
  const localId = String(id || '').replace('draft-plan:', '')
  draftCostPlans.value = draftCostPlans.value.filter((item) => String(item.localId) !== localId)
}

async function persistDraftCostPlans(taskRowsToPersist) {
  if (!draftCostPlans.value.length) return

  const rows = Array.isArray(taskRowsToPersist) ? taskRowsToPersist : []
  const drafts = [...draftCostPlans.value]
  draftCostPlans.value = []

  for (const item of drafts) {
    const taskValue = String(item.draftTaskValue || '')
    const localId = taskValue.startsWith('draft:') ? taskValue.slice('draft:'.length) : ''
    const targetRow = localId ? rows.find((row) => String(row.localId) === localId) || null : null
    const resolvedTaskId = targetRow?.taskId ? String(targetRow.taskId) : null

    if (!resolvedTaskId) {
      continue
    }

    await createProjectCostPlan(projectId.value, {
      name: item.name,
      type: item.type,
      taskId: resolvedTaskId,
      phase: item.phase || null,
      plannedAmount: Number(item.plannedAmount || 0),
      currency: item.currency || 'CNY',
      remark: item.remark || null,
    })
  }
}

const costTaskOptions = computed(() => {
  const optionMap = new Map()

  taskRows.value
    .filter((row) => hasRowContent(row) && String(row.name || '').trim())
    .forEach((row) => {
      const value = row.taskId ? String(row.taskId) : `draft:${row.localId}`
      optionMap.set(value, {
        value,
        label: `${row.wbsCode || '-'} | ${row.name || '未命名任务'}${row.mode ? ` | ${row.mode}` : ''}`,
        isDraft: !row.taskId,
      })
    })

  loadedTasks.value.forEach((task) => {
    const value = String(task.id)
    if (!optionMap.has(value)) {
      optionMap.set(value, {
        value,
        label: `${task.name || '未命名任务'}${task.remark ? ` | ${task.remark}` : ''}`,
        isDraft: false,
      })
    }
  })

  return Array.from(optionMap.values())
})

const timesheetTaskOptions = computed(() => {
  const persistedTaskIds = new Set(loadedTasks.value.map((task) => String(task.id)))
  return costTaskOptions.value.filter((option) => !option.isDraft && persistedTaskIds.has(String(option.value)))
})

const currentAuthUser = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('pm_auth_user') || 'null')
  } catch {
    return null
  }
})

const currentUserId = computed(() => currentAuthUser.value?.id ? String(currentAuthUser.value.id) : '')

const activeProjectMembers = computed(() => projectMembers.value.filter((item) => item?.memberStatus === 'ACTIVE'))

const projectTeamMembers = computed(() => {
  const members = [...projectMembers.value]
  const ownerId = projectDetail.value?.ownerId ? String(projectDetail.value.ownerId) : ''
  if (!ownerId) return members
  const ownerExists = members.some((item) => String(item.userId || '') === ownerId)
  if (!ownerExists) {
    members.unshift({
      id: `owner-${ownerId}`,
      userId: ownerId,
      realName: projectDetail.value?.ownerName || '',
      username: '',
      projectRole: 'PROJECT_MANAGER',
      memberStatus: 'ACTIVE',
      joinedAt: projectDetail.value?.createdAt || null,
      isProjectOwner: true,
      isVirtualOwner: true,
    })
  } else {
    members.forEach((item) => {
      if (String(item.userId || '') === ownerId) {
        item.isProjectOwner = true
      }
    })
  }
  return members
})

const currentUserRoleCodes = computed(() => {
  const raw = currentAuthUser.value?.roleCodes || currentAuthUser.value?.roles
  return Array.isArray(raw) ? raw : []
})

const isSysAdmin = computed(() => currentUserRoleCodes.value.includes('SYS_ADMIN'))

const isSystemProjectManager = computed(() => currentUserRoleCodes.value.includes('PROJECT_MANAGER'))

const currentProjectMember = computed(() => {
  if (!currentUserId.value) return null
  return projectMembers.value.find((item) => String(item.userId || '') === currentUserId.value) || null
})

const isProjectOwner = computed(() => {
  if (!currentUserId.value || !projectDetail.value?.ownerId) return false
  return String(projectDetail.value.ownerId) === currentUserId.value
})

const currentProjectRoleCode = computed(() => {
  if (isProjectOwner.value || isSysAdmin.value || isSystemProjectManager.value) {
    return 'PROJECT_MANAGER'
  }
  return currentProjectMember.value?.projectRole || ''
})

const canManageProject = computed(() => currentProjectRoleCode.value === 'PROJECT_MANAGER')
const isClosedProject = computed(() => projectDetail.value?.status === 'CLOSED')
const canMutateClosedProject = computed(() => canManageProject.value && !isClosedProject.value)

const isReadOnlyProjectUser = computed(() => currentProjectRoleCode.value === 'READ_ONLY')

const canEditTaskPlan = computed(() => canMutateClosedProject.value)

const canEditTaskBasic = computed(() => canMutateClosedProject.value)

const canCommentOnTasks = computed(() => currentUserId.value && !isReadOnlyProjectUser.value && !isClosedProject.value)

const canUseTimesheet = computed(() => currentUserId.value && !isReadOnlyProjectUser.value && !isClosedProject.value)

const canUpdateTaskProgressByRole = computed(() => currentUserId.value && !isReadOnlyProjectUser.value && !isClosedProject.value)

const visibleTabs = computed(() => allTabs)

const effectiveTaskMineOnly = computed(() => taskMineOnly.value)

const visibleTaskDetailSections = computed(() => allTaskDetailSections)

const selectedTaskCanUpdateProgress = computed(() => {
  if (!selectedTaskRow.value || !canUpdateTaskProgressByRole.value) return false
  if (canManageProject.value) return true
  return String(selectedTaskRow.value.assigneeId || '') === currentUserId.value
})

const taskAssigneeOptions = computed(() => {
  const optionMap = new Map()
  if (projectDetail.value?.ownerId) {
    optionMap.set(String(projectDetail.value.ownerId), {
      value: String(projectDetail.value.ownerId),
      label: projectDetail.value.ownerName || `项目负责人${projectDetail.value.ownerId}`,
    })
  }
  activeProjectMembers.value.forEach((item) => {
    if (!item?.userId) return
    optionMap.set(String(item.userId), {
      value: String(item.userId),
      label: item.realName || item.username || `成员${item.userId}`,
    })
  })
  return Array.from(optionMap.values())
})

const availableUserOptions = computed(() => {
  const existingUserIds = new Set(projectMembers.value.map((item) => String(item.userId)))
  if (projectDetail.value?.ownerId) {
    existingUserIds.add(String(projectDetail.value.ownerId))
  }
  return availableUsers.value
    .filter((item) => item?.id && !existingUserIds.has(String(item.id)))
    .map((item) => ({
      value: String(item.id),
      label: item.realName || item.username || `用户${item.id}`,
    }))
})

const closureReadinessItems = computed(() => ([
  {
    label: '验收事项已完成',
    done: Boolean(closureCheck.value?.acceptanceCompleted),
  },
  {
    label: '归档项已完成',
    done: Boolean(closureCheck.value?.archiveCompleted),
  },
  {
    label: '所需报告已就绪',
    done: Boolean(closureCheck.value?.requiredReportsReady),
  },
  {
    label: `未关闭风险 ${closureCheck.value?.openRisks ?? 0} 项`,
    done: Number(closureCheck.value?.openRisks ?? 0) === 0,
  },
  {
    label: `待处理变更 ${closureCheck.value?.pendingChanges ?? 0} 项`,
    done: Number(closureCheck.value?.pendingChanges ?? 0) === 0,
  },
]))

const acceptanceOwnerOptions = computed(() => {
  const optionMap = new Map()
  try {
    const currentUser = JSON.parse(localStorage.getItem('pm_auth_user') || 'null')
    if (currentUser?.id) {
      optionMap.set(String(currentUser.id), {
        value: String(currentUser.id),
        label: currentUser.realName || currentUser.username || `用户${currentUser.id}`,
      })
    }
  } catch {
    // Ignore invalid local user cache.
  }
  if (projectDetail.value?.ownerId) {
    optionMap.set(String(projectDetail.value.ownerId), {
      value: String(projectDetail.value.ownerId),
      label: projectDetail.value.ownerName || `项目负责人${projectDetail.value.ownerId}`,
    })
  }
  return Array.from(optionMap.values())
})

const selectedChangeImpactHints = computed(() => {
  if (!selectedChangeRequest.value) return []
  const type = selectedChangeRequest.value.changeType
  if (type === 'SCOPE') {
    return [
      `当前任务数 ${projectDashboard.value?.taskTotal ?? 0} 项`,
      `当前范围基线 ${scopeBaselines.value.length} 条`,
    ]
  }
  if (type === 'SCHEDULE') {
    return [
      `已完成任务 ${projectDashboard.value?.taskCompleted ?? 0} 项`,
      `计划完成率 ${projectDashboard.value?.taskCompletionRate ?? 0}%`,
    ]
  }
  if (type === 'COST') {
    return [
      `计划成本 ${formatMoney(costPlans.value.reduce((sum, item) => sum + Number(item.plannedAmount || 0), 0))}`,
      `当前 AC ${formatMoney(evmMetrics.value?.ac)}`,
    ]
  }
  if (type === 'QUALITY') {
    return [
      `未关闭风险 ${closureCheck.value?.openRisks ?? 0} 项`,
      `验收事项 ${acceptanceItems.value.length} 条`,
    ]
  }
  return []
})

const closureStepOptions = [
  { key: 'acceptance', label: '1. 验收' },
  { key: 'report', label: '2. 报告' },
  { key: 'archive', label: '3. 归档' },
  { key: 'lesson', label: '4. 经验' },
  { key: 'review', label: '5. 关闭检查' },
]

function resetCostPlanForm() {
  editingCostPlanId.value = null
  costPlanForm.name = ''
  costPlanForm.type = 'LABOR'
  costPlanForm.taskId = ''
  costPlanForm.phase = ''
  costPlanForm.plannedAmount = ''
  costPlanForm.currency = 'CNY'
  costPlanForm.remark = ''
}

function resetCostActualForm() {
  costActualForm.taskId = ''
  costActualForm.sourceType = 'MANUAL'
  costActualForm.amount = ''
  costActualForm.currency = 'CNY'
  costActualForm.spendDate = todayString
  costActualForm.remark = ''
}

function resetTimesheetFilters() {
  timesheetFilterForm.taskId = ''
  timesheetFilterForm.startDate = ''
  timesheetFilterForm.endDate = ''
}

function resetTimesheetForm() {
  editingTimesheetId.value = null
  timesheetForm.taskId = timesheetTaskOptions.value[0]?.value || ''
  timesheetForm.workDate = todayString
  timesheetForm.hours = ''
  timesheetForm.description = ''
}

function populateTimesheetForm(item) {
  editingTimesheetId.value = item?.id ? String(item.id) : null
  timesheetForm.taskId = item?.taskId != null ? String(item.taskId) : ''
  timesheetForm.workDate = item?.workDate || todayString
  timesheetForm.hours = item?.hours != null ? String(item.hours) : ''
  timesheetForm.description = item?.description || ''
}

function resetArchiveForm() {
  archiveForm.itemName = ''
  archiveForm.archiveType = 'DOCUMENT'
  archiveForm.attachmentId = ''
  archiveForm.attachmentName = ''
  archiveForm.repositoryUrl = ''
  archiveForm.status = 'ARCHIVED'
}

function resetLessonForm() {
  editingLessonId.value = null
  lessonForm.category = '过程'
  lessonForm.title = ''
  lessonForm.content = ''
}

function populateLessonForm(item) {
  editingLessonId.value = item?.id ? String(item.id) : null
  lessonForm.category = item?.category || '过程'
  lessonForm.title = item?.title || ''
  lessonForm.content = item?.content || ''
}

function resetClosureSummaryForm() {
  closureSummaryForm.type = 'SUMMARY'
  closureSummaryForm.startDate = projectForm.startDate || ''
  closureSummaryForm.endDate = projectForm.endDate || ''
}

function resetAcceptanceForm() {
  editingAcceptanceId.value = null
  acceptanceForm.itemName = ''
  acceptanceForm.itemType = 'DOCUMENT'
  acceptanceForm.description = ''
  acceptanceForm.status = 'PENDING'
  acceptanceForm.ownerId = acceptanceOwnerOptions.value[0]?.value || ''
  acceptanceForm.attachmentId = ''
  acceptanceForm.attachmentName = ''
}

function populateAcceptanceForm(item) {
  editingAcceptanceId.value = item?.id ? String(item.id) : null
  acceptanceForm.itemName = item?.itemName || ''
  acceptanceForm.itemType = item?.itemType || 'DOCUMENT'
  acceptanceForm.description = item?.description || ''
  acceptanceForm.status = item?.status || 'PENDING'
  acceptanceForm.ownerId = item?.ownerId != null ? String(item.ownerId) : ''
  acceptanceForm.attachmentId = item?.attachmentId != null ? String(item.attachmentId) : ''
  acceptanceForm.attachmentName = item?.attachmentId ? `附件 ${item.attachmentId}` : ''
}

function resetChangeRequestForm() {
  changeRequestForm.title = ''
  changeRequestForm.type = 'SCOPE'
  changeRequestForm.priority = 'MEDIUM'
  changeRequestForm.impactAnalysis = ''
  changeRequestForm.reason = ''
}

function resetChangeApprovalForm() {
  changeApprovalForm.id = ''
  changeApprovalForm.decision = 'APPROVED'
  changeApprovalForm.comment = ''
}

function formatAcceptanceStatus(status) {
  return acceptanceStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatRole(role) {
  return projectMemberRoleOptions.find((item) => item.value === role)?.label || role || '-'
}

function formatMemberStatus(status) {
  return projectMemberStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function getProjectMemberBadges(item) {
  const badges = []
  if (String(item?.userId || '') === currentUserId.value) {
    badges.push('我')
  }
  if (item?.isProjectOwner || (projectDetail.value?.ownerId && String(item?.userId || '') === String(projectDetail.value.ownerId))) {
    badges.push('创建者')
  }
  return badges
}

function formatChangeStatus(status) {
  const map = {
    SUBMITTED: '已提交',
    UNDER_REVIEW: '评审中',
    APPROVED: '已批准',
    REJECTED: '已驳回',
    IMPLEMENTED: '已实施',
  }
  return map[status] || status || '-'
}

function formatChangeType(type) {
  return changeTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function resetCostBaselineForm() {
  costBaselineForm.name = `成本基线V${costBaselines.value.length + 1}`
}

function ensureCostTaskSelection(taskId) {
  if (!taskId) return null
  return String(taskId).startsWith('draft:') ? null : String(taskId)
}

function buildCostBaselineSnapshot() {
  return JSON.stringify({
    snapshotType: 'COST_ONLY',
    createdAt: new Date().toISOString(),
    plans: costPlans.value.map((item) => ({
      id: item.id || null,
      name: item.name || '',
      type: item.type || '',
      taskId: item.taskId || null,
      taskName: item.taskName || '',
      phase: item.phase || '',
      plannedAmount: item.plannedAmount ?? 0,
      currency: item.currency || 'CNY',
      remark: item.remark || '',
    })),
    totals: {
      planned: costPlans.value.reduce((sum, item) => sum + Number(item.plannedAmount || 0), 0),
      actual: costActuals.value.reduce((sum, item) => sum + Number(item.amount || 0), 0),
    },
  })
}

function parseCostBaselineSnapshot(item) {
  try {
    const parsed = JSON.parse(item?.snapshotJson || '{}')
    return {
      plans: Array.isArray(parsed?.plans) ? parsed.plans : [],
      totals: parsed?.totals || { planned: 0, actual: 0 },
    }
  } catch {
    return { plans: [], totals: { planned: 0, actual: 0 } }
  }
}

function formatMoney(value, currency = 'CNY') {
  const amount = Number(value || 0)
  if (!Number.isFinite(amount)) return '-'
  return `${currency} ${amount.toFixed(2)}`
}

function formatTaskStatus(status) {
  const statusMap = {
    TODO: '未开始',
    IN_PROGRESS: '进行中',
    DONE: '已完成',
    BLOCKED: '阻塞',
  }
  return statusMap[status] || status || '-'
}

function resetTaskProgressForm(detail = null) {
  taskProgressForm.status = detail?.status || selectedTaskRow.value?.status || 'TODO'
  taskProgressForm.progress = Number(detail?.progress ?? selectedTaskRow.value?.progress ?? 0)
  taskProgressForm.remark = ''
}

function resetTaskBasicForm(detail = null) {
  taskBasicForm.description = detail?.description || selectedTaskRow.value?.name || ''
  taskBasicForm.milestoneId = detail?.milestoneId != null ? String(detail.milestoneId) : ''
  taskBasicForm.assigneeId = detail?.assigneeId != null
    ? String(detail.assigneeId)
    : (selectedTaskRow.value?.assigneeId || '')
}

function resetTaskDependencyForm() {
  taskDependencyForm.predecessorTaskId = ''
  taskDependencyForm.dependencyType = 'FS'
}

function resetTaskCommentForm() {
  taskCommentForm.content = ''
  taskCommentForm.replyToId = ''
  taskCommentForm.replyToName = ''
}

function resetMilestoneForm() {
  editingMilestoneId.value = null
  milestoneForm.name = `里程碑${milestoneList.value.length + 1}`
  milestoneForm.description = ''
  milestoneForm.plannedDate = ''
  milestoneForm.status = 'PENDING'
}

function evaluateRiskLevel(probability, impact) {
  const score = Number(probability || 0) * Number(impact || 0)
  if (score >= 16) return 'CRITICAL'
  if (score >= 10) return 'HIGH'
  if (score >= 5) return 'MEDIUM'
  return 'LOW'
}

function resetRiskForm() {
  editingRiskId.value = null
  riskForm.name = ''
  riskForm.description = ''
  riskForm.probability = 3
  riskForm.impact = 3
  riskForm.level = evaluateRiskLevel(riskForm.probability, riskForm.impact)
  riskForm.responseStrategy = ''
  riskForm.taskId = ''
  riskForm.phaseName = ''
}

function populateRiskForm(item) {
  editingRiskId.value = item?.id ? String(item.id) : null
  riskForm.name = item?.name || ''
  riskForm.description = item?.description || ''
  riskForm.probability = Number(item?.probability || 1)
  riskForm.impact = Number(item?.impact || 1)
  riskForm.level = item?.level || evaluateRiskLevel(riskForm.probability, riskForm.impact)
  riskForm.responseStrategy = item?.responseStrategy || ''
  riskForm.taskId = item?.taskId != null ? String(item.taskId) : ''
  riskForm.phaseName = item?.phaseName || ''
}

function resetRiskStatusForm() {
  riskStatusForm.riskId = ''
  riskStatusForm.status = 'IDENTIFIED'
  riskStatusForm.comment = ''
}

function populateMilestoneForm(item) {
  editingMilestoneId.value = item?.id ? String(item.id) : null
  milestoneForm.name = item?.name || ''
  milestoneForm.description = item?.description || ''
  milestoneForm.plannedDate = item?.plannedDate ? String(item.plannedDate).slice(0, 19) : ''
  milestoneForm.status = item?.status || 'PENDING'
}

function normalizeDateTimeValue(value) {
  if (!value) return null
  const raw = String(value).trim()
  if (!raw) return null
  return raw.length === 16 ? `${raw}:00` : raw
}

function formatRiskLevel(level) {
  return riskLevelOptions.find((item) => item.value === level)?.label || level || '-'
}

function formatRiskStatus(status) {
  return riskStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function getTaskRisks(row) {
  if (!row?.taskId) return []
  return taskRiskMap.value.get(String(row.taskId)) || []
}

function hasTaskRisk(row) {
  return getTaskRisks(row).length > 0
}

function getHighestRiskLevel(row) {
  const levelWeight = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 }
  return getTaskRisks(row)
    .map((item) => item.level || 'LOW')
    .sort((a, b) => (levelWeight[b] || 0) - (levelWeight[a] || 0))[0] || ''
}

function getRiskBadgeClass(level) {
  return {
    'risk-badge': true,
    'risk-badge-low': level === 'LOW',
    'risk-badge-medium': level === 'MEDIUM',
    'risk-badge-high': level === 'HIGH',
    'risk-badge-critical': level === 'CRITICAL',
  }
}

function resetImportForm() {
  importForm.module = 'TASK'
  importForm.file = null
  importForm.fileName = ''
  importResult.value = null
}

const currentImportTemplate = computed(() => importTemplateConfig[importForm.module] || importTemplateConfig.TASK)

function buildAttachmentDownloadUrl(attachmentId) {
  if (!attachmentId) return ''
  const token = localStorage.getItem('pm_auth_token') || ''
  const url = new URL(`${API_BASE_URL}/attachments/${attachmentId}/download`)
  if (token) {
    url.searchParams.set('token', token)
  }
  return url.toString()
}

function openAttachmentDownload(attachmentId) {
  const url = buildAttachmentDownloadUrl(attachmentId)
  if (!url) return
  window.open(url, '_blank')
}

function downloadImportTemplate() {
  const token = localStorage.getItem('pm_auth_token') || ''
  const url = new URL(`${API_BASE_URL}/imports/excel/template`)
  url.searchParams.set('module', importForm.module)
  if (token) {
    url.searchParams.set('token', token)
  }
  window.open(url.toString(), '_blank')
}

function getMatrixCellItems(probability, impact) {
  return matrixCellMap.value.get(`${probability}-${impact}`) || []
}

function getMatrixCellClass(probability, impact) {
  const level = evaluateRiskLevel(probability, impact)
  return {
    'risk-matrix-cell': true,
    'risk-matrix-low': level === 'LOW',
    'risk-matrix-medium': level === 'MEDIUM',
    'risk-matrix-high': level === 'HIGH',
    'risk-matrix-critical': level === 'CRITICAL',
  }
}

const dependencyTaskOptions = computed(() => {
  const currentTaskId = selectedTaskRow.value?.taskId ? String(selectedTaskRow.value.taskId) : ''
  return loadedTasks.value
    .filter((task) => String(task.id) !== currentTaskId)
    .map((task) => ({
      value: String(task.id),
      label: task.name || `任务${task.id}`,
    }))
})

const milestoneOptions = computed(() =>
  milestoneList.value.map((item) => ({
    value: String(item.id),
    label: item.plannedDate
      ? `${item.name || `里程碑${item.id}`} | ${String(item.plannedDate).slice(0, 10)}`
      : (item.name || `里程碑${item.id}`),
  })),
)

const riskTaskOptions = computed(() => {
  const optionMap = new Map()

  taskRows.value
    .filter((row) => hasRowContent(row) && String(row.name || '').trim())
    .forEach((row) => {
      if (!row.taskId) return
      const value = String(row.taskId)
      optionMap.set(value, {
        value,
        label: `${row.wbsCode || '-'} | ${row.name || '未命名任务'}`,
        phaseName: `${row.wbsCode || '-'} | ${row.name || '未命名任务'}`,
      })
    })

  loadedTasks.value.forEach((task) => {
    const value = String(task.id)
    if (!optionMap.has(value)) {
      optionMap.set(value, {
        value,
        label: `${task.taskCode || '-'} | ${task.name || '未命名任务'}`,
        phaseName: `${task.taskCode || '-'} | ${task.name || '未命名任务'}`,
      })
    }
  })

  return Array.from(optionMap.values())
})

const taskCommentThreads = computed(() => {
  const comments = Array.isArray(taskDetail.value?.comments) ? taskDetail.value.comments : []
  const commentMap = new Map()
  const roots = []

  comments.forEach((item) => {
    commentMap.set(String(item.id), { ...item, replies: [] })
  })

  comments.forEach((item) => {
    const current = commentMap.get(String(item.id))
    if (!current) return
    if (item.replyToId != null) {
      const parent = commentMap.get(String(item.replyToId))
      if (parent) {
        parent.replies.push(current)
        return
      }
    }
    roots.push(current)
  })

  return roots
})

const taskRelatedRisks = computed(() => {
  const taskId = selectedTaskRow.value?.taskId ? String(selectedTaskRow.value.taskId) : ''
  if (!taskId) return []
  return riskList.value.filter((item) => String(item.taskId || '') === taskId)
})

const taskRiskMap = computed(() => {
  const map = new Map()
  riskList.value.forEach((item) => {
    if (!item?.taskId) return
    const key = String(item.taskId)
    if (!map.has(key)) {
      map.set(key, [])
    }
    map.get(key).push(item)
  })
  return map
})

const matrixCellMap = computed(() => {
  const map = new Map()
  const levels = Array.isArray(riskMatrix.value?.levels) ? riskMatrix.value.levels : []
  levels.forEach((item) => {
    const key = `${item.probability}-${item.impact}`
    if (!map.has(key)) {
      map.set(key, [])
    }
    map.get(key).push(item)
  })
  return map
})

async function openTaskInfoDialog(targetRow = null) {
  if (targetRow?.localId) {
    selectTaskRow(targetRow)
  }

  const row = targetRow?.localId ? targetRow : selectedTaskRow.value
  if (!row || isBlankPlaceholderRow(row)) {
    ElMessage.warning('请先选中一条有效任务')
    return
  }

  taskInfoDialogVisible.value = true
  taskDetail.value = null
  taskDetailSection.value = 'basic'
  resetTaskBasicForm()
  resetTaskProgressForm()
  await loadProjectMembersData()

  if (!row.taskId) {
    return
  }

  try {
    taskDetailLoading.value = true
    const detail = await getProjectTaskDetail(projectId.value, row.taskId)
    taskDetail.value = detail
    resetTaskBasicForm(detail)
    resetTaskProgressForm(detail)
    resetTaskDependencyForm()
    resetTaskCommentForm()
  } catch (error) {
    ElMessage.error(error.message || '任务详情加载失败')
  } finally {
    taskDetailLoading.value = false
  }
}

async function saveTaskComment() {
  if (!canCommentOnTasks.value) {
    ElMessage.warning('当前角色不能发表评论')
    return
  }
  const row = selectedTaskRow.value
  if (!row?.taskId) {
    ElMessage.warning('请先保存任务，再发表评论')
    return
  }
  if (!taskCommentForm.content.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    taskDetailLoading.value = true
    await createProjectTaskComment(projectId.value, row.taskId, {
      content: taskCommentForm.content.trim(),
      replyToId: taskCommentForm.replyToId || null,
    })
    taskDetail.value = await getProjectTaskDetail(projectId.value, row.taskId)
    resetTaskBasicForm(taskDetail.value)
    resetTaskCommentForm()
    ElMessage.success('评论已发布')
  } catch (error) {
    ElMessage.error(error.message || '评论发布失败')
  } finally {
    taskDetailLoading.value = false
  }
}

async function removeTaskComment(item) {
  if (!canManageProject.value) {
    ElMessage.warning('当前角色不能删除评论')
    return
  }
  const row = selectedTaskRow.value
  if (!row?.taskId) return

  try {
    await ElMessageBox.confirm(
      '确认删除这条评论吗？',
      '删除评论',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    taskDetailLoading.value = true
    await deleteProjectTaskComment(projectId.value, row.taskId, item.id)
    taskDetail.value = await getProjectTaskDetail(projectId.value, row.taskId)
    resetTaskBasicForm(taskDetail.value)
    ElMessage.success('评论已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除评论失败')
    }
  } finally {
    taskDetailLoading.value = false
  }
}

async function saveTaskDependency() {
  const row = selectedTaskRow.value
  if (!row || !row.taskId) {
    ElMessage.warning('请先保存任务，再设置依赖')
    return
  }
  if (!taskDependencyForm.predecessorTaskId) {
    ElMessage.warning('请选择前置任务')
    return
  }

  try {
    taskDetailLoading.value = true
    await createProjectTaskDependency(projectId.value, {
      predecessorTaskId: taskDependencyForm.predecessorTaskId,
      successorTaskId: row.taskId,
      dependencyType: taskDependencyForm.dependencyType,
    })
    taskDetail.value = await getProjectTaskDetail(projectId.value, row.taskId)
    resetTaskBasicForm(taskDetail.value)
    const dependencies = await getProjectTaskDependencies(projectId.value)
    projectTaskDependencies.value = Array.isArray(dependencies) ? dependencies : projectTaskDependencies.value
    resetTaskDependencyForm()
    ElMessage.success('任务依赖已新增')
  } catch (error) {
    ElMessage.error(error.message || '任务依赖保存失败')
  } finally {
    taskDetailLoading.value = false
  }
}

async function removeTaskDependency(item) {
  const row = selectedTaskRow.value
  if (!row?.taskId) return

  try {
    await ElMessageBox.confirm(
      `确认删除依赖“${item.predecessorTaskName} -> ${item.successorTaskName}”吗？`,
      '删除任务依赖',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    taskDetailLoading.value = true
    await deleteProjectTaskDependency(projectId.value, item.id)
    taskDetail.value = await getProjectTaskDetail(projectId.value, row.taskId)
    resetTaskBasicForm(taskDetail.value)
    const dependencies = await getProjectTaskDependencies(projectId.value)
    projectTaskDependencies.value = Array.isArray(dependencies) ? dependencies : projectTaskDependencies.value
    ElMessage.success('任务依赖已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除任务依赖失败')
    }
  } finally {
    taskDetailLoading.value = false
  }
}

async function saveTaskProgress() {
  if (!selectedTaskCanUpdateProgress.value) {
    ElMessage.warning('当前只能更新自己负责的任务进度')
    return
  }
  const row = selectedTaskRow.value
  if (!row || !row.taskId) {
    ElMessage.warning('请先保存任务，再更新进度')
    return
  }

  try {
    taskDetailLoading.value = true
    const detail = await updateProjectTaskProgress(projectId.value, row.taskId, {
      progress: Number(taskProgressForm.progress || 0),
      status: taskProgressForm.status,
      remark: taskProgressForm.remark.trim() || null,
    })
    taskDetail.value = detail
    resetTaskBasicForm(detail)
    row.progress = Number(detail.progress || 0)
    row.status = detail.status || row.status
    const loadedTask = loadedTasks.value.find((item) => String(item.id) === String(row.taskId))
    if (loadedTask) {
      loadedTask.progress = detail.progress
      loadedTask.status = detail.status
    }
    hasUnsavedChanges.value = false
    resetTaskProgressForm(detail)
    ElMessage.success('任务进度已更新')
  } catch (error) {
    ElMessage.error(error.message || '任务进度更新失败')
  } finally {
    taskDetailLoading.value = false
  }
}

function startReplyComment(item) {
  taskCommentForm.replyToId = item?.id ? String(item.id) : ''
  taskCommentForm.replyToName = item?.userName || '该评论'
}

async function saveTaskBasicInfo() {
  if (!canEditTaskBasic.value) {
    ElMessage.warning('当前角色不能修改任务基础信息')
    return
  }
  const row = selectedTaskRow.value
  if (!row || !row.taskId) {
    ElMessage.warning('请先保存任务，再维护基础信息')
    return
  }

  try {
    taskDetailLoading.value = true
    const detail = await updateProjectTask(projectId.value, row.taskId, {
      parentTaskId: row.backendParentTaskId || null,
      wbsId: taskDetail.value?.wbsId || null,
      milestoneId: taskBasicForm.milestoneId || null,
      name: row.name || '未命名任务',
      description: taskBasicForm.description.trim() || null,
      assigneeId: taskBasicForm.assigneeId || null,
      plannedStartDate: row.start ? `${row.start}T00:00:00` : null,
      plannedEndDate: row.finish ? `${row.finish}T00:00:00` : null,
      plannedHours: Number(row.duration || 0) > 0 ? Number(row.duration || 0) * 8 : null,
      priority: taskDetail.value?.priority || 'MEDIUM',
      taskType: isMilestoneRow(row) ? 'MILESTONE_TASK' : mapModeToTaskType(row.mode),
      status: taskProgressForm.status || row.status || 'TODO',
      progress: Number(taskProgressForm.progress ?? row.progress ?? 0),
      sortOrder: Number(row.sortOrder || 0) || null,
      remark: row.mode || '',
    })
    taskDetail.value = detail
    resetTaskBasicForm(detail)
    row.assigneeId = detail.assigneeId != null ? String(detail.assigneeId) : null
    row.assigneeName = detail.assigneeName || ''
    ElMessage.success('基础信息已保存')
  } catch (error) {
    ElMessage.error(error.message || '基础信息保存失败')
  } finally {
    taskDetailLoading.value = false
  }
}

async function openMilestoneDialog() {
  try {
    milestoneLoading.value = true
    await loadProjectMilestonesData()
    resetMilestoneForm()
    milestoneDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '里程碑加载失败')
  } finally {
    milestoneLoading.value = false
  }
}

async function openRiskDialog() {
  try {
    riskLoading.value = true
    await loadProjectRisksData()
    resetRiskForm()
    resetRiskStatusForm()
    riskDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '风险列表加载失败')
  } finally {
    riskLoading.value = false
  }
}

async function openRiskMatrixDialog() {
  try {
    riskLoading.value = true
    await loadProjectRiskMatrixData()
    riskMatrixDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '风险矩阵加载失败')
  } finally {
    riskLoading.value = false
  }
}

async function saveRisk() {
  if (!riskForm.name.trim()) {
    ElMessage.warning('请输入风险名称')
    return
  }

  try {
    riskLoading.value = true
    const isEditing = Boolean(editingRiskId.value)
    const payload = {
      name: riskForm.name.trim(),
      description: riskForm.description.trim() || null,
      probability: Number(riskForm.probability || 1),
      impact: Number(riskForm.impact || 1),
      level: riskForm.level,
      responseStrategy: riskForm.responseStrategy.trim() || null,
      taskId: riskForm.taskId || null,
      phaseName: riskForm.phaseName || null,
      ownerId: null,
    }

    if (isEditing) {
      await updateProjectRisk(projectId.value, editingRiskId.value, payload)
    } else {
      await createProjectRisk(projectId.value, payload)
    }

    await Promise.all([
      loadProjectRisksData(),
      loadProjectRiskMatrixData(),
    ])
    resetRiskForm()
    ElMessage.success(isEditing ? '风险已更新' : '风险已新增')
  } catch (error) {
    ElMessage.error(error.message || '风险保存失败')
  } finally {
    riskLoading.value = false
  }
}

function startRiskStatusEdit(item) {
  riskStatusForm.riskId = item?.id ? String(item.id) : ''
  riskStatusForm.status = item?.status || 'IDENTIFIED'
  riskStatusForm.comment = ''
}

async function saveRiskStatus() {
  if (!riskStatusForm.riskId) {
    ElMessage.warning('请先在列表中选择一条风险进行状态更新')
    return
  }

  try {
    riskLoading.value = true
    await updateProjectRiskStatus(projectId.value, riskStatusForm.riskId, {
      status: riskStatusForm.status,
      comment: riskStatusForm.comment.trim() || null,
    })
    await Promise.all([
      loadProjectRisksData(),
      loadProjectRiskMatrixData(),
    ])
    resetRiskStatusForm()
    ElMessage.success('风险状态已更新')
  } catch (error) {
    ElMessage.error(error.message || '风险状态更新失败')
  } finally {
    riskLoading.value = false
  }
}

async function openExportDialog() {
  exportTasks.value = []
  exportDialogVisible.value = true
}

async function submitExportTask() {
  try {
    opsLoading.value = true
    const result = await createExportTask({
      projectId: projectId.value,
      module: exportForm.module,
      format: exportForm.format,
      filters: {
        projectName: projectDetail.value?.name || '',
      },
    })
    exportTasks.value.unshift(result)
    ElMessage.success('导出任务已创建')
  } catch (error) {
    ElMessage.error(error.message || '导出任务创建失败')
  } finally {
    opsLoading.value = false
  }
}

function openImportDialog() {
  resetImportForm()
  importDialogVisible.value = true
}

function handleImportFileChange(uploadFile) {
  const rawFile = uploadFile?.raw || uploadFile || null
  importForm.file = rawFile
  importForm.fileName = rawFile?.name || ''
}

async function submitImportExcel() {
  if (!importForm.file) {
    ElMessage.warning('请先选择 Excel 文件')
    return
  }

  try {
    opsLoading.value = true
    importResult.value = null
    const result = await importExcelFile(importForm.file, importForm.module, projectId.value)
    importResult.value = result || { successCount: 0, failCount: 0, errors: [] }
    if (importForm.module === 'TASK') {
      await loadProjectTasksData()
    } else if (importForm.module === 'RISK') {
      await Promise.all([
        loadProjectRisksData(),
        loadProjectRiskMatrixData(),
      ])
    }
    ElMessage.success(`导入完成，成功 ${result?.successCount ?? 0} 条，失败 ${result?.failCount ?? 0} 条`)
  } catch (error) {
    ElMessage.error(error.message || 'Excel 导入失败')
  } finally {
    opsLoading.value = false
  }
}

async function openGlobalSearchDialog() {
  globalSearchResults.value = []
  globalSearchDialogVisible.value = true
}

async function runGlobalSearch() {
  try {
    opsLoading.value = true
    const pageResult = await searchGlobal({
      keyword: searchForm.keyword.trim(),
      type: searchForm.type,
      projectId: projectId.value,
      page: 1,
      pageSize: 100,
    })
    globalSearchResults.value = Array.isArray(pageResult?.list) ? pageResult.list : []
  } catch (error) {
    ElMessage.error(error.message || '全局搜索失败')
  } finally {
    opsLoading.value = false
  }
}

async function openAuditLogDialog() {
  auditLogDialogVisible.value = true
  await loadAuditLogs()
}

async function loadAuditLogs() {
  try {
    opsLoading.value = true
    const pageResult = await getAuditLogs({
      projectId: projectId.value,
      module: auditForm.module,
      startTime: auditForm.startTime || null,
      endTime: auditForm.endTime || null,
      page: 1,
      pageSize: 200,
    })
    auditLogs.value = Array.isArray(pageResult?.list) ? pageResult.list : []
  } catch (error) {
    ElMessage.error(error.message || '审计日志加载失败')
  } finally {
    opsLoading.value = false
  }
}

async function saveMilestone() {
  if (!milestoneForm.name.trim()) {
    ElMessage.warning('请输入里程碑名称')
    return
  }

  try {
    milestoneLoading.value = true
    const isEditing = Boolean(editingMilestoneId.value)
    const payload = {
      name: milestoneForm.name.trim(),
      description: milestoneForm.description.trim() || null,
      plannedDate: normalizeDateTimeValue(milestoneForm.plannedDate),
    }

    if (isEditing) {
      await updateProjectMilestone(projectId.value, editingMilestoneId.value, {
        ...payload,
        actualDate: null,
        status: milestoneForm.status || 'PENDING',
      })
    } else {
      await createProjectMilestone(projectId.value, payload)
    }

    await loadProjectMilestonesData()
    resetMilestoneForm()
    ElMessage.success(isEditing ? '里程碑已更新' : '里程碑已新增')
  } catch (error) {
    ElMessage.error(error.message || '里程碑保存失败')
  } finally {
    milestoneLoading.value = false
  }
}

async function removeMilestone(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除里程碑“${item.name || item.id}”吗？`,
      '删除里程碑',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    milestoneLoading.value = true
    await deleteProjectMilestone(projectId.value, item.id)
    await loadProjectMilestonesData()
    if (editingMilestoneId.value && String(editingMilestoneId.value) === String(item.id)) {
      resetMilestoneForm()
    }
    ElMessage.success('里程碑已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除里程碑失败')
    }
  } finally {
    milestoneLoading.value = false
  }
}

const totalPlannedCost = computed(() => costPlans.value.reduce((sum, item) => sum + Number(item.plannedAmount || 0), 0))
const totalActualCost = computed(() => costActuals.value.reduce((sum, item) => sum + Number(item.amount || 0), 0))

async function openWbsDialog() {
  wbsConfigSnapshot = cloneWbsConfig()
  wbsDialogVisible.value = true
}

function cancelWbsDialog() {
  applyWbsConfig(wbsConfigSnapshot || createDefaultWbsConfig())
  refreshWbsCodes()
  wbsDialogVisible.value = false
}

function saveWbsConfig() {
  saveProjectWbsConfig()
  refreshWbsCodes()
  wbsDialogVisible.value = false
  ElMessage.success('WBS 编码规则已保存')
}

function resequenceWbsCodes() {
  refreshOutlineLevels()
  ElMessage.success('WBS 编码已重排')
}

async function openRequirementDialog() {
  try {
    loading.value = true
    requirements.value = normalizeListResult(await getProjectRequirements(projectId.value, { page: 1, pageSize: 100 }))
    resetRequirementForm()
    requirementDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || 'Requirements load failed')
  } finally {
    loading.value = false
  }
}

async function saveRequirement() {
  if (!requirementForm.title.trim()) {
    ElMessage.warning('请输入需求标题')
    return
  }

  try {
    loading.value = true
    await createProjectRequirement(projectId.value, {
      title: requirementForm.title.trim(),
      description: requirementForm.description.trim() || null,
      priority: requirementForm.priority,
    })
    requirements.value = normalizeListResult(await getProjectRequirements(projectId.value, { page: 1, pageSize: 100 }))
    resetRequirementForm()
    ElMessage.success('Requirement created')
  } catch (error) {
    ElMessage.error(error.message || 'Requirement create failed')
  } finally {
    loading.value = false
  }
}

async function openScopeBaselineDialog() {
  try {
    loading.value = true
    scopeBaselines.value = normalizeListResult(await getProjectScopeBaselines(projectId.value))
    activeScopeBaselineId.value = null
    resetScopeBaselineForm()
    scopeBaselineDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || 'Scope baseline load failed')
  } finally {
    loading.value = false
  }
}

async function saveScopeBaseline() {
  if (!scopeBaselineForm.name.trim()) {
    ElMessage.warning('请输入范围基线名称')
    return
  }

  try {
    loading.value = true
    await createProjectScopeBaseline(projectId.value, {
      baselineName: scopeBaselineForm.name.trim(),
      description: scopeBaselineForm.description.trim() || null,
      snapshotJson: buildScopeBaselineSnapshot(),
    })
    scopeBaselines.value = normalizeListResult(await getProjectScopeBaselines(projectId.value))
    activeScopeBaselineId.value = scopeBaselines.value[0]?.id || null
    resetScopeBaselineForm()
    ElMessage.success('范围基线已保存，当前 WBS 快照已锁定')
  } catch (error) {
    ElMessage.error(error.message || 'Scope baseline create failed')
  } finally {
    loading.value = false
  }
}

async function generateWeeklyReport() {
  try {
    loading.value = true
    await generateProjectReport(projectId.value, {
      type: reportGenerateForm.type,
      startDate: reportGenerateForm.startDate || null,
      endDate: reportGenerateForm.endDate || null,
    })
    await openReportDialog()
    ElMessage.success('报表已生成')
  } catch (error) {
    ElMessage.error(error.message || '生成报表失败')
  } finally {
    loading.value = false
  }
}

function getNextProjectStatus(currentStatus) {
  const flow = {
    PLANNING: 'IN_PROGRESS',
    IN_PROGRESS: 'MONITORING',
    MONITORING: 'CLOSED',
  }
  return flow[currentStatus] || ''
}

function formatProjectStatus(status) {
  const statusMap = {
    PLANNING: '规划中',
    IN_PROGRESS: '进行中',
    MONITORING: '监控中',
    CLOSED: '已关闭',
  }
  return statusMap[status] || status || '未知状态'
}

function openProjectStatusDialog() {
  selectedProjectStatus.value = projectDetail.value?.status || 'PLANNING'
  projectStatusDialogVisible.value = true
}

async function saveProjectStatus() {
  const currentStatus = projectDetail.value?.status
  const nextStatus = selectedProjectStatus.value

  if (!nextStatus) {
    ElMessage.warning('请选择项目状态')
    return
  }

  if (currentStatus === nextStatus) {
    projectStatusDialogVisible.value = false
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认将项目状态从“${formatProjectStatus(currentStatus)}”变更为“${formatProjectStatus(nextStatus)}”吗？`,
      '项目状态流转',
      {
        type: 'warning',
        confirmButtonText: '确认变更',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await changeProjectStatus(projectId.value, { status: nextStatus })
    await loadProjectDetailData()
    projectStatusDialogVisible.value = false
    ElMessage.success('项目状态已更新')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '项目状态流转失败')
    }
  } finally {
    loading.value = false
  }
}

async function loadTimesheetRecords() {
  const result = await getProjectTimesheets(projectId.value, {
    taskId: ensureCostTaskSelection(timesheetFilterForm.taskId),
    startDate: timesheetFilterForm.startDate || null,
    endDate: timesheetFilterForm.endDate || null,
    page: 1,
    pageSize: 200,
  })
  timesheetRecords.value = normalizeListResult(result)
  timesheetReport.value = await getProjectTimesheetReport(projectId.value)
}

async function openTimesheetDialog() {
  if (!canUseTimesheet.value) {
    ElMessage.warning('当前角色不能登记工时')
    return
  }
  try {
    loading.value = true
    resetTimesheetFilters()
    loadedTasks.value = normalizeListResult(await getProjectTasks(projectId.value))
    await loadTimesheetRecords()
    resetTimesheetForm()
    timesheetDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '工时数据加载失败')
  } finally {
    loading.value = false
  }
}

async function saveTimesheet() {
  if (!canUseTimesheet.value) {
    ElMessage.warning('当前角色不能登记工时')
    return
  }
  const taskId = ensureCostTaskSelection(timesheetForm.taskId)
  if (!taskId) {
    ElMessage.warning('工时只能关联已保存的任务')
    return
  }
  if (!timesheetTaskOptions.value.some((option) => String(option.value) === String(taskId))) {
    ElMessage.warning('所选任务不存在或尚未保存，请重新选择')
    return
  }
  if (!timesheetForm.workDate) {
    ElMessage.warning('请选择工时日期')
    return
  }

  try {
    loading.value = true
    const isEditing = Boolean(editingTimesheetId.value)
    const payload = {
      taskId,
      workDate: timesheetForm.workDate,
      hours: Number(timesheetForm.hours || 0),
      description: timesheetForm.description || null,
    }
    if (isEditing) {
      await updateProjectTimesheet(projectId.value, editingTimesheetId.value, payload)
    } else {
      await createProjectTimesheet(projectId.value, payload)
    }
    await loadTimesheetRecords()
    resetTimesheetForm()
    ElMessage.success(isEditing ? '工时已更新' : '工时已新增')
  } catch (error) {
    ElMessage.error(error.message || '工时保存失败')
  } finally {
    loading.value = false
  }
}

async function removeTimesheet(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除 ${item.workDate || ''} 的这条工时记录吗？`,
      '删除工时',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectTimesheet(projectId.value, item.id)
    await loadTimesheetRecords()
    if (editingTimesheetId.value === String(item.id)) {
      resetTimesheetForm()
    }
    ElMessage.success('工时已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除工时失败')
    }
  } finally {
    loading.value = false
  }
}

async function loadProjectClosureData() {
  const [check, archives, lessons, reports] = await Promise.all([
    getProjectClosureCheck(projectId.value),
    getProjectArchives(projectId.value),
    getProjectLessonsLearned(projectId.value),
    getProjectReports(projectId.value),
  ])
  closureCheck.value = check
  closureArchives.value = normalizeListResult(archives)
  closureLessons.value = normalizeListResult(lessons)
  reportList.value = normalizeListResult(reports)
}

async function openProjectClosureDialog() {
  try {
    loading.value = true
    await Promise.all([
      loadProjectClosureData(),
      loadAcceptanceItems(),
    ])
    closureStep.value = 'acceptance'
    resetArchiveForm()
    resetLessonForm()
    resetClosureSummaryForm()
    projectClosureDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '项目收尾数据加载失败')
  } finally {
    loading.value = false
  }
}

async function saveArchive() {
  if (!archiveForm.itemName.trim()) {
    ElMessage.warning('请填写归档项名称')
    return
  }
  try {
    loading.value = true
    await createProjectArchive(projectId.value, {
      itemName: archiveForm.itemName.trim(),
      archiveType: archiveForm.archiveType,
      attachmentId: archiveForm.attachmentId ? Number(archiveForm.attachmentId) : null,
      repositoryUrl: archiveForm.repositoryUrl || null,
      status: archiveForm.status || 'ARCHIVED',
    })
    await loadProjectClosureData()
    resetArchiveForm()
    ElMessage.success('归档项已新增')
  } catch (error) {
    ElMessage.error(error.message || '归档项保存失败')
  } finally {
    loading.value = false
  }
}

async function handleArchiveAttachmentChange(uploadFile) {
  const rawFile = uploadFile?.raw || uploadFile || null
  if (!rawFile) return

  try {
    loading.value = true
    if (archiveForm.attachmentId) {
      await deleteProjectAttachment(archiveForm.attachmentId)
    }
    const attachment = await uploadProjectAttachment(projectId.value, rawFile)
    archiveForm.attachmentId = attachment?.id ? String(attachment.id) : ''
    archiveForm.attachmentName = attachment?.fileName || rawFile.name || ''
    ElMessage.success('附件已上传')
  } catch (error) {
    ElMessage.error(error.message || '附件上传失败')
  } finally {
    loading.value = false
  }
}

async function clearArchiveAttachment() {
  if (!archiveForm.attachmentId) {
    archiveForm.attachmentName = ''
    return
  }

  try {
    loading.value = true
    await deleteProjectAttachment(archiveForm.attachmentId)
    archiveForm.attachmentId = ''
    archiveForm.attachmentName = ''
    ElMessage.success('附件已移除')
  } catch (error) {
    ElMessage.error(error.message || '附件移除失败')
  } finally {
    loading.value = false
  }
}

async function handleAcceptanceAttachmentChange(uploadFile) {
  const rawFile = uploadFile?.raw || uploadFile || null
  if (!rawFile) return

  try {
    loading.value = true
    if (acceptanceForm.attachmentId) {
      await deleteProjectAttachment(acceptanceForm.attachmentId)
    }
    const attachment = await uploadProjectAttachment(projectId.value, rawFile)
    acceptanceForm.attachmentId = attachment?.id ? String(attachment.id) : ''
    acceptanceForm.attachmentName = attachment?.fileName || rawFile.name || ''
    ElMessage.success('验收证明材料已上传')
  } catch (error) {
    ElMessage.error(error.message || '验收证明材料上传失败')
  } finally {
    loading.value = false
  }
}

async function clearAcceptanceAttachment() {
  if (!acceptanceForm.attachmentId) {
    acceptanceForm.attachmentName = ''
    return
  }

  try {
    loading.value = true
    await deleteProjectAttachment(acceptanceForm.attachmentId)
    acceptanceForm.attachmentId = ''
    acceptanceForm.attachmentName = ''
    ElMessage.success('验收证明材料已移除')
  } catch (error) {
    ElMessage.error(error.message || '验收证明材料移除失败')
  } finally {
    loading.value = false
  }
}

async function removeArchive(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除归档项“${item.itemName || item.id}”吗？`,
      '删除归档项',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectArchive(projectId.value, item.id)
    await loadProjectClosureData()
    ElMessage.success('归档项已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除归档项失败')
    }
  } finally {
    loading.value = false
  }
}

async function saveLesson() {
  if (!lessonForm.title.trim() || !lessonForm.content.trim()) {
    ElMessage.warning('请填写经验标题和内容')
    return
  }
  try {
    loading.value = true
    const isEditing = Boolean(editingLessonId.value)
    const payload = {
      category: lessonForm.category.trim(),
      title: lessonForm.title.trim(),
      content: lessonForm.content.trim(),
    }
    if (isEditing) {
      await updateProjectLessonLearned(projectId.value, editingLessonId.value, payload)
    } else {
      await createProjectLessonLearned(projectId.value, payload)
    }
    await loadProjectClosureData()
    resetLessonForm()
    ElMessage.success(isEditing ? '经验教训已更新' : '经验教训已新增')
  } catch (error) {
    ElMessage.error(error.message || '经验教训保存失败')
  } finally {
    loading.value = false
  }
}

async function removeLesson(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除经验教训“${item.title || item.id}”吗？`,
      '删除经验教训',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectLessonLearned(projectId.value, item.id)
    await loadProjectClosureData()
    if (editingLessonId.value === String(item.id)) {
      resetLessonForm()
    }
    ElMessage.success('经验教训已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除经验教训失败')
    }
  } finally {
    loading.value = false
  }
}

async function generateSummaryReportAction() {
  try {
    loading.value = true
    await generateProjectSummaryReport(projectId.value, {
      type: closureSummaryForm.type,
      startDate: closureSummaryForm.startDate || null,
      endDate: closureSummaryForm.endDate || null,
    })
    await loadProjectClosureData()
    ElMessage.success('项目总结报告已生成')
  } catch (error) {
    ElMessage.error(error.message || '项目总结报告生成失败')
  } finally {
    loading.value = false
  }
}

async function loadAcceptanceItems() {
  acceptanceItems.value = normalizeListResult(await getAcceptanceItems(projectId.value))
}

async function openAcceptanceDialog() {
  try {
    loading.value = true
    await loadAcceptanceItems()
    resetAcceptanceForm()
    acceptanceDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '验收事项加载失败')
  } finally {
    loading.value = false
  }
}

async function saveAcceptanceItemAction() {
  if (!acceptanceForm.itemName.trim()) {
    ElMessage.warning('请填写验收事项名称')
    return
  }
  try {
    loading.value = true
    const isEditing = Boolean(editingAcceptanceId.value)
    const payload = {
      itemName: acceptanceForm.itemName.trim(),
      itemType: acceptanceForm.itemType || 'DOCUMENT',
      description: acceptanceForm.description || null,
      status: acceptanceForm.status || 'PENDING',
      ownerId: acceptanceForm.ownerId || null,
      attachmentId: acceptanceForm.attachmentId || null,
    }
    if (isEditing) {
      await updateAcceptanceItem(projectId.value, editingAcceptanceId.value, payload)
    } else {
      await createAcceptanceItem(projectId.value, payload)
    }
    await loadAcceptanceItems()
    resetAcceptanceForm()
    ElMessage.success(isEditing ? '验收事项已更新' : '验收事项已新增')
  } catch (error) {
    ElMessage.error(error.message || '验收事项保存失败')
  } finally {
    loading.value = false
  }
}

async function removeAcceptanceItemAction(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除验收事项“${item.itemName || item.id}”吗？`,
      '删除验收事项',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteAcceptanceItem(projectId.value, item.id)
    await loadAcceptanceItems()
    if (editingAcceptanceId.value === String(item.id)) {
      resetAcceptanceForm()
    }
    ElMessage.success('验收事项已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除验收事项失败')
    }
  } finally {
    loading.value = false
  }
}

async function removeRisk(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除风险“${item.name || item.id}”吗？`,
      '删除风险',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    riskLoading.value = true
    await deleteProjectRisk(projectId.value, item.id)
    await Promise.all([
      loadProjectRisksData(),
      loadProjectRiskMatrixData(),
    ])
    if (editingRiskId.value === String(item.id)) {
      resetRiskForm()
    }
    if (riskStatusForm.riskId === String(item.id)) {
      resetRiskStatusForm()
    }
    ElMessage.success('风险已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除风险失败')
    }
  } finally {
    riskLoading.value = false
  }
}

async function loadChangeRequests() {
  const pageResult = await getProjectChangeRequests(projectId.value, {
    status: changeRequestFilterForm.status || null,
    priority: changeRequestFilterForm.priority || null,
    page: 1,
    pageSize: 200,
  })
  changeRequests.value = normalizeListResult(pageResult)
}

async function loadChangeRequestLogs(id) {
  if (!id) {
    changeRequestLogs.value = []
    selectedChangeRequest.value = null
    return
  }
  const [detail, logs] = await Promise.all([
    getProjectChangeRequestDetail(projectId.value, id),
    getProjectChangeRequestLogs(projectId.value, id),
  ])
  selectedChangeRequest.value = detail || null
  changeRequestLogs.value = normalizeListResult(logs)
}

async function openChangeRequestDialog() {
  try {
    loading.value = true
    await Promise.all([
      loadChangeRequests(),
      getProjectDashboard(projectId.value).then((data) => { projectDashboard.value = data }),
      getProjectClosureCheck(projectId.value).then((data) => { closureCheck.value = data }),
      getProjectEvm(projectId.value).then((data) => { evmMetrics.value = data }),
    ])
    resetChangeRequestForm()
    resetChangeApprovalForm()
    selectedChangeRequest.value = null
    changeRequestLogs.value = []
    changeRequestDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '变更数据加载失败')
  } finally {
    loading.value = false
  }
}

async function saveChangeRequestAction() {
  if (!changeRequestForm.title.trim() || !changeRequestForm.reason.trim()) {
    ElMessage.warning('请填写变更标题和变更原因')
    return
  }
  try {
    loading.value = true
    const created = await createProjectChangeRequest(projectId.value, {
      title: changeRequestForm.title.trim(),
      type: changeRequestForm.type,
      priority: changeRequestForm.priority,
      impactAnalysis: changeRequestForm.impactAnalysis || null,
      reason: changeRequestForm.reason.trim(),
    })
    await loadChangeRequests()
    resetChangeRequestForm()
    changeApprovalForm.id = created?.id ? String(created.id) : ''
    if (created?.id) {
      await loadChangeRequestLogs(created.id)
    }
    ElMessage.success('变更申请已提交')
  } catch (error) {
    ElMessage.error(error.message || '变更申请提交失败')
  } finally {
    loading.value = false
  }
}

async function approveChangeRequestAction() {
  if (!changeApprovalForm.id) {
    ElMessage.warning('请先选择一条变更申请')
    return
  }
  try {
    loading.value = true
    await approveProjectChangeRequest(projectId.value, changeApprovalForm.id, {
      decision: changeApprovalForm.decision,
      comment: changeApprovalForm.comment || null,
    })
    await loadChangeRequests()
    await loadChangeRequestLogs(changeApprovalForm.id)
    resetChangeApprovalForm()
    ElMessage.success('变更审批已完成')
  } catch (error) {
    ElMessage.error(error.message || '变更审批失败')
  } finally {
    loading.value = false
  }
}

async function createBaselineFromApprovedChange(type) {
  const change = selectedChangeRequest.value
  if (!change?.id) {
    ElMessage.warning('请先选择一条变更申请')
    return
  }
  if (change.status !== 'APPROVED') {
    ElMessage.warning('只有已批准的变更才能生成新基线')
    return
  }

  const changeCode = change.changeCode || `CR${change.id}`
  const changeTitle = change.title || '未命名变更'

  try {
    loading.value = true
    if (type === 'SCOPE') {
      await createProjectScopeBaseline(projectId.value, {
        baselineName: `${changeCode} 范围基线`,
        description: `由已批准变更生成: ${changeTitle}`,
        snapshotJson: buildScopeBaselineSnapshot(),
      })
      scopeBaselines.value = normalizeListResult(await getProjectScopeBaselines(projectId.value))
      ElMessage.success('已根据变更生成新的范围基线')
      return
    }

    if (type === 'COST') {
      await createProjectCostBaseline(projectId.value, {
        baselineName: `${changeCode} 成本基线`,
        snapshotJson: buildCostBaselineSnapshot(),
      })
      costBaselines.value = normalizeListResult(await getProjectCostBaselines(projectId.value))
      ElMessage.success('已根据变更生成新的成本基线')
    }
  } catch (error) {
    ElMessage.error(error.message || '生成变更基线失败')
  } finally {
    loading.value = false
  }
}

async function openCostPlanDialog() {
  try {
    loading.value = true
    costPlans.value = mergeCostPlans(await getProjectCostPlans(projectId.value))
    resetCostPlanForm()
    costPlanDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '成本计划加载失败')
  } finally {
    loading.value = false
  }
}

function editCostPlan(item) {
  editingCostPlanId.value = String(item.id)
  costPlanForm.name = item.name || ''
  costPlanForm.type = item.type || 'LABOR'
  costPlanForm.taskId = item.isDraftPlan ? (item.draftTaskValue || '') : (item.taskId ? String(item.taskId) : '')
  costPlanForm.phase = item.phase || ''
  costPlanForm.plannedAmount = item.plannedAmount ?? ''
  costPlanForm.currency = item.currency || 'CNY'
  costPlanForm.remark = item.remark || ''
}

async function saveCostPlan() {
  if (!costPlanForm.name.trim()) {
    ElMessage.warning('请输入成本项名称')
    return
  }
  if (costPlanForm.plannedAmount === '' || Number(costPlanForm.plannedAmount) < 0) {
    ElMessage.warning('请输入有效的计划金额')
    return
  }

  try {
    loading.value = true
    const payload = {
      name: costPlanForm.name.trim(),
      type: costPlanForm.type,
      taskId: ensureCostTaskSelection(costPlanForm.taskId),
      phase: costPlanForm.phase.trim() || null,
      plannedAmount: Number(costPlanForm.plannedAmount),
      currency: costPlanForm.currency,
      remark: costPlanForm.remark.trim() || null,
    }
    const selectedDraftTask = String(costPlanForm.taskId || '').startsWith('draft:')
    if (selectedDraftTask) {
      if (editingCostPlanId.value && !isDraftPlanId(editingCostPlanId.value)) {
        ElMessage.warning('已保存的成本计划暂不支持直接改绑未保存任务，请先保存任务后再调整')
        return
      }
      upsertDraftCostPlan()
      costPlans.value = mergeCostPlans(await getProjectCostPlans(projectId.value))
      resetCostPlanForm()
      ElMessage.success('草稿成本计划已暂存，保存任务后会一并写入')
      return
    }

    if (editingCostPlanId.value && isDraftPlanId(editingCostPlanId.value)) {
      await createProjectCostPlan(projectId.value, payload)
      removeDraftCostPlanById(editingCostPlanId.value)
      ElMessage.success('草稿成本计划已转为正式数据')
    } else if (editingCostPlanId.value) {
      await updateProjectCostPlan(projectId.value, editingCostPlanId.value, payload)
      rememberCostDraftBinding(editingCostPlanId.value, costPlanForm.taskId)
      ElMessage.success('成本计划已更新')
    } else {
      await createProjectCostPlan(projectId.value, payload)
      ElMessage.success('成本计划已新增')
    }
    costPlans.value = mergeCostPlans(await getProjectCostPlans(projectId.value))
    resetCostPlanForm()
  } catch (error) {
    ElMessage.error(error.message || '成本计划保存失败')
  } finally {
    loading.value = false
  }
}

async function removeCostPlan(item) {
  try {
    if (item.isDraftPlan) {
      removeDraftCostPlanById(item.id)
      costPlans.value = mergeCostPlans(await getProjectCostPlans(projectId.value))
      if (editingCostPlanId.value === String(item.id)) {
        resetCostPlanForm()
      }
      ElMessage.success('草稿成本计划已删除')
      return
    }
    await ElMessageBox.confirm(
      `确认删除成本计划“${item.name || item.id}”吗？`,
      '删除成本计划',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectCostPlan(projectId.value, item.id)
    clearCostDraftBinding(item.id)
    costPlans.value = mergeCostPlans(await getProjectCostPlans(projectId.value))
    if (editingCostPlanId.value === String(item.id)) {
      resetCostPlanForm()
    }
    ElMessage.success('成本计划已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除成本计划失败')
    }
  } finally {
    loading.value = false
  }
}

async function openCostActualDialog() {
  try {
    loading.value = true
    costActuals.value = normalizeListResult(await getProjectCostActuals(projectId.value))
    resetCostActualForm()
    costActualDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '实际成本加载失败')
  } finally {
    loading.value = false
  }
}

async function saveCostActual() {
  if (costActualForm.amount === '' || Number(costActualForm.amount) < 0) {
    ElMessage.warning('请输入有效的实际金额')
    return
  }
  if (!costActualForm.spendDate) {
    ElMessage.warning('请选择发生日期')
    return
  }

  try {
    loading.value = true
    await createProjectCostActual(projectId.value, {
      taskId: ensureCostTaskSelection(costActualForm.taskId),
      sourceType: costActualForm.sourceType,
      amount: Number(costActualForm.amount),
      currency: costActualForm.currency,
      spendDate: costActualForm.spendDate,
      remark: costActualForm.remark.trim() || null,
    })
    costActuals.value = normalizeListResult(await getProjectCostActuals(projectId.value))
    resetCostActualForm()
    ElMessage.success('实际成本已登记')
  } catch (error) {
    ElMessage.error(error.message || '实际成本保存失败')
  } finally {
    loading.value = false
  }
}

async function removeCostActual(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除这条实际成本记录吗？金额 ${formatMoney(item.amount, item.currency || 'CNY')}`,
      '删除实际成本',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectCostActual(projectId.value, item.id)
    costActuals.value = normalizeListResult(await getProjectCostActuals(projectId.value))
    ElMessage.success('实际成本已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除实际成本失败')
    }
  } finally {
    loading.value = false
  }
}

async function openCostBaselineDialog() {
  try {
    loading.value = true
    const [plans, baselines, actuals] = await Promise.all([
      getProjectCostPlans(projectId.value),
      getProjectCostBaselines(projectId.value),
      getProjectCostActuals(projectId.value),
    ])
    costPlans.value = mergeCostPlans(plans)
    costBaselines.value = normalizeListResult(baselines)
    costActuals.value = normalizeListResult(actuals)
    resetCostBaselineForm()
    costBaselineDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '成本基线加载失败')
  } finally {
    loading.value = false
  }
}

async function saveCostBaseline() {
  if (!costBaselineForm.name.trim()) {
    ElMessage.warning('请输入成本基线名称')
    return
  }

  try {
    loading.value = true
    await createProjectCostBaseline(projectId.value, {
      baselineName: costBaselineForm.name.trim(),
      snapshotJson: buildCostBaselineSnapshot(),
    })
    costBaselines.value = normalizeListResult(await getProjectCostBaselines(projectId.value))
    resetCostBaselineForm()
    ElMessage.success('成本基线已保存')
  } catch (error) {
    ElMessage.error(error.message || '成本基线保存失败')
  } finally {
    loading.value = false
  }
}

async function removeCostBaseline(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除成本基线“${item.baselineName || item.id}”吗？`,
      '删除成本基线',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectCostBaseline(projectId.value, item.id)
    costBaselines.value = normalizeListResult(await getProjectCostBaselines(projectId.value))
    ElMessage.success('成本基线已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除成本基线失败')
    }
  } finally {
    loading.value = false
  }
}

async function openEvmDialog() {
  try {
    loading.value = true
    evmMetrics.value = await getProjectEvm(projectId.value)
    costPlans.value = normalizeListResult(await getProjectCostPlans(projectId.value))
    costActuals.value = normalizeListResult(await getProjectCostActuals(projectId.value))
    evmDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || 'EVM 数据加载失败')
  } finally {
    loading.value = false
  }
}

async function removeScopeBaseline(item) {
  try {
    await ElMessageBox.confirm(
      `确认删除范围基线“${item.baselineName || item.name || item.id}”吗？删除后无法恢复。`,
      '删除范围基线',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectScopeBaseline(projectId.value, item.id)
    scopeBaselines.value = normalizeListResult(await getProjectScopeBaselines(projectId.value))
    if (activeScopeBaselineId.value === item.id) {
      activeScopeBaselineId.value = null
    }
    ElMessage.success('范围基线已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除范围基线失败')
    }
  } finally {
    loading.value = false
  }
}

async function removeCurrentProject() {
  try {
    await ElMessageBox.confirm(
      '删除项目后将无法恢复，确认继续吗？',
      '删除项目',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProject(projectId.value)
    hasUnsavedChanges.value = false
    ElMessage.success('项目已删除')
    router.push({ name: 'dashboard' })
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除项目失败')
    }
  } finally {
    loading.value = false
  }
}

async function closeCurrentProject() {
  try {
    await ElMessageBox.confirm('确认关闭当前项目吗？', '关闭项目', {
      type: 'warning',
      confirmButtonText: '确认关闭',
      cancelButtonText: '取消',
    })
    loading.value = true
    await closeProject(projectId.value)
    hasUnsavedChanges.value = false
    router.push({ name: 'dashboard' })
    ElMessage.success('项目已关闭')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '关闭项目失败')
    }
  } finally {
    loading.value = false
  }
}

async function navigateBack() {
  if (!hasUnsavedChanges.value) {
    router.push({ name: 'dashboard' })
    return
  }

  try {
    await ElMessageBox.confirm('当前有未保存内容，确认直接返回吗？', '未保存提示', {
      type: 'warning',
      confirmButtonText: '直接返回',
      cancelButtonText: '继续编辑',
    })
    router.push({ name: 'dashboard' })
  } catch {
    // Keep editing on cancel.
  }
}

function ensureManagerAction(actionKey) {
  if (canUseAction(actionKey)) {
    return true
  }
  if (isClosedProject.value) {
    ElMessage.warning('项目已关闭，当前仅支持查看')
  }
  else {
    ElMessage.warning('当前权限不支持此操作')
  }
  return false
}

function handleRibbonAction(actionKey) {
  if (!ensureManagerAction(actionKey)) {
    return
  }
  if (actionKey === 'open-gantt-style') {
    openGanttStyleDialog()
    return
  }
  if (actionKey.startsWith('format:')) {
    applyGanttFormat(actionKey.slice(7))
    return
  }

  switch (actionKey) {
    case 'save': saveEditorContent(); break
    case 'back': navigateBack(); break
    case 'add-task': addTaskRow(); break
    case 'add-child': addChildTaskRow(); break
    case 'add-summary': addSummaryTaskRow(); break
    case 'add-milestone': addMilestoneRow(); break
    case 'indent': indentSelectedRow(); break
    case 'outdent': outdentSelectedRow(); break
    case 'delete': deleteSelectedTaskRow(); break
    case 'task-info': openTaskInfoDialog(); break
    case 'calendar':
    case 'alerts': openScheduleDialog(); break
    case 'my-tasks':
      taskMineOnly.value = !taskMineOnly.value
      break
    case 'project-info': openProjectInfo(); break
    case 'project-charter': openProjectCharterDialog(); break
    case 'project-members': openProjectMembersDialog(); break
    case 'project-dashboard': openDashboardDialog(); break
    case 'project-milestones': openMilestoneDialog(); break
    case 'project-risks': openRiskDialog(); break
    case 'project-risk-matrix': openRiskMatrixDialog(); break
    case 'project-wbs': openWbsDialog(); break
    case 'project-requirements': openRequirementDialog(); break
    case 'project-scope-baselines': openScopeBaselineDialog(); break
    case 'project-cost-plans': openCostPlanDialog(); break
    case 'project-cost-actuals': openCostActualDialog(); break
    case 'project-cost-baselines': openCostBaselineDialog(); break
    case 'project-evm': openEvmDialog(); break
    case 'project-timesheets': openTimesheetDialog(); break
    case 'project-acceptance': openAcceptanceDialog(); break
    case 'project-changes': openChangeRequestDialog(); break
    case 'project-closure': openProjectClosureDialog(); break
    case 'project-status': openProjectStatusDialog(); break
    case 'project-delete': removeCurrentProject(); break
    case 'project-close': closeCurrentProject(); break
    case 'platform-export': openExportDialog(); break
    case 'platform-import': openImportDialog(); break
    case 'platform-search': openGlobalSearchDialog(); break
    case 'platform-audit': openAuditLogDialog(); break
    case 'report-open':
    case 'report-generate': openReportDialog(); break
    default:
      break
  }
}

const timelineRange = computed(() => {
  const start = projectForm.startDate || todayString
  const baseEnd = projectForm.endDate && projectForm.endDate > start
    ? projectForm.endDate
    : formatDateToValue(addDays(new Date(`${start}T00:00:00`), 27))
  return { start, end: baseEnd }
})

const timelineWeeks = computed(() => {
  const startDate = new Date(`${timelineRange.value.start}T00:00:00`)
  const endDate = new Date(`${timelineRange.value.end}T00:00:00`)
  const totalDays = diffDays(startDate, endDate) + 1
  const extraDays = (7 - (totalDays % 7)) % 7
  const paddedEnd = addDays(endDate, extraDays)
  const weeks = []
  let cursor = new Date(startDate)

  while (cursor <= paddedEnd) {
    const days = []
    for (let i = 0; i < 7; i += 1) {
      const current = addDays(cursor, i)
      days.push({
        key: current.toISOString(),
        date: formatDateToValue(current),
        label: DAY_NAMES[current.getDay()],
        isBeyondEnd: current > endDate,
      })
    }
    weeks.push({
      key: cursor.toISOString(),
      label: `${cursor.getFullYear()}-${String(cursor.getMonth() + 1).padStart(2, '0')}-${String(cursor.getDate()).padStart(2, '0')}`,
      days,
    })
    cursor = addDays(cursor, 7)
  }

  return weeks
})

const timelineDays = computed(() => timelineWeeks.value.flatMap((week) => week.days))
const timelineDayCount = computed(() => timelineDays.value.length)
const endMarkerOffset = computed(() => {
  const index = timelineDays.value.findIndex((day) => day.date === timelineRange.value.end)
  return index >= 0 ? (index + 1) * DAY_WIDTH : null
})

function getTaskBarStyle(row) {
  if (!row.start || !row.finish || row.finish < row.start || isMilestoneRow(row)) return null
  const startIndex = timelineDays.value.findIndex((day) => day.date === row.start)
  const endIndex = timelineDays.value.findIndex((day) => day.date === row.finish)
  if (startIndex < 0 || endIndex < startIndex) return null
  const type = getRowBarType(row)
  const config = getBarStyleConfig(type)
  const thickness = Math.max(6, Number(config.thickness) || 16)
  return {
    left: `${startIndex * DAY_WIDTH + 3}px`,
    width: `${(endIndex - startIndex + 1) * DAY_WIDTH - 6}px`,
    top: `${getBarTop(thickness)}px`,
    height: `${thickness}px`,
    background: config.color,
    borderColor: config.border,
    color: config.border,
  }
}

function getTaskBarClass(row) {
  const type = getRowBarType(row)
  const config = getBarStyleConfig(type)
  const variant = getTaskBarVariant(row)
  return {
    'task-bar': true,
    [`${type}-bar`]: true,
    [`bar-variant-${variant}`]: isTaskBarType(type),
    [`task-shape-${config.shape}`]: type === 'main' || type === 'child' || type === 'parent',
    [`summary-shape-${config.shape}`]: type === 'summary',
  }
}

const visibleTaskRows = computed(() => {
  if (!effectiveTaskMineOnly.value || !currentUserId.value) {
    return taskRows.value.filter((row) => !isHiddenByCollapsedAncestor(row))
  }

  const visibleLocalIds = new Set()
  taskRows.value.forEach((row) => {
    if (isBlankPlaceholderRow(row)) return
    if (String(row.assigneeId || '') !== currentUserId.value) return
    visibleLocalIds.add(row.localId)

    let parentLocalId = row.localParentId
    while (parentLocalId) {
      visibleLocalIds.add(parentLocalId)
      const parentRow = taskRows.value.find((item) => item.localId === parentLocalId)
      parentLocalId = parentRow?.localParentId || null
    }
  })

  return taskRows.value.filter((row) => {
    if (isHiddenByCollapsedAncestor(row)) return false
    if (isBlankPlaceholderRow(row)) return false
    return visibleLocalIds.has(row.localId)
  })
})

function getRowTimelineAnchor(row, side = 'end') {
  if (isMilestoneRow(row)) {
    const style = getMilestoneStyle(row)
    if (!style) return null
    const left = Number.parseFloat(String(style.left || '0').replace('px', ''))
    const width = Number.parseFloat(String(style.width || '16').replace('px', ''))
    const top = Number.parseFloat(String(style.top || '0').replace('px', ''))
    const height = Number.parseFloat(String(style.height || '16').replace('px', ''))
    return {
      x: left + width / 2,
      y: top + height / 2,
    }
  }

  const style = getTaskBarStyle(row)
  if (!style) return null
  const left = Number.parseFloat(String(style.left || '0').replace('px', ''))
  const width = Number.parseFloat(String(style.width || '0').replace('px', ''))
  const top = Number.parseFloat(String(style.top || '0').replace('px', ''))
  const height = Number.parseFloat(String(style.height || '0').replace('px', ''))
  return {
    x: side === 'start' ? left : left + width,
    y: top + height / 2,
  }
}

const ganttDependencyPaths = computed(() => {
  const rowMap = new Map(visibleTaskRows.value.map((row, index) => [String(row.taskId || ''), { row, index }]))
  const rowHeight = 38

  return projectTaskDependencies.value
    .map((dependency) => {
      const predecessorMeta = rowMap.get(String(dependency.predecessorTaskId || ''))
      const successorMeta = rowMap.get(String(dependency.successorTaskId || ''))
      if (!predecessorMeta || !successorMeta) return null

      const type = String(dependency.dependencyType || 'FS').toUpperCase()
      const fromSide = type === 'SS' || type === 'SF' ? 'start' : 'end'
      const toSide = type === 'SS' || type === 'FS' ? 'start' : 'end'

      const fromAnchor = getRowTimelineAnchor(predecessorMeta.row, fromSide)
      const toAnchor = getRowTimelineAnchor(successorMeta.row, toSide)
      if (!fromAnchor || !toAnchor) return null

      const fromX = fromAnchor.x
      const fromY = predecessorMeta.index * rowHeight + fromAnchor.y
      const toX = toAnchor.x
      const toY = successorMeta.index * rowHeight + toAnchor.y
      const horizontalGap = 18
      const bendX = toX >= fromX ? fromX + horizontalGap : fromX + Math.max(horizontalGap, Math.abs(toX - fromX) / 2)

      return {
        id: dependency.id,
        d: `M ${fromX} ${fromY} L ${bendX} ${fromY} L ${bendX} ${toY} L ${toX} ${toY}`,
      }
    })
    .filter(Boolean)
})

function showSummaryCaps() {
  return ganttAppearance.summaryShape === 'bracket'
}

function getMilestoneClass(row) {
  const config = getBarStyleConfig(getRowBarType(row))
  return ['milestone-marker', `milestone-shape-${config.shape}`]
}

function getMilestoneStyle(row) {
  if (!isMilestoneRow(row) || !row.start) return null
  const startIndex = timelineDays.value.findIndex((day) => day.date === row.start)
  if (startIndex < 0) return null
  const config = getBarStyleConfig('milestone')
  const size = Math.max(10, Number(config.size) || 16)
  return {
    left: `${startIndex * DAY_WIDTH + DAY_WIDTH / 2 - size / 2}px`,
    top: `${Math.max(4, Math.round((38 - size) / 2))}px`,
    width: `${size}px`,
    height: `${size}px`,
    background: config.color,
    borderColor: config.border,
    color: config.border,
  }
}

function openGanttStyleDialog() {
  activeTab.value = 'format'
  ganttAppearanceSnapshot = cloneGanttAppearance()
  ganttStyleDialogVisible.value = true
}

function cancelGanttStyleDialog() {
  if (ganttAppearanceSnapshot) {
    applyGanttAppearanceConfig(ganttAppearanceSnapshot)
  }
  ganttAppearanceSnapshot = null
  ganttStyleDialogVisible.value = false
}

function confirmGanttStyleDialog() {
  saveGlobalGanttAppearance()
  ganttAppearanceSnapshot = null
  ganttStyleDialogVisible.value = false
}

function handleGanttStyleDialogBeforeClose(done) {
  cancelGanttStyleDialog()
  done()
}

function handleTimelineBlankClick(event) {
  const target = event.target
  if (!(target instanceof HTMLElement)) return
  if (target.closest('.task-bar, .milestone-marker, .end-marker')) return
  openGanttStyleDialog()
}

function handleGanttItemClick(row) {
  if (!row || isBlankPlaceholderRow(row)) return
  openTaskInfoDialog(row)
}

watch(
  taskRows,
  () => {
    ensureTrailingEmptyRows()
    syncSummaryRows()
    if (!isHydratingRows.value) hasUnsavedChanges.value = true
  },
  { deep: true },
)

watch(
  () => [wbsConfig.prefix, wbsConfig.separator, wbsConfig.paddingWidth],
  () => {
    refreshWbsCodes()
  },
)

watch(
  () => [riskForm.probability, riskForm.impact],
  () => {
    riskForm.level = evaluateRiskLevel(riskForm.probability, riskForm.impact)
  },
)

watch(
  () => riskForm.taskId,
  () => {
    const selected = riskTaskOptions.value.find((item) => item.value === riskForm.taskId)
    riskForm.phaseName = selected?.phaseName || ''
  },
)

watch(
  () => changeApprovalForm.id,
  async (value) => {
    if (!changeRequestDialogVisible.value) return
    await loadChangeRequestLogs(value)
  },
)

watch(
  visibleTabs,
  (nextTabs) => {
    if (!nextTabs.some((tab) => tab.key === activeTab.value)) {
      activeTab.value = nextTabs[0]?.key || 'task'
    }
  },
  { immediate: true },
)

watch(
  () => route.query.taskId,
  (value) => {
    pendingRouteTaskId.value = typeof value === 'string' ? value.trim() : ''
    if (pendingRouteTaskId.value) {
      openRouteTaskIfNeeded()
    }
  },
  { immediate: true },
)

watch(
  taskRows,
  () => {
    if (pendingRouteTaskId.value) {
      openRouteTaskIfNeeded()
    }
  },
  { deep: true },
)

onMounted(async () => {
  try {
    loading.value = true
    if (!projectId.value) throw new Error('未找到有效项目')
    loadGlobalGanttAppearance()
    loadProjectWbsConfig()
    await Promise.all([
      loadProjectDetailData(),
      loadProjectTasksData(),
      loadProjectMembersData(),
      loadProjectMilestonesData(),
      loadProjectRisksData(),
      loadProjectRiskMatrixData(),
    ])
    await openRouteTaskIfNeeded()
  } catch (error) {
    ElMessage.error(error.message || '项目加载失败')
    router.replace({ name: 'dashboard' })
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="editor-page">
    <header class="editor-toolbar">
      <div class="toolbar-tabs">
        <button type="button" class="nav-back" title="返回项目列表" @click="navigateBack">
          ←
        </button>
        <button type="button" class="save-shortcut" :disabled="saving || !canEditTaskPlan" title="保存文件" @click="saveEditorContent">
          <img :src="saveIcon" alt="保存" />
        </button>
        <button
          v-for="tab in visibleTabs"
          :key="tab.key"
          type="button"
          class="tab"
          :class="{ active: activeTab === tab.key }"
          @click="activeTab = tab.key"
        >
          {{ tab.label }}
        </button>
        <div class="toolbar-state">
          <span v-if="saving">保存中...</span>
          <span v-else-if="hasUnsavedChanges">未保存</span>
          <span v-else>已保存</span>
        </div>
      </div>

      <div class="ribbon">
        <div
          v-for="group in currentRibbonGroups"
          :key="group.title"
          class="ribbon-group"
          :class="{ 'ribbon-group-danger': group.emphasis === 'danger' }"
        >
          <div class="group-actions">
            <button
              v-for="action in group.actions"
              :key="action.key"
              type="button"
              class="tool-action"
              :disabled="isActionDisabled(action.key)"
              :class="[
                { active: isActionActive(action.key), 'tool-action-disabled': isActionDisabled(action.key) },
                action.variant ? `tool-action-${action.variant}` : '',
              ]"
              @click="handleRibbonAction(action.key)"
            >
              {{ action.label }}
            </button>
          </div>
          <span class="group-title">{{ group.title }}</span>
        </div>
      </div>
    </header>

    <main class="editor-content">
      <div class="schedule-banner">
        {{ projectDetail?.name || '项目计划' }}
        <span v-if="projectDetail?.status"> | {{ formatProjectStatus(projectDetail.status) }}</span>
      </div>

      <div class="gantt-shell" :style="ganttThemeStyle">
        <section class="task-grid" :class="{ 'task-grid-readonly': !canEditTaskPlan }">
          <div class="grid-header">
            <div class="header-cell indicator">#</div>
            <div class="header-cell wbs">WBS</div>
            <div class="header-cell mode">任务模式</div>
            <div class="header-cell name">任务名称</div>
            <div class="header-cell duration">工期</div>
            <div class="header-cell start">开始时间</div>
            <div class="header-cell finish">完成时间</div>
          </div>

          <div
            v-for="row in visibleTaskRows"
            :key="row.localId"
            class="grid-row"
            :class="{ 'parent-task-row': hasChildRows(row) && !isSummaryRow(row) }"
          >
            <div class="grid-cell indicator" :class="{ 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }">{{ getRowDisplayNumber(row) }}</div>
            <div class="grid-cell wbs" :class="{ 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }">
              {{ row.wbsCode || '-' }}
            </div>

            <div
              class="grid-cell mode editable"
              :class="{ active: canEditTaskPlan && isActiveCell(row.localId, 'mode'), 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }"
              @click="activateEditableCell(row.localId, 'mode')"
            >
              <input
                v-model="row.mode"
                type="text"
                :readonly="!canEditTaskPlan"
                :ref="(el) => setInputRef(row.localId, 'mode', el)"
                @focus="activateEditableCell(row.localId, 'mode')"
                @keydown.enter.prevent="focusNextRow(row.localId, 'mode')"
                @keydown.left.prevent="focusHorizontalCell(row.localId, 'mode', -1)"
                @keydown.right.prevent="focusHorizontalCell(row.localId, 'mode', 1)"
                @keydown.up.prevent="focusVerticalCell(row.localId, 'mode', -1)"
                @keydown.down.prevent="focusVerticalCell(row.localId, 'mode', 1)"
              />
            </div>

            <div
              class="grid-cell name editable"
              :class="{ active: canEditTaskPlan && isActiveCell(row.localId, 'name'), 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }"
              @click="activateEditableCell(row.localId, 'name')"
            >
              <button
                v-if="hasChildRows(row)"
                type="button"
                class="outline-toggle"
                :style="{ left: `${(row.outlineLevel || 0) * 22 + 6}px` }"
                @click.stop="toggleRowExpanded(row)"
              >
                {{ row.expanded === false ? '+' : '-' }}
              </button>
              <input
                v-model="row.name"
                type="text"
                :readonly="!canEditTaskPlan"
                :ref="(el) => setInputRef(row.localId, 'name', el)"
                :style="getNameCellStyle(row)"
                :class="getNameInputClass(row)"
                @focus="activateEditableCell(row.localId, 'name')"
                @keydown.enter.prevent="focusNextRow(row.localId, 'name')"
                @keydown.left.prevent="focusHorizontalCell(row.localId, 'name', -1)"
                @keydown.right.prevent="focusHorizontalCell(row.localId, 'name', 1)"
                @keydown.up.prevent="focusVerticalCell(row.localId, 'name', -1)"
                @keydown.down.prevent="focusVerticalCell(row.localId, 'name', 1)"
              />
              <span
                v-if="hasTaskRisk(row)"
                :class="getRiskBadgeClass(getHighestRiskLevel(row))"
                :title="`该任务关联 ${getTaskRisks(row).length} 条风险`"
              >
                !
              </span>
            </div>

            <div class="grid-cell duration editable" :class="{ active: canEditTaskPlan && isActiveCell(row.localId, 'duration') }" @click="activateEditableCell(row.localId, 'duration')">
              <input
                v-model="row.duration"
                type="text"
                :readonly="!canEditTaskPlan"
                :ref="(el) => setInputRef(row.localId, 'duration', el)"
                @input="handleDurationInput(row)"
                @focus="activateEditableCell(row.localId, 'duration')"
                @keydown.enter.prevent="focusNextRow(row.localId, 'duration')"
                @keydown.left.prevent="focusHorizontalCell(row.localId, 'duration', -1)"
                @keydown.right.prevent="focusHorizontalCell(row.localId, 'duration', 1)"
                @keydown.up.prevent="focusVerticalCell(row.localId, 'duration', -1)"
                @keydown.down.prevent="focusVerticalCell(row.localId, 'duration', 1)"
              />
            </div>

            <div class="grid-cell start editable" :class="{ active: canEditTaskPlan && isActiveCell(row.localId, 'start') }" @click="activateEditableCell(row.localId, 'start')">
              <el-date-picker
                v-model="row.start"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                :editable="false"
                :disabled="!canEditTaskPlan"
                :disabled-date="(date) => isDateDisabledByParent(row, date)"
                :ref="(el) => setInputRef(row.localId, 'start', el)"
                class="date-editor"
                @change="handleStartInput(row)"
                @focus="activateEditableCell(row.localId, 'start')"
              />
            </div>

            <div class="grid-cell finish editable" :class="{ active: canEditTaskPlan && isActiveCell(row.localId, 'finish') }" @click="activateEditableCell(row.localId, 'finish')">
              <el-date-picker
                v-model="row.finish"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                :editable="false"
                :disabled="!canEditTaskPlan"
                :disabled-date="(date) => isDateDisabledByParent(row, date)"
                :ref="(el) => setInputRef(row.localId, 'finish', el)"
                class="date-editor"
                @change="handleFinishInput(row)"
                @focus="activateEditableCell(row.localId, 'finish')"
              />
            </div>
          </div>
        </section>

        <section class="timeline-panel">
          <div class="timeline-header" :style="{ width: `${timelineDayCount * DAY_WIDTH}px` }">
            <div v-for="week in timelineWeeks" :key="week.key" class="week-block" :style="{ width: `${week.days.length * DAY_WIDTH}px` }">
              <div class="week-label">{{ week.label }}</div>
              <div class="week-days" :style="{ gridTemplateColumns: `repeat(${week.days.length}, ${DAY_WIDTH}px)` }">
                <span v-for="day in week.days" :key="day.key">{{ day.label }}</span>
              </div>
            </div>
          </div>

          <div class="timeline-body" @click="handleTimelineBlankClick">
            <div v-if="endMarkerOffset !== null" class="end-marker" :style="{ left: `${endMarkerOffset}px` }">
              <span>结束时间</span>
            </div>

            <svg
              v-if="ganttDependencyPaths.length"
              class="gantt-dependency-layer"
              :width="timelineDayCount * DAY_WIDTH"
              :height="visibleTaskRows.length * 38"
            >
              <defs>
                <marker id="gantt-dependency-arrow" markerWidth="8" markerHeight="8" refX="6" refY="4" orient="auto">
                  <path d="M 0 0 L 8 4 L 0 8 z" fill="#4f6fa8" />
                </marker>
              </defs>
              <path
                v-for="item in ganttDependencyPaths"
                :key="`gantt-dependency-${item.id}`"
                :d="item.d"
                class="gantt-dependency-path"
                marker-end="url(#gantt-dependency-arrow)"
              />
            </svg>

            <div
              v-for="row in visibleTaskRows"
              :key="`timeline-${row.localId}`"
              class="timeline-row"
              :class="{ active: selectedTaskRow?.localId === row.localId }"
              :style="{ gridTemplateColumns: `repeat(${timelineDayCount}, ${DAY_WIDTH}px)` }"
            >
              <span
                v-for="day in timelineDays"
                :key="`${row.localId}-${day.key}`"
                class="timeline-cell"
                :class="{ weekend: [0, 6].includes(new Date(`${day.date}T00:00:00`).getDay()), disabled: day.isBeyondEnd }"
              />
              <div
                v-if="getTaskBarStyle(row)"
                :class="getTaskBarClass(row)"
                :style="getTaskBarStyle(row)"
                @click.stop="handleGanttItemClick(row)"
              >
                <span
                  v-if="hasTaskRisk(row)"
                  :class="getRiskBadgeClass(getHighestRiskLevel(row))"
                  class="gantt-risk-badge"
                  :title="`该任务关联 ${getTaskRisks(row).length} 条风险`"
                >
                  !
                </span>
                <span
                  v-if="!isSummaryRow(row)"
                  class="task-progress-fill"
                  :style="{ width: `${Math.max(0, Math.min(100, Number(row.progress || 0)))}%` }"
                />
                <span v-if="isSummaryRow(row) && showSummaryCaps()" class="summary-cap summary-cap-left" />
                <span v-if="isSummaryRow(row) && showSummaryCaps()" class="summary-cap summary-cap-right" />
              </div>
              <div
                v-if="getMilestoneStyle(row)"
                :class="getMilestoneClass(row)"
                :style="getMilestoneStyle(row)"
                @click.stop="handleGanttItemClick(row)"
              />
            </div>
          </div>
        </section>
      </div>
    </main>

    <el-dialog
      v-model="ganttStyleDialogVisible"
      title="条形图样式"
      width="1120px"
      class="gantt-style-dialog"
      :close-on-click-modal="false"
      :before-close="handleGanttStyleDialogBeforeClose"
    >
      <div class="gantt-style-layout">
        <section class="gantt-style-list">
          <div class="style-list-toolbar">
            <el-button size="small">剪切行</el-button>
            <el-button size="small">粘贴行</el-button>
            <el-button size="small">插入行</el-button>
          </div>
          <div class="style-list-header">
            <span>名称</span>
            <span>外观</span>
            <span>类型</span>
          </div>
          <button
            v-for="item in ganttBarStyleOptions"
            :key="item.key"
            type="button"
            class="style-list-row"
            :class="{ active: selectedBarStyleType === item.key }"
            @click="selectedBarStyleType = item.key"
          >
            <span>{{ item.label }}</span>
            <span class="style-preview-cell">
              <span
                v-if="item.key !== 'milestone'"
                class="dialog-preview-bar"
                :class="getPreviewBarClass(item.key)"
                :style="getPreviewBarStyle(item.key, 84)"
              >
                <span v-if="item.key === 'summary' && getBarStyleConfig(item.key).shape === 'bracket'" class="summary-cap summary-cap-left" :style="{ borderRightColor: getBarStyleConfig(item.key).border }" />
                <span v-if="item.key === 'summary' && getBarStyleConfig(item.key).shape === 'bracket'" class="summary-cap summary-cap-right" :style="{ borderLeftColor: getBarStyleConfig(item.key).border }" />
              </span>
              <span
                v-else
                class="dialog-preview-milestone milestone-marker"
                :class="`milestone-shape-${getBarStyleConfig(item.key).shape}`"
                :style="getPreviewBarStyle(item.key)"
              />
            </span>
            <span>{{ item.label }}</span>
          </button>
        </section>
        <section class="gantt-style-editor">
          <div class="style-editor-tabs">
            <span class="active">条形图</span>
            <span>文本</span>
          </div>
          <div class="style-editor-panels">
            <div class="style-editor-panel">
              <h4>形状</h4>
              <el-select v-model="ganttAppearance[`${selectedBarStyleType}Shape`]">
                <el-option
                  v-for="shape in ganttShapeOptions[getStyleOptionFamily(selectedBarStyleType)]"
                  :key="shape.key"
                  :label="shape.label"
                  :value="shape.key"
                />
              </el-select>
            </div>
            <div v-if="selectedBarStyleType !== 'milestone'" class="style-editor-panel">
              <h4>粗度</h4>
              <el-slider
                v-model="ganttAppearance[`${selectedBarStyleType}Thickness`]"
                :min="6"
                :max="30"
                :show-input="true"
                size="small"
              />
            </div>
            <div v-else class="style-editor-panel">
              <h4>大小</h4>
              <el-slider
                v-model="ganttAppearance[`${selectedBarStyleType}Size`]"
                :min="10"
                :max="30"
                :show-input="true"
                size="small"
              />
            </div>
            <div class="style-editor-panel">
              <h4>填充颜色</h4>
              <div class="color-edit-row">
                <input v-model="ganttAppearance[`${selectedBarStyleType}Color`]" type="color" class="color-input" />
                <el-input
                  v-model="ganttAppearance[`${selectedBarStyleType}Color`]"
                  maxlength="7"
                  @blur="commitBarColor(selectedBarStyleType, 'Color')"
                />
              </div>
              <div class="color-presets">
                <button
                  v-for="preset in ganttStylePalette[getStyleOptionFamily(selectedBarStyleType)]"
                  :key="preset.key"
                  type="button"
                  class="color-chip"
                  :style="{ background: preset.color }"
                  @click="ganttAppearance[`${selectedBarStyleType}Color`] = preset.color; ganttAppearance[`${selectedBarStyleType}Border`] = preset.border"
                />
              </div>
              <div class="color-palette-grid">
                <button
                  v-for="color in ganttColorPalette"
                  :key="`${selectedBarStyleType}-fill-${color}`"
                  type="button"
                  class="color-chip palette-chip"
                  :style="{ background: color }"
                  @click="ganttAppearance[`${selectedBarStyleType}Color`] = color"
                />
              </div>
            </div>
            <div class="style-editor-panel">
              <h4>边框颜色</h4>
              <div class="color-edit-row">
                <input v-model="ganttAppearance[`${selectedBarStyleType}Border`]" type="color" class="color-input" />
                <el-input
                  v-model="ganttAppearance[`${selectedBarStyleType}Border`]"
                  maxlength="7"
                  @blur="commitBarColor(selectedBarStyleType, 'Border')"
                />
              </div>
              <div class="color-palette-grid">
                <button
                  v-for="color in ganttColorPalette"
                  :key="`${selectedBarStyleType}-border-${color}`"
                  type="button"
                  class="color-chip palette-chip"
                  :style="{ background: color }"
                  @click="ganttAppearance[`${selectedBarStyleType}Border`] = color"
                />
              </div>
            </div>
          </div>
          <div class="style-preview-board">
            <span>预览</span>
            <div class="style-preview-track">
              <span
                v-if="selectedBarStyleType !== 'milestone'"
                class="dialog-preview-bar preview-large"
                :class="getPreviewBarClass(selectedBarStyleType)"
                :style="getPreviewBarStyle(selectedBarStyleType, 160)"
              >
                <span v-if="selectedBarStyleType === 'summary' && currentBarStyleConfig.shape === 'bracket'" class="summary-cap summary-cap-left" :style="{ borderRightColor: currentBarStyleConfig.border }" />
                <span v-if="selectedBarStyleType === 'summary' && currentBarStyleConfig.shape === 'bracket'" class="summary-cap summary-cap-right" :style="{ borderLeftColor: currentBarStyleConfig.border }" />
              </span>
              <span
                v-else
                class="dialog-preview-milestone milestone-marker"
                :class="`milestone-shape-${currentBarStyleConfig.shape}`"
                :style="getPreviewBarStyle(selectedBarStyleType)"
              />
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="cancelGanttStyleDialog">取消</el-button>
        <el-button type="primary" @click="confirmGanttStyleDialog">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="wbsDialogVisible" title="WBS" width="760px">
      <div class="info-section">
        <h4>WBS 编码规则</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="前缀">
            <el-input v-model="wbsConfig.prefix" placeholder="例如 WBS / P / PRJ" />
          </el-form-item>
          <el-form-item label="分隔符">
            <el-input v-model="wbsConfig.separator" maxlength="3" placeholder="例如 ." />
          </el-form-item>
          <el-form-item label="层级位数">
            <el-input-number v-model="wbsConfig.paddingWidth" :min="1" :max="6" style="width: 100%" />
          </el-form-item>
          <el-form-item label="示例" class="scope-form-span">
            <el-input :model-value="formatWbsCode([1, 2, 3]) || '1.2.3'" disabled />
          </el-form-item>
        </el-form>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>当前任务 WBS</h4>
          <el-button @click="resequenceWbsCodes">重排 WBS 编码</el-button>
        </div>
        <div v-if="wbsTreeNodes.length" class="wbs-tree">
          <div v-for="node in wbsTreeNodes" :key="`wbs-tree-root-${node.localId}`" class="wbs-tree-node">
            <div class="wbs-tree-row" :style="{ paddingLeft: '0px' }">
              <button
                v-if="node.children.length"
                type="button"
                class="wbs-tree-toggle"
                @click="toggleWbsTreeNode(node.localId)"
              >
                {{ isWbsTreeExpanded(node.localId) ? '-' : '+' }}
              </button>
              <span v-else class="wbs-tree-spacer" />
              <strong>{{ node.wbsCode || '-' }}</strong>
              <span>{{ node.name || '未命名任务' }}</span>
            </div>
            <template v-if="node.children.length && isWbsTreeExpanded(node.localId)">
              <div v-for="child in node.children" :key="`wbs-tree-child-${child.localId}`" class="wbs-tree-node">
                <div class="wbs-tree-row" :style="{ paddingLeft: `${((child.outlineLevel || 0) + 1) * 16}px` }">
                  <button
                    v-if="child.children.length"
                    type="button"
                    class="wbs-tree-toggle"
                    @click="toggleWbsTreeNode(child.localId)"
                  >
                    {{ isWbsTreeExpanded(child.localId) ? '-' : '+' }}
                  </button>
                  <span v-else class="wbs-tree-spacer" />
                  <strong>{{ child.wbsCode || '-' }}</strong>
                  <span>{{ child.name || '未命名任务' }}</span>
                </div>
                <template v-if="child.children.length && isWbsTreeExpanded(child.localId)">
                  <div v-for="grandChild in child.children" :key="`wbs-tree-grand-${grandChild.localId}`" class="wbs-tree-node">
                    <div class="wbs-tree-row" :style="{ paddingLeft: `${((grandChild.outlineLevel || 0) + 1) * 16}px` }">
                      <button
                        v-if="grandChild.children.length"
                        type="button"
                        class="wbs-tree-toggle"
                        @click="toggleWbsTreeNode(grandChild.localId)"
                      >
                        {{ isWbsTreeExpanded(grandChild.localId) ? '-' : '+' }}
                      </button>
                      <span v-else class="wbs-tree-spacer" />
                      <strong>{{ grandChild.wbsCode || '-' }}</strong>
                      <span>{{ grandChild.name || '未命名任务' }}</span>
                    </div>
                    <template v-if="grandChild.children.length && isWbsTreeExpanded(grandChild.localId)">
                      <div v-for="deepChild in grandChild.children" :key="`wbs-tree-deep-${deepChild.localId}`" class="wbs-tree-node">
                        <div class="wbs-tree-row" :style="{ paddingLeft: `${((deepChild.outlineLevel || 0) + 1) * 16}px` }">
                          <button
                            v-if="deepChild.children.length"
                            type="button"
                            class="wbs-tree-toggle"
                            @click="toggleWbsTreeNode(deepChild.localId)"
                          >
                            {{ isWbsTreeExpanded(deepChild.localId) ? '-' : '+' }}
                          </button>
                          <span v-else class="wbs-tree-spacer" />
                          <strong>{{ deepChild.wbsCode || '-' }}</strong>
                          <span>{{ deepChild.name || '未命名任务' }}</span>
                        </div>
                      </div>
                    </template>
                  </div>
                </template>
              </div>
            </template>
          </div>
        </div>
        <el-empty v-else description="暂无可生成 WBS 的任务" />
      </div>
      <template #footer>
        <el-button @click="cancelWbsDialog">取消</el-button>
        <el-button type="primary" @click="saveWbsConfig">保存规则</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="projectStatusDialogVisible" title="项目状态流转" width="420px">
      <el-form label-width="88px">
        <el-form-item label="当前状态">
          <el-input :model-value="formatProjectStatus(projectDetail?.status)" disabled />
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="selectedProjectStatus" :disabled="!canManageProject" style="width: 100%">
            <el-option v-for="option in projectStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="projectStatusDialogVisible = false">{{ canManageProject ? '取消' : '关闭' }}</el-button>
        <el-button v-if="canManageProject" type="primary" :loading="loading" @click="saveProjectStatus">确认变更</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="requirementDialogVisible" title="需求" width="760px">
      <div class="info-section">
        <h4>新增需求</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="标题"><el-input v-model="requirementForm.title" placeholder="例如 登录必须支持双因子验证" /></el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="requirementForm.priority" style="width: 100%">
              <el-option label="高" value="HIGH" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-form-item>
          <el-form-item label="说明" class="scope-form-span">
            <el-input v-model="requirementForm.description" type="textarea" :rows="3" placeholder="记录需求背景、验收口径或来源" />
          </el-form-item>
        </el-form>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>需求列表</h4>
          <el-button type="primary" :loading="loading" @click="saveRequirement">新增需求</el-button>
        </div>
        <div v-if="requirements.length" class="simple-list">
          <div v-for="item in requirements" :key="`requirement-${item.id}`" class="simple-list-item">
            <strong>{{ item.title || item.name || `需求${item.id}` }}</strong>
            <span>优先级: {{ item.priority || 'MEDIUM' }} | 状态: {{ item.status || 'DRAFT' }}</span>
            <span>{{ item.description || '暂无说明' }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无需求" />
      </div>
    </el-dialog>

    <el-dialog v-model="scopeBaselineDialogVisible" title="范围基线" width="760px">
      <div class="info-section">
        <h4>新增范围基线</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="名称"><el-input v-model="scopeBaselineForm.name" placeholder="例如 V1.0 范围基线" /></el-form-item>
          <el-form-item label="说明" class="scope-form-span">
            <el-input v-model="scopeBaselineForm.description" type="textarea" :rows="3" placeholder="记录本次范围冻结说明、依据文档或审批备注" />
          </el-form-item>
        </el-form>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>基线列表</h4>
          <el-button type="primary" :loading="loading" @click="saveScopeBaseline">新增基线</el-button>
        </div>
        <div v-if="scopeBaselines.length" class="simple-list">
          <div v-for="item in scopeBaselines" :key="`scope-baseline-${item.id}`" class="simple-list-item">
            <strong>{{ item.baselineName || item.name || `基线${item.id}` }}</strong>
            <span>创建时间: {{ item.publishedAt || item.createdAt || item.createdTime || '-' }} | 状态: {{ item.status || 'ACTIVE' }}</span>
            <span>{{ item.description || '暂无说明' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="toggleScopeBaselinePreview(item.id)">
                {{ isScopeBaselineActive(item.id) ? '收起快照' : '查看快照' }}
              </el-button>
              <el-button size="small" type="primary" plain @click="restoreScopeBaseline(item)">
                回退到此基线
              </el-button>
              <el-button size="small" type="danger" plain @click="removeScopeBaseline(item)">
                删除基线
              </el-button>
            </div>
            <div v-if="isScopeBaselineActive(item.id)" class="baseline-snapshot-card">
              <div class="baseline-snapshot-meta">
                <span>快照类型: 仅锁定 WBS 工作内容</span>
                <span>任务数量: {{ parseScopeBaselineSnapshot(item).tasks.length }}</span>
              </div>
              <div v-if="buildSnapshotTree(parseScopeBaselineSnapshot(item)).length" class="wbs-tree">
                <div v-for="node in buildSnapshotTree(parseScopeBaselineSnapshot(item))" :key="`baseline-tree-root-${item.id}-${node.localId}`" class="wbs-tree-node">
                  <div class="wbs-tree-row" :style="{ paddingLeft: '0px' }">
                    <button
                      v-if="node.children.length"
                      type="button"
                      class="wbs-tree-toggle"
                      @click="toggleScopeBaselineTreeNode(`${item.id}-${node.localId}`)"
                    >
                      {{ isScopeBaselineTreeExpanded(`${item.id}-${node.localId}`) ? '-' : '+' }}
                    </button>
                    <span v-else class="wbs-tree-spacer" />
                    <strong>{{ node.wbsCode || '-' }}</strong>
                    <span>{{ node.name || '未命名任务' }}</span>
                  </div>
                  <template v-if="node.children.length && isScopeBaselineTreeExpanded(`${item.id}-${node.localId}`)">
                    <div v-for="child in node.children" :key="`baseline-tree-child-${item.id}-${child.localId}`" class="wbs-tree-node">
                      <div class="wbs-tree-row" :style="{ paddingLeft: `${((child.outlineLevel || 0) + 1) * 16}px` }">
                        <button
                          v-if="child.children.length"
                          type="button"
                          class="wbs-tree-toggle"
                          @click="toggleScopeBaselineTreeNode(`${item.id}-${child.localId}`)"
                        >
                          {{ isScopeBaselineTreeExpanded(`${item.id}-${child.localId}`) ? '-' : '+' }}
                        </button>
                        <span v-else class="wbs-tree-spacer" />
                        <strong>{{ child.wbsCode || '-' }}</strong>
                        <span>{{ child.name || '未命名任务' }}</span>
                      </div>
                      <template v-if="child.children.length && isScopeBaselineTreeExpanded(`${item.id}-${child.localId}`)">
                        <div v-for="grandChild in child.children" :key="`baseline-tree-grand-${item.id}-${grandChild.localId}`" class="wbs-tree-node">
                          <div class="wbs-tree-row" :style="{ paddingLeft: `${((grandChild.outlineLevel || 0) + 1) * 16}px` }">
                            <button
                              v-if="grandChild.children.length"
                              type="button"
                              class="wbs-tree-toggle"
                              @click="toggleScopeBaselineTreeNode(`${item.id}-${grandChild.localId}`)"
                            >
                              {{ isScopeBaselineTreeExpanded(`${item.id}-${grandChild.localId}`) ? '-' : '+' }}
                            </button>
                            <span v-else class="wbs-tree-spacer" />
                            <strong>{{ grandChild.wbsCode || '-' }}</strong>
                            <span>{{ grandChild.name || '未命名任务' }}</span>
                          </div>
                          <template v-if="grandChild.children.length && isScopeBaselineTreeExpanded(`${item.id}-${grandChild.localId}`)">
                            <div v-for="deepChild in grandChild.children" :key="`baseline-tree-deep-${item.id}-${deepChild.localId}`" class="wbs-tree-node">
                              <div class="wbs-tree-row" :style="{ paddingLeft: `${((deepChild.outlineLevel || 0) + 1) * 16}px` }">
                                <button
                                  v-if="deepChild.children.length"
                                  type="button"
                                  class="wbs-tree-toggle"
                                  @click="toggleScopeBaselineTreeNode(`${item.id}-${deepChild.localId}`)"
                                >
                                  {{ isScopeBaselineTreeExpanded(`${item.id}-${deepChild.localId}`) ? '-' : '+' }}
                                </button>
                                <span v-else class="wbs-tree-spacer" />
                                <strong>{{ deepChild.wbsCode || '-' }}</strong>
                                <span>{{ deepChild.name || '未命名任务' }}</span>
                              </div>
                            </div>
                          </template>
                        </div>
                      </template>
                    </div>
                  </template>
                </div>
              </div>
              <el-empty v-else description="该基线下暂无 WBS 快照内容" />
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无范围基线" />
      </div>
    </el-dialog>

    <el-dialog v-model="costPlanDialogVisible" title="成本计划" width="860px">
      <div class="info-section">
        <h4>{{ editingCostPlanId ? '编辑成本计划' : '新增成本计划' }}</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="名称">
            <el-input v-model="costPlanForm.name" placeholder="例如 后端开发人工成本" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="costPlanForm.type" style="width: 100%">
              <el-option label="人工" value="LABOR" />
              <el-option label="采购" value="PROCUREMENT" />
              <el-option label="设备" value="EQUIPMENT" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="关联任务">
            <el-select v-model="costPlanForm.taskId" clearable filterable placeholder="可选，建议先保存任务">
              <el-option v-for="option in costTaskOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="阶段">
            <el-input v-model="costPlanForm.phase" placeholder="例如 开发 / 测试 / 上线" />
          </el-form-item>
          <el-form-item label="计划金额">
            <el-input v-model="costPlanForm.plannedAmount" type="number" placeholder="0.00" />
          </el-form-item>
          <el-form-item label="币种">
            <el-input v-model="costPlanForm.currency" placeholder="CNY" />
          </el-form-item>
          <el-form-item label="备注" class="scope-form-span">
            <el-input v-model="costPlanForm.remark" type="textarea" :rows="3" placeholder="记录预算依据、采购说明或估算口径" />
          </el-form-item>
        </el-form>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>计划列表</h4>
          <div class="dialog-summary-text">总计划成本: {{ formatMoney(totalPlannedCost) }}</div>
        </div>
        <div class="dialog-actions-inline">
          <el-button @click="resetCostPlanForm">重置表单</el-button>
          <el-button type="primary" :loading="loading" @click="saveCostPlan">{{ editingCostPlanId ? '保存修改' : '新增计划' }}</el-button>
        </div>
        <div v-if="costPlans.length" class="simple-list">
          <div v-for="item in costPlans" :key="`cost-plan-${item.id}`" class="simple-list-item">
            <strong>{{ item.name || `成本计划${item.id}` }}</strong>
            <span>状态: {{ item.isDraftPlan ? '草稿，退出不保存则丢失' : '已保存' }} | 类型: {{ item.type || '-' }}</span>
            <span>任务: {{ item.taskName || item.draftTaskLabel || '未关联任务' }} | 阶段: {{ item.phase || '-' }}</span>
            <span>金额: {{ formatMoney(item.plannedAmount, item.currency || 'CNY') }}</span>
            <span>{{ item.remark || '暂无备注' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="editCostPlan(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeCostPlan(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无成本计划" />
      </div>
    </el-dialog>

    <el-dialog v-model="costActualDialogVisible" title="实际成本" width="860px">
      <div class="info-section">
        <h4>登记实际成本</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="关联任务">
            <el-select v-model="costActualForm.taskId" clearable filterable placeholder="可选，建议先保存任务">
              <el-option v-for="option in costTaskOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="来源">
            <el-select v-model="costActualForm.sourceType" style="width: 100%">
              <el-option label="手工录入" value="MANUAL" />
              <el-option label="报销单" value="EXPENSE" />
              <el-option label="采购付款" value="PURCHASE" />
              <el-option label="工时折算" value="TIMESHEET" />
            </el-select>
          </el-form-item>
          <el-form-item label="实际金额">
            <el-input v-model="costActualForm.amount" type="number" placeholder="0.00" />
          </el-form-item>
          <el-form-item label="币种">
            <el-input v-model="costActualForm.currency" placeholder="CNY" />
          </el-form-item>
          <el-form-item label="发生日期">
            <el-date-picker v-model="costActualForm.spendDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="备注" class="scope-form-span">
            <el-input v-model="costActualForm.remark" type="textarea" :rows="3" placeholder="记录报销、付款、合同或工时成本说明" />
          </el-form-item>
        </el-form>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>实际成本列表</h4>
          <div class="dialog-summary-text">总实际成本: {{ formatMoney(totalActualCost) }}</div>
        </div>
        <div class="dialog-actions-inline">
          <el-button type="primary" :loading="loading" @click="saveCostActual">新增登记</el-button>
        </div>
        <div v-if="costActuals.length" class="simple-list">
          <div v-for="item in costActuals" :key="`cost-actual-${item.id}`" class="simple-list-item">
            <strong>{{ item.taskName || '未关联任务' }}</strong>
            <span>来源: {{ item.sourceType || '-' }} | 日期: {{ item.spendDate || '-' }}</span>
            <span>金额: {{ formatMoney(item.amount, item.currency || 'CNY') }}</span>
            <span>{{ item.remark || '暂无备注' }}</span>
            <div class="baseline-actions">
              <el-button size="small" type="danger" plain @click="removeCostActual(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无实际成本" />
      </div>
    </el-dialog>

    <el-dialog v-model="costBaselineDialogVisible" title="成本基线" width="860px">
      <div class="info-section">
        <h4>新增成本基线</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="名称">
            <el-input v-model="costBaselineForm.name" placeholder="例如 成本基线V1" />
          </el-form-item>
          <el-form-item label="快照说明" class="scope-form-span">
            <el-input model-value="保存时会冻结当前成本计划快照，后续计划调整不会影响已保存基线。" type="textarea" :rows="2" disabled />
          </el-form-item>
        </el-form>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>基线列表</h4>
          <el-button type="primary" :loading="loading" @click="saveCostBaseline">新增基线</el-button>
        </div>
        <div v-if="costBaselines.length" class="simple-list">
          <div v-for="item in costBaselines" :key="`cost-baseline-${item.id}`" class="simple-list-item">
            <strong>{{ item.baselineName || `成本基线${item.id}` }}</strong>
            <span>创建时间: {{ item.publishedAt || '-' }}</span>
            <span>快照计划数: {{ parseCostBaselineSnapshot(item).plans.length }} | 快照计划总额: {{ formatMoney(parseCostBaselineSnapshot(item).totals.planned) }}</span>
            <div class="baseline-actions">
              <el-button size="small" type="danger" plain @click="removeCostBaseline(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无成本基线" />
      </div>
    </el-dialog>

    <el-dialog v-model="evmDialogVisible" title="EVM" width="760px">
      <div class="dashboard-panels">
        <div class="dashboard-card"><span>PV 计划值</span><strong>{{ formatMoney(evmMetrics?.pv) }}</strong></div>
        <div class="dashboard-card"><span>EV 挣值</span><strong>{{ formatMoney(evmMetrics?.ev) }}</strong></div>
        <div class="dashboard-card"><span>AC 实际成本</span><strong>{{ formatMoney(evmMetrics?.ac) }}</strong></div>
        <div class="dashboard-card"><span>CV 成本偏差</span><strong>{{ formatMoney(evmMetrics?.cv) }}</strong></div>
        <div class="dashboard-card"><span>SV 进度偏差</span><strong>{{ formatMoney(evmMetrics?.sv) }}</strong></div>
        <div class="dashboard-card"><span>CPI 成本绩效指数</span><strong>{{ evmMetrics?.cpi ?? 0 }}</strong></div>
        <div class="dashboard-card"><span>SPI 进度绩效指数</span><strong>{{ evmMetrics?.spi ?? 0 }}</strong></div>
      </div>
      <div class="info-section">
        <h4>EVM 说明</h4>
        <div class="simple-list">
          <div class="simple-list-item">
            <strong>数据口径</strong>
            <span>PV 来自成本计划总额，EV 按任务进度折算计划值，AC = 手工实际成本 + 工时自动折算人工成本。</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="infoDialogVisible" title="项目信息" width="680px">
      <el-form label-width="96px">
        <el-form-item label="项目名称"><el-input v-model="projectForm.name" :disabled="!canManageProject" /></el-form-item>
        <el-form-item label="项目描述"><el-input v-model="projectForm.description" :disabled="!canManageProject" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="生命周期">
          <el-select v-model="projectForm.lifeCycleModel" :disabled="!canManageProject" style="width: 100%">
            <el-option v-for="option in lifeCycleOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="projectForm.startDate" :disabled="!canManageProject" type="date" value-format="YYYY-MM-DD" :disabled-date="disabledStartDate" @change="normalizeProjectDates" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="projectForm.endDate" :disabled="!canManageProject" type="date" value-format="YYYY-MM-DD" :disabled-date="disabledEndDate" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="infoDialogVisible = false">{{ canManageProject ? '取消' : '关闭' }}</el-button>
        <el-button v-if="canManageProject" type="primary" :loading="loading" @click="saveProjectInfo">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="projectMembersDialogVisible" title="项目成员" width="860px">
      <div v-if="canManageProject" class="info-section">
        <div class="section-header-inline">
          <h4>{{ projectMemberForm.memberId ? '编辑成员' : '添加成员' }}</h4>
          <div class="dialog-summary-text">任务负责人仅可从项目负责人和在项成员中选择</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="成员">
            <el-select
              v-model="projectMemberForm.userId"
              filterable
              :disabled="Boolean(projectMemberForm.memberId)"
              placeholder="选择要加入项目的用户"
            >
              <el-option
                v-for="option in availableUserOptions"
                :key="`project-member-user-${option.value}`"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="projectMemberForm.projectRole" style="width: 100%">
              <el-option
                v-for="option in projectMemberRoleOptions"
                :key="`project-member-role-${option.value}`"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item v-if="projectMemberForm.memberId" label="状态">
            <el-select v-model="projectMemberForm.memberStatus" style="width: 100%">
              <el-option
                v-for="option in projectMemberStatusOptions"
                :key="`project-member-status-${option.value}`"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetProjectMemberForm()">重置</el-button>
          <el-button type="primary" :loading="loading" @click="saveProjectMemberAction">
            {{ projectMemberForm.memberId ? '保存成员' : '加入项目' }}
          </el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>成员列表</h4>
          <div class="dialog-summary-text">在项成员可分配任务，被移除成员不会再出现在负责人列表中</div>
        </div>
        <div v-if="projectTeamMembers.length" class="simple-list">
          <div v-for="item in projectTeamMembers" :key="`project-member-${item.id}`" class="simple-list-item">
            <strong>
              {{ item.realName || item.username || `成员${item.userId}` }}
              <template v-if="getProjectMemberBadges(item).length">
                <span v-for="badge in getProjectMemberBadges(item)" :key="`${item.id}-${badge}`" class="member-inline-tag">{{ badge }}</span>
              </template>
            </strong>
            <span>账号: {{ item.username || '-' }}</span>
            <span>角色: {{ formatRole(item.projectRole) }}</span>
            <span>状态: {{ formatMemberStatus(item.memberStatus) }}</span>
            <span>加入时间: {{ item.joinedAt ? String(item.joinedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <div class="baseline-actions">
              <el-button v-if="canManageProject && !item.isVirtualOwner" size="small" @click="resetProjectMemberForm(item)">编辑</el-button>
              <el-button v-if="canManageProject && !item.isVirtualOwner" size="small" type="danger" plain @click="removeProjectMemberAction(item)">移除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无项目成员" />
      </div>
    </el-dialog>

    <el-dialog v-model="charterDialogVisible" title="项目章程" width="760px">
      <el-form label-width="96px">
        <el-form-item label="项目目标"><el-input v-model="charterForm.objective" :disabled="!canManageProject" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="范围摘要"><el-input v-model="charterForm.scopeSummary" :disabled="!canManageProject" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="发起人"><el-input v-model="charterForm.sponsor" :disabled="!canManageProject" /></el-form-item>
        <el-form-item label="审批人"><el-input v-model="charterForm.approver" :disabled="!canManageProject" /></el-form-item>
        <el-form-item label="干系人"><el-input v-model="charterForm.stakeholders" :disabled="!canManageProject" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="成功标准"><el-input v-model="charterForm.successCriteria" :disabled="!canManageProject" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="charterDialogVisible = false">{{ canManageProject ? '取消' : '关闭' }}</el-button>
        <el-button v-if="canManageProject" type="primary" :loading="loading" @click="saveCharter">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="dashboardDialogVisible" title="项目概览" width="760px">
      <div class="dashboard-panels">
        <div class="dashboard-card"><span>任务总数</span><strong>{{ projectDashboard?.taskTotal ?? 0 }}</strong></div>
        <div class="dashboard-card"><span>已完成任务</span><strong>{{ projectDashboard?.taskCompleted ?? 0 }}</strong></div>
        <div class="dashboard-card"><span>完成率</span><strong>{{ projectDashboard?.taskCompletionRate ?? 0 }}</strong></div>
      </div>
    </el-dialog>

    <el-dialog v-model="scheduleDialogVisible" title="日程" width="860px">
      <div class="info-section">
        <h4>项目日程</h4>
        <div v-if="calendarEvents.length" class="simple-list">
          <div v-for="item in calendarEvents" :key="`calendar-${item.id}`" class="simple-list-item">
            <strong>{{ item.title }}</strong>
            <span>{{ item.eventType }} | {{ item.startTime }} - {{ item.endTime }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无日程数据" />
      </div>
      <div class="info-section">
        <h4>关键路径</h4>
        <div v-if="criticalPathTasks.length" class="simple-list">
          <div v-for="item in criticalPathTasks" :key="`critical-${item.id}`" class="simple-list-item">
            <strong>{{ item.name }}</strong>
            <span>{{ item.plannedStartDate }} - {{ item.plannedEndDate }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无关键路径数据" />
      </div>
      <div class="info-section">
        <h4>预警</h4>
        <div v-if="scheduleAlerts.length" class="simple-list">
          <div v-for="item in scheduleAlerts" :key="`alert-${item.taskId}`" class="simple-list-item">
            <strong>{{ item.taskName }}</strong>
            <span>{{ item.alertLevel }} | {{ item.alertMessage }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无预警" />
      </div>
    </el-dialog>

    <el-dialog v-model="taskInfoDialogVisible" title="任务详情" width="820px">
      <div v-if="selectedTaskRow" class="info-section">
        <div class="dashboard-panels">
          <div class="dashboard-card"><span>任务名称</span><strong>{{ selectedTaskRow.name || '-' }}</strong></div>
          <div class="dashboard-card"><span>WBS</span><strong>{{ selectedTaskRow.wbsCode || '-' }}</strong></div>
          <div class="dashboard-card"><span>任务模式</span><strong>{{ selectedTaskRow.mode || '-' }}</strong></div>
          <div class="dashboard-card"><span>计划开始</span><strong>{{ selectedTaskRow.start || '-' }}</strong></div>
          <div class="dashboard-card"><span>计划完成</span><strong>{{ selectedTaskRow.finish || '-' }}</strong></div>
          <div class="dashboard-card"><span>当前状态</span><strong>{{ formatTaskStatus(taskDetail?.status || selectedTaskRow.status) }}</strong></div>
        </div>
      </div>
      <div v-if="selectedTaskRow" class="task-detail-section-tabs">
        <button
          v-for="item in visibleTaskDetailSections"
          :key="item.key"
          type="button"
          class="task-detail-tab"
          :class="{ active: taskDetailSection === item.key }"
          @click="taskDetailSection = item.key"
        >
          {{ item.label }}
        </button>
      </div>
      <el-skeleton :loading="taskDetailLoading" animated>
        <template #template>
          <div class="simple-list">
            <div class="simple-list-item"><strong>正在加载任务详情...</strong></div>
          </div>
        </template>
        <template #default>
          <div v-if="selectedTaskRow && taskDetailSection === 'basic'" class="info-section">
            <div class="section-header-inline">
              <h4>基础信息</h4>
              <div class="dialog-summary-text">维护描述和关联里程碑</div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item label="负责人">
                <el-select v-model="taskBasicForm.assigneeId" clearable filterable :disabled="!canEditTaskBasic" placeholder="从项目成员中选择负责人">
                  <el-option v-for="option in taskAssigneeOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="描述" class="scope-form-span">
                <el-input
                  v-model="taskBasicForm.description"
                  :disabled="!canEditTaskBasic"
                  type="textarea"
                  :rows="3"
                  placeholder="输入任务说明、执行目标或补充信息"
                />
              </el-form-item>
              <el-form-item label="关联里程碑">
                <el-select v-model="taskBasicForm.milestoneId" clearable filterable :disabled="!canEditTaskBasic" placeholder="可选绑定到一个里程碑">
                  <el-option v-for="option in milestoneOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
            </el-form>
            <div v-if="canEditTaskBasic" class="dialog-actions-inline">
              <el-button type="primary" :loading="taskDetailLoading" @click="saveTaskBasicInfo">保存基础信息</el-button>
            </div>
            <div class="simple-list">
              <div class="simple-list-item">
                <strong>描述</strong>
                <span>{{ taskDetail?.description || taskBasicForm.description || selectedTaskRow.name || '暂无描述' }}</span>
              </div>
              <div class="simple-list-item">
                <strong>进度</strong>
                <span>{{ Number(taskDetail?.progress ?? selectedTaskRow.progress ?? 0) }}%</span>
              </div>
              <div class="simple-list-item">
                <strong>负责人</strong>
                <span>{{ taskDetail?.assigneeName || selectedTaskRow.assigneeName || '未分配' }}</span>
              </div>
              <div class="simple-list-item">
                <strong>关联里程碑</strong>
                <span>{{ taskDetail?.milestoneName || '未关联' }}</span>
              </div>
              <div class="simple-list-item">
                <strong>依赖数量</strong>
                <span>{{ taskDetail?.dependencies?.length || 0 }}</span>
              </div>
              <div class="simple-list-item">
                <strong>评论数量</strong>
                <span>{{ taskDetail?.comments?.length || 0 }}</span>
              </div>
            </div>
          </div>
          <div v-if="selectedTaskRow && taskDetailSection === 'progress'" class="info-section">
            <div class="section-header-inline">
              <h4>进度更新</h4>
              <div class="dialog-summary-text">更新后会直接影响 EVM 的 EV / SPI / CPI 计算</div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item label="状态">
                <el-select v-model="taskProgressForm.status" :disabled="!selectedTaskCanUpdateProgress" style="width: 100%">
                  <el-option label="未开始" value="TODO" />
                  <el-option label="进行中" value="IN_PROGRESS" />
                  <el-option label="已完成" value="DONE" />
                  <el-option label="阻塞" value="BLOCKED" />
                </el-select>
              </el-form-item>
              <el-form-item label="进度">
                <el-slider v-model="taskProgressForm.progress" :disabled="!selectedTaskCanUpdateProgress" :min="0" :max="100" :show-input="true" />
              </el-form-item>
              <el-form-item label="备注" class="scope-form-span">
                <el-input v-model="taskProgressForm.remark" :disabled="!selectedTaskCanUpdateProgress" type="textarea" :rows="3" placeholder="记录本次进展、阻塞原因或完成说明" />
              </el-form-item>
            </el-form>
          </div>
          <div v-if="selectedTaskRow?.taskId && taskDetailSection === 'dependency'" class="info-section">
            <div class="section-header-inline">
              <h4>任务依赖</h4>
              <div class="dialog-summary-text">当前任务作为后置任务，选择它的前置任务</div>
            </div>
            <div class="simple-list">
              <div v-if="taskDetail?.dependencies?.length" class="simple-list">
                <div v-for="item in taskDetail.dependencies" :key="`task-dependency-${item.id}`" class="simple-list-item">
                  <strong>{{ item.predecessorTaskName }} -> {{ item.successorTaskName }}</strong>
                  <span>类型: {{ item.dependencyType || 'FS' }}</span>
                  <div class="baseline-actions">
                    <el-button v-if="canManageProject" size="small" type="danger" plain @click="removeTaskDependency(item)">删除</el-button>
                  </div>
                </div>
              </div>
              <span v-else>暂无依赖</span>
            </div>
            <el-form v-if="canManageProject" label-width="88px" class="scope-form-grid">
              <el-form-item label="前置任务">
                <el-select v-model="taskDependencyForm.predecessorTaskId" filterable clearable placeholder="请选择前置任务">
                  <el-option v-for="option in dependencyTaskOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="依赖类型">
                <el-select v-model="taskDependencyForm.dependencyType" style="width: 100%">
                  <el-option label="完成-开始 FS" value="FS" />
                  <el-option label="开始-开始 SS" value="SS" />
                  <el-option label="完成-完成 FF" value="FF" />
                  <el-option label="开始-完成 SF" value="SF" />
                </el-select>
              </el-form-item>
            </el-form>
            <div v-if="canManageProject" class="dialog-actions-inline">
              <el-button type="primary" :loading="taskDetailLoading" @click="saveTaskDependency">新增依赖</el-button>
            </div>
          </div>
          <div v-if="selectedTaskRow && taskDetailSection === 'risk'" class="info-section">
            <div class="section-header-inline">
              <h4>关联风险</h4>
              <div class="dialog-summary-text">查看当前任务挂接的风险，风险内容在项目页风险登记册里统一维护</div>
            </div>
            <div class="simple-list">
              <div v-if="taskRelatedRisks.length" class="simple-list">
                <div v-for="item in taskRelatedRisks" :key="`task-risk-${item.id}`" class="simple-list-item">
                  <strong>{{ item.riskCode || '-' }} | {{ item.name || '未命名风险' }}</strong>
                  <span>等级: {{ formatRiskLevel(item.level) }} | 状态: {{ formatRiskStatus(item.status) }}</span>
                  <span>阶段: {{ item.phaseName || '未设置阶段' }}</span>
                  <span>应对策略: {{ item.responseStrategy || '未填写' }}</span>
                </div>
              </div>
              <span v-else>当前任务未关联风险</span>
            </div>
          </div>
          <div v-if="selectedTaskRow && taskDetailSection === 'comment'" class="info-section">
            <div class="section-header-inline">
              <h4>任务评论</h4>
              <div class="dialog-summary-text">记录进展说明、协作沟通或问题跟踪</div>
            </div>
            <div v-if="taskCommentForm.replyToId" class="reply-target-banner">
              <span>正在回复：{{ taskCommentForm.replyToName }}</span>
              <el-button text @click="resetTaskCommentForm">取消回复</el-button>
            </div>
            <div class="scope-form-grid">
              <div class="scope-form-span">
                <el-input
                  v-model="taskCommentForm.content"
                  :disabled="!canCommentOnTasks"
                  type="textarea"
                  :rows="3"
                  placeholder="输入评论内容"
                />
              </div>
            </div>
            <div v-if="canCommentOnTasks" class="dialog-actions-inline">
              <el-button type="primary" :loading="taskDetailLoading" @click="saveTaskComment">发表评论</el-button>
            </div>
            <div class="simple-list">
              <div v-if="taskCommentThreads.length" class="comment-thread-list">
                <div v-for="item in taskCommentThreads" :key="`task-comment-${item.id}`" class="comment-thread-item">
                  <div class="simple-list-item">
                    <strong>{{ item.userName || '未知用户' }}</strong>
                    <span>{{ item.createdAt || '-' }}</span>
                    <span>{{ item.content || '' }}</span>
                    <div class="baseline-actions">
                      <el-button v-if="canCommentOnTasks" size="small" text @click="startReplyComment(item)">回复</el-button>
                      <el-button v-if="canManageProject" size="small" type="danger" plain @click="removeTaskComment(item)">删除</el-button>
                    </div>
                  </div>
                  <div v-if="item.replies?.length" class="comment-replies">
                    <div v-for="reply in item.replies" :key="`task-comment-reply-${reply.id}`" class="simple-list-item comment-reply-item">
                      <strong>{{ reply.userName || '未知用户' }}</strong>
                      <span>{{ reply.createdAt || '-' }}</span>
                      <span>回复 {{ reply.replyToName || item.userName || '该评论' }}：{{ reply.content || '' }}</span>
                      <div class="baseline-actions">
                        <el-button v-if="canCommentOnTasks" size="small" text @click="startReplyComment(reply)">回复</el-button>
                        <el-button v-if="canManageProject" size="small" type="danger" plain @click="removeTaskComment(reply)">删除</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <span v-else>暂无评论</span>
            </div>
          </div>
        </template>
      </el-skeleton>
      <el-empty v-if="!selectedTaskRow" description="请先选中任务" />
      <template #footer>
        <el-button @click="taskInfoDialogVisible = false">关闭</el-button>
        <el-button v-if="taskDetailSection === 'progress' && selectedTaskCanUpdateProgress" type="primary" :loading="taskDetailLoading" @click="saveTaskProgress">保存进度</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="milestoneDialogVisible" title="里程碑管理" width="860px">
      <div class="info-section">
        <div class="section-header-inline">
          <h4>{{ editingMilestoneId ? '编辑里程碑' : '新增里程碑' }}</h4>
          <div class="dialog-summary-text">独立维护项目里程碑，并可在任务详情里进行关联</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="名称">
            <el-input v-model="milestoneForm.name" placeholder="例如 需求冻结、版本上线" />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="milestoneForm.status" style="width: 100%">
              <el-option label="待处理" value="PENDING" />
              <el-option label="已达成" value="REACHED" />
              <el-option label="已延期" value="DELAYED" />
            </el-select>
          </el-form-item>
          <el-form-item label="计划时间">
            <el-date-picker
              v-model="milestoneForm.plannedDate"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择计划时间"
            />
          </el-form-item>
          <el-form-item label="说明" class="scope-form-span">
            <el-input v-model="milestoneForm.description" type="textarea" :rows="3" placeholder="记录该里程碑的验收标准或关键说明" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetMilestoneForm">重置</el-button>
          <el-button type="primary" :loading="milestoneLoading" @click="saveMilestone">{{ editingMilestoneId ? '保存修改' : '新增里程碑' }}</el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>里程碑列表</h4>
          <div class="dialog-summary-text">当前项目下全部独立里程碑</div>
        </div>
        <div v-if="milestoneList.length" class="simple-list">
          <div v-for="item in milestoneList" :key="`milestone-${item.id}`" class="simple-list-item">
            <strong>{{ item.name || `里程碑${item.id}` }}</strong>
            <span>状态: {{ item.status || 'PENDING' }}</span>
            <span>计划时间: {{ item.plannedDate ? String(item.plannedDate).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <span>{{ item.description || '暂无说明' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="populateMilestoneForm(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeMilestone(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无里程碑" />
      </div>
    </el-dialog>

    <el-dialog v-model="riskDialogVisible" title="风险登记册" width="980px">
      <div class="info-section">
        <div class="section-header-inline">
          <h4>{{ editingRiskId ? '编辑风险' : '新增风险' }}</h4>
          <div class="dialog-summary-text">维护风险清单、应对策略和状态流转</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="风险名称">
            <el-input v-model="riskForm.name" placeholder="例如 核心人员流失、需求频繁变更" />
          </el-form-item>
          <el-form-item label="风险等级">
            <el-input :model-value="formatRiskLevel(riskForm.level)" disabled />
          </el-form-item>
          <el-form-item label="概率">
            <el-slider v-model="riskForm.probability" :min="1" :max="5" :step="1" :show-stops="true" :show-input="true" />
          </el-form-item>
          <el-form-item label="影响">
            <el-slider v-model="riskForm.impact" :min="1" :max="5" :step="1" :show-stops="true" :show-input="true" />
          </el-form-item>
          <el-form-item label="关联任务">
            <el-select v-model="riskForm.taskId" clearable filterable placeholder="选择风险影响的 WBS 任务">
              <el-option
                v-for="option in riskTaskOptions"
                :key="`risk-task-${option.value}`"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关联阶段">
            <el-input v-model="riskForm.phaseName" placeholder="可自动带出，也可手动补充阶段说明" />
          </el-form-item>
          <el-form-item label="风险描述" class="scope-form-span">
            <el-input v-model="riskForm.description" type="textarea" :rows="3" placeholder="描述触发条件、影响范围或成因" />
          </el-form-item>
          <el-form-item label="应对策略" class="scope-form-span">
            <el-input v-model="riskForm.responseStrategy" type="textarea" :rows="3" placeholder="记录规避、减轻、转移或接受策略" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetRiskForm">重置</el-button>
          <el-button type="primary" :loading="riskLoading" @click="saveRisk">{{ editingRiskId ? '保存修改' : '新增风险' }}</el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>状态流转</h4>
          <div class="dialog-summary-text">从列表中选中一条风险后更新状态</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="当前风险">
            <el-select v-model="riskStatusForm.riskId" filterable clearable placeholder="请选择风险">
              <el-option
                v-for="item in riskList"
                :key="`risk-status-${item.id}`"
                :label="`${item.riskCode || '-'} | ${item.name || '未命名风险'}`"
                :value="String(item.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="新状态">
            <el-select v-model="riskStatusForm.status" style="width: 100%">
              <el-option v-for="option in riskStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="说明" class="scope-form-span">
            <el-input v-model="riskStatusForm.comment" type="textarea" :rows="2" placeholder="记录本次状态变更说明" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button type="primary" :loading="riskLoading" @click="saveRiskStatus">更新状态</el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>风险列表</h4>
          <div class="dialog-summary-text">按概率和影响维护项目风险登记册</div>
        </div>
        <div v-if="riskList.length" class="simple-list">
          <div v-for="item in riskList" :key="`risk-${item.id}`" class="simple-list-item">
            <strong>{{ item.riskCode || '-' }} | {{ item.name || '未命名风险' }}</strong>
            <span>等级: {{ formatRiskLevel(item.level) }} | 状态: {{ formatRiskStatus(item.status) }}</span>
            <span>概率: {{ item.probability || 0 }} | 影响: {{ item.impact || 0 }}</span>
            <span>关联任务: {{ item.taskName || '未绑定任务' }}</span>
            <span>关联阶段: {{ item.phaseName || '未设置阶段' }}</span>
            <span>应对策略: {{ item.responseStrategy || '未填写' }}</span>
            <span>识别时间: {{ item.identifiedAt ? String(item.identifiedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="populateRiskForm(item)">编辑</el-button>
              <el-button size="small" @click="startRiskStatusEdit(item)">改状态</el-button>
              <el-button size="small" type="danger" plain @click="removeRisk(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无风险数据" />
      </div>
    </el-dialog>

    <el-dialog v-model="riskMatrixDialogVisible" title="风险矩阵" width="940px">
      <div class="dashboard-panels">
        <div class="dashboard-card"><span>高风险</span><strong>{{ riskMatrix?.highCount ?? 0 }}</strong></div>
        <div class="dashboard-card"><span>严重风险</span><strong>{{ riskMatrix?.criticalCount ?? 0 }}</strong></div>
        <div class="dashboard-card"><span>风险总数</span><strong>{{ riskMatrix?.levels?.length ?? 0 }}</strong></div>
      </div>
      <div class="info-section">
        <h4>5 x 5 风险矩阵</h4>
        <div class="risk-matrix-board">
          <div class="risk-matrix-corner">概率 \ 影响</div>
          <div v-for="impact in 5" :key="`impact-head-${impact}`" class="risk-matrix-header">{{ impact }}</div>
          <template v-for="probability in [5, 4, 3, 2, 1]" :key="`row-${probability}`">
            <div class="risk-matrix-header">{{ probability }}</div>
            <div
              v-for="impact in 5"
              :key="`cell-${probability}-${impact}`"
              :class="getMatrixCellClass(probability, impact)"
            >
              <div class="risk-matrix-count">{{ getMatrixCellItems(probability, impact).length }}</div>
              <div v-if="getMatrixCellItems(probability, impact).length" class="risk-matrix-names">
                <span
                  v-for="item in getMatrixCellItems(probability, impact)"
                  :key="`matrix-risk-${item.riskId}`"
                >
                  {{ item.riskName || `风险${item.riskId}` }}
                </span>
              </div>
            </div>
          </template>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="exportDialogVisible" title="导出" width="760px">
      <div class="info-section">
        <h4>创建导出任务</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="模块">
            <el-select v-model="exportForm.module" style="width: 100%">
              <el-option v-for="option in exportModuleOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="格式">
            <el-select v-model="exportForm.format" style="width: 100%">
              <el-option v-for="option in exportFormatOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button type="primary" :loading="opsLoading" @click="submitExportTask">创建导出任务</el-button>
        </div>
      </div>
      <div class="info-section">
        <h4>本次会话导出记录</h4>
        <div v-if="exportTasks.length" class="simple-list">
          <div v-for="item in exportTasks" :key="`export-${item.id}`" class="simple-list-item">
            <strong>{{ item.moduleName || '-' }} | {{ item.exportFormat || '-' }}</strong>
            <span>状态: {{ item.status || '-' }}</span>
            <span>请求时间: {{ item.requestedAt ? String(item.requestedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <div class="baseline-actions">
              <el-button
                v-if="item.fileAttachmentId"
                size="small"
                type="primary"
                plain
                @click="openAttachmentDownload(item.fileAttachmentId)"
              >
                下载文件
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无导出记录" />
      </div>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="Excel导入" width="720px">
      <div class="info-section">
        <h4>上传 Excel 文件</h4>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="模块">
            <el-select v-model="importForm.module" style="width: 100%">
              <el-option v-for="option in exportModuleOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="模板">
            <el-button @click="downloadImportTemplate">下载当前模块模板</el-button>
          </el-form-item>
          <el-form-item label="文件" class="scope-form-span">
            <el-upload
              :auto-upload="false"
              :show-file-list="false"
              :limit="1"
              accept=".xlsx,.xls,.csv"
              action="#"
              :on-change="handleImportFileChange"
            >
              <el-button>选择文件</el-button>
            </el-upload>
            <div class="dialog-summary-text">{{ importForm.fileName || '尚未选择文件' }}</div>
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button type="primary" :loading="opsLoading" @click="submitImportExcel">开始导入</el-button>
        </div>
        <div class="import-template-panel">
          <strong>当前模板字段</strong>
          <div class="dialog-summary-text">{{ currentImportTemplate.headers.join(', ') }}</div>
          <strong>填写说明</strong>
          <div class="template-notes">
            <span v-for="item in currentImportTemplate.fields" :key="`${importForm.module}-${item}`">{{ item }}</span>
          </div>
        </div>
        <div v-if="importResult" class="import-result-panel">
          <div class="section-header-inline">
            <h4>导入结果</h4>
            <div class="dialog-summary-text">成功 {{ importResult.successCount ?? 0 }} 条，失败 {{ importResult.failCount ?? 0 }} 条</div>
          </div>
          <div class="dashboard-panels">
            <div class="dashboard-card"><span>成功数</span><strong>{{ importResult.successCount ?? 0 }}</strong></div>
            <div class="dashboard-card"><span>失败数</span><strong>{{ importResult.failCount ?? 0 }}</strong></div>
            <div class="dashboard-card"><span>模块</span><strong>{{ importForm.module }}</strong></div>
          </div>
          <div class="simple-list">
            <div v-if="importResult.errors?.length" class="simple-list">
              <div v-for="(item, index) in importResult.errors" :key="`import-error-${index}`" class="simple-list-item import-error-item">
                <strong>错误 {{ index + 1 }}</strong>
                <span>{{ item }}</span>
              </div>
            </div>
            <span v-else>本次导入没有错误明细。</span>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="globalSearchDialogVisible" title="全局搜索" width="860px">
      <div class="info-section">
        <div class="report-generate-form">
          <el-input v-model="searchForm.keyword" placeholder="输入关键词，搜索项目 / 任务 / 风险 / 经验教训" style="width: 320px" />
          <el-select v-model="searchForm.type" style="width: 160px">
            <el-option v-for="option in searchTypeOptions" :key="option.value || 'ALL'" :label="option.label" :value="option.value" />
          </el-select>
          <el-button type="primary" :loading="opsLoading" @click="runGlobalSearch">搜索</el-button>
        </div>
      </div>
      <div class="info-section">
        <h4>搜索结果</h4>
        <div v-if="globalSearchResults.length" class="simple-list">
          <div v-for="item in globalSearchResults" :key="`search-${item.type}-${item.id}`" class="simple-list-item">
            <strong>{{ item.type || '-' }} | {{ item.title || '-' }}</strong>
            <span>项目: {{ item.projectName || '-' }}</span>
            <span>{{ item.summary || '无摘要' }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无搜索结果" />
      </div>
    </el-dialog>

    <el-dialog v-model="auditLogDialogVisible" title="审计日志" width="980px">
      <div class="info-section">
        <div class="report-generate-form">
          <el-input v-model="auditForm.module" placeholder="模块名，例如 TASK / RISK / COST" style="width: 220px" />
          <el-date-picker v-model="auditForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="开始时间" />
          <el-date-picker v-model="auditForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="结束时间" />
          <el-button type="primary" :loading="opsLoading" @click="loadAuditLogs">查询</el-button>
        </div>
      </div>
      <div class="info-section">
        <h4>日志列表</h4>
        <div v-if="auditLogs.length" class="simple-list">
          <div v-for="item in auditLogs" :key="`audit-${item.id}`" class="simple-list-item">
            <strong>{{ item.moduleName || '-' }} | {{ item.action || '-' }}</strong>
            <span>操作人: {{ item.operatorName || item.operatorId || '-' }}</span>
            <span>请求: {{ item.requestMethod || '-' }} {{ item.requestPath || '-' }}</span>
            <span>结果码: {{ item.resultCode ?? '-' }} | IP: {{ item.ipAddress || '-' }}</span>
            <span>时间: {{ item.createdAt ? String(item.createdAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无审计日志" />
      </div>
    </el-dialog>

    <el-dialog v-model="timesheetDialogVisible" title="工时管理" width="920px">
      <div class="dashboard-panels">
        <div class="dashboard-card"><span>总工时</span><strong>{{ timesheetReport?.totalHours ?? 0 }}</strong></div>
        <div class="dashboard-card"><span>参与人数</span><strong>{{ timesheetReport?.userCount ?? 0 }}</strong></div>
        <div class="dashboard-card"><span>人工成本</span><strong>{{ formatMoney(timesheetReport?.totalLaborCost) }}</strong></div>
        <div class="dashboard-card"><span>记录数</span><strong>{{ timesheetRecords.length }}</strong></div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>{{ editingTimesheetId ? '编辑工时' : '登记工时' }}</h4>
          <div class="dialog-summary-text">工时只能关联已保存任务</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="关联任务">
            <el-select v-model="timesheetForm.taskId" filterable placeholder="请选择已保存任务">
              <el-option v-for="option in timesheetTaskOptions" :key="`timesheet-task-${option.value}`" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="工时日期">
            <el-date-picker v-model="timesheetForm.workDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="工时">
            <el-input v-model="timesheetForm.hours" type="number" min="0" step="0.5" placeholder="例如 8" />
          </el-form-item>
          <el-form-item label="说明" class="scope-form-span">
            <el-input v-model="timesheetForm.description" type="textarea" :rows="3" placeholder="记录今天完成了什么、是否有阻塞或返工" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetTimesheetForm">重置</el-button>
          <el-button type="primary" :loading="loading" @click="saveTimesheet">{{ editingTimesheetId ? '保存修改' : '新增工时' }}</el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>工时记录</h4>
          <div class="report-generate-form">
            <el-select v-model="timesheetFilterForm.taskId" clearable filterable style="width: 220px" placeholder="按任务筛选">
              <el-option v-for="option in timesheetTaskOptions" :key="`timesheet-filter-${option.value}`" :label="option.label" :value="option.value" />
            </el-select>
            <el-date-picker v-model="timesheetFilterForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
            <el-date-picker v-model="timesheetFilterForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
            <el-button @click="resetTimesheetFilters">清空</el-button>
            <el-button type="primary" :loading="loading" @click="loadTimesheetRecords">查询</el-button>
          </div>
        </div>
        <div v-if="timesheetRecords.length" class="simple-list">
          <div v-for="item in timesheetRecords" :key="`timesheet-${item.id}`" class="simple-list-item">
            <strong>{{ item.workDate || '-' }} | {{ item.taskName || '未命名任务' }}</strong>
            <span>填报人: {{ item.userName || item.userId || '-' }} | 工时: {{ item.hours || 0 }}</span>
            <span>工时单价: {{ formatMoney(item.costRate) }} | 人工成本: {{ formatMoney(item.laborCost) }}</span>
            <span>状态: {{ item.status || '-' }}</span>
            <span>{{ item.description || '暂无说明' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="populateTimesheetForm(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeTimesheet(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无工时记录" />
      </div>
    </el-dialog>

    <el-dialog v-model="projectClosureDialogVisible" title="项目收尾" width="980px">
      <div class="closure-steps">
        <button
          v-for="item in closureStepOptions"
          :key="item.key"
          type="button"
          class="closure-step"
          :class="{ active: closureStep === item.key }"
          @click="closureStep = item.key"
        >
          {{ item.label }}
        </button>
      </div>
      <div class="dashboard-panels">
        <div class="dashboard-card"><span>归档项</span><strong>{{ closureArchives.length }}</strong></div>
        <div class="dashboard-card"><span>验收事项</span><strong>{{ acceptanceItems.length }}</strong></div>
        <div class="dashboard-card"><span>经验教训</span><strong>{{ closureLessons.length }}</strong></div>
        <div class="dashboard-card"><span>报告数</span><strong>{{ reportList.length }}</strong></div>
      </div>
      <div v-if="closureStep === 'archive'" class="info-section">
        <div class="section-header-inline">
          <h4>第三步：归档项</h4>
          <div class="dialog-summary-text">将已完成验收并形成报告的文档、代码仓和交付物正式归档</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="名称">
            <el-input v-model="archiveForm.itemName" placeholder="例如 项目验收文档、源代码仓库" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="archiveForm.archiveType" style="width: 100%">
              <el-option v-for="option in archiveTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="归档附件">
            <div class="upload-inline">
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                :limit="1"
                action="#"
                :on-change="handleArchiveAttachmentChange"
              >
                <el-button :loading="loading">选择文件</el-button>
              </el-upload>
              <el-button v-if="archiveForm.attachmentId" @click="clearArchiveAttachment">清空附件</el-button>
            </div>
            <div class="dialog-summary-text">
              {{ archiveForm.attachmentName || '尚未上传附件，可直接上传并自动绑定' }}
            </div>
          </el-form-item>
          <el-form-item label="仓库地址">
            <el-input v-model="archiveForm.repositoryUrl" placeholder="可选，例如 Git 仓库或知识库地址" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetArchiveForm">重置</el-button>
          <el-button type="primary" :loading="loading" @click="saveArchive">新增归档项</el-button>
        </div>
        <div v-if="closureArchives.length" class="simple-list">
          <div v-for="item in closureArchives" :key="`archive-${item.id}`" class="simple-list-item">
            <strong>{{ item.itemName || `归档项${item.id}` }}</strong>
            <span>类型: {{ item.archiveType || '-' }} | 状态: {{ item.status || '-' }}</span>
            <span>仓库: {{ item.repositoryUrl || '未填写' }}</span>
            <span>归档时间: {{ item.archivedAt ? String(item.archivedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <div class="baseline-actions">
              <el-button v-if="item.attachmentId" size="small" @click="openAttachmentDownload(item.attachmentId)">下载附件</el-button>
              <el-button size="small" type="danger" plain @click="removeArchive(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无归档项" />
      </div>
      <div v-if="closureStep === 'acceptance'" class="info-section">
        <div class="section-header-inline">
          <h4>第一步：验收确认</h4>
          <div class="dialog-summary-text">先确认项目交付结果、签收状态和验收证明材料</div>
        </div>
        <div v-if="acceptanceItems.length" class="simple-list">
          <div v-for="item in acceptanceItems" :key="`closure-acceptance-${item.id}`" class="simple-list-item">
            <strong>{{ item.itemName || `验收事项${item.id}` }}</strong>
            <span>状态: {{ formatAcceptanceStatus(item.status) }}</span>
            <span>负责人: {{ item.ownerName || item.ownerId || '未设置' }}</span>
            <span>{{ item.description || '暂无说明' }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无验收事项，请先在验收事项模块中补充" />
      </div>
      <div v-if="closureStep === 'report'" class="info-section">
        <div class="section-header-inline">
          <h4>第二步：生成总结报告</h4>
          <div class="dialog-summary-text">基于验收结果生成总结报告，后续可作为归档材料留存</div>
        </div>
        <div class="report-generate-form">
          <el-date-picker v-model="closureSummaryForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
          <el-date-picker v-model="closureSummaryForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
          <el-button type="primary" :loading="loading" @click="generateSummaryReportAction">生成总结报告</el-button>
        </div>
        <div v-if="reportList.length" class="simple-list">
          <div v-for="item in reportList" :key="`closure-report-${item.id}`" class="simple-list-item">
            <strong>{{ item.title || `报告${item.id}` }}</strong>
            <span>类型: {{ item.reportType || '-' }}</span>
            <span>周期: {{ item.startDate || '-' }} ~ {{ item.endDate || '-' }}</span>
            <span>生成时间: {{ item.generatedAt ? String(item.generatedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无已生成报告" />
      </div>
      <div v-if="closureStep === 'lesson'" class="info-section">
        <div class="section-header-inline">
          <h4>第四步：{{ editingLessonId ? '编辑经验教训' : '新增经验教训' }}</h4>
          <div class="dialog-summary-text">沉淀过程问题、成功做法和后续建议</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="分类">
            <el-input v-model="lessonForm.category" placeholder="例如 过程 / 技术 / 协作 / 质量" />
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="lessonForm.title" placeholder="例如 需求评审前置后返工明显下降" />
          </el-form-item>
          <el-form-item label="内容" class="scope-form-span">
            <el-input v-model="lessonForm.content" type="textarea" :rows="3" placeholder="记录背景、现象、处理方式和复用建议" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetLessonForm">重置</el-button>
          <el-button type="primary" :loading="loading" @click="saveLesson">{{ editingLessonId ? '保存修改' : '新增经验' }}</el-button>
        </div>
        <div v-if="closureLessons.length" class="simple-list">
          <div v-for="item in closureLessons" :key="`lesson-${item.id}`" class="simple-list-item">
            <strong>{{ item.title || `经验${item.id}` }}</strong>
            <span>分类: {{ item.category || '-' }} | 作者: {{ item.authorName || item.authorId || '-' }}</span>
            <span>创建时间: {{ item.createdAt ? String(item.createdAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <span>{{ item.content || '暂无内容' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="populateLessonForm(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeLesson(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无经验教训" />
      </div>
      <div v-if="closureStep === 'review'" class="info-section">
        <div class="section-header-inline">
          <h4>第五步：收尾检查</h4>
          <div class="dialog-summary-text">最后检查是否满足项目关闭前置条件</div>
        </div>
        <div class="simple-list">
          <div v-for="item in closureReadinessItems" :key="item.label" class="simple-list-item">
            <strong>{{ item.label }}</strong>
            <span>{{ item.done ? '已满足' : '未满足' }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="closure-footer">
          <el-button
            :disabled="closureStep === closureStepOptions[0].key"
            @click="closureStep = closureStepOptions[Math.max(0, closureStepOptions.findIndex((item) => item.key === closureStep) - 1)].key"
          >
            上一步
          </el-button>
          <el-button
            v-if="closureStep !== closureStepOptions[closureStepOptions.length - 1].key"
            type="primary"
            @click="closureStep = closureStepOptions[Math.min(closureStepOptions.length - 1, closureStepOptions.findIndex((item) => item.key === closureStep) + 1)].key"
          >
            下一步
          </el-button>
          <el-button v-else type="primary" @click="projectClosureDialogVisible = false">完成</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="acceptanceDialogVisible" title="验收事项" width="920px">
      <div class="info-section">
        <div class="section-header-inline">
          <h4>{{ editingAcceptanceId ? '编辑验收事项' : '新增验收事项' }}</h4>
          <div class="dialog-summary-text">维护项目验收清单，记录每条交付内容是否已确认通过</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="名称">
            <el-input v-model="acceptanceForm.itemName" placeholder="例如 UAT 验收通过、部署清单已签收" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="acceptanceForm.itemType" style="width: 100%">
              <el-option v-for="option in acceptanceTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="acceptanceForm.status" style="width: 100%">
              <el-option v-for="option in acceptanceStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="负责人">
            <el-select v-model="acceptanceForm.ownerId" clearable filterable style="width: 100%" placeholder="可选">
              <el-option v-for="option in acceptanceOwnerOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="验收证明材料">
            <div class="upload-inline">
              <el-upload
                :auto-upload="false"
                :show-file-list="false"
                :limit="1"
                action="#"
                :on-change="handleAcceptanceAttachmentChange"
              >
                <el-button :loading="loading">选择文件</el-button>
              </el-upload>
              <el-button v-if="acceptanceForm.attachmentId" @click="clearAcceptanceAttachment">清空附件</el-button>
            </div>
            <div class="dialog-summary-text">
              {{ acceptanceForm.attachmentName || '尚未上传证明材料，可上传签字单、邮件截图、测试记录等文件' }}
            </div>
          </el-form-item>
          <el-form-item label="说明" class="scope-form-span">
            <el-input v-model="acceptanceForm.description" type="textarea" :rows="3" placeholder="记录验收标准、签收说明、确认人或结果备注" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetAcceptanceForm">重置</el-button>
          <el-button type="primary" :loading="loading" @click="saveAcceptanceItemAction">{{ editingAcceptanceId ? '保存修改' : '新增验收事项' }}</el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>验收事项清单</h4>
          <div class="dialog-summary-text">全部完成后，项目收尾检查中的验收事项会自动变为已满足</div>
        </div>
        <div v-if="acceptanceItems.length" class="simple-list">
          <div v-for="item in acceptanceItems" :key="`acceptance-${item.id}`" class="simple-list-item">
            <strong>{{ item.itemName || `验收事项${item.id}` }}</strong>
            <span>类型: {{ item.itemType || '-' }} | 状态: {{ formatAcceptanceStatus(item.status) }}</span>
            <span>负责人: {{ item.ownerName || item.ownerId || '未设置' }}</span>
            <span>完成时间: {{ item.acceptedAt ? String(item.acceptedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <span>{{ item.description || '暂无说明' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="populateAcceptanceForm(item)">编辑</el-button>
              <el-button v-if="item.attachmentId" size="small" @click="openAttachmentDownload(item.attachmentId)">证明材料</el-button>
              <el-button size="small" type="danger" plain @click="removeAcceptanceItemAction(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无验收事项" />
      </div>
    </el-dialog>

    <el-dialog v-model="changeRequestDialogVisible" title="变更管理" width="980px">
      <div class="info-section">
        <div class="section-header-inline">
          <h4>提交变更申请</h4>
          <div class="dialog-summary-text">用于记录范围、进度、成本和质量变更的申请与审批过程</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="标题">
            <el-input v-model="changeRequestForm.title" placeholder="例如 新增报表模块、延期两周上线" />
          </el-form-item>
          <el-form-item label="类型">
            <el-select v-model="changeRequestForm.type" style="width: 100%">
              <el-option v-for="option in changeTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级">
            <el-select v-model="changeRequestForm.priority" style="width: 100%">
              <el-option v-for="option in changePriorityOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="影响分析" class="scope-form-span">
            <el-input v-model="changeRequestForm.impactAnalysis" type="textarea" :rows="2" placeholder="说明对范围、工期、成本、质量、资源的影响" />
          </el-form-item>
          <el-form-item label="变更原因" class="scope-form-span">
            <el-input v-model="changeRequestForm.reason" type="textarea" :rows="3" placeholder="说明为什么要变更，当前问题是什么" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetChangeRequestForm">重置</el-button>
          <el-button type="primary" :loading="loading" @click="saveChangeRequestAction">提交申请</el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>审批操作</h4>
          <div class="report-generate-form">
            <el-select v-model="changeRequestFilterForm.status" clearable style="width: 160px" placeholder="状态">
              <el-option label="已提交" value="SUBMITTED" />
              <el-option label="评审中" value="UNDER_REVIEW" />
              <el-option label="已批准" value="APPROVED" />
              <el-option label="已驳回" value="REJECTED" />
            </el-select>
            <el-select v-model="changeRequestFilterForm.priority" clearable style="width: 160px" placeholder="优先级">
              <el-option v-for="option in changePriorityOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
            <el-button type="primary" :loading="loading" @click="loadChangeRequests">查询</el-button>
          </div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="申请单">
            <el-select v-model="changeApprovalForm.id" filterable clearable style="width: 100%" placeholder="请选择变更申请">
              <el-option
                v-for="item in changeRequests"
                :key="`change-approve-${item.id}`"
                :label="`${item.changeCode || '-'} | ${item.title || '未命名申请'}`"
                :value="String(item.id)"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="审批结果">
            <el-select v-model="changeApprovalForm.decision" style="width: 100%">
              <el-option v-for="option in changeDecisionOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="审批意见" class="scope-form-span">
            <el-input v-model="changeApprovalForm.comment" type="textarea" :rows="2" placeholder="记录同意或驳回理由" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button type="primary" :loading="loading" @click="approveChangeRequestAction">提交审批</el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>变更申请列表</h4>
          <div class="dialog-summary-text">点击“日志”可查看完整状态流转</div>
        </div>
        <div v-if="changeRequests.length" class="simple-list">
          <div v-for="item in changeRequests" :key="`change-${item.id}`" class="simple-list-item">
            <strong>{{ item.changeCode || '-' }} | {{ item.title || '未命名变更' }}</strong>
            <span>类型: {{ formatChangeType(item.changeType) }} | 优先级: {{ item.priority || '-' }}</span>
            <span>状态: {{ formatChangeStatus(item.status) }}</span>
            <span>申请人: {{ item.proposerName || item.proposerId || '-' }} | 审批人: {{ item.approverName || item.approverId || '-' }}</span>
            <span>提交时间: {{ item.submittedAt ? String(item.submittedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <span>{{ item.reason || '暂无原因' }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="loadChangeRequestLogs(item.id)">日志</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无变更申请" />
      </div>
      <div v-if="selectedChangeRequest" class="info-section">
        <div class="section-header-inline">
          <h4>变更详情</h4>
          <div class="dialog-summary-text">{{ selectedChangeRequest.changeCode || '-' }}</div>
        </div>
        <div class="simple-list">
          <div class="simple-list-item">
            <strong>{{ selectedChangeRequest.title || '-' }}</strong>
            <span>状态: {{ formatChangeStatus(selectedChangeRequest.status) }}</span>
            <span>影响分析: {{ selectedChangeRequest.impactAnalysis || '未填写' }}</span>
            <span>审批意见: {{ selectedChangeRequest.decisionComment || '未填写' }}</span>
            <span v-if="selectedChangeImpactHints.length">当前影响参考: {{ selectedChangeImpactHints.join(' | ') }}</span>
          </div>
        </div>
        <div v-if="selectedChangeRequest.status === 'APPROVED'" class="dialog-actions-inline">
          <el-button
            v-if="selectedChangeRequest.changeType === 'SCOPE'"
            :loading="loading"
            @click="createBaselineFromApprovedChange('SCOPE')"
          >
            生成范围基线
          </el-button>
          <el-button
            v-if="selectedChangeRequest.changeType === 'COST'"
            :loading="loading"
            @click="createBaselineFromApprovedChange('COST')"
          >
            生成成本基线
          </el-button>
        </div>
        <div class="section-header-inline">
          <h4>流转日志</h4>
        </div>
        <div v-if="changeRequestLogs.length" class="simple-list">
          <div v-for="item in changeRequestLogs" :key="`change-log-${item.id}`" class="simple-list-item">
            <strong>{{ item.action || '-' }}</strong>
            <span>{{ formatChangeStatus(item.fromStatus) }} -> {{ formatChangeStatus(item.toStatus) }}</span>
            <span>操作人: {{ item.operatorName || item.operatorId || '-' }}</span>
            <span>时间: {{ item.createdAt ? String(item.createdAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <span>{{ item.comment || '无备注' }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无流转日志" />
      </div>
    </el-dialog>

    <el-dialog v-model="reportDialogVisible" title="报表中心" width="860px">
      <div v-if="canManageProject" class="info-section">
        <h4>生成报表</h4>
        <div class="report-generate-form">
          <el-select v-model="reportGenerateForm.type" style="width: 160px">
            <el-option label="周报" value="WEEKLY" />
            <el-option label="月报" value="MONTHLY" />
          </el-select>
          <el-date-picker v-model="reportGenerateForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
          <el-date-picker v-model="reportGenerateForm.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
          <el-button type="primary" :loading="loading" @click="generateWeeklyReport">生成</el-button>
        </div>
      </div>
      <div class="info-section">
        <h4>已有报表</h4>
        <div v-if="reportList.length" class="simple-list">
          <div v-for="item in reportList" :key="`report-${item.id}`" class="simple-list-item">
            <strong>{{ item.title }}</strong>
            <span>{{ item.reportType }} | {{ item.status }} | {{ item.generatedAt }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无报表" />
      </div>
      <div class="info-section">
        <h4>工时报表</h4>
        <div class="dashboard-panels">
          <div class="dashboard-card"><span>总工时</span><strong>{{ timesheetReport?.totalHours ?? 0 }}</strong></div>
          <div class="dashboard-card"><span>参与人数</span><strong>{{ timesheetReport?.userCount ?? 0 }}</strong></div>
          <div class="dashboard-card"><span>记录数</span><strong>{{ timesheetReport?.records?.length ?? 0 }}</strong></div>
        </div>
      </div>
    </el-dialog>
  </section>
</template>

<style scoped>
.editor-page {
  height: 100vh;
  display: grid;
  grid-template-rows: auto 1fr;
  background: #fff;
  overflow: hidden;
}

.editor-toolbar {
  position: sticky;
  top: 0;
  z-index: 20;
  border-bottom: 1px solid #d6d6d6;
  background: #f3f3f3;
}

.toolbar-tabs {
  display: flex;
  align-items: stretch;
  border-bottom: 1px solid #d6d6d6;
}

.nav-back,
.save-shortcut,
.tab {
  border: 0;
  border-right: 1px solid #d6d6d6;
  background: #f3f3f3;
}

.nav-back {
  width: 52px;
  display: grid;
  place-items: center;
  color: #1f5fbf;
  font-size: 24px;
  cursor: pointer;
}

.nav-back:hover {
  background: #e8eefc;
}

.save-shortcut {
  width: 52px;
  display: grid;
  place-items: center;
  cursor: pointer;
}

.save-shortcut:disabled,
.tab:disabled,
.tool-action:disabled {
  cursor: default;
  opacity: 0.48;
}

.save-shortcut img {
  width: 24px;
  height: 24px;
}

.tab {
  min-width: 92px;
  height: 40px;
  color: #333;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
}

.tab.active {
  background: #1f5fbf;
  color: #fff;
}

.toolbar-state {
  margin-left: auto;
  display: flex;
  align-items: center;
  padding: 0 14px;
  color: #666;
  font-size: 13px;
}

.ribbon {
  display: flex;
  min-height: 96px;
  overflow-x: auto;
}

.ribbon-group {
  min-width: 196px;
  padding: 10px 14px 6px;
  border-right: 1px solid #d6d6d6;
  display: grid;
  gap: 10px;
  align-content: space-between;
}

.group-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tool-action {
  min-height: 30px;
  padding: 0 12px;
  border: 1px solid #c7c7c7;
  background: #fff;
  color: #333;
  font-size: 13px;
  cursor: pointer;
  transition: border-color 160ms ease, background 160ms ease, color 160ms ease;
}

.tool-action:hover {
  border-color: #9fb7d9;
  background: #f7faff;
}

.tool-action.active {
  border-color: #1f5fbf;
  background: #eaf1ff;
  color: #1f5fbf;
  font-weight: 700;
}

.tool-action-disabled:hover {
  border-color: #c7c7c7;
  background: #fff;
}

.tool-action-danger {
  border-color: #e3b2b2;
  background: #fff3f2;
  color: #b1372a;
}

.ribbon-group-danger {
  background: #faf7f7;
}

.group-title {
  text-align: center;
  color: #666;
  font-size: 13px;
}

.editor-content {
  min-height: 0;
  display: grid;
  grid-template-rows: auto 1fr;
  overflow: auto;
}

.schedule-banner {
  margin: 10px 12px 8px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border: 1px solid #b8b8b8;
  background: #fff;
  color: #444;
  font-size: 18px;
}

.gantt-shell {
  min-height: 0;
  display: grid;
  grid-template-columns: max-content minmax(0, 1fr);
  border-top: 1px solid #d6d6d6;
  overflow: hidden;
}

.task-grid {
  width: max-content;
  min-width: max-content;
  border-right: 1px solid #c7c7c7;
  overflow: hidden;
}

.grid-header,
.grid-row {
  display: grid;
  grid-template-columns: 44px 110px 110px 220px 90px 152px 152px;
}

.header-cell,
.grid-cell {
  min-width: 0;
  height: 38px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  border-right: 1px solid #ddd;
  border-bottom: 1px solid #e5e5e5;
  background: #fff;
  color: #333;
}

.header-cell {
  height: 52px;
  font-size: 14px;
  font-weight: 600;
}

.indicator {
  justify-content: center;
  padding: 0;
}

.wbs {
  justify-content: center;
  font-variant-numeric: tabular-nums;
}

.editable {
  position: relative;
  padding: 0;
  cursor: text;
}

.task-grid-readonly .editable {
  cursor: default;
}

.name.editable {
  padding-right: 28px;
}

.editable input {
  width: 100%;
  height: 100%;
  border: 0;
  outline: 0;
  padding: 0 10px;
  background: transparent;
  color: #2f2f2f;
  font-size: 14px;
}

.outline-toggle {
  position: absolute;
  top: 8px;
  width: 18px;
  height: 18px;
  border: 0;
  background: transparent;
  color: #4b4b4b;
  font-size: 12px;
  line-height: 18px;
  cursor: pointer;
  z-index: 1;
}

.editable.active {
  box-shadow: inset 0 0 0 2px #1f5fbf;
}

.parent-task-cell {
  background: #f4f8ff;
}

.parent-task-row .indicator,
.parent-task-row .mode input {
  color: #1f5fbf;
  font-weight: 700;
}

.parent-task-input {
  font-weight: 700;
  color: #173f82;
}

.parent-task-row .outline-toggle {
  color: #1f5fbf;
}

.summary-input {
  font-weight: 700;
}

.milestone-input {
  color: #1f5fbf;
}

.editable :deep(.date-editor) {
  width: 100%;
}

.editable :deep(.el-input__wrapper) {
  min-height: 38px;
  border-radius: 0;
  box-shadow: none;
  background: transparent;
}

.timeline-panel {
  min-width: 0;
  overflow-x: auto;
  overflow-y: hidden;
}

.timeline-header {
  display: flex;
  border-bottom: 1px solid #d6d6d6;
}

.week-block {
  flex: 0 0 auto;
  border-right: 1px solid #ddd;
  background: #fff;
}

.week-label {
  height: 28px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  color: #595959;
  font-size: 13px;
}

.week-days {
  display: grid;
  border-top: 1px solid #e7e7e7;
}

.week-days span {
  height: 28px;
  display: grid;
  place-items: center;
  border-right: 1px solid #efefef;
  color: #555;
  font-size: 13px;
}

.timeline-body {
  position: relative;
  min-width: max-content;
}

.gantt-dependency-layer {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
  pointer-events: none;
  overflow: visible;
}

.gantt-dependency-path {
  fill: none;
  stroke: #4f6fa8;
  stroke-width: 1.5;
  stroke-linejoin: round;
  stroke-linecap: round;
  opacity: 0.95;
}

.timeline-row {
  position: relative;
  display: grid;
  min-width: max-content;
}

.timeline-row.active .timeline-cell {
  background: #f4f8ff;
}

.timeline-cell {
  width: 32px;
  height: 38px;
  border-right: 1px solid #efefef;
  border-bottom: 1px solid #e5e5e5;
  background: #fff;
}

.timeline-cell.weekend {
  background: var(--gantt-weekend-color);
}

.timeline-cell.disabled {
  background: #ececec;
}

.task-bar {
  position: absolute;
  top: 8px;
  height: 22px;
  border: 1px solid var(--gantt-main-border);
  background: var(--gantt-main-color);
  color: var(--gantt-main-border);
  overflow: hidden;
  z-index: 3;
  cursor: pointer;
}

.risk-badge {
  position: absolute;
  right: 6px;
  top: 9px;
  width: 16px;
  height: 16px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  z-index: 4;
}

.gantt-risk-badge {
  top: -6px;
  right: -6px;
}

.risk-badge-low {
  background: #4caf50;
}

.risk-badge-medium {
  background: #f0ad4e;
}

.risk-badge-high {
  background: #f57c00;
}

.risk-badge-critical {
  background: #d64545;
}

.task-progress-fill {
  position: absolute;
  inset: 0 auto 0 0;
  height: 100%;
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.42), rgba(255, 255, 255, 0.18));
  border-right: 1px solid rgba(255, 255, 255, 0.7);
  pointer-events: none;
}

.main-bar {
  top: 8px;
  height: 22px;
}

.child-bar {
  top: 11px;
  height: 16px;
}

.parent-bar {
  top: 5px;
  height: 26px;
  background: transparent !important;
  border-top-width: 3px;
  border-right-width: 3px;
  border-bottom-width: 0;
  border-left-width: 3px;
  box-shadow: none;
}

.parent-bar .task-progress-fill {
  display: none;
}

.task-bar::before,
.task-bar::after {
  content: '';
  position: absolute;
  box-sizing: border-box;
  pointer-events: none;
}

.bar-variant-main-root {
  top: 8px;
  height: 20px;
  box-shadow: inset 0 -3px 0 rgba(255, 255, 255, 0.2);
}

.bar-variant-main-root::before {
  left: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: currentColor;
  opacity: 0.26;
}

.bar-variant-main-root::after {
  top: 4px;
  right: 3px;
  bottom: 4px;
  width: 2px;
  background: currentColor;
  opacity: 0.35;
}

.bar-variant-child-level-1 {
  top: 11px;
  height: 16px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.26);
}

.bar-variant-child-level-1::before {
  left: 3px;
  top: 2px;
  bottom: 2px;
  width: 4px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.45);
}

.bar-variant-child-level-1::after {
  top: 4px;
  right: 4px;
  width: 6px;
  height: 6px;
  border-top: 2px solid currentColor;
  border-right: 2px solid currentColor;
  border-radius: 0 4px 0 0;
  opacity: 0.32;
}

.bar-variant-child-deep {
  top: 13px;
  height: 12px;
  border-style: dashed;
  box-shadow: inset 0 0 0 999px rgba(255, 255, 255, 0.06);
}

.bar-variant-child-deep::before {
  left: 3px;
  right: 8px;
  top: 2px;
  bottom: 2px;
  border-radius: 999px;
  background: repeating-linear-gradient(135deg, rgba(255, 255, 255, 0.34) 0 6px, transparent 6px 12px);
}

.bar-variant-child-deep::after {
  top: 1px;
  right: 0;
  bottom: 1px;
  width: 5px;
  border-left: 1px solid rgba(255, 255, 255, 0.42);
  border-right: 2px solid currentColor;
  opacity: 0.5;
}

.bar-variant-parent-major {
  top: 4px;
  height: 28px;
  border-width: 3px 3px 0;
  border-style: solid;
  box-shadow: none;
}

.bar-variant-parent-major::before {
  left: 2px;
  top: 0;
  width: 10px;
  height: 10px;
  border-top: 3px solid currentColor;
  border-left: 3px solid currentColor;
  background: transparent;
  opacity: 0.18;
}

.bar-variant-parent-major::after {
  top: 0;
  right: 2px;
  width: 10px;
  height: 10px;
  border-top: 3px solid currentColor;
  border-right: 3px solid currentColor;
  background: transparent;
  opacity: 0.18;
}

.bar-variant-parent-nested {
  top: 7px;
  height: 22px;
  border-width: 2px 2px 0;
  border-style: solid;
  box-shadow: none;
}

.bar-variant-parent-nested::before {
  left: 2px;
  top: 0;
  width: 8px;
  height: 8px;
  border-top: 2px solid currentColor;
  border-left: 2px solid currentColor;
  background: transparent;
  opacity: 0.16;
}

.bar-variant-parent-nested::after {
  top: 0;
  right: 2px;
  width: 8px;
  height: 8px;
  border-top: 2px solid currentColor;
  border-right: 2px solid currentColor;
  background: transparent;
  opacity: 0.16;
}

.task-shape-rounded {
  border-radius: 6px;
}

.task-shape-square {
  border-radius: 0;
}

.task-shape-capsule {
  border-radius: 999px;
}

.summary-bar {
  top: 12px;
  height: 10px;
  border-color: var(--gantt-summary-border);
  background: color-mix(in srgb, var(--gantt-summary-color) 78%, white);
}

.summary-shape-bracket {
  border-radius: 0;
}

.summary-shape-pill {
  top: 8px;
  height: 18px;
  border-radius: 999px;
}

.summary-shape-block {
  top: 9px;
  height: 16px;
  border-radius: 4px;
}

.summary-cap {
  position: absolute;
  top: -1px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
}

.summary-cap-left {
  left: -1px;
  border-left: 0;
  border-right: 8px solid var(--gantt-summary-border);
}

.summary-cap-right {
  right: -1px;
  border-right: 0;
  border-left: 8px solid var(--gantt-summary-border);
}

.milestone-marker {
  position: absolute;
  top: 11px;
  width: 16px;
  height: 16px;
  border: 1px solid var(--gantt-milestone-border);
  background: var(--gantt-milestone-color);
  transform-origin: center;
  z-index: 3;
  cursor: pointer;
}

.milestone-shape-diamond {
  transform: rotate(45deg);
}

.milestone-shape-square {
  transform: none;
  border-radius: 3px;
}

.milestone-shape-circle {
  top: 12px;
  width: 14px;
  height: 14px;
  border-radius: 999px;
  transform: none;
}

.gantt-style-layout {
  display: grid;
  grid-template-columns: 1.15fr 0.85fr;
  gap: 18px;
}

.gantt-style-list,
.gantt-style-editor {
  border: 1px solid #d7d7d7;
  background: #fff;
}

.style-list-toolbar,
.style-list-header,
.style-list-row,
.style-editor-tabs,
.style-editor-panels,
.style-preview-board {
  padding: 12px 14px;
}

.style-list-toolbar {
  display: flex;
  gap: 8px;
  border-bottom: 1px solid #e3e3e3;
}

.style-list-header,
.style-list-row {
  display: grid;
  grid-template-columns: 1.1fr 0.8fr 1fr;
  align-items: center;
  gap: 12px;
}

.style-list-header {
  color: #666;
  font-size: 13px;
  font-weight: 700;
  border-bottom: 1px solid #ececec;
}

.style-list-row {
  width: 100%;
  border: 0;
  border-bottom: 1px solid #f0f0f0;
  background: #fff;
  text-align: left;
  cursor: pointer;
}

.style-list-row.active {
  background: #edf4ff;
  box-shadow: inset 3px 0 0 #1f5fbf;
}

.style-preview-cell,
.style-preview-track {
  position: relative;
  min-height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
}

.dialog-preview-bar {
  position: relative;
  display: inline-flex;
  align-items: center;
  width: 84px;
  height: 16px;
  border: 1px solid #444;
  flex: 0 0 auto;
}

.dialog-preview-milestone {
  position: relative;
  display: inline-block;
  top: auto;
  left: auto;
  flex: 0 0 auto;
  transform-origin: center;
}

.preview-large {
  width: 160px;
}

.style-editor-tabs {
  display: flex;
  gap: 18px;
  border-bottom: 1px solid #e3e3e3;
}

.style-editor-tabs span {
  color: #666;
}

.style-editor-tabs .active {
  color: #1f5fbf;
  font-weight: 700;
}

.style-editor-panels {
  display: grid;
  gap: 16px;
}

.style-editor-panel {
  display: grid;
  gap: 10px;
}

.style-editor-panel h4,
.style-preview-board > span {
  margin: 0;
  color: #4a4a4a;
  font-size: 14px;
  font-weight: 700;
}

.color-input {
  width: 72px;
  height: 38px;
  padding: 0;
  border: 1px solid #d0d0d0;
  background: #fff;
}

.color-presets {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.color-chip {
  width: 28px;
  height: 28px;
  border: 1px solid rgba(0, 0, 0, 0.16);
  cursor: pointer;
}

.color-edit-row {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 10px;
  align-items: center;
}

.color-palette-grid {
  display: grid;
  grid-template-columns: repeat(9, 28px);
  gap: 8px;
}

.palette-chip {
  border-radius: 6px;
}

.style-preview-board {
  border-top: 1px solid #ececec;
  display: grid;
  gap: 12px;
}

.style-preview-track {
  min-height: 74px;
  border: 1px dashed #d8d8d8;
  background: linear-gradient(180deg, #fafafa, #f3f3f3);
  justify-content: center;
}

.end-marker {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 0;
  border-left: 2px solid #1f5fbf;
  z-index: 2;
  pointer-events: none;
}

.end-marker span {
  position: absolute;
  top: 6px;
  left: 6px;
  padding: 2px 6px;
  background: #1f5fbf;
  color: #fff;
  font-size: 12px;
  white-space: nowrap;
}

.dashboard-panels {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.dashboard-card {
  display: grid;
  gap: 8px;
  padding: 16px;
  border: 1px solid #ddd;
  background: #fff;
}

.dashboard-card span {
  color: #666;
  font-size: 13px;
}

.dashboard-card strong {
  color: #303030;
  font-size: 1.2rem;
}

.info-section {
  margin-bottom: 18px;
}

.info-section h4 {
  margin: 0 0 10px;
  color: #303030;
  font-size: 15px;
}

.simple-list {
  display: grid;
  gap: 10px;
}

.simple-list-item {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  border: 1px solid #ddd;
  background: #fff;
}

.simple-list-item span {
  color: #666;
  font-size: 13px;
}

.baseline-actions {
  display: flex;
  justify-content: flex-start;
}

.baseline-snapshot-card {
  display: grid;
  gap: 12px;
  padding: 12px;
  border: 1px solid #e4ebf7;
  background: #f8fbff;
}

.baseline-snapshot-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  color: #4d5b73;
  font-size: 13px;
}

.wbs-tree {
  display: grid;
  gap: 8px;
}

.wbs-tree-node {
  display: grid;
  gap: 6px;
}

.wbs-tree-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 38px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  background: #fff;
}

.wbs-tree-row strong {
  min-width: 96px;
  color: #1f5fbf;
  font-variant-numeric: tabular-nums;
}

.wbs-tree-row span:last-child {
  color: #333;
}

.wbs-tree-toggle,
.wbs-tree-spacer {
  width: 18px;
  height: 18px;
  flex: 0 0 18px;
}

.wbs-tree-toggle {
  border: 1px solid #cfd8ea;
  background: #f4f8ff;
  color: #1f5fbf;
  line-height: 16px;
  cursor: pointer;
}

.scope-form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 12px;
}

.scope-form-span {
  grid-column: 1 / -1;
}

.section-header-inline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.section-header-inline h4 {
  margin: 0;
}

.task-detail-section-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin: 0 0 14px;
}

.task-detail-tab {
  min-width: 96px;
  height: 34px;
  padding: 0 14px;
  border: 1px solid #cfd7e6;
  background: #fff;
  color: #41526d;
  cursor: pointer;
  transition: all 0.15s ease;
}

.task-detail-tab.active {
  border-color: #1f5fbf;
  background: #eaf1ff;
  color: #1f5fbf;
  font-weight: 700;
}

.reply-target-banner {
  margin-bottom: 10px;
  padding: 10px 12px;
  border: 1px solid #d8e4ff;
  background: #f4f8ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: #3f4f6d;
}

.comment-thread-list {
  display: grid;
  gap: 10px;
}

.comment-thread-item {
  display: grid;
  gap: 8px;
}

.comment-replies {
  margin-left: 28px;
  display: grid;
  gap: 8px;
}

.comment-reply-item {
  border-left: 3px solid #dbe4f6;
}

.risk-matrix-board {
  display: grid;
  grid-template-columns: 96px repeat(5, minmax(0, 1fr));
  border: 1px solid #d9dfeb;
}

.risk-matrix-corner,
.risk-matrix-header {
  min-height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-right: 1px solid #d9dfeb;
  border-bottom: 1px solid #d9dfeb;
  background: #f6f8fc;
  color: #40506b;
  font-weight: 700;
}

.risk-matrix-cell {
  min-height: 110px;
  padding: 10px;
  border-right: 1px solid #d9dfeb;
  border-bottom: 1px solid #d9dfeb;
  display: grid;
  align-content: start;
  gap: 8px;
}

.risk-matrix-low {
  background: #edf8ef;
}

.risk-matrix-medium {
  background: #fff9e6;
}

.risk-matrix-high {
  background: #fff0dd;
}

.risk-matrix-critical {
  background: #fde9e7;
}

.risk-matrix-count {
  font-size: 22px;
  font-weight: 700;
  color: #21324f;
}

.risk-matrix-names {
  display: grid;
  gap: 4px;
  font-size: 12px;
  color: #31415d;
}

.import-template-panel {
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px solid #d9dfeb;
  background: #f8fbff;
  display: grid;
  gap: 8px;
}

.template-notes {
  display: grid;
  gap: 4px;
  color: #40506b;
  font-size: 13px;
}

.import-result-panel {
  margin-top: 12px;
  padding: 12px 14px;
  border: 1px solid #d9dfeb;
  background: #fff;
  display: grid;
  gap: 12px;
}

.import-error-item {
  border-left: 3px solid #d64545;
}

.dialog-summary-text {
  color: #4d5b73;
  font-size: 13px;
}

.member-inline-tag {
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
  padding: 0 6px;
  min-height: 18px;
  background: #eef2f7;
  color: #556274;
  font-size: 12px;
  font-weight: 500;
}

.upload-inline {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.task-grid-readonly .editable input {
  background: #f7f9fc;
  color: #5a6780;
}

.task-grid-readonly .editable .el-input__wrapper,
.task-grid-readonly .editable .el-textarea__inner,
.task-grid-readonly .editable .el-date-editor.el-input {
  background: #f7f9fc;
}

.closure-steps {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
  margin-bottom: 16px;
}

.closure-step {
  border: 1px solid #cfd7e6;
  background: #f7f9fc;
  color: #4d5b73;
  height: 40px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
}

.closure-step.active {
  border-color: #1f5fbf;
  background: #1f5fbf;
  color: #fff;
}

.closure-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.dialog-actions-inline {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-bottom: 10px;
}

.report-generate-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}
</style>
