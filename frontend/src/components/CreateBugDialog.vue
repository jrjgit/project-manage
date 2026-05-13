<template>
  <n-modal v-model:show="show" title="创建Bug" preset="dialog" style="width: 500px;">
    <n-form ref="formRef" :model="form" :rules="rules" label-width="80">
      <n-form-item label="标题" path="title">
        <n-input v-model:value="form.title" placeholder="Bug标题" />
      </n-form-item>
      <n-form-item label="描述" path="description">
        <n-input v-model:value="form.description" type="textarea" placeholder="详细描述Bug现象、复现步骤" />
      </n-form-item>
      <n-form-item label="所属任务" path="task_id">
        <n-select v-model:value="form.task_id" :options="taskOptions" placeholder="选择关联任务" />
      </n-form-item>
      <n-form-item label="严重程度" path="severity">
        <n-select v-model:value="form.severity" :options="severityOptions" />
      </n-form-item>
      <n-form-item label="指派给" path="assignee_id">
        <n-select v-model:value="form.assignee_id" :options="devOptions" placeholder="选择修复人" />
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
import { createBug } from '@/api/bugs'
import { getTasks } from '@/api/tasks'
import { getUsers } from '@/api/users'
import { NModal, NForm, NFormItem, NInput, NSelect, NButton } from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const emit = defineEmits(['success'])

const formRef = ref(null)
const loading = ref(false)
const tasks = ref([])
const users = ref([])

const form = ref({
  title: '',
  description: '',
  task_id: null,
  severity: 'medium',
  assignee_id: null
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  task_id: [{ required: true, type: 'number', message: '请选择任务', trigger: 'change' }],
  assignee_id: [{ required: true, type: 'number', message: '请选择修复人', trigger: 'change' }]
}

const taskOptions = computed(() => tasks.value.map(t => ({ label: t.title, value: t.id })))
const devOptions = computed(() => users.value.filter(u => u.role === 'dev').map(u => ({ label: u.name, value: u.id })))
const severityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'critical' }
]

watch(show, async (val) => {
  if (val) {
    form.value = { title: '', description: '', task_id: null, severity: 'medium', assignee_id: null }
    try {
      tasks.value = await getTasks()
      users.value = await getUsers()
    } catch (e) { console.error(e) }
  }
})

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    await createBug(form.value)
    window.$message.success('创建成功')
    show.value = false
    emit('success')
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}
</script>
