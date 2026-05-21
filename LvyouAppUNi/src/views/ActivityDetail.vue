<template>
  <div class="activity-detail page-nav-only">
    <van-nav-bar title="活动详情" fixed left-arrow @click-left="goBack" />
    <div class="nav-spacer"></div>

    <div v-if="loading" class="loading">
      <van-loading type="spinner" />
    </div>
    
    <div v-else-if="activity" class="detail-scroll">
      <!-- 活动封面 -->
      <div class="cover-section">
        <van-image
          :src="activity.coverImage || '/default-activity.png'"
          class="cover-image"
          fit="cover"
        />
        <div class="cover-overlay">
          <div class="activity-status" :class="getStatusClass(activity.status)">
            {{ getStatusText(activity.status) }}
          </div>
        </div>
      </div>
      
      <!-- 活动基本信息 -->
      <div class="info-section">
        <h1 class="title">{{ activity.title }}</h1>
        <div class="meta-info">
          <div class="meta-item">
            <van-icon name="location-o" />
            <span>{{ activity.destination }}</span>
          </div>
          <div class="meta-item">
            <van-icon name="clock-o" />
            <span>开始 {{ formatDate(activity.startTime) }}</span>
          </div>
          <div v-if="activity.registrationDeadline" class="meta-item hint">
            <van-icon name="clock" />
            <span>报名截止 {{ formatDate(activity.registrationDeadline) }}</span>
          </div>
          <div class="meta-item">
            <van-icon name="user-o" />
            <span>{{ activity.currentParticipants }}/{{ activity.maxParticipants }} 人</span>
          </div>
          <div class="meta-item">
            <van-icon name="gold-coin-o" />
            <span>¥{{ activity.cost }}</span>
          </div>
        </div>
      </div>
      
      <!-- 组织者信息 -->
      <div class="organizer-section">
        <div class="organizer-info">
          <van-image
            :src="activity.organizer?.avatar || '/default-avatar.png'"
            class="organizer-avatar"
            round
            fit="cover"
          />
          <div class="organizer-details">
            <h3>{{ activity.organizer?.nickname }}</h3>
            <p>活动组织者</p>
          </div>
        </div>
      </div>
      
      <!-- 活动描述 -->
      <div class="description-section">
        <h3>活动详情</h3>
        <p class="description">{{ activity.description }}</p>
      </div>
      
      <!-- 活动要求 -->
      <div v-if="activity.requirements" class="requirements-section">
        <h3>参与要求</h3>
        <p class="requirements">{{ activity.requirements }}</p>
      </div>
      
      <!-- 联系方式 -->
      <div v-if="activity.contactInfo" class="contact-section">
        <h3>联系方式</h3>
        <p class="contact">{{ activity.contactInfo }}</p>
      </div>
    </div>

    <!-- 底部固定操作区（与正文分离，避免遮挡） -->
    <footer v-if="activity" class="action-footer safe-bottom">
      <div class="action-inner">
        <div class="button-group">
          <template v-if="isOrganizer">
            <van-button type="primary" block class="footer-btn" @click="editActivity">编辑活动</van-button>
            <van-button type="default" block class="footer-btn" @click="goToParticipants">管理参与者</van-button>
          </template>
          <template v-else-if="!isJoined">
            <van-button
              type="primary"
              block
              class="footer-btn"
              :disabled="!canSignUp"
              @click="showJoinDialog"
            >
              {{ signUpButtonText }}
            </van-button>
            <p v-if="!canSignUp && signUpDisabledReason" class="sign-up-tip">{{ signUpDisabledReason }}</p>
          </template>
          <van-button v-else type="warning" block class="footer-btn" @click="cancelJoin">取消报名</van-button>
        </div>
        <div class="chat-section">
          <van-button
            v-if="canAccessChat"
            type="success"
            block
            class="footer-btn chat-btn"
            icon="chat-o"
            @click="goToChat"
          >进入聊天室</van-button>
          <van-button
            v-else
            type="default"
            block
            disabled
            class="footer-btn chat-btn"
            icon="chat-o"
          >{{ chatButtonText }}</van-button>
        </div>
      </div>
    </footer>
    
    <!-- 报名对话框 -->
    <van-dialog
      v-model:show="showJoinDialogRef"
      title="报名参加活动"
      show-cancel-button
      @confirm="joinActivity"
    >
      <van-field
        v-model="joinNote"
        type="textarea"
        placeholder="请填写您的申请说明"
        rows="3"
        maxlength="200"
        show-word-limit
      />
    </van-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getActivity, joinActivity as joinActivityApi, cancelJoinActivity, checkParticipation } from '@/api/activities'
import { useUserStore } from '@/stores/user'

