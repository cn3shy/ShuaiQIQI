// 响应式断点类型
export type Breakpoint = 'xs' | 'sm' | 'md' | 'lg' | 'xl' | 'xxl';

export interface Breakpoints {
  xs: number;
  sm: number;
  md: number;
  lg: number;
  xl: number;
  xxl: number;
}

// 用户相关类型
export interface User {
  id: string;
  username: string;
  email: string;
  phone?: string;
  avatar?: string;
  bio?: string;
  role?: string;
  createTime: string;
  updateTime: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  remember?: boolean;
}

export interface RegisterRequest {
  username: string;
  email: string;
  phone?: string;
  password: string;
  confirmPassword: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: User;
}

// 内容相关类型
export interface Content {
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

export interface Category {
  id: string;
  name: string;
  description?: string;
  icon?: string;
  sort: number;
}

export interface ContentListParams {
  page?: number;
  pageSize?: number;
  categoryId?: string;
  keyword?: string;
  sortBy?: 'latest' | 'popular' | 'hot';
}

export interface ContentListResponse {
  list: Content[];
  total: number;
  page: number;
  pageSize: number;
}

// 评论相关类型
export interface Comment {
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

export interface CreateCommentRequest {
  contentId: string;
  content: string;
  parentId?: string;
}

// API响应类型
export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T = unknown> {
  list: T[];
  total: number;
  page: number;
  pageSize: number;
}

// 分页参数
export interface PageParams {
  page?: number;
  pageSize?: number;
}

// 通知相关类型
export interface Notification {
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

export interface NotificationListResponse {
  list: Notification[];
  total: number;
  unreadCount: number;
}

// 关注相关类型
export interface FollowUser {
  id: string;
  username: string;
  avatar?: string;
  bio?: string;
  isFollowing: boolean;
}

export interface FollowListResponse {
  list: FollowUser[];
  total: number;
}