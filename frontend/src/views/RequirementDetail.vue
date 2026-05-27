<template>
  <AppLayout>
    <div class="detail-page">
      <!-- Back button -->
      <div class="back-row">
        <n-button text @click="$router.push('/requirements')">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px">
              <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
            </svg>
          </template>
          返回需求列表
        </n-button>
      </div>

      <!-- Header -->
      <section class="header-card">
        <div class="header-top">
          <div class="header-number">{{ req.number || `REQ-${String(req.id || '').padStart(4, '0')}` }}</div>
          <div class="header-tags">
            <n-select v-if="editingField === 'status'" v-model:value="req.status" :options="statusOptions" size="small"
              @blur="saveField('status'); editingField = null"
              @keyup.enter="saveField('status'); editingField = null" autofocus @click.stop style="width:130px" />
            <n-tag v-else :type="statusMeta?.tone || 'default'" size="small" round style="cursor:pointer" @click="startEdit('status')">{{ statusMeta?.label || req.status }}</n-tag>
            <n-tag :type="priorityMeta[req.priority]?.tone || 'default'" size="small" round>{{ priorityMeta[req.priority]?.label || req.priority }}</n-tag>
            <n-tag v-if="req.project?.name" size="small" round>{{ req.project.name }}</n-tag>
          </div>
        </div>
        <div class="header-actions">
          <n-button v-if="authStore.isPM && !req.iteration_id" size="small" type="primary" ghost @click="showIterationSelector = true">纳入发布清单</n-button>
          <n-button v-if="authStore.isPM && req.iteration_id" size="small" type="error" ghost @click="handleRemoveFromRelease">从发布库移除</n-button>
        </div>
      </section>

      <!-- Description (read-only) -->
      <section class="section-card">
        <div class="section-header"><h3>需求描述</h3></div>
        <div class="description-text">{{ req.description || '暂无描述' }}</div>
      </section>

      <!-- Basic Info -->
      <section class="section-card">
        <div class="section-header"><h3>基本信息</h3></div>
        <div class="info-grid">
          <div class="info-cell"><span class="info-label">业务负责人</span><span class="info-value">{{ req.person_name || req.person?.name || '-' }}</span></div>

          <div class="info-cell">
            <span class="info-label">项目类型</span>
            <span class="info-value">{{ req.project_type === 'project' ? '项目需求' : req.project_type === 'ops' ? '运维需求' : '-' }}</span>
          </div>
          <div class="info-cell">
            <span class="info-label">所属系统</span>
            <span class="info-value">{{ req.system || '-' }}</span>
          </div>
          <div class="info-cell editable-cell" @click="startEdit('dev_lead_id')">
            <span class="info-label">开发组长</span>
            <template v-if="editingField === 'dev_lead_id'">
              <div class="inline-edit-row" @click.stop>
                <n-select v-model:value="req.dev_lead_id" :options="devLeadOptions" size="small" style="width:140px" filterable
                  @keyup.enter="saveAssignDevLead(); editingField = null" autofocus />
                <n-button size="tiny" type="primary" @click="saveAssignDevLead(); editingField = null">确定</n-button>
                <n-button size="tiny" type="error" ghost @click="editingField = null">取消</n-button>
              </div>
            </template>
            <span v-else class="info-value edit-hint">{{ req.dev_lead?.name || '点击指派' }}</span>
          </div>
          <div class="info-cell editable-cell" @click="startEdit('priority')">
            <span class="info-label">优先级</span>
            <template v-if="editingField === 'priority'">
              <n-select v-model:value="req.priority" :options="priorityOptions" size="small"
                @blur="saveField('priority'); editingField = null"
                @keyup.enter="saveField('priority'); editingField = null" autofocus @click.stop />
            </template>
            <span v-else class="info-value edit-hint">{{ priorityMeta[req.priority]?.label || req.priority || '点击选择' }}</span>
          </div>
          <div class="info-cell editable-cell" @click="startEdit('iteration_id')">
            <span class="info-label">发布迭代</span>
            <template v-if="editingField === 'iteration_id'">
              <n-select v-model:value="req.iteration_id" :options="iterationOptions" size="small" clearable
                @blur="editingField = null"
                @update:value="onIterationChange" autofocus @click.stop />
            </template>
            <span v-else class="info-value edit-hint">{{ req.iteration_name || (req.iteration_id ? `迭代 #${req.iteration_id}` : '未纳入') }}</span>
          </div>
          <div class="info-cell">
            <span class="info-label">计划完成时间</span>
            <span class="info-value">{{ req.planned_completion_time ? formatDate2(req.planned_completion_time) : '-' }}</span>
          </div>
          <div class="info-cell"><span class="info-label">开发人天</span><span class="info-value">{{ req.dev_total || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">开发单价</span><span class="info-value">{{ req.dev_price || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">测试人天</span><span class="info-value">{{ req.test_total || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">测试单价</span><span class="info-value">{{ req.test_price || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">总人天</span><span class="info-value">{{ req.total_amount || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">总价</span><span class="info-value">{{ req.total_price || '-' }}</span></div>
        </div>
      </section>

      <!-- Notes -->
      <section class="section-card">
        <div class="section-header"><h3>备注信息</h3></div>
        <n-input v-model:value="localNotes" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" placeholder="添加备注..." @blur="saveNotes" />
      </section>

      <!-- Document -->
      <section class="section-card">
        <div class="section-header"><h3>需求文档</h3></div>
        <div v-if="req.document_name" class="document-row">
          <div class="document-info">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;flex-shrink:0">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>
            </svg>
            <span class="document-name">{{ req.document_name }}</span>
            <span v-if="req.document_size" class="document-size">({{ formatSize(req.document_size) }})</span>
          </div>
          <n-space>
            <n-button size="small" @click="handleDownloadDocument">下载</n-button>
            <n-button v-if="authStore.isPM" size="small" type="error" ghost @click="handleDeleteDocument">删除</n-button>
          </n-space>
        </div>
        <div v-else-if="authStore.isPM" class="document-upload">
          <n-upload :show-file-list="false" :custom-request="handleCustomUpload" accept="*/*">
            <n-button size="small" :loading="documentLoading">上传文档</n-button>
          </n-upload>
        </div>
        <div v-else class="empty-state">暂无文档</div>
      </section>

      <!-- Features -->
      <section v-if="authStore.isDevLead" class="section-card">
        <div class="section-header">
          <h3>功能点 ({{ features.length }})</h3>
          <n-button size="tiny" text @click="showCreateFeature = true">新增</n-button>
        </div>
        <div v-if="features.length > 0" class="feature-list">
          <div v-for="f in features" :key="f.id" class="feature-item">
            <div class="feature-header">
              <span class="feature-title">{{ f.title }}</span>
              <n-tag :type="featureStatusTag(f.status)" size="tiny" round>{{ featureStatusLabel[f.status] || f.status }}</n-tag>
              <n-button text size="tiny" type="error" @click="handleDeleteFeature(f)">删除</n-button>
            </div>
            <div class="feature-meta-row">
              <span class="feature-meta-label">开发：</span>
              <template v-if="editingFeatureDev === f.id">
                <n-select v-model:value="f.developer_id" :options="userOptions" size="small" style="width:140px" filterable
                  @blur="saveFeatureDev(f); editingFeatureDev = null"
                  @keyup.enter="saveFeatureDev(f); editingFeatureDev = null" autofocus />
              </template>
              <span v-else class="feature-meta-value" @click="startFeatureEdit('dev', f)">{{ f.developer?.name || '点击设置' }}</span>
              <span class="feature-meta-label" style="margin-left:12px">测试：</span>
              <template v-if="editingFeatureTester === f.id">
                <n-select v-model:value="f.tester_id" :options="userOptions" size="small" style="width:140px" filterable
                  @blur="saveFeatureTester(f); editingFeatureTester = null"
                  @keyup.enter="saveFeatureTester(f); editingFeatureTester = null" autofocus />
              </template>
              <span v-else class="feature-meta-value" @click="startFeatureEdit('tester', f)">{{ f.tester?.name || '点击设置' }}</span>
            </div>
            <div v-if="f.assignments?.length" class="assignment-list">
              <div v-for="a in f.assignments" :key="a.id" class="assignment-item">
                <span class="assignment-terminal">{{ a.terminal }}</span>
                <span class="assignment-dev">{{ a.developer?.name }}</span>
                <n-tag :type="a.status === 'done' ? 'success' : a.status === 'developing' ? 'warning' : 'default'" size="tiny" round>
                  {{ a.status === 'done' ? '已完成' : a.status === 'developing' ? '开发中' : '待开始' }}
                </n-tag>
                <n-button text size="tiny" type="error" @click="handleDeleteAssignment(a)">移除</n-button>
              </div>
            </div>
            <div class="feature-actions">
              <n-button text size="tiny" @click="openAssign(f)">分配开发</n-button>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无功能点</div>
      </section>

      <!-- Dev Progress -->
      <section class="section-card">
        <div class="section-header"><h3>开发进度</h3></div>
        <div v-if="terminalDevProgress.length > 0" class="progress-list">
          <div v-for="tp in terminalDevProgress" :key="tp.terminal" class="progress-item" @click="toggleTerminalTasks(tp.terminal)">
            <div class="progress-item-header">
              <div class="progress-item-left">
                <span class="progress-terminal">{{ tp.terminal }}</span>
                <n-tag v-if="tp.overdue_days > 0" type="error" size="tiny" round>逾期 {{ tp.overdue_days }} 天</n-tag>
              </div>
              <span class="progress-percent">{{ tp.progress }}%</span>
            </div>
            <n-progress type="line" :percentage="tp.progress" :height="10" :border-radius="5"
              :color="tp.progress >= 100 ? '#18a058' : '#2080f0'" indicator-placement="inside" />
            <div v-if="expandedTerminal === tp.terminal && tp.tasks?.length" class="terminal-task-list">
              <div v-for="task in tp.tasks" :key="task.id" class="terminal-task-item">
                <span class="terminal-task-name">{{ task.title }}</span>
                <n-tag :type="task.status === 'done' ? 'success' : 'warning'" size="tiny" round>{{ task.status === 'done' ? '已完成' : '进行中' }}</n-tag>
              </div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无开发进度数据</div>
      </section>

      <!-- Integration Test Progress -->
      <section class="section-card">
        <div class="section-header"><h3>综合测试进度</h3></div>
        <div class="bug-stat-grid" @click="showIntegrationBugs = !showIntegrationBugs">
          <div class="bug-stat-item"><span class="bug-stat-value">{{ integrationBugStats.total }}</span><span class="bug-stat-label">Bug 总数</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#18a058">{{ integrationBugStats.closed }}</span><span class="bug-stat-label">已关闭</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#f59e0b">{{ integrationBugStats.fixing }}</span><span class="bug-stat-label">修复中</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#d03050">{{ integrationBugStats.pending }}</span><span class="bug-stat-label">待验证</span></div>
        </div>
        <div v-if="showIntegrationBugs && integrationBugs.length" class="bug-list">
          <div v-for="bug in integrationBugs" :key="bug.id" class="bug-item">
            <span class="bug-name">{{ bug.title }}</span>
            <n-tag :type="bug.status === 'closed' ? 'success' : 'warning'" size="tiny" round>{{ bug.status }}</n-tag>
          </div>
        </div>
      </section>

      <!-- Business Test Progress -->
      <section class="section-card">
        <div class="section-header"><h3>业务测试进度</h3></div>
        <div class="bug-stat-grid" @click="showBusinessBugs = !showBusinessBugs">
          <div class="bug-stat-item"><span class="bug-stat-value">{{ businessBugStats.total }}</span><span class="bug-stat-label">Bug 总数</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#18a058">{{ businessBugStats.closed }}</span><span class="bug-stat-label">已关闭</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#f59e0b">{{ businessBugStats.fixing }}</span><span class="bug-stat-label">修复中</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#d03050">{{ businessBugStats.pending }}</span><span class="bug-stat-label">待验证</span></div>
        </div>
        <div v-if="showBusinessBugs && businessBugs.length" class="bug-list">
          <div v-for="bug in businessBugs" :key="bug.id" class="bug-item">
            <span class="bug-name">{{ bug.title }}</span>
            <n-tag :type="bug.status === 'closed' ? 'success' : 'warning'" size="tiny" round>{{ bug.status }}</n-tag>
          </div>
        </div>
      </section>
    </div>

    <!-- Create Feature Modal -->
    <n-modal v-model:show="showCreateFeature" preset="card" style="width: 480px" title="新增功能点" :mask-closable="false">
      <n-form :model="createFeatureForm" label-placement="top">
        <n-form-item label="功能点名称" path="title" :rule="{ required: true, message: '请输入功能点名称' }">
          <n-input v-model:value="createFeatureForm.title" placeholder="输入功能点名称" />
        </n-form-item>
        <n-form-item label="描述">
          <n-input v-model:value="createFeatureForm.description" type="textarea" placeholder="功能点描述" />
        </n-form-item>
        <n-form-item label="开发负责人">
          <n-select v-model:value="createFeatureForm.developer_id" :options="userOptions" placeholder="选择开发负责人" clearable filterable />
        </n-form-item>
        <n-form-item label="测试负责人">
          <n-select v-model:value="createFeatureForm.tester_id" :options="userOptions" placeholder="选择测试负责人" clearable filterable />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreateFeature = false">取消</n-button>
          <n-button type="primary" :loading="creatingFeature" :disabled="!createFeatureForm.title.trim()" @click="handleCreateFeature">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Assign Developer to Feature Modal -->
    <n-modal v-model:show="showAssignModal" preset="card" style="width: 420px" title="分配开发人员" :mask-closable="false">
      <n-form :model="assignForm" label-placement="top">
        <n-form-item label="功能点">
          <n-input :value="assigningFeature?.title" disabled />
        </n-form-item>
        <n-form-item label="终端" path="terminal" :rule="{ required: true, message: '请选择终端' }">
          <n-select v-model:value="assignForm.terminal" :options="platformOptions" placeholder="选择终端" />
        </n-form-item>
        <n-form-item label="开发人员" path="developer_id" :rule="{ required: true, message: '请选择开发人员' }">
          <n-select v-model:value="assignForm.developer_id" :options="devOptions" placeholder="选择开发人员" filterable />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showAssignModal = false">取消</n-button>
          <n-button type="primary" :loading="assignLoading" :disabled="!assignForm.terminal || !assignForm.developer_id" @click="handleCreateAssignment">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Iteration Selector Modal -->
    <n-modal v-model:show="showIterationSelector" preset="card" style="width: 400px" title="选择发布迭代">
      <n-select
        v-model:value="selectedIterationId"
        :options="iterationOptions"
        placeholder="选择迭代"
        filterable
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showIterationSelector = false">取消</n-button>
          <n-button type="primary" :loading="releasing" @click="handleAddToRelease">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { getUsers } from '@/api/users'
import {
  getRequirement,
  updateRequirement,
  addToRelease,
  removeFromRelease,
  uploadRequirementDocument,
  downloadRequirementDocument,
  deleteRequirementDocument,
  getFeatures,
  createFeature,
  updateFeature,
  deleteFeature,
  assignDevLead,
  getFeatureAssignments,
  createFeatureAssignment,
  deleteFeatureAssignment
} from '@/api/requirements'
import { getIterations } from '@/api/iterations'
import { requirementStatusMeta } from '@/constants/requirementMeta'
import { priorityMeta } from '@/constants/statusMeta'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NInput, NSelect, NSpace, NTag, NProgress, NUpload, NForm, NFormItem } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const req = ref({})
const users = ref([])
const iterations = ref([])
const editingField = ref(null)
const localDescription = ref('')
const localNotes = ref('')

const expandedTerminal = ref(null)
const features = ref([])
const editingFeatureDev = ref(null)
const editingFeatureTester = ref(null)
const showCreateFeature = ref(false)
const creatingFeature = ref(false)
const createFeatureForm = ref({ title: '', description: '', developer_id: null, tester_id: null })
const showAssignModal = ref(false)
const assignLoading = ref(false)
const assigningFeature = ref(null)
const assignForm = ref({ terminal: null, developer_id: null })

const devLeadOptions = computed(() => users.value.filter(u => u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id })))
const devOptions = computed(() => users.value.filter(u => u.role === 'dev').map(u => ({ label: u.name, value: u.id })))
const platformOptions = [
  { label: 'iOS', value: 'iOS' },
  { label: 'Android', value: 'Android' },
  { label: '鸿蒙', value: '鸿蒙' },
  { label: '小程序', value: '小程序' },
  { label: 'H5', value: 'H5' },
  { label: '后台', value: '后台' },
  { label: '前端', value: '前端' },
  { label: '其他', value: '其他' }
]
const showIntegrationBugs = ref(false)
const showBusinessBugs = ref(false)
const showIterationSelector = ref(false)
const selectedIterationId = ref(null)
const releasing = ref(false)
const documentLoading = ref(false)

const statusMeta = computed(() => requirementStatusMeta[req.value.status] || null)

const userOptions = computed(() =>
  users.value.map((u) => ({ label: u.name, value: u.id }))
)

const priorityOptions = Object.entries(priorityMeta).map(([value, meta]) => ({
  label: meta.label,
  value
}))
const statusOptions = Object.entries(requirementStatusMeta).map(([value, meta]) => ({
  label: meta.label,
  value
}))

const iterationOptions = computed(() => {
  return iterations.value.map((it) => ({ label: it.name, value: String(it.id) }))
})

const terminalDevProgress = computed(() => {
  if (!req.value.dev_progress?.terminals) return []
  return req.value.dev_progress.terminals.map((tp) => ({
    ...tp,
    progress: tp.progress || 0
  }))
})

const integrationBugStats = computed(() => {
  const bugs = req.value.integration_test_bugs || []
  return {
    total: bugs.length,
    closed: bugs.filter((b) => b.status === 'closed').length,
    fixing: bugs.filter((b) => b.status === 'fixing' || b.status === 'assigned').length,
    pending: bugs.filter((b) => b.status === 'pending_verify').length
  }
})

const integrationBugs = computed(() => req.value.integration_test_bugs || [])

const businessBugStats = computed(() => {
  const bugs = req.value.business_test_bugs || []
  return {
    total: bugs.length,
    closed: bugs.filter((b) => b.status === 'closed').length,
    fixing: bugs.filter((b) => b.status === 'fixing' || b.status === 'assigned').length,
    pending: bugs.filter((b) => b.status === 'pending_verify').length
  }
})

const businessBugs = computed(() => req.value.business_test_bugs || [])

function startEdit(field) {
  if (!authStore.isPM) return
  editingField.value = field
}

async function saveField(field) {
  try {
    await updateRequirement(req.value.id, { [field]: req.value[field] })
    window.$message?.success('更新成功')
  } catch (e) {
    window.$message?.error('更新失败')
  }
}

async function saveDescription() {
  if (localDescription.value === req.value.description) return
  try {
    await updateRequirement(req.value.id, { description: localDescription.value })
    req.value.description = localDescription.value
  } catch (e) {
    window.$message?.error('保存描述失败')
  }
}

async function saveNotes() {
  if (localNotes.value === req.value.notes) return
  try {
    await updateRequirement(req.value.id, { notes: localNotes.value })
    req.value.notes = localNotes.value
  } catch (e) {
    window.$message?.error('保存备注失败')
  }
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDate2(d) {
  if (!d) return ''
  const date = new Date(d)
  if (isNaN(date.getTime())) return d
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

async function handleCustomUpload({ file, onFinish, onError }) {
  documentLoading.value = true
  try {
    await uploadRequirementDocument(req.value.id, file.file)
    window.$message?.success('上传成功')
    await loadReq()
    onFinish()
  } catch (e) {
    window.$message?.error('上传失败')
    onError()
  }
  documentLoading.value = false
}

async function handleDownloadDocument() {
  try {
    const res = await downloadRequirementDocument(req.value.id)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = req.value.document_name || 'document'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    window.$message?.error('下载失败')
  }
}

async function handleDeleteDocument() {
  try {
    await deleteRequirementDocument(req.value.id)
    window.$message?.success('删除成功')
    req.value.document_name = null
    req.value.document_path = null
    req.value.document_size = null
  } catch (e) {
    window.$message?.error('删除失败')
  }
}

function toggleTerminalTasks(terminal) {
  expandedTerminal.value = expandedTerminal.value === terminal ? null : terminal
}

async function onIterationChange(val) {
  req.value.iteration_id = val
  editingField.value = null
  await saveField('iteration_id')
}

async function handleAddToRelease() {
  if (!selectedIterationId.value) {
    window.$message?.warning('请选择迭代')
    return
  }
  releasing.value = true
  try {
    await addToRelease(req.value.id, selectedIterationId.value)
    req.value.iteration_id = selectedIterationId.value
    window.$message?.success('已纳入发布清单')
    showIterationSelector.value = false
  } catch (e) {
    window.$message?.error('操作失败')
  }
  releasing.value = false
}

async function handleRemoveFromRelease() {
  try {
    await removeFromRelease(req.value.id)
    req.value.iteration_id = null
    window.$message?.success('已从发布库移除')
  } catch (e) {
    window.$message?.error('操作失败')
  }
}

async function loadUsers() {
  try { users.value = await getUsers() } catch (e) {}
}

async function loadIterations() {
  try {
    const data = await getIterations()
    iterations.value = data || []
  } catch (e) {}
}

async function saveAssignDevLead() {
  if (!req.value.dev_lead_id) return
  try {
    await assignDevLead(req.value.id, req.value.dev_lead_id)
    window.$message?.success('开发组长已指派')
    await loadReq()
  } catch (e) {
    window.$message?.error('指派失败')
  }
}

async function loadAssignments(f) {
  try {
    f.assignments = await getFeatureAssignments(f.id)
  } catch (e) { f.assignments = [] }
}

async function loadFeatures() {
  const id = route.params.id
  if (!id) return
  try {
    const data = await getFeatures(id)
    for (const f of data) {
      await loadAssignments(f)
    }
    features.value = data
  } catch (e) { console.error(e) }
}

function openAssign(f) {
  assigningFeature.value = f
  assignForm.value = { terminal: null, developer_id: null }
  showAssignModal.value = true
}

async function handleCreateAssignment() {
  if (!assignForm.value.terminal || !assignForm.value.developer_id) return
  assignLoading.value = true
  try {
    await createFeatureAssignment(assigningFeature.value.id, {
      terminal: assignForm.value.terminal,
      developer_id: assignForm.value.developer_id
    })
    window.$message?.success('分配成功，任务已自动创建')
    showAssignModal.value = false
    await loadFeatures()
  } catch (e) {
    window.$message?.error('分配失败')
  }
  assignLoading.value = false
}

function startFeatureEdit(type, f) {
  if (type === 'dev') editingFeatureDev.value = f.id
  else editingFeatureTester.value = f.id
}

async function saveFeatureDev(f) {
  if (!f.developer_id) return
  try {
    await updateFeature(f.id, { developer_id: f.developer_id })
    window.$message?.success('开发负责人已更新')
    await loadFeatures()
  } catch (e) { window.$message?.error('更新失败') }
}

async function saveFeatureTester(f) {
  if (!f.tester_id) return
  try {
    await updateFeature(f.id, { tester_id: f.tester_id })
    window.$message?.success('测试负责人已更新')
    await loadFeatures()
  } catch (e) { window.$message?.error('更新失败') }
}

async function handleDeleteAssignment(a) {
  try {
    await deleteFeatureAssignment(a.id)
    window.$message?.success('已移除分配')
    await loadFeatures()
  } catch (e) {
    window.$message?.error('移除失败')
  }
}

async function loadReq() {
  const id = route.params.id
  if (!id) return
  try {
    const data = await getRequirement(id)
    req.value = data
    localDescription.value = data.description || ''
    localNotes.value = data.notes || ''
    await loadFeatures()
  } catch (e) {
    window.$message?.error('加载需求详情失败')
  }
}

const featureStatusLabel = {
  planned: '已计划', locked: '已锁定', developing: '开发中',
  developed: '已开发', pending_test: '待测试', closed: '已关闭',
  pending_dev: '待开发'
}

function featureStatusTag(status) {
  const map = { planned: 'default', locked: 'info', developing: 'warning', developed: 'success', pending_dev: 'info' }
  return map[status] || 'default'
}

async function handleCreateFeature() {
  if (!createFeatureForm.value.title.trim()) {
    window.$message?.warning('请输入功能点名称')
    return
  }
  creatingFeature.value = true
  try {
    await createFeature(route.params.id, createFeatureForm.value)
    window.$message?.success('功能点已创建')
    showCreateFeature.value = false
    createFeatureForm.value = { title: '', description: '', developer_id: null, tester_id: null }
    await loadFeatures()
  } catch (e) {
    window.$message?.error('创建失败')
  }
  creatingFeature.value = false
}

async function handleDeleteFeature(f) {
  try {
    await deleteFeature(f.id)
    window.$message?.success('功能点已删除')
    await loadFeatures()
  } catch (e) {
    window.$message?.error('删除失败')
  }
}

watch(
  () => route.params.id,
  (newId) => {
    if (newId) loadReq()
  }
)

onMounted(() => {
  loadReq()
  loadUsers()
  loadIterations()
})
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.back-row {
  display: flex;
  align-items: center;
}

.back-row .n-button {
  color: #64748b;
  font-weight: 500;
}

/* Header Card */
.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.header-top {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-number {
  font-size: 20px;
  font-weight: 700;
  color: #6366f1;
  letter-spacing: 0.02em;
}

.header-tags {
  display: flex;
  gap: 8px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* Description text (read-only) */
.description-text {
  font-size: 14px;
  line-height: 1.8;
  color: #334155;
  white-space: pre-wrap;
}

/* Info grid */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 10px;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 14px;
  background: #f8fafc;
  border-radius: 10px;
}

.info-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}

.info-value {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

.editable-cell {
  cursor: pointer;
  transition: background 0.15s;
}

.editable-cell:hover {
  background: #eef2ff;
}

.edit-hint {
  color: #6366f1 !important;
}

.inline-edit-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* Section Card */
.section-card {
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  padding: 22px;
}

.section-header {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

/* Progress List */
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-item {
  cursor: pointer;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition: background 0.15s;
}

.progress-item:hover {
  background: #f1f5f9;
}

.progress-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-terminal {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.progress-percent {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.terminal-task-list {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.terminal-task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.terminal-task-name {
  font-size: 12px;
  color: #334155;
}

/* Bug Stat Grid */
.bug-stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 12px;
  cursor: pointer;
}

.bug-stat-item {
  text-align: center;
  padding: 12px 8px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition: background 0.15s;
}

.bug-stat-item:hover {
  background: #eef2ff;
}

.bug-stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.bug-stat-label {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #64748b;
}

.bug-list {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bug-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-radius: 8px;
  background: #fafafa;
}

.bug-name {
  font-size: 12px;
  color: #334155;
}


.empty-state {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.document-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}

.document-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.document-name {
  font-size: 13px;
  font-weight: 500;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.document-size {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

/* Feature List */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.feature-item {
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}

.feature-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.feature-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  flex: 1;
}

.feature-meta {
  margin-top: 6px;
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #64748b;
}

.feature-meta-row {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.feature-meta-label {
  color: #64748b;
  white-space: nowrap;
}

.feature-meta-value {
  color: #6366f1;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
}

.feature-meta-value:hover {
  background: #eef2ff;
}

.assignment-list {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.assignment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  background: #fff;
  border-radius: 6px;
  font-size: 12px;
}

.assignment-terminal {
  font-weight: 600;
  color: #6366f1;
  min-width: 40px;
}

.assignment-dev {
  flex: 1;
  color: #334155;
}

.feature-actions {
  margin-top: 6px;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-top: 8px;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 12px;
  background: #f8fafc;
  border-radius: 8px;
}

.info-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}

.info-value {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

@media (max-width: 1100px) {
  .header-card {
    flex-direction: column;
    align-items: stretch;
  }

  .info-grid {
    grid-template-columns: 1fr 1fr;
  }

  .bug-stat-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
