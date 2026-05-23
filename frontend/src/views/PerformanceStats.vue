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

      <!-- Performance table -->
      <section class="section-card">
        <div class="list-header">
          <h3>个人完成量排行</h3>
        </div>
        <n-data-table
          :columns="columns"
          :data="sortedData"
          :pagination="{ pageSize: 20 }"
          :bordered="false"
          :single-line="false"
          striped
        />
      </section>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue'
import { getPerformanceStats } from '@/api/statistics'
import AppLayout from '@/components/AppLayout.vue'
import { NSelect, NDataTable, NTag } from 'naive-ui'

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

const sortedData = computed(() => {
  return [...performanceData.value].sort((a, b) => (b.done || 0) - (a.done || 0))
})

const columns = [
  { title: '用户名', key: 'userName', width: 120 },
  {
    title: '进行中',
    key: 'inProgress',
    width: 100,
    render(row) {
      const val = row.inProgress || 0
      return h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
        h('div', {
          style: {
            height: '18px',
            width: `${Math.min(val * 20, 120)}px`,
            borderRadius: '4px',
            background: 'linear-gradient(90deg, #3b82f6, #60a5fa)',
            transition: 'width 0.3s ease'
          }
        }),
        h('span', null, val)
      ])
    }
  },
  {
    title: '逾期',
    key: 'overdue',
    width: 100,
    render(row) {
      const val = row.overdue || 0
      return h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
        h('div', {
          style: {
            height: '18px',
            width: `${Math.min(val * 20, 120)}px`,
            borderRadius: '4px',
            background: `linear-gradient(90deg, #ef4444, #f87171)`,
            transition: 'width 0.3s ease'
          }
        }),
        val > 0
          ? h(NTag, { type: 'error', size: 'small', round: true }, { default: () => val })
          : h('span', null, val)
      ])
    }
  },
  {
    title: '已完成',
    key: 'done',
    width: 120,
    render(row) {
      const val = row.done || 0
      return h('div', { style: 'display: flex; align-items: center; gap: 8px;' }, [
        h('div', {
          style: {
            height: '18px',
            width: `${Math.min(val * 20, 120)}px`,
            borderRadius: '4px',
            background: 'linear-gradient(90deg, #10b981, #34d399)',
            transition: 'width 0.3s ease'
          }
        }),
        h('span', { style: 'font-weight: 600;' }, val)
      ])
    }
  }
]

async function loadData() {
  try {
    performanceData.value = await getPerformanceStats(selectedYear.value, selectedMonth.value)
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
</style>
