<template>
  <AppLayout>
    <div class="developer-page">
      <!-- Hero -->
      <section class="hero-card">
        <div class="hero-info">
          <div class="hero-avatar">{{ authStore.userInfo?.name?.charAt(0) || 'D' }}</div>
          <div>
            <div class="hero-name">{{ authStore.userInfo?.name || '开发者' }}</div>
            <div class="hero-role">{{ roleLabel }}</div>
            <div class="hero-hint">专注处理您的开发任务与 Bug</div>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-pill"><span class="stat-num">{{ stats.total }}</span><span class="stat-label">任务总数</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#f59e0b">{{ stats.developing }}</span><span class="stat-label">开发中</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#d03050">{{ stats.overdue }}</span><span class="stat-label">逾期</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#d03050">{{ stats.pendingBugs }}</span><span class="stat-label">待修复Bug</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#18a058">{{ stats.done }}</span><span class="stat-label">已完成</span></div>
        </div>
      </section>

      <!-- Main split: Tasks left, Bugs right -->
      <div class="main-split">
        <section class="section-card task-section">
          <div class="section-header">
            <h3>待处理任务</h3>
            <n-input v-model:value="reqFilter" placeholder="输入需求编号筛选任务 / Bug" clearable size="small" style="width: 220px;" />
          </div>
          <TaskBoard :tasks="filteredBoardTasks" :column-keys="['pending', 'developing']" :skills-map="skillsMap" @status-change="onStatusChange" @task-click="onTaskClick" />
        </section>

        <section class="section-card bug-section">
          <div class="section-header"><h3>待处理 BUG</h3></div>
          <div v-if="filteredBugs.length === 0" class="empty-state">{{ reqFilter.trim() ? '没有匹配的 Bug' : '暂无待处理Bug' }}</div>
          <div v-for="bug in filteredBugs" :key="bug.id" class="bug-item" @click="onBugClick(bug)">
            <div class="bug-item-top">
              <span class="bug-title">{{ bug.title }}</span>
              <div class="bug-item-tags">
                <n-tag v-if="bug.terminal" size="tiny" round>{{ skillsMap[bug.terminal] || bug.terminal }}</n-tag>
                <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'" round>{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
              </div>
            </div>
             <div class="bug-item-meta">
               <n-tag size="tiny" :type="bugStatusMeta[bug.status]?.tone || 'default'">{{ bugStatusMeta[bug.status]?.label || bug.status }}</n-tag>
               <span v-if="bug.taskTitle" class="bug-task">关联: {{ bug.taskTitle }}</span>
               <span v-if="bug.expected_result" class="bug-expected">预期: {{ bug.expected_result.substring(0, 30) }}{{ bug.expected_result.length > 30 ? '...' : '' }}</span>
             </div>
          </div>
        </section>
      </div>

      <!-- 已完成任务 -->
      <section class="section-card completed-section">
        <n-collapse>
          <n-collapse-item title="已完成任务" name="completed">
            <template #header-extra>
              <span class="completed-count">{{ filteredCompletedTasks.length }} 项</span>
            </template>
            <div v-if="filteredCompletedTasks.length === 0" class="empty-state">{{ reqFilter.trim() ? '没有匹配的已完成任务' : '暂无已完成任务' }}</div>
            <div v-for="t in filteredCompletedTasks" :key="t.id" class="completed-item" @click="onTaskClick(t.id)">
              <div class="completed-item-top">
                <span class="completed-item-title">{{ t.title }}</span>
                <div class="completed-item-tags">
                  <n-tag v-if="t.terminal" size="tiny" round>{{ skillsMap[t.terminal] || t.terminal }}</n-tag>
                  <n-tag size="tiny" type="success" round>已完成</n-tag>
                </div>
              </div>
              <div class="completed-item-meta">
                <span class="completed-meta-text">{{ t.reqNumber || '-' }}</span>
                <span class="completed-meta-text">{{ t.system || '-' }}</span>
                <span class="completed-meta-text" v-if="t.deadline">截止: {{ t.deadline.substring(0, 10) }}</span>
              </div>
            </div>
          </n-collapse-item>
        </n-collapse>
      </section>
    </div>

    <TaskDetailDrawer v-model:show="showTaskDetail" :task-id="selectedTaskId" developer-mode @refresh="loadData" />
    <BugDetailDrawer v-model:show="showBugDetail" :bug-id="selectedBugId" @refresh="loadData" />
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { getDeveloperDashboard } from '@/api/statistics'
import { getDictionaries } from '@/api/dictionaries'
import { taskStatusMeta, priorityMeta, bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import TaskBoard from '@/components/TaskBoard.vue'
import TaskDetailDrawer from '@/components/TaskDetailDrawer.vue'
import BugDetailDrawer from '@/components/BugDetailDrawer.vue'
import AppLayout from '@/components/AppLayout.vue'
import { NTag, NInput, NCollapse, NCollapseItem } from 'naive-ui'

const authStore = useAuthStore()
const route = useRoute()

const dashData = ref({})
const skillsMap = ref({})
const showTaskDetail = ref(false)
const selectedTaskId = ref(null)
const showBugDetail = ref(false)
const selectedBugId = ref(null)
const reqFilter = ref('')

const roleLabel = computed(() => ({ dev_lead: '开发组长', dev: '开发', pm: '项目经理' })[authStore.userInfo?.role] || '')

const stats = computed(() => dashData.value.stats || { total: 0, developing: 0, done: 0, overdue: 0, pendingBugs: 0 })
const boardData = computed(() => dashData.value.board || [])
const bugs = computed(() => dashData.value.bugs || [])
const completedTasks = computed(() => dashData.value.completedTasks || [])

const boardTasks = computed(() => {
  const result = []
  for (const col of boardData.value) {
    if (col.tasks) result.push(...col.tasks)
  }
  return result
})

const filteredBoardTasks = computed(() => {
  if (!reqFilter.value.trim()) return boardTasks.value
  const kw = reqFilter.value.trim().toLowerCase()
  return boardTasks.value.filter(t => {
    const reqNum = (t.reqNumber || '').toString().toLowerCase()
    return reqNum.includes(kw)
  })
})

const filteredBugs = computed(() => {
  if (!reqFilter.value.trim()) return bugs.value
  const kw = reqFilter.value.trim().toLowerCase()
  return bugs.value.filter(b => {
    const reqNum = (b.reqNumber || '').toString().toLowerCase()
    return reqNum.includes(kw)
  })
})

const filteredCompletedTasks = computed(() => {
  if (!reqFilter.value.trim()) return completedTasks.value
  const kw = reqFilter.value.trim().toLowerCase()
  return completedTasks.value.filter(t => {
    const reqNum = (t.reqNumber || '').toString().toLowerCase()
    return reqNum.includes(kw)
  })
})

async function loadData() {
  try {
    dashData.value = await getDeveloperDashboard()
  } catch (e) { console.error(e) }
}

async function loadDictionaries() {
  try {
    const data = await getDictionaries() || []
    const map = {}
    for (const d of data) {
      if (d.dict_type === 'skill') map[d.dict_key] = d.dict_value
    }
    skillsMap.value = map
  } catch (e) { console.error(e) }
}

async function onStatusChange({ taskId, newStatus }) {
  const task = boardTasks.value.find(t => t.id === taskId)
  if (!task) return
  const oldLabel = taskStatusMeta[task.status]?.label || task.status
  const newLabel = taskStatusMeta[newStatus]?.label || newStatus
  try {
    const confirmed = await new Promise((resolve) => {
      window.$dialog?.warning({
        title: '确认变更任务状态',
        content: `将任务「${task.title || taskId}」从「${oldLabel}」变更为「${newLabel}」？`,
        positiveText: '确定',
        negativeText: '取消',
        onPositiveClick: () => resolve(true),
        onNegativeClick: () => resolve(false),
        onClose: () => resolve(false)
      })
    })
    if (!confirmed) return
    const { changeTaskStatus } = await import('@/api/tasks')
    await changeTaskStatus(taskId, { new_status: newStatus, comment: '' })
    await loadData()
  } catch (e) { console.error(e) }
}

function onTaskClick(taskId) {
  selectedTaskId.value = taskId
  showTaskDetail.value = true
}

function onBugClick(bug) {
  selectedBugId.value = bug.id
  showBugDetail.value = true
}

watch(() => route.query.bugId, (id) => {
  if (id) {
    selectedBugId.value = Number(id)
    showBugDetail.value = true
  }
}, { immediate: true })

watch(() => route.query.taskId, (id) => {
  if (id) {
    selectedTaskId.value = Number(id)
    showTaskDetail.value = true
  }
}, { immediate: true })

onMounted(() => { loadData(); loadDictionaries() })
</script>

<style scoped>
.developer-page { display: flex; flex-direction: column; gap: 16px; flex: 1; min-height: 0; overflow: hidden; }
.hero-card {
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
  padding: 22px 24px; border-radius: 18px;
  background: linear-gradient(135deg, #eef2ff 0%, #ffffff 55%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}
.hero-info { display: flex; align-items: center; gap: 14px; }
.hero-avatar {
  width: 48px; height: 48px; border-radius: 50%;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  color: white; display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700;
}
.hero-name { font-size: 18px; font-weight: 700; color: #0f172a; }
.hero-role { font-size: 12px; color: #6366f1; font-weight: 600; margin-top: 2px; }
.hero-hint { font-size: 12px; color: #94a3b8; margin-top: 4px; }
.hero-stats { display: flex; gap: 12px; }
.stat-pill {
  min-width: 80px; padding: 10px 14px; border-radius: 14px;
  background: white; border: 1px solid #e2e8f0; text-align: center;
}
.stat-num { display: block; font-size: 20px; font-weight: 700; color: #0f172a; }
.stat-label { font-size: 11px; color: #64748b; margin-top: 2px; }
.main-split { display: flex; gap: 16px; align-items: flex-start; }
.task-section { flex: 1; min-width: 0; }
.bug-section { flex: 1; min-width: 0; }
.section-card { background: white; border-radius: 16px; border: 1px solid #e2e8f0; padding: 16px; }
.section-header { margin-bottom: 12px; display: flex; align-items: center; justify-content: space-between; gap: 12px; }
.section-header h3 { margin: 0; font-size: 15px; font-weight: 700; color: #0f172a; }

.bug-item {
  padding: 10px 12px; border: 1px solid #f1f5f9; border-radius: 10px;
  cursor: pointer; transition: background 0.12s; margin-bottom: 6px;
}
.bug-item:hover { background: #f1f5f9; }
.bug-item-top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.bug-title { font-size: 13px; font-weight: 500; color: #0f172a; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bug-item-meta { display: flex; align-items: center; gap: 8px; margin-top: 6px; }
.bug-task { font-size: 11px; color: #94a3b8; }
.bug-expected { font-size: 11px; color: #6366f1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 200px; }
.empty-state { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; }
.completed-section { padding: 8px 16px; }
.completed-count { font-size: 12px; color: #94a3b8; font-weight: 500; }
.completed-item {
  padding: 10px 12px; border: 1px solid #f1f5f9; border-radius: 10px;
  cursor: pointer; transition: background 0.12s; margin-bottom: 6px;
}
.completed-item:hover { background: #f0fdf4; }
.completed-item-top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.completed-item-title { font-size: 13px; font-weight: 500; color: #0f172a; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.completed-item-tags { display: flex; align-items: center; gap: 6px; flex-shrink: 0; }
.completed-item-meta { display: flex; gap: 12px; margin-top: 6px; }
.completed-meta-text { font-size: 11px; color: #94a3b8; }
@media (max-width: 768px) {
  .hero-card { flex-direction: column; align-items: flex-start; }
  .hero-stats { flex-wrap: wrap; }
}
@media (max-width: 1024px) {
  .main-split { flex-direction: column; }
  .task-section, .bug-section { width: 100%; }
}
</style>
