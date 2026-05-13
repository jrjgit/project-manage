<template>
  <AppLayout>
    <div class="page-shell">
      <section class="hero-card">
        <div>
          <div class="section-kicker">协作小组</div>
          <h2 class="hero-title">小组管理</h2>
          <p class="hero-subtitle">创建开发小组，分配组长与成员，方便任务分配时精准定位。</p>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">{{ groups.length }}</span>
          <span class="hero-stat-label">小组总数</span>
        </div>
      </section>

      <div class="main-grid">
        <div class="group-list">
          <div class="list-header">
            <h3>小组列表</h3>
            <n-button type="primary" size="small" @click="showCreate = true">创建小组</n-button>
          </div>
          <div v-if="groups.length === 0" class="empty-state">暂无小组，点击"创建小组"开始。</div>
          <div
            v-for="g in groups"
            :key="g.id"
            :class="['group-row', { active: selectedGroup?.id === g.id }]"
            @click="selectGroup(g)"
          >
            <div class="group-row-main">
              <div class="group-name">{{ g.name }}</div>
              <div class="group-lead">组长: {{ g.dev_lead?.name }}</div>
            </div>
            <div class="group-actions">
              <span class="member-count">{{ g.member_count }} 名开发</span>
              <n-button text size="tiny" type="error" @click.stop="confirmDelete(g)">删除</n-button>
            </div>
          </div>
        </div>

        <div class="member-panel" v-if="selectedGroup">
          <div class="panel-header">
            <h3>{{ selectedGroup.name }} — 成员</h3>
          </div>
          <div v-if="selectedMembers.length === 0" class="empty-state">该组暂无开发人员。</div>
          <div v-for="m in selectedMembers" :key="m.id" class="member-row">
            <div class="member-info">
              <span class="member-name">{{ m.name }}</span>
              <span class="member-role">{{ roleMap[m.role] || m.role }}</span>
            </div>
            <n-button text size="tiny" type="error" @click="handleRemoveMember(m.id)">移除</n-button>
          </div>
          <div class="add-member-row">
            <n-select
              v-model:value="newMemberId"
              :options="availableDevOptions"
              placeholder="选择开发人员..."
              size="small"
              style="flex:1"
            />
            <n-button size="small" type="primary" :disabled="!newMemberId" @click="handleAddMember">加入</n-button>
          </div>
        </div>
        <div v-else class="member-panel empty-panel">
          <div class="empty-state">选择左侧小组查看成员。</div>
        </div>
      </div>
    </div>

    <!-- 创建小组弹窗 -->
    <n-modal v-model:show="showCreate" title="创建小组" preset="dialog" style="width:420px">
      <n-form :model="createForm" label-width="80">
        <n-form-item label="组名" required>
          <n-input v-model:value="createForm.name" placeholder="如：前端组" />
        </n-form-item>
        <n-form-item label="组长" required>
          <n-select v-model:value="createForm.dev_lead_id" :options="devLeadOptions" placeholder="选择开发组长" />
        </n-form-item>
      </n-form>
      <template #action>
        <n-button @click="showCreate = false">取消</n-button>
        <n-button type="primary" :disabled="!createForm.name || !createForm.dev_lead_id" :loading="creating" @click="handleCreate">确定</n-button>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getGroups, getGroup, createGroup, deleteGroup, addMember, removeMember } from '@/api/groups'
import { getUsers } from '@/api/users'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NForm, NFormItem, NInput, NSelect } from 'naive-ui'

const groups = ref([])
const users = ref([])
const selectedGroup = ref(null)
const selectedMembers = ref([])
const newMemberId = ref(null)
const showCreate = ref(false)
const creating = ref(false)
const createForm = ref({ name: '', dev_lead_id: null })

const roleMap = { pm: '项目经理', dev_lead: '开发组长', dev: '开发', tester_lead: '测试组长', tester: '测试' }

const devLeadOptions = computed(() =>
  users.value.filter(u => u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id }))
)

const availableDevOptions = computed(() => {
  const existingIds = selectedMembers.value.map(m => m.id)
  return users.value
    .filter(u => u.role === 'dev' && !existingIds.includes(u.id))
    .map(u => ({ label: u.name, value: u.id }))
})

async function loadGroups() {
  try { groups.value = await getGroups() } catch (e) { console.error(e) }
}

async function loadUsers() {
  try { users.value = await getUsers() } catch (e) { console.error(e) }
}

async function selectGroup(g) {
  selectedGroup.value = g
  newMemberId.value = null
  try {
    const res = await getGroup(g.id)
    selectedMembers.value = res.members || []
    const idx = groups.value.findIndex(gr => gr.id === g.id)
    if (idx >= 0) {
      groups.value[idx].member_count = (res.members || []).length
    }
  } catch (e) { console.error(e) }
}

async function handleCreate() {
  creating.value = true
  try {
    await createGroup({ name: createForm.value.name, dev_lead_id: createForm.value.dev_lead_id })
    window.$message.success('小组创建成功')
    showCreate.value = false
    createForm.value = { name: '', dev_lead_id: null }
    await loadGroups()
  } catch (e) {
    window.$message.error('创建失败')
  } finally {
    creating.value = false
  }
}

async function confirmDelete(g) {
  if (!window.confirm(`确定删除小组「${g.name}」？成员将被移除。`)) return
  try {
    await deleteGroup(g.id)
    window.$message.success('已删除')
    if (selectedGroup.value?.id === g.id) {
      selectedGroup.value = null
      selectedMembers.value = []
    }
    await loadGroups()
  } catch (e) { window.$message.error('删除失败') }
}

async function handleAddMember() {
  if (!newMemberId.value || !selectedGroup.value) return
  try {
    await addMember(selectedGroup.value.id, { user_id: newMemberId.value })
    window.$message.success('成员已加入')
    newMemberId.value = null
    await selectGroup(selectedGroup.value)
  } catch (e) { window.$message.error('添加失败') }
}

async function handleRemoveMember(userId) {
  if (!selectedGroup.value) return
  try {
    await removeMember(selectedGroup.value.id, userId)
    window.$message.success('成员已移除')
    await selectGroup(selectedGroup.value)
  } catch (e) { window.$message.error('移除失败') }
}

onMounted(() => {
  loadGroups()
  loadUsers()
})
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

.main-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.group-list,
.member-panel {
  background: white;
  border-radius: 18px;
  padding: 16px;
  border: 1px solid #e2e8f0;
}

.list-header,
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.list-header h3,
.panel-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

.group-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.15s;
  margin-bottom: 4px;
}

.group-row:hover { background: #f8fafc; }
.group-row.active { background: #eef2ff; }

.group-name { font-size: 14px; font-weight: 600; color: #0f172a; }
.group-lead { font-size: 12px; color: #64748b; margin-top: 2px; }
.group-actions { display: flex; align-items: center; gap: 8px; }
.member-count { font-size: 12px; color: #94a3b8; }

.member-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 10px;
  border-bottom: 1px solid #f1f5f9;
}

.member-name { font-size: 14px; font-weight: 500; }
.member-role { font-size: 12px; color: #64748b; margin-left: 8px; }

.add-member-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.empty-state {
  padding: 40px 18px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.empty-panel {
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
