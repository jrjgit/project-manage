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
          <span class="nav-label">{{ item.label }}</span>
          <span
            v-if="item.badge === 'unread' && messageStore.unreadCount > 0"
            class="nav-badge"
          >{{ messageStore.unreadCount > 99 ? '99+' : messageStore.unreadCount }}</span>
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
          <div class="bell-wrapper" ref="bellRef">
            <button class="bell-btn" :class="{ 'has-new': messageStore.unreadCount > 0 }" @click.stop="toggleDropdown">
              <svg class="bell-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
              <span v-if="messageStore.unreadCount > 0" class="bell-badge">
                {{ messageStore.unreadCount > 99 ? '99+' : messageStore.unreadCount }}
              </span>
            </button>
            <div class="bell-dropdown" v-if="showDropdown">
              <div class="dropdown-header">
                <span>消息</span>
                <button class="dropdown-mark-all" @click.stop="handleMarkAllRead" v-if="unreadList.length > 0">全部已读</button>
              </div>
              <div class="dropdown-body">
                <div v-for="msg in unreadList" :key="msg.id" class="dropdown-item" @click.stop="handleDropdownItemClick(msg)">
                  <div class="dropdown-item-title">{{ msg.title }}</div>
                  <div class="dropdown-item-time">{{ formatTime(msg.created_at) }}</div>
                </div>
                <div v-if="unreadList.length === 0" class="dropdown-empty">暂无未读消息</div>
              </div>
              <div class="dropdown-footer" v-if="messageStore.unreadCount > 0">
                <router-link to="/messages" @click="showDropdown = false">查看全部消息</router-link>
              </div>
            </div>
          </div>
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
import { computed, h, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { useMessageStore } from '@/store/useMessageStore'
import { fetchMessages, markRead, markAllRead } from '@/api/message'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const messageStore = useMessageStore()

const showDropdown = ref(false)
const unreadList = ref([])
const bellRef = ref(null)

async function fetchUnreadList() {
  try {
    const res = await fetchMessages(1, 5, null, false)
    unreadList.value = res.records || []
  } catch {}
}

function toggleDropdown() {
  showDropdown.value = !showDropdown.value
  if (showDropdown.value) {
    fetchUnreadList()
  }
}

async function handleMarkAllRead() {
  await markAllRead()
  messageStore.unreadCount = 0
  unreadList.value = []
  showDropdown.value = false
}

async function handleDropdownItemClick(msg) {
  showDropdown.value = false
  if (!msg.is_read && msg.is_read !== undefined) {
    try { await markRead(msg.id); messageStore.refreshUnreadCount() } catch {}
  }
  const id = msg.related_id || msg.relatedId || msg.id
  if (!id || !msg.type) return
  const routes = { task: '/developer?taskId=', bug: '/developer?bugId=', requirement: '/requirements/' }
  const path = routes[msg.type]
  if (path) {
    const sep = path.includes('?') ? '&' : '?'
    router.push(path + id + sep + '_t=' + Date.now()).catch(e => console.error('[nav]', e))
  }
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 172800000) return '昨天'
  const y = d.getFullYear()
  const mo = String(d.getMonth() + 1).padStart(2, '0')
  const da = String(d.getDate()).padStart(2, '0')
  return `${y}-${mo}-${da}`
}

function handleClickOutside(e) {
  if (bellRef.value && !bellRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

watch(() => messageStore.unreadCount, (newVal, oldVal) => {
  if (oldVal !== undefined && newVal > oldVal && window.$notification) {
    window.$notification.info({
      title: '新消息提醒',
      content: `您有 ${newVal - oldVal} 条新消息`,
      duration: 4000
    })
  }
  document.title = newVal > 0 ? `(${newVal}) ProjectOS` : 'ProjectOS'
})

function handleVisibility() {
  if (!document.hidden) {
    messageStore.refreshUnreadCount()
    document.body.classList.remove('page-paused')
  } else {
    document.body.classList.add('page-paused')
  }
}

onMounted(() => {
  messageStore.refreshUnreadCount()
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('visibilitychange', handleVisibility)
})

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('visibilitychange', handleVisibility)
})

const roleLabelMap = {
  pm: '项目经理', dev_lead: '开发组长', dev: '开发',
  tester: '测试'
}
const roleLabel = computed(() => roleLabelMap[authStore.userInfo?.role] || '')
const avatarLetter = computed(() => authStore.userInfo?.name?.charAt(0)?.toUpperCase() || '?')

