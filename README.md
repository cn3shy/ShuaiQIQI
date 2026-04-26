# ShuaiQIQI

内容社区平台。前端 React + 后端 Spring Boot。

## 快速启动

### Docker 部署 (推荐)

```bash
./deploy.sh
```

服务地址:
- 前端: http://localhost
- 后端 API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

### 本地开发

**后端:**
```bash
cd backend
mvn spring-boot:run
```

**前端:**
```bash
cd frontend
npm install
npm run dev
```

## 环境要求

- Docker & Docker Compose
- MySQL 8.0 (或使用 Docker)
- Redis 7 (或使用 Docker)

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| DB_HOST | localhost | MySQL 地址 |
| DB_PORT | 3306 | MySQL 端口 |
| DB_USERNAME | root | 数据库用户名 |
| DB_PASSWORD | root | 数据库密码 |
| REDIS_HOST | localhost | Redis 地址 |
| REDIS_PORT | 6379 | Redis 端口 |
| JWT_SECRET_KEY | - | JWT 密钥 (必填生产环境) |
| SWAGGER_ENABLED | false | 启用 Swagger |

## 技术栈

- **后端:** Java 17, Spring Boot 3.1.5, MyBatis Plus, JWT
- **前端:** React 19, Vite 5, TypeScript, Ant Design 6, Zustand
- **中间件:** MySQL 8.0, Redis 7, Nginx