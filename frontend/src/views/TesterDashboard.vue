<template>
  <AppLayout>
    <div class="tester-page">
      <section class="hero-card">
        <div class="hero-info">
          <div class="hero-avatar">{{ authStore.userInfo?.name?.charAt(0) || 'T' }}</div>
          <div>
            <div class="hero-name">{{ authStore.userInfo?.name || '测试' }}</div>
            <div class="hero-role">测试</div>
            <div class="hero-hint">查看待测试任务并验证 Bug</div>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-pill"><span class="stat-num">{{ stats.totalTesting }}</span><span class="stat-label">待测试</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#6366f1">{{ stats.pickedByMe }}</span><span class="stat-label">我已接取</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#d03050">{{ stats.pendingVerify }}</span><span class="stat-label">待验证 Bug</span></div>
        </div>
      </section>

      <div class="main-split">
        <section class="section-card task-section">
          <div class="section-header"><h3>待测试任务</h3></div>
          <TaskBoard :tasks="tasks" :column-keys="['testing']" @status-change="onStatusChange" @task-click="onTaskClick" />
        </section>

        <section class="section-card bug-section">
          <div class="section-header"><h3>待验证 BUG</h3></div>
          <div v-if="pendingVerifyBugs.length === 0" class="empty-state">暂无待验证 Bug</div>
          <div v-for="bug in pendingVerifyBugs" :key="bug.id" class="bug-item" @click="onBugClick(bug)">
            <div class="bug-item-top">
              <span class="bug-title">{{ bug.title }}</span>
              <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'" round>{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
            </div>
            <div class="bug-item-meta">
              <span v-if="bug.taskTitle" class="bug-task">关联: {{ bug.taskTitle }}</span>
            </div>
          </div>
        </section>
      </div>
    </div>

    <TaskDetailDrawer v-model:show="showTaskDetail" :task-id="selectedTaskId" @refresh="loadData" />
    <BugDetailDrawer v-model:show="showBugDetail" :bug-id="selectedBugId" @refresh="loadData" />
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getTesterDashboard } from '@/api/statistics'
import { taskStatusMeta, bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import TaskBoard from '@/components/TaskBoard.vue'
import TaskDetailDrawer from '@/components/TaskDetailDrawer.vue'
import BugDetailDrawer from '@/components/BugDetailDrawer.vue'
import AppLayout from '@/components/AppLayout.vue'
import { NTag } from 'naive-ui'

const authStore = useAuthStore()

const dashData = ref({})
const showTaskDetail = ref(false)
const selectedTaskId = ref(null)
const showBugDetail = ref(false)
const selectedBugId = ref(null)

const stats = computed(() => dashData.value.stats || { totalTesting: 0, pickedByMe: 0, pendingVerify: 0 })
const tasks = computed(() => dashData.value.tasks || [])
const pendingVerifyBugs = computed(() => dashData.value.pendingVerifyBugs || [])

async function loadData() {
  try { dashData.value = await getTesterDashboard() } catch (e) { console.error(e) }
}

async function onStatusChange({ taskId, newStatus }) {
  const { changeTaskStatus } = await import('@/api/tasks')
  try {
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

onMounted(loadData)
</script>

<style scoped>
.tester-page { display: flex; flex-direction: column; gap: 16px; }
.hero-card {
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
  padding: 22px 24px; border-radius: 18px;
  background: linear-gradient(135deg, #fefce8 0%, #ffffff 55%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}
.hero-info { display: flex; align-items: center; gap: 14px; }
.hero-avatar {
  width: 48px; height: 48px; border-radius: 50%;
  background: linear-gradient(135deg, #f59e0b, #fbbf24);
  color: white; display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700;
}
.hero-name { font-size: 18px; font-weight: 700; color: #0f172a; }
.hero-role { font-size: 12px; color: #f59e0b; font-weight: 600; margin-top: 2px; }
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
.section-header { margin-bottom: 12px; }
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
.empty-state { text-align: center; padding: 24px; color: #94a3b8; font-size: 13px; }
</style>
