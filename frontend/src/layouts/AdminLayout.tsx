// 后台管理布局组件
import React from 'react';
import { Layout, Menu, Avatar, Dropdown, Space } from 'antd';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import {
  DashboardOutlined,
  FileTextOutlined,
  UserOutlined,
  LogoutOutlined,
  HomeOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@stores/auth';
import type { MenuProps } from 'antd';

const { Header, Sider, Content } = Layout;

const AdminLayout: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, clearAuth } = useAuthStore();

  const menuItems: MenuProps['items'] = [
    {
      key: '/admin',
      icon: <DashboardOutlined />,
      label: '仪表盘',
    },
    {
      key: '/admin/content',
      icon: <FileTextOutlined />,
      label: '内容管理',
    },
    {
      key: '/admin/user',
      icon: <UserOutlined />,
      label: '用户管理',
    },
  ];

  const handleMenuClick: MenuProps['onClick'] = (e) => {
    navigate(e.key);
  };

  const handleLogout = () => {
    clearAuth();
    navigate('/login');
  };

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'home',
      icon: <HomeOutlined />,
      label: '返回前台',
      onClick: () => navigate('/'),
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
          <h2 style={{ color: '#fff', margin: 0 }}>管理后台</h2>
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header style={{ background: '#fff', padding: '0 24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Space size="middle">
            <h2 style={{ margin: 0 }}>帅气气管理后台</h2>
          </Space>
          {user && (
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space style={{ cursor: 'pointer' }}>
                <Avatar src={user.avatar} size={40}>
                  {user.username[0]?.toUpperCase()}
                </Avatar>
                <span>{user.username}</span>
              </Space>
            </Dropdown>
          )}
        </Header>
        <Content style={{ margin: '24px', background: '#fff', borderRadius: 4, padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;
