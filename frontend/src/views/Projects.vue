<template>
  <AppLayout>
    <template #actions>
      <n-button v-if="authStore.isPM" type="primary" class="action-btn" @click="showCreate = true">
        <template #icon>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="width:16px;height:16px">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
        </template>
        创建项目
      </n-button>
    </template>

    <div class="page-shell">
      <section class="hero-card">
        <div>
          <div class="section-kicker">项目总览</div>
          <h2 class="hero-title">当前项目空间</h2>
          <p class="hero-subtitle">集中查看项目负责人、创建时间与可维护入口，保持视觉风格与执行页一致。</p>
        </div>
        <div class="hero-stat">
          <span class="hero-stat-value">{{ projects.length }}</span>
          <span class="hero-stat-label">项目总数</span>
        </div>
      </section>

      <div class="projects-grid">
        <div v-for="project in projects" :key="project.id" class="project-card">
          <div class="project-header">
            <div class="project-icon">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z"/>
              </svg>
            </div>
            <div class="project-actions" v-if="authStore.isPM">
              <button class="icon-btn" @click="editProject(project)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              </button>
              <button class="icon-btn danger" @click="deleteProject(project.id)">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/></svg>
              </button>
            </div>
          </div>
          <h3 class="project-name">{{ project.name }}</h3>
          <div class="project-meta">
            <span class="pm-badge">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
              {{ project.pm?.name }}
            </span>
            <span class="date-badge">{{ formatDate(project.created_at) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 创建/编辑弹窗 -->
    <n-modal v-model:show="showCreate" preset="card" style="width: 440px" :title="editingId ? '编辑项目' : '创建项目'">
      <n-form :model="form" :rules="{ name: [{ required: true, message: '请输入项目名称' }] }">
        <n-form-item label="项目名称" path="name">
          <n-input v-model:value="form.name" placeholder="输入项目名称" />
        </n-form-item>
      </n-form>
      <template #footer>
        <n-space justify="end">
          <n-button @click="showCreate = false">取消</n-button>
          <n-button type="primary" :loading="loading" @click="submit">确定</n-button>
        </n-space>
      </template>
    </n-modal>
  </AppLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getProjects, createProject, updateProject, deleteProject as apiDelete } from '@/api/projects'
import AppLayout from '@/components/AppLayout.vue'
import { NButton, NModal, NForm, NFormItem, NInput, NSpace } from 'naive-ui'

const authStore = useAuthStore()
const projects = ref([])
const showCreate = ref(false)
const loading = ref(false)
const editingId = ref(null)
const form = ref({ name: '' })

async function load() {
  try { projects.value = await getProjects() } catch (e) {}
}

function editProject(p) {
  editingId.value = p.id
  form.value = { name: p.name }
  showCreate.value = true
}

async function submit() {
  if (!form.value.name.trim()) return
  loading.value = true
  try {
    if (editingId.value) {
      await updateProject(editingId.value, { name: form.value.name })
    } else {
      await createProject({ name: form.value.name })
    }
    window.$message.success(editingId.value ? '更新成功' : '创建成功')
    showCreate.value = false
    editingId.value = null
    form.value = { name: '' }
    await load()
  } catch (e) {}
  loading.value = false
}

async function deleteProject(id) {
  try {
    await apiDelete(id)
    window.$message.success('删除成功')
    await load()
  } catch (e) {}
}

function formatDate(d) {
  if (!d) return ''
  const date = new Date(d)
  return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`
}

onMounted(load)
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

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.project-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s;
  position: relative;
  overflow: hidden;
}

.project-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #6366f1, #8b5cf6);
  opacity: 0;
  transition: opacity 0.3s;
}

.project-card:hover {
  box-shadow: 0 8px 30px rgba(0,0,0,0.08);
  transform: translateY(-2px);
}

.project-card:hover::before {
  opacity: 1;
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.project-icon {
  width: 44px;
  height: 44px;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.project-icon svg {
  width: 22px;
  height: 22px;
}

.project-actions {
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: opacity 0.2s;
}

.project-card:hover .project-actions {
  opacity: 1;
}

.icon-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  border: none;
  background: #f1f5f9;
  color: #64748b;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.icon-btn:hover {
  background: #e2e8f0;
  color: #334155;
}

.icon-btn.danger:hover {
  background: #fee2e2;
  color: #ef4444;
}

.icon-btn svg {
  width: 14px;
  height: 14px;
}

.project-name {
  font-size: 17px;
  font-weight: 600;
  color: #0f172a;
  margin-bottom: 12px;
}

.project-meta {
  display: flex;
  gap: 12px;
  align-items: center;
}

.pm-badge, .date-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #64748b;
  background: #f8fafc;
  padding: 4px 10px;
  border-radius: 20px;
}

.pm-badge svg, .date-badge svg {
  width: 13px;
  height: 13px;
}

.action-btn {
  border-radius: 10px;
  font-weight: 500;
}
</style>
