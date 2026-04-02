import React from 'react';
import { CommentOutlined, LikeOutlined, StarOutlined, UserAddOutlined } from '@ant-design/icons';

const ICON_MAP: Record<string, React.ReactElement> = {
  comment: <CommentOutlined style={{ color: '#1890ff' }} />,
  like: <LikeOutlined style={{ color: '#ff4d4f' }} />,
  favorite: <StarOutlined style={{ color: '#faad14' }} />,
  follow: <UserAddOutlined style={{ color: '#52c41a' }} />,
};

export function getNotificationIcon(type: string): React.ReactElement | null {
  return ICON_MAP[type] || null;
}
