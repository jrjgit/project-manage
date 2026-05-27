<template>
  <n-drawer v-model:show="show" :width="520" placement="right">
    <n-drawer-content v-if="task" closable>
      <template #header>
        <div class="drawer-header">
          <div class="drawer-kicker">任务 #{{ task.id }}</div>
          <div class="drawer-title">{{ task.title }}</div>
        </div>
      </template>

      <div class="drawer-body">
        <section class="hero-card">
          <div class="hero-main">
            <div class="chip-row">
              <n-tag :type="statusMeta.tone" round>{{ statusMeta.label }}</n-tag>
              <n-tag :type="priorityMetaItem.tone" round>{{ priorityMetaItem.label }}</n-tag>
            </div>
            <div class="next-action-title">当前状态</div>
            <div class="next-action-copy">{{ nextActionText }}</div>
          </div>
        </section>

        <section v-if="actionGroups.length || canAssignDev || canEditDevLead || canEditTester" class="section-card">
          <div class="section-title">可执行操作</div>

          <div v-if="canEditDevLead" class="action-block">
            <div class="action-block-title">切换开发组长</div>
            <div class="action-block-copy">重新指定该任务的开发组长。</div>
            <div class="action-row">
              <n-select v-model:value="selectedDevLead" :options="devLeadOptions" placeholder="选择开发组长" style="width: 220px;" />
              <n-button :loading="actionLoading" @click="saveDevLead">确认</n-button>
            </div>
          </div>

          <div v-if="canEditTester" class="action-block">
            <div class="action-block-title">指派测试人员</div>
            <div class="action-block-copy">为任务指定测试负责人。</div>
            <div class="action-row">
              <n-select v-model:value="selectedTester" :options="testerOptions" placeholder="选择测试人员" style="width: 220px;" />
              <n-button :loading="actionLoading" @click="saveTester">确认指派</n-button>
            </div>
          </div>

          <div v-for="group in actionGroups" :key="group.title" class="action-block">
            <div class="action-block-title">{{ group.title }}</div>
            <div class="action-row wrap">
              <n-button v-for="action in group.actions" :key="action.status"
                :type="action.type" :loading="actionLoading" @click="executeAction(action)">
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
              <div class="info-item"><span>所属需求</span><strong>{{ task.requirement_name || '-' }}</strong></div>
              <div class="info-item"><span>创建人</span><strong>{{ task.creator?.name || '-' }}</strong></div>
              <div class="info-item"><span>开发组长</span><strong>{{ task.dev_lead?.name || '-' }}</strong></div>
              <div class="info-item"><span>指派人</span><strong>{{ task.assignee?.name || '-' }}</strong></div>
              <div class="info-item"><span>测试人员</span><strong>{{ task.tester?.name || '-' }}</strong></div>
              <div class="info-item"><span>技能</span><strong>{{ task.terminal || '-' }}</strong></div>
              <div class="info-item"><span>绩效工时</span><strong>{{ task.performance || '-' }}</strong></div>
              <div class="info-item"><span>截止日期</span><strong>{{ formatDate(task.deadline) || '-' }}</strong></div>
            </div>
          </div>
          <div class="section-card">
            <div class="section-title">说明</div>
            <div class="description-block">{{ task.description || '暂无描述' }}</div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-title">状态历史</div>
          <n-timeline class="timeline">
            <n-timeline-item v-for="history in histories" :key="history.id" type="default">
              <div class="timeline-title">{{ formatHistory(history) }}</div>
              <div class="timeline-meta">{{ history.user?.name || '系统' }} · {{ formatTime(history.changed_at) }}</div>
            </n-timeline-item>
          </n-timeline>
        </section>
      </div>
    </n-drawer-content>
  </n-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getTask, getTaskHistory, changeTaskStatus, updateTask } from '@/api/tasks'
import { getUsers } from '@/api/users'
import { priorityMeta, taskStatusMeta } from '@/constants/statusMeta'
import { NDrawer, NDrawerContent, NTag, NButton, NTimeline, NTimelineItem, NSelect } from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const props = defineProps({ taskId: Number })
const emit = defineEmits(['status-change', 'refresh'])

const authStore = useAuthStore()
const task = ref(null)
const histories = ref([])
const users = ref([])
const selectedTester = ref(null)
const selectedDevLead = ref(null)
const actionLoading = ref(false)

const statusMeta = computed(() => taskStatusMeta[task.value?.status] || { label: task.value?.status || '-', tone: 'default' })
const priorityMetaItem = computed(() => priorityMeta[task.value?.priority] || { label: task.value?.priority || '-', tone: 'default' })

const testerOptions = computed(() => users.value.filter(u => u.role === 'tester').map(u => ({ label: u.name, value: u.id })))
const devLeadOptions = computed(() => users.value.filter(u => u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id })))

const canEditDevLead = computed(() => authStore.userInfo?.role === 'pm' && ['pending', 'developing'].includes(task.value?.status))
const canEditTester = computed(() => authStore.userInfo?.role === 'pm' && ['pending', 'developing', 'testing'].includes(task.value?.status))

const nextActionText = computed(() => ({
  pending: '等待分配开发人员进行开发。',
  developing: '开发中，完成后将进入综合测试。',
  testing: '测试人员执行验证，通过后即可完成。',
  closed: '任务流程已完成。'
}[task.value?.status] || '查看当前状态并继续推进。'))

