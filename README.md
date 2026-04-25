# 帅气气 - 全栈社交媒体平台

一个前后端分离的全栈社交媒体/内容分享平台，支持内容发布、评论互动、关注系统、实时通知等功能。

## 项目结构

```
shuaiqi-qi/
├── frontend/                    # React 前端
│   ├── src/
│   │   ├── components/          # 通用组件
│   │   ├── hooks/               # 自定义 Hooks
│   │   ├── layouts/             # 布局组件
│   │   ├── pages/               # 页面组件
│   │   │   └── admin/           # 后台管理页面
│   │   ├── services/            # API 服务
│   │   ├── stores/              # Zustand 状态管理
│   │   ├── types/               # TypeScript 类型定义
│   │   ├── router/              # 路由配置
│   │   └── utils/               # 工具函数
│   ├── Dockerfile
│   └── nginx.conf
├── backend/                     # Spring Boot 后端
│   ├── common/                  # 公共模块
│   ├── gateway/                 # API 网关 (端口 8080)
│   ├── auth-service/            # 认证服务 (端口 8081)
│   ├── user-service/            # 用户服务 (端口 8082)
│   ├── content-service/         # 内容服务 (端口 8083)
│   ├── comment-service/         # 评论服务 (端口 8084)
│   ├── notification-service/    # 通知服务 (端口 8085)
│   └── sql/
│       └── init.sql             # 数据库初始化脚本
├── docker-compose.yml           # Docker 编排
└── deploy.sh                    # 部署脚本
```

## 功能特性

### 用户系统
- 用户注册、登录、登出
- 忘记密码、重置密码
- 个人资料编辑、头像上传
- 用户关注/取消关注
- 粉丝列表查看

### 内容系统
- 内容发布（标题、摘要、正文、封面）
- 内容分类管理
- 内容列表浏览、搜索
- 热门/推荐内容
- 内容点赞/取消点赞
- 内容收藏/取消收藏

### 评论系统
- 发表评论
- 回复评论（多级评论）
- 评论点赞

### 通知系统
- 实时 WebSocket 通知
- 评论通知
- 点赞通知
- 收藏通知
- 关注通知
- 未读消息计数

### 后台管理
- 数据仪表盘
- 内容管理
- 用户管理
- RBAC 权限控制

## 技术栈

### 前端
- **框架**: React 19 + TypeScript 5.9
- **构建工具**: Vite 8
- **UI 库**: Ant Design 6
- **状态管理**: Zustand 5
- **路由**: React Router 7
- **HTTP 客户端**: Axios

### 后端
- **框架**: Spring Boot 3.2
- **微服务**: Spring Cloud 2023.0
- **注册中心**: Nacos 2.3
- **ORM**: MyBatis Plus 3.5
- **数据库**: MySQL 8.0
- **缓存**: Redis 7
- **认证**: JWT
- **API 文档**: Swagger 3

## 快速开始

### 环境要求

- Node.js 18+
- npm 8+
- Java 17
- Maven 3.6+
- MySQL 8.0
- Redis 7

### 方式一：Docker 部署（推荐）

```bash
# 克隆项目
git clone https://github.com/yourusername/shuaiqi-qi.git
cd shuaiqi-qi

# 启动所有服务
docker-compose up -d

# 访问应用
# 前端: http://localhost:3000
# API 网关: http://localhost:8080
# Nacos 控制台: http://localhost:8848
```

### 方式二：本地开发

#### 1. 数据库初始化

```bash
# 创建数据库并导入初始数据
mysql -u root -p < backend/sql/init.sql
```

#### 2. 启动后端服务

```bash
# 启动 Nacos（需先安装）
# 启动 MySQL 和 Redis

# 构建后端
cd backend
mvn clean install

# 启动各个服务（分别在不同终端）
cd gateway && mvn spring-boot:run          # 端口 8080
cd auth-service && mvn spring-boot:run     # 端口 8081
cd user-service && mvn spring-boot:run     # 端口 8082
cd content-service && mvn spring-boot:run  # 端口 8083
cd comment-service && mvn spring-boot:run  # 端口 8084
cd notification-service && mvn spring-boot:run  # 端口 8085
```

#### 3. 启动前端

```bash
cd frontend
npm install
npm run dev    # 端口 3000
```

## 页面路由

### 前台页面

| 路由 | 说明 |
|------|------|
| `/` | 首页（内容推荐） |
| `/content` | 内容列表 |
| `/content/:id` | 内容详情 |
| `/content/create` | 发布内容 |
| `/profile` | 个人中心 |
| `/profile/settings` | 设置页 |
| `/notification` | 消息通知 |
| `/user/:userId` | 用户主页 |
| `/login` | 登录页 |
| `/register` | 注册页 |
| `/forgot-password` | 忘记密码 |

### 后台管理

| 路由 | 说明 |
|------|------|
| `/admin` | 仪表盘 |
| `/admin/content` | 内容管理 |
| `/admin/user` | 用户管理 |

## API 接口

所有 API 通过网关统一访问：`http://localhost:8080/api`

| 模块 | 路径前缀 | 说明 |
|------|---------|------|
| 认证 | `/api/auth/` | 登录、注册、登出、Token 刷新、密码重置 |
| 用户 | `/api/user/` | 用户信息、头像上传、密码修改、关注系统 |
| 内容 | `/api/content/` | 内容 CRUD、点赞、收藏、分类 |
| 评论 | `/api/comment/` | 评论 CRUD、点赞 |
| 通知 | `/api/notification/` | 通知列表、标记已读、未读计数 |

## 数据库设计

共 6 张核心表：

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `user_follow` | 用户关注表 |
| `category` | 内容分类表 |
| `content` | 内容表 |
| `comment` | 评论表 |
| `notification` | 通知表 |

详细结构见 `backend/sql/init.sql`

## 开发规范

### 前端
- 使用 TypeScript 严格模式
- 组件使用 `React.FC` 类型
- 使用路径别名（`@/`、`@components/` 等）
- 优先使用 Ant Design 组件
- 状态管理使用 Zustand

### 后端
- 使用 Lombok 注解
- 统一响应格式 `Result<T>`
- 使用 MyBatis Plus 操作数据库
- RESTful API 设计规范

## 默认账号

管理员账号：
- 用户名: `admin`
- 密码: `admin123`

## 许可证

MIT License
