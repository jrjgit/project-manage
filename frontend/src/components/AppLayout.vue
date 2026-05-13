<template>
  <div class="app-layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="sidebar-brand">
        <div class="brand-icon">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z"/>
            <path d="M2 17l10 5 10-5"/>
            <path d="M2 12l10 5 10-5"/>
          </svg>
        </div>
        <div class="brand-text">ProjectOS</div>
      </div>

      <nav class="sidebar-nav">
        <router-link
          v-for="item in visibleMenus"
          :key="item.key"
          :to="item.path"
          :class="['nav-item', { active: route.path === item.path }]"
        >
          <component :is="item.icon" class="nav-icon" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <div class="sidebar-footer">
        <div class="user-info">
          <div class="user-avatar">{{ avatarLetter }}</div>
          <div class="user-meta">
            <div class="user-name">{{ authStore.userInfo?.name }}</div>
            <div class="user-role">{{ roleLabel }}</div>
          </div>
        </div>
        <button class="logout-btn" @click="logout">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>
    </aside>

    <!-- 主内容区 -->
    <main class="main-content">
      <header class="top-bar">
        <div class="top-bar-copy">
          <div class="page-kicker">Execution Workbench</div>
          <div>
            <h1 class="page-title">{{ pageTitle }}</h1>
            <p class="page-subtitle">{{ pageSubtitle }}</p>
          </div>
        </div>
        <div class="top-actions">
          <slot name="actions" />
        </div>
      </header>
      <div class="page-body">
        <slot />
      </div>
    </main>
  </div>
</template>

