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
        </div>
      </section>

      <!-- Main split -->
      <div class="main-split">
        <div class="left-panel">
          <!-- Task Board -->
          <section class="section-card">
            <div class="section-header"><h3>待处理任务</h3></div>
            <TaskBoard :tasks="boardTasks" @status-change="onStatusChange" @task-click="onTaskClick" />
          </section>

          <!-- Bug List -->
          <section class="section-card">
            <div class="section-header"><h3>待处理 BUG</h3></div>
            <div v-if="bugs.length === 0" class="empty-state">暂无待处理Bug</div>
            <div v-for="bug in bugs" :key="bug.id" class="bug-item" @click="onBugClick(bug)">
              <div class="bug-item-top">
                <span class="bug-title">{{ bug.title }}</span>
                <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'" round>{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
              </div>
              <div class="bug-item-meta">
                <n-tag size="tiny" :type="bugStatusMeta[bug.status]?.tone || 'default'">{{ bugStatusMeta[bug.status]?.label || bug.status }}</n-tag>
                <span v-if="bug.taskTitle" class="bug-task">关联: {{ bug.taskTitle }}</span>
              </div>
            </div>
          </section>
        </div>

        <!-- Right Detail Panel -->
        <div class="right-panel">
          <section class="section-card detail-panel">
            <div v-if="!activeDetail" class="empty-hint">点击左侧任务或BUG查看详情</div>
            <template v-else-if="activeDetail.type === 'task'">
              <div class="detail-title">{{ activeDetail.title }}</div>
              <div class="detail-tags">
                <n-tag :type="taskStatusMeta[activeDetail.status]?.tone || 'default'" round>{{ taskStatusMeta[activeDetail.status]?.label || activeDetail.status }}</n-tag>
                <n-tag :type="priorityMeta[activeDetail.priority]?.tone || 'default'" round>{{ priorityMeta[activeDetail.priority]?.label || activeDetail.priority }}</n-tag>
              </div>
              <div class="detail-meta-grid">
                <div><span>项目</span><strong>{{ activeDetail.project?.name || '-' }}</strong></div>
                <div><span>指派人</span><strong>{{ activeDetail.assignee?.name || '-' }}</strong></div>
                <div><span>截止日期</span><strong>{{ formatDate(activeDetail.deadline) || '-' }}</strong></div>
                <div><span>绩效</span><strong>{{ activeDetail.performance || '-' }}</strong></div>
              </div>
              <div class="detail-section">
                <div class="detail-section-title">项目经理备注</div>
                <div class="detail-text">{{ activeDetail.requirement_desc || activeDetail.description || '暂无' }}</div>
              </div>
              <div v-if="activeDetail.requirement_desc" class="detail-section">
                <div class="detail-section-title">需求描述</div>
                <div class="detail-text">{{ activeDetail.requirement_desc }}</div>
              </div>
            </template>
            <template v-else>
              <div class="detail-title">{{ activeDetail.title }}</div>
              <div class="detail-tags">
                <n-tag :type="bugStatusMeta[activeDetail.status]?.tone || 'default'" round>{{ bugStatusMeta[activeDetail.status]?.label || activeDetail.status }}</n-tag>
                <n-tag :type="severityMeta[activeDetail.severity]?.tone || 'default'" round>{{ severityMeta[activeDetail.severity]?.label || activeDetail.severity }}</n-tag>
              </div>
              <div class="detail-meta-grid">
                <div><span>指派人</span><strong>{{ activeDetail.assignee?.name || '-' }}</strong></div>
                <div v-if="activeDetail.task"><span>关联任务</span><strong>{{ activeDetail.task?.title || '-' }}</strong></div>
              </div>
              <div class="detail-section">
                <div class="detail-section-title">Bug描述</div>
                <div class="detail-text">{{ activeDetail.description || '暂无' }}</div>
              </div>
              <div v-if="activeDetail.task?.description" class="detail-section">
                <div class="detail-section-title">任务描述</div>
                <div class="detail-text">{{ activeDetail.task.description }}</div>
              </div>
              <div v-if="activeDetail.task?.requirement_desc" class="detail-section">
                <div class="detail-section-title">需求描述</div>
                <div class="detail-text">{{ activeDetail.task.requirement_desc }}</div>
              </div>
              <div class="detail-actions" v-if="detailBugActions.length">
                <n-button v-for="act in detailBugActions" :key="act.status" :type="act.type" size="small" :loading="actionLoading" @click="executeBugAction(act)">{{ act.label }}</n-button>
              </div>
            </template>
          </section>
        </div>
      </div>

      <!-- Quick Links -->
      <section class="quick-links">
        <a v-for="link in quickLinks" :key="link.label" :href="link.url" target="_blank" rel="noopener" class="quick-link-item">
          <span class="quick-link-label">{{ link.label }}</span>
        </a>
      </section>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getDeveloperDashboard } from '@/api/statistics'
