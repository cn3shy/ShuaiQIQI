const app = getApp<IAppOption>()

Component({
  data: {
    username: '',
    password: '',
    loading: false,
  },
  methods: {
    onUsernameInput(e: any) {
      this.setData({ username: e.detail.value })
    },
    onPasswordInput(e: any) {
      this.setData({ password: e.detail.value })
    },
    async handleLogin() {
      if (!this.data.username || !this.data.password) {
        wx.showToast({ title: '请输入用户名和密码', icon: 'none' })
        return
      }
      this.setData({ loading: true })
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/auth/login`,
          method: 'POST',
          data: { username: this.data.username, password: this.data.password },
        })
        if (res.data.code === 200) {
          app.globalData.token = res.data.data.token
          app.globalData.refreshToken = res.data.data.refreshToken
          wx.setStorageSync('token', res.data.data.token)
          wx.setStorageSync('refreshToken', res.data.data.refreshToken)
          await app.getUserInfo()
          wx.navigateBack()
        } else {
          wx.showToast({ title: res.data.message || '登录失败', icon: 'none' })
        }
      } catch (err) {
        wx.showToast({ title: '登录失败', icon: 'none' })
        console.error(err)
      } finally {
        this.setData({ loading: false })
      }
    },
    goToRegister() {
      wx.navigateTo({ url: '/pages/register/register' })
    },
    async wxLogin() {
      try {
        const loginRes = await wx.login()
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/auth/wx-login`,
          method: 'POST',
          data: { code: loginRes.code },
        })
        if (res.data.code === 200) {
          app.globalData.token = res.data.data.token
          app.globalData.refreshToken = res.data.data.refreshToken
          wx.setStorageSync('token', res.data.data.token)
          wx.setStorageSync('refreshToken', res.data.data.refreshToken)
          await app.getUserInfo()
          wx.navigateBack()
        }
      } catch (err) {
        wx.showToast({ title: '微信登录失败', icon: 'none' })
        console.error(err)
      }
    },
  },
})
