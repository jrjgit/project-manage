<template>
  <n-drawer v-model:show="show" :width="560" placement="right">
    <n-drawer-content v-if="task" closable>
      <template #header>
        <div class="drawer-header">
          <div class="drawer-kicker">任务 #{{ task.id }}</div>
          <div class="drawer-title">{{ task.title }}</div>
          <div class="drawer-subtitle">{{ summaryText }}</div>
        </div>
      </template>

      <div class="drawer-body">
        <section class="hero-card">
          <div class="hero-main">
            <div class="chip-row">
              <n-tag :type="statusMeta.tone" round>{{ statusMeta.label }}</n-tag>
              <n-tag :type="priorityMetaItem.tone" round>{{ priorityMetaItem.label }}</n-tag>
            </div>
            <div class="next-action-title">下一步动作</div>
            <div class="next-action-copy">{{ nextActionText }}</div>
          </div>
        </section>

        <section v-if="actionGroups.length || canAssignDev || canAssignTesterLead || canAssignTester" class="section-card">
          <div class="section-title">可执行操作</div>

          <div v-if="canAssignDev" class="action-block">
            <div class="action-block-title">分配开发并进入开发</div>
            <div class="action-block-copy">开发组长先指定负责人，再把任务推进到开发中。</div>
            <div class="action-row">
              <n-select v-model:value="selectedDev" :options="devOptions" placeholder="选择开发人员" style="width: 220px;" />
              <n-button type="primary" :loading="actionLoading" @click="assignDevAndStart">指派并开始开发</n-button>
            </div>
          </div>

          <div v-if="canAssignTesterLead" class="action-block">
            <div class="action-block-title">分配测试组长并进入测试</div>
            <div class="action-block-copy">项目经理指定测试组长后，任务会进入测试阶段。</div>
            <div class="action-row">
              <n-select v-model:value="selectedTesterLead" :options="testerLeadOptions" placeholder="选择测试组长" style="width: 220px;" />
              <n-button type="primary" :loading="actionLoading" @click="assignTesterLeadAndTest">分配并开始测试</n-button>
            </div>
          </div>

          <div v-if="canAssignTester" class="action-block">
            <div class="action-block-title">分配测试人员</div>
            <div class="action-block-copy">测试组长把当前测试任务明确分配给测试人员。</div>
            <div class="action-row">
              <n-select v-model:value="selectedTester" :options="testerOptions" placeholder="选择测试人员" style="width: 220px;" />
              <n-button :loading="actionLoading" @click="assignTester">确认分配</n-button>
            </div>
          </div>

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
            <div class="section-title">任务摘要</div>
            <div class="info-list">
              <div class="info-item"><span>项目</span><strong>{{ task.project?.name || '-' }}</strong></div>
              <div class="info-item"><span>创建人</span><strong>{{ task.creator?.name || '-' }}</strong></div>
              <div class="info-item"><span>开发组长</span><strong>{{ task.dev_lead?.name || '-' }}</strong></div>
              <div class="info-item"><span>当前开发</span><strong>{{ task.assignee?.name || '-' }}</strong></div>
              <div class="info-item"><span>测试组长</span><strong>{{ task.tester_lead?.name || '-' }}</strong></div>
              <div class="info-item"><span>测试人员</span><strong>{{ task.tester?.name || '-' }}</strong></div>
              <div class="info-item"><span>截止日期</span><strong>{{ formatDate(task.deadline) || '-' }}</strong></div>
            </div>
          </div>

          <div class="section-card">
            <div class="section-title">说明</div>
            <div class="description-block">{{ task.description || '暂无描述' }}</div>
            <div v-if="task.reject_reason" class="reason-block error">
              <div class="reason-title">最近打回原因</div>
              <div>{{ task.reject_reason }}</div>
            </div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-title">状态历史</div>
          <n-timeline class="timeline">
            <n-timeline-item
              v-for="history in histories"
              :key="history.id"
              :type="history.to_status === 'rejected' ? 'error' : 'default'"
            >
              <div class="timeline-title">{{ formatHistory(history) }}</div>
              <div class="timeline-meta">{{ history.user?.name || '系统' }} · {{ formatTime(history.changed_at) }}</div>
            </n-timeline-item>
          </n-timeline>
        </section>
      </div>
    </n-drawer-content>
  </n-drawer>

  <n-modal v-model:show="showRejectModal" title="填写打回原因" preset="dialog">
    <n-input v-model:value="rejectReason" type="textarea" placeholder="请输入打回原因" />
    <template #action>
      <n-button @click="showRejectModal = false">取消</n-button>
      <n-button type="error" @click="confirmReject">确认打回</n-button>
    </template>
  </n-modal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getTask, getTaskHistory, changeTaskStatus, updateTask } from '@/api/tasks'
