<template>
  <AppLayout>
    <div class="dashboard-page">
      <section class="hero-card">
        <div>
          <div class="hero-eyebrow">执行工作台</div>
          <h2 class="hero-title">{{ heroTitle }}</h2>
          <p class="hero-subtitle">{{ heroSubtitle }}</p>
        </div>
        <div class="hero-meta">
          <div class="meta-pill">
            <span class="meta-value">{{ tasks.length }}</span>
            <span class="meta-label">任务</span>
          </div>
          <div class="meta-pill">
            <span class="meta-value">{{ bugs.length }}</span>
            <span class="meta-label">Bug</span>
          </div>
        </div>
      </section>

      <section class="action-section">
        <div class="section-heading">
          <div>
            <div class="section-kicker">现在该做什么</div>
            <h3>优先入口</h3>
          </div>
        </div>
        <div class="action-grid">
          <button
            v-for="action in primaryActions"
            :key="action.key"
            class="action-card"
            type="button"
            @click="goAction(action)"
          >
            <div class="action-card-top">
              <div class="action-icon" :class="action.theme">
                <component :is="action.icon" />
              </div>
              <span class="action-count">{{ action.count }}</span>
            </div>
            <div class="action-title">{{ action.title }}</div>
            <div class="action-description">{{ action.description }}</div>
            <div class="action-link">打开 {{ action.targetLabel }} →</div>
          </button>
        </div>
      </section>

      <section class="overview-grid">
        <div class="overview-main section-card">
          <div class="section-header">
            <div>
              <div class="section-kicker">我的焦点</div>
              <h3>需要跟进的任务</h3>
            </div>
            <router-link :to="taskListLink" class="view-all">查看任务 →</router-link>
          </div>
          <div class="focus-list">
            <button
              v-for="task in focusTasks"
              :key="task.id"
              class="focus-row"
              type="button"
              @click="goTasks(taskListQuery)"
            >
              <div class="focus-main">
                <div class="focus-title">{{ task.title }}</div>
                <div class="focus-meta">
                  <span>{{ task.assignee?.name || task.tester?.name || '未指派' }}</span>
                  <span v-if="task.project?.name">{{ task.project.name }}</span>
                </div>
              </div>
              <div class="focus-side">
                <span class="status-chip" :style="statusStyle(taskStatusMeta[task.status]?.color)">
                  {{ taskStatusMeta[task.status]?.label || task.status }}
                </span>
              </div>
            </button>
            <div v-if="focusTasks.length === 0" class="empty-state">当前没有需要立即跟进的任务。</div>
          </div>
        </div>

        <div class="overview-side">
          <div class="section-card compact-card">
            <div class="section-header compact-header">
              <div>
                <div class="section-kicker">默认视角</div>
                <h3>任务入口</h3>
              </div>
            </div>
            <div class="view-list">
              <button
                v-for="view in taskViews"
                :key="view.key"
                class="view-row"
                type="button"
                @click="goTasks(buildViewQuery(view))"
              >
                <span>{{ view.label }}</span>
                <span class="view-row-arrow">→</span>
              </button>
            </div>
          </div>

          <div class="section-card compact-card">
            <div class="section-header compact-header">
              <div>
                <div class="section-kicker">待验证 / 待修复</div>
                <h3>Bug 焦点</h3>
              </div>
              <router-link :to="bugListLink" class="view-all">查看 Bug →</router-link>
            </div>
            <div class="focus-list compact-list">
              <button
                v-for="bug in focusBugs"
                :key="bug.id"
                class="focus-row compact-row"
                type="button"
                @click="goBugs(bugListQuery)"
              >
                <div class="focus-main">
                  <div class="focus-title">{{ bug.title }}</div>
                  <div class="focus-meta">
                    <span>{{ bug.assignee?.name || bug.creator?.name || '未指派' }}</span>
                  </div>
                </div>
                <div class="focus-side">
                  <span class="status-chip" :style="statusStyle(bugStatusMeta[bug.status]?.color)">
                    {{ bugStatusMeta[bug.status]?.label || bug.status }}
                  </span>
                </div>
              </button>
              <div v-if="focusBugs.length === 0" class="empty-state compact-empty">当前没有需要优先处理的 Bug。</div>
            </div>
          </div>
        </div>
      </section>

      <section v-if="authStore.isDevLead" class="my-team-section section-card">
        <div class="section-header">
          <div>
            <div class="section-kicker">我的团队</div>
            <h3>组内成员</h3>
          </div>
        </div>
        <div class="team-grid" v-if="teamMembers.length > 0">
          <div v-for="m in teamMembers" :key="m.id" class="team-member-chip">
            <span class="team-member-name">{{ m.name }}</span>
            <span class="team-member-role">{{ roleMap[m.role] }}</span>
          </div>
        </div>
        <div v-else class="empty-state">暂无团队数据。</div>
      </section>
    </div>
  </AppLayout>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { getTasks } from '@/api/tasks'