export default {
  name: 'ActivityDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()
    
    const activity = ref(null)
    const loading = ref(true)
    const showJoinDialogRef = ref(false)
    const joinNote = ref('')
    const canAccessChat = ref(false)
    const participationStatus = ref(null)
    
    // 获取活动详情
    const loadActivity = async () => {
      try {
        loading.value = true
        const response = await getActivity(route.params.id)
        activity.value = response
        
        // 检查聊天室访问权限
        await checkChatPermission()
      } catch (error) {
        console.error('获取活动详情失败:', error)
        showToast('获取活动详情失败')
      } finally {
        loading.value = false
      }
    }
    
    // 判断是否是组织者
    const isOrganizer = computed(() => {
      const result = activity.value && userStore.user && 
             activity.value.organizer?.id === userStore.user.id
      
      // 调试信息
      console.log('权限检查:', {
        hasActivity: !!activity.value,
        hasUser: !!userStore.user,
        activityOrganizerId: activity.value?.organizer?.id,
        currentUserId: userStore.user?.id,
        isOrganizer: result
      })
      
      return result
    })
    
    // 判断是否已报名
    const isJoined = computed(() => {
      return activity.value?.isJoined || false
    })

    const parseTime = (t) => (t ? new Date(t).getTime() : null)
    /** 与后端一致：未开始且未满、未过有效报名截止则可点报名 */
    const canSignUp = computed(() => {
      const a = activity.value
      if (!a || a.status !== 'APPROVED') return false
      const now = Date.now()
      const start = parseTime(a.startTime)
      if (start != null && now >= start) return false
      if ((a.currentParticipants || 0) >= (a.maxParticipants || 0)) return false
      const regEnd = parseTime(a.registrationDeadline)
      const last = regEnd != null && start != null && start > regEnd ? start : regEnd
      if (last != null && now > last) return false
      return true
    })
    const signUpDisabledReason = computed(() => {
      const a = activity.value
      if (!a) return ''
      if (a.status !== 'APPROVED') return '活动未通过审核，暂不可报名'
      const now = Date.now()
      if (parseTime(a.startTime) != null && now >= parseTime(a.startTime)) return '活动已开始，无法报名'
      if ((a.currentParticipants || 0) >= (a.maxParticipants || 0)) return '名额已满'
      const regEnd = parseTime(a.registrationDeadline)
      const start = parseTime(a.startTime)
      const last = regEnd != null && start != null && start > regEnd ? start : regEnd
      if (last != null && now > last) return '已超过报名时间'
      return ''
    })
    const signUpButtonText = computed(() => (canSignUp.value ? '报名参加' : '不可报名'))
    
    // 检查聊天室访问权限
    const checkChatPermission = async () => {
      try {
        const response = await checkParticipation(route.params.id)
        canAccessChat.value = response.canAccessChat
        participationStatus.value = response.participationStatus
      } catch (error) {
        console.error('检查权限失败:', error)
        canAccessChat.value = false
      }
    }
    
    // 聊天按钮文本
    const chatButtonText = computed(() => {
      if (isOrganizer.value) {
        return '进入聊天室'
      }
      
      if (!participationStatus.value) {
        return '请先报名参加活动'
      }
      
      switch (participationStatus.value) {
        case 'PENDING':
          return '等待审核通过后可进入聊天室'
        case 'REJECTED':
          return '报名被拒绝，无法进入聊天室'
        case 'CANCELLED':
          return '已取消报名，无法进入聊天室'
        default:
          return '请先报名参加活动'
      }
    })
    
    // 显示报名对话框
    const showJoinDialog = () => {
      if (!userStore.user) {
        showToast('请先登录')
        router.push('/login')
        return
      }
      if (!canSignUp.value) {
        showToast(signUpDisabledReason.value || '当前不可报名')
        return
      }
      if (isJoined.value) {
        showToast('您已经报名此活动')
        return
      }
      showJoinDialogRef.value = true
    }
    
    // 报名活动
    const joinActivity = async () => {
      if (!joinNote.value.trim()) {
        showToast('请填写申请说明')
        return
      }
      
      try {
        await joinActivityApi(route.params.id, { applicationNote: joinNote.value })
        showToast('报名申请已提交，等待组织者审核')
        showJoinDialogRef.value = false
        joinNote.value = ''
        // 重新加载活动详情和权限
        await loadActivity()
      } catch (error) {
        console.error('报名失败:', error)
        const msg = error.response?.data?.message || error.response?.data
        showToast(typeof msg === 'string' ? msg : '报名失败，请稍后重试')
      }
    }
    
    // 取消报名
    const cancelJoin = async () => {
      try {
        await showConfirmDialog({
          title: '确认取消',
          message: '确定要取消报名吗？'
        })
        
        await cancelJoinActivity(route.params.id)
        showToast('已取消报名')
        // 重新加载活动详情和权限
        await loadActivity()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消报名失败:', error)
          showToast('取消报名失败')
        }
      }
    }
    
    // 前往参与者管理
    const goToParticipants = () => {
      router.push(`/activities/${route.params.id}/participants`)
    }
    
    // 编辑活动
    const editActivity = () => {
      console.log('点击编辑活动，活动ID:', route.params.id)
      console.log('跳转路径:', `/activities/${route.params.id}/edit`)
      console.log('当前路由:', route.path)
      console.log('路由对象:', router)
      
      // 先显示一个提示
      showToast('正在跳转到编辑页面...')
      
      try {
        // 尝试使用路径字符串跳转
        const targetPath = `/activities/${route.params.id}/edit`
        console.log('准备跳转到:', targetPath)
        
        // 使用 window.location 作为备用方案
        setTimeout(() => {
          console.log('使用 router.push 跳转')
          router.push(targetPath)
        }, 100)
        
        // 备用方案：直接使用 window.location
        setTimeout(() => {
          console.log('使用 window.location 跳转')
          window.location.href = targetPath
        }, 2000)
        
      } catch (error) {
        console.error('路由跳转失败:', error)
        showToast('页面跳转失败')
      }
    }
    
    // 前往聊天室
    const goToChat = () => {
      router.push(`/activities/${route.params.id}/chat`)
    }
    
    // 格式化日期
    const formatDate = (dateString) => {
      if (!dateString) return ''
      return new Date(dateString).toLocaleString()
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'PENDING': '待审核',
        'APPROVED': '已通过',
        'REJECTED': '已拒绝',
        'CANCELLED': '已取消'
      }
      return statusMap[status] || status
    }
    
    // 获取状态样式类
    const getStatusClass = (status) => {
      return `status-${status.toLowerCase()}`
    }
    
    // 返回
    const goBack = () => {
      router.back()
    }
    
    onMounted(async () => {
      // 确保用户状态已初始化
      if (userStore.token && !userStore.user) {
        try {
          await userStore.fetchUserInfo()
        } catch (error) {
          console.error('获取用户信息失败:', error)
        }
      }
      loadActivity()
    })
    
    return {
      activity,
      loading,
      showJoinDialogRef,
      joinNote,
      isOrganizer,
      isJoined,
      canSignUp,
      signUpDisabledReason,
      signUpButtonText,
      canAccessChat,
      chatButtonText,
      showJoinDialog,
      joinActivity,
      cancelJoin,
      goToParticipants,
      editActivity,
      goToChat,
      formatDate,
      getStatusText,
      getStatusClass,
      goBack
    }
  }
}
</script>

