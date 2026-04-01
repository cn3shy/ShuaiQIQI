// 后台仪表盘页面
import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Spin } from 'antd';
import {
  FileTextOutlined,
  UserOutlined,
  CommentOutlined,
  LikeOutlined,
} from '@ant-design/icons';
import request from '@services/api';

interface DashboardData {
  totalContent: number;
  totalUsers: number;
  totalComments: number;
  totalLikes: number;
}

const AdminDashboardPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<DashboardData>({
    totalContent: 0,
    totalUsers: 0,
    totalComments: 0,
    totalLikes: 0,
  });

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    setLoading(true);
    try {
      const [contentRes, userRes, commentRes, likesRes] = await Promise.all([
        request.get<number>('/content/count'),
        request.get<number>('/user/count'),
        request.get<number>('/comment/count'),
        request.get<number>('/content/likes/count'),
      ]);

      setData({
        totalContent: contentRes.data || 0,
        totalUsers: userRes.data || 0,
        totalComments: commentRes.data || 0,
        totalLikes: likesRes.data || 0,
      });
    } catch (error) {
      console.error('加载仪表盘数据失败:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>仪表盘</h2>

      <Spin spinning={loading}>
        <Row gutter={[16, 16]}>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="总内容数"
                value={data.totalContent}
                prefix={<FileTextOutlined />}
                valueStyle={{ color: '#1890ff' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="总用户数"
                value={data.totalUsers}
                prefix={<UserOutlined />}
                valueStyle={{ color: '#52c41a' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="总评论数"
                value={data.totalComments}
                prefix={<CommentOutlined />}
                valueStyle={{ color: '#faad14' }}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} md={6}>
            <Card>
              <Statistic
                title="总点赞数"
                value={data.totalLikes}
                prefix={<LikeOutlined />}
                valueStyle={{ color: '#ff4d4f' }}
              />
            </Card>
          </Col>
        </Row>

        <Row gutter={[16, 16]} style={{ marginTop: 24 }}>
          <Col xs={24} md={12}>
            <Card title="最近发布的内容" style={{ height: 300 }}>
              <div style={{ color: '#999', textAlign: 'center', paddingTop: 100 }}>
                图表区域（待实现）
              </div>
            </Card>
          </Col>
          <Col xs={24} md={12}>
            <Card title="用户增长趋势" style={{ height: 300 }}>
              <div style={{ color: '#999', textAlign: 'center', paddingTop: 100 }}>
                图表区域（待实现）
              </div>
            </Card>
          </Col>
        </Row>
      </Spin>
    </div>
  );
};

export default AdminDashboardPage;
