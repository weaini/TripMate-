<template>
  <div class="admin-post-review">
    <van-nav-bar title="动态审核" left-arrow @click-left="goBack" />
    
    <!-- 筛选标签 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="待审核" name="pending" />
      <van-tab title="已通过" name="approved" />
      <van-tab title="已拒绝" name="rejected" />
    </van-tabs>
    
    <!-- 动态列表 -->
    <div class="post-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div v-for="post in posts" :key="post.id" class="post-item">
            <van-card
              :title="post.user?.nickname || post.user?.username"
              :thumb="post.user?.avatar || '/default-avatar.png'"
            >
              <template #desc>
                <div class="post-content">{{ post.content }}</div>
                <div class="post-meta">
                  <span class="post-location" v-if="post.location">{{ post.location }}</span>
                  <span class="post-time">{{ formatTime(post.createdAt) }}</span>
                </div>
                <div class="post-stats">
                  <span>👍 {{ post.likeCount || 0 }}</span>
                  <span>💬 {{ post.commentCount || 0 }}</span>
                  <span>👁️ {{ post.viewCount || 0 }}</span>
                </div>
              </template>
              
              <template #footer>
                <div class="post-actions">
                  <van-button 
                    v-if="post.status === 'PENDING'"
                    type="primary" 
                    size="small" 
                    @click="approvePost(post)"
                  >
                    通过
                  </van-button>
                  <van-button 
                    v-if="post.status === 'PENDING'"
                    type="danger" 
                    size="small" 
                    @click="rejectPost(post)"
                  >
                    拒绝
                  </van-button>
                  <van-tag 
                    :type="getStatusType(post.status)"
                    size="medium"
                  >
                    {{ getStatusText(post.status) }}
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
      title="拒绝动态"
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
import { showToast, showConfirmDialog } from 'vant'
import request from '@/api/request'

export default {
  name: 'AdminPostReview',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const activeTab = ref('pending')
    const posts = ref([])
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const page = ref(0)
    const size = ref(10)
    
    const showApprovalDialog = ref(false)
    const showRejectionDialog = ref(false)
    const approvalNote = ref('')
    const rejectionReason = ref('')
    const currentPost = ref(null)
    
    const goBack = () => {
      router.go(-1)
    }
    
    const onTabChange = (name) => {
      activeTab.value = name
      resetList()
      loadPosts()
    }
    
    const resetList = () => {
      posts.value = []
      page.value = 0
      finished.value = false
    }
    
    const loadPosts = async () => {
      if (loading.value) return
      
      loading.value = true
      
      try {
        let url = '/admin/posts/pending'
        const params = { page: page.value, size: size.value }
        if (activeTab.value === 'approved') {
          url = '/admin/posts'
          params.status = 'APPROVED'
        } else if (activeTab.value === 'rejected') {
          url = '/admin/posts'
          params.status = 'REJECTED'
        }
        
        const response = await request.get(url, { params })
        
        const newPosts = response.content || response.data || []
          
          if (page.value === 0) {
            posts.value = newPosts
          } else {
            posts.value.push(...newPosts)
          }
          
        finished.value = newPosts.length < size.value
        page.value++
      } catch (error) {
        console.error('加载动态失败:', error)
        showToast('加载动态失败')
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }
    
    const onLoad = () => {
      loadPosts()
    }
    
    const onRefresh = () => {
      resetList()
      loadPosts()
    }
    
    const approvePost = (post) => {
      currentPost.value = post
      approvalNote.value = ''
      showApprovalDialog.value = true
    }
    
    const rejectPost = (post) => {
      currentPost.value = post
      rejectionReason.value = ''
      showRejectionDialog.value = true
    }
    
    const confirmApproval = async () => {
      try {
        await request.post(`/admin/posts/${currentPost.value.id}/approve`, {
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
        await request.post(`/admin/posts/${currentPost.value.id}/reject`, {
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
        loadPosts()
      }
    })
    
    return {
      activeTab,
      posts,
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
      approvePost,
      rejectPost,
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
.admin-post-review {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.post-list {
  padding: 16px;
}

.post-item {
  margin-bottom: 16px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.post-content {
  margin: 8px 0;
  line-height: 1.5;
  word-break: break-word;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 8px 0;
  font-size: 12px;
  color: #666;
}

.post-location {
  color: #1989fa;
}

.post-stats {
  display: flex;
  gap: 16px;
  margin: 8px 0;
  font-size: 12px;
  color: #666;
}

.post-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
