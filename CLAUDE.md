# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Full-stack social media platform: React 19 frontend + Spring Boot 3.2 microservices backend + WeChat mini-program.

```
shuaiqi-qi/
├── frontend/                 # React 19 + Vite 8 + TypeScript 5.9
├── backend/                 # Spring Boot 3.2 + Spring Cloud microservices
├── wechatweb/              # WeChat mini-program (Glass-Easel)
├── docker-compose.yml       # All services orchestration
└── AGENTS.md               # Chinese AI assistant guidelines (detailed)
```

## Build Commands

### Frontend (frontend/)
```bash
npm install              # Install deps
npm run dev               # Dev server port 3000
npm run build             # Production build
npm run lint              # ESLint check
npm run preview            # Preview build
```

### Backend (backend/)
```bash
mvn clean install         # Build all modules
mvn test                  # Run all tests
mvn test -pl user-service # Run single module tests
mvn spring-boot:run -pl gateway  # Run single service
```

### WeChat Mini-Program
Open `wechatweb/` in WeChat DevTools. Build via IDE, no CLI.

## Architecture

### Frontend (port 3000)
- **React 19** + TypeScript strict mode + Vite 8
- **State**: Zustand 5 with localStorage persistence
- **Routing**: React Router 7 (lazy-loaded, auth-guarded)
- **HTTP**: Axios with interceptors + token refresh
- **UI**: Ant Design 6 (Chinese locale)

### Backend Microservices
- **Gateway** (8080): API gateway, all requests route through here
- **Auth Service** (8081): JWT authentication, token refresh
- **User Service** (8082): User profiles, follow system
- **Content Service** (8083): Posts, likes, favorites
- **Comment Service** (8084): Nested comments
- **Notification Service** (8085): WebSocket real-time notifications

### Data Layer
- **Registry**: Nacos 2.3
- **Database**: MySQL 8.0
- **Cache**: Redis 7
- **ORM**: MyBatis Plus 3.5

### WeChat Mini-Program
- **Rendering**: Glass-Easel (not WebView)
- **Auth**: WeChat login via `wx.login()` + backend token exchange
- **API Base**: `http://localhost:8080/api`

## Path Aliases (frontend)

```
@/           -> ./src/
@components/ -> ./src/components/
@layouts/    -> ./src/layouts/
@pages/      -> ./src/pages/
@hooks/      -> ./src/hooks/
@services/   -> ./src/services/
@stores/     -> ./src/stores/
@utils/      -> ./src/utils/
@types/      -> ./src/types/
@styles/     -> ./src/styles/
```

## Key Conventions

### TypeScript/React
- Props interface: `ComponentNameProps`
- Component type: `React.FC<Props>`
- Avoid `any`; use explicit types
- Strict TypeScript enabled

### API Layer
- All API calls via `request.ts` (axios wrapper)
- Automatic 401 handling + token refresh
- Proxies `/api` to `http://localhost:8080`

### Backend
- Unified response: `Result<T>`
- Package structure: `com.shuaiqi.{service}.{module}`
- Lombok for boilerplate

## Default Credentials

Admin: `admin` / `admin123`

## Important Notes

1. Frontend uses `npm` (not pnpm/yarn)
2. All commits go directly to `main` branch
3. Chinese comments used throughout codebase
4. Vite proxy config: `/api` -> `http://localhost:8080`
5. WeChat mini-program uses `Component()` constructor, not `Page()`
