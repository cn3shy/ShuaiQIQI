// 认证服务API
import request from './api';
import type { LoginRequest, RegisterRequest, AuthResponse } from '@types';

// 用户注册
export const register = (data: RegisterRequest) => {
  return request.post<AuthResponse>('/auth/register', data);
};

// 用户登录
export const login = (data: LoginRequest) => {
  return request.post<AuthResponse>('/auth/login', data);
};

// 用户登出
export const logout = () => {
  return request.post('/auth/logout');
};

// 刷新token
export const refreshToken = (refreshToken: string) => {
  return request.post<{ token: string; refreshToken: string }>('/auth/refresh', { refreshToken });
};

// 忘记密码
export const forgotPassword = (email: string) => {
  return request.post<string>('/auth/forgot-password', { email });
};

// 重置密码
export const resetPassword = (token: string, newPassword: string, confirmPassword: string) => {
  return request.post('/auth/reset-password', { token, newPassword, confirmPassword });
};