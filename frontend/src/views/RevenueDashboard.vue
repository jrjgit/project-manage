<template>
  <AppLayout title="运维营收统计" subtitle="年度运维需求产值统计">
    <div class="revenue-page">
      <!-- Filter bar -->
      <section class="toolbar section-card">
        <div class="toolbar-row">
          <div class="toolbar-left">
            <h2 class="toolbar-title">运维营收统计</h2>
          </div>
          <div class="toolbar-right">
            <n-select
              v-model:value="selectedProject"
              :options="projectOptions"
              placeholder="全部项目"
              clearable
              filterable
              style="width: 180px;"
              size="small"
              @update:value="loadData"
            />
            <n-select
              v-model:value="selectedYear"
              :options="yearOptions"
              style="width: 110px;"
              size="small"
              @update:value="loadData"
            />
          </div>
        </div>
      </section>

      <!-- Monthly created bar chart -->
      <section class="section-card chart-section">
        <div class="section-header gradient-header" style="background: linear-gradient(135deg, #eef2ff, #ffffff);">
          <div>
            <div class="section-kicker">接收趋势</div>
            <h3>月度接收产值</h3>
          </div>
        </div>
        <div class="chart-wrap">
          <BarChart :data="monthlyCreated" color="#6366f1" />
        </div>
      </section>

      <!-- Monthly done bar chart -->
      <section class="section-card chart-section">
        <div class="section-header gradient-header" style="background: linear-gradient(135deg, #ecfdf5, #ffffff);">
          <div>
            <div class="section-kicker">完成趋势</div>
            <h3>月度完成产值</h3>
          </div>
        </div>
        <div class="chart-wrap">
          <BarChart :data="monthlyDone" color="#10b981" />
        </div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getRevenueStats } from '@/api/statistics'
import { getProjects } from '@/api/projects'
import AppLayout from '@/components/AppLayout.vue'
import { NSelect } from 'naive-ui'
import BarChart from '@/components/BarChart.vue'

const selectedYear = ref(new Date().getFullYear())
const selectedProject = ref(null)
const stats = ref({})
const monthlyCreated = ref([])
const monthlyDone = ref([])
const projects = ref([])

const projectOptions = computed(() => projects.value.map(p => ({ label: p.name, value: p.id })))

const yearOptions = Array.from({ length: 7 }, (_, i) => {
  const y = 2024 + i
  return { label: `${y}年`, value: y }
})

async function loadProjects() {
  try { projects.value = await getProjects() || [] } catch (e) { console.error(e) }
}

async function loadData() {
  try {
    const params = { year: selectedYear.value }
    if (selectedProject.value) params.projectId = selectedProject.value
    const res = await getRevenueStats(params)
    stats.value = {
      total_created: res.total_created,
      total_done: res.total_done
    }
    monthlyCreated.value = (res.monthly_created || []).map((item) => ({
      label: `${item.month}月`,
      value: item.amount || 0
    }))
    monthlyDone.value = (res.monthly_done || []).map((item) => ({
      label: `${item.month}月`,
      value: item.amount || 0
    }))
  } catch (e) {
    window.$message?.error('加载营收统计失败')
  }
}

function formatMoney(v) {
  if (v == null || v === 0) return '0'
  return '¥' + Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 0, maximumFractionDigits: 2 })
}

onMounted(() => { loadProjects(); loadData() })
</script>

<style scoped>
.revenue-page {
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
  flex-wrap: wrap;
}

.section-card {
  background: white;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  padding: 16px;
}

.chart-section {
  padding: 0;
  overflow: hidden;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #e2e8f0;
}

.gradient-header {
  border-radius: 18px 18px 0 0;
}

.section-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6366f1;
}

.section-header h3 {
  margin: 4px 0 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.chart-wrap {
  padding: 8px;
}

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
