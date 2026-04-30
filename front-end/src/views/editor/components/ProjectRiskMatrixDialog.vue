<script setup>
defineProps({
  visible: {
    type: Boolean,
    default: false,
  },
  riskMatrix: {
    type: Object,
    required: true,
  },
  helpers: {
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
    title="风险矩阵"
    width="940px"
    @update:model-value="updateVisible"
  >
    <div class="dashboard-panels">
      <div class="dashboard-card"><span>高风险</span><strong>{{ riskMatrix?.highCount ?? 0 }}</strong></div>
      <div class="dashboard-card"><span>严重风险</span><strong>{{ riskMatrix?.criticalCount ?? 0 }}</strong></div>
      <div class="dashboard-card"><span>风险总数</span><strong>{{ riskMatrix?.levels?.length ?? 0 }}</strong></div>
    </div>
    <div class="info-section">
      <h4>5 x 5 风险矩阵</h4>
      <div class="risk-matrix-board">
        <div class="risk-matrix-corner">概率 \ 影响</div>
        <div v-for="impact in 5" :key="`impact-head-${impact}`" class="risk-matrix-header">{{ impact }}</div>
        <template v-for="probability in [5, 4, 3, 2, 1]" :key="`row-${probability}`">
          <div class="risk-matrix-header">{{ probability }}</div>
          <div
            v-for="impact in 5"
            :key="`cell-${probability}-${impact}`"
            :class="helpers.getMatrixCellClass(probability, impact)"
          >
            <div class="risk-matrix-count">{{ helpers.getMatrixCellItems(probability, impact).length }}</div>
            <div v-if="helpers.getMatrixCellItems(probability, impact).length" class="risk-matrix-names">
              <span
                v-for="item in helpers.getMatrixCellItems(probability, impact)"
                :key="`matrix-risk-${item.riskId}`"
              >
                {{ item.riskName || `风险${item.riskId}` }}
              </span>
            </div>
          </div>
        </template>
      </div>
    </div>
  </el-dialog>
</template>

<style scoped src="./editor-dialogs.shared.css"></style>
