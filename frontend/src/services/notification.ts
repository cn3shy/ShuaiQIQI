// 通知服务 API
import request from './api';
import type { Notification, NotificationListResponse } from '@types';

// 获取通知列表
export const getNotificationList = (params: { page?: number; pageSize?: number }) => {
  return request.get<NotificationListResponse>('/notification/list', { params });
};

// 标记通知为已读
export const markAsRead = (id: string) => {
  return request.put(`/notification/${id}/read`);
};

// 标记所有通知为已读
export const markAllAsRead = () => {
  return request.put('/notification/read-all');
};

// 删除通知
export const deleteNotification = (id: string) => {
  return request.delete(`/notification/${id}`);
};

// 获取未读通知数量
export const getUnreadCount = () => {
  return request.get<{ count: number }>('/notification/unread-count');
};
