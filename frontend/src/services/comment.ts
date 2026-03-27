// 评论服务API
import request from './api';
import type {
  Comment,
  CreateCommentRequest,
  PageParams
} from '@types';

// 获取评论列表
export const getCommentList = (contentId: string, params: PageParams = {}) => {
  return request.get<{ list: Comment[]; total: number }>(`/comment/content/${contentId}`, { params });
};

// 发布评论
export const createComment = (data: CreateCommentRequest) => {
  return request.post<Comment>('/comment/create', data);
};

// 删除评论
export const deleteComment = (id: string) => {
  return request.delete(`/comment/${id}`);
};

// 点赞评论
export const likeComment = (id: string) => {
  return request.post(`/comment/${id}/like`);
};

// 取消点赞评论
export const unlikeComment = (id: string) => {
  return request.delete(`/comment/${id}/like`);
};