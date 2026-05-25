# AGENTS.md

## 交互要求

1. 处理所有问题时，**全程思考过程必须使用中文**（包括需求分析、逻辑拆解、方案选择、步骤推导等所有内部推理环节）；
2. 最终输出的所有回答内容（包括文字解释、代码注释、步骤说明等）**必须全部使用中文**，仅代码语法本身的英文关键词除外。

## 一个后端，一个前端

- `backend-java/` — Java 17, Spring Boot 3.2 + MyBatis-Plus + MySQL。功能完整。
- `frontend/` — Vue 3 + Vite + Pinia + Naive UI。

后端暴露 `/api` 接口。前端期望响应为 `Result<T>` JSON 包装格式（`{data: ...}`）。

## 命令

### Java 后端（`backend-java/`）

使用自定义 JDK，路径为 `D:\repository\jdk17\`。需要运行中的 MySQL 实例。

```bash
mvnw spring-boot:run              # 启动（默认 profile）
mvnw test                         # 运行所有测试（使用 H2 内存数据库）
```

测试使用 `@ActiveProfiles("test")` → H2 内存数据库，MySQL 兼容模式，显式 `schema.sql`。

### 前端（`frontend/`）

```bash
npm install
npm run dev       # Vite 开发服务器，地址 127.0.0.1:5173
npm run build     # 输出到 dist/
```

Vite 开发服务器代理 `/api` → `http://127.0.0.1:8080`（参见 `vite.config.js`）。

## 工作流规则

工作流规则存储在 `workflow_rules` 表中，由 `WorkflowService` 缓存。处理器代码中没有硬编码流转逻辑。默认规则由 `DataInitializer` 在首次启动时种入（仅在表为空时）。

额外模块：Requirement（需求）、Feature（特性）、Iteration（迭代）、Dictionary（字典）、Statistics（统计）。

## 认证

- JWT，HS256 签名，24 小时过期。Token 存储在前端 `localStorage` 中。
- JWT 载荷：`user_id`、`name`、`role`、`group_id`。
- 角色：`pm`（项目经理）、`dev_lead`（开发组长）、`dev`（开发）、`tester_lead`（测试组长）、`tester`（测试）。

首次运行自动创建默认管理员：`admin` / `admin123`（角色 `pm`）。

## 配置

Java 后端使用 `application.yml`，通过 `${ENV_VAR:默认值}` 占位符读取环境变量。

## 前端注意事项

- Axios 响应拦截器（`src/api/request.js`）自动拆解 `{data: ...}` → 如果存在 `body.data` 则返回 `body.data`。
- 401 响应强制登出并跳转到 `/login`。
- Naive UI 离散式 API 挂载在 `window.$message`、`window.$dialog`、`window.$notification` 上。

## 测试

- Java：H2 内存数据库（MySQL 模式）+ `application-test.yml` + `schema.sql`。测试使用 `MockMvc`。
- 无前端测试。

## 环境

- Java 后端需要运行中的 MySQL，数据库名为 `management`（可通过环境变量配置）。
- 通知：邮件（通过 SMTP）和 nanobot（企业微信命令行工具）。两者默认均包装为异步适配器。