<script setup>
import { computed, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const roleLabelMap = {
  pm: '项目经理', dev_lead: '开发组长', dev: '开发',
  tester_lead: '测试组长', tester: '测试'
}
const roleLabel = computed(() => roleLabelMap[authStore.userInfo?.role] || '')
const avatarLetter = computed(() => authStore.userInfo?.name?.charAt(0)?.toUpperCase() || '?')

const allMenus = [
  { key: 'dashboard', label: '仪表盘', path: '/dashboard', icon: LayoutDashboardIcon },
  { key: 'projects', label: '项目管理', path: '/projects', icon: FolderKanbanIcon },
  { key: 'tasks', label: '任务管理', path: '/tasks', icon: ClipboardListIcon },
  { key: 'bugs', label: 'Bug管理', path: '/bugs', icon: BugIcon },
  { key: 'users', label: '用户管理', path: '/users', icon: UsersIcon, pmOnly: true },
  { key: 'groups', label: '小组管理', path: '/groups', icon: GroupIcon, pmOnly: true }
]

const visibleMenus = computed(() =>
  allMenus.filter(m => !m.pmOnly || authStore.isPM)
)

const pageMeta = computed(() => {
  const map = {
    dashboard: {
      title: '执行工作台',
      subtitle: '先处理最重要的事项，再回看整体进度。'
    },
    projects: {
      title: '项目管理',
      subtitle: '查看项目范围、成员协作与当前交付状态。'
    },
    tasks: {
      title: '任务管理',
      subtitle: '按角色主视角切换任务队列，直接进入执行。'
    },
    bugs: {
      title: 'Bug 管理',
      subtitle: '优先处理待修复、待验证与重新打开的问题。'
    },
    users: {
      title: '用户管理',
      subtitle: '维护角色配置与协作分工。'
    },
    groups: {
      title: '小组管理',
      subtitle: '创建和管理开发小组，分配组长与成员。'
    }
  }
  const name = route.name?.toLowerCase()
  return map[name] || { title: '', subtitle: '' }
})

const pageTitle = computed(() => pageMeta.value.title)
const pageSubtitle = computed(() => pageMeta.value.subtitle)

function logout() {
  authStore.logout()
  router.push('/login')
}

// SVG icon components
function LayoutDashboardIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('rect', { x: '3', y: '3', width: '7', height: '7', rx: '1' }),
    h('rect', { x: '14', y: '3', width: '7', height: '7', rx: '1' }),
    h('rect', { x: '14', y: '14', width: '7', height: '7', rx: '1' }),
    h('rect', { x: '3', y: '14', width: '7', height: '7', rx: '1' })
  ])
}
function FolderKanbanIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M4 20h16a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.93a2 2 0 0 1-1.66-.9l-.82-1.2A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13c0 1.1.9 2 2 2Z' }),
    h('line', { x1: '9', y1: '10', x2: '15', y2: '10' }),
    h('line', { x1: '12', y1: '10', x2: '12', y2: '16' })
  ])
}
function ClipboardListIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('rect', { x: '8', y: '2', width: '8', height: '4', rx: '1' }),
    h('path', { d: 'M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2' }),
    h('path', { d: 'M12 11h4' }),
    h('path', { d: 'M12 16h4' }),
    h('path', { d: 'M8 11h.01' }),
    h('path', { d: 'M8 16h.01' })
  ])
}
function BugIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'm8 2 1.88 1.88' }),
    h('path', { d: 'M14.12 3.88 16 2' }),
    h('path', { d: 'M9 7.13v-1a3.003 3.003 0 1 1 6 0v1' }),
    h('path', { d: 'M12 20c-3.3 0-6-2.7-6-6v-3a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v3c0 3.3-2.7 6-6 6' }),
    h('path', { d: 'M12 20v-9' }),
    h('path', { d: 'M6.53 9C4.6 8.8 3 7.1 3 5' }),
    h('path', { d: 'M6 13H2' }),
    h('path', { d: 'M3 21c0-2.1 1.7-3.9 3.8-4' }),
    h('path', { d: 'M20.97 5c0 2.1-1.6 3.8-3.5 4' }),
    h('path', { d: 'M22 13h-4' }),
    h('path', { d: 'M17.2 17c2.1.1 3.8 1.9 3.8 4' })
  ])
}
function UsersIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2' }),
    h('circle', { cx: '9', cy: '7', r: '4' }),
    h('path', { d: 'M22 21v-2a4 4 0 0 0-3-3.87' }),
    h('path', { d: 'M16 3.13a4 4 0 0 1 0 7.75' })
  ])
}
function GroupIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('rect', { x: '2', y: '3', width: '20', height: '18', rx: '2' }),
    h('path', { d: 'M8 8h8' }),
    h('path', { d: 'M8 12h8' }),
    h('path', { d: 'M8 16h5' })
  ])
}
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background: #f1f5f9;
}

/* Sidebar */
.sidebar {
  width: 260px;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

.brand-icon {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.brand-icon svg {
  width: 20px;
  height: 20px;
}

.brand-text {
  font-size: 20px;
  font-weight: 700;
  color: white;
  letter-spacing: -0.5px;
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border-radius: 10px;
  color: rgba(255,255,255,0.55);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.nav-item:hover {
  background: rgba(255,255,255,0.05);
  color: rgba(255,255,255,0.85);
}

.nav-item.active {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  color: white;
  box-shadow: 0 4px 12px rgba(99,102,241,0.3);
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 14px;
  font-weight: 600;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.user-name {
  color: white;
  font-size: 13px;
  font-weight: 600;
}

.user-role {
  color: rgba(255,255,255,0.45);
  font-size: 11px;
}

.logout-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255,255,255,0.05);
  border: none;
  color: rgba(255,255,255,0.5);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: rgba(239,68,68,0.2);
  color: #ef4444;
}

.logout-btn svg {
  width: 16px;
  height: 16px;
}

/* Main Content */
.main-content {
  flex: 1;
  margin-left: 260px;
  display: flex;
  flex-direction: column;
}

.top-bar {
  min-height: 88px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px 32px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.top-bar-copy {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.page-kicker {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6366f1;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
  letter-spacing: -0.5px;
}

.page-subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #64748b;
}

.top-actions {
  display: flex;
  gap: 12px;
}

.page-body {
  padding: 28px 32px;
  flex: 1;
}
</style>
