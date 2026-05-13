# Execution Workbench Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework the current task/bug management app into an execution-first workbench with role-aware defaults, clearer workflow semantics, and a more actionable UI.

**Architecture:** Keep the current Gin + GORM backend and Vue 3 + Pinia frontend structure, but add a thin “view intent” layer in the frontend so pages can derive default filters and quick-entry states from the current role. On the backend, tighten workflow semantics in the existing handlers instead of introducing a new service layer, and back the changed rules with focused handler tests.

**Tech Stack:** Go 1.21, Gin, GORM, SQLite, Vue 3, Vite, Pinia, Naive UI

---

## File structure and responsibilities

### Backend files to modify
- `backend/handlers/task.go` — refine task list filtering, clarify task status transitions, preserve history semantics, support frontend’s execution-first views.
- `backend/handlers/bug.go` — unify bug verification flow semantics and align transition rules with the product spec.
- `backend/handlers/user.go` — source of role-filtered user lists used by action-oriented drawers.
- `backend/models/task.go` — task status constants referenced by both API and frontend behavior.
- `backend/models/bug.go` — bug status constants referenced by both API and frontend behavior.
- `backend/dto/*.go` — only if the chosen frontend interaction requires new query params or update fields.
- `backend/main.go` — only if route wiring changes are needed for any added filter/query endpoints.

### Backend files to create
- `backend/handlers/task_test.go` — task role/filter/transition tests.
- `backend/handlers/bug_test.go` — bug role/filter/transition tests.

### Frontend files to modify
- `frontend/src/views/Dashboard.vue` — convert from generic stats page into action-first workbench.
- `frontend/src/views/Tasks.vue` — add role-aware primary views before advanced filters.
- `frontend/src/views/Bugs.vue` — add bug-specific primary views before advanced filters.
- `frontend/src/components/TaskDetailDrawer.vue` — reorganize drawer into summary + next action + history.
- `frontend/src/components/BugDetailDrawer.vue` — reorganize drawer into summary + next action + history.
- `frontend/src/components/TaskBoard.vue` — align column semantics and visual hierarchy with updated task states.
- `frontend/src/components/TaskCard.vue` — improve action density and status visibility.
- `frontend/src/components/BugList.vue` — improve bug list readability and state visibility.
- `frontend/src/components/AppLayout.vue` — support workbench framing and more consistent page-level actions.
- `frontend/src/api/tasks.js` — pass structured query params for primary views and quick actions.
- `frontend/src/api/bugs.js` — pass structured query params for primary views and quick actions.
- `frontend/src/router/index.js` — preserve navigation behavior when quick-entry links encode view state.
- `frontend/src/store/useAuthStore.js` — central place for role-derived flags reused by view defaults.

### Frontend files to create
- `frontend/src/constants/taskViews.js` — primary task view definitions keyed by role and state.
- `frontend/src/constants/bugViews.js` — primary bug view definitions keyed by role and state.
- `frontend/src/constants/statusMeta.js` — shared labels/colors for task and bug statuses.
- `frontend/src/utils/viewState.js` — route query ↔ page filter normalization.

### Notes on sequencing
- Implement backend state semantics before polishing drawers, otherwise the UI will encode stale assumptions.
- Implement shared frontend constants before reworking Dashboard/Tasks/Bugs, otherwise labels/colors/defaults will be duplicated.
- Add backend tests before changing handler logic to reduce regressions in role filtering and state transitions.

## Task 1: Add backend tests for current and target workflow semantics

**Files:**
- Create: `backend/handlers/task_test.go`
- Create: `backend/handlers/bug_test.go`
- Read while implementing: `backend/handlers/task.go`, `backend/handlers/bug.go`, `backend/models/task.go`, `backend/models/bug.go`, `backend/db/database.go`

- [ ] **Step 1: Create task handler tests for role-based list filtering**

