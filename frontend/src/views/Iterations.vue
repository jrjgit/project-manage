<template>
  <AppLayout title="发布迭代管理" subtitle="管理发布迭代与版本计划">
    <div class="iterations-page">
      <section class="hero-card">
        <div>
          <div class="section-kicker">发布迭代</div>
          <h2 class="hero-title">迭代管理</h2>
          <p class="hero-subtitle">管理发布迭代与版本计划，跟踪每个迭代关联的需求交付进度。</p>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">{{ iterations.length }}</span>
          <span class="hero-stat-label">迭代总数</span>
        </div>
      </section>

      <section class="section-card">
        <div class="list-header">
          <h3>迭代列表</h3>
          <n-button v-if="authStore.isPM" type="primary" size="small" @click="openCreate">新建迭代</n-button>
        </div>
        <n-data-table
          :columns="columns"
          :data="iterations"
          :pagination="{ pageSize: 15 }"
          :bordered="false"
          :single-line="false"
          striped
          :row-key="row => row.id"
          :expanded-row-render="expandedRowRender"
        />
      </section>
    </div>

    <!-- Create / Edit Modal -->
    <n-modal v-model:show="showModal" preset="card" style="width: 480px" :title="isEditing ? '编辑迭代' : '新建迭代'" :mask-closable="false">
      <n-form :model="form" label-placement="top">
        <n-form-item label="迭代名称" path="name" :rule="{ required: true, message: '请输入迭代名称' }">
          <n-input v-model:value="form.name" placeholder="如：v2.3.0" />
        </n-form-item>
        <n-form-item label="发布日期" path="releaseTime">
          <n-date-picker v-model:value="form.releaseTime" type="date" placeholder="选择发布日期" clearable style="width: 100%" />
        </n-form-item>
        <n-form-item label="备注">
          <n-input v-model:value="form.notes" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" placeholder="输入备注" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" :disabled="!form.name" @click="handleSave">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Delete Confirm Modal -->
    <n-modal v-model:show="showDeleteConfirm" preset="dialog" type="warning" title="确认删除" content="确定要删除该迭代？关联的需求将被解除发布关联。" style="width: 400px">
      <template #action>
        <n-button @click="showDeleteConfirm = false">取消</n-button>
        <n-button type="error" :loading="deleting" @click="handleDelete">删除</n-button>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { ref, computed, h, onMounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getIterations, createIteration, updateIteration, deleteIteration } from '@/api/iterations'
import { getRequirements } from '@/api/requirements'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NDatePicker, NSpace, NTag } from 'naive-ui'

const authStore = useAuthStore()

const iterations = ref([])
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const saving = ref(false)
const showDeleteConfirm = ref(false)
const deleting = ref(false)
const deletingId = ref(null)
const form = ref({ name: '', releaseTime: null, notes: '' })
const expandedRequirements = ref({})

