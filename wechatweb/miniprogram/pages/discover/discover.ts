const app = getApp<IAppOption>()

Component({
  data: {
    contentList: [] as any[],
    loading: false,
  },
  methods: {
    onLoad() {
      this.loadContentList()
    },
    onShow() {
      if (typeof this.getTabBar === 'function') {
        this.getTabBar().setData({ selected: 1 })
      }
    },
    async loadContentList() {
      this.setData({ loading: true })
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/content/list`,
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
