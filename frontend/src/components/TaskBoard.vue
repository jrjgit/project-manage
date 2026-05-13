<template>
  <div class="task-board">
    <div
      v-for="(row, rowIdx) in rows"
      :key="rowIdx"
      class="board-row"
    >
      <div
        v-for="col in row"
        :key="col.key"
        class="board-column"
        @dragover.prevent
        @drop="onDrop($event, col.key)"
      >
        <div class="column-header">
          <div class="column-title">
            <span class="status-indicator" :style="{ background: col.meta.color }"></span>
            <span>{{ col.meta.label }}</span>
          </div>
          <span class="count">{{ col.tasks.length }}</span>
        </div>
        <div class="column-body">
          <TaskCard
            v-for="task in col.tasks"
            :key="task.id"
            :task="task"
            draggable="true"
            @dragstart="onDragStart($event, task)"
            @click="$emit('task-click', task.id)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import TaskCard from './TaskCard.vue'
import { taskStatusMeta } from '@/constants/statusMeta'

const props = defineProps({ tasks: Array })
const emit = defineEmits(['status-change', 'task-click'])

const row1Keys = ['pending', 'assigned_lead', 'developing', 'developed']
const row2Keys = ['pending_test', 'testing', 'passed', 'rejected', 'closed']

function buildCols(keys) {
  return keys.map((key) => ({
    key,
    meta: taskStatusMeta[key],
    tasks: props.tasks.filter((task) => task.status === key)
  }))
}

const rows = computed(() => [
  buildCols(row1Keys),
  buildCols(row2Keys)
])

function onDragStart(event, task) {
  event.dataTransfer.setData('taskId', task.id)
  event.dataTransfer.setData('oldStatus', task.status)
}

function onDrop(event, newStatus) {
  const taskId = event.dataTransfer.getData('taskId')
  const oldStatus = event.dataTransfer.getData('oldStatus')
  if (!taskId || oldStatus === newStatus) return
  emit('status-change', { taskId: Number(taskId), newStatus, comment: '' })
}
</script>

<style scoped>
.task-board {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.board-row {
  display: flex;
  gap: 12px;
}

.board-column {
  flex: 1;
  min-width: 0;
  background: #f8fafc;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  max-height: calc((100vh - 200px) / 2);
  border: 1px solid #e2e8f0;
}

.column-header {
  padding: 10px 14px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}

.column-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
  overflow: hidden;
}

.column-title span:last-child {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.count {
  font-size: 12px;
  color: #94a3b8;
  background: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: 600;
  flex-shrink: 0;
  margin-left: 8px;
}

.column-body {
  padding: 10px;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}
</style>
