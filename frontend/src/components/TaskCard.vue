<template>
  <div class="task-card" v-bind="$attrs">
    <div class="card-priority" :style="{ background: priorityMetaItem.color }"></div>
    <div class="card-body">
      <div class="card-header-row">
        <span class="task-id">#{{ task.id }}</span>
        <n-tag :type="priorityMetaItem.tone" size="tiny" round>{{ priorityMetaItem.label }}</n-tag>
      </div>
      <div class="card-title">{{ task.title }}</div>
      <div class="card-meta-row">
        <span class="status-pill" :style="statusPillStyle">{{ statusMetaItem.label }}</span>
        <span v-if="task.project?.name" class="project-name">{{ task.project.name }}</span>
      </div>
      <div class="card-meta">
        <span v-if="task.assignee" class="assignee">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
          {{ task.assignee.name }}
        </span>
        <span v-else class="unassigned">未指派</span>
      </div>
      <div v-if="task.deadline" class="card-deadline">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
        {{ formatDate(task.deadline) }}
      </div>
      <div v-if="task.terminal || task.system" class="card-tags-row">
        <n-tag size="tiny" round>{{ skillsMap[task.terminal] || task.terminal }}</n-tag>
        <span v-if="task.system" class="card-system">{{ task.system }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { NTag } from 'naive-ui'
import { priorityMeta, taskStatusMeta } from '@/constants/statusMeta'

const props = defineProps({ task: Object, skillsMap: { type: Object, default: () => ({}) } })

const priorityMetaItem = computed(() => priorityMeta[props.task.priority] || { label: props.task.priority, tone: 'default', color: '#94a3b8' })
const statusMetaItem = computed(() => taskStatusMeta[props.task.status] || { label: props.task.status, tone: 'default', color: '#94a3b8' })
const statusPillStyle = computed(() => ({ '--status-color': statusMetaItem.value.color }))

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  return `${date.getMonth() + 1}/${date.getDate()}`
}
</script>

<style scoped>
.task-card { background: white; border-radius: 12px; cursor: pointer; box-shadow: 0 1px 3px rgba(0,0,0,0.04); transition: all 0.25s; position: relative; overflow: hidden; display: flex; }
.task-card:hover { box-shadow: 0 6px 20px rgba(0,0,0,0.08); transform: translateY(-2px); }
.card-priority { width: 4px; flex-shrink: 0; }
.card-body { padding: 12px 14px; flex: 1; min-width: 0; }
.card-header-row { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.task-id { font-size: 11px; color: #94a3b8; font-weight: 500; }
.card-title { font-size: 13px; font-weight: 600; color: #1e293b; margin-bottom: 10px; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; }
.card-meta-row { display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-bottom: 10px; }
.status-pill { display: inline-flex; align-items: center; padding: 3px 8px; border-radius: 999px; font-size: 11px; font-weight: 600; color: var(--status-color); background: color-mix(in srgb, var(--status-color) 12%, white); }
.project-name { font-size: 11px; color: #94a3b8; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-meta { display: flex; align-items: center; justify-content: space-between; }
.assignee { font-size: 11px; color: #64748b; display: flex; align-items: center; gap: 4px; }
.assignee svg { width: 12px; height: 12px; }
.unassigned { font-size: 11px; color: #cbd5e1; font-style: italic; }
.card-deadline { font-size: 11px; color: #f43f5e; margin-top: 8px; display: flex; align-items: center; gap: 4px; }
.card-deadline svg { width: 12px; height: 12px; }
.card-tags-row { display: flex; align-items: center; gap: 8px; margin-top: 8px; flex-wrap: wrap; }
.card-system { font-size: 11px; color: #64748b; background: #f1f5f9; padding: 2px 8px; border-radius: 8px; }
</style>
