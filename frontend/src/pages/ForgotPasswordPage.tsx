import React, { useState } from 'react';
import { Form, Input, Button, message, Card, Steps, Space } from 'antd';
import { MailOutlined, LockOutlined, SafetyOutlined } from '@ant-design/icons';
import { Link, useNavigate } from 'react-router-dom';
import { forgotPassword, resetPassword } from '@services/auth';

/**
 * 忘记密码页
 */
const ForgotPasswordPage: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [currentStep, setCurrentStep] = useState(0);
  const [resetToken, setResetToken] = useState<string>('');
  const navigate = useNavigate();

  // 发送重置链接
  const handleSendResetLink = async (values: { email: string }) => {
    setLoading(true);
    try {
      await forgotPassword(values.email);
      message.success('重置链接已发送到您的邮箱，请查收');
      setCurrentStep(1);
      // 模拟获取重置令牌（实际应该从邮件中获取）
      // 这里为了演示，直接生成一个模拟令牌
      setResetToken('demo-token-' + Date.now());
    } catch (error: any) {
      message.error(error.message || '发送失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  // 重置密码
  const handleResetPassword = async (values: { resetCode: string; newPassword: string; confirmPassword: string }) => {
    if (values.newPassword !== values.confirmPassword) {
      message.error('两次密码输入不一致');
      return;
    }

    setLoading(true);
    try {
      await resetPassword(resetToken, values.newPassword);
      message.success('密码重置成功，请重新登录');
      navigate('/login');
    } catch (error: any) {
      message.error(error.message || '重置失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  const steps = [
    {
      title: '验证邮箱',
      content: (
        <Form form={form} layout="vertical" onFinish={handleSendResetLink}>
          <Form.Item
            name="email"
            rules={[
              { required: true, message: '请输入邮箱' },
              { type: 'email', message: '请输入有效的邮箱地址' }
            ]}
          >
            <Input prefix={<MailOutlined />} placeholder="请输入注册邮箱" size="large" />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} block size="large">
              发送重置链接
            </Button>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <Link to="/login">返回登录</Link>
          </div>
        </Form>
      ),
    },
    {
      title: '重置密码',
      content: (
        <Form form={form} layout="vertical" onFinish={handleResetPassword}>
          <Form.Item
            name="resetCode"
            rules={[{ required: true, message: '请输入重置码' }]}
          >
            <Input prefix={<SafetyOutlined />} placeholder="请输入邮件中的重置码" size="large" />
          </Form.Item>

          <Form.Item
            name="newPassword"
            rules={[
              { required: true, message: '请输入新密码' },
              { min: 6, message: '密码长度至少6位' }
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="新密码" size="large" />
          </Form.Item>

          <Form.Item
            name="confirmPassword"
            rules={[{ required: true, message: '请确认密码' }]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="确认密码" size="large" />
          </Form.Item>

          <Form.Item>
            <Space direction="vertical" style={{ width: '100%' }}>
              <Button type="primary" htmlType="submit" loading={loading} block size="large">
                重置密码
              </Button>
              <Button block onClick={() => setCurrentStep(0)}>
                重新发送
              </Button>
            </Space>
          </Form.Item>

          <div style={{ textAlign: 'center' }}>
            <Link to="/login">返回登录</Link>
          </div>
        </Form>
      ),
    },
  ];

  return (
    <div style={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#f0f2f5' }}>
      <Card title="忘记密码 - 帅气气" style={{ width: 450 }}>
        <Steps current={currentStep} items={steps.map(s => ({ title: s.title }))} style={{ marginBottom: 24 }} />
        {steps[currentStep].content}
      </Card>
    </div>
  );
};

export default ForgotPasswordPage;