const allMenus = [
  { key: 'dashboard', label: '仪表盘', path: '/dashboard', icon: LayoutDashboardIcon, roles: ['pm'] },
  { key: 'developer', label: '开发工作台', path: '/developer', icon: CodeIcon, roles: ['pm', 'dev_lead', 'dev'] },
  { key: 'tester', label: '测试工作台', path: '/tester', icon: BugIcon, roles: ['pm', 'dev_lead', 'dev', 'tester'] },
  { key: 'projects', label: '项目管理', path: '/projects', icon: FolderKanbanIcon, roles: ['pm'] },
  { key: 'tasks', label: '任务管理', path: '/tasks', icon: ClipboardListIcon, roles: [] },
  { key: 'messages', label: '消息中心', path: '/messages', icon: BellIcon, badge: 'unread', roles: ['pm', 'dev_lead', 'dev', 'tester'] },
  { key: 'requirements', label: '需求管理', path: '/requirements', icon: FileTextIcon, roles: ['pm', 'dev_lead'] },
  { key: 'iterations', label: '发布迭代', path: '/iterations', icon: MilestoneIcon, roles: ['pm', 'dev_lead', 'dev', 'tester'] },
  { key: 'revenue', label: '运维营收统计', path: '/revenue', icon: ChartBarIcon, roles: ['pm'] },
  { key: 'performance', label: '人员绩效', path: '/performance', icon: TrendingUpIcon, roles: ['pm', 'dev_lead', 'dev', 'tester_lead', 'tester'] },
  { key: 'users', label: '用户管理', path: '/users', icon: UsersIcon, roles: ['pm'] },
  { key: 'systems', label: '系统管理', path: '/systems', icon: BookOpenIcon, roles: ['pm'] },
  { key: 'dictionary', label: '基础字典', path: '/dictionary', icon: BookOpenIcon, roles: ['pm'] }
]

const visibleMenus = computed(() =>
  allMenus.filter(m => m.roles.includes(authStore.role))
)

const pageMeta = computed(() => {
  const map = {
    dashboard: {
      title: '执行工作台',
      subtitle: '先处理最重要的事项，再回看整体进度。'
    },
    developer: {
      title: '开发工作台',
      subtitle: '一站式任务看板，兼顾任务与 Bug 处理。'
    },
    tester: {
      title: '测试工作台',
      subtitle: '待测试任务与待验证 Bug 一目了然。'
    },
    projects: {
      title: '项目管理',
      subtitle: '查看项目范围、成员协作与当前交付状态。'
    },
    tasks: {
      title: '任务管理',
      subtitle: '按角色主视角切换任务队列，直接进入执行。'
    },
    messages: {
      title: '消息中心',
      subtitle: '查看系统通知和消息提醒'
    },
    revenue: {
      title: '运维营收统计',
      subtitle: '年度运维需求产值统计'
    },
    performance: {
      title: '人员绩效统计',
      subtitle: '个人任务完成情况'
    },
    users: {
      title: '用户管理',
      subtitle: '维护角色配置与协作分工。'
    },
    dictionary: {
      title: '基础数据字典',
      subtitle: '系统配置字典维护'
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
function FileTextIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z' }),
    h('polyline', { points: '14 2 14 8 20 8' }),
    h('line', { x1: '16', y1: '13', x2: '8', y2: '13' }),
    h('line', { x1: '16', y1: '17', x2: '8', y2: '17' }),
    h('polyline', { points: '10 9 9 9 8 9' })
  ])
}
function MilestoneIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M12 12v8' }),
    h('path', { d: 'M16 16l-4-4-4 4' }),
    h('circle', { cx: '12', cy: '8', r: '6' }),
    h('path', { d: 'M12 5v3' })
  ])
}
function ChartBarIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M3 3v16a2 2 0 0 0 2 2h16' }),
    h('rect', { x: '7', y: '13', width: '4', height: '6', rx: '1' }),
    h('rect', { x: '15', y: '8', width: '4', height: '11', rx: '1' })
  ])
}
function TrendingUpIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('polyline', { points: '22 7 13.5 15.5 8.5 10.5 2 17' }),
    h('polyline', { points: '16 7 22 7 22 13' })
  ])
}
function BellIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9' }),
    h('path', { d: 'M13.73 21a2 2 0 0 1-3.46 0' })
  ])
}

function BookOpenIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('path', { d: 'M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z' }),
    h('path', { d: 'M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z' })
  ])
}

function CodeIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('polyline', { points: '16 18 22 12 16 6' }),
    h('polyline', { points: '8 6 2 12 8 18' })
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
  width: var(--sidebar-width);
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  height: 100vh;
  z-index: 100;
  transition: width 0.2s ease;
}

