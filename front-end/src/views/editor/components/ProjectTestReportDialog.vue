<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  canEditProjectContent: {
    type: Boolean,
    default: false,
  },
  testingLoading: {
    type: Boolean,
    default: false,
  },
  testReportList: {
    type: Array,
    default: () => [],
  },
  testReportGenerateForm: {
    type: Object,
    required: true,
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
    title="测试报告"
    width="1080px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>生成测试报告</h4>
        <div class="dialog-summary-text">按当前测试计划、用例和缺陷数据生成汇总快照</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="报告类型">
          <el-input v-model="testReportGenerateForm.type" disabled />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="testReportGenerateForm.startDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="testReportGenerateForm.endDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetTestReportGenerateForm">重置日期</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="testingLoading"
          @click="actions.generateTestReport"
        >
          生成报告
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>报告列表</h4>
        <div class="dialog-summary-text">保留每次生成时的测试快照，便于阶段回顾</div>
      </div>
      <div v-if="testReportList.length" class="simple-list">
        <div v-for="item in testReportList" :key="`test-report-${item.id}`" class="simple-list-item">
          <strong>{{ item.title || `测试报告${item.id}` }}</strong>
          <span>生成时间: {{ formatters.formatDateTimeText(item.generatedAt) }} | 生成人: {{ item.generatedByName || item.generatedBy || '-' }}</span>
          <span>
            用例总数: {{ helpers.getTestReportSummary(item)?.caseCount ?? 0 }} |
            已执行: {{ helpers.getTestReportSummary(item)?.executedCaseCount ?? 0 }} |
            通过率: {{ formatters.formatPercentValue(helpers.getTestReportSummary(item)?.passRate ?? 0) }}
          </span>
          <span>
            测试计划: {{ helpers.getTestReportSummary(item)?.planCount ?? 0 }} |
            开放缺陷: {{ helpers.getTestReportSummary(item)?.openDefectCount ?? 0 }}
          </span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" type="danger" plain @click="actions.removeTestReport(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无测试报告" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
