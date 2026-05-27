<template>
  <AppLayout>
    <div class="dashboard-page">
      <!-- Hero Greeting -->
      <section class="hero-card">
        <div class="hero-top">
          <div>
            <div class="hero-kicker">{{ roleLabel }} · {{ greeting }}</div>
            <h2 class="hero-title">{{ userName }}，欢迎回来</h2>
            <p class="hero-subtitle">{{ roleHint }}</p>
          </div>
        </div>
        <div class="hero-stats">
          <div v-for="s in statItems" :key="s.label" class="stat-pill">
            <span class="stat-value" :style="{color:s.color}">{{ s.value }}</span>
            <span class="stat-label">{{ s.label }}</span>
          </div>
        </div>
      </section>

      <!-- PM Dashboard -->
      <template v-if="authStore.isPM">
        <section class="section-card">
          <div class="section-header"><h3>待办事项</h3></div>
          <div class="todo-list">
            <div v-if="stats.pendingTask > 0" class="todo-item">
              <span class="todo-icon">📋</span>
              <span>{{ stats.pendingTask }} 个需求任务待分配，<a @click="$router.push('/requirements')" style="color:#6366f1">前往处理</a></span>
            </div>
            <div v-if="stats.pendingRelease > 0" class="todo-item">
              <span class="todo-icon">🚀</span>
              <span>{{ stats.pendingRelease }} 个需求待发布，<a @click="$router.push('/requirements')" style="color:#6366f1">前往处理</a></span>
            </div>
            <div v-if="stats.overdueTasks > 0" class="todo-item">
              <span class="todo-icon">⚠️</span>
              <span>{{ stats.overdueTasks }} 个任务已逾期，<a @click="$router.push('/tasks')" style="color:#ef4444">查看详情</a></span>
            </div>
            <div v-if="!stats.pendingTask && !stats.pendingRelease && !stats.overdueTasks" class="empty-state">暂无待办事项</div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-header"><h3>逾期任务</h3></div>
          <div v-if="overdueTasks.length" class="compact-list">
            <div v-for="t in overdueTasks" :key="t.id" class="compact-item" @click="$router.push(`/tasks?taskId=${t.id}`)">
              <span class="compact-title">{{ t.title }}</span>
              <span class="compact-meta" style="color:#ef4444">逾期{{ t.overdue_days }}天</span>
              <span class="compact-meta">{{ t.assignee }}</span>
            </div>
          </div>
          <div v-else class="empty-state">暂无逾期任务</div>
        </section>

        <section class="section-card">
          <div class="section-header"><h3>需求管道</h3></div>
          <div class="pipeline">
            <div class="pipe-item"><span class="pipe-label">任务待分配</span><span class="pipe-value">{{ pipeline.pendingTask || 0 }}</span></div>
            <div class="pipe-arrow">→</div>
            <div class="pipe-item"><span class="pipe-label">开发中</span><span class="pipe-value">{{ pipeline.inProgress || 0 }}</span></div>
            <div class="pipe-arrow">→</div>
            <div class="pipe-item"><span class="pipe-label">测试中</span><span class="pipe-value">{{ pipeline.testing || 0 }}</span></div>
            <div class="pipe-arrow">→</div>
            <div class="pipe-item"><span class="pipe-label">待发布</span><span class="pipe-value">{{ pipeline.pendingRelease || 0 }}</span></div>
          </div>
        </section>
      </template>

      <!-- Dev Lead Dashboard -->
      <template v-if="authStore.isDevLead">
        <section class="section-card">
          <div class="section-header"><h3>团队任务概览</h3></div>
          <div class="pipeline">
            <div v-for="col in board" :key="col.status" class="pipe-item">
              <span class="pipe-label">{{ col.label }}</span>
              <span class="pipe-value">{{ col.count }}</span>
            </div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-header"><h3>人员负载</h3></div>
          <div class="workload-grid">
            <div v-for="w in workload" :key="w.name" class="workload-item">
              <span class="wl-name">{{ w.name }}</span>
              <span class="wl-count">共{{ w.total }} 开发中{{ w.developing }} 已完成{{ w.done }}</span>
            </div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-header"><h3>逾期任务</h3></div>
          <div v-if="overdueTasks.length" class="compact-list">
            <div v-for="t in overdueTasks" :key="t.id" class="compact-item" @click="$router.push(`/tasks?taskId=${t.id}`)">
              <span class="compact-title">{{ t.title }}</span>
              <span class="compact-meta" style="color:#ef4444">逾期{{ t.overdue_days }}天</span>
              <span class="compact-meta">{{ t.assignee }}</span>
            </div>
          </div>
          <div v-else class="empty-state">团队暂无逾期任务</div>
        </section>
      </template>

      <!-- Dev Dashboard -->
      <template v-if="authStore.isDev">
        <section class="section-card">
          <div class="section-header"><h3>我的任务</h3></div>
          <div v-if="myTasks.length" class="compact-list">
            <div v-for="t in myTasks" :key="t.id" class="compact-item" @click="$router.push(`/tasks?taskId=${t.id}`)">
              <div class="compact-left">
                <span class="compact-title">{{ t.title }}</span>
                <span class="compact-meta">指派: {{ t.assignee }}</span>
              </div>
              <div class="compact-right">
                <n-tag v-if="t.overdue" type="error" size="tiny" round>逾期{{ t.overdue_days }}天</n-tag>
                <n-tag :type="taskStatusMeta[t.status]?.tone || 'default'" size="tiny" round>{{ taskStatusMeta[t.status]?.label || t.status }}</n-tag>
              </div>
            </div>
          </div>
          <div v-else class="empty-state">暂无任务</div>
        </section>
      </template>

      <!-- Tester Dashboard -->
      <template v-if="authStore.isTester || authStore.isTesterLead">
        <section class="section-card">
          <div class="section-header"><h3>待综合测试的任务</h3></div>
          <div v-if="testingTasks.length" class="compact-list">
            <div v-for="t in testingTasks" :key="t.id" class="compact-item" @click="$router.push(`/tasks?taskId=${t.id}`)">
              <span class="compact-title">{{ t.title }}</span>
              <span class="compact-meta">{{ t.assignee }}</span>
            </div>
          </div>
          <div v-else class="empty-state">暂无待测试任务</div>
        </section>

        <section class="section-card">
          <div class="section-header"><h3>我的 Bug</h3></div>
          <div v-if="bugs.length" class="compact-list">
            <div v-for="b in bugs" :key="b.id" class="compact-item" @click="$router.push(`/bugs`)">
              <span class="compact-title">{{ b.title }}</span>
              <n-tag :type="bugStatusMeta[b.status]?.tone || 'default'" size="tiny" round>{{ bugStatusMeta[b.status]?.label || b.status }}</n-tag>
            </div>
          </div>
          <div v-else class="empty-state">暂无 Bug</div>
        </section>
      </template>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getDashboard } from '@/api/statistics'
