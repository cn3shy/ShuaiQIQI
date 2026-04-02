// 用户详情页面
import React, { useState, useEffect } from 'react';
import { Card, Avatar, Button, Space, Tabs, List, Empty, Spin, message } from 'antd';
import { UserOutlined, EditOutlined } from '@ant-design/icons';
import { useParams, Link } from 'react-router-dom';
import { getUserDetail } from '@services/user';
import { followUser, unfollowUser, getFollowingList, getFollowerList } from '@services/follow';
import { useAuthStore } from '@stores/auth';
import type { User, FollowUser } from '@types';

const UserDetailPage: React.FC = () => {
  const { userId } = useParams<{ userId: string }>();
  const { user: currentUser } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [userInfo, setUserInfo] = useState<User | null>(null);
  const [isFollowing, setIsFollowing] = useState(false);
  const [activeTab, setActiveTab] = useState('following');
  const [followingList, setFollowingList] = useState<FollowUser[]>([]);
  const [followerList, setFollowerList] = useState<FollowUser[]>([]);

  const loadUserInfo = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const data = await getUserDetail(userId);
      setUserInfo(data.data);
      if (currentUser && currentUser.id !== userId) {
        try {
          const followData = await getFollowingList(currentUser.id, { page: 1, pageSize: 1000 });
          const isFollowing = followData.data?.list?.some((u: FollowUser) => u.id === userId) || false;
          setIsFollowing(isFollowing);
        } catch {
          setIsFollowing(false);
        }
      }
    } catch (error) {
      console.error('加载用户信息失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadFollowingList = async () => {
    if (!userId) return;
    try {
      const data = await getFollowingList(userId, { page: 1, pageSize: 20 });
      setFollowingList(data.data?.list || []);
    } catch (error) {
      console.error('加载关注列表失败:', error);
    }
  };

  const loadFollowerList = async () => {
    if (!userId) return;
    try {
      const data = await getFollowerList(userId, { page: 1, pageSize: 20 });
      setFollowerList(data.data?.list || []);
    } catch (error) {
      console.error('加载粉丝列表失败:', error);
    }
  };

  useEffect(() => {
    loadUserInfo();
    loadFollowingList();
    loadFollowerList();
  }, [userId]);

  const handleFollow = async () => {
    if (!userId || !currentUser) {
      message.warning('请先登录');
      return;
    }

    try {
      if (isFollowing) {
        await unfollowUser(userId);
        setIsFollowing(false);
        message.success('已取消关注');
      } else {
        await followUser(userId);
        setIsFollowing(true);
        message.success('关注成功');
      }
    } catch {
      message.error('操作失败');
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin spinning={loading} />
      </div>
    );
  }

  if (!userInfo) {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Empty description="用户不存在" />
      </div>
    );
  }

  const isOwnProfile = currentUser?.id === userId;

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <Card>
        <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
          <Avatar
            size={100}
            src={userInfo.avatar}
            icon={<UserOutlined />}
            style={{ backgroundColor: '#1890ff' }}
          >
            {userInfo.username[0]?.toUpperCase()}
          </Avatar>
          <div style={{ flex: 1 }}>
            <h2 style={{ margin: '0 0 8px 0' }}>{userInfo.username}</h2>
            <p style={{ color: '#666', margin: '0 0 16px 0' }}>
              {userInfo.bio || '这个人很懒，什么都没留下'}
            </p>
            <Space size={16}>
              <span>
                <strong>{followingList.length}</strong> 关注
              </span>
              <span>
                <strong>{followerList.length}</strong> 粉丝
              </span>
            </Space>
          </div>
          <div>
            {isOwnProfile ? (
              <Link to="/profile/settings">
                <Button icon={<EditOutlined />}>编辑资料</Button>
              </Link>
            ) : (
              <Button
                type={isFollowing ? 'default' : 'primary'}
                onClick={handleFollow}
              >
                {isFollowing ? '已关注' : '关注'}
              </Button>
            )}
          </div>
        </div>
      </Card>

      <Card style={{ marginTop: 16 }}>
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'following',
              label: `关注 (${followingList.length})`,
              children: followingList.length === 0 ? (
                <Empty description="暂无关注" />
              ) : (
                <List
                  dataSource={followingList}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={
                          <Link to={`/user/${item.id}`}>
                            <Avatar src={item.avatar} icon={<UserOutlined />}>
                              {item.username[0]?.toUpperCase()}
                            </Avatar>
                          </Link>
                        }
                        title={<Link to={`/user/${item.id}`}>{item.username}</Link>}
                        description={item.bio || '这个人很懒，什么都没留下'}
                      />
                    </List.Item>
                  )}
                />
              ),
            },
            {
              key: 'followers',
              label: `粉丝 (${followerList.length})`,
              children: followerList.length === 0 ? (
                <Empty description="暂无粉丝" />
              ) : (
                <List
                  dataSource={followerList}
                  renderItem={(item) => (
                    <List.Item>
                      <List.Item.Meta
                        avatar={
                          <Link to={`/user/${item.id}`}>
                            <Avatar src={item.avatar} icon={<UserOutlined />}>
                              {item.username[0]?.toUpperCase()}
                            </Avatar>
                          </Link>
                        }
                        title={<Link to={`/user/${item.id}`}>{item.username}</Link>}
                        description={item.bio || '这个人很懒，什么都没留下'}
                      />
                    </List.Item>
                  )}
                />
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
};

export default UserDetailPage;
