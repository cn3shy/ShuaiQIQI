const app = getApp<IAppOption>()

Component({
  data: {
    userInfo: null as any,
  },
  methods: {
    onLoad() {
      this.setData({ userInfo: app.globalData.userInfo })
    },
    goToEditProfile() {
      wx.navigateTo({ url: '/pages/profile-edit/profile-edit' })
    },
    handleLogout() {
      wx.showModal({
        title: '提示',
        content: '确定要退出登录吗？',
        success: (res) => {
          if (res.confirm) {
            app.logout()
            wx.navigateBack()
          }
        },
      })
    },
  },
})
