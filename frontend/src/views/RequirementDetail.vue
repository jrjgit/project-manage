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

      <!-- Header -->
      <section class="header-card">
        <div class="header-top">
          <div class="header-number">{{ req.number || `REQ-${String(req.id || '').padStart(4, '0')}` }}</div>
          <div class="header-tags">
            <n-select v-if="editingField === 'status'" v-model:value="req.status" :options="statusOptions" size="small"
              @blur="saveField('status'); editingField = null"
              @keyup.enter="saveField('status'); editingField = null" autofocus @click.stop style="width:130px" />
            <n-tag v-else :type="statusMeta?.tone || 'default'" size="small" round style="cursor:pointer" @click="startEdit('status')">{{ statusMeta?.label || req.status }}</n-tag>
            <n-tag :type="priorityMeta[req.priority]?.tone || 'default'" size="small" round>{{ priorityMeta[req.priority]?.label || req.priority }}</n-tag>
            <n-tag v-if="req.project?.name" size="small" round>{{ req.project.name }}</n-tag>
          </div>
        </div>
        <div class="header-actions">
          <n-button v-if="authStore.isPM && !req.iteration_id" size="small" type="primary" ghost @click="showIterationSelector = true">纳入发布清单</n-button>
          <n-button v-if="authStore.isPM && req.iteration_id" size="small" type="error" ghost @click="handleRemoveFromRelease">从发布库移除</n-button>
        </div>
      </section>

      <!-- Description (read-only) -->
      <section class="section-card">
        <div class="section-header"><h3>需求描述</h3></div>
        <div class="description-text">{{ req.description || '暂无描述' }}</div>
      </section>

      <!-- Basic Info -->
      <section class="section-card">
        <div class="section-header"><h3>基本信息</h3></div>
        <div class="info-grid">
          <div class="info-cell"><span class="info-label">业务负责人</span><span class="info-value">{{ req.person_name || req.person?.name || '-' }}</span></div>

          <div class="info-cell">
            <span class="info-label">项目类型</span>
            <span class="info-value">{{ req.project_type === 'project' ? '项目需求' : req.project_type === 'ops' ? '运维需求' : '-' }}</span>
          </div>
          <div class="info-cell">
            <span class="info-label">所属系统</span>
            <span class="info-value">{{ req.system || '-' }}</span>
          </div>
          <div class="info-cell editable-cell" @click="startEdit('dev_lead_id')">
            <span class="info-label">开发组长</span>
            <template v-if="editingField === 'dev_lead_id'">
              <div class="inline-edit-row" @click.stop>
                <n-select v-model:value="req.dev_lead_id" :options="devLeadOptions" size="small" style="width:140px" filterable
                  @keyup.enter="saveAssignDevLead(); editingField = null" autofocus />
                <n-button size="tiny" type="primary" @click="saveAssignDevLead(); editingField = null">确定</n-button>
                <n-button size="tiny" type="error" ghost @click="editingField = null">取消</n-button>
              </div>
            </template>
            <span v-else class="info-value edit-hint">{{ req.dev_lead?.name || '点击指派' }}</span>
          </div>
          <div class="info-cell editable-cell" @click="startEdit('priority')">
            <span class="info-label">优先级</span>
            <template v-if="editingField === 'priority'">
              <n-select v-model:value="req.priority" :options="priorityOptions" size="small"
                @blur="saveField('priority'); editingField = null"
                @keyup.enter="saveField('priority'); editingField = null" autofocus @click.stop />
            </template>
            <span v-else class="info-value edit-hint">{{ priorityMeta[req.priority]?.label || req.priority || '点击选择' }}</span>
          </div>
          <div class="info-cell editable-cell" @click="startEdit('iteration_id')">
            <span class="info-label">发布迭代</span>
            <template v-if="editingField === 'iteration_id'">
              <n-select v-model:value="req.iteration_id" :options="iterationOptions" size="small" clearable
                @blur="editingField = null"
                @update:value="onIterationChange" autofocus @click.stop />
            </template>
            <span v-else class="info-value edit-hint">{{ req.iteration_name || (req.iteration_id ? `迭代 #${req.iteration_id}` : '未纳入') }}</span>
          </div>
          <div class="info-cell editable-cell" @click="startEdit('planned_completion_time')">
            <span class="info-label">计划完成时间</span>
            <template v-if="editingField === 'planned_completion_time'">
              <n-date-picker v-model:value="localPlannedTime" type="date" size="small" clearable
                @blur="savePlannedTime(); editingField = null"
                @keyup.enter="savePlannedTime(); editingField = null" autofocus @click.stop />
            </template>
            <span v-else class="info-value edit-hint">{{ req.planned_completion_time ? formatDate2(req.planned_completion_time) : '点击设置' }}</span>
          </div>
          <div class="info-cell"><span class="info-label">开发人天</span><span class="info-value">{{ req.dev_total || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">开发单价</span><span class="info-value">{{ req.dev_price || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">测试人天</span><span class="info-value">{{ req.test_total || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">测试单价</span><span class="info-value">{{ req.test_price || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">总人天</span><span class="info-value">{{ req.total_amount || '-' }}</span></div>
          <div class="info-cell"><span class="info-label">总价</span><span class="info-value">{{ req.total_price || '-' }}</span></div>
        </div>
      </section>

      <!-- Notes -->
      <section class="section-card">
        <div class="section-header"><h3>备注信息</h3></div>
        <n-input v-model:value="localNotes" type="textarea" :autosize="{ minRows: 3, maxRows: 8 }" placeholder="添加备注..." @blur="saveNotes" />
      </section>

      <!-- Document -->
      <section class="section-card">
        <div class="section-header"><h3>需求文档</h3></div>
        <div v-if="req.document_name" class="document-row">
          <div class="document-info">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:20px;height:20px;flex-shrink:0">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>
            </svg>
            <span class="document-name">{{ req.document_name }}</span>
            <span v-if="req.document_size" class="document-size">({{ formatSize(req.document_size) }})</span>
          </div>
          <n-space>
            <n-button size="small" @click="handleDownloadDocument">下载</n-button>
            <n-button v-if="authStore.isPM" size="small" type="error" ghost @click="handleDeleteDocument">删除</n-button>
          </n-space>
        </div>
        <div v-else-if="authStore.isPM" class="document-upload">
          <n-upload :show-file-list="false" :custom-request="handleCustomUpload" accept="*/*">
            <n-button size="small" :loading="documentLoading">上传文档</n-button>
          </n-upload>
        </div>
        <div v-else class="empty-state">暂无文档</div>
      </section>

      <!-- 任务看板（按人员分组） -->
      <section v-if="authStore.isPM || authStore.isDevLead" class="section-card">
        <div class="section-header">
          <h3>开发任务进度 ({{ tasks.length }})</h3>
          <n-button type="primary" ghost round size="small" @click="openTaskDispatch">任务派发/修改</n-button>
        </div>
        <div v-if="tasksByPerson.length > 0" class="person-board">
          <div v-for="person in tasksByPerson" :key="person.id" class="person-card">
            <div class="person-card-header">{{ person.name }}</div>
            <div class="person-card-body">
              <div v-for="t in person.tasks" :key="t.id" class="person-task-row">
                <div class="person-task-top">
                  <div class="person-task-title">{{ t.title }}</div>
                  <div class="person-task-badges">
                    <n-tag v-if="calcOverdueDays(t.deadline) > 0" type="error" size="tiny" round>逾期{{ calcOverdueDays(t.deadline) }}天</n-tag>
                    <n-tag :type="taskStatusMeta[t.status]?.tone || 'default'" size="tiny" round>{{ taskStatusMeta[t.status]?.label || t.status }}</n-tag>
                  </div>
                </div>
                <div class="person-task-info">
                  <n-tag size="tiny" type="info">{{ skillsMap[t.terminal] || t.terminal }}</n-tag>
                  <span class="info-text">绩效 {{ t.performance || '-' }}</span>
                  <span class="info-text">截止 {{ t.deadline ? formatDate2(t.deadline) : '-' }}</span>
                </div>
                <n-progress v-if="t.progress != null" type="line" :percentage="t.progress" :height="5" :border-radius="3"
                  :color="t.progress >= 100 ? '#18a058' : '#6366f1'" indicator-placement="inside" />
                <div class="person-task-actions">
                  <template v-if="transferringTaskId === t.id">
                    <n-select v-model:value="t.assignee_id" :options="devOptions" size="tiny" style="width:130px" filterable />
                    <n-button size="tiny" type="primary" @click="confirmTransfer(t)">确定</n-button>
                    <n-button size="tiny" @click="cancelTransfer">取消</n-button>
                  </template>
                  <template v-else>
                    <n-button v-if="authStore.isPM || authStore.isDevLead" text size="tiny" type="warning" @click="startTransfer(t)">转让</n-button>
                    <n-button v-if="authStore.isPM || authStore.isDevLead" text size="tiny" type="error" @click="handleDeleteTask(t)">删除</n-button>
                  </template>
                </div>
              </div>
              <div v-if="!person.tasks.length" class="empty-state" style="padding:8px">暂无任务</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无任务</div>
      </section>

      <!-- Integration Test Progress -->
      <section class="section-card">
        <div class="section-header"><h3>综合测试进度</h3></div>
        <div class="bug-stat-grid" @click="showIntegrationBugs = !showIntegrationBugs">
          <div class="bug-stat-item"><span class="bug-stat-value">{{ integrationBugStats.total }}</span><span class="bug-stat-label">Bug 总数</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#18a058">{{ integrationBugStats.closed }}</span><span class="bug-stat-label">已关闭</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#f59e0b">{{ integrationBugStats.fixing }}</span><span class="bug-stat-label">修复中</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#d03050">{{ integrationBugStats.pending }}</span><span class="bug-stat-label">待验证</span></div>
        </div>
        <div v-if="showIntegrationBugs && integrationBugs.length" class="bug-list">
          <div v-for="bug in integrationBugs" :key="bug.id" class="bug-item">
            <span class="bug-name">{{ bug.title }}</span>
            <n-tag :type="bug.status === 'closed' ? 'success' : 'warning'" size="tiny" round>{{ bug.status }}</n-tag>
          </div>
        </div>
      </section>

      <!-- Business Test Progress -->
      <section class="section-card">
        <div class="section-header"><h3>业务测试进度</h3></div>
        <div class="bug-stat-grid" @click="showBusinessBugs = !showBusinessBugs">
          <div class="bug-stat-item"><span class="bug-stat-value">{{ businessBugStats.total }}</span><span class="bug-stat-label">Bug 总数</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#18a058">{{ businessBugStats.closed }}</span><span class="bug-stat-label">已关闭</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#f59e0b">{{ businessBugStats.fixing }}</span><span class="bug-stat-label">修复中</span></div>
          <div class="bug-stat-item"><span class="bug-stat-value" style="color:#d03050">{{ businessBugStats.pending }}</span><span class="bug-stat-label">待验证</span></div>
        </div>
        <div v-if="showBusinessBugs && businessBugs.length" class="bug-list">
          <div v-for="bug in businessBugs" :key="bug.id" class="bug-item">
            <span class="bug-name">{{ bug.title }}</span>
            <n-tag :type="bug.status === 'closed' ? 'success' : 'warning'" size="tiny" round>{{ bug.status }}</n-tag>
          </div>
        </div>
      </section>
    </div>

    <!-- Create Task Modal -->
    <n-modal v-model:show="showCreateTask" preset="card" style="width:90vw;max-width:1400px;height:90vh;overflow:auto" title="任务派发/修改" :mask-closable="false">
      <div class="dispatch-layout">
        <div class="dispatch-select-area">
          <div class="dispatch-select-inner">
            <div class="dispatch-left">
              <div class="dispatch-section-title">选择开发人员</div>
              <div class="dev-select-grid">
                <div v-for="opt in projectDevOptions" :key="opt.value" class="dev-select-item" :class="{ selected: createTaskForm.developer_ids.includes(opt.value) }" @click="toggleDev(opt.value)">
                  <span class="dev-select-name">{{ opt.label }}</span>
                </div>
              </div>
              <div v-if="createTaskForm.developer_ids.length === 0" class="empty-hint">请选择开发人员</div>
            </div>
            <div class="dispatch-divider"></div>
            <div class="dispatch-right">
              <div v-if="createTaskForm.developer_ids.length > 0" class="skill-area">
                <div class="dispatch-section-title">选择技能（点击技能以生成任务）</div>
                <div v-for="uid in createTaskForm.developer_ids" :key="uid" class="skill-section">
                  <div class="skill-section-title">{{ userNameMap[uid] || '未知' }}</div>
                  <div class="skill-tag-group">
                    <div v-for="skill in (userSkillMap[uid] || [])" :key="skill"
                      class="skill-tag-item"
                      :class="{ active: (selectedSkills[uid] || []).includes(skill) }"
                      @click="toggleSkill(uid, skill)">
                      <span>{{ skillsMap[skill] || skill }}</span>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="empty-hint" style="margin-top:60px">请先在左侧选择开发人员</div>
            </div>
          </div>
        </div>

        <div v-if="taskPreview.length" class="dispatch-task-area">
          <div class="dispatch-section-title">任务列表（{{ taskPreview.length }}）</div>
          <div v-for="(item, idx) in taskPreview" :key="item._key || idx" class="preview-assign">
            <div class="preview-header">
              <span class="preview-name">{{ item.name }}</span>
              <n-tag size="tiny" type="info">{{ item.skillLabel }}</n-tag>
              <span v-if="item.id" style="font-size:11px;color:#94a3b8">已有任务</span>
              <span style="flex:1"></span>
              <n-button size="tiny" type="primary" ghost @click="appendTask(item.userId, item.skill)">+ 追加</n-button>
              <n-button size="tiny" type="error" ghost @click="removeTaskItem(item)">删除</n-button>
            </div>
            <div class="preview-fields">
              <n-input v-model:value="item.description" type="textarea" :autosize="{ minRows: 1, maxRows: 3 }" placeholder="任务描述" />
              <div class="preview-row-3">
                <n-input v-model:value="item.performance" placeholder="绩效工时" size="small" />
                <n-date-picker v-model:value="item.deadline" type="date" placeholder="计划完成时间" size="small" clearable style="width:100%" />
                <n-input v-model:value="item.notes" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" placeholder="备注" />
              </div>
            </div>
          </div>
        </div>
        <div v-else-if="createTaskForm.developer_ids.length > 0" class="empty-state" style="margin-top:16px">请在上方选择技能以生成任务</div>
      </div>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreateTask = false">取消</n-button>
          <n-button type="primary" :loading="creatingTask" :disabled="!taskPreview.length" @click="handleCreateTasks">保存并关闭</n-button>
        </n-space>
      </template>
    </n-modal>

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
import { getTasks, createTask, updateTask, deleteTask } from '@/api/tasks'
import {
  getRequirement,
  updateRequirement,
  addToRelease,
  removeFromRelease,
  uploadRequirementDocument,
  downloadRequirementDocument,
  deleteRequirementDocument,
  assignDevLead
} from '@/api/requirements'
import { getIterations } from '@/api/iterations'
import { getDictionaries } from '@/api/dictionaries'
import { requirementStatusMeta } from '@/constants/requirementMeta'
import { priorityMeta, taskStatusMeta } from '@/constants/statusMeta'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NInput, NSelect, NSpace, NTag, NProgress, NUpload, NForm, NFormItem, NDatePicker, NTransfer } from 'naive-ui'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const req = ref({})
const users = ref([])
const iterations = ref([])
const editingField = ref(null)
const localDescription = ref('')
const localNotes = ref('')
const localPlannedTime = ref(null)

const tasks = ref([])
const transferringTaskId = ref(null)
const showCreateTask = ref(false)
const creatingTask = ref(false)
const createTaskForm = ref({ developer_ids: [] })
const taskPreview = ref([])
const skillsMap = ref({})

// 弹窗：每个人员已勾选的技能  { [userId]: string[] }
const selectedSkills = ref({})
// 弹窗：取消勾选后待删除的已有任务 ID
const pendingDeleteTaskIds = ref([])
// 弹窗：已有任务的缓存数据 { [userId]: { [skill]: item } }
const devTaskCache = ref({})

const devLeadOptions = computed(() => users.value.filter(u => u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id })))
const devOptions = computed(() => users.value.filter(u => u.role === 'dev' || u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id })))
const projectDevOptions = computed(() => {
  return users.value.filter(u => u.role === 'dev' || u.role === 'dev_lead').map(u => ({ label: `${u.name}${u.skills ? ' (' + u.skills.split(',').map(s => skillsMap.value[s] || s).join('、') + ')' : ''}`, value: u.id }))
})
const userNameMap = computed(() => {
  const map = {}
  for (const u of users.value) map[u.id] = u.name
  return map
})
const userSkillMap = computed(() => {
  const map = {}
  for (const u of users.value) map[u.id] = u.skills ? u.skills.split(',').filter(Boolean) : []
  return map
})

