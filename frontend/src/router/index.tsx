// 路由配置
import { createBrowserRouter, Navigate } from 'react-router-dom';
import React from 'react';
import MainLayout from '@layouts/MainLayout';
import MobileLayout from '@layouts/MobileLayout';

// 页面组件（稍后创建）
const HomePage = React.lazy(() => import('@pages/HomePage'));
const LoginPage = React.lazy(() => import('@pages/LoginPage'));
const RegisterPage = React.lazy(() => import('@pages/RegisterPage'));
const ContentListPage = React.lazy(() => import('@pages/ContentListPage'));
const ContentDetailPage = React.lazy(() => import('@pages/ContentDetailPage'));
const CreateContentPage = React.lazy(() => import('@pages/CreateContentPage'));
const ProfilePage = React.lazy(() => import('@pages/ProfilePage'));
const SettingsPage = React.lazy(() => import('@pages/SettingsPage'));
const NotFoundPage = React.lazy(() => import('@pages/NotFoundPage'));

// 响应式布局选择器
const ResponsiveLayout = () => {
  const isMobile = window.innerWidth < 768;
  return isMobile ? <MobileLayout /> : <MainLayout />;
};

export const router = createBrowserRouter([
  {
    path: '/',
    element: <ResponsiveLayout />,
    children: [
      {
        index: true,
        element: <HomePage />,
      },
      {
        path: 'content',
        element: <ContentListPage />,
      },
      {
        path: 'content/:id',
        element: <ContentDetailPage />,
      },
      {
        path: 'content/create',
        element: <CreateContentPage />,
      },
      {
        path: 'profile',
        element: <ProfilePage />,
      },
      {
        path: 'profile/settings',
        element: <SettingsPage />,
      },
    ],
  },
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/register',
    element: <RegisterPage />,
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);