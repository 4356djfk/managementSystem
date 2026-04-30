<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  canEditProjectContent: {
    type: Boolean,
    default: false,
  },
  canEditTaskPlan: {
    type: Boolean,
    default: false,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  wbsConfig: {
    type: Object,
    required: true,
  },
  scheduleOptions: {
    type: Object,
    required: true,
  },
  scheduleModeOptions: {
    type: Array,
    default: () => [],
  },
  currentScheduleModeLabel: {
    type: String,
    default: '',
  },
  currentScheduleModeDescription: {
    type: String,
    default: '',
  },
  scheduleHolidaySummary: {
    type: String,
    default: '',
  },
  disabledHolidayPickerDate: {
    type: Function,
    required: true,
  },
  wbsTreeNodes: {
    type: Array,
    default: () => [],
  },
  formatWbsCode: {
    type: Function,
    required: true,
  },
  isWbsTreeExpanded: {
    type: Function,
    required: true,
  },
})

const emit = defineEmits([
  'update:visible',
  'cancel',
  'save',
  'rerun-project-schedule',
  'resequence-wbs-codes',
  'toggle-wbs-tree-node',
])

function updateVisible(value) {
  emit('update:visible', value)
}

function cancelDialog() {
  emit('cancel')
}

function saveDialog() {
  emit('save')
}

function rerunProjectSchedule() {
  emit('rerun-project-schedule')
}

function resequenceWbsCodes() {
  emit('resequence-wbs-codes')
}

function toggleWbsTreeNode(localId) {
  emit('toggle-wbs-tree-node', localId)
}

function flattenWbsTree(nodes, depth, result) {
  nodes.forEach((node) => {
    const children = Array.isArray(node?.children) ? node.children : []
    result.push({
      ...node,
      depth,
      hasChildren: children.length > 0,
    })
    if (children.length && props.isWbsTreeExpanded(node.localId)) {
      flattenWbsTree(children, depth + 1, result)
    }
  })
}

const visibleWbsNodes = computed(() => {
  const result = []
  flattenWbsTree(Array.isArray(props.wbsTreeNodes) ? props.wbsTreeNodes : [], 0, result)
  return result
})
</script>

<template>
  <el-dialog
    :model-value="visible"
    title="WBS / 排程"
    width="760px"
    @update:model-value="updateVisible"
  >
    <div class="info-section">
      <h4>WBS 编码规则</h4>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="前缀">
          <el-input
            v-model="wbsConfig.prefix"
            :disabled="!canEditProjectContent"
            placeholder="例如 WBS / P / PRJ"
          />
        </el-form-item>
        <el-form-item label="分隔符">
          <el-input
            v-model="wbsConfig.separator"
            :disabled="!canEditProjectContent"
            maxlength="3"
            placeholder="例如 ."
          />
        </el-form-item>
        <el-form-item label="层级位数">
          <el-input-number
            v-model="wbsConfig.paddingWidth"
            :disabled="!canEditProjectContent"
            :min="1"
            :max="6"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="示例" class="scope-form-span">
          <el-input :model-value="formatWbsCode([1, 2, 3]) || '1.2.3'" disabled />
        </el-form-item>
      </el-form>
    </div>

    <div class="info-section">
      <div class="section-header-inline">
        <h4>排程规则</h4>
        <div class="baseline-actions">
          <div class="dialog-summary-text">当前口径：{{ currentScheduleModeLabel }}</div>
          <el-button :disabled="!canEditTaskPlan" size="small" @click="rerunProjectSchedule">重新推排</el-button>
        </div>
      </div>
      <el-form label-width="88px" class="scope-form-grid">
        <el-form-item label="工期口径" class="scope-form-span">
          <el-radio-group v-model="scheduleOptions.scheduleMode" :disabled="!canEditProjectContent">
            <el-radio-button
              v-for="option in scheduleModeOptions"
              :key="`schedule-mode-${option.value}`"
              :label="option.value"
            >
              {{ option.label }}
            </el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="额外假日" class="scope-form-span">
          <el-date-picker
            v-model="scheduleOptions.holidayDates"
            type="dates"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            style="width: 100%"
            :disabled="!canEditProjectContent"
            :disabled-date="disabledHolidayPickerDate"
            placeholder="可多选额外非工作日"
          />
        </el-form-item>
        <el-form-item label="规则说明" class="scope-form-span">
          <el-input :model-value="currentScheduleModeDescription" type="textarea" :rows="2" disabled />
        </el-form-item>
        <el-form-item label="配置摘要" class="scope-form-span">
          <el-input :model-value="scheduleHolidaySummary" type="textarea" :rows="2" disabled />
        </el-form-item>
      </el-form>
    </div>

    <div class="info-section">
      <div class="section-header-inline">
        <h4>当前任务 WBS</h4>
        <el-button :disabled="!canEditProjectContent" @click="resequenceWbsCodes">重排 WBS 编码</el-button>
      </div>
      <div v-if="visibleWbsNodes.length" class="wbs-tree">
        <div
          v-for="node in visibleWbsNodes"
          :key="`wbs-tree-node-${node.localId}`"
          class="wbs-tree-row"
          :style="{ paddingLeft: `${node.depth * 16}px` }"
        >
          <button
            v-if="node.hasChildren"
            type="button"
            class="wbs-tree-toggle"
            @click="toggleWbsTreeNode(node.localId)"
          >
            {{ isWbsTreeExpanded(node.localId) ? '-' : '+' }}
          </button>
          <span v-else class="wbs-tree-spacer" />
          <strong>{{ node.wbsCode || '-' }}</strong>
          <span>{{ node.name || '未命名任务' }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无可生成 WBS 的任务" />
    </div>

    <template #footer>
      <el-button @click="cancelDialog">{{ canEditProjectContent ? '取消' : '关闭' }}</el-button>
      <el-button v-if="canEditProjectContent" type="primary" :loading="loading" @click="saveDialog">保存规则</el-button>
    </template>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
