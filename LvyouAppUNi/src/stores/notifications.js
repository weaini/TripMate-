import { defineStore } from 'pinia'
import { fetchNotifications, fetchUnreadCount, markRead, markAllRead, getMyNotificationPreference, updateMyNotificationPreference } from '@/api/notifications'

export const useNotificationStore = defineStore('notifications', {
  state: () => ({
    items: [],
    page: 0,
    size: 10,
    finished: false,
    loading: false,
    unreadCount: 0,
    client: null,
    connected: false,
    preference: null,
  }),

  actions: {
    async init() {
      await this.refreshUnread()
      await this.load(true)
      this.connect()
      await this.loadPreference()
    },

    async load(reset = false) {
      if (this.loading || this.finished && !reset) return
      this.loading = true
      try {
        const page = reset ? 0 : this.page
        const res = await fetchNotifications({ page, size: this.size })
        const content = res.content || res.data?.content || []
        this.items = reset ? content : this.items.concat(content)
        this.page = page + 1
        this.finished = content.length < this.size
      } finally {
        this.loading = false
      }
    },

    async refreshUnread() {
      const res = await fetchUnreadCount()
      this.unreadCount = res.count ?? 0
    },

    async markSelectedRead(ids) {
      if (!ids || ids.length === 0) return
      await markRead(ids)
      this.items = this.items.map(n => ids.includes(n.id) ? { ...n, status: 'READ' } : n)
      await this.refreshUnread()
    },

    async markAllReadAction() {
      await markAllRead()
      this.items = this.items.map(n => ({ ...n, status: 'READ' }))
      this.unreadCount = 0
    },

    async loadPreference() {
      this.preference = await getMyNotificationPreference()
    },

    async updatePreference(updates) {
      const saved = await updateMyNotificationPreference(updates)
      this.preference = saved
      return saved
    },

    // WebSocket STOMP 连接
    async connect() {
      if (this.connected) return
      try {
        const token = localStorage.getItem('token')
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
        if (!userInfo?.id) return

        const SockJS = (await import('https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js')).default
        const { Client } = await import('https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/+esm')

        const wsUrl = 'http://localhost:8081/ws'
        const client = new Client({
          webSocketFactory: () => new SockJS(wsUrl),
          connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
          debug: () => {},
          reconnectDelay: 3000,
        })

        client.onConnect = () => {
          this.connected = true
          client.subscribe(`/topic/user/${userInfo.id}/notifications`, (message) => {
            try {
              const data = JSON.parse(message.body)
              this.items.unshift(data)
              this.unreadCount += 1
            } catch (e) {
              this.unreadCount += 1
            }
          })
        }

        client.onStompError = () => { this.connected = false }
        client.onWebSocketClose = () => { this.connected = false }
        client.activate()
        this.client = client
      } catch (e) {
        // 忽略连接失败，仍可使用拉取接口
      }
    },
  },
})


