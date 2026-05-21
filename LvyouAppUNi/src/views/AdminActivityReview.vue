<template>
  <div class="admin-activity-review">
    <van-nav-bar title="活动审核" left-arrow @click-left="goBack" />
    
    <!-- 筛选标签 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="待审核" name="pending" />
      <van-tab title="已通过" name="approved" />
      <van-tab title="已拒绝" name="rejected" />
    </van-tabs>
    
    <!-- 活动列表 -->
    <div class="activity-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div v-for="activity in activities" :key="activity.id" class="activity-item">
            <van-card
              :title="activity.title"
              :thumb="activity.coverImage || '/default-activity.png'"
            >
              <template #desc>
                <div class="activity-content">{{ activity.description }}</div>
                <div class="activity-meta">
                  <div class="activity-info">
                    <span class="destination">📍 {{ activity.destination }}</span>
                    <span class="time">⏰ {{ formatTime(activity.startTime) }}</span>
                  </div>
                  <div class="activity-stats">
                    <span>👥 {{ activity.currentParticipants }}/{{ activity.maxParticipants }}</span>
                    <span>💰 {{ activity.cost ? '¥' + activity.cost : '免费' }}</span>
                    <span>👁️ {{ activity.viewCount || 0 }}</span>
                  </div>
                </div>
                <div class="organizer-info">
                  组织者：{{ activity.organizer?.nickname || activity.organizer?.username }}
                </div>
              </template>
              
              <template #footer>
                <div class="activity-actions">
                  <van-button 
                    v-if="activity.status === 'PENDING'"
                    type="primary" 
                    size="small" 
                    @click="approveActivity(activity)"
                  >
                    通过
                  </van-button>
                  <van-button 
                    v-if="activity.status === 'PENDING'"
                    type="danger" 
                    size="small" 
                    @click="rejectActivity(activity)"
                  >
                    拒绝
                  </van-button>
                  <van-tag 
                    :type="getStatusType(activity.status)"
                    size="medium"
                  >
                    {{ getStatusText(activity.status) }}
                  </van-tag>
                </div>
              </template>
            </van-card>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
    
    <!-- 审核对话框 -->
    <van-dialog
      v-model:show="showApprovalDialog"
      title="审核通过"
      show-cancel-button
      @confirm="confirmApproval"
    >
      <van-field
        v-model="approvalNote"
        label="审核备注"
        placeholder="请输入审核备注（可选）"
        type="textarea"
        rows="3"
      />
    </van-dialog>
    
    <!-- 拒绝对话框 -->
    <van-dialog
      v-model:show="showRejectionDialog"
      title="拒绝活动"
      show-cancel-button
      @confirm="confirmRejection"
    >
      <van-field
        v-model="rejectionReason"
        label="拒绝原因"
        placeholder="请输入拒绝原因"
        type="textarea"
        rows="3"
        required
      />
    </van-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import request from '@/api/request'

export default {
  name: 'AdminActivityReview',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const activeTab = ref('pending')
    const activities = ref([])
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const page = ref(0)
    const size = ref(10)
    
    const showApprovalDialog = ref(false)
    const showRejectionDialog = ref(false)
    const approvalNote = ref('')
    const rejectionReason = ref('')
    const currentActivity = ref(null)
    
    const goBack = () => {
      router.go(-1)
    }
    
    const onTabChange = (name) => {
      activeTab.value = name
      resetList()
      loadActivities()
    }
    
    const resetList = () => {
      activities.value = []
      page.value = 0
      finished.value = false
    }
    
    const loadActivities = async () => {
      if (loading.value) return
      
      loading.value = true
      
      try {
        let url = '/admin/activities/pending'
        if (activeTab.value === 'approved') {
          url = '/activities?status=APPROVED'
        } else if (activeTab.value === 'rejected') {
          url = '/activities?status=REJECTED'
        }
        
        const response = await request.get(url, {
          params: {
            page: page.value,
            size: size.value
          }
        })
        
        const newActivities = response.content || response.data || []
          
          if (page.value === 0) {
            activities.value = newActivities
          } else {
            activities.value.push(...newActivities)
          }
          
        finished.value = newActivities.length < size.value
        page.value++
      } catch (error) {
        console.error('加载活动失败:', error)
        showToast('加载活动失败')
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }
    
    const onLoad = () => {
      loadActivities()
    }
    
    const onRefresh = () => {
      resetList()
      loadActivities()
    }
    
    const approveActivity = (activity) => {
      currentActivity.value = activity
      approvalNote.value = ''
      showApprovalDialog.value = true
    }
    
    const rejectActivity = (activity) => {
      currentActivity.value = activity
      rejectionReason.value = ''
      showRejectionDialog.value = true
    }
    
    const confirmApproval = async () => {
      try {
        await request.post(`/admin/activities/${currentActivity.value.id}/approve`, {
          note: approvalNote.value
        })
        
        showToast('审核通过')
        onRefresh()
      } catch (error) {
        console.error('审核失败:', error)
        showToast('审核失败')
      } finally {
        showApprovalDialog.value = false
      }
    }
    
    const confirmRejection = async () => {
      if (!rejectionReason.value.trim()) {
        showToast('请输入拒绝原因')
        return
      }
      
      try {
        await request.post(`/admin/activities/${currentActivity.value.id}/reject`, {
          reason: rejectionReason.value
        })
        
        showToast('已拒绝')
        onRefresh()
      } catch (error) {
        console.error('操作失败:', error)
        showToast('操作失败')
      } finally {
        showRejectionDialog.value = false
      }
    }
    
    const getStatusType = (status) => {
      switch (status) {
        case 'APPROVED': return 'success'
        case 'REJECTED': return 'danger'
        case 'PENDING': return 'warning'
        default: return 'default'
      }
    }
    
    const getStatusText = (status) => {
      switch (status) {
        case 'APPROVED': return '已通过'
        case 'REJECTED': return '已拒绝'
        case 'PENDING': return '待审核'
        default: return '未知'
      }
    }
    
    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }
    
    // 检查管理员权限
    const checkAdminPermission = () => {
      if (!userStore.isLoggedIn) {
        showToast('请先登录')
        router.push('/login')
        return false
      }
      
      if (!userStore.isAdmin) {
        showToast('权限不足，仅管理员可访问')
        router.push('/home')
        return false
      }
      
      return true
    }
    
    onMounted(() => {
      if (checkAdminPermission()) {
        loadActivities()
      }
    })
    
    return {
      activeTab,
      activities,
      loading,
      finished,
      refreshing,
      showApprovalDialog,
      showRejectionDialog,
      approvalNote,
      rejectionReason,
      goBack,
      onTabChange,
      onLoad,
      onRefresh,
      approveActivity,
      rejectActivity,
      confirmApproval,
      confirmRejection,
      getStatusType,
      getStatusText,
      formatTime
    }
  }
}
</script>

<style scoped>
.admin-activity-review {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.activity-list {
  padding: 16px;
}

.activity-item {
  margin-bottom: 16px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.activity-content {
  margin: 8px 0;
  line-height: 1.5;
  word-break: break-word;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-meta {
  margin: 8px 0;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.destination {
  color: #1989fa;
}

.activity-stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #666;
}

.organizer-info {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

.activity-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
