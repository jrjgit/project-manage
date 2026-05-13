# Group Management Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add team/group management so PMs can create groups, assign dev leads and members, and directly assign tasks to specific developers at creation time.

**Architecture:** New Group model with GORM, Group CRUD handler following existing ProjectHandler pattern, frontend Groups management page following Users.vue pattern. Task creation extended with optional assignee_id. Existing workflow state machine unchanged.

**Tech Stack:** Go 1.21 + Gin + GORM + SQLite backend, Vue 3 + Vite + Pinia + Naive UI frontend

---

## File Structure

| File | Action | Responsibility |
|------|--------|----------------|
| `backend/models/group.go` | Create | Group GORM model |
| `backend/dto/dto.go` | Modify | Add Group DTOs + extend CreateTaskRequest |
| `backend/db/database.go` | Modify | Add Group to AutoMigrate |
| `backend/handlers/group.go` | Create | Group CRUD + member management + MyTeam |
| `backend/handlers/task.go` | Modify | Handle assignee_id in CreateTask |
| `backend/main.go` | Modify | Register Group routes |
| `frontend/src/api/groups.js` | Create | Group API wrappers |
| `frontend/src/views/Groups.vue` | Create | PM group management page |
| `frontend/src/router/index.js` | Modify | Add /groups route |
| `frontend/src/components/AppLayout.vue` | Modify | Add nav entry + GroupsIcon |
| `frontend/src/components/CreateTaskDialog.vue` | Modify | Add assignee dropdown |
| `frontend/src/views/Dashboard.vue` | Modify | Add My Team for dev_lead |

---

### Task 1: Create Group model

**Files:**
- Create: `backend/models/group.go`

- [ ] **Step 1: Write Group model**

```go
package models

import "time"

// Group 小组模型
type Group struct {
	ID        uint      `json:"id" gorm:"primaryKey"`
	Name      string    `json:"name" gorm:"not null"`
	PMID      uint      `json:"pm_id" gorm:"not null;index"`
	PM        User      `json:"pm,omitempty" gorm:"foreignKey:PMID"`
	DevLeadID uint      `json:"dev_lead_id" gorm:"not null;index"`
	DevLead   User      `json:"dev_lead,omitempty" gorm:"foreignKey:DevLeadID"`
	CreatedAt time.Time `json:"created_at"`
}
```

- [ ] **Step 2: Add Group to AutoMigrate**

In `backend/db/database.go`, add `&models.Group{}` to the AutoMigrate call:

```go
return DB.AutoMigrate(
	&models.User{},
	&models.Project{},
	&models.Task{},
	&models.Bug{},
	&models.TaskStatusHistory{},
	&models.BugStatusHistory{},
	&models.Group{},
)
```

- [ ] **Step 3: Verify compilation**

```bash
cd backend && go build ./...
```

Expected: builds without errors

---

### Task 2: Add Group DTOs and extend CreateTaskRequest

**Files:**
- Modify: `backend/dto/dto.go`

- [ ] **Step 1: Add Group DTOs at end of dto.go**

```go
// ========== Group DTOs ==========

type CreateGroupRequest struct {
	Name      string `json:"name" binding:"required"`
	DevLeadID uint   `json:"dev_lead_id" binding:"required"`
}

type UpdateGroupRequest struct {
	Name      string `json:"name" binding:"required"`
	DevLeadID uint   `json:"dev_lead_id"`
}

type AddMemberRequest struct {
	UserID uint `json:"user_id" binding:"required"`
}
```

- [ ] **Step 2: Add AssigneeID to CreateTaskRequest**

Change `CreateTaskRequest` from:
```go
type CreateTaskRequest struct {
	Title       string     `json:"title" binding:"required"`
	Description string     `json:"description"`
	Priority    string     `json:"priority" binding:"oneof=low medium high critical"`
	ProjectID   uint       `json:"project_id" binding:"required"`
	DevLeadID   uint       `json:"dev_lead_id" binding:"required"`
	Deadline    *string    `json:"deadline"`
}
```
To:
```go
type CreateTaskRequest struct {
	Title       string  `json:"title" binding:"required"`
	Description string  `json:"description"`
	Priority    string  `json:"priority" binding:"oneof=low medium high critical"`
	ProjectID   uint    `json:"project_id" binding:"required"`
	DevLeadID   uint    `json:"dev_lead_id" binding:"required"`
	AssigneeID  *uint   `json:"assignee_id"`
	Deadline    *string `json:"deadline"`
}
```

- [ ] **Step 3: Verify compilation**

```bash
cd backend && go build ./...
```

---

### Task 3: Create GroupHandler

**Files:**
- Create: `backend/handlers/group.go`

- [ ] **Step 1: Write GroupHandler with all methods**

