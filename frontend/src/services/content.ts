// 内容服务API
import request from './api';
import type {
  Content,
  Category,
  ContentListParams,
  ContentListResponse,
  PageParams
} from '@types';

// 获取内容列表
export const getContentList = (params: ContentListParams = {}) => {
  return request.get<ContentListResponse>('/content/list', { params });
};

// 获取内容详情
export const getContentDetail = (id: string) => {
  return request.get<Content>(`/content/${id}`);
};

// 创建内容
export const createContent = (data: Partial<Content>) => {
  return request.post<Content>('/content/create', data);
};

// 更新内容
export const updateContent = (id: string, data: Partial<Content>) => {
  return request.put<Content>(`/content/${id}`, data);
};

// 删除内容
export const deleteContent = (id: string) => {
  return request.delete(`/content/${id}`);
};

// 点赞内容
export const likeContent = (id: string) => {
  return request.post(`/content/${id}/like`);
};

// 取消点赞内容
export const unlikeContent = (id: string) => {
  return request.delete(`/content/${id}/like`);
};

// 收藏内容
export const favoriteContent = (id: string) => {
  return request.post(`/content/${id}/favorite`);
};

// 取消收藏内容
export const unfavoriteContent = (id: string) => {
  return request.delete(`/content/${id}/favorite`);
};

// 获取分类列表
export const getCategoryList = () => {
  return request.get<Category[]>('/content/categories');
};

// 获取分类详情
export const getCategoryDetail = (id: string) => {
  return request.get<Category>(`/content/category/${id}`);
};

// 获取推荐内容
export const getRecommendContent = (params: PageParams = {}) => {
  return request.get<ContentListResponse>('/content/recommend', { params });
};

// 获取热门内容
export const getHotContent = (params: PageParams = {}) => {
  return request.get<ContentListResponse>('/content/hot', { params });
};