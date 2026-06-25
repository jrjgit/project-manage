<template>
  <n-drawer v-model:show="show" :width="windowWidth <= 768 ? '100%' : 780" placement="right">
    <n-drawer-content v-if="bug" closable>
      <template #header>
        <div class="drawer-header">
          <div class="drawer-kicker">BUG-{{ bug.id }}</div>
          <div class="drawer-title">{{ bug.title }}</div>
          <div class="drawer-subtitle">{{ summaryText }}</div>
        </div>
      </template>

      <div class="drawer-body">
        <section class="hero-card bug-hero">
          <div class="hero-main">
            <div class="chip-row">
              <n-tag :type="statusMeta.tone" round>{{ statusMeta.label }}</n-tag>
              <n-tag :type="severityMetaItem.tone" round>{{ severityMetaItem.label }}</n-tag>
            </div>
            <div class="next-action-title">下一步动作</div>
            <div class="next-action-copy">{{ nextActionText }}</div>
          </div>
        </section>

        <section v-if="actionGroups.length" class="section-card">
          <div class="section-title">可执行操作</div>
          <div v-for="group in actionGroups" :key="group.title" class="action-block">
            <div class="action-block-title">{{ group.title }}</div>
            <div class="action-row wrap">
              <n-button
                v-for="action in group.actions"
                :key="action.status"
                :type="action.type"
                :loading="actionLoading"
                @click="executeAction(action)"
              >
                {{ action.label }}
              </n-button>
            </div>
          </div>
        </section>

          <section class="detail-grid">
          <div class="section-card">
            <div class="section-title">Bug 摘要</div>
            <div class="info-list">
              <div class="info-item"><span>状态</span><strong><n-tag size="small" :type="statusMeta.tone" round>{{ statusMeta.label }}</n-tag></strong></div>
              <div class="info-item"><span>严重程度</span><strong><n-tag size="small" :type="severityMetaItem.tone" round>{{ severityMetaItem.label }}</n-tag></strong></div>
              <div class="info-item"><span>所属任务</span><strong>{{ bug.task?.title || '-' }}</strong></div>
              <div class="info-item"><span>所属需求</span><strong>{{ bug.requirement?.number || bug.requirement?.requirementId || '-' }}</strong></div>
              <div class="info-item"><span>所属系统</span><strong>{{ bug.requirement?.system || '-' }}</strong></div>
              <div class="info-item"><span>创建人</span><strong>{{ bug.creator?.name || '-' }}</strong></div>
              <div class="info-item"><span>当前处理人</span><strong>{{ bug.assignee?.name || '-' }}</strong></div>
              <div class="info-item"><span>创建时间</span><strong>{{ formatTime(bug.created_at) }}</strong></div>
            </div>
          </div>

          <div class="section-card">
            <div class="section-title">实际结果</div>
            <div class="description-block">{{ bug.description || '暂无描述' }}</div>
          </div>
          <div class="section-card">
            <div class="section-title">预期结果</div>
            <div class="description-block">{{ bug.expected_result || '暂无描述' }}</div>
          </div>

          <div class="section-card">
            <div class="section-title">备注</div>
            <div class="description-block">{{ bug.remark || '暂无备注' }}</div>
          </div>

          <div class="section-card">
            <div class="section-title">截图（{{ images.length }}）</div>
            <div v-if="images.length" class="image-grid">
              <div v-for="img in images" :key="img.id" class="image-item">
                <n-image :src="getImageUrl(img)" width="100%" style="border-radius:6px;max-height:180px;object-fit:contain;background:#f3f4f6" />
                <span class="image-name-label">{{ img.image_name }}</span>
                <div class="image-item-actions">
                  <n-button v-if="isCreatorOrPM" size="tiny" type="error" ghost @click="handleDeleteImage(img)">删除</n-button>
                </div>
              </div>
            </div>
            <div class="image-upload-area">
              <n-upload :show-file-list="false" :custom-request="handleImageUpload" accept="image/*">
                <n-button :loading="imageUploading" size="small">上传截图</n-button>
              </n-upload>
            </div>
          </div>
        </section>

        <section class="section-card">
          <div class="section-title">状态历史</div>
          <n-timeline class="timeline">
            <n-timeline-item
              v-for="history in histories"
              :key="history.id"
              :type="history.to_status === 'unfixed' ? 'warning' : 'default'"
            >
              <div class="timeline-title">{{ formatHistory(history) }}</div>
              <div class="timeline-meta">{{ history.user?.name || '系统' }} · {{ formatTime(history.changed_at) }}</div>
            </n-timeline-item>
          </n-timeline>
        </section>
      </div>
    </n-drawer-content>
    <n-drawer-content v-else-if="bugNotFound" closable>
      <div style="text-align:center;padding:60px 0;color:#94a3b8">
        <div style="font-size:40px;font-weight:700;color:#d03050;margin-bottom:12px">404</div>
        <div style="font-size:15px">该 Bug 已被删除</div>
      </div>
    </n-drawer-content>
  </n-drawer>

  <n-modal v-model:show="showCommentModal" title="操作备注" preset="dialog" style="width: min(92vw, 420px)">
    <n-input v-model:value="pendingComment" type="textarea" placeholder="请输入操作备注（可选）" />
    <template #action>
      <n-button @click="showCommentModal = false">取消</n-button>
      <n-button :type="pendingAction?.type || 'primary'" :loading="actionLoading" @click="confirmAction">确认</n-button>
    </template>
  </n-modal>