import { getTask, changeTaskStatus } from '@/api/tasks'
import { getBug, changeBugStatus } from '@/api/bugs'
import { taskStatusMeta, priorityMeta, bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import TaskBoard from '@/components/TaskBoard.vue'
import AppLayout from '@/components/AppLayout.vue'
import { NTag, NButton } from 'naive-ui'

const authStore = useAuthStore()

const dashData = ref({})
const activeDetail = ref(null)
const actionLoading = ref(false)

const roleLabel = computed(() => ({ dev_lead: '开发组长', dev: '开发' })[authStore.userInfo?.role] || '')

const stats = computed(() => dashData.value.stats || { total: 0, developing: 0, overdue: 0, pendingBugs: 0 })
const boardData = computed(() => dashData.value.board || [])
const bugs = computed(() => dashData.value.bugs || [])

const boardTasks = computed(() => {
  const result = []
  for (const col of boardData.value) {
    if (col.tasks) result.push(...col.tasks)
  }
  return result
})

const detailBugActions = computed(() => {
  if (activeDetail.value?.type !== 'bug') return []
  const status = activeDetail.value.status
  const actions = []
  if (status === 'assigned') actions.push({ label: '开始修复', status: 'fixing', type: 'primary' })
  if (status === 'fixing') actions.push({ label: '标记已修复', status: 'fixed', type: 'success' })
  return actions
})

const quickLinks = [
  { label: '登录地址', url: 'http://' + window.location.host },
  { label: '在线文档', url: '#' },
  { label: '千系人', url: '#' },
  { label: '系统配置参数', url: '#' },
  { label: '在线会议', url: '#' },
  { label: '代码GIT权限', url: '#' }
]

async function loadData() {
  try {
    dashData.value = await getDeveloperDashboard()
  } catch (e) { console.error(e) }
}

async function onStatusChange({ taskId, newStatus }) {
  try {
    await changeTaskStatus(taskId, { new_status: newStatus, comment: '' })
    await loadData()
    if (activeDetail.value?.id === taskId && activeDetail.value?.type === 'task') {
      activeDetail.value = null
    }
  } catch (e) { console.error(e) }
}

async function onTaskClick(taskId) {
  try {
    const res = await getTask(taskId)
    activeDetail.value = { type: 'task', ...res.task }
  } catch (e) { console.error(e) }
}

async function onBugClick(bug) {
  try {
    const res = await getBug(bug.id)
    activeDetail.value = { type: 'bug', ...res }
  } catch (e) { console.error(e) }
}

async function executeBugAction(action) {
  if (!activeDetail.value || activeDetail.value.type !== 'bug') return
  actionLoading.value = true
  try {
    await changeBugStatus(activeDetail.value.id, { new_status: action.status, comment: '' })
    await loadData()
    activeDetail.value = null
  } catch (e) { console.error(e) }
  actionLoading.value = false
}

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`
}

onMounted(loadData)
</script>

<style scoped>
.developer-page { display: flex; flex-direction: column; gap: 16px; }
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
.left-panel { flex: 3; display: flex; flex-direction: column; gap: 16px; min-width: 0; }
.right-panel { flex: 2; min-width: 0; position: sticky; top: 16px; }
.section-card { background: white; border-radius: 16px; border: 1px solid #e2e8f0; padding: 16px; }
.section-header { margin-bottom: 12px; }
.section-header h3 { margin: 0; font-size: 15px; font-weight: 700; color: #0f172a; }
.detail-panel { min-height: 300px; }
.empty-hint { text-align: center; color: #94a3b8; font-size: 13px; padding: 40px 0; }
.detail-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 10px; }
.detail-tags { display: flex; gap: 8px; margin-bottom: 12px; }
.detail-meta-grid {
  display: grid; grid-template-columns: 1fr 1fr; gap: 8px;
  padding: 12px; background: #f8fafc; border-radius: 10px; margin-bottom: 12px;
}
.detail-meta-grid div { font-size: 12px; color: #64748b; display: flex; justify-content: space-between; gap: 8px; }
.detail-meta-grid strong { color: #0f172a; }
.detail-section { margin-bottom: 12px; }
.detail-section-title { font-size: 12px; font-weight: 600; color: #64748b; margin-bottom: 4px; }
.detail-text { font-size: 13px; color: #334155; line-height: 1.7; white-space: pre-wrap; }
.detail-actions { display: flex; gap: 8px; margin-top: 12px; }

.bug-item {
  padding: 10px 12px; border: 1px solid #f1f5f9; border-radius: 10px;
  cursor: pointer; transition: background 0.12s; margin-bottom: 6px;
}
.bug-item:hover { background: #f1f5f9; }
.bug-item-top { display: flex; align-items: center; justify-content: space-between; gap: 8px; }
.bug-title { font-size: 13px; font-weight: 500; color: #0f172a; flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bug-item-meta { display: flex; align-items: center; gap: 8px; margin-top: 6px; }
.bug-task { font-size: 11px; color: #94a3b8; }

.quick-links {
  display: flex; flex-wrap: wrap; gap: 8px;
  padding: 14px 16px; border-radius: 14px;
  background: #f8fafc; border: 1px solid #e2e8f0;
}
.quick-link-item {
  padding: 6px 14px; border-radius: 8px;
  background: white; border: 1px solid #e2e8f0;
  color: #6366f1; font-size: 12px; font-weight: 500;
  text-decoration: none; transition: all 0.12s;
}
.quick-link-item:hover { background: #eef2ff; border-color: #6366f1; }

.empty-state { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; }
</style>
