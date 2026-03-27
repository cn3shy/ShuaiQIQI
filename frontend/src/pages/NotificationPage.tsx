// 通知页面
import React, { useState, useEffect } from 'react';
import { List, Card, Button, Space, Tag, Empty, Spin, message, Popconfirm } from 'antd';
import {
  CommentOutlined,
  LikeOutlined,
  StarOutlined,
  UserAddOutlined,
  DeleteOutlined,
  CheckOutlined,
} from '@ant-design/icons';
import { getNotificationList, markAsRead, markAllAsRead, deleteNotification } from '@services/notification';
import type { Notification } from '@types';

const NotificationPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [page, setPage] = useState(1);

  const loadNotifications = async () => {
    setLoading(true);
    try {
      const data = await getNotificationList({ page, pageSize: 20 });
      setNotifications(data.data?.list || []);
    } catch (error) {
      console.error('加载通知失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadNotifications();
  }, [page]);

  const handleMarkAsRead = async (id: string) => {
    try {
      await markAsRead(id);
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
      );
      message.success('已标记为已读');
    } catch {
      message.error('操作失败');
    }
  };

  const handleMarkAllAsRead = async () => {
    try {
      await markAllAsRead();
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));
      message.success('已全部标记为已读');
    } catch {
      message.error('操作失败');
    }
  };

  const handleDelete = async (id: string) => {
    try {
      await deleteNotification(id);
      setNotifications((prev) => prev.filter((n) => n.id !== id));
      message.success('删除成功');
    } catch {
      message.error('删除失败');
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

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ margin: 0 }}>消息通知</h2>
        <Button
          type="link"
          icon={<CheckOutlined />}
          onClick={handleMarkAllAsRead}
        >
          全部已读
        </Button>
      </div>

      <Spin spinning={loading}>
        {notifications.length === 0 ? (
          <Empty description="暂无通知" />
        ) : (
          <List
            dataSource={notifications}
            renderItem={(item) => (
              <Card
                style={{
                  marginBottom: 16,
                  backgroundColor: item.isRead ? '#fff' : '#f6ffed',
                }}
              >
                <List.Item
                  actions={[
                    !item.isRead && (
                      <Button
                        type="link"
                        size="small"
                        onClick={() => handleMarkAsRead(item.id)}
                      >
                        标记已读
                      </Button>
                    ),
                    <Popconfirm
                      title="确定删除此通知？"
                      onConfirm={() => handleDelete(item.id)}
                      okText="确定"
                      cancelText="取消"
                    >
                      <Button type="link" size="small" danger icon={<DeleteOutlined />}>
                        删除
                      </Button>
                    </Popconfirm>,
                  ].filter(Boolean)}
                >
                  <List.Item.Meta
                    avatar={getIcon(item.type)}
                    title={
                      <Space>
                        {item.title}
                        {!item.isRead && <Tag color="green">未读</Tag>}
                      </Space>
                    }
                    description={
                      <div>
                        <div style={{ marginBottom: 8 }}>{item.content}</div>
                        <div style={{ color: '#999', fontSize: 12 }}>
                          {new Date(item.createTime).toLocaleString()}
                        </div>
                      </div>
                    }
                  />
                </List.Item>
              </Card>
            )}
          />
        )}
      </Spin>
    </div>
  );
};

export default NotificationPage;
