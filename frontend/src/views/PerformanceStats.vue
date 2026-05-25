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

      <!-- Performance bar charts -->
      <section class="section-card chart-section">
        <div class="list-header">
          <h3>进行中任务</h3>
        </div>
        <BarChart :data="inProgressData" color="#3b82f6" />
      </section>

      <section class="section-card chart-section">
        <div class="list-header">
          <h3>已延期任务</h3>
        </div>
        <BarChart :data="overdueData" color="#ef4444" />
      </section>

      <section class="section-card chart-section">
        <div class="list-header">
          <h3>绩效产能</h3>
        </div>
        <BarChart :data="doneData" color="#10b981" />
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

const inProgressData = computed(() =>
  performanceData.value.map(d => ({ label: d.userName, value: d.inProgress || 0 }))
)
const overdueData = computed(() =>
  performanceData.value.map(d => ({ label: d.userName, value: d.overdue || 0 }))
)
const doneData = computed(() =>
  [...performanceData.value].sort((a, b) => (b.done || 0) - (a.done || 0)).map(d => ({ label: d.userName, value: d.done || 0 }))
)

async function loadData() {
  try {
    const res = await getPerformanceStats(selectedYear.value, selectedMonth.value)
    performanceData.value = (res.users || []).map(u => ({
      userName: u.user_name,
      inProgress: u.in_progress,
      overdue: u.overdue,
      done: u.done
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

.toolbar {
  padding: 14px 18px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-card {
  background: white;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  padding: 16px;
}

.list-header {
  margin-bottom: 12px;
}

.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.chart-section {
  padding: 0;
  overflow: hidden;
}
</style>
