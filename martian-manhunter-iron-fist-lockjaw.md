# 需求/项目管理系统 — 开发提示词（Prompt）

## 一、项目背景与目标

基于现有 `backend-java` + `frontend` 技术栈，将 zl 文件夹中的**需求管理业务设计**与**原型截图**落地为可运行的全栈系统。

现有系统已具备基础的项目管理（Project）、任务管理（Task）、Bug 管理（Bug）、用户与权限、组管理、JWT 认证、工作流规则与通知体系。本次开发目标是：
- **以"需求（Requirement）"为核心主线**，重构/扩展现有数据模型与页面，使其匹配 zl 设计文档中的业务规则与原型图。
- 新增/改造功能模块：**需求管理**、**发布迭代管理**、**营收统计看板**、**人员绩效统计**、**运维需求清单**、**项目需求清单**、**需求详情页**、**基础数据字典维护**。

---

## 二、技术栈（必须严格遵循）

### 后端 — `backend-java/`
- **Spring Boot 3.2.7** + Java 17
- **Spring Security 6** + JWT（jjwt 0.12.6）
- **MyBatis Plus 3.5.7** + MySQL
- **Lombok** + **Validation**
- 统一返回 `Result<T>` 包装（`{ data, error }`，`@JsonInclude(NON_NULL)`）
- Jackson 使用 `SNAKE_CASE` 序列化
- RESTful API，前缀 `/api`
- 角色控制：`@PreAuthorize("hasRole('PM')")` 等
- 已有实体命名：`users`, `projects`, `tasks`, `bugs`, `groups`

### 前端 — `frontend/`
- **Vue 3.5** + **Vite 6**
- **Pinia** 状态管理
- **Naive UI 2.41**（组件库）
- **Vue Router 4**（history 模式）
- **Axios**（统一封装在 `api/request.js`，baseURL `/api`，Bearer Token 自动注入，401 跳转登录）
- **Lucide Vue Next** 图标
- 视觉风格：现代卡片式 UI，圆角（12~24px），渐变色 Hero 卡片，浅色背景 `#f5f7fa`

---

## 三、业务核心规则（来自 zl/数据表设计.docx）

### 3.1 需求（Requirement）是核心
- 需求表是关键，大量字段与**大需求**绑定。
- **需求状态**（由项目经理手动修改，非自动流转）：
  1. 待规划
  2. 进行中
  3. 综合测试
  4. 业务测试
  5. 待发布
  6. 已发布
- **状态流转约束**：只有管理员/PM 能将状态修改为**待发布**，此时必须关联一个**发布迭代**。需求变更时也支持从发布库中移除。
- **开发进度** = 关联任务的累计开发进度 / 任务条数
- **综合测试进度** = 累计 BUG 条数 / 已关闭 BUG 条数
- **业务测试进度** = 累计 BUG 条数 / 已关闭 BUG 条数

### 3.2 功能点（Feature / 子需求）
- 功能点全部挂在**大需求**下。
- 功能点可绑定开发人员与测试人员。
- **功能点状态**：1. 规划中 → 2. 开发中 → 3. 待测试 → 4. 已关闭
- 功能点有对应的**开发任务**和**测试任务**。
- 开发任务完成后，功能点状态才能变为**已关闭**。
- 当一个功能点下**没有 Bug** 时，功能点可以验收通过。
- **所有功能点验收通过后，需求才能完成**。

### 3.3 Bug 规则
- 可为**大需求**创建 Bug，字段选择**开发负责人**和**测试负责人**。
- 只有对应的开发人员才能修改 Bug；只有对应的测试人员才能关闭/验证 Bug。

### 3.4 任务与绩效
- 任务有绩效数值，用于统计个人产能。
- 延期任务需要被单独标识和统计。

---

## 四、数据模型设计（需扩展）

