<template>
  <AppLayout>
    <div class="page-shell">
      <section class="hero-card">
        <div>
          <div class="section-kicker">协作成员</div>
          <h2 class="hero-title">用户与角色分工</h2>
          <p class="hero-subtitle">统一用与任务、Bug 页面一致的容器与层级，方便集中维护协作角色。</p>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">{{ users.length }}</span>
          <span class="hero-stat-label">用户总数</span>
        </div>
      </section>

      <div class="list-container">
        <n-data-table
          :columns="columns"
          :data="users"
          :pagination="{ pageSize: 10 }"
          :bordered="false"
          :single-line="false"
          striped
        />
      </div>
    </div>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted, h } from 'vue'
import { getUsers, updateUserRole } from '@/api/users'
import AppLayout from '@/components/AppLayout.vue'
import { NDataTable, NTag, NSelect } from 'naive-ui'

const users = ref([])

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
  { title: '企业微信', key: 'wechat_id', width: 140 },
  { title: '邮箱', key: 'email', ellipsis: { tooltip: true } },
  { title: '创建时间', key: 'created_at', width: 140,
    render(row) {
      const d = new Date(row.created_at)
      return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    }
  },
  { title: '操作', key: 'actions', width: 160,
    render(row) {
      return h(NSelect, {
        value: row.role,
        options: roleOptions,
        size: 'small',
        style: 'width: 140px;',
        onUpdateValue: (val) => handleRoleChange(row.id, val)
      })
    }
  }
]

async function handleRoleChange(id, role) {
  try {
    await updateUserRole(id, { role })
    window.$message.success('角色更新成功')
    loadUsers()
  } catch (e) {}
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

.list-container {
  background: white;
  border-radius: 18px;
  padding: 8px;
  border: 1px solid #e2e8f0;
}
</style>
