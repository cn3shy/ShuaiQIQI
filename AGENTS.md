# AGENTS.md

本文件为 AI 编码助手提供项目指南。

## 项目概述

前后端分离全栈项目：React 19 前端 + Spring Boot 3.2 微服务后端 + 微信小程序用户端。

```
shuaiqi-qi/
├── frontend/          # React 19 + TypeScript 5.9 + Vite 8
│   └── src/
│       ├── components/    # 可复用组件 (ContentCard, ErrorBoundary, etc.)
│       ├── layouts/       # 布局 (MainLayout, MobileLayout, AdminLayout)
│       ├── pages/         # 页面组件 (HomePage, LoginPage, admin/*)
│       ├── router/        # 路由配置 (懒加载 + 守卫 + 响应式布局)
│       ├── services/      # API 服务 (axios 封装 + token 刷新)
│       ├── stores/        # Zustand 状态管理 (auth)
│       ├── hooks/         # 自定义 Hooks
│       ├── types/         # TypeScript 类型定义
│       └── utils/         # 工具函数
├── backend/           # Spring Boot 3.2 + Spring Cloud 2023 + JDK 17
│   ├── gateway/           # API 网关 (8080)
│   ├── auth-service/      # 认证服务 (8081)
│   ├── user-service/      # 用户服务 (8082)
│   ├── content-service/   # 内容服务 (8083)
│   ├── comment-service/   # 评论服务 (8084)
│   ├── notification-service/ # 通知服务 (8085)
│   ├── common/            # 公共模块
│   └── common-core/       # 核心公共模块 (Result, 异常处理)
└── wechatweb/         # 微信小程序用户端 (TypeScript + Glass-Easel 渲染引擎)
    └── miniprogram/
        ├── pages/             # 页面 (index/discover/notification/profile/login/register/content-detail/content-create/user-profile/settings)
        ├── utils/             # 工具函数 (request.ts/auth.ts/util.ts)
        ├── images/            # 图片资源 (tab 图标等)
        ├── app.ts             # 应用入口 (全局状态 + 认证 + 请求封装)
        ├── app.json           # 应用配置 (页面路由 + tabBar + 权限)
        └── app.wxss           # 全局样式
```

## 构建与测试命令

### 前端 (frontend/)

```bash
pnpm install              # 安装依赖（必须使用 pnpm）
pnpm dev                  # 开发服务器 (端口 3000)
pnpm build                # 生产构建 (tsc -b && vite build)
pnpm lint                 # ESLint 检查
pnpm preview              # 预览构建结果
```

**前端测试**: 未配置测试框架。推荐 Vitest + React Testing Library。

### 后端 (backend/)

```bash
mvn clean install                    # 构建所有模块
mvn test                             # 运行所有测试
mvn test -pl user-service            # 运行单个模块测试
mvn test -Dtest=UserServiceTest      # 运行特定测试类
mvn spring-boot:run -pl gateway      # 启动单个服务
```

**后端测试**: JUnit 5 + Spring Boot Test，测试文件在 `src/test/java/`。当前无测试文件。

### 微信小程序 (wechatweb/)

```bash
# 使用微信开发者工具打开 wechatweb/ 目录进行编译和预览
# 或使用微信开发者工具导入项目 (AppID: wxca0de6d1588b1dd9)
```

**小程序构建**: 通过微信开发者工具编译，无需命令行。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | React 19 + TypeScript 5.9 (严格模式) + Vite 8 |
| UI | Ant Design 6 (中文 locale) |
| 状态 | Zustand 5 (persist 持久化) |
| 路由 | React Router 7 (懒加载 + 守卫) |
| HTTP | Axios (拦截器 + token 刷新) |
| 后端 | Spring Boot 3.2 + Spring Cloud + JDK 17 |
| ORM | MyBatis Plus 3.5.4 |
| 工具 | Lombok, Hutool |
| 小程序 | TypeScript + Glass-Easel 渲染引擎 + 微信原生组件 |

