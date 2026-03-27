// 关注服务 API
import request from './api';
import type { FollowUser, FollowListResponse } from '@types';

// 关注用户
export const followUser = (userId: string) => {
  return request.post(`/user/${userId}/follow`);
};

// 取消关注
export const unfollowUser = (userId: string) => {
  return request.delete(`/user/${userId}/follow`);
};

// 获取关注列表
export const getFollowingList = (userId: string, params: { page?: number; pageSize?: number }) => {
  return request.get<FollowListResponse>(`/user/${userId}/following`, { params });
};

// 获取粉丝列表
export const getFollowerList = (userId: string, params: { page?: number; pageSize?: number }) => {
  return request.get<FollowListResponse>(`/user/${userId}/followers`, { params });
};

// 检查是否关注
export const checkIsFollowing = (userId: string) => {
  return request.get<{ isFollowing: boolean }>(`/user/${userId}/is-following`);
};