```go
package handlers

import (
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"management/db"
	"management/dto"
	"management/models"
)

type GroupHandler struct{}

func NewGroupHandler() *GroupHandler {
	return &GroupHandler{}
}

// ListGroups 列出所有组（含组长和成员计数）
func (h *GroupHandler) ListGroups(c *gin.Context) {
	var groups []models.Group
	if err := db.DB.Preload("DevLead").Find(&groups).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	type GroupWithCount struct {
		models.Group
		MemberCount int64 `json:"member_count"`
	}

	var result []GroupWithCount
	for _, g := range groups {
		var count int64
		db.DB.Model(&models.User{}).Where("group_id = ? AND role = ?", g.ID, models.RoleDev).Count(&count)
		result = append(result, GroupWithCount{
			Group:       g,
			MemberCount: count,
		})
	}

	c.JSON(http.StatusOK, result)
}

// CreateGroup 创建组（仅PM）
func (h *GroupHandler) CreateGroup(c *gin.Context) {
	var req dto.CreateGroupRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	userID := c.GetUint("userID")

	group := models.Group{
		Name:      req.Name,
		PMID:      userID,
		DevLeadID: req.DevLeadID,
	}

	if err := db.DB.Create(&group).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	// 将组长的 group_id 设为此组
	db.DB.Model(&models.User{}).Where("id = ?", req.DevLeadID).Update("group_id", group.ID)

	c.JSON(http.StatusCreated, group)
}

// GetGroup 获取组详情 + 成员列表
func (h *GroupHandler) GetGroup(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	var group models.Group
	if err := db.DB.Preload("DevLead").First(&group, id).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "group not found"})
		return
	}

	var members []models.User
	db.DB.Where("group_id = ?", id).Find(&members)

	c.JSON(http.StatusOK, gin.H{
		"group":   group,
		"members": members,
	})
}

// UpdateGroup 编辑组信息（仅PM）
func (h *GroupHandler) UpdateGroup(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	var req dto.UpdateGroupRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	updates := map[string]interface{}{
		"name":         req.Name,
		"dev_lead_id":  req.DevLeadID,
	}

	// 如果更换了组长，更新新旧组长的 group_id
	var oldGroup models.Group
	if err := db.DB.First(&oldGroup, id).Error; err == nil {
		if oldGroup.DevLeadID != req.DevLeadID {
			db.DB.Model(&models.User{}).Where("id = ?", oldGroup.DevLeadID).Update("group_id", nil)
			db.DB.Model(&models.User{}).Where("id = ?", req.DevLeadID).Update("group_id", uint(id))
		}
	}

	if err := db.DB.Model(&models.Group{}).Where("id = ?", id).Updates(updates).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "group updated"})
}

// DeleteGroup 删除组（清空成员 group_id）
func (h *GroupHandler) DeleteGroup(c *gin.Context) {
	id, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	// 清空所有成员的 group_id
	db.DB.Model(&models.User{}).Where("group_id = ?", id).Update("group_id", nil)

	if err := db.DB.Delete(&models.Group{}, id).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "group deleted"})
}

// AddMember 添加成员
func (h *GroupHandler) AddMember(c *gin.Context) {
	groupID, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	var req dto.AddMemberRequest
	if err := c.ShouldBindJSON(&req); err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": err.Error()})
		return
	}

	// 验证组存在
	if err := db.DB.First(&models.Group{}, groupID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "group not found"})
		return
	}

	// 验证用户存在且为 dev 角色
	var user models.User
	if err := db.DB.First(&user, req.UserID).Error; err != nil {
		c.JSON(http.StatusNotFound, gin.H{"error": "user not found"})
		return
	}

	// 设置用户的 group_id
	if err := db.DB.Model(&user).Update("group_id", uint(groupID)).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "member added"})
}

// RemoveMember 移除成员
func (h *GroupHandler) RemoveMember(c *gin.Context) {
	groupID, err := strconv.ParseUint(c.Param("id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid group id"})
		return
	}

	userID, err := strconv.ParseUint(c.Param("user_id"), 10, 64)
	if err != nil {
		c.JSON(http.StatusBadRequest, gin.H{"error": "invalid user id"})
		return
	}

	// 清空该用户的 group_id（仅当该用户确实在此组中）
	if err := db.DB.Model(&models.User{}).Where("id = ? AND group_id = ?", userID, uint(groupID)).Update("group_id", nil).Error; err != nil {
		c.JSON(http.StatusInternalServerError, gin.H{"error": err.Error()})
		return
	}

	c.JSON(http.StatusOK, gin.H{"message": "member removed"})
}

// GetMyTeam 开发组长查看自己的组员
func (h *GroupHandler) GetMyTeam(c *gin.Context) {
	userID := c.GetUint("userID")

	// 查找当前用户作为组长的 Group
	var group models.Group
	if err := db.DB.Where("dev_lead_id = ?", userID).First(&group).Error; err != nil {
		c.JSON(http.StatusOK, gin.H{
			"group":   nil,
			"members": []models.User{},
		})
		return
	}

	var members []models.User
	db.DB.Where("group_id = ?", group.ID).Find(&members)

	c.JSON(http.StatusOK, gin.H{
		"group":   group,
		"members": members,
	})
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd backend && go build ./...
```

