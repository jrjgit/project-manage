<template>
  <n-modal v-model:show="show" title="创建任务" preset="dialog" style="width: 500px;">
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
      <n-form-item label="指派开发" path="assignee_id">
        <n-select v-model:value="form.assignee_id" :options="assigneeOptions" placeholder="可选，指定组内开发" clearable />
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
import { NModal, NForm, NFormItem, NInput, NSelect, NDatePicker, NButton } from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const emit = defineEmits(['success'])

const formRef = ref(null)
const loading = ref(false)
const projects = ref([])
const users = ref([])

const assigneeOptions = ref([])

const form = ref({
  title: '',
  description: '',
  project_id: null,
  dev_lead_id: null,
  priority: 'medium',
  deadline: null
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  project_id: [{ required: true, type: 'number', message: '请选择项目', trigger: 'change' }],
  dev_lead_id: [{ required: true, type: 'number', message: '请选择开发组长', trigger: 'change' }]
}

const projectOptions = computed(() => projects.value.map(p => ({ label: p.name, value: p.id })))
const devLeadOptions = computed(() => users.value.filter(u => u.role === 'dev_lead').map(u => ({ label: u.name, value: u.id })))
const priorityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'critical' }
]

watch(show, async (val) => {
  if (val) {
    form.value = { title: '', description: '', project_id: null, dev_lead_id: null, assignee_id: null, priority: 'medium', deadline: null }
    assigneeOptions.value = []
    try {
      projects.value = await getProjects()
      users.value = await getUsers()
    } catch (e) { console.error(e) }
  }
})

watch(() => form.value.dev_lead_id, async (devLeadId) => {
  form.value.assignee_id = null
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
    if (!payload.assignee_id) delete payload.assignee_id
    await createTask(payload)
    window.$message.success('创建成功')
    show.value = false
    emit('success')
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
</script>
