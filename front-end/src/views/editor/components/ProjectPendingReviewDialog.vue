<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  pendingReviewTaskCount: {
    type: Number,
    default: 0,
  },
  pendingReviewLoading: {
    type: Boolean,
    default: false,
  },
  pendingReviewTasks: {
    type: Array,
    default: () => [],
  },
  pendingReviewActingTaskId: {
    type: String,
    default: '',
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
    :title="`待验收任务（${pendingReviewTaskCount}）`"
    width="920px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>待验收任务列表</h4>
        <div class="dialog-summary-text">可直接通过验收，也可填写处理说明后直接打回修改。</div>
      </div>
      <el-skeleton :loading="pendingReviewLoading" animated>
        <template #template>
          <div class="simple-list">
            <div class="simple-list-item"><strong>正在加载待验收任务...</strong></div>
          </div>
        </template>
        <template #default>
          <div v-if="pendingReviewTasks.length" class="simple-list">
            <div
              v-for="item in pendingReviewTasks"
              :key="`pending-review-${item.id}`"
              class="simple-list-item"
            >
              <div class="pending-review-item-header">
                <strong>{{ item.name || `任务 ${item.id}` }}</strong>
                <span>{{ formatters.formatTaskStatus(item.status) }} | {{ formatters.formatPercentValue(item.progress) }}</span>
              </div>
              <div class="pending-review-item-meta">
                <span>负责人: {{ item.assigneeName || '-' }}</span>
                <span>计划时间: {{ formatters.formatDateText(item.plannedStartDate) }} - {{ formatters.formatDateText(item.plannedEndDate) }}</span>
                <span>计划工时: {{ formatters.formatHoursText(item.plannedHours) }}</span>
              </div>
              <span>{{ item.remark || item.description || '暂无提交说明' }}</span>
              <div class="pending-review-item-actions">
                <el-button
                  link
                  type="primary"
                  :disabled="pendingReviewActingTaskId === String(item.id)"
                  @click="actions.openPendingReviewTask(item)"
                >
                  查看并处理
                </el-button>
                <el-button
                  type="danger"
                  plain
                  size="small"
                  :loading="helpers.isPendingReviewActing(item.id, 'reject')"
                  @click="actions.rejectPendingReviewTask(item)"
                >
                  打回修改
                </el-button>
                <el-button
                  type="success"
                  plain
                  size="small"
                  :loading="helpers.isPendingReviewActing(item.id, 'approve')"
                  @click="actions.approvePendingReviewTask(item)"
                >
                  通过验收
                </el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无待验收任务" />
        </template>
      </el-skeleton>
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
