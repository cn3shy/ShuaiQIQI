// 移动端布局组件
import React, { useState } from 'react';
import { Layout, Tabs, Avatar, Space, Button } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  HomeOutlined,
  CompassOutlined,
  PlusCircleOutlined,
  UserOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@stores/auth';
import type { TabsProps } from 'antd';
import './MobileLayout.css';

const { Header, Content, Footer } = Layout;

const MobileLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, clearAuth } = useAuthStore();
  const [activeKey, setActiveKey] = useState(location.pathname);

  // 监听路由变化同步 TabBar 状态
  React.useEffect(() => {
    setActiveKey(location.pathname);
  }, [location.pathname]);

  const handleLogout = () => {
    clearAuth();
    navigate('/login');
  };

  const tabItems: TabsProps['items'] = [
    {
      key: '/',
      label: '首页',
      icon: <HomeOutlined />,
    },
    {
      key: '/content',
      label: '发现',
      icon: <CompassOutlined />,
    },
    {
      key: '/content/create',
      label: '发布',
      icon: <PlusCircleOutlined />,
    },
    {
      key: '/profile',
      label: '我的',
      icon: <UserOutlined />,
    },
  ];

  return (
    <Layout className="mobile-layout">
      <Header className="mobile-header">
        <Space>
          <Avatar size="small" src={user?.avatar}>
            {user?.username[0]?.toUpperCase()}
          </Avatar>
          <span style={{ color: '#fff', fontSize: 16 }}>帅气气</span>
        </Space>
      </Header>
      <Content className="mobile-content">
        <Outlet />
      </Content>
      <Footer className="mobile-footer">
        <Tabs
          activeKey={activeKey}
          onChange={(key) => {
            setActiveKey(key);
            navigate(key);
          }}
          items={tabItems}
          size="small"
          centered
        />
        {user && (
          <Button
            size="small"
            block
            icon={<LogoutOutlined />}
            onClick={handleLogout}
            style={{ marginTop: 8 }}
          >
            退出登录
          </Button>
        )}
      </Footer>
    </Layout>
  );
};

export default MobileLayout;
