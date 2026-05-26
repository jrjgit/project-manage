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
      <section class="hero-card">
        <div>
          <div class="hero-eyebrow">需求总览</div>
          <h2 class="hero-title">需求交付看板</h2>
          <p class="hero-subtitle">集中管理运维需求与项目需求，跟踪开发、测试与发布全流程。</p>
        </div>
        <div class="hero-meta">
          <div class="meta-pill"><span class="meta-value">{{ allData.length }}</span><span class="meta-label">需求总数</span></div>
          <div class="meta-pill"><span class="meta-value">{{ inProgressCount }}</span><span class="meta-label">开发中</span></div>
          <div class="meta-pill"><span class="meta-value">{{ pendingReleaseCount }}</span><span class="meta-label">待发布</span></div>
        </div>
      </section>

      <section class="section-card">
        <div class="filter-bar">
          <n-input v-model:value="filters.number" placeholder="需求编号" clearable style="width:140px" size="small" />
          <n-input v-model:value="filters.title" placeholder="需求描述" clearable style="width:180px" size="small" />
          <n-select v-model:value="filters.status" :options="statusOptions" placeholder="状态" clearable style="width:130px" size="small" />
          <n-select v-model:value="filters.project_type" :options="projectTypeOptions" placeholder="项目类型" clearable style="width:130px" size="small" />
          <n-select v-model:value="filters.project_id" :options="projectOptions" placeholder="项目名称" clearable filterable style="width:160px" size="small" />
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
        />
      </section>
    </div>

    <n-modal v-model:show="showModal" preset="card" style="width: 640px" :title="editingId ? '编辑需求' : '新增需求'" :mask-closable="false">
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
        <n-form-item label="需求描述" path="title" :rule="{ required: true, message: '请输入需求描述' }">
          <n-input v-model:value="form.title" placeholder="输入需求描述" />
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
              <n-select v-model:value="form.iteration_id" :options="iterationOptions" placeholder="选择迭代" clearable filterable />
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
        <n-form-item label="需求方案附件">
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
            <n-form-item label="开发人天">
              <n-input v-model:value="form.dev_total" placeholder="开发人天" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="开发单价">
              <n-input v-model:value="form.dev_price" placeholder="开发单价" />
            </n-form-item>
          </n-gi>
        </n-grid>
        <n-grid :cols="2" :x-gap="16">
          <n-gi>
            <n-form-item label="测试人天">
              <n-input v-model:value="form.test_total" placeholder="测试人天" />
            </n-form-item>
          </n-gi>
          <n-gi>
            <n-form-item label="测试单价">
              <n-input v-model:value="form.test_price" placeholder="测试单价" />
            </n-form-item>
          </n-gi>
        </n-grid>
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
import { useRouter } from 'vue-router'
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
  uploadRequirementDocument
} from '@/api/requirements'
import { requirementStatusMeta } from '@/constants/requirementMeta'
import { priorityMeta } from '@/constants/statusMeta'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NDataTable, NTag, NDatePicker, NGrid, NGi, NUpload } from 'naive-ui'

const authStore = useAuthStore()
const router = useRouter()

const allData = ref([])
const projects = ref([])
const systems = ref([])
const users = ref([])
const iterations = ref([])
const showModal = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const attachFile = ref(null)
const attachFileName = ref('')
const attachUploading = ref(false)
const form = ref(emptyForm())

const filters = ref({ number: '', title: '', status: null, project_type: null, project_id: null, iteration_id: null })

function emptyForm() {
  return {
    number: '', title: '', description: '', status: 'planned', priority: 'medium',
    project_id: null, project_type: 'ops', system: null, person_id: null, person_name: '',
    iteration_id: null, dev_total: '', dev_price: '', test_total: '', test_price: '',
    total_amount: '', total_price: '', planned_completion_time: null
  }
}

const projectOptions = computed(() => projects.value.map(p => ({ label: p.name, value: p.id })))
const userOptions = computed(() => users.value.map(u => ({ label: u.name, value: u.id })))
const iterationOptions = computed(() => iterations.value.map(i => ({ label: i.name, value: String(i.id) })))
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

const inProgressCount = computed(() => allData.value.filter(r => r.status === 'in_progress').length)
const pendingReleaseCount = computed(() => allData.value.filter(r => r.status === 'pending_release').length)

