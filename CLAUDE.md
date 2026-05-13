# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Repository structure

This repository has two main parts:

- `backend/`: Go 1.21 application using Gin + GORM + SQLite
- `frontend/`: Vue 3 + Vite + Pinia + Naive UI single-page app

The backend is the system entrypoint in production-style usage: it exposes the `/api` endpoints and, when `frontend/dist` exists, also serves the built frontend assets from the same process.

## Common commands

### Backend (`backend/`)

Install dependencies:

```bash
go mod download
```

Run the backend locally:

```bash
go run .
```

Build the backend:

```bash
go build -o management.exe .
```

Run all backend tests:

```bash
go test ./...
```

Run a single backend test:

```bash
go test ./handlers -run TestName
```

### Frontend (`frontend/`)

Install dependencies:

```bash
npm install
```

Start the Vite dev server:

```bash
npm run dev
```

Build the frontend:

```bash
npm run build
```

Preview the production build:

```bash
npm run preview
```

## Runtime configuration

Backend configuration is loaded from environment variables in `backend/config/config.go`:

- `PORT` defaults to `8080`
- `DB_PATH` defaults to `./management.db`
- `JWT_SECRET` defaults to `your-secret-key-change-in-production`
- `NANOBOT_PATH` defaults to `nanobot`

The SQLite database file is created locally and schema setup happens via GORM `AutoMigrate` during startup.

## Architecture overview

### Backend request flow

The backend starts in `backend/main.go` and wires together:

1. configuration loading
2. SQLite initialization via `backend/db/database.go`
3. route registration for auth, tasks, bugs, users, and projects
4. JWT authentication middleware and role checks
5. static frontend hosting from `frontend/dist` when that build output exists

The backend is organized by responsibility rather than by framework layer depth:

- `config/`: environment-based app configuration
- `db/`: database initialization and shared GORM handle
- `models/`: persistent entities and workflow status constants
- `dto/`: request payload structs for handlers
- `middlewares/`: JWT parsing plus role-based access control
- `handlers/`: HTTP endpoints and business workflow orchestration
- `services/`: cross-cutting services, currently notification dispatch

### Core domain model

The app is a lightweight workflow management system centered around:

- `User`: users have roles such as `pm`, `dev_lead`, `dev`, `tester_lead`, `tester`
- `Project`: projects belong to a PM
- `Task`: project work items with explicit assignee, dev lead, tester lead, tester, deadline, and status history
- `Bug`: defects linked to a task, with creator/assignee and status history

Status history is persisted separately for both tasks and bugs, so status changes are treated as auditable workflow events, not just field updates.

### Workflow rules live in handlers

The most important business logic is in `backend/handlers/task.go` and `backend/handlers/bug.go`.

Those files do more than HTTP I/O:

- define the allowed status transition graphs
- enforce role-based transition permissions
- persist status history rows inside DB transactions
- compute notification targets after transitions

For tasks, `developed -> pending_test` includes an automatic second transition performed inside the same transactional flow. If you change task workflow behavior, inspect both the explicit transition map and the auto-transition block.

### Notification behavior

`backend/services/notification.go` builds human-readable messages for task and bug state changes and routes them to users with `WechatID` set.

Important detail: the default notification path is currently log-only simulation. The real `nanobot` command execution exists but is not enabled by default.

### Frontend structure

The frontend is a Vue SPA with these main pieces:

- `src/main.js`: app bootstrap, Pinia registration, router registration, Naive UI global discrete APIs on `window`
- `src/router/index.js`: route table and auth / PM-only navigation guards
- `src/store/useAuthStore.js`: token persistence, current-user state, role-derived computed flags
- `src/api/`: thin axios wrappers around backend resources
- `src/views/`: page-level screens (`Login`, `Dashboard`, `Projects`, `Tasks`, `Bugs`, `Users`)
- `src/components/`: reusable workflow UI such as task board/cards, drawers, dialogs, and layout

Axios is configured in `src/api/request.js` with base URL `/api`, injects the bearer token from Pinia/localStorage, and forces logout on `401`.

## Frontend/backend integration notes

- Production-style integration assumes the backend serves the built frontend from `frontend/dist`.
- `frontend/vite.config.js` does not define a dev proxy for `/api`. If you run `npm run dev` separately, API requests still target `/api` on the frontend origin, so you may need a reverse proxy or another same-origin setup during local development.

## Testing state

- There are currently no repository-authored Go test files under `backend/`.
- There is no frontend test setup in the app source; any matches under `frontend/node_modules/` are dependency tests, not project tests.

When adding tests, prefer commands that scope narrowly to the affected package or file because the repository does not currently define a higher-level test runner wrapper.
