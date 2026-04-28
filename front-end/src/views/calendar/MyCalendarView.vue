<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

import { getMyCalendar } from '@/api/editor'
import { getProjects } from '@/api/projects'

const router = useRouter()

const WEEKDAY_LABELS = ['一', '二', '三', '四', '五', '六', '日']
const now = new Date()

const loading = ref(false)
const events = ref([])
const projects = ref([])
const visibleMonth = ref(startOfMonth(now))
const selectedDateKey = ref(formatDateKey(now))

const projectNameMap = computed(() => {
  const map = new Map()
  projects.value.forEach((item) => {
    map.set(String(item.id), item.name)
  })
  return map
})

const eventsByDate = computed(() => {
  const map = new Map()

  events.value
    .slice()
    .sort((left, right) => eventTimeValue(left) - eventTimeValue(right))
    .forEach((item) => {
      const date = eventPrimaryDate(item)
      const key = date ? formatDateKey(date) : 'unscheduled'
      if (!map.has(key)) {
        map.set(key, [])
      }
      map.get(key).push(item)
    })

  return map
})

const currentMonthEvents = computed(() => (
  events.value.filter((item) => {
    const date = eventPrimaryDate(item)
    return date && isSameMonth(date, visibleMonth.value)
  })
))

const eventSummary = computed(() => {
  const total = events.value.length
  const taskCount = events.value.filter((item) => item.eventType === 'TASK').length
  const milestoneCount = events.value.filter((item) => item.eventType === 'MILESTONE').length
  const monthCount = currentMonthEvents.value.length

  return [
    { label: '全部事件', value: total, hint: '我的全部安排' },
    { label: '本月安排', value: monthCount, hint: currentMonthLabel.value },
    { label: '任务日程', value: taskCount, hint: '任务型事件' },
    { label: '里程碑', value: milestoneCount, hint: '关键节点' },
  ]
})

const currentMonthLabel = computed(() => new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: 'long',
}).format(visibleMonth.value))

const calendarDays = computed(() => {
  const monthStart = startOfMonth(visibleMonth.value)
  const monthOffset = getMondayFirstWeekday(monthStart)
  const gridStart = addDays(monthStart, -monthOffset)
  const todayKey = formatDateKey(new Date())

  return Array.from({ length: 42 }, (_, index) => {
    const date = addDays(gridStart, index)
    const key = formatDateKey(date)
    const dayEvents = eventsByDate.value.get(key) || []

    return {
      key,
      date,
      dayNumber: date.getDate(),
      isCurrentMonth: isSameMonth(date, monthStart),
      isToday: key === todayKey,
      isSelected: key === selectedDateKey.value,
      events: dayEvents,
      previewEvents: dayEvents.slice(0, 1),
      moreCount: Math.max(0, dayEvents.length - 1),
    }
  })
})

const calendarWeeks = computed(() => {
  const weeks = []
  for (let index = 0; index < calendarDays.value.length; index += 7) {
    weeks.push(calendarDays.value.slice(index, index + 7))
  }
  return weeks
})

const selectedDate = computed(() => parseDateKey(selectedDateKey.value))

const selectedDateLabel = computed(() => new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long',
}).format(selectedDate.value))

const selectedDayEvents = computed(() => (
  (eventsByDate.value.get(selectedDateKey.value) || []).slice()
))

function startOfMonth(date) {
  return new Date(date.getFullYear(), date.getMonth(), 1)
}

function addDays(date, days) {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate() + days)
}

function addMonths(date, months) {
  return new Date(date.getFullYear(), date.getMonth() + months, 1)
}

function daysInMonth(date) {
  return new Date(date.getFullYear(), date.getMonth() + 1, 0).getDate()
}

function getMondayFirstWeekday(date) {
  return (date.getDay() + 6) % 7
}

function pad(value) {
  return String(value).padStart(2, '0')
}

