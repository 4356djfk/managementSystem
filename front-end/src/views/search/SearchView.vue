<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { searchGlobal } from '@/api/ops'

const router = useRouter()

const loading = ref(false)
const results = ref([])
const form = reactive({
  keyword: '',
  type: '',
})

const typeOptions = [
  { label: '全部', value: '' },
  { label: '项目', value: 'PROJECT' },
  { label: '任务', value: 'TASK' },
  { label: '风险', value: 'RISK' },
  { label: '经验教训', value: 'LESSON' },
]

function formatType(value) {
  return typeOptions.find((item) => item.value === value)?.label || value || '全部'
}

async function runSearch() {
  if (!form.keyword.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  try {
    const pageResult = await searchGlobal({
      keyword: form.keyword.trim(),
      type: form.type,
      page: 1,
      pageSize: 200,
    })
    results.value = Array.isArray(pageResult?.list) ? pageResult.list : []
  } catch (error) {
    ElMessage.error(error.message || '全局搜索失败')
  } finally {
    loading.value = false
  }
}

function openResult(item) {
  if (!item?.projectId) {
    ElMessage.info('该结果没有关联项目')
    return
  }

  router.push({
    name: 'project-editor',
    params: { projectId: String(item.projectId) },
  })
}
</script>

<template>
  <section class="search-view surface-card">
    <header class="page-header">
      <div>
        <div class="page-eyebrow">全局搜索</div>
        <h1>跨模块检索</h1>
        <p>统一搜索项目、任务、风险和经验教训，先开放基础查询和项目跳转。</p>
      </div>
    </header>

    <section class="search-panel">
      <label class="search-box" for="global-search-keyword">
        <span class="search-icon" aria-hidden="true" />
        <input
          id="global-search-keyword"
          v-model="form.keyword"
          type="text"
          placeholder="输入关键词，搜索项目 / 任务 / 风险 / 经验教训"
          @keyup.enter="runSearch"
        />
      </label>
      <el-select v-model="form.type" style="width: 160px">
        <el-option v-for="item in typeOptions" :key="item.value || 'ALL'" :label="item.label" :value="item.value" />
      </el-select>
      <el-button type="primary" :loading="loading" @click="runSearch">搜索</el-button>
    </section>

    <div v-if="!results.length" class="state-block">
      <strong>暂无搜索结果</strong>
      <span>输入关键词后开始检索。</span>
    </div>

    <section v-else class="result-list">
      <button
        v-for="item in results"
        :key="`${item.type}-${item.id}`"
        type="button"
        class="result-card"
        @click="openResult(item)"
      >
        <div class="result-card-head">
          <strong>{{ item.title || '-' }}</strong>
          <span class="type-pill">{{ formatType(item.type) }}</span>
        </div>
        <div class="result-meta">
          <span>项目: {{ item.projectName || '未关联项目' }}</span>
          <span>ID: {{ item.id }}</span>
        </div>
        <p>{{ item.summary || '无摘要' }}</p>
      </button>
    </section>
  </section>
</template>

<style scoped>
.search-view {
  min-height: 100vh;
  display: grid;
  align-content: start;
  gap: 20px;
  padding: 24px;
  border-radius: 0;
  background: #f3f3f3;
  border: 0;
  box-shadow: none;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
}

.page-eyebrow {
  color: #2563eb;
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 6px 0 10px;
  font-size: 2rem;
  line-height: 1.08;
}

.page-header p {
  margin: 0;
  color: #64748b;
}

.search-panel {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.search-box {
  width: min(560px, 100%);
  height: 46px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
  border: 1px solid #a3a3a3;
  background: #fff;
}

.search-icon {
  position: relative;
  width: 20px;
  height: 20px;
  flex: 0 0 20px;
}

.search-icon::before {
  content: '';
  position: absolute;
  inset: 0;
  border: 2px solid #2563eb;
  border-radius: 999px;
}

.search-icon::after {
  content: '';
  position: absolute;
  right: -4px;
  bottom: -1px;
  width: 10px;
  height: 2px;
  background: #2563eb;
  transform: rotate(45deg);
}

.search-box input {
  width: 100%;
  border: 0;
  outline: 0;
  background: transparent;
  color: var(--foreground);
}

.state-block {
  display: grid;
  gap: 6px;
  padding: 26px 24px;
  border: 1px solid #d4d4d8;
  background: #ffffff;
}

.state-block strong {
  font-size: 1rem;
}

.state-block span {
  color: #64748b;
}

.result-list {
  display: grid;
  gap: 12px;
}

.result-card {
  display: grid;
  gap: 10px;
  padding: 18px 20px;
  border: 1px solid #d4d4d8;
  background: #ffffff;
  text-align: left;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease;
}

.result-card:hover {
  border-color: #93c5fd;
  background: #f8fbff;
}

.result-card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
}

.result-card-head strong {
  font-size: 1rem;
  color: #111827;
}

.type-pill {
  display: inline-flex;
  align-items: center;
  min-height: 26px;
  padding: 0 10px;
  border-radius: 999px;
  color: #334155;
  background: #e2e8f0;
  font-size: 0.78rem;
  font-weight: 700;
}

.result-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 18px;
  color: #64748b;
  font-size: 0.88rem;
}

.result-card p {
  margin: 0;
  color: #334155;
  line-height: 1.65;
}

@media (max-width: 640px) {
  .search-view {
    padding: 18px 16px 24px;
  }

  .page-header h1 {
    font-size: 1.6rem;
  }

  .result-card-head {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>