```go
package handlers

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
)

func TestListTasks_DevSeesOnlyAssignedTasks(t *testing.T) {
	gin.SetMode(gin.TestMode)

	r := gin.New()
	r.GET("/tasks", func(c *gin.Context) {
		c.Set("userID", uint(2))
		c.Set("userRole", "dev")
		c.Set("groupID", (*uint)(nil))
		newTestTaskHandler(t).ListTasks(c)
	})

	req := httptest.NewRequest(http.MethodGet, "/tasks", nil)
	w := httptest.NewRecorder()
	r.ServeHTTP(w, req)

	if w.Code != http.StatusOK {
		t.Fatalf("expected 200, got %d", w.Code)
	}

	assertTaskTitles(t, w.Body.Bytes(), []string{"Assigned To Dev"})
}
```

- [ ] **Step 2: Create task handler tests for changed workflow semantics**

```go
func TestChangeTaskStatus_DevelopedCreatesPendingTestHistory(t *testing.T) {
	h := newTestTaskHandler(t)
	taskID, devID := seedDevelopingTaskForDev(t)

	if err := h.ChangeTaskStatus(taskID, "developed", devID, "ready for qa"); err != nil {
		t.Fatalf("expected status change to succeed: %v", err)
	}

	assertTaskCurrentStatus(t, taskID, "pending_test")
	assertTaskHistoryTransitions(t, taskID, []string{
		"developing->developed",
		"developed->pending_test",
	})
}
```

- [ ] **Step 3: Create bug handler tests for verification flow**

```go
func TestChangeBugStatus_FixedMustMoveToPendingVerify(t *testing.T) {
	h := newTestBugHandler(t)
	bugID, devID := seedFixingBugForDev(t)

	if err := h.ChangeBugStatus(bugID, "fixed", devID, "patched and ready"); err != nil {
		t.Fatalf("expected fixed transition to succeed: %v", err)
	}

	assertBugCurrentStatus(t, bugID, "pending_verify")
}

func TestChangeBugStatus_TesterCanCloseFromPendingVerify(t *testing.T) {
	h := newTestBugHandler(t)
	bugID, testerID := seedPendingVerifyBugForTester(t)

	if err := h.ChangeBugStatus(bugID, "closed", testerID, "verified"); err != nil {
		t.Fatalf("expected close transition to succeed: %v", err)
	}
}
```

- [ ] **Step 4: Add minimal shared test helpers inside each test file**

```go
func assertTaskCurrentStatus(t *testing.T, taskID uint, expected string) {
	t.Helper()
	var status string
	if err := db.DB.Table("tasks").Select("status").Where("id = ?", taskID).Scan(&status).Error; err != nil {
		t.Fatalf("read status: %v", err)
	}
	if status != expected {
		t.Fatalf("expected status %s, got %s", expected, status)
	}
}
```

- [ ] **Step 5: Run backend tests and confirm the new expectations fail before implementation**

Run:
```bash
cd /d/repository/management/backend && go test ./handlers -run 'TestListTasks_DevSeesOnlyAssignedTasks|TestChangeTaskStatus_DevelopedCreatesPendingTestHistory|TestChangeBugStatus_FixedMustMoveToPendingVerify|TestChangeBugStatus_TesterCanCloseFromPendingVerify' -v
```

Expected:
```text
--- FAIL: TestChangeBugStatus_FixedMustMoveToPendingVerify
--- FAIL: TestChangeBugStatus_TesterCanCloseFromPendingVerify
FAIL
```

## Task 2: Align backend task and bug workflow semantics with the approved spec

**Files:**
- Modify: `backend/handlers/task.go`
- Modify: `backend/handlers/bug.go`
- Modify: `backend/models/task.go` (only if constants or comments need cleanup)
- Modify: `backend/models/bug.go` (only if constants or comments need cleanup)
- Test: `backend/handlers/task_test.go`
- Test: `backend/handlers/bug_test.go`

- [ ] **Step 1: Refine task transition logic without changing the current storage model**

