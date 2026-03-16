// 评论服务API
import request from './api';
import type {
  Comment,
  CreateCommentRequest,
  PageParams
} from '@types';

// 获取评论列表
export const getCommentList = (contentId: string, params: PageParams = {}) => {
  return request.get<{ list: Comment[]; total: number }>(`/comments/${contentId}`, { params });
};

// 发布评论
export const createComment = (data: CreateCommentRequest) => {
  return request.post<Comment>('/comments/create', data);
};

// 回复评论
export const replyComment = (commentId: string, content: string) => {
  return request.post<Comment>(`/comments/${commentId}/reply`, { content });
};

// 删除评论
export const deleteComment = (id: string) => {
  return request.delete(`/comments/${id}`);
};

// 点赞评论
export const likeComment = (id: string) => {
  return request.post(`/comments/${id}/like`);
};

// 取消点赞评论
export const unlikeComment = (id: string) => {
  return request.delete(`/comments/${id}/like`);
};

// 举报评论
export const reportComment = (id: string, reason: string) => {
  return request.post(`/comments/${id}/report`, { reason });
};