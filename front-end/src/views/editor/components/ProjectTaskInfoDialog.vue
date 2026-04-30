<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  selectedTaskRow: {
    type: Object,
    default: null,
  },
  taskDetail: {
    type: Object,
    default: null,
  },
  taskDetailLoading: {
    type: Boolean,
    default: false,
  },
  taskDetailSection: {
    type: String,
    default: 'basic',
  },
  visibleTaskDetailSections: {
    type: Array,
    default: () => [],
  },
  taskBasicForm: {
    type: Object,
    required: true,
  },
  taskProgressForm: {
    type: Object,
    required: true,
  },
  taskDependencyForm: {
    type: Object,
    required: true,
  },
  taskCommentForm: {
    type: Object,
    required: true,
  },
  taskCommentThreads: {
    type: Array,
    default: () => [],
  },
  taskRelatedRisks: {
    type: Array,
    default: () => [],
  },
  taskAssigneeOptions: {
    type: Array,
    default: () => [],
  },
  milestoneOptions: {
    type: Array,
    default: () => [],
  },
  dependencyTaskOptions: {
    type: Array,
    default: () => [],
  },
  taskConstraintTypeOptions: {
    type: Array,
    default: () => [],
  },
  taskProgressDisplayStatus: {
    type: String,
    default: '',
  },
  taskProgressWorkflowHint: {
    type: String,
    default: '',
  },
  selectedTaskAssigneePlaceholder: {
    type: String,
    default: '',
  },
  permissions: {
    type: Object,
    required: true,
  },
  helpers: {
    type: Object,
    required: true,
  },
  formatters: {
    type: Object,
    required: true,
  },
  actions: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits([
  'update:visible',
  'update:taskDetailSection',
])

function updateVisible(value) {
  emit('update:visible', value)
}

