const app = getApp<IAppOption>()
const defaultAvatarUrl = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'

Component({
  data: {
    userInfo: null as any,
    isLogin: false,
  },
  methods: {
    onLoad() {
      this.checkLogin()
    },
    onShow() {
      if (typeof this.getTabBar === 'function') {
        this.getTabBar().setData({ selected: 3 })
      }
      this.checkLogin()
    },
    checkLogin() {
      const token = app.globalData.token
      const userInfo = app.globalData.userInfo
      this.setData({
        isLogin: !!token,
        userInfo: userInfo || { avatarUrl: defaultAvatarUrl, nickName: '未登录' },
      })
    },
    goToLogin() {
      wx.navigateTo({ url: '/pages/login/login' })
    },
    goToSettings() {
      wx.navigateTo({ url: '/pages/settings/settings' })
    },
    goToMyContent() {
      if (!this.data.isLogin) {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      wx.navigateTo({ url: '/pages/user-profile/user-profile' })
    },
  },
})
