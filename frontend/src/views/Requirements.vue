<template>
  <AppLayout>
    <template #actions>
      <n-button v-if="authStore.isPM" type="primary" class="action-btn" @click="openCreate">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
        </template>
        新增需求
      </n-button>
    </template>

    <div class="requirements-page">
      <!-- 系统板块展示 -->
      <div v-if="systemStats.length" class="system-cards">
        <div v-for="stat in systemStats" :key="stat.system" class="system-card" :class="{ active: filters.system === stat.system }" @click="selectSystem(stat.system)">
          <span class="system-card-count">{{ stat.count }}</span>
          <span class="system-card-name">{{ stat.system }}</span>
        </div>
      </div>
      <section class="section-card">
        <div class="filter-bar">
          <n-input v-model:value="filters.number" placeholder="需求编号" clearable style="width:140px" size="small" />
          <n-input v-model:value="filters.description" placeholder="需求描述" clearable style="width:180px" size="small" />
          <n-select v-model:value="filters.status" :options="statusOptions" placeholder="状态" clearable style="width:130px" size="small" />
          <n-select v-model:value="filters.project_type" :options="projectTypeOptions" placeholder="项目类型" clearable style="width:130px" size="small" />
          <n-select v-model:value="filters.project_id" :options="projectOptions" placeholder="项目名称" clearable filterable style="width:160px" size="small" />
          <n-select v-model:value="filters.system" :options="filterSystemOptions" placeholder="所属系统" clearable filterable style="width:140px" size="small" />
          <n-select v-model:value="filters.iteration_id" :options="iterationOptions" placeholder="需求迭代" clearable filterable style="width:160px" size="small" />
          <n-button size="small" quaternary @click="resetFilters">重置</n-button>
        </div>
        <n-data-table
          :columns="columns"
          :data="filteredData"
          :pagination="{ pageSize: 15 }"
          :bordered="false"
          :single-line="false"
          striped
          :scroll-x="1370"
        />
      </section>
    </div>

    <!-- 设置需求迭代 -->
    <n-modal v-model:show="showIterationModal" preset="card" style="width: min(92vw, 400px)" title="设置需求迭代" :mask-closable="false">
      <n-select v-model:value="editingIterationId" :options="publishableIterationOptions" placeholder="选择迭代" clearable filterable />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showIterationModal = false">取消</n-button>
          <n-button type="primary" :loading="savingIteration" @click="confirmSetIteration">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <n-modal v-model:show="showModal" preset="card" style="width: min(92vw, 640px)" :title="editingId ? '编辑需求' : '新增需求'" :mask-closable="false">
      <n-form :model="form" label-placement="top">
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="需求编号">
              <n-input v-model:value="form.number" placeholder="留空则自动生成" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="需求状态">
              <n-select v-model:value="form.status" :options="statusOptions" placeholder="选择状态" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item label="需求描述" path="description" :rule="{ required: true, message: '请输入需求描述' }">
          <n-input v-model:value="form.description" type="textarea" :autosize="{ minRows: 3, maxRows: 6 }" placeholder="输入需求描述" />
        </n-form-item>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="所属项目">
              <n-select v-model:value="form.project_id" :options="projectOptions" placeholder="选择项目" clearable filterable @update:value="onProjectChange" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="所属系统">
              <n-select v-model:value="form.system" :options="systemOptions" placeholder="选择系统" clearable />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="项目类型">
              <n-input :value="projectTypeLabel" disabled />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="所属迭代">
              <n-select v-model:value="form.iteration_id" :options="publishableIterationOptions" placeholder="选择迭代" clearable filterable />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="优先级">
              <n-select v-model:value="form.priority" :options="priorityOptions" placeholder="选择优先级" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="业务负责人">
              <n-input v-model:value="form.person_name" placeholder="输入业务负责人" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item label="需求文档">
          <n-upload
            :show-file-list="false"
            :custom-request="handleAttachUpload"
            accept="*/*"
          >
            <n-button :loading="attachUploading">
              <template #icon>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/><polyline points="17 8 12 3 7 8"/><line x1="12" y1="3" x2="12" y2="15"/>
                </svg>
              </template>
              {{ attachFileName || '选择文件' }}
            </n-button>
          </n-upload>
          <div v-if="attachFileName" style="margin-top:6px;font-size:12px;color:#18a058">已选择: {{ attachFileName }}</div>
        </n-form-item>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="总人天">
              <n-input v-model:value="form.total_amount" placeholder="总人天" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="总价">
              <n-input v-model:value="form.total_price" placeholder="总价" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-form-item label="计划完成时间">
          <n-date-picker v-model:value="form.planned_completion_time" type="date" placeholder="选择计划完成时间" clearable style="width:100%" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="submit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { computed, h, onMounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { getProjects } from '@/api/projects'
