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
      <n-form-item label="测试类型" path="test_type">
        <n-select v-model:value="form.test_type" :options="testTypeOptions" />
      </n-form-item>
      <n-form-item label="指派给" path="assignee_id">
        <n-select v-model:value="form.assignee_id" :options="bugDevOptions" placeholder="选择修复人" />
      </n-form-item>
      <n-form-item label="截图">
        <n-upload :show-file-list="false" :custom-request="handleAttachUpload" accept="image/*">
          <n-button :loading="imageUploading">选择图片</n-button>
        </n-upload>
        <span v-if="attachFileName" style="font-size:12px;color:#18a058;margin-left:8px">{{ attachFileName }}</span>
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
import { createBug, uploadBugImage } from '@/api/bugs'
import { getTasks } from '@/api/tasks'
import { getUsers } from '@/api/users'
import { NModal, NForm, NFormItem, NInput, NSelect, NButton, NUpload } from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const emit = defineEmits(['success'])

const formRef = ref(null)
const loading = ref(false)
const tasks = ref([])
const users = ref([])
const imageFile = ref(null)
const attachFileName = ref('')
const imageUploading = ref(false)

const form = ref({
  title: '',
  description: '',
  task_id: null,
  severity: 'medium',
  test_type: 'integration',
  assignee_id: null
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  task_id: [{ required: true, type: 'number', message: '请选择任务', trigger: 'change' }]
}

const taskOptions = computed(() => tasks.value.map(t => ({ label: t.title, value: t.id })))

const bugDevOptions = computed(() => {
  if (!form.value.task_id) return []
  const task = tasks.value.find(t => t.id === form.value.task_id)
  if (!task) return []
  // 优先使用 task.assignees（多开发关联表）
  if (task.assignees && task.assignees.length > 0) {
    return task.assignees.map(a => {
      const name = a.user?.name || ''
      const platform = a.platform ? `（${a.platform}端）` : ''
      return { label: `${name}${platform}`, value: a.user_id }
    })
  }
  // 兼容旧格式：从全局 dev 列表找
  if (task.assignee) {
    return [{ label: task.assignee.name, value: task.assignee.id }]
  }
  return users.value.filter(u => u.role === 'dev').map(u => ({ label: u.name, value: u.id }))
})

const severityOptions = [
  { label: '低', value: 'low' },
  { label: '中', value: 'medium' },
  { label: '高', value: 'high' },
  { label: '紧急', value: 'critical' }
]

const testTypeOptions = [
  { label: '综合测试', value: 'integration' },
  { label: '业务测试', value: 'business' },
  { label: 'IT测试', value: 'it_test' }
]

watch(show, async (val) => {
  if (val) {
    form.value = { title: '', description: '', task_id: null, severity: 'medium', test_type: 'integration', assignee_id: null }
    try {
      tasks.value = await getTasks()
      users.value = await getUsers()
    } catch (e) { console.error(e) }
  }
})

watch(() => form.value.task_id, () => {
  form.value.assignee_id = null
})

function handleAttachUpload({ file }) {
  imageFile.value = file.file
  attachFileName.value = file.name
}

async function submit() {
  await formRef.value?.validate()
  loading.value = true
  try {
    const created = await createBug(form.value)
    if (imageFile.value) {
      imageUploading.value = true
      await uploadBugImage(created.id, imageFile.value)
    }
    window.$message.success('创建成功')
    show.value = false
    emit('success')
  } catch (e) { console.error(e) }
  finally {
    loading.value = false
    imageUploading.value = false
  }
}
</script>
