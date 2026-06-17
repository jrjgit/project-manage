<template>
  <AppLayout title="人员绩效统计" subtitle="个人任务完成情况">
    <div class="performance-page">
      <!-- Filter bar -->
      <section class="toolbar section-card">
        <div class="toolbar-row">
          <div class="toolbar-left">
            <h2 class="toolbar-title">绩效统计</h2>
          </div>
          <div class="toolbar-right">
            <n-radio-group v-model:value="filterType" size="small" @update:value="onFilterTypeChange">
              <n-radio-button value="year">按年</n-radio-button>
              <n-radio-button value="month">按月</n-radio-button>
              <n-radio-button value="range">按范围</n-radio-button>
            </n-radio-group>
            <template v-if="filterType === 'year'">
              <n-select v-model:value="selectedYear" :options="yearOptions" style="width: 110px;" size="small" @update:value="loadData" />
            </template>
            <template v-if="filterType === 'month'">
              <n-select v-model:value="selectedYear" :options="yearOptions" style="width: 110px;" size="small" @update:value="loadData" />
              <n-select v-model:value="selectedMonth" :options="monthOptions" style="width: 90px;" size="small" @update:value="loadData" />
            </template>
            <template v-if="filterType === 'range'">
              <n-date-picker v-model:value="rangeStart" type="date" placeholder="开始日期" size="small" style="width: 140px;" @update:value="loadData" />
              <n-date-picker v-model:value="rangeEnd" type="date" placeholder="结束日期" size="small" style="width: 140px;" @update:value="loadData" />
            </template>
          </div>
        </div>
      </section>

      <!-- Performance bar charts -->
      <section class="section-card chart-section">
        <div class="section-header gradient-header" style="background: linear-gradient(135deg, #eff6ff, #ffffff);">
          <div>
            <div class="section-kicker" style="color: #3b82f6;">任务负载</div>
            <h3>进行中任务</h3>
          </div>
          <div class="section-hint">点击柱状图查看人员任务明细</div>
        </div>
        <div class="chart-wrap">
          <BarChart :data="inProgressData" color="#3b82f6" @click="openUserTasks" />
        </div>
      </section>

      <section class="section-card chart-section">
        <div class="section-header gradient-header" style="background: linear-gradient(135deg, #fefce8, #ffffff);">
          <div>
            <div class="section-kicker" style="color: #f59e0b;">绩效产值</div>
            <h3>绩效产值统计</h3>
          </div>
        </div>
        <div class="chart-wrap">
          <BarChart :data="performanceValueData" color="#f59e0b" />
        </div>
      </section>
    </div>

    <!-- User tasks detail modal -->
    <n-modal v-model:show="showTaskModal" preset="card" style="width: min(92vw, 900px)" :title="`${selectedUserName} - 进行中任务明细`" :mask-closable="false">
      <n-data-table
        :columns="taskColumns"
        :data="selectedUserTasks"
        :pagination="{ pageSize: 10 }"
        :bordered="false"
        :single-line="false"
        striped
        :scroll-x="760"
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showTaskModal = false">关闭</n-button>
        </n-space>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { getPerformanceStats } from '@/api/statistics'
import AppLayout from '@/components/AppLayout.vue'
import { NSelect, NRadioGroup, NRadioButton, NDatePicker, NModal, NDataTable, NSpace, NButton, NTag } from 'naive-ui'
import BarChart from '@/components/BarChart.vue'
import { taskStatusMeta } from '@/constants/statusMeta'

const filterType = ref('year')
const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const rangeStart = ref(null)
const rangeEnd = ref(null)
const performanceData = ref([])

const showTaskModal = ref(false)
const selectedUserName = ref('')
const selectedUserTasks = ref([])

const yearOptions = Array.from({ length: 7 }, (_, i) => {
  const y = 2024 + i
  return { label: `${y}年`, value: y }
})