import { getSystems } from '@/api/systems'
import { getUsers } from '@/api/users'
import { getIterations } from '@/api/iterations'
import {
  getRequirements,
  createRequirement,
  updateRequirement,
  deleteRequirement,
  uploadRequirementDocument,
  getRequirementSystemStats,
  addToRelease,
  removeFromRelease
} from '@/api/requirements'
import { requirementStatusMeta } from '@/constants/requirementMeta'
import { priorityMeta } from '@/constants/statusMeta'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NDataTable, NTag, NDatePicker, NGrid, NGi, NUpload } from 'naive-ui'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const allData = ref([])
const projects = ref([])
const systems = ref([])
const users = ref([])
const iterations = ref([])
const systemStats = ref([])
const showModal = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const attachFile = ref(null)
const attachFileName = ref('')
const attachUploading = ref(false)
const form = ref(emptyForm())

const showIterationModal = ref(false)
const editingRequirementId = ref(null)
const editingIterationId = ref(null)
const savingIteration = ref(false)

const filters = ref({ number: '', description: '', status: null, project_type: null, project_id: null, iteration_id: null, system: null, overdue: false })

function emptyForm() {
  return {
    number: '', description: '', status: 'planned', priority: 'medium',
    project_id: null, project_type: 'ops', system: null, person_id: null, person_name: '',
    iteration_id: null, dev_total: '', dev_price: '', test_total: '', test_price: '',
    total_amount: '', total_price: '', planned_completion_time: null
  }
}

const projectOptions = computed(() => projects.value.map(p => ({ label: p.name, value: p.id })))
const userOptions = computed(() => users.value.map(u => ({ label: u.name, value: u.id })))
const iterationOptions = computed(() => iterations.value.map(i => ({ label: i.name, value: String(i.id) })))
const publishableIterationOptions = computed(() =>
  iterations.value
    .filter(i => i.status === 'pending_publish')
    .map(i => ({ label: i.name, value: String(i.id) }))
)
const systemOptions = computed(() => {
  if (form.value.project_id) {
    const p = projects.value.find(x => x.id === form.value.project_id)
    if (p && p.system_scope) {
      const ids = p.system_scope.split(',').map(Number)
      return systems.value.filter(s => ids.includes(s.id)).map(s => ({ label: s.name, value: s.name }))
    }
    return []
  }
  return systems.value.map(s => ({ label: s.name, value: s.name }))
})
const statusOptions = Object.entries(requirementStatusMeta).map(([v, m]) => ({ label: m.label, value: v }))
const projectTypeLabel = computed(() => {
  if (!form.value.project_id) return '选择项目后自动匹配'
  const p = projects.value.find(x => x.id === form.value.project_id)
  if (!p) return '选择项目后自动匹配'
  return p.project_type === 'invite_bidding' ? '项目需求' : '运维需求'
})
const priorityOptions = Object.entries(priorityMeta).map(([v, m]) => ({ label: m.label, value: v }))
const projectTypeOptions = [
  { label: '运维需求', value: 'ops' },
  { label: '项目需求', value: 'project' }
]
const filterSystemOptions = computed(() =>
  systems.value.map(s => ({ label: s.name, value: s.name }))
)

const filteredData = computed(() => {
  const f = filters.value
  return allData.value.filter(r => {
    if (f.number && !(r.number || '').includes(f.number)) return false
    if (f.description && !(r.description || '').toLowerCase().includes(f.description.toLowerCase())) return false
    return true
  })
})

