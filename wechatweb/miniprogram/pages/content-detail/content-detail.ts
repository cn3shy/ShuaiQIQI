const app = getApp<IAppOption>()

Component({
  data: {
    id: '',
    content: null as any,
    comments: [] as any[],
    loading: false,
  },
  methods: {
    onLoad(options: any) {
      if (options.id) {
        this.setData({ id: options.id })
        this.loadContent()
        this.loadComments()
      }
    },
    async loadContent() {
      this.setData({ loading: true })
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/content/${this.data.id}`,
          method: 'GET',
        })
        if (res.data.code === 200) {
          this.setData({ content: res.data.data })
        }
      } catch (err) {
        console.error(err)
      } finally {
        this.setData({ loading: false })
      }
    },
    async loadComments() {
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/comment/list?contentId=${this.data.id}`,
          method: 'GET',
        })
        if (res.data.code === 200) {
          this.setData({ comments: res.data.data })
        }
      } catch (err) {
        console.error(err)
      }
    },
    async onLike() {
      if (!app.globalData.token) {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      try {
        await wx.request({
          url: `${app.globalData.baseUrl}/content/like/${this.data.id}`,
          method: 'POST',
          header: { Authorization: `Bearer ${app.globalData.token}` },
        })
        this.loadContent()
      } catch (err) {
        console.error(err)
      }
    },
    async onFavorite() {
      if (!app.globalData.token) {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      try {
        await wx.request({
          url: `${app.globalData.baseUrl}/content/favorite/${this.data.id}`,
          method: 'POST',
          header: { Authorization: `Bearer ${app.globalData.token}` },
        })
        this.loadContent()
      } catch (err) {
        console.error(err)
      }
    },
    goToAuthor(e: any) {
      const userId = e.currentTarget.dataset.userid
      wx.navigateTo({ url: `/pages/user-profile/user-profile?userId=${userId}` })
    },
  },
})
