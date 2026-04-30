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
  editingQualityMetricId: {
    type: [String, Number],
    default: null,
  },
  qualityLoading: {
    type: Boolean,
    default: false,
  },
  qualityMetrics: {
    type: Array,
    default: () => [],
  },
  qualityMetricForm: {
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
])

function updateVisible(value) {
  emit('update:visible', value)
}
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="质量指标"
    width="980px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingQualityMetricId ? '编辑质量指标' : '新增质量指标' }}</h4>
        <div class="dialog-summary-text">维护缺陷密度、通过率、返工率等度量</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="指标名称">
          <el-input v-model="qualityMetricForm.metricName" placeholder="例如 用例通过率、缺陷密度、返工率" />
        </el-form-item>
        <el-form-item label="指标值">
          <el-input v-model="qualityMetricForm.metricValue" type="number" placeholder="0.00" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="qualityMetricForm.metricUnit" placeholder="例如 % / 个/KLOC / 次" />
        </el-form-item>
        <el-form-item label="统计日期">
          <el-date-picker
            v-model="qualityMetricForm.statisticDate"
            type="date"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetQualityMetricForm()">重置表单</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="qualityLoading"
          @click="actions.saveQualityMetric"
        >
          {{ editingQualityMetricId ? '保存修改' : '新增指标' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>指标列表</h4>
        <div class="dialog-summary-text">可逐步沉淀质量趋势，为测试管理模块打基础</div>
      </div>
      <div v-if="qualityMetrics.length" class="simple-list">
        <div v-for="item in qualityMetrics" :key="`quality-metric-${item.id}`" class="simple-list-item">
          <strong>{{ item.metricName || `质量指标${item.id}` }}</strong>
          <span>指标值: {{ item.metricValue ?? '-' }} {{ item.metricUnit || '' }}</span>
          <span>统计日期: {{ formatters.formatDateText(item.statisticDate) }} | 创建时间: {{ formatters.formatDateTimeText(item.createdAt) }}</span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" @click="actions.editQualityMetric(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="actions.removeQualityMetric(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无质量指标" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
