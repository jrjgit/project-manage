<template>
  <n-modal v-model:show="show" title="创建任务" preset="dialog" style="width: 520px;">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80">
      <n-form-item label="标题" path="title">
        <n-input v-model:value="form.title" placeholder="请输入任务标题" />
      </n-form-item>
      <n-form-item label="描述" path="description">
        <n-input v-model:value="form.description" type="textarea" placeholder="任务描述" />
      </n-form-item>
      <n-form-item label="项目" path="project_id">
        <n-select v-model:value="form.project_id" :options="projectOptions" placeholder="选择项目" />
      </n-form-item>
      <n-form-item label="指派组长" path="dev_lead_id">
        <n-select v-model:value="form.dev_lead_id" :options="devLeadOptions" placeholder="选择开发组长" />
      </n-form-item>
      <n-form-item label="指派开发" path="assignees">
        <div class="assignee-rows">
          <div v-for="(row, idx) in form.assignees" :key="idx" class="assignee-row">
            <n-select v-model:value="row.user_id" :options="availableAssigneeOptions(idx)" placeholder="选择开发人员" size="small" style="width: 140px;" />
            <n-select v-model:value="row.platform" :options="platformOptions" placeholder="端" size="small" style="width: 110px;" />
            <n-button text size="tiny" type="error" @click="removeAssigneeRow(idx)">删除</n-button>
          </div>
          <n-button text size="small" @click="addAssigneeRow" :disabled="!form.dev_lead_id">
            <template #icon>
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:14px;height:14px"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
            </template>
            添加开发人员
          </n-button>
        </div>
      </n-form-item>
      <n-form-item label="指派测试" path="tester_id">
        <n-select v-model:value="form.tester_id" :options="testerOptions" placeholder="可选，指定测试人员" clearable />
      </n-form-item>
      <n-form-item label="所属需求">
        <n-select v-model:value="form.requirement_id" :options="requirementOptions" placeholder="可选，关联需求" clearable filterable @update:value="onRequirementChange" />
      </n-form-item>
      <n-form-item label="所属功能点">
        <n-select v-model:value="form.feature_id" :options="featureOptions" placeholder="可选，关联功能点" clearable filterable :disabled="!form.requirement_id" />
      </n-form-item>
      <n-form-item label="优先级" path="priority">
        <n-select v-model:value="form.priority" :options="priorityOptions" />
      </n-form-item>
      <n-form-item label="截止日期" path="deadline">
        <n-date-picker v-model:value="form.deadline" type="date" placeholder="选择截止日期" clearable style="width: 100%;" />
      </n-form-item>
    </n-form>
    <template #action>
      <n-button @click="show = false">取消</n-button>
      <n-button type="primary" :loading="loading" @click="submit">确定</n-button>
    </template>
  </n-modal>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { createTask } from '@/api/tasks'
import { getProjects } from '@/api/projects'
import { getUsers } from '@/api/users'
import { getGroup } from '@/api/groups'
import { getRequirements, getFeatures } from '@/api/requirements'
import { NModal, NForm, NFormItem, NInput, NSelect, NDatePicker, NButton } from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const emit = defineEmits(['success'])

const formRef = ref(null)
const loading = ref(false)
const projects = ref([])
const users = ref([])

const assigneeOptions = ref([])
const requirements = ref([])
const features = ref([])

const form = ref({
  title: '',
  description: '',
  project_id: null,
  dev_lead_id: null,
  assignees: [],
  tester_id: null,
  priority: 'medium',
  deadline: null,
  requirement_id: null,
  feature_id: null
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  project_id: [{ required: true, type: 'number', message: '请选择项目', trigger: 'change' }],
  dev_lead_id: [{ required: true, type: 'number', message: '请选择开发组长', trigger: 'change' }]
}

const projectOptions = computed(() => projects.value.map(p => ({ label: p.name, value: p.id })))
const devLeadOptions = computed(() => users.value.filter(u => u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id })))
const requirementOptions = computed(() => requirements.value.map(r => ({ label: `REQ-${String(r.id).padStart(4, '0')} ${r.title}`, value: r.id })))
const featureOptions = computed(() => features.value.map(f => ({ label: f.title, value: f.id })))
const testerOptions = computed(() => users.value.filter(u => u.role === 'tester').map(u => ({ label: u.name, value: u.id })))
const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'critical' }
]
const platformOptions = [
  { label: 'iOS', value: 'iOS' },
  { label: 'Android', value: 'Android' },
  { label: '后台', value: '后台' },
  { label: '前端', value: '前端' },
  { label: '其他', value: '其他' }
]

function availableAssigneeOptions(currentIdx) {
  const selectedIds = form.value.assignees
    .map((r, i) => i !== currentIdx ? r.user_id : null)
    .filter(Boolean)
  return assigneeOptions.value.filter(opt => !selectedIds.includes(opt.value))
}

function addAssigneeRow() {
  form.value.assignees.push({ user_id: null, platform: '' })
}

function removeAssigneeRow(idx) {
  form.value.assignees.splice(idx, 1)
}

async function onRequirementChange(reqId) {
  form.value.feature_id = null
  features.value = []
  if (!reqId) return
  try {
    features.value = await getFeatures(reqId)
  } catch (e) { console.error(e) }
}

watch(show, async (val) => {
  if (val) {
    form.value = { title: '', description: '', project_id: null, dev_lead_id: null, assignees: [], tester_id: null, priority: 'medium', deadline: null, requirement_id: null, feature_id: null }
    features.value = []
    assigneeOptions.value = []
    try {
      const [projRes, userRes, reqRes] = await Promise.all([
        getProjects(), getUsers(), getRequirements()
      ])
      projects.value = projRes
      users.value = userRes
      requirements.value = reqRes || []
    } catch (e) { console.error(e) }
  }
})

watch(() => form.value.dev_lead_id, async (devLeadId) => {
  form.value.assignees = []
  assigneeOptions.value = []
  if (!devLeadId) return

  const devLead = users.value.find(u => u.id === devLeadId)
  if (!devLead || !devLead.group_id) return

  try {
    const res = await getGroup(devLead.group_id)
    const members = res.members || []
    assigneeOptions.value = [
      { label: `${devLead.name}（组长）`, value: devLead.id },
      ...members.filter(m => m.role === 'dev').map(m => ({ label: m.name, value: m.id }))
    ]
  } catch (e) { console.error(e) }
})

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const payload = { ...form.value }
    if (payload.deadline) {
      const d = new Date(payload.deadline)
      payload.deadline = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    }
    // 过滤掉未填完整的行
    payload.assignees = (payload.assignees || []).filter(r => r.user_id)
    if (payload.assignees.length === 0) delete payload.assignees
    if (!payload.tester_id) delete payload.tester_id
    if (!payload.requirement_id) { delete payload.requirement_id; delete payload.feature_id }
    if (!payload.feature_id) delete payload.feature_id
    await createTask(payload)
    window.$message.success('创建成功')
    show.value = false
    emit('success')
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
</script>

<style scoped>
.assignee-rows {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.assignee-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