import { getUsers } from '@/api/users'
import { priorityMeta, taskStatusMeta } from '@/constants/statusMeta'
import {
  NDrawer,
  NDrawerContent,
  NTag,
  NButton,
  NTimeline,
  NTimelineItem,
  NModal,
  NInput,
  NSelect
} from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const props = defineProps({ taskId: Number })
const emit = defineEmits(['status-change', 'refresh'])

const authStore = useAuthStore()
const task = ref(null)
const histories = ref([])
const users = ref([])
const showRejectModal = ref(false)
const rejectReason = ref('')
const selectedTester = ref(null)
const selectedDev = ref(null)
const selectedTesterLead = ref(null)
const actionLoading = ref(false)

const statusMeta = computed(() => taskStatusMeta[task.value?.status] || { label: task.value?.status || '-', tone: 'default' })
const priorityMetaItem = computed(() => priorityMeta[task.value?.priority] || { label: task.value?.priority || '-', tone: 'default' })

const devOptions = computed(() => users.value.filter((user) => user.role === 'dev').map((user) => ({ label: user.name, value: user.id })))
const testerLeadOptions = computed(() => users.value.filter((user) => user.role === 'tester_lead').map((user) => ({ label: user.name, value: user.id })))
const testerOptions = computed(() => users.value.filter((user) => user.role === 'tester').map((user) => ({ label: user.name, value: user.id })))

const canAssignDev = computed(() => authStore.userInfo?.role === 'dev_lead' && task.value?.status === 'assigned_lead')
const canAssignTesterLead = computed(() => authStore.userInfo?.role === 'pm' && task.value?.status === 'pending_test')
const canAssignTester = computed(() => authStore.userInfo?.role === 'tester_lead' && task.value?.status === 'testing')

const summaryText = computed(() => {
  const parts = [task.value?.project?.name, statusMeta.value.label]
  if (task.value?.assignee?.name) parts.push(`当前开发：${task.value.assignee.name}`)
  else if (task.value?.tester?.name) parts.push(`当前测试：${task.value.tester.name}`)
  return parts.filter(Boolean).join(' · ')
})

const nextActionMap = {
  pending: '等待项目经理分配开发组长。',
  assigned_lead: '开发组长需要指定开发负责人并让任务开始进入开发。',
  developing: '开发负责人完成实现后，将任务推进到开发完成。',
  developed: '系统会把任务加入测试池，等待测试侧接手。',
  pending_test: '项目经理需要指定测试组长并让任务进入测试。',
  testing: '测试人员执行验证，并给出通过或打回结果。',
  passed: '项目经理确认结果后关闭任务。',
  rejected: '开发负责人根据打回原因重新进入开发。',
  closed: '任务流程已完成。'
}
const nextActionText = computed(() => nextActionMap[task.value?.status] || '查看当前状态并继续推进。')

const role = computed(() => authStore.userInfo?.role)
const availableActions = computed(() => {
  const status = task.value?.status
  const actions = []

  if (role.value === 'dev' && (status === 'assigned_lead' || status === 'rejected')) {
    actions.push({ label: status === 'rejected' ? '重新进入开发' : '开始开发', status: 'developing', type: 'primary' })
  }
  if (role.value === 'dev' && status === 'developing') {
    actions.push({ label: '标记开发完成', status: 'developed', type: 'primary' })
  }
  if (role.value === 'tester' && status === 'testing') {
    actions.push({ label: '测试通过', status: 'passed', type: 'primary' })
    actions.push({ label: '打回修改', status: 'rejected', type: 'error' })
  }
  if (role.value === 'pm' && status === 'pending') {
    actions.push({ label: '分配开发组长', status: 'assigned_lead', type: 'primary' })
  }
  if (role.value === 'pm' && status === 'pending_test') {
    actions.push({ label: '分配测试组长并进入测试', status: 'testing', type: 'primary' })
  }
  if (role.value === 'pm' && (status === 'passed' || status === 'rejected')) {
    actions.push({ label: '关闭任务', status: 'closed', type: 'default' })
  }

  return actions
})