import { getBugs } from '@/api/bugs'
import { getMyTeam } from '@/api/groups'
import AppLayout from '@/components/AppLayout.vue'
import { taskPrimaryViews } from '@/constants/taskViews'
import { bugPrimaryViews } from '@/constants/bugViews'
import { bugStatusMeta, taskStatusMeta } from '@/constants/statusMeta'
import { buildViewQuery } from '@/utils/viewState'

const router = useRouter()
const authStore = useAuthStore()
const tasks = ref([])
const bugs = ref([])
const teamMembers = ref([])

const role = computed(() => authStore.role)
const roleMap = { pm: '项目经理', dev_lead: '开发组长', dev: '开发', tester_lead: '测试组长', tester: '测试' }
const taskViews = computed(() => taskPrimaryViews[role.value] || taskPrimaryViews.dev || [])
const bugViews = computed(() => bugPrimaryViews[role.value] || bugPrimaryViews.dev || [])

const defaultTaskView = computed(() => taskViews.value[0] || { key: 'all', label: '全部任务', params: {} })
const defaultBugView = computed(() => bugViews.value[0] || { key: 'all', label: '全部 Bug', params: {} })

const taskListQuery = computed(() => buildViewQuery(defaultTaskView.value))
const bugListQuery = computed(() => buildViewQuery(defaultBugView.value))
const taskListLink = computed(() => ({ path: '/tasks', query: taskListQuery.value }))
const bugListLink = computed(() => ({ path: '/bugs', query: bugListQuery.value }))

const heroTitleMap = {
  pm: '优先处理阻塞与分派，让项目继续流动。',
  dev_lead: '先把本组任务分出去，再盯住正在开发的项。',
  dev: '先处理你手上的待办，再回看被打回和重新打开的问题。',
  tester_lead: '先分配测试入口，再清理验证池中的积压。',
  tester: '先完成待验证项，再补充必要的缺陷反馈。'
}

const heroSubtitleMap = {
  pm: '顶部入口按你的角色聚合了最常进入的任务与 Bug 视图，下面保留当前最值得跟进的具体事项。',
  dev_lead: '工作台优先展示你最常用的接手、分派和回看入口，减少在列表里找目标的成本。',
  dev: '这里不再先给总览数字，而是直接把你下一步最可能要打开的工作入口放在前面。',
  tester_lead: '测试池和验证焦点被前置，方便你先做流转，再看整体情况。',
  tester: '把待验证和我创建的缺陷放在最前面，方便直接进入执行。'
}

const heroTitle = computed(() => heroTitleMap[role.value] || '先打开最重要的工作入口。')
const heroSubtitle = computed(() => heroSubtitleMap[role.value] || '这里会根据你的角色优先展示最常用的执行入口。')

function TaskIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('rect', { x: '8', y: '2', width: '8', height: '4', rx: '1' }),
    h('path', { d: 'M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2' }),
    h('path', { d: 'M9 12h6' }),
    h('path', { d: 'M9 16h6' })
  ])
}

function BugIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'm8 2 1.88 1.88' }),
    h('path', { d: 'M14.12 3.88 16 2' }),
    h('path', { d: 'M9 7.13v-1a3.003 3.003 0 1 1 6 0v1' }),
    h('path', { d: 'M12 20c-3.3 0-6-2.7-6-6v-3a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v3c0 3.3-2.7 6-6 6' })
  ])
}

function AlertIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M10.29 3.86 1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0Z' }),
    h('path', { d: 'M12 9v4' }),
    h('path', { d: 'M12 17h.01' })
  ])
}

function CheckCircleIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M22 11.08V12a10 10 0 1 1-5.93-9.14' }),
    h('path', { d: 'm9 11 3 3L22 4' })
  ])
}