const monthOptions = Array.from({ length: 12 }, (_, i) => ({
  label: `${i + 1}月`,
  value: i + 1
}))

const inProgressData = computed(() =>
  performanceData.value.map(d => ({ label: d.userName, value: d.inProgressLoad || 0, userId: d.userId }))
)

const performanceValueData = computed(() =>
  [...performanceData.value].sort((a, b) => (b.performanceValue || 0) - (a.performanceValue || 0))
    .map(d => ({ label: d.userName, value: d.performanceValue || 0 }))
)

function onFilterTypeChange() {
  // 切换模式时保持合理的默认值
  if (filterType.value === 'range') {
    const now = new Date()
    const start = new Date(now.getFullYear(), now.getMonth(), 1)
    rangeStart.value = start.getTime()
    rangeEnd.value = now.getTime()
  }
  loadData()
}

async function loadData() {
  try {
    const params = { filterType: filterType.value, year: selectedYear.value }
    if (filterType.value === 'month') {
      params.month = selectedMonth.value
    } else if (filterType.value === 'range') {
      params.startDate = rangeStart.value ? formatDate(rangeStart.value) : null
      params.endDate = rangeEnd.value ? formatDate(rangeEnd.value) : null
    }
    const res = await getPerformanceStats(params)
    performanceData.value = (res.users || []).map(u => ({
      userId: u.user_id,
      userName: u.user_name,
      inProgressLoad: u.in_progress_load,
      performanceValue: u.performance_value,
      tasks: u.tasks || []
    }))
  } catch (e) {
    window.$message?.error('加载绩效统计失败')
  }
}

function formatDate(ts) {
  const d = new Date(ts)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function openUserTasks(item) {
  const user = performanceData.value.find(u => u.userId === item.userId)
  if (!user) return
  selectedUserName.value = user.userName
  selectedUserTasks.value = (user.tasks || []).filter(t => t.status !== 'closed')
  showTaskModal.value = true
}

const taskColumns = [
  { title: '需求编号', key: 'requirement_number', width: 150 },
  { title: '所属系统', key: 'system', width: 120 },
  { title: '平台', key: 'terminal', width: 100 },
  {
    title: '任务描述', key: 'description', minWidth: 200, ellipsis: true,
    render(row) { return h('span', { title: row.description || '' }, row.description || '-') }
  },
  { title: '负责人', key: 'assignee_name', width: 100 },
  {
    title: '任务状态', key: 'status', width: 110,
    render(row) {
      const meta = taskStatusMeta[row.status] || { label: row.status, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  {
    title: '完成比例', key: 'progress', width: 100,
    render(row) {
      const color = (row.progress || 0) >= 100 ? '#18a058' : '#6366f1'
      return h('span', { style: `font-weight:700;color:${color}` }, `${row.progress || 0}%`)
    }
  }
]

onMounted(loadData)
</script>

<style scoped>
.performance-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.toolbar { padding: 14px 18px; }
.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}
.toolbar-left { display: flex; align-items: center; gap: 10px; }
.toolbar-title {
  margin: 0; font-size: 18px; font-weight: 700; color: #0f172a;
}
.toolbar-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.section-card {
  background: white; border-radius: 18px;
  border: 1px solid #e2e8f0; padding: 16px;
}
.chart-section { padding: 0; overflow: hidden; }
.section-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 20px; border-bottom: 1px solid #e2e8f0;
}
.gradient-header { border-radius: 18px 18px 0 0; }
.section-kicker {
  font-size: 12px; font-weight: 700;
  letter-spacing: 0.08em; text-transform: uppercase;
}
.section-header h3 {
  margin: 4px 0 0; font-size: 16px; font-weight: 700; color: #0f172a;
}
.section-hint {
  font-size: 12px; color: #94a3b8;
}
.chart-wrap { padding: 8px; }
@media (max-width: 768px) {
  .toolbar-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .chart-wrap {
    padding: 4px;
  }
}
</style>