### 4.1 现有表（需兼容/改造）
| 表名 | 说明 |
|------|------|
| `users` | 用户（已有 id, name, password, role, group_id, email 等） |
| `projects` | 项目（已有 id, name, pm_id, created_at） |
| `groups` | 组（已有） |
| `tasks` | 现有任务表，需评估是否改造为 `tasks_pool` 或保留并扩展 |
| `bugs` | 现有 Bug 表，需评估是否改造为 `bugs_pool` 或保留并扩展 |

### 4.2 新增/改造表（来自设计文档）

#### `requirements` — 需求表
```sql
CREATE TABLE requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requirement_id VARCHAR(128) NOT NULL COMMENT '需求ID，唯一标识',
    number VARCHAR(128) NOT NULL COMMENT '需求编号',
    title VARCHAR(128) NOT NULL COMMENT '需求标题/描述',
    description TEXT COMMENT '详细描述',
    source INT NOT NULL COMMENT '需求来源',
    status INT NOT NULL COMMENT '需求状态：1待规划 2进行中 3综合测试 4业务测试 5待发布 6已发布',
    priority INT NOT NULL COMMENT '优先级',
    system VARCHAR(32) NOT NULL COMMENT '所属系统',
    project VARCHAR(32) NOT NULL COMMENT '所属项目',
    project_type VARCHAR(32) NOT NULL COMMENT '项目类型（运维需求/项目需求）',
    person VARCHAR(32) COMMENT '业务负责人',
    relevant TEXT COMMENT '相关人员',
    total VARCHAR(128) COMMENT '总工时/总价',
    develop_total VARCHAR(128) COMMENT '开发总工时',
    develop_price VARCHAR(128) COMMENT '开发单价',
    test_total VARCHAR(128) COMMENT '测试总工时',
    test_price VARCHAR(128) COMMENT '测试单价',
    business_test_total VARCHAR(128) COMMENT '业务测试总工时',
    business_test_price VARCHAR(128) COMMENT '业务测试单价',
    release_time DATETIME COMMENT '发布时间',
    iteration_id VARCHAR(128) COMMENT '关联发布迭代ID',
    c_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    u_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### `features` — 功能点表（设计文档中未单独建表，但业务描述存在，需补充）
建议新增，关联 requirement_id：
- `id`, `feature_id`, `requirement_id`, `title`, `description`, `status`, `developer`, `tester`, `c_time`, `u_time`

#### `tasks_pool` — 任务池（终端维度）
```sql
CREATE TABLE tasks_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(128) NOT NULL COMMENT '任务ID',
    requirement_id VARCHAR(128) NOT NULL COMMENT '关联需求ID',
    terminal VARCHAR(128) COMMENT '终端（后台/iOS/安卓/鸿蒙/小程序/H5）',
    description TEXT COMMENT '任务描述',
    develop_total VARCHAR(128) COMMENT '开发总工时',
    status INT NOT NULL COMMENT '任务状态：1.待规划 2.开发中 3.待测试 4.已关闭 5.已取消',
    creator VARCHAR(32) COMMENT '创建人',
    owner VARCHAR(32) COMMENT '负责人',
    remark TEXT COMMENT '备注',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    c_time DATETIME NOT NULL,
    u_time DATETIME NOT NULL
);
```

#### `develop_tasks_pool` — 开发任务池
- 关联 `requirement_id` + `point_id`（功能点ID）
- 字段：`id`, `task_id`, `requirement_id`, `point_id`, `title`, `description`, `status`, `creator`, `owner`, `c_time`, `u_time`

#### `qa_tasks_pool` — 测试任务池
- 类似开发任务池，增加 `developer` 字段

#### `bugs_pool` — Bug 池
```sql
CREATE TABLE bugs_pool (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    bug_id VARCHAR(128) NOT NULL COMMENT 'Bug唯一标识',
    requirement_id VARCHAR(128) NOT NULL COMMENT '关联需求ID',
    task_id VARCHAR(128) COMMENT '关联任务ID',
    title VARCHAR(128) NOT NULL COMMENT 'Bug标题',
    description TEXT COMMENT 'Bug描述',
    expected_res TEXT COMMENT '预期结果',
    status INT NOT NULL COMMENT 'Bug状态：0.未修改 1.已修改 2.开发确认非Bug 3.已关闭',
    creator VARCHAR(32) COMMENT '创建人',
    owner VARCHAR(32) COMMENT '测试负责人',
    developer VARCHAR(32) COMMENT '开发负责人',
    c_time DATETIME NOT NULL,
    u_time DATETIME NOT NULL
);
```

#### `iterations` — 发布迭代管理
- `id`, `iteration_id`, `name`, `release_time`, `c_time`, `u_time`

#### `requirement_feature_logs` — 变更日志
- 已提供完整 SQL，支持 JSON 快照记录需求/功能点变更

---

## 五、页面功能清单（来自原型图）

### 5.1 需求列表页（核心）
- 表头：需求编号 | 需求描述 | 业务负责人 | 需求状态 | 优先级 | 开发进度 | 综合测试进度 | 业务测试进度 | 发布迭代 | 所属项目 | 需求方案 | 备注
- 筛选：按系统（单选）、按状态（单选）
- 两个固定列表视图：
  - **运维需求清单**：默认固定 `需求类型=运维需求`，`需求状态≠已发布`
  - **项目需求清单**：默认固定 `需求类型=项目需求`，`需求状态≠已发布`
- 交互：
  - 点击需求编号 → 跳转需求详情页
  - 点击优先级 → 可修改
  - 需求方案 → 点击可打开
  - 备注 → 点击可编辑
  - 操作：纳入发布清单 / 从发布库移除

### 5.2 需求详情页
- 顶部信息：需求编号、业务负责人（可编辑）、需求状态、优先级（可编辑）、发布迭代（可编辑）、所属项目
- 主体：需求描述、需求方案、备注（可编辑）
- **开发进度卡片**：按终端展示（后台、iOS、安卓、鸿蒙、小程序、H5），每个终端显示进度百分比 + 延期天数，点击可展示该负责人的任务进度清单
- **综合测试进度卡片**：累计 BUG 数量 | 已关闭 BUG 数量 | 修复中 BUG 数量 | 待复测 BUG 数量 | 测试中 BUG 数量，点击展示对应 BUG 清单
- **业务测试进度卡片**：同上
- 底部操作：纳入发布清单

### 5.3 年度营收统计看板（Dashboard 扩展）
- 筛选条件：年度（单选）、按系统、项目类型
- 统计卡片：累计接收（数量/金额）、未开始、已完成、开发中、测试中
- 图表1：月度接收产值量（按月统计累计创建的需求量）
- 图表2：月度完成产值量（按月统计累计完成的需求量）

### 5.4 人员绩效统计页
- 筛选：年度（单选）、月度（单选）
- 图表1：进行中任务 — 统计进行中任务对应的任务绩效数，没有的显示 0
- 图表2：已延期任务 — 统计进行中并已延期的任务对应的任务绩效数
- 图表3：绩效产能 — 统计已完成的绩效数
- 按人员（姓名）作为 X 轴展示柱状图

### 5.5 发布迭代管理
- 列表：迭代名称、发布时间
- 基础数据维护入口

### 5.6 基础数据字典维护
- 项目名称、项目类型等基础字典的可维护列表

---

## 六、现有代码约束与规范

### 后端
1. **实体类**：使用 Lombok `@Data`，`@TableName` 指定表名，`@TableId(type = IdType.AUTO)`，`@TableField(exist = false)` 用于关联对象。
2. **Mapper**：继承 `BaseMapper<Entity>`，放在 `mapper` 包下。
3. **Service**：业务逻辑层，调用 Mapper，处理事务。
4. **Controller**：RESTful，`@RestController`，`@RequestMapping("/api/xxx")`，返回 `Result<T>`。
5. **DTO**：请求体使用独立的 DTO 类（如 `CreateRequirementRequest`），加 `@Valid` 校验。
6. **角色权限**：PM 拥有管理权限，其他角色按业务规则限制操作。
7. **通知**：已有 `NotificationService` + `MultiNotifier`，关键状态变更时应发送通知。
8. **工作流**：已有 `WorkflowService` + `WorkflowRule`，可将需求状态流转纳入工作流规则。

### 前端
1. **API 层**：每个模块独立文件（如 `api/requirements.js`），函数直接 export，使用统一 `request` 实例。
2. **视图**：放在 `views/` 下，使用 `<AppLayout>` 包裹。
3. **组件**：可复用组件放在 `components/` 下。
4. **状态管理**：Pinia store（已有 `useAuthStore`）。
5. **常量**：状态元数据、视图配置放在 `constants/` 下（参考 `statusMeta.js`, `taskViews.js`）。
6. **路由**：`router/index.js` 中注册，需要鉴权的加 `meta: { requiresAuth: true }`。
7. **UI 风格**：
   - Hero 卡片使用渐变背景（如 `linear-gradient(135deg, #0f172a 0%, #1e293b 55%, #312e81 100%)`）
   - 圆角：大卡片 20~24px，按钮/输入框 10~14px
   - 色彩：主色 `#6366f1`（indigo），成功 `#10b981`，警告 `#f59e0b`，危险 `#f43f5e`
   - 阴影：悬停时 `0 16px 36px rgba(15, 23, 42, 0.08)`
8. **Naive UI 组件**：直接使用按需导入（如 `NButton`, `NModal`, `NForm`, `NDataTable`, `NSelect` 等）。

---

## 七、开发优先级建议

1. **P0 — 数据层**：创建/改造数据库表，编写 MyBatis Plus Entity + Mapper。
2. **P0 — 需求管理后端 API**：需求的 CRUD、状态修改、进度计算、关联查询。
3. **P0 — 需求列表前端页**：列表展示、筛选、基础交互。
4. **P1 — 需求详情页**：详情展示 + 终端进度卡片 + BUG 统计卡片。
5. **P1 — 发布迭代管理**：迭代的 CRUD，需求关联迭代。
6. **P2 — 统计看板**：营收统计（卡片 + ECharts 柱状图）。
7. **P2 — 人员绩效统计**：绩效柱状图。
8. **P2 — 基础数据字典**：项目名称、项目类型等维护页面。
9. **P3 — 变更日志**：`requirement_feature_logs` 的写入与查询展示。

---

## 八、关键交互与边界条件

- 需求状态为**待发布**时，必须校验已关联发布迭代，否则报错。
- 纳入发布清单时，需求状态自动变为**待发布**；从发布库移除时，状态回退到**业务测试**或**综合测试**。
- 开发进度、测试进度在查询时实时计算（或在变更时异步更新缓存）。
- 只有需求的**创建者/业务负责人/PM** 才能修改需求基础信息；只有**对应开发人员**才能修改 Bug；只有**对应测试人员**才能关闭 Bug。
- 功能点状态自动流转：开发任务完成 → 功能点变为待测试；测试任务完成且无 Bug → 功能点变为已关闭。
- 所有功能点变为已关闭后，需求状态才允许被手动改为**已发布**。

---

## 九、现有系统已提供的可复用能力

- **认证与授权**：JWT Filter + Spring Security，角色 `pm`, `dev_lead`, `dev`, `tester_lead`, `tester`
- **统一异常处理**：`GlobalExceptionHandler`
- **统一返回**：`Result<T>`
- **前端布局**：`AppLayout.vue`（含侧边栏导航）
- **前端请求拦截**：`request.js`（Token、401 处理、错误提示）
- **前端状态**：`useAuthStore`（用户信息、角色判断 `isPM`, `isDevLead` 等）
- **前端路由守卫**：已鉴权 + PM 权限控制

---

> **使用方式**：将本提示词直接喂给 AI 开发助手，指示其按照上述技术栈、业务规则、数据模型和 UI 风格，逐步实现各功能模块。每次开发单个模块时，可截取对应章节作为上下文。
