const app = getApp<IAppOption>();

export interface UserInfo {
  id: string;
  username: string;
  email?: string;
  phone?: string;
  avatar?: string;
  bio?: string;
  role?: string;
  createTime: string;
  updateTime: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  remember?: boolean;
}

export interface RegisterRequest {
  username: string;
  email: string;
  phone?: string;
  password: string;
  confirmPassword: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  user: UserInfo;
}

export const login = async (data: LoginRequest): Promise<AuthResponse> => {
  const res = await wx.request({
    url: `${app.globalData.baseUrl}/auth/login`,
    method: 'POST',
    data,
  });

  if (res.data?.code === 200) {
    const { token, refreshToken, user } = res.data.data;
    app.globalData.token = token;
    app.globalData.refreshToken = refreshToken;
    app.globalData.userInfo = user;

    wx.setStorageSync('token', token);
    wx.setStorageSync('refreshToken', refreshToken);
    wx.setStorageSync('userInfo', user);

    return res.data.data;
  }

  throw new Error(res.data?.message || '登录失败');
};

export const register = async (data: RegisterRequest): Promise<void> => {
  const res = await wx.request({
    url: `${app.globalData.baseUrl}/auth/register`,
    method: 'POST',
    data,
  });

  if (res.data?.code !== 200) {
    throw new Error(res.data?.message || '注册失败');
  }
};

export const wxLogin = async (): Promise<AuthResponse> => {
  const { code } = await wx.login();
  const res = await wx.request({
    url: `${app.globalData.baseUrl}/auth/wx-login`,
    method: 'POST',
    data: { code },
  });

  if (res.data?.code === 200) {
    const { token, refreshToken, user } = res.data.data;
    app.globalData.token = token;
    app.globalData.refreshToken = refreshToken;
    app.globalData.userInfo = user;

    wx.setStorageSync('token', token);
    wx.setStorageSync('refreshToken', refreshToken);
    wx.setStorageSync('userInfo', user);

    return res.data.data;
  }

  throw new Error(res.data?.message || '微信登录失败');
};

export const logout = (): void => {
  app.globalData.token = '';
  app.globalData.refreshToken = '';
  app.globalData.userInfo = null;

  wx.removeStorageSync('token');
  wx.removeStorageSync('refreshToken');
  wx.removeStorageSync('userInfo');

  wx.reLaunch({ url: '/pages/login/login' });
};

export const isLogin = (): boolean => {
  return !!app.globalData.token;
};

export const getUserInfo = async (): Promise<UserInfo> => {
  const res = await wx.request({
    url: `${app.globalData.baseUrl}/user/info`,
    method: 'GET',
    header: {
      Authorization: `Bearer ${app.globalData.token}`,
    },
  });

  if (res.data?.code === 200) {
    app.globalData.userInfo = res.data.data;
    wx.setStorageSync('userInfo', res.data.data);
    return res.data.data;
  }

  throw new Error(res.data?.message || '获取用户信息失败');
};

export const updateUserInfo = async (data: Partial<UserInfo>): Promise<void> => {
  const res = await wx.request({
    url: `${app.globalData.baseUrl}/user/update`,
    method: 'PUT',
    data,
    header: {
      Authorization: `Bearer ${app.globalData.token}`,
    },
  });

  if (res.data?.code === 200) {
    app.globalData.userInfo = { ...app.globalData.userInfo, ...data } as UserInfo;
    wx.setStorageSync('userInfo', app.globalData.userInfo);
  } else {
    throw new Error(res.data?.message || '更新用户信息失败');
  }
};
