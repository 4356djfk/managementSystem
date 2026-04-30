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
  editingDefectId: {
    type: [String, Number],
    default: null,
  },
  testingLoading: {
    type: Boolean,
    default: false,
  },
  defectList: {
    type: Array,
    default: () => [],
  },
  defectForm: {
    type: Object,
    required: true,
  },
  testCaseOptions: {
    type: Array,
    default: () => [],
  },
  requirementOptions: {
    type: Array,
    default: () => [],
  },
  riskTaskOptions: {
    type: Array,
    default: () => [],
  },
  taskAssigneeOptions: {
    type: Array,
    default: () => [],
  },
  defectSeverityOptions: {
    type: Array,
    default: () => [],
  },
  defectPriorityOptions: {
    type: Array,
    default: () => [],
  },
  defectStatusOptions: {
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
    title="缺陷管理"
    width="1080px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingDefectId ? '编辑缺陷' : '新增缺陷' }}</h4>
        <div class="dialog-summary-text">用于记录发现、分派、修复和关闭过程</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="来源用例">
          <el-select v-model="defectForm.testCaseId" clearable filterable placeholder="可选，选择缺陷来源用例">
            <el-option
              v-for="option in testCaseOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联需求">
          <el-select v-model="defectForm.requirementId" clearable filterable placeholder="可选，关联需求">
            <el-option
              v-for="option in requirementOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联任务">
          <el-select v-model="defectForm.taskId" clearable filterable placeholder="可选，关联任务">
            <el-option
              v-for="option in riskTaskOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="报告人">
          <el-select v-model="defectForm.reporterId" clearable filterable placeholder="留空则默认为当前用户">
            <el-option
              v-for="option in taskAssigneeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="处理人">
          <el-select v-model="defectForm.assigneeId" clearable filterable placeholder="选择处理人">
            <el-option
              v-for="option in taskAssigneeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="缺陷标题" class="scope-form-span">
          <el-input v-model="defectForm.title" placeholder="例如 提交订单后页面白屏、审批流节点丢失" />
        </el-form-item>
        <el-form-item label="严重级别">
          <el-select v-model="defectForm.severity" style="width: 100%">
            <el-option
              v-for="option in defectSeverityOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="defectForm.priority" style="width: 100%">
            <el-option
              v-for="option in defectPriorityOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="defectForm.status" style="width: 100%">
            <el-option
              v-for="option in defectStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="缺陷描述" class="scope-form-span">
          <el-input
            v-model="defectForm.description"
            type="textarea"
            :rows="3"
            placeholder="记录复现步骤、影响范围、环境信息和出现频率"
          />
        </el-form-item>
        <el-form-item label="处理结论" class="scope-form-span">
          <el-input
            v-model="defectForm.resolution"
            type="textarea"
            :rows="2"
            placeholder="记录修复方案、回归结果或关闭说明"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetDefectForm()">重置表单</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="testingLoading"
          @click="actions.saveDefect"
        >
          {{ editingDefectId ? '保存修改' : '新增缺陷' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>缺陷列表</h4>
        <div class="dialog-summary-text">优先级和严重级别分开维护，便于排期和评估影响</div>
      </div>
      <div v-if="defectList.length" class="simple-list">
        <div v-for="item in defectList" :key="`defect-${item.id}`" class="simple-list-item">
          <strong>{{ item.defectCode || '-' }} | {{ item.title || `缺陷${item.id}` }}</strong>
          <span>状态: {{ formatters.formatDefectStatus(item.status) }} | 严重级别: {{ formatters.formatDefectSeverity(item.severity) }} | 优先级: {{ item.priority || '-' }}</span>
          <span>来源用例: {{ item.testCaseTitle || '未关联用例' }} | 需求: {{ item.requirementTitle || '未关联需求' }}</span>
          <span>报告人: {{ item.reporterName || '未指定' }} | 处理人: {{ item.assigneeName || '未指定' }}</span>
          <span>描述: {{ item.description || '暂无描述' }}</span>
          <span>结论: {{ item.resolution || '暂无处理结论' }}</span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" @click="actions.editDefect(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="actions.removeDefect(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无缺陷记录" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