```go
validTransitions := map[string][]string{
	models.TaskPending:      {models.TaskAssignedLead},
	models.TaskAssignedLead: {models.TaskDeveloping},
	models.TaskDeveloping:   {models.TaskDeveloped},
	models.TaskDeveloped:    {models.TaskPendingTest},
	models.TaskPendingTest:  {models.TaskTesting},
	models.TaskTesting:      {models.TaskPassed, models.TaskRejected},
	models.TaskRejected:     {models.TaskDeveloping, models.TaskClosed},
	models.TaskPassed:       {models.TaskClosed},
}
```

Keep the auto-hop to `pending_test`, but treat it as required product semantics rather than a hidden side effect: preserve the explicit second history row and keep the user-facing comment consistent.

- [ ] **Step 2: Make bug verification a first-class state**

```go
validTransitions := map[string][]string{
	models.BugNew:           {models.BugAssigned},
	models.BugAssigned:      {models.BugFixing},
	models.BugFixing:        {models.BugFixed},
	models.BugFixed:         {models.BugPendingVerify},
	models.BugPendingVerify: {models.BugClosed, models.BugReopened},
	models.BugReopened:      {models.BugAssigned},
}
```

And change `ChangeBugStatus` to auto-hop `fixed -> pending_verify` if that is the approved storage behavior, or make `fixed` the explicit status returned to the client only momentarily within the same transaction. Use one approach consistently and keep history rows aligned.

- [ ] **Step 3: Update role guards to match the chosen bug flow**

```go
if from == models.BugAssigned && to == models.BugFixing && role != models.RoleDev {
	return false
}
if from == models.BugFixing && to == models.BugFixed && role != models.RoleDev {
	return false
}
if from == models.BugPendingVerify && (to == models.BugClosed || to == models.BugReopened) && role != models.RoleTester {
	return false
}
```

- [ ] **Step 4: Update notification target resolution for the verification step**

```go
case models.BugFixing + "->" + models.BugFixed:
	if err := tx.First(&u, bug.CreatorID).Error; err == nil {
		targets = append(targets, u)
	}
case models.BugFixed + "->" + models.BugPendingVerify:
	if err := tx.First(&u, bug.CreatorID).Error; err == nil {
		targets = append(targets, u)
	}
case models.BugPendingVerify + "->" + models.BugClosed,
	models.BugPendingVerify + "->" + models.BugReopened:
	if bug.AssigneeID != nil {
		if err := tx.First(&u, *bug.AssigneeID).Error; err == nil {
			targets = append(targets, u)
		}
	}
```

- [ ] **Step 5: Run focused backend tests and then the full backend suite**

Run:
```bash
cd /d/repository/management/backend && go test ./handlers -run 'TestChangeTaskStatus_DevelopedCreatesPendingTestHistory|TestChangeBugStatus_FixedMustMoveToPendingVerify|TestChangeBugStatus_TesterCanCloseFromPendingVerify' -v
```

Expected:
```text
PASS
ok   	management/handlers
```

Then run:
```bash
cd /d/repository/management/backend && go test ./... 
```

Expected:
```text
ok   	management/handlers
ok   	management/...
```

## Task 3: Add role-aware primary view definitions for tasks and bugs

**Files:**
- Create: `frontend/src/constants/taskViews.js`
- Create: `frontend/src/constants/bugViews.js`
- Create: `frontend/src/constants/statusMeta.js`
- Create: `frontend/src/utils/viewState.js`
- Modify: `frontend/src/store/useAuthStore.js`
- Modify: `frontend/src/api/tasks.js`
- Modify: `frontend/src/api/bugs.js`

- [ ] **Step 1: Create shared task/bug status metadata**

