<template>
  <AppLayout>
    <div class="detail-page">
      <!-- Back button -->
      <div class="back-row">
        <n-button text @click="$router.push('/requirements')">
          <template #icon>
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:18px;height:18px">
              <line x1="19" y1="12" x2="5" y2="12"/><polyline points="12 19 5 12 12 5"/>
            </svg>
          </template>
          返回需求列表
        </n-button>
      </div>

      <!-- Hero Card -->
      <section class="hero-card">
        <div class="hero-left">
          <div class="hero-eyebrow">REQ-{{ String(req.id || '').padStart(4, '0') }}</div>
          <h2 class="hero-title">{{ req.title }}</h2>
          <div class="hero-tags">
            <n-tag :type="statusMeta?.tone || 'default'" size="small" round>
              {{ statusMeta?.label || req.status }}
            </n-tag>
            <n-tag v-if="req.project?.name" size="small" round>{{ req.project.name }}</n-tag>
          </div>
        </div>
        <div class="hero-right">
          <div class="hero-field">
            <span class="hero-field-label">业务负责人</span>
            <n-select
              v-if="editingField === 'person_id'"
              v-model:value="req.person_id"
              :options="userOptions"
              size="small"
              style="width:160px"
              @blur="saveField('person_id'); editingField = null"
              @keyup.enter="saveField('person_id'); editingField = null"
              autofocus
            />
            <span v-else class="hero-field-value" @click="startEdit('person_id')">
              {{ req.person?.name || '点击选择' }}
            </span>
          </div>
          <div class="hero-field">
            <span class="hero-field-label">状态</span>
            <n-tag :type="statusMeta?.tone || 'default'" size="small" round>
              {{ statusMeta?.label || req.status }}
            </n-tag>
          </div>
          <div class="hero-field">
            <span class="hero-field-label">优先级</span>
            <n-select
              v-if="editingField === 'priority'"
              v-model:value="req.priority"
              :options="priorityOptions"
              size="small"
              style="width:120px"
              @blur="saveField('priority'); editingField = null"
              @keyup.enter="saveField('priority'); editingField = null"
              autofocus
            />
            <span v-else class="hero-field-value" @click="startEdit('priority')">
              {{ priorityMeta[req.priority]?.label || req.priority || '点击选择' }}
            </span>
          </div>
          <div class="hero-field">
            <span class="hero-field-label">发布迭代</span>
            <n-select
              v-if="editingField === 'iteration_id'"
              v-model:value="req.iteration_id"
              :options="iterationOptions"
              size="small"
              style="width:160px"
              clearable
              @blur="editingField = null"
              @update:value="onIterationChange"
              autofocus
            />
            <span v-else class="hero-field-value" @click="startEdit('iteration_id')">
              {{ req.iteration_id ? `迭代 #${req.iteration_id}` : '未纳入' }}
            </span>
          </div>
        </div>
      </section>

      <!-- Main content grid -->
      <div class="detail-grid">
        <!-- Left column -->
        <div class="detail-left">
          <!-- Description -->
          <section class="section-card">
            <div class="section-header">
              <h3>需求描述</h3>
            </div>
            <n-input
              v-model:value="localDescription"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 8 }"
              placeholder="添加需求描述..."
              @blur="saveDescription"
            />
          </section>

          <!-- Notes -->
          <section class="section-card">
            <div class="section-header">
              <h3>备注信息</h3>
            </div>
            <n-input
              v-model:value="localNotes"
              type="textarea"
              :autosize="{ minRows: 3, maxRows: 8 }"
              placeholder="添加备注..."
              @blur="saveNotes"
            />
          </section>
        </div>

        <!-- Right column -->
        <div class="detail-right">
          <!-- Dev Progress -->
          <section class="section-card">
            <div class="section-header">
              <h3>开发进度</h3>
            </div>
            <div v-if="terminalDevProgress.length > 0" class="progress-list">
              <div
                v-for="tp in terminalDevProgress"
                :key="tp.terminal"
                class="progress-item"
                @click="toggleTerminalTasks(tp.terminal)"
              >
                <div class="progress-item-header">
                  <div class="progress-item-left">
                    <span class="progress-terminal">{{ tp.terminal }}</span>
                    <n-tag v-if="tp.overdue_days > 0" type="error" size="tiny" round>
                      逾期 {{ tp.overdue_days }} 天
                    </n-tag>
                  </div>
                  <span class="progress-percent">{{ tp.progress }}%</span>
                </div>
                <n-progress
                  type="line"
                  :percentage="tp.progress"
                  :height="10"
                  :border-radius="5"
                  :color="tp.progress >= 100 ? '#18a058' : '#2080f0'"
                  indicator-placement="inside"
                />
                <div v-if="expandedTerminal === tp.terminal && tp.tasks?.length" class="terminal-task-list">
                  <div v-for="task in tp.tasks" :key="task.id" class="terminal-task-item">
                    <span class="terminal-task-name">{{ task.title }}</span>
                    <n-tag :type="task.status === 'done' ? 'success' : 'warning'" size="tiny" round>
                      {{ task.status === 'done' ? '已完成' : '进行中' }}
                    </n-tag>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">暂无开发进度数据</div>
          </section>

          <!-- Integration Test Progress -->
          <section class="section-card">
            <div class="section-header">
              <h3>综合测试进度</h3>
            </div>
            <div class="bug-stat-grid" @click="showIntegrationBugs = !showIntegrationBugs">
              <div class="bug-stat-item">
                <span class="bug-stat-value">{{ integrationBugStats.total }}</span>
                <span class="bug-stat-label">Bug 总数</span>
              </div>
              <div class="bug-stat-item">
                <span class="bug-stat-value" style="color:#18a058">{{ integrationBugStats.closed }}</span>
                <span class="bug-stat-label">已关闭</span>
              </div>
              <div class="bug-stat-item">
                <span class="bug-stat-value" style="color:#f59e0b">{{ integrationBugStats.fixing }}</span>
                <span class="bug-stat-label">修复中</span>
              </div>
              <div class="bug-stat-item">
                <span class="bug-stat-value" style="color:#d03050">{{ integrationBugStats.pending }}</span>
                <span class="bug-stat-label">待验证</span>
              </div>
            </div>
            <div v-if="showIntegrationBugs && integrationBugs.length" class="bug-list">
              <div v-for="bug in integrationBugs" :key="bug.id" class="bug-item">
                <span class="bug-name">{{ bug.title }}</span>
                <n-tag :type="bug.status === 'closed' ? 'success' : 'warning'" size="tiny" round>
                  {{ bug.status }}
                </n-tag>
              </div>
            </div>
          </section>

          <!-- Business Test Progress -->
          <section class="section-card">
            <div class="section-header">
              <h3>业务测试进度</h3>
            </div>
            <div class="bug-stat-grid" @click="showBusinessBugs = !showBusinessBugs">
              <div class="bug-stat-item">
                <span class="bug-stat-value">{{ businessBugStats.total }}</span>
                <span class="bug-stat-label">Bug 总数</span>
              </div>
              <div class="bug-stat-item">
                <span class="bug-stat-value" style="color:#18a058">{{ businessBugStats.closed }}</span>
                <span class="bug-stat-label">已关闭</span>
              </div>
              <div class="bug-stat-item">
                <span class="bug-stat-value" style="color:#f59e0b">{{ businessBugStats.fixing }}</span>
                <span class="bug-stat-label">修复中</span>
              </div>
              <div class="bug-stat-item">
                <span class="bug-stat-value" style="color:#d03050">{{ businessBugStats.pending }}</span>
                <span class="bug-stat-label">待验证</span>
              </div>
            </div>
            <div v-if="showBusinessBugs && businessBugs.length" class="bug-list">
              <div v-for="bug in businessBugs" :key="bug.id" class="bug-item">
                <span class="bug-name">{{ bug.title }}</span>
                <n-tag :type="bug.status === 'closed' ? 'success' : 'warning'" size="tiny" round>
                  {{ bug.status }}
                </n-tag>
              </div>
            </div>
          </section>
        </div>
      </div>

      <!-- Bottom Action Bar -->
      <section class="action-bar section-card">
        <div class="action-bar-left">
          <n-button
            v-if="authStore.isPM"
            type="primary"
            ghost
            @click="showIterationSelector = true"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/>
                <circle cx="9" cy="7" r="4"/>
                <path d="M19 8v6"/><path d="M16 11h6"/>
              </svg>
            </template>
            纳入发布清单
          </n-button>
          <n-button
            v-if="authStore.isPM && req.iteration_id"
            type="error"
            ghost
            @click="handleRemoveFromRelease"
          >
            <template #icon>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </template>
            从发布库移除
          </n-button>
        </div>
      </section>
    </div>

    <!-- Iteration Selector Modal -->
    <n-modal v-model:show="showIterationSelector" preset="card" style="width: 400px" title="选择发布迭代">
      <n-select
        v-model:value="selectedIterationId"
        :options="iterationOptions"
        placeholder="选择迭代"
        filterable
      />
      <template #footer>
        <n-space justify="end">
          <n-button @click="showIterationSelector = false">取消</n-button>
          <n-button type="primary" :loading="releasing" @click="handleAddToRelease">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/useAuthStore'
