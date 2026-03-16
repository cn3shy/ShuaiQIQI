// 用户服务API
import request from './api';
import type { User } from '@types';

// 获取当前用户信息
export const getCurrentUser = () => {
  return request.get<User>('/user/info');
};

// 更新用户信息
export const updateUserInfo = (data: Partial<User>) => {
  return request.put<User>('/user/info', data);
};

// 上传头像
export const uploadAvatar = (file: File) => {
  const formData = new FormData();
  formData.append('file', file);
  return request.post<{ url: string }>('/user/avatar', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
};

// 修改密码
export const changePassword = (oldPassword: string, newPassword: string) => {
  return request.post('/user/change-password', { oldPassword, newPassword });
};

// 获取用户详情（公开信息）
export const getUserDetail = (userId: string) => {
  return request.get<User>(`/user/${userId}`);
};