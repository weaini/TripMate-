<template>
  <div class="profile page-with-tabbar">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="个人中心" fixed>
      <template #left>
        <van-icon name="user-o" size="20" color="#1989fa" />
      </template>
      <template #right>
        <van-icon name="setting-o" size="18" @click="goToSettings" />
      </template>
    </van-nav-bar>

    <!-- 用户信息头部 -->
    <div class="profile-header">
      <!-- 背景装饰 -->
      <div class="header-bg">
        <div class="bg-decoration"></div>
        <div class="bg-pattern"></div>
      </div>
      
      <!-- 用户信息 -->
      <div class="user-section">
        <div class="avatar-section">
          <div class="avatar-wrapper" @click="goToEditProfile">
            <div class="avatar-ring">
              <div class="avatar" @click="goToEditProfile">
                <van-image
                  v-if="userStore.userInfo?.avatar"
                  :src="getUserAvatarUrl(userStore.userInfo)"
                  class="avatar-image"
                  round
                  fit="cover"
                />
                <van-icon v-else name="user-o" size="40" class="avatar-icon" />
              </div>
              <div class="avatar-badge" v-if="userStore.userInfo?.role === 'EXPERT'">
                <van-icon name="star" size="14" />
              </div>
            </div>
            <div class="edit-hint">
              <van-icon name="edit" size="12" />
            </div>
          </div>
        </div>
        
        <div class="user-info">
          <div class="user-name-section">
            <h1 class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username || '游客' }}</h1>
            <van-tag 
              v-if="userStore.userInfo?.role" 
              :type="getRoleTagType(userStore.userInfo.role)"
              size="medium"
              class="role-tag"
            >
              {{ getRoleText(userStore.userInfo.role) }}
            </van-tag>
          </div>
          
          <div class="points-overview" @click="goToPoints">
            <div class="points-display">
              <span class="points-text">
                <span>{{ userStore.userInfo?.points || 0 }} 积分</span>
                <span class="points-separator"></span>
                <span>Lv.{{ levelInfo.currentLevel.level }}</span>
              </span>
              <span class="points-level-tag">{{ levelInfo.currentLevel.title }}</span>
              <van-icon name="arrow" size="14" class="points-arrow" />
            </div>
            <div class="points-next-tip">
              {{ levelInfo.nextLevel ? `距下一等级还差 ${levelInfo.pointsToNextLevel} 分` : '已达到当前最高等级' }}
            </div>
            <div class="points-progress">
              <div class="points-progress-track">
                <div class="points-progress-fill" :style="{ width: `${levelInfo.levelProgress}%` }"></div>
              </div>
              <span class="points-progress-text">{{ levelInfo.levelProgress }}%</span>
            </div>
          </div>
          
          <p class="user-bio">{{ userStore.userInfo?.bio || '这个人很懒，什么都没有留下' }}</p>
          
          <!-- 统计数据 -->
          <div class="stats-grid">
            <div class="stat-card" @click="goToFollowers">
              <div class="stat-content">
                <span class="stat-number">{{ userStore.userInfo?.followersCount || 0 }}</span>
                <span class="stat-label">粉丝</span>
              </div>
            </div>
            
            <div class="stat-card" @click="goToFollowing">
              <div class="stat-content">
                <span class="stat-number">{{ userStore.userInfo?.followingCount || 0 }}</span>
                <span class="stat-label">关注</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <van-button
          type="primary"
          size="normal"
          icon="edit"
          @click="goToEditProfile"
          class="primary-btn"
          block
        >
          编辑资料
        </van-button>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <div class="section-title">
        <h3>我的内容</h3>
        <p>管理您的发布内容</p>
      </div>
      
      <div class="menu-grid">
        <div class="menu-item" @click="goToMyPosts">
          <div class="menu-icon posts">
            <van-icon name="chat-o" size="24" />
          </div>
          <div class="menu-content">
            <h4>我的动态</h4>
            <p>{{ userStore.userInfo?.postsCount || 0 }} 条动态</p>
          </div>
          <div class="menu-arrow">
            <van-icon name="arrow" size="16" />
          </div>
        </div>
        
        <div class="menu-item" @click="goToMyActivities">
          <div class="menu-icon activities">
            <van-icon name="calendar-o" size="24" />
          </div>
          <div class="menu-content">
            <h4>我的活动</h4>
            <p>{{ userStore.userInfo?.activitiesCount || 0 }} 个活动</p>
          </div>
          <div class="menu-arrow">
            <van-icon name="arrow" size="16" />
          </div>
        </div>
        
        <div class="menu-item" @click="goToMyStrategies">
          <div class="menu-icon strategies">
            <van-icon name="bookmark-o" size="24" />
          </div>
          <div class="menu-content">
            <h4>我的攻略</h4>
            <p>{{ userStore.userInfo?.strategiesCount || 0 }} 篇攻略</p>
          </div>
          <div class="menu-arrow">
            <van-icon name="arrow" size="16" />
          </div>
        </div>
        
      </div>
    </div>

    <!-- 钱包和设置 -->
    <div class="wallet-section">
      <div class="wallet-card" @click="goToWallet">
        <div class="wallet-icon">
          <van-icon name="gold-coin-o" size="24" />
        </div>
        <div class="wallet-content">
          <h4>我的钱包</h4>
          <p class="wallet-amount">¥{{ userStore.userInfo?.walletBalance || 0 }}</p>
        </div>
        <div class="wallet-arrow">
          <van-icon name="arrow" size="16" />
        </div>
      </div>
    </div>

    <!-- 管理员和达人功能 -->
    <div v-if="userStore.userInfo?.role === 'ADMIN'" class="admin-section">
      <div class="section-title">
        <h3>管理功能</h3>
        <p>平台管理工具</p>
      </div>
      
      <div class="admin-grid">
        <div class="admin-item" @click="goToAdminDashboard">
          <div class="admin-icon dashboard">
            <van-icon name="manager-o" size="20" />
          </div>
          <div class="admin-content">
            <h4>管理后台</h4>
            <p>内容审核与管理</p>
          </div>
          <van-icon name="arrow" size="16" class="admin-arrow" />
        </div>
      </div>
    </div>

    <!-- 达人申请 -->
    <div v-if="userStore.userInfo?.role === 'TOURIST'" class="expert-section">
      <div class="section-title">
        <h3>成为旅游达人</h3>
        <p>分享您的旅游经验</p>
      </div>
      
      <div class="expert-grid">
        <div class="expert-item" @click="goToExpertApplication">
          <div class="expert-icon application">
            <van-icon name="star-o" size="20" />
          </div>
          <div class="expert-content">
            <h4>申请成为达人</h4>
            <p>获得更多权限和功能</p>
          </div>
          <van-icon name="arrow" size="16" class="expert-arrow" />
        </div>
      </div>
    </div>

    <!-- 设置菜单 -->
    <div class="settings-section">
      <div class="section-title">
        <h3>设置与帮助</h3>
        <p>个性化您的体验</p>
      </div>
      
      <div class="settings-grid">
        <div class="setting-item" @click="goToNotificationSettings">
          <div class="setting-icon notifications">
            <van-icon name="bell-o" size="20" />
          </div>
          <div class="setting-content">
            <h4>通知设置</h4>
            <p>管理消息通知</p>
          </div>
          <van-icon name="arrow" size="16" class="setting-arrow" />
        </div>
        
        <div class="setting-item" @click="goToPrivacySettings">
          <div class="setting-icon privacy">
            <van-icon name="shield-o" size="20" />
          </div>
          <div class="setting-content">
            <h4>隐私设置</h4>
            <p>保护您的隐私</p>
          </div>
          <van-icon name="arrow" size="16" class="setting-arrow" />
        </div>
        
        <div class="setting-item" @click="goToHelp">
          <div class="setting-icon help">
            <van-icon name="question-o" size="20" />
          </div>
          <div class="setting-content">
            <h4>帮助中心</h4>
            <p>获取使用帮助</p>
          </div>
          <van-icon name="arrow" size="16" class="setting-arrow" />
        </div>
        
        <div class="setting-item" @click="goToAbout">
          <div class="setting-icon about">
            <van-icon name="info-o" size="20" />
          </div>
          <div class="setting-content">
            <h4>关于我们</h4>
            <p>了解产品信息</p>
          </div>
          <van-icon name="arrow" size="16" class="setting-arrow" />
        </div>
      </div>
    </div>

    <!-- 退出登录 -->
    <div class="logout-section">
      <van-button
        type="danger"
        size="large"
        block
        @click="handleLogout"
      >
        退出登录
      </van-button>
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
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showConfirmDialog, showToast } from 'vant'
import { getUserAvatarUrl } from '@/utils/avatar'
import { getLevelInfo } from '@/utils/pointsLevel'