import { getUsers } from '@/api/users'
import {
  getRequirement,
  updateRequirement,
  addToRelease,
  removeFromRelease
} from '@/api/requirements'
import { requirementStatusMeta } from '@/constants/requirementMeta'
import { priorityMeta } from '@/constants/statusMeta'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NInput, NSelect, NSpace, NTag, NProgress } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const req = ref({})
const users = ref([])
const editingField = ref(null)
const localDescription = ref('')
const localNotes = ref('')

const expandedTerminal = ref(null)
const showIntegrationBugs = ref(false)
const showBusinessBugs = ref(false)
const showIterationSelector = ref(false)
const selectedIterationId = ref(null)
const releasing = ref(false)

const statusMeta = computed(() => requirementStatusMeta[req.value.status] || null)

const userOptions = computed(() =>
  users.value.map((u) => ({ label: u.name, value: u.id }))
)

const priorityOptions = Object.entries(priorityMeta).map(([value, meta]) => ({
  label: meta.label,
  value
}))

const iterationOptions = computed(() => {
  const opts = []
  for (let i = 1; i <= 20; i++) {
    opts.push({ label: `迭代 #${i}`, value: i })
  }
  return opts
})

const terminalDevProgress = computed(() => {
  if (!req.value.dev_progress?.terminals) return []
  return req.value.dev_progress.terminals.map((tp) => ({
    ...tp,
    progress: tp.progress || 0
  }))
})

