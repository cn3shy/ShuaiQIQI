const app = getApp<IAppOption>()

Component({
  data: {
    userId: '',
    userInfo: null as any,
    contentList: [] as any[],
    loading: false,
  },
  methods: {
    onLoad(options: any) {
      if (options.userId) {
        this.setData({ userId: options.userId })
        this.loadUserInfo()
        this.loadUserContent()
      }
    },
    async loadUserInfo() {
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/user/${this.data.userId}`,
          method: 'GET',
        })
        if (res.data.code === 200) {
          this.setData({ userInfo: res.data.data })
        }
      } catch (err) {
        console.error(err)
      }
    },
    async loadUserContent() {
      this.setData({ loading: true })
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/content/user/${this.data.userId}`,
          method: 'GET',
        })
        if (res.data.code === 200) {
          this.setData({ contentList: res.data.data })
        }
      } catch (err) {
        console.error(err)
      } finally {
        this.setData({ loading: false })
      }
    },
    goToDetail(e: any) {
      const id = e.currentTarget.dataset.id
      wx.navigateTo({ url: `/pages/content-detail/content-detail?id=${id}` })
    },
  },
})