```js
export const taskStatusMeta = {
  pending: { label: '待分配', tone: 'default', color: '#64748b' },
  assigned_lead: { label: '待开发接手', tone: 'info', color: '#3b82f6' },
  developing: { label: '开发中', tone: 'warning', color: '#f59e0b' },
  pending_test: { label: '待测试', tone: 'info', color: '#2563eb' },
  testing: { label: '测试中', tone: 'warning', color: '#f97316' },
  passed: { label: '测试通过', tone: 'success', color: '#10b981' },
  rejected: { label: '打回修改', tone: 'error', color: '#ef4444' },
  closed: { label: '已关闭', tone: 'default', color: '#94a3b8' }
}

export const bugStatusMeta = {
  assigned: { label: '待修复', tone: 'info', color: '#3b82f6' },
  fixing: { label: '修复中', tone: 'warning', color: '#f59e0b' },
  pending_verify: { label: '待验证', tone: 'info', color: '#2563eb' },
  closed: { label: '已关闭', tone: 'default', color: '#94a3b8' },
  reopened: { label: '重新打开', tone: 'error', color: '#ef4444' }
}
```

- [ ] **Step 2: Create primary task and bug view definitions**

```js
export const taskPrimaryViews = {
  dev: [
    { key: 'my-todo', label: '我的待办', params: { mine: 'todo' } },
    { key: 'my-work', label: '我的参与', params: { mine: 'all' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  tester: [
    { key: 'my-todo', label: '我的待办', params: { mine: 'todo' } },
    { key: 'my-work', label: '我的参与', params: { mine: 'all' } },
    { key: 'all', label: '全部任务', params: {} }
  ],
  pm: [
    { key: 'all', label: '全部任务', params: {} },
    { key: 'risk', label: '风险项', params: { focus: 'risk' } },
    { key: 'my-work', label: '我的关注', params: { mine: 'watch' } }
  ]
}
```

```js
export const bugPrimaryViews = {
  dev: [
    { key: 'assigned-to-me', label: '待我修复', params: { mine: 'fix' } },
    { key: 'reopened', label: '重新打开', params: { status: 'reopened' } },
    { key: 'all', label: '全部 Bug', params: {} }
  ],
  tester: [
    { key: 'verify', label: '待我验证', params: { mine: 'verify' } },
    { key: 'created-by-me', label: '我创建的', params: { mine: 'created' } },
    { key: 'all', label: '全部 Bug', params: {} }
  ]
}
```

- [ ] **Step 3: Add small helper utilities for route-query normalization**

```js
export function buildViewQuery(view) {
  return {
    view: view.key,
    ...Object.fromEntries(
      Object.entries(view.params || {}).filter(([, value]) => value !== undefined && value !== null && value !== '')
    )
  }
}

export function pickActiveView(views, routeQuery) {
  return views.find((view) => view.key === routeQuery.view) || views[0]
}
```

- [ ] **Step 4: Keep API wrappers simple but param-driven**

```js
export const getTasks = (params = {}) => request.get('/tasks', { params })
export const getBugs = (params = {}) => request.get('/bugs', { params })
```

And add role helper accessors in the auth store only if a page currently repeats that logic.

- [ ] **Step 5: Build the frontend to verify shared modules compile cleanly**

Run:
```bash
cd /d/repository/management/frontend && npm run build
```

Expected:
```text
vite v...
✓ built in ...
```

## Task 4: Rework Dashboard into an action-first workbench

**Files:**
- Modify: `frontend/src/views/Dashboard.vue`
- Modify: `frontend/src/components/AppLayout.vue`
- Modify: `frontend/src/router/index.js`
- Reuse: `frontend/src/constants/taskViews.js`, `frontend/src/constants/bugViews.js`, `frontend/src/constants/statusMeta.js`, `frontend/src/utils/viewState.js`

- [ ] **Step 1: Replace generic stat cards with a role-aware action section**

```vue
<div class="action-grid">
  <button
    v-for="item in primaryActions"
    :key="item.key"
    class="action-card"
    @click="goToAction(item)"
  >
    <div class="action-card__label">{{ item.label }}</div>
    <div class="action-card__value">{{ item.value }}</div>
    <div class="action-card__hint">{{ item.hint }}</div>
  </button>
</div>
```

- [ ] **Step 2: Keep project overview and recent activity as secondary sections**

