<template>
  <div class="admin-expert-review">
    <van-nav-bar title="达人申请审核" left-arrow @click-left="goBack" />
    
    <!-- 筛选标签 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="待审核" name="pending" />
      <van-tab title="已通过" name="approved" />
      <van-tab title="已拒绝" name="rejected" />
    </van-tabs>
    
    <!-- 申请列表 -->
    <div class="application-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div v-for="application in applications" :key="application.id" class="application-item">
            <van-card
              :title="application.nickname || application.username"
              :thumb="'/default-avatar.png'"
            >
              <template #desc>
                <div class="application-content">
                  <div class="reason-section">
                    <h4>申请理由：</h4>
                    <p>{{ application.reason }}</p>
                  </div>
                  
                  <div class="bio-section">
                    <h4>个人简介：</h4>
                    <p>{{ application.bio }}</p>
                  </div>
                  
                  <div v-if="application.expertise" class="expertise-section">
                    <h4>专业领域：</h4>
                    <p>{{ application.expertise }}</p>
                  </div>
                  
                  <div v-if="application.contactInfo" class="contact-section">
                    <h4>联系方式：</h4>
                    <p>{{ application.contactInfo }}</p>
                  </div>
                </div>
                
                <div class="application-meta">
                  <span class="application-time">申请时间：{{ formatTime(application.createdAt) }}</span>
                  <span v-if="application.reviewedAt" class="review-time">审核时间：{{ formatTime(application.reviewedAt) }}</span>
                </div>
                
                <div v-if="application.adminNote" class="admin-note">
                  <h4>管理员备注：</h4>
                  <p>{{ application.adminNote }}</p>
                </div>
                
                <div v-if="application.rejectReason" class="reject-reason">
                  <h4>拒绝原因：</h4>
                  <p>{{ application.rejectReason }}</p>
                </div>
              </template>
              
              <template #footer>
                <div class="application-actions">
                  <van-button 
                    v-if="application.status === 'PENDING'"
                    type="primary" 
                    size="small" 
                    @click="approveApplication(application)"
                  >
                    通过
                  </van-button>
                  <van-button 
                    v-if="application.status === 'PENDING'"
                    type="danger" 
                    size="small" 
                    @click="rejectApplication(application)"
                  >
                    拒绝
                  </van-button>
                  <van-tag 
                    :type="getStatusType(application.status)"
                    size="medium"
                  >
                    {{ getStatusText(application.status) }}
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
      title="拒绝申请"
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
  name: 'AdminExpertReview',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const activeTab = ref('pending')
    const applications = ref([])
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const page = ref(0)
    const size = ref(10)
    
    const showApprovalDialog = ref(false)
    const showRejectionDialog = ref(false)
    const approvalNote = ref('')
    const rejectionReason = ref('')
    const currentApplication = ref(null)
    
    const goBack = () => {
      router.go(-1)
    }
    
    const onTabChange = (name) => {
      activeTab.value = name
      resetList()
      loadApplications()
    }
    
    const resetList = () => {
      applications.value = []
      page.value = 0
      finished.value = false
    }
    
    const loadApplications = async () => {
      if (loading.value) return
      
      loading.value = true
      
      try {
        const response = await request.get('/admin/expert-applications', {
          params: {
            page: page.value,
            size: size.value
          }
        })
        
        const newApplications = response.content || response.data || []
          
          // 根据状态筛选
          let filteredApplications = newApplications
          if (activeTab.value === 'pending') {
            filteredApplications = newApplications.filter(app => app.status === 'PENDING')
          } else if (activeTab.value === 'approved') {
            filteredApplications = newApplications.filter(app => app.status === 'APPROVED')
          } else if (activeTab.value === 'rejected') {
            filteredApplications = newApplications.filter(app => app.status === 'REJECTED')
          }
          
          if (page.value === 0) {
            applications.value = filteredApplications
          } else {
            applications.value.push(...filteredApplications)
          }
          
        finished.value = newApplications.length < size.value
        page.value++
      } catch (error) {
        console.error('加载申请失败:', error)
        showToast('加载申请失败')
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }
    
    const onLoad = () => {
      loadApplications()
    }
    
    const onRefresh = () => {
      resetList()
      loadApplications()
    }
    
    const approveApplication = (application) => {
      currentApplication.value = application
      approvalNote.value = ''
      showApprovalDialog.value = true
    }
    
    const rejectApplication = (application) => {
      currentApplication.value = application
      rejectionReason.value = ''
      showRejectionDialog.value = true
    }
    
    const confirmApproval = async () => {
      try {
        await request.post(`/admin/expert-applications/${currentApplication.value.id}/approve`, {
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
        await request.post(`/admin/expert-applications/${currentApplication.value.id}/reject`, {
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
        loadApplications()
      }
    })
    
    return {
      activeTab,
      applications,
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
      approveApplication,
      rejectApplication,
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
.admin-expert-review {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.application-list {
  padding: 16px;
}

.application-item {
  margin-bottom: 16px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.application-content {
  margin: 8px 0;
}

.reason-section,
.bio-section,
.expertise-section,
.contact-section,
.admin-note,
.reject-reason {
  margin-bottom: 12px;
}

.reason-section h4,
.bio-section h4,
.expertise-section h4,
.contact-section h4,
.admin-note h4,
.reject-reason h4 {
  margin: 0 0 4px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.reason-section p,
.bio-section p,
.expertise-section p,
.contact-section p,
.admin-note p,
.reject-reason p {
  margin: 0;
  line-height: 1.5;
  word-break: break-word;
  color: #666;
}

.application-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin: 8px 0;
  font-size: 12px;
  color: #999;
}

.application-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
