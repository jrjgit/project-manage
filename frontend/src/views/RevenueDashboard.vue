<template>
  <AppLayout title="营收统计看板" subtitle="年度需求产值统计">
    <div class="revenue-page">
      <!-- Filter bar -->
      <section class="toolbar section-card">
        <div class="toolbar-row">
          <div class="toolbar-left">
            <h2 class="toolbar-title">营收统计</h2>
          </div>
          <div class="toolbar-right">
            <n-select
              v-model:value="selectedYear"
              :options="yearOptions"
              style="width: 140px;"
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
                <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
              </svg>
            </div>
            <span class="stat-label">累计接收</span>
          </div>
          <div class="stat-value">{{ formatMoney(stats.total_created) }}</div>
        </div>
        <div class="stat-card" style="--accent: #f59e0b;">
          <div class="stat-card-header">
            <div class="stat-icon" style="background: linear-gradient(135deg, #f59e0b, #fbbf24);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <span class="stat-label">未开始</span>
          </div>
          <div class="stat-value">{{ formatMoney(stats.pending_count) }}</div>
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
          <div class="stat-value">{{ formatMoney(stats.done_count) }}</div>
        </div>
        <div class="stat-card" style="--accent: #3b82f6;">
          <div class="stat-card-header">
            <div class="stat-icon" style="background: linear-gradient(135deg, #3b82f6, #60a5fa);">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2" />
              </svg>
            </div>
            <span class="stat-label">测试中</span>
          </div>
          <div class="stat-value">{{ formatMoney(stats.testing_count) }}</div>
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
import AppLayout from '@/components/AppLayout.vue'
import { NSelect } from 'naive-ui'
import BarChart from '@/components/BarChart.vue'

const selectedYear = ref(new Date().getFullYear())
const stats = ref({})
const monthlyCreated = ref([])
const monthlyDone = ref([])

const yearOptions = Array.from({ length: 7 }, (_, i) => {
  const y = 2024 + i
  return { label: `${y}年`, value: y }
})

async function loadData() {
  try {
    const res = await getRevenueStats(selectedYear.value)
    stats.value = {
      total_created: res.total_created,
      pending_count: res.pending_count,
      done_count: res.done_count,
      testing_count: res.testing_count
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

onMounted(loadData)
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
}

.section-card {
  background: white;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  padding: 16px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  background: white;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  padding: 20px;
  transition: box-shadow 0.18s ease;
}

.stat-card:hover {
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.06);
}

.stat-card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 16px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon svg {
  width: 18px;
  height: 18px;
}

.stat-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #0f172a;
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

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
