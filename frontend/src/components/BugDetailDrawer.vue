<template>
  <n-drawer v-model:show="show" :width="560" placement="right">
    <n-drawer-content v-if="bug" closable>
      <template #header>
        <div class="drawer-header">
          <div class="drawer-kicker">Bug #{{ bug.id }}</div>
          <div class="drawer-title">{{ bug.title }}</div>
          <div class="drawer-subtitle">{{ summaryText }}</div>
        </div>
      </template>

      <div class="drawer-body">
        <section class="hero-card bug-hero">
          <div class="hero-main">
            <div class="chip-row">
              <n-tag :type="statusMeta.tone" round>{{ statusMeta.label }}</n-tag>
              <n-tag :type="severityMetaItem.tone" round>{{ severityMetaItem.label }}</n-tag>
            </div>
            <div class="next-action-title">下一步动作</div>
            <div class="next-action-copy">{{ nextActionText }}</div>
          </div>
        </section>

        <section v-if="actionGroups.length" class="section-card">
          <div class="section-title">可执行操作</div>
          <div v-for="group in actionGroups" :key="group.title" class="action-block">
            <div class="action-block-title">{{ group.title }}</div>
            <div class="action-row wrap">
              <n-button
                v-for="action in group.actions"
                :key="action.status"
                :type="action.type"
                :loading="actionLoading"
                @click="executeAction(action)"
              >
                {{ action.label }}
              </n-button>
            </div>
          </div>
        </section>

        <section class="detail-grid">
          <div class="section-card">
            <div class="section-title">Bug 摘要</div>
            <div class="info-list">
              <div class="info-item"><span>所属任务</span><strong>{{ bug.task?.title || '-' }}</strong></div>
              <div class="info-item"><span>创建人</span><strong>{{ bug.creator?.name || '-' }}</strong></div>
              <div class="info-item"><span>当前处理人</span><strong>{{ bug.assignee?.name || '-' }}</strong></div>
            </div>
          </div>

          <div class="section-card">
            <div class="section-title">说明</div>
            <div class="description-block">{{ bug.description || '暂无描述' }}</div>
            <div v-if="bug.fix_comment" class="reason-block success">
              <div class="reason-title">最近修复说明</div>
              <div>{{ bug.fix_comment }}</div>
            </div>
            <div v-if="bug.reopen_reason" class="reason-block error">
              <div class="reason-title">最近重新打开原因</div>
              <div>{{ bug.reopen_reason }}</div>
            </div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-title">状态历史</div>
          <n-timeline class="timeline">
            <n-timeline-item
              v-for="history in histories"
              :key="history.id"
              :type="history.to_status === 'reopened' ? 'error' : 'default'"
            >
              <div class="timeline-title">{{ formatHistory(history) }}</div>
              <div class="timeline-meta">{{ history.user?.name || '系统' }} · {{ formatTime(history.changed_at) }}</div>
            </n-timeline-item>
          </n-timeline>
        </section>
      </div>
    </n-drawer-content>
  </n-drawer>

  <n-modal v-model:show="showReopenModal" title="填写重新打开原因" preset="dialog">
    <n-input v-model:value="reopenReason" type="textarea" placeholder="请输入重新打开原因" />
    <template #action>
      <n-button @click="showReopenModal = false">取消</n-button>
      <n-button type="error" @click="confirmReopen">确认重新打开</n-button>
    </template>
  </n-modal>

  <n-modal v-model:show="showFixModal" title="填写修复说明" preset="dialog">
    <n-input v-model:value="fixComment" type="textarea" placeholder="请输入修复说明" />
    <template #action>
      <n-button @click="showFixModal = false">取消</n-button>
      <n-button type="primary" @click="confirmFix">确认已修复</n-button>
    </template>
  </n-modal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getBug, getBugHistory, changeBugStatus, updateBug } from '@/api/bugs'
import { bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import {
  NDrawer,
  NDrawerContent,
  NTag,
  NButton,
  NTimeline,
  NTimelineItem,
  NModal,
  NInput
} from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const props = defineProps({ bugId: Number })
const emit = defineEmits(['refresh'])

const authStore = useAuthStore()
const bug = ref(null)
const histories = ref([])
const showReopenModal = ref(false)
const showFixModal = ref(false)
const reopenReason = ref('')
const fixComment = ref('')
const actionLoading = ref(false)

const statusMeta = computed(() => bugStatusMeta[bug.value?.status] || { label: bug.value?.status || '-', tone: 'default' })
const severityMetaItem = computed(() => severityMeta[bug.value?.severity] || { label: bug.value?.severity || '-', tone: 'default' })

const summaryText = computed(() => {
  const parts = [bug.value?.task?.title, statusMeta.value.label]
  if (bug.value?.assignee?.name) parts.push(`当前处理人：${bug.value.assignee.name}`)
  return parts.filter(Boolean).join(' · ')
})

const nextActionMap = {
  new: '等待分配给开发处理。',
  assigned: '开发负责人需要开始修复。',
  fixing: '开发负责人完成修复后，提交到待验证。',
  fixed: '系统会自动推进到待验证。',
  pending_verify: '测试人员验证结果，并决定关闭或重新打开。',
  reopened: '需要重新指派给开发继续处理。',
  closed: 'Bug 已完成闭环。'
}
const nextActionText = computed(() => nextActionMap[bug.value?.status] || '查看当前状态并继续推进。')

const role = computed(() => authStore.userInfo?.role)
const availableActions = computed(() => {
  const status = bug.value?.status
  const actions = []

  if (role.value === 'dev' && status === 'assigned') {
    actions.push({ label: '开始修复', status: 'fixing', type: 'primary' })
  }
  if (role.value === 'dev' && status === 'fixing') {
    actions.push({ label: '标记已修复', status: 'fixed', type: 'primary' })
  }
  if (role.value === 'tester' && status === 'pending_verify') {
    actions.push({ label: '验证通过', status: 'closed', type: 'primary' })
    actions.push({ label: '重新打开', status: 'reopened', type: 'error' })
  }
  if ((role.value === 'pm' || role.value === 'tester') && status === 'reopened') {
    actions.push({ label: '重新指派', status: 'assigned', type: 'default' })
  }

  return actions
})

const actionGroups = computed(() => {
  if (!availableActions.value.length) return []
  return [{ title: '状态推进', actions: availableActions.value }]
})

watch(
  [() => props.bugId, show],
  async ([id, visible]) => {
    if (id && visible) {
      await loadDetail()
      await loadHistory()
    }
  },
  { immediate: false }
)

async function loadDetail() {
  try {
    bug.value = await getBug(props.bugId)
  } catch (error) {
    console.error(error)
  }
}

async function loadHistory() {
  try {
    histories.value = await getBugHistory(props.bugId)
  } catch (error) {
    console.error(error)
  }
}

function executeAction(action) {
  if (actionLoading.value) return
  if (action.status === 'reopened') {
    showReopenModal.value = true
    return
  }
  if (action.status === 'fixed') {
    showFixModal.value = true
    return
  }
  doChangeStatus(action.status, '')
}

async function confirmReopen() {
  if (actionLoading.value) return
  if (!reopenReason.value.trim()) {
    window.$message.warning('请输入原因')
    return
  }
  actionLoading.value = true
  try {
    await doChangeStatus('reopened', reopenReason.value)
    try {
      await updateBug(props.bugId, { reopen_reason: reopenReason.value })
    } catch (error) { console.error(error) }
  } finally {
    actionLoading.value = false
    showReopenModal.value = false
    reopenReason.value = ''
  }
}

async function confirmFix() {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    await doChangeStatus('fixed', fixComment.value)
    try {
      await updateBug(props.bugId, { fix_comment: fixComment.value })
    } catch (error) { console.error(error) }
  } finally {
    actionLoading.value = false
    showFixModal.value = false
    fixComment.value = ''
  }
}

async function doChangeStatus(newStatus, comment) {
  actionLoading.value = true
  try {
    await changeBugStatus(props.bugId, { new_status: newStatus, comment })
    window.$message.success('状态变更成功')
    emit('refresh')
    await loadDetail()
    await loadHistory()
  } catch (error) {
    console.error(error)
  } finally {
    actionLoading.value = false
  }
}

function formatHistory(history) {
  const from = bugStatusMeta[history.from_status]?.label || history.from_status
  const to = bugStatusMeta[history.to_status]?.label || history.to_status
  return history.comment ? `${from} → ${to}（${history.comment}）` : `${from} → ${to}`
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.drawer-kicker {
  font-size: 12px;
  color: #e11d48;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.drawer-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.drawer-subtitle {
  font-size: 13px;
  color: #64748b;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-card,
.section-card {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: white;
}

.hero-card {
  padding: 18px;
}

.bug-hero {
  background: linear-gradient(135deg, #fff1f2 0%, #ffffff 100%);
}

.chip-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.next-action-title,
.section-title,
.action-block-title,
.reason-title {
  font-weight: 700;
  color: #0f172a;
}

.next-action-title,
.section-title {
  font-size: 15px;
}

.next-action-copy,
.description-block,
.timeline-meta {
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
}

.section-card {
  padding: 18px;
}

.action-block + .action-block {
  margin-top: 16px;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.action-row.wrap {
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}

.info-item span {
  color: #64748b;
}

.info-item strong {
  color: #0f172a;
  text-align: right;
}

.description-block {
  margin-top: 14px;
}

.reason-block {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 14px;
}

.reason-block.success {
  background: #ecfdf5;
  color: #047857;
}

.reason-block.error {
  background: #fff1f2;
  color: #be123c;
}

.timeline {
  margin-top: 16px;
}

.timeline-title {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

.timeline-meta {
  margin-top: 4px;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .action-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
