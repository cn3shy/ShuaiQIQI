app.ts

interface IUserInfo {
  id: string;
  username: string;
  avatar?: string;
  bio?: string;
}

interface IAppOption {
  globalData: {
    token: string;
    refreshToken: string;
    userInfo: IUserInfo | null;
    baseUrl: string;
  };
  login: () => Promise<void>;
  getUserInfo: () => Promise<void>;
  logout: () => void;
  isLogin: () => boolean;
}

App<IAppOption>({
  globalData: {
    token: '',
    refreshToken: '',
    userInfo: null,
    baseUrl: 'http://localhost:8080/api',
  },

  onLaunch() {
    this.initAuth();
  },

  onShow() {
    this.checkLoginStatus();
  },

  initAuth() {
    const token = wx.getStorageSync('token');
    const refreshToken = wx.getStorageSync('refreshToken');
    const userInfo = wx.getStorageSync('userInfo');

    if (token) {
      this.globalData.token = token;
      this.globalData.refreshToken = refreshToken || '';
      this.globalData.userInfo = userInfo || null;
    }
  },

  checkLoginStatus() {
    const token = wx.getStorageSync('token');
    if (!token) {
      this.globalData.token = '';
      this.globalData.userInfo = null;
    }
  },

  async login() {
    try {
      const { code } = await wx.login();
      const res = await this.request('/auth/wx-login', {
        method: 'POST',
        data: { code },
      });

      if (res.code === 200) {
        const { token, refreshToken, user } = res.data;
        this.globalData.token = token;
        this.globalData.refreshToken = refreshToken;
        this.globalData.userInfo = user;

        wx.setStorageSync('token', token);
        wx.setStorageSync('refreshToken', refreshToken);
        wx.setStorageSync('userInfo', user);
      }
    } catch (error) {
      console.error('登录失败:', error);
      throw error;
    }
  },

  async getUserInfo() {
    try {
      const res = await this.request('/user/info');
      if (res.code === 200) {
        this.globalData.userInfo = res.data;
        wx.setStorageSync('userInfo', res.data);
      }
    } catch (error) {
      console.error('获取用户信息失败:', error);
    }
  },

  logout() {
    this.globalData.token = '';
    this.globalData.refreshToken = '';
    this.globalData.userInfo = null;

    wx.removeStorageSync('token');
    wx.removeStorageSync('refreshToken');
    wx.removeStorageSync('userInfo');

    wx.reLaunch({ url: '/pages/login/login' });
  },

  isLogin(): boolean {
    return !!this.globalData.token;
  },

  request(url: string, options: WechatMiniprogram.RequestOption = {}) {
    return new Promise<any>((resolve, reject) => {
      wx.request({
        url: `${this.globalData.baseUrl}${url}`,
        method: 'GET',
        header: {
          'Content-Type': 'application/json',
          Authorization: this.globalData.token ? `Bearer ${this.globalData.token}` : '',
        },
        ...options,
        success: (res) => {
          if (res.statusCode === 401) {
            this.logout();
            reject(new Error('未授权，请重新登录'));
            return;
          }
          resolve(res.data);
        },
        fail: (err) => reject(err),
      });
    });
  },
});