const router = useRouter()
const userStore = useUserStore()
const levelInfo = computed(() => getLevelInfo(userStore.userInfo?.points || 0))

// 响应式数据
const activeTab = ref(4) // 当前在"我的"页面

// 方法
const goToSettings = () => {
  router.push('/profile/settings')
}

const goToEditProfile = () => {
  router.push('/profile/edit')
}

const goToFollowers = () => {
  router.push('/profile/followers')
}

const goToFollowing = () => {
  router.push('/profile/following')
}

const goToPoints = () => {
  router.push('/profile/points-records')
}

const getRoleTagType = (role) => {
  switch (role) {
    case 'ADMIN':
      return 'danger'
    case 'EXPERT':
      return 'warning'
    case 'TOURIST':
      return 'primary'
    default:
      return 'default'
  }
}

const getRoleText = (role) => {
  switch (role) {
    case 'ADMIN':
      return '管理员'
    case 'EXPERT':
      return '旅游达人'
    case 'TOURIST':
      return '游客'
    default:
      return '用户'
  }
}

const goToMyPosts = () => {
  console.log('🎯 点击我的动态，准备跳转')
  showToast('正在跳转到我的动态...')
  try {
    router.push('/my-posts')
    console.log('✅ 跳转成功')
  } catch (error) {
    console.error('❌ 跳转失败:', error)
    showToast('跳转失败，请重试')
  }
}