const primaryActions = computed(() => {
  const taskAction = {
    key: 'tasks',
    title: defaultTaskView.value.label,
    description: '进入当前角色的默认任务视角，直接开始处理最常见的任务。',
    targetLabel: '任务页',
    count: countTasks(defaultTaskView.value.params),
    theme: 'indigo',
    icon: TaskIcon,
    path: '/tasks',
    query: taskListQuery.value
  }

  const bugAction = {
    key: 'bugs',
    title: defaultBugView.value.label,
    description: '进入当前角色的默认 Bug 视角，优先清理验证或修复积压。',
    targetLabel: 'Bug 页',
    count: countBugs(defaultBugView.value.params),
    theme: 'rose',
    icon: BugIcon,
    path: '/bugs',
    query: bugListQuery.value
  }

  const roleActions = {
    pm: [
      taskAction,
      bugAction,
      {
        key: 'testing',
        title: '查看测试中任务',
        description: '优先识别卡在测试阶段的任务，避免交付滞留。',
        targetLabel: '任务页',
        count: countTasks({ status: 'testing' }),
        theme: 'emerald',
        icon: CheckCircleIcon,
        path: '/tasks',
        query: buildViewQuery({ key: 'testing', params: { status: 'testing' } })
      }
    ],
    dev_lead: [
      taskAction,
      {
        key: 'group-active',
        title: '本组开发中',
        description: '快速查看本组正在推进的工作，便于协调资源。',
        targetLabel: '任务页',
        count: tasks.value.filter((task) => task.dev_lead_id === authStore.userInfo?.id && task.status === 'developing').length,
        theme: 'emerald',
        icon: CheckCircleIcon,
        path: '/tasks',
        query: buildViewQuery({ key: 'group-active', params: { mine: 'group', status: 'developing' } })
      },
      bugAction
    ],
    dev: [taskAction, bugAction, {
      key: 'rejected',
      title: '查看打回修改',
      description: '优先处理被测试打回的任务，减少往返。',
      targetLabel: '任务页',
      count: tasks.value.filter((task) => task.assignee_id === authStore.userInfo?.id && task.status === 'rejected').length,
      theme: 'amber',
      icon: AlertIcon,
      path: '/tasks',
      query: buildViewQuery({ key: 'rejected', params: { mine: 'all', status: 'rejected' } })
    }],
    tester_lead: [
      taskAction,
      bugAction,
      {
        key: 'critical-bugs',
        title: '查看高风险 Bug',
        description: '优先清理高严重度问题，降低回归与发布风险。',
        targetLabel: 'Bug 页',
        count: countBugs({ severity: 'critical' }),
        theme: 'amber',
        icon: AlertIcon,
        path: '/bugs',
        query: buildViewQuery({ key: 'critical', params: { severity: 'critical' } })
      }
    ],
    tester: [
      taskAction,
      bugAction,
      {
        key: 'created-bugs',
        title: '查看我创建的 Bug',
        description: '快速回看自己提报的问题是否已经进入验证或被重新打开。',
        targetLabel: 'Bug 页',
        count: bugs.value.filter((bug) => bug.creator_id === authStore.userInfo?.id).length,
        theme: 'emerald',
        icon: CheckCircleIcon,
        path: '/bugs',
        query: buildViewQuery({ key: 'created', params: { mine: 'created' } })
      }
    ]
  }

  return roleActions[role.value] || [taskAction, bugAction]
})

const focusTasks = computed(() => {
  const taskPriority = ['rejected', 'pending_test', 'testing', 'assigned_lead', 'developing', 'pending']
  return [...tasks.value]
    .sort((a, b) => taskPriority.indexOf(a.status) - taskPriority.indexOf(b.status))
    .slice(0, 6)
})

const focusBugs = computed(() => {
  const bugPriority = ['pending_verify', 'reopened', 'assigned', 'fixing', 'new', 'fixed']
  return [...bugs.value]
    .sort((a, b) => bugPriority.indexOf(a.status) - bugPriority.indexOf(b.status))
    .slice(0, 5)
})

function countTasks(params = {}) {
  return tasks.value.filter((task) => matchesTask(task, params)).length
}

function countBugs(params = {}) {
  return bugs.value.filter((bug) => matchesBug(bug, params)).length
}

function matchesTask(task, params = {}) {
  if (params.status && task.status !== params.status) return false
  if (params.focus === 'risk') return ['rejected', 'pending_test', 'testing'].includes(task.status)
  if (params.mine === 'todo') {
    if (role.value === 'dev') return task.assignee_id === authStore.userInfo?.id && ['assigned_lead', 'rejected'].includes(task.status)
    if (role.value === 'tester') return task.tester_id === authStore.userInfo?.id && ['pending_test', 'testing', 'rejected'].includes(task.status)
    if (role.value === 'dev_lead') return task.dev_lead_id === authStore.userInfo?.id && task.status === 'assigned_lead'
  }
  if (params.mine === 'group') return task.dev_lead_id === authStore.userInfo?.id
  if (params.mine === 'verify-pool') return ['pending_test', 'testing'].includes(task.status)
  if (params.mine === 'all') {
    if (role.value === 'dev') return task.assignee_id === authStore.userInfo?.id
    if (role.value === 'tester') return task.tester_id === authStore.userInfo?.id
  }
  return true
}

