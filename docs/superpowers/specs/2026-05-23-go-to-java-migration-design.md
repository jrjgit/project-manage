# Go → Java 后端重构设计

## 概述

将 `backend/` 从 Go (Gin + GORM + SQLite) 重构为 Java (Spring Boot 3.2 + MyBatis-Plus + MySQL)，新建独立目录 `backend-java/`，完全重写。

**核心约束**：RESTful API 的路径、请求方法、参数及响应结构必须与原 Go 后端完全一致，确保 `frontend/` 前端代码无需修改即可对接。

---

## 技术选型

| 维度 | 选择 |
|------|------|
| Java 版本 | Java 17 |
| 框架 | Spring Boot 3.2.7 |
| 数据库 | MySQL 8.x |
| ORM | MyBatis-Plus 3.5.7（`mybatis-plus-spring-boot3-starter`） |
| 安全 | Spring Security 6 + JJWT 0.12.6（无状态 JWT） |
| 构建工具 | Maven |
| 校验 | spring-boot-starter-validation |
| 邮件 | spring-boot-starter-mail |
| 测试 | JUnit 5 + Mockito + H2（内存数据库） |
| 工具 | Lombok |

---

## 目录结构（按业务领域分包）

```
backend-java/
├── pom.xml
├── src/main/java/com/management/
│   ├── ManagementApplication.java
│   ├── common/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java            # Spring Security 无状态 JWT
│   │   │   ├── MyBatisPlusConfig.java         # 分页插件
│   │   │   ├── CorsConfig.java                # CORS
│   │   │   ├── WebMvcConfig.java              # 静态资源 + SPA fallback
│   │   │   ├── AsyncConfig.java               # @Async 线程池
│   │   │   └── PasswordEncoderConfig.java     # BCrypt
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java    # @RestControllerAdvice
│   │   │   └── BusinessException.java         # 业务异常
│   │   ├── result/
│   │   │   └── Result.java                    # 统一响应体
│   │   ├── jwt/
│   │   │   ├── JwtUtils.java                  # 生成/解析
│   │   │   ├── JwtAuthFilter.java             # OncePerRequestFilter
│   │   │   └── JwtUserDetails.java            # UserDetails 实现
│   │   ├── workflow/
│   │   │   ├── WorkflowRule.java              # Entity: workflow_rules 表
│   │   │   ├── WorkflowRuleMapper.java
│   │   │   └── WorkflowService.java           # 带缓存的流转校验
│   │   └── notification/
│   │       ├── Notifier.java                  # 通知接口
│   │       ├── EmailNotifier.java
│   │       ├── NanobotNotifier.java
│   │       ├── LogNotifier.java
│   │       ├── MultiNotifier.java
│   │       └── NotificationService.java       # 事件通知编排
│   ├── auth/
│   │   ├── AuthController.java
│   │   ├── AuthService.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── LoginResponse.java
│   ├── task/
│   │   ├── TaskController.java
│   │   ├── TaskService.java
│   │   ├── entity/
│   │   │   ├── Task.java
│   │   │   ├── TaskAssignee.java
│   │   │   └── TaskStatusHistory.java
│   │   ├── mapper/
│   │   │   ├── TaskMapper.java
│   │   │   ├── TaskAssigneeMapper.java
│   │   │   └── TaskStatusHistoryMapper.java
│   │   └── dto/
│   │       ├── CreateTaskRequest.java
│   │       ├── UpdateTaskRequest.java
│   │       ├── ChangeTaskStatusRequest.java
│   │       └── AddTaskAssigneeRequest.java
│   ├── bug/
│   │   ├── BugController.java
│   │   ├── BugService.java
│   │   ├── entity/Bug.java, BugStatusHistory.java
│   │   ├── mapper/BugMapper.java, BugStatusHistoryMapper.java
│   │   └── dto/CreateBugRequest.java, UpdateBugRequest.java, ChangeBugStatusRequest.java
│   ├── project/
│   │   ├── ProjectController.java, ProjectService.java
│   │   ├── entity/Project.java, mapper/ProjectMapper.java
│   │   └── dto/CreateProjectRequest.java, UpdateProjectRequest.java
│   ├── user/
│   │   ├── UserController.java, UserService.java
│   │   ├── entity/User.java, mapper/UserMapper.java
│   └── group/
│       ├── GroupController.java, GroupService.java
│       ├── entity/Group.java, mapper/GroupMapper.java
│       └── dto/CreateGroupRequest.java, UpdateGroupRequest.java, AddMemberRequest.java
└── src/main/resources/
    ├── application.yml
    ├── application-dev.yml
    └── db/migration/V1__init_schema.sql
```

