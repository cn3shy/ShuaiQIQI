// 移动端布局组件
import React from 'react';
import { Layout, TabBar, Avatar, Badge } from 'antd-mobile';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  HomeOutlined,
  CompassOutlined,
  PlusCircleOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@stores/auth';

const MobileLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuthStore();

  const tabs = [
    {
      key: '/',
      title: '首页',
      icon: <HomeOutlined />,
    },
    {
      key: '/content',
      title: '发现',
      icon: <CompassOutlined />,
    },
    {
      key: '/content/create',
      title: '发布',
      icon: <PlusCircleOutlined />,
    },
    {
      key: '/profile',
      title: '我的',
      icon: <UserOutlined />,
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh', paddingBottom: 50 }}>
      <Outlet />
      <TabBar
        activeKey={location.pathname}
        onChange={(key) => navigate(key)}
        safeArea
      >
        {tabs.map((item) => (
          <TabBar.Item
            key={item.key}
            icon={item.icon}
            title={item.title}
          />
        ))}
      </TabBar>
    </Layout>
  );
};

export default MobileLayout;