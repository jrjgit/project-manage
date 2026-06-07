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
          <div class="stat-pill"><span class="stat-num" style="color:#2563eb">{{ stats.totalTesting }}</span><span class="stat-label">待测试</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#d03050">{{ stats.pendingVerify }}</span><span class="stat-label">待验证 Bug</span></div>
        </div>
      </section>

      <div class="main-split">
        <div class="left-panel">
          <section class="section-card">
            <div class="section-header"><h3>待测试任务（{{ tasks.length }}）</h3></div>
            <div v-if="tasks.length === 0" class="empty-state">暂无待测试任务</div>
            <div v-for="t in tasks" :key="t.id" class="task-item" @click="onTaskClick(t.id)">
              <div class="task-item-top">
                <span class="task-item-title">{{ t.title }}</span>
                <span class="task-item-assignee">{{ t.assignee }}</span>
              </div>
            </div>
          </section>
        </div>

        <div class="right-panel">
          <section class="section-card">
            <div class="section-header"><h3>待验证 BUG（{{ pendingVerifyBugs.length }}）</h3></div>
            <div v-if="pendingVerifyBugs.length === 0" class="empty-state">暂无待验证 Bug</div>
            <div v-for="bug in pendingVerifyBugs" :key="bug.id" class="bug-item" @click="onBugClick(bug)">
              <div class="bug-item-top">
                <span class="bug-title">{{ bug.title }}</span>
                <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'" round>{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
              </div>
              <div v-if="bug.taskTitle" class="bug-item-meta">
                <span class="bug-task">关联: {{ bug.taskTitle }}</span>
              </div>
            </div>
          </section>
        </div>
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
import { severityMeta } from '@/constants/statusMeta'
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

const stats = computed(() => dashData.value.stats || { totalTesting: 0, pendingVerify: 0 })
const tasks = computed(() => dashData.value.tasks || [])
const pendingVerifyBugs = computed(() => dashData.value.pendingVerifyBugs || [])

async function loadData() {
  try { dashData.value = await getTesterDashboard() } catch (e) { console.error(e) }
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
.hero-avatar { width:48px;height:48px;border-radius:50%;background:linear-gradient(135deg,#f59e0b,#fbbf24);color:white;display:flex;align-items:center;justify-content:center;font-size:20px;font-weight:700; }
.hero-name { font-size:18px;font-weight:700;color:#0f172a; }
.hero-role { font-size:12px;color:#f59e0b;font-weight:600;margin-top:2px; }
.hero-hint { font-size:12px;color:#94a3b8;margin-top:4px; }
.hero-stats { display:flex;gap:12px; }
.stat-pill { min-width:80px;padding:10px 14px;border-radius:14px;background:white;border:1px solid #e2e8f0;text-align:center; }
.stat-num { display:block;font-size:20px;font-weight:700; }
.stat-label { font-size:11px;color:#64748b;margin-top:2px; }
.main-split { display:flex;gap:16px;align-items:flex-start; }
.left-panel { flex:1;display:flex;flex-direction:column;gap:16px;min-width:0; }
.right-panel { flex:1;min-width:0; }
.section-card { background:white;border-radius:16px;border:1px solid #e2e8f0;padding:16px; }
.section-header { margin-bottom:12px; }
.section-header h3 { margin:0;font-size:15px;font-weight:700;color:#0f172a; }
.task-item { padding:10px 12px;border:1px solid #f1f5f9;border-radius:10px;margin-bottom:6px;cursor:pointer;transition:background .12s; }
.task-item:hover { background:#f1f5f9; }
.task-item-top { display:flex;align-items:center;justify-content:space-between;gap:8px;margin-bottom:8px; }
.task-item-title { font-size:13px;font-weight:500;color:#0f172a;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap; }
.task-item-assignee { font-size:11px;color:#94a3b8;white-space:nowrap; }
.task-item-actions { display:flex;gap:6px; }
.bug-item { padding:10px 12px;border:1px solid #f1f5f9;border-radius:10px;cursor:pointer;transition:background .12s;margin-bottom:6px; }
.bug-item:hover { background:#f1f5f9; }
.bug-item-top { display:flex;align-items:center;justify-content:space-between;gap:8px; }
.bug-title { font-size:13px;font-weight:500;color:#0f172a;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap; }
.bug-item-meta { margin-top:6px; }
.bug-task { font-size:11px;color:#94a3b8; }
.empty-state { text-align:center;padding:24px;color:#94a3b8;font-size:13px; }
</style>