const columns = [
  {
    title: '需求编号', key: 'number', width: 140,
    render(row) {
      return h('a', {
        style: { color: '#6366f1', cursor: 'pointer', fontWeight: '600', textDecoration: 'none' },
        onClick: () => router.push({ path: `/requirements/${row.id}`, query: buildFilterQuery() })
      }, row.number || `REQ-${String(row.id).padStart(4, '0')}`)
    }
  },
  { title: '所属系统', key: 'system', width: 100, render(row) { return row.system || '-' } },
  {
    title: '需求描述', key: 'description', minWidth: 200, ellipsis: true,
    render(row) {
      return h('span', { style: 'overflow:hidden;text-overflow:ellipsis;white-space:nowrap;display:block', title: row.description || '' }, row.description)
    }
  },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      const meta = requirementStatusMeta[row.status] || { label: row.status, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  {
    title: '开发进度', key: 'dev_progress', width: 90,
    render(row) {
      if (row.dev_progress == null) return h('span', { style: 'color:#94a3b8' }, '待分配')
      const color = row.dev_progress >= 100 ? '#18a058' : '#6366f1'
      return h('span', { style: `font-weight:700;color:${color}` }, row.dev_progress + '%')
    }
  },
  {
    title: '测试进度', key: 'test_progress', width: 90,
    render(row) {
      if (row.test_progress == null) return h('span', { style: 'color:#94a3b8' }, '未开始')
      const color = row.test_progress >= 100 ? '#18a058' : '#6366f1'
      return h('span', { style: `font-weight:700;color:${color}` }, row.test_progress + '%')
    }
  },
  {
    title: '优先级', key: 'priority', width: 80,
    render(row) {
      const meta = priorityMeta[row.priority] || { label: row.priority, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  {
    title: '需求迭代', key: 'iteration', width: 120,
    render(row) {
      const label = row.iteration_name || (row.iteration_id ? `迭代 #${row.iteration_id}` : '设置迭代')
      return h('a', {
        style: 'cursor:pointer;color:#6366f1;',
        onClick: () => openSetIteration(row)
      }, label)
    }
  },
  {
    title: '项目名称', key: 'project', width: 120,
    render(row) { return row.project?.name || '-' }
  },
  {
    title: '项目类型', key: 'project_type', width: 90,
    render(row) { return row.project_type === 'ops' ? '运维需求' : row.project_type === 'project' ? '项目需求' : '-' }
  },
  {
    title: '操作', key: 'actions', width: 140,
    render(row) {
      if (!authStore.isPM) return null
      return h('span', { style: 'display:flex;gap:8px' }, [
        h('a', { style: 'cursor:pointer;color:#6366f1', onClick: () => openEdit(row) }, '编辑'),
        h('a', { style: 'cursor:pointer;color:#d03050', onClick: () => handleDelete(row) }, '删除')
      ])
    }
  }
]

function resetFilters() {
  filters.value = { number: '', description: '', status: null, project_type: null, project_id: null, iteration_id: null, system: null, overdue: false }
}

function buildFilterQuery() {
  const q = {}
  const f = filters.value
  if (f.number) q.number = f.number
  if (f.description) q.description = f.description
  if (f.status) q.status = f.status
  if (f.project_type) q.project_type = f.project_type
  if (f.project_id) q.project_id = f.project_id
  if (f.iteration_id) q.iteration_id = f.iteration_id
  if (f.system) q.system = f.system
  if (f.overdue) q.overdue = 'true'
  return q
}

function restoreFiltersFromQuery() {
  const q = route.query
  filters.value = {
    number: q.number || '',
    description: q.description || '',
    status: q.status || null,
    project_type: q.project_type || null,
    project_id: q.project_id ? Number(q.project_id) : null,
    iteration_id: q.iteration_id || null,
    system: q.system || null,
    overdue: q.overdue === 'true'
  }
}

function selectSystem(system) {
  if (filters.value.system === system) {
    filters.value.system = null
  } else {
    filters.value.system = system
  }
}

function openSetIteration(row) {
  editingRequirementId.value = row.id
  editingIterationId.value = row.iteration_id || null
  showIterationModal.value = true
}

async function confirmSetIteration() {
  if (!editingRequirementId.value) return
  savingIteration.value = true
  try {
    if (editingIterationId.value) {
      await addToRelease(editingRequirementId.value, editingIterationId.value)
    } else {
      await removeFromRelease(editingRequirementId.value)
    }
    window.$message?.success('迭代设置已更新')
    showIterationModal.value = false
    await loadData()
  } catch (e) { console.error(e) }
  savingIteration.value = false
}

function onProjectChange() {
  form.value.system = null
  const p = projects.value.find(x => x.id === form.value.project_id)
  form.value.project_type = p ? (p.project_type === 'invite_bidding' ? 'project' : 'ops') : 'ops'
}

function openCreate() {
  editingId.value = null
  form.value = emptyForm()
  showModal.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    number: row.number || '',
    description: row.description || '',
    status: row.status,
    priority: row.priority,
    project_id: row.project_id,
    project_type: row.project_type,
    system: row.system,
    person_id: row.person_id,
    person_name: row.person_name || '',
    iteration_id: row.iteration_id,
    dev_total: row.dev_total || '',
    dev_price: row.dev_price || '',
    test_total: row.test_total || '',
    test_price: row.test_price || '',
    total_amount: row.total_amount || '',
    total_price: row.total_price || '',
    planned_completion_time: row.planned_completion_time ? new Date(row.planned_completion_time).getTime() : null
  }
  attachFileName.value = row.document_name || ''
  attachFile.value = null
  showModal.value = true
}

async function submit() {
  if (!form.value.description.trim()) { window.$message?.warning('请输入需求描述'); return }
  submitting.value = true
  try {
    const payload = {
      ...form.value, description: form.value.description || '',
      planned_completion_time: form.value.planned_completion_time ? new Date(form.value.planned_completion_time).toISOString() : null,
      project_id: form.value.project_id || null,
      person_id: form.value.person_id || null,
      iteration_id: form.value.iteration_id || null
    }
    if (editingId.value) {
      await updateRequirement(editingId.value, payload)
      if (attachFile.value) {
        await uploadRequirementDocument(editingId.value, attachFile.value)
      }
      window.$message?.success('更新成功')
    } else {
      const created = await createRequirement(payload)
      if (attachFile.value) {
        await uploadRequirementDocument(created.id, attachFile.value)
      }
      window.$message?.success('创建成功')
    }
    showModal.value = false
    attachFile.value = null
    attachFileName.value = ''
    await loadData()
  } catch (e) { console.error(e) }
  submitting.value = false
}

function handleAttachUpload({ file }) {
  attachFile.value = file.file
  attachFileName.value = file.name
}

async function handleDelete(row) {
  window.$dialog?.warning({
    title: '确认删除', content: `确定删除需求「${row.description}」吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try { await deleteRequirement(row.id); window.$message?.success('已删除'); await loadData() }
      catch (e) { console.error(e) }
    }
  })
}

async function loadData() {
  try {
    const f = filters.value
    const params = {}
    if (f.status) params.status = f.status
    if (f.system) params.system = f.system
    if (f.project_type) params.project_type = f.project_type
    if (f.project_id) params.project_id = f.project_id
    if (f.iteration_id) params.iteration_id = f.iteration_id
    if (f.overdue) params.overdue = true
    const [reqs, proj, sys, usr, iters, stats] = await Promise.all([
      getRequirements(params), getProjects(), getSystems(), getUsers(), getIterations(),
      getRequirementSystemStats()
    ])
    allData.value = reqs; projects.value = proj; systems.value = sys; users.value = usr; iterations.value = iters; systemStats.value = stats || []
  } catch (e) { console.error(e) }
}

onMounted(() => {
  restoreFiltersFromQuery()
  loadData()
})

watch(
  () => {
    const f = filters.value
    return [f.status, f.system, f.project_type, f.project_id, f.iteration_id, f.overdue]
  },
  () => { loadData() }
)
</script>

<style scoped>
.requirements-page { display: flex; flex-direction: column; gap: 20px; }

.system-cards { display: flex; gap: 12px; flex-wrap: wrap; }
.system-card {
  display: flex; align-items: center; gap: 10px; padding: 14px 20px;
  border-radius: 14px; border: 1px solid #e2e8f0; background: linear-gradient(135deg, #eef2ff 0%, #ffffff 100%);
  min-width: 140px;
  cursor: pointer;
  transition: box-shadow 0.15s, border-color 0.15s;
}
.system-card:hover {
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.12);
}
.system-card.active {
  border-color: #6366f1;
  background: linear-gradient(135deg, #e0e7ff 0%, #ffffff 100%);
}
.system-card-count { font-size: 26px; font-weight: 800; color: #6366f1; line-height: 1; }
.system-card-name { font-size: 13px; color: #334155; font-weight: 500; }
.section-card { background: white; border-radius: 20px; border: 1px solid #e2e8f0; padding: 22px; overflow-x: auto; }
.filter-bar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.action-btn { border-radius: 10px; font-weight: 500; }

@media (max-width: 768px) {
  :deep(.filter-bar > .n-input),
  :deep(.filter-bar > .n-select) {
    flex: 1 1 45%;
    width: auto !important;
  }
}
</style>
