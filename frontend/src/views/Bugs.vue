<template>
  <AppLayout>
    <template #actions>
      <n-button v-if="authStore.isTester" type="primary" class="action-btn" @click="showCreateDialog = true">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
        </template>
        创建 Bug
      </n-button>
    </template>

    <div class="bugs-page">
      <section class="view-hero section-card">
        <div>
          <div class="section-kicker">角色主视角</div>
          <h2 class="hero-title">{{ activeView?.label || '全部 Bug' }}</h2>
          <p class="hero-subtitle">{{ viewDescription }}</p>
        </div>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="hero-stat-value">{{ filteredBugs.length }}</span>
            <span class="hero-stat-label">当前结果</span>
          </div>
          <div class="hero-stat">
            <span class="hero-stat-value">{{ bugs.length }}</span>
            <span class="hero-stat-label">Bug 总数</span>
          </div>
        </div>
      </section>

      <section class="view-switcher section-card">
        <div class="section-header compact-header">
          <div>
            <div class="section-kicker">快速切换</div>
            <h3>优先入口</h3>
          </div>
        </div>
        <div class="view-list">
          <button
            v-for="view in availableViews"
            :key="view.key"
            type="button"
            :class="['view-chip', { active: activeView?.key === view.key }]"
            @click="applyView(view)"
          >
            <span>{{ view.label }}</span>
            <span class="view-chip-count">{{ countForView(view) }}</span>
          </button>
        </div>
      </section>

      <section class="filter-bar section-card">
        <div class="filter-left">
          <n-select v-model:value="filterTask" :options="taskOptions" placeholder="筛选任务" clearable style="width: 180px;" size="small" />
          <n-select v-model:value="filterStatus" :options="bugStatusOptions" placeholder="筛选状态" clearable style="width: 160px;" size="small" />
          <n-select v-model:value="filterSeverity" :options="severityOptions" placeholder="筛选严重程度" clearable style="width: 160px;" size="small" />
        </div>
        <div class="filter-right">
          <n-button size="small" quaternary @click="resetFilters">重置筛选</n-button>
          <n-button size="small" quaternary @click="loadBugs">
            <template #icon><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg></template>
          </n-button>
        </div>
      </section>

      <div class="list-container section-card">
        <BugList :bugs="filteredBugs" @bug-click="openDetail" />
      </div>

      <CreateBugDialog v-model:show="showCreateDialog" @success="loadBugs" />
      <BugDetailDrawer
        v-model:show="showDetailDrawer"
        :bug-id="selectedBugId"
        @refresh="loadBugs"
      />
    </div>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { getBugs } from '@/api/bugs'
