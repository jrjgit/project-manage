<template>
  <n-data-table
    :columns="columns"
    :data="bugs"
    :pagination="{ pageSize: 10 }"
  />
</template>

<script setup>
import { h } from 'vue'
import { NDataTable, NTag, NButton } from 'naive-ui'
import { bugStatusMeta, severityMeta } from '@/constants/statusMeta'

const props = defineProps({ bugs: Array })
const emit = defineEmits(['bug-click'])

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title' },
  {
    title: '严重程度',
    key: 'severity',
    width: 100,
    render(row) {
      const meta = severityMeta[row.severity] || { label: row.severity, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 110,
    render(row) {
      const meta = bugStatusMeta[row.status] || { label: row.status, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  {
    title: '所属任务',
    key: 'task',
    render(row) {
      return row.task?.title || '-'
    }
  },
  {
    title: '创建人',
    key: 'creator',
    width: 100,
    render(row) {
      return row.creator?.name || '-'
    }
  },
  {
    title: '指派给',
    key: 'assignee',
    width: 100,
    render(row) {
      return row.assignee?.name || '未指派'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 80,
    render(row) {
      return h(NButton, { size: 'small', type: 'primary', ghost: true, onClick: () => emit('bug-click', row.id) }, { default: () => '详情' })
    }
  }
]
</script>
