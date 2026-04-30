<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  editingMilestoneTaskLocalId: {
    type: [String, Number],
    default: null,
  },
  editingMilestoneId: {
    type: [String, Number],
    default: null,
  },
  canEditTaskPlan: {
    type: Boolean,
    default: false,
  },
  milestoneForm: {
    type: Object,
    required: true,
  },
  milestoneLoading: {
    type: Boolean,
    default: false,
  },
  visibleMilestoneList: {
    type: Array,
    default: () => [],
  },
  formatters: {
    type: Object,
    required: true,
  },
  helpers: {
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
])

function updateVisible(value) {
  emit('update:visible', value)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="里程碑管理"
    width="860px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingMilestoneTaskLocalId || editingMilestoneId ? '编辑里程碑' : '新增里程碑' }}</h4>
        <div class="dialog-summary-text">
          统一管理任务计划中的里程碑节点，保存文件后会同步到项目日历、报表和任务关联
        </div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="名称">
          <el-input v-model="milestoneForm.name" placeholder="例如 需求冻结、版本上线" />
        </el-form-item>
        <el-form-item label="状态">
          <el-input :model-value="formatters.formatMilestoneStatus(milestoneForm.status)" disabled />
        </el-form-item>
        <el-form-item label="计划时间">
          <el-date-picker
            v-model="milestoneForm.plannedDate"
            type="date"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            :disabled-date="helpers.disabledScheduleModeDate"
            placeholder="选择计划日期"
          />
        </el-form-item>
        <el-form-item label="说明" class="scope-form-span">
          <el-input
            v-model="milestoneForm.description"
            type="textarea"
            :rows="3"
            placeholder="记录该里程碑的验收标准或关键说明"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetMilestoneForm">重置</el-button>
        <el-button v-if="canEditTaskPlan" type="primary" :loading="milestoneLoading" @click="actions.saveMilestone">
          {{ editingMilestoneTaskLocalId || editingMilestoneId ? '更新任务计划' : '写入任务计划' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>里程碑列表</h4>
        <div class="dialog-summary-text">任务里程碑是主数据；历史台账里程碑可纳入计划统一管理</div>
      </div>
      <div v-if="visibleMilestoneList.length" class="simple-list">
        <div
          v-for="item in visibleMilestoneList"
          :key="item.isTaskBased ? `milestone-task-${item.localId}` : `milestone-legacy-${item.id}`"
          class="simple-list-item"
        >
          <strong>{{ item.name || `里程碑${item.id}` }}</strong>
          <span>
            状态: {{ formatters.formatMilestoneStatus(item.status || 'PENDING') }}
            <template v-if="item.isTaskBased">
              | 来源: 任务计划
              <template v-if="item.isTaskDraft"> | 待保存</template>
            </template>
            <template v-else> | 来源: 历史台账</template>
          </span>
          <span>计划时间: {{ item.plannedDate ? String(item.plannedDate).replace('T', ' ').slice(0, 19) : '-' }}</span>
          <span>{{ item.description || '暂无说明' }}</span>
          <div v-if="canEditTaskPlan" class="baseline-actions">
            <el-button size="small" @click="actions.populateMilestoneForm(item)">
              {{ item.isLegacyProjection ? '纳入计划' : '编辑' }}
            </el-button>
            <el-button size="small" type="danger" plain @click="actions.removeMilestone(item)">
              {{ item.isLegacyProjection ? '删除历史项' : '删除' }}
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无里程碑" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
