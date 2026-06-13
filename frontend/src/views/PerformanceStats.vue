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
            <n-select
              v-model:value="selectedYear"
              :options="yearOptions"
              style="width: 140px;"
              size="small"
              @update:value="loadData"
            />
            <n-select
              v-model:value="selectedMonth"
              :options="monthOptions"
              style="width: 120px;"
              size="small"
              @update:value="loadData"
            />
          </div>
        </div>
      </section>

      <!-- Hero stats row -->
      <section class="stats-grid">
        <div class="stat-card" style="--accent: #6366f1;">
          <div class="stat-card-header">
            <div class="stat-icon" style="background: linear-gradient(135deg, #6366f1, #8b5cf6);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                <path d="M16 3.13a4 4 0 0 1 0 7.75" />
              </svg>
            </div>
            <span class="stat-label">统计人数</span>
          </div>
          <div class="stat-value">{{ stats.totalUsers }}</div>
        </div>
        <div class="stat-card" style="--accent: #3b82f6;">
          <div class="stat-card-header">
            <div class="stat-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <span class="stat-label">进行中</span>
          </div>
          <div class="stat-value">{{ stats.totalInProgress }}</div>
        </div>
        <div class="stat-card" style="--accent: #ef4444;">
          <div class="stat-card-header">
            <div class="stat-icon" style="background: linear-gradient(135deg, #ef4444, #f87171);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z" />
                <line x1="12" y1="9" x2="12" y2="13" />
                <line x1="12" y1="17" x2="12.01" y2="17" />
              </svg>
            </div>
            <span class="stat-label">已延期</span>
          </div>
          <div class="stat-value">{{ stats.totalOverdue }}</div>
        </div>
        <div class="stat-card" style="--accent: #10b981;">
          <div class="stat-card-header">
            <div class="stat-icon" style="background: linear-gradient(135deg, #10b981, #34d399);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </div>
            <span class="stat-label">已完成</span>
          </div>
          <div class="stat-value">{{ stats.totalDone }}</div>
        </div>
        <div class="stat-card" style="--accent: #f59e0b;">
          <div class="stat-card-header">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z"/>
                <path d="M19 10v2a7 7 0 0 1-14 0v-2"/>
                <line x1="12" y1="19" x2="12" y2="23"/>
                <line x1="8" y1="23" x2="16" y2="23"/>
              </svg>
            </div>
            <span class="stat-label">总绩效工时</span>
          </div>
          <div class="stat-value">{{ stats.totalPerformance }}</div>
        </div>
      </section>

      <!-- Performance bar charts -->
      <section class="section-card chart-section">
        <div class="section-header gradient-header" style="background: linear-gradient(135deg, #eff6ff, #ffffff);">
          <div>
            <div class="section-kicker" style="color: #3b82f6;">任务负载</div>
            <h3>进行中任务</h3>
          </div>
        </div>
        <div class="chart-wrap">
          <BarChart :data="inProgressData" color="#3b82f6" />
        </div>
      </section>

      <section class="section-card chart-section">
        <div class="section-header gradient-header" style="background: linear-gradient(135deg, #ecfdf5, #ffffff);">
          <div>
            <div class="section-kicker" style="color: #10b981;">产出排行</div>
            <h3>已完成任务</h3>
          </div>
        </div>
        <div class="chart-wrap">
          <BarChart :data="doneData" color="#10b981" />
        </div>
      </section>

      <section class="section-card chart-section">
        <div class="section-header gradient-header" style="background: linear-gradient(135deg, #fefce8, #ffffff);">
          <div>
            <div class="section-kicker" style="color: #f59e0b;">绩效工时</div>
            <h3>绩效工时总计</h3>
          </div>
        </div>
        <div class="chart-wrap">
          <BarChart :data="performanceData2" color="#f59e0b" />
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getPerformanceStats } from '@/api/statistics'
import AppLayout from '@/components/AppLayout.vue'
import { NSelect } from 'naive-ui'
import BarChart from '@/components/BarChart.vue'

const selectedYear = ref(new Date().getFullYear())
const selectedMonth = ref(new Date().getMonth() + 1)
const performanceData = ref([])

const yearOptions = Array.from({ length: 7 }, (_, i) => {
  const y = 2024 + i
  return { label: `${y}年`, value: y }
})

const monthOptions = Array.from({ length: 12 }, (_, i) => ({
  label: `${i + 1}月`,
  value: i + 1
}))

const stats = computed(() => {
  const data = performanceData.value
  return {
    totalUsers: data.length,
    totalInProgress: data.reduce((s, d) => s + (d.inProgress || 0), 0),
    totalOverdue: data.reduce((s, d) => s + (d.overdue || 0), 0),
    totalDone: data.reduce((s, d) => s + (d.done || 0), 0),
    totalPerformance: data.reduce((s, d) => s + (d.performance || 0), 0)
  }
})

const inProgressData = computed(() =>
  performanceData.value.map(d => ({ label: d.userName, value: d.inProgress || 0 }))
)

const doneData = computed(() =>
  [...performanceData.value].sort((a, b) => (b.done || 0) - (a.done || 0)).map(d => ({ label: d.userName, value: d.done || 0 }))
)

const performanceData2 = computed(() =>
  [...performanceData.value].sort((a, b) => (b.performance || 0) - (a.performance || 0)).map(d => ({ label: d.userName, value: d.performance || 0 }))
)

async function loadData() {
  try {
    const res = await getPerformanceStats(selectedYear.value, selectedMonth.value)
    performanceData.value = (res.users || []).map(u => ({
      userName: u.user_name,
      inProgress: u.in_progress,
      overdue: u.overdue,
      done: u.done,
      performance: u.performance
    }))
  } catch (e) {
    window.$message?.error('加载绩效统计失败')
  }
}

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
.toolbar-right { display: flex; align-items: center; gap: 10px; }
.section-card {
  background: white; border-radius: 18px;
  border: 1px solid #e2e8f0; padding: 16px;
}
.stats-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 16px;
}
.stat-card {
  background: white; border-radius: 18px;
  border: 1px solid #e2e8f0; padding: 20px;
  transition: box-shadow 0.18s ease;
}
.stat-card:hover { box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06); }
.stat-card-header {
  display: flex; align-items: center; gap: 10px; margin-bottom: 16px;
}
.stat-icon {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; color: white;
}
.stat-icon svg { width: 18px; height: 18px; }
.stat-label { font-size: 13px; font-weight: 600; color: #64748b; }
.stat-value { font-size: 32px; font-weight: 700; color: #0f172a; }
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
.chart-wrap { padding: 8px; }
@media (max-width: 1100px) {
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
}
@media (max-width: 700px) {
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 480px) {
  .stats-grid { grid-template-columns: 1fr; }
}
@media (max-width: 768px) {
  .toolbar-row {
    flex-direction: column;
    align-items: flex-start;
  }
  .stat-value {
    font-size: 24px;
  }
  .chart-wrap {
    padding: 4px;
  }
}
</style>
