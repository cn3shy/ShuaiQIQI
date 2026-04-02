import request from './api';
import { convertPageResponse } from '@utils/page';
import type {
  Content,
  Category,
  ContentListParams,
  PageParams
} from '@types';

export const getContentList = async (params: ContentListParams = {}) => {
  const res = await request.get<Content>('/content/list', { params });
  return convertPageResponse<Content>(res.data);
};

export const getMyContentList = async (params: ContentListParams = {}) => {
  const res = await request.get<Content>('/content/my', { params });
  return convertPageResponse<Content>(res.data);
};

export const getContentDetail = (id: string) => {
  return request.get<Content>(`/content/${id}`);
};

export const createContent = (data: Partial<Content>) => {
  return request.post<Content>('/content/create', data);
};

export const updateContent = (id: string, data: Partial<Content>) => {
  return request.put<Content>(`/content/${id}`, data);
};

export const deleteContent = (id: string) => {
  return request.delete(`/content/${id}`);
};

export const likeContent = (id: string) => {
  return request.post(`/content/${id}/like`);
};

export const unlikeContent = (id: string) => {
  return request.delete(`/content/${id}/like`);
};

export const favoriteContent = (id: string) => {
  return request.post(`/content/${id}/favorite`);
};

export const unfavoriteContent = (id: string) => {
  return request.delete(`/content/${id}/favorite`);
};

export const getCategoryList = () => {
  return request.get<Category[]>('/content/categories');
};

export const getCategoryDetail = (id: string) => {
  return request.get<Category>(`/content/category/${id}`);
};

export const getRecommendContent = async (params: PageParams = {}) => {
  const res = await request.get<Content>('/content/recommend', { params });
  return convertPageResponse<Content>(res.data);
};

export const getHotContent = async (params: PageParams = {}) => {
  const res = await request.get<Content>('/content/hot', { params });
  return convertPageResponse<Content>(res.data);
};
