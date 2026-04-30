<script setup>
import { computed, defineAsyncComponent, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'

import saveIcon from '@/assets/save-icon.svg'
import {
  createProjectArchive,
  createProjectTimesheet,
  createProjectLessonLearned,
  deleteProjectAttachment,
  deleteProjectArchive,
  deleteProjectLessonLearned,
  deleteProjectReport,
  deleteProjectTimesheet,
  generateProjectReport,
  generateProjectSummaryReport,
  getProjectArchives,
  getProjectCalendar,
  getProjectClosureCheck,
  getProjectLessonsLearned,
  getProjectCriticalPath,
  getProjectEditorPreferences,
  getProjectReports,
  getProjectScheduleAlerts,
  getProjectTimesheets,
  getProjectTimesheetReport,
  saveProjectEditorPreferences,
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
  getProjectMemberCandidates,
  getProjectCharter,
  getProjectDashboard,
  getProjectDetail,
  removeProjectMember,
  saveProjectCharter,
  updateProjectMember,
  updateProject,
} from '@/api/projects'
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
  createProjectQualityActivity,
  createProjectQualityMetric,
  createProjectQualityPlan,
  deleteProjectQualityActivity,
  deleteProjectQualityMetric,
  deleteProjectQualityPlan,
  getProjectQualityActivities,
  getProjectQualityMetrics,
  getProjectQualityPlans,
  updateProjectQualityActivity,
  updateProjectQualityMetric,
  updateProjectQualityPlan,
} from '@/api/quality'
import {
  createProjectDefect,
  createProjectTestCase,
  createProjectTestPlan,
  deleteProjectDefect,
  deleteProjectTestCase,
  deleteProjectTestPlan,
  deleteProjectTestReport,
  generateProjectTestReport,
  getProjectDefects,
  getProjectTestCases,
  getProjectTestPlans,
  getProjectTestReports,
  updateProjectDefect,
  updateProjectTestCase,
  updateProjectTestPlan,
} from '@/api/test'
import {
  createProjectCommunicationMatrix,
  createProjectCommunicationRecord,
  createProjectMeeting,
  deleteProjectCommunicationMatrix,
  deleteProjectCommunicationRecord,
  deleteProjectMeeting,
  getProjectCommunicationMatrix,
  getProjectCommunicationRecords,
  getProjectMeetings,
  updateProjectCommunicationMatrix,
  updateProjectCommunicationRecord,
  updateProjectMeeting,
} from '@/api/communication'
import {
  createProjectContract,
  createProjectProcurement,
  createProjectSupplier,
  deleteProjectContract,
  deleteProjectProcurement,
  deleteProjectSupplier,
  getProjectContracts,
  getProjectProcurements,
  getProjectSuppliers,
  updateProjectContract,
  updateProjectProcurement,
  updateProjectProcurementStatus,
  updateProjectSupplier,
} from '@/api/procurement'
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
import {
  createProjectBaseline,
  createProjectConfigItem,
  deleteProjectBaseline,
  deleteProjectConfigItem,
  getProjectBaselines,
  getProjectConfigItems,
  restoreProjectBaseline,
  updateProjectConfigItem,
} from '@/api/configuration'
import { API_BASE_URL } from '@/api/http'
import UserAvatar from '@/components/ui/UserAvatar.vue'
import { useAuthStore } from '@/stores/auth'
import { recordRecentProject } from '@/utils/recentProjects'

const ProjectTaskInfoDialog = defineAsyncComponent(() => import('./components/ProjectTaskInfoDialog.vue'))
const ProjectPendingReviewDialog = defineAsyncComponent(() => import('./components/ProjectPendingReviewDialog.vue'))
const ProjectMilestoneDialog = defineAsyncComponent(() => import('./components/ProjectMilestoneDialog.vue'))
const ProjectRiskDialog = defineAsyncComponent(() => import('./components/ProjectRiskDialog.vue'))
const ProjectQualityPlanDialog = defineAsyncComponent(() => import('./components/ProjectQualityPlanDialog.vue'))
const ProjectQualityActivityDialog = defineAsyncComponent(() => import('./components/ProjectQualityActivityDialog.vue'))
const ProjectQualityMetricDialog = defineAsyncComponent(() => import('./components/ProjectQualityMetricDialog.vue'))
const ProjectTestPlanDialog = defineAsyncComponent(() => import('./components/ProjectTestPlanDialog.vue'))
const ProjectTestCaseDialog = defineAsyncComponent(() => import('./components/ProjectTestCaseDialog.vue'))
const ProjectDefectDialog = defineAsyncComponent(() => import('./components/ProjectDefectDialog.vue'))
const ProjectTestReportDialog = defineAsyncComponent(() => import('./components/ProjectTestReportDialog.vue'))
const ProjectRiskMatrixDialog = defineAsyncComponent(() => import('./components/ProjectRiskMatrixDialog.vue'))
const ProjectCommunicationMatrixDialog = defineAsyncComponent(() => import('./components/ProjectCommunicationMatrixDialog.vue'))
const ProjectWbsDialog = defineAsyncComponent(() => import('./components/ProjectWbsDialog.vue'))

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const DAY_WIDTH = 32
const MIN_ROW_COUNT = 18
const DAY_NAMES = ['日', '一', '二', '三', '四', '五', '六']
const EDITABLE_FIELDS = ['mode', 'name', 'duration', 'start', 'finish']
const GANTT_APPEARANCE_STORAGE_KEY = 'pm_global_gantt_appearance'
const WBS_CONFIG_STORAGE_PREFIX = 'pm_project_wbs_config_'
const SCHEDULE_OPTIONS_STORAGE_PREFIX = 'pm_project_schedule_options_'
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
const pendingReviewTasks = ref([])
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
const selectedReportPreview = ref(null)
const reportCriticalTimelineRef = ref(null)
const projectTaskDependencies = ref([])

const infoDialogVisible = ref(false)
const charterDialogVisible = ref(false)
const dashboardDialogVisible = ref(false)
const projectMembersDialogVisible = ref(false)
const scheduleDialogVisible = ref(false)
const pendingReviewDialogVisible = ref(false)
const taskInfoDialogVisible = ref(false)
const milestoneDialogVisible = ref(false)
const riskDialogVisible = ref(false)
const riskMatrixDialogVisible = ref(false)
const qualityPlanDialogVisible = ref(false)
const qualityActivityDialogVisible = ref(false)
const qualityMetricDialogVisible = ref(false)
const testPlanDialogVisible = ref(false)
const testCaseDialogVisible = ref(false)
const defectDialogVisible = ref(false)
const testReportDialogVisible = ref(false)
const communicationMatrixDialogVisible = ref(false)
const meetingDialogVisible = ref(false)
const communicationRecordDialogVisible = ref(false)
const configurationDialogVisible = ref(false)
const procurementDialogVisible = ref(false)
const exportDialogVisible = ref(false)
const importDialogVisible = ref(false)
const globalSearchDialogVisible = ref(false)
const auditLogDialogVisible = ref(false)
const reportDialogVisible = ref(false)
const reportPreviewDialogVisible = ref(false)
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
let scheduleOptionsSnapshot = null
let syncingTaskProgressForm = false
const wbsTreeExpandedKeys = ref({})
const activeScopeBaselineId = ref(null)
const activeConfigBaselineId = ref(null)
const scopeBaselineTreeExpandedKeys = ref({})
const configurationDialogMode = ref('management')
const configurationBaselineTypeFilter = ref('')
const taskDetailLoading = ref(false)
const pendingReviewLoading = ref(false)
const taskDetail = ref(null)
const pendingReviewActingTaskId = ref('')
const pendingReviewActingAction = ref('')
const milestoneLoading = ref(false)
const milestoneList = ref([])
const riskLoading = ref(false)
const riskList = ref([])
const riskMatrix = ref({ levels: [], highCount: 0, criticalCount: 0 })
const qualityLoading = ref(false)
const testingLoading = ref(false)
const communicationLoading = ref(false)
const configurationLoading = ref(false)
const procurementLoading = ref(false)
const opsLoading = ref(false)
const exportTasks = ref([])
const importResult = ref(null)
const globalSearchResults = ref([])
const auditLogs = ref([])
const taskDetailSection = ref('basic')
const pendingRouteTaskId = ref('')
const editorContextMenuRef = ref(null)
const editorContextSubmenuRef = ref(null)
const contextMenuPosition = reactive({ x: 0, y: 0 })
const contextMenuState = reactive({
  visible: false,
  title: '项目快捷操作',
  subtitle: '右键打开常用入口',
  area: 'workspace',
  targetRowId: null,
  activeGroupTitle: '',
  submenuTop: 0,
  submenuDirection: 'right',
  groups: [],
})
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
  projectRole: 'READ_ONLY',
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
  deadlineDate: '',
  constraintType: '',
  constraintDate: '',
})

const taskDependencyForm = reactive({
  predecessorTaskId: '',
  dependencyType: 'FS',
  lagDays: 0,
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
const editingMilestoneTaskLocalId = ref(null)
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

const testReportGenerateForm = reactive({
  type: 'TEST',
  startDate: '',
  endDate: '',
})

const wbsNodes = ref([])
const requirements = ref([])
const qualityPlans = ref([])
const qualityActivities = ref([])
const qualityMetrics = ref([])
const testPlans = ref([])
const testCases = ref([])
const defectList = ref([])
const testReportList = ref([])
const communicationMatrixList = ref([])
const meetingList = ref([])
const communicationRecordList = ref([])
const communicationRecordFilterMeetingId = ref('')
const configItemList = ref([])
const baselineRecordList = ref([])
const supplierList = ref([])
const procurementList = ref([])
const contractList = ref([])
const scopeBaselines = ref([])
const costPlans = ref([])
const costActuals = ref([])
const costBaselines = ref([])
const evmMetrics = ref(null)
const editingCostPlanId = ref(null)
const editingQualityPlanId = ref(null)
const editingQualityActivityId = ref(null)
const editingQualityMetricId = ref(null)
const editingTestPlanId = ref(null)
const editingTestCaseId = ref(null)
const editingDefectId = ref(null)
const editingCommunicationMatrixId = ref(null)
const editingMeetingId = ref(null)
const editingCommunicationRecordId = ref(null)
const editingConfigItemId = ref(null)
const editingSupplierId = ref(null)
const editingProcurementId = ref(null)
const editingContractId = ref(null)
const draftCostPlans = ref([])
const configurationTab = ref('items')
const procurementTab = ref('suppliers')

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

const qualityPlanForm = reactive({
  title: '',
  qualityStandard: '',
  acceptanceRule: '',
  ownerId: '',
  status: 'DRAFT',
})

const qualityActivityForm = reactive({
  qualityPlanId: '',
  activityName: '',
  activityType: 'REVIEW',
  plannedDate: '',
  actualDate: '',
  result: '',
  ownerId: '',
})

const qualityMetricForm = reactive({
  metricName: '',
  metricValue: '',
  metricUnit: '',
  statisticDate: '',
})

const testPlanForm = reactive({
  title: '',
  versionNo: '',
  scopeDesc: '',
  ownerId: '',
  status: 'DRAFT',
})

const testCaseForm = reactive({
  testPlanId: '',
  requirementId: '',
  taskId: '',
  title: '',
  precondition: '',
  steps: '',
  expectedResult: '',
  actualResult: '',
  executionStatus: 'NOT_RUN',
  testerId: '',
  executedAt: '',
})

const defectForm = reactive({
  testCaseId: '',
  requirementId: '',
  taskId: '',
  title: '',
  severity: 'MEDIUM',
  priority: 'MEDIUM',
  status: 'NEW',
  reporterId: '',
  assigneeId: '',
  description: '',
  resolution: '',
})

const communicationMatrixForm = reactive({
  senderRole: '',
  receiverRole: '',
  channel: 'MEETING',
  frequency: '',
  topic: '',
})

const meetingForm = reactive({
  meetingType: 'DAILY',
  title: '',
  scheduledAt: '',
  durationMinutes: '30',
  hostId: '',
  location: '',
  status: 'PLANNED',
})

const communicationRecordForm = reactive({
  meetingId: '',
  recordType: 'MEETING_MINUTES',
  title: '',
  content: '',
  recorderId: '',
})

const configItemForm = reactive({
  itemName: '',
  itemType: 'DOC',
  versionNo: '',
  status: 'DRAFT',
  repositoryUrl: '',
  remark: '',
})

const baselineForm = reactive({
  baselineType: 'CONFIG',
  baselineName: '',
  description: '',
})

const supplierForm = reactive({
  supplierName: '',
  contactName: '',
  contactPhone: '',
  contactEmail: '',
  address: '',
  remark: '',
})

const procurementForm = reactive({
  supplierId: '',
  costPlanId: '',
  itemName: '',
  quantity: 1,
  unitPrice: '',
  status: 'PLANNED',
  expectedDeliveryDate: '',
  actualDeliveryDate: '',
})

const procurementStatusForm = reactive({
  procurementId: '',
  status: 'PLANNED',
  actualDeliveryDate: '',
})

const contractForm = reactive({
  supplierId: '',
  contractNo: '',
  contractName: '',
  contractType: 'FIXED_PRICE',
  signDate: '',
  totalAmount: '',
  deliverables: '',
  paymentTerms: '',
  status: 'DRAFT',
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

function createDefaultScheduleOptions() {
  return {
    scheduleMode: 'CALENDAR_DAY',
    holidayDates: [],
  }
}

const scheduleOptions = reactive(createDefaultScheduleOptions())

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

const scheduleModeOptions = [
  { label: '自然日', value: 'CALENDAR_DAY' },
  { label: '工作日', value: 'WORKDAY' },
]

const currentScheduleModeLabel = computed(
  () => scheduleModeOptions.find((item) => item.value === scheduleOptions.scheduleMode)?.label || '自然日',
)

const currentScheduleModeDescription = computed(() => (
  scheduleOptions.scheduleMode === 'WORKDAY'
    ? '工作日模式下，任务工期会跳过周六/周日，并额外排除你配置的节假日；非工作日会顺延到下一个工作日。'
    : '自然日模式下，任务工期按连续日历日计算；额外配置的节假日会保留，但只在工作日模式下生效。'
))

const configuredHolidayDateSet = computed(() => new Set(normalizeScheduleHolidayDates(scheduleOptions.holidayDates)))

const scheduleHolidaySummary = computed(() => {
  const holidayDates = normalizeScheduleHolidayDates(scheduleOptions.holidayDates)
  if (!holidayDates.length) {
    return scheduleOptions.scheduleMode === 'WORKDAY'
      ? '周六/周日会自动排除，当前未额外配置法定假日。'
      : '当前未配置额外非工作日；切换到工作日模式后，系统会自动排除周六/周日。'
  }
  const preview = holidayDates.slice(0, 4).join('、')
  return `已额外配置 ${holidayDates.length} 个非工作日${preview ? `：${preview}${holidayDates.length > 4 ? ' 等' : ''}` : ''}。`
})

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

const qualityPlanStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '执行中', value: 'ACTIVE' },
  { label: '已关闭', value: 'CLOSED' },
]

const qualityActivityTypeOptions = [
  { label: '评审', value: 'REVIEW' },
  { label: '测试', value: 'TEST' },
  { label: '审计', value: 'AUDIT' },
]

const testPlanStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '执行中', value: 'ACTIVE' },
  { label: '已关闭', value: 'CLOSED' },
]

const testCaseStatusOptions = [
  { label: '未执行', value: 'NOT_RUN' },
  { label: '通过', value: 'PASSED' },
  { label: '失败', value: 'FAILED' },
  { label: '阻塞', value: 'BLOCKED' },
]

const defectStatusOptions = [
  { label: '新建', value: 'NEW' },
  { label: '处理中', value: 'PROCESSING' },
  { label: '已修复', value: 'FIXED' },
  { label: '已关闭', value: 'CLOSED' },
]

const defectSeverityOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' },
  { label: '严重', value: 'CRITICAL' },
]

const defectPriorityOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高', value: 'HIGH' },
]

const communicationChannelOptions = [
  { label: '会议', value: 'MEETING' },
  { label: '邮件', value: 'EMAIL' },
  { label: '系统', value: 'SYSTEM' },
  { label: '即时通讯', value: 'CHAT' },
]

const meetingTypeOptions = [
  { label: '站会', value: 'DAILY' },
  { label: '周会', value: 'WEEKLY' },
  { label: '评审会', value: 'REVIEW' },
  { label: '其他', value: 'OTHER' },
]

const meetingStatusOptions = [
  { label: '待召开', value: 'PLANNED' },
  { label: '已完成', value: 'FINISHED' },
  { label: '已取消', value: 'CANCELED' },
]

const communicationRecordTypeOptions = [
  { label: '会议纪要', value: 'MEETING_MINUTES' },
  { label: '消息记录', value: 'MESSAGE' },
  { label: '通知公告', value: 'NOTICE' },
]

const configItemTypeOptions = [
  { label: '文档', value: 'DOC' },
  { label: '代码', value: 'CODE' },
  { label: '环境', value: 'ENV' },
  { label: '数据库', value: 'DB' },
  { label: '其他', value: 'OTHER' },
]

const configItemStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已纳入基线', value: 'BASELINED' },
  { label: '已变更', value: 'CHANGED' },
  { label: '已归档', value: 'ARCHIVED' },
]

const baselineTypeOptions = [
  { label: '范围基线', value: 'SCOPE' },
  { label: '进度基线', value: 'SCHEDULE' },
  { label: '成本基线', value: 'COST' },
  { label: '配置基线', value: 'CONFIG' },
]

const baselineStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已发布', value: 'PUBLISHED' },
  { label: '已作废', value: 'VOID' },
]

const taskConstraintTypeOptions = [
  { label: '无约束', value: '' },
  { label: '尽早开始 ASAP', value: 'ASAP' },
  { label: '尽晚完成 ALAP', value: 'ALAP' },
  { label: '开始不早于 SNET', value: 'SNET' },
  { label: '开始不晚于 SNLT', value: 'SNLT' },
  { label: '完成不早于 FNET', value: 'FNET' },
  { label: '完成不晚于 FNLT', value: 'FNLT' },
  { label: '必须开始于 MSO', value: 'MSO' },
  { label: '必须完成于 MFO', value: 'MFO' },
]

const isConfigBaselineDialogMode = computed(() => configurationDialogMode.value === 'config-baselines')
const configurationDialogTitle = computed(() => (
  isConfigBaselineDialogMode.value ? '配置基线' : '配置管理'
))
const displayedBaselineRecords = computed(() => {
  if (!configurationBaselineTypeFilter.value) {
    return baselineRecordList.value
  }
  return baselineRecordList.value.filter((item) => item?.baselineType === configurationBaselineTypeFilter.value)
})

const procurementStatusOptions = [
  { label: '规划中', value: 'PLANNED' },
  { label: '已下单', value: 'ORDERED' },
  { label: '已交付', value: 'DELIVERED' },
  { label: '已验收', value: 'ACCEPTED' },
]

const contractTypeOptions = [
  { label: '固定总价', value: 'FIXED_PRICE' },
  { label: '工时材料', value: 'TIME_MATERIAL' },
  { label: '其他', value: 'OTHER' },
]

const contractStatusOptions = [
  { label: '草稿', value: 'DRAFT' },
  { label: '已签署', value: 'SIGNED' },
  { label: '履约中', value: 'IN_PROGRESS' },
  { label: '已完成', value: 'COMPLETED' },
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
  { label: '可编辑成员', value: 'TEAM_MEMBER' },
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

const auditModuleOptions = [
  { label: '全部模块', value: '' },
  { label: '编辑器偏好', value: 'EDITOR_PREFERENCE' },
  { label: '项目成员', value: 'PROJECT_MEMBER' },
  { label: '项目章程', value: 'PROJECT_CHARTER' },
  { label: 'WBS', value: 'WBS' },
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
  { label: '导出', value: 'EXPORT' },
  { label: '导入', value: 'IMPORT' },
  { label: '附件', value: 'ATTACHMENT' },
]

const importModuleOptions = [
  { label: '任务', value: 'TASK' },
  { label: '风险', value: 'RISK' },
  { label: '成本', value: 'COST' },
  { label: '工时', value: 'TIMESHEET' },
]

const exportFormatOptions = [
  { label: 'Excel', value: 'EXCEL' },
  { label: 'PDF', value: 'PDF' },
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
  COST: {
    fileName: '成本导入模板.xlsx',
    headers: ['recordType', 'type', 'name', 'taskId', 'phase', 'amount', 'currency', 'sourceType', 'spendDate', 'remark'],
    sample: ['PLAN', 'LABOR', '后端开发人工预算', '', '开发', '12000', 'CNY', '', '', '计划成本示例'],
    fields: [
      'recordType 必填: PLAN 或 ACTUAL',
      'PLAN 行建议填写: type, name, amount；taskId / phase / currency / remark 可选',
      'ACTUAL 行建议填写: amount, sourceType, spendDate；taskId / remark 可选',
      'type 可选: LABOR / PROCUREMENT / EQUIPMENT / OTHER',
      'sourceType 可选: MANUAL / EXPENSE / PURCHASE / TIMESHEET',
      '实际成本行的 currency 当前主要用于模板对齐，界面展示仍沿用系统现有币种逻辑',
    ],
  },
  TIMESHEET: {
    fileName: '工时导入模板.xlsx',
    headers: ['taskId', 'userId', 'workDate', 'hours', 'description'],
    sample: ['10001', '', '2026-04-29', '8', '完成接口开发并自测'],
    fields: [
      '必填: taskId, workDate, hours',
      'userId 可选，留空时默认记到当前导入人名下',
      'taskId 仅支持当前项目已存在任务 ID',
      '日期支持: yyyy-MM-dd，也兼容带时间的日期格式',
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
          { key: 'add-milestone', label: '新增里程碑' },
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
          { key: 'pending-review', label: '待验收' },
          { key: 'project-reschedule', label: '重新推排' },
          { key: 'project-task-overview', label: '项目任务总览' },
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
          { key: 'project-milestones', label: '里程碑管理' },
          { key: 'project-scope-baselines', label: '范围基线' },
        ],
      },
      {
        title: '风险与变更',
        actions: [
          { key: 'project-risks', label: '风险登记册' },
          { key: 'project-risk-matrix', label: '风险矩阵' },
          { key: 'project-changes', label: '变更管理' },
          { key: 'project-configuration', label: '配置管理' },
          { key: 'project-config-baselines', label: '配置基线' },
        ],
      },
      {
        title: '质量管理',
        actions: [
          { key: 'project-quality-plans', label: '质量计划' },
          { key: 'project-quality-activities', label: '质量活动' },
          { key: 'project-quality-metrics', label: '质量指标' },
        ],
      },
      {
        title: '测试管理',
        actions: [
          { key: 'project-test-plans', label: '测试计划' },
          { key: 'project-test-cases', label: '测试用例' },
          { key: 'project-defects', label: '缺陷管理' },
          { key: 'project-test-reports', label: '测试报告' },
        ],
      },
      {
        title: '会议与沟通',
        actions: [
          { key: 'project-communication-matrix', label: '沟通安排' },
          { key: 'project-meetings', label: '会议计划' },
          { key: 'project-communication-records', label: '沟通记录' },
        ],
      },
      {
        title: '采购与合同',
        actions: [
          { key: 'project-procurement-suppliers', label: '供应商' },
          { key: 'project-procurement-items', label: '采购项' },
          { key: 'project-procurement-contracts', label: '合同' },
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

const procurementActionTabMap = {
  'project-procurement-suppliers': 'suppliers',
  'project-procurement-items': 'procurements',
  'project-procurement-contracts': 'contracts',
}

function isActionActive(actionKey) {
  if (actionKey.startsWith('format:')) return ganttFormat.value === actionKey.slice(7)
  if (procurementActionTabMap[actionKey]) {
    return procurementDialogVisible.value && procurementTab.value === procurementActionTabMap[actionKey]
  }
  if (actionKey === 'open-gantt-style') return ganttStyleDialogVisible.value
  if (actionKey === 'pending-review') return pendingReviewDialogVisible.value
  if (actionKey === 'project-task-overview') return !effectiveTaskMineOnly.value
  if (actionKey === 'my-tasks') return effectiveTaskMineOnly.value
  return false
}

const memberReadableActionKeys = new Set([
  'back',
  'save',
  'task-info',
  'calendar',
  'alerts',
  'project-task-overview',
  'my-tasks',
  'project-dashboard',
  'project-info',
  'project-charter',
  'project-members',
  'project-status',
  'project-wbs',
  'project-requirements',
  'project-milestones',
  'project-scope-baselines',
  'project-risks',
  'project-risk-matrix',
  'project-changes',
  'project-configuration',
  'project-config-baselines',
  'project-quality-plans',
  'project-quality-activities',
  'project-quality-metrics',
  'project-quality',
  'project-test-plans',
  'project-test-cases',
  'project-defects',
  'project-test-reports',
  'project-testing',
  'project-communication-matrix',
  'project-meetings',
  'project-communication-records',
  'project-communication',
  'project-procurement',
  'project-procurement-suppliers',
  'project-procurement-items',
  'project-procurement-contracts',
  'project-cost-plans',
  'project-cost-actuals',
  'project-cost-baselines',
  'project-evm',
  'project-acceptance',
  'project-closure',
  'report-open',
])

const closedReadableActionKeys = new Set([
  'task-info', 'calendar', 'alerts', 'project-task-overview', 'my-tasks', 'back',
  'project-info', 'project-charter', 'project-dashboard', 'project-members', 'project-milestones',
  'project-risks', 'project-risk-matrix', 'project-wbs', 'project-requirements',
  'project-scope-baselines', 'project-quality-plans', 'project-quality-activities', 'project-quality-metrics', 'project-quality', 'project-test-plans', 'project-test-cases', 'project-defects', 'project-test-reports', 'project-testing', 'project-communication-matrix', 'project-meetings', 'project-communication-records', 'project-communication', 'project-configuration', 'project-config-baselines', 'project-procurement', 'project-cost-plans', 'project-cost-actuals',
  'project-procurement-suppliers', 'project-procurement-items', 'project-procurement-contracts',
  'project-cost-baselines', 'project-evm', 'project-timesheets',
  'project-acceptance', 'project-changes', 'project-closure', 'project-status',
  'report-open', 'report-generate',
])

const editableProjectRoleCodes = new Set(['OWNER', 'PARTICIPANT', 'PROJECT_MANAGER', 'TEAM_MEMBER'])

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

  if (!currentProjectRoleCode.value) {
    return false
  }

  if (isClosedProject.value) {
    return closedReadableActionKeys.has(actionKey)
  }

  return memberReadableActionKeys.has(actionKey)
}

function isActionDisabled(actionKey) {
  return !canUseAction(actionKey)
}

const contextActionTabMap = {
  'add-task': 'task',
  'add-child': 'task',
  'add-summary': 'task',
  'add-milestone': 'task',
  'indent': 'task',
  'outdent': 'task',
  'delete': 'task',
  'task-info': 'task',
  'save': 'task',
  'calendar': 'task',
  'alerts': 'task',
  'pending-review': 'task',
  'project-reschedule': 'task',
  'project-task-overview': 'task',
  'my-tasks': 'task',
  'open-gantt-style': 'format',
  'project-dashboard': 'project',
  'project-members': 'team',
  'project-risks': 'project',
  'project-procurement-suppliers': 'project',
  'project-procurement-items': 'project',
  'project-procurement-contracts': 'project',
  'project-timesheets': 'team',
  'report-open': 'report',
}

function isContextMenuActionDisabled(actionKey, row = null) {
  if (actionKey === 'task-basic' || actionKey === 'task-progress') {
    return !row || isBlankPlaceholderRow(row)
  }
  if (actionKey === 'task-dependency') {
    return !row || isBlankPlaceholderRow(row) || !row.taskId
  }
  return isActionDisabled(actionKey)
}

function isContextMenuActionActive(actionKey) {
  if (actionKey === 'task-basic') return taskInfoDialogVisible.value && taskDetailSection.value === 'basic'
  if (actionKey === 'task-progress') return taskInfoDialogVisible.value && taskDetailSection.value === 'progress'
  if (actionKey === 'task-dependency') return taskInfoDialogVisible.value && taskDetailSection.value === 'dependency'
  return isActionActive(actionKey)
}

function createContextMenuAction(actionKey, label, description, options = {}) {
  const row = options.row || null
  const disabled = isContextMenuActionDisabled(actionKey, row)
  if (options.hideWhenDisabled && disabled) {
    return null
  }
  return {
    key: actionKey,
    label,
    description,
    tone: options.tone || 'default',
    disabled,
    active: !disabled && isContextMenuActionActive(actionKey),
  }
}

function createContextMenuGroup(title, actions) {
  const availableActions = actions.filter(Boolean)
  if (!availableActions.length) return null
  return {
    title,
    actions: availableActions,
  }
}

const activeContextMenuGroup = computed(() => {
  if (!contextMenuState.activeGroupTitle) return null
  return contextMenuState.groups.find((group) => group.title === contextMenuState.activeGroupTitle) || null
})

function setActiveContextMenuGroup(title) {
  if (!title) return
  if (contextMenuState.groups.some((group) => group.title === title)) {
    contextMenuState.activeGroupTitle = title
  }
}

async function openContextSubmenu(title, triggerElement = null) {
  setActiveContextMenuGroup(title)
  await nextTick()
  syncContextSubmenuPosition(triggerElement)
}

function syncContextSubmenuPosition(triggerElement = null) {
  const menuEl = editorContextMenuRef.value
  const submenuEl = editorContextSubmenuRef.value
  if (!(menuEl instanceof HTMLElement) || !(submenuEl instanceof HTMLElement)) return

  const menuRect = menuEl.getBoundingClientRect()
  const submenuRect = submenuEl.getBoundingClientRect()
  const triggerRect = triggerElement instanceof HTMLElement ? triggerElement.getBoundingClientRect() : null

  const preferredTop = triggerRect ? triggerRect.top - menuRect.top - 6 : 0
  const maxTop = Math.max(0, window.innerHeight - submenuRect.height - menuRect.top - 12)
  contextMenuState.submenuTop = Math.min(Math.max(0, preferredTop), maxTop)

  const spaceRight = window.innerWidth - menuRect.right - 12
  const spaceLeft = menuRect.left - 12
  contextMenuState.submenuDirection = spaceRight >= submenuRect.width + 6 || spaceRight >= spaceLeft ? 'right' : 'left'
}

function getTaskContextSubtitle(row) {
  const tokens = []
  if (row?.wbsCode) {
    tokens.push(`WBS ${row.wbsCode}`)
  }
  tokens.push(row?.assigneeName ? `负责人 ${row.assigneeName}` : '未分配负责人')
  if (!row?.taskId) {
    tokens.push('未保存任务')
  }
  return tokens.join(' · ')
}

function getTimelineContextSubtitle(row) {
  const tokens = []
  if (row?.start || row?.finish) {
    tokens.push(`${row?.start || '-'} 至 ${row?.finish || '-'}`)
  }
  tokens.push(getTaskProgressLabel(row))
  return tokens.join(' · ')
}

function buildPageContextMenuGroups() {
  return [
    createContextMenuGroup('任务', [
      createContextMenuAction('add-task', '新增任务', '插入一条任务', { hideWhenDisabled: true }),
      createContextMenuAction('project-task-overview', '项目任务总览', '回到项目全部任务视图'),
      createContextMenuAction('my-tasks', '我的任务', '切换到个人视图'),
    ]),
    createContextMenuGroup('项目', [
      createContextMenuAction('project-dashboard', '项目概览', '查看整体进度'),
      createContextMenuAction('project-members', '项目成员', '查看项目成员'),
      createContextMenuAction('report-open', '报表中心', '打开项目报表'),
    ]),
    createContextMenuGroup('操作', [
      createContextMenuAction('save', '保存内容', '保存当前编辑', { hideWhenDisabled: true }),
    ]),
  ].filter(Boolean)
}

function buildToolbarContextMenuGroups() {
  return [
    createContextMenuGroup('任务', [
      createContextMenuAction('project-task-overview', '项目任务总览', '查看项目全部任务'),
      createContextMenuAction('my-tasks', '我的任务', '切换个人任务视图'),
      createContextMenuAction('calendar', '日程', '查看任务日程'),
    ]),
    createContextMenuGroup('项目', [
      createContextMenuAction('project-dashboard', '项目概览', '查看总览信息'),
      createContextMenuAction('report-open', '报表中心', '打开报表中心'),
    ]),
    createContextMenuGroup('操作', [
      createContextMenuAction('project-reschedule', '重新推排', '按依赖和约束重算', { hideWhenDisabled: true }),
      createContextMenuAction('save', '保存内容', '保存当前编辑', { hideWhenDisabled: true }),
      createContextMenuAction('back', '返回首页', '回到项目列表'),
    ]),
  ].filter(Boolean)
}

function buildBannerContextMenuGroups() {
  return [
    createContextMenuGroup('项目', [
      createContextMenuAction('project-dashboard', '项目概览', '查看整体进度'),
      createContextMenuAction('project-info', '项目信息', '查看项目基础信息'),
      createContextMenuAction('project-members', '项目成员', '查看成员和权限'),
    ]),
    createContextMenuGroup('任务', [
      createContextMenuAction('project-task-overview', '项目任务总览', '回到全部任务视图'),
      createContextMenuAction('my-tasks', '我的任务', '切换个人任务视图'),
      createContextMenuAction('project-risks', '风险登记册', '查看任务相关风险'),
      createContextMenuAction('project-timesheets', '工时管理', '查看工时', { hideWhenDisabled: true }),
    ]),
    createContextMenuGroup('操作', [
      createContextMenuAction('report-open', '报表中心', '打开报表中心'),
    ]),
  ].filter(Boolean)
}

function buildTaskGridContextMenuGroups() {
  return [
    createContextMenuGroup('任务', [
      createContextMenuAction('add-task', '新增任务', '插入同级任务', { hideWhenDisabled: true }),
      createContextMenuAction('add-summary', '新增摘要任务', '插入父级结构', { hideWhenDisabled: true }),
      createContextMenuAction('add-milestone', '新增里程碑', '插入关键节点', { hideWhenDisabled: true }),
      createContextMenuAction('project-task-overview', '项目任务总览', '回到全部任务视图'),
      createContextMenuAction('my-tasks', '我的任务', '切换个人任务视图'),
    ]),
    createContextMenuGroup('项目', [
      createContextMenuAction('project-members', '项目成员', '查看可分配成员'),
    ]),
    createContextMenuGroup('操作', [
      createContextMenuAction('project-reschedule', '重新推排', '按依赖和约束重算', { hideWhenDisabled: true }),
      createContextMenuAction('save', '保存内容', '保存当前计划', { hideWhenDisabled: true }),
    ]),
  ].filter(Boolean)
}

function buildTimelineContextMenuGroups() {
  return [
    createContextMenuGroup('任务', [
      createContextMenuAction('calendar', '日程', '查看任务日程'),
      createContextMenuAction('alerts', '预警', '查看计划预警'),
      createContextMenuAction('project-reschedule', '重新推排', '按依赖和约束重算', { hideWhenDisabled: true }),
      createContextMenuAction('project-task-overview', '项目任务总览', '回到全部任务视图'),
      createContextMenuAction('my-tasks', '我的任务', '切换个人任务视图'),
    ]),
    createContextMenuGroup('视图', [
      createContextMenuAction('open-gantt-style', '甘特图样式', '调整条形图样式'),
    ]),
    createContextMenuGroup('项目', [
      createContextMenuAction('report-open', '报表中心', '打开报表中心'),
    ]),
  ].filter(Boolean)
}

function buildTaskGridRowContextMenuGroups(row) {
  return [
    createContextMenuGroup('任务', [
      createContextMenuAction('task-info', '任务详情', '查看详情', { row }),
      createContextMenuAction('task-basic', '分配负责人', '调整负责人', { row }),
      createContextMenuAction('task-progress', '更新进度', '填写进度', { row }),
      createContextMenuAction('task-dependency', '设置依赖', '维护前置关系', { row, hideWhenDisabled: true }),
      createContextMenuAction('delete', '删除任务', '删除当前任务', { row, tone: 'danger', hideWhenDisabled: true }),
    ]),
    createContextMenuGroup('结构', [
      createContextMenuAction('add-child', '新增子任务', '继续拆分当前任务', { row, hideWhenDisabled: true }),
      createContextMenuAction('add-task', '新增同级任务', '插入同级任务', { row, hideWhenDisabled: true }),
      createContextMenuAction('indent', '任务降级', '缩进到上一条任务下', { row, hideWhenDisabled: true }),
      createContextMenuAction('outdent', '任务升级', '提升到上一层级', { row, hideWhenDisabled: true }),
    ]),
    createContextMenuGroup('项目', [
      createContextMenuAction('project-members', '项目成员', '查看可分配成员'),
      createContextMenuAction('project-risks', '风险登记册', '查看关联风险'),
    ]),
    createContextMenuGroup('操作', [
      createContextMenuAction('save', '保存内容', '保存当前计划', { hideWhenDisabled: true }),
    ]),
  ].filter(Boolean)
}

function buildTimelineRowContextMenuGroups(row) {
  return [
    createContextMenuGroup('任务', [
      createContextMenuAction('task-info', '任务详情', '查看详情', { row }),
      createContextMenuAction('task-progress', '更新进度', '填写进度', { row }),
      createContextMenuAction('task-basic', '分配负责人', '调整负责人', { row }),
    ]),
    createContextMenuGroup('视图', [
      createContextMenuAction('calendar', '日程', '查看日程'),
      createContextMenuAction('alerts', '预警', '查看预警'),
      createContextMenuAction('project-task-overview', '项目任务总览', '回到全部任务视图'),
      createContextMenuAction('my-tasks', '我的任务', '切换个人视图'),
    ]),
  ].filter(Boolean)
}

async function openEditorContextMenu(event, options = {}) {
  contextMenuState.area = options.area || 'workspace'
  contextMenuState.targetRowId = options.row?.localId || null
  contextMenuState.title = options.title || '项目快捷操作'
  contextMenuState.subtitle = options.subtitle || '右键打开常用入口'
  contextMenuState.groups = Array.isArray(options.groups) ? options.groups.filter(Boolean) : []
  contextMenuState.activeGroupTitle = ''
  contextMenuState.submenuTop = 0
  contextMenuState.submenuDirection = 'right'
  contextMenuPosition.x = event.clientX
  contextMenuPosition.y = event.clientY
  contextMenuState.visible = true

  await nextTick()

  const menuEl = editorContextMenuRef.value
  if (menuEl instanceof HTMLElement) {
    const padding = 12
    contextMenuPosition.x = Math.min(
      Math.max(padding, event.clientX),
      Math.max(padding, window.innerWidth - menuEl.offsetWidth - padding),
    )
    contextMenuPosition.y = Math.min(
      Math.max(padding, event.clientY),
      Math.max(padding, window.innerHeight - menuEl.offsetHeight - padding),
    )

    const firstCategory = menuEl.querySelector('[data-context-group-trigger="true"]')
    firstCategory?.focus?.()
  }
}

function closeContextMenu() {
  contextMenuState.visible = false
  contextMenuState.area = 'workspace'
  contextMenuState.title = '项目快捷操作'
  contextMenuState.subtitle = '右键打开常用入口'
  contextMenuState.targetRowId = null
  contextMenuState.activeGroupTitle = ''
  contextMenuState.submenuTop = 0
  contextMenuState.submenuDirection = 'right'
  contextMenuState.groups = []
}

function openPageContextMenu(event) {
  openEditorContextMenu(event, {
    area: 'page',
    title: '页面操作',
    subtitle: '当前项目的通用入口',
    groups: buildPageContextMenuGroups(),
  })
}

function openToolbarContextMenu(event) {
  openEditorContextMenu(event, {
    area: 'toolbar',
    title: '工具栏',
    subtitle: '导航和全局操作',
    groups: buildToolbarContextMenuGroups(),
  })
}

function openBannerContextMenu(event) {
  openEditorContextMenu(event, {
    area: 'banner',
    title: projectDetail.value?.name || '项目计划',
    subtitle: projectDetail.value?.status ? formatProjectStatus(projectDetail.value.status) : '项目总览入口',
    groups: buildBannerContextMenuGroups(),
  })
}

function openTaskGridContextMenu(event) {
  openEditorContextMenu(event, {
    area: 'task-grid',
    title: '任务表',
    subtitle: '插入任务或保存当前计划',
    groups: buildTaskGridContextMenuGroups(),
  })
}

function openTimelineContextMenu(event, area = 'timeline') {
  openEditorContextMenu(event, {
    area,
    title: '时间线',
    subtitle: '查看排期或调整甘特图显示',
    groups: buildTimelineContextMenuGroups(),
  })
}

function openWorkspaceContextMenu(event) {
  openEditorContextMenu(event, {
    area: 'workspace',
    title: '项目快捷操作',
    subtitle: '浏览器默认右键已禁用',
    groups: buildPageContextMenuGroups(),
  })
}

function openRowContextMenu(event, row, area = 'task') {
  if (!row || isBlankPlaceholderRow(row)) {
    if (area === 'timeline') {
      openTimelineContextMenu(event, 'timeline')
      return
    }
    openTaskGridContextMenu(event)
    return
  }

  selectTaskRow(row)
  const isTimelineArea = area === 'timeline'
  openEditorContextMenu(event, {
    area,
    row,
    title: row.name || '未命名任务',
    subtitle: isTimelineArea ? getTimelineContextSubtitle(row) : getTaskContextSubtitle(row),
    groups: isTimelineArea ? buildTimelineRowContextMenuGroups(row) : buildTaskGridRowContextMenuGroups(row),
  })
}

function setActiveTabForContextAction(actionKey) {
  const nextTab = contextActionTabMap[actionKey]
  if (nextTab) {
    activeTab.value = nextTab
  }
}

async function handleContextMenuAction(actionKey) {
  const row = contextMenuState.targetRowId
    ? taskRows.value.find((item) => item.localId === contextMenuState.targetRowId) || null
    : null

  closeContextMenu()

  switch (actionKey) {
    case 'task-basic':
      setActiveTabForContextAction('task-info')
      await openTaskInfoDialog(row, 'basic')
      break
    case 'task-progress':
      setActiveTabForContextAction('task-info')
      await openTaskInfoDialog(row, 'progress')
      break
    case 'task-dependency':
      setActiveTabForContextAction('task-info')
      await openTaskInfoDialog(row, 'dependency')
      break
    default:
      setActiveTabForContextAction(actionKey)
      handleRibbonAction(actionKey)
      break
  }
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
    'parent-shell-bar': type === 'parent',
    'parent-shell-expanded': type === 'parent',
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

function parsePixelValue(value, fallback = 0) {
  const parsed = Number.parseFloat(String(value ?? fallback).replace('px', ''))
  return Number.isFinite(parsed) ? parsed : fallback
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

function readGlobalGanttAppearanceFromLocal() {
  try {
    const raw = localStorage.getItem(GANTT_APPEARANCE_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function loadGlobalGanttAppearance() {
  const localConfig = readGlobalGanttAppearanceFromLocal()
  if (localConfig) {
    applyGanttAppearanceConfig(localConfig)
  } else {
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

function readProjectWbsConfigFromLocal() {
  try {
    const raw = localStorage.getItem(`${WBS_CONFIG_STORAGE_PREFIX}${projectId.value}`)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function loadProjectWbsConfig() {
  const localConfig = readProjectWbsConfigFromLocal()
  if (localConfig) {
    applyWbsConfig(localConfig)
  } else {
    applyWbsConfig(createDefaultWbsConfig())
  }
}

function saveProjectWbsConfig() {
  localStorage.setItem(`${WBS_CONFIG_STORAGE_PREFIX}${projectId.value}`, JSON.stringify(cloneWbsConfig()))
}

function cloneScheduleOptions() {
  return normalizeScheduleOptionsConfig(scheduleOptions)
}

function getScheduleOptionsSignature(config = scheduleOptions) {
  return JSON.stringify(normalizeScheduleOptionsConfig(config))
}

function applyScheduleOptions(config) {
  const next = normalizeScheduleOptionsConfig(config)
  scheduleOptions.scheduleMode = next.scheduleMode
  scheduleOptions.holidayDates = next.holidayDates
}

function readProjectScheduleOptionsFromLocal() {
  try {
    const raw = localStorage.getItem(`${SCHEDULE_OPTIONS_STORAGE_PREFIX}${projectId.value}`)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

function loadProjectScheduleOptions() {
  const localConfig = readProjectScheduleOptionsFromLocal()
  if (localConfig) {
    applyScheduleOptions(localConfig)
  } else {
    applyScheduleOptions(createDefaultScheduleOptions())
  }
}

function saveProjectScheduleOptions() {
  localStorage.setItem(`${SCHEDULE_OPTIONS_STORAGE_PREFIX}${projectId.value}`, JSON.stringify(cloneScheduleOptions()))
}

async function loadProjectEditorPreferenceData(options = {}) {
  const { migrateLocalFallback = false } = options
  let preference = null
  try {
    preference = await getProjectEditorPreferences(projectId.value)
  } catch {
    preference = null
  }

  const localGanttAppearance = readGlobalGanttAppearanceFromLocal()
  const localWbsConfig = readProjectWbsConfigFromLocal()
  const localScheduleOptions = readProjectScheduleOptionsFromLocal()
  const hasRemoteGanttAppearance = Boolean(preference?.ganttAppearance && Object.keys(preference.ganttAppearance).length)
  const hasRemoteWbsConfig = Boolean(preference?.wbsConfig && Object.keys(preference.wbsConfig).length)
  const hasRemoteScheduleOptions = Boolean(preference?.scheduleOptions && Object.keys(preference.scheduleOptions).length)

  if (hasRemoteGanttAppearance) {
    applyGanttAppearanceConfig(preference.ganttAppearance)
  } else if (localGanttAppearance) {
    loadGlobalGanttAppearance()
  } else {
    applyGanttAppearanceConfig(createDefaultGanttAppearance())
  }

  if (hasRemoteWbsConfig) {
    applyWbsConfig(preference.wbsConfig)
  } else if (localWbsConfig) {
    loadProjectWbsConfig()
  } else {
    applyWbsConfig(createDefaultWbsConfig())
  }

  if (hasRemoteScheduleOptions) {
    applyScheduleOptions(preference.scheduleOptions)
  } else if (localScheduleOptions) {
    loadProjectScheduleOptions()
  } else {
    applyScheduleOptions(createDefaultScheduleOptions())
  }

  saveGlobalGanttAppearance()
  saveProjectWbsConfig()
  saveProjectScheduleOptions()
  applyScheduleModeToProjectAndTasks()

  if (!migrateLocalFallback) {
    return
  }

  const hasRemotePreference = hasRemoteGanttAppearance || hasRemoteWbsConfig || hasRemoteScheduleOptions
  const hasLocalFallback = Boolean(localGanttAppearance || localWbsConfig || localScheduleOptions)
  if (!hasRemotePreference && hasLocalFallback && canEditProjectContent.value) {
    try {
      await persistProjectEditorPreferenceData()
    } catch (error) {
      console.warn('Failed to migrate editor preferences from local storage to server.', error)
    }
  }
}

async function persistProjectEditorPreferenceData() {
  await saveProjectEditorPreferences(projectId.value, {
    ganttAppearance: cloneGanttAppearance(),
    wbsConfig: cloneWbsConfig(),
    scheduleOptions: cloneScheduleOptions(),
  })
  saveGlobalGanttAppearance()
  saveProjectWbsConfig()
  saveProjectScheduleOptions()
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
    milestoneId: null,
    milestoneName: '',
    description: '',
    backendParentTaskId: null,
    localParentId: null,
    outlineLevel: 0,
    expanded: true,
    mode,
    name: '',
    duration: '',
    start: '',
    finish: '',
    deadlineDate: '',
    constraintType: '',
    constraintDate: '',
    taskType: 'TASK',
    status: 'TODO',
    progress: 0,
    directAssigneeId: null,
    directAssigneeName: '',
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

function clampTaskProgressValue(progress) {
  const amount = Number(progress ?? 0)
  if (!Number.isFinite(amount)) return 0
  return Math.max(0, Math.min(100, amount))
}

function deriveTaskStatusFromProgress(progress, status = '') {
  const normalizedProgress = clampTaskProgressValue(progress)
  if (normalizedProgress <= 0) return 'TODO'
  if (status === 'PENDING_REVIEW' && normalizedProgress >= 100) return 'PENDING_REVIEW'
  if (normalizedProgress >= 100) return 'DONE'
  if (status === 'BLOCKED') return 'BLOCKED'
  return 'IN_PROGRESS'
}

function normalizeTaskProgressState(progress, status = '') {
  const normalizedProgress = clampTaskProgressValue(progress)
  const normalizedStatus = deriveTaskStatusFromProgress(normalizedProgress, status)
  if (normalizedStatus === 'TODO') {
    return { progress: 0, status: 'TODO' }
  }
  if (normalizedStatus === 'PENDING_REVIEW') {
    return { progress: 100, status: 'PENDING_REVIEW' }
  }
  if (normalizedStatus === 'DONE') {
    return { progress: 100, status: 'DONE' }
  }
  return { progress: normalizedProgress, status: normalizedStatus }
}

function deriveTaskProgressEditorStatus(progress, workflowStatus = '') {
  const normalizedProgress = clampTaskProgressValue(progress)
  const normalizedWorkflowStatus = String(workflowStatus || '').toUpperCase()
  if (normalizedWorkflowStatus === 'PENDING_REVIEW' && normalizedProgress >= 100) return 'PENDING_REVIEW'
  if (normalizedWorkflowStatus === 'DONE' && normalizedProgress >= 100) return 'DONE'
  if (normalizedProgress <= 0) return 'TODO'
  if (normalizedProgress >= 100) return 'DONE'
  return 'IN_PROGRESS'
}

function normalizeTaskProgressRecord(item) {
  if (!item || typeof item !== 'object') return item
  const normalized = normalizeTaskProgressState(item.progress, item.status)
  return {
    ...item,
    progress: normalized.progress,
    status: normalized.status,
  }
}

function createRowFromTask(task, index) {
  const row = createEmptyRow(task.remark || '手动计划', false)
  const normalized = normalizeTaskProgressState(task.progress, task.status)
  row.taskId = task.id != null ? String(task.id) : null
  row.milestoneId = task.milestoneId != null ? String(task.milestoneId) : null
  row.milestoneName = task.milestoneName || ''
  row.description = task.description || ''
  row.backendParentTaskId = task.parentTaskId != null ? String(task.parentTaskId) : null
  row.name = task.name || ''
  row.start = toDateValue(task.plannedStartDate)
  row.finish = toDateValue(task.plannedEndDate)
  row.deadlineDate = task.deadlineDate ? formatDateText(task.deadlineDate) : ''
  row.constraintType = task.constraintType || ''
  row.constraintDate = task.constraintDate ? formatDateText(task.constraintDate) : ''
  row.duration = formatDuration(task.plannedHours)
  row.progress = normalized.progress
  row.status = normalized.status
  row.directAssigneeId = task.directAssigneeId != null
    ? String(task.directAssigneeId)
    : (task.assigneeId != null ? String(task.assigneeId) : null)
  row.directAssigneeName = task.directAssigneeName || task.assigneeName || ''
  row.assigneeId = task.assigneeId != null ? String(task.assigneeId) : row.directAssigneeId
  row.assigneeName = task.assigneeName || row.directAssigneeName
  row.taskType = task.taskType || mapModeToTaskType(task.remark || '')
  row.sortOrder = task.sortOrder ?? index + 1

  if (!row.duration && row.start && row.finish) {
    row.duration = String(getScheduledDurationDays(parseDateValue(row.start), parseDateValue(row.finish)))
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

function parseConfigBaselineSnapshot(item) {
  try {
    const parsed = JSON.parse(item?.snapshotJson || '[]')
    return Array.isArray(parsed) ? parsed.filter((snapshotItem) => snapshotItem && typeof snapshotItem === 'object') : []
  } catch {
    return []
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

function isConfigBaselineItem(item) {
  return item?.baselineType === 'CONFIG'
}

function isConfigBaselineActive(itemId) {
  return activeConfigBaselineId.value === itemId
}

function toggleConfigBaselinePreview(itemId) {
  activeConfigBaselineId.value = activeConfigBaselineId.value === itemId ? null : itemId
}

async function restoreScopeBaseline(item) {
  if (!requireProjectContentEditPermission('回退范围基线')) return
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
  applyScheduleModeToProjectAndTasks()
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
  const row = taskRows.value.find((item) => item.localId === rowId) || null
  if (!canEditTaskField(row, field)) return
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

function appendRootTaskRow(row) {
  const blankIndex = findFirstBlankPlaceholderIndex()
  row.localParentId = null
  row.outlineLevel = 0
  if (blankIndex >= 0) {
    taskRows.value.splice(blankIndex, 0, row)
  } else {
    taskRows.value.push(row)
  }
  refreshOutlineLevels()
  ensureMinimumRows()
  ensureTrailingEmptyRows()
  hasUnsavedChanges.value = true
}

function removeTaskRowByLocalId(localId) {
  const startIndex = findRowIndex(localId)
  if (startIndex < 0) return
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
        row.duration = String(getScheduledDurationDays(parseDateValue(row.start), parseDateValue(row.finish)))
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

function parseDateValue(value) {
  if (!value) return null
  return new Date(`${String(value).slice(0, 10)}T00:00:00`)
}

function formatDateToValue(date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

function normalizePlainDateValue(value) {
  const parsed = parseDateValue(value)
  if (!parsed) return ''
  return formatDateToValue(parsed)
}

function normalizeScheduleHolidayDates(value) {
  if (!Array.isArray(value)) return []
  const deduped = new Set()
  return value
    .map((item) => normalizePlainDateValue(item))
    .filter(Boolean)
    .filter((item) => {
      if (deduped.has(item)) return false
      deduped.add(item)
      return true
    })
    .sort((left, right) => left.localeCompare(right))
}

function normalizeScheduleOptionsConfig(config) {
  const next = { ...createDefaultScheduleOptions(), ...(config || {}) }
  return {
    scheduleMode: next.scheduleMode === 'WORKDAY' ? 'WORKDAY' : 'CALENDAR_DAY',
    holidayDates: normalizeScheduleHolidayDates(next.holidayDates),
  }
}

function isWeekendDate(date) {
  const day = date.getDay()
  return day === 0 || day === 6
}

function isConfiguredHolidayDate(date) {
  const normalized = date instanceof Date ? formatDateToValue(date) : normalizePlainDateValue(date)
  if (!normalized) return false
  return configuredHolidayDateSet.value.has(normalized)
}

function isNonWorkingScheduleDate(date) {
  return isWeekendDate(date) || isConfiguredHolidayDate(date)
}

function isWorkdayScheduleMode() {
  return scheduleOptions.scheduleMode === 'WORKDAY'
}

function moveDateToNextWorkday(date) {
  let next = new Date(date)
  while (isNonWorkingScheduleDate(next)) {
    next = addDays(next, 1)
  }
  return next
}

function normalizeDateToScheduleMode(date) {
  if (!date) return null
  return isWorkdayScheduleMode() ? moveDateToNextWorkday(date) : new Date(date)
}

function normalizeDateValueToScheduleMode(value) {
  const parsed = parseDateValue(value)
  if (!parsed) return ''
  return formatDateToValue(normalizeDateToScheduleMode(parsed))
}

function countWorkdaysInclusive(startDate, endDate) {
  let count = 0
  let cursor = new Date(startDate)
  while (cursor <= endDate) {
    if (!isNonWorkingScheduleDate(cursor)) {
      count += 1
    }
    cursor = addDays(cursor, 1)
  }
  return count
}

function getScheduledDurationDays(startDate, endDate) {
  if (!startDate || !endDate) return 0
  const normalizedStart = normalizeDateToScheduleMode(startDate)
  const normalizedEnd = normalizeDateToScheduleMode(endDate)
  if (normalizedEnd < normalizedStart) return 1
  if (!isWorkdayScheduleMode()) {
    return diffDays(normalizedStart, normalizedEnd) + 1
  }
  return Math.max(countWorkdaysInclusive(normalizedStart, normalizedEnd), 1)
}

function addScheduledDays(startDate, days) {
  const normalizedStart = normalizeDateToScheduleMode(startDate)
  if (!isWorkdayScheduleMode()) {
    return addDays(normalizedStart, days)
  }
  let next = new Date(normalizedStart)
  let remaining = Number(days || 0)
  const step = remaining >= 0 ? 1 : -1
  while (remaining !== 0) {
    next = addDays(next, step)
    if (!isNonWorkingScheduleDate(next)) {
      remaining -= step
    }
  }
  return next
}

function normalizeTaskRowDates(row) {
  if (!row || typeof row !== 'object') return false
  let changed = false

  for (const field of ['start', 'finish']) {
    const current = String(row[field] || '').trim()
    if (!current) continue
    const normalized = normalizeDateValueToScheduleMode(current)
    if (normalized && normalized !== current) {
      row[field] = normalized
      changed = true
    }
  }

  const beforeStart = row.start
  const beforeFinish = row.finish
  clampRowToBounds(row)
  changed = changed || beforeStart !== row.start || beforeFinish !== row.finish

  if (row.start && row.finish && row.finish < row.start) {
    row.finish = row.start
    changed = true
  }

  if (isMilestoneRow(row) && row.start && row.finish !== row.start) {
    row.finish = row.start
    changed = true
  }

  return changed
}

function normalizeTaskRowDuration(row) {
  if (!row.start || !row.finish) return false
  const nextDuration = String(getScheduledDurationDays(parseDateValue(row.start), parseDateValue(row.finish)))
  if (row.duration === nextDuration) return false
  row.duration = nextDuration
  return true
}

function applyScheduleModeToProjectAndTasks() {
  if (!taskRows.value.length) return false
  const beforeMap = new Map(
    taskRows.value.map((row) => [row.localId, `${row.start || ''}|${row.finish || ''}|${row.duration || ''}|${row.deadlineDate || ''}|${row.constraintType || ''}|${row.constraintDate || ''}`]),
  )

  for (const row of taskRows.value) {
    normalizeTaskRowDates(row)
    row.deadlineDate = row.deadlineDate ? normalizeDateValueToScheduleMode(row.deadlineDate) : ''
    const constraint = getTaskConstraintSnapshot(row)
    row.constraintType = constraint.type || ''
    row.constraintDate = constraint.date || ''
    if (!isSummaryRow(row)) {
      normalizeTaskRowDuration(row)
    }
  }

  syncSummaryRows()

  if (selectedTaskRow.value?.localId) {
    taskBasicForm.deadlineDate = selectedTaskRow.value.deadlineDate || ''
    taskBasicForm.constraintType = selectedTaskRow.value.constraintType || ''
    taskBasicForm.constraintDate = selectedTaskRow.value.constraintDate || ''
  }

  return taskRows.value.some((row) => beforeMap.get(row.localId) !== `${row.start || ''}|${row.finish || ''}|${row.duration || ''}|${row.deadlineDate || ''}|${row.constraintType || ''}|${row.constraintDate || ''}`)
}

function getParentRow(row) {
  if (!row?.localParentId) return null
  return taskRows.value.find((item) => item.localId === row.localParentId) || null
}

function getProjectDateBounds() {
  if (!projectForm.startDate || !projectForm.endDate) return null
  return {
    min: normalizeDateValueToScheduleMode(projectForm.startDate),
    max: normalizeDateValueToScheduleMode(projectForm.endDate),
  }
}

function getParentDateBounds(row) {
  const parent = getParentRow(row)
  if (!parent || !parent.start || !parent.finish) return null
  return {
    min: normalizeDateValueToScheduleMode(parent.start),
    max: normalizeDateValueToScheduleMode(parent.finish),
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

function getTaskScheduleSignature(row) {
  return `${row?.start || ''}|${row?.finish || ''}|${row?.duration || ''}`
}

function captureTaskScheduleSnapshot() {
  return new Map(taskRows.value.map((row) => [row.localId, getTaskScheduleSignature(row)]))
}

function collectChangedTaskIdsFromScheduleSnapshot(snapshot) {
  const changedTaskIds = new Set()
  for (const row of taskRows.value) {
    if (snapshot.get(row.localId) !== getTaskScheduleSignature(row) && row.taskId) {
      changedTaskIds.add(String(row.taskId))
    }
  }
  return changedTaskIds
}

function normalizeDependencyTypeValue(value) {
  const normalized = String(value || 'FS').trim().toUpperCase()
  return ['FS', 'SS', 'FF', 'SF'].includes(normalized) ? normalized : 'FS'
}

function getDependencyPredecessorAnchorKey(type) {
  return type === 'SS' || type === 'SF' ? 'start' : 'finish'
}

function getDependencySuccessorAnchorKey(type) {
  return type === 'FF' || type === 'SF' ? 'finish' : 'start'
}

function getIncomingTaskDependencies(taskId) {
  return projectTaskDependencies.value.filter(
    (item) => String(item?.successorTaskId || '') === String(taskId || ''),
  )
}

function getOutgoingTaskDependencies(taskId) {
  return projectTaskDependencies.value.filter(
    (item) => String(item?.predecessorTaskId || '') === String(taskId || ''),
  )
}

function getScheduledOffsetDays(fromDate, toDate) {
  if (!fromDate || !toDate) return 0
  const normalizedFrom = normalizeDateToScheduleMode(fromDate)
  const normalizedTo = normalizeDateToScheduleMode(toDate)
  if (!isWorkdayScheduleMode()) {
    return diffDays(normalizedFrom, normalizedTo)
  }
  if (normalizedFrom.getTime() === normalizedTo.getTime()) {
    return 0
  }

  let offset = 0
  let cursor = new Date(normalizedFrom)
  const step = normalizedTo > normalizedFrom ? 1 : -1

  while (cursor.getTime() !== normalizedTo.getTime()) {
    cursor = addDays(cursor, step)
    if (!isNonWorkingScheduleDate(cursor)) {
      offset += step
    }
  }

  return offset
}

function getTaskConstraintSnapshot(row, override = null) {
  const overrideConstraintType = override && (
    Object.prototype.hasOwnProperty.call(override, 'constraintType')
      ? override.constraintType
      : (Object.prototype.hasOwnProperty.call(override, 'type') ? override.type : undefined)
  )
  const normalizedConstraintType = normalizeTaskConstraintTypeValue(
    overrideConstraintType !== undefined ? overrideConstraintType : row?.constraintType,
  ) || ''
  const rawConstraintDate = override && Object.prototype.hasOwnProperty.call(override, 'constraintDate')
    ? override.constraintDate
    : (
      override && Object.prototype.hasOwnProperty.call(override, 'date')
        ? override.date
        : row?.constraintDate
    )
  const normalizedConstraintDate = requiresTaskConstraintDate(normalizedConstraintType)
    ? normalizeDateValueToScheduleMode(rawConstraintDate ? String(rawConstraintDate).slice(0, 10) : '')
    : ''

  return {
    type: normalizedConstraintType,
    date: normalizedConstraintDate,
  }
}

function getTaskConstraintRequestedOffset(row, constraint = getTaskConstraintSnapshot(row)) {
  const type = constraint?.type || ''
  const constraintDate = parseDateValue(constraint?.date)
  if (!type || !constraintDate) return 0

  const startDate = parseDateValue(row?.start)
  const finishDate = parseDateValue(row?.finish)

  switch (type) {
    case 'SNET':
      return startDate && normalizeDateToScheduleMode(startDate) < constraintDate
        ? getScheduledOffsetDays(startDate, constraintDate)
        : 0
    case 'SNLT':
      return startDate && normalizeDateToScheduleMode(startDate) > constraintDate
        ? getScheduledOffsetDays(startDate, constraintDate)
        : 0
    case 'FNET':
      return finishDate && normalizeDateToScheduleMode(finishDate) < constraintDate
        ? getScheduledOffsetDays(finishDate, constraintDate)
        : 0
    case 'FNLT':
      return finishDate && normalizeDateToScheduleMode(finishDate) > constraintDate
        ? getScheduledOffsetDays(finishDate, constraintDate)
        : 0
    case 'MSO':
      return startDate && normalizeDateToScheduleMode(startDate).getTime() !== constraintDate.getTime()
        ? getScheduledOffsetDays(startDate, constraintDate)
        : 0
    case 'MFO':
      return finishDate && normalizeDateToScheduleMode(finishDate).getTime() !== constraintDate.getTime()
        ? getScheduledOffsetDays(finishDate, constraintDate)
        : 0
    default:
      return 0
  }
}

function getTaskRowShiftLimits(row, constraint = getTaskConstraintSnapshot(row)) {
  const lowerBounds = []
  const upperBounds = []
  const bounds = getRowDateBounds(row)
  const startDate = parseDateValue(row?.start)
  const finishDate = parseDateValue(row?.finish)

  if (bounds?.min) {
    const minDate = parseDateValue(bounds.min)
    if (minDate && startDate) {
      lowerBounds.push(getScheduledOffsetDays(startDate, minDate))
    }
    if (minDate && finishDate) {
      lowerBounds.push(getScheduledOffsetDays(finishDate, minDate))
    }
  }

  if (bounds?.max) {
    const maxDate = parseDateValue(bounds.max)
    if (maxDate && startDate) {
      upperBounds.push(getScheduledOffsetDays(startDate, maxDate))
    }
    if (maxDate && finishDate) {
      upperBounds.push(getScheduledOffsetDays(finishDate, maxDate))
    }
  }

  const constraintDate = parseDateValue(constraint?.date)
  if (constraint?.type && constraintDate) {
    switch (constraint.type) {
      case 'SNET':
        if (startDate) lowerBounds.push(getScheduledOffsetDays(startDate, constraintDate))
        break
      case 'SNLT':
        if (startDate) upperBounds.push(getScheduledOffsetDays(startDate, constraintDate))
        break
      case 'FNET':
        if (finishDate) lowerBounds.push(getScheduledOffsetDays(finishDate, constraintDate))
        break
      case 'FNLT':
        if (finishDate) upperBounds.push(getScheduledOffsetDays(finishDate, constraintDate))
        break
      case 'MSO':
        if (startDate) {
          const offset = getScheduledOffsetDays(startDate, constraintDate)
          lowerBounds.push(offset)
          upperBounds.push(offset)
        }
        break
      case 'MFO':
        if (finishDate) {
          const offset = getScheduledOffsetDays(finishDate, constraintDate)
          lowerBounds.push(offset)
          upperBounds.push(offset)
        }
        break
      default:
        break
    }
  }

  return {
    minOffset: lowerBounds.length ? Math.max(...lowerBounds) : Number.NEGATIVE_INFINITY,
    maxOffset: upperBounds.length ? Math.min(...upperBounds) : Number.POSITIVE_INFINITY,
  }
}

function getTaskSubtreeShiftLimits(rootRow, options = {}) {
  const startIndex = findRowIndex(rootRow?.localId)
  if (startIndex < 0) {
    return {
      minOffset: Number.NEGATIVE_INFINITY,
      maxOffset: Number.POSITIVE_INFINITY,
    }
  }
  const endIndex = findSubtreeEndIndex(startIndex)
  const overrideMap = options.constraintOverrides || new Map()
  let minOffset = Number.NEGATIVE_INFINITY
  let maxOffset = Number.POSITIVE_INFINITY

  for (let index = startIndex; index <= endIndex; index += 1) {
    const row = taskRows.value[index]
    if (!row || isBlankPlaceholderRow(row)) continue
    const rowConstraint = overrideMap.get(row.localId) || null
    const rowLimits = getTaskRowShiftLimits(row, getTaskConstraintSnapshot(row, rowConstraint))
    minOffset = Math.max(minOffset, rowLimits.minOffset)
    maxOffset = Math.min(maxOffset, rowLimits.maxOffset)
  }

  return { minOffset, maxOffset }
}

function getDependencyRequiredDate(predecessorRow, dependency) {
  if (!predecessorRow) return null
  const type = normalizeDependencyTypeValue(dependency?.dependencyType)
  const predecessorAnchorKey = getDependencyPredecessorAnchorKey(type)
  const baseDate = parseDateValue(predecessorRow[predecessorAnchorKey])
  if (!baseDate) return null
  const lagDays = Number(dependency?.lagDays || 0)
  const offset = type === 'FS' ? lagDays + 1 : lagDays
  return addScheduledDays(baseDate, offset)
}

function shiftTaskSubtreeByScheduledOffset(rootRow, offsetDays, options = {}) {
  const requestedOffset = Number(offsetDays || 0)
  if (!rootRow?.localId || requestedOffset === 0) {
    return {
      changedTaskIds: new Set(),
      blocked: false,
    }
  }

  const shiftLimits = getTaskSubtreeShiftLimits(rootRow, options)
  let allowedOffset = 0
  if (requestedOffset > 0) {
    allowedOffset = Math.max(Math.min(requestedOffset, shiftLimits.maxOffset), 0)
  } else {
    allowedOffset = Math.min(Math.max(requestedOffset, shiftLimits.minOffset), 0)
  }

  if (allowedOffset === 0) {
    return {
      changedTaskIds: new Set(),
      blocked: requestedOffset !== 0,
    }
  }

  const beforeSnapshot = captureTaskScheduleSnapshot()
  const startIndex = findRowIndex(rootRow.localId)
  if (startIndex < 0) {
    return {
      changedTaskIds: new Set(),
      blocked: requestedOffset > 0,
    }
  }
  const endIndex = findSubtreeEndIndex(startIndex)
  if (endIndex < startIndex) {
    return {
      changedTaskIds: new Set(),
      blocked: requestedOffset > 0,
    }
  }

  for (let index = startIndex; index <= endIndex; index += 1) {
    const row = taskRows.value[index]
    if (!row || isBlankPlaceholderRow(row)) continue

    if (row.start) {
      row.start = formatDateToValue(addScheduledDays(parseDateValue(row.start), allowedOffset))
    }
    if (row.finish) {
      row.finish = formatDateToValue(addScheduledDays(parseDateValue(row.finish), allowedOffset))
    }
    normalizeTaskRowDates(row)
    if (!isSummaryRow(row) && row.start && row.finish) {
      syncDurationFromDates(row)
    }
  }

  syncSummaryRows()

  return {
    changedTaskIds: collectChangedTaskIdsFromScheduleSnapshot(beforeSnapshot),
    blocked: allowedOffset !== requestedOffset,
  }
}

function applyTaskConstraintSchedule(row, options = {}) {
  if (!row?.localId) {
    return {
      changed: false,
      blocked: false,
      changedTaskIds: [],
    }
  }

  const constraintOverride = options.constraintOverride || null
  const constraint = getTaskConstraintSnapshot(row, constraintOverride)
  const requestedOffset = getTaskConstraintRequestedOffset(row, constraint)
  if (!requestedOffset) {
    return {
      changed: false,
      blocked: false,
      changedTaskIds: [],
    }
  }

  const result = shiftTaskSubtreeByScheduledOffset(
    row,
    requestedOffset,
    {
      constraintOverrides: new Map([[row.localId, constraintOverride || constraint]]),
    },
  )

  return {
    changed: result.changedTaskIds.size > 0,
    blocked: result.blocked,
    changedTaskIds: [...result.changedTaskIds],
  }
}

function applySingleDependencyScheduleRule(dependency) {
  const predecessorRow = findRowByTaskId(dependency?.predecessorTaskId)
  const successorRow = findRowByTaskId(dependency?.successorTaskId)
  if (!predecessorRow || !successorRow) {
    return {
      changedTaskIds: new Set(),
      blocked: false,
    }
  }

  const type = normalizeDependencyTypeValue(dependency?.dependencyType)
  const successorAnchorKey = getDependencySuccessorAnchorKey(type)
  const currentAnchorDate = parseDateValue(successorRow[successorAnchorKey])
  const requiredDate = getDependencyRequiredDate(predecessorRow, dependency)
  if (!currentAnchorDate || !requiredDate) {
    return {
      changedTaskIds: new Set(),
      blocked: false,
    }
  }

  if (normalizeDateToScheduleMode(currentAnchorDate).getTime() >= requiredDate.getTime()) {
    return {
      changedTaskIds: new Set(),
      blocked: false,
    }
  }

  const requestedOffset = getScheduledOffsetDays(currentAnchorDate, requiredDate)
  const result = shiftTaskSubtreeByScheduledOffset(successorRow, requestedOffset)
  const refreshedSuccessorRow = findRowByTaskId(dependency?.successorTaskId) || successorRow
  const actualAnchorDate = parseDateValue(refreshedSuccessorRow[successorAnchorKey])
  const blocked = result.blocked
    || !actualAnchorDate
    || normalizeDateToScheduleMode(actualAnchorDate).getTime() < requiredDate.getTime()

  return {
    changedTaskIds: result.changedTaskIds,
    blocked,
  }
}

function propagateTaskDependencySchedule(taskIds, options = {}) {
  if (!canManageProject.value || !projectTaskDependencies.value.length) {
    return {
      changed: false,
      blocked: false,
      changedTaskIds: [],
    }
  }

  const queue = [...new Set(
    normalizeListResult(taskIds)
      .map((item) => String(item || '').trim())
      .filter(Boolean),
  )]
  if (!queue.length) {
    return {
      changed: false,
      blocked: false,
      changedTaskIds: [],
    }
  }

  const pendingTaskIds = new Set(queue)
  const changedTaskIds = new Set()
  const maxIterations = Math.max(projectTaskDependencies.value.length * Math.max(taskRows.value.length, 1), 50)
  let blocked = false
  let iterations = 0

  while (queue.length && iterations < maxIterations) {
    iterations += 1
    const currentTaskId = queue.shift()
    pendingTaskIds.delete(currentTaskId)

    const relatedDependencies = [
      ...getIncomingTaskDependencies(currentTaskId),
      ...getOutgoingTaskDependencies(currentTaskId),
    ]

    for (const dependency of relatedDependencies) {
      const result = applySingleDependencyScheduleRule(dependency)
      if (result.blocked) {
        blocked = true
      }
      for (const changedTaskId of result.changedTaskIds) {
        changedTaskIds.add(changedTaskId)
        if (!pendingTaskIds.has(changedTaskId)) {
          queue.push(changedTaskId)
          pendingTaskIds.add(changedTaskId)
        }
      }
    }
  }

  if (queue.length) {
    blocked = true
  }

  if (blocked && !options.silent) {
    ElMessage.warning('部分任务受项目日期或父任务范围限制，依赖关系未能完全推排到位')
  }

  return {
    changed: changedTaskIds.size > 0,
    blocked,
    changedTaskIds: [...changedTaskIds],
  }
}

function applyDependencyScheduleAfterTaskRowChange(row) {
  if (!canManageProject.value || !row) return
  const beforeSnapshot = captureTaskScheduleSnapshot()
  applyTaskConstraintSchedule(row)
  syncSummaryRows()
  const seedTaskIds = new Set(row.taskId ? [String(row.taskId)] : [])
  for (const changedTaskId of collectChangedTaskIdsFromScheduleSnapshot(beforeSnapshot)) {
    seedTaskIds.add(changedTaskId)
  }
  propagateTaskDependencySchedule([...seedTaskIds], { silent: true })
}

function buildTaskConstraintViolations() {
  return taskRows.value
    .filter((row) => !isBlankPlaceholderRow(row))
    .map((row) => {
      const constraint = getTaskConstraintSnapshot(row)
      if (!constraint.type) return null

      if (requiresTaskConstraintDate(constraint.type) && !constraint.date) {
        return {
          row,
          message: `任务“${row.name || row.taskId || '未命名任务'}”缺少约束日期`,
        }
      }

      const startDate = parseDateValue(row.start)
      const finishDate = parseDateValue(row.finish)
      const constraintDate = parseDateValue(constraint.date)
      if (!constraintDate) return null

      const normalizedStart = startDate ? normalizeDateToScheduleMode(startDate) : null
      const normalizedFinish = finishDate ? normalizeDateToScheduleMode(finishDate) : null

      if (constraint.type === 'SNET' && normalizedStart && normalizedStart < constraintDate) {
        return { row, message: `任务“${row.name || row.taskId || '未命名任务'}”违反“开始不早于 ${constraint.date}”约束` }
      }
      if (constraint.type === 'SNLT' && normalizedStart && normalizedStart > constraintDate) {
        return { row, message: `任务“${row.name || row.taskId || '未命名任务'}”违反“开始不晚于 ${constraint.date}”约束` }
      }
      if (constraint.type === 'FNET' && normalizedFinish && normalizedFinish < constraintDate) {
        return { row, message: `任务“${row.name || row.taskId || '未命名任务'}”违反“完成不早于 ${constraint.date}”约束` }
      }
      if (constraint.type === 'FNLT' && normalizedFinish && normalizedFinish > constraintDate) {
        return { row, message: `任务“${row.name || row.taskId || '未命名任务'}”违反“完成不晚于 ${constraint.date}”约束` }
      }
      if (constraint.type === 'MSO' && normalizedStart && normalizedStart.getTime() !== constraintDate.getTime()) {
        return { row, message: `任务“${row.name || row.taskId || '未命名任务'}”违反“必须开始于 ${constraint.date}”约束` }
      }
      if (constraint.type === 'MFO' && normalizedFinish && normalizedFinish.getTime() !== constraintDate.getTime()) {
        return { row, message: `任务“${row.name || row.taskId || '未命名任务'}”违反“必须完成于 ${constraint.date}”约束` }
      }

      return null
    })
    .filter(Boolean)
}

function ensureTaskConstraintScheduleConsistency() {
  const violations = buildTaskConstraintViolations()
  if (!violations.length) return
  throw new Error(violations[0].message)
}

function buildDependencyScheduleViolations() {
  return projectTaskDependencies.value
    .map((dependency) => {
      const predecessorRow = findRowByTaskId(dependency?.predecessorTaskId)
      const successorRow = findRowByTaskId(dependency?.successorTaskId)
      if (!predecessorRow || !successorRow) return null

      const type = normalizeDependencyTypeValue(dependency?.dependencyType)
      const successorAnchorKey = getDependencySuccessorAnchorKey(type)
      const currentAnchorDate = parseDateValue(successorRow[successorAnchorKey])
      const requiredDate = getDependencyRequiredDate(predecessorRow, dependency)
      if (!currentAnchorDate || !requiredDate) return null
      if (normalizeDateToScheduleMode(currentAnchorDate).getTime() >= requiredDate.getTime()) return null

      return {
        predecessorName: predecessorRow.name || dependency?.predecessorTaskName || `任务 ${dependency?.predecessorTaskId || ''}`,
        successorName: successorRow.name || dependency?.successorTaskName || `任务 ${dependency?.successorTaskId || ''}`,
        type,
        targetLabel: successorAnchorKey === 'finish' ? '完成日期' : '开始日期',
        requiredDate: formatDateToValue(requiredDate),
        currentDate: successorRow[successorAnchorKey] || '',
      }
    })
    .filter(Boolean)
}

function ensureDependencyScheduleConsistency() {
  if (!canManageProject.value) return
  const violations = buildDependencyScheduleViolations()
  if (!violations.length) return
  const first = violations[0]
  throw new Error(
    `存在未满足的任务依赖，请先调整：${first.predecessorName} -> ${first.successorName}（${first.type}，要求${first.targetLabel}不早于 ${first.requiredDate}，当前为 ${first.currentDate || '未设置'}）`,
  )
}

function captureTaskPlanningStateSnapshot() {
  return taskRows.value.map((row) => ({
    localId: row.localId,
    start: row.start,
    finish: row.finish,
    duration: row.duration,
    deadlineDate: row.deadlineDate,
    constraintType: row.constraintType,
    constraintDate: row.constraintDate,
  }))
}

function restoreTaskPlanningStateSnapshot(snapshot) {
  if (!Array.isArray(snapshot)) return
  const snapshotMap = new Map(snapshot.map((item) => [item.localId, item]))
  for (const row of taskRows.value) {
    const previous = snapshotMap.get(row.localId)
    if (!previous) continue
    row.start = previous.start
    row.finish = previous.finish
    row.duration = previous.duration
    row.deadlineDate = previous.deadlineDate
    row.constraintType = previous.constraintType
    row.constraintDate = previous.constraintDate
  }
}

function getPersistedTaskIds() {
  return [...new Set(
    taskRows.value
      .map((row) => String(row?.taskId || '').trim())
      .filter(Boolean),
  )]
}

function hasTaskScheduleSnapshotChanged(snapshot) {
  if (!(snapshot instanceof Map)) return false
  if (snapshot.size !== taskRows.value.length) return true
  return taskRows.value.some((row) => snapshot.get(row.localId) !== getTaskScheduleSignature(row))
}

function rescheduleProjectTasks(options = {}) {
  const planningSnapshot = captureTaskPlanningStateSnapshot()
  const beforeScheduleSnapshot = captureTaskScheduleSnapshot()
  const persistedTaskIds = getPersistedTaskIds()
  const maxIterations = Math.max(taskRows.value.length * Math.max(projectTaskDependencies.value.length, 1), 20)
  let blocked = false
  let stabilized = false
  let iterations = 0

  try {
    for (const row of taskRows.value) {
      if (!row || isBlankPlaceholderRow(row)) continue
      normalizeTaskRowDates(row)
      if (!isSummaryRow(row)) {
        normalizeTaskRowDuration(row)
      }
    }
    syncSummaryRows()

    while (iterations < maxIterations) {
      iterations += 1
      const iterationSnapshot = captureTaskScheduleSnapshot()

      for (const row of taskRows.value) {
        if (!row || isBlankPlaceholderRow(row)) continue
        const constraintResult = applyTaskConstraintSchedule(row)
        if (constraintResult.blocked) {
          blocked = true
        }
      }

      syncSummaryRows()

      if (persistedTaskIds.length) {
        const dependencyResult = propagateTaskDependencySchedule(persistedTaskIds, { silent: true })
        if (dependencyResult.blocked) {
          blocked = true
        }
      }

      syncSummaryRows()

      if (!hasTaskScheduleSnapshotChanged(iterationSnapshot)) {
        stabilized = true
        break
      }
    }

    const constraintViolations = buildTaskConstraintViolations()
    const dependencyViolations = buildDependencyScheduleViolations()
    const changed = hasTaskScheduleSnapshotChanged(beforeScheduleSnapshot)
    const changedTaskIds = [...collectChangedTaskIdsFromScheduleSnapshot(beforeScheduleSnapshot)]

    if (changed) {
      hasUnsavedChanges.value = true
    }

    return {
      changed,
      blocked: blocked || !stabilized || constraintViolations.length > 0 || dependencyViolations.length > 0,
      iterations,
      changedTaskIds,
      constraintViolations,
      dependencyViolations,
    }
  } catch (error) {
    restoreTaskPlanningStateSnapshot(planningSnapshot)
    throw error
  }
}

function rerunProjectSchedule() {
  if (!requireProjectManagePermission('重新推排项目计划')) return
  if (!taskRows.value.some((row) => hasRowContent(row) && !isBlankPlaceholderRow(row))) {
    ElMessage.warning('当前没有可推排的任务')
    return
  }

  const result = rescheduleProjectTasks()
  if (result.constraintViolations.length || result.dependencyViolations.length) {
    const firstMessage = result.constraintViolations[0]?.message
      || (result.dependencyViolations[0]
        ? `依赖未完全满足：${result.dependencyViolations[0].predecessorName} -> ${result.dependencyViolations[0].successorName}`
        : '')
    ElMessage.warning(firstMessage || '已尽量重新推排，但仍存在未解决的排程冲突')
    return
  }

  if (result.blocked) {
    ElMessage.warning('已尽量重新推排，但部分任务受项目日期或父任务范围限制，未能完全到位')
    return
  }

  if (result.changed) {
    ElMessage.success('项目任务已按约束和依赖重新推排，请继续保存任务计划')
    return
  }

  ElMessage.info('当前排程已是最新，无需重新推排')
}

function isDateDisabledByParent(row, date) {
  if (disabledScheduleModeDate(date)) {
    return true
  }
  const bounds = getRowDateBounds(row)
  if (!bounds) return false
  const value = formatDateToValue(date)
  return value < bounds.min || value > bounds.max
}

function syncDurationFromDates(row) {
  if (!row.start || !row.finish) return
  normalizeTaskRowDates(row)
  if (row.finish < row.start) row.finish = row.start
  row.duration = String(getScheduledDurationDays(parseDateValue(row.start), parseDateValue(row.finish)))
}

function syncFinishFromDuration(row) {
  const duration = parseDuration(row.duration)
  if (!row.start || duration === null) return
  row.start = normalizeDateValueToScheduleMode(row.start)
  row.finish = formatDateToValue(addScheduledDays(parseDateValue(row.start), Math.max(duration, 1) - 1))
  normalizeTaskRowDates(row)
}

function handleStartInput(row) {
  if (!row.start) return
  normalizeTaskRowDates(row)
  if (parseDuration(row.duration) !== null) syncFinishFromDuration(row)
  else if (row.finish) syncDurationFromDates(row)
  clampRowToBounds(row)
  if (row.start && row.finish) syncDurationFromDates(row)
  applyDependencyScheduleAfterTaskRowChange(row)
}

function handleFinishInput(row) {
  normalizeTaskRowDates(row)
  clampRowToBounds(row)
  if (row.start && row.finish) syncDurationFromDates(row)
  applyDependencyScheduleAfterTaskRowChange(row)
}

function handleDurationInput(row) {
  normalizeTaskRowDates(row)
  if (parseDuration(row.duration) !== null && row.start) syncFinishFromDuration(row)
  clampRowToBounds(row)
  if (row.start && row.finish) syncDurationFromDates(row)
  applyDependencyScheduleAfterTaskRowChange(row)
}

function buildTaskPayload(row, index) {
  const name = String(row.name || '').trim()
  if (!name) {
    throw new Error(`第 ${index + 1} 行任务名称不能为空`)
  }
  normalizeTaskRowDates(row)
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

  const loadedTask = row.taskId
    ? loadedTasks.value.find((task) => String(task.id || '') === String(row.taskId || '')) || null
    : null
  const scheduleMetadata = buildTaskScheduleMetadata(
    row.deadlineDate || loadedTask?.deadlineDate,
    row.constraintType || loadedTask?.constraintType,
    row.constraintDate || loadedTask?.constraintDate,
  )

  return {
    name,
    description: String(row.description || loadedTask?.description || name).trim() || name,
    parentTaskId: null,
    milestoneId: row.milestoneId || null,
    assigneeId: row.directAssigneeId || null,
    plannedStartDate: `${row.start}T00:00:00`,
    plannedEndDate: `${row.finish}T00:00:00`,
    deadlineDate: scheduleMetadata.deadlineDate,
    constraintType: scheduleMetadata.constraintType,
    constraintDate: scheduleMetadata.constraintDate,
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
  recordRecentProject({
    projectId: detail?.id || projectId.value,
    name: detail?.name || `项目${projectId.value}`,
  })
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

function getMilestonePlannedDateFromRow(row) {
  const date = row?.finish || row?.start || ''
  return date ? `${date}T00:00:00` : null
}

function deriveMilestoneStatusFromTaskRow(row) {
  const normalizedStatus = String(row?.status || '').toUpperCase()
  const progress = Number(row?.progress || 0)
  if (normalizedStatus === 'DONE' || progress >= 100) {
    return 'REACHED'
  }
  const plannedDate = row?.finish || row?.start || ''
  if (plannedDate && plannedDate < todayString) {
    return 'DELAYED'
  }
  return 'PENDING'
}

function buildMilestonePayloadFromTaskRow(row) {
  const loadedTask = row?.taskId
    ? loadedTasks.value.find((task) => String(task.id || '') === String(row.taskId || '')) || null
    : null
  const linkedMilestone = row?.milestoneId
    ? milestoneList.value.find((item) => String(item.id || '') === String(row.milestoneId || '')) || null
    : null
  return {
    name: String(row?.name || '').trim(),
    description: String(row?.description || loadedTask?.description || linkedMilestone?.description || '').trim() || null,
    plannedDate: normalizeDateTimeValue(getMilestonePlannedDateFromRow(row)),
  }
}

async function syncTaskMilestoneRow(row) {
  if (!isMilestoneRow(row) || !String(row?.name || '').trim()) return

  const payload = buildMilestonePayloadFromTaskRow(row)
  const status = deriveMilestoneStatusFromTaskRow(row)
  const linkedMilestone = row?.milestoneId
    ? milestoneList.value.find((item) => String(item.id || '') === String(row.milestoneId || '')) || null
    : null

  try {
    if (row.milestoneId) {
      await updateProjectMilestone(projectId.value, row.milestoneId, {
        ...payload,
        actualDate: status === 'REACHED' ? (linkedMilestone?.actualDate || payload.plannedDate) : null,
        status,
        ownerId: linkedMilestone?.ownerId || null,
      })
      row.milestoneName = payload.name
      return
    }
  } catch (error) {
    if (!String(error?.message || '').includes('milestone not found')) {
      throw error
    }
    row.milestoneId = null
    row.milestoneName = ''
  }

  const detail = await createProjectMilestone(projectId.value, payload)
  row.milestoneId = detail?.id != null ? String(detail.id) : row.milestoneId
  row.milestoneName = detail?.name || payload.name
}

async function syncProjectMilestonesFromTaskRows(rows) {
  const milestoneRows = rows.filter((row) => isMilestoneRow(row) && hasRowContent(row))
  for (const row of milestoneRows) {
    await syncTaskMilestoneRow(row)
  }
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
  const tasks = Array.isArray(pageResult?.list) ? pageResult.list.map(normalizeTaskProgressRecord) : []
  loadedTasks.value = tasks
  projectTaskDependencies.value = Array.isArray(dependencies) ? dependencies : []
  replaceTaskRows(tasks.map((task, index) => createRowFromTask(task, index)))
}

async function loadProjectMembersData() {
  projectMembers.value = normalizeListResult(await getProjectMembers(projectId.value))
}

async function loadAvailableUsersData() {
  const result = await getProjectMemberCandidates(projectId.value)
  availableUsers.value = normalizeListResult(result)
}

function resetProjectMemberForm(member = null) {
  projectMemberForm.memberId = member?.id ? String(member.id) : ''
  projectMemberForm.userId = member?.userId ? String(member.userId) : ''
  projectMemberForm.projectRole = member?.projectRole || 'READ_ONLY'
  projectMemberForm.memberStatus = member?.memberStatus || 'ACTIVE'
}

async function saveEditorContent() {
  if (!canSaveTaskPlan.value) {
    ElMessage.warning('当前没有可保存的任务计划内容')
    return
  }
  try {
    saving.value = true
    if (!canManageProject.value) {
      ensureTaskConstraintScheduleConsistency()
      const editableRows = taskRows.value.filter((row) => canEditTaskRow(row) && hasRowContent(row))
      for (const row of editableRows) {
        const index = taskRows.value.findIndex((item) => item.localId === row.localId)
        const payload = buildTaskPayload(row, index < 0 ? 0 : index)
        payload.parentTaskId = row.backendParentTaskId || null
        await updateProjectTask(projectId.value, row.taskId, payload)
      }
      await loadProjectTasksData()
      ElMessage.success('本人任务已保存')
      return
    }

    const persistedTaskIds = taskRows.value
      .map((row) => String(row.taskId || '').trim())
      .filter(Boolean)
    if (persistedTaskIds.length) {
      propagateTaskDependencySchedule(persistedTaskIds, { silent: true })
      ensureTaskConstraintScheduleConsistency()
      ensureDependencyScheduleConsistency()
    }

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

    await syncProjectMilestonesFromTaskRows(filledRows)

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

      const remainingMilestoneProjectionIds = new Set(
        filledRows
          .map((row) => String(row.milestoneId || ''))
          .filter(Boolean),
      )

      const removedMilestoneProjectionIds = [
        ...new Set(
          loadedTasks.value
            .filter((task) =>
              task?.taskType === 'MILESTONE_TASK'
              && task?.milestoneId != null
              && !persistedIds.has(String(task.id || ''))
              && !remainingMilestoneProjectionIds.has(String(task.milestoneId || '')))
            .map((task) => String(task.milestoneId || ''))
            .filter(Boolean),
        ),
      ]

      for (const milestoneId of removedMilestoneProjectionIds) {
        await deleteProjectMilestone(projectId.value, milestoneId)
      }

      await persistDraftCostPlans(filledRows)

      await loadProjectTasksData()
      await loadProjectMilestonesData()
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
  if (projectForm.startDate) {
    projectForm.startDate = normalizeDateValueToScheduleMode(projectForm.startDate)
  }
  if (projectForm.endDate) {
    projectForm.endDate = normalizeDateValueToScheduleMode(projectForm.endDate)
  }
  if (projectForm.endDate && projectForm.startDate && projectForm.endDate <= projectForm.startDate) {
    projectForm.endDate = ''
  }
}

function disabledStartDate(date) {
  if (date.getTime() < new Date(`${todayString}T00:00:00`).getTime()) {
    return true
  }
  return disabledScheduleModeDate(date)
}

function disabledEndDate(date) {
  if (disabledScheduleModeDate(date)) {
    return true
  }
  const minDate = normalizeDateValueToScheduleMode(projectForm.startDate || todayString)
  return date.getTime() <= new Date(`${minDate}T00:00:00`).getTime()
}

function disabledScheduleModeDate(date) {
  return isWorkdayScheduleMode() && isNonWorkingScheduleDate(date)
}

function disabledHolidayPickerDate(date) {
  return isWeekendDate(date)
}

async function openProjectInfo() {
  loading.value = true
  try {
    await loadProjectDetailData()
    normalizeProjectDates()
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
  if (!requireProjectManagePermission('调整项目成员')) return
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
  if (!requireProjectManagePermission('移除项目成员')) return
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
  if (!requireProjectManagePermission('修改项目信息')) return
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
  if (!requireProjectContentEditPermission('编辑项目章程')) return
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

async function loadPendingReviewTasks(options = {}) {
  const { silent = false } = options
  if (!silent) {
    pendingReviewLoading.value = true
  }
  try {
    const [pendingResult, allTaskResult] = await Promise.all([
      getProjectTasks(projectId.value, {
        page: 1,
        pageSize: 500,
        status: 'PENDING_REVIEW',
      }),
      getProjectTasks(projectId.value, {
        page: 1,
        pageSize: 500,
      }),
    ])
    const parentIds = new Set()
    const allTasks = Array.isArray(allTaskResult?.list) ? allTaskResult.list : []
    allTasks.forEach((item) => {
      if (item?.parentTaskId != null && item.parentTaskId !== '') {
        parentIds.add(String(item.parentTaskId))
      }
    })
    const tasks = Array.isArray(pendingResult?.list) ? pendingResult.list.map(normalizeTaskProgressRecord) : []
    pendingReviewTasks.value = tasks.filter((item) => item?.id != null && !parentIds.has(String(item.id)))
  } finally {
    if (!silent) {
      pendingReviewLoading.value = false
    }
  }
}

async function openPendingReviewDialog() {
  if (!requireProjectManagePermission('处理待验收任务')) {
    return
  }
  try {
    await loadPendingReviewTasks()
    pendingReviewDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '待验收任务加载失败')
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
    reportList.value = normalizeListResult(reports)
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

function resetQualityPlanForm(item = null) {
  editingQualityPlanId.value = item?.id || null
  qualityPlanForm.title = item?.title || ''
  qualityPlanForm.qualityStandard = item?.qualityStandard || ''
  qualityPlanForm.acceptanceRule = item?.acceptanceRule || ''
  qualityPlanForm.ownerId = item?.ownerId != null ? String(item.ownerId) : ''
  qualityPlanForm.status = item?.status || 'DRAFT'
}

function resetQualityActivityForm(item = null) {
  editingQualityActivityId.value = item?.id || null
  qualityActivityForm.qualityPlanId = item?.qualityPlanId != null ? String(item.qualityPlanId) : ''
  qualityActivityForm.activityName = item?.activityName || ''
  qualityActivityForm.activityType = item?.activityType || 'REVIEW'
  qualityActivityForm.plannedDate = toDateTimeInputValue(item?.plannedDate)
  qualityActivityForm.actualDate = toDateTimeInputValue(item?.actualDate)
  qualityActivityForm.result = item?.result || ''
  qualityActivityForm.ownerId = item?.ownerId != null ? String(item.ownerId) : ''
}

function resetQualityMetricForm(item = null) {
  editingQualityMetricId.value = item?.id || null
  qualityMetricForm.metricName = item?.metricName || ''
  qualityMetricForm.metricValue = item?.metricValue ?? ''
  qualityMetricForm.metricUnit = item?.metricUnit || ''
  qualityMetricForm.statisticDate = item?.statisticDate ? formatDateText(item.statisticDate) : ''
}

function resetTestPlanForm(item = null) {
  editingTestPlanId.value = item?.id || null
  testPlanForm.title = item?.title || ''
  testPlanForm.versionNo = item?.versionNo || ''
  testPlanForm.scopeDesc = item?.scopeDesc || ''
  testPlanForm.ownerId = item?.ownerId != null ? String(item.ownerId) : ''
  testPlanForm.status = item?.status || 'DRAFT'
}

function resetTestCaseForm(item = null) {
  editingTestCaseId.value = item?.id || null
  testCaseForm.testPlanId = item?.testPlanId != null ? String(item.testPlanId) : ''
  testCaseForm.requirementId = item?.requirementId != null ? String(item.requirementId) : ''
  testCaseForm.taskId = item?.taskId != null ? String(item.taskId) : ''
  testCaseForm.title = item?.title || ''
  testCaseForm.precondition = item?.precondition || ''
  testCaseForm.steps = item?.steps || ''
  testCaseForm.expectedResult = item?.expectedResult || ''
  testCaseForm.actualResult = item?.actualResult || ''
  testCaseForm.executionStatus = item?.executionStatus || 'NOT_RUN'
  testCaseForm.testerId = item?.testerId != null ? String(item.testerId) : ''
  testCaseForm.executedAt = toDateTimeInputValue(item?.executedAt)
}

function resetDefectForm(item = null) {
  editingDefectId.value = item?.id || null
  defectForm.testCaseId = item?.testCaseId != null ? String(item.testCaseId) : ''
  defectForm.requirementId = item?.requirementId != null ? String(item.requirementId) : ''
  defectForm.taskId = item?.taskId != null ? String(item.taskId) : ''
  defectForm.title = item?.title || ''
  defectForm.severity = item?.severity || 'MEDIUM'
  defectForm.priority = item?.priority || 'MEDIUM'
  defectForm.status = item?.status || 'NEW'
  defectForm.reporterId = item?.reporterId != null ? String(item.reporterId) : ''
  defectForm.assigneeId = item?.assigneeId != null ? String(item.assigneeId) : ''
  defectForm.description = item?.description || ''
  defectForm.resolution = item?.resolution || ''
}

function resetCommunicationMatrixForm(item = null) {
  editingCommunicationMatrixId.value = item?.id || null
  communicationMatrixForm.senderRole = item?.senderRole || ''
  communicationMatrixForm.receiverRole = item?.receiverRole || ''
  communicationMatrixForm.channel = item?.channel || 'MEETING'
  communicationMatrixForm.frequency = item?.frequency || ''
  communicationMatrixForm.topic = item?.topic || ''
}

function resetMeetingForm(item = null) {
  editingMeetingId.value = item?.id || null
  meetingForm.meetingType = item?.meetingType || 'DAILY'
  meetingForm.title = item?.title || ''
  meetingForm.scheduledAt = toDateTimeInputValue(item?.scheduledAt)
  meetingForm.durationMinutes = item?.durationMinutes != null ? String(item.durationMinutes) : '30'
  meetingForm.hostId = item?.hostId != null ? String(item.hostId) : ''
  meetingForm.location = item?.location || ''
  meetingForm.status = item?.status || 'PLANNED'
}

function resetCommunicationRecordForm(item = null) {
  editingCommunicationRecordId.value = item?.id || null
  communicationRecordForm.meetingId = item?.meetingId != null ? String(item.meetingId) : ''
  communicationRecordForm.recordType = item?.recordType || 'MEETING_MINUTES'
  communicationRecordForm.title = item?.title || ''
  communicationRecordForm.content = item?.content || ''
  communicationRecordForm.recorderId = item?.recorderId != null ? String(item.recorderId) : (currentUserId.value || '')
}

function resetConfigItemForm(item = null) {
  editingConfigItemId.value = item?.id || null
  configItemForm.itemName = item?.itemName || ''
  configItemForm.itemType = item?.itemType || 'DOC'
  configItemForm.versionNo = item?.versionNo || ''
  configItemForm.status = item?.status || 'DRAFT'
  configItemForm.repositoryUrl = item?.repositoryUrl || ''
  configItemForm.remark = item?.remark || ''
}

function resetBaselineForm() {
  baselineForm.baselineType = 'CONFIG'
  baselineForm.baselineName = ''
  baselineForm.description = ''
}

function resetSupplierForm(item = null) {
  editingSupplierId.value = item?.id || null
  supplierForm.supplierName = item?.supplierName || ''
  supplierForm.contactName = item?.contactName || ''
  supplierForm.contactPhone = item?.contactPhone || ''
  supplierForm.contactEmail = item?.contactEmail || ''
  supplierForm.address = item?.address || ''
  supplierForm.remark = item?.remark || ''
}

function resetProcurementForm(item = null) {
  editingProcurementId.value = item?.id || null
  procurementForm.supplierId = item?.supplierId != null ? String(item.supplierId) : ''
  procurementForm.costPlanId = item?.costPlanId != null ? String(item.costPlanId) : ''
  procurementForm.itemName = item?.itemName || ''
  procurementForm.quantity = Number(item?.quantity || 1)
  procurementForm.unitPrice = item?.unitPrice ?? ''
  procurementForm.status = item?.status || 'PLANNED'
  procurementForm.expectedDeliveryDate = item?.expectedDeliveryDate ? formatDateText(item.expectedDeliveryDate) : ''
  procurementForm.actualDeliveryDate = item?.actualDeliveryDate ? formatDateText(item.actualDeliveryDate) : ''
}

function resetProcurementStatusForm(item = null) {
  procurementStatusForm.procurementId = item?.id != null ? String(item.id) : ''
  procurementStatusForm.status = item?.status || 'PLANNED'
  procurementStatusForm.actualDeliveryDate = item?.actualDeliveryDate ? formatDateText(item.actualDeliveryDate) : ''
}

function resetContractForm(item = null) {
  editingContractId.value = item?.id || null
  contractForm.supplierId = item?.supplierId != null ? String(item.supplierId) : ''
  contractForm.contractNo = item?.contractNo || ''
  contractForm.contractName = item?.contractName || ''
  contractForm.contractType = item?.contractType || 'FIXED_PRICE'
  contractForm.signDate = item?.signDate ? formatDateText(item.signDate) : ''
  contractForm.totalAmount = item?.totalAmount ?? ''
  contractForm.deliverables = item?.deliverables || ''
  contractForm.paymentTerms = item?.paymentTerms || ''
  contractForm.status = item?.status || 'DRAFT'
}

function resetTestReportGenerateForm() {
  testReportGenerateForm.type = 'TEST'
  testReportGenerateForm.startDate = ''
  testReportGenerateForm.endDate = ''
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

function toNullableIdValue(value) {
  if (value == null) return null

  const normalized = String(value).trim()
  return normalized || null
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
  if (authStore.user) {
    return authStore.user
  }
  try {
    return JSON.parse(localStorage.getItem('pm_auth_user') || 'null')
  } catch {
    return null
  }
})

const currentUserId = computed(() => currentAuthUser.value?.id ? String(currentAuthUser.value.id) : '')
const currentUserDisplayName = computed(() =>
  currentAuthUser.value?.realName || currentAuthUser.value?.username || '当前用户')

const activeProjectMembers = computed(() => projectMembers.value.filter((item) =>
  item?.memberStatus === 'ACTIVE' && editableProjectRoleCodes.has(item?.projectRole)))

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
      projectRole: 'OWNER',
      memberStatus: 'ACTIVE',
      joinedAt: projectDetail.value?.createdAt || null,
      isProjectOwner: true,
      isVirtualOwner: true,
    })
  } else {
    members.forEach((item) => {
      if (String(item.userId || '') === ownerId) {
        item.isProjectOwner = true
        item.projectRole = 'OWNER'
      }
    })
  }
  return members
})

const loadedTaskParentIds = computed(() => {
  const ids = new Set()
  loadedTasks.value.forEach((item) => {
    if (item?.parentTaskId != null && item.parentTaskId !== '') {
      ids.add(String(item.parentTaskId))
    }
  })
  return ids
})

function isLeafTaskId(taskId) {
  const normalizedTaskId = String(taskId || '')
  if (!normalizedTaskId) return false
  return !loadedTaskParentIds.value.has(normalizedTaskId)
}

const pendingReviewTaskCount = computed(() => {
  if (pendingReviewDialogVisible.value) {
    return pendingReviewTasks.value.length
  }
  return loadedTasks.value.filter((item) => item?.status === 'PENDING_REVIEW' && isLeafTaskId(item.id)).length
})

const currentProjectMember = computed(() => {
  if (!currentUserId.value) return null
  return projectMembers.value.find((item) =>
    String(item.userId || '') === currentUserId.value && item.memberStatus === 'ACTIVE') || null
})

const isProjectOwner = computed(() => {
  if (!currentUserId.value || !projectDetail.value?.ownerId) return false
  return String(projectDetail.value.ownerId) === currentUserId.value
})

const currentProjectRoleCode = computed(() => {
  if (isProjectOwner.value) {
    return 'OWNER'
  }
  return currentProjectMember.value?.projectRole || ''
})

const canManageProject = computed(() => currentProjectRoleCode.value === 'OWNER')
const isClosedProject = computed(() => projectDetail.value?.status === 'CLOSED')
const canEditProjectContent = computed(() =>
  editableProjectRoleCodes.has(currentProjectRoleCode.value)
  && !isClosedProject.value)
const canSubmitChangeRequest = computed(() => canEditProjectContent.value && !canManageProject.value)
const canApproveChangeRequest = computed(() => canManageProject.value)
const changeRequestDialogTitle = computed(() => {
  if (canApproveChangeRequest.value) return '变更审批'
  if (canSubmitChangeRequest.value) return '变更申请'
  return '变更记录'
})

const canEditTaskPlan = computed(() => canManageProject.value && !isClosedProject.value)

const canManageTaskBasic = computed(() => canManageProject.value && !isClosedProject.value)

const canEditTaskBasic = computed(() => canEditTaskRow(selectedTaskRow.value))

const canEditTaskDependency = computed(() => canManageProject.value && !isClosedProject.value)

const selectedTaskAssigneeLocked = computed(() =>
  !canManageTaskBasic.value)

const selectedTaskAssigneePlaceholder = computed(() => {
  if (selectedTaskAssigneeLocked.value) {
    return '仅项目负责人可调整任务负责人'
  }
  if (selectedTaskRow.value?.backendParentTaskId) {
    return '不设置则继承父任务负责人'
  }
  return '从项目成员中选择负责人'
})

const canSaveTaskPlan = computed(() => {
  if (canEditTaskPlan.value) return true
  if (!canEditProjectContent.value || isClosedProject.value || !currentUserId.value) return false
  return taskRows.value.some((row) => canEditTaskRow(row))
})

const canCommentOnTasks = computed(() => canEditProjectContent.value)

const canUseTimesheet = computed(() => canEditProjectContent.value)

const canUpdateTaskProgressByRole = computed(() => canEditProjectContent.value)

const visibleTabs = computed(() => allTabs)

const effectiveTaskMineOnly = computed(() => taskMineOnly.value)
const taskViewModeLabel = computed(() => effectiveTaskMineOnly.value ? '我的任务' : '项目任务总览')
const taskViewModeHint = computed(() =>
  effectiveTaskMineOnly.value
    ? '只看分配给你的任务'
    : '查看项目全部任务')

const visibleTaskDetailSections = computed(() => allTaskDetailSections)

const selectedTaskCanUpdateProgress = computed(() => {
  if (!selectedTaskRow.value || !canUpdateTaskProgressByRole.value) return false
  if (canManageProject.value) return true
  if (selectedTaskWorkflowStatus.value === 'DONE') return false
  return getEffectiveRowAssigneeId(selectedTaskRow.value) === currentUserId.value
})

const selectedTaskWorkflowStatus = computed(() =>
  taskDetail.value?.status || selectedTaskRow.value?.status || 'TODO')

const selectedTaskSubmitProgress = computed(() => {
  const detailMatchesSelected = selectedTaskRow.value?.taskId
    && taskDetail.value?.id != null
    && String(taskDetail.value.id) === String(selectedTaskRow.value.taskId)

  if (taskInfoDialogVisible.value && taskDetailSection.value === 'progress' && detailMatchesSelected) {
    return clampTaskProgressValue(taskProgressForm.progress)
  }

  return clampTaskProgressValue(taskDetail.value?.progress ?? selectedTaskRow.value?.progress ?? 0)
})

const selectedTaskIsLeaf = computed(() => {
  if (!selectedTaskRow.value) return false
  const detailMatchesSelected = taskDetail.value?.id != null
    && String(taskDetail.value.id) === String(selectedTaskRow.value.taskId || '')
  if (detailMatchesSelected && Number(taskDetail.value?.childTaskCount || 0) > 0) return false
  return !hasChildRows(selectedTaskRow.value)
})

const selectedTaskCanSubmitReview = computed(() => {
  if (!selectedTaskCanUpdateProgress.value || canManageProject.value || !selectedTaskIsLeaf.value) return false
  return selectedTaskWorkflowStatus.value !== 'DONE'
    && selectedTaskWorkflowStatus.value !== 'PENDING_REVIEW'
    && selectedTaskSubmitProgress.value >= 100
})

const selectedTaskCanApproveReview = computed(() =>
  canManageProject.value
  && selectedTaskIsLeaf.value
  && selectedTaskWorkflowStatus.value === 'PENDING_REVIEW')

const selectedTaskCanReopen = computed(() =>
  canManageProject.value
  && selectedTaskIsLeaf.value
  && selectedTaskWorkflowStatus.value === 'DONE')

const selectedTaskSubmitBlockedReason = computed(() => {
  if (activeTab.value !== 'task' || !selectedTaskRow.value || isBlankPlaceholderRow(selectedTaskRow.value)) {
    return '请先选中一条有效任务'
  }
  if (selectedTaskCanSubmitReview.value) return ''
  if (canManageProject.value) {
    return '你当前是项目负责人，负责人不点“提交完成”，而是等成员提交后做验收'
  }
  if (!selectedTaskIsLeaf.value) {
    return '当前是父任务，只有最底层的具体执行任务才能提交完成'
  }
  if (selectedTaskWorkflowStatus.value === 'PENDING_REVIEW') {
    return '当前任务已经提交验收，正在等待负责人处理'
  }
  if (selectedTaskWorkflowStatus.value === 'DONE') {
    return '当前任务已经验收完成，如需继续处理请联系项目负责人重开'
  }
  if (getEffectiveRowAssigneeId(selectedTaskRow.value) !== currentUserId.value) {
    return '当前任务不是分配给你的，所以不能由你提交完成'
  }
  if (selectedTaskSubmitProgress.value < 100) {
    return `当前进度 ${formatPercentValue(selectedTaskSubmitProgress.value)}，请先更新到 100% 后再提交完成`
  }
  return '当前任务暂时不能提交完成'
})

const selectedTaskApproveBlockedReason = computed(() => {
  if (!canManageProject.value) {
    return '只有项目负责人才能验收任务'
  }
  if (selectedTaskCanApproveReview.value) return ''
  if (!selectedTaskIsLeaf.value) {
    return '当前是父任务，不能直接验收，请处理底层具体任务'
  }
  if (selectedTaskWorkflowStatus.value === 'DONE') {
    return '当前任务已经验收完成；如需继续处理，可由项目负责人直接重开'
  }
  if (selectedTaskWorkflowStatus.value !== 'PENDING_REVIEW') {
    return '当前任务还没有被成员提交为待验收状态'
  }
  return '当前任务暂时不能验收'
})

const selectedTaskActionHint = computed(() => {
  if (activeTab.value !== 'task' || !selectedTaskRow.value || isBlankPlaceholderRow(selectedTaskRow.value)) {
    return ''
  }
  if (selectedTaskCanSubmitReview.value) {
    return '当前进度已到 100%，可以直接点右侧“提交完成”提交给负责人验收。'
  }
  if (selectedTaskCanApproveReview.value) {
    return '这条任务已经提交验收，右侧可以直接通过或打回。'
  }
  if (selectedTaskCanReopen.value) {
    return '该任务已验收完成；如需继续处理，可由项目负责人直接重开。'
  }
  if (!canManageProject.value && selectedTaskWorkflowStatus.value === 'DONE') {
    return '该任务已验收完成，成员不能继续编辑；如需继续处理请联系项目负责人重开。'
  }
  if (!canManageProject.value && selectedTaskSubmitProgress.value < 100) {
    return `当前进度是 ${formatPercentValue(selectedTaskSubmitProgress.value)}，到 100% 后才能提交完成。`
  }
  if (canManageProject.value && pendingReviewTaskCount.value > 0) {
    return `你当前是项目负责人；“提交完成”只给成员使用，当前还有 ${pendingReviewTaskCount.value} 条待验收任务可直接处理。`
  }
  if (canManageProject.value) {
    return selectedTaskApproveBlockedReason.value || '你当前是项目负责人；成员提交后你直接在这里验收。'
  }
  return selectedTaskSubmitBlockedReason.value || '可先打开进度页查看当前进展和处理说明。'
})

const taskProgressDisplayStatus = computed(() =>
  deriveTaskProgressEditorStatus(taskProgressForm.progress, selectedTaskWorkflowStatus.value))

const taskProgressWorkflowHint = computed(() => {
  if (selectedTaskCanApproveReview.value) {
    return '该任务已提交完成，项目创建者验收通过后才算真正完成并计入主任务完成。'
  }
  if (selectedTaskCanReopen.value) {
    return '该任务已验收完成，项目负责人可直接重开；如需换人可重新分配。'
  }
  if (!canManageProject.value && selectedTaskWorkflowStatus.value === 'DONE') {
    return '该任务已验收完成，当前只有项目负责人可以重开或重新分配。'
  }
  if (!canManageProject.value && selectedTaskSubmitProgress.value < 100) {
    return `当前进度是 ${formatPercentValue(selectedTaskSubmitProgress.value)}，请先更新到 100% 后再提交完成。`
  }
  if (!canManageProject.value && selectedTaskWorkflowStatus.value === 'PENDING_REVIEW') {
    return '该任务已提交验收，等待项目创建者处理；如需继续修改，可调整进度后重新保存。'
  }
  if (selectedTaskCanSubmitReview.value) {
    return '成员完成后请点“提交完成”，项目创建者验收通过后才会并入主任务。'
  }
  return '更新后会直接影响 EVM 的 EV / SPI / CPI 计算。'
})

function requireProjectManagePermission(actionLabel = '管理当前项目') {
  if (canManageProject.value) {
    return true
  }
  ElMessage.warning(`只有创建者可以${actionLabel}`)
  return false
}

function requireTaskBasicEditPermission(actionLabel = '修改任务基础信息') {
  if (canEditTaskBasic.value) {
    return true
  }
  ElMessage.warning(`只有创建者或当前负责人可以${actionLabel}`)
  return false
}

function canEditTaskRow(row) {
  if (!row || isClosedProject.value) return false
  if (canManageProject.value) return true
  if (!canEditProjectContent.value || !currentUserId.value) return false
  if (!row.taskId || isBlankPlaceholderRow(row)) return false
  if (row.status === 'DONE') return false
  return getEffectiveRowAssigneeId(row) === currentUserId.value
}

function canEditTaskField(row, field) {
  if (!row) return false
  if (field === 'mode') {
    return canEditTaskPlan.value
  }
  return canEditTaskRow(row)
}

function getEffectiveRowAssigneeId(row) {
  if (!row) return ''
  if (row.status === 'DONE') return ''
  if (row.directAssigneeId) {
    return String(row.directAssigneeId)
  }
  if (row.localParentId) {
    const parentRow = taskRows.value.find((item) => item.localId === row.localParentId) || null
    return getEffectiveRowAssigneeId(parentRow)
  }
  return String(row.assigneeId || '')
}

function requireProjectContentEditPermission(actionLabel = '编辑当前项目内容') {
  if (canEditProjectContent.value) {
    return true
  }
  ElMessage.warning(`当前项目身份不能${actionLabel}`)
  return false
}

function canDeleteTaskComment(item) {
  if (!canCommentOnTasks.value) return false
  if (canManageProject.value) return true
  if (!currentUserId.value) return false
  return String(item?.userId || '') === currentUserId.value
}

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

const qualityPlanOptions = computed(() => qualityPlans.value.map((item) => ({
  value: String(item.id),
  label: item.title || `质量计划${item.id}`,
})))

const requirementOptions = computed(() => requirements.value.map((item) => ({
  value: String(item.id),
  label: `${item.requirementCode || '-'} | ${item.title || `需求${item.id}`}`,
})))

const testPlanOptions = computed(() => testPlans.value.map((item) => ({
  value: String(item.id),
  label: `${item.title || `测试计划${item.id}`}${item.versionNo ? ` | ${item.versionNo}` : ''}`,
})))

const testCaseOptions = computed(() => testCases.value.map((item) => ({
  value: String(item.id),
  label: `${item.caseCode || '-'} | ${item.title || `测试用例${item.id}`}`,
  requirementId: item.requirementId != null ? String(item.requirementId) : '',
  taskId: item.taskId != null ? String(item.taskId) : '',
})))

const meetingOptions = computed(() => meetingList.value.map((item) => ({
  value: String(item.id),
  label: `${item.title || `会议${item.id}`}${item.scheduledAt ? ` | ${formatDateTimeText(item.scheduledAt)}` : ''}`,
})))

const filteredCommunicationRecordList = computed(() => {
  if (!communicationRecordFilterMeetingId.value) {
    return communicationRecordList.value
  }
  return communicationRecordList.value.filter((item) => String(item?.meetingId || '') === String(communicationRecordFilterMeetingId.value))
})

const communicationRecordFilterMeetingLabel = computed(() => (
  meetingOptions.value.find((item) => item.value === String(communicationRecordFilterMeetingId.value || ''))?.label || ''
))

const supplierOptions = computed(() => supplierList.value.map((item) => ({
  value: String(item.id),
  label: item.supplierName || `供应商${item.id}`,
})))

const procurementCostPlanOptions = computed(() => costPlans.value
  .filter((item) => item?.id != null && Number.isFinite(Number(item.id)))
  .map((item) => ({
    value: String(item.id),
    label: `${item.name || `预算项${item.id}`}${item.type ? ` | ${item.type}` : ''}`,
  })))

const procurementOptions = computed(() => procurementList.value.map((item) => ({
  value: String(item.id),
  label: `${item.itemName || `采购项${item.id}`}${item.supplierName ? ` | ${item.supplierName}` : ''}`,
})))

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

const selectedReportPreviewContent = computed(() => parseReportContent(selectedReportPreview.value?.contentJson))
const activeReportPreviewTab = ref('overview')
const availableReportPreviewTabs = computed(() => {
  const content = selectedReportPreviewContent.value
  if (!isRichReportContent(content)) return []
  return [
    { key: 'overview', label: '概览', visible: true },
    {
      key: 'task',
      label: '任务',
      visible: Boolean(
        content?.taskSummary
          || content?.criticalTasks?.length
          || content?.taskDetails?.length
          || content?.overdueTasks?.length
          || content?.milestoneReport?.length,
      ),
    },
    {
      key: 'resource',
      label: '资源',
      visible: Boolean(content?.resourceOverview?.length || content?.resourceAssignments?.length),
    },
    {
      key: 'cost',
      label: '成本',
      visible: Boolean(content?.costOverview || content?.costDetails?.length || content?.costCashFlow?.length),
    },
    {
      key: 'schedule',
      label: '进度/EVM',
      visible: Boolean(
        content?.upcomingTasks?.length
          || content?.upcomingMilestones?.length
          || content?.scheduleComparison?.length
          || content?.baselineSnapshots?.latestScopeBaseline
          || content?.baselineSnapshots?.latestCostBaseline
          || content?.evmOverview
          || content?.evmDetails?.length,
      ),
    },
    {
      key: 'risk',
      label: '风险/变更',
      visible: Boolean(content?.openRisks?.length || content?.pendingChanges?.length),
    },
    {
      key: 'summary',
      label: '总结',
      visible: Boolean(content?.archives?.length || content?.lessonsLearned?.length),
    },
  ].filter((item) => item.visible)
})

watch(availableReportPreviewTabs, (tabs) => {
  if (!tabs.length) {
    activeReportPreviewTab.value = 'overview'
    return
  }
  if (!tabs.some((item) => item.key === activeReportPreviewTab.value)) {
    activeReportPreviewTab.value = tabs[0].key
  }
}, { immediate: true })

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
  if (!canEditTimesheetRecord(item)) {
    ElMessage.warning('只能编辑自己登记的工时，创建者可编辑全部工时')
    return
  }
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
  if (role === 'OWNER') return '创建者'
  if (['PARTICIPANT', 'PROJECT_MANAGER', 'TEAM_MEMBER'].includes(role)) return '可编辑成员'
  if (role === 'READ_ONLY') return '只读成员'
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

function canEditTimesheetRecord(item) {
  if (canManageProject.value) return true
  if (!currentUserId.value) return false
  return String(item?.userId || '') === currentUserId.value
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

function normalizeReportTitle(title) {
  const map = {
    '椤圭洰鍛ㄦ姤': '项目周报',
    '椤圭洰鏈堟姤': '项目月报',
    '椤圭洰鎬荤粨鎶ュ憡': '项目总结报告',
  }
  return map[title] || title || '-'
}

function formatReportType(reportType, title = '') {
  const normalizedTitle = normalizeReportTitle(title)
  if (normalizedTitle.includes('周报')) return '周报'
  if (normalizedTitle.includes('月报')) return '月报'
  if (normalizedTitle.includes('总结报告')) return '总结报告'

  const map = {
    PERIODIC: '周期报表',
    SUMMARY: '总结报告',
    WEEKLY: '周报',
    MONTHLY: '月报',
  }
  return map[reportType] || reportType || '-'
}

function formatReportStatus(status) {
  const map = {
    GENERATED: '已生成',
    PENDING: '待生成',
    FAILED: '生成失败',
    ARCHIVED: '已归档',
  }
  return map[status] || status || '-'
}

function formatDateTimeText(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 19)
}

function toDateTimeInputValue(value) {
  if (!value) return ''
  return String(value).replace(' ', 'T').slice(0, 19)
}

function formatDateText(value) {
  if (!value) return '-'
  return String(value).replace('T', ' ').slice(0, 10)
}

function formatPercentValue(value) {
  const amount = Number(value ?? 0)
  if (!Number.isFinite(amount)) return '-'
  return `${amount.toFixed(amount % 1 === 0 ? 0 : 2)}%`
}

function formatPriorityText(priority) {
  return changePriorityOptions.find((item) => item.value === priority)?.label || priority || '-'
}

function formatTaskConstraintType(type) {
  return taskConstraintTypeOptions.find((item) => item.value === (type || ''))?.label || type || '无约束'
}

function formatDependencyLagText(lagDays) {
  const value = Number(lagDays || 0)
  if (!Number.isFinite(value) || value === 0) return '无偏移'
  return value > 0 ? `滞后 ${value} 天` : `提前 ${Math.abs(value)} 天`
}

function formatArchiveType(type) {
  return archiveTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function formatCalendarEventType(type) {
  const map = {
    TASK: '任务',
    MILESTONE: '里程碑',
    MEETING: '会议',
    PROCUREMENT_DELIVERY: '采购交付',
  }
  return map[type] || type || '-'
}

function formatArchiveStatus(status) {
  const map = {
    ARCHIVED: '已归档',
  }
  return map[status] || status || '-'
}

function formatMilestoneStatus(status) {
  const map = {
    PENDING: '待处理',
    REACHED: '已达成',
    DELAYED: '已延期',
  }
  return map[status] || status || '-'
}

function parseReportContent(contentJson) {
  if (!contentJson) return null
  if (typeof contentJson === 'object') return contentJson
  try {
    return JSON.parse(contentJson)
  } catch {
    return { _raw: String(contentJson) }
  }
}

function isRichReportContent(content) {
  return Number(content?.version || 0) >= 2
}

function formatReportPeriodText(item, content = null) {
  const period = content?.period || {}
  const start = formatDateText(period.startDate || item?.startDate)
  const end = formatDateText(period.endDate || item?.endDate)
  if (start === '-' && end === '-') return '未设置'
  return `${start} ~ ${end}`
}

function formatReportJsonContent(content) {
  if (!content) return ''
  try {
    return JSON.stringify(content, null, 2)
  } catch {
    return String(content)
  }
}

function openReportPreview(item) {
  if (!item) return
  selectedReportPreview.value = item
  activeReportPreviewTab.value = 'overview'
  reportPreviewDialogVisible.value = true
}

function selectReportPreviewTab(key) {
  activeReportPreviewTab.value = key
}

function scrollReportCriticalTimeline(direction) {
  const container = reportCriticalTimelineRef.value
  if (!container) return
  const offset = Math.max(container.clientWidth * 0.75, 320) * direction
  container.scrollBy({
    left: offset,
    behavior: 'smooth',
  })
}

function getReportTaskTimelineClass(status) {
  return {
    'report-preview-task-node-todo': status === 'TODO',
    'report-preview-task-node-active': status === 'IN_PROGRESS',
    'report-preview-task-node-review': status === 'PENDING_REVIEW',
    'report-preview-task-node-done': status === 'DONE',
    'report-preview-task-node-blocked': status === 'BLOCKED',
  }
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

function formatDecimalText(value, digits = 2) {
  const amount = Number(value ?? 0)
  if (!Number.isFinite(amount)) return '-'
  return amount.toFixed(digits)
}

function formatHoursText(value) {
  const amount = Number(value ?? 0)
  if (!Number.isFinite(amount)) return '-'
  return `${amount.toFixed(amount % 1 === 0 ? 0 : 2)} h`
}

function formatDaysText(value) {
  const amount = Number(value ?? 0)
  if (!Number.isFinite(amount)) return '-'
  return `${amount} 天`
}

function formatListText(value) {
  if (Array.isArray(value)) {
    return value.length ? value.join('、') : '-'
  }
  return value || '-'
}

function formatYesNo(value) {
  return value ? '是' : '否'
}

function formatTaskStatus(status) {
  const statusMap = {
    TODO: '未开始',
    IN_PROGRESS: '进行中',
    PENDING_REVIEW: '待验收',
    DONE: '已完成',
    BLOCKED: '阻塞',
  }
  return statusMap[status] || status || '-'
}

function resetTaskProgressForm(detail = null) {
  const normalized = normalizeTaskProgressState(
    detail?.progress ?? selectedTaskRow.value?.progress ?? 0,
    detail?.status || selectedTaskRow.value?.status || 'TODO',
  )
  taskProgressForm.status = normalized.status
  taskProgressForm.progress = normalized.progress
  taskProgressForm.remark = ''
}

function syncTaskProgressFormFromProgress() {
  if (syncingTaskProgressForm) return
  syncingTaskProgressForm = true
  const nextProgress = clampTaskProgressValue(taskProgressForm.progress)
  taskProgressForm.progress = nextProgress
  const currentWorkflowStatus = selectedTaskWorkflowStatus.value
  if (currentWorkflowStatus === 'PENDING_REVIEW' && nextProgress >= 100) {
    taskProgressForm.status = 'PENDING_REVIEW'
  } else if (currentWorkflowStatus === 'DONE' && nextProgress >= 100) {
    taskProgressForm.status = 'DONE'
  } else if (nextProgress <= 0) {
    taskProgressForm.status = 'TODO'
  } else if (nextProgress >= 100) {
    taskProgressForm.status = canManageProject.value ? 'DONE' : 'IN_PROGRESS'
  } else {
    taskProgressForm.status = 'IN_PROGRESS'
  }
  syncingTaskProgressForm = false
}

function resetTaskBasicForm(detail = null) {
  taskBasicForm.description = detail?.description || selectedTaskRow.value?.name || ''
  taskBasicForm.milestoneId = detail?.milestoneId != null ? String(detail.milestoneId) : ''
  taskBasicForm.assigneeId = detail?.directAssigneeId != null
    ? String(detail.directAssigneeId)
    : (selectedTaskRow.value?.directAssigneeId || '')
  taskBasicForm.deadlineDate = detail?.deadlineDate ? formatDateText(detail.deadlineDate) : (selectedTaskRow.value?.deadlineDate || '')
  taskBasicForm.constraintType = detail?.constraintType || selectedTaskRow.value?.constraintType || ''
  taskBasicForm.constraintDate = detail?.constraintDate ? formatDateText(detail.constraintDate) : (selectedTaskRow.value?.constraintDate || '')
}

function resetTaskDependencyForm() {
  taskDependencyForm.predecessorTaskId = ''
  taskDependencyForm.dependencyType = 'FS'
  taskDependencyForm.lagDays = 0
}

function handleTaskConstraintTypeChange(value) {
  taskBasicForm.constraintType = normalizeTaskConstraintTypeValue(value) || ''
  if (!requiresTaskConstraintDate(taskBasicForm.constraintType)) {
    taskBasicForm.constraintDate = ''
  }
}

function resetTaskCommentForm() {
  taskCommentForm.content = ''
  taskCommentForm.replyToId = ''
  taskCommentForm.replyToName = ''
}

function resetMilestoneForm() {
  editingMilestoneId.value = null
  editingMilestoneTaskLocalId.value = null
  milestoneForm.name = `里程碑${visibleMilestoneList.value.length + 1}`
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
  editingMilestoneTaskLocalId.value = item?.isTaskBased && item?.localId != null ? String(item.localId) : null
  editingMilestoneId.value = item?.isLegacyProjection
    ? String(item.milestoneId || item.id || '')
    : (item?.isTaskBased && item?.milestoneId ? String(item.milestoneId) : null)
  milestoneForm.name = item?.name || ''
  milestoneForm.description = item?.description || ''
  milestoneForm.plannedDate = item?.plannedDate ? String(item.plannedDate).slice(0, 10) : ''
  milestoneForm.status = item?.status || 'PENDING'
}

function normalizeDateTimeValue(value) {
  if (!value) return null
  const raw = String(value).trim()
  if (!raw) return null
  const normalized = raw.replace(' ', 'T')
  return normalized.length === 16 ? `${normalized}:00` : normalized
}

function normalizeDateFieldValue(value) {
  if (!value) return null
  const raw = String(value).trim()
  if (!raw) return null
  return raw.length === 10 ? `${raw}T00:00:00` : normalizeDateTimeValue(raw)
}

function normalizeDateFieldValueToScheduleMode(value) {
  if (!value) return null
  const raw = String(value).trim()
  if (!raw) return null
  const normalizedDate = normalizeDateValueToScheduleMode(raw.slice(0, 10))
  return normalizedDate ? `${normalizedDate}T00:00:00` : null
}

function normalizeTaskConstraintTypeValue(value) {
  const raw = String(value || '').trim().toUpperCase()
  return raw || null
}

function requiresTaskConstraintDate(type) {
  return ['SNET', 'SNLT', 'FNET', 'FNLT', 'MSO', 'MFO'].includes(String(type || '').toUpperCase())
}

function buildTaskScheduleMetadata(deadlineDate, constraintType, constraintDate) {
  const normalizedConstraintType = normalizeTaskConstraintTypeValue(constraintType)
  return {
    deadlineDate: normalizeDateFieldValueToScheduleMode(deadlineDate),
    constraintType: normalizedConstraintType,
    constraintDate: requiresTaskConstraintDate(normalizedConstraintType)
      ? normalizeDateFieldValueToScheduleMode(constraintDate)
      : null,
  }
}

function normalizeMilestonePlannedDateValue(value) {
  return normalizeDateFieldValue(value)
}

function findMilestoneTaskRow(localId) {
  if (localId == null || localId === '') return null
  return taskRows.value.find((row) => String(row.localId || '') === String(localId)) || null
}

function applyMilestoneFormToTaskRow(row) {
  if (!row) return
  const normalizedName = milestoneForm.name.trim()
  row.mode = '里程碑'
  row.taskType = 'MILESTONE_TASK'
  row.name = normalizedName
  row.description = milestoneForm.description.trim()
  row.start = milestoneForm.plannedDate || ''
  row.finish = milestoneForm.plannedDate || ''
  row.duration = '0'
  row.milestoneName = normalizedName
}

function buildMilestoneTaskRowFromForm() {
  const row = createEmptyRow('里程碑', false)
  applyMilestoneFormToTaskRow(row)
  if (editingMilestoneId.value) {
    row.milestoneId = String(editingMilestoneId.value)
  }
  return row
}

function countMilestoneLinkedTaskRows(milestoneId, excludedLocalId = null) {
  if (!milestoneId) return 0
  return taskRows.value.filter((row) => {
    if (!hasRowContent(row) || !row.milestoneId) return false
    if (excludedLocalId != null && String(row.localId || '') === String(excludedLocalId)) return false
    return String(row.milestoneId || '') === String(milestoneId)
  }).length
}

function formatRiskLevel(level) {
  return riskLevelOptions.find((item) => item.value === level)?.label || level || '-'
}

function formatRiskStatus(status) {
  return riskStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatQualityPlanStatus(status) {
  return qualityPlanStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatQualityActivityType(type) {
  return qualityActivityTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function formatTestPlanStatus(status) {
  return testPlanStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatTestCaseStatus(status) {
  return testCaseStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatDefectStatus(status) {
  return defectStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatDefectSeverity(severity) {
  return defectSeverityOptions.find((item) => item.value === severity)?.label || severity || '-'
}

function formatCommunicationChannel(channel) {
  return communicationChannelOptions.find((item) => item.value === channel)?.label || channel || '-'
}

function formatMeetingType(type) {
  return meetingTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function formatMeetingStatus(status) {
  return meetingStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatCommunicationRecordType(type) {
  return communicationRecordTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function formatConfigItemType(type) {
  return configItemTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function formatConfigItemStatus(status) {
  return configItemStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatBaselineType(type) {
  return baselineTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function formatBaselineStatus(status) {
  return baselineStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatProcurementStatus(status) {
  return procurementStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function formatContractType(type) {
  return contractTypeOptions.find((item) => item.value === type)?.label || type || '-'
}

function formatContractStatus(status) {
  return contractStatusOptions.find((item) => item.value === status)?.label || status || '-'
}

function calculateProcurementTotalAmount(quantity, unitPrice) {
  const qty = Number(quantity || 0)
  const price = Number(unitPrice || 0)
  if (!Number.isFinite(qty) || !Number.isFinite(price)) return 0
  return qty * price
}

function parseReportContentJson(contentJson) {
  if (!contentJson) return null
  try {
    return JSON.parse(contentJson)
  } catch {
    return null
  }
}

function getTestReportSummary(report) {
  return parseReportContentJson(report?.contentJson)?.summary || null
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
  visibleMilestoneList.value
    .filter((item) => item.milestoneId)
    .map((item) => ({
      value: String(item.milestoneId),
      label: [
        item.plannedDate
          ? `${item.name || `里程碑${item.milestoneId}`} | ${String(item.plannedDate).slice(0, 10)}`
          : (item.name || `里程碑${item.milestoneId}`),
        item.isLegacyProjection ? '历史台账' : '',
      ].filter(Boolean).join(' | '),
    })),
)

function getLinkedMilestoneProjection(row) {
  if (!row?.milestoneId) return null
  return milestoneList.value.find((item) => String(item.id || '') === String(row.milestoneId || '')) || null
}

function buildMilestoneTaskRecord(row) {
  const linkedMilestone = getLinkedMilestoneProjection(row)
  return {
    id: row.taskId || `draft-${row.localId}`,
    localId: row.localId,
    taskId: row.taskId ? String(row.taskId) : '',
    milestoneId: row.milestoneId ? String(row.milestoneId) : '',
    name: row.name || `里程碑${row.localId}`,
    description: row.description || linkedMilestone?.description || '来自任务计划的里程碑节点',
    plannedDate: getMilestonePlannedDateFromRow(row),
    status: deriveMilestoneStatusFromTaskRow(row),
    ownerName: row.assigneeName || linkedMilestone?.ownerName || '',
    isTaskBased: true,
    isTaskDraft: !row.taskId || !row.milestoneId,
    isLegacyProjection: false,
  }
}

const milestoneTaskRecords = computed(() =>
  taskRows.value
    .filter((row) => isMilestoneRow(row) && hasRowContent(row) && String(row.name || '').trim())
    .map(buildMilestoneTaskRecord)
    .sort((left, right) => String(left.plannedDate || '').localeCompare(String(right.plannedDate || ''))),
)

const legacyMilestoneRecords = computed(() => {
  const linkedProjectionIds = new Set(
    milestoneTaskRecords.value
      .map((item) => String(item.milestoneId || ''))
      .filter(Boolean),
  )
  return milestoneList.value
    .filter((item) => !linkedProjectionIds.has(String(item.id || '')))
    .map((item) => ({
      ...item,
      milestoneId: item?.id != null ? String(item.id) : '',
      isTaskBased: false,
      isTaskDraft: false,
      isLegacyProjection: true,
    }))
})

const visibleMilestoneList = computed(() => [
  ...milestoneTaskRecords.value,
  ...legacyMilestoneRecords.value,
])

const isEditingTaskBasedMilestone = computed(() => Boolean(editingMilestoneTaskLocalId.value))
const isEditingLegacyMilestone = computed(() => Boolean(editingMilestoneId.value) && !editingMilestoneTaskLocalId.value)

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

const taskInfoDialogPermissions = computed(() => ({
  canEditTaskBasic: canEditTaskBasic.value,
  canManageTaskBasic: canManageTaskBasic.value,
  canEditTaskDependency: canEditTaskDependency.value,
  canCommentOnTasks: canCommentOnTasks.value,
  selectedTaskCanUpdateProgress: selectedTaskCanUpdateProgress.value,
  selectedTaskCanSubmitReview: selectedTaskCanSubmitReview.value,
  selectedTaskCanApproveReview: selectedTaskCanApproveReview.value,
  selectedTaskCanReopen: selectedTaskCanReopen.value,
  selectedTaskAssigneeLocked: selectedTaskAssigneeLocked.value,
}))

const taskInfoDialogHelpers = {
  disabledScheduleModeDate,
  canDeleteTaskComment,
}

const taskInfoDialogFormatters = {
  formatTaskStatus,
  formatDateText,
  formatTaskConstraintType,
  formatDependencyLagText,
  formatRiskLevel,
  formatRiskStatus,
  requiresTaskConstraintDate,
}

const taskInfoDialogActions = {
  saveTaskBasicInfo,
  saveTaskProgress,
  submitTaskCompletion,
  approveTaskCompletion,
  rejectTaskCompletion,
  reopenTaskCompletion,
  handleTaskConstraintTypeChange,
  saveTaskDependency,
  removeTaskDependency,
  resetTaskCommentForm,
  saveTaskComment,
  startReplyComment,
  removeTaskComment,
}

const pendingReviewDialogFormatters = {
  formatTaskStatus,
  formatPercentValue,
  formatDateText,
  formatHoursText,
}

const pendingReviewDialogHelpers = {
  isPendingReviewActing,
}

const pendingReviewDialogActions = {
  openPendingReviewTask,
  rejectPendingReviewTask,
  approvePendingReviewTask,
}

const milestoneDialogFormatters = {
  formatMilestoneStatus,
}

const milestoneDialogHelpers = {
  disabledScheduleModeDate,
}

const milestoneDialogActions = {
  resetMilestoneForm,
  saveMilestone,
  populateMilestoneForm,
  removeMilestone,
}

const riskDialogFormatters = {
  formatRiskLevel,
  formatRiskStatus,
}

const riskDialogActions = {
  resetRiskForm,
  saveRisk,
  saveRiskStatus,
  populateRiskForm,
  startRiskStatusEdit,
  removeRisk,
}

const qualityPlanDialogFormatters = {
  formatQualityPlanStatus,
  formatDateTimeText,
}

const qualityPlanDialogActions = {
  resetQualityPlanForm,
  saveQualityPlan,
  editQualityPlan,
  removeQualityPlan,
}

const qualityActivityDialogFormatters = {
  formatQualityActivityType,
  formatDateTimeText,
}

const qualityActivityDialogActions = {
  resetQualityActivityForm,
  saveQualityActivity,
  editQualityActivity,
  removeQualityActivity,
}

const qualityMetricDialogFormatters = {
  formatDateText,
  formatDateTimeText,
}

const qualityMetricDialogActions = {
  resetQualityMetricForm,
  saveQualityMetric,
  editQualityMetric,
  removeQualityMetric,
}

const testPlanDialogFormatters = {
  formatTestPlanStatus,
  formatDateTimeText,
}

const testPlanDialogActions = {
  resetTestPlanForm,
  saveTestPlan,
  editTestPlan,
  removeTestPlan,
}

const testCaseDialogFormatters = {
  formatTestCaseStatus,
  formatDateTimeText,
}

const testCaseDialogActions = {
  resetTestCaseForm,
  saveTestCase,
  editTestCase,
  removeTestCase,
}

const defectDialogFormatters = {
  formatDefectStatus,
  formatDefectSeverity,
}

const defectDialogActions = {
  resetDefectForm,
  saveDefect,
  editDefect,
  removeDefect,
}

const testReportDialogFormatters = {
  formatDateTimeText,
  formatPercentValue,
}

const testReportDialogHelpers = {
  getTestReportSummary,
}

const testReportDialogActions = {
  resetTestReportGenerateForm,
  generateTestReport,
  removeTestReport,
}

const riskMatrixDialogHelpers = {
  getMatrixCellItems,
  getMatrixCellClass,
}

const communicationMatrixDialogFormatters = {
  formatCommunicationChannel,
  formatDateTimeText,
}

const communicationMatrixDialogActions = {
  resetCommunicationMatrixForm,
  saveCommunicationMatrix,
  editCommunicationMatrix,
  removeCommunicationMatrix,
}

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

async function openTaskInfoDialog(targetRow = null, section = 'basic') {
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
  taskDetailSection.value = allTaskDetailSections.some((item) => item.key === section) ? section : 'basic'
  resetTaskBasicForm()
  resetTaskProgressForm()
  await loadProjectMembersData()

  if (!row.taskId) {
    return
  }

  try {
    taskDetailLoading.value = true
    const detail = normalizeTaskProgressRecord(await getProjectTaskDetail(projectId.value, row.taskId))
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

function getSelectedTaskDetailSnapshot() {
  if (!selectedTaskRow.value?.taskId) return null
  if (taskDetail.value?.id == null) return null
  return String(taskDetail.value.id) === String(selectedTaskRow.value.taskId) ? taskDetail.value : null
}

function syncSelectedTaskProgressForm() {
  resetTaskProgressForm(getSelectedTaskDetailSnapshot())
}

async function openSelectedTaskProgressDialog() {
  if (!selectedTaskRow.value || isBlankPlaceholderRow(selectedTaskRow.value)) {
    ElMessage.warning('请先选中一条有效任务')
    return
  }
  await openTaskInfoDialog(selectedTaskRow.value, 'progress')
}

async function quickSubmitSelectedTaskCompletion() {
  if (!selectedTaskCanSubmitReview.value) {
    ElMessage.warning(selectedTaskSubmitBlockedReason.value || '当前任务暂时不能提交完成')
    return
  }
  syncSelectedTaskProgressForm()
  await submitTaskCompletion()
}

async function quickApproveSelectedTaskCompletion() {
  if (!selectedTaskCanApproveReview.value) {
    ElMessage.warning('当前任务暂时不能直接验收')
    return
  }
  syncSelectedTaskProgressForm()
  await approveTaskCompletion()
}

async function quickRejectSelectedTaskCompletion() {
  if (!selectedTaskCanApproveReview.value) {
    ElMessage.warning('当前任务暂时不能直接打回')
    return
  }

  let rejectRemark = ''
  try {
    const result = await ElMessageBox.prompt(
      `请填写“${selectedTaskRow.value?.name || '当前任务'}”的打回说明。`,
      '打回修改',
      {
        type: 'warning',
        confirmButtonText: '确认打回',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '例如：请补充测试截图，并修正异常分支后重新提交',
        inputValidator: (value) => String(value || '').trim() ? true : '请填写处理说明',
      },
    )
    rejectRemark = String(result?.value || '').trim()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '打回操作失败')
    }
    return
  }

  syncSelectedTaskProgressForm()
  taskProgressForm.progress = Math.max(1, Math.min(99, Number(taskProgressForm.progress || 99)))
  taskProgressForm.status = 'IN_PROGRESS'
  taskProgressForm.remark = rejectRemark
  await rejectTaskCompletion()
}

async function openPendingReviewTask(item) {
  const taskId = String(item?.id || '')
  if (!taskId) {
    ElMessage.warning('待验收任务数据不完整')
    return
  }
  const row = findRowByTaskId(taskId)
  if (!row) {
    ElMessage.warning('当前任务树中找不到该任务，请先刷新页面再试')
    return
  }
  pendingReviewDialogVisible.value = false
  await openTaskInfoDialog(row, 'progress')
}

function isPendingReviewActing(taskId, action = '') {
  const normalizedTaskId = String(taskId || '')
  if (!normalizedTaskId) return false
  if (pendingReviewActingTaskId.value !== normalizedTaskId) return false
  return !action || pendingReviewActingAction.value === action
}

async function applyPendingReviewDecision(item, {
  progress,
  status,
  remark,
  successMessage,
  errorMessage,
  action,
}) {
  const taskId = String(item?.id || '')
  if (!taskId) {
    ElMessage.warning('待验收任务数据不完整')
    return
  }

  const currentSelectedTaskId = String(selectedTaskRow.value?.taskId || '')

  try {
    pendingReviewActingTaskId.value = taskId
    pendingReviewActingAction.value = action
    await updateProjectTaskProgress(projectId.value, taskId, {
      progress,
      status,
      remark,
    })
    await loadProjectTasksData()
    if (currentSelectedTaskId) {
      const restoredRow = findRowByTaskId(currentSelectedTaskId)
      if (restoredRow) {
        selectTaskRow(restoredRow)
      }
    }
    if (taskInfoDialogVisible.value && String(taskDetail.value?.id || '') === taskId) {
      const detail = normalizeTaskProgressRecord(await getProjectTaskDetail(projectId.value, taskId))
      taskDetail.value = detail
      resetTaskBasicForm(detail)
      resetTaskProgressForm(detail)
    }
    await loadPendingReviewTasks({ silent: true })
    ElMessage.success(successMessage)
  } catch (error) {
    ElMessage.error(error.message || errorMessage)
  } finally {
    pendingReviewActingTaskId.value = ''
    pendingReviewActingAction.value = ''
  }
}

async function approvePendingReviewTask(item) {
  const taskId = String(item?.id || '')
  if (!taskId) {
    ElMessage.warning('待验收任务数据不完整')
    return
  }
  if (hasUnsavedChanges.value) {
    ElMessage.warning('当前有未保存的计划修改，请先保存后再直接验收')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认通过“${item.name || `任务 ${taskId}`}”的验收吗？通过后会正式并入主任务进度。`,
      '通过验收',
      {
        type: 'warning',
        confirmButtonText: '通过',
        cancelButtonText: '取消',
      },
    )
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '验收操作失败')
    }
    return
  }

  await applyPendingReviewDecision(item, {
    progress: 100,
    status: 'DONE',
    remark: item.remark || null,
    successMessage: '任务已验收通过',
    errorMessage: '任务验收失败',
    action: 'approve',
  })
}

async function rejectPendingReviewTask(item) {
  const taskId = String(item?.id || '')
  if (!taskId) {
    ElMessage.warning('待验收任务数据不完整')
    return
  }
  if (hasUnsavedChanges.value) {
    ElMessage.warning('当前有未保存的计划修改，请先保存后再直接打回')
    return
  }

  let rejectRemark = ''
  try {
    const result = await ElMessageBox.prompt(
      `请填写“${item.name || `任务 ${taskId}`}”的打回说明，成员会看到这条处理意见。`,
      '打回修改',
      {
        type: 'warning',
        confirmButtonText: '确认打回',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputPlaceholder: '例如：请补充联调截图，并修正异常分支处理后重新提交',
        inputValidator: (value) => String(value || '').trim() ? true : '请填写处理说明',
      },
    )
    rejectRemark = String(result?.value || '').trim()
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '打回操作失败')
    }
    return
  }

  await applyPendingReviewDecision(item, {
    progress: Math.max(1, Math.min(99, Number(item?.progress || 99))),
    status: 'IN_PROGRESS',
    remark: rejectRemark,
    successMessage: '任务已打回修改',
    errorMessage: '任务打回失败',
    action: 'reject',
  })
}

async function saveTaskComment() {
  if (!canCommentOnTasks.value) {
    ElMessage.warning('当前项目身份不能发表评论')
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
    taskDetail.value = normalizeTaskProgressRecord(await getProjectTaskDetail(projectId.value, row.taskId))
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
  if (!canDeleteTaskComment(item)) {
    ElMessage.warning('只有创建者或评论作者可以删除评论')
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
    taskDetail.value = normalizeTaskProgressRecord(await getProjectTaskDetail(projectId.value, row.taskId))
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
  if (!requireProjectManagePermission('编辑任务依赖')) return
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
      lagDays: Number(taskDependencyForm.lagDays || 0),
    })
    taskDetail.value = normalizeTaskProgressRecord(await getProjectTaskDetail(projectId.value, row.taskId))
    resetTaskBasicForm(taskDetail.value)
    const dependencies = await getProjectTaskDependencies(projectId.value)
    projectTaskDependencies.value = Array.isArray(dependencies) ? dependencies : projectTaskDependencies.value
    const dependencyResult = propagateTaskDependencySchedule(
      [row.taskId, taskDependencyForm.predecessorTaskId],
      { silent: true },
    )
    resetTaskDependencyForm()
    ElMessage.success(
      dependencyResult.changed
        ? '任务依赖已新增，相关后置任务已自动推排，请继续保存任务计划'
        : '任务依赖已新增',
    )
    if (dependencyResult.blocked) {
      ElMessage.warning('新依赖已建立，但部分任务受项目日期或父任务范围限制，未能完全推排到位')
    }
  } catch (error) {
    ElMessage.error(error.message || '任务依赖保存失败')
  } finally {
    taskDetailLoading.value = false
  }
}

async function removeTaskDependency(item) {
  if (!requireProjectManagePermission('编辑任务依赖')) return
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
    taskDetail.value = normalizeTaskProgressRecord(await getProjectTaskDetail(projectId.value, row.taskId))
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

async function applyTaskProgressUpdate({ progress, status, remark, successMessage, requireRemark = false }) {
  if (!selectedTaskCanUpdateProgress.value) {
    ElMessage.warning('当前只能更新自己负责的任务进度')
    return
  }
  const row = selectedTaskRow.value
  if (!row || !row.taskId) {
    ElMessage.warning('请先保存任务，再更新进度')
    return
  }
  const nextRemark = String(remark || '').trim()
  if (requireRemark && !nextRemark) {
    ElMessage.warning('请先填写处理说明')
    return
  }

  try {
    taskDetailLoading.value = true
    const savedTaskId = String(row.taskId)
    const detail = normalizeTaskProgressRecord(await updateProjectTaskProgress(projectId.value, row.taskId, {
      progress: Number(progress ?? taskProgressForm.progress ?? 0),
      status: status || taskProgressForm.status,
      remark: nextRemark || null,
    }))
    await loadProjectTasksData()
    const refreshedRow = findRowByTaskId(savedTaskId)
    if (refreshedRow) {
      selectTaskRow(refreshedRow)
      taskDetail.value = normalizeTaskProgressRecord(await getProjectTaskDetail(projectId.value, savedTaskId))
    } else {
      taskDetail.value = detail
    }
    resetTaskBasicForm(taskDetail.value)
    hasUnsavedChanges.value = false
    resetTaskProgressForm(taskDetail.value)
    if (pendingReviewDialogVisible.value) {
      await loadPendingReviewTasks({ silent: true })
    }
    ElMessage.success(successMessage)
  } catch (error) {
    ElMessage.error(error.message || '任务进度更新失败')
  } finally {
    taskDetailLoading.value = false
  }
}

async function saveTaskProgress() {
  await applyTaskProgressUpdate({
    progress: taskProgressForm.progress,
    status: taskProgressForm.status || deriveTaskProgressEditorStatus(taskProgressForm.progress, selectedTaskWorkflowStatus.value),
    remark: taskProgressForm.remark,
    successMessage: '任务进度已保存',
  })
}

async function submitTaskCompletion() {
  const nextProgress = clampTaskProgressValue(taskProgressForm.progress)
  if (nextProgress < 100) {
    ElMessage.warning(`当前进度 ${formatPercentValue(nextProgress)}，请先更新到 100% 后再提交完成`)
    return
  }
  await applyTaskProgressUpdate({
    progress: nextProgress,
    status: 'PENDING_REVIEW',
    remark: taskProgressForm.remark,
    successMessage: '任务已提交验收',
  })
}

async function approveTaskCompletion() {
  await applyTaskProgressUpdate({
    progress: 100,
    status: 'DONE',
    remark: taskProgressForm.remark,
    successMessage: '任务已验收通过',
  })
}

async function reopenTaskCompletion() {
  await applyTaskProgressUpdate({
    progress: 99,
    status: 'IN_PROGRESS',
    remark: taskProgressForm.remark,
    successMessage: '任务已重开，可直接继续处理；如需换人可重新分配负责人',
  })
}

async function rejectTaskCompletion() {
  const nextProgress = Math.max(1, Math.min(99, Number(taskProgressForm.progress || 99)))
  await applyTaskProgressUpdate({
    progress: nextProgress,
    status: 'IN_PROGRESS',
    remark: taskProgressForm.remark,
    successMessage: '任务已打回修改',
    requireRemark: true,
  })
}

function startReplyComment(item) {
  taskCommentForm.replyToId = item?.id ? String(item.id) : ''
  taskCommentForm.replyToName = item?.userName || '该评论'
}

async function saveTaskBasicInfo() {
  if (!requireTaskBasicEditPermission('修改任务基础信息')) return
  const row = selectedTaskRow.value
  if (!row || !row.taskId) {
    ElMessage.warning('请先保存任务，再维护基础信息')
    return
  }

  let planningSnapshot = null
  let taskPersisted = false
  try {
    taskDetailLoading.value = true
    const savedTaskId = String(row.taskId)
    planningSnapshot = canManageProject.value ? captureTaskPlanningStateSnapshot() : null
    const scheduleMetadata = buildTaskScheduleMetadata(
      taskBasicForm.deadlineDate,
      taskBasicForm.constraintType,
      taskBasicForm.constraintDate,
    )
    if (canManageProject.value) {
      row.deadlineDate = scheduleMetadata.deadlineDate ? formatDateText(scheduleMetadata.deadlineDate) : ''
      row.constraintType = scheduleMetadata.constraintType || ''
      row.constraintDate = scheduleMetadata.constraintDate ? formatDateText(scheduleMetadata.constraintDate) : ''
      const constraintResult = applyTaskConstraintSchedule(row)
      const seedTaskIds = new Set([savedTaskId, ...constraintResult.changedTaskIds])
      propagateTaskDependencySchedule([...seedTaskIds], { silent: true })
      ensureTaskConstraintScheduleConsistency()
      ensureDependencyScheduleConsistency()
    }
    const detail = normalizeTaskProgressRecord(await updateProjectTask(projectId.value, row.taskId, {
      parentTaskId: row.backendParentTaskId || null,
      wbsId: taskDetail.value?.wbsId || null,
      milestoneId: taskBasicForm.milestoneId || null,
      name: row.name || '未命名任务',
      description: taskBasicForm.description.trim() || null,
      assigneeId: taskBasicForm.assigneeId || null,
      plannedStartDate: row.start ? `${row.start}T00:00:00` : null,
      plannedEndDate: row.finish ? `${row.finish}T00:00:00` : null,
      deadlineDate: scheduleMetadata.deadlineDate,
      constraintType: scheduleMetadata.constraintType,
      constraintDate: scheduleMetadata.constraintDate,
      plannedHours: Number(row.duration || 0) > 0 ? Number(row.duration || 0) * 8 : null,
      priority: taskDetail.value?.priority || 'MEDIUM',
      taskType: isMilestoneRow(row) ? 'MILESTONE_TASK' : mapModeToTaskType(row.mode),
      status: taskProgressForm.status || row.status || 'TODO',
      progress: Number(taskProgressForm.progress ?? row.progress ?? 0),
      sortOrder: Number(row.sortOrder || 0) || null,
      remark: row.mode || '',
    }))
    taskPersisted = true
    await loadProjectTasksData()
    const refreshedRow = findRowByTaskId(savedTaskId)
    if (refreshedRow) {
      selectTaskRow(refreshedRow)
    }
    taskDetail.value = detail
    resetTaskBasicForm(detail)
    resetTaskProgressForm(detail)
    ElMessage.success('基础信息已保存')
  } catch (error) {
    if (canManageProject.value && planningSnapshot && !taskPersisted) {
      restoreTaskPlanningStateSnapshot(planningSnapshot)
    }
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
  if (!requireProjectContentEditPermission('维护风险')) return
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
  if (!requireProjectContentEditPermission('更新风险状态')) return
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
    } else if (importForm.module === 'COST') {
      const [plans, actuals, evm] = await Promise.all([
        getProjectCostPlans(projectId.value),
        getProjectCostActuals(projectId.value),
        getProjectEvm(projectId.value),
      ])
      costPlans.value = mergeCostPlans(plans)
      costActuals.value = normalizeListResult(actuals)
      evmMetrics.value = evm
    } else if (importForm.module === 'TIMESHEET') {
      const [timesheetList, timesheetSummary] = await Promise.all([
        getProjectTimesheets(projectId.value),
        getProjectTimesheetReport(projectId.value),
      ])
      timesheetRecords.value = normalizeListResult(timesheetList)
      timesheetReport.value = timesheetSummary
      await loadProjectTasksData()
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
  if (!requireProjectContentEditPermission('维护里程碑')) return
  if (!canEditTaskPlan.value) {
    ElMessage.warning('只有项目负责人可以维护任务计划中的里程碑')
    return
  }
  if (!milestoneForm.name.trim()) {
    ElMessage.warning('请输入里程碑名称')
    return
  }
  if (!milestoneForm.plannedDate) {
    ElMessage.warning('请选择里程碑计划日期')
    return
  }

  try {
    milestoneLoading.value = true
    const editingRow = findMilestoneTaskRow(editingMilestoneTaskLocalId.value)

    if (editingRow) {
      applyMilestoneFormToTaskRow(editingRow)
      selectTaskRow(editingRow)
      ElMessage.success('里程碑已更新到任务计划，请保存文件后同步项目日历和报表')
    } else {
      const row = buildMilestoneTaskRowFromForm()
      appendRootTaskRow(row)
      selectTaskRow(row)
      ElMessage.success(
        isEditingLegacyMilestone.value
          ? '历史里程碑已纳入任务计划，请保存文件后同步项目日历和报表'
          : '里程碑已插入任务计划，请保存文件后同步项目日历和报表',
      )
    }
    resetMilestoneForm()
  } catch (error) {
    ElMessage.error(error.message || '里程碑保存失败')
  } finally {
    milestoneLoading.value = false
  }
}

async function removeMilestone(item) {
  if (!requireProjectContentEditPermission('删除里程碑')) return
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

    if (item?.isTaskBased) {
      if (!canEditTaskPlan.value) {
        ElMessage.warning('只有项目负责人可以维护任务计划中的里程碑')
        return
      }
      const linkedTaskCount = countMilestoneLinkedTaskRows(item.milestoneId, item.localId)
      if (linkedTaskCount > 0) {
        ElMessage.warning(`还有 ${linkedTaskCount} 条任务关联了该里程碑，请先解除关联再删除`)
        return
      }
      removeTaskRowByLocalId(item.localId)
      if (editingMilestoneTaskLocalId.value && String(editingMilestoneTaskLocalId.value) === String(item.localId)) {
        resetMilestoneForm()
      }
      ElMessage.success('里程碑已从任务计划移除，请保存文件后同步项目日历和报表')
      return
    }

    const legacyLinkedTaskCount = countMilestoneLinkedTaskRows(item.milestoneId || item.id)
    if (legacyLinkedTaskCount > 0) {
      ElMessage.warning(`还有 ${legacyLinkedTaskCount} 条任务关联了该历史里程碑，请先纳入计划或解除关联再删除`)
      return
    }

    await deleteProjectMilestone(projectId.value, item.milestoneId || item.id)
    await loadProjectMilestonesData()
    if (editingMilestoneId.value && String(editingMilestoneId.value) === String(item.milestoneId || item.id)) {
      resetMilestoneForm()
    }
    ElMessage.success('历史里程碑已删除')
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
  scheduleOptionsSnapshot = cloneScheduleOptions()
  wbsDialogVisible.value = true
}

function cancelWbsDialog() {
  applyWbsConfig(wbsConfigSnapshot || createDefaultWbsConfig())
  applyScheduleOptions(scheduleOptionsSnapshot || createDefaultScheduleOptions())
  wbsConfigSnapshot = null
  scheduleOptionsSnapshot = null
  refreshWbsCodes()
  wbsDialogVisible.value = false
}

async function saveWbsConfig() {
  if (!requireProjectContentEditPermission('保存 WBS / 排程规则')) return
  try {
    loading.value = true
    const previousScheduleOptionsSignature = getScheduleOptionsSignature(
      scheduleOptionsSnapshot || createDefaultScheduleOptions(),
    )
    await persistProjectEditorPreferenceData()
    const scheduleChanged = previousScheduleOptionsSignature !== getScheduleOptionsSignature(scheduleOptions)
    const scheduleAdjusted = scheduleChanged ? applyScheduleModeToProjectAndTasks() : false
    const rescheduleResult = scheduleChanged && canManageProject.value
      ? rescheduleProjectTasks({ silent: true })
      : {
        changed: false,
        blocked: false,
        iterations: 0,
        changedTaskIds: [],
        constraintViolations: [],
        dependencyViolations: [],
      }
    wbsConfigSnapshot = null
    scheduleOptionsSnapshot = null
    refreshWbsCodes()
    wbsDialogVisible.value = false
    ElMessage.success(
      scheduleChanged && (scheduleAdjusted || rescheduleResult.changed)
        ? 'WBS / 排程规则已保存，任务工期和排程结果已按新规则重算，请继续保存任务计划'
        : 'WBS / 排程规则已保存',
    )
    if (rescheduleResult.constraintViolations.length || rescheduleResult.dependencyViolations.length) {
      const firstMessage = rescheduleResult.constraintViolations[0]?.message
        || (rescheduleResult.dependencyViolations[0]
          ? `依赖未完全满足：${rescheduleResult.dependencyViolations[0].predecessorName} -> ${rescheduleResult.dependencyViolations[0].successorName}`
          : '')
      ElMessage.warning(firstMessage || '排程规则已切换，但仍存在未解决的排程冲突')
    } else if (rescheduleResult.blocked) {
      ElMessage.warning('排程规则已切换，但部分任务受项目日期或父任务范围限制，未能完全推排到位')
    }
  } catch (error) {
    ElMessage.error(error.message || 'WBS / 排程规则保存失败')
  } finally {
    loading.value = false
  }
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
  if (!requireProjectContentEditPermission('新增需求')) return
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

async function loadQualityPlansData() {
  qualityPlans.value = normalizeListResult(await getProjectQualityPlans(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadQualityActivitiesData() {
  qualityActivities.value = normalizeListResult(await getProjectQualityActivities(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadQualityMetricsData() {
  qualityMetrics.value = normalizeListResult(await getProjectQualityMetrics(projectId.value, { page: 1, pageSize: 200 }))
}

async function openQualityPlanDialog() {
  try {
    qualityLoading.value = true
    await Promise.all([
      loadQualityPlansData(),
      loadProjectMembersData(),
    ])
    resetQualityPlanForm()
    qualityPlanDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '质量计划数据加载失败')
  } finally {
    qualityLoading.value = false
  }
}

async function openQualityActivityDialog() {
  try {
    qualityLoading.value = true
    await Promise.all([
      loadQualityActivitiesData(),
      loadQualityPlansData(),
      loadProjectMembersData(),
    ])
    resetQualityActivityForm()
    qualityActivityDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '质量活动数据加载失败')
  } finally {
    qualityLoading.value = false
  }
}

async function openQualityMetricDialog() {
  try {
    qualityLoading.value = true
    await loadQualityMetricsData()
    resetQualityMetricForm()
    qualityMetricDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '质量指标数据加载失败')
  } finally {
    qualityLoading.value = false
  }
}

function editQualityPlan(item) {
  resetQualityPlanForm(item)
  qualityPlanDialogVisible.value = true
}

async function saveQualityPlan() {
  if (!requireProjectContentEditPermission('维护质量计划')) return
  if (!qualityPlanForm.title.trim()) {
    ElMessage.warning('请输入质量计划标题')
    return
  }

  try {
    qualityLoading.value = true
    const isEditing = Boolean(editingQualityPlanId.value)
    const payload = {
      title: qualityPlanForm.title.trim(),
      qualityStandard: qualityPlanForm.qualityStandard.trim() || null,
      acceptanceRule: qualityPlanForm.acceptanceRule.trim() || null,
      ownerId: toNullableIdValue(qualityPlanForm.ownerId),
      status: qualityPlanForm.status,
    }
    if (editingQualityPlanId.value) {
      await updateProjectQualityPlan(projectId.value, editingQualityPlanId.value, payload)
    } else {
      await createProjectQualityPlan(projectId.value, payload)
    }
    await Promise.all([
      loadQualityPlansData(),
      loadQualityActivitiesData(),
    ])
    resetQualityPlanForm()
    ElMessage.success(isEditing ? '质量计划已更新' : '质量计划已新增')
  } catch (error) {
    ElMessage.error(error.message || '质量计划保存失败')
  } finally {
    qualityLoading.value = false
  }
}

async function removeQualityPlan(item) {
  if (!requireProjectContentEditPermission('删除质量计划')) return
  try {
    await ElMessageBox.confirm(
      `确认删除质量计划“${item.title || item.id}”吗？`,
      '删除质量计划',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    qualityLoading.value = true
    await deleteProjectQualityPlan(projectId.value, item.id)
    await Promise.all([
      loadQualityPlansData(),
      loadQualityActivitiesData(),
    ])
    if (editingQualityPlanId.value && String(editingQualityPlanId.value) === String(item.id)) {
      resetQualityPlanForm()
    }
    ElMessage.success('质量计划已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除质量计划失败')
    }
  } finally {
    qualityLoading.value = false
  }
}

function editQualityActivity(item) {
  resetQualityActivityForm(item)
  qualityActivityDialogVisible.value = true
}

async function saveQualityActivity() {
  if (!requireProjectContentEditPermission('维护质量活动')) return
  if (!qualityActivityForm.activityName.trim()) {
    ElMessage.warning('请输入质量活动名称')
    return
  }

  try {
    qualityLoading.value = true
    const isEditing = Boolean(editingQualityActivityId.value)
    const payload = {
      qualityPlanId: toNullableIdValue(qualityActivityForm.qualityPlanId),
      activityName: qualityActivityForm.activityName.trim(),
      activityType: qualityActivityForm.activityType,
      plannedDate: normalizeDateTimeValue(qualityActivityForm.plannedDate),
      actualDate: normalizeDateTimeValue(qualityActivityForm.actualDate),
      result: qualityActivityForm.result.trim() || null,
      ownerId: toNullableIdValue(qualityActivityForm.ownerId),
    }
    if (editingQualityActivityId.value) {
      await updateProjectQualityActivity(projectId.value, editingQualityActivityId.value, payload)
    } else {
      await createProjectQualityActivity(projectId.value, payload)
    }
    await loadQualityActivitiesData()
    resetQualityActivityForm()
    ElMessage.success(isEditing ? '质量活动已更新' : '质量活动已新增')
  } catch (error) {
    ElMessage.error(error.message || '质量活动保存失败')
  } finally {
    qualityLoading.value = false
  }
}

async function removeQualityActivity(item) {
  if (!requireProjectContentEditPermission('删除质量活动')) return
  try {
    await ElMessageBox.confirm(
      `确认删除质量活动“${item.activityName || item.id}”吗？`,
      '删除质量活动',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    qualityLoading.value = true
    await deleteProjectQualityActivity(projectId.value, item.id)
    await loadQualityActivitiesData()
    if (editingQualityActivityId.value && String(editingQualityActivityId.value) === String(item.id)) {
      resetQualityActivityForm()
    }
    ElMessage.success('质量活动已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除质量活动失败')
    }
  } finally {
    qualityLoading.value = false
  }
}

function editQualityMetric(item) {
  resetQualityMetricForm(item)
  qualityMetricDialogVisible.value = true
}

async function saveQualityMetric() {
  if (!requireProjectContentEditPermission('维护质量指标')) return
  if (!qualityMetricForm.metricName.trim()) {
    ElMessage.warning('请输入质量指标名称')
    return
  }
  if (qualityMetricForm.metricValue !== '' && !Number.isFinite(Number(qualityMetricForm.metricValue))) {
    ElMessage.warning('请输入合法的指标值')
    return
  }

  try {
    qualityLoading.value = true
    const isEditing = Boolean(editingQualityMetricId.value)
    const payload = {
      metricName: qualityMetricForm.metricName.trim(),
      metricValue: qualityMetricForm.metricValue === '' ? null : Number(qualityMetricForm.metricValue),
      metricUnit: qualityMetricForm.metricUnit.trim() || null,
      statisticDate: qualityMetricForm.statisticDate || null,
    }
    if (editingQualityMetricId.value) {
      await updateProjectQualityMetric(projectId.value, editingQualityMetricId.value, payload)
    } else {
      await createProjectQualityMetric(projectId.value, payload)
    }
    await loadQualityMetricsData()
    resetQualityMetricForm()
    ElMessage.success(isEditing ? '质量指标已更新' : '质量指标已新增')
  } catch (error) {
    ElMessage.error(error.message || '质量指标保存失败')
  } finally {
    qualityLoading.value = false
  }
}

async function removeQualityMetric(item) {
  if (!requireProjectContentEditPermission('删除质量指标')) return
  try {
    await ElMessageBox.confirm(
      `确认删除质量指标“${item.metricName || item.id}”吗？`,
      '删除质量指标',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    qualityLoading.value = true
    await deleteProjectQualityMetric(projectId.value, item.id)
    await loadQualityMetricsData()
    if (editingQualityMetricId.value && String(editingQualityMetricId.value) === String(item.id)) {
      resetQualityMetricForm()
    }
    ElMessage.success('质量指标已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除质量指标失败')
    }
  } finally {
    qualityLoading.value = false
  }
}

async function loadTestPlansData() {
  testPlans.value = normalizeListResult(await getProjectTestPlans(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadTestCasesData() {
  testCases.value = normalizeListResult(await getProjectTestCases(projectId.value, { page: 1, pageSize: 300 }))
}

async function loadDefectsData() {
  defectList.value = normalizeListResult(await getProjectDefects(projectId.value, { page: 1, pageSize: 300 }))
}

async function loadTestReportsData() {
  testReportList.value = normalizeListResult(await getProjectTestReports(projectId.value))
}

async function loadTestRequirementData() {
  requirements.value = normalizeListResult(await getProjectRequirements(projectId.value, { page: 1, pageSize: 200 }))
}

async function openTestPlanDialog() {
  try {
    testingLoading.value = true
    await Promise.all([
      loadTestPlansData(),
      loadProjectMembersData(),
    ])
    resetTestPlanForm()
    testPlanDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '测试计划数据加载失败')
  } finally {
    testingLoading.value = false
  }
}

async function openTestCaseDialog() {
  try {
    testingLoading.value = true
    await Promise.all([
      loadTestCasesData(),
      loadTestPlansData(),
      loadTestRequirementData(),
      loadProjectMembersData(),
    ])
    resetTestCaseForm()
    testCaseDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '测试用例数据加载失败')
  } finally {
    testingLoading.value = false
  }
}

async function openDefectDialog() {
  try {
    testingLoading.value = true
    await Promise.all([
      loadDefectsData(),
      loadTestCasesData(),
      loadTestRequirementData(),
      loadProjectMembersData(),
    ])
    resetDefectForm()
    defectDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '缺陷数据加载失败')
  } finally {
    testingLoading.value = false
  }
}

async function openTestReportDialog() {
  try {
    testingLoading.value = true
    await loadTestReportsData()
    resetTestReportGenerateForm()
    testReportDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '测试报告数据加载失败')
  } finally {
    testingLoading.value = false
  }
}

function editTestPlan(item) {
  resetTestPlanForm(item)
  testPlanDialogVisible.value = true
}

async function saveTestPlan() {
  if (!requireProjectContentEditPermission('维护测试计划')) return
  if (!testPlanForm.title.trim()) {
    ElMessage.warning('请输入测试计划标题')
    return
  }

  try {
    testingLoading.value = true
    const isEditing = Boolean(editingTestPlanId.value)
    const payload = {
      title: testPlanForm.title.trim(),
      versionNo: testPlanForm.versionNo.trim() || null,
      scopeDesc: testPlanForm.scopeDesc.trim() || null,
      ownerId: toNullableIdValue(testPlanForm.ownerId),
      status: testPlanForm.status,
    }
    if (editingTestPlanId.value) {
      await updateProjectTestPlan(projectId.value, editingTestPlanId.value, payload)
    } else {
      await createProjectTestPlan(projectId.value, payload)
    }
    await loadTestPlansData()
    resetTestPlanForm()
    ElMessage.success(isEditing ? '测试计划已更新' : '测试计划已新增')
  } catch (error) {
    ElMessage.error(error.message || '测试计划保存失败')
  } finally {
    testingLoading.value = false
  }
}

async function removeTestPlan(item) {
  if (!requireProjectContentEditPermission('删除测试计划')) return
  try {
    await ElMessageBox.confirm(
      `确认删除测试计划“${item.title || item.id}”吗？`,
      '删除测试计划',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    testingLoading.value = true
    await deleteProjectTestPlan(projectId.value, item.id)
    await Promise.all([
      loadTestPlansData(),
      loadTestCasesData(),
    ])
    if (editingTestPlanId.value && String(editingTestPlanId.value) === String(item.id)) {
      resetTestPlanForm()
    }
    ElMessage.success('测试计划已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除测试计划失败')
    }
  } finally {
    testingLoading.value = false
  }
}

function editTestCase(item) {
  resetTestCaseForm(item)
  testCaseDialogVisible.value = true
}

async function saveTestCase() {
  if (!requireProjectContentEditPermission('维护测试用例')) return
  if (!testCaseForm.title.trim()) {
    ElMessage.warning('请输入测试用例标题')
    return
  }

  try {
    testingLoading.value = true
    const isEditing = Boolean(editingTestCaseId.value)
    const payload = {
      testPlanId: toNullableIdValue(testCaseForm.testPlanId),
      requirementId: toNullableIdValue(testCaseForm.requirementId),
      taskId: toNullableIdValue(testCaseForm.taskId),
      title: testCaseForm.title.trim(),
      precondition: testCaseForm.precondition.trim() || null,
      steps: testCaseForm.steps.trim() || null,
      expectedResult: testCaseForm.expectedResult.trim() || null,
      actualResult: testCaseForm.actualResult.trim() || null,
      executionStatus: testCaseForm.executionStatus,
      testerId: toNullableIdValue(testCaseForm.testerId),
      executedAt: normalizeDateTimeValue(testCaseForm.executedAt),
    }
    if (editingTestCaseId.value) {
      await updateProjectTestCase(projectId.value, editingTestCaseId.value, payload)
    } else {
      await createProjectTestCase(projectId.value, payload)
    }
    await Promise.all([
      loadTestCasesData(),
      loadDefectsData(),
    ])
    resetTestCaseForm()
    ElMessage.success(isEditing ? '测试用例已更新' : '测试用例已新增')
  } catch (error) {
    ElMessage.error(error.message || '测试用例保存失败')
  } finally {
    testingLoading.value = false
  }
}

async function removeTestCase(item) {
  if (!requireProjectContentEditPermission('删除测试用例')) return
  try {
    await ElMessageBox.confirm(
      `确认删除测试用例“${item.caseCode || item.title || item.id}”吗？`,
      '删除测试用例',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    testingLoading.value = true
    await deleteProjectTestCase(projectId.value, item.id)
    await Promise.all([
      loadTestCasesData(),
      loadDefectsData(),
    ])
    if (editingTestCaseId.value && String(editingTestCaseId.value) === String(item.id)) {
      resetTestCaseForm()
    }
    ElMessage.success('测试用例已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除测试用例失败')
    }
  } finally {
    testingLoading.value = false
  }
}

function editDefect(item) {
  resetDefectForm(item)
  defectDialogVisible.value = true
}

async function saveDefect() {
  if (!requireProjectContentEditPermission('维护缺陷')) return
  if (!defectForm.title.trim()) {
    ElMessage.warning('请输入缺陷标题')
    return
  }

  try {
    testingLoading.value = true
    const isEditing = Boolean(editingDefectId.value)
    const payload = {
      testCaseId: toNullableIdValue(defectForm.testCaseId),
      requirementId: toNullableIdValue(defectForm.requirementId),
      taskId: toNullableIdValue(defectForm.taskId),
      title: defectForm.title.trim(),
      severity: defectForm.severity,
      priority: defectForm.priority,
      status: defectForm.status,
      reporterId: toNullableIdValue(defectForm.reporterId),
      assigneeId: toNullableIdValue(defectForm.assigneeId),
      description: defectForm.description.trim() || null,
      resolution: defectForm.resolution.trim() || null,
    }
    if (editingDefectId.value) {
      await updateProjectDefect(projectId.value, editingDefectId.value, payload)
    } else {
      await createProjectDefect(projectId.value, payload)
    }
    await loadDefectsData()
    resetDefectForm()
    ElMessage.success(isEditing ? '缺陷已更新' : '缺陷已新增')
  } catch (error) {
    ElMessage.error(error.message || '缺陷保存失败')
  } finally {
    testingLoading.value = false
  }
}

async function removeDefect(item) {
  if (!requireProjectContentEditPermission('删除缺陷')) return
  try {
    await ElMessageBox.confirm(
      `确认删除缺陷“${item.defectCode || item.title || item.id}”吗？`,
      '删除缺陷',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    testingLoading.value = true
    await deleteProjectDefect(projectId.value, item.id)
    await loadDefectsData()
    if (editingDefectId.value && String(editingDefectId.value) === String(item.id)) {
      resetDefectForm()
    }
    ElMessage.success('缺陷已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除缺陷失败')
    }
  } finally {
    testingLoading.value = false
  }
}

async function generateTestReport() {
  if (!requireProjectContentEditPermission('生成测试报告')) return
  try {
    testingLoading.value = true
    await generateProjectTestReport(projectId.value, {
      type: testReportGenerateForm.type,
      startDate: testReportGenerateForm.startDate || null,
      endDate: testReportGenerateForm.endDate || null,
    })
    await loadTestReportsData()
    ElMessage.success('测试报告已生成')
  } catch (error) {
    ElMessage.error(error.message || '测试报告生成失败')
  } finally {
    testingLoading.value = false
  }
}

async function removeTestReport(item) {
  if (!requireProjectContentEditPermission('删除测试报告')) return
  try {
    await ElMessageBox.confirm(
      `确认删除测试报告“${item.title || item.id}”吗？`,
      '删除测试报告',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    testingLoading.value = true
    await deleteProjectTestReport(projectId.value, item.id)
    await loadTestReportsData()
    ElMessage.success('测试报告已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除测试报告失败')
    }
  } finally {
    testingLoading.value = false
  }
}

async function loadCommunicationMatrixData() {
  communicationMatrixList.value = normalizeListResult(await getProjectCommunicationMatrix(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadMeetingsData() {
  meetingList.value = normalizeListResult(await getProjectMeetings(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadCommunicationRecordsData() {
  communicationRecordList.value = normalizeListResult(await getProjectCommunicationRecords(projectId.value, { page: 1, pageSize: 200 }))
}

async function openCommunicationMatrixDialog() {
  try {
    communicationLoading.value = true
    await loadCommunicationMatrixData()
    resetCommunicationMatrixForm()
    communicationMatrixDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '沟通安排数据加载失败')
  } finally {
    communicationLoading.value = false
  }
}

async function openMeetingDialog() {
  try {
    communicationLoading.value = true
    await Promise.all([
      loadMeetingsData(),
      loadProjectMembersData(),
    ])
    resetMeetingForm()
    meetingDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '会议计划数据加载失败')
  } finally {
    communicationLoading.value = false
  }
}

async function openCommunicationRecordDialog(options = {}) {
  const targetMeetingId = options.meetingId != null ? String(options.meetingId) : ''
  try {
    communicationLoading.value = true
    await Promise.all([
      loadCommunicationRecordsData(),
      loadMeetingsData(),
      loadProjectMembersData(),
    ])
    communicationRecordFilterMeetingId.value = targetMeetingId
    resetCommunicationRecordForm()
    if (targetMeetingId) {
      communicationRecordForm.meetingId = targetMeetingId
    }
    communicationRecordDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '沟通记录数据加载失败')
  } finally {
    communicationLoading.value = false
  }
}

function editCommunicationMatrix(item) {
  resetCommunicationMatrixForm(item)
  communicationMatrixDialogVisible.value = true
}

async function saveCommunicationMatrix() {
  if (!requireProjectContentEditPermission('维护沟通安排')) return
  if (!communicationMatrixForm.senderRole.trim()) {
    ElMessage.warning('请输入发送方角色')
    return
  }
  if (!communicationMatrixForm.receiverRole.trim()) {
    ElMessage.warning('请输入接收方角色')
    return
  }

  try {
    communicationLoading.value = true
    const isEditing = Boolean(editingCommunicationMatrixId.value)
    const payload = {
      senderRole: communicationMatrixForm.senderRole.trim(),
      receiverRole: communicationMatrixForm.receiverRole.trim(),
      channel: communicationMatrixForm.channel,
      frequency: communicationMatrixForm.frequency.trim() || null,
      topic: communicationMatrixForm.topic.trim() || null,
    }
    if (editingCommunicationMatrixId.value) {
      await updateProjectCommunicationMatrix(projectId.value, editingCommunicationMatrixId.value, payload)
    } else {
      await createProjectCommunicationMatrix(projectId.value, payload)
    }
    await loadCommunicationMatrixData()
    resetCommunicationMatrixForm()
    ElMessage.success(isEditing ? '沟通安排已更新' : '沟通安排已新增')
  } catch (error) {
    ElMessage.error(error.message || '沟通安排保存失败')
  } finally {
    communicationLoading.value = false
  }
}

async function removeCommunicationMatrix(item) {
  if (!requireProjectContentEditPermission('删除沟通安排')) return
  try {
    await ElMessageBox.confirm(
      `确认删除沟通安排“${item.topic || `${item.senderRole} -> ${item.receiverRole}`}”吗？`,
      '删除沟通安排',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    communicationLoading.value = true
    await deleteProjectCommunicationMatrix(projectId.value, item.id)
    await loadCommunicationMatrixData()
    if (editingCommunicationMatrixId.value && String(editingCommunicationMatrixId.value) === String(item.id)) {
      resetCommunicationMatrixForm()
    }
    ElMessage.success('沟通安排已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除沟通安排失败')
    }
  } finally {
    communicationLoading.value = false
  }
}

function editMeeting(item) {
  resetMeetingForm(item)
  meetingDialogVisible.value = true
}

async function saveMeeting() {
  if (!requireProjectContentEditPermission('维护会议计划')) return
  if (!meetingForm.title.trim()) {
    ElMessage.warning('请输入会议标题')
    return
  }
  if (!meetingForm.scheduledAt) {
    ElMessage.warning('请选择会议时间')
    return
  }
  if (meetingForm.durationMinutes !== '' && !Number.isFinite(Number(meetingForm.durationMinutes))) {
    ElMessage.warning('请输入合法的会议时长')
    return
  }

  try {
    communicationLoading.value = true
    const isEditing = Boolean(editingMeetingId.value)
    const payload = {
      meetingType: meetingForm.meetingType,
      title: meetingForm.title.trim(),
      scheduledAt: meetingForm.scheduledAt,
      durationMinutes: meetingForm.durationMinutes === '' ? null : Number(meetingForm.durationMinutes),
      hostId: toNullableIdValue(meetingForm.hostId),
      location: meetingForm.location.trim() || null,
      status: meetingForm.status,
    }
    if (editingMeetingId.value) {
      await updateProjectMeeting(projectId.value, editingMeetingId.value, payload)
    } else {
      await createProjectMeeting(projectId.value, payload)
    }
    await Promise.all([
      loadMeetingsData(),
      loadCommunicationRecordsData(),
    ])
    resetMeetingForm()
    ElMessage.success(isEditing ? '会议计划已更新' : '会议计划已新增')
  } catch (error) {
    ElMessage.error(error.message || '会议计划保存失败')
  } finally {
    communicationLoading.value = false
  }
}

async function removeMeeting(item) {
  if (!requireProjectContentEditPermission('删除会议计划')) return
  try {
    await ElMessageBox.confirm(
      `确认删除会议“${item.title || item.id}”吗？`,
      '删除会议计划',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    communicationLoading.value = true
    await deleteProjectMeeting(projectId.value, item.id)
    await loadMeetingsData()
    if (editingMeetingId.value && String(editingMeetingId.value) === String(item.id)) {
      resetMeetingForm()
    }
    ElMessage.success('会议计划已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      if (
        error?.message === 'meeting still has communication records'
        || error?.message === '该会议下还有沟通记录，请先删除相关沟通记录后再删除会议'
      ) {
        meetingDialogVisible.value = false
        await openCommunicationRecordDialog({ meetingId: item?.id || '' })
        ElMessage.error('该会议下还有沟通记录，请先删除相关沟通记录后再删除会议')
      } else {
        ElMessage.error(error.message || '删除会议计划失败')
      }
    }
  } finally {
    communicationLoading.value = false
  }
}

function clearCommunicationRecordFilter() {
  communicationRecordFilterMeetingId.value = ''
}

function editCommunicationRecord(item) {
  resetCommunicationRecordForm(item)
  communicationRecordFilterMeetingId.value = item?.meetingId != null ? String(item.meetingId) : ''
  communicationRecordDialogVisible.value = true
}

async function saveCommunicationRecord() {
  if (!requireProjectContentEditPermission('维护沟通记录')) return
  if (!communicationRecordForm.title.trim()) {
    ElMessage.warning('请输入记录标题')
    return
  }
  if (!communicationRecordForm.content.trim()) {
    ElMessage.warning('请输入记录内容')
    return
  }

  try {
    communicationLoading.value = true
    const isEditing = Boolean(editingCommunicationRecordId.value)
    const payload = {
      meetingId: toNullableIdValue(communicationRecordForm.meetingId),
      recordType: communicationRecordForm.recordType,
      title: communicationRecordForm.title.trim(),
      content: communicationRecordForm.content.trim(),
      recorderId: toNullableIdValue(communicationRecordForm.recorderId),
    }
    if (editingCommunicationRecordId.value) {
      await updateProjectCommunicationRecord(projectId.value, editingCommunicationRecordId.value, payload)
    } else {
      await createProjectCommunicationRecord(projectId.value, payload)
    }
    await loadCommunicationRecordsData()
    resetCommunicationRecordForm()
    ElMessage.success(isEditing ? '沟通记录已更新' : '沟通记录已新增')
  } catch (error) {
    ElMessage.error(error.message || '沟通记录保存失败')
  } finally {
    communicationLoading.value = false
  }
}

async function removeCommunicationRecord(item) {
  if (!requireProjectContentEditPermission('删除沟通记录')) return
  try {
    await ElMessageBox.confirm(
      `确认删除沟通记录“${item.title || item.id}”吗？`,
      '删除沟通记录',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    communicationLoading.value = true
    await deleteProjectCommunicationRecord(projectId.value, item.id)
    await loadCommunicationRecordsData()
    if (editingCommunicationRecordId.value && String(editingCommunicationRecordId.value) === String(item.id)) {
      resetCommunicationRecordForm()
    }
    ElMessage.success('沟通记录已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除沟通记录失败')
    }
  } finally {
    communicationLoading.value = false
  }
}

async function loadConfigItemsData() {
  configItemList.value = normalizeListResult(await getProjectConfigItems(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadBaselineRecordsData() {
  const params = {
    page: 1,
    pageSize: 200,
  }
  if (configurationBaselineTypeFilter.value) {
    params.baselineType = configurationBaselineTypeFilter.value
  }
  baselineRecordList.value = normalizeListResult(await getProjectBaselines(projectId.value, params))
}

async function openConfigurationWorkspace(tab = 'items', mode = 'management') {
  try {
    configurationDialogMode.value = mode
    configurationBaselineTypeFilter.value = mode === 'config-baselines' ? 'CONFIG' : ''
    configurationLoading.value = true
    await Promise.all([
      loadConfigItemsData(),
      loadBaselineRecordsData(),
    ])
    resetConfigItemForm()
    resetBaselineForm()
    activeConfigBaselineId.value = null
    configurationTab.value = tab
    configurationDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '配置管理数据加载失败')
  } finally {
    configurationLoading.value = false
  }
}

async function openConfigurationDialog(tab = 'items') {
  await openConfigurationWorkspace(tab, 'management')
}

async function openConfigBaselineDialog() {
  await openConfigurationWorkspace('baselines', 'config-baselines')
}

function editConfigItem(item) {
  resetConfigItemForm(item)
  configurationTab.value = 'items'
}

async function saveConfigItem() {
  if (!requireProjectContentEditPermission('维护配置项')) return
  if (!configItemForm.itemName.trim()) {
    ElMessage.warning('请输入配置项名称')
    return
  }

  try {
    configurationLoading.value = true
    const isEditing = Boolean(editingConfigItemId.value)
    const payload = {
      itemName: configItemForm.itemName.trim(),
      itemType: configItemForm.itemType,
      versionNo: configItemForm.versionNo.trim() || null,
      status: configItemForm.status,
      repositoryUrl: configItemForm.repositoryUrl.trim() || null,
      remark: configItemForm.remark.trim() || null,
    }
    if (editingConfigItemId.value) {
      await updateProjectConfigItem(projectId.value, editingConfigItemId.value, payload)
    } else {
      await createProjectConfigItem(projectId.value, payload)
    }
    await loadConfigItemsData()
    resetConfigItemForm()
    ElMessage.success(isEditing ? '配置项已更新' : '配置项已新增')
  } catch (error) {
    ElMessage.error(error.message || '配置项保存失败')
  } finally {
    configurationLoading.value = false
  }
}

async function removeConfigItem(item) {
  if (!requireProjectContentEditPermission('删除配置项')) return
  try {
    await ElMessageBox.confirm(
      `确认删除配置项“${item.itemName || item.id}”吗？`,
      '删除配置项',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    configurationLoading.value = true
    await deleteProjectConfigItem(projectId.value, item.id)
    await loadConfigItemsData()
    if (editingConfigItemId.value && String(editingConfigItemId.value) === String(item.id)) {
      resetConfigItemForm()
    }
    ElMessage.success('配置项已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除配置项失败')
    }
  } finally {
    configurationLoading.value = false
  }
}

async function saveProjectBaseline() {
  if (!requireProjectContentEditPermission('创建基线')) return
  if (!baselineForm.baselineName.trim()) {
    ElMessage.warning('请输入基线名称')
    return
  }

  try {
    configurationLoading.value = true
    const baselineType = isConfigBaselineDialogMode.value ? 'CONFIG' : baselineForm.baselineType
    let snapshotJson = null
    if (baselineType === 'SCOPE') {
      snapshotJson = buildScopeBaselineSnapshot()
    } else if (baselineType === 'COST') {
      snapshotJson = buildCostBaselineSnapshot()
    }
    await createProjectBaseline(projectId.value, {
      baselineType,
      baselineName: baselineForm.baselineName.trim(),
      description: baselineForm.description.trim() || null,
      snapshotJson,
    })
    await loadBaselineRecordsData()
    resetBaselineForm()
    ElMessage.success('基线已创建')
  } catch (error) {
    ElMessage.error(error.message || '基线创建失败')
  } finally {
    configurationLoading.value = false
  }
}

async function removeProjectBaseline(item) {
  if (!requireProjectContentEditPermission('删除基线')) return
  try {
    await ElMessageBox.confirm(
      `确认删除基线“${item.baselineName || item.id}”吗？`,
      '删除基线',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    configurationLoading.value = true
    await deleteProjectBaseline(projectId.value, item.id)
    await loadBaselineRecordsData()
    if (String(activeConfigBaselineId.value) === String(item.id)) {
      activeConfigBaselineId.value = null
    }
    ElMessage.success('基线已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除基线失败')
    }
  } finally {
    configurationLoading.value = false
  }
}

async function restoreConfigBaseline(item) {
  if (!requireProjectContentEditPermission('恢复配置基线')) return
  if (!isConfigBaselineItem(item)) {
    ElMessage.warning('仅配置基线支持恢复')
    return
  }

  const snapshotItems = parseConfigBaselineSnapshot(item)
  const impactText = snapshotItems.length
    ? `这会用基线中的 ${snapshotItems.length} 个配置项覆盖当前配置列表。`
    : '该基线下没有配置项，恢复后会清空当前配置列表。'

  try {
    await ElMessageBox.confirm(
      `确认恢复配置基线“${item.baselineName || item.id}”吗？${impactText}`,
      '恢复配置基线',
      {
        type: 'warning',
        confirmButtonText: '确认恢复',
        cancelButtonText: '取消',
      },
    )
    configurationLoading.value = true
    await restoreProjectBaseline(projectId.value, item.id)
    await Promise.all([
      loadConfigItemsData(),
      loadBaselineRecordsData(),
    ])
    resetConfigItemForm()
    activeConfigBaselineId.value = item.id
    ElMessage.success('已恢复到所选配置基线')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '恢复配置基线失败')
    }
  } finally {
    configurationLoading.value = false
  }
}

async function loadSuppliersData() {
  supplierList.value = normalizeListResult(await getProjectSuppliers(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadProcurementsData() {
  procurementList.value = normalizeListResult(await getProjectProcurements(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadContractsData() {
  contractList.value = normalizeListResult(await getProjectContracts(projectId.value, { page: 1, pageSize: 200 }))
}

async function loadProcurementCostPlansData() {
  costPlans.value = normalizeListResult(await getProjectCostPlans(projectId.value))
}

async function openProcurementDialog(tab = 'suppliers') {
  try {
    procurementLoading.value = true
    await Promise.all([
      loadSuppliersData(),
      loadProcurementsData(),
      loadContractsData(),
      loadProcurementCostPlansData(),
    ])
    resetSupplierForm()
    resetProcurementForm()
    resetProcurementStatusForm()
    resetContractForm()
    procurementTab.value = tab
    procurementDialogVisible.value = true
  } catch (error) {
    ElMessage.error(error.message || '采购管理数据加载失败')
  } finally {
    procurementLoading.value = false
  }
}

function editSupplier(item) {
  resetSupplierForm(item)
  procurementTab.value = 'suppliers'
}

async function saveSupplier() {
  if (!requireProjectContentEditPermission('维护供应商')) return
  if (!supplierForm.supplierName.trim()) {
    ElMessage.warning('请输入供应商名称')
    return
  }

  try {
    procurementLoading.value = true
    const isEditing = Boolean(editingSupplierId.value)
    const payload = {
      supplierName: supplierForm.supplierName.trim(),
      contactName: supplierForm.contactName.trim() || null,
      contactPhone: supplierForm.contactPhone.trim() || null,
      contactEmail: supplierForm.contactEmail.trim() || null,
      address: supplierForm.address.trim() || null,
      remark: supplierForm.remark.trim() || null,
    }
    if (editingSupplierId.value) {
      await updateProjectSupplier(projectId.value, editingSupplierId.value, payload)
    } else {
      await createProjectSupplier(projectId.value, payload)
    }
    await Promise.all([
      loadSuppliersData(),
      loadProcurementsData(),
      loadContractsData(),
    ])
    resetSupplierForm()
    ElMessage.success(isEditing ? '供应商已更新' : '供应商已新增')
  } catch (error) {
    ElMessage.error(error.message || '供应商保存失败')
  } finally {
    procurementLoading.value = false
  }
}

async function removeSupplier(item) {
  if (!requireProjectContentEditPermission('删除供应商')) return
  try {
    await ElMessageBox.confirm(
      `确认删除供应商“${item.supplierName || item.id}”吗？`,
      '删除供应商',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    procurementLoading.value = true
    await deleteProjectSupplier(projectId.value, item.id)
    await Promise.all([
      loadSuppliersData(),
      loadProcurementsData(),
      loadContractsData(),
    ])
    if (editingSupplierId.value && String(editingSupplierId.value) === String(item.id)) {
      resetSupplierForm()
    }
    ElMessage.success('供应商已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除供应商失败')
    }
  } finally {
    procurementLoading.value = false
  }
}

function editProcurement(item) {
  resetProcurementForm(item)
  resetProcurementStatusForm(item)
  procurementTab.value = 'procurements'
}

async function saveProcurement() {
  if (!requireProjectContentEditPermission('维护采购项')) return
  if (!procurementForm.itemName.trim()) {
    ElMessage.warning('请输入采购项名称')
    return
  }
  if (!Number.isFinite(Number(procurementForm.quantity)) || Number(procurementForm.quantity) <= 0) {
    ElMessage.warning('请输入合法的采购数量')
    return
  }
  if (procurementForm.unitPrice !== '' && !Number.isFinite(Number(procurementForm.unitPrice))) {
    ElMessage.warning('请输入合法的采购单价')
    return
  }

  try {
    procurementLoading.value = true
    const isEditing = Boolean(editingProcurementId.value)
    const payload = {
      supplierId: toNullableIdValue(procurementForm.supplierId),
      costPlanId: toNullableIdValue(procurementForm.costPlanId),
      itemName: procurementForm.itemName.trim(),
      quantity: Number(procurementForm.quantity),
      unitPrice: procurementForm.unitPrice === '' ? null : Number(procurementForm.unitPrice),
      status: procurementForm.status,
      expectedDeliveryDate: procurementForm.expectedDeliveryDate || null,
      actualDeliveryDate: procurementForm.actualDeliveryDate || null,
    }
    if (editingProcurementId.value) {
      await updateProjectProcurement(projectId.value, editingProcurementId.value, payload)
    } else {
      await createProjectProcurement(projectId.value, payload)
    }
    await loadProcurementsData()
    resetProcurementForm()
    ElMessage.success(isEditing ? '采购项已更新' : '采购项已新增')
  } catch (error) {
    ElMessage.error(error.message || '采购项保存失败')
  } finally {
    procurementLoading.value = false
  }
}

async function quickUpdateProcurementStatus() {
  if (!requireProjectContentEditPermission('更新采购执行状态')) return
  if (!procurementStatusForm.procurementId) {
    ElMessage.warning('请选择一个采购项')
    return
  }

  try {
    procurementLoading.value = true
    await updateProjectProcurementStatus(projectId.value, procurementStatusForm.procurementId, {
      status: procurementStatusForm.status,
      actualDeliveryDate: procurementStatusForm.actualDeliveryDate || null,
    })
    await loadProcurementsData()
    const current = procurementList.value.find((item) => String(item.id) === String(procurementStatusForm.procurementId))
    resetProcurementStatusForm(current || null)
    ElMessage.success('采购执行状态已更新')
  } catch (error) {
    ElMessage.error(error.message || '采购执行状态更新失败')
  } finally {
    procurementLoading.value = false
  }
}

async function removeProcurement(item) {
  if (!requireProjectContentEditPermission('删除采购项')) return
  try {
    await ElMessageBox.confirm(
      `确认删除采购项“${item.itemName || item.id}”吗？`,
      '删除采购项',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    procurementLoading.value = true
    await deleteProjectProcurement(projectId.value, item.id)
    await loadProcurementsData()
    if (editingProcurementId.value && String(editingProcurementId.value) === String(item.id)) {
      resetProcurementForm()
    }
    if (String(procurementStatusForm.procurementId || '') === String(item.id)) {
      resetProcurementStatusForm()
    }
    ElMessage.success('采购项已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除采购项失败')
    }
  } finally {
    procurementLoading.value = false
  }
}

function editContract(item) {
  resetContractForm(item)
  procurementTab.value = 'contracts'
}

async function saveContract() {
  if (!requireProjectContentEditPermission('维护合同')) return
  if (!contractForm.contractNo.trim()) {
    ElMessage.warning('请输入合同编号')
    return
  }
  if (!contractForm.contractName.trim()) {
    ElMessage.warning('请输入合同名称')
    return
  }
  if (contractForm.totalAmount !== '' && !Number.isFinite(Number(contractForm.totalAmount))) {
    ElMessage.warning('请输入合法的合同总额')
    return
  }

  try {
    procurementLoading.value = true
    const isEditing = Boolean(editingContractId.value)
    const payload = {
      supplierId: toNullableIdValue(contractForm.supplierId),
      contractNo: contractForm.contractNo.trim(),
      contractName: contractForm.contractName.trim(),
      contractType: contractForm.contractType,
      signDate: contractForm.signDate || null,
      totalAmount: contractForm.totalAmount === '' ? null : Number(contractForm.totalAmount),
      deliverables: contractForm.deliverables.trim() || null,
      paymentTerms: contractForm.paymentTerms.trim() || null,
      status: contractForm.status,
    }
    if (editingContractId.value) {
      await updateProjectContract(projectId.value, editingContractId.value, payload)
    } else {
      await createProjectContract(projectId.value, payload)
    }
    await loadContractsData()
    resetContractForm()
    ElMessage.success(isEditing ? '合同已更新' : '合同已新增')
  } catch (error) {
    ElMessage.error(error.message || '合同保存失败')
  } finally {
    procurementLoading.value = false
  }
}

async function removeContract(item) {
  if (!requireProjectContentEditPermission('删除合同')) return
  try {
    await ElMessageBox.confirm(
      `确认删除合同“${item.contractName || item.contractNo || item.id}”吗？`,
      '删除合同',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    procurementLoading.value = true
    await deleteProjectContract(projectId.value, item.id)
    await loadContractsData()
    if (editingContractId.value && String(editingContractId.value) === String(item.id)) {
      resetContractForm()
    }
    ElMessage.success('合同已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除合同失败')
    }
  } finally {
    procurementLoading.value = false
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
  if (!requireProjectContentEditPermission('维护范围基线')) return
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
  if (!requireProjectManagePermission('生成项目报表')) return
  try {
    loading.value = true
    const report = await generateProjectReport(projectId.value, {
      type: reportGenerateForm.type,
      startDate: reportGenerateForm.startDate || null,
      endDate: reportGenerateForm.endDate || null,
    })
    await openReportDialog()
    openReportPreview(report)
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
  if (!requireProjectManagePermission('调整项目状态')) return
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
    ElMessage.warning('当前项目身份不能登记工时')
    return
  }
  try {
    loading.value = true
    resetTimesheetFilters()
    loadedTasks.value = normalizeListResult(await getProjectTasks(projectId.value)).map(normalizeTaskProgressRecord)
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
    ElMessage.warning('当前项目身份不能登记工时')
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
  if (!canEditTimesheetRecord(item)) {
    ElMessage.warning('只能删除自己登记的工时，创建者可删除全部工时')
    return
  }
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
  if (!requireProjectContentEditPermission('维护归档项')) return
  if (!archiveForm.itemName.trim()) {
    ElMessage.warning('请填写归档项名称')
    return
  }
  try {
    loading.value = true
    await createProjectArchive(projectId.value, {
      itemName: archiveForm.itemName.trim(),
      archiveType: archiveForm.archiveType,
      attachmentId: toNullableIdValue(archiveForm.attachmentId),
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
  if (!requireProjectContentEditPermission('上传归档附件')) return
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
  if (!requireProjectContentEditPermission('移除归档附件')) return
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
  if (!requireProjectContentEditPermission('上传验收附件')) return
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
  if (!requireProjectContentEditPermission('移除验收附件')) return
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
  if (!requireProjectContentEditPermission('删除归档项')) return
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
  if (!requireProjectContentEditPermission('维护经验教训')) return
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
  if (!requireProjectContentEditPermission('删除经验教训')) return
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

async function removeReport(item) {
  if (!requireProjectManagePermission('删除项目报表')) return
  try {
    await ElMessageBox.confirm(
      `确认删除报表“${normalizeReportTitle(item?.title) || item?.id}”吗？`,
      '删除报表',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
      },
    )
    loading.value = true
    await deleteProjectReport(projectId.value, item.id)
    reportList.value = normalizeListResult(await getProjectReports(projectId.value))
    if (String(selectedReportPreview.value?.id || '') === String(item?.id || '')) {
      selectedReportPreview.value = null
      reportPreviewDialogVisible.value = false
    }
    ElMessage.success('报表已删除')
  } catch (error) {
    if (error !== 'cancel' && error?.message !== 'cancel') {
      ElMessage.error(error.message || '删除报表失败')
    }
  } finally {
    loading.value = false
  }
}

async function generateSummaryReportAction() {
  if (!requireProjectManagePermission('生成项目总结报告')) return
  try {
    loading.value = true
    const report = await generateProjectSummaryReport(projectId.value, {
      type: closureSummaryForm.type,
      startDate: closureSummaryForm.startDate || null,
      endDate: closureSummaryForm.endDate || null,
    })
    await loadProjectClosureData()
    openReportPreview(report)
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
  if (!requireProjectContentEditPermission('维护验收事项')) return
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
  if (!requireProjectContentEditPermission('删除验收事项')) return
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
  if (!requireProjectContentEditPermission('删除风险')) return
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
  if (!canSubmitChangeRequest.value) {
    ElMessage.warning('当前角色不能提交变更申请')
    return
  }
  if (!requireProjectContentEditPermission('提交变更申请')) return
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
  if (!canApproveChangeRequest.value) {
    ElMessage.warning('当前角色不能审批变更申请')
    return
  }
  if (!requireProjectManagePermission('审批变更申请')) return
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
  if (!requireProjectContentEditPermission('生成变更基线')) return
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
  if (!requireProjectContentEditPermission('维护成本计划')) return
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
  if (!requireProjectContentEditPermission('删除成本计划')) return
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
  if (!requireProjectContentEditPermission('登记实际成本')) return
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
  if (!requireProjectContentEditPermission('删除实际成本')) return
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
  if (!requireProjectContentEditPermission('维护成本基线')) return
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
  if (!requireProjectContentEditPermission('删除成本基线')) return
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
  if (!requireProjectContentEditPermission('删除范围基线')) return
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
  if (!requireProjectManagePermission('删除当前项目')) return
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
  if (!requireProjectManagePermission('关闭当前项目')) return
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
    case 'pending-review': openPendingReviewDialog(); break
    case 'project-reschedule': rerunProjectSchedule(); break
    case 'project-task-overview':
      taskMineOnly.value = false
      break
    case 'my-tasks':
      taskMineOnly.value = true
      break
    case 'project-info': openProjectInfo(); break
    case 'project-charter': openProjectCharterDialog(); break
    case 'project-members': openProjectMembersDialog(); break
    case 'project-dashboard': openDashboardDialog(); break
    case 'project-milestones': openMilestoneDialog(); break
    case 'project-risks': openRiskDialog(); break
    case 'project-risk-matrix': openRiskMatrixDialog(); break
    case 'project-quality-plans': openQualityPlanDialog(); break
    case 'project-quality-activities': openQualityActivityDialog(); break
    case 'project-quality-metrics': openQualityMetricDialog(); break
    case 'project-quality': openQualityPlanDialog(); break
    case 'project-test-plans': openTestPlanDialog(); break
    case 'project-test-cases': openTestCaseDialog(); break
    case 'project-defects': openDefectDialog(); break
    case 'project-test-reports': openTestReportDialog(); break
    case 'project-testing': openTestPlanDialog(); break
    case 'project-communication-matrix': openCommunicationMatrixDialog(); break
    case 'project-meetings': openMeetingDialog(); break
    case 'project-communication-records': openCommunicationRecordDialog(); break
    case 'project-communication': openMeetingDialog(); break
    case 'project-configuration': openConfigurationDialog(); break
    case 'project-config-baselines': openConfigBaselineDialog(); break
    case 'project-procurement': openProcurementDialog(); break
    case 'project-procurement-suppliers': openProcurementDialog('suppliers'); break
    case 'project-procurement-items': openProcurementDialog('procurements'); break
    case 'project-procurement-contracts': openProcurementDialog('contracts'); break
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
  const start = normalizeDateValueToScheduleMode(projectForm.startDate || todayString) || todayString
  const projectEnd = normalizeDateValueToScheduleMode(projectForm.endDate)
  const baseEnd = projectEnd && projectEnd > start
    ? projectEnd
    : formatDateToValue(addDays(parseDateValue(start), 27))
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
        isWeekend: isWeekendDate(current),
        isHoliday: isConfiguredHolidayDate(current),
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

function getTaskBarDaySpan(row) {
  if (!row?.start || !row?.finish) return 0
  const startIndex = timelineDays.value.findIndex((day) => day.date === row.start)
  const endIndex = timelineDays.value.findIndex((day) => day.date === row.finish)
  if (startIndex < 0 || endIndex < startIndex) return 0
  return endIndex - startIndex + 1
}

function shouldShowTaskProgressText(row) {
  return !isMilestoneRow(row) && !isSummaryRow(row)
}

function getTaskProgressLabel(row) {
  return formatPercentValue(clampTaskProgressValue(row?.progress ?? 0))
}

function getTaskProgressTextClass(row) {
  const span = getTaskBarDaySpan(row)
  const isParent = hasChildRows(row)
  const isCollapsedParent = isParent && row.expanded === false
  return {
    'task-progress-text': true,
    'task-progress-text-compact': span > 0 && span <= 1 && !isCollapsedParent,
    'task-progress-text-parent': isParent && !isCollapsedParent,
    'task-progress-text-parent-collapsed': isCollapsedParent,
  }
}

function getTaskBarClass(row) {
  const type = getRowBarType(row)
  const config = getBarStyleConfig(type)
  const variant = getTaskBarVariant(row)
  const isParentShell = hasChildRows(row) && !isSummaryRow(row) && !isMilestoneRow(row)
  return {
    'task-bar': true,
    [`${type}-bar`]: true,
    [`bar-variant-${variant}`]: isTaskBarType(type),
    [`task-shape-${config.shape}`]: type === 'main' || type === 'child' || type === 'parent',
    [`summary-shape-${config.shape}`]: type === 'summary',
    'parent-shell-bar': isParentShell,
    'parent-shell-expanded': isParentShell && row.expanded !== false,
    'parent-shell-collapsed': isParentShell && row.expanded === false,
  }
}

const visibleTaskRows = computed(() => {
  if (!effectiveTaskMineOnly.value || !currentUserId.value) {
    return taskRows.value.filter((row) => !isHiddenByCollapsedAncestor(row))
  }

  const rowByLocalId = new Map(taskRows.value.map((row) => [row.localId, row]))
  const childrenByParentLocalId = new Map()
  taskRows.value.forEach((row) => {
    if (!row.localParentId) return
    if (!childrenByParentLocalId.has(row.localParentId)) {
      childrenByParentLocalId.set(row.localParentId, [])
    }
    childrenByParentLocalId.get(row.localParentId).push(row)
  })

  const visibleLocalIds = new Set()
  taskRows.value.forEach((row) => {
    if (isBlankPlaceholderRow(row)) return
    if (getEffectiveRowAssigneeId(row) !== currentUserId.value) return
    visibleLocalIds.add(row.localId)

    let parentLocalId = row.localParentId
    while (parentLocalId) {
      visibleLocalIds.add(parentLocalId)
      const parentRow = rowByLocalId.get(parentLocalId)
      parentLocalId = parentRow?.localParentId || null
    }

    const queue = [...(childrenByParentLocalId.get(row.localId) || [])]
    while (queue.length) {
      const child = queue.shift()
      if (!child) continue
      visibleLocalIds.add(child.localId)
      const descendants = childrenByParentLocalId.get(child.localId)
      if (descendants?.length) {
        queue.push(...descendants)
      }
    }
  })

  return taskRows.value.filter((row) => {
    if (isBlankPlaceholderRow(row)) return false
    return visibleLocalIds.has(row.localId) && !isHiddenByCollapsedAncestor(row)
  })
})

function getRowTimelineBox(row) {
  if (isMilestoneRow(row)) {
    const style = getMilestoneStyle(row)
    if (!style) return null
    return {
      left: parsePixelValue(style.left),
      top: parsePixelValue(style.top),
      width: parsePixelValue(style.width, 16),
      height: parsePixelValue(style.height, 16),
    }
  }

  const style = getTaskBarStyle(row)
  if (!style) return null
  return {
    left: parsePixelValue(style.left),
    top: parsePixelValue(style.top),
    width: parsePixelValue(style.width),
    height: parsePixelValue(style.height),
  }
}

function getRowTimelineAnchor(row, side = 'end') {
  const box = getRowTimelineBox(row)
  if (!box) return null
  return {
    x: side === 'start' ? box.left : box.left + box.width,
    y: box.top + box.height / 2,
  }
}

function resolveVisibleDependencyEndpoint(taskId, visibleRowIndexMap, taskRowByTaskId, taskRowByLocalId) {
  if (taskId == null || taskId === '') return null
  const sourceRow = taskRowByTaskId.get(String(taskId))
  if (!sourceRow) return null

  const directIndex = visibleRowIndexMap.get(sourceRow.localId)
  if (directIndex != null) {
    return { row: sourceRow, index: directIndex }
  }

  let currentParentId = sourceRow.localParentId
  while (currentParentId) {
    const parentRow = taskRowByLocalId.get(currentParentId)
    if (!parentRow) break
    const visibleIndex = visibleRowIndexMap.get(parentRow.localId)
    if (visibleIndex != null && parentRow.expanded === false) {
      return { row: parentRow, index: visibleIndex }
    }
    currentParentId = parentRow.localParentId
  }

  return null
}

const parentShellOverlays = computed(() => {
  const rowHeight = 38
  const rowIndexMap = new Map(visibleTaskRows.value.map((row, index) => [row.localId, index]))

  return visibleTaskRows.value
    .map((row, index) => {
      if (!hasChildRows(row) || isSummaryRow(row) || isMilestoneRow(row)) return null
      if (row.expanded === false) return null

      const parentBox = getRowTimelineBox(row)
      if (!parentBox) return null

      const descendants = getDescendantRows(row.localId).filter(
        (item) => !isBlankPlaceholderRow(item) && !isHiddenByCollapsedAncestor(item),
      )
      if (!descendants.length) return null
      const tailRow = descendants.length ? descendants[descendants.length - 1] : row
      const tailIndex = rowIndexMap.get(tailRow.localId) ?? index
      const tailBox = getRowTimelineBox(tailRow)
      const bottomOffset = Number(row.outlineLevel || 0) === 0 ? 8 : 6
      const sideEdge = Number(row.outlineLevel || 0) === 0 ? 8 : 6
      const tailBottom = tailBox
        ? tailIndex * rowHeight + tailBox.top + tailBox.height + bottomOffset
        : (tailIndex + 1) * rowHeight - 6
      const top = index * rowHeight + parentBox.top
      const height = Math.max(parentBox.height, tailBottom - top)
      const stroke = Number(row.outlineLevel || 0) === 0 ? 3 : 2

      return {
        key: `parent-shell-${row.localId}`,
        style: {
          left: `${parentBox.left}px`,
          top: `${top}px`,
          width: `${parentBox.width}px`,
          height: `${height}px`,
          color: getBarStyleConfig(getRowBarType(row)).border,
          '--parent-shell-stroke': `${stroke}px`,
          '--parent-shell-side-edge': `${sideEdge}px`,
        },
      }
    })
    .filter(Boolean)
})

const ganttDependencyPaths = computed(() => {
  const visibleRowIndexMap = new Map(visibleTaskRows.value.map((row, index) => [row.localId, index]))
  const taskRowByTaskId = new Map(
    taskRows.value
      .filter((row) => row.taskId != null && row.taskId !== '')
      .map((row) => [String(row.taskId), row]),
  )
  const taskRowByLocalId = new Map(taskRows.value.map((row) => [row.localId, row]))
  const rowHeight = 38
  const visualPathKeys = new Set()

  return projectTaskDependencies.value
    .map((dependency) => {
      const predecessorMeta = resolveVisibleDependencyEndpoint(
        dependency.predecessorTaskId,
        visibleRowIndexMap,
        taskRowByTaskId,
        taskRowByLocalId,
      )
      const successorMeta = resolveVisibleDependencyEndpoint(
        dependency.successorTaskId,
        visibleRowIndexMap,
        taskRowByTaskId,
        taskRowByLocalId,
      )
      if (!predecessorMeta || !successorMeta) return null

      const type = String(dependency.dependencyType || 'FS').toUpperCase()
      if (predecessorMeta.row.localId === successorMeta.row.localId) return null

      const visualKey = `${predecessorMeta.row.localId}:${successorMeta.row.localId}:${type}`
      if (visualPathKeys.has(visualKey)) return null
      visualPathKeys.add(visualKey)

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
        id: visualKey,
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

async function confirmGanttStyleDialog() {
  if (!requireProjectContentEditPermission('保存条形图样式')) return
  try {
    loading.value = true
    await persistProjectEditorPreferenceData()
    ganttAppearanceSnapshot = null
    ganttStyleDialogVisible.value = false
    ElMessage.success('条形图样式已保存')
  } catch (error) {
    ElMessage.error(error.message || '条形图样式保存失败')
  } finally {
    loading.value = false
  }
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

function handleGlobalPointerDown(event) {
  if (!contextMenuState.visible) return
  const target = event.target
  if (target instanceof Node && editorContextMenuRef.value?.contains(target)) return
  closeContextMenu()
}

function handleGlobalKeydown(event) {
  if (event.key === 'Escape') {
    closeContextMenu()
  }
}

function handleGlobalViewportChange() {
  closeContextMenu()
}

watch(
  () => taskProgressForm.progress,
  () => {
    syncTaskProgressFormFromProgress()
  },
)

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
  () => defectForm.testCaseId,
  (value) => {
    if (!value) return
    const selected = testCaseOptions.value.find((item) => item.value === value)
    if (!selected) return
    defectForm.requirementId = selected.requirementId || ''
    defectForm.taskId = selected.taskId || ''
  },
)

watch(
  () => procurementStatusForm.procurementId,
  (value) => {
    if (!value) return
    const selected = procurementList.value.find((item) => String(item.id) === String(value))
    if (!selected) return
    procurementStatusForm.status = selected.status || 'PLANNED'
    procurementStatusForm.actualDeliveryDate = selected.actualDeliveryDate ? formatDateText(selected.actualDeliveryDate) : ''
  },
)

watch(
  () => testCaseForm.executionStatus,
  (value) => {
    if (value === 'NOT_RUN') {
      testCaseForm.executedAt = ''
    }
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
  document.addEventListener('pointerdown', handleGlobalPointerDown)
  document.addEventListener('scroll', handleGlobalViewportChange, true)
  window.addEventListener('resize', handleGlobalViewportChange)
  window.addEventListener('keydown', handleGlobalKeydown)

  try {
    loading.value = true
    if (!projectId.value) throw new Error('未找到有效项目')
    await Promise.all([
      loadProjectDetailData(),
      loadProjectTasksData(),
      loadProjectMembersData(),
      loadProjectMilestonesData(),
      loadProjectRisksData(),
      loadProjectRiskMatrixData(),
    ])
    await loadProjectEditorPreferenceData({ migrateLocalFallback: true })
    await openRouteTaskIfNeeded()
    // Initial preference hydration can recalculate WBS/task presentation and
    // briefly trip the deep row watcher even when the user changed nothing.
    hasUnsavedChanges.value = false
  } catch (error) {
    ElMessage.error(error.message || '项目加载失败')
    router.replace({ name: 'dashboard' })
  } finally {
    loading.value = false
  }
})

onBeforeUnmount(() => {
  document.removeEventListener('pointerdown', handleGlobalPointerDown)
  document.removeEventListener('scroll', handleGlobalViewportChange, true)
  window.removeEventListener('resize', handleGlobalViewportChange)
  window.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<template>
  <section class="editor-page" @contextmenu.prevent="openPageContextMenu">
    <header class="editor-toolbar" @contextmenu.prevent.stop="openToolbarContextMenu">
      <div class="toolbar-tabs">
        <button type="button" class="nav-back" title="返回项目列表" @click="navigateBack">
          ←
        </button>
        <button type="button" class="save-shortcut" :disabled="saving || !canSaveTaskPlan" title="保存文件" @click="saveEditorContent">
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
        <div class="toolbar-spacer" />
        <div class="toolbar-state">
          <span v-if="saving">保存中...</span>
          <span v-else-if="hasUnsavedChanges">未保存</span>
          <span v-else>已保存</span>
        </div>
        <div class="toolbar-user">
          <UserAvatar
            :avatar-url="currentAuthUser?.avatarUrl"
            :username="currentAuthUser?.username"
            :display-name="currentUserDisplayName"
            :size="34"
          />
          <div class="toolbar-user-copy">
            <strong>{{ currentUserDisplayName }}</strong>
            <span>{{ currentAuthUser?.username || '-' }}</span>
          </div>
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
      <div class="schedule-banner" @contextmenu.prevent.stop="openBannerContextMenu">
        {{ projectDetail?.name || '项目计划' }}
        <span v-if="projectDetail?.status"> | {{ formatProjectStatus(projectDetail.status) }}</span>
        <span v-if="activeTab === 'task'" class="schedule-banner-view">{{ taskViewModeLabel }}</span>
        <span v-if="activeTab === 'task'" class="schedule-banner-hint">{{ taskViewModeHint }}</span>
        <span class="schedule-banner-hint">右键快速操作</span>
      </div>

      <div
        v-if="activeTab === 'task' && selectedTaskRow && !isBlankPlaceholderRow(selectedTaskRow)"
        class="task-action-banner"
      >
        <div class="task-action-banner-copy">
          <strong>{{ selectedTaskRow.name || `任务 ${selectedTaskRow.taskId || ''}` }}</strong>
          <span>
            {{ formatTaskStatus(taskDetail?.status || selectedTaskRow.status) }}
            <template v-if="selectedTaskRow.assigneeName"> | 负责人: {{ selectedTaskRow.assigneeName }}</template>
            <template v-if="selectedTaskActionHint"> | {{ selectedTaskActionHint }}</template>
          </span>
        </div>
        <div class="task-action-banner-actions">
          <el-button size="small" plain @click="openSelectedTaskProgressDialog">打开进度</el-button>
          <el-button
            type="primary"
            size="small"
            :disabled="!selectedTaskCanSubmitReview"
            :loading="taskDetailLoading"
            :title="selectedTaskSubmitBlockedReason || '将当前任务提交给项目负责人验收'"
            @click="quickSubmitSelectedTaskCompletion"
          >
            提交完成
          </el-button>
          <template v-if="canManageProject">
            <el-button
              v-if="selectedTaskCanReopen"
              size="small"
              type="primary"
              plain
              :loading="taskDetailLoading"
              @click="reopenTaskCompletion"
            >
              重开任务
            </el-button>
            <el-button
              type="success"
              size="small"
              :disabled="!selectedTaskCanApproveReview"
              :loading="taskDetailLoading"
              :title="selectedTaskApproveBlockedReason || '确认成员提交的任务已验收通过'"
              @click="quickApproveSelectedTaskCompletion"
            >
              通过验收
            </el-button>
            <el-button
              type="warning"
              size="small"
              plain
              :disabled="!selectedTaskCanApproveReview"
              :loading="taskDetailLoading"
              :title="selectedTaskApproveBlockedReason || '填写原因后将任务打回给成员继续修改'"
              @click="quickRejectSelectedTaskCompletion"
            >
              打回修改
            </el-button>
          </template>
          <el-button
            v-if="canManageProject"
            size="small"
            plain
            :disabled="pendingReviewTaskCount <= 0"
            @click="openPendingReviewDialog"
          >
            待验收列表（{{ pendingReviewTaskCount }}）
          </el-button>
        </div>
        <div
          v-if="!canManageProject && selectedTaskSubmitBlockedReason"
          class="task-action-banner-reason"
        >
          当前不能提交完成：{{ selectedTaskSubmitBlockedReason }}
        </div>
        <div
          v-else-if="canManageProject && !selectedTaskCanApproveReview && selectedTaskApproveBlockedReason"
          class="task-action-banner-reason"
        >
          当前不能直接验收：{{ selectedTaskApproveBlockedReason }}
        </div>
      </div>

      <div class="gantt-shell" :style="ganttThemeStyle">
        <section class="task-grid" :class="{ 'task-grid-readonly': !canSaveTaskPlan }" @contextmenu.prevent.stop="openTaskGridContextMenu">
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
            :class="{
              active: selectedTaskRow?.localId === row.localId,
              'parent-task-row': hasChildRows(row) && !isSummaryRow(row),
              'placeholder-task-row': isBlankPlaceholderRow(row),
            }"
            @contextmenu.prevent.stop="openRowContextMenu($event, row, 'task-grid')"
          >
            <div class="grid-cell indicator" :class="{ 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }">{{ getRowDisplayNumber(row) }}</div>
            <div class="grid-cell wbs" :class="{ 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }">
              {{ row.wbsCode || '-' }}
            </div>

            <div
              class="grid-cell mode editable"
              :class="{ active: canEditTaskField(row, 'mode') && isActiveCell(row.localId, 'mode'), 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }"
              @click="activateEditableCell(row.localId, 'mode')"
            >
              <input
                v-model="row.mode"
                type="text"
                :readonly="!canEditTaskField(row, 'mode')"
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
              :class="{ active: canEditTaskField(row, 'name') && isActiveCell(row.localId, 'name'), 'parent-task-cell': hasChildRows(row) && !isSummaryRow(row) }"
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
                :readonly="!canEditTaskField(row, 'name')"
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

            <div class="grid-cell duration editable" :class="{ active: canEditTaskField(row, 'duration') && isActiveCell(row.localId, 'duration') }" @click="activateEditableCell(row.localId, 'duration')">
              <input
                v-model="row.duration"
                type="text"
                :readonly="!canEditTaskField(row, 'duration')"
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

            <div class="grid-cell start editable" :class="{ active: canEditTaskField(row, 'start') && isActiveCell(row.localId, 'start') }" @click="activateEditableCell(row.localId, 'start')">
              <el-date-picker
                v-model="row.start"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                :editable="false"
                :disabled="!canEditTaskField(row, 'start')"
                :disabled-date="(date) => isDateDisabledByParent(row, date)"
                :ref="(el) => setInputRef(row.localId, 'start', el)"
                class="date-editor"
                @change="handleStartInput(row)"
                @focus="activateEditableCell(row.localId, 'start')"
              />
            </div>

            <div class="grid-cell finish editable" :class="{ active: canEditTaskField(row, 'finish') && isActiveCell(row.localId, 'finish') }" @click="activateEditableCell(row.localId, 'finish')">
              <el-date-picker
                v-model="row.finish"
                type="date"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                :editable="false"
                :disabled="!canEditTaskField(row, 'finish')"
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
          <div class="timeline-header" :style="{ width: `${timelineDayCount * DAY_WIDTH}px` }" @contextmenu.prevent.stop="openTimelineContextMenu($event, 'timeline-header')">
            <div v-for="week in timelineWeeks" :key="week.key" class="week-block" :style="{ width: `${week.days.length * DAY_WIDTH}px` }">
              <div class="week-label">{{ week.label }}</div>
              <div class="week-days" :style="{ gridTemplateColumns: `repeat(${week.days.length}, ${DAY_WIDTH}px)` }">
                <span v-for="day in week.days" :key="day.key">{{ day.label }}</span>
              </div>
            </div>
          </div>

          <div class="timeline-body" @click="handleTimelineBlankClick" @contextmenu.prevent.stop="openTimelineContextMenu($event, 'timeline-body')">
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
              v-for="shell in parentShellOverlays"
              :key="shell.key"
              class="parent-shell-overlay"
              :style="shell.style"
            >
              <span class="parent-shell-overlay-lid parent-shell-overlay-lid-top" />
              <span class="parent-shell-overlay-lid parent-shell-overlay-lid-bottom" />
            </div>

            <div
              v-for="row in visibleTaskRows"
              :key="`timeline-${row.localId}`"
              class="timeline-row"
              :class="{ active: selectedTaskRow?.localId === row.localId }"
              :style="{ gridTemplateColumns: `repeat(${timelineDayCount}, ${DAY_WIDTH}px)` }"
              @contextmenu.prevent.stop="openRowContextMenu($event, row, 'timeline')"
            >
              <span
                v-for="day in timelineDays"
                :key="`${row.localId}-${day.key}`"
                class="timeline-cell"
                :class="{ weekend: day.isWeekend, holiday: day.isHoliday && !day.isWeekend, disabled: day.isBeyondEnd }"
              />
              <div
                v-if="getTaskBarStyle(row)"
                :class="getTaskBarClass(row)"
                :style="getTaskBarStyle(row)"
                @click.stop="handleGanttItemClick(row)"
                :title="`${row.name || '未命名任务'} | ${getTaskProgressLabel(row)}`"
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
                <span
                  v-if="shouldShowTaskProgressText(row)"
                  :class="getTaskProgressTextClass(row)"
                >
                  {{ getTaskProgressLabel(row) }}
                </span>
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

    <div
      v-if="contextMenuState.visible"
      ref="editorContextMenuRef"
      class="editor-context-menu"
      :style="{ left: `${contextMenuPosition.x}px`, top: `${contextMenuPosition.y}px` }"
      @contextmenu.prevent.stop
    >
      <div class="context-menu-header">
        <strong>{{ contextMenuState.title }}</strong>
        <span v-if="contextMenuState.subtitle">{{ contextMenuState.subtitle }}</span>
      </div>
      <div class="context-menu-categories">
        <button
          v-for="group in contextMenuState.groups"
          :key="`${contextMenuState.area}-${group.title}`"
          type="button"
          class="context-menu-category"
          data-context-group-trigger="true"
          :class="{ active: activeContextMenuGroup?.title === group.title }"
          @mouseenter="openContextSubmenu(group.title, $event.currentTarget)"
          @focus="openContextSubmenu(group.title, $event.currentTarget)"
          @click="openContextSubmenu(group.title, $event.currentTarget)"
        >
          <span>{{ group.title }}</span>
          <span class="context-menu-category-arrow">›</span>
        </button>
      </div>
      <div
        v-if="activeContextMenuGroup"
        ref="editorContextSubmenuRef"
        class="context-menu-submenu-panel"
        :class="{ 'submenu-panel-left': contextMenuState.submenuDirection === 'left' }"
        :style="{ top: `${contextMenuState.submenuTop}px` }"
        @contextmenu.prevent.stop
      >
        <div class="context-menu-submenu-title">
          {{ activeContextMenuGroup.title }}
        </div>
        <div class="context-menu-actions">
          <button
            v-for="action in activeContextMenuGroup.actions"
            :key="action.key"
            type="button"
            class="context-menu-action"
            :class="[
              { active: action.active, 'context-menu-action-danger': action.tone === 'danger' },
            ]"
            :disabled="action.disabled"
            @click="handleContextMenuAction(action.key)"
          >
            <strong>{{ action.label }}</strong>
            <span>{{ action.description }}</span>
          </button>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="ganttStyleDialogVisible"
      title="条形图样式"
      width="1120px"
      class="gantt-style-dialog"
      :close-on-click-modal="false"
      :before-close="handleGanttStyleDialogBeforeClose"
    >
      <div class="gantt-style-layout" :class="{ 'style-layout-readonly': !canEditProjectContent }">
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
                <span v-if="item.key === 'parent'" class="parent-box-lid parent-box-lid-top" />
                <span v-if="item.key === 'parent'" class="parent-box-lid parent-box-lid-bottom" />
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
                <span v-if="selectedBarStyleType === 'parent'" class="parent-box-lid parent-box-lid-top" />
                <span v-if="selectedBarStyleType === 'parent'" class="parent-box-lid parent-box-lid-bottom" />
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
        <el-button @click="cancelGanttStyleDialog">{{ canEditProjectContent ? '取消' : '关闭' }}</el-button>
        <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="confirmGanttStyleDialog">确定</el-button>
      </template>
    </el-dialog>

    <ProjectWbsDialog
      :visible="wbsDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :can-edit-task-plan="canEditTaskPlan"
      :loading="loading"
      :wbs-config="wbsConfig"
      :schedule-options="scheduleOptions"
      :schedule-mode-options="scheduleModeOptions"
      :current-schedule-mode-label="currentScheduleModeLabel"
      :current-schedule-mode-description="currentScheduleModeDescription"
      :schedule-holiday-summary="scheduleHolidaySummary"
      :disabled-holiday-picker-date="disabledHolidayPickerDate"
      :wbs-tree-nodes="wbsTreeNodes"
      :format-wbs-code="formatWbsCode"
      :is-wbs-tree-expanded="isWbsTreeExpanded"
      @update:visible="wbsDialogVisible = $event"
      @cancel="cancelWbsDialog"
      @save="saveWbsConfig"
      @rerun-project-schedule="rerunProjectSchedule"
      @resequence-wbs-codes="resequenceWbsCodes"
      @toggle-wbs-tree-node="toggleWbsTreeNode"
    />

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
          <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="saveRequirement">新增需求</el-button>
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

    <ProjectQualityPlanDialog
      :visible="qualityPlanDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-quality-plan-id="editingQualityPlanId"
      :quality-loading="qualityLoading"
      :quality-plans="qualityPlans"
      :quality-plan-form="qualityPlanForm"
      :quality-plan-status-options="qualityPlanStatusOptions"
      :task-assignee-options="taskAssigneeOptions"
      :formatters="qualityPlanDialogFormatters"
      :actions="qualityPlanDialogActions"
      @update:visible="qualityPlanDialogVisible = $event"
    />

    <ProjectQualityActivityDialog
      :visible="qualityActivityDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-quality-activity-id="editingQualityActivityId"
      :quality-loading="qualityLoading"
      :quality-activities="qualityActivities"
      :quality-activity-form="qualityActivityForm"
      :quality-plan-options="qualityPlanOptions"
      :quality-activity-type-options="qualityActivityTypeOptions"
      :task-assignee-options="taskAssigneeOptions"
      :formatters="qualityActivityDialogFormatters"
      :actions="qualityActivityDialogActions"
      @update:visible="qualityActivityDialogVisible = $event"
    />

    <ProjectQualityMetricDialog
      :visible="qualityMetricDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-quality-metric-id="editingQualityMetricId"
      :quality-loading="qualityLoading"
      :quality-metrics="qualityMetrics"
      :quality-metric-form="qualityMetricForm"
      :formatters="qualityMetricDialogFormatters"
      :actions="qualityMetricDialogActions"
      @update:visible="qualityMetricDialogVisible = $event"
    />

    <ProjectTestPlanDialog
      :visible="testPlanDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-test-plan-id="editingTestPlanId"
      :testing-loading="testingLoading"
      :test-plans="testPlans"
      :test-plan-form="testPlanForm"
      :task-assignee-options="taskAssigneeOptions"
      :test-plan-status-options="testPlanStatusOptions"
      :formatters="testPlanDialogFormatters"
      :actions="testPlanDialogActions"
      @update:visible="testPlanDialogVisible = $event"
    />

    <ProjectTestCaseDialog
      :visible="testCaseDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-test-case-id="editingTestCaseId"
      :testing-loading="testingLoading"
      :test-cases="testCases"
      :test-case-form="testCaseForm"
      :test-plan-options="testPlanOptions"
      :requirement-options="requirementOptions"
      :risk-task-options="riskTaskOptions"
      :task-assignee-options="taskAssigneeOptions"
      :test-case-status-options="testCaseStatusOptions"
      :formatters="testCaseDialogFormatters"
      :actions="testCaseDialogActions"
      @update:visible="testCaseDialogVisible = $event"
    />

    <ProjectDefectDialog
      :visible="defectDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-defect-id="editingDefectId"
      :testing-loading="testingLoading"
      :defect-list="defectList"
      :defect-form="defectForm"
      :test-case-options="testCaseOptions"
      :requirement-options="requirementOptions"
      :risk-task-options="riskTaskOptions"
      :task-assignee-options="taskAssigneeOptions"
      :defect-severity-options="defectSeverityOptions"
      :defect-priority-options="defectPriorityOptions"
      :defect-status-options="defectStatusOptions"
      :formatters="defectDialogFormatters"
      :actions="defectDialogActions"
      @update:visible="defectDialogVisible = $event"
    />

    <ProjectTestReportDialog
      :visible="testReportDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :testing-loading="testingLoading"
      :test-report-list="testReportList"
      :test-report-generate-form="testReportGenerateForm"
      :formatters="testReportDialogFormatters"
      :helpers="testReportDialogHelpers"
      :actions="testReportDialogActions"
      @update:visible="testReportDialogVisible = $event"
    />

    <ProjectCommunicationMatrixDialog
      :visible="communicationMatrixDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-communication-matrix-id="editingCommunicationMatrixId"
      :communication-loading="communicationLoading"
      :communication-matrix-list="communicationMatrixList"
      :communication-matrix-form="communicationMatrixForm"
      :communication-channel-options="communicationChannelOptions"
      :formatters="communicationMatrixDialogFormatters"
      :actions="communicationMatrixDialogActions"
      @update:visible="communicationMatrixDialogVisible = $event"
    />

    <el-dialog v-model="meetingDialogVisible" title="会议计划" width="1080px">
      <div class="info-section">
        <div class="section-header-inline">
          <h4>{{ editingMeetingId ? '编辑会议计划' : '新增会议计划' }}</h4>
          <div class="dialog-summary-text">站会、周会、评审会都统一在这里安排</div>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="会议类型">
            <el-select v-model="meetingForm.meetingType" style="width: 100%">
              <el-option v-for="option in meetingTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="会议状态">
            <el-select v-model="meetingForm.status" style="width: 100%">
              <el-option v-for="option in meetingStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="会议标题" class="scope-form-span">
            <el-input v-model="meetingForm.title" placeholder="例如 每周项目例会、UAT 评审会、上线准备会" />
          </el-form-item>
          <el-form-item label="开始时间">
            <el-date-picker
              v-model="meetingForm.scheduledAt"
              type="datetime"
              value-format="YYYY-MM-DDTHH:mm:ss"
              format="YYYY-MM-DD HH:mm:ss"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="时长(分钟)">
            <el-input v-model="meetingForm.durationMinutes" type="number" min="0" placeholder="30" />
          </el-form-item>
          <el-form-item label="主持人">
            <el-select v-model="meetingForm.hostId" clearable filterable placeholder="可选，选择主持人">
              <el-option v-for="option in taskAssigneeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="地点/链接" class="scope-form-span">
            <el-input v-model="meetingForm.location" placeholder="例如 线上腾讯会议链接、现场会议室、飞书会议号" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetMeetingForm()">重置表单</el-button>
          <el-button v-if="canEditProjectContent" type="primary" :loading="communicationLoading" @click="saveMeeting">
            {{ editingMeetingId ? '保存修改' : '新增会议' }}
          </el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>会议列表</h4>
          <div class="dialog-summary-text">便于回看计划、主持人、时间和会议完成状态</div>
        </div>
        <div v-if="meetingList.length" class="simple-list">
          <div v-for="item in meetingList" :key="`meeting-${item.id}`" class="simple-list-item">
            <strong>{{ item.title || `会议${item.id}` }}</strong>
            <span>类型: {{ formatMeetingType(item.meetingType) }} | 状态: {{ formatMeetingStatus(item.status) }} | 主持人: {{ item.hostName || '未指定' }}</span>
            <span>时间: {{ formatDateTimeText(item.scheduledAt) }} | 时长: {{ item.durationMinutes != null ? `${item.durationMinutes} 分钟` : '未设置' }}</span>
            <span>地点/链接: {{ item.location || '未设置' }}</span>
            <div v-if="canEditProjectContent" class="baseline-actions">
              <el-button size="small" @click="editMeeting(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeMeeting(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无会议计划" />
      </div>
    </el-dialog>

    <el-dialog v-model="communicationRecordDialogVisible" title="沟通记录" width="1080px">
      <div class="info-section">
        <div class="section-header-inline">
          <h4>{{ editingCommunicationRecordId ? '编辑沟通记录' : '新增沟通记录' }}</h4>
          <div class="dialog-summary-text">会议纪要、通知和关键消息都可以沉淀到项目档案里</div>
        </div>
        <div v-if="communicationRecordFilterMeetingId" class="dialog-filter-inline">
          <span>当前只显示会议“{{ communicationRecordFilterMeetingLabel || `会议${communicationRecordFilterMeetingId}` }}”的沟通记录</span>
          <el-button link type="primary" @click="clearCommunicationRecordFilter">查看全部</el-button>
        </div>
        <el-form label-width="88px" class="scope-form-grid">
          <el-form-item label="记录类型">
            <el-select v-model="communicationRecordForm.recordType" style="width: 100%">
              <el-option v-for="option in communicationRecordTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="关联会议">
            <el-select v-model="communicationRecordForm.meetingId" clearable filterable placeholder="可选，选择关联会议">
              <el-option v-for="option in meetingOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录人">
            <el-select v-model="communicationRecordForm.recorderId" clearable filterable placeholder="留空则默认当前用户">
              <el-option v-for="option in taskAssigneeOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="记录标题" class="scope-form-span">
            <el-input v-model="communicationRecordForm.title" placeholder="例如 例会纪要、客户确认事项、版本发布通知" />
          </el-form-item>
          <el-form-item label="记录内容" class="scope-form-span">
            <el-input v-model="communicationRecordForm.content" type="textarea" :rows="5" placeholder="记录讨论结论、行动项、责任人、时间节点和需要同步的关键信息" />
          </el-form-item>
        </el-form>
        <div class="dialog-actions-inline">
          <el-button @click="resetCommunicationRecordForm()">重置表单</el-button>
          <el-button v-if="canEditProjectContent" type="primary" :loading="communicationLoading" @click="saveCommunicationRecord">
            {{ editingCommunicationRecordId ? '保存修改' : '新增记录' }}
          </el-button>
        </div>
      </div>
      <div class="info-section">
        <div class="section-header-inline">
          <h4>记录列表</h4>
          <div class="dialog-summary-text">沉淀会议纪要、沟通结论和通知留痕</div>
        </div>
        <div v-if="filteredCommunicationRecordList.length" class="simple-list">
          <div v-for="item in filteredCommunicationRecordList" :key="`communication-record-${item.id}`" class="simple-list-item">
            <strong>{{ item.title || `记录${item.id}` }}</strong>
            <span>类型: {{ formatCommunicationRecordType(item.recordType) }} | 关联会议: {{ item.meetingTitle || '未关联' }}</span>
            <span>记录人: {{ item.recorderName || '未指定' }} | 时间: {{ formatDateTimeText(item.updatedAt || item.createdAt) }}</span>
            <span>{{ item.content || '暂无内容' }}</span>
            <div v-if="canEditProjectContent" class="baseline-actions">
              <el-button size="small" @click="editCommunicationRecord(item)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="removeCommunicationRecord(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else :description="communicationRecordFilterMeetingId ? '当前会议暂无沟通记录' : '暂无沟通记录'" />
      </div>
    </el-dialog>

    <el-dialog v-model="procurementDialogVisible" title="采购与合同管理" width="1080px">
      <el-tabs v-model="procurementTab">
        <el-tab-pane label="供应商" name="suppliers">
          <div class="info-section">
            <div class="section-header-inline">
              <h4>{{ editingSupplierId ? '编辑供应商' : '新增供应商' }}</h4>
              <div class="dialog-summary-text">当前共 {{ supplierList.length }} 家供应商</div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item label="供应商名称">
                <el-input v-model="supplierForm.supplierName" placeholder="例如 XX 科技有限公司" />
              </el-form-item>
              <el-form-item label="联系人">
                <el-input v-model="supplierForm.contactName" placeholder="例如 张三" />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="supplierForm.contactPhone" placeholder="例如 13800000000" />
              </el-form-item>
              <el-form-item label="联系邮箱">
                <el-input v-model="supplierForm.contactEmail" placeholder="例如 vendor@example.com" />
              </el-form-item>
              <el-form-item label="地址" class="scope-form-span">
                <el-input v-model="supplierForm.address" placeholder="记录办公地址或交付地址" />
              </el-form-item>
              <el-form-item label="备注" class="scope-form-span">
                <el-input v-model="supplierForm.remark" type="textarea" :rows="2" placeholder="可记录资质、合作评价、发票信息或补充说明" />
              </el-form-item>
            </el-form>
            <div class="dialog-actions-inline">
              <el-button @click="resetSupplierForm()">重置表单</el-button>
              <el-button v-if="canEditProjectContent" type="primary" :loading="procurementLoading" @click="saveSupplier">
                {{ editingSupplierId ? '保存修改' : '新增供应商' }}
              </el-button>
            </div>
          </div>
          <div class="info-section">
            <div class="section-header-inline">
              <h4>供应商列表</h4>
              <div class="dialog-summary-text">采购项和合同会复用这里的供应商主数据</div>
            </div>
            <div v-if="supplierList.length" class="simple-list">
              <div v-for="item in supplierList" :key="`supplier-${item.id}`" class="simple-list-item">
                <strong>{{ item.supplierName || `供应商${item.id}` }}</strong>
                <span>联系人: {{ item.contactName || '未填写' }} | 电话: {{ item.contactPhone || '-' }} | 邮箱: {{ item.contactEmail || '-' }}</span>
                <span>地址: {{ item.address || '未填写' }}</span>
                <span>备注: {{ item.remark || '暂无备注' }}</span>
                <div v-if="canEditProjectContent" class="baseline-actions">
                  <el-button size="small" @click="editSupplier(item)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeSupplier(item)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无供应商" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="采购项" name="procurements">
          <div class="info-section">
            <div class="section-header-inline">
              <h4>{{ editingProcurementId ? '编辑采购项' : '新增采购项' }}</h4>
              <div class="dialog-summary-text">可关联成本计划，形成预算与实际采购映射</div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item label="采购项名称">
                <el-input v-model="procurementForm.itemName" placeholder="例如 云服务器、第三方短信包、测试设备" />
              </el-form-item>
              <el-form-item label="供应商">
                <el-select v-model="procurementForm.supplierId" clearable filterable placeholder="可选，选择供应商">
                  <el-option v-for="option in supplierOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="关联预算">
                <el-select v-model="procurementForm.costPlanId" clearable filterable placeholder="可选，关联一个成本计划">
                  <el-option v-for="option in procurementCostPlanOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="procurementForm.status" style="width: 100%">
                  <el-option v-for="option in procurementStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="数量">
                <el-input v-model="procurementForm.quantity" type="number" min="1" placeholder="1" />
              </el-form-item>
              <el-form-item label="单价">
                <el-input v-model="procurementForm.unitPrice" type="number" min="0" placeholder="0.00" />
              </el-form-item>
              <el-form-item label="预计总额">
                <el-input :model-value="formatMoney(calculateProcurementTotalAmount(procurementForm.quantity, procurementForm.unitPrice))" disabled />
              </el-form-item>
              <el-form-item label="预计交付">
                <el-date-picker v-model="procurementForm.expectedDeliveryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
              <el-form-item label="实际交付">
                <el-date-picker v-model="procurementForm.actualDeliveryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
            </el-form>
            <div class="dialog-actions-inline">
              <el-button @click="resetProcurementForm()">重置表单</el-button>
              <el-button v-if="canEditProjectContent" type="primary" :loading="procurementLoading" @click="saveProcurement">
                {{ editingProcurementId ? '保存修改' : '新增采购项' }}
              </el-button>
            </div>
          </div>
          <div v-if="canEditProjectContent" class="info-section">
            <div class="section-header-inline">
              <h4>采购执行状态更新</h4>
              <div class="dialog-summary-text">对应接口文档中的采购执行状态 PATCH 更新</div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item label="采购项">
                <el-select v-model="procurementStatusForm.procurementId" clearable filterable placeholder="选择采购项">
                  <el-option v-for="option in procurementOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="目标状态">
                <el-select v-model="procurementStatusForm.status" style="width: 100%">
                  <el-option v-for="option in procurementStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="实际交付">
                <el-date-picker v-model="procurementStatusForm.actualDeliveryDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
            </el-form>
            <div class="dialog-actions-inline">
              <el-button @click="resetProcurementStatusForm()">重置状态</el-button>
              <el-button type="primary" :loading="procurementLoading" @click="quickUpdateProcurementStatus">更新状态</el-button>
            </div>
          </div>
          <div class="info-section">
            <div class="section-header-inline">
              <h4>采购项列表</h4>
              <div class="dialog-summary-text">跟踪下单、交付、验收的执行状态</div>
            </div>
            <div v-if="procurementList.length" class="simple-list">
              <div v-for="item in procurementList" :key="`procurement-${item.id}`" class="simple-list-item">
                <strong>{{ item.itemName || `采购项${item.id}` }}</strong>
                <span>供应商: {{ item.supplierName || '未指定' }} | 预算项: {{ item.costPlanName || '未关联' }} | 状态: {{ formatProcurementStatus(item.status) }}</span>
                <span>数量: {{ item.quantity || 0 }} | 单价: {{ formatMoney(item.unitPrice) }} | 总额: {{ formatMoney(item.totalAmount) }}</span>
                <span>预计交付: {{ formatDateText(item.expectedDeliveryDate) }} | 实际交付: {{ formatDateText(item.actualDeliveryDate) }}</span>
                <div v-if="canEditProjectContent" class="baseline-actions">
                  <el-button size="small" @click="editProcurement(item)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeProcurement(item)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无采购项" />
          </div>
        </el-tab-pane>

        <el-tab-pane label="合同" name="contracts">
          <div class="info-section">
            <div class="section-header-inline">
              <h4>{{ editingContractId ? '编辑合同' : '新增合同' }}</h4>
              <div class="dialog-summary-text">按供应商维度维护合同主数据和履约状态</div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item label="合同编号">
                <el-input v-model="contractForm.contractNo" placeholder="例如 HT-2026-001" />
              </el-form-item>
              <el-form-item label="合同名称">
                <el-input v-model="contractForm.contractName" placeholder="例如 云服务采购合同" />
              </el-form-item>
              <el-form-item label="供应商">
                <el-select v-model="contractForm.supplierId" clearable filterable placeholder="可选，选择供应商">
                  <el-option v-for="option in supplierOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="合同类型">
                <el-select v-model="contractForm.contractType" style="width: 100%">
                  <el-option v-for="option in contractTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="签订日期">
                <el-date-picker v-model="contractForm.signDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
              </el-form-item>
              <el-form-item label="合同总额">
                <el-input v-model="contractForm.totalAmount" type="number" min="0" placeholder="0.00" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="contractForm.status" style="width: 100%">
                  <el-option v-for="option in contractStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="交付物" class="scope-form-span">
                <el-input v-model="contractForm.deliverables" type="textarea" :rows="2" placeholder="记录合同交付清单、里程碑或验收对象" />
              </el-form-item>
              <el-form-item label="付款条款" class="scope-form-span">
                <el-input v-model="contractForm.paymentTerms" type="textarea" :rows="2" placeholder="记录付款节点、比例、发票要求或结算方式" />
              </el-form-item>
            </el-form>
            <div class="dialog-actions-inline">
              <el-button @click="resetContractForm()">重置表单</el-button>
              <el-button v-if="canEditProjectContent" type="primary" :loading="procurementLoading" @click="saveContract">
                {{ editingContractId ? '保存修改' : '新增合同' }}
              </el-button>
            </div>
          </div>
          <div class="info-section">
            <div class="section-header-inline">
              <h4>合同列表</h4>
              <div class="dialog-summary-text">用于跟踪签约、履约和收尾的合同状态</div>
            </div>
            <div v-if="contractList.length" class="simple-list">
              <div v-for="item in contractList" :key="`contract-${item.id}`" class="simple-list-item">
                <strong>{{ item.contractNo || '-' }} | {{ item.contractName || `合同${item.id}` }}</strong>
                <span>供应商: {{ item.supplierName || '未指定' }} | 类型: {{ formatContractType(item.contractType) }} | 状态: {{ formatContractStatus(item.status) }}</span>
                <span>签订日期: {{ formatDateText(item.signDate) }} | 总额: {{ formatMoney(item.totalAmount) }}</span>
                <span>交付物: {{ item.deliverables || '暂无交付物说明' }}</span>
                <span>付款条款: {{ item.paymentTerms || '暂无付款条款' }}</span>
                <div v-if="canEditProjectContent" class="baseline-actions">
                  <el-button size="small" @click="editContract(item)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeContract(item)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无合同" />
          </div>
        </el-tab-pane>
      </el-tabs>
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
          <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="saveScopeBaseline">新增基线</el-button>
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
              <el-button v-if="canEditProjectContent" size="small" type="primary" plain @click="restoreScopeBaseline(item)">
                回退到此基线
              </el-button>
              <el-button v-if="canEditProjectContent" size="small" type="danger" plain @click="removeScopeBaseline(item)">
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
          <el-date-picker v-model="projectForm.endDate" :disabled="!canManageProject" type="date" value-format="YYYY-MM-DD" :disabled-date="disabledEndDate" @change="normalizeProjectDates" />
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
          <div class="dialog-summary-text">新加入成员默认只读；创建者可切换为可编辑成员，任务负责人仅可从创建者和可编辑成员中选择</div>
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
          <el-form-item label="身份">
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
          <div class="dialog-summary-text">只读成员可查看项目内容，可编辑成员可参与修改，被移除成员不会再出现在负责人列表中</div>
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
            <span>身份: {{ formatRole(item.projectRole) }}</span>
            <span>状态: {{ formatMemberStatus(item.memberStatus) }}</span>
            <span>加入时间: {{ item.joinedAt ? String(item.joinedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <div class="baseline-actions">
              <el-button v-if="canManageProject && !item.isProjectOwner" size="small" @click="resetProjectMemberForm(item)">编辑</el-button>
              <el-button v-if="canManageProject && !item.isProjectOwner" size="small" type="danger" plain @click="removeProjectMemberAction(item)">移除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无项目成员" />
      </div>
    </el-dialog>

    <el-dialog v-model="charterDialogVisible" title="项目章程" width="760px">
      <el-form label-width="96px">
        <el-form-item label="项目目标"><el-input v-model="charterForm.objective" :disabled="!canEditProjectContent" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="范围摘要"><el-input v-model="charterForm.scopeSummary" :disabled="!canEditProjectContent" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="发起人"><el-input v-model="charterForm.sponsor" :disabled="!canEditProjectContent" /></el-form-item>
        <el-form-item label="审批人"><el-input v-model="charterForm.approver" :disabled="!canEditProjectContent" /></el-form-item>
        <el-form-item label="干系人"><el-input v-model="charterForm.stakeholders" :disabled="!canEditProjectContent" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="成功标准"><el-input v-model="charterForm.successCriteria" :disabled="!canEditProjectContent" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="charterDialogVisible = false">{{ canEditProjectContent ? '取消' : '关闭' }}</el-button>
        <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="saveCharter">保存</el-button>
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
          <div v-for="item in calendarEvents" :key="`calendar-${item.eventType}-${item.bizId || item.id}`" class="simple-list-item">
            <strong>{{ item.title }}</strong>
            <span>{{ formatCalendarEventType(item.eventType) }} | {{ formatDateTimeText(item.startTime) }} - {{ formatDateTimeText(item.endTime || item.startTime) }}</span>
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

    <ProjectPendingReviewDialog
      :visible="pendingReviewDialogVisible"
      :pending-review-task-count="pendingReviewTaskCount"
      :pending-review-loading="pendingReviewLoading"
      :pending-review-tasks="pendingReviewTasks"
      :pending-review-acting-task-id="pendingReviewActingTaskId"
      :formatters="pendingReviewDialogFormatters"
      :helpers="pendingReviewDialogHelpers"
      :actions="pendingReviewDialogActions"
      @update:visible="pendingReviewDialogVisible = $event"
    />

    <ProjectTaskInfoDialog
      :visible="taskInfoDialogVisible"
      :selected-task-row="selectedTaskRow"
      :task-detail="taskDetail"
      :task-detail-loading="taskDetailLoading"
      :task-detail-section="taskDetailSection"
      :visible-task-detail-sections="visibleTaskDetailSections"
      :task-basic-form="taskBasicForm"
      :task-progress-form="taskProgressForm"
      :task-dependency-form="taskDependencyForm"
      :task-comment-form="taskCommentForm"
      :task-comment-threads="taskCommentThreads"
      :task-related-risks="taskRelatedRisks"
      :task-assignee-options="taskAssigneeOptions"
      :milestone-options="milestoneOptions"
      :dependency-task-options="dependencyTaskOptions"
      :task-constraint-type-options="taskConstraintTypeOptions"
      :task-progress-display-status="taskProgressDisplayStatus"
      :task-progress-workflow-hint="taskProgressWorkflowHint"
      :selected-task-assignee-placeholder="selectedTaskAssigneePlaceholder"
      :permissions="taskInfoDialogPermissions"
      :helpers="taskInfoDialogHelpers"
      :formatters="taskInfoDialogFormatters"
      :actions="taskInfoDialogActions"
      @update:visible="taskInfoDialogVisible = $event"
      @update:task-detail-section="taskDetailSection = $event"
    />

    <ProjectMilestoneDialog
      :visible="milestoneDialogVisible"
      :editing-milestone-task-local-id="editingMilestoneTaskLocalId"
      :editing-milestone-id="editingMilestoneId"
      :can-edit-task-plan="canEditTaskPlan"
      :milestone-form="milestoneForm"
      :milestone-loading="milestoneLoading"
      :visible-milestone-list="visibleMilestoneList"
      :formatters="milestoneDialogFormatters"
      :helpers="milestoneDialogHelpers"
      :actions="milestoneDialogActions"
      @update:visible="milestoneDialogVisible = $event"
    />

    <ProjectRiskDialog
      :visible="riskDialogVisible"
      :can-edit-project-content="canEditProjectContent"
      :editing-risk-id="editingRiskId"
      :risk-form="riskForm"
      :risk-status-form="riskStatusForm"
      :risk-loading="riskLoading"
      :risk-task-options="riskTaskOptions"
      :risk-status-options="riskStatusOptions"
      :risk-list="riskList"
      :formatters="riskDialogFormatters"
      :actions="riskDialogActions"
      @update:visible="riskDialogVisible = $event"
    />

    <ProjectRiskMatrixDialog
      :visible="riskMatrixDialogVisible"
      :risk-matrix="riskMatrix"
      :helpers="riskMatrixDialogHelpers"
      @update:visible="riskMatrixDialogVisible = $event"
    />

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
              <el-option v-for="option in importModuleOptions" :key="option.value" :label="option.label" :value="option.value" />
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
          <el-select v-model="auditForm.module" placeholder="选择模块" style="width: 220px">
            <el-option
              v-for="option in auditModuleOptions"
              :key="option.value || 'all'"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
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
              <el-button v-if="canEditTimesheetRecord(item)" size="small" @click="populateTimesheetForm(item)">编辑</el-button>
              <el-button v-if="canEditTimesheetRecord(item)" size="small" type="danger" plain @click="removeTimesheet(item)">删除</el-button>
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
          <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="saveArchive">新增归档项</el-button>
        </div>
        <div v-if="closureArchives.length" class="simple-list">
          <div v-for="item in closureArchives" :key="`archive-${item.id}`" class="simple-list-item">
            <strong>{{ item.itemName || `归档项${item.id}` }}</strong>
            <span>类型: {{ item.archiveType || '-' }} | 状态: {{ item.status || '-' }}</span>
            <span>仓库: {{ item.repositoryUrl || '未填写' }}</span>
            <span>归档时间: {{ item.archivedAt ? String(item.archivedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <div class="baseline-actions">
              <el-button v-if="item.attachmentId" size="small" @click="openAttachmentDownload(item.attachmentId)">下载附件</el-button>
              <el-button v-if="canEditProjectContent" size="small" type="danger" plain @click="removeArchive(item)">删除</el-button>
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
          <el-button v-if="canManageProject" type="primary" :loading="loading" @click="generateSummaryReportAction">生成总结报告</el-button>
        </div>
        <div v-if="reportList.length" class="simple-list">
          <div v-for="item in reportList" :key="`closure-report-${item.id}`" class="simple-list-item">
            <strong>{{ normalizeReportTitle(item.title) || `报告${item.id}` }}</strong>
            <span>类型: {{ formatReportType(item.reportType, item.title) }}</span>
            <span>周期: {{ item.startDate || '-' }} ~ {{ item.endDate || '-' }}</span>
            <span>生成时间: {{ formatDateTimeText(item.generatedAt) }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="openReportPreview(item)">查看详情</el-button>
              <el-button v-if="canManageProject" size="small" type="danger" plain @click="removeReport(item)">删除</el-button>
            </div>
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
          <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="saveLesson">{{ editingLessonId ? '保存修改' : '新增经验' }}</el-button>
        </div>
        <div v-if="closureLessons.length" class="simple-list">
          <div v-for="item in closureLessons" :key="`lesson-${item.id}`" class="simple-list-item">
            <strong>{{ item.title || `经验${item.id}` }}</strong>
            <span>分类: {{ item.category || '-' }} | 作者: {{ item.authorName || item.authorId || '-' }}</span>
            <span>创建时间: {{ item.createdAt ? String(item.createdAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
            <span>{{ item.content || '暂无内容' }}</span>
            <div class="baseline-actions">
              <el-button v-if="canEditProjectContent" size="small" @click="populateLessonForm(item)">编辑</el-button>
              <el-button v-if="canEditProjectContent" size="small" type="danger" plain @click="removeLesson(item)">删除</el-button>
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
          <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="saveAcceptanceItemAction">{{ editingAcceptanceId ? '保存修改' : '新增验收事项' }}</el-button>
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
              <el-button v-if="canEditProjectContent" size="small" @click="populateAcceptanceForm(item)">编辑</el-button>
              <el-button v-if="item.attachmentId" size="small" @click="openAttachmentDownload(item.attachmentId)">证明材料</el-button>
              <el-button v-if="canEditProjectContent" size="small" type="danger" plain @click="removeAcceptanceItemAction(item)">删除</el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无验收事项" />
      </div>
    </el-dialog>

    <el-dialog v-model="configurationDialogVisible" :title="configurationDialogTitle" width="1080px">
      <el-tabs v-model="configurationTab">
        <el-tab-pane v-if="!isConfigBaselineDialogMode" label="配置项" name="items">
          <div class="info-section">
            <div class="section-header-inline">
              <h4>{{ editingConfigItemId ? '编辑配置项' : '新增配置项' }}</h4>
              <div class="dialog-summary-text">记录文档、代码、环境、数据库等配置对象的当前版本与状态</div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item label="名称">
                <el-input v-model="configItemForm.itemName" placeholder="例如 需求规格说明书、后端服务、生产环境变量、初始化脚本" />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="configItemForm.itemType" style="width: 100%">
                  <el-option v-for="option in configItemTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="版本号">
                <el-input v-model="configItemForm.versionNo" placeholder="例如 v1.0.3、2026.04、main-20260426" />
              </el-form-item>
              <el-form-item label="状态">
                <el-select v-model="configItemForm.status" style="width: 100%">
                  <el-option v-for="option in configItemStatusOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item label="地址" class="scope-form-span">
                <el-input v-model="configItemForm.repositoryUrl" placeholder="例如 Git 仓库地址、文档链接、部署地址、制品地址" />
              </el-form-item>
              <el-form-item label="备注" class="scope-form-span">
                <el-input v-model="configItemForm.remark" type="textarea" :rows="3" placeholder="记录归属模块、负责人、变更说明、发布窗口或补充说明" />
              </el-form-item>
            </el-form>
            <div class="dialog-actions-inline">
              <el-button @click="resetConfigItemForm()">重置表单</el-button>
              <el-button v-if="canEditProjectContent" type="primary" :loading="configurationLoading" @click="saveConfigItem">
                {{ editingConfigItemId ? '保存修改' : '新增配置项' }}
              </el-button>
            </div>
          </div>
          <div class="info-section">
            <div class="section-header-inline">
              <h4>配置项列表</h4>
              <div class="dialog-summary-text">用于回看配置对象的版本和是否已纳入基线</div>
            </div>
            <div v-if="configItemList.length" class="simple-list">
              <div v-for="item in configItemList" :key="`config-item-${item.id}`" class="simple-list-item">
                <strong>{{ item.itemName || `配置项${item.id}` }}</strong>
                <span>类型: {{ formatConfigItemType(item.itemType) }} | 版本: {{ item.versionNo || '未设置' }} | 状态: {{ formatConfigItemStatus(item.status) }}</span>
                <span>地址: {{ item.repositoryUrl || '未设置' }}</span>
                <span>备注: {{ item.remark || '暂无备注' }}</span>
                <span>更新人: {{ item.updatedByName || item.createdByName || '-' }} | 更新时间: {{ formatDateTimeText(item.updatedAt || item.createdAt) }}</span>
                <div v-if="canEditProjectContent" class="baseline-actions">
                  <el-button size="small" @click="editConfigItem(item)">编辑</el-button>
                  <el-button size="small" type="danger" plain @click="removeConfigItem(item)">删除</el-button>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无配置项" />
          </div>
        </el-tab-pane>

        <el-tab-pane :label="isConfigBaselineDialogMode ? '配置基线' : '统一基线'" name="baselines">
          <div class="info-section">
            <div class="section-header-inline">
              <h4>{{ isConfigBaselineDialogMode ? '创建配置基线' : '创建基线' }}</h4>
              <div class="dialog-summary-text">
                {{ isConfigBaselineDialogMode ? '冻结当前配置项快照，后续可直接预览并恢复当前配置列表' : '统一管理范围、进度、成本、配置四类基线快照' }}
              </div>
            </div>
            <el-form label-width="88px" class="scope-form-grid">
              <el-form-item v-if="!isConfigBaselineDialogMode" label="基线类型">
                <el-select v-model="baselineForm.baselineType" style="width: 100%">
                  <el-option v-for="option in baselineTypeOptions" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </el-form-item>
              <el-form-item v-else label="基线类型">
                <el-input value="配置基线" readonly />
              </el-form-item>
              <el-form-item label="基线名称">
                <el-input v-model="baselineForm.baselineName" placeholder="例如 V1.0 配置基线、迭代二进度基线、需求冻结基线" />
              </el-form-item>
              <el-form-item label="说明" class="scope-form-span">
                <el-input v-model="baselineForm.description" type="textarea" :rows="3" placeholder="可记录创建背景、冻结范围、审批结论或版本说明；范围基线会保存该说明" />
              </el-form-item>
            </el-form>
            <div class="dialog-actions-inline">
              <el-button @click="resetBaselineForm()">重置表单</el-button>
              <el-button v-if="canEditProjectContent" type="primary" :loading="configurationLoading" @click="saveProjectBaseline">创建基线</el-button>
            </div>
          </div>
          <div class="info-section">
            <div class="section-header-inline">
              <h4>{{ isConfigBaselineDialogMode ? '配置基线列表' : '基线列表' }}</h4>
              <div class="dialog-summary-text">
                {{ isConfigBaselineDialogMode ? '查看配置项快照并恢复当前配置列表' : '统一展示当前项目各类基线记录，便于变更后追溯' }}
              </div>
            </div>
            <div v-if="displayedBaselineRecords.length" class="simple-list">
              <div v-for="item in displayedBaselineRecords" :key="`project-baseline-${item.baselineType}-${item.id}`" class="simple-list-item">
                <strong>{{ item.baselineName || `基线${item.id}` }}</strong>
                <span>类型: {{ formatBaselineType(item.baselineType) }} | 版本: {{ item.versionNo || '-' }} | 状态: {{ formatBaselineStatus(item.status) }}</span>
                <span>发布时间: {{ formatDateTimeText(item.publishedAt || item.createdAt) }} | 发布人: {{ item.publishedByName || item.publishedBy || '-' }}</span>
                <span>说明: {{ item.description || '无附加说明' }}</span>
                <span>快照: {{ item.snapshotJson ? '已生成' : '未生成' }}</span>
                <div v-if="canEditProjectContent" class="baseline-actions">
                  <el-button
                    v-if="isConfigBaselineItem(item)"
                    size="small"
                    @click="toggleConfigBaselinePreview(item.id)"
                  >
                    {{ isConfigBaselineActive(item.id) ? '收起快照' : '查看快照' }}
                  </el-button>
                  <el-button
                    v-if="isConfigBaselineItem(item)"
                    size="small"
                    type="primary"
                    plain
                    @click="restoreConfigBaseline(item)"
                  >
                    恢复到此基线
                  </el-button>
                  <el-button size="small" type="danger" plain @click="removeProjectBaseline(item)">删除</el-button>
                </div>
                <div v-if="isConfigBaselineItem(item) && isConfigBaselineActive(item.id)" class="baseline-snapshot-card">
                  <div class="baseline-snapshot-meta">
                    <span>快照配置项数: {{ parseConfigBaselineSnapshot(item).length }}</span>
                    <span>恢复操作会覆盖当前配置项列表</span>
                  </div>
                  <div v-if="parseConfigBaselineSnapshot(item).length" class="simple-list">
                    <div
                      v-for="(snapshotItem, index) in parseConfigBaselineSnapshot(item)"
                      :key="`config-baseline-preview-${item.id}-${snapshotItem.id || index}`"
                      class="simple-list-item"
                    >
                      <strong>{{ snapshotItem.itemName || `配置项${index + 1}` }}</strong>
                      <span>类型: {{ formatConfigItemType(snapshotItem.itemType) }} | 版本: {{ snapshotItem.versionNo || '未设置' }} | 状态: {{ formatConfigItemStatus(snapshotItem.status) }}</span>
                      <span>地址: {{ snapshotItem.repositoryUrl || '未设置' }}</span>
                      <span>备注: {{ snapshotItem.remark || '暂无备注' }}</span>
                      <span>更新人: {{ snapshotItem.updatedByName || snapshotItem.createdByName || '-' }} | 更新时间: {{ formatDateTimeText(snapshotItem.updatedAt || snapshotItem.createdAt) }}</span>
                    </div>
                  </div>
                  <el-empty v-else description="该配置基线下没有配置项快照" />
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无基线记录" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <el-dialog v-model="changeRequestDialogVisible" :title="changeRequestDialogTitle" width="980px">
      <div v-if="canSubmitChangeRequest" class="info-section">
        <div class="section-header-inline">
          <h4>提交变更申请</h4>
          <div class="dialog-summary-text">当前界面只处理申请，不展示审批操作。</div>
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

      <div v-else-if="canApproveChangeRequest" class="info-section">
        <div class="section-header-inline">
          <h4>审批操作</h4>
          <div class="dialog-summary-text">当前界面只处理审批，不展示申请表单。</div>
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

      <div v-else class="info-section">
        <div class="section-header-inline">
          <h4>变更记录</h4>
          <div class="dialog-summary-text">当前角色只能查看变更记录，不能提交或审批。</div>
        </div>
      </div>

      <div class="info-section">
        <div class="section-header-inline">
          <h4>变更申请列表</h4>
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
        <div v-if="selectedChangeRequest.status === 'APPROVED' && canEditProjectContent" class="dialog-actions-inline">
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
            <strong>{{ normalizeReportTitle(item.title) }}</strong>
            <span>{{ formatReportType(item.reportType, item.title) }} | {{ formatReportStatus(item.status) }} | {{ formatDateTimeText(item.generatedAt) }}</span>
            <span>统计周期: {{ formatReportPeriodText(item, parseReportContent(item.contentJson)) }}</span>
            <div class="baseline-actions">
              <el-button size="small" @click="openReportPreview(item)">查看详情</el-button>
              <el-button v-if="canManageProject" size="small" type="danger" plain @click="removeReport(item)">删除</el-button>
            </div>
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

    <el-dialog
      v-model="reportPreviewDialogVisible"
      :title="selectedReportPreview ? normalizeReportTitle(selectedReportPreview.title) : '报表详情'"
      width="980px"
      append-to-body
      class="report-preview-dialog"
    >
      <div v-if="selectedReportPreview" class="report-preview">
        <div class="report-preview-hero">
          <div class="report-preview-hero-main">
            <h3>{{ normalizeReportTitle(selectedReportPreview.title) || `报告${selectedReportPreview.id}` }}</h3>
            <p>
              {{ formatReportType(selectedReportPreview.reportType, selectedReportPreview.title) }}
              | {{ formatReportStatus(selectedReportPreview.status) }}
              | {{ formatDateTimeText(selectedReportPreview.generatedAt) }}
            </p>
          </div>
          <div class="report-preview-hero-side">
            <span>统计周期</span>
            <strong>{{ formatReportPeriodText(selectedReportPreview, selectedReportPreviewContent) }}</strong>
            <small>生成人: {{ selectedReportPreview.generatedByName || selectedReportPreviewContent?.generatedBy?.name || '-' }}</small>
            <el-button v-if="canManageProject" size="small" type="danger" plain @click="removeReport(selectedReportPreview)">删除报表</el-button>
          </div>
        </div>

        <template v-if="isRichReportContent(selectedReportPreviewContent)">
          <div class="report-preview-metrics">
            <div class="report-preview-metric-card">
              <span>任务完成率</span>
              <strong>{{ formatPercentValue(selectedReportPreviewContent?.dashboard?.taskCompletionRate ?? selectedReportPreviewContent?.overview?.taskCompletionRate) }}</strong>
            </div>
            <div class="report-preview-metric-card">
              <span>累计工时</span>
              <strong>{{ formatHoursText(selectedReportPreviewContent?.dashboard?.actualHours ?? selectedReportPreviewContent?.overview?.totalHours) }}</strong>
            </div>
            <div class="report-preview-metric-card">
              <span>逾期任务</span>
              <strong>{{ selectedReportPreviewContent?.dashboard?.overdueTaskCount ?? 0 }}</strong>
            </div>
            <div class="report-preview-metric-card">
              <span>成本偏差</span>
              <strong>{{ formatMoney(selectedReportPreviewContent?.dashboard?.costVariance) }}</strong>
            </div>
            <div class="report-preview-metric-card">
              <span>未关闭风险</span>
              <strong>{{ selectedReportPreviewContent?.dashboard?.openRiskCount ?? selectedReportPreviewContent?.overview?.openRiskCount ?? 0 }}</strong>
            </div>
            <div class="report-preview-metric-card">
              <span>待处理变更</span>
              <strong>{{ selectedReportPreviewContent?.dashboard?.pendingChangeCount ?? selectedReportPreviewContent?.overview?.pendingChangeCount ?? 0 }}</strong>
            </div>
          </div>

          <div v-if="availableReportPreviewTabs.length > 1" class="report-preview-tab-bar">
            <button
              v-for="tab in availableReportPreviewTabs"
              :key="`report-preview-tab-${tab.key}`"
              type="button"
              class="report-preview-tab"
              :class="{ active: activeReportPreviewTab === tab.key }"
              @click="selectReportPreviewTab(tab.key)"
            >
              {{ tab.label }}
            </button>
          </div>

          <template v-if="activeReportPreviewTab === 'overview'">
            <div class="report-preview-section">
            <div class="section-header-inline">
              <h4>项目概况</h4>
              <div class="dialog-summary-text">{{ selectedReportPreviewContent?.project?.projectCode || '-' }}</div>
            </div>
            <div class="report-preview-meta-grid">
              <div class="report-preview-meta-item">
                <span>项目名称</span>
                <strong>{{ selectedReportPreviewContent?.project?.name || '-' }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>当前状态</span>
                <strong>{{ formatProjectStatus(selectedReportPreviewContent?.project?.status) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>负责人</span>
                <strong>{{ selectedReportPreviewContent?.project?.ownerName || '-' }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>计划区间</span>
                <strong>{{ formatDateText(selectedReportPreviewContent?.project?.startDate) }} ~ {{ formatDateText(selectedReportPreviewContent?.project?.endDate) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>实际区间</span>
                <strong>{{ formatDateText(selectedReportPreviewContent?.project?.actualStartDate) }} ~ {{ formatDateText(selectedReportPreviewContent?.project?.actualEndDate) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>项目成员</span>
                <strong>{{ selectedReportPreviewContent?.overview?.memberCount ?? 0 }} 人</strong>
              </div>
            </div>
            <div v-if="selectedReportPreviewContent?.project?.description" class="report-preview-note">
              {{ selectedReportPreviewContent.project.description }}
            </div>
            </div>

            <div class="report-preview-section">
            <div class="section-header-inline">
              <h4>执行数据</h4>
              <div class="dialog-summary-text">
                已完成 {{ selectedReportPreviewContent?.overview?.taskCompleted ?? 0 }} / {{ selectedReportPreviewContent?.overview?.taskTotal ?? 0 }} 项任务
              </div>
            </div>
            <div class="report-preview-meta-grid">
              <div class="report-preview-meta-item">
                <span>计划成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.overview?.plannedCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>实际成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.overview?.actualCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>人工成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.overview?.totalLaborCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>未开始任务</span>
                <strong>{{ selectedReportPreviewContent?.overview?.todoCount ?? 0 }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>进行中任务</span>
                <strong>{{ selectedReportPreviewContent?.overview?.inProgressCount ?? 0 }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>阻塞任务</span>
                <strong>{{ selectedReportPreviewContent?.overview?.blockedCount ?? 0 }}</strong>
              </div>
            </div>
            </div>

            <div v-if="selectedReportPreviewContent?.dashboard" class="report-preview-section">
            <div class="section-header-inline">
              <h4>仪表板概览</h4>
              <div class="dialog-summary-text">项目整体进度、工时、成本与风险总览</div>
            </div>
            <div class="report-preview-meta-grid">
              <div class="report-preview-meta-item">
                <span>关键任务</span>
                <strong>{{ selectedReportPreviewContent?.dashboard?.criticalTaskCount ?? 0 }} 项</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>里程碑总数</span>
                <strong>{{ selectedReportPreviewContent?.dashboard?.milestoneTotal ?? 0 }} 个</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>已完成里程碑</span>
                <strong>{{ selectedReportPreviewContent?.dashboard?.milestoneCompleted ?? 0 }} 个</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>剩余工时</span>
                <strong>{{ formatHoursText(selectedReportPreviewContent?.dashboard?.remainingHours) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>预算执行率</span>
                <strong>{{ formatPercentValue(selectedReportPreviewContent?.dashboard?.budgetExecutionRate) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>活跃资源</span>
                <strong>{{ selectedReportPreviewContent?.dashboard?.activeUserCount ?? 0 }} 人</strong>
              </div>
            </div>
            </div>
          </template>

          <template v-if="activeReportPreviewTab === 'task'">
            <div v-if="selectedReportPreviewContent?.taskSummary" class="report-preview-section">
            <div class="section-header-inline">
              <h4>任务摘要</h4>
              <div class="dialog-summary-text">按状态、依赖、关键路径汇总任务执行情况</div>
            </div>
            <div class="report-preview-meta-grid">
              <div class="report-preview-meta-item">
                <span>未开始</span>
                <strong>{{ selectedReportPreviewContent?.taskSummary?.todoCount ?? 0 }} 项</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>进行中</span>
                <strong>{{ selectedReportPreviewContent?.taskSummary?.inProgressCount ?? 0 }} 项</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>阻塞</span>
                <strong>{{ selectedReportPreviewContent?.taskSummary?.blockedCount ?? 0 }} 项</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>关键任务</span>
                <strong>{{ selectedReportPreviewContent?.taskSummary?.criticalTaskCount ?? 0 }} 项</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>依赖关系</span>
                <strong>{{ selectedReportPreviewContent?.taskSummary?.dependencyCount ?? 0 }} 条</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>任务完成率</span>
                <strong>{{ formatPercentValue(selectedReportPreviewContent?.taskSummary?.completionRate) }}</strong>
              </div>
            </div>
            </div>

          <div v-if="selectedReportPreviewContent?.upcomingTasks?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>即将开始与即将到期任务</h4>
              <div class="dialog-summary-text">聚焦未来 7 天需要关注的任务</div>
            </div>
            <div class="report-preview-list">
              <div
                v-for="item in selectedReportPreviewContent.upcomingTasks"
                :key="`report-preview-upcoming-task-${item.id}`"
                class="report-preview-list-item"
              >
                <strong>{{ item.taskCode || '-' }} | {{ item.name || '未命名任务' }}</strong>
                <span>提醒类型: {{ item.alertType || '-' }} | 负责人: {{ item.assigneeName || '未设置' }}</span>
                <span>计划时间: {{ formatDateText(item.plannedStartDate) }} ~ {{ formatDateText(item.plannedEndDate) }} | 预计工期: {{ formatDaysText(item.durationDays) }}</span>
                <span>前置任务: {{ formatListText(item.predecessorTasks) }}</span>
              </div>
            </div>
          </div>

          <div v-if="selectedReportPreviewContent?.upcomingMilestones?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>近期里程碑</h4>
              <div class="dialog-summary-text">按计划时间展示最近关注节点</div>
            </div>
            <div class="report-preview-list">
              <div
                v-for="item in selectedReportPreviewContent.upcomingMilestones"
                :key="`report-preview-milestone-${item.id}`"
                class="report-preview-list-item"
              >
                <strong>{{ item.name || '未命名里程碑' }}</strong>
                <span>状态: {{ formatMilestoneStatus(item.status) }} | 负责人: {{ item.ownerName || '未设置' }}</span>
                <span>计划时间: {{ formatDateTimeText(item.plannedDate) }}</span>
                <span v-if="item.description">{{ item.description }}</span>
              </div>
            </div>
          </div>

          <div v-if="selectedReportPreviewContent?.criticalTasks?.length" class="report-preview-section">
            <div class="section-header-inline report-preview-timeline-header">
              <h4>关键任务</h4>
              <div class="report-preview-timeline-header-meta">
                <div class="dialog-summary-text">关键路径上的重点任务，可左右滑动查看</div>
                <div class="report-preview-timeline-controls">
                  <button type="button" class="report-preview-timeline-control" aria-label="向左查看关键任务" @click="scrollReportCriticalTimeline(-1)">
                    ‹
                  </button>
                  <button type="button" class="report-preview-timeline-control" aria-label="向右查看关键任务" @click="scrollReportCriticalTimeline(1)">
                    ›
                  </button>
                </div>
              </div>
            </div>
            <div ref="reportCriticalTimelineRef" class="report-preview-task-timeline-scroll" tabindex="0" aria-label="关键任务时间线，可左右滚动">
              <div class="report-preview-task-timeline-inner">
                <div
                  v-for="item in selectedReportPreviewContent.criticalTasks"
                  :key="`report-preview-task-${item.id}`"
                  class="report-preview-task-node"
                  :class="getReportTaskTimelineClass(item.status)"
                >
                  <div class="report-preview-task-node-head">
                    <span class="report-preview-task-node-dot"></span>
                    <span class="report-preview-task-node-date">{{ formatDateText(item.plannedStartDate) }}</span>
                  </div>
                  <article class="report-preview-task-card">
                    <div class="report-preview-task-card-top">
                      <strong>{{ item.taskCode || '-' }}</strong>
                      <span class="report-preview-task-status">{{ formatTaskStatus(item.status) }}</span>
                    </div>
                    <h5>{{ item.name || '未命名任务' }}</h5>
                    <p>{{ item.remark || item.description || '暂无说明' }}</p>
                    <div class="report-preview-task-card-meta">
                      <span>负责人 {{ item.assigneeName || '未设置' }}</span>
                      <span>优先级 {{ formatPriorityText(item.priority) }}</span>
                      <span>进度 {{ formatPercentValue(item.progress) }}</span>
                      <span>{{ formatDateText(item.plannedStartDate) }} → {{ formatDateText(item.plannedEndDate) }}</span>
                    </div>
                  </article>
                </div>
              </div>
            </div>
          </div>

          <div v-if="selectedReportPreviewContent?.taskDetails?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>任务明细</h4>
              <div class="dialog-summary-text">共 {{ selectedReportPreviewContent.taskDetails.length }} 项任务，支持计划与实际对照</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>任务</th>
                    <th>负责人</th>
                    <th>状态</th>
                    <th>进度</th>
                    <th>计划时间</th>
                    <th>实际时间</th>
                    <th>工时</th>
                    <th>延期</th>
                    <th>前置任务</th>
                    <th>关键路径</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.taskDetails"
                    :key="`report-preview-task-detail-${item.id}`"
                  >
                    <td>
                      <strong>{{ item.taskCode || '-' }}</strong>
                      <div class="report-preview-table-sub">{{ item.name || '未命名任务' }}</div>
                    </td>
                    <td>{{ item.assigneeName || '未设置' }}</td>
                    <td>{{ formatTaskStatus(item.status) }}</td>
                    <td>{{ formatPercentValue(item.progress) }}</td>
                    <td>{{ formatDateText(item.plannedStartDate) }} ~ {{ formatDateText(item.plannedEndDate) }}</td>
                    <td>{{ formatDateText(item.actualStartDate) }} ~ {{ formatDateText(item.actualEndDate) }}</td>
                    <td>{{ formatHoursText(item.actualHours) }} / {{ formatHoursText(item.plannedHours) }}</td>
                    <td>{{ formatDaysText(item.delayDays) }}</td>
                    <td>{{ formatListText(item.predecessorTasks) }}</td>
                    <td>{{ formatYesNo(item.isCritical) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-if="selectedReportPreviewContent?.overdueTasks?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>逾期任务</h4>
              <div class="dialog-summary-text">只显示当前已经偏离计划完成时间的任务</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>任务</th>
                    <th>负责人</th>
                    <th>状态</th>
                    <th>计划完成</th>
                    <th>延期天数</th>
                    <th>进度</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.overdueTasks"
                    :key="`report-preview-overdue-task-${item.id}`"
                  >
                    <td>
                      <strong>{{ item.taskCode || '-' }}</strong>
                      <div class="report-preview-table-sub">{{ item.name || '未命名任务' }}</div>
                    </td>
                    <td>{{ item.assigneeName || '未设置' }}</td>
                    <td>{{ formatTaskStatus(item.status) }}</td>
                    <td>{{ formatDateText(item.plannedEndDate) }}</td>
                    <td>{{ formatDaysText(item.delayDays) }}</td>
                    <td>{{ formatPercentValue(item.progress) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-if="selectedReportPreviewContent?.milestoneReport?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>里程碑报表</h4>
              <div class="dialog-summary-text">完整展示所有里程碑节点的计划与实际状态</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>里程碑</th>
                    <th>负责人</th>
                    <th>计划时间</th>
                    <th>实际时间</th>
                    <th>状态</th>
                    <th>延期天数</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.milestoneReport"
                    :key="`report-preview-milestone-detail-${item.id}`"
                  >
                    <td>
                      <strong>{{ item.name || '未命名里程碑' }}</strong>
                      <div class="report-preview-table-sub">{{ item.description || '暂无说明' }}</div>
                    </td>
                    <td>{{ item.ownerName || '未设置' }}</td>
                    <td>{{ formatDateTimeText(item.plannedDate) }}</td>
                    <td>{{ formatDateTimeText(item.actualDate) }}</td>
                    <td>{{ formatMilestoneStatus(item.status) }}</td>
                    <td>{{ formatDaysText(item.delayDays) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          </template>

          <template v-if="activeReportPreviewTab === 'resource'">
            <div v-if="selectedReportPreviewContent?.resourceOverview?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>资源概况</h4>
              <div class="dialog-summary-text">按资源查看任务分配、工时利用率与过载情况</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>资源</th>
                    <th>分配任务</th>
                    <th>计划工时</th>
                    <th>实际工时</th>
                    <th>剩余工时</th>
                    <th>利用率</th>
                    <th>人工成本</th>
                    <th>过载天数</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.resourceOverview"
                    :key="`report-preview-resource-${item.userId}`"
                  >
                    <td>{{ item.userName || '未命名资源' }}</td>
                    <td>{{ item.assignedTaskCount ?? 0 }}</td>
                    <td>{{ formatHoursText(item.plannedHours) }}</td>
                    <td>{{ formatHoursText(item.actualHours) }}</td>
                    <td>{{ formatHoursText(item.remainingHours) }}</td>
                    <td>{{ formatPercentValue(item.utilizationRate) }}</td>
                    <td>{{ formatMoney(item.laborCost) }}</td>
                    <td>{{ item.overloadDays ?? 0 }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            </div>

          <div v-if="selectedReportPreviewContent?.resourceAssignments?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>资源分配</h4>
              <div class="dialog-summary-text">按资源-任务组合查看计划与实际投入</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>资源</th>
                    <th>任务</th>
                    <th>状态</th>
                    <th>进度</th>
                    <th>计划工时</th>
                    <th>实际工时</th>
                    <th>剩余工时</th>
                    <th>人工成本</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.resourceAssignments"
                    :key="`report-preview-resource-assignment-${item.userId}-${item.taskId}`"
                  >
                    <td>{{ item.userName || '未命名资源' }}</td>
                    <td>{{ item.taskName || '未命名任务' }}</td>
                    <td>{{ formatTaskStatus(item.status) }}</td>
                    <td>{{ formatPercentValue(item.progress) }}</td>
                    <td>{{ formatHoursText(item.plannedHours) }}</td>
                    <td>{{ formatHoursText(item.actualHours) }}</td>
                    <td>{{ formatHoursText(item.remainingHours) }}</td>
                    <td>{{ formatMoney(item.laborCost) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          </template>

          <template v-if="activeReportPreviewTab === 'cost'">
            <div v-if="selectedReportPreviewContent?.costOverview" class="report-preview-section">
            <div class="section-header-inline">
              <h4>成本概述</h4>
              <div class="dialog-summary-text">预算、计划、实际、基线与 CPI 汇总</div>
            </div>
            <div class="report-preview-meta-grid">
              <div class="report-preview-meta-item">
                <span>项目预算</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.costOverview?.budgetCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>计划成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.costOverview?.plannedCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>实际成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.costOverview?.actualCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>人工成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.costOverview?.laborCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>剩余成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.costOverview?.remainingCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>基线成本</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.costOverview?.baselineCost) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>成本偏差</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.costOverview?.costVariance) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>CPI</span>
                <strong>{{ formatDecimalText(selectedReportPreviewContent?.costOverview?.cpi, 4) }}</strong>
              </div>
            </div>
            </div>

          <div v-if="selectedReportPreviewContent?.costDetails?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>任务成本明细</h4>
              <div class="dialog-summary-text">按任务汇总计划成本、实际成本与偏差</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>任务</th>
                    <th>负责人</th>
                    <th>计划成本</th>
                    <th>人工成本</th>
                    <th>实际成本</th>
                    <th>剩余成本</th>
                    <th>成本偏差</th>
                    <th>进度</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.costDetails"
                    :key="`report-preview-cost-detail-${item.taskId}`"
                  >
                    <td>
                      <strong>{{ item.taskCode || '-' }}</strong>
                      <div class="report-preview-table-sub">{{ item.taskName || '未命名任务' }}</div>
                    </td>
                    <td>{{ item.assigneeName || '未设置' }}</td>
                    <td>{{ formatMoney(item.plannedCost) }}</td>
                    <td>{{ formatMoney(item.laborCost) }}</td>
                    <td>{{ formatMoney(item.actualCost) }}</td>
                    <td>{{ formatMoney(item.remainingCost) }}</td>
                    <td>{{ formatMoney(item.costVariance) }}</td>
                    <td>{{ formatPercentValue(item.progress) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <div v-if="selectedReportPreviewContent?.costCashFlow?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>现金流量</h4>
              <div class="dialog-summary-text">按月汇总人工与实际支出</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>期间</th>
                    <th>人工成本</th>
                    <th>实际成本</th>
                    <th>总支出</th>
                    <th>累计支出</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.costCashFlow"
                    :key="`report-preview-cash-flow-${item.period}`"
                  >
                    <td>{{ item.period || '-' }}</td>
                    <td>{{ formatMoney(item.laborCost) }}</td>
                    <td>{{ formatMoney(item.manualActualCost) }}</td>
                    <td>{{ formatMoney(item.actualCost) }}</td>
                    <td>{{ formatMoney(item.cumulativeActualCost) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          </template>

          <template v-if="activeReportPreviewTab === 'schedule'">
            <div v-if="selectedReportPreviewContent?.scheduleComparison?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>进度对比</h4>
              <div class="dialog-summary-text">计划开始/完成与实际执行情况对比</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>任务</th>
                    <th>状态</th>
                    <th>计划开始</th>
                    <th>计划完成</th>
                    <th>实际开始</th>
                    <th>实际完成</th>
                    <th>开始偏差</th>
                    <th>完成偏差</th>
                    <th>关键路径</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.scheduleComparison"
                    :key="`report-preview-schedule-${item.id}`"
                  >
                    <td>
                      <strong>{{ item.taskCode || '-' }}</strong>
                      <div class="report-preview-table-sub">{{ item.name || '未命名任务' }}</div>
                    </td>
                    <td>{{ item.varianceStatus || formatTaskStatus(item.status) }}</td>
                    <td>{{ formatDateText(item.plannedStartDate) }}</td>
                    <td>{{ formatDateText(item.plannedEndDate) }}</td>
                    <td>{{ formatDateText(item.actualStartDate) }}</td>
                    <td>{{ formatDateText(item.actualEndDate) }}</td>
                    <td>{{ formatDaysText(item.startVarianceDays) }}</td>
                    <td>{{ formatDaysText(item.finishVarianceDays) }}</td>
                    <td>{{ formatYesNo(item.isCritical) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
            </div>

            <div
            v-if="
              selectedReportPreviewContent?.baselineSnapshots
                && (
                  (selectedReportPreviewContent?.baselineSnapshots?.scopeBaselineCount ?? 0) > 0
                  || (selectedReportPreviewContent?.baselineSnapshots?.costBaselineCount ?? 0) > 0
                )
            "
            class="report-preview-section"
          >
            <div class="section-header-inline">
              <h4>基线快照</h4>
              <div class="dialog-summary-text">当前项目已保存的范围基线与成本基线信息</div>
            </div>
            <div class="report-preview-meta-grid">
              <div class="report-preview-meta-item">
                <span>范围基线数</span>
                <strong>{{ selectedReportPreviewContent?.baselineSnapshots?.scopeBaselineCount ?? 0 }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>成本基线数</span>
                <strong>{{ selectedReportPreviewContent?.baselineSnapshots?.costBaselineCount ?? 0 }}</strong>
              </div>
            </div>
            <div class="report-preview-list">
              <div
                v-if="selectedReportPreviewContent?.baselineSnapshots?.latestScopeBaseline"
                class="report-preview-list-item"
              >
                <strong>最新范围基线: {{ selectedReportPreviewContent?.baselineSnapshots?.latestScopeBaseline?.baselineName || '-' }}</strong>
                <span>版本: {{ selectedReportPreviewContent?.baselineSnapshots?.latestScopeBaseline?.versionNo || '-' }} | 状态: {{ selectedReportPreviewContent?.baselineSnapshots?.latestScopeBaseline?.status || '-' }}</span>
                <span>发布时间: {{ formatDateTimeText(selectedReportPreviewContent?.baselineSnapshots?.latestScopeBaseline?.publishedAt) }} | 快照任务数: {{ selectedReportPreviewContent?.baselineSnapshots?.latestScopeBaseline?.taskCount ?? 0 }}</span>
              </div>
              <div
                v-if="selectedReportPreviewContent?.baselineSnapshots?.latestCostBaseline"
                class="report-preview-list-item"
              >
                <strong>最新成本基线: {{ selectedReportPreviewContent?.baselineSnapshots?.latestCostBaseline?.baselineName || '-' }}</strong>
                <span>发布时间: {{ formatDateTimeText(selectedReportPreviewContent?.baselineSnapshots?.latestCostBaseline?.publishedAt) }}</span>
                <span>基线成本: {{ formatMoney(selectedReportPreviewContent?.baselineSnapshots?.latestCostBaseline?.plannedCost) }}</span>
              </div>
            </div>
            </div>

            <div v-if="selectedReportPreviewContent?.evmOverview" class="report-preview-section">
            <div class="section-header-inline">
              <h4>挣值概述</h4>
              <div class="dialog-summary-text">PV、EV、AC 及 CPI、SPI 等核心指标</div>
            </div>
            <div class="report-preview-meta-grid">
              <div class="report-preview-meta-item">
                <span>PV</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.evmOverview?.pv) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>EV</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.evmOverview?.ev) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>AC</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.evmOverview?.ac) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>CV</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.evmOverview?.cv) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>SV</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.evmOverview?.sv) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>CPI</span>
                <strong>{{ formatDecimalText(selectedReportPreviewContent?.evmOverview?.cpi, 4) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>SPI</span>
                <strong>{{ formatDecimalText(selectedReportPreviewContent?.evmOverview?.spi, 4) }}</strong>
              </div>
              <div class="report-preview-meta-item">
                <span>EAC</span>
                <strong>{{ formatMoney(selectedReportPreviewContent?.evmOverview?.eac) }}</strong>
              </div>
            </div>
            </div>

          <div v-if="selectedReportPreviewContent?.evmDetails?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>挣值详情</h4>
              <div class="dialog-summary-text">按任务拆解 PV、EV、AC 与绩效指数</div>
            </div>
            <div class="report-preview-table-wrapper">
              <table class="report-preview-table">
                <thead>
                  <tr>
                    <th>任务</th>
                    <th>状态</th>
                    <th>PV</th>
                    <th>EV</th>
                    <th>AC</th>
                    <th>CV</th>
                    <th>SV</th>
                    <th>CPI</th>
                    <th>SPI</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="item in selectedReportPreviewContent.evmDetails"
                    :key="`report-preview-evm-detail-${item.taskId}`"
                  >
                    <td>
                      <strong>{{ item.taskCode || '-' }}</strong>
                      <div class="report-preview-table-sub">{{ item.taskName || '未命名任务' }}</div>
                    </td>
                    <td>{{ formatTaskStatus(item.status) }} / {{ formatPercentValue(item.progress) }}</td>
                    <td>{{ formatMoney(item.pv) }}</td>
                    <td>{{ formatMoney(item.ev) }}</td>
                    <td>{{ formatMoney(item.ac) }}</td>
                    <td>{{ formatMoney(item.cv) }}</td>
                    <td>{{ formatMoney(item.sv) }}</td>
                    <td>{{ formatDecimalText(item.cpi, 4) }}</td>
                    <td>{{ formatDecimalText(item.spi, 4) }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          </template>

          <template v-if="activeReportPreviewTab === 'risk'">
            <div v-if="selectedReportPreviewContent?.openRisks?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>未关闭风险</h4>
              <div class="dialog-summary-text">按影响程度展示当前主要风险</div>
            </div>
            <div class="report-preview-list">
              <div
                v-for="item in selectedReportPreviewContent.openRisks"
                :key="`report-preview-risk-${item.id}`"
                class="report-preview-list-item"
              >
                <strong>{{ item.riskCode || '-' }} | {{ item.name || '未命名风险' }}</strong>
                <span>等级: {{ formatRiskLevel(item.level) }} | 状态: {{ formatRiskStatus(item.status) }}</span>
                <span>责任人: {{ item.ownerName || '未设置' }} | 关联任务: {{ item.taskName || '未关联' }}</span>
                <span>{{ item.responseStrategy || item.description || '暂无应对说明' }}</span>
              </div>
            </div>
            </div>

            <div v-if="selectedReportPreviewContent?.pendingChanges?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>待处理变更</h4>
              <div class="dialog-summary-text">展示当前尚未关闭的变更申请</div>
            </div>
            <div class="report-preview-list">
              <div
                v-for="item in selectedReportPreviewContent.pendingChanges"
                :key="`report-preview-change-${item.id}`"
                class="report-preview-list-item"
              >
                <strong>{{ item.changeCode || '-' }} | {{ item.title || '未命名变更' }}</strong>
                <span>类型: {{ formatChangeType(item.changeType) }} | 优先级: {{ formatPriorityText(item.priority) }} | 状态: {{ formatChangeStatus(item.status) }}</span>
                <span>申请人: {{ item.proposerName || '-' }} | 提交时间: {{ formatDateTimeText(item.submittedAt) }}</span>
                <span>{{ item.impactAnalysis || item.reason || '暂无影响分析' }}</span>
              </div>
            </div>
            </div>
          </template>

          <template v-if="activeReportPreviewTab === 'summary'">
            <div v-if="selectedReportPreviewContent?.archives?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>归档材料</h4>
              <div class="dialog-summary-text">总结报告附带归档结果</div>
            </div>
            <div class="report-preview-list">
              <div
                v-for="item in selectedReportPreviewContent.archives"
                :key="`report-preview-archive-${item.id}`"
                class="report-preview-list-item"
              >
                <strong>{{ item.itemName || '未命名归档项' }}</strong>
                <span>类型: {{ formatArchiveType(item.archiveType) }} | 状态: {{ formatArchiveStatus(item.status) }}</span>
                <span>归档时间: {{ formatDateTimeText(item.archivedAt) }}</span>
                <span>{{ item.repositoryUrl || (item.attachmentId ? `附件 ID: ${item.attachmentId}` : '无仓库地址或附件') }}</span>
              </div>
            </div>
            </div>

            <div v-if="selectedReportPreviewContent?.lessonsLearned?.length" class="report-preview-section">
            <div class="section-header-inline">
              <h4>经验教训</h4>
              <div class="dialog-summary-text">总结报告附带项目复盘内容</div>
            </div>
            <div class="report-preview-list">
              <div
                v-for="item in selectedReportPreviewContent.lessonsLearned"
                :key="`report-preview-lesson-${item.id}`"
                class="report-preview-list-item"
              >
                <strong>{{ item.title || '未命名经验' }}</strong>
                <span>分类: {{ item.category || '-' }} | 作者: {{ item.authorName || '-' }}</span>
                <span>记录时间: {{ formatDateTimeText(item.createdAt) }}</span>
                <span>{{ item.content || '暂无内容' }}</span>
              </div>
            </div>
            </div>
          </template>
        </template>

        <div v-else class="report-preview-section">
          <div class="section-header-inline">
            <h4>旧版报表内容</h4>
            <div class="dialog-summary-text">这是旧版生成的报表，目前仅能展示当时保存的参数</div>
          </div>
          <div class="report-preview-meta-grid">
            <div class="report-preview-meta-item">
              <span>项目 ID</span>
              <strong>{{ selectedReportPreviewContent?.projectId || '-' }}</strong>
            </div>
            <div class="report-preview-meta-item">
              <span>报表类型</span>
              <strong>{{ formatReportType(selectedReportPreviewContent?.reportType, selectedReportPreview.title) }}</strong>
            </div>
            <div class="report-preview-meta-item">
              <span>周期类型</span>
              <strong>{{ selectedReportPreviewContent?.periodType || '-' }}</strong>
            </div>
            <div class="report-preview-meta-item">
              <span>开始日期</span>
              <strong>{{ formatDateText(selectedReportPreviewContent?.startDate) }}</strong>
            </div>
            <div class="report-preview-meta-item">
              <span>结束日期</span>
              <strong>{{ formatDateText(selectedReportPreviewContent?.endDate) }}</strong>
            </div>
            <div class="report-preview-meta-item">
              <span>生成时间</span>
              <strong>{{ formatDateTimeText(selectedReportPreviewContent?.generatedAt || selectedReportPreview.generatedAt) }}</strong>
            </div>
          </div>
          <pre v-if="selectedReportPreviewContent" class="report-preview-raw">{{ formatReportJsonContent(selectedReportPreviewContent) }}</pre>
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

.toolbar-spacer {
  margin-left: auto;
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
  display: flex;
  align-items: center;
  padding: 0 14px;
  color: #666;
  font-size: 13px;
}

.toolbar-user {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 16px 0 0;
}

.toolbar-user-copy {
  min-width: 0;
  display: grid;
  gap: 2px;
}

.toolbar-user-copy strong,
.toolbar-user-copy span {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.toolbar-user-copy strong {
  color: #1f2937;
  font-size: 13px;
  font-weight: 700;
}

.toolbar-user-copy span {
  color: #667085;
  font-size: 12px;
}

.ribbon {
  display: flex;
  flex-wrap: wrap;
  min-height: 96px;
  overflow-x: visible;
}

.ribbon-group {
  flex: 1 1 196px;
  min-width: 196px;
  padding: 10px 14px 6px;
  border-right: 1px solid #d6d6d6;
  border-bottom: 1px solid #d6d6d6;
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
  grid-template-rows: auto auto 1fr;
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

.schedule-banner-hint {
  margin-left: 12px;
  color: #777;
  font-size: 12px;
}

.schedule-banner-view {
  margin-left: 8px;
  padding: 2px 8px;
  border-radius: 999px;
  background: #eef4ff;
  color: #1f5fbf;
  font-size: 12px;
  font-weight: 700;
}

.task-action-banner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
  margin: 0 12px 8px;
  padding: 8px 12px;
  border: 1px solid #dbe3ee;
  border-left: 3px solid #7ea6dd;
  background: #f9fbfe;
  border-radius: 4px;
}

.task-action-banner-copy {
  min-width: 0;
  display: grid;
  gap: 3px;
}

.task-action-banner-copy strong {
  color: #26466f;
  font-size: 14px;
  line-height: 1.3;
}

.task-action-banner-copy span {
  color: #607089;
  font-size: 12px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.task-action-banner-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  flex-shrink: 0;
  gap: 6px;
}

.task-action-banner-reason {
  width: 100%;
  color: #b2461a;
  font-size: 12px;
  line-height: 1.4;
}

.editor-context-menu {
  position: fixed;
  z-index: 90;
  width: 156px;
  max-height: calc(100vh - 24px);
  padding: 0;
  border: 1px solid #d7d7d7;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  overflow: visible;
}

.context-menu-header {
  display: grid;
  gap: 3px;
  padding: 10px 12px 8px;
  border-bottom: 1px solid #efefef;
}

.context-menu-header strong {
  color: #222;
  font-size: 14px;
  line-height: 1.2;
}

.context-menu-header span {
  color: #777;
  font-size: 12px;
  line-height: 1.4;
}

.context-menu-categories {
  padding: 6px 0;
  min-height: 0;
  overflow: auto;
}

.context-menu-category {
  width: 100%;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 12px;
  border: 0;
  background: #fff;
  color: #222;
  font-size: 13px;
  text-align: left;
  cursor: pointer;
  transition: background 120ms ease;
}

.context-menu-category:hover,
.context-menu-category:focus-visible,
.context-menu-category.active {
  background: #f5f5f5;
  outline: none;
}

.context-menu-category-arrow {
  color: #999;
  font-size: 14px;
}

.context-menu-submenu-panel {
  position: absolute;
  top: 0;
  left: calc(100% + 4px);
  width: 228px;
  max-height: calc(100vh - 24px);
  border: 1px solid #d7d7d7;
  border-radius: 6px;
  background: #fff;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  overflow: auto;
}

.submenu-panel-left {
  left: auto;
  right: calc(100% + 4px);
}

.context-menu-submenu-title {
  padding: 9px 12px 7px;
  border-bottom: 1px solid #efefef;
  color: #888;
  font-size: 11px;
  font-weight: 700;
}

.context-menu-actions {
  display: grid;
  gap: 0;
}

.context-menu-action {
  width: 100%;
  display: grid;
  gap: 2px;
  padding: 8px 12px;
  border: 0;
  border-radius: 0;
  background: #fff;
  text-align: left;
  cursor: pointer;
  transition: background 120ms ease;
}

.context-menu-action:hover:not(:disabled),
.context-menu-action:focus-visible {
  background: #f5f5f5;
  outline: none;
}

.context-menu-action strong {
  color: #222;
  font-size: 13px;
  font-weight: 600;
}

.context-menu-action span {
  color: #8a8a8a;
  font-size: 11px;
  line-height: 1.35;
}

.context-menu-action.active {
  background: #f2f6fb;
}

.context-menu-action:disabled {
  opacity: 0.45;
  cursor: default;
}

.context-menu-action-danger {
  background: #fff;
}

.context-menu-action-danger strong {
  color: #c0392b;
}

.gantt-shell {
  min-height: 0;
  display: grid;
  grid-template-columns: max-content minmax(0, 1fr);
  border-top: 1px solid #d9e1ec;
  background: #f3f6fb;
  overflow: hidden;
}

.task-grid {
  width: max-content;
  min-width: max-content;
  border-right: 1px solid #dbe3ef;
  background: #f7f9fc;
  overflow: hidden;
}

.grid-header,
.grid-row {
  display: grid;
  grid-template-columns: 44px 110px 110px 220px 90px 152px 152px;
}

.grid-row {
  --grid-row-bg: #ffffff;
  --grid-row-hover-bg: #f6f9fe;
  --grid-row-border: #e5ebf3;
  --grid-row-text: #344054;
  position: relative;
}

.grid-row:nth-child(even) {
  --grid-row-bg: #fbfcff;
  --grid-row-hover-bg: #f4f8ff;
}

.grid-row:hover {
  --grid-row-bg: var(--grid-row-hover-bg);
}

.parent-task-row {
  --grid-row-bg: #f3f7ff;
  --grid-row-hover-bg: #edf4ff;
  --grid-row-border: #dbe5f3;
}

.placeholder-task-row {
  --grid-row-bg: #fafbfd;
  --grid-row-hover-bg: #f7f9fc;
  --grid-row-text: #7c879a;
}

.task-grid-readonly .grid-row {
  --grid-row-bg: #f9fbfd;
  --grid-row-hover-bg: #f5f8fc;
}

.task-grid-readonly .parent-task-row {
  --grid-row-bg: #f2f6fb;
  --grid-row-hover-bg: #edf3fa;
}

.grid-row.active {
  --grid-row-bg: #eaf2ff;
  --grid-row-hover-bg: #eaf2ff;
  --grid-row-border: #cfe0fb;
}

.grid-row.active::before {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 3px;
  background: linear-gradient(180deg, #2c68c9, #74a6ff);
  z-index: 2;
}

.header-cell,
.grid-cell {
  min-width: 0;
  height: 38px;
  display: flex;
  align-items: center;
  padding: 0 10px;
  border-right: 1px solid #e3eaf3;
  border-bottom: 1px solid var(--grid-row-border, #e5ebf3);
  background: var(--grid-row-bg, #fff);
  color: var(--grid-row-text, #333);
  transition: background-color 0.16s ease, border-color 0.16s ease, color 0.16s ease, box-shadow 0.16s ease;
}

.header-cell {
  height: 50px;
  padding: 0 12px;
  border-right-color: #dbe3ee;
  border-bottom: 1px solid #d9e1ec;
  background: linear-gradient(180deg, #f8fbff 0%, #eef3f9 100%);
  color: #3d4f68;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.01em;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
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
  color: inherit;
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
  background: rgba(31, 95, 191, 0.08);
  box-shadow: inset 0 0 0 2px #1f5fbf;
  z-index: 1;
}

.parent-task-cell {
  background: transparent;
}

.parent-task-row .indicator,
.parent-task-row .wbs,
.parent-task-row .mode input {
  color: #215aa8;
  font-weight: 700;
}

.parent-task-input {
  font-weight: 700;
  color: #1d477f;
}

.parent-task-row .outline-toggle {
  color: #2a63bb;
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

.parent-shell-overlay {
  position: absolute;
  box-sizing: border-box;
  pointer-events: none;
  z-index: 2;
  opacity: 0.96;
  overflow: visible;
}

.parent-shell-overlay::before,
.parent-shell-overlay::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  width: var(--parent-shell-stroke, 2px);
  background: linear-gradient(
    to bottom,
    currentColor 0 var(--parent-shell-side-edge, 8px),
    transparent var(--parent-shell-side-edge, 8px),
    transparent calc(100% - var(--parent-shell-side-edge, 8px)),
    currentColor calc(100% - var(--parent-shell-side-edge, 8px)) 100%
  );
}

.parent-shell-overlay::before {
  left: 0;
}

.parent-shell-overlay::after {
  right: 0;
}

.parent-shell-overlay-lid {
  position: absolute;
  left: 0;
  right: 0;
  height: 0;
}

.parent-shell-overlay-lid-top {
  top: 0;
  border-top: var(--parent-shell-stroke, 2px) solid currentColor;
}

.parent-shell-overlay-lid-bottom {
  bottom: 0;
  border-bottom: var(--parent-shell-stroke, 2px) solid currentColor;
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

.timeline-cell.holiday {
  background: linear-gradient(135deg, #fff7cf, #ffe6a3);
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
  overflow: visible;
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

.task-progress-text {
  position: absolute;
  inset: 0 8px 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 0;
  color: #fff;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-shadow: 0 1px 2px rgba(19, 37, 67, 0.35);
  pointer-events: none;
  z-index: 4;
  white-space: nowrap;
}

.task-progress-text-compact {
  inset: -18px auto auto 50%;
  padding: 2px 6px;
  border: 1px solid rgba(69, 94, 135, 0.16);
  background: rgba(255, 255, 255, 0.96);
  color: #274268;
  text-shadow: none;
  transform: translateX(-50%);
  box-shadow: 0 3px 10px rgba(31, 52, 85, 0.14);
}

.task-progress-text-parent {
  inset: auto auto -18px 50%;
  padding: 2px 8px;
  border: 1px solid rgba(69, 94, 135, 0.16);
  background: rgba(255, 255, 255, 0.96);
  color: #274268;
  text-shadow: none;
  transform: translateX(-50%);
  box-shadow: 0 3px 10px rgba(31, 52, 85, 0.14);
}

.task-progress-text-parent-collapsed {
  inset: 0 8px 0 8px;
  color: #274268;
  text-shadow: none;
  transform: none;
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
  --parent-shell-stroke: 3px;
  --parent-shell-segment: 10px;
}

.bar-variant-parent-nested {
  top: 7px;
  height: 22px;
  --parent-shell-stroke: 2px;
  --parent-shell-segment: 8px;
}

.parent-shell-bar {
  background: transparent !important;
  border: 0 !important;
  box-shadow: none !important;
  overflow: visible;
  z-index: 3;
}

.parent-shell-bar::before,
.parent-shell-bar::after {
  content: '';
  position: absolute;
  top: 0;
  height: var(--parent-shell-segment, 8px);
  pointer-events: none;
}

.parent-shell-bar::before {
  left: 0;
  right: 0;
  border-top: var(--parent-shell-stroke, 2px) solid currentColor;
}

.parent-shell-bar::after {
  left: 0;
  right: 0;
  border-left: var(--parent-shell-stroke, 2px) solid currentColor;
  border-right: var(--parent-shell-stroke, 2px) solid currentColor;
  opacity: 0.92;
}

.parent-shell-bar .task-progress-fill {
  display: none;
}

.parent-shell-collapsed {
  background: color-mix(in srgb, currentColor 12%, white 88%) !important;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.14) !important;
}

.parent-shell-collapsed::after {
  height: 100%;
  border-bottom: var(--parent-shell-stroke, 2px) solid currentColor;
}

.parent-box-lid {
  position: absolute;
  left: 0;
  right: 0;
  height: var(--parent-shell-lid-height, 8px);
  pointer-events: none;
  opacity: 0.96;
}

.parent-box-lid::before,
.parent-box-lid::after {
  content: '';
  position: absolute;
  top: 0;
  bottom: 0;
  width: 0;
  box-sizing: border-box;
}

.parent-box-lid-top {
  top: 0;
  border-top: var(--parent-shell-stroke, 2px) solid currentColor;
}

.parent-box-lid-bottom {
  bottom: 0;
  border-bottom: var(--parent-shell-stroke, 2px) solid currentColor;
}

.parent-box-lid::before {
  left: 0;
  border-left: var(--parent-shell-stroke, 2px) solid currentColor;
}

.parent-box-lid::after {
  right: 0;
  border-right: var(--parent-shell-stroke, 2px) solid currentColor;
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

.style-layout-readonly {
  pointer-events: none;
  opacity: 0.7;
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
  flex-wrap: wrap;
  gap: 8px;
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

.task-progress-status-field {
  display: grid;
  gap: 6px;
  width: 100%;
}

.task-progress-status-note {
  color: #6b778c;
  font-size: 12px;
  line-height: 1.5;
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
  color: #5a6780;
  cursor: default;
}

.task-grid-readonly .editable :deep(.el-input__wrapper),
.task-grid-readonly .editable :deep(.el-textarea__inner),
.task-grid-readonly .editable :deep(.el-date-editor.el-input),
.task-grid-readonly .editable :deep(.el-date-editor .el-input__wrapper),
.task-grid-readonly .editable :deep(.el-input.is-disabled .el-input__wrapper),
.task-grid-readonly .editable :deep(.el-textarea.is-disabled .el-textarea__inner) {
  background: transparent;
  box-shadow: none;
}

.task-grid-readonly .editable :deep(.el-input.is-disabled .el-input__inner),
.task-grid-readonly .editable :deep(.el-textarea.is-disabled .el-textarea__inner),
.task-grid-readonly .editable :deep(.el-date-editor .el-input__inner) {
  color: #5a6780;
  -webkit-text-fill-color: #5a6780;
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

.dialog-filter-inline {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  margin-bottom: 10px;
  border: 1px solid #d9e2f1;
  background: #f7faff;
  color: #40506b;
  font-size: 13px;
}

.report-generate-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.report-preview {
  display: grid;
  gap: 16px;
}

.report-preview-hero {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border: 1px solid #d9e2f1;
  background: #f7faff;
}

.report-preview-hero-main {
  display: grid;
  gap: 6px;
}

.report-preview-hero-main h3 {
  margin: 0;
  color: #20324d;
  font-size: 18px;
}

.report-preview-hero-main p {
  margin: 0;
  color: #5a6d8a;
  font-size: 13px;
}

.report-preview-hero-side {
  min-width: 220px;
  display: grid;
  align-content: start;
  gap: 4px;
  color: #5a6d8a;
  font-size: 13px;
  text-align: right;
}

.report-preview-hero-side strong {
  color: #20324d;
  font-size: 15px;
}

.report-preview-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.report-preview-metric-card {
  display: grid;
  gap: 8px;
  padding: 14px 16px;
  border: 1px solid #dfe5ef;
  background: #fff;
}

.report-preview-metric-card span {
  color: #60718b;
  font-size: 13px;
}

.report-preview-metric-card strong {
  color: #20324d;
  font-size: 20px;
}

.report-preview-tab-bar {
  display: flex;
  gap: 8px;
  padding: 6px;
  border: 1px solid #dfe5ef;
  background: #fff;
  overflow-x: auto;
}

.report-preview-tab {
  flex: 0 0 auto;
  padding: 9px 14px;
  border: 0;
  background: transparent;
  color: #60718b;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  transition: background 0.2s ease, color 0.2s ease;
}

.report-preview-tab:hover,
.report-preview-tab:focus-visible {
  background: #eef5ff;
  color: #173b72;
  outline: none;
}

.report-preview-tab.active {
  background: #1f5fbf;
  color: #fff;
}

.report-preview-section {
  display: grid;
  gap: 12px;
  padding: 14px 16px;
  border: 1px solid #dfe5ef;
  background: #fff;
}

.report-preview-meta-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.report-preview-meta-item {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  border: 1px solid #eef2f7;
  background: #f9fbfd;
}

.report-preview-meta-item span {
  color: #60718b;
  font-size: 12px;
}

.report-preview-meta-item strong {
  color: #20324d;
  font-size: 14px;
}

.report-preview-note {
  padding: 12px 14px;
  border: 1px solid #eef2f7;
  background: #f9fbfd;
  color: #41526d;
  line-height: 1.6;
}

.report-preview-list {
  display: grid;
  gap: 10px;
}

.report-preview-list-item {
  display: grid;
  gap: 6px;
  padding: 12px 14px;
  border: 1px solid #eef2f7;
  background: #f9fbfd;
}

.report-preview-list-item span {
  color: #54657f;
  font-size: 13px;
}

.report-preview-table-wrapper {
  overflow-x: auto;
  border: 1px solid #eef2f7;
  background: #fff;
}

.report-preview-table {
  width: 100%;
  min-width: 920px;
  border-collapse: collapse;
}

.report-preview-table th,
.report-preview-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #eef2f7;
  text-align: left;
  vertical-align: top;
  font-size: 13px;
  line-height: 1.5;
}

.report-preview-table th {
  background: #f8fbff;
  color: #49627f;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

.report-preview-table td {
  color: #22354f;
}

.report-preview-table tbody tr:last-child td {
  border-bottom: 0;
}

.report-preview-table strong {
  color: #20324d;
  font-size: 13px;
}

.report-preview-table-sub {
  margin-top: 4px;
  color: #60718b;
  font-size: 12px;
}

.report-preview-timeline-header {
  align-items: flex-start;
}

.report-preview-timeline-header-meta {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.report-preview-timeline-controls {
  display: inline-flex;
  gap: 8px;
}

.report-preview-timeline-control {
  width: 36px;
  height: 36px;
  border: 1px solid #d4deee;
  background: #f4f8ff;
  color: #274268;
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: background 0.2s ease, border-color 0.2s ease, color 0.2s ease;
}

.report-preview-timeline-control:hover,
.report-preview-timeline-control:focus-visible {
  border-color: #7ea3db;
  background: #e8f1ff;
  color: #173b72;
  outline: none;
}

.report-preview-task-timeline-scroll {
  overflow-x: auto;
  overflow-y: hidden;
  padding-bottom: 8px;
  scroll-snap-type: x proximity;
}

.report-preview-task-timeline-scroll:focus-visible {
  outline: 2px solid #7ea3db;
  outline-offset: 4px;
}

.report-preview-task-timeline-inner {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 18px;
  min-width: max-content;
  padding: 8px 6px 10px;
}

.report-preview-task-timeline-inner::before {
  content: '';
  position: absolute;
  top: 23px;
  left: 24px;
  right: 24px;
  height: 2px;
  background: linear-gradient(90deg, #d8e4f6 0%, #9dbce5 100%);
}

.report-preview-task-node {
  --timeline-accent: #5d87c9;
  --timeline-soft: #eaf2ff;
  position: relative;
  flex: 0 0 292px;
  display: grid;
  gap: 12px;
  scroll-snap-align: start;
}

.report-preview-task-node-todo {
  --timeline-accent: #7e8ea8;
  --timeline-soft: #eef2f8;
}

.report-preview-task-node-active {
  --timeline-accent: #2a78d8;
  --timeline-soft: #e8f2ff;
}

.report-preview-task-node-review {
  --timeline-accent: #c88918;
  --timeline-soft: #fff4dc;
}

.report-preview-task-node-done {
  --timeline-accent: #2f9c6b;
  --timeline-soft: #e7f7ef;
}

.report-preview-task-node-blocked {
  --timeline-accent: #d1604f;
  --timeline-soft: #fff0eb;
}

.report-preview-task-node-head {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 10px;
}

.report-preview-task-node-dot {
  width: 16px;
  height: 16px;
  border: 3px solid #fff;
  border-radius: 50%;
  background: var(--timeline-accent);
  box-shadow: 0 0 0 3px var(--timeline-soft);
  flex: 0 0 16px;
}

.report-preview-task-node-date {
  padding: 4px 10px;
  border: 1px solid #dde6f4;
  background: #fff;
  color: #48617f;
  font-size: 12px;
  white-space: nowrap;
}

.report-preview-task-card {
  display: grid;
  gap: 10px;
  min-height: 212px;
  padding: 16px;
  border: 1px solid #dde5f1;
  border-top: 3px solid var(--timeline-accent);
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 10px 24px rgba(41, 71, 120, 0.08);
}

.report-preview-task-card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.report-preview-task-card-top strong {
  color: #20324d;
  font-size: 13px;
  letter-spacing: 0.02em;
}

.report-preview-task-status {
  padding: 4px 10px;
  background: var(--timeline-soft);
  color: var(--timeline-accent);
  font-size: 12px;
  white-space: nowrap;
}

.report-preview-task-card h5 {
  margin: 0;
  color: #20324d;
  font-size: 16px;
  line-height: 1.4;
}

.report-preview-task-card p {
  margin: 0;
  color: #5a6d8a;
  font-size: 13px;
  line-height: 1.6;
}

.report-preview-task-card-meta {
  margin-top: auto;
  display: grid;
  gap: 6px;
}

.report-preview-task-card-meta span {
  color: #465a77;
  font-size: 12px;
}

.report-preview-raw {
  margin: 0;
  padding: 12px 14px;
  border: 1px solid #eef2f7;
  background: #f6f8fb;
  color: #33445f;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.6;
}

.report-preview-dialog :deep(.el-dialog__body) {
  max-height: 72vh;
  overflow: auto;
}

@media (max-width: 900px) {
  .schedule-banner {
    flex-wrap: wrap;
    justify-content: flex-start;
    height: auto;
    padding: 10px 12px;
  }

  .schedule-banner-hint {
    margin-left: 0;
  }

  .task-action-banner {
    flex-direction: column;
    align-items: flex-start;
    padding: 10px 12px;
  }

  .task-action-banner-copy span {
    white-space: normal;
  }

  .task-action-banner-actions {
    justify-content: flex-start;
    width: 100%;
  }

  .editor-context-menu {
    width: 140px;
  }

  .context-menu-submenu-panel {
    width: min(220px, calc(100vw - 36px));
  }

  .ribbon-group {
    flex-basis: 164px;
    min-width: 164px;
  }

  .toolbar-state {
    display: none;
  }

  .toolbar-user {
    padding-right: 10px;
  }

  .toolbar-user-copy span {
    display: none;
  }

  .toolbar-user-copy strong {
    max-width: 92px;
  }

  .report-preview-hero {
    flex-direction: column;
  }

  .report-preview-hero-side {
    min-width: 0;
    text-align: left;
  }

  .report-preview-metrics {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .report-preview-meta-grid {
    grid-template-columns: 1fr;
  }

  .report-preview-timeline-header-meta {
    width: 100%;
    justify-content: space-between;
  }

  .report-preview-task-node {
    flex-basis: 258px;
  }
}
</style>