---

## 数据库表结构

### 对等迁移（Go models → MySQL）

| Go Model (GORM) | MySQL Table | 关键变化 |
|-----------------|-------------|---------|
| models.User | users | uint → BIGINT, *uint → nullable BIGINT |
| models.Project | projects | PMID → pm_id (snake_case) |
| models.Task | tasks | assignee_id/dev_lead_id 等 *uint → nullable |
| models.TaskAssignee | task_assignees | 新增连接表 |
| models.TaskStatusHistory | task_status_histories | — |
| models.Bug | bugs | — |
| models.BugStatusHistory | bug_status_histories | — |
| models.Group | groups | — |
| — | workflow_rules | **新增**：流转规则配置表 |

### workflow_rules 表

```sql
CREATE TABLE workflow_rules (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_type  VARCHAR(20)  NOT NULL COMMENT 'task | bug',
    role       VARCHAR(20)  NOT NULL,
    from_status VARCHAR(30) NOT NULL,
    to_status  VARCHAR(30)  NOT NULL,
    UNIQUE KEY uk_rule (rule_type, role, from_status, to_status)
);
```

### Entity 字段类型映射

- Go `uint` → Java `Long`（`BIGINT AUTO_INCREMENT`）
- Go `*uint` → Java `Long`（nullable）
- Go `time.Time` → Java `LocalDateTime`
- Go GORM `foreignKey` 关联 → `@TableField(exist = false)` + 手动组装

---

## API 路由对照（30 个端点）

所有 URL、方法、参数与 Go 版一致。角色校验使用 `@PreAuthorize` 注解。

| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| POST | /api/auth/register | 公开 | 注册 |
| POST | /api/auth/login | 公开 | 登录，返回 JWT |
| GET | /api/tasks | 认证 | 列表（按角色过滤） |
| POST | /api/tasks | PM | 创建 |
| GET | /api/tasks/{id} | 认证 | 详情 |
| PUT | /api/tasks/{id} | 认证 | 更新 |
| PATCH | /api/tasks/{id}/status | 认证 | 变更状态 |
| GET | /api/tasks/{id}/history | 认证 | 状态历史 |
| POST | /api/tasks/{id}/assignees | PM/DevLead | 添加指派人 |
| DELETE | /api/tasks/{id}/assignees/{userId} | PM/DevLead | 移除指派人 |
| GET | /api/bugs | 认证 | 列表 |
| POST | /api/bugs | Tester/TesterLead | 创建 |
| GET | /api/bugs/{id} | 认证 | 详情 |
| PUT | /api/bugs/{id} | 认证 | 更新 |
| PATCH | /api/bugs/{id}/status | 认证 | 变更状态 |
| GET | /api/bugs/{id}/history | 认证 | 状态历史 |
| GET | /api/users | 所有角色 | 用户列表 |
| GET | /api/users/{id} | PM | 用户详情 |
| PUT | /api/users/{id}/role | PM | 修改角色 |
| GET/POST/PUT/DELETE | /api/projects | PM(写)/认证(读) | CRUD |
| GET/POST/PUT/DELETE | /api/groups | PM(写)/认证(读) | 含 my-team, members |

---

## 安全架构

### 认证流程

1. 前端发送 `Authorization: Bearer <token>`
2. `JwtAuthFilter`（`OncePerRequestFilter`）解析 JWT，提取 `{userId, name, role, groupId}`
3. 构造 `JwtUserDetails` 封入 `SecurityContextHolder`
4. 之后的 `@PreAuthorize("hasRole('PM')")` 和 `SecurityConfig` 的 `authenticated()` 规则生效

### 401/403 响应格式

```json
{"error":"未登录或令牌已过期"}    // 401
{"error":"无权限执行此操作"}      // 403
```