```vue
<div class="secondary-grid">
  <section class="section-card">
    <div class="section-header">
      <h3>项目总览</h3>
    </div>
    <div class="summary-list">
      <div v-for="item in projectSummary" :key="item.key" class="summary-row">
        <span>{{ item.label }}</span>
        <strong>{{ item.value }}</strong>
      </div>
    </div>
  </section>
</div>
```

- [ ] **Step 3: Make action cards navigate with encoded view intent**

```js
function goToAction(item) {
  router.push({
    path: item.targetPath,
    query: item.query
  })
}
```

Where `item.query` comes from `buildViewQuery(...)` and status-specific quick entries.

- [ ] **Step 4: Update layout framing so workbench pages read as primary content**

```vue
<header class="top-bar top-bar--workbench">
  <div>
    <h1 class="page-title">{{ pageTitle }}</h1>
    <p v-if="pageSubtitle" class="page-subtitle">{{ pageSubtitle }}</p>
  </div>
  <div class="top-actions">
    <slot name="actions" />
  </div>
</header>
```

- [ ] **Step 5: Run the frontend build and manually validate the Dashboard in the browser**

Run backend:
```bash
cd /d/repository/management/backend && go run .
```

Run frontend dev server in another terminal:
```bash
cd /d/repository/management/frontend && npm run dev
```

Manual checks:
```text
1. Login as dev/tester/pm test users.
2. Confirm the first screen highlights role-specific next actions.
3. Click each action card and verify it lands on the expected page/query state.
```

## Task 5: Rework Tasks and Bugs into primary-view-first execution pages

**Files:**
- Modify: `frontend/src/views/Tasks.vue`
- Modify: `frontend/src/views/Bugs.vue`
- Modify: `frontend/src/components/TaskBoard.vue`
- Modify: `frontend/src/components/TaskCard.vue`
- Modify: `frontend/src/components/BugList.vue`
- Reuse: `frontend/src/constants/taskViews.js`, `frontend/src/constants/bugViews.js`, `frontend/src/constants/statusMeta.js`, `frontend/src/utils/viewState.js`

- [ ] **Step 1: Add primary view tabs above advanced filters in Tasks.vue**

```vue
<div class="primary-view-bar">
  <button
    v-for="view in taskViews"
    :key="view.key"
    :class="['primary-view-chip', { active: activeView.key === view.key }]"
    @click="selectPrimaryView(view)"
  >
    {{ view.label }}
  </button>
</div>
```

```js
const taskViews = computed(() => taskPrimaryViews[authStore.userInfo?.role] || taskPrimaryViews.dev)
const activeView = computed(() => pickActiveView(taskViews.value, route.query))
```

- [ ] **Step 2: Keep advanced filters but apply them on top of the primary view**

```js
const requestParams = computed(() => ({
  ...activeView.value.params,
  project_id: filterProject.value || undefined,
  status: filterStatus.value || undefined,
  priority: filterPriority.value || undefined
}))

async function loadTasks() {
  tasks.value = await getTasks(requestParams.value)
}
```

- [ ] **Step 3: Apply the same structure to Bugs.vue with bug-specific labels**

```js
const bugViews = computed(() => bugPrimaryViews[authStore.userInfo?.role] || bugPrimaryViews.tester)
const activeBugView = computed(() => pickActiveView(bugViews.value, route.query))

async function loadBugs() {
  bugs.value = await getBugs({
    ...activeBugView.value.params,
    task_id: filterTask.value || undefined,
    status: filterStatus.value || undefined,
    severity: filterSeverity.value || undefined
  })
}
```

- [ ] **Step 4: Update board/list components to use shared status metadata and stronger state hierarchy**

```js
const columns = computed(() => ['assigned_lead', 'developing', 'pending_test', 'testing', 'rejected'].map((key) => ({
  key,
  label: taskStatusMeta[key].label,
  color: taskStatusMeta[key].color,
  tasks: props.tasks.filter((task) => task.status === key)
})))
```

```vue
<div class="task-card__footer">
  <span class="task-card__assignee">{{ task.assignee?.name || '未指派' }}</span>
  <span class="task-card__status">{{ taskStatusMeta[task.status].label }}</span>
</div>
```

