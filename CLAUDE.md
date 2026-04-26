# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目架构

```
ShuaiQIQI/
├── backend/           # Java Spring Boot 后端 (多模块 Maven 项目)
│   ├── src/           # 单体后端入口 (仍在使用)
│   ├── auth-service/  # 认证服务
│   ├── user-service/  # 用户服务
│   ├── content-service/# 内容服务
│   ├── comment-service/# 评论服务
│   ├── notification-service/# 通知服务
│   ├── gateway/       # API 网关
│   ├── common/        # 公共配置
│   └── common-core/   # 公共代码 (异常、Result)
├── frontend/          # React 19 + Vite + TypeScript 前端
└── docker-compose.yml # Docker 部署配置
```

**技术栈:**
- Backend: Java 17, Spring Boot 3.1.5, MyBatis Plus 3.5.6, JWT (jjwt 0.12.3)
- Frontend: React 19, Vite 5, TypeScript, Ant Design 6, Zustand (状态管理), React Router 7
- 中间件: MySQL 8.0, Redis 7, Nginx

## 常用命令

### 后端
```bash
cd backend
mvn clean compile          # 编译
mvn test                   # 运行测试
mvn spring-boot:run        # 本地运行单体后端 (端口 8080)
```

### 前端
```bash
cd frontend
npm install                # 安装依赖
npm run dev                # 开发模式 (端口 3000)
npm run build              # 生产构建
npm run lint               # ESLint 检查
```

### Docker 部署
```bash
docker-compose up -d       # 启动所有服务
docker-compose down       # 停止服务
docker-compose ps         # 查看服务状态
```

## 重要配置

- 后端配置: `backend/src/main/resources/application.yml`
- 环境变量: `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, `REDIS_HOST`, `REDIS_PORT`, `JWT_SECRET_KEY`
- Swagger: 启用 `SWAGGER_ENABLED=true` 后访问 `/swagger-ui.html`

## API 路由

前端 API 代理到 `/api/*` → 后端 `:8080`

## 前端状态管理

Zustand store 在 `src/store/` 目录 (如存在)