const goToMyActivities = () => {
  console.log('🎯 点击我的活动，准备跳转')
  showToast('正在跳转到我的活动...')
  try {
    router.push('/my-activities')
    console.log('✅ 跳转成功')
  } catch (error) {
    console.error('❌ 跳转失败:', error)
    showToast('跳转失败，请重试')
  }
}

const goToMyStrategies = () => {
  router.push('/my-guides')
}

const goToWallet = () => {
  showToast('钱包功能开发中')
}

const goToNotificationSettings = () => {
  router.push('/notification-settings')
}

const goToPrivacySettings = () => {
  router.push('/profile/privacy')
}

const goToHelp = () => {
  router.push('/help-center')
}

const goToAbout = () => {
  router.push('/about')
}

const goToAdminDashboard = () => {
  router.push('/admin')
}

const goToExpertApplication = () => {
  router.push('/expert/apply')
}

const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '确认退出',
      message: '确定要退出登录吗？'
    })
    
    userStore.logout()
    showToast('已退出登录')
    router.push('/login')
  } catch (error) {
    // 用户取消操作
  }
}

// 初始化
onMounted(async () => {
  console.log('Profile页面初始化，登录状态:', userStore.isLoggedIn, '用户信息:', userStore.userInfo)
  
  // 如果用户未登录，跳转到登录页
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  
  // 如果用户已登录但没有用户信息，尝试获取
  if (userStore.isLoggedIn && !userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      // 如果获取失败，可能token已过期，跳转到登录页
      if (error.response?.status === 401) {
        showToast('登录已过期，请重新登录')
        router.push('/login')
      }
    }
  }
})
</script>

<style scoped>
.profile {
  padding: 0;
  padding-top: var(--nav-height);
  background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: 100vh;
  width: 100%;
  overflow-x: hidden;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  position: relative;
  z-index: 1;
}

/* 用户信息头部 */
.profile-header {
  position: relative;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 30px 20px 20px;
  margin: 0 12px 24px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(102, 126, 234, 0.15);
  width: calc(100% - 24px);
}

.header-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  top: -50px;
  right: -50px;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  animation: float 6s ease-in-out infinite;
}

.bg-pattern {
  position: absolute;
  bottom: -30px;
  left: -30px;
  width: 150px;
  height: 150px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 50%;
  animation: float 4s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% { transform: translateY(0px) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(180deg); }
}