## 路径别名

```
@/           -> ./src/          @components/ -> ./src/components/
@layouts/    -> ./src/layouts/  @pages/      -> ./src/pages/
@hooks/      -> ./src/hooks/    @services/   -> ./src/services/
@stores/     -> ./src/stores/   @utils/      -> ./src/utils/
@types/      -> ./src/types/    @styles/     -> ./src/styles/
```

## 代码风格

### TypeScript/React

- `interface` 定义对象类型，`type` 定义联合类型
- Props 接口命名 `ComponentNameProps`，组件使用 `React.FC`
- 避免 `any`，提供具体类型
- 启用 `noUnusedLocals`、`noUnusedParameters`、`strict: true`

```typescript
interface ContentCardProps {
  content: Content;
  onLike?: (id: string) => void;
}
const ContentCard: React.FC<ContentCardProps> = ({ content, onLike }) => { ... };
```

### 命名约定

- **组件**: PascalCase (`ContentCard.tsx`)
- **页面**: PascalCase + Page 后缀 (`HomePage.tsx`)
- **工具/服务**: camelCase (`api.ts`, `content.ts`)
- **类型**: 统一在 `types/index.ts`
- **后端包**: `com.shuaiqi.{service}.{module}`

### 导入顺序

```typescript
// 1. React  2. 第三方库  3. 路由  4. 项目内部 (@别名)
```

### ESLint

Flat config，继承 `typescript-eslint:recommended`、`react-hooks:recommended`、`react-refresh`。忽略 `dist/`。

### 状态管理 (Zustand)

```typescript
export const useAuthStore = create<AuthState>()(
  persist((set) => ({ user: null, token: null, ... }), { name: 'auth-storage' })
);
```

### API 服务

```typescript
import request from '@services/api';
export const getContentList = (params) => request.get<Content[]>('/content/list', { params });
```

### 后端 Java

- Lombok: `@Data`, `@Slf4j`, `@NoArgsConstructor`, `@AllArgsConstructor`
- 统一响应: `Result<T>` (code/message/data/timestamp)
- MyBatis Plus 数据库操作

### 微信小程序

- 使用 `Component()` 构造页面/组件，避免使用 `Page()`
- 全局类型通过 `getApp<IAppOption>()` 获取
- 请求封装: `utils/request.ts` (get/post/put/del/patch + token 刷新 + 401 拦截)
- 认证封装: `utils/auth.ts` (login/register/wxLogin/logout/getUserInfo/updateUserInfo)
- 全局状态: `app.ts` 的 `globalData` (token/refreshToken/userInfo/baseUrl)
- 渲染引擎: Glass-Easel (`"componentFramework": "glass-easel"`)

## 错误处理

- **前端**: Axios 拦截器统一处理 (401 跳转登录 + token 刷新)，组件内 try-catch + `console.error`
- **后端**: 全局异常处理器，返回统一 `Result` 格式

## 注意事项

1. **包管理器**: 前端必须使用 `pnpm`
2. **中文注释**: 保持一致性
3. **响应式**: Ant Design Grid (xs/sm/md/lg/xl/xxl)，移动端断点 768px
4. **代理**: `/api` → `http://localhost:8080`
5. **路由**: 使用 `React.lazy` 懒加载，`ErrorBoundary` 包裹，`ProtectedRoute` 守卫

## Git 工作流

- 直接在 `main` 分支开发，完成后 `git push origin main`

## 页面路由

### Web 前台
`/` 首页 | `/content` 发现 | `/content/:id` 详情 | `/content/create` 创建 | `/profile` 个人中心 | `/profile/settings` 设置 | `/notification` 通知 | `/user/:userId` 用户 | `/login` 登录 | `/register` 注册 | `/forgot-password` 忘记密码

### Web 后台
`/admin` 仪表盘 | `/admin/content` 内容管理 | `/admin/user` 用户管理