import { taskStatusMeta, bugStatusMeta } from '@/constants/statusMeta'
import AppLayout from '@/components/AppLayout.vue'
import { NTag } from 'naive-ui'

const authStore = useAuthStore()
const dashData = ref({})
const d = dashData

const roleLabel = computed(() => {
  const map = { pm: '项目经理', dev_lead: '开发组长', dev: '开发', tester_lead: '测试组长', tester: '测试' }
  return map[authStore.userInfo?.role] || ''
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const userName = computed(() => authStore.userInfo?.name || '')

const roleHint = computed(() => {
  const map = {
    pm: '关注需求整体进展、任务分配与发布计划。',
    dev_lead: '关注团队任务负载、开发进度与风险。',
    dev: '关注我的开发任务，按时完成交付。',
    tester: '关注待测试任务与 Bug 验证。',
    tester_lead: '关注测试进度与团队 Bug 情况。'
  }
  return map[authStore.userInfo?.role] || ''
})

const stats = computed(() => d.value.stats || {})
const overdueTasks = computed(() => d.value.overdueTasks || [])
const pipeline = computed(() => d.value.pipeline || {})
const board = computed(() => d.value.board || [])
const workload = computed(() => d.value.workload || [])
const myTasks = computed(() => d.value.tasks || [])
const testingTasks = computed(() => d.value.testingTasks || [])
const bugs = computed(() => d.value.bugs || [])

const statItems = computed(() => {
  const s = stats.value
  if (authStore.isPM) return [
    { label: '需求总数', value: s.totalRequirements || 0, color: '#6366f1' },
    { label: '进行中', value: s.inProgress || 0, color: '#3b82f6' },
    { label: '待发布', value: s.pendingRelease || 0, color: '#f59e0b' },
    { label: '逾期任务', value: s.overdueTasks || 0, color: '#ef4444' },
  ]
  if (authStore.isDevLead) return [
    { label: '团队任务', value: s.total || 0, color: '#6366f1' },
    { label: '开发中', value: s.developing || 0, color: '#3b82f6' },
    { label: '已完成', value: s.done || 0, color: '#10b981' },
    { label: '逾期', value: s.overdue || 0, color: '#ef4444' },
  ]
  if (authStore.isDev) return [
    { label: '我的任务', value: s.total || 0, color: '#6366f1' },
    { label: '待受理', value: s.pending || 0, color: '#94a3b8' },
    { label: '开发中', value: s.developing || 0, color: '#f59e0b' },
    { label: '已完成', value: s.done || 0, color: '#10b981' },
    { label: '逾期', value: s.overdue || 0, color: '#ef4444' },
  ]
  if (authStore.isTester || authStore.isTesterLead) return [
    { label: '待测试', value: s.pendingTest || 0, color: '#3b82f6' },
    { label: '测试中', value: s.testing || 0, color: '#f59e0b' },
    { label: '待验证Bug', value: s.bugPendingVerify || 0, color: '#ef4444' },
    { label: '我的Bug', value: s.bugTotal || 0, color: '#6366f1' },
  ]
  return []
})

async function loadData() {
  try {
    dashData.value = await getDashboard()
  } catch (e) {
    console.error(e)
  }
}

onMounted(loadData)
</script>

<style scoped>
.dashboard-page { display: flex; flex-direction: column; gap: 20px; }
.hero-card {
  padding: 24px 28px; border-radius: 20px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 55%, #312e81 100%);
  color: white; display: flex; justify-content: space-between; gap: 20px;
}
.hero-top { flex: 1; }
.hero-kicker { font-size: 12px; font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase; color: rgba(255,255,255,0.6); margin-bottom: 8px; }
.hero-title { margin: 0; font-size: 24px; font-weight: 700; }
.hero-subtitle { margin: 8px 0 0; font-size: 14px; color: rgba(255,255,255,0.75); }
.hero-stats { display: flex; gap: 12px; align-items: flex-start; }
.stat-pill { min-width: 80px; text-align: center; padding: 12px 14px; background: rgba(255,255,255,0.08); border-radius: 14px; border: 1px solid rgba(255,255,255,0.1); }
.stat-value { display: block; font-size: 26px; font-weight: 700; }
.stat-label { display: block; margin-top: 2px; font-size: 11px; color: rgba(255,255,255,0.7); }
.section-card { background: white; border-radius: 18px; border: 1px solid #e2e8f0; padding: 18px; }
.section-header { margin-bottom: 14px; }
.section-header h3 { margin: 0; font-size: 16px; font-weight: 700; color: #0f172a; }
.todo-list { display: flex; flex-direction: column; gap: 10px; }
.todo-item { display: flex; align-items: center; gap: 10px; padding: 10px 14px; background: #f8fafc; border-radius: 10px; font-size: 14px; }
.todo-icon { font-size: 18px; }
.pipeline { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; }
.pipe-item { flex: 1; min-width: 80px; text-align: center; padding: 14px; background: #f8fafc; border-radius: 12px; }
.pipe-label { display: block; font-size: 12px; color: #64748b; margin-bottom: 4px; }
.pipe-value { display: block; font-size: 28px; font-weight: 700; color: #0f172a; }
.pipe-arrow { font-size: 20px; color: #94a3b8; }
.compact-list { display: flex; flex-direction: column; gap: 6px; }
.compact-item {
  display: flex; align-items: center; gap: 12px; padding: 10px 14px;
  background: #f8fafc; border-radius: 10px; cursor: pointer; font-size: 13px;
  transition: background 0.15s;
}
.compact-item:hover { background: #eef2ff; }
.compact-title { flex: 1; font-weight: 600; color: #0f172a; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.compact-meta { color: #64748b; white-space: nowrap; font-size: 12px; }
.compact-left { flex: 1; display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.compact-right { display: flex; gap: 6px; flex-shrink: 0; }
.workload-grid { display: flex; flex-wrap: wrap; gap: 10px; }
.workload-item { padding: 12px 16px; background: #f8fafc; border-radius: 10px; min-width: 180px; }
.wl-name { display: block; font-size: 14px; font-weight: 600; color: #0f172a; }
.wl-count { display: block; margin-top: 4px; font-size: 12px; color: #64748b; }
.empty-state { padding: 20px; text-align: center; font-size: 13px; color: #94a3b8; }
@media (max-width: 900px) {
  .hero-card { flex-direction: column; }
  .hero-stats { flex-wrap: wrap; }
  .pipeline { flex-wrap: wrap; }
  .pipe-item { min-width: 60px; }
}
</style>
