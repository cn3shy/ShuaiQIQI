const app = getApp<IAppOption>();

interface RequestConfig {
  url: string;
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH';
  data?: Record<string, any>;
  header?: Record<string, string>;
  needAuth?: boolean;
}

interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
  timestamp: string;
}

let isRefreshing = false;
let requestQueue: Array<(token: string) => void> = [];

const refreshToken = async (): Promise<string | null> => {
  const refreshToken = wx.getStorageSync('refreshToken');
  if (!refreshToken) {
    return null;
  }

  try {
    const res = await wx.request({
      url: `${app.globalData.baseUrl}/auth/refresh`,
      method: 'POST',
      data: { refreshToken },
    });

    if (res.data?.code === 200) {
      const { token, refreshToken: newRefreshToken } = res.data.data;
      app.globalData.token = token;
      app.globalData.refreshToken = newRefreshToken;
      wx.setStorageSync('token', token);
      wx.setStorageSync('refreshToken', newRefreshToken);
      return token;
    }
    return null;
  } catch (error) {
    console.error('刷新token失败:', error);
    wx.removeStorageSync('token');
    wx.removeStorageSync('refreshToken');
    wx.removeStorageSync('userInfo');
    return null;
  }
};

const handleUnauthorized = () => {
  wx.removeStorageSync('token');
  wx.removeStorageSync('refreshToken');
  wx.removeStorageSync('userInfo');
  wx.reLaunch({ url: '/pages/login/login' });
};

const request = <T = any>(config: RequestConfig): Promise<ApiResponse<T>> => {
  const { url, method = 'GET', data, header = {}, needAuth = true } = config;

  return new Promise((resolve, reject) => {
    const token = app.globalData.token;

    if (needAuth && !token) {
      handleUnauthorized();
      reject(new Error('未授权，请重新登录'));
      return;
    }

    wx.request({
      url: `${app.globalData.baseUrl}${url}`,
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        Authorization: token ? `Bearer ${token}` : '',
        ...header,
      },
      success: async (res) => {
        const response = res.data as ApiResponse<T>;

        if (response.code === 200 || response.code === 0) {
          resolve(response);
          return;
        }

        if (response.code === 401) {
          if (isRefreshing) {
            return new Promise((resolveQueue) => {
              requestQueue.push((newToken: string) => {
                config.header = config.header || {};
                config.header.Authorization = `Bearer ${newToken}`;
                request<T>(config).then(resolve).catch(reject);
                resolveQueue();
              });
            });
          }

          isRefreshing = true;
          const newToken = await refreshToken();
          isRefreshing = false;

          if (newToken) {
            requestQueue.forEach((cb) => cb(newToken));
            requestQueue = [];
            config.header = config.header || {};
            config.header.Authorization = `Bearer ${newToken}`;
            const result = await request<T>(config);
            resolve(result);
          } else {
            handleUnauthorized();
            reject(new Error('未授权，请重新登录'));
          }
          return;
        }

        reject(new Error(response.message || '请求失败'));
      },
      fail: (error) => {
        let message = '网络请求失败';
        if ((error as any).errMsg) {
          if ((error as any).errMsg.includes('timeout')) {
            message = '请求超时，请检查网络';
          } else if ((error as any).errMsg.includes('fail')) {
            message = '网络连接失败，请检查网络';
          }
        }
        reject(new Error(message));
      },
    });
  });
};

export const get = <T = any>(url: string, data?: Record<string, any>): Promise<ApiResponse<T>> =>
  request<T>({ url, method: 'GET', data });

export const post = <T = any>(url: string, data?: Record<string, any>): Promise<ApiResponse<T>> =>
  request<T>({ url, method: 'POST', data });

export const put = <T = any>(url: string, data?: Record<string, any>): Promise<ApiResponse<T>> =>
  request<T>({ url, method: 'PUT', data });

export const del = <T = any>(url: string, data?: Record<string, any>): Promise<ApiResponse<T>> =>
  request<T>({ url, method: 'DELETE', data });

export const patch = <T = any>(url: string, data?: Record<string, any>): Promise<ApiResponse<T>> =>
  request<T>({ url, method: 'PATCH', data });

export default { get, post, put, delete: del, patch };
