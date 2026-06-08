<template>
  <AppLayout>
    <div class="tester-page">
      <section class="hero-card">
        <div class="hero-info">
          <div class="hero-avatar">{{ authStore.userInfo?.name?.charAt(0) || 'T' }}</div>
          <div>
            <div class="hero-name">{{ authStore.userInfo?.name || '测试' }}</div>
            <div class="hero-role">测试</div>
            <div class="hero-hint">查看待测试任务并验证 Bug</div>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat-pill"><span class="stat-num" style="color:#2563eb">{{ stats.totalTesting }}</span><span class="stat-label">待测试</span></div>
          <div class="stat-pill"><span class="stat-num" style="color:#d03050">{{ stats.pendingVerify }}</span><span class="stat-label">待验证 Bug</span></div>
        </div>
      </section>

      <section class="toolbar-row">
        <div style="flex:1"></div>
        <n-button type="primary" size="small" @click="openCreateBug">+ 创建Bug</n-button>
      </section>

      <div class="main-split">
        <div class="left-panel">
          <section class="section-card">
            <div class="section-header"><h3>待测试任务（{{ tasks.length }}）</h3></div>
            <div v-if="tasks.length === 0" class="empty-state">暂无待测试任务</div>
            <div v-for="t in tasks" :key="t.id" class="task-item" @click="onTaskClick(t.id)">
              <div class="task-item-top">
                <span class="task-item-title">{{ t.title }}</span>
                <span class="task-item-assignee">{{ t.assignee }}</span>
              </div>
            </div>
          </section>
        </div>

        <div class="right-panel">
          <section class="section-card">
            <div class="section-header"><h3>待验证 BUG（{{ pendingVerifyBugs.length }}）</h3></div>
            <div v-if="pendingVerifyBugs.length === 0" class="empty-state">暂无待验证 Bug</div>
            <div v-for="bug in pendingVerifyBugs" :key="bug.id" class="bug-item" @click="onBugClick(bug)">
              <div class="bug-item-top">
                <span class="bug-title">{{ bug.title }}</span>
                <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'" round>{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
              </div>
              <div v-if="bug.taskTitle" class="bug-item-meta">
                <span class="bug-task">关联: {{ bug.taskTitle }}</span>
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- Create Bug Modal -->
      <n-modal v-model:show="showCreateBug" preset="card" style="width:520px" title="创建 Bug" :mask-closable="false">
        <n-form label-placement="top">
          <n-form-item label="测试类型">
            <n-select v-model:value="bugForm.test_type" :options="testTypeOptions" />
          </n-form-item>
          <n-form-item label="关联需求">
            <n-select v-model:value="bugForm.requirement_id" :options="reqOptions" placeholder="（可选）请选择需求" clearable filterable />
          </n-form-item>
          <n-form-item label="标题" path="title">
            <n-input v-model:value="bugForm.title" placeholder="Bug标题" />
          </n-form-item>
          <n-form-item label="描述">
            <n-input v-model:value="bugForm.description" type="textarea" placeholder="描述 Bug 现象" />
          </n-form-item>
          <n-form-item label="严重程度">
            <n-select v-model:value="bugForm.severity" :options="severityOptions" />
          </n-form-item>
          <n-form-item label="修复人">
            <n-select v-model:value="bugForm.assignee_id" :options="devOptions" placeholder="（可选）指派给" clearable filterable />
          </n-form-item>
          <n-form-item label="截图">
            <n-upload :show-file-list="true" :custom-request="handleAttachUpload" accept="image/*" multiple
              :file-list="attachFiles" @remove="handleRemoveAttach">
              <n-button :loading="imageUploading">选择图片</n-button>
            </n-upload>
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showCreateBug = false">取消</n-button>
            <n-button type="primary" :loading="bugSubmitting" @click="submitBug">确定</n-button>
          </n-space>
        </template>
      </n-modal>
    </div>

    <TaskDetailDrawer v-model:show="showTaskDetail" :task-id="selectedTaskId" @refresh="loadData" />
    <BugDetailDrawer v-model:show="showBugDetail" :bug-id="selectedBugId" @refresh="loadData" />
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getTesterDashboard } from '@/api/statistics'
import { createBug, uploadBugImage } from '@/api/bugs'
import { getRequirements } from '@/api/requirements'
import { getUsers } from '@/api/users'
import { severityMeta } from '@/constants/statusMeta'
import TaskDetailDrawer from '@/components/TaskDetailDrawer.vue'
import BugDetailDrawer from '@/components/BugDetailDrawer.vue'
import AppLayout from '@/components/AppLayout.vue'
import { NTag, NButton, NModal, NForm, NFormItem, NInput, NSelect, NUpload, NSpace } from 'naive-ui'

const authStore = useAuthStore()

const dashData = ref({})
const showTaskDetail = ref(false)
const selectedTaskId = ref(null)
const showBugDetail = ref(false)
const selectedBugId = ref(null)

const requirements = ref([])
const users = ref([])

// Create Bug
const showCreateBug = ref(false)
const bugForm = ref({ test_type: 'integration', requirement_id: null, assignee_id: null, title: '', description: '', severity: 'medium' })
const bugSubmitting = ref(false)
const imageFiles = ref([])
const attachFiles = ref([])
const imageUploading = ref(false)

const testTypeOptions = [
  { label: '综合测试', value: 'integration' },
  { label: '业务测试', value: 'business' },
  { label: 'IT测试', value: 'it_test' }
]

const severityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'critical' }
]