const integrationBugStats = computed(() => {
  const bugs = req.value.integration_test_bugs || []
  return {
    total: bugs.length,
    closed: bugs.filter((b) => b.status === 'closed').length,
    fixing: bugs.filter((b) => b.status === 'fixing' || b.status === 'assigned').length,
    pending: bugs.filter((b) => b.status === 'pending_verify').length
  }
})

const integrationBugs = computed(() => req.value.integration_test_bugs || [])

const businessBugStats = computed(() => {
  const bugs = req.value.business_test_bugs || []
  return {
    total: bugs.length,
    closed: bugs.filter((b) => b.status === 'closed').length,
    fixing: bugs.filter((b) => b.status === 'fixing' || b.status === 'assigned').length,
    pending: bugs.filter((b) => b.status === 'pending_verify').length
  }
})

const businessBugs = computed(() => req.value.business_test_bugs || [])

function startEdit(field) {
  if (!authStore.isPM) return
  editingField.value = field
}

async function saveField(field) {
  try {
    await updateRequirement(req.value.id, { [field]: req.value[field] })
    window.$message?.success('更新成功')
  } catch (e) {
    window.$message?.error('更新失败')
  }
}

async function saveDescription() {
  if (localDescription.value === req.value.description) return
  try {
    await updateRequirement(req.value.id, { description: localDescription.value })
    req.value.description = localDescription.value
  } catch (e) {
    window.$message?.error('保存描述失败')
  }
}