function matchesBug(bug, params = {}) {
  if (params.status && bug.status !== params.status) return false
  if (params.severity && bug.severity !== params.severity) return false
  if (params.mine === 'fix') return bug.assignee_id === authStore.userInfo?.id && ['assigned', 'fixing', 'reopened'].includes(bug.status)
  if (params.mine === 'verify') return bug.status === 'pending_verify'
  if (params.mine === 'created') return bug.creator_id === authStore.userInfo?.id
  if (params.mine === 'group') return true
  return true
}

function goAction(action) {
  router.push({ path: action.path, query: action.query })
}

function goTasks(query) {
  router.push({ path: '/tasks', query })
}

function goBugs(query) {
  router.push({ path: '/bugs', query })
}

function statusStyle(color) {
  return {
    '--chip-color': color || '#94a3b8'
  }
}

onMounted(async () => {
  try {
    tasks.value = await getTasks()
    bugs.value = await getBugs()
  } catch (error) {
    window.$message?.error('加载工作台数据失败')
  }
  if (authStore.isDevLead) {
    try {
      const res = await getMyTeam()
      teamMembers.value = res.members || []
    } catch (e) { console.error(e) }
  }
})
</script>

<style scoped>
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.hero-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 30px;
  border-radius: 24px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 55%, #312e81 100%);
  color: white;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.18);
}

.hero-eyebrow,
.section-kicker {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.hero-eyebrow {
  color: rgba(255, 255, 255, 0.72);
  margin-bottom: 12px;
}

.hero-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.25;
}

.hero-subtitle {
  margin: 12px 0 0;
  max-width: 760px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}

.hero-meta {
  display: flex;
  gap: 12px;
}

.meta-pill {
  min-width: 92px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(6px);
}

.meta-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
}

.meta-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.74);
}

.section-heading h3,
.section-header h3 {
  margin: 4px 0 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.section-kicker {
  color: #6366f1;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.action-card {
  padding: 20px;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  background: white;
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  border-color: #c7d2fe;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.08);
}

.action-card-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.action-icon {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.action-icon svg {
  width: 20px;
  height: 20px;
}

.action-icon.indigo { background: linear-gradient(135deg, #6366f1, #8b5cf6); }
.action-icon.rose { background: linear-gradient(135deg, #f43f5e, #fb7185); }
.action-icon.emerald { background: linear-gradient(135deg, #10b981, #34d399); }
.action-icon.amber { background: linear-gradient(135deg, #f59e0b, #fbbf24); }

.action-count {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
}

.action-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.action-description {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
  min-height: 44px;
}

.action-link {
  margin-top: 16px;
  font-size: 13px;
  font-weight: 600;
  color: #6366f1;
}

.overview-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.6fr) minmax(320px, 0.9fr);
  gap: 20px;
}

.overview-side {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-card {
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  padding: 22px;
}

.compact-card {
  padding: 20px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 18px;
}

.compact-header {
  margin-bottom: 14px;
}

.view-all {
  font-size: 13px;
  color: #6366f1;
  text-decoration: none;
  font-weight: 600;
}

.view-all:hover {
  text-decoration: underline;
}

.focus-list,
.view-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.focus-row,
.view-row {
  width: 100%;
  border: 0;
  background: #f8fafc;
  border-radius: 14px;
  cursor: pointer;
  transition: background 0.18s ease, transform 0.18s ease;
}

.focus-row:hover,
.view-row:hover {
  background: #eef2ff;
  transform: translateY(-1px);
}

.focus-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 14px 16px;
  text-align: left;
}

.compact-row {
  padding: 12px 14px;
}

.focus-main {
  min-width: 0;
}

.focus-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.focus-meta {
  display: flex;
  gap: 10px;
  margin-top: 6px;
  font-size: 12px;
  color: #64748b;
}

.focus-side {
  flex-shrink: 0;
}

.status-chip {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: var(--chip-color);
  background: color-mix(in srgb, var(--chip-color) 12%, white);
  border: 1px solid color-mix(in srgb, var(--chip-color) 18%, white);
}

.view-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  font-size: 14px;
  font-weight: 600;
  color: #334155;
}

.view-row-arrow {
  color: #94a3b8;
}

.empty-state {
  padding: 28px 18px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.compact-empty {
  padding: 20px 12px;
}

.my-team-section {
  margin-top: 0;
}

.team-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.team-member-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.team-member-name {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.team-member-role {
  font-size: 11px;
  color: #64748b;
  padding: 1px 6px;
  border-radius: 4px;
  background: #e2e8f0;
}

@media (max-width: 1200px) {
  .action-grid,
  .overview-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 900px) {
  .hero-card {
    flex-direction: column;
  }

  .hero-meta {
    width: 100%;
  }

  .meta-pill {
    flex: 1;
  }
}
</style>
