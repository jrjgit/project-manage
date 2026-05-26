<template>
  <AppLayout>
    <template #actions>
      <n-button v-if="authStore.isPM" type="primary" class="action-btn" @click="openCreate">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
        </template>
        新增项目
      </n-button>
    </template>

    <div class="projects-page">
      <section class="section-card">
        <n-data-table
          :columns="columns"
          :data="projects"
          :pagination="{ pageSize: 15 }"
          :bordered="false"
          :single-line="false"
          striped
        />
      </section>
    </div>

    <n-modal v-model:show="showModal" preset="card" style="width: 560px" :title="editingId ? '编辑项目' : '新增项目'" :mask-closable="false">
      <n-form :model="form" label-placement="top">
        <n-form-item label="项目名称" path="name" :rule="{ required: true, message: '请输入项目名称' }">
          <n-input v-model:value="form.name" placeholder="输入项目名称" />
        </n-form-item>
        <n-form-item label="项目编号">
          <n-input v-model:value="form.code" placeholder="输入项目编号" />
        </n-form-item>
        <n-form-item label="项目类型">
          <n-select v-model:value="form.project_type" :options="projectTypeOptions" placeholder="选择项目类型" />
        </n-form-item>
        <n-form-item label="系统范围">
          <n-select v-model:value="form.system_scope" :options="systemOptions" multiple placeholder="选择系统范围" />
        </n-form-item>
        <n-form-item label="人力资源范围">
          <n-select v-model:value="form.hr_scope" :options="userOptions" multiple placeholder="选择人员" filterable />
        </n-form-item>
        <n-form-item v-if="editingId" label="项目经理">
          <n-select v-model:value="form.pm_id" :options="userOptions" placeholder="切换项目经理" filterable clearable />
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
import { computed, h, onMounted, ref } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getProjects, createProject, updateProject, deleteProject } from '@/api/projects'
import { getSystems } from '@/api/systems'
import { getUsers } from '@/api/users'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NForm, NFormItem, NInput, NSelect, NSpace, NDataTable } from 'naive-ui'

const authStore = useAuthStore()
const projects = ref([])
const systems = ref([])
const users = ref([])
const showModal = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const form = ref({ name: '', code: '', project_type: null, system_scope: [], hr_scope: [], pm_id: null })

const projectTypeOptions = [
  { label: '邀标项目', value: 'invite_bidding' },
  { label: '应需运维', value: 'ops' }
]
const systemOptions = computed(() => systems.value.map(s => ({ label: s.name, value: s.id })))
const userOptions = computed(() => users.value.map(u => ({ label: u.name, value: u.id })))

const columns = [
  { title: '项目名称', key: 'name', minWidth: 140 },
  { title: '项目编号', key: 'code', width: 120, render(row) { return row.code || '-' } },
  { title: '项目类型', key: 'project_type', width: 100, render(row) { return row.project_type === 'invite_bidding' ? '邀标项目' : row.project_type === 'ops' ? '应需运维' : '-' } },
  { title: '项目经理', key: 'pm', width: 100, render(row) { return row.pm?.name || '-' } },
  { title: '创建人', key: 'creator', width: 100, render(row) { return row.creator?.name || '-' } },
  { title: '创建时间', key: 'created_at', width: 160, render(row) { return row.created_at ? new Date(row.created_at).toLocaleString() : '-' } },
  {
    title: '操作', key: 'actions', width: 120,
    render(row) {
      if (!authStore.isPM) return null
      return h('span', { style: 'display:flex;gap:8px' }, [
        h('a', { style: 'cursor:pointer;color:#6366f1', onClick: () => openEdit(row) }, '编辑'),
        h('a', { style: 'cursor:pointer;color:#d03050', onClick: () => handleDelete(row) }, '删除')
      ])
    }
  }
]

function openCreate() {
  editingId.value = null
  form.value = { name: '', code: '', project_type: null, system_scope: [], hr_scope: [], pm_id: null }
  showModal.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    name: row.name,
    code: row.code || '',
    project_type: row.project_type || null,
    system_scope: row.system_scope ? row.system_scope.split(',').map(Number) : [],
    hr_scope: row.hr_scope ? row.hr_scope.split(',').map(Number) : [],
    pm_id: row.pm_id || null
  }
  showModal.value = true
}

async function submit() {
  if (!form.value.name.trim()) { window.$message?.warning('请输入项目名称'); return }
  submitting.value = true
  try {
    const payload = {
      name: form.value.name,
      code: form.value.code || null,
      project_type: form.value.project_type,
      system_scope: form.value.system_scope.length ? form.value.system_scope : null,
      hr_scope: form.value.hr_scope.length ? form.value.hr_scope : null,
      pm_id: form.value.pm_id || null
    }
    if (editingId.value) {
      await updateProject(editingId.value, payload)
      window.$message?.success('更新成功')
    } else {
      await createProject(payload)
      window.$message?.success('创建成功')
    }
    showModal.value = false
    await loadData()
  } catch (e) { window.$message?.error('操作失败') }
  submitting.value = false
}

async function handleDelete(row) {
  window.$dialog?.confirm({
    title: '确认删除', content: `确定删除项目「${row.name}」吗？`,
    onPositiveClick: async () => {
      try { await deleteProject(row.id); window.$message?.success('已删除'); await loadData() }
      catch (e) { window.$message?.error('删除失败') }
    }
  })
}

async function loadData() {
  try {
    const [proj, sys, usr] = await Promise.all([getProjects(), getSystems(), getUsers()])
    projects.value = proj; systems.value = sys; users.value = usr
  } catch (e) { window.$message?.error('加载失败') }
}

onMounted(() => loadData())
</script>

<style scoped>
.projects-page { display: flex; flex-direction: column; gap: 20px; }
.section-card { background: white; border-radius: 20px; border: 1px solid #e2e8f0; padding: 22px; }
.action-btn { border-radius: 10px; font-weight: 500; }
</style>
