# 帅气气 - 全栈开发项目

## 项目结构

```
shuaiqi-qi/
├── frontend/          # 前端项目 (React + Vite + Ant Design)
└── backend/           # 后端项目 (Spring Boot + Spring Cloud)
```

## 前端开发

### 启动开发服务器

```bash
cd frontend
pnpm dev
# 访问 http://localhost:3001 (或提示的端口)
```

### 可用页面

- `/` - 首页（内容推荐）
- `/login` - 登录页
- `/register` - 注册页
- `/content` - 内容列表
- `/content/create` - 发布内容
- `/content/:id` - 内容详情
- `/profile` - 个人中心
- `/profile/settings` - 设置页

### 技术栈

- React 18 + TypeScript
- Vite
- Ant Design (antd)
- Zustand (状态管理)
- React Router v6
- Axios (HTTP客户端)

## 后端开发

### 环境要求

- Java 17
- Maven 3.6+
- MySQL 8.0
- Redis
- Nacos (服务注册与配置中心)

### 启动步骤

1. **启动Nacos**
   ```bash
   # 下载并启动Nacos
   # 访问 http://localhost:8848
   ```

2. **启动网关服务**
   ```bash
   cd backend/gateway
   mvn spring-boot:run
   # 端口: 8080
   ```

3. **启动其他服务**
   ```bash
   cd backend/auth-service && mvn spring-boot:run    # 端口: 8081
   cd backend/user-service && mvn spring-boot:run    # 端口: 8082
   cd backend/content-service && mvn spring-boot:run # 端口: 8083
   cd backend/comment-service && mvn spring-boot:run # 端口: 8084
   ```

### API网关

所有后端服务通过网关访问：`http://localhost:8080/api`

## 数据库脚本

```sql
CREATE DATABASE IF NOT EXISTS shuaiqi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `email` VARCHAR(100) NOT NULL UNIQUE,
  `phone` VARCHAR(20),
  `avatar` VARCHAR(500),
  `bio` VARCHAR(500),
  `password` VARCHAR(100) NOT NULL,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 内容表
CREATE TABLE IF NOT EXISTS `content` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `title` VARCHAR(200) NOT NULL,
  `summary` VARCHAR(500),
  `content` TEXT,
  `cover_image` VARCHAR(500),
  `author_id` BIGINT NOT NULL,
  `category_id` BIGINT,
  `like_count` INT DEFAULT 0,
  `favorite_count` INT DEFAULT 0,
  `comment_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_author` (`author_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content` TEXT NOT NULL,
  `content_id` BIGINT NOT NULL,
  `user_id` BIGINT NOT NULL,
  `parent_id` BIGINT,
  `like_count` INT DEFAULT 0,
  `status` TINYINT DEFAULT 1,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX `idx_content` (`content_id`),
  INDEX `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 响应式设计

### 断点设置

| 名称 | 宽度 | 设备 |
|------|------|------|
| xs | < 576px | 手机竖屏 |
| sm | 576px - 767px | 手机横屏 |
| md | 768px - 991px | 平板竖屏 |
| lg | 992px - 1199px | 平板横屏/小屏PC |
| xl | 1200px - 1599px | 桌面 |
| xxl | >= 1600px | 大屏桌面 |

## 目录结构

### 前端
```
frontend/src/
├── components/     # 通用组件
├── layouts/        # 布局组件
├── pages/          # 页面组件
├── services/       # API服务
├── stores/         # 状态管理
├── types/          # TypeScript类型定义
└── styles/         # 全局样式
```

### 后端
```
backend/
├── common/         # 公共模块
├── gateway/        # API网关
├── auth-service/   # 认证服务
├── user-service/   # 用户服务
├── content-service/# 内容服务
└── comment-service/# 评论服务
```

## 开发规范

- 使用TypeScript进行类型安全开发
- 组件命名使用PascalCase
- 文件命名使用camelCase（组件文件除外）
- 遵循Ant Design Design Guidelines
- 后端遵循RESTful API设计规范

## 许可证

MIT License