function setSection(value) {
  emit('update:taskDetailSection', value)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="任务详情"
    width="820px"
    @update:model-value="updateVisible"
  >
    <div v-if="selectedTaskRow" class="info-section">
      <div class="dashboard-panels">
        <div class="dashboard-card"><span>任务名称</span><strong>{{ selectedTaskRow.name || '-' }}</strong></div>
        <div class="dashboard-card"><span>WBS</span><strong>{{ selectedTaskRow.wbsCode || '-' }}</strong></div>
        <div class="dashboard-card"><span>任务模式</span><strong>{{ selectedTaskRow.mode || '-' }}</strong></div>
        <div class="dashboard-card"><span>计划开始</span><strong>{{ selectedTaskRow.start || '-' }}</strong></div>
        <div class="dashboard-card"><span>计划完成</span><strong>{{ selectedTaskRow.finish || '-' }}</strong></div>
        <div class="dashboard-card">
          <span>当前状态</span>
          <strong>{{ formatters.formatTaskStatus(taskDetail?.status || selectedTaskRow.status) }}</strong>
        </div>
      </div>
    </div>

    <div v-if="selectedTaskRow" class="task-detail-section-tabs">
      <button
        v-for="item in visibleTaskDetailSections"
        :key="item.key"
        type="button"
        class="task-detail-tab"
        :class="{ active: taskDetailSection === item.key }"
        @click="setSection(item.key)"
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
            <div class="dialog-summary-text">维护描述、截止日期和任务约束</div>
          </div>
          <el-form label-width="88px" class="scope-form-grid">
            <el-form-item label="负责人">
              <el-select
                v-model="taskBasicForm.assigneeId"
                clearable
                filterable
                :disabled="!permissions.canEditTaskBasic || permissions.selectedTaskAssigneeLocked"
                :placeholder="selectedTaskAssigneePlaceholder"
              >
                <el-option
                  v-for="option in taskAssigneeOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="描述" class="scope-form-span">
              <el-input
                v-model="taskBasicForm.description"
                :disabled="!permissions.canEditTaskBasic"
                type="textarea"
                :rows="3"
                placeholder="输入任务说明、执行目标或补充信息"
              />
            </el-form-item>
            <el-form-item label="关联里程碑">
              <el-select
                v-model="taskBasicForm.milestoneId"
                clearable
                filterable
                :disabled="!permissions.canManageTaskBasic"
                placeholder="可选绑定到一个里程碑"
              >
                <el-option
                  v-for="option in milestoneOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="截止日期">
              <el-date-picker
                v-model="taskBasicForm.deadlineDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled="!permissions.canManageTaskBasic"
                :disabled-date="helpers.disabledScheduleModeDate"
                placeholder="可选设置任务截止日期"
              />
            </el-form-item>
            <el-form-item label="约束类型">
              <el-select
                v-model="taskBasicForm.constraintType"
                :disabled="!permissions.canManageTaskBasic"
                style="width: 100%"
                placeholder="选择约束类型"
                @change="actions.handleTaskConstraintTypeChange"
              >
                <el-option
                  v-for="option in taskConstraintTypeOptions"
                  :key="option.value || 'none'"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item label="约束日期">
              <el-date-picker
                v-model="taskBasicForm.constraintDate"
                type="date"
                value-format="YYYY-MM-DD"
                style="width: 100%"
                :disabled="!permissions.canManageTaskBasic || !formatters.requiresTaskConstraintDate(taskBasicForm.constraintType)"
                :disabled-date="helpers.disabledScheduleModeDate"
                placeholder="按约束类型设置日期"
              />
            </el-form-item>
          </el-form>
          <div v-if="permissions.canEditTaskBasic" class="dialog-actions-inline">
            <el-button type="primary" :loading="taskDetailLoading" @click="actions.saveTaskBasicInfo">保存基础信息</el-button>
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
              <strong>截止日期</strong>
              <span>{{ formatters.formatDateText(taskDetail?.deadlineDate || taskBasicForm.deadlineDate) }}</span>
            </div>
            <div class="simple-list-item">
              <strong>约束规则</strong>
              <span>
                {{ formatters.formatTaskConstraintType(taskDetail?.constraintType || taskBasicForm.constraintType) }}
                <template v-if="taskDetail?.constraintDate || taskBasicForm.constraintDate">
                  | {{ formatters.formatDateText(taskDetail?.constraintDate || taskBasicForm.constraintDate) }}
                </template>
              </span>
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
            <div class="dialog-summary-text">{{ taskProgressWorkflowHint }}</div>
          </div>
          <el-form label-width="88px" class="scope-form-grid">
            <el-form-item label="状态">
              <div class="task-progress-status-field">
                <el-input :model-value="formatters.formatTaskStatus(taskProgressDisplayStatus)" disabled />
                <span class="task-progress-status-note">
                  状态按进度自动计算，0% 为未开始，1%-99% 为进行中，100% 显示已完成；成员提交后才会进入待验收。
                </span>
              </div>
            </el-form-item>
            <el-form-item label="进度">
              <el-slider
                v-model="taskProgressForm.progress"
                :disabled="!permissions.selectedTaskCanUpdateProgress"
                :min="0"
                :max="100"
                :show-input="true"
              />
            </el-form-item>
            <el-form-item label="备注" class="scope-form-span">
              <el-input
                v-model="taskProgressForm.remark"
                :disabled="!permissions.selectedTaskCanUpdateProgress"
                type="textarea"
                :rows="3"
                placeholder="记录本次进展、阻塞原因或完成说明"
              />
            </el-form-item>
          </el-form>
        </div>

        <div v-if="selectedTaskRow?.taskId && taskDetailSection === 'dependency'" class="info-section">
          <div class="section-header-inline">
            <h4>任务依赖</h4>
            <div class="dialog-summary-text">维护当前任务的前后置关系，正数表示滞后，负数表示提前</div>
          </div>
          <div class="simple-list">
            <div v-if="taskDetail?.dependencies?.length" class="simple-list">
              <div
                v-for="item in taskDetail.dependencies"
                :key="`task-dependency-${item.id}`"
                class="simple-list-item"
              >
                <strong>{{ item.predecessorTaskName }} -> {{ item.successorTaskName }}</strong>
                <span>类型: {{ item.dependencyType || 'FS' }} | {{ formatters.formatDependencyLagText(item.lagDays) }}</span>
                <div class="baseline-actions">
                  <el-button
                    v-if="permissions.canEditTaskDependency"
                    size="small"
                    type="danger"
                    plain
                    @click="actions.removeTaskDependency(item)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
            <span v-else>暂无依赖</span>
          </div>
          <el-form v-if="permissions.canEditTaskDependency" label-width="88px" class="scope-form-grid">
            <el-form-item label="前置任务">
              <el-select
                v-model="taskDependencyForm.predecessorTaskId"
                filterable
                clearable
                placeholder="请选择前置任务"
              >
                <el-option
                  v-for="option in dependencyTaskOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
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
            <el-form-item label="提前/滞后">
              <el-input-number v-model="taskDependencyForm.lagDays" :step="1" style="width: 100%" />
            </el-form-item>
          </el-form>
          <div v-if="permissions.canEditTaskDependency" class="dialog-actions-inline">
            <el-button type="primary" :loading="taskDetailLoading" @click="actions.saveTaskDependency">新增依赖</el-button>
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
                <span>级别: {{ formatters.formatRiskLevel(item.level) }} | 状态: {{ formatters.formatRiskStatus(item.status) }}</span>
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
            <el-button text @click="actions.resetTaskCommentForm">取消回复</el-button>
          </div>
          <div class="scope-form-grid">
            <div class="scope-form-span">
              <el-input
                v-model="taskCommentForm.content"
                :disabled="!permissions.canCommentOnTasks"
                type="textarea"
                :rows="3"
                placeholder="输入评论内容"
              />
            </div>
          </div>
          <div v-if="permissions.canCommentOnTasks" class="dialog-actions-inline">
            <el-button type="primary" :loading="taskDetailLoading" @click="actions.saveTaskComment">发表评论</el-button>
          </div>
          <div class="simple-list">
            <div v-if="taskCommentThreads.length" class="comment-thread-list">
              <div v-for="item in taskCommentThreads" :key="`task-comment-${item.id}`" class="comment-thread-item">
                <div class="simple-list-item">
                  <strong>{{ item.userName || '未知用户' }}</strong>
                  <span>{{ item.createdAt || '-' }}</span>
                  <span>{{ item.content || '' }}</span>
                  <div class="baseline-actions">
                    <el-button
                      v-if="permissions.canCommentOnTasks"
                      size="small"
                      text
                      @click="actions.startReplyComment(item)"
                    >
                      回复
                    </el-button>
                    <el-button
                      v-if="helpers.canDeleteTaskComment(item)"
                      size="small"
                      type="danger"
                      plain
                      @click="actions.removeTaskComment(item)"
                    >
                      删除
                    </el-button>
                  </div>
                </div>
                <div v-if="item.replies?.length" class="comment-replies">
                  <div
                    v-for="reply in item.replies"
                    :key="`task-comment-reply-${reply.id}`"
                    class="simple-list-item comment-reply-item"
                  >
                    <strong>{{ reply.userName || '未知用户' }}</strong>
                    <span>{{ reply.createdAt || '-' }}</span>
                    <span>回复 {{ reply.replyToName || item.userName || '该评论' }}：{{ reply.content || '' }}</span>
                    <div class="baseline-actions">
                      <el-button
                        v-if="permissions.canCommentOnTasks"
                        size="small"
                        text
                        @click="actions.startReplyComment(reply)"
                      >
                        回复
                      </el-button>
                      <el-button
                        v-if="helpers.canDeleteTaskComment(reply)"
                        size="small"
                        type="danger"
                        plain
                        @click="actions.removeTaskComment(reply)"
                      >
                        删除
                      </el-button>
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
      <el-button @click="updateVisible(false)">关闭</el-button>
      <el-button
        v-if="taskDetailSection === 'progress' && permissions.selectedTaskCanUpdateProgress"
        :loading="taskDetailLoading"
        @click="actions.saveTaskProgress"
      >
        保存进度
      </el-button>
      <el-button
        v-if="taskDetailSection === 'progress' && permissions.selectedTaskCanSubmitReview"
        type="primary"
        :loading="taskDetailLoading"
        @click="actions.submitTaskCompletion"
      >
        提交完成
      </el-button>
      <el-button
        v-if="taskDetailSection === 'progress' && permissions.selectedTaskCanApproveReview"
        type="success"
        :loading="taskDetailLoading"
        @click="actions.approveTaskCompletion"
      >
        通过验收
      </el-button>
      <el-button
        v-if="taskDetailSection === 'progress' && permissions.selectedTaskCanApproveReview"
        type="warning"
        plain
        :loading="taskDetailLoading"
        @click="actions.rejectTaskCompletion"
      >
        打回修改
      </el-button>
      <el-button
        v-if="taskDetailSection === 'progress' && permissions.selectedTaskCanReopen"
        type="primary"
        plain
        :loading="taskDetailLoading"
        @click="actions.reopenTaskCompletion"
      >
        重开任务
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
