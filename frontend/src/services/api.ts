// API基础配置
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import type { ApiResponse } from '@types';

// 创建axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 是否正在刷新token
let isRefreshing = false;
// 重试队列
let requests: Array<(token: string) => void> = [];

// 刷新token
const refreshToken = async (): Promise<string | null> => {
  const refreshTokenStr = localStorage.getItem('refreshToken');
  if (!refreshTokenStr) {
    return null;
  }
  try {
    const response = await axios.post('/api/auth/refresh-token', { refreshToken: refreshTokenStr });
    if (response.data?.code === 200 || response.data?.code === 0) {
      const { token, refreshToken: newRefreshToken } = response.data.data;
      localStorage.setItem('token', token);
      if (newRefreshToken) {
        localStorage.setItem('refreshToken', newRefreshToken);
      }
      return token;
    }
    return null;
  } catch {
    return null;
  }
};

// 清除认证信息
const clearAuth = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('auth-storage');
};

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 响应拦截器
apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    if (!response.data) {
      return response;
    }
    const { code, message } = response.data;

    // 业务成功
    if (code === 200 || code === 0) {
      return response;
    }

    // 业务失败
    if (code === 401) {
      clearAuth();
      window.location.href = '/login';
      return Promise.reject(new Error('未授权，请重新登录'));
    }

    return Promise.reject(new Error(message || '请求失败'));
  },
  async (error) => {
    const originalRequest = error.config;

    // HTTP 401 且未尝试过刷新
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      if (isRefreshing) {
        // 正在刷新，将请求加入队列
        return new Promise((resolve) => {
          requests.push((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(apiClient(originalRequest));
          });
        });
      }

      isRefreshing = true;

      const newToken = await refreshToken();

      if (newToken) {
        // 刷新成功，重试队列中的请求
        requests.forEach((cb) => cb(newToken));
        requests = [];
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } else {
        // 刷新失败，清除认证信息并跳转登录
        clearAuth();
        window.location.href = '/login';
      }

      isRefreshing = false;
    }

    // 其他错误处理
    if (error.response) {
      switch (error.response.status) {
        case 404:
          error.message = '请求的资源不存在';
          break;
        case 500:
          error.message = '服务器错误';
          break;
        case 502:
          error.message = '网关错误';
          break;
        case 503:
          error.message = '服务不可用';
          break;
        case 504:
          error.message = '网关超时';
          break;
        default:
          error.message = `请求失败 (${error.response.status})`;
      }
    } else if (error.request) {
      error.message = '网络连接失败，请检查网络';
    } else {
      error.message = '请求配置错误';
    }

    return Promise.reject(error);
  }
);

// 封装请求方法
const request = {
  get: <T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
    return apiClient.get(url, config).then((res) => res.data);
  },
  post: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
    return apiClient.post(url, data, config).then((res) => res.data);
  },
  put: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
    return apiClient.put(url, data, config).then((res) => res.data);
  },
  delete: <T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
    return apiClient.delete(url, config).then((res) => res.data);
  },
  patch: <T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> => {
    return apiClient.patch(url, data, config).then((res) => res.data);
  },
};

export default request;