async function saveNotes() {
  if (localNotes.value === req.value.notes) return
  try {
    await updateRequirement(req.value.id, { notes: localNotes.value })
    req.value.notes = localNotes.value
  } catch (e) {
    window.$message?.error('保存备注失败')
  }
}

function toggleTerminalTasks(terminal) {
  expandedTerminal.value = expandedTerminal.value === terminal ? null : terminal
}

async function onIterationChange(val) {
  req.value.iteration_id = val
  editingField.value = null
  await saveField('iteration_id')
}

async function handleAddToRelease() {
  if (!selectedIterationId.value) {
    window.$message?.warning('请选择迭代')
    return
  }
  releasing.value = true
  try {
    await addToRelease(req.value.id, selectedIterationId.value)
    req.value.iteration_id = selectedIterationId.value
    window.$message?.success('已纳入发布清单')
    showIterationSelector.value = false
  } catch (e) {
    window.$message?.error('操作失败')
  }
  releasing.value = false
}

async function handleRemoveFromRelease() {
  try {
    await removeFromRelease(req.value.id)
    req.value.iteration_id = null
    window.$message?.success('已从发布库移除')
  } catch (e) {
    window.$message?.error('操作失败')
  }
}

async function loadUsers() {
  try { users.value = await getUsers() } catch (e) {}
}

async function loadReq() {
  const id = route.params.id
  if (!id) return
  try {
    const data = await getRequirement(id)
    req.value = data
    localDescription.value = data.description || ''
    localNotes.value = data.notes || ''
  } catch (e) {
    window.$message?.error('加载需求详情失败')
  }
}

watch(
  () => route.params.id,
  (newId) => {
    if (newId) loadReq()
  }
)

onMounted(() => {
  loadReq()
  loadUsers()
})
</script>

<style scoped>
.detail-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.back-row {
  display: flex;
  align-items: center;
}

.back-row .n-button {
  color: #64748b;
  font-weight: 500;
}

/* Hero Card */
.hero-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 24px;
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
  margin-bottom: 8px;
}

.hero-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  line-height: 1.3;
}

.hero-tags {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

.hero-right {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 200px;
}

.hero-field {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hero-field-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  min-width: 60px;
}

.hero-field-value {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: background 0.15s;
}

.hero-field-value:hover {
  background: rgba(255, 255, 255, 0.1);
}

/* Detail Grid */
.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.detail-left,
.detail-right {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Section Card */
.section-card {
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
  padding: 22px;
}

.section-header {
  margin-bottom: 16px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: #0f172a;
}

/* Progress List */
.progress-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-item {
  cursor: pointer;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition: background 0.15s;
}

.progress-item:hover {
  background: #f1f5f9;
}

.progress-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-terminal {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
}

.progress-percent {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
}

.terminal-task-list {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.terminal-task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.terminal-task-name {
  font-size: 12px;
  color: #334155;
}

/* Bug Stat Grid */
.bug-stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 12px;
  cursor: pointer;
}

.bug-stat-item {
  text-align: center;
  padding: 12px 8px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
  transition: background 0.15s;
}

.bug-stat-item:hover {
  background: #eef2ff;
}

.bug-stat-value {
  display: block;
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.bug-stat-label {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #64748b;
}

.bug-list {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.bug-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 8px;
  border-radius: 8px;
  background: #fafafa;
}

.bug-name {
  font-size: 12px;
  color: #334155;
}

/* Action Bar */
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.action-bar-left {
  display: flex;
  gap: 12px;
}

.empty-state {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

@media (max-width: 1100px) {
  .hero-card {
    flex-direction: column;
  }

  .hero-right {
    width: 100%;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .bug-stat-grid {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