---

### Task 4: Modify CreateTask handler to support assignee_id

**Files:**
- Modify: `backend/handlers/task.go:324-361`

- [ ] **Step 1: Update task struct construction in CreateTask**

In `CreateTask`, change the `task` construction:

```go
task := models.Task{
	Title:       req.Title,
	Description: req.Description,
	Status:      models.TaskPending,
	Priority:    req.Priority,
	ProjectID:   req.ProjectID,
	CreatorID:   userID,
	DevLeadID:   &req.DevLeadID,
	Deadline:    deadline,
}
// 如果 PM 指定了 assignee_id，创建时直接填入
if req.AssigneeID != nil {
	task.AssigneeID = req.AssigneeID
}
```

- [ ] **Step 2: Verify compilation**

```bash
cd backend && go build ./...
```

---

### Task 5: Register Group routes in main.go

**Files:**
- Modify: `backend/main.go`

- [ ] **Step 1: Add GroupHandler initialization**

After `projectHandler := handlers.NewProjectHandler()`:

```go
groupHandler := handlers.NewGroupHandler()
```

- [ ] **Step 2: Add Group routes**

After the project routes block, add:

```go
// 小组
authorized.GET("/groups", groupHandler.ListGroups)
authorized.POST("/groups", middlewares.RoleRequired(models.RolePM), groupHandler.CreateGroup)
authorized.GET("/groups/my-team", middlewares.RoleRequired(models.RoleDevLead), groupHandler.GetMyTeam)
authorized.GET("/groups/:id", groupHandler.GetGroup)
authorized.PUT("/groups/:id", middlewares.RoleRequired(models.RolePM), groupHandler.UpdateGroup)
authorized.DELETE("/groups/:id", middlewares.RoleRequired(models.RolePM), groupHandler.DeleteGroup)
authorized.POST("/groups/:id/members", middlewares.RoleRequired(models.RolePM), groupHandler.AddMember)
authorized.DELETE("/groups/:id/members/:user_id", middlewares.RoleRequired(models.RolePM), groupHandler.RemoveMember)
```

Important: `my-team` route must be registered BEFORE `:id` route to avoid `my-team` being captured as an `:id` parameter.

- [ ] **Step 3: Verify compilation**

```bash
cd backend && go build ./...
```

---

### Task 6: Create frontend Groups API module

**Files:**
- Create: `frontend/src/api/groups.js`

- [ ] **Step 1: Write groups.js**

```js
import request from './request'

export const getGroups = () => request.get('/groups')
export const getGroup = (id) => request.get(`/groups/${id}`)
export const createGroup = (data) => request.post('/groups', data)
export const updateGroup = (id, data) => request.put(`/groups/${id}`, data)
export const deleteGroup = (id) => request.delete(`/groups/${id}`)
export const addMember = (groupId, data) => request.post(`/groups/${groupId}/members`, data)
export const removeMember = (groupId, userId) => request.delete(`/groups/${groupId}/members/${userId}`)
export const getMyTeam = () => request.get('/groups/my-team')
```

---

### Task 7: Create Groups management page

**Files:**
- Create: `frontend/src/views/Groups.vue`

- [ ] **Step 1: Write Groups.vue**

```vue
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
    // 更新列表中的 member_count
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
```

---

### Task 8: Add /groups route

**Files:**
- Modify: `frontend/src/router/index.js`

- [ ] **Step 1: Add Groups route**

After the `/users` route block, add:

```js
  {
    path: '/groups',
    name: 'Groups',
    component: () => import('@/views/Groups.vue'),
    meta: { requiresAuth: true, requiresPM: true }
  }
```

---

### Task 9: Add Groups nav entry in AppLayout

**Files:**
- Modify: `frontend/src/components/AppLayout.vue`

- [ ] **Step 1: Add navigation menu item**

In `allMenus` array, after the `users` menu item, add:

```js
  { key: 'groups', label: '小组管理', path: '/groups', icon: GroupIcon, pmOnly: true }
```

- [ ] **Step 2: Add pageMeta for groups**

In the `pageMeta` computed's `map` object, add:

```js
    groups: {
      title: '小组管理',
      subtitle: '创建和管理开发小组，分配组长与成员。'
    }
```

- [ ] **Step 3: Add GroupIcon SVG component**

After the `UsersIcon` function, add:

