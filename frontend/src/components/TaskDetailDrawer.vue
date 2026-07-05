<template>
  <n-drawer v-model:show="show" :width="windowWidth <= 768 ? '100%' : 920" placement="right">
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
            <div class="task-overview-row">
              <div class="overview-item">
                <span class="overview-label">需求编号</span>
                <span class="overview-value">{{ requirementData?.number || task.requirement_name || '-' }}</span>
              </div>
              <div class="overview-item">
                <span class="overview-label">所属系统</span>
                <span class="overview-value">{{ requirementData?.system || '-' }}</span>
              </div>
              <div class="overview-item">
                <span class="overview-label">平台</span>
                <span class="overview-value">{{ skillsMap[task.terminal] || task.terminal || '-' }}</span>
              </div>
            </div>
            <div class="chip-row">
              <n-tag :type="statusMeta.tone" round>{{ statusMeta.label }}</n-tag>
              <n-tag :type="priorityMetaItem.tone" round>{{ priorityMetaItem.label }}</n-tag>
            </div>
            <div class="deadline-row">
              <span class="deadline-item">
                <span class="deadline-label">截止日期</span>
                <span class="deadline-value">{{ formatDate(task.deadline) || '-' }}</span>
              </span>
              <span v-if="overdueDays > 0" class="deadline-overdue">延期天数：{{ overdueDays }} 天</span>
            </div>
            <div class="next-action-title">当前状态</div>
            <div class="next-action-copy">{{ nextActionText }}</div>
          </div>
        </section>

        <template v-if="!readonly">
          <section v-if="actionGroups.length || canEditDevLead" class="section-card">
            <div class="section-title">可执行操作</div>

            <div v-if="canEditDevLead" class="action-block">
              <div class="action-block-title">切换开发组长</div>
              <div class="action-block-copy">重新指定该任务的开发组长。</div>
              <div class="action-row">
                <n-select v-model:value="selectedDevLead" :options="devLeadOptions" placeholder="选择开发组长" style="width: 220px;" />
                <n-button :loading="actionLoading" @click="saveDevLead">确认</n-button>
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
        </template>

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
               <div class="info-item"><span>终端</span><strong>{{ skillsMap[task.terminal] || task.terminal || '-' }}</strong></div>
              <div class="info-item"><span>所属系统</span><strong>{{ requirementData?.system || '-' }}</strong></div>
              <div class="info-item"><span>绩效工时</span><strong>{{ task.performance || '-' }}</strong></div>
              <div class="info-item"><span>截止日期</span><strong>{{ formatDate(task.deadline) || '-' }}</strong></div>
            </div>
          </div>
          <div style="display:flex;flex-direction:column;gap:16px">
            <div class="section-card">
              <div class="section-title">项目经理备注</div>
              <div class="description-block">{{ requirementData?.notes || '暂无备注' }}</div>
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
    <n-drawer-content v-else-if="taskNotFound" closable>
      <div style="text-align:center;padding:60px 0;color:#94a3b8">
        <div style="font-size:40px;font-weight:700;color:#d03050;margin-bottom:12px">404</div>
        <div style="font-size:15px">该任务已被删除</div>
      </div>
    </n-drawer-content>
  </n-drawer>

  <!-- Create Bug Modal -->
  <n-modal v-model:show="showCreateBugModal" preset="card" style="width: min(92vw, 500px)" title="创建 Bug" :mask-closable="false">
    <n-form label-placement="top" @paste="handleBugFormPaste">
      <n-form-item label="标题" path="title">
        <n-input v-model:value="bugForm.title" placeholder="Bug标题" />
      </n-form-item>
      <n-form-item label="实际结果">
        <n-input v-model:value="bugForm.description" type="textarea" placeholder="描述实际出现的现象" />
      </n-form-item>
      <n-form-item label="预期结果">
        <n-input v-model:value="bugForm.expected_result" type="textarea" placeholder="描述预期的正常行为" />
      </n-form-item>
      <n-form-item label="严重程度">
        <n-select v-model:value="bugForm.severity" :options="severityOptions" />
      </n-form-item>
      <n-form-item label="截图">
        <n-upload :show-file-list="false" :custom-request="handleBugUpload" accept="image/*" multiple>
          <n-button :loading="bugSubmitting">选择图片</n-button>
        </n-upload>
        <span style="font-size:11px;color:#94a3b8;margin-left:8px">或 Ctrl+V 粘贴截图</span>
        <div v-if="bugPreviewUrls.length" class="preview-grid">
          <div v-for="(url, idx) in bugPreviewUrls" :key="idx" class="preview-item">
            <img :src="url" style="width:100%;height:80px;object-fit:cover;border-radius:6px" />
            <n-button size="tiny" type="error" ghost class="preview-remove" @click="removeBugImage(idx)">×</n-button>
          </div>
        </div>
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
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getTask, getTaskHistory, changeTaskStatus, updateTask, acceptTask } from '@/api/tasks'
import { createBug, uploadBugImage, getBugs } from '@/api/bugs'
import { getRequirement, downloadRequirementDocument } from '@/api/requirements'
import { getUsers } from '@/api/users'
import { getDictionaries } from '@/api/dictionaries'
import { priorityMeta, taskStatusMeta, bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import { NDrawer, NDrawerContent, NTag, NButton, NTimeline, NTimelineItem, NSelect, NSlider, NModal, NForm, NFormItem, NInput, NUpload, NSpace } from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const props = defineProps({ taskId: Number, readonly: Boolean, testerMode: { type: Boolean, default: false }, developerMode: { type: Boolean, default: false } })
const emit = defineEmits(['status-change', 'refresh'])

const authStore = useAuthStore()

const windowWidth = ref(window.innerWidth)
function onResize() { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const task = ref(null)
const histories = ref([])
const users = ref([])
const selectedDevLead = ref(null)
const actionLoading = ref(false)
const reqDoc = ref(null)
const requirementData = ref(null)
const taskBugs = ref([])
const skillsMap = ref({})
const reportProgress = ref(0)
const progressComment = ref('')
const submittingProgress = ref(false)
const taskNotFound = ref(false)

// 创建 Bug 弹窗
const showCreateBugModal = ref(false)
const bugForm = ref({ title: '', description: '', expected_result: '', severity: 'medium' })
const bugImageFiles = ref([])
const bugPreviewUrls = ref([])
const bugSubmitting = ref(false)

const severityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'critical' }
]