<style scoped>
.activity-detail {
  background: var(--page-bg);
  min-height: 100vh;
  /* 为固定底栏预留空间，避免正文最后一屏被挡 */
  --footer-safe: env(safe-area-inset-bottom, 0px);
  --footer-estimate: 228px;
  padding-bottom: calc(var(--footer-estimate) + var(--footer-safe));
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 可滚动正文：底栏之上全部区域 */
.detail-scroll {
  padding-bottom: 24px;
}

.cover-section {
  position: relative;
  height: 200px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-overlay {
  position: absolute;
  top: 16px;
  right: 16px;
}

.activity-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: bold;
  color: white;
}

.status-pending {
  background: #faad14;
}

.status-approved {
  background: #52c41a;
}

.status-rejected {
  background: #ff4d4f;
}

.status-cancelled {
  background: #8c8c8c;
}

.info-section {
  background: white;
  padding: 20px;
  margin-bottom: 8px;
}

.title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: bold;
  color: #333;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
  font-size: 14px;
}

.organizer-section {
  background: white;
  padding: 20px;
  margin-bottom: 8px;
}

.organizer-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.organizer-avatar {
  width: 48px;
  height: 48px;
}

.organizer-details h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: bold;
}

.organizer-details p {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.description-section,
.requirements-section,
.contact-section {
  background: white;
  padding: 20px;
  margin-bottom: 8px;
}

.description-section h3,
.requirements-section h3,
.contact-section h3 {
  margin: 0 0 12px 0;
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.description,
.requirements,
.contact {
  margin: 0;
  line-height: 1.6;
  color: #666;
}

.action-footer {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 100;
  background: #fff;
  border-top: 1px solid #ebedf0;
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.06);
}
.safe-bottom {
  padding-bottom: env(safe-area-inset-bottom, 0px);
}
.action-inner {
  padding: 12px 16px 12px;
  max-width: 100%;
  box-sizing: border-box;
}
.footer-btn {
  margin-top: 0;
}
.footer-btn + .footer-btn,
.button-group .footer-btn:not(:first-child) {
  margin-top: 10px;
}
.sign-up-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #ee0a24;
  text-align: center;
  line-height: 1.4;
}
.meta-item.hint {
  opacity: 0.9;
}
.button-group {
  margin-bottom: 10px;
}
.chat-section {
  padding-top: 4px;
  border-top: 1px solid #f0f0f0;
}
.chat-btn {
  margin-top: 0;
}
</style>