const reqOptions = computed(() =>
  requirements.value.map(r => ({ label: `${r.number || r.requirement_id || 'REQ-'+r.id} ${r.description || ''}`.substring(0, 60), value: r.id }))
)
const devOptions = computed(() =>
  users.value.filter(u => ['dev', 'dev_lead'].includes(u.role)).map(u => ({ label: u.name, value: u.id }))
)

const stats = computed(() => dashData.value.stats || { totalTesting: 0, pendingVerify: 0 })
const tasks = computed(() => dashData.value.tasks || [])
const pendingVerifyBugs = computed(() => dashData.value.pendingVerifyBugs || [])

async function loadData() {
  try { dashData.value = await getTesterDashboard() } catch (e) { console.error(e) }
}

async function loadRequirements() {
  try {
    const all = await getRequirements() || []
    requirements.value = all.filter(r => r.status !== 'closed' && r.status !== 'released')
  } catch (e) { console.error(e) }
}

async function loadUsers() {
  try { users.value = await getUsers() || [] } catch (e) { console.error(e) }
}

function openCreateBug() {
  bugForm.value = { test_type: 'integration', requirement_id: null, assignee_id: null, title: '', description: '', severity: 'medium' }
  imageFiles.value = []
  attachFiles.value = []
  showCreateBug.value = true
}

function handleAttachUpload({ file, onFinish }) {
  imageFiles.value.push(file.file)
  attachFiles.value.push({ id: file.id, name: file.name })
  onFinish?.()
  return { abort: () => {} }
}

function handleRemoveAttach({ file }) {
  attachFiles.value = attachFiles.value.filter(f => f.id !== file.id)
}

async function submitBug() {
  if (!bugForm.value.title.trim()) { window.$message?.warning('请输入标题'); return }
  bugSubmitting.value = true
  try {
    const payload = {
      title: bugForm.value.title,
      description: bugForm.value.description || undefined,
      severity: bugForm.value.severity,
      test_type: bugForm.value.test_type,
      task_id: undefined,
      assignee_id: bugForm.value.assignee_id || undefined
    }
    if (bugForm.value.requirement_id) {
      payload.requirement_id = bugForm.value.requirement_id
    }
    const created = await createBug(payload)
    for (const f of imageFiles.value) {
      await uploadBugImage(created.id, f)
    }
    window.$message?.success('Bug 创建成功')
    showCreateBug.value = false
    await loadData()
  } catch (e) { console.error(e) }
  bugSubmitting.value = false
}

function onTaskClick(taskId) {
  selectedTaskId.value = taskId
  showTaskDetail.value = true
}

function onBugClick(bug) {
  selectedBugId.value = bug.id
  showBugDetail.value = true
}

onMounted(() => { loadData(); loadRequirements(); loadUsers() })
</script>

<style scoped>
.tester-page { display: flex; flex-direction: column; gap: 16px; }
.hero-card {
  display: flex; align-items: center; justify-content: space-between; gap: 20px;
  padding: 22px 24px; border-radius: 18px;
  background: linear-gradient(135deg, #fefce8 0%, #ffffff 55%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}
.hero-info { display: flex; align-items: center; gap: 14px; }
.hero-avatar { width:48px;height:48px;border-radius:50%;background:linear-gradient(135deg,#f59e0b,#fbbf24);color:white;display:flex;align-items:center;justify-content:center;font-size:20px;font-weight:700; }
.hero-name { font-size:18px;font-weight:700;color:#0f172a; }
.hero-role { font-size:12px;color:#f59e0b;font-weight:600;margin-top:2px; }
.hero-hint { font-size:12px;color:#94a3b8;margin-top:4px; }
.hero-stats { display:flex;gap:12px; }
.stat-pill { min-width:80px;padding:10px 14px;border-radius:14px;background:white;border:1px solid #e2e8f0;text-align:center; }
.stat-num { display:block;font-size:20px;font-weight:700; }
.stat-label { font-size:11px;color:#64748b;margin-top:2px; }
.toolbar-row { display:flex;align-items:center;padding:0 4px; }
.main-split { display: flex; gap: 16px; align-items: flex-start; flex-wrap: wrap; }
.left-panel { flex: 1; display: flex; flex-direction: column; gap: 16px; min-width: 300px; }
.right-panel { flex: 1; min-width: 300px; }
.section-card { background:white;border-radius:16px;border:1px solid #e2e8f0;padding:16px; }
.section-header { margin-bottom:12px; }
.section-header h3 { margin:0;font-size:15px;font-weight:700;color:#0f172a; }
.task-item { padding:10px 12px;border:1px solid #f1f5f9;border-radius:10px;margin-bottom:6px;cursor:pointer;transition:background .12s; }
.task-item:hover { background:#f1f5f9; }
.task-item-top { display:flex;align-items:center;justify-content:space-between;gap:8px; }
.task-item-title { font-size:13px;font-weight:500;color:#0f172a;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap; }
.task-item-assignee { font-size:11px;color:#94a3b8;white-space:nowrap; }
.bug-item { padding:10px 12px;border:1px solid #f1f5f9;border-radius:10px;cursor:pointer;transition:background .12s;margin-bottom:6px; }
.bug-item:hover { background:#f1f5f9; }
.bug-item-top { display:flex;align-items:center;justify-content:space-between;gap:8px; }
.bug-title { font-size:13px;font-weight:500;color:#0f172a;flex:1;overflow:hidden;text-overflow:ellipsis;white-space:nowrap; }
.bug-item-meta { margin-top:6px; }
.bug-task { font-size:11px;color:#94a3b8; }
.empty-state { text-align:center;padding:24px;color:#94a3b8;font-size:13px; }
</style>
