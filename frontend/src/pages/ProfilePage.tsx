import React, { useState, useEffect } from 'react';
import { Card, Avatar, Tabs, Row, Col, Spin, Empty, Button } from 'antd';
import { UserOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { getCurrentUser } from '@services/user';
import { getContentList } from '@services/content';
import { useAuthStore } from '@stores/auth';
import ContentCard from '@components/ContentCard';
import type { Content } from '@types';

/**
 * 个人中心页
 */
const ProfilePage: React.FC = () => {
  const { user, setUser } = useAuthStore();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [tabKey, setTabKey] = useState('info');
  const [myContents, setMyContents] = useState<Content[]>([]);
  const [myContentsLoading, setMyContentsLoading] = useState(false);

  useEffect(() => {
    loadUserInfo();
  }, []);

  useEffect(() => {
    if (tabKey === 'contents' && user) {
      loadMyContents();
    }
  }, [tabKey, user]);

  const loadUserInfo = async () => {
    if (user) return;
    setLoading(true);
    try {
      const response = await getCurrentUser();
      setUser(response.data);
    } catch (error) {
      console.error('加载用户信息失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMyContents = async () => {
    if (!user) return;
    setMyContentsLoading(true);
    try {
      const response = await getContentList({ page: 1, pageSize: 20 });
      // 筛选当前用户的内容
      const filtered = (response.data?.list || []).filter(
        (c: Content) => c.author?.id === user.id
      );
      setMyContents(filtered);
    } catch (error) {
      console.error('加载我的内容失败:', error);
    } finally {
      setMyContentsLoading(false);
    }
  };

  if (!user && loading) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!user) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <div style={{ padding: '40px 0' }}>
          <UserOutlined style={{ fontSize: 48, color: '#999' }} />
          <p style={{ marginTop: 16 }}>请先登录</p>
          <Button type="primary" onClick={() => navigate('/login')}>
            去登录
          </Button>
        </div>
      </div>
    );
  }

  const tabItems = [
    {
      key: 'info',
      label: '个人信息',
      children: (
        <Card title="我的资料">
          <div style={{ display: 'flex', alignItems: 'center', gap: 32, padding: '24px 0' }}>
            <Avatar
              size={120}
              src={user.avatar}
              icon={<UserOutlined />}
              style={{ fontSize: 48, backgroundColor: '#1890ff' }}
            >
              {user.username?.[0]?.toUpperCase()}
            </Avatar>
            <div style={{ flex: 1 }}>
              <h2 style={{ margin: '0 0 8px 0' }}>{user.username}</h2>
              <p style={{ color: '#666' }}>{user.email}</p>
              {user.phone && <p style={{ color: '#666' }}>手机号: {user.phone}</p>}
            </div>
          </div>

          <Row gutter={[16, 16]} style={{ marginTop: 32 }}>
            <Col span={8}>
              <div style={{ fontWeight: 500 }}>用户名:</div>
            </Col>
            <Col span={16}>{user.username}</Col>

            <Col span={8}>
              <div style={{ fontWeight: 500 }}>邮箱:</div>
            </Col>
            <Col span={16}>{user.email}</Col>

            <Col span={8}>
              <div style={{ fontWeight: 500 }}>手机号:</div>
            </Col>
            <Col span={16}>{user.phone || '未设置'}</Col>

            <Col span={8}>
              <div style={{ fontWeight: 500 }}>个人简介:</div>
            </Col>
            <Col span={16}>{user.bio || '暂无简介'}</Col>
          </Row>
        </Card>
      ),
    },
    {
      key: 'contents',
      label: '我的内容',
      children: (
        <Spin spinning={myContentsLoading}>
          {myContents.length === 0 ? (
            <Empty description="暂无发布内容">
              <Button type="primary" onClick={() => navigate('/content/create')}>
                去发布
              </Button>
            </Empty>
          ) : (
            <Row gutter={[16, 16]}>
              {myContents.map((content) => (
                <Col xs={24} sm={24} md={12} key={content.id}>
                  <ContentCard
                    content={content}
                    onLike={() => {}}
                    onFavorite={() => {}}
                  />
                </Col>
              ))}
            </Row>
          )}
        </Spin>
      ),
    },
    {
      key: 'favorites',
      label: '我的收藏',
      children: (
        <div style={{ padding: 24 }}>
          <Empty description="收藏功能待后端支持" />
        </div>
      ),
    },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Tabs
        activeKey={tabKey}
        onChange={setTabKey}
        items={tabItems}
        style={{ borderRadius: 8 }}
      />
    </div>
  );
};

export default ProfilePage;
