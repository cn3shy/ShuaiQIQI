// 通知铃铛组件
import React, { useState, useEffect, useCallback } from 'react';
import { Badge, Dropdown, List, Empty, Button, Space } from 'antd';
import { BellOutlined, CommentOutlined, LikeOutlined, StarOutlined, UserAddOutlined } from '@ant-design/icons';
import { getNotificationList, markAsRead } from '@services/notification';
import { Link } from 'react-router-dom';
import { useAuthStore } from '@stores/auth';
import { useWebSocket } from '@hooks/useWebSocket';
import type { Notification } from '@types';

const NotificationBell: React.FC = () => {
  const { user } = useAuthStore();
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [unreadCount, setUnreadCount] = useState(0);

  const handleNewNotification = useCallback((notification: Notification) => {
    setNotifications((prev) => [notification, ...prev.slice(0, 4)]);
    setUnreadCount((prev) => prev + 1);
  }, []);

  const { isConnected } = useWebSocket({
    userId: user?.id,
    onNotification: handleNewNotification,
  });

  const loadNotifications = async () => {
    try {
      const data = await getNotificationList({ page: 1, pageSize: 5 });
      setNotifications(data.data?.list || []);
      setUnreadCount(data.data?.unreadCount || 0);
    } catch (error) {
      console.error('加载通知失败:', error);
    }
  };

  useEffect(() => {
    loadNotifications();
    // 每30秒刷新一次
    const interval = setInterval(loadNotifications, 30000);
    return () => clearInterval(interval);
  }, []);

  const handleMarkAsRead = async (id: string) => {
    try {
      await markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (error) {
      console.error('标记已读失败:', error);
    }
  };

  const getIcon = (type: string) => {
    switch (type) {
      case 'comment':
        return <CommentOutlined style={{ color: '#1890ff' }} />;
      case 'like':
        return <LikeOutlined style={{ color: '#ff4d4f' }} />;
      case 'favorite':
        return <StarOutlined style={{ color: '#faad14' }} />;
      case 'follow':
        return <UserAddOutlined style={{ color: '#52c41a' }} />;
      default:
        return null;
    }
  };

  const dropdownContent = (
    <div style={{ width: 320, background: '#fff', borderRadius: 8, boxShadow: '0 2px 8px rgba(0,0,0,0.15)' }}>
      <div style={{ padding: '12px 16px', borderBottom: '1px solid #f0f0f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontWeight: 500 }}>通知</span>
        <Link to="/notification">
          <Button type="link" size="small">查看全部</Button>
        </Link>
      </div>
      {notifications.length === 0 ? (
        <div style={{ padding: 24 }}>
          <Empty description="暂无通知" image={Empty.PRESENTED_IMAGE_SIMPLE} />
        </div>
      ) : (
        <List
          dataSource={notifications}
          renderItem={(item) => (
            <List.Item
              style={{
                padding: '12px 16px',
                cursor: 'pointer',
                backgroundColor: item.isRead ? '#fff' : '#f6ffed',
              }}
              onClick={() => !item.isRead && handleMarkAsRead(item.id)}
            >
              <List.Item.Meta
                avatar={getIcon(item.type)}
                title={
                  <Space>
                    <span style={{ fontSize: 13 }}>{item.title}</span>
                    {!item.isRead && (
                      <span style={{ width: 6, height: 6, borderRadius: '50%', background: '#52c41a' }} />
                    )}
                  </Space>
                }
                description={
                  <div style={{ fontSize: 12, color: '#999' }}>
                    {item.content.length > 30 ? item.content.substring(0, 30) + '...' : item.content}
                  </div>
                }
              />
            </List.Item>
          )}
        />
      )}
    </div>
  );

  return (
    <Dropdown
      dropdownRender={() => dropdownContent}
      trigger={['click']}
      placement="bottomRight"
    >
      <Badge count={unreadCount} size="small" offset={[-2, 2]}>
        <BellOutlined style={{ fontSize: 20, cursor: 'pointer' }} />
      </Badge>
    </Dropdown>
  );
};

export default NotificationBell;
