import request from './api';
import { convertPageResponse } from '@utils/page';
import type {
  Comment,
  CreateCommentRequest,
  PageParams
} from '@types';

export const getCommentList = async (contentId: string, params: PageParams = {}) => {
  const res = await request.get<Comment[]>(`/comment/content/${contentId}`, { params });
  return convertPageResponse<Comment>(res.data);
};

export const createComment = (data: CreateCommentRequest) => {
  return request.post<Comment>('/comment/create', data);
};

export const deleteComment = (id: string) => {
  return request.delete(`/comment/${id}`);
};

export const likeComment = (id: string) => {
  return request.post(`/comment/${id}/like`);
};

export const unlikeComment = (id: string) => {
  return request.delete(`/comment/${id}/like`);
};