function formatDate(ts) {
  if (!ts) return '-'
  const d = new Date(ts)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function openCreate() {
  isEditing.value = false
  editingId.value = null
  form.value = { name: '', releaseTime: null, notes: '' }
  showModal.value = true
}

function openEdit(row) {
  isEditing.value = true
  editingId.value = row.id
  form.value = { name: row.name, releaseTime: row.release_time ? new Date(row.release_time).getTime() : null, notes: row.notes || '' }
  showModal.value = true
}

function confirmDelete(row) {
  deletingId.value = row.id
  showDeleteConfirm.value = true
}

async function handleSave() {
  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      release_time: form.value.releaseTime ? new Date(form.value.releaseTime).toISOString() : null,
      notes: form.value.notes || null
    }
    if (isEditing.value) {
      await updateIteration(editingId.value, payload)
      window.$message.success('迭代已更新')
    } else {
      await createIteration(payload)
      window.$message.success('迭代已创建')
    }
    showModal.value = false
    await loadIterations()
  } catch (e) {
    window.$message.error(isEditing.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  deleting.value = true
  try {
    await deleteIteration(deletingId.value)
    window.$message.success('迭代已删除')
    showDeleteConfirm.value = false
    deletingId.value = null
    await loadIterations()
  } catch (e) {
    window.$message.error('删除失败')
  } finally {
    deleting.value = false
  }
}

async function loadIterations() {
  try {
    iterations.value = await getIterations()
  } catch (e) {
    window.$message.error('加载迭代列表失败')
  }
}

async function loadRequirementsForIteration(iterationId) {
  if (expandedRequirements.value[iterationId]) return
  try {
    const data = await getRequirements({ iteration_id: iterationId })
    expandedRequirements.value[iterationId] = data
  } catch (e) {
    expandedRequirements.value[iterationId] = []
  }
}

function expandedRowRender(row) {
  const reqs = expandedRequirements.value[row.id]
  if (!reqs) {
    loadRequirementsForIteration(row.id)
    return h('div', { style: { padding: '12px', color: '#94a3b8', fontSize: '13px' } }, '加载中...')
  }
  if (reqs.length === 0) {
    return h('div', { style: { padding: '12px', color: '#94a3b8', fontSize: '13px' } }, '暂无关联需求')
  }
  return h('div', { style: { padding: '8px 12px' } }, [
    h('div', { style: { fontWeight: 600, fontSize: '13px', color: '#64748b', marginBottom: '8px' } }, '关联需求'),
    ...reqs.map(req =>
      h('div', {
        style: { display: 'flex', alignItems: 'center', gap: '10px', padding: '6px 0', borderBottom: '1px solid #f1f5f9', fontSize: '13px' }
      }, [
        h('span', { style: { color: '#6366f1', fontWeight: 600, minWidth: '80px' } }, `REQ-${String(req.id).padStart(4, '0')}`),
        h('span', { style: { flex: 1 } }, req.title),
        h(NTag, { size: 'tiny', round: true, type: req.status === 'released' ? 'success' : 'info' }, { default: () => req.status })
      ])
    )
  ])
}

const columns = computed(() => [
  {
    title: '迭代名称',
    key: 'name',
    width: 200
  },
  {
    title: '发布日期',
    key: 'release_time',
    width: 140,
    render(row) {
      return formatDate(row.release_time)
    }
  },
  {
    title: '创建人',
    key: 'creator',
    width: 100,
    render(row) {
      return row.creator?.name || '-'
    }
  },
  {
    title: '创建时间',
    key: 'created_at',
    width: 160,
    render(row) {
      return row.created_at ? new Date(row.created_at).toLocaleString() : '-'
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 140,
    render(row) {
      if (!authStore.isPM) return ''
      return h(NSpace, null, {
        default: () => [
          h(NButton, { size: 'tiny', quaternary: true, onClick: () => openEdit(row) }, { default: () => '编辑' }),
          h(NButton, { size: 'tiny', quaternary: true, type: 'error', onClick: () => confirmDelete(row) }, { default: () => '删除' })
        ]
      })
    }
  }
])

onMounted(() => {
  loadIterations()
})
</script>

<style scoped>
.iterations-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  padding: 22px 24px;
  border-radius: 18px;
  background: linear-gradient(135deg, #eef2ff 0%, #ffffff 55%, #f8fafc 100%);
  border: 1px solid #e2e8f0;
}

.section-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: #6366f1;
}

.hero-title {
  margin: 8px 0 0;
  font-size: 26px;
  font-weight: 700;
  color: #0f172a;
}

.hero-subtitle {
  margin: 10px 0 0;
  max-width: 720px;
  color: #64748b;
  font-size: 14px;
  line-height: 1.7;
}

.hero-stat {
  min-width: 96px;
  padding: 14px 16px;
  border-radius: 16px;
  background: white;
  border: 1px solid #dbeafe;
}

.hero-stat-value {
  display: block;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.hero-stat-label {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}

.section-card {
  background: white;
  border-radius: 18px;
  padding: 16px;
  border: 1px solid #e2e8f0;
}

.list-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.list-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}
</style>
