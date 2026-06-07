<template>
  <n-drawer v-model:show="show" :width="760" placement="right">
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

        <section v-if="actionGroups.length || canEditDevLead || canEditTester" class="section-card">
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

        <section v-if="canReportProgress" class="section-card">
          <div class="section-title">上报进度</div>
          <div style="margin-top:12px">
            <div style="display:flex;align-items:center;gap:12px">
              <n-slider v-model:value="reportProgress" :min="0" :max="100" :step="1" style="flex:1" />
              <span style="font-size:15px;font-weight:700;color:#6366f1;min-width:40px;text-align:right">{{ reportProgress }}%</span>
            </div>
            <n-input v-model:value="progressComment" type="textarea" :autosize="{ minRows: 1, maxRows: 3 }" placeholder="备注（可选）" style="margin-top:8px" />
            <n-button type="primary" size="small" style="margin-top:8px" :loading="submittingProgress" @click="submitProgress">提交进度</n-button>
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
              <div class="info-item"><span>开发人员</span><strong>{{ task.assignee?.name || '-' }}</strong></div>
              <div class="info-item"><span>测试人员</span><strong>{{ task.tester?.name || '-' }}</strong></div>
              <div class="info-item"><span>技能</span><strong>{{ task.terminal || '-' }}</strong></div>
              <div class="info-item"><span>绩效工时</span><strong>{{ task.performance || '-' }}</strong></div>
              <div class="info-item"><span>截止日期</span><strong>{{ formatDate(task.deadline) || '-' }}</strong></div>
            </div>
          </div>
          <div style="display:flex;flex-direction:column;gap:16px">
            <div class="section-card">
              <div class="section-title">项目经理备注</div>
              <div class="description-block">{{ task.requirement_desc || task.description || '暂无描述' }}</div>
            </div>
            <div class="section-card">
            <div class="section-title">技术经理备注</div>
            <div class="description-block" style="white-space:pre-wrap">{{ task.description || '暂无备注' }}</div>
          </div>
          </div>
        </section>

        <!-- 关联 Bug -->
        <section v-if="taskBugs.length > 0" class="section-card">
          <div class="section-title">关联 Bug（{{ taskBugs.length }}）</div>
          <div class="bug-list-compact">
            <div v-for="bug in taskBugs" :key="bug.id" class="bug-item-compact">
              <span class="bugc-title">{{ bug.title }}</span>
              <n-tag size="tiny" :type="bugStatusMeta[bug.status]?.tone || 'default'">{{ bugStatusMeta[bug.status]?.label || bug.status }}</n-tag>
              <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'">{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
              <span class="bugc-creator">{{ bug.creator?.name || '-' }}</span>
            </div>
          </div>
        </section>

        <!-- 需求文档 -->
        <section v-if="reqDoc?.document_name" class="section-card">
          <div class="section-title">需求文档</div>
          <div class="doc-row">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px;flex-shrink:0">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
            <span class="doc-name">{{ reqDoc.document_name }}</span>
            <n-button size="tiny" @click="downloadDoc">下载</n-button>
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

  <!-- Create Bug Modal -->
  <n-modal v-model:show="showCreateBugModal" preset="card" style="width:500px" title="创建 Bug" :mask-closable="false">
    <n-form label-placement="top">
      <n-form-item label="标题" path="title">
        <n-input v-model:value="bugForm.title" placeholder="Bug标题" />
      </n-form-item>
      <n-form-item label="描述">
        <n-input v-model:value="bugForm.description" type="textarea" placeholder="描述 Bug 现象" />
      </n-form-item>
      <n-form-item label="严重程度">
        <n-select v-model:value="bugForm.severity" :options="severityOptions" />
      </n-form-item>
      <n-form-item label="截图">
        <n-upload :show-file-list="false" :custom-request="handleBugUpload" accept="image/*">
          <n-button :loading="bugSubmitting">选择图片</n-button>
        </n-upload>
        <span v-if="bugAttachName" style="font-size:12px;color:#18a058;margin-left:8px">{{ bugAttachName }}</span>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="showCreateBugModal = false">取消</n-button>
        <n-button type="primary" :loading="bugSubmitting" @click="submitBug">确定</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getTask, getTaskHistory, changeTaskStatus, updateTask } from '@/api/tasks'
import { createBug, uploadBugImage, getBugs } from '@/api/bugs'
import { getRequirement, downloadRequirementDocument } from '@/api/requirements'
import { getUsers } from '@/api/users'
import { priorityMeta, taskStatusMeta, bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import { NDrawer, NDrawerContent, NTag, NButton, NTimeline, NTimelineItem, NSelect, NSlider, NModal, NForm, NFormItem, NInput, NUpload, NSpace } from 'naive-ui'

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
const reqDoc = ref(null)
const taskBugs = ref([])
const reportProgress = ref(0)
const progressComment = ref('')
const submittingProgress = ref(false)

// 创建 Bug 弹窗
const showCreateBugModal = ref(false)
const bugForm = ref({ title: '', description: '', severity: 'medium' })
const bugImageFile = ref(null)
const bugAttachName = ref('')
const bugSubmitting = ref(false)
const severityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'critical' }
]

const statusMeta = computed(() => taskStatusMeta[task.value?.status] || { label: task.value?.status || '-', tone: 'default' })
const priorityMetaItem = computed(() => priorityMeta[task.value?.priority] || { label: task.value?.priority || '-', tone: 'default' })