</template>

<script setup>
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '@/store/useAuthStore'
import { getBug, getBugHistory, changeBugStatus, uploadBugImage, getBugImages, deleteBugImageById } from '@/api/bugs'
import { bugStatusMeta, severityMeta } from '@/constants/statusMeta'
import {
  NDrawer,
  NDrawerContent,
  NTag,
  NButton,
  NTimeline,
  NTimelineItem,
  NModal,
  NInput,
  NImage,
  NUpload
} from 'naive-ui'

const show = defineModel('show', { type: Boolean, default: false })
const props = defineProps({ bugId: Number })
const emit = defineEmits(['refresh'])

const authStore = useAuthStore()

const windowWidth = ref(window.innerWidth)
function onResize() { windowWidth.value = window.innerWidth }
onMounted(() => window.addEventListener('resize', onResize))
onUnmounted(() => window.removeEventListener('resize', onResize))

const bug = ref(null)
const histories = ref([])
const actionLoading = ref(false)
const showCommentModal = ref(false)
const pendingComment = ref('')
const pendingAction = ref(null)
const images = ref([])
const imageUploading = ref(false)
const bugNotFound = ref(false)

async function handleImageUpload({ file }) {
  imageUploading.value = true
  try {
    await uploadBugImage(props.bugId, file.file)
    window.$message.success('截图上传成功')
    await loadImages()
  } catch (e) {
    console.error(e)
  } finally {
    imageUploading.value = false
  }
}

const statusMeta = computed(() => bugStatusMeta[bug.value?.status] || { label: bug.value?.status || '-', tone: 'default' })
const severityMetaItem = computed(() => severityMeta[bug.value?.severity] || { label: bug.value?.severity || '-', tone: 'default' })

const isCreatorOrPM = computed(() => {
  const role = authStore.userInfo?.role
  const myId = authStore.userInfo?.id
  return role === 'pm' || bug.value?.creator_id === myId
})

const summaryText = computed(() => {
  const parts = [bug.value?.task?.title, statusMeta.value.label]
  if (bug.value?.assignee?.name) parts.push(`当前处理人：${bug.value.assignee.name}`)
  return parts.filter(Boolean).join(' · ')
})

const nextActionMap = {
  unfixed: '开发负责人需要修复或确认为非 Bug。',
  fixed: '测试人员验证结果，确认关闭或打回未修复。',
  not_a_bug: '测试人员确认是否接受开发判定，接受则关闭。',
  pending_verify: '测试人员验证结果，确认关闭或打回未修复。',
  closed: 'Bug 已完成闭环。'
}
const nextActionText = computed(() => nextActionMap[bug.value?.status] || '查看当前状态并继续推进。')

const availableActions = computed(() => {
  const status = bug.value?.status
  const role = authStore.userInfo?.role
  const myId = authStore.userInfo?.id
  const isMyBug = bug.value?.assignee_id === myId
  const actions = []
  if (['dev', 'dev_lead'].includes(role) && isMyBug && status === 'unfixed') {
    actions.push({ label: '已修复', status: 'fixed', type: 'primary' })
    actions.push({ label: '确认为非Bug', status: 'not_a_bug', type: 'info' })
  }
  if (['tester', 'pm', 'dev', 'dev_lead'].includes(role) && ['fixed', 'not_a_bug', 'pending_verify'].includes(status)) {
    actions.push({ label: '验证通过', status: 'closed', type: 'success' })
    actions.push({ label: '未修复', status: 'unfixed', type: 'error' })
  }
  return actions
})