function formatDateKey(date) {
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

function parseDateKey(value) {
  const [year, month, day] = String(value || '').split('-').map(Number)
  if (!year || !month || !day) {
    return new Date()
  }
  return new Date(year, month - 1, day)
}

function isSameMonth(left, right) {
  return left.getFullYear() === right.getFullYear() && left.getMonth() === right.getMonth()
}

function eventPrimaryDate(event) {
  const raw = event?.startTime || event?.endTime
  if (!raw) return null

  const date = new Date(raw)
  if (Number.isNaN(date.getTime())) return null
  return date
}

function eventTimeValue(event) {
  const date = eventPrimaryDate(event)
  return date ? date.getTime() : Number.MAX_SAFE_INTEGER
}

function formatEventType(value) {
  const map = {
    TASK: '任务',
    MILESTONE: '里程碑',
  }
  return map[value] || value || '事件'
}

function eventTypeClass(value) {
  const map = {
    TASK: 'event-tag-task',
    MILESTONE: 'event-tag-milestone',
  }
  return map[value] || 'event-tag-neutral'
}

function resolveProjectName(projectId) {
  if (!projectId) return '未关联项目'
  return projectNameMap.value.get(String(projectId)) || `项目 #${projectId}`
}

function formatEventTime(value, withDate = false) {
  if (!value) return '未设置'

  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '未设置'

  return new Intl.DateTimeFormat('zh-CN', withDate
    ? {
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      }
    : {
        hour: '2-digit',
        minute: '2-digit',
        hour12: false,
      }).format(date)
}

function formatEventRange(event) {
  if (!event?.startTime && !event?.endTime) {
    return '未设置时间'
  }

  if (event?.startTime && event?.endTime) {
    const startDate = eventPrimaryDate({ startTime: event.startTime })
    const endDate = eventPrimaryDate({ startTime: event.endTime })
    const sameDay = startDate && endDate && formatDateKey(startDate) === formatDateKey(endDate)

    if (sameDay) {
      return `${formatEventTime(event.startTime)} - ${formatEventTime(event.endTime)}`
    }

    return `${formatEventTime(event.startTime, true)} - ${formatEventTime(event.endTime, true)}`
  }

  return event.startTime
    ? `${formatEventTime(event.startTime, true)} 开始`
    : `${formatEventTime(event.endTime, true)} 截止`
}

function openProject(event) {
  if (!event?.projectId) {
    ElMessage.info('该事件没有关联项目')
    return
  }

  router.push({
    name: 'project-editor',
    params: { projectId: String(event.projectId) },
  })
}

function selectDay(day) {
  selectedDateKey.value = day.key
  visibleMonth.value = startOfMonth(day.date)
}

function activateDay(day) {
  selectDay(day)
}

function shiftMonth(offset) {
  const targetMonth = addMonths(visibleMonth.value, offset)
  const selectedDate = parseDateKey(selectedDateKey.value)
  const targetDay = Math.min(selectedDate.getDate(), daysInMonth(targetMonth))
  const targetDate = new Date(targetMonth.getFullYear(), targetMonth.getMonth(), targetDay)

  visibleMonth.value = startOfMonth(targetMonth)
  selectedDateKey.value = formatDateKey(targetDate)
}

function goToToday() {
  const today = new Date()
  visibleMonth.value = startOfMonth(today)
  selectedDateKey.value = formatDateKey(today)
}

async function loadCalendar() {
  loading.value = true
  try {
    const [calendar, projectResult] = await Promise.all([
      getMyCalendar(),
      getProjects({ page: 1, pageSize: 200 }),
    ])
    events.value = Array.isArray(calendar) ? calendar : []
    projects.value = Array.isArray(projectResult?.list) ? projectResult.list : []
  } catch (error) {
    ElMessage.error(error.message || '个人日历加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(loadCalendar)
</script>

<template>
  <section class="calendar-view surface-card">
    <header class="page-header">
      <div class="page-header-main">
        <div class="page-eyebrow">我的日历</div>
        <h1>个人日程</h1>
        <p>按真实月历查看我负责的任务和里程碑，点选日期后可在右侧直接查看当天事项并跳转到项目。</p>
      </div>
      <div class="header-actions">
        <el-button @click="goToToday">回到今天</el-button>
        <el-button :loading="loading" type="primary" @click="loadCalendar">刷新</el-button>
      </div>
    </header>

    <section class="summary-grid">
      <article v-for="item in eventSummary" :key="item.label" class="summary-card">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
        <small>{{ item.hint }}</small>
      </article>
    </section>

    <section class="calendar-shell">
      <div class="calendar-board">
        <div class="calendar-board-head">
          <div>
            <strong>{{ currentMonthLabel }}</strong>
            <span>{{ currentMonthEvents.length ? `本月共 ${currentMonthEvents.length} 项安排` : '本月暂时没有安排' }}</span>
          </div>
          <div class="month-switcher">
            <button type="button" class="month-switcher-btn" aria-label="上个月" @click="shiftMonth(-1)">‹</button>
            <button type="button" class="month-switcher-btn" aria-label="下个月" @click="shiftMonth(1)">›</button>
          </div>
        </div>

        <div class="calendar-legend">
          <span class="legend-item"><i class="legend-dot legend-task" />任务</span>
          <span class="legend-item"><i class="legend-dot legend-milestone" />里程碑</span>
        </div>

        <div class="calendar-weekdays">
          <span v-for="item in WEEKDAY_LABELS" :key="item">{{ item }}</span>
        </div>

        <div class="calendar-grid">
          <template v-for="(week, weekIndex) in calendarWeeks" :key="`week-${weekIndex}`">
            <article
              v-for="day in week"
              :key="day.key"
              class="calendar-day"
              :class="{
                'is-current-month': day.isCurrentMonth,
                'is-other-month': !day.isCurrentMonth,
                'is-today': day.isToday,
                'is-selected': day.isSelected,
              }"
              role="button"
              tabindex="0"
              :aria-label="`${day.key}，共 ${day.events.length} 项安排`"
              @click="selectDay(day)"
              @keydown.enter.prevent="activateDay(day)"
              @keydown.space.prevent="activateDay(day)"
            >
              <div class="calendar-day-head">
                <span class="calendar-day-number">{{ day.dayNumber }}</span>
                <span v-if="day.events.length" class="calendar-day-count">{{ day.events.length }}</span>
              </div>

              <div class="calendar-day-events">
                <button
                  v-for="item in day.previewEvents"
                  :key="`${day.key}-${item.eventType}-${item.id}`"
                  type="button"
                  class="day-event-chip"
                  :class="eventTypeClass(item.eventType)"
                  @click.stop="openProject(item)"
                >
                  <span class="day-event-time">{{ formatEventTime(item.startTime) }}</span>
                  <strong>{{ item.title || '未命名事件' }}</strong>
                </button>
                <span v-if="day.moreCount > 0" class="more-events">+{{ day.moreCount }} 更多</span>
              </div>
            </article>
          </template>
        </div>
      </div>

      <aside class="agenda-panel">
        <div class="agenda-panel-head">
          <div>
            <span class="agenda-label">当日事项</span>
            <strong>{{ selectedDateLabel }}</strong>
          </div>
          <span class="agenda-total">{{ selectedDayEvents.length }} 项</span>
        </div>

        <div v-if="loading" class="agenda-state">
          <strong>正在同步日历</strong>
          <span>请稍候。</span>
        </div>

        <div v-else-if="selectedDayEvents.length" class="agenda-list">
          <article
            v-for="item in selectedDayEvents"
            :key="`${selectedDateKey}-${item.eventType}-${item.id}`"
            class="agenda-card"
          >
            <div class="agenda-card-head">
              <strong>{{ item.title || '未命名事件' }}</strong>
              <span class="agenda-type" :class="eventTypeClass(item.eventType)">{{ formatEventType(item.eventType) }}</span>
            </div>
            <div class="agenda-card-meta">
              <span>{{ resolveProjectName(item.projectId) }}</span>
              <span>{{ formatEventRange(item) }}</span>
            </div>
            <div class="agenda-card-actions">
              <el-button size="small" type="primary" plain @click="openProject(item)">打开项目</el-button>
            </div>
          </article>
        </div>

        <div v-else class="agenda-state">
          <strong>当天暂无安排</strong>
          <span>可以切换月份或点选其他日期查看事项。</span>
        </div>
      </aside>
    </section>
  </section>
</template>

<style scoped>
.calendar-view {
  --calendar-bg: #f5f7fb;
  --calendar-surface: #ffffff;
  --calendar-border: #d8e0ee;
  --calendar-border-strong: #a8b6cf;
  --calendar-text: #17314f;
  --calendar-muted: #61748f;
  --calendar-soft: #edf3fb;
  --calendar-primary: #2f6fe4;
  --calendar-primary-soft: #eaf2ff;
  --calendar-task: #2f6fe4;
  --calendar-task-soft: #eaf2ff;
  --calendar-milestone: #cf7a18;
  --calendar-milestone-soft: #fff2df;
  height: 100vh;
  height: 100dvh;
  box-sizing: border-box;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 14px;
  padding: 18px 20px 20px;
  border-radius: 0;
  border: 0;
  background:
    radial-gradient(circle at top right, rgba(47, 111, 228, 0.08), transparent 26%),
    linear-gradient(180deg, #fbfcff 0%, var(--calendar-bg) 100%);
  box-shadow: none;
  overflow: hidden;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
}

.page-header-main {
  display: grid;
  gap: 4px;
  max-width: 760px;
}

.page-eyebrow {
  color: var(--calendar-primary);
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.page-header h1 {
  margin: 0;
  color: var(--calendar-text);
  font-size: 1.72rem;
  line-height: 1.04;
}

.page-header p {
  margin: 0;
  color: var(--calendar-muted);
  font-size: 0.93rem;
  line-height: 1.45;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.summary-card {
  display: grid;
  gap: 4px;
  padding: 12px;
  border: 1px solid var(--calendar-border);
  background: rgba(255, 255, 255, 0.88);
  box-shadow: 0 12px 24px rgba(23, 49, 79, 0.04);
}

.summary-card span {
  color: var(--calendar-muted);
  font-size: 12px;
}

.summary-card strong {
  color: var(--calendar-text);
  font-size: 1.12rem;
  line-height: 1.2;
}

.summary-card small {
  color: #8a98ae;
  font-size: 12px;
}

.calendar-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.8fr) minmax(260px, 0.78fr);
  gap: 14px;
  min-height: 0;
}

.calendar-board,
.agenda-panel {
  display: grid;
  align-content: start;
  gap: 12px;
  min-height: 0;
  padding: 14px;
  border: 1px solid var(--calendar-border);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 14px 36px rgba(23, 49, 79, 0.06);
}

.calendar-board {
  grid-template-rows: auto auto auto minmax(0, 1fr);
}

.calendar-board-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.calendar-board-head strong {
  display: block;
  color: var(--calendar-text);
  font-size: 1.12rem;
}

.calendar-board-head span {
  color: var(--calendar-muted);
  font-size: 0.82rem;
}

.month-switcher {
  display: inline-flex;
  gap: 6px;
}

.month-switcher-btn {
  width: 34px;
  height: 34px;
  border: 1px solid var(--calendar-border);
  background: #fff;
  color: var(--calendar-text);
  font-size: 1.02rem;
  cursor: pointer;
  transition: border-color 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.month-switcher-btn:hover,
.month-switcher-btn:focus-visible {
  border-color: var(--calendar-primary);
  background: var(--calendar-primary-soft);
  color: var(--calendar-primary);
  outline: none;
}

.calendar-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  color: var(--calendar-muted);
  font-size: 12px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-task {
  background: var(--calendar-task);
}

.legend-milestone {
  background: var(--calendar-milestone);
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 6px;
}

.calendar-weekdays span {
  padding: 0 6px;
  color: #7a89a1;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.calendar-grid {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  grid-auto-rows: 1fr;
  gap: 6px;
  min-height: 0;
  height: 100%;
}

.calendar-day {
  min-height: 0;
  height: 100%;
  display: grid;
  align-content: start;
  gap: 6px;
  padding: 7px 7px 6px;
  border: 1px solid var(--calendar-border);
  background: #ffffff;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.18s ease, background 0.18s ease, transform 0.18s ease, box-shadow 0.18s ease;
}

.calendar-day:hover,
.calendar-day:focus-visible {
  border-color: var(--calendar-primary);
  background: #fbfdff;
  box-shadow: 0 8px 20px rgba(47, 111, 228, 0.08);
  outline: none;
  transform: translateY(-1px);
}

.calendar-day.is-other-month {
  background: #f7f9fc;
}

.calendar-day.is-other-month .calendar-day-number {
  color: #9aa8bd;
}

.calendar-day.is-today {
  border-color: var(--calendar-border-strong);
}

.calendar-day.is-today .calendar-day-number {
  color: var(--calendar-primary);
}

.calendar-day.is-selected {
  border-color: var(--calendar-primary);
  background: linear-gradient(180deg, #ffffff 0%, var(--calendar-primary-soft) 100%);
  box-shadow: 0 12px 24px rgba(47, 111, 228, 0.12);
}

.calendar-day-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 6px;
}

.calendar-day-number {
  color: var(--calendar-text);
  font-size: 0.92rem;
  font-weight: 700;
}

.calendar-day-count {
  min-width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0 5px;
  border-radius: 999px;
  background: #edf3fb;
  color: #46607c;
  font-size: 11px;
  font-weight: 700;
}

.calendar-day-events {
  display: grid;
  align-content: start;
  gap: 4px;
  min-height: 0;
  overflow: hidden;
}

.day-event-chip {
  display: grid;
  gap: 1px;
  width: 100%;
  padding: 4px 6px;
  border: 0;
  background: var(--calendar-soft);
  color: var(--calendar-text);
  text-align: left;
  cursor: pointer;
  transition: filter 0.18s ease, transform 0.18s ease;
}

.day-event-chip:hover,
.day-event-chip:focus-visible {
  filter: brightness(0.98);
  transform: translateX(1px);
  outline: none;
}

.day-event-time {
  color: var(--calendar-muted);
  font-size: 10px;
}

.day-event-chip strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 11px;
  line-height: 1.2;
}

.event-tag-task {
  background: var(--calendar-task-soft);
}

.event-tag-milestone {
  background: var(--calendar-milestone-soft);
}

.event-tag-neutral {
  background: #eef2f7;
}

.more-events {
  color: var(--calendar-muted);
  font-size: 11px;
  font-weight: 600;
}

.agenda-panel {
  position: sticky;
  top: 12px;
  align-self: start;
  max-height: calc(100vh - 24px);
  max-height: calc(100dvh - 24px);
  overflow: auto;
}

.agenda-panel-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.agenda-label {
  display: block;
  margin-bottom: 4px;
  color: #7a89a1;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.agenda-panel-head strong {
  color: var(--calendar-text);
  font-size: 1rem;
  line-height: 1.35;
}

.agenda-total {
  color: var(--calendar-primary);
  font-size: 0.82rem;
  font-weight: 700;
}

.agenda-list {
  display: grid;
  gap: 10px;
}

.agenda-card {
  display: grid;
  gap: 8px;
  padding: 10px 12px;
  border: 1px solid var(--calendar-border);
  background: #ffffff;
}

.agenda-card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.agenda-card-head strong {
  color: var(--calendar-text);
  font-size: 0.94rem;
}

.agenda-type {
  display: inline-flex;
  align-items: center;
  min-height: 22px;
  padding: 0 8px;
  color: #344256;
  font-size: 0.72rem;
  font-weight: 700;
  white-space: nowrap;
}

.agenda-card-meta {
  display: grid;
  gap: 3px;
  color: var(--calendar-muted);
  font-size: 0.82rem;
}

.agenda-card-actions {
  display: flex;
  justify-content: flex-start;
}

.agenda-state {
  display: grid;
  gap: 4px;
  padding: 16px 14px;
  border: 1px dashed var(--calendar-border);
  background: #f8fbff;
}

.agenda-state strong {
  color: var(--calendar-text);
  font-size: 1rem;
}

.agenda-state span {
  color: var(--calendar-muted);
  line-height: 1.6;
}

@media (max-width: 1100px) {
  .calendar-view {
    height: auto;
    overflow: visible;
  }

  .calendar-shell {
    grid-template-columns: 1fr;
  }

  .calendar-board {
    grid-template-rows: auto;
  }

  .agenda-panel {
    position: static;
    max-height: none;
    overflow: visible;
  }
}

@media (max-width: 900px) {
  .page-header {
    flex-direction: column;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .calendar-grid,
  .calendar-weekdays {
    gap: 8px;
  }

  .calendar-day {
    min-height: 92px;
    height: auto;
    padding: 8px;
  }
}

@media (max-width: 640px) {
  .calendar-view {
    padding: 18px 16px 24px;
  }

  .page-header h1 {
    font-size: 1.6rem;
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }

  .calendar-board,
  .agenda-panel {
    padding: 14px;
  }

  .calendar-grid,
  .calendar-weekdays {
    gap: 6px;
  }

  .calendar-day {
    min-height: 96px;
    gap: 8px;
    padding: 7px;
  }

  .calendar-day-number {
    font-size: 0.92rem;
  }

  .day-event-chip {
    padding: 6px;
  }

  .day-event-chip strong {
    font-size: 11px;
  }

  .day-event-time,
  .more-events,
  .calendar-weekdays span {
    font-size: 10px;
  }

  .agenda-card-head {
    flex-direction: column;
  }
}
</style>
