// 评论服务API
import request from './api';
import { convertPageResponse } from '@utils/page';
import type {
  Comment,
  CreateCommentRequest,
  PageParams
} from '@types';

// 获取评论列表
export const getCommentList = async (contentId: string, params: PageParams = {}) => {
  const res = await request.get<any>(`/comment/content/${contentId}`, { params });
  return convertPageResponse<Comment>(res.data);
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