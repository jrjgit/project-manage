<template>
  <AppLayout>
    <template #actions>
      <n-button v-if="authStore.isPM" type="primary" class="action-btn" @click="showCreateModal = true">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
        </template>
        创建需求
      </n-button>
    </template>

    <div class="requirements-page">
      <!-- Hero Stats -->
      <section class="hero-card">
        <div>
          <div class="hero-eyebrow">需求总览</div>
          <h2 class="hero-title">需求交付看板</h2>
          <p class="hero-subtitle">集中管理运维需求与项目需求，跟踪开发、测试与发布全流程。</p>
        </div>
        <div class="hero-meta">
          <div class="meta-pill">
            <span class="meta-value">{{ opsCount }}</span>
            <span class="meta-label">运维需求</span>
          </div>
          <div class="meta-pill">
            <span class="meta-value">{{ projectCount }}</span>
            <span class="meta-label">项目需求</span>
          </div>
          <div class="meta-pill">
            <span class="meta-value">{{ inProgressCount }}</span>
            <span class="meta-label">进行中</span>
          </div>
          <div class="meta-pill">
            <span class="meta-value">{{ pendingReleaseCount }}</span>
            <span class="meta-label">待发布</span>
          </div>
        </div>
      </section>

      <!-- Tabs -->
      <section class="section-card">
        <n-tabs v-model:value="activeTab" type="line" animated>
          <n-tab-pane name="ops" tab="运维需求清单">
            <div class="filter-bar">
              <n-select v-model:value="filterSystem" :options="systemOptions" placeholder="筛选系统" clearable style="width: 160px;" size="small" />
              <n-select v-model:value="filterStatus" :options="statusOptions" placeholder="筛选状态" clearable style="width: 160px;" size="small" />
              <n-button size="small" quaternary @click="resetFilters">重置</n-button>
              <n-button size="small" quaternary @click="loadData">
                <template #icon><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg></template>
              </n-button>
            </div>
            <div class="table-container">
              <n-data-table
                :columns="columns"
                :data="filteredOpsRequirements"
                :pagination="{ pageSize: 15 }"
                :bordered="false"
                :single-line="false"
                striped
              />
            </div>
          </n-tab-pane>
          <n-tab-pane name="project" tab="项目需求清单">
            <div class="filter-bar">
              <n-select v-model:value="filterSystem" :options="systemOptions" placeholder="筛选系统" clearable style="width: 160px;" size="small" />
              <n-select v-model:value="filterStatus" :options="statusOptions" placeholder="筛选状态" clearable style="width: 160px;" size="small" />
              <n-button size="small" quaternary @click="resetFilters">重置</n-button>
              <n-button size="small" quaternary @click="loadData">
                <template #icon><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px"><polyline points="23 4 23 10 17 10"/><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"/></svg></template>
              </n-button>
            </div>
            <div class="table-container">
              <n-data-table
                :columns="columns"
                :data="filteredProjectRequirements"
                :pagination="{ pageSize: 15 }"
                :bordered="false"
                :single-line="false"
                striped
              />
            </div>
          </n-tab-pane>
        </n-tabs>
      </section>
    </div>

    <!-- Create Modal -->
    <n-modal v-model:show="showCreateModal" preset="card" style="width: 560px" title="创建需求" :mask-closable="false">
      <n-form :model="createForm" label-placement="top">
        <n-form-item label="需求编号">
          <n-input v-model:value="createForm.number" placeholder="留空则自动生成" />
        </n-form-item>
        <n-form-item label="需求标题" path="title" :rule="{ required: true, message: '请输入需求标题' }">
          <n-input v-model:value="createForm.title" placeholder="输入需求标题" />
        </n-form-item>
        <n-form-item label="需求类型">
          <n-select v-model:value="createForm.project_type" :options="projectTypeOptions" placeholder="选择类型" />
        </n-form-item>
        <n-form-item label="所属项目">
          <n-select v-model:value="createForm.project_id" :options="projectOptions" placeholder="选择项目（可选）" clearable filterable />
        </n-form-item>
        <n-form-item label="业务负责人">
          <n-select v-model:value="createForm.person_id" :options="userOptions" placeholder="选择负责人" filterable />
        </n-form-item>
        <n-form-item label="关联系统">
          <n-select v-model:value="createForm.system" :options="systemOptions" placeholder="选择系统" clearable />
        </n-form-item>
        <n-form-item label="优先级">
          <n-select v-model:value="createForm.priority" :options="priorityOptions" placeholder="选择优先级" />
        </n-form-item>
        <n-form-item label="来源">
          <n-select v-model:value="createForm.source" :options="sourceOptions" placeholder="选择来源" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreateModal = false">取消</n-button>
          <n-button type="primary" :loading="creating" @click="handleCreate">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { computed, h, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { getProjects } from '@/api/projects'
import { getUsers } from '@/api/users'
import {
  getRequirements,
  getOpsRequirements,
  getProjectRequirements,
  createRequirement,
  changeRequirementStatus,
  updateRequirement
} from '@/api/requirements'
import { requirementStatusMeta, requirementSourceMeta } from '@/constants/requirementMeta'
import { priorityMeta } from '@/constants/statusMeta'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NDataTable, NTag, NTabs, NTabPane, NProgress } from 'naive-ui'

