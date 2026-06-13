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
          <div class="stat-pill"><span class="stat-num" style="color:#f59e0b">{{ stats.unfixed }}</span><span class="stat-label">未修复 Bug</span></div>
        </div>
      </section>

      <section class="toolbar-row">
        <div style="flex:1"></div>
        <n-button type="primary" size="small" @click="openCreateBug">+ 创建Bug</n-button>
      </section>

      <div class="main-split">
        <div class="left-panel">
          <section class="section-card">
            <div class="section-header">
              <h3>待测试任务（{{ filteredTasks.length }}）</h3>
              <div class="filter-row">
                <span class="filter-label">端</span><n-select v-model:value="filterTerminal" :options="terminalOptions" placeholder="全部端" clearable style="width:120px" />
                <span class="filter-label">系统</span><n-select v-model:value="filterSystem" :options="systemOptions" placeholder="全部系统" clearable style="width:140px" />
                <span class="filter-label">需求</span><n-input v-model:value="filterRequirement" placeholder="需求编号" clearable style="width:140px" />
              </div>
            </div>
            <div v-if="filteredTasks.length === 0" class="empty-state">暂无待测试任务</div>
            <div class="task-list-scroll">
              <div v-for="t in filteredTasks" :key="t.id" class="task-item" @click="onTaskClick(t.id)">
                <div class="task-item-top">
                  <span class="task-item-title">{{ t.title }}</span>
                  <n-tag v-if="t.terminal" size="tiny" round>{{ skillsMap[t.terminal] || t.terminal }}</n-tag>
                </div>
                <div class="task-item-meta">
                  <span class="task-meta">{{ t.reqNumber || '-' }}</span>
                  <span class="task-meta">{{ t.system || '-' }}</span>
                  <span class="task-meta">{{ t.assignee || '-' }}</span>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div class="right-panel">
          <section class="section-card">
            <div class="section-header"><h3>未修复 BUG（{{ unfixedBugs.length }}）</h3></div>
            <div v-if="unfixedBugs.length === 0" class="empty-state">暂无未修复 Bug</div>
            <div class="task-list-scroll">
              <div v-for="bug in unfixedBugs" :key="bug.id" class="bug-item" @click="onBugClick(bug)">
                <div class="bug-item-top">
                  <span class="bug-title">{{ bug.title }}</span>
                  <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'" round>{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
                </div>
                <div v-if="bug.taskTitle" class="bug-item-meta">
                  <span class="bug-task">关联: {{ bug.taskTitle }}</span>
                </div>
              </div>
            </div>
          </section>
          <section class="section-card">
            <div class="section-header"><h3>待验证 BUG（{{ pendingVerifyBugs.length }}）</h3></div>
            <div v-if="pendingVerifyBugs.length === 0" class="empty-state">暂无待验证 Bug</div>
            <div class="task-list-scroll">
              <div v-for="bug in pendingVerifyBugs" :key="bug.id" class="bug-item" @click="onBugClick(bug)">
                <div class="bug-item-top">
                  <span class="bug-title">{{ bug.title }}</span>
                  <n-tag size="tiny" :type="severityMeta[bug.severity]?.tone || 'default'" round>{{ severityMeta[bug.severity]?.label || bug.severity }}</n-tag>
                </div>
                <div v-if="bug.taskTitle" class="bug-item-meta">
                  <span class="bug-task">关联: {{ bug.taskTitle }}</span>
                </div>
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
          <n-form-item label="实际结果">
            <n-input v-model:value="bugForm.description" type="textarea" placeholder="描述实际出现的现象" />
          </n-form-item>
          <n-form-item label="预期结果">
            <n-input v-model:value="bugForm.expected_result" type="textarea" placeholder="描述预期的正常行为" />
          </n-form-item>
          <n-form-item label="严重程度">
            <n-select v-model:value="bugForm.severity" :options="severityOptions" />
          </n-form-item>
          <n-form-item label="修复人">
            <n-select v-model:value="bugForm.assignee_id" :options="devOptions" placeholder="（可选）指派给" clearable filterable />
          </n-form-item>
          <n-form-item label="截图">
            <n-upload :show-file-list="false" :custom-request="handleAttachUpload" accept="image/*" multiple>
              <n-button :loading="imageUploading">选择图片</n-button>
            </n-upload>
            <div v-if="previewUrls.length" class="preview-grid">
              <div v-for="(url, idx) in previewUrls" :key="idx" class="preview-item">
                <img :src="url" style="width:100%;height:80px;object-fit:cover;border-radius:6px" />
                <n-button size="tiny" type="error" ghost class="preview-remove" @click="removeImage(idx)">×</n-button>
              </div>
            </div>
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
import { getDictionaries } from '@/api/dictionaries'
import { getSystems } from '@/api/systems'
import { getUsers } from '@/api/users'
import { severityMeta } from '@/constants/statusMeta'
import TaskDetailDrawer from '@/components/TaskDetailDrawer.vue'
import BugDetailDrawer from '@/components/BugDetailDrawer.vue'
import AppLayout from '@/components/AppLayout.vue'
import { NTag, NButton, NModal, NForm, NFormItem, NInput, NSelect, NUpload, NSpace } from 'naive-ui'

const authStore = useAuthStore()

const dashData = ref({})
const skillsMap = ref({})
const systems = ref([])
const showTaskDetail = ref(false)
const selectedTaskId = ref(null)
const showBugDetail = ref(false)
const selectedBugId = ref(null)