const tasksByPerson = computed(() => {
  const map = {}
  for (const t of tasks.value) {
    const uid = t.assignee?.id || t.assignee_id || t.creator?.id
    if (!uid) continue
    if (!map[uid]) map[uid] = { id: uid, name: t.assignee?.name || t.creator?.name || '未知', tasks: [] }
    map[uid].tasks.push(t)
  }
  return Object.values(map)
})

function toggleDev(id) {
  const idx = createTaskForm.value.developer_ids.indexOf(id)
  if (idx >= 0) {
    createTaskForm.value.developer_ids.splice(idx, 1)
    delete selectedSkills.value[id]
    for (const item of taskPreview.value) {
      if (item.userId === id && item.id && !pendingDeleteTaskIds.value.includes(item.id)) {
        pendingDeleteTaskIds.value.push(item.id)
      }
    }
    rebuildTaskPreview()
  } else {
    // 重新选中人员时，把之前标记删除的该人员任务恢复
    const cache = devTaskCache.value[id]
    if (cache) {
      for (const skill of Object.keys(cache)) {
        for (const c of cache[skill]) {
          if (c.id) {
            const delIdx = pendingDeleteTaskIds.value.indexOf(c.id)
            if (delIdx >= 0) pendingDeleteTaskIds.value.splice(delIdx, 1)
          }
        }
      }
    }
    createTaskForm.value.developer_ids.push(id)
    if (!selectedSkills.value[id]) selectedSkills.value[id] = []
    rebuildTaskPreview()
  }
}

