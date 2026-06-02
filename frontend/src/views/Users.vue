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

      <n-modal v-model:show="showModal" preset="card" :title="editingId ? '编辑用户' : '新增用户'" style="width: 460px;" :mask-closable="false">
        <n-form ref="formRef" :model="form" :rules="rules" label-placement="top">
          <n-form-item label="用户名" path="name">
            <n-input v-model:value="form.name" placeholder="输入用户名（显示用）" />
          </n-form-item>
          <n-form-item label="账号" path="account">
            <n-input v-model:value="form.account" placeholder="输入登录账号" />
          </n-form-item>
          <n-form-item v-if="!editingId" label="密码" path="password">
            <n-input v-model:value="form.password" type="password" placeholder="至少 6 位字符" />
          </n-form-item>
          <n-form-item label="角色" path="role">
            <n-select v-model:value="form.role" :options="roleOptions" placeholder="请选择角色" />
          </n-form-item>
          <n-form-item label="邮箱">
            <n-input v-model:value="form.email" type="email" placeholder="可选" />
          </n-form-item>
          <n-form-item label="技能">
            <n-select v-model:value="form.skills" :options="skillOptions" multiple placeholder="选择技能" />
          </n-form-item>
        </n-form>
        <template #footer>
          <n-space justify="end">
            <n-button @click="showModal = false">取消</n-button>
            <n-button type="primary" :loading="saving" @click="handleSave">确定</n-button>
          </n-space>
        </template>
      </n-modal>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted, h, computed } from 'vue'
import { getUsers, updateUser, updateUserRole, createUser, deleteUser } from '@/api/users'
import { getDictionaries } from '@/api/dictionaries'
import AppLayout from '@/components/AppLayout.vue'
import {
  NDataTable, NTag, NSelect, NButton, NModal,
  NForm, NFormItem, NInput, NSpace, NPopconfirm
} from 'naive-ui'

const users = ref([])
const skills = ref([])
const showModal = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formRef = ref(null)

const skillOptions = computed(() => skills.value.map(s => ({ label: s.dict_value, value: s.dict_key })))

const form = ref({ name: '', account: '', password: '', role: null, email: '', skills: [] })

const rules = {
  name: { required: true, message: '请输入用户名', trigger: 'blur' },
  account: { required: true, message: '请输入登录账号', trigger: 'blur' },
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
  { title: '账号', key: 'account', ellipsis: { tooltip: true } },
  { title: '技能', key: 'skills', width: 160, render(row) { return row.skills ? row.skills.split(',').join('、') : '-' } },
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
        h('a', { style: 'cursor:pointer;color:#6366f1;font-size:13px', onClick: () => openEdit(row) }, '编辑'),
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
  editingId.value = null
  form.value = { name: '', account: '', password: '', role: null, email: '', skills: [] }
  showModal.value = true
}

function openEdit(row) {
  editingId.value = row.id
  form.value = {
    name: row.name,
    account: row.account || '',
    password: '',
    role: row.role,
    email: row.email || '',
    skills: row.skills ? row.skills.split(',') : []
  }
  showModal.value = true
}

async function handleSave() {
  try {
    if (!editingId.value) await formRef.value.validate()
  } catch { return }
  saving.value = true
  try {
    const payload = {
      name: form.value.name,
      account: form.value.account,
      role: form.value.role,
      email: form.value.email || undefined,
      skills: form.value.skills.length ? form.value.skills.join(',') : undefined
    }
    if (editingId.value) {
      await updateUser(editingId.value, payload)
      window.$message.success('用户更新成功')
    } else {
      await createUser({ ...payload, password: form.value.password })
      window.$message.success('用户创建成功')
    }
    showModal.value = false
    loadUsers()
  } catch (e) { console.error(e) }
  saving.value = false
}

async function handleRoleChange(id, role) {
  try {
    await updateUserRole(id, { role })
    window.$message.success('角色更新成功')
    loadUsers()
  } catch (e) { console.error(e) }
}
 
async function handleDelete(id) {
  try {
    await deleteUser(id)
    window.$message.success('用户已删除')
    loadUsers()
  } catch (e) { console.error(e) }
}

async function loadUsers() {
  try { users.value = await getUsers() } catch (e) { console.error(e) }
}
 
async function loadSkills() {
  try { skills.value = await getDictionaries('skill') } catch (e) { console.error(e) }
}

onMounted(() => { loadUsers(); loadSkills() })
</script>

<style scoped>
.page-shell { display: flex; flex-direction: column; gap: 20px; }
.hero-card { display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 20px 24px; border-radius: 16px; background: #fff; border: 1px solid #e5e7eb; }
.section-kicker { font-size: 12px; font-weight: 700; letter-spacing: 0.06em; text-transform: uppercase; color: #6366f1; }
.hero-title { margin: 6px 0 0; font-size: 22px; font-weight: 700; color: #0f172a; }
.hero-stat { min-width: 80px; padding: 10px 14px; border-radius: 10px; background: #f1f5f9; border: 1px solid #e2e8f0; text-align: center; }
.hero-stat-value { display: block; font-size: 22px; font-weight: 700; color: #0f172a; }
.hero-stat-label { display: block; margin-top: 2px; font-size: 12px; color: #64748b; }
.list-container { background: white; border-radius: 16px; padding: 0 8px 8px; border: 1px solid #e5e7eb; }
.toolbar { padding: 12px 8px; display: flex; justify-content: flex-end; }
</style>
