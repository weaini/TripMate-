<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useNotificationStore } from '@/stores/notifications'

const userStore = useUserStore()
const notificationStore = useNotificationStore()

onMounted(async () => {
  // 应用启动时初始化用户状态
  await userStore.initializeUser()
  if (userStore.isLoggedIn) {
    // 初始化通知（未读数与订阅）
    await notificationStore.init()
  }
})
</script>

<style>
#app {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: #2c3e50;
  background-color: #f5f5f5;
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
  margin: 0;
  padding: 0rem;
  -webkit-overflow-scrolling: touch;
}

* {
  box-sizing: border-box;
  margin: 0;
  padding: 0rem;
}

body {
  margin: 0;
  padding: 0rem;
  background-color: #f5f5f5;
  height: 100vh;
  width: 100vw;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

html {
  margin: 0;
  padding: 0rem;
  height: 100vh;
  width: 100vw;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

/* 移动端适配 */
@media (max-width: 768px) {
  #app {
    max-width: 100vw;
    overflow-x: hidden;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  body {
    overflow-x: hidden;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  html {
    overflow-x: hidden;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 4px;
  height: 4px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 2px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