const testerOptions = computed(() => users.value.filter(u => u.role === 'tester').map(u => ({ label: u.name, value: u.id })))
const devLeadOptions = computed(() => users.value.filter(u => u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id })))

const canEditDevLead = computed(() => authStore.userInfo?.role === 'pm' && ['pending', 'developing'].includes(task.value?.status))
const canEditTester = computed(() => authStore.userInfo?.role === 'pm' && ['pending', 'developing', 'testing'].includes(task.value?.status))

const nextActionText = computed(() => ({
  pending: '等待分配开发人员进行开发。',
  developing: '开发中，完成后将进入测试。',
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
  if (['dev', 'dev_lead'].includes(role) && isMyTask && status === 'pending') actions.push({ label: '开始开发', status: 'developing', type: 'primary' })
  if (['dev', 'dev_lead'].includes(role) && isMyTask && status === 'developing') actions.push({ label: '完成开发', status: 'testing', type: 'primary' })
  if (role === 'tester' && status === 'testing') {
    actions.push({ label: '创建Bug', status: null, type: 'warning', action: 'createBug' })
    actions.push({ label: '关闭任务', status: 'closed', type: 'success' })
  }
  return actions
})
const actionGroups = computed(() => {
  if (!availableActions.value.length) return []
  return [{ title: '状态推进', actions: availableActions.value }]
})

const canReportProgress = computed(() => {
  const status = task.value?.status
  const role = authStore.userInfo?.role
  const myId = authStore.userInfo?.id
  const isMyTask = task.value?.assignee_id === myId
  return status === 'developing' && (['dev', 'dev_lead'].includes(role) && isMyTask || role === 'pm')
})

watch([() => props.taskId, show], async ([id, visible]) => {
  if (id && visible) { await loadDetail(); await loadHistory(); await loadUsers() }
})

async function loadUsers() { try { users.value = await getUsers() } catch (e) { console.error(e) } }
async function loadDetail() {
  try {
    const res = await getTask(props.taskId)
    task.value = res.task
    if (task.value?.progress != null) reportProgress.value = task.value.progress
    if (task.value?.requirement_id) {
      const reqData = await getRequirement(task.value.requirement_id)
      if (reqData.document_name) reqDoc.value = reqData
      else reqDoc.value = null
    }
    taskBugs.value = await getBugs({ task_id: props.taskId }) || []
  } catch (e) { console.error(e) }
}
async function loadHistory() { try { histories.value = await getTaskHistory(props.taskId) } catch (e) { console.error(e) } }

async function downloadDoc() {
  if (!reqDoc.value?.id) return
  try {
    const res = await downloadRequirementDocument(reqDoc.value.id)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url; a.download = reqDoc.value.document_name || 'document'
    document.body.appendChild(a); a.click(); document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) { console.error(e) }
}

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

function handleCreateBug() {
  if (!task.value) return
  bugForm.value = {
    title: task.value.title,
    description: '',
    severity: 'medium'
  }
  bugImageFile.value = null
  bugAttachName.value = ''
  showCreateBugModal.value = true
}

function handleBugUpload({ file }) {
  bugImageFile.value = file.file
  bugAttachName.value = file.name
}

async function submitBug() {
  if (!bugForm.value.title.trim()) { window.$message?.warning('请输入标题'); return }
  if (!task.value) return
  bugSubmitting.value = true
  try {
    const payload = {
      title: bugForm.value.title,
      description: bugForm.value.description || undefined,
      severity: bugForm.value.severity,
      task_id: props.taskId,
      assignee_id: task.value?.assignee_id
    }
    const created = await createBug(payload)
    if (bugImageFile.value) {
      await uploadBugImage(created.id, bugImageFile.value)
    }
    window.$message?.success('Bug 创建成功')
    showCreateBugModal.value = false
    emit('refresh')
  } catch (e) { console.error(e) }
  bugSubmitting.value = false
}

function executeAction(action) {
  if (action.action === 'createBug') {
    handleCreateBug()
  } else {
    doChangeStatus(action.status, '')
  }
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

async function submitProgress() {
  if (reportProgress.value < 0 || reportProgress.value > 100) { window.$message?.warning('进度范围为0-100'); return }
  submittingProgress.value = true
  try {
    await updateTask(props.taskId, { progress: reportProgress.value, description: progressComment.value || undefined })
    window.$message?.success('进度已上报')
    if (task.value) task.value.progress = reportProgress.value
    emit('refresh')
  } catch (e) { console.error(e) }
  submittingProgress.value = false
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
.doc-row { display: flex; align-items: center; gap: 10px; margin-top: 12px; padding: 10px 12px; background: #f8fafc; border-radius: 8px; }
.doc-name { flex: 1; font-size: 13px; color: #0f172a; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 760px) { .detail-grid { grid-template-columns: 1fr; } }
.bug-list-compact { display: flex; flex-direction: column; gap: 6px; margin-top: 12px; }
.bug-item-compact { display: flex; align-items: center; gap: 8px; padding: 6px 0; border-bottom: 1px solid #f1f5f9; font-size: 12px; }
.bug-item-compact .bugc-title { flex: 1; color: #334155; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.bug-item-compact .bugc-creator { color: #94a3b8; min-width: 40px; text-align: right; }
</style>
