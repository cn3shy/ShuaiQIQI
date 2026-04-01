// 主布局组件 - PC端和平板端
import React from 'react';
import { Layout, Menu, Avatar, Dropdown, Space } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  HomeOutlined,
  UserOutlined,
  LogoutOutlined,
  SettingOutlined,
  PlusOutlined,
  CompassOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@stores/auth';
import NotificationBell from '@components/NotificationBell';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;

const MainLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, clearAuth } = useAuthStore();

  const menuItems: MenuProps['items'] = [
    {
      key: '/',
      icon: <HomeOutlined />,
      label: '首页',
    },
    {
      key: '/content',
      icon: <CompassOutlined />,
      label: '发现',
    },
    {
      key: '/content/create',
      icon: <PlusOutlined />,
      label: '发布',
    },
    {
      key: '/profile',
      icon: <UserOutlined />,
      label: '我的',
    },
  ];

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    if (e.key !== 'logout') {
      navigate(e.key);
    }
  };

  const handleLogout = () => {
    clearAuth();
    navigate('/login');
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
      onClick: () => navigate('/profile'),
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '设置',
      onClick: () => navigate('/profile/settings'),
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ];

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider breakpoint="lg" collapsedWidth="0">
        <div style={{ padding: 16, textAlign: 'center' }}>
          <h2 style={{ color: '#fff', margin: 0 }}>帅气气</h2>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname.startsWith('/content/') ? '/content' : location.pathname.startsWith('/profile/') ? '/profile' : location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Space size="middle">
            <h2 style={{ margin: 0 }}>帅气气</h2>
          </Space>
          {user ? (
            <Space size={24}>
              <NotificationBell />
              <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
                <Space style={{ cursor: 'pointer' }}>
                  <Avatar src={user.avatar} size={40}>
                    {user.username[0]?.toUpperCase()}
                  </Avatar>
                  <span>{user.username}</span>
                </Space>
              </Dropdown>
            </Space>
          ) : (
            <Space>
              <span onClick={() => navigate('/login')} style={{ cursor: 'pointer' }}>登录</span>
              <span onClick={() => navigate('/register')} style={{ cursor: 'pointer' }}>注册</span>
            </Space>
          )}
        </Header>
        <Content style={{ margin: '24px', background: '#fff', borderRadius: 4 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default MainLayout;
