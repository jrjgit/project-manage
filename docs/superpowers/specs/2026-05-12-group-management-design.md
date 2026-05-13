# 小组管理与任务分配增强 — 设计文档

## 背景

当前系统缺少小组管理功能，导致以下问题：

- User 模型有 `group_id` 字段但没有 Group 实体和 CRUD
- PM 创建任务时只能指派开发组长，无法直接指定具体开发人员
- 开发组长无法查看自己的组员
- 开发组长无法将任务分配给自己（因为组长也是开发）

## 方案选择

采用**方案 A：完整 Group 模型 + 增强任务分配**。新建 Group 表，PM 集中管理小组，任务创建时可选指定开发人员，不改动现有工作流状态机。

## 数据模型

### 新增：Group

```go
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

### 不变：现有模型

- `User.GroupID` — 已存在，dev_lead 和 dev 指向所属 Group
- `Task.AssigneeID` — 已存在，创建任务时填入
- `Task.DevLeadID` — 已存在，创建任务时必填

### DTO 变更

```go
// CreateTaskRequest — 新增 assignee_id 可选字段
type CreateTaskRequest struct {
    Title       string  `json:"title" binding:"required"`
    Description string  `json:"description"`
    Priority    string  `json:"priority"`
    ProjectID   uint    `json:"project_id" binding:"required"`
    DevLeadID   uint    `json:"dev_lead_id" binding:"required"`
    AssigneeID  *uint   `json:"assignee_id"`  // 新增，可选
    Deadline    *string `json:"deadline"`
}

// 新增 Group DTO
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

## API 设计

### Group CRUD（仅 PM）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/groups | 列出所有组（含组长和成员计数） |
| POST | /api/groups | 创建组 |
| GET | /api/groups/:id | 组详情 + 成员列表 |
| PUT | /api/groups/:id | 编辑组信息 |
| DELETE | /api/groups/:id | 删除组（清空成员 group_id） |

### 组员管理（仅 PM）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/groups/:id/members | 添加成员 |
| DELETE | /api/groups/:id/members/:user_id | 移除成员 |

### 组长查组员

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/groups/my-team | DevLead 查自己的组所有成员 |

### 任务创建扩展

`POST /api/tasks` 接口不变，仅请求体新增可选字段 `assignee_id`。若传入，创建任务时直接填入，状态仍为 `pending`，后续工作流不变。

## 工作流行为

### 路径 1：PM 同时指定组长和开发

1. PM 创建任务（dev_lead_id + assignee_id），status = pending
2. PM 操作：pending → assigned_lead（assignee 已预设，无需再分配）
3. DevLead 操作：assigned_lead → developing（按预设 assignee 开发）
4. Dev 操作：developing → developed → 自动进入测试池

### 路径 2：PM 只指定组长（兼容旧流程）

1. PM 创建任务（dev_lead_id），status = pending，assignee = 空
2. PM 操作：pending → assigned_lead
3. DevLead 在任务详情中设置 assignee（可选自己或组员）
4. DevLead 操作：assigned_lead → developing

### 不变

- 任务状态机完全不变
- 状态流转角色权限不变
- Bug 和测试流程不涉及
- UpdateTask 接口不变，assignee 更新始终可用

## 权限总结

| 操作 | 谁可以做 |
|------|----------|
| 创建/管理小组 | PM |
| 管理组成员 | PM |
| 查看我的组员 | DevLead（GET /api/groups/my-team） |
| 创建任务时指定开发 | PM |
| 修改任务 assignee | PM / DevLead（已有 UpdateTask） |
| 组长自分配 | DevLead（assignee 选自己） |

## 前端变更

### 新增文件

- `src/views/Groups.vue`：小组管理页面，PM 创建/删除组，添加/移除成员
- `src/api/groups.js`：Group API 调用封装

### 修改文件

- `src/router/index.js`：新增 `/groups` 路由（PM only）
- `src/components/AppLayout.vue`：导航栏新增"小组管理"入口（PM only）
- `src/components/CreateTaskDialog.vue`：选择组长后动态加载组员下拉（含组长自己），支持选 assignee
- `src/views/Dashboard.vue`：DevLead 看到"我的团队"区域

### 关键交互

- 创建任务时：PM 选择"指派组长"后，系统通过 `GET /api/groups/:id` 获取该组所有 dev 角色成员
- 开发人员下拉列表包含：组内所有 dev + dev_lead 本人（自分配选项）
- assignee 字段为可选，保持向后兼容

## 实施范围

### 后端

1. `models/group.go`：新增 Group 模型
2. `handlers/group.go`：新增 GroupHandler（CRUD + 组员管理 + MyTeam）
3. `dto/dto.go`：新增 Group DTO，修改 CreateTaskRequest
4. `handlers/task.go`：CreateTask 方法处理 assignee_id
5. `main.go`：注册 Group 路由

### 前端

1. `src/api/groups.js`：新增
2. `src/views/Groups.vue`：新增
3. `src/router/index.js`：新增路由
4. `src/components/AppLayout.vue`：导航栏新增入口
5. `src/components/CreateTaskDialog.vue`：新增 assignee 下拉
6. `src/views/Dashboard.vue`：新增"我的团队"区域