const authStore = useAuthStore()
const router = useRouter()

const activeTab = ref('ops')
const allRequirements = ref([])
const opsRequirements = ref([])
const projectRequirements = ref([])
const projects = ref([])
const users = ref([])
const showCreateModal = ref(false)
const creating = ref(false)
const createForm = ref({
  title: '',
  project_type: 'ops',
  project_id: null,
  person_id: null,
  system: null,
  priority: 'medium',
  source: 'internal',
  number: ''
})

const filterSystem = ref(null)
const filterStatus = ref(null)

const projectOptions = computed(() => projects.value.map((p) => ({ label: p.name, value: p.id })))
const userOptions = computed(() => users.value.map((u) => ({ label: u.name, value: u.id })))

const systemOptions = [
  { label: 'CRM', value: 'crm' },
  { label: 'ERP', value: 'erp' },
  { label: 'WMS', value: 'wms' },
  { label: 'OMS', value: 'oms' },
  { label: 'BI', value: 'bi' },
  { label: 'OA', value: 'oa' },
  { label: '其他', value: 'other' }
]

const statusOptions = Object.entries(requirementStatusMeta).map(([value, meta]) => ({ label: meta.label, value }))

const priorityOptions = Object.entries(priorityMeta).map(([value, meta]) => ({ label: meta.label, value }))

const projectTypeOptions = [
  { label: '运维需求', value: 'ops' },
  { label: '项目需求', value: 'project' }
]

const sourceOptions = Object.entries(requirementSourceMeta).map(([value, meta]) => ({ label: meta.label, value }))

const opsCount = computed(() => opsRequirements.value.length)
const projectCount = computed(() => projectRequirements.value.length)
const inProgressCount = computed(() => allRequirements.value.filter((r) => r.status === 'in_progress').length)
const pendingReleaseCount = computed(() => allRequirements.value.filter((r) => r.status === 'pending_release').length)

const filteredOpsRequirements = computed(() => {
  return opsRequirements.value.filter((req) => {
    if (filterSystem.value && req.system !== filterSystem.value) return false
    if (filterStatus.value && req.status !== filterStatus.value) return false
    return true
  })
})

const filteredProjectRequirements = computed(() => {
  return projectRequirements.value.filter((req) => {
    if (filterSystem.value && req.system !== filterSystem.value) return false
    if (filterStatus.value && req.status !== filterStatus.value) return false
    return true
  })
})

