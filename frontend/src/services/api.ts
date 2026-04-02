import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse } from 'axios';
import type { ApiResponse } from '@types';
import { useAuthStore } from '@stores/auth';

const apiClient: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
});

let isRefreshing = false;
let requests: Array<(token: string) => void> = [];

const refreshToken = async (): Promise<string | null> => {
  const { refreshToken: refreshTkn } = useAuthStore.getState();
  if (!refreshTkn) {
    return null;
  }
  try {
    const response = await apiClient.post('/auth/refresh', { refreshToken: refreshTkn });
    if (response.data?.code === 200) {
      const { token, refreshToken: newRefreshToken } = response.data.data;
      useAuthStore.getState().setAuth({
        token,
        refreshToken: newRefreshToken,
        user: useAuthStore.getState().user!,
      });
      return token;
    }
    return null;
  } catch {
    useAuthStore.getState().clearAuth();
    return null;
  }
};

const handleUnauthorized = () => {
  useAuthStore.getState().clearAuth();
  window.location.href = '/login';
};

apiClient.interceptors.request.use(
  (config) => {
    const token = useAuthStore.getState().token;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    if (!response.data) return response;
    const { code, message } = response.data;
    if (code === 200 || code === 0) return response;
    if (code === 401) {
      handleUnauthorized();
      return Promise.reject(new Error('未授权，请重新登录'));
    }
    return Promise.reject(new Error(message || '请求失败'));
  },
  async (error) => {
    const originalRequest = error.config;
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      if (isRefreshing) {
        return new Promise((resolve) => {
          requests.push((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            resolve(apiClient(originalRequest));
          });
        });
      }
      isRefreshing = true;
      const newToken = await refreshToken();
      isRefreshing = false;
      if (newToken) {
        requests.forEach((cb) => cb(newToken));
        requests = [];
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return apiClient(originalRequest);
      } else {
        handleUnauthorized();
        return Promise.reject(error);
      }
    }
    if (error.response) {
      switch (error.response.status) {
        case 404: error.message = '请求的资源不存在'; break;
        case 500: error.message = '服务器错误'; break;
        case 502: error.message = '网关错误'; break;
        case 503: error.message = '服务不可用'; break;
        case 504: error.message = '网关超时'; break;
        default: error.message = `请求失败 (${error.response.status})`;
      }
    } else if (error.request) {
      error.message = '网络连接失败，请检查网络';
    } else {
      error.message = '请求配置错误';
    }
    return Promise.reject(error);
  }
);

const request = {
  get: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.get(url, config).then((res) => res.data),
  post: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.post(url, data, config).then((res) => res.data),
  put: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.put(url, data, config).then((res) => res.data),
  delete: <T = unknown>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.delete(url, config).then((res) => res.data),
  patch: <T = unknown>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<ApiResponse<T>> =>
    apiClient.patch(url, data, config).then((res) => res.data),
};

export default request;