const filteredData = computed(() => {
  const f = filters.value
  return allData.value.filter(r => {
    if (f.number && !(r.number || '').includes(f.number)) return false
    if (f.title && !(r.title || '').toLowerCase().includes(f.title.toLowerCase())) return false
    if (f.status && r.status !== f.status) return false
    if (f.project_type && r.project_type !== f.project_type) return false
    if (f.project_id && r.project_id !== f.project_id) return false
    if (f.iteration_id && r.iteration_id !== f.iteration_id) return false
    return true
  })
})

const columns = [
  {
    title: '需求编号', key: 'number', width: 140,
    render(row) {
      return h('a', {
        style: { color: '#6366f1', cursor: 'pointer', fontWeight: '600', textDecoration: 'none' },
        onClick: () => router.push(`/requirements/${row.id}`)
      }, row.number || `REQ-${String(row.id).padStart(4, '0')}`)
    }
  },
  { title: '需求描述', key: 'title', ellipsis: { tooltip: true }, minWidth: 200 },
  {
    title: '状态', key: 'status', width: 100,
    render(row) {
      const meta = requirementStatusMeta[row.status] || { label: row.status, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
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
    render(row) { return row.iteration_name || (row.iteration_id ? `迭代 #${row.iteration_id}` : '-') }
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
  filters.value = { number: '', title: '', status: null, project_type: null, project_id: null, iteration_id: null }
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
    title: row.title,
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
  attachFileName.value = ''
  attachFile.value = null
  showModal.value = true
}

async function submit() {
  if (!form.value.title.trim()) { window.$message?.warning('请输入需求描述'); return }
  submitting.value = true
  try {
    const payload = {
      ...form.value,
      planned_completion_time: form.value.planned_completion_time ? new Date(form.value.planned_completion_time).toISOString() : null,
      project_id: form.value.project_id || null,
      person_id: form.value.person_id || null,
      iteration_id: form.value.iteration_id || null
    }
    if (editingId.value) {
      await updateRequirement(editingId.value, payload)
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
  } catch (e) { window.$message?.error('操作失败') }
  submitting.value = false
}

function handleAttachUpload({ file }) {
  attachFile.value = file.file
  attachFileName.value = file.name
}

async function handleDelete(row) {
  window.$dialog?.warning({
    title: '确认删除', content: `确定删除需求「${row.title}」吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try { await deleteRequirement(row.id); window.$message?.success('已删除'); await loadData() }
      catch (e) { window.$message?.error('删除失败') }
    }
  })
}

async function loadData() {
  try {
    const [reqs, proj, sys, usr, iters] = await Promise.all([
      getRequirements(), getProjects(), getSystems(), getUsers(), getIterations()
    ])
    allData.value = reqs; projects.value = proj; systems.value = sys; users.value = usr; iterations.value = iters
  } catch (e) { window.$message?.error('加载数据失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.requirements-page { display: flex; flex-direction: column; gap: 20px; }
.hero-card {
  display: flex; align-items: flex-start; justify-content: space-between; gap: 20px;
  padding: 28px 30px; border-radius: 24px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 55%, #312e81 100%);
  color: white; box-shadow: 0 18px 48px rgba(15,23,42,0.18);
}
.hero-eyebrow { font-size: 12px; font-weight: 600; letter-spacing: 0.08em; text-transform: uppercase; color: rgba(255,255,255,0.72); margin-bottom: 12px; }
.hero-title { margin: 0; font-size: 28px; font-weight: 700; line-height: 1.25; }
.hero-subtitle { margin: 12px 0 0; max-width: 760px; font-size: 14px; line-height: 1.7; color: rgba(255,255,255,0.82); }
.hero-meta { display: flex; gap: 12px; }
.meta-pill { min-width: 92px; padding: 14px 16px; border-radius: 18px; background: rgba(255,255,255,0.08); border: 1px solid rgba(255,255,255,0.12); backdrop-filter: blur(6px); }
.meta-value { display: block; font-size: 24px; font-weight: 700; }
.meta-label { display: block; margin-top: 4px; font-size: 12px; color: rgba(255,255,255,0.74); }
.section-card { background: white; border-radius: 20px; border: 1px solid #e2e8f0; padding: 22px; }
.filter-bar { display: flex; align-items: center; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.action-btn { border-radius: 10px; font-weight: 500; }
</style>
