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
  editingQualityActivityId: {
    type: [String, Number],
    default: null,
  },
  qualityLoading: {
    type: Boolean,
    default: false,
  },
  qualityActivities: {
    type: Array,
    default: () => [],
  },
  qualityActivityForm: {
    type: Object,
    required: true,
  },
  qualityPlanOptions: {
    type: Array,
    default: () => [],
  },
  qualityActivityTypeOptions: {
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
    title="质量活动"
    width="980px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingQualityActivityId ? '编辑质量活动' : '新增质量活动' }}</h4>
        <div class="dialog-summary-text">维护评审、测试、审计等执行动作</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="关联计划">
          <el-select
            v-model="qualityActivityForm.qualityPlanId"
            clearable
            filterable
            placeholder="可选，挂到一条质量计划下"
          >
            <el-option
              v-for="option in qualityPlanOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="活动名称">
          <el-input v-model="qualityActivityForm.activityName" placeholder="例如 需求评审、提测冒烟、上线审计" />
        </el-form-item>
        <el-form-item label="活动类型">
          <el-select v-model="qualityActivityForm.activityType" style="width: 100%">
            <el-option
              v-for="option in qualityActivityTypeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="qualityActivityForm.ownerId" clearable filterable placeholder="选择负责人">
            <el-option
              v-for="option in taskAssigneeOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="计划时间">
          <el-date-picker
            v-model="qualityActivityForm.plannedDate"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="实际时间">
          <el-date-picker
            v-model="qualityActivityForm.actualDate"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="执行结果" class="scope-form-span">
          <el-input
            v-model="qualityActivityForm.result"
            type="textarea"
            :rows="2"
            placeholder="记录发现问题、结论、整改意见或通过情况"
          />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetQualityActivityForm()">重置表单</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="qualityLoading"
          @click="actions.saveQualityActivity"
        >
          {{ editingQualityActivityId ? '保存修改' : '新增活动' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>活动列表</h4>
        <div class="dialog-summary-text">计划与实际执行时间可用于后续测试/审计扩展</div>
      </div>
      <div v-if="qualityActivities.length" class="simple-list">
        <div v-for="item in qualityActivities" :key="`quality-activity-${item.id}`" class="simple-list-item">
          <strong>{{ item.activityName || `质量活动${item.id}` }}</strong>
          <span>类型: {{ formatters.formatQualityActivityType(item.activityType) }} | 计划: {{ item.qualityPlanTitle || '未关联计划' }}</span>
          <span>负责人: {{ item.ownerName || '未指定' }} | 计划时间: {{ formatters.formatDateTimeText(item.plannedDate) }}</span>
          <span>实际时间: {{ formatters.formatDateTimeText(item.actualDate) }}</span>
          <span>结果: {{ item.result || '暂无结果' }}</span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" @click="actions.editQualityActivity(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="actions.removeQualityActivity(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无质量活动" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