- [ ] **Step 5: Run frontend build and manually verify task/bug page behavior**

Run:
```bash
cd /d/repository/management/frontend && npm run build
```

Manual checks:
```text
1. Open /tasks and /bugs directly after login.
2. Confirm the default selected primary view matches the current role.
3. Switch primary views and confirm data reloads with the expected scope.
4. Add advanced filters and confirm they narrow the current primary view rather than replacing it.
```

## Task 6: Turn task and bug drawers into summary + next-action workspaces

**Files:**
- Modify: `frontend/src/components/TaskDetailDrawer.vue`
- Modify: `frontend/src/components/BugDetailDrawer.vue`
- Modify: `frontend/src/api/tasks.js` (only if helper calls need cleanup)
- Modify: `frontend/src/api/bugs.js` (only if helper calls need cleanup)
- Reuse: `frontend/src/constants/statusMeta.js`

- [ ] **Step 1: Split the task drawer into summary, action, and history sections**

```vue
<div class="drawer-summary">
  <div class="drawer-summary__header">
    <h2>{{ task.title }}</h2>
    <n-tag :type="taskStatusMeta[task.status].tone">{{ taskStatusMeta[task.status].label }}</n-tag>
  </div>
  <div class="drawer-summary__meta">
    <span>项目：{{ task.project?.name || '-' }}</span>
    <span>当前指派：{{ task.assignee?.name || '未指派' }}</span>
    <span>测试负责人：{{ task.tester?.name || '-' }}</span>
  </div>
</div>
```

- [ ] **Step 2: Generate only the actions the current role can actually execute**

```js
const nextActions = computed(() => {
  const role = authStore.userInfo?.role
  const status = task.value?.status

  if (role === 'dev' && status === 'assigned_lead') {
    return [{ key: 'start-dev', label: '开始开发', status: 'developing', type: 'primary' }]
  }
  if (role === 'tester' && status === 'testing') {
    return [
      { key: 'pass', label: '测试通过', status: 'passed', type: 'success' },
      { key: 'reject', label: '打回修改', status: 'rejected', type: 'error' }
    ]
  }
  return []
})
```

- [ ] **Step 3: Mirror the same action-first structure in the bug drawer**

```js
const nextActions = computed(() => {
  const role = authStore.userInfo?.role
  const status = bug.value?.status

  if (role === 'dev' && status === 'assigned') {
    return [{ key: 'start-fix', label: '开始修复', status: 'fixing', type: 'primary' }]
  }
  if (role === 'tester' && status === 'pending_verify') {
    return [
      { key: 'close', label: '验证通过', status: 'closed', type: 'success' },
      { key: 'reopen', label: '重新打开', status: 'reopened', type: 'error' }
    ]
  }
  return []
})
```

- [ ] **Step 4: Keep history visible but visually secondary**

```vue
<section class="drawer-section drawer-section--history">
  <h3>状态历史</h3>
  <n-timeline>
    <n-timeline-item v-for="item in histories" :key="item.id">
      <div>{{ formatHistory(item) }}</div>
      <small>{{ item.user?.name || '系统' }} · {{ formatTime(item.changed_at) }}</small>
    </n-timeline-item>
  </n-timeline>
</section>
```

- [ ] **Step 5: Validate drawers end-to-end in the browser**

Manual checks:
```text
1. Open a task as dev, tester, tester_lead, and pm.
2. Verify the drawer shows the same summary frame but different next actions per role/state.
3. Confirm forbidden actions are absent, not merely disabled.
4. Repeat for bug detail, especially the pending_verify close/reopen step.
```

## Task 7: Apply visual system cleanup and consistency pass

**Files:**
- Modify: `frontend/src/components/AppLayout.vue`
- Modify: `frontend/src/views/Dashboard.vue`
- Modify: `frontend/src/views/Tasks.vue`
- Modify: `frontend/src/views/Bugs.vue`
- Modify: `frontend/src/views/Projects.vue`
- Modify: `frontend/src/components/TaskBoard.vue`
- Modify: `frontend/src/components/TaskCard.vue`
- Modify: `frontend/src/components/BugList.vue`
- Modify: `frontend/src/components/TaskDetailDrawer.vue`
- Modify: `frontend/src/components/BugDetailDrawer.vue`

