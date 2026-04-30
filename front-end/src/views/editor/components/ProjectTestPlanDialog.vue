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
  editingTestPlanId: {
    type: [String, Number],
    default: null,
  },
  testingLoading: {
    type: Boolean,
    default: false,
  },
  testPlans: {
    type: Array,
    default: () => [],
  },
  testPlanForm: {
    type: Object,
    required: true,
  },
  taskAssigneeOptions: {
    type: Array,
    default: () => [],
  },
  testPlanStatusOptions: {
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
    title="测试计划"
    width="1080px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingTestPlanId ? '编辑测试计划' : '新增测试计划' }}</h4>
        <div class="dialog-summary-text">当前共 {{ testPlans.length }} 条测试计划</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="计划标题">
          <el-input v-model="testPlanForm.title" placeholder="例如 回归测试计划、UAT 测试计划" />
        </el-form-item>
        <el-form-item label="版本号">
          <el-input v-model="testPlanForm.versionNo" placeholder="例如 V1.0.3" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="testPlanForm.ownerId" clearable filterable placeholder="选择负责人">
            <el-option
              v-for="option in taskAssigneeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="testPlanForm.status" style="width: 100%">
            <el-option
              v-for="option in testPlanStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="测试范围" class="scope-form-span">
          <el-input
            v-model="testPlanForm.scopeDesc"
            type="textarea"
            :rows="3"
            placeholder="说明本次测试覆盖的需求、场景、系统边界和限制条件"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetTestPlanForm()">重置表单</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="testingLoading"
          @click="actions.saveTestPlan"
        >
          {{ editingTestPlanId ? '保存修改' : '新增计划' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>计划列表</h4>
        <div class="dialog-summary-text">用于组织回归、冒烟、UAT 等不同阶段测试</div>
      </div>
      <div v-if="testPlans.length" class="simple-list">
        <div v-for="item in testPlans" :key="`test-plan-${item.id}`" class="simple-list-item">
          <strong>{{ item.title || `测试计划${item.id}` }}</strong>
          <span>版本: {{ item.versionNo || '-' }} | 负责人: {{ item.ownerName || '未指定' }} | 状态: {{ formatters.formatTestPlanStatus(item.status) }}</span>
          <span>{{ item.scopeDesc || '暂无测试范围说明' }}</span>
          <span>更新时间: {{ formatters.formatDateTimeText(item.updatedAt || item.createdAt) }}</span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" @click="actions.editTestPlan(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="actions.removeTestPlan(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无测试计划" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