@media (max-width: 1023px) {
  .sidebar {
    width: var(--sidebar-collapsed-width);
  }
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255,255,255,0.06);
}

@media (max-width: 1023px) {
  .sidebar-brand {
    justify-content: center;
    padding: 20px 12px;
  }
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

@media (max-width: 1023px) {
  .brand-text {
    display: none;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

@media (max-width: 1023px) {
  .sidebar-nav {
    padding: 12px 8px;
    align-items: center;
  }
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

@media (max-width: 1023px) {
  .nav-item {
    justify-content: center;
    padding: 12px;
    overflow: hidden;
  }
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

@media (max-width: 1023px) {
  .nav-label {
    display: none;
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 1px solid rgba(255,255,255,0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

@media (max-width: 1023px) {
  .sidebar-footer {
    justify-content: center;
    padding: 12px 8px;
  }
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

@media (max-width: 1023px) {
  .user-meta {
    display: none;
  }
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
  margin-left: var(--sidebar-width);
  display: flex;
  flex-direction: column;
  transition: margin-left 0.2s ease;
  overflow: hidden;
}

@media (max-width: 1023px) {
  .main-content {
    margin-left: var(--sidebar-collapsed-width);
  }
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
  gap: 16px;
}

@media (max-width: 768px) {
  .top-bar {
    min-height: auto;
    padding: 12px 20px;
  }

  .page-kicker,
  .page-subtitle {
    display: none;
  }

  .page-title {
    font-size: 20px;
  }
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
  flex-wrap: wrap;
  justify-content: flex-end;
}

.page-body {
  padding: 28px var(--content-padding);
  flex: 1;
  min-height: 0;
  overflow-y: auto;
}

@media (max-width: 768px) {
  .page-body {
    padding: 20px var(--content-padding-md);
  }
}

@media (max-width: 640px) {
  .page-body {
    padding: 16px var(--content-padding-sm);
  }
}

/* Nav Badge */
.nav-badge {
  margin-left: auto;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 700;
  min-width: 20px;
  height: 20px;
  line-height: 20px;
  text-align: center;
  border-radius: 10px;
  padding: 0 6px;
}

@media (max-width: 1023px) {
  .nav-badge {
    display: none;
  }
}

/* Bell Wrapper */
.bell-wrapper {
  position: relative;
}

.bell-btn {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  background: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.bell-btn:hover {
  background: #f8fafc;
  border-color: #cbd5e1;
}

.bell-btn.has-new {
  border-color: #fca5a5;
  animation: bellPulse 2s ease-in-out 3;
}

.bell-icon {
  width: 20px;
  height: 20px;
  color: #475569;
}

.bell-btn.has-new .bell-icon {
  color: #ef4444;
}

.bell-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  background: #ef4444;
  color: white;
  font-size: 11px;
  font-weight: 700;
  border-radius: 9px;
  padding: 0 5px;
  box-shadow: 0 2px 4px rgba(239, 68, 68, 0.4);
}

@keyframes bellPulse {
  0%, 100% { box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.3); }
  50% { box-shadow: 0 0 0 8px rgba(239, 68, 68, 0); }
}

/* Bell Dropdown */
.bell-dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: min(360px, calc(100vw - 32px));
  max-width: calc(100vw - 32px);
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0,0,0,0.12), 0 2px 8px rgba(0,0,0,0.06);
  border: 1px solid #e2e8f0;
  z-index: 200;
  overflow: hidden;
}

.dropdown-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.dropdown-mark-all {
  font-size: 12px;
  color: #6366f1;
  background: none;
  border: none;
  cursor: pointer;
  font-weight: 500;
}

.dropdown-mark-all:hover {
  color: #4f46e5;
}

.dropdown-body {
  max-height: 320px;
  overflow-y: auto;
}

.dropdown-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f8fafc;
  cursor: pointer;
  transition: background 0.15s;
}

.dropdown-item:hover {
  background: #f8fafc;
}

.dropdown-item-title {
  font-size: 13px;
  font-weight: 500;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
}

.dropdown-item-time {
  font-size: 11px;
  color: #94a3b8;
}

.dropdown-empty {
  padding: 32px 16px;
  text-align: center;
  color: #94a3b8;
  font-size: 13px;
}

.dropdown-footer {
  padding: 10px 16px;
  border-top: 1px solid #f1f5f9;
  text-align: center;
}

.dropdown-footer a {
  font-size: 13px;
  color: #6366f1;
  text-decoration: none;
  font-weight: 500;
}

.dropdown-footer a:hover {
  color: #4f46e5;
}
</style>