const actionGroups = computed(() => {
  if (!availableActions.value.length) return []
  return [{ title: '状态推进', actions: availableActions.value }]
})

watch(
  [() => props.taskId, show],
  async ([id, visible]) => {
    if (id && visible) {
      await loadDetail()
      await loadHistory()
      await loadUsers()
    }
  },
  { immediate: false }
)

async function loadUsers() {
  try {
    users.value = await getUsers()
  } catch (error) {}
}

async function loadDetail() {
  try {
    task.value = await getTask(props.taskId)
  } catch (error) {
    console.error(error)
  }
}

async function loadHistory() {
  try {
    histories.value = await getTaskHistory(props.taskId)
  } catch (error) {
    console.error(error)
  }
}

function executeAction(action) {
  if (actionLoading.value) return
  if (action.status === 'rejected') {
    showRejectModal.value = true
    return
  }
  if (action.status === 'developing' && authStore.userInfo?.role === 'dev' && !task.value.assignee_id) {
    actionLoading.value = true
    updateTask(props.taskId, { assignee_id: authStore.userInfo.id }).then(() => {
      doChangeStatus(action.status, '')
    }).finally(() => {
      actionLoading.value = false
    })
    return
  }
  doChangeStatus(action.status, '')
}

async function assignDevAndStart() {
  if (actionLoading.value) return
  if (!selectedDev.value) {
    window.$message.warning('请选择开发人员')
    return
  }
  actionLoading.value = true
  try {
    await updateTask(props.taskId, { assignee_id: selectedDev.value })
    await doChangeStatus('developing', '')
    selectedDev.value = null
  } catch (error) {
    console.error(error)
  } finally {
    actionLoading.value = false
  }
}

async function assignTesterLeadAndTest() {
  if (actionLoading.value) return
  if (!selectedTesterLead.value) {
    window.$message.warning('请选择测试组长')
    return
  }
  actionLoading.value = true
  try {
    await updateTask(props.taskId, { tester_lead_id: selectedTesterLead.value })
    await doChangeStatus('testing', '')
    selectedTesterLead.value = null
  } catch (error) {
    console.error(error)
  } finally {
    actionLoading.value = false
  }
}

async function assignTester() {
  if (actionLoading.value) return
  if (!selectedTester.value) {
    window.$message.warning('请选择测试人员')
    return
  }
  actionLoading.value = true
  try {
    await updateTask(props.taskId, { tester_id: selectedTester.value })
    window.$message.success('分配成功')
    selectedTester.value = null
    await loadDetail()
    emit('refresh')
  } catch (error) {
    console.error(error)
  } finally {
    actionLoading.value = false
  }
}

async function confirmReject() {
  if (actionLoading.value) return
  if (!rejectReason.value.trim()) {
    window.$message.warning('请输入打回原因')
    return
  }
  actionLoading.value = true
  try {
    await doChangeStatus('rejected', rejectReason.value)
  } finally {
    actionLoading.value = false
    showRejectModal.value = false
    rejectReason.value = ''
  }
}

async function doChangeStatus(newStatus, comment) {
  actionLoading.value = true
  try {
    await changeTaskStatus(props.taskId, { new_status: newStatus, comment })
    window.$message.success('状态变更成功')
    emit('status-change', { taskId: props.taskId, newStatus, comment })
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
  const from = taskStatusMeta[history.from_status]?.label || history.from_status
  const to = taskStatusMeta[history.to_status]?.label || history.to_status
  return history.comment ? `${from} → ${to}（${history.comment}）` : `${from} → ${to}`
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

function formatDate(value) {
  if (!value) return ''
  const date = new Date(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
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
  color: #6366f1;
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
  background: linear-gradient(135deg, #eef2ff 0%, #ffffff 100%);
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
.action-block-copy,
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

.action-block-copy {
  margin-top: 6px;
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
