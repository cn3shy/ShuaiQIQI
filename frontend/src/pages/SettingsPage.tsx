import React, { useState } from 'react';
import { Card, Upload, Button, Form, Input, message, Avatar, Spin } from 'antd';
import { UserOutlined, EditOutlined } from '@ant-design/icons';
import { updateUserInfo, uploadAvatar, changePassword } from '@services/user';
import { useAuthStore } from '@stores/auth';
import type { User } from '@types';

/**
 * 设置页
 */
const SettingsPage: React.FC = () => {
  const { user, setUser } = useAuthStore();
  const [infoForm] = Form.useForm();
  const [passwordForm] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [passwordLoading, setPasswordLoading] = useState(false);

  const handleAvatarUpload = async (file: File) => {
    try {
      const response = await uploadAvatar(file);
      const avatarUrl = response.data.url;
      setUser({
        ...user!,
        avatar: avatarUrl,
      });
      message.success('头像上传成功');
      return { url: avatarUrl };
    } catch {
      message.error('上传失败');
      return { url: '' };
    }
  };

  const handleAvatarCustomRequest = async ({ file, onSuccess, onError }: { file: File; onSuccess?: (response: unknown) => void; onError?: (error: Error) => void }) => {
    try {
      await handleAvatarUpload(file as File);
      onSuccess?.('ok');
    } catch (err) {
      onError?.(err);
    }
  };

  const handleInfoSubmit = async (values: Partial<User>) => {
    if (!user) return;
    setLoading(true);
    try {
      await updateUserInfo(values);
      setUser({
        ...user!,
        ...values,
      });
      message.success('信息更新成功');
    } catch (error) {
      const err = error as Error;
      message.error(err.message || '更新失败');
    } finally {
      setLoading(false);
    }
  };

  const handlePasswordSubmit = async (values: { oldPassword: string; newPassword: string; confirmPassword: string }) => {
    if (!user) return;
    setPasswordLoading(true);
    try {
      await changePassword(values.oldPassword, values.newPassword);
      message.success('密码修改成功，请重新登录');
      localStorage.removeItem('token');
      window.location.href = '/login';
    } catch (error) {
      const err = error as Error;
      message.error(err.message || '修改失败');
    } finally {
      setPasswordLoading(false);
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

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <h2 style={{ marginBottom: 24 }}>设置</h2>

      <Card title="头像设置" style={{ marginBottom: 24 }}>
        <div style={{ textAlign: 'center', padding: 24 }}>
          <Avatar
            size={120}
            src={user.avatar}
            icon={<UserOutlined />}
            style={{ fontSize: 48, marginBottom: 16, backgroundColor: '#1890ff' }}
          >
            {user.username[0]?.toUpperCase()}
          </Avatar>
          <Upload
            name="file"
            showUploadList={false}
            customRequest={handleAvatarCustomRequest}
          >
            <Button icon={<EditOutlined />}>上传头像</Button>
          </Upload>
        </div>
      </Card>

      <Card title="基本信息" style={{ marginBottom: 24 }}>
        <Form
          form={infoForm}
          layout="vertical"
          onFinish={handleInfoSubmit}
          initialValues={user}
        >
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
            <Button type="primary" htmlType="submit" loading={loading}>
              保存
            </Button>
          </Form.Item>
        </Form>
      </Card>

      <Card title="修改密码">
        <Form form={passwordForm} layout="vertical" onFinish={handlePasswordSubmit}>
          <Form.Item
            label="旧密码"
            name="oldPassword"
            rules={[{ required: true, message: '请输入旧密码' }]}
          >
            <Input.Password />
          </Form.Item>

          <Form.Item
            label="新密码"
            name="newPassword"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 8, message: '密码长度至少8位' },
            ]}
          >
            <Input.Password />
          </Form.Item>

          <Form.Item
            label="确认新密码"
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
            <Input.Password />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={passwordLoading}>
              修改密码
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default SettingsPage;