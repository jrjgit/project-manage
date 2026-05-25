# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository structure

This repository has two main parts:

- `backend-java/`: Java 17 + Spring Boot 3.2 + MyBatis-Plus + MySQL backend
- `frontend/`: Vue 3 + Vite + Pinia + Naive UI single-page app

## Common commands

### Java Backend (`backend-java/`)

```bash
mvnw spring-boot:run              # 启动（默认 profile）
mvnw test                         # 运行所有测试（使用 H2 内存数据库）
```

### Frontend (`frontend/`)

```bash
npm install
npm run dev       # Vite 开发服务器，地址 127.0.0.1:5173
npm run build     # 输出到 dist/
```

## Architecture overview

### Backend request flow

The backend starts in `backend-java/` with Spring Boot and wires together:

1. JWT authentication filter and role checks
2. REST controllers for auth, tasks, bugs, users, projects, requirements, features, iterations, dictionary, statistics
3. MyBatis-Plus for database access
4. Workflow rules driven by database table, not hardcoded

### Core domain model

- `User`: roles include `pm`, `dev_lead`, `dev`, `tester_lead`, `tester`
- `Project`: projects belong to a PM
- `Task`: project work items with assignee, status history, workflow transitions
- `Bug`: defects linked to a task
- `Requirement` / `Feature`: requirement management with two-level breakdown
- `Iteration`: sprint/iteration management
- `Dictionary`: system configuration key-value pairs

Status history is persisted separately for audit trails.

### Notification behavior

Notifications support email (SMTP) and nanobot (WeChat Work command-line tool), both wrapped as async adapters.

### Frontend structure

The frontend is a Vue SPA with these main pieces:

- `src/main.js`: app bootstrap, Pinia, Router, Naive UI global discrete APIs on `window`
- `src/router/index.js`: route table with auth and PM-only navigation guards
- `src/store/useAuthStore.js`: token persistence, current-user state
- `src/api/`: axios wrappers around backend resources
- `src/views/`: page-level screens (Login, Dashboard, Projects, Tasks, Bugs, Users, Requirements, Iterations, Dictionary, Reports)
- `src/components/`: reusable workflow UI components

Axios is configured with base URL `/api`, auto-injects bearer token, and forces logout on 401.

## Frontend/backend integration notes

- Vite dev server proxies `/api` → `http://127.0.0.1:8080` (configured in `vite.config.js`)
- Backend responses use `Result<T>` format: `{data: ...}`
- Axios interceptor auto-unwraps `response.data`

## Testing

- Java backend tests use H2 in-memory database (MySQL mode) + MockMvc
- No frontend test setup
