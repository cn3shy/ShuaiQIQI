const app = getApp<IAppOption>()

Component({
  data: {
    username: '',
    password: '',
    confirmPassword: '',
    loading: false,
  },
  methods: {
    onUsernameInput(e: any) {
      this.setData({ username: e.detail.value })
    },
    onPasswordInput(e: any) {
      this.setData({ password: e.detail.value })
    },
    onConfirmPasswordInput(e: any) {
      this.setData({ confirmPassword: e.detail.value })
    },
    async handleRegister() {
      if (!this.data.username || !this.data.password) {
        wx.showToast({ title: '请填写完整信息', icon: 'none' })
        return
      }
      if (this.data.password !== this.data.confirmPassword) {
        wx.showToast({ title: '两次密码不一致', icon: 'none' })
        return
      }
      this.setData({ loading: true })
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/auth/register`,
          method: 'POST',
          data: { username: this.data.username, password: this.data.password },
        })
        if (res.data.code === 200) {
          wx.showToast({ title: '注册成功', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 1500)
        } else {
          wx.showToast({ title: res.data.message || '注册失败', icon: 'none' })
        }
      } catch (err) {
        wx.showToast({ title: '注册失败', icon: 'none' })
        console.error(err)
      } finally {
        this.setData({ loading: false })
      }
    },
    goToLogin() {
      wx.navigateBack()
    },
  },
})
