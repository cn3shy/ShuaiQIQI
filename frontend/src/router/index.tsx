// 路由配置
import { createBrowserRouter, Navigate } from 'react-router-dom';
import React, { Suspense, useState, useEffect } from 'react';
import MainLayout from '@layouts/MainLayout';
import MobileLayout from '@layouts/MobileLayout';
import AdminLayout from '@layouts/AdminLayout';
import { useAuthStore } from '@stores/auth';
import ErrorBoundary from '@components/ErrorBoundary';

// 页面组件（稍后创建）
const HomePage = React.lazy(() => import('@pages/HomePage'));
const LoginPage = React.lazy(() => import('@pages/LoginPage'));
const RegisterPage = React.lazy(() => import('@pages/RegisterPage'));
const ForgotPasswordPage = React.lazy(() => import('@pages/ForgotPasswordPage'));
const ContentListPage = React.lazy(() => import('@pages/ContentListPage'));
const ContentDetailPage = React.lazy(() => import('@pages/ContentDetailPage'));
const CreateContentPage = React.lazy(() => import('@pages/CreateContentPage'));
const ProfilePage = React.lazy(() => import('@pages/ProfilePage'));
const SettingsPage = React.lazy(() => import('@pages/SettingsPage'));
const NotificationPage = React.lazy(() => import('@pages/NotificationPage'));
const UserDetailPage = React.lazy(() => import('@pages/UserDetailPage'));
const NotFoundPage = React.lazy(() => import('@pages/NotFoundPage'));

// 后台管理页面
const AdminDashboardPage = React.lazy(() => import('@pages/admin/DashboardPage'));
const AdminContentPage = React.lazy(() => import('@pages/admin/ContentPage'));
const AdminUserPage = React.lazy(() => import('@pages/admin/UserPage'));

// 响应式布局选择器
const ResponsiveLayout = () => {
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return isMobile ? <MobileLayout /> : <MainLayout />;
};

// 路由守卫组件
const ProtectedRoute: React.FC<{ children: React.ReactNode; adminOnly?: boolean }> = ({ children, adminOnly }) => {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (adminOnly && user?.role !== 'admin') {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <Suspense fallback={<div style={{ padding: 24, textAlign: 'center' }}>加载中...</div>}>
        <ResponsiveLayout />
      </Suspense>
    ),
    errorElement: <Navigate to="/404" replace />,
    children: [
      {
        index: true,
        element: (
          <ErrorBoundary>
            <HomePage />
          </ErrorBoundary>
        ),
      },
      {
        path: 'content',
        element: (
          <ErrorBoundary>
            <ContentListPage />
          </ErrorBoundary>
        ),
      },
      {
        path: 'content/:id',
        element: (
          <ErrorBoundary>
            <ContentDetailPage />
          </ErrorBoundary>
        ),
      },
      {
        path: 'content/create',
        element: (
          <ProtectedRoute>
            <ErrorBoundary>
              <CreateContentPage />
            </ErrorBoundary>
          </ProtectedRoute>
        ),
      },
      {
        path: 'profile',
        element: (
          <ProtectedRoute>
            <ErrorBoundary>
              <ProfilePage />
            </ErrorBoundary>
          </ProtectedRoute>
        ),
      },
      {
        path: 'profile/settings',
        element: (
          <ProtectedRoute>
            <ErrorBoundary>
              <SettingsPage />
            </ErrorBoundary>
          </ProtectedRoute>
        ),
      },
      {
        path: 'notification',
        element: (
          <ProtectedRoute>
            <ErrorBoundary>
              <NotificationPage />
            </ErrorBoundary>
          </ProtectedRoute>
        ),
      },
      {
        path: 'user/:userId',
        element: (
          <ErrorBoundary>
            <UserDetailPage />
          </ErrorBoundary>
        ),
      },
    ],
  },
  {
    path: '/login',
    element: (
      <Suspense fallback={<div style={{ padding: 24, textAlign: 'center' }}>加载中...</div>}>
        <LoginPage />
      </Suspense>
    ),
  },
  {
    path: '/register',
    element: (
      <Suspense fallback={<div style={{ padding: 24, textAlign: 'center' }}>加载中...</div>}>
        <RegisterPage />
      </Suspense>
    ),
  },
  {
    path: '/forgot-password',
    element: (
      <Suspense fallback={<div style={{ padding: 24, textAlign: 'center' }}>加载中...</div>}>
        <ForgotPasswordPage />
      </Suspense>
    ),
  },
  {
    path: '/admin',
    element: (
      <Suspense fallback={<div style={{ padding: 24, textAlign: 'center' }}>加载中...</div>}>
        <ProtectedRoute adminOnly>
          <AdminLayout />
        </ProtectedRoute>
      </Suspense>
    ),
    children: [
      {
        index: true,
        element: <AdminDashboardPage />,
      },
      {
        path: 'content',
        element: <AdminContentPage />,
      },
      {
        path: 'user',
        element: <AdminUserPage />,
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);