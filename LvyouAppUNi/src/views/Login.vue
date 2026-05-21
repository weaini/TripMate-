<template>
  <div class="login">
    <van-nav-bar title="登录" left-arrow @click-left="goBack" />
    
    <div class="login-container">
      <!-- 顶部装饰 -->
      <div class="top-decoration">
        <div class="decoration-circle circle-1"></div>
        <div class="decoration-circle circle-2"></div>
        <div class="decoration-circle circle-3"></div>
      </div>

      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo-wrapper">
          <van-icon name="globe-o" size="60" class="logo-icon" />
        </div>
        <h1 class="app-title">旅游社交平台</h1>
        <p class="app-subtitle">发现世界，分享精彩</p>
      </div>

      <!-- 登录卡片 -->
      <div class="login-card">
        <div class="card-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账户</p>
        </div>

        <!-- 登录表单 -->
        <van-form @submit="handleLogin" class="login-form">
          <div class="form-group">
            <van-field
              v-model="loginForm.usernameOrEmail"
              name="usernameOrEmail"
              placeholder="用户名或邮箱"
              :rules="[{ required: true, message: '请输入用户名或邮箱' }]"
              left-icon="user-o"
              class="custom-field"
            />
          </div>
          
          <div class="form-group">
            <van-field
              v-model="loginForm.password"
              type="password"
              name="password"
              placeholder="密码"
              :rules="[{ required: true, message: '请输入密码' }]"
              left-icon="lock"
              class="custom-field"
            />
          </div>

          <div class="form-actions">
            <van-button
              round
              block
              type="primary"
              native-type="submit"
              :loading="loading"
              loading-text="登录中..."
              class="login-btn"
            >
              登录
            </van-button>
          </div>
        </van-form>

        <!-- 其他操作 -->
        <div class="other-actions">
          <van-button type="default" size="small" @click="goToRegister" class="action-btn">
            立即注册
          </van-button>
          <van-button type="default" size="small" @click="forgotPassword" class="action-btn">
            忘记密码？
          </van-button>
        </div>
      </div>

      <!-- 快速登录提示 -->
      <div class="quick-login">
        <p class="quick-login-title">快速体验</p>
        <div class="demo-accounts">
          <div class="demo-account" @click="quickLogin('testuser', '123456')">
            <van-icon name="user-o" size="16" />
            <span>测试用户</span>
          </div>
          <div class="demo-account" @click="quickLogin('traveler', '123456')">
            <van-icon name="star" size="16" />
            <span>旅行达人</span>
          </div>
          <div class="demo-account" @click="quickLogin('admin', '123456')">
            <van-icon name="setting-o" size="16" />
            <span>管理员</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { login } from '@/api/auth'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const loginForm = ref({
  usernameOrEmail: '',
  password: ''
})

// 方法
const goBack = () => {
  router.back()
}

const goToRegister = () => {
  router.push('/register')
}

const forgotPassword = () => {
  showToast('请联系客服重置密码')
}

const handleLogin = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const response = await userStore.login(loginForm.value)
    
    showToast('登录成功')
    
    // 登录成功后跳转到首页
    router.push('/home')
  } catch (error) {
    console.error('登录失败:', error)
    showToast(error.response?.data?.message || '登录失败，请检查用户名和密码')
  } finally {
    loading.value = false
  }
}

const loginWithWechat = () => {
  showToast('微信登录功能开发中')
}

const loginWithQQ = () => {
  showToast('QQ登录功能开发中')
}

const quickLogin = (username, password) => {
  loginForm.value.usernameOrEmail = username
  loginForm.value.password = password
  handleLogin()
}
</script>

<style scoped>
.login {
  min-height: 100vh;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
  margin: 0;
  padding: 0;
}

.login-container {
  padding: 20px;
  height: 100vh;
  width: 100%;
  display: flex;
  flex-direction: column;
  position: relative;
  z-index: 2;
  box-sizing: border-box;
  overflow-y: auto;
}

/* 顶部装饰 */
.top-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 200px;
  overflow: hidden;
  z-index: 1;
}

.decoration-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: -60px;
  right: -30px;
  animation: float 6s ease-in-out infinite;
}

.circle-2 {
  width: 80px;
  height: 80px;
  top: 20px;
  left: -20px;
  animation: float 4s ease-in-out infinite reverse;
}

.circle-3 {
  width: 60px;
  height: 60px;
  top: 80px;
  right: 50px;
  animation: float 5s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

/* Logo区域 */
.logo-section {
  text-align: center;
  margin: 20px 0 30px;
  color: white;
  position: relative;
  z-index: 3;
}

.logo-wrapper {
  margin-bottom: 15px;
}

.logo-icon {
  color: white;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 50%;
  padding: 15px;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.app-title {
  font-size: 28px;
  margin: 0 0 8px 0;
  font-weight: 700;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.app-subtitle {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

/* 登录卡片 */
.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 20px;
  padding: 30px 25px;
  margin-bottom: 20px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.card-header {
  text-align: center;
  margin-bottom: 25px;
}

.card-header h2 {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px 0;
}

.card-header p {
  font-size: 14px;
  color: #666;
  margin: 0;
}

/* 表单样式 */
.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.custom-field {
  background: #f8f9fa;
  border-radius: 12px;
  border: 1px solid #e9ecef;
  transition: all 0.3s ease;
}

.custom-field:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.login-btn {
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

/* 其他操作 */
.other-actions {
  display: flex;
  justify-content: space-between;
  margin-top: 15px;
}

.action-btn {
  font-size: 14px;
  color: #667eea;
  background: transparent;
  border: none;
  padding: 8px 12px;
}

.action-btn:hover {
  background: rgba(102, 126, 234, 0.1);
}

/* 快速登录 */
.quick-login {
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 15px;
  padding: 20px;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.quick-login-title {
  text-align: center;
  color: white;
  font-size: 14px;
  margin: 0 0 15px 0;
  font-weight: 500;
}

.demo-accounts {
  display: flex;
  gap: 10px;
  justify-content: center;
}

.demo-account {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 12px 8px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 10px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  min-width: 60px;
}

.demo-account:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-2px);
}

.demo-account .van-icon {
  margin-bottom: 4px;
}

.demo-account span {
  font-size: 12px;
  font-weight: 500;
}

/* 深度选择器 */
:deep(.van-cell-group) {
  background: transparent;
  border: none;
  box-shadow: none;
}

:deep(.van-field__control) {
  color: #333;
  font-size: 16px;
}

:deep(.van-field__left-icon) {
  color: #667eea;
}

:deep(.van-button--primary) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

:deep(.van-button--primary:active) {
  background: linear-gradient(135deg, #5a6fd8 0%, #6a4190 100%);
}
</style>