import { getTasks } from '@/api/tasks'
import BugList from '@/components/BugList.vue'
import CreateBugDialog from '@/components/CreateBugDialog.vue'
import BugDetailDrawer from '@/components/BugDetailDrawer.vue'
import AppLayout from '@/components/AppLayout.vue'
import { bugPrimaryViews } from '@/constants/bugViews'
import { bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import { buildViewQuery, compactParams, pickActiveView } from '@/utils/viewState'
import { NButton, NSelect } from 'naive-ui'

const authStore = useAuthStore()
const route = useRoute()
const router = useRouter()

const bugs = ref([])
const tasks = ref([])
const showCreateDialog = ref(false)
const showDetailDrawer = ref(false)
const selectedBugId = ref(null)

const filterTask = ref(null)
const filterStatus = ref(null)
const filterSeverity = ref(null)

const availableViews = computed(() => bugPrimaryViews[authStore.role] || bugPrimaryViews.dev || [])
const activeView = computed(() => pickActiveView(availableViews.value, route.query) || availableViews.value[0] || null)

const taskOptions = computed(() => tasks.value.map((task) => ({ label: task.title, value: task.id })))
const bugStatusOptions = Object.entries(bugStatusMeta).map(([value, meta]) => ({ label: meta.label, value }))
const severityOptions = Object.entries(severityMeta).map(([value, meta]) => ({ label: meta.label, value }))

const viewDescriptionMap = {
  all: '展示你当前可见的全部 Bug，适合做整体回看。',
  critical: '优先查看高严重度问题。',
  reopened: '集中处理重新打开的问题，减少往返。',
  group: '查看当前开发组相关的缺陷集合。',
  fix: '直接打开分配给你的修复工作。',
  verify: '聚焦待验证 Bug，优先完成闭环。',
  created: '回看你创建的问题当前进度。'
}

const viewDescription = computed(() => viewDescriptionMap[activeView.value?.key] || '按角色主视角筛出最常用的 Bug 队列。')

const filteredBugs = computed(() => {
  return bugs.value.filter((bug) => {
    if (!matchesView(bug, activeView.value?.params || {})) return false
    if (filterTask.value && bug.task_id !== filterTask.value) return false
    if (filterStatus.value && bug.status !== filterStatus.value) return false
    if (filterSeverity.value && bug.severity !== filterSeverity.value) return false
    return true
  })
})

function matchesView(bug, params = {}) {
  if (params.status && bug.status !== params.status) return false
  if (params.severity && bug.severity !== params.severity) return false
  if (params.mine === 'fix') return bug.assignee_id === authStore.userInfo?.id && ['assigned', 'fixing', 'reopened'].includes(bug.status)
  if (params.mine === 'verify') return bug.status === 'pending_verify'
  if (params.mine === 'created') return bug.creator_id === authStore.userInfo?.id
  if (params.mine === 'group') return true
  return true
}

function countForView(view) {
  return bugs.value.filter((bug) => matchesView(bug, view.params || {})).length
}

function applyView(view) {
  router.replace({ path: '/bugs', query: compactParams(buildViewQuery(view)) })
}

function resetFilters() {
  filterTask.value = null
  filterStatus.value = null
  filterSeverity.value = null
}

async function loadBugs() {
  try {
    bugs.value = await getBugs()
  } catch (error) {
    window.$message?.error('加载 Bug 失败')
  }
}

async function loadTasks() {
  try {
    tasks.value = await getTasks()
  } catch (error) {
    window.$message?.error('加载任务失败')
  }
}

function openDetail(id) {
  selectedBugId.value = id
  showDetailDrawer.value = true
}

watch(() => route.params.id, (id) => {
  if (id) {
    openDetail(Number(id))
  }
}, { immediate: true })

onMounted(() => {
  loadBugs()
  loadTasks()
})
</script>

<style scoped>
.bugs-page {
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

.view-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  background: linear-gradient(135deg, #fff1f2 0%, #ffffff 55%, #f8fafc 100%);
}

.section-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #e11d48;
}

.hero-title {
  margin: 8px 0 0;
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
}

.hero-subtitle {
  margin: 10px 0 0;
  max-width: 760px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}

.hero-stats {
  display: flex;
  gap: 12px;
}

.hero-stat {
  min-width: 96px;
  padding: 14px 16px;
  border-radius: 16px;
  background: white;
  border: 1px solid #fecdd3;
}

.hero-stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.hero-stat-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.compact-header h3 {
  margin: 4px 0 0;
  font-size: 18px;
  color: #0f172a;
}

.view-list {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 16px;
}

.view-chip {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 11px 14px;
  border-radius: 999px;
  border: 1px solid #e2e8f0;
  background: #f8fafc;
  color: #334155;
  cursor: pointer;
  font-size: 13px;
  font-weight: 600;
  transition: all 0.18s ease;
}

.view-chip.active {
  background: #fff1f2;
  border-color: #fda4af;
  color: #be123c;
}

.view-chip-count {
  min-width: 22px;
  padding: 2px 7px;
  border-radius: 999px;
  background: white;
  color: #64748b;
  font-size: 12px;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.filter-left,
.filter-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.list-container {
  padding: 8px;
}

.action-btn {
  border-radius: 10px;
  font-weight: 500;
}

@media (max-width: 1100px) {
  .view-hero,
  .filter-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .hero-stats,
  .filter-left,
  .filter-right {
    flex-wrap: wrap;
  }
}
</style>
