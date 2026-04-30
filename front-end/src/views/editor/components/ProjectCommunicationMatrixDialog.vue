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
  editingCommunicationMatrixId: {
    type: [String, Number],
    default: null,
  },
  communicationLoading: {
    type: Boolean,
    default: false,
  },
  communicationMatrixList: {
    type: Array,
    default: () => [],
  },
  communicationMatrixForm: {
    type: Object,
    required: true,
  },
  communicationChannelOptions: {
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
    title="沟通安排"
    width="1080px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <div class="section-header-inline">
        <h4>{{ editingCommunicationMatrixId ? '编辑沟通安排' : '新增沟通安排' }}</h4>
        <div class="dialog-summary-text">当前共 {{ communicationMatrixList.length }} 条沟通规则</div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="发送方角色">
          <el-input v-model="communicationMatrixForm.senderRole" placeholder="例如 项目经理、实施负责人、客户代表" />
        </el-form-item>
        <el-form-item label="接收方角色">
          <el-input v-model="communicationMatrixForm.receiverRole" placeholder="例如 开发团队、测试团队、管理层" />
        </el-form-item>
        <el-form-item label="沟通渠道">
          <el-select v-model="communicationMatrixForm.channel" style="width: 100%">
            <el-option
              v-for="option in communicationChannelOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="沟通频率">
          <el-input v-model="communicationMatrixForm.frequency" placeholder="例如 每日、每周、按里程碑、按需" />
        </el-form-item>
        <el-form-item label="沟通主题" class="scope-form-span">
          <el-input v-model="communicationMatrixForm.topic" placeholder="例如 项目进度同步、风险升级、上线审批、客户例会" />
        </el-form-item>
      </el-form>
      <div class="dialog-actions-inline">
        <el-button @click="actions.resetCommunicationMatrixForm()">重置表单</el-button>
        <el-button
          v-if="canEditProjectContent"
          type="primary"
          :loading="communicationLoading"
          @click="actions.saveCommunicationMatrix"
        >
          {{ editingCommunicationMatrixId ? '保存修改' : '新增规则' }}
        </el-button>
      </div>
    </div>
    <div class="info-section">
      <div class="section-header-inline">
        <h4>沟通安排列表</h4>
        <div class="dialog-summary-text">明确谁在何时通过什么渠道向谁同步什么信息</div>
      </div>
      <div v-if="communicationMatrixList.length" class="simple-list">
        <div v-for="item in communicationMatrixList" :key="`communication-matrix-${item.id}`" class="simple-list-item">
          <strong>{{ item.senderRole || '-' }} -> {{ item.receiverRole || '-' }}</strong>
          <span>渠道: {{ formatters.formatCommunicationChannel(item.channel) }} | 频率: {{ item.frequency || '未设置' }}</span>
          <span>主题: {{ item.topic || '未设置' }}</span>
          <span>更新时间: {{ formatters.formatDateTimeText(item.updatedAt || item.createdAt) }}</span>
          <div v-if="canEditProjectContent" class="baseline-actions">
            <el-button size="small" @click="actions.editCommunicationMatrix(item)">编辑</el-button>
            <el-button size="small" type="danger" plain @click="actions.removeCommunicationMatrix(item)">删除</el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无沟通安排" />
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
