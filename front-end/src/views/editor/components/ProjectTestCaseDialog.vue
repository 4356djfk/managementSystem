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
  editingTestCaseId: {
    type: [String, Number],
    default: null,
  },
  testingLoading: {
    type: Boolean,
    default: false,
  },
  testCases: {
    type: Array,
    default: () => [],
  },
  testCaseForm: {
    type: Object,
    required: true,
  },
  testPlanOptions: {
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
  testCaseStatusOptions: {
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
    title="测试用例"
    width="1080px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingTestCaseId ? '编辑测试用例' : '新增测试用例' }}</h4>
        <div class="dialog-summary-text">覆盖需求、任务和执行结果的闭环记录</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="所属计划">
          <el-select v-model="testCaseForm.testPlanId" clearable filterable placeholder="可选，归属到某个测试计划">
            <el-option
              v-for="option in testPlanOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联需求">
          <el-select v-model="testCaseForm.requirementId" clearable filterable placeholder="可选，关联需求">
            <el-option
              v-for="option in requirementOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="关联任务">
          <el-select v-model="testCaseForm.taskId" clearable filterable placeholder="可选，关联任务">
            <el-option
              v-for="option in riskTaskOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="测试人">
          <el-select v-model="testCaseForm.testerId" clearable filterable placeholder="选择测试人">
            <el-option
              v-for="option in taskAssigneeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="用例标题" class="scope-form-span">
          <el-input v-model="testCaseForm.title" placeholder="例如 正常登录成功、余额不足下单失败" />
        </el-form-item>
        <el-form-item label="前置条件" class="scope-form-span">
          <el-input
            v-model="testCaseForm.precondition"
            type="textarea"
            :rows="2"
            placeholder="记录环境、账号、数据准备或前置开关"
          />
        </el-form-item>
        <el-form-item label="执行步骤" class="scope-form-span">
          <el-input
            v-model="testCaseForm.steps"
            type="textarea"
            :rows="3"
            placeholder="建议按 1.2.3. 的形式描述操作步骤"
          />
        </el-form-item>
        <el-form-item label="预期结果" class="scope-form-span">
          <el-input
            v-model="testCaseForm.expectedResult"
            type="textarea"
            :rows="2"
            placeholder="记录系统应有表现和验收口径"
          />
        </el-form-item>
        <el-form-item label="实际结果" class="scope-form-span">
          <el-input
            v-model="testCaseForm.actualResult"
            type="textarea"
            :rows="2"
            placeholder="执行后记录实际结果；未执行可留空"
          />
        </el-form-item>
        <el-form-item label="执行状态">
          <el-select v-model="testCaseForm.executionStatus" style="width: 100%">
            <el-option
              v-for="option in testCaseStatusOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="执行时间">
          <el-date-picker
            v-model="testCaseForm.executedAt"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetTestCaseForm()">重置表单</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="testingLoading"
          @click="actions.saveTestCase"
        >
          {{ editingTestCaseId ? '保存修改' : '新增用例' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>用例列表</h4>
        <div class="dialog-summary-text">用例执行失败后可直接在缺陷页签登记问题</div>
      </div>
      <div v-if="testCases.length" class="simple-list">
        <div v-for="item in testCases" :key="`test-case-${item.id}`" class="simple-list-item">
          <strong>{{ item.caseCode || '-' }} | {{ item.title || `测试用例${item.id}` }}</strong>
          <span>计划: {{ item.testPlanTitle || '未归属计划' }} | 状态: {{ formatters.formatTestCaseStatus(item.executionStatus) }} | 测试人: {{ item.testerName || '未指定' }}</span>
          <span>需求: {{ item.requirementTitle || '未关联需求' }} | 任务: {{ item.taskName || '未关联任务' }}</span>
          <span>预期: {{ item.expectedResult || '暂无预期结果' }}</span>
          <span>实际: {{ item.actualResult || '暂无实际结果' }}</span>
          <span>执行时间: {{ formatters.formatDateTimeText(item.executedAt) }}</span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" @click="actions.editTestCase(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="actions.removeTestCase(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无测试用例" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