.user-section {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: flex-start;
  gap: 20px;
  margin-bottom: 20px;
}

.avatar-section {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-ring {
  position: relative;
  width: 80px;
  height: 80px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  padding: 3px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.avatar-wrapper:hover .avatar-ring {
  transform: scale(1.05);
  border-color: rgba(255, 255, 255, 0.5);
}

.avatar {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.avatar-icon {
  color: rgba(255, 255, 255, 0.8);
}

.avatar-badge {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background: #ff6b35;
  color: white;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(255, 107, 53, 0.3);
}

.edit-hint {
  position: absolute;
  bottom: -8px;
  right: -8px;
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.user-info {
  flex: 1;
  color: white;
}

.user-name-section {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.username {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.role-tag {
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
}

.user-bio {
  font-size: 14px;
  opacity: 0.9;
  margin: 0 0 20px 0;
  line-height: 1.5;
}

.points-overview {
  margin: 8px 0 16px 0;
  display: inline-flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}

.points-display {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.points-arrow {
  color: rgba(255, 255, 255, 0.9);
}

.points-text {
  font-size: 18px;
  font-weight: 700;
  color: white;
  background: rgba(255, 255, 255, 0.16);
  padding: 9px 18px;
  border-radius: 25px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.22);
  display: inline-block;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.08);
}

.points-separator {
  width: 1px;
  height: 16px;
  margin: 0 10px;
  background: rgba(255, 255, 255, 0.45);
  display: inline-block;
  vertical-align: middle;
}

.points-level-tag {
  font-size: 12px;
  font-weight: 600;
  color: #fffdf2;
  padding: 6px 12px;
  border-radius: 999px;
  background: linear-gradient(90deg, rgba(255, 224, 138, 0.26) 0%, rgba(255, 213, 79, 0.22) 100%);
  border: 1px solid rgba(255, 224, 138, 0.38);
  backdrop-filter: blur(8px);
}

.points-next-tip {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.84);
  line-height: 1.4;
}

.points-progress {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
}

.points-progress-track {
  flex: 1;
  min-width: 160px;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  overflow: hidden;
}

.points-progress-fill {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #ffe08a 0%, #ffd54f 100%);
  box-shadow: 0 0 12px rgba(255, 224, 138, 0.35);
}

.points-progress-text {
  font-size: 12px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
}

.stats-grid {
  display: flex;
  gap: 8px;
  margin-top: 12px;
  justify-content: center;
}

.stat-card {
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(8px);
  border-radius: 8px;
  padding: 8px 16px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.15);
  min-width: 60px;
  flex: 1;
  max-width: 80px;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.2);
  transform: translateY(-1px);
  border-color: rgba(255, 255, 255, 0.3);
}

.stat-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-number {
  font-size: 16px;
  font-weight: 600;
  color: white;
  line-height: 1.2;
}

.stat-label {
  font-size: 11px;
  opacity: 0.85;
  color: white;
  font-weight: 400;
}

.action-buttons {
  display: flex;
  gap: 12px;
  position: relative;
  z-index: 2;
}

.primary-btn {
  flex: 1;
  height: 44px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.9);
  color: #667eea;
  border: none;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.secondary-btn {
  flex: 1;
  height: 44px;
  border-radius: 22px;
  background: transparent;
  color: white;
  border: 2px solid rgba(255, 255, 255, 0.3);
  font-weight: 600;
  backdrop-filter: blur(10px);
}

/* 菜单部分 */
.menu-section, .settings-section, .admin-section, .expert-section {
  margin: 0 12px 24px;
  padding: 0 8px;
  width: calc(100% - 24px);
}

.section-title {
  margin-bottom: 16px;
}

.section-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 4px 0;
}

.section-title p {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  width: 100%;
  max-width: 100%;
}

.menu-item {
  background: white;
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
}

.menu-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
}