async function openTaskDispatch() {
  createTaskForm.value = { developer_ids: [] }
  taskPreview.value = []
  devTaskCache.value = {}
  selectedSkills.value = {}
  pendingDeleteTaskIds.value = []

  // 从后端查询该需求的最新任务列表
  let latestTasks = []
  try {
    latestTasks = await getTasks({ requirement_id: route.params.id }) || []
  } catch (e) { console.error(e) }

  let seq = 0
  for (const t of latestTasks) {
    const uid = t.assignee?.id || t.assignee_id
    const skill = t.terminal
    if (!uid || !skill) continue
    const u = users.value.find(x => x.id === uid)
    if (!u) continue

    if (!devTaskCache.value[uid]) devTaskCache.value[uid] = {}
    if (!devTaskCache.value[uid][skill]) devTaskCache.value[uid][skill] = []
    const _key = uid + '-' + skill + '-' + (seq++)
    devTaskCache.value[uid][skill].push({
      _key,
      id: t.id,
      userId: uid,
      name: t.assignee?.name || u.name,
      skill,
      skillLabel: skillsMap.value[skill] || skill,
      description: t.title || t.description || req.value.description || '',
      performance: t.performance || '',
      deadline: t.deadline ? new Date(t.deadline).getTime() : null,
      notes: t.description || ''
    })

    if (!createTaskForm.value.developer_ids.includes(uid)) {
      createTaskForm.value.developer_ids.push(uid)
    }
    if (!selectedSkills.value[uid]) selectedSkills.value[uid] = []
    if (!selectedSkills.value[uid].includes(skill)) {
      selectedSkills.value[uid].push(skill)
    }
  }

  rebuildTaskPreview()
  showCreateTask.value = true
}
const showIntegrationBugs = ref(false)
const showBusinessBugs = ref(false)
const showIterationSelector = ref(false)
const selectedIterationId = ref(null)
const releasing = ref(false)
const documentLoading = ref(false)

