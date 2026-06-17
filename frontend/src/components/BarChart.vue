<template>
  <div v-if="data.length === 0" class="empty-chart">暂无数据</div>
  <v-chart v-else class="chart" :option="option" autoresize @click="onChartClick" />
</template>

<script setup>
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart as EBarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { computed } from 'vue'

use([CanvasRenderer, EBarChart, GridComponent, TooltipComponent])

const props = defineProps({
  data: { type: Array, default: () => [] },
  color: { type: String, default: '#6366f1' }
})

const emit = defineEmits(['click'])

function onChartClick(params) {
  const item = props.data[params.dataIndex]
  if (item) emit('click', item)
}

const option = computed(() => {
  const labels = props.data.map(d => d.label || d.month || '')
  const values = props.data.map(d => d.value || d.count || 0)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    grid: {
      left: '2%',
      right: '2%',
      bottom: '8%',
      top: '12%'
    },
    xAxis: {
      type: 'category',
      data: labels,
      axisTick: { alignWithLabel: true },
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b', interval: 0 }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
      axisLabel: { color: '#64748b' }
    },
    series: [{
      type: 'bar',
      barWidth: '50%',
      data: values,
      itemStyle: {
        color: props.color,
        borderRadius: [6, 6, 0, 0]
      }
    }]
  }
})
</script>

<style scoped>
.chart {
  height: 280px;
  width: 100%;
}
.empty-chart {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 280px;
  color: #94a3b8;
  font-size: 14px;
}
</style>