.menu-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.menu-icon.posts {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.menu-icon.activities {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.menu-icon.strategies {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.menu-icon.favorites {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.menu-content {
  flex: 1;
}

.menu-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 4px 0;
}

.menu-content p {
  font-size: 12px;
  color: #718096;
  margin: 0;
}

.menu-arrow {
  color: #cbd5e0;
}

/* 钱包部分 */
.wallet-section {
  margin: 0 12px 24px;
  padding: 0 8px;
  width: calc(100% - 24px);
}

.wallet-card {
  background: linear-gradient(135deg, #ffecd2 0%, #fcb69f 100%);
  border-radius: 16px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(252, 182, 159, 0.3);
}

.wallet-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(252, 182, 159, 0.4);
}

.wallet-icon {
  width: 48px;
  height: 48px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #8b4513;
}

.wallet-content {
  flex: 1;
}

.wallet-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #8b4513;
  margin: 0 0 4px 0;
}

.wallet-amount {
  font-size: 20px;
  font-weight: 700;
  color: #8b4513;
  margin: 0;
}

.wallet-arrow {
  color: #8b4513;
}

/* 设置部分 */
.settings-grid, .admin-grid, .expert-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.setting-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
  border: 1px solid #e2e8f0;
}

.setting-item:hover {
  background: #f7fafc;
  transform: translateX(4px);
}

.setting-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.setting-icon.notifications {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.setting-icon.privacy {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.setting-icon.help {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.setting-icon.about {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.setting-content {
  flex: 1;
}

.setting-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a202c;
  margin: 0 0 2px 0;
}

.setting-content p {
  font-size: 12px;
  color: #718096;
  margin: 0;
}

.setting-arrow, .admin-arrow, .expert-arrow {
  color: #cbd5e0;
}

/* 管理员和达人申请样式 */
.admin-item, .expert-item {
  background: white;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.admin-item:hover, .expert-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
}

.admin-icon, .expert-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
}

.admin-icon.dashboard {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.expert-icon.application {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.admin-content, .expert-content {
  flex: 1;
}

.admin-content h4, .expert-content h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.admin-content p, .expert-content p {
  margin: 0;
  font-size: 12px;
  color: #666;
  line-height: 1.4;
}

/* 退出登录 */
.logout-section {
  padding: 20px;
  margin: 0 12px 40px;
  width: calc(100% - 24px);
}

:deep(.van-button--danger) {
  height: 48px;
  border-radius: 24px;
  font-weight: 600;
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a52 100%);
  border: none;
  box-shadow: 0 4px 15px rgba(255, 107, 107, 0.3);
}

:deep(.van-button--danger:hover) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(255, 107, 107, 0.4);
}

/* 响应式设计 */
@media (max-width: 480px) {
  .profile {
    padding-left: 0;
    padding-right: 0;
    padding-bottom: 100px;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .profile-header {
    margin: 0 8px 24px;
    padding: 20px 16px;
    width: calc(100% - 16px);
  }
  
  .menu-section, .settings-section, .wallet-section {
    margin: 0 8px 24px;
    padding: 0 4px;
    width: calc(100% - 16px);
  }
  
  .logout-section {
    margin: 0 8px 40px;
    padding: 16px;
    width: calc(100% - 16px);
  }
  
  .menu-grid {
    grid-template-columns: 1fr;
    gap: 8px;
  }
  
  .stats-grid {
    flex-direction: row;
    gap: 6px;
    margin-top: 10px;
  }
  
  .stat-card {
    padding: 6px 12px;
    min-width: 50px;
    max-width: 70px;
  }
  
  .stat-number {
    font-size: 14px;
  }
  
  .stat-label {
    font-size: 10px;
  }
  
  .user-section {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
  
  .avatar-section {
    margin-bottom: 16px;
  }
  
  .points-display {
    text-align: center;
  }
  
  .menu-item {
    padding: 16px;
  }
  
  .wallet-card {
    padding: 16px;
  }
}

@media (max-width: 360px) {
  .profile {
    padding-left: 0;
    padding-right: 0;
    padding-bottom: 100px;
    overflow-y: auto;
    -webkit-overflow-scrolling: touch;
  }
  
  .profile-header {
    margin: 0 4px 20px;
    padding: 16px 12px;
    width: calc(100% - 8px);
  }
  
  .menu-section, .settings-section, .wallet-section {
    margin: 0 4px 20px;
    padding: 0 2px;
    width: calc(100% - 8px);
  }
  
  .logout-section {
    margin: 0 4px 40px;
    padding: 12px;
    width: calc(100% - 8px);
  }
}
</style>
