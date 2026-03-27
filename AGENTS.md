# AGENTS.md

本文件为 AI 编码助手提供项目指南，帮助理解代码结构和开发规范。

## 项目概述

前后端分离的全栈项目，包含 React 前端和 Spring Boot 微服务后端。

```
shuaiqi-qi/
├── frontend/          # React + TypeScript + Vite
└── backend/           # Spring Boot + Spring Cloud 微服务
    ├── gateway/       # API 网关 (端口 8080)
    ├── auth-service/  # 认证服务 (端口 8081)
    ├── user-service/  # 用户服务 (端口 8082)
    ├── content-service/ # 内容服务 (端口 8083)
    ├── comment-service/ # 评论服务 (端口 8084)
    ├── notification-service/ # 通知服务 (端口 8085)
    └── common/        # 公共模块
```

## 构建与开发命令

### 前端 (frontend/)

```bash
# 安装依赖 (必须使用 pnpm)
pnpm install

# 启动开发服务器 (端口 3000)
pnpm dev

# 构建生产版本
pnpm build

# 代码检查
pnpm lint

# 预览构建结果
pnpm preview
```

### 后端 (backend/)

```bash
# 构建所有模块
mvn clean install

# 启动单个服务
cd backend/gateway && mvn spring-boot:run
cd backend/auth-service && mvn spring-boot:run

# 运行测试
mvn test

# 运行单个模块测试
mvn test -pl user-service

# 运行特定测试类
mvn test -Dtest=UserServiceTest
```

## 前端技术栈

- **框架**: React 19 + TypeScript 5.9
- **构建工具**: Vite 8
- **UI 库**: Ant Design 6
- **状态管理**: Zustand 5
- **路由**: React Router 7
- **HTTP 客户端**: Axios

## 路径别名

在 `vite.config.ts` 和 `tsconfig.app.json` 中配置:

```typescript
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

## 代码风格指南

### TypeScript/React

- 使用 TypeScript 严格模式 (`strict: true`)
- 组件使用 `React.FC` 类型定义
- Props 接口命名为 `ComponentNameProps`
- 使用 `interface` 定义对象类型，`type` 定义联合类型

```typescript
// 正确示例
interface ContentCardProps {
  content: Content;
  onLike?: (id: string) => void;
}

const ContentCard: React.FC<ContentCardProps> = ({ content, onLike }) => {
  return <Card>...</Card>;
};
```

### 命名约定

- **组件文件**: PascalCase (如 `ContentCard.tsx`)
- **页面文件**: PascalCase + Page 后缀 (如 `HomePage.tsx`)
- **工具/服务文件**: camelCase (如 `api.ts`, `auth.ts`)
- **类型定义**: 统一放在 `types/index.ts`
- **CSS 类名**: 避免使用，优先使用 Ant Design 组件

### 导入顺序

```typescript
// 1. React 相关
import React, { useState, useEffect } from 'react';

// 2. 第三方库
import { Card, Button } from 'antd';
import { SearchOutlined } from '@ant-design/icons';

// 3. 路由相关
import { Link, useNavigate } from 'react-router-dom';

// 4. 项目内部 - 使用路径别名
import type { Content } from '@types';
import { getContentList } from '@services/content';
import ContentCard from '@components/ContentCard';
```

### 状态管理 (Zustand)

```typescript
// stores/auth.ts 示例
interface AuthState {
  user: User | null;
  token: string | null;
  setAuth: (auth: AuthResponse) => void;
  clearAuth: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      // state 和 actions
    }),
    { name: 'auth-storage' }
  )
);
```

### API 服务

```typescript
// services/api.ts - 基础配置
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

// 使用封装的 request 方法
import request from './api';
export const getContentList = (params: ContentListParams) =>
  request.get<ContentListResponse>('/content/list', { params });
```

### 后端 Java

- 使用 Lombok 注解 (`@Data`, `@Slf4j`)
- 统一响应封装 `Result<T>`
- 包命名: `com.shuaiqi.{service}.{module}`
- 使用 MyBatis Plus 进行数据库操作

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;
    
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "操作成功", data, System.currentTimeMillis());
    }
}
```

## 错误处理

### 前端

- API 请求统一在 `api.ts` 拦截器中处理错误
- 组件内使用 try-catch 处理异步操作
- 使用 `console.error` 记录错误

```typescript
try {
  const data = await getContentList(params);
  setContents(data.data?.list || []);
} catch (error) {
  console.error('加载内容失败:', error);
}
```

### 后端

- 使用全局异常处理器
- 返回统一的 `Result` 格式

## 环境变量

前端 `.env` 文件:
- `VITE_API_BASE_URL`: API 基础路径，默认 `/api`

## 注意事项

1. **包管理器**: 前端必须使用 `pnpm`，不要使用 npm 或 yarn
2. **中文注释**: 项目使用中文注释，保持一致性
3. **响应式设计**: 使用 Ant Design 的 Grid 系统 (xs/sm/md/lg/xl/xxl)
4. **类型安全**: 避免使用 `any`，尽量提供具体类型

## Git 工作流

- 直接在 `main` 分支开发和提交
- 每次完成功能后 `git push origin main`

## 项目页面结构

### 前台页面
- `/` - 首页（内容列表、搜索、热门/推荐）
- `/content` - 发现页（内容浏览）
- `/content/:id` - 内容详情页
- `/content/create` - 创建内容
- `/profile` - 个人中心
- `/profile/settings` - 设置页
- `/notification` - 消息通知
- `/user/:userId` - 用户详情页
- `/login` - 登录页
- `/register` - 注册页
- `/forgot-password` - 忘记密码页

### 后台管理页面
- `/admin` - 仪表盘
- `/admin/content` - 内容管理
- `/admin/user` - 用户管理