```js
function GroupIcon(props) {
  return h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '2', ...props }, [
    h('rect', { x: '2', y: '3', width: '20', height: '18', rx: '2' }),
    h('path', { d: 'M8 8h8' }),
    h('path', { d: 'M8 12h8' }),
    h('path', { d: 'M8 16h5' })
  ])
}
```

---

### Task 10: Add assignee dropdown to CreateTaskDialog

**Files:**
- Modify: `frontend/src/components/CreateTaskDialog.vue`

- [ ] **Step 1: Add assignee select to template**

After the `dev_lead_id` form item, add:

```html
      <n-form-item label="指派开发" path="assignee_id">
        <n-select v-model:value="form.assignee_id" :options="assigneeOptions" placeholder="可选，指定组内开发" clearable />
      </n-form-item>
```

- [ ] **Step 2: Add script logic**

In the `<script setup>` block:

Add import for `getGroup`:
```js
import { getGroup } from '@/api/groups'
```

Add reactive state:
```js
const assigneeOptions = ref([])
```

Add watcher for dev_lead_id change — add this after the existing `watch(show, ...)` block:

```js
watch(() => form.value.dev_lead_id, async (devLeadId) => {
  form.value.assignee_id = null
  assigneeOptions.value = []
  if (!devLeadId) return

  // 找到该组长所在的组
  const devLead = users.value.find(u => u.id === devLeadId)
  if (!devLead || !devLead.group_id) return

  try {
    const res = await getGroup(devLead.group_id)
    const members = res.members || []
    assigneeOptions.value = [
      // 包含组长自己（自分配选项）
      ...(devLead ? [{ label: `${devLead.name}（组长）`, value: devLead.id }] : []),
      // 包含组内 dev
      ...members.filter(m => m.role === 'dev').map(m => ({ label: m.name, value: m.id }))
    ]
  } catch (e) { console.error(e) }
})
```

Update form reset in `watch(show, ...)`:
```js
    form.value = { title: '', description: '', project_id: null, dev_lead_id: null, assignee_id: null, priority: 'medium', deadline: null }
```

Update payload building in `submit()`:
```js
    const payload = { ...form.value }
    if (payload.deadline) {
      const d = new Date(payload.deadline)
      payload.deadline = `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
    }
    if (!payload.assignee_id) delete payload.assignee_id
```

---

### Task 11: Add My Team section to Dashboard for dev_lead

**Files:**
- Modify: `frontend/src/views/Dashboard.vue`

- [ ] **Step 1: Add My Team section to template**

After the closing `</section>` of `overview-grid`, add:

```html
      <section v-if="authStore.isDevLead" class="my-team-section section-card">
        <div class="section-header">
          <div>
            <div class="section-kicker">我的团队</div>
            <h3>组内成员</h3>
          </div>
        </div>
        <div class="team-grid" v-if="teamMembers.length > 0">
          <div v-for="m in teamMembers" :key="m.id" class="team-member-chip">
            <span class="team-member-name">{{ m.name }}</span>
            <span class="team-member-role">{{ roleMap[m.role] }}</span>
          </div>
        </div>
        <div v-else class="empty-state">暂无团队数据。</div>
      </section>
```

- [ ] **Step 2: Add script logic**

Add import:
```js
import { getMyTeam } from '@/api/groups'
```

Add reactive state:
```js
const teamMembers = ref([])
```

Add roleMap (before onMounted):
```js
const roleMap = { pm: '项目经理', dev_lead: '开发组长', dev: '开发', tester_lead: '测试组长', tester: '测试' }
```

Add to onMounted:
```js
    if (authStore.isDevLead) {
      try {
        const res = await getMyTeam()
        teamMembers.value = res.members || []
      } catch (e) { console.error(e) }
    }
```

- [ ] **Step 3: Add styles**

Add scoped styles:

```css
.my-team-section {
  margin-top: 0;
}

.team-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.team-member-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 12px;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
}

.team-member-name {
  font-size: 14px;
  font-weight: 600;
  color: #0f172a;
}

.team-member-role {
  font-size: 11px;
  color: #64748b;
  padding: 1px 6px;
  border-radius: 4px;
  background: #e2e8f0;
}
```

---

### Task 12: End-to-end verification

- [ ] **Step 1: Build and run backend**

```bash
cd backend && go build -o management.exe . && ./management.exe
```

- [ ] **Step 2: Build frontend**

```bash
cd frontend && npm run build
```

- [ ] **Step 3: Verify the flow**

1. Login as PM, navigate to "小组管理"
2. Create a group with a dev_lead
3. Add dev members to the group
4. Go to Tasks, create a task — select the dev_lead, verify assignee dropdown shows group members (including dev_lead)
5. Select an assignee (or leave blank), create the task
6. Login as dev_lead, verify Dashboard shows "My Team" section
7. Login as dev_lead, verify can see task assigned to them
