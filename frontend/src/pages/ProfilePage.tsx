import React, { useState, useEffect } from 'react';
import { Card, Avatar, Tabs, Row, Col, Spin, Empty, Upload, Button, Form, Input, message } from 'antd';
import { UserOutlined, EditOutlined, StarOutlined } from '@ant-design/icons';
import { getCurrentUser, updateUserInfo, uploadAvatar, changePassword } from '@services/user';
import { useAuthStore } from '@stores/auth';
import type { User } from '@types';

/**
 * 个人中心页
 */
const ProfilePage: React.FC = () => {
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [tabKey, setTabKey] = useState('info');

  const loadUserInfo = async () => {
    if (user) return; // 已经有用户信息
    setLoading(true);
    try {
      const response = await getCurrentUser();
      // TODO: 更新用户信息到store
    } catch (error) {
      console.error('加载用户信息失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUserInfo();
  }, []);

  const handleAvatarUpload = async (file: File) => {
    try {
      const response = await uploadAvatar(file);
      message.success('头像上传成功');
      return { url: response.data.url };
    } catch (error) {
      message.error('上传失败');
      return { url: '' };
    }
  };

  const handleInfoSubmit = async (values: Partial<User>) => {
    if (!user) return;
    setLoading(true);
    try {
      await updateUserInfo(values);
      message.success('信息更新成功');
    } catch (error: any) {
      message.error(error.message || '更新失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordSubmit = async (values: any) => {
    if (!user) return;
    setLoading(true);
    try {
      await changePassword(values.oldPassword, values.newPassword);
      message.success('密码修改成功，请重新登录');
      localStorage.removeItem('token');
      window.location.href = '/login';
    } catch (error: any) {
      message.error(error.message || '修改失败');
    } finally {
      setLoading(false);
    }
  };

  if (!user) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Spin spinning={loading}>
          <div style={{ padding: '40px 0' }}>
            <UserOutlined style={{ fontSize: 48, color: '#999' }} />
            <p style={{ marginTop: 16 }}>请先登录</p>
          </div>
        </Spin>
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
              {user.username[0]?.toUpperCase()}
            </Avatar>
            <div style={{ flex: 1 }}>
              <h2 style={{ margin: '0 0 8px 0' }}>{user.username}</h2>
              <p style={{ color: '#666' }}>{user.email}</p>
              {user.phone && <p style={{ color: '#666' }}>手机号: {user.phone}</p>}
            </div>
          </div>

          <Row gutter={16} style={{ marginTop: 32 }}>
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
      key: 'favorite',
      label: '我的收藏',
      children: (
        <div style={{ padding: 24 }}>
          <Empty description="暂无收藏内容" />
        </div>
      ),
    },
    {
      key: 'settings',
      label: '设置',
      children: (
        <Card title="设置">
          <Form form={Form.useForm()} layout="vertical" onFinish={handleInfoSubmit} initialValues={user}>
            <Form.Item label="用户名" name="username">
              <Input readOnly />
            </Form.Item>

            <Form.Item label="邮箱" name="email">
              <Input readOnly />
            </Form.Item>

            <Form.Item label="手机号" name="phone">
              <Input placeholder="请输入手机号" />
            </Form.Item>

            <Form.Item label="个人简介" name="bio">
              <Input.TextArea placeholder="介绍一下自己" rows={4} maxLength={200} />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading}>保存信息</Button>
            </Form.Item>
          </Form>

          <div style={{ marginTop: 32 }}>
            <h3>修改密码</h3>
            <Form form={Form.useForm()} layout="vertical" onFinish={handlePasswordSubmit}>
              <Form.Item
                name="oldPassword"
                rules={[{ required: true, message: '请输入旧密码' }]}
              >
                <Input.Password placeholder="旧密码" />
              </Form.Item>

              <Form.Item
                name="newPassword"
                rules={[{ required: true, message: '请输入新密码' }, { min: 6, message: '密码长度至少6位' }]}
              >
                <Input.Password placeholder="新密码" />
              </Form.Item>

              <Form.Item
                name="confirmPassword"
                dependencies={['newPassword']}
                rules={[
                  { required: true, message: '请确认新密码' },
                  ({ getFieldValue }) => ({
                    validator(_, value) {
                      if (!value || getFieldValue('newPassword') === value) {
                        return Promise.resolve();
                      }
                      return Promise.reject(new Error('两次密码输入不一致'));
                    },
                  }),
                ]}
              >
                <Input.Password placeholder="确认新密码" />
              </Form.Item>

              <Form.Item>
                <Button type="primary" htmlType="submit" loading={loading}>修改密码</Button>
              </Form.Item>
            </Form>
          </div>
        </Card>
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