### 微信小程序
`pages/index/index` 首页 | `pages/discover/discover` 发现 | `pages/notification/notification` 通知 | `pages/profile/profile` 我的 | `pages/login/login` 登录 | `pages/register/register` 注册 | `pages/content-detail/content-detail` 内容详情 | `pages/content-create/content-create` 创建内容 | `pages/user-profile/user-profile` 用户主页 | `pages/settings/settings` 设置

## 微信小程序架构

### 全局配置
- **AppID**: `wxca0de6d1588b1dd9`
- **渲染引擎**: Glass-Easel (`"componentFramework": "glass-easel"`)
- **基础库版本**: `trial` (最新测试版)
- **主包目录**: `miniprogram/`
- **baseUrl**: `http://localhost:8080/api`

### 全局状态 (app.ts globalData)
```typescript
{
  token: string;           // 登录令牌
  refreshToken: string;    // 刷新令牌
  userInfo: IUserInfo | null;  // 用户信息
  baseUrl: string;         // API 基础地址
}
```

### 认证方法 (app.ts)
- `login()` - 微信一键登录 (wx.login + 后端换取 token)
- `getUserInfo()` - 获取用户信息
- `logout()` - 退出登录 (清除缓存 + 跳转登录页)
- `isLogin()` - 检查登录状态
- `initAuth()` - 初始化认证 (从本地存储恢复)
- `checkLoginStatus()` - 检查登录状态

### 请求封装 (utils/request.ts)
- `get<T>()` / `post<T>()` / `put<T>()` / `del<T>()` / `patch<T>()`
- 自动携带 Bearer token
- 401 自动刷新 token (请求队列机制)
- 刷新失败清除登录态并跳转登录页
- 统一错误提示 (中文)

### 认证服务 (utils/auth.ts)
- `login(data)` - 账号密码登录
- `register(data)` - 注册
- `wxLogin()` - 微信登录
- `logout()` - 退出登录
- `isLogin()` - 检查登录状态
- `getUserInfo()` - 获取用户信息
- `updateUserInfo(data)` - 更新用户信息

### 数据模型

**用户 (UserInfo)**
```typescript
{
  id: string;
  username: string;
  email?: string;
  phone?: string;
  avatar?: string;
  bio?: string;
  role?: string;
  createTime: string;
  updateTime: string;
}
```

**内容 (Content)**
```typescript
{
  id: string;
  title: string;
  summary: string;
  content: string;
  author: User;
  coverImage?: string;
  categoryId?: string;
  tags?: string[];
  likeCount: number;
  favoriteCount: number;
  commentCount: number;
  isLiked: boolean;
  isFavorited: boolean;
  createTime: string;
  updateTime: string;
}
```

**评论 (Comment)**
```typescript
{
  id: string;
  content: string;
  contentId: string;
  userId: string;
  user: User;
  parentId?: string;
  likeCount: number;
  isLiked: boolean;
  children?: Comment[];
  createTime: string;
  updateTime: string;
}
```

**通知 (Notification)**
```typescript
{
  id: string;
  type: 'comment' | 'like' | 'favorite' | 'follow';
  title: string;
  content: string;
  userId: string;
  targetId?: string;
  targetType?: 'content' | 'comment';
  isRead: boolean;
  createTime: string;
}
```

### tabBar 配置
| 页签 | 路径 | 图标 |
|---|---|---|
| 首页 | pages/index/index | images/tab/home.png |
| 发现 | pages/discover/discover | images/tab/discover.png |
| 通知 | pages/notification/notification | images/tab/notification.png |
| 我的 | pages/profile/profile | images/tab/profile.png |

### 页面文件结构
每个页面包含 4 个文件：
- `page.json` - 页面配置 (usingComponents)
- `page.ts` - 页面逻辑 (使用 `Component()` 构造)
- `page.wxml` - 页面模板
- `page.wxss` - 页面样式
