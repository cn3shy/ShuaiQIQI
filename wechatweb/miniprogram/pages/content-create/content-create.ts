const app = getApp<IAppOption>()

Component({
  data: {
    title: '',
    summary: '',
    content: '',
    coverImage: '',
    loading: false,
  },
  methods: {
    onTitleInput(e: any) {
      this.setData({ title: e.detail.value })
    },
    onSummaryInput(e: any) {
      this.setData({ summary: e.detail.value })
    },
    onContentInput(e: any) {
      this.setData({ content: e.detail.value })
    },
    chooseImage() {
      wx.chooseMedia({
        count: 1,
        mediaType: ['image'],
        sourceType: ['album', 'camera'],
        success: (res) => {
          this.setData({ coverImage: res.tempFiles[0].tempFilePath })
        },
      })
    },
    async handleSubmit() {
      if (!this.data.title || !this.data.content) {
        wx.showToast({ title: '请填写标题和内容', icon: 'none' })
        return
      }
      if (!app.globalData.token) {
        wx.navigateTo({ url: '/pages/login/login' })
        return
      }
      this.setData({ loading: true })
      try {
        const res = await wx.request({
          url: `${app.globalData.baseUrl}/content/create`,
          method: 'POST',
          header: { Authorization: `Bearer ${app.globalData.token}` },
          data: {
            title: this.data.title,
            summary: this.data.summary,
            content: this.data.content,
            coverImage: this.data.coverImage,
          },
        })
        if (res.data.code === 200) {
          wx.showToast({ title: '发布成功', icon: 'success' })
          setTimeout(() => wx.navigateBack(), 1500)
        } else {
          wx.showToast({ title: res.data.message || '发布失败', icon: 'none' })
        }
      } catch (err) {
        wx.showToast({ title: '发布失败', icon: 'none' })
        console.error(err)
      } finally {
        this.setData({ loading: false })
      }
    },
  },
})
