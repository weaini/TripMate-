<template>
  <div class="strategies">
    <!-- 重定向到新的攻略页面 -->
    <div class="redirect-content">
      <van-loading type="spinner" />
      <p>正在跳转到攻略页面...</p>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" fixed>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/posts">动态</van-tabbar-item>
      <van-tabbar-item icon="calendar-o" to="/activities">活动</van-tabbar-item>
      <van-tabbar-item icon="bookmark-o" to="/guides">攻略</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 底部导航栏当前选中的tab
const activeTab = ref(3)

// 自动重定向到新的攻略页面
onMounted(() => {
  setTimeout(() => {
    router.replace('/guides')
  }, 1000)
})
</script>

<style scoped>
.strategies {
  padding-top: 46px;
  padding-bottom: 50px;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.redirect-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #666;
}

.redirect-content p {
  margin-top: 16px;
  font-size: 14px;
}
</style>
