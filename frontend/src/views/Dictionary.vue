<template>
  <AppLayout title="基础数据字典" subtitle="系统配置字典维护">
    <div class="dictionary-page">
      <!-- Filter bar -->
      <section class="toolbar section-card">
        <div class="toolbar-row">
          <div class="toolbar-left">
            <h2 class="toolbar-title">字典管理</h2>
          </div>
          <div class="toolbar-right">
            <n-select
              v-model:value="filterType"
              :options="typeOptions"
              placeholder="字典类型"
              clearable
              style="width: 160px;"
              size="small"
              @update:value="loadData"
            />
            <n-button
              v-if="authStore.isPM"
              type="primary"
              size="small"
              @click="openCreate"
            >
              新建字典
            </n-button>
          </div>
        </div>
      </section>

      <!-- Data table -->
      <section class="section-card">
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

    <!-- Create / Edit Modal -->
    <n-modal v-model:show="showModal" preset="card" style="width: 480px" :title="isEditing ? '编辑字典' : '新建字典'" :mask-closable="false">
      <n-form :model="form" label-placement="top">
        <n-form-item label="字典类型" path="dictType" :rule="{ required: true, message: '请选择字典类型' }">
          <n-select v-model:value="form.dictType" :options="typeOptions" placeholder="选择字典类型" />
        </n-form-item>
        <n-form-item label="字典键" path="dictKey" :rule="{ required: true, message: '请输入字典键' }">
          <n-input v-model:value="form.dictKey" placeholder="如：normal" />
        </n-form-item>
        <n-form-item label="字典值" path="dictValue" :rule="{ required: true, message: '请输入字典值' }">
          <n-input v-model:value="form.dictValue" placeholder="如：普通" />
        </n-form-item>
        <n-form-item label="排序" path="sortOrder">
          <n-input-number v-model:value="form.sortOrder" :min="0" style="width: 100%;" placeholder="排序号" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="saving" :disabled="!form.dictType || !form.dictKey || !form.dictValue" @click="handleSave">确定</n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- Delete Confirm Modal -->
    <n-modal v-model:show="showDeleteConfirm" preset="dialog" type="warning" title="确认删除" content="确定要删除该字典项？" style="width: 400px">
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
import { getDictionaries, createDictionary, updateDictionary, deleteDictionary } from '@/api/dictionaries'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NDataTable, NModal, NForm, NFormItem, NInput, NInputNumber, NSelect, NSpace, NTag } from 'naive-ui'

const authStore = useAuthStore()

const allData = ref([])
const filterType = ref(null)
const showModal = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const saving = ref(false)
const showDeleteConfirm = ref(false)
const deleting = ref(false)
const deletingId = ref(null)
const form = ref({ dictType: null, dictKey: '', dictValue: '', sortOrder: 0 })

const typeOptions = [
  { label: '系统', value: 'system' },
  { label: '来源', value: 'source' },
  { label: '项目类型', value: 'project_type' }
]

const typeTagMap = {
  system: { type: 'info', label: '系统' },
  source: { type: 'warning', label: '来源' },
  project_type: { type: 'success', label: '项目类型' }
}

const filteredData = computed(() => {
  if (!filterType.value) return allData.value
  return allData.value.filter((item) => item.dictType === filterType.value)
})

const columns = [
  {
    title: '字典类型',
    key: 'dictType',
    width: 130,
    render(row) {
      const meta = typeTagMap[row.dictType] || { type: 'default', label: row.dictType }
      return h(NTag, { type: meta.type, size: 'small', round: true }, { default: () => meta.label })
    }
  },
  { title: '字典键', key: 'dictKey', width: 160 },
  { title: '字典值', key: 'dictValue', width: 200 },
  { title: '排序', key: 'sortOrder', width: 80 },
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
]

function openCreate() {
  isEditing.value = false
  editingId.value = null
  form.value = { dictType: null, dictKey: '', dictValue: '', sortOrder: 0 }
  showModal.value = true
}

function openEdit(row) {
  isEditing.value = true
  editingId.value = row.id
  form.value = {
    dictType: row.dictType,
    dictKey: row.dictKey,
    dictValue: row.dictValue,
    sortOrder: row.sortOrder || 0
  }
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
      dictType: form.value.dictType,
      dictKey: form.value.dictKey,
      dictValue: form.value.dictValue,
      sortOrder: form.value.sortOrder
    }
    if (isEditing.value) {
      await updateDictionary(editingId.value, payload)
      window.$message.success('字典已更新')
    } else {
      await createDictionary(payload)
      window.$message.success('字典已创建')
    }
    showModal.value = false
    await loadData()
  } catch (e) {
    window.$message.error(isEditing.value ? '更新失败' : '创建失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete() {
  deleting.value = true
  try {
    await deleteDictionary(deletingId.value)
    window.$message.success('字典已删除')
    showDeleteConfirm.value = false
    deletingId.value = null
    await loadData()
  } catch (e) {
    window.$message.error('删除失败')
  } finally {
    deleting.value = false
  }
}

async function loadData() {
  try {
    const res = await getDictionaries(filterType.value)
    allData.value = (res || []).map(item => ({
      id: item.id,
      dictType: item.dict_type,
      dictKey: item.dict_key,
      dictValue: item.dict_value,
      sortOrder: item.sort_order
    }))
  } catch (e) {
    window.$message?.error('加载字典数据失败')
  }
}

onMounted(loadData)
</script>

<style scoped>
.dictionary-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.toolbar {
  padding: 14px 18px;
}

.toolbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-card {
  background: white;
  border-radius: 18px;
  border: 1px solid #e2e8f0;
  padding: 16px;
}
</style>
