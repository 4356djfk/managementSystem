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
  editingQualityPlanId: {
    type: [String, Number],
    default: null,
  },
  qualityLoading: {
    type: Boolean,
    default: false,
  },
  qualityPlans: {
    type: Array,
    default: () => [],
  },
  qualityPlanForm: {
    type: Object,
    required: true,
  },
  qualityPlanStatusOptions: {
    type: Array,
    default: () => [],
  },
  taskAssigneeOptions: {
    type: Array,
    default: () => [],
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
    title="质量计划"
    width="980px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingQualityPlanId ? '编辑质量计划' : '新增质量计划' }}</h4>
        <div class="dialog-summary-text">当前共 {{ qualityPlans.length }} 条质量计划</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="计划标题">
          <el-input v-model="qualityPlanForm.title" placeholder="例如 版本交付质量控制计划" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="qualityPlanForm.ownerId" clearable filterable placeholder="选择负责人">
            <el-option
              v-for="option in taskAssigneeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="qualityPlanForm.status" style="width: 100%">
            <el-option
              v-for="option in qualityPlanStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="质量标准" class="scope-form-span">
          <el-input
            v-model="qualityPlanForm.qualityStandard"
            type="textarea"
            :rows="2"
            placeholder="记录执行标准、检查口径、编码规范或交付标准"
          />
        </el-form-item>
        <el-form-item label="验收规则" class="scope-form-span">
          <el-input
            v-model="qualityPlanForm.acceptanceRule"
            type="textarea"
            :rows="2"
            placeholder="记录验收方式、准入准出条件或评审门禁"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetQualityPlanForm()">重置表单</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="qualityLoading"
          @click="actions.saveQualityPlan"
        >
          {{ editingQualityPlanId ? '保存修改' : '新增计划' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>计划列表</h4>
        <div class="dialog-summary-text">按状态和负责人维护项目质量基线</div>
      </div>
      <div v-if="qualityPlans.length" class="simple-list">
        <div v-for="item in qualityPlans" :key="`quality-plan-${item.id}`" class="simple-list-item">
          <strong>{{ item.title || `质量计划${item.id}` }}</strong>
          <span>负责人: {{ item.ownerName || '未指定' }} | 状态: {{ formatters.formatQualityPlanStatus(item.status) }}</span>
          <span>质量标准: {{ item.qualityStandard || '暂无' }}</span>
          <span>验收规则: {{ item.acceptanceRule || '暂无' }}</span>
          <span>更新时间: {{ formatters.formatDateTimeText(item.updatedAt || item.createdAt) }}</span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" @click="actions.editQualityPlan(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="actions.removeQualityPlan(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无质量计划" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
