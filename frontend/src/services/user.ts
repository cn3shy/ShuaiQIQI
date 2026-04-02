import request from './api';
import { convertPageResponse } from '@utils/page';
import type { User } from '@types';

export const getCurrentUser = () => {
  return request.get<User>('/user/info');
};

export const updateUserInfo = (data: Partial<User>) => {
  return request.put<User>('/user/info', data);
};

export const uploadAvatar = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<{ url: string }>('/user/avatar', formData);
};

export const changePassword = (oldPassword: string, newPassword: string) => {
  return request.post('/user/change-password', { oldPassword, newPassword });
};

export const getUserDetail = (userId: string) => {
  return request.get<User>(`/user/${userId}`);
};

export const getUserList = async (params: { page?: number; pageSize?: number; keyword?: string }) => {
  const res = await request.get<User>('/user/list', { params });
  return convertPageResponse<User>(res.data);
};

export const deleteUser = (userId: string) => {
  return request.delete(`/user/${userId}`);
};
