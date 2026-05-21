<template>
  <div class="profile-settings">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="设置" fixed>
      <template #left>
        <van-icon name="arrow-left" @click="goBack" />
      </template>
    </van-nav-bar>

    <!-- 设置列表 -->
    <div class="settings-list">
      <!-- 账户安全 -->
      <van-cell-group title="账户安全">
        <van-cell
          title="修改密码"
          is-link
          @click="goToChangePassword"
        >
          <template #icon>
            <van-icon name="lock" size="20" class="setting-icon" />
          </template>
        </van-cell>
        
        <van-cell
          title="手机号绑定"
          :value="userStore.userInfo?.phone || '未绑定'"
          is-link
          @click="goToBindPhone"
        >
          <template #icon>
            <van-icon name="phone-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
        
        <van-cell
          title="邮箱绑定"
          :value="userStore.userInfo?.email || '未绑定'"
          is-link
          @click="goToBindEmail"
        >
          <template #icon>
            <van-icon name="envelop-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 隐私设置 -->
      <van-cell-group title="隐私设置">
        <van-cell
          title="个人资料可见性"
          value="公开"
          is-link
          @click="goToPrivacySettings"
        >
          <template #icon>
            <van-icon name="shield-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
        
        <van-cell
          title="动态可见性"
          value="公开"
          is-link
          @click="goToPostPrivacy"
        >
          <template #icon>
            <van-icon name="chat-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
        
        <van-cell
          title="位置信息"
          value="开启"
          is-link
          @click="goToLocationSettings"
        >
          <template #icon>
            <van-icon name="location-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 通知设置 -->
      <van-cell-group title="通知设置">
        <van-cell
          title="消息通知"
          is-link
          @click="goToNotificationSettings"
        >
          <template #icon>
            <van-icon name="bell-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
        
        <van-cell
          title="系统通知"
          is-link
          @click="goToSystemNotifications"
        >
          <template #icon>
            <van-icon name="setting-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 其他设置 -->
      <van-cell-group title="其他">
        <van-cell
          title="清除缓存"
          is-link
          @click="clearCache"
        >
          <template #icon>
            <van-icon name="delete-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
        
        <van-cell
          title="关于我们"
          is-link
          @click="goToAbout"
        >
          <template #icon>
            <van-icon name="info-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
        
        <van-cell
          title="意见反馈"
          is-link
          @click="goToFeedback"
        >
          <template #icon>
            <van-icon name="comment-o" size="20" class="setting-icon" />
          </template>
        </van-cell>
      </van-cell-group>

      <!-- 退出登录 -->
      <van-cell-group>
        <van-cell
          title="退出登录"
          @click="handleLogout"
          class="logout-cell"
        >
          <template #icon>
            <van-icon name="revoke" size="20" class="setting-icon logout-icon" />
          </template>
        </van-cell>
      </van-cell-group>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 方法
const goBack = () => {
  router.back()
}

const goToChangePassword = () => {
  router.push('/profile/change-password')
}

const goToBindPhone = () => {
  showToast('手机号绑定功能开发中')
}

const goToBindEmail = () => {
  showToast('邮箱绑定功能开发中')
}

const goToPrivacySettings = () => {
  router.push('/profile/privacy')
}

const goToPostPrivacy = () => {
  showToast('动态隐私设置功能开发中')
}

const goToLocationSettings = () => {
  showToast('位置设置功能开发中')
}

const goToNotificationSettings = () => {
  showToast('通知设置功能开发中')
}

const goToSystemNotifications = () => {
  showToast('系统通知设置功能开发中')
}

const clearCache = () => {
  showConfirmDialog({
    title: '确认清除',
    message: '确定要清除应用缓存吗？'
  }).then(() => {
    // 清除缓存逻辑
    localStorage.removeItem('cache')
    showToast('缓存清除成功')
  }).catch(() => {
    // 用户取消
  })
}

const goToAbout = () => {
  router.push('/about')
}

const goToFeedback = () => {
  showToast('意见反馈功能开发中')
}

const handleLogout = () => {
  showConfirmDialog({
    title: '确认退出',
    message: '确定要退出登录吗？'
  }).then(() => {
    userStore.logout()
    showToast('已退出登录')
    router.push('/login')
  }).catch(() => {
    // 用户取消
  })
}
</script>

<style scoped>
.profile-settings {
  padding: 0rem;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.settings-list {
  padding: 0rem;
  margin-top: 46px;
}

.setting-icon {
  margin-right: 12px;
  color: #666;
}

.logout-cell {
  color: #ff4444;
}

.logout-icon {
  color: #ff4444;
}

:deep(.van-cell-group__title) {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding: 15px 16px 10px;
}

:deep(.van-cell) {
  font-size: 14px;
}

:deep(.van-cell__title) {
  font-size: 14px;
  color: #333;
}

:deep(.van-cell__value) {
  font-size: 14px;
  color: #666;
}
</style>
