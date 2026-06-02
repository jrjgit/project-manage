<template>
  <AppLayout>
    <template #actions></template>
    <div class="tasks-page">
      <section class="toolbar section-card">
        <div class="toolbar-row">
          <div class="toolbar-left">
            <h2 class="toolbar-title">我的任务</h2>
            <span class="toolbar-count">{{ tasks.length }}</span>
          </div>
          <div class="toolbar-right">
            <n-radio-group v-model:value="viewMode" size="small">
              <n-radio-button value="board">看板</n-radio-button>
              <n-radio-button value="list">列表</n-radio-button>
            </n-radio-group>
          </div>
        </div>
        <div class="toolbar-row secondary">
          <div class="filter-controls">
            <n-select v-model:value="filterProject" :options="projectOptions" placeholder="项目" clearable style="width: 140px;" size="small" />
            <n-select v-model:value="filterStatus" :options="statusOptions" placeholder="状态" clearable style="width: 130px;" size="small" />
            <n-select v-model:value="filterPriority" :options="priorityOptions" placeholder="优先级" clearable style="width: 130px;" size="small" />
            <n-button size="small" quaternary @click="resetFilters">重置</n-button>
            <n-button size="small" quaternary @click="loadTasks">
              <template #icon><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg></template>
            </n-button>
          </div>
        </div>
      </section>

      <TaskBoard
        v-if="viewMode === 'board'"
        :tasks="filteredTasks"
        @status-change="onStatusChange"
        @task-click="openDetail"
      />

      <div v-else class="list-container section-card">
        <n-data-table
          :columns="columns"
          :data="filteredTasks"
          :pagination="{ pageSize: 10 }"
          :bordered="false"
          :single-line="false"
          striped
        />
      </div>

      <TaskDetailDrawer
        v-model:show="showDetailDrawer"
        :task-id="selectedTaskId"
        @status-change="onStatusChange"
        @refresh="loadTasks"
      />
    </div>
  </AppLayout>
</template>

<script setup>
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getTasks, changeTaskStatus } from '@/api/tasks'
import { getProjects } from '@/api/projects'
import TaskBoard from '@/components/TaskBoard.vue'
import TaskDetailDrawer from '@/components/TaskDetailDrawer.vue'
import AppLayout from '@/components/AppLayout.vue'
import { priorityMeta, taskStatusMeta } from '@/constants/statusMeta'
import { NButton, NSelect, NRadioGroup, NRadioButton, NDataTable, NTag } from 'naive-ui'

const route = useRoute()
const router = useRouter()

const viewMode = ref('board')
const tasks = ref([])
const projects = ref([])
const showDetailDrawer = ref(false)
const selectedTaskId = ref(null)

const filterProject = ref(null)
const filterStatus = ref(null)
const filterPriority = ref(null)

const projectOptions = computed(() => projects.value.map((project) => ({ label: project.name, value: project.id })))
const statusOptions = Object.entries(taskStatusMeta).map(([value, meta]) => ({ label: meta.label, value }))
const priorityOptions = Object.entries(priorityMeta).map(([value, meta]) => ({ label: meta.label, value }))

const filteredTasks = computed(() => {
  return tasks.value.filter((task) => {
    if (filterProject.value && task.project_id !== filterProject.value) return false
    if (filterStatus.value && task.status !== filterStatus.value) return false
    if (filterPriority.value && task.priority !== filterPriority.value) return false
    return true
  })
})

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '标题', key: 'title', ellipsis: { tooltip: true }, minWidth: 160 },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      const meta = taskStatusMeta[row.status] || { label: row.status, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  {
    title: '优先级', key: 'priority', width: 80,
    render(row) {
      const meta = priorityMeta[row.priority] || { label: row.priority, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  { title: '指派人', key: 'assignee', width: 100, render(row) { return row.assignee?.name || '-' } },
  { title: '技能', key: 'terminal', width: 90, render(row) { return row.terminal || '-' } },
  { title: '进度', key: 'progress', width: 80, render(row) { return row.progress != null ? row.progress + '%' : '-' } },
  { title: '绩效工时', key: 'performance', width: 80, render(row) { return row.performance || '-' } },
  { title: '截止时间', key: 'deadline', width: 100, render(row) { return row.deadline ? formatDateShort(row.deadline) : '-' } },
  { title: '项目', key: 'project', width: 120, render(row) { return row.project?.name || '-' } },
  {
    title: '操作', key: 'actions', width: 80,
    render(row) {
      return h(NButton, { size: 'small', type: 'primary', ghost: true, round: true, onClick: () => openDetail(row.id) }, { default: () => '详情' })
    }
  }
]

function formatDateShort(d) {
  if (!d) return ''
  const date = new Date(d)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

function resetFilters() {
  filterProject.value = null
  filterStatus.value = null
  filterPriority.value = null
}

async function loadTasks() {
  try {
    tasks.value = await getTasks()
  } catch (error) {
    window.$message?.error('加载任务失败')
  }
}

async function loadProjects() {
  try {
    projects.value = await getProjects()
  } catch (error) {
    window.$message?.error('加载项目失败')
  }
}

async function onStatusChange({ taskId, newStatus, comment }) {
  // TaskDetailDrawer 已经调用了 changeTaskStatus，这里只需要刷新列表
  loadTasks()
}

function openDetail(id) {
  selectedTaskId.value = id
  showDetailDrawer.value = true
}

// 通过路由参数打开任务详情（消息跳转）
watch(() => route.params.id, (id) => {
  if (id) {
    openDetail(Number(id))
  }
}, { immediate: true })

watch(
  () => route.query.mode,
  (mode) => {
    if (mode === 'list' || mode === 'board') {
      viewMode.value = mode
    }
  },
  { immediate: true }
)

watch(viewMode, (mode) => {
  router.replace({ path: '/tasks', query: { ...route.query, mode } })
})

onMounted(() => {
  loadTasks()
  loadProjects()
})
</script>

<style scoped>
.tasks-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  background: white;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  padding: 20px;
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

.toolbar-row.secondary {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f1f5f9;
  flex-wrap: wrap;
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

.toolbar-count {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 500;
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 999px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.create-btn {
  border-radius: 8px;
  font-weight: 600;
}
  font-size: 12px;
}

.list-container {
  padding: 8px;
}

@media (max-width: 1100px) {
  .toolbar-row,
  .toolbar-row.secondary {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-right,
  .filter-controls {
    flex-wrap: wrap;
  }
}
</style>
