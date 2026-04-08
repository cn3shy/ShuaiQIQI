const app = getApp<IAppOption>()

Component({
  data: {
    notifications: [] as any[],
    loading: false,
  },
  methods: {
    onLoad() {
      this.loadNotifications()
    },
    onShow() {
      if (typeof this.getTabBar === 'function') {
        this.getTabBar().setData({ selected: 2 })
      }
    },
    async loadNotifications() {
      if (!app.globalData.token) {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      this.setData({ loading: true })
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/notification/list`,
          method: 'GET',
          header: { Authorization: `Bearer ${app.globalData.token}` },
        })
        if (res.data.code === 200) {
          this.setData({ notifications: res.data.data })
        }
      } catch (err) {
        console.error(err)
      } finally {
        this.setData({ loading: false })
      }
    },
    async onTapItem(e: any) {
      const id = e.currentTarget.dataset.id
      const item = this.data.notifications.find(n => n.id === id)
      if (item && !item.isRead) {
        await wx.request({
          url: `${app.globalData.baseUrl}/notification/read/${id}`,
          method: 'PUT',
          header: { Authorization: `Bearer ${app.globalData.token}` },
        })
        this.loadNotifications()
      }
      if (item?.targetType === 'content' && item?.targetId) {
        wx.navigateTo({ url: `/pages/content-detail/content-detail?id=${item.targetId}` })
      }
    },
  },
})