const availableActions = computed(() => {
  const status = task.value?.status
  const role = authStore.userInfo?.role
  const actions = []
  const myId = authStore.userInfo?.id
  const isMyLead = task.value?.dev_lead_id === myId
  const isMyTask = task.value?.assignee_id === myId
  if (role === 'pm' && status === 'pending') actions.push({ label: '开始开发', status: 'developing', type: 'primary' })
  if (role === 'pm' && status === 'developing') actions.push({ label: '进入测试', status: 'testing', type: 'primary' })
  if (role === 'pm' && status === 'testing') actions.push({ label: '完成', status: 'closed', type: 'success' })
  if (role === 'pm' && status === 'testing') actions.push({ label: '打回开发', status: 'developing', type: 'error' })
  if (role === 'dev_lead' && isMyLead && status === 'pending') actions.push({ label: '开始开发', status: 'developing', type: 'primary' })
  if (role === 'dev_lead' && isMyLead && status === 'developing') actions.push({ label: '完成开发', status: 'testing', type: 'primary' })
  if (role === 'dev' && isMyTask && status === 'pending') actions.push({ label: '开始开发', status: 'developing', type: 'primary' })
  if (role === 'dev' && isMyTask && status === 'developing') actions.push({ label: '完成开发', status: 'testing', type: 'primary' })
  if (role === 'tester' && status === 'testing') { actions.push({ label: '测试通过', status: 'closed', type: 'success' }); actions.push({ label: '打回开发', status: 'developing', type: 'error' }) }
  return actions
})
const actionGroups = computed(() => availableActions.value.length ? [{ title: '状态推进', actions: availableActions.value }] : [])

watch([() => props.taskId, show], async ([id, visible]) => {
  if (id && visible) { await loadDetail(); await loadHistory(); await loadUsers() }
})

async function loadUsers() { try { users.value = await getUsers() } catch {} }
async function loadDetail() { try { const res = await getTask(props.taskId); task.value = res.task } catch {} }
async function loadHistory() { try { histories.value = await getTaskHistory(props.taskId) } catch {} }

async function saveDevLead() {
  if (!selectedDevLead.value) { window.$message?.warning('请选择开发组长'); return }
  actionLoading.value = true
  try {
    await updateTask(props.taskId, { dev_lead_id: selectedDevLead.value })
    window.$message?.success('开发组长已更新')
    selectedDevLead.value = null
    await loadDetail(); emit('refresh')
  } catch (e) { console.error(e) }
  actionLoading.value = false
}

async function saveTester() {
  if (!selectedTester.value) { window.$message?.warning('请选择测试人员'); return }
  actionLoading.value = true
  try {
    await updateTask(props.taskId, { tester_id: selectedTester.value })
    window.$message?.success('指派成功')
    selectedTester.value = null
    await loadDetail(); emit('refresh')
  } catch (e) { console.error(e) }
  actionLoading.value = false
}

function executeAction(action) {
  doChangeStatus(action.status, '')
}

async function doChangeStatus(newStatus, comment) {
  actionLoading.value = true
  try {
    await changeTaskStatus(props.taskId, { new_status: newStatus, comment })
    window.$message?.success('状态变更成功')
    emit('status-change', { taskId: props.taskId, newStatus, comment })
    emit('refresh')
    await loadDetail()
    await loadHistory()
  } catch (e) { console.error(e) }
  actionLoading.value = false
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
.drawer-header { display: flex; flex-direction: column; gap: 6px; }
.drawer-kicker { font-size: 12px; color: #6366f1; font-weight: 700; letter-spacing: 0.08em; text-transform: uppercase; }
.drawer-title { font-size: 20px; font-weight: 700; color: #0f172a; }
.drawer-body { display: flex; flex-direction: column; gap: 16px; }
.hero-card, .section-card { border: 1px solid #e2e8f0; border-radius: 18px; background: white; }
.hero-card { padding: 18px; background: linear-gradient(135deg, #eef2ff 0%, #ffffff 100%); }
.section-card { padding: 18px; }
.chip-row { display: flex; gap: 10px; margin-bottom: 14px; }
.next-action-title { font-size: 15px; font-weight: 700; color: #0f172a; }
.next-action-copy { margin-top: 6px; font-size: 13px; line-height: 1.7; color: #64748b; }
.section-title { font-size: 15px; font-weight: 700; color: #0f172a; }
.action-block + .action-block { margin-top: 16px; }
.action-block-copy { margin-top: 6px; font-size: 13px; color: #64748b; }
.action-row { display: flex; align-items: center; gap: 12px; margin-top: 12px; }
.action-row.wrap { flex-wrap: wrap; }
.detail-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.info-list { display: flex; flex-direction: column; gap: 10px; margin-top: 14px; }
.info-item { display: flex; justify-content: space-between; gap: 12px; font-size: 13px; }
.info-item span { color: #64748b; }
.info-item strong { color: #0f172a; text-align: right; }
.description-block { margin-top: 14px; font-size: 13px; line-height: 1.7; color: #64748b; }
.timeline { margin-top: 16px; }
.timeline-title { font-size: 13px; color: #0f172a; font-weight: 600; }
.timeline-meta { margin-top: 4px; font-size: 12px; color: #94a3b8; }
@media (max-width: 900px) { .detail-grid { grid-template-columns: 1fr; } }
</style>