const statusMeta = computed(() => taskStatusMeta[task.value?.status] || { label: task.value?.status || '-', tone: 'default' })
const priorityMetaItem = computed(() => priorityMeta[task.value?.priority] || { label: task.value?.priority || '-', tone: 'default' })
const overdueDays = computed(() => {
  if (!task.value?.deadline || task.value?.status === 'closed') return 0
  const now = new Date()
  const end = new Date(task.value.deadline)
  const diff = Math.floor((now - end) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
})

const devLeadOptions = computed(() => {
  const devSkillKeys = Object.keys(skillsMap.value)
  return users.value.filter(u => {
    if (u.role === 'dev_lead') return true
    if (u.role === 'pm' && u.skills) {
      const userSkills = u.skills.split(',').filter(Boolean)
      if (userSkills.some(s => devSkillKeys.includes(s))) return true
    }
    return false
  }).map(u => ({ label: u.name, value: u.id }))
})

// 在测试工作台打开时，所有角色按测试人员权限处理
const effectiveRole = computed(() => {
  if (props.testerMode) return 'tester'
  if (props.developerMode && authStore.userInfo?.role === 'pm') return 'dev'
  return authStore.userInfo?.role
})

const canEditDevLead = computed(() => effectiveRole.value === 'pm' && ['pending', 'developing'].includes(task.value?.status))

const nextActionText = computed(() => ({
  pending: '等待分配开发人员进行开发。',
  developing: '开发中，完成后将进入测试。',
  pending_test: '开发已完成，等待测试人员受理并验证。',
  testing: '测试人员执行验证，通过后即可完成。',
  closed: '任务流程已完成。'
}[task.value?.status] || '查看当前状态并继续推进。'))

const availableActions = computed(() => {
  const status = task.value?.status
  const role = effectiveRole.value
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
  if (role === 'tester' && (status === 'pending_test' || (status === 'testing' && !task.value?.tester_id))) {
    actions.push({ label: '受理测试', status: null, type: 'primary', action: 'accept' })
  }
  if (role === 'tester' && status === 'testing' && task.value?.tester_id) {
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
  const role = effectiveRole.value
  const myId = authStore.userInfo?.id
  const isMyTask = task.value?.assignee_id === myId
  return status === 'developing' && (['dev', 'dev_lead'].includes(role) && isMyTask || role === 'pm')
})

watch([() => props.taskId, show], async ([id, visible]) => {
  if (id && visible) { await loadDetail(); await loadHistory(); await loadUsers(); await loadDictionaries() }
})

async function loadUsers() { try { users.value = await getUsers() } catch (e) { console.error(e) } }
async function loadDictionaries() {
  try {
    const data = await getDictionaries() || []
    const map = {}
    for (const d of data) {
      if (d.dict_type === 'skill') map[d.dict_key] = d.dict_value
    }
    skillsMap.value = map
  } catch (e) { console.error(e) }
}
async function loadDetail() {
  taskNotFound.value = false
  try {
    const res = await getTask(props.taskId)
    task.value = res.task
    if (task.value?.progress != null) reportProgress.value = task.value.progress
    if (task.value?.requirement_id) {
      const reqData = await getRequirement(task.value.requirement_id)
      requirementData.value = reqData
      reqDoc.value = reqData.document_name ? reqData : null
    }
    taskBugs.value = await getBugs({ task_id: props.taskId }) || []
  } catch (e) {
    taskNotFound.value = true
    task.value = null
    console.error(e)
  }
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

function handleCreateBug() {
  if (!task.value) return
  bugForm.value = {
    title: task.value.title,
    description: '',
    expected_result: '',
    severity: 'medium'
  }
  for (const url of bugPreviewUrls.value) URL.revokeObjectURL(url)
  bugImageFiles.value = []
  bugPreviewUrls.value = []
  showCreateBugModal.value = true
}

function handleBugUpload({ file }) {
  bugImageFiles.value.push(file.file)
  bugPreviewUrls.value.push(URL.createObjectURL(file.file))
  return { abort: () => {} }
}

function removeBugImage(idx) {
  URL.revokeObjectURL(bugPreviewUrls.value[idx])
  bugImageFiles.value.splice(idx, 1)
  bugPreviewUrls.value.splice(idx, 1)
}

function handleBugFormPaste(e) {
  const items = e.clipboardData?.items
  if (!items) return
  let hasImage = false
  for (const item of items) {
    if (item.type.startsWith('image/')) {
      hasImage = true
      const blob = item.getAsFile()
      if (blob) {
        const file = new File([blob], `paste-${Date.now()}.png`, { type: blob.type })
        bugImageFiles.value.push(file)
        bugPreviewUrls.value.push(URL.createObjectURL(file))
      }
    }
  }
  if (hasImage) e.preventDefault()
}

async function submitBug() {
  if (!bugForm.value.title.trim()) { window.$message?.warning('请输入标题'); return }
  if (!task.value) return
  bugSubmitting.value = true
  try {
    const payload = {
      title: bugForm.value.title,
      description: bugForm.value.description || undefined,
      expected_result: bugForm.value.expected_result || undefined,
      severity: bugForm.value.severity,
      task_id: props.taskId,
      assignee_id: task.value?.assignee_id
    }
    const created = await createBug(payload)
    for (const f of bugImageFiles.value) {
      await uploadBugImage(created.id, f)
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
  } else if (action.action === 'accept') {
    handleAcceptTask()
  } else {
    window.$dialog?.warning({
      title: '确认操作',
      content: `确定将任务「${task.value?.title || props.taskId}」状态变更为「${action.label}」吗？`,
      positiveText: '确定',
      negativeText: '取消',
      onPositiveClick: () => { doChangeStatus(action.status, '') },
      onNegativeClick: () => {},
      onClose: () => {}
    })
  }
}

async function handleAcceptTask() {
  actionLoading.value = true
  try {
    await acceptTask(props.taskId)
    window.$message?.success('任务受理成功')
    emit('refresh')
    await loadDetail()
    await loadHistory()
  } catch (e) { console.error(e) }
  actionLoading.value = false
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
.task-overview-row { display: flex; gap: 24px; margin-bottom: 14px; flex-wrap: wrap; }
.overview-item { display: flex; flex-direction: column; gap: 2px; }
.overview-label { font-size: 11px; color: #94a3b8; }
.overview-value { font-size: 14px; font-weight: 700; color: #0f172a; }
.deadline-row { display: flex; align-items: center; gap: 16px; margin-bottom: 14px; flex-wrap: wrap; }
.deadline-item { display: flex; align-items: center; gap: 6px; }
.deadline-label { font-size: 12px; color: #94a3b8; }
.deadline-value { font-size: 13px; font-weight: 600; color: #0f172a; }
.deadline-overdue { font-size: 13px; font-weight: 700; color: #d03050; }
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
.preview-grid { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
.preview-item { position: relative; width: 80px; height: 80px; }
.preview-item img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.preview-remove { position: absolute; top: -6px; right: -6px; min-width: 20px; height: 20px; padding: 0; font-size: 12px; border-radius: 50%; }
</style>