const columns = [
  {
    title: '需求编号',
    key: 'number',
    width: 140,
    render(row) {
      return h('a', {
        style: { color: '#6366f1', cursor: 'pointer', fontWeight: '600', textDecoration: 'none' },
        onClick: () => router.push(`/requirements/${row.id}`)
      }, row.number || `REQ-${String(row.id).padStart(4, '0')}`)
    }
  },
  {
    title: '标题',
    key: 'title',
    ellipsis: { tooltip: true },
    minWidth: 180
  },
  {
    title: '业务负责人',
    key: 'person',
    width: 120,
    render(row) {
      return row.person?.name || '-'
    }
  },
  {
    title: '状态',
    key: 'status',
    width: 110,
    render(row) {
      const meta = requirementStatusMeta[row.status] || { label: row.status, tone: 'default' }
      return h(NTag, { type: meta.tone, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  {
    title: '优先级',
    key: 'priority',
    width: 90,
    render(row) {
      const meta = priorityMeta[row.priority] || { label: row.priority, tone: 'default' }
      return h(NTag, {
        type: meta.tone,
        size: 'small',
        round: true,
        style: { cursor: 'pointer' },
        onClick: () => handlePriorityClick(row)
      }, { default: () => meta.label })
    }
  },
  {
    title: '开发进度',
    key: 'dev_progress',
    width: 140,
    render(row) {
      const pct = row.dev_progress != null ? row.dev_progress : 0
      return h(NProgress, {
        type: 'line',
        percentage: pct,
        indicatorPlacement: 'inside',
        height: 18,
        borderRadius: 4,
        color: pct >= 100 ? '#18a058' : '#2080f0'
      })
    }
  },
  {
    title: '综合测试',
    key: 'integration_test_progress',
    width: 100,
    render(row) {
      const pct = row.integration_test_progress != null ? row.integration_test_progress : 0
      return h(NProgress, {
        type: 'line',
        percentage: pct,
        indicatorPlacement: 'inside',
        height: 18,
        borderRadius: 4,
        color: '#f59e0b'
      })
    }
  },
  {
    title: '业务测试',
    key: 'business_test_progress',
    width: 100,
    render(row) {
      const pct = row.business_test_progress != null ? row.business_test_progress : 0
      return h(NProgress, {
        type: 'line',
        percentage: pct,
        indicatorPlacement: 'inside',
        height: 18,
        borderRadius: 4,
        color: '#f0a020'
      })
    }
  },
  {
    title: '发布迭代',
    key: 'iteration',
    width: 120,
    render(row) {
      return row.iteration_id ? `迭代 #${row.iteration_id}` : '-'
    }
  },
  {
    title: '所属项目',
    key: 'project',
    width: 120,
    render(row) {
      return row.project?.name || '-'
    }
  }
]

function handlePriorityClick(row) {
  if (!authStore.isPM) return
  const current = row.priority || 'medium'
  const keys = Object.keys(priorityMeta)
  const idx = keys.indexOf(current)
  const next = keys[(idx + 1) % keys.length]
  updateRequirement(row.id, { priority: next }).then(() => {
    row.priority = next
    window.$message?.success('优先级已更新')
  }).catch(() => {
    window.$message?.error('更新失败')
  })
}

function resetFilters() {
  filterSystem.value = null
  filterStatus.value = null
}

async function handleCreate() {
  if (!createForm.value.title.trim()) {
    window.$message?.warning('请输入需求标题')
    return
  }
  creating.value = true
  try {
    await createRequirement(createForm.value)
    window.$message?.success('创建成功')
    showCreateModal.value = false
      createForm.value = {
        title: '',
        project_type: 'ops',
        project_id: null,
        person_id: null,
        system: null,
        priority: 'medium',
        source: 'internal',
        number: ''
      }
    await loadData()
  } catch (e) {
    window.$message?.error('创建失败')
  }
  creating.value = false
}

async function loadData() {
  try {
    const [ops, proj] = await Promise.all([
      getOpsRequirements(),
      getProjectRequirements()
    ])
    opsRequirements.value = ops
    projectRequirements.value = proj
    allRequirements.value = [...ops, ...proj]
  } catch (e) {
    window.$message?.error('加载需求数据失败')
  }
}

async function loadProjects() {
  try { projects.value = await getProjects() } catch (e) {}
}

async function loadUsers() {
  try { users.value = await getUsers() } catch (e) {}
}

onMounted(() => {
  loadData()
  loadProjects()
  loadUsers()
})
</script>

<style scoped>
.requirements-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 30px;
  border-radius: 24px;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 55%, #312e81 100%);
  color: white;
  box-shadow: 0 18px 48px rgba(15, 23, 42, 0.18);
}

.hero-eyebrow {
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(255, 255, 255, 0.72);
  margin-bottom: 12px;
}

.hero-title {
  margin: 0;
  font-size: 28px;
  font-weight: 700;
  line-height: 1.25;
}

.hero-subtitle {
  margin: 12px 0 0;
  max-width: 760px;
  font-size: 14px;
  line-height: 1.7;
  color: rgba(255, 255, 255, 0.82);
}

.hero-meta {
  display: flex;
  gap: 12px;
}

.meta-pill {
  min-width: 92px;
  padding: 14px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(6px);
}

.meta-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
}

.meta-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.74);
}

.section-card {
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  padding: 22px;
}

.filter-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.table-container {
  padding: 0;
}

.action-btn {
  border-radius: 10px;
  font-weight: 500;
}

@media (max-width: 1100px) {
  .hero-card {
    flex-direction: column;
  }

  .hero-meta {
    width: 100%;
    flex-wrap: wrap;
  }

  .meta-pill {
    flex: 1;
    min-width: 80px;
  }
}
</style>