const actionGroups = computed(() => {
  if (!availableActions.value.length) return []
  return [{ title: '状态推进', actions: availableActions.value }]
})

watch(
  [() => props.bugId, show],
  async ([id, visible]) => {
    console.log('[BugDetailDrawer] watch fired - bugId:', id, 'show:', visible)
    if (id && visible) {
      await loadDetail()
      await loadHistory()
    }
  },
  { immediate: false }
)

async function loadDetail() {
  bugNotFound.value = false
  try {
    bug.value = await getBug(props.bugId)
    await loadImages()
  } catch (error) {
    bugNotFound.value = true
    bug.value = null
    console.error(error)
  }
}

async function loadHistory() {
  try {
    histories.value = await getBugHistory(props.bugId)
  } catch (error) {
    console.error(error)
  }
}

function executeAction(action) {
  if (actionLoading.value) return
  pendingAction.value = action
  pendingComment.value = ''
  showCommentModal.value = true
}

async function confirmAction() {
  if (actionLoading.value) return
  actionLoading.value = true
  try {
    await doChangeStatus(pendingAction.value.status, pendingComment.value)
    showCommentModal.value = false
  } finally {
    actionLoading.value = false
  }
}

async function loadImages() {
  try {
    const list = await getBugImages(props.bugId) || []
    images.value = list
  } catch (e) {
    images.value = []
  }
}

function getImageUrl(img) {
  return img?.image_path ? '/uploads/' + img.image_path : ''
}

async function handleDeleteImage(img) {
  try {
    await deleteBugImageById(props.bugId, img.id)
    images.value = images.value.filter(i => i.id !== img.id)
    window.$message.success('截图已删除')
  } catch (e) {
    window.$message.error('删除失败: ' + (e.response?.data?.error || e.message || '未知错误'))
    console.error(e)
  }
}

async function doChangeStatus(newStatus, comment) {
  actionLoading.value = true
  try {
    await changeBugStatus(props.bugId, { new_status: newStatus, comment })
    window.$message.success('状态变更成功')
    emit('refresh')
    await loadDetail()
    await loadHistory()
  } catch (error) {
    console.error(error)
  } finally {
    actionLoading.value = false
  }
}

function formatHistory(history) {
  const from = bugStatusMeta[history.from_status]?.label || history.from_status
  const to = bugStatusMeta[history.to_status]?.label || history.to_status
  return history.comment ? `${from} → ${to}（${history.comment}）` : `${from} → ${to}`
}

function formatTime(value) {
  if (!value) return ''
  const date = new Date(value)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}
</script>

<style scoped>
.drawer-header {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.drawer-kicker {
  font-size: 12px;
  color: #e11d48;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.drawer-title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.drawer-subtitle {
  font-size: 13px;
  color: #64748b;
}

.drawer-body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-card,
.section-card {
  border: 1px solid #e2e8f0;
  border-radius: 18px;
  background: white;
}

.hero-card {
  padding: 18px;
}

.bug-hero {
  background: linear-gradient(135deg, #fff1f2 0%, #ffffff 100%);
}

.chip-row {
  display: flex;
  gap: 10px;
  margin-bottom: 14px;
}

.next-action-title,
.section-title,
.action-block-title,
.reason-title {
  font-weight: 700;
  color: #0f172a;
}

.next-action-title,
.section-title {
  font-size: 15px;
}

.next-action-copy,
.description-block,
.timeline-meta {
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
}

.section-card {
  padding: 18px;
}

.action-block + .action-block {
  margin-top: 16px;
}

.action-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
}

.action-row.wrap {
  flex-wrap: wrap;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 14px;
}

.info-item {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}

.info-item span {
  color: #64748b;
}

.info-item strong {
  color: #0f172a;
  text-align: right;
}

.description-block {
  margin-top: 14px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.image-item {
  position: relative;
}

.image-item-actions {
  margin-top: 6px;
  text-align: right;
}

.image-name-label {
  display: block;
  margin-top: 4px;
  font-size: 11px;
  color: #64748b;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-upload-area {
  margin-top: 12px;
}

.reason-block {
  margin-top: 16px;
  padding: 12px 14px;
  border-radius: 14px;
}

.reason-block.success {
  background: #ecfdf5;
  color: #047857;
}

.reason-block.error {
  background: #fff1f2;
  color: #be123c;
}

.timeline {
  margin-top: 16px;
}

.timeline-title {
  font-size: 13px;
  color: #0f172a;
  font-weight: 600;
}

.timeline-meta {
  margin-top: 4px;
}

@media (max-width: 900px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .action-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