const requirements = ref([])
const users = ref([])

// Create Bug
const showCreateBug = ref(false)
const bugForm = ref({ test_type: 'integration', requirement_id: null, assignee_id: null, title: '', description: '', expected_result: '', severity: 'medium' })
const bugSubmitting = ref(false)
const imageFiles = ref([])
const attachFileNames = ref([])
const previewUrls = ref([])
const imageUploading = ref(false)

// Filters
const filterTerminal = ref('')
const filterRequirement = ref('')
const filterSystem = ref('')

const terminalOptions = computed(() => {
  const set = new Set(tasks.value.map(t => t.terminal).filter(Boolean))
  return [...set].map(v => ({ label: skillsMap.value[v] || v, value: v }))
})

const systemOptions = computed(() =>
  systems.value.map(s => ({ label: s.name, value: s.name }))
)

const filteredTasks = computed(() => {
  return tasks.value.filter(t => {
    if (filterTerminal.value && t.terminal !== filterTerminal.value) return false
    if (filterRequirement.value && !(t.reqNumber || '').includes(filterRequirement.value)) return false
    if (filterSystem.value && !(t.system || '').includes(filterSystem.value)) return false
    return true
  })
})

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

const stats = computed(() => dashData.value.stats || { totalTesting: 0, pendingVerify: 0, unfixed: 0 })
const tasks = computed(() => dashData.value.tasks || [])
const pendingVerifyBugs = computed(() => dashData.value.pendingVerifyBugs || [])
const unfixedBugs = computed(() => dashData.value.unfixedBugs || [])

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

function handleAttachUpload({ file, onFinish }) {
  if (file.file && imageFiles.value.some(f => f === file.file || f.name === file.file.name && f.size === file.file.size)) {
    onFinish?.()
    return { abort: () => {} }
  }
  imageFiles.value.push(file.file)
  attachFileNames.value.push(file.name)
  previewUrls.value.push(URL.createObjectURL(file.file))
  onFinish?.()
  return { abort: () => {} }
}

function removeImage(idx) {
  URL.revokeObjectURL(previewUrls.value[idx])
  imageFiles.value.splice(idx, 1)
  attachFileNames.value.splice(idx, 1)
  previewUrls.value.splice(idx, 1)
}

function openCreateBug() {
  bugForm.value = { test_type: 'integration', requirement_id: null, assignee_id: null, title: '', description: '', expected_result: '', severity: 'medium' }
  for (const url of previewUrls.value) URL.revokeObjectURL(url)
  imageFiles.value = []
  attachFileNames.value = []
  previewUrls.value = []
  showCreateBug.value = true
}

async function submitBug() {
  if (!bugForm.value.title.trim()) { window.$message?.warning('请输入标题'); return }
  bugSubmitting.value = true
  try {
    const payload = {
      title: bugForm.value.title,
      description: bugForm.value.description || undefined,
      expected_result: bugForm.value.expected_result || undefined,
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

async function loadSystems() {
  try { systems.value = await getSystems() || [] } catch (e) { console.error(e) }
}

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

function onTaskClick(taskId) {
  selectedTaskId.value = taskId
  showTaskDetail.value = true
}

function onBugClick(bug) {
  selectedBugId.value = bug.id
  showBugDetail.value = true
}

onMounted(() => { loadData(); loadRequirements(); loadUsers(); loadSystems(); loadDictionaries() })
</script>

<style scoped>
.tester-page { display: flex; flex-direction: column; gap: 16px; flex: 1; min-height: 0; overflow: hidden; }
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
.main-split { display: flex; gap: 16px; align-items: flex-start; flex-wrap: wrap; flex: 1; min-height: 0; }
.left-panel { flex: 1; display: flex; flex-direction: column; gap: 16px; min-width: 300px; min-height: 0; }
.right-panel { flex: 1; min-width: 300px; min-height: 0; }
.section-card { background:white;border-radius:16px;border:1px solid #e2e8f0;padding:16px; }
.section-header { margin-bottom:12px; display: flex; justify-content: space-between; align-items: center; gap: 12px; flex-wrap: wrap; }
.section-header h3 { margin:0;font-size:15px;font-weight:700;color:#0f172a; }
.task-list-scroll { max-height: calc(100vh - 340px); overflow-y: auto; }
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
.preview-grid { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 10px; }
.preview-item { position: relative; width: 80px; height: 80px; }
.preview-item img { width: 100%; height: 100%; object-fit: cover; border-radius: 6px; }
.preview-remove { position: absolute; top: -6px; right: -6px; min-width: 20px; height: 20px; padding: 0; font-size: 12px; border-radius: 50%; }
.filter-row { display: flex; gap: 8px; align-items: center; margin-left: auto; flex-wrap: wrap; }
.filter-label { font-size: 12px; color: #64748b; white-space: nowrap; }
.filter-row .n-select, .filter-row .n-input { flex: 1; min-width: 100px; }
.task-item-meta { display: flex; gap: 12px; margin-top: 4px; font-size: 11px; color: #94a3b8; }
.task-meta { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
@media (max-width: 768px) {
  .hero-card { flex-direction: column; align-items: flex-start; }
  .section-header { flex-direction: column; align-items: flex-start; }
  .filter-row { margin-left: 0; width: 100%; }
  .filter-row .n-select, .filter-row .n-input { width: auto !important; }
}
@media (max-width: 1024px) {
  .left-panel, .right-panel { width: 100%; flex: none; }
}
</style>
