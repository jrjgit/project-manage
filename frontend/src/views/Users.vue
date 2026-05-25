<template>
  <AppLayout>
    <div class="page-shell">
      <section class="hero-card">
        <div>
          <div class="section-kicker">协作成员</div>
          <h2 class="hero-title">用户管理</h2>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">{{ users.length }}</span>
          <span class="hero-stat-label">用户总数</span>
        </div>
      </section>

      <div class="list-container">
        <div class="toolbar">
          <n-button type="primary" size="small" @click="openCreate">
            <template #icon>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
              </svg>
            </template>
            新增用户
          </n-button>
        </div>
        <n-data-table
          :columns="columns"
          :data="users"
          :pagination="{ pageSize: 10 }"
          :bordered="false"
          :single-line="false"
          striped
        />
      </div>

      <n-modal v-model:show="showModal" preset="card" title="新增用户" style="width: 460px;" :mask-closable="false">
        <n-form ref="formRef" :model="createForm" :rules="rules" label-placement="top">
          <n-form-item label="用户名" path="name">
            <n-input v-model:value="createForm.name" placeholder="请输入用户名" />
          </n-form-item>
          <n-form-item label="密码" path="password">
            <n-input v-model:value="createForm.password" type="password" placeholder="至少 6 位字符" />
          </n-form-item>
          <n-form-item label="角色" path="role">
            <n-select v-model:value="createForm.role" :options="roleOptions" placeholder="请选择角色" />
          </n-form-item>
          <n-form-item label="邮箱">
            <n-input v-model:value="createForm.email" type="email" placeholder="可选" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showModal = false">取消</n-button>
            <n-button type="primary" :loading="creating" @click="handleCreate">确定</n-button>
          </n-space>
        </template>
      </n-modal>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { getUsers, updateUserRole, createUser, deleteUser } from '@/api/users'
import AppLayout from '@/components/AppLayout.vue'
import {
  NDataTable, NTag, NSelect, NButton, NModal,
  NForm, NFormItem, NInput, NSpace, NPopconfirm
} from 'naive-ui'

const users = ref([])
const showModal = ref(false)
const creating = ref(false)
const formRef = ref(null)

const createForm = ref({
  name: '',
  password: '',
  role: null,
  email: ''
})

const rules = {
  name: { required: true, message: '请输入用户名', trigger: 'blur' },
  password: { required: true, min: 6, message: '密码至少 6 位', trigger: 'blur' },
  role: { required: true, message: '请选择角色', trigger: 'change' }
}

const roleMap = { pm: '项目经理', dev_lead: '开发组长', dev: '开发', tester_lead: '测试组长', tester: '测试' }
const roleTagTypeMap = { pm: 'error', dev_lead: 'warning', dev: 'success', tester_lead: 'info', tester: 'default' }

const roleOptions = [
  { label: '项目经理', value: 'pm' },
  { label: '开发组长', value: 'dev_lead' },
  { label: '开发', value: 'dev' },
  { label: '测试组长', value: 'tester_lead' },
  { label: '测试', value: 'tester' }
]

const columns = [
  { title: 'ID', key: 'id', width: 60 },
  { title: '用户名', key: 'name', ellipsis: { tooltip: true } },
  { title: '角色', key: 'role', width: 120,
    render(row) {
      return h(NTag, { type: roleTagTypeMap[row.role], size: 'small', round: true }, { default: () => roleMap[row.role] || row.role })
    }
  },
  { title: '邮箱', key: 'email', ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'created_at', width: 140,
    render(row) {
      const d = new Date(row.created_at)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    }
  },
  { title: '操作', key: 'actions', width: 210,
    render(row) {
      return h('div', { style: 'display:flex; align-items:center; gap:8px;' }, [
        h(NSelect, {
          value: row.role,
          options: roleOptions,
          size: 'small',
          style: 'width: 120px;',
          onUpdateValue: (val) => handleRoleChange(row.id, val)
        }),
        h(NPopconfirm, {
          onPositiveClick: () => handleDelete(row.id)
        }, {
          trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
          default: () => `确定删除用户「${row.name}」吗？`
        })
      ])
    }
  }
]

function openCreate() {
  createForm.value = { name: '', password: '', role: null, email: '' }
  showModal.value = true
}

async function handleCreate() {
  try {
    await formRef.value.validate()
  } catch { return }
  creating.value = true
  try {
    await createUser({
      name: createForm.value.name,
      password: createForm.value.password,
      role: createForm.value.role,
      email: createForm.value.email || undefined
    })
    window.$message.success('用户创建成功')
    showModal.value = false
    loadUsers()
  } catch (e) { console.error(e) }
  creating.value = false
}

async function handleRoleChange(id, role) {
  try {
    await updateUserRole(id, { role })
    window.$message.success('角色更新成功')
    loadUsers()
  } catch (e) {}
}

async function handleDelete(id) {
  try {
    await deleteUser(id)
    window.$message.success('用户已删除')
    loadUsers()
  } catch (e) { console.error(e) }
}

async function loadUsers() {
  try { users.value = await getUsers() } catch (e) {}
}

onMounted(loadUsers)
</script>

<style scoped>
.page-shell {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.hero-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  padding: 20px 24px;
  border-radius: 16px;
  background: #fff;
  border: 1px solid #e5e7eb;
}

.section-kicker {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: #6366f1;
}

.hero-title {
  margin: 6px 0 0;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.hero-stat {
  min-width: 80px;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  text-align: center;
}

.hero-stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.hero-stat-label {
  display: block;
  margin-top: 2px;
  font-size: 12px;
  color: #64748b;
}

.list-container {
  background: white;
  border-radius: 16px;
  padding: 0 8px 8px;
  border: 1px solid #e5e7eb;
}

.toolbar {
  padding: 12px 8px;
  display: flex;
  justify-content: flex-end;
}
</style>