- [ ] **Step 1: Normalize card, section, and filter container styles**

```css
.section-card {
  background: #ffffff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  box-shadow: 0 1px 2px rgba(15, 23, 42, 0.04);
}

.filter-shell {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px 18px;
  border-radius: 14px;
  background: #ffffff;
  border: 1px solid #e2e8f0;
}
```

- [ ] **Step 2: Make status colors consistent across cards, tags, and board columns**

```js
const statusToneMap = {
  info: '#2563eb',
  warning: '#f59e0b',
  success: '#10b981',
  error: '#ef4444',
  default: '#94a3b8'
}
```

Use the metadata from `statusMeta.js` rather than local copies in each component.

- [ ] **Step 3: Improve interaction affordances on clickable surfaces**

```css
.action-card,
.project-card,
.task-card,
.bug-row {
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
}

.action-card:hover,
.project-card:hover,
.task-card:hover,
.bug-row:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08);
  border-color: #cbd5e1;
}
```

- [ ] **Step 4: Verify medium-width layout behavior before calling the polish done**

Manual checks:
```text
1. Resize the browser between roughly 1024px and 1366px widths.
2. Confirm filter bars wrap cleanly.
3. Confirm board columns still scroll horizontally without clipping actions.
4. Confirm drawer content remains readable without overlapping controls.
```

- [ ] **Step 5: Run the production build and perform a final smoke test**

Run:
```bash
cd /d/repository/management/frontend && npm run build
cd /d/repository/management/backend && go run .
```

Smoke test:
```text
1. Login.
2. Open Dashboard.
3. Follow one task path and one bug path to completion.
4. Confirm visual hierarchy still supports the main action path at every step.
```

## Task 8: Final verification and documentation sync

**Files:**
- Modify: `CLAUDE.md`
- Modify: `docs/superpowers/specs/2026-05-12-execution-workbench-design.md` (only if implementation decisions materially diverged)
- Test: `backend/handlers/task_test.go`
- Test: `backend/handlers/bug_test.go`

- [ ] **Step 1: Update `CLAUDE.md` if the workflow semantics or development commands changed materially**

```md
- Task workflow keeps an explicit history row for `developing -> developed -> pending_test`.
- Bug workflow now treats `pending_verify` as the verification gate before `closed` or `reopened`.
- Tasks and Bugs pages are role-aware and default to execution-first primary views.
```

- [ ] **Step 2: Re-run all automated checks used in this plan**

Run:
```bash
cd /d/repository/management/backend && go test ./...
cd /d/repository/management/frontend && npm run build
```

Expected:
```text
ok   	management/...
✓ built in ...
```

- [ ] **Step 3: Run one full manual role-based workflow per major persona**

Manual checklist:
```text
Developer:
- Login
- See execution-first Dashboard
- Open my task
- Move assigned_lead -> developing -> developed
- Confirm task lands in pending_test and history is explicit

Tester:
- Login
- See pending verification focus
- Open bug in pending_verify
- Close or reopen bug
- Confirm history and next actions remain consistent

PM:
- Login
- See management actions without losing global overview
- Open task waiting for closure
- Close task successfully
```

- [ ] **Step 4: Check the plan against the approved spec and note any intentional deviations**

```md
If implementation chooses to keep `fixed` as a visible transient history state before `pending_verify`, record that in the spec so frontend copy and QA expectations stay aligned.
```

- [ ] **Step 5: Mark the feature ready for execution handoff**

Definition of done:
```text
- Backend transitions match the approved workflow semantics.
- Dashboard/Tasks/Bugs default to action-first role-aware views.
- Drawers show only meaningful next actions.
- Visual language is consistent across major pages.
- Tests and build pass.
```
