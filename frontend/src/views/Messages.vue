<template>
  <div class="messages-page">
    <div class="messages-header">
      <div class="filter-bar">
        <div class="filter-tabs">
          <button
            v-for="t in typeFilters"
            :key="t.value"
            :class="['filter-btn', { active: typeFilter === t.value }]"
            @click="typeFilter = t.value; loadMessages()"
          >{{ t.label }}</button>
        </div>
        <div class="filter-tabs">
          <button
            v-for="s in statusFilters"
            :key="s.value"
            :class="['filter-btn', { active: statusFilter === s.value }]"
            @click="statusFilter = s.value; loadMessages()"
          >{{ s.label }}</button>
        </div>
        <button class="mark-all-btn" @click="handleMarkAllRead" v-if="hasUnread">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="btn-icon">
            <polyline points="9 11 12 14 22 4"/>
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
          </svg>
          全部已读
        </button>
      </div>
    </div>

    <div class="messages-list" v-if="messages.length > 0">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['msg-item', { unread: !msg.isRead }]"
        @click="handleMarkRead(msg)"
      >
        <div class="msg-indicator" v-if="!msg.isRead"></div>
        <div class="msg-body">
          <div class="msg-title">{{ msg.title }}</div>
          <div class="msg-content">{{ msg.content }}</div>
          <div class="msg-meta">
            <span :class="['msg-type', msg.type]">{{ typeLabel(msg.type) }}</span>
            <span class="msg-time">{{ formatTime(msg.createdAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else-if="!loading">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
        <path d="M22 17a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h5l2-2h6l2 2h5a2 2 0 0 1 2 2v8z"/>
        <line x1="12" y1="11" x2="12" y2="17"/>
        <line x1="9" y1="14" x2="15" y2="14"/>
      </svg>
      <p>暂无消息</p>
    </div>

    <div class="pagination-bar" v-if="total > size">
      <n-pagination
        :page="page"
        :page-count="pageCount"
        :page-slot="5"
        @update:page="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NPagination } from 'naive-ui'
import { fetchMessages, markRead, markAllRead } from '@/api/message'
import { useMessageStore } from '@/store/useMessageStore'

const router = useRouter()
const messageStore = useMessageStore()

const messages = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)
const loading = ref(false)
const typeFilter = ref('all')
const statusFilter = ref('all')

const typeFilters = [
  { label: '全部', value: 'all' },
  { label: '任务', value: 'task' },
  { label: 'Bug', value: 'bug' },
  { label: '系统', value: 'system' }
]

const statusFilters = [
  { label: '全部', value: 'all' },
  { label: '未读', value: 'unread' },
  { label: '已读', value: 'read' }
]

const pageCount = computed(() => Math.ceil(total.value / size.value))
const hasUnread = computed(() => messages.value.some(m => !m.isRead))

function typeLabel(type) {
  const map = { task: '任务', bug: 'Bug', system: '系统' }
  return map[type] || type
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
  const h = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${mo}-${da} ${h}:${mi}`
}

async function loadMessages() {
  loading.value = true
  try {
    const isRead = statusFilter.value === 'all' ? null : statusFilter.value === 'read'
    const res = await fetchMessages(page.value, size.value, typeFilter.value, isRead)
    messages.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function navigateToDetail(msg) {
  const routes = { task: '/tasks/', bug: '/bugs/', requirement: '/requirements/' }
  const path = routes[msg.type]
  if (path && msg.relatedId) {
    router.push(path + msg.relatedId)
  }
}

async function handleMarkRead(msg) {
  if (!msg.isRead) {
    await markRead(msg.id)
    msg.isRead = true
    messageStore.refreshUnreadCount()
  }
  navigateToDetail(msg)
}

async function handleMarkAllRead() {
  await markAllRead()
  for (const msg of messages.value) {
    msg.isRead = true
  }
  messageStore.refreshUnreadCount()
}

function handlePageChange(p) {
  page.value = p
  loadMessages()
}

onMounted(loadMessages)
</script>

<style scoped>
.messages-page {
  max-width: 800px;
  margin: 0 auto;
}

.messages-header {
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.filter-tabs {
  display: flex;
  gap: 4px;
  background: #f1f5f9;
  border-radius: 8px;
  padding: 3px;
}

.filter-btn {
  padding: 6px 16px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #64748b;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn:hover {
  color: #334155;
}

.filter-btn.active {
  background: white;
  color: #0f172a;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.mark-all-btn {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 16px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: white;
  color: #6366f1;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.mark-all-btn:hover {
  background: #f0f0ff;
  border-color: #6366f1;
}

.btn-icon {
  width: 16px;
  height: 16px;
}

/* Messages List */
.messages-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
  background: #f1f5f9;
  border-radius: 12px;
  overflow: hidden;
}

.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 0;
  padding: 16px 20px;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
}

.msg-item:hover {
  background: #f8fafc;
}

.msg-item:not(:last-child) {
  border-bottom: 1px solid #f1f5f9;
}

.msg-item.unread {
  background: #f0f4ff;
}

.msg-item.unread:hover {
  background: #e8eeff;
}

.msg-indicator {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #6366f1;
  flex-shrink: 0;
  margin-top: 6px;
  margin-right: 12px;
}

.msg-body {
  flex: 1;
  min-width: 0;
}

.msg-title {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.msg-item.unread .msg-title {
  color: #1e293b;
}

.msg-content {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
}

.msg-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.msg-type {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 4px;
  text-transform: uppercase;
}

.msg-type.task {
  background: #e0f2fe;
  color: #0369a1;
}

.msg-type.bug {
  background: #fce7f3;
  color: #be185d;
}

.msg-type.system {
  background: #f1f5f9;
  color: #475569;
}

.msg-time {
  font-size: 12px;
  color: #94a3b8;
}

/* Empty State */
.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: #94a3b8;
}

.empty-icon {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  font-size: 14px;
}

/* Pagination */
.pagination-bar {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}
</style>