与前端 axios 拦截器期望的 `error.response.data.error` 格式一致。

---

## 核心业务逻辑：状态机

### 流转规则存储

流转规则不在代码中硬编码，也不在 yml 中配置，而是存入 `workflow_rules` 表。`WorkflowService` 在内存中维护缓存（`Map` 结构），提供 `isAllowed(role, fromStatus, toStatus, type)` 校验方法。

### 缓存刷新时机

- 应用启动时：从数据库加载并构建缓存
- 管理页面保存规则时：调用 `refreshCache()` 实时刷新

### 自动跳转

在 `@Transactional` 方法内保留原 Go 逻辑：
- **Task**: `developed` → 自动创建 `pending_test` 状态历史
- **Bug**: `fixed` → 自动创建 `pending_verify` 状态历史

### 通知触发

状态变更后，`NotificationService.emitTaskEvent/emitBugEvent` 计算通知目标并发送。通知目标计算逻辑完全对等原 Go `resolveTaskNotifyTargets/resolveBugNotifyTargets`。

---

## 通知系统

原 Go 6 个文件 → Java 6 个类，职责不变：

- `Notifier` 接口
- `EmailNotifier`（spring-boot-starter-mail）
- `NanobotNotifier`（ProcessBuilder 调用 CLI）
- `LogNotifier`
- `MultiNotifier`（组合模式）
- 异步改用 `@Async("notifyExecutor")` 注解（替代 Go goroutine+channel）

---

## 统一响应格式

Controller 返回 `Result<T>`，序列化为：

```json
{"data": {...}, "error": null}        // 成功
{"data": null, "error": "错误描述"}    // 失败
```

`GlobalExceptionHandler` 统一处理 `BusinessException`、`MethodArgumentNotValidException`、`AccessDeniedException` 及其他异常，始终返回上述结构。

---

## 初始化数据

`DataInitializer`（`ApplicationRunner`）在首次启动时执行：
1. 检查无 PM 用户时创建默认管理员 `admin/admin123`
2. 检查 `workflow_rules` 为空表时插入默认流转规则

---

## 测试策略

| 层级 | 类型 | 工具 | 覆盖重点 |
|------|------|------|---------|
| Service | 单元测试 | JUnit 5 + Mockito | 状态机逻辑、流转校验、通知目标计算 |
| Mapper | 集成测试 | @MybatisPlusTest + H2 | SQL 正确性、分页、关联查询 |
| Controller | 集成测试 | @SpringBootTest + MockMvc | API 兼容性、角色校验、错误响应格式 |

---

## 配置管理

Go `config/config.go` 中的环境变量 → Spring Boot `application.yml` 中的 `${ENV:default}` 占位符，所有配置项映射如下：

| Go 环境变量 | yml 配置项 | 默认值 |
|------------|-----------|-------|
| PORT | server.port | 8080 |
| DB_PATH | 废弃（改用 spring.datasource.url） | — |
| JWT_SECRET | app.jwt-secret | your-secret-key-change-in-production |
| NANOBOT_PATH | app.nanobot-path | nanobot |
| SMTP_HOST/PORT/USER/PASSWORD/FROM | spring.mail.* | — |
| NOTIFY_EMAIL_ENABLE | app.notification.email-enable | false |
| NOTIFY_NANOBOT_ENABLE | app.notification.nanobot-enable | true |
| NOTIFY_ASYNC | app.notification.async | true |

---

## 实现顺序建议

1. 项目骨架 + pom.xml + application.yml + 数据源配置
2. common 包：Result、BusinessException、GlobalExceptionHandler
3. JWT 认证链路：JwtUtils → JwtUserDetails → JwtAuthFilter → SecurityConfig
4. 数据层：Entity/Mapper + 建表 + DataInitializer
5. 核心业务层（按依赖顺序）：
   - auth（无依赖）
   - user（无依赖）
   - project（依赖 user）
   - group（依赖 user）
   - task（依赖 user/project/workflow，最复杂）
   - bug（依赖 task/workflow）
6. workflow_rules 表 + WorkflowService
7. 通知系统
8. CORS + 静态资源服务 + SPA fallback
9. 测试
