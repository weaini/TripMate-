import { defineStore } from 'pinia'
import { getCurrentUser, login } from '@/api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: localStorage.getItem('token') || null,
    isLoggedIn: !!localStorage.getItem('token'),
  }),

  getters: {
    // 获取用户信息
    getUserInfo: (state) => state.userInfo,
    
    // 用户信息别名，保持向后兼容
    user: (state) => state.userInfo,
    
    // 是否已登录
    getIsLoggedIn: (state) => state.isLoggedIn,
    
    // 获取用户角色
    getUserRole: (state) => state.userInfo?.role || 'TOURIST',
    
    // 是否为管理员
    isAdmin: (state) => state.userInfo?.role === 'ADMIN',
    
    // 是否为旅游达人
    isExpert: (state) => state.userInfo?.role === 'EXPERT',
  },

  actions: {
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      this.isLoggedIn = true
    },

    // 设置token
    setToken(token) {
      this.token = token
      localStorage.setItem('token', token)
    },

    // 登录
    async login(loginData) {
      try {
        const response = await login(loginData)
        if (response.token) {
          this.setToken(response.token)
          await this.fetchUserInfo()
          return response
        }
      } catch (error) {
        throw error
      }
    },

    // 获取用户信息
    async fetchUserInfo() {
      try {
        const userInfo = await getCurrentUser()
        this.setUserInfo(userInfo)
        return userInfo
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果是401错误，清除本地存储
        if (error.response?.status === 401) {
          this.logout()
        }
        throw error
      }
    },

    // 登出
    logout() {
      this.userInfo = null
      this.token = null
      this.isLoggedIn = false
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },

    // 更新用户信息
    updateUserInfo(updates) {
      if (this.userInfo) {
        this.userInfo = { ...this.userInfo, ...updates }
        // 同时更新本地存储
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      }
    },

    // 初始化用户状态
    async initializeUser() {
      if (this.token && !this.userInfo) {
        try {
          await this.fetchUserInfo()
        } catch (error) {
          console.error('初始化用户信息失败:', error)
          // 如果token无效，清除登录状态
          if (error.response?.status === 401) {
            this.logout()
          }
        }
      }
    },
  },
})
