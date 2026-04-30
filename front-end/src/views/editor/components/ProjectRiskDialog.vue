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
  editingRiskId: {
    type: [String, Number],
    default: null,
  },
  riskForm: {
    type: Object,
    required: true,
  },
  riskStatusForm: {
    type: Object,
    required: true,
  },
  riskLoading: {
    type: Boolean,
    default: false,
  },
  riskTaskOptions: {
    type: Array,
    default: () => [],
  },
  riskStatusOptions: {
    type: Array,
    default: () => [],
  },
  riskList: {
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
    title="风险登记册"
    width="980px"
    @update:model-value="updateVisible"
  >
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
          <el-input :model-value="formatters.formatRiskLevel(riskForm.level)" disabled />
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
          <el-input
            v-model="riskForm.description"
            type="textarea"
            :rows="3"
            placeholder="描述触发条件、影响范围或成因"
          />
        </el-form-item>
        <el-form-item label="应对策略" class="scope-form-span">
          <el-input
            v-model="riskForm.responseStrategy"
            type="textarea"
            :rows="3"
            placeholder="记录规避、减轻、转移或接受策略"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetRiskForm">重置</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="riskLoading"
          @click="actions.saveRisk"
        >
          {{ editingRiskId ? '保存修改' : '新增风险' }}
        </el-button>
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
            <el-option
              v-for="option in riskStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="说明" class="scope-form-span">
          <el-input
            v-model="riskStatusForm.comment"
            type="textarea"
            :rows="2"
            placeholder="记录本次状态变更说明"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="riskLoading"
          @click="actions.saveRiskStatus"
        >
          更新状态
        </el-button>
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
          <span>等级: {{ formatters.formatRiskLevel(item.level) }} | 状态: {{ formatters.formatRiskStatus(item.status) }}</span>
          <span>概率: {{ item.probability || 0 }} | 影响: {{ item.impact || 0 }}</span>
          <span>关联任务: {{ item.taskName || '未绑定任务' }}</span>
          <span>关联阶段: {{ item.phaseName || '未设置阶段' }}</span>
          <span>应对策略: {{ item.responseStrategy || '未填写' }}</span>
          <span>识别时间: {{ item.identifiedAt ? String(item.identifiedAt).replace('T', ' ').slice(0, 19) : '-' }}</span>
          <div class="baseline-actions">
            <el-button v-if="canEditProjectContent" size="small" @click="actions.populateRiskForm(item)">编辑</el-button>
            <el-button v-if="canEditProjectContent" size="small" @click="actions.startRiskStatusEdit(item)">改状态</el-button>
            <el-button v-if="canEditProjectContent" size="small" type="danger" plain @click="actions.removeRisk(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无风险数据" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