const statusMeta = computed(() => requirementStatusMeta[req.value.status] || null)

const userOptions = computed(() =>
  users.value.map((u) => ({ label: u.name, value: u.id }))
)

const priorityOptions = Object.entries(priorityMeta).map(([value, meta]) => ({
  label: meta.label,
  value
}))
const statusOptions = Object.entries(requirementStatusMeta).map(([value, meta]) => ({
  label: meta.label,
  value
}))

const iterationOptions = computed(() => {
  return iterations.value.map((it) => ({ label: it.name, value: String(it.id) }))
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

async function savePlannedTime() {
  try {
    await updateRequirement(req.value.id, { planned_completion_time: localPlannedTime.value ? new Date(localPlannedTime.value).toISOString() : null })
    req.value.planned_completion_time = localPlannedTime.value ? new Date(localPlannedTime.value).toISOString() : null
  } catch (e) { window.$message?.error('保存失败') }
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

function calcOverdueDays(deadline) {
  if (!deadline) return 0
  const now = new Date()
  const end = new Date(deadline)
  const diff = Math.floor((now - end) / (1000 * 60 * 60 * 24))
  return diff > 0 ? diff : 0
}

function openTaskDetail(taskId) {
  router.push(`/tasks?taskId=${taskId}`)
}

function formatSize(bytes) {
  if (!bytes) return '0 B'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

function formatDate2(d) {
  if (!d) return ''
  const date = new Date(d)
  if (isNaN(date.getTime())) return d
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

async function handleCustomUpload({ file, onFinish, onError }) {
  documentLoading.value = true
  try {
    await uploadRequirementDocument(req.value.id, file.file)
    window.$message?.success('上传成功')
    await loadReq()
    onFinish()
  } catch (e) {
    window.$message?.error('上传失败')
    onError()
  }
  documentLoading.value = false
}

async function handleDownloadDocument() {
  try {
    const res = await downloadRequirementDocument(req.value.id)
    const blob = new Blob([res])
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = req.value.document_name || 'document'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    window.$message?.error('下载失败')
  }
}

async function handleDeleteDocument() {
  try {
    await deleteRequirementDocument(req.value.id)
    window.$message?.success('删除成功')
    req.value.document_name = null
    req.value.document_path = null
    req.value.document_size = null
  } catch (e) {
    window.$message?.error('删除失败')
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
async function loadSkills() {
  try {
    const data = await getDictionaries('skill')
    const map = {}
    for (const s of data) map[s.dict_key] = s.dict_value
    skillsMap.value = map
  } catch (e) {}
}

async function loadIterations() {
  try {
    const data = await getIterations()
    iterations.value = data || []
  } catch (e) {}
}

async function saveAssignDevLead() {
  if (!req.value.dev_lead_id) return
  try {
    await assignDevLead(req.value.id, req.value.dev_lead_id)
    window.$message?.success('开发组长已指派')
    await loadReq()
  } catch (e) {
    window.$message?.error('指派失败')
  }
}

async function loadTasks() {
  const id = route.params.id
  if (!id) return
  try {
    const data = await getTasks({ requirement_id: id })
    tasks.value = data || []
  } catch (e) { console.error(e) }
}

function rebuildTaskPreview() {
  // 先将当前编辑内容同步到缓存
  for (const item of taskPreview.value) {
    const uid = item.userId
    const skill = item.skill
    if (!uid || !skill || !item._key) continue
    if (!devTaskCache.value[uid]) devTaskCache.value[uid] = {}
    if (!devTaskCache.value[uid][skill]) devTaskCache.value[uid][skill] = []
    const idx = devTaskCache.value[uid][skill].findIndex(c => c._key === item._key)
    if (idx >= 0) {
      devTaskCache.value[uid][skill][idx] = { ...item }
    } else {
      devTaskCache.value[uid][skill].push({ ...item })
    }
  }

  // 从缓存重建已勾选技能的预览项
  const result = []
  let seq = 0
  for (const uid of createTaskForm.value.developer_ids) {
    const skills = selectedSkills.value[uid] || []
    const u = users.value.find(x => x.id === uid)
    if (!u) continue
    for (const skill of skills) {
      const cachedList = devTaskCache.value[uid]?.[skill] || []
      if (cachedList.length > 0) {
        result.push(...cachedList.map(c => ({ ...c })))
      } else {
        const _key = uid + '-' + skill + '-' + (seq++)
        result.push({
          _key,
          userId: uid,
          name: u.name,
          skill,
          skillLabel: skillsMap.value[skill] || skill,
          description: req.value.description || '',
          performance: '',
          deadline: null,
          notes: ''
        })
      }
    }
  }
  taskPreview.value = result
}

function toggleSkill(uid, skill) {
  if (!selectedSkills.value[uid]) selectedSkills.value[uid] = []
  const idx = selectedSkills.value[uid].indexOf(skill)
  if (idx >= 0) {
    const currentItems = taskPreview.value.filter(p => p.userId === uid && p.skill === skill)
    for (const item of currentItems) {
      if (item.id && !pendingDeleteTaskIds.value.includes(item.id)) {
        pendingDeleteTaskIds.value.push(item.id)
      }
    }
    selectedSkills.value[uid].splice(idx, 1)
    rebuildTaskPreview()
  } else {
    const cachedList = devTaskCache.value[uid]?.[skill] || []
    for (const c of cachedList) {
      if (c.id) {
        const delIdx = pendingDeleteTaskIds.value.indexOf(c.id)
        if (delIdx >= 0) pendingDeleteTaskIds.value.splice(delIdx, 1)
      }
    }
    selectedSkills.value[uid].push(skill)
    rebuildTaskPreview()
  }
}

function appendTask(uid, skill) {
  const u = users.value.find(x => x.id === uid)
  if (!u) return
  const existing = taskPreview.value.filter(p => p.userId === uid && p.skill === skill)
  const seq = existing.length
  const _key = uid + '-' + skill + '-' + Date.now() + '-' + seq

  const newItem = {
    _key,
    userId: uid,
    name: u.name,
    skill,
    skillLabel: skillsMap.value[skill] || skill,
    description: req.value.description || '',
    performance: '',
    deadline: null,
    notes: ''
  }

  if (!devTaskCache.value[uid]) devTaskCache.value[uid] = {}
  if (!devTaskCache.value[uid][skill]) devTaskCache.value[uid][skill] = []
  devTaskCache.value[uid][skill].push({ ...newItem })
  taskPreview.value.push(newItem)
}

function removeTaskItem(item) {
  const idx = taskPreview.value.findIndex(p => p._key === item._key)
  if (idx < 0) return
  if (item.id && !pendingDeleteTaskIds.value.includes(item.id)) {
    pendingDeleteTaskIds.value.push(item.id)
  }
  taskPreview.value.splice(idx, 1)
}

async function handleCreateTasks() {
  if (!taskPreview.value.length) return
  creatingTask.value = true
  let success = 0

  // 先删除被取消勾选的已有任务
  for (const id of pendingDeleteTaskIds.value) {
    try { await deleteTask(id); success++ } catch (e) { console.error(e) }
  }

  for (const item of taskPreview.value) {
    try {
      const payload = {
        title: item.description || req.value.description || '任务',
        description: item.notes || undefined,
        requirement_id: req.value.id,
        project_id: req.value.project_id,
        dev_lead_id: req.value.dev_lead_id,
        assignee_id: item.userId,
        terminal: item.skill,
        performance: item.performance || undefined,
        deadline: item.deadline ? new Date(item.deadline).toISOString() : undefined
      }
      if (item.id) {
        await updateTask(item.id, payload)
      } else {
        await createTask(payload)
      }
      success++
    } catch (e) { console.error(e) }
  }
  window.$message?.success(`保存成功 ${success}/${taskPreview.value.length} 条`)
  showCreateTask.value = false
  createTaskForm.value = { developer_ids: [] }
  taskPreview.value = []
  devTaskCache.value = {}
  selectedSkills.value = {}
  pendingDeleteTaskIds.value = []
  creatingTask.value = false
  await loadTasks()
}

function startTransfer(t) {
  transferringTaskId.value = t.id
}

function cancelTransfer() {
  transferringTaskId.value = null
}

async function confirmTransfer(t) {
  if (!t.assignee_id) { window.$message?.warning('请选择开发人员'); return }
  try {
    await updateTask(t.id, { assignee_id: t.assignee_id })
    window.$message?.success('开发人员已转让')
    transferringTaskId.value = null
    await loadTasks()
  } catch (e) { window.$message?.error('转让失败') }
}

async function handleDeleteTask(t) {
  window.$dialog?.warning({
    title: '确认删除',
    content: `确定删除任务吗？`,
    positiveText: '确定', negativeText: '取消',
    onPositiveClick: async () => {
      try { await deleteTask(t.id); window.$message?.success('已删除'); await loadTasks() }
      catch (e) { window.$message?.error('删除失败') }
    }
  })
}

async function loadReq() {
  const id = route.params.id
  if (!id) return
  try {
    const data = await getRequirement(id)
    req.value = data
    localDescription.value = data.description || ''
    localNotes.value = data.notes || ''
    localPlannedTime.value = data.planned_completion_time ? new Date(data.planned_completion_time).getTime() : null
    await loadTasks()
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
  loadIterations()
  loadSkills()
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

/* Header Card */
.header-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  background: white;
  border-radius: 20px;
  border: 1px solid #e2e8f0;
}

.header-top {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.header-number {
  font-size: 20px;
  font-weight: 700;
  color: #6366f1;
  letter-spacing: 0.02em;
}

.header-tags {
  display: flex;
  gap: 8px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

/* Description text (read-only) */
.description-text {
  font-size: 14px;
  line-height: 1.8;
  color: #334155;
  white-space: pre-wrap;
}

/* Info grid */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1fr;
  gap: 8px;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 1px;
  padding: 6px 10px;
  background: #f8fafc;
  border-radius: 8px;
}

.info-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}

.info-value {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

.editable-cell {
  cursor: pointer;
  transition: background 0.15s;
}

.editable-cell:hover {
  background: #eef2ff;
}

.edit-hint {
  color: #6366f1 !important;
}

.inline-edit-row {
  display: flex;
  align-items: center;
  gap: 6px;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
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


.preview-list {
  margin-top: 8px;
  padding: 12px;
  background: #f8fafc;
  border-radius: 10px;
}

.preview-title {
  font-size: 12px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 8px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  font-size: 13px;
}

.preview-assign {
  padding: 10px;
  background: white;
  border-radius: 10px;
  border: 1px solid #e2e8f0;
  margin-bottom: 8px;
}

.preview-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  font-size: 13px;
}

.preview-name {
  font-weight: 600;
  color: #0f172a;
}

.preview-fields {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.preview-row-3 {
  display: grid;
  grid-template-columns: 80px 140px 1fr;
  gap: 8px;
  align-items: start;
}

.empty-state {
  padding: 20px;
  text-align: center;
  font-size: 13px;
  color: #94a3b8;
}

.document-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #f8fafc;
  border: 1px solid #f1f5f9;
}

.document-info {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.document-name {
  font-size: 13px;
  font-weight: 500;
  color: #0f172a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.document-size {
  font-size: 12px;
  color: #94a3b8;
  white-space: nowrap;
}

/* Person Board */
.person-board {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.person-card {
  flex: 1;
  min-width: 240px;
  max-width: 320px;
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  overflow: hidden;
}

.person-card-header {
  padding: 10px 14px;
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
  background: linear-gradient(135deg, #eef2ff 0%, #f8fafc 100%);
  border-bottom: 1px solid #e2e8f0;
}

.person-card-body {
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.person-task-row {
  padding: 10px;
  background: white;
  border-radius: 8px;
  border: 1px solid #f1f5f9;
}

.person-task-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 6px;
  margin-bottom: 4px;
}

.person-task-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.person-task-badges {
  display: flex;
  gap: 4px;
  flex-shrink: 0;
}

.person-task-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: #64748b;
  margin-bottom: 4px;
  flex-wrap: wrap;
}

.info-text {
  color: #64748b;
  white-space: nowrap;
}

.person-task-row .n-progress {
  margin-bottom: 4px;
}

.person-task-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 2px;
}



.info-label {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
}

.info-value {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

@media (max-width: 1100px) {
  .header-card {
    flex-direction: column;
    align-items: stretch;
  }

  .info-grid {
    grid-template-columns: 1fr 1fr;
  }

  .bug-stat-grid {
    grid-template-columns: 1fr 1fr;
  }
}

.dev-select-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dev-select-item {
  padding: 6px 14px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  color: #334155;
  background: white;
  transition: all 0.15s;
  user-select: none;
}

.dev-select-item:hover {
  border-color: #6366f1;
  background: #eef2ff;
}

.dev-select-item.selected {
  border-color: #6366f1;
  background: #6366f1;
  color: white;
}

.dispatch-layout {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 300px;
}
.dispatch-select-area {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
  background: #fafbfc;
}
.dispatch-select-inner {
  display: flex;
  gap: 24px;
}
.dispatch-divider {
  width: 1px;
  flex-shrink: 0;
  background: #e2e8f0;
}
.dispatch-left {
  width: 220px;
  flex-shrink: 0;
}
.dispatch-right {
  flex: 1;
  min-width: 0;
}
.dispatch-task-area {
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  padding: 20px;
}
.dispatch-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 12px;
}
.empty-hint {
  margin-top: 12px;
  font-size: 12px;
  color: #94a3b8;
  text-align: center;
}
.skill-area {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.skill-section {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px;
}
.skill-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 8px;
}
.skill-tag-group {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}
.skill-tag-item {
  padding: 4px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  cursor: pointer;
  font-size: 12px;
  color: #334155;
  background: white;
  transition: all 0.15s;
  user-select: none;
}
.skill-tag-item:hover {
  border-color: #6366f1;
  background: #eef2ff;
}
.skill-tag-item.active {
  border-color: #6366f1;
  background: #6366f1;
  color: white;
}
</style>
