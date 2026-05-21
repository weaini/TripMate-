<template>
  <div class="admin-user-management">
    <van-nav-bar title="用户管理" left-arrow @click-left="goBack" />
    
    <!-- 搜索栏 -->
    <div class="search-section">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索用户名或昵称"
        @search="onSearch"
        @clear="onClear"
      />
    </div>
    
    <!-- 筛选标签 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="全部用户" name="all" />
      <van-tab title="游客" name="TOURIST" />
      <van-tab title="旅游达人" name="EXPERT" />
      <van-tab title="管理员" name="ADMIN" />
    </van-tabs>
    
    <!-- 用户列表 -->
    <div class="user-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <div v-for="user in users" :key="user.id" class="user-item">
            <van-card
              :title="user.nickname || user.username"
              :thumb="user.avatar || '/default-avatar.png'"
            >
              <template #desc>
                <div class="user-info">
                  <div class="user-basic">
                    <span class="username">@{{ user.username }}</span>
                    <van-tag 
                      :type="getRoleType(user.role)"
                      size="medium"
                    >
                      {{ getRoleText(user.role) }}
                    </van-tag>
                  </div>
                  <div class="user-stats">
                    <span>粉丝: {{ user.followersCount || 0 }}</span>
                    <span>关注: {{ user.followingCount || 0 }}</span>
                    <span>积分: {{ user.points || 0 }}</span>
                  </div>
                  <div class="user-meta">
                    <span>注册时间: {{ formatTime(user.createdAt) }}</span>
                    <span v-if="user.lastLoginAt">最后登录: {{ formatTime(user.lastLoginAt) }}</span>
                  </div>
                </div>
              </template>
              
              <template #footer>
                <div class="user-actions">
                  <van-button 
                    v-if="user.role !== 'ADMIN'"
                    type="primary" 
                    size="small" 
                    @click="viewUserDetail(user)"
                  >
                    查看详情
                  </van-button>
                  <van-button 
                    v-if="user.role !== 'ADMIN'"
                    type="warning" 
                    size="small" 
                    @click="toggleUserStatus(user)"
                  >
                    {{ user.status === 'ACTIVE' ? '禁用' : '启用' }}
                  </van-button>
                  <van-button 
                    v-if="user.role === 'TOURIST'"
                    type="success" 
                    size="small" 
                    @click="promoteToExpert(user)"
                  >
                    设为达人
                  </van-button>
                </div>
              </template>
            </van-card>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
    
    <!-- 用户详情对话框 -->
    <van-dialog
      v-model:show="showUserDetail"
      title="用户详情"
      :show-cancel-button="false"
      confirm-button-text="关闭"
    >
      <div v-if="selectedUser" class="user-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <p><strong>用户名:</strong> {{ selectedUser.username }}</p>
          <p><strong>昵称:</strong> {{ selectedUser.nickname || '未设置' }}</p>
          <p><strong>邮箱:</strong> {{ selectedUser.email || '未设置' }}</p>
          <p><strong>手机:</strong> {{ selectedUser.phone || '未设置' }}</p>
          <p><strong>角色:</strong> {{ getRoleText(selectedUser.role) }}</p>
          <p><strong>状态:</strong> {{ getStatusText(selectedUser.status) }}</p>
        </div>
        
        <div class="detail-section">
          <h4>统计信息</h4>
          <p><strong>粉丝数:</strong> {{ selectedUser.followersCount || 0 }}</p>
          <p><strong>关注数:</strong> {{ selectedUser.followingCount || 0 }}</p>
          <p><strong>积分:</strong> {{ selectedUser.points || 0 }}</p>
          <p><strong>注册时间:</strong> {{ formatTime(selectedUser.createdAt) }}</p>
          <p v-if="selectedUser.lastLoginAt"><strong>最后登录:</strong> {{ formatTime(selectedUser.lastLoginAt) }}</p>
        </div>
        
        <div v-if="selectedUser.bio" class="detail-section">
          <h4>个人简介</h4>
          <p>{{ selectedUser.bio }}</p>
        </div>
      </div>
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
  name: 'AdminUserManagement',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const activeTab = ref('all')
    const users = ref([])
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const page = ref(0)
    const size = ref(10)
    const searchKeyword = ref('')
    
    const showUserDetail = ref(false)
    const selectedUser = ref(null)
    
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
    
    const goBack = () => {
      router.go(-1)
    }
    
    const onTabChange = (name) => {
      activeTab.value = name
      resetList()
      loadUsers()
    }
    
    const resetList = () => {
      users.value = []
      page.value = 0
      finished.value = false
    }
    
    const loadUsers = async () => {
      if (loading.value) return
      
      loading.value = true
      
      try {
        const params = {
          page: page.value,
          size: size.value
        }
        
        // 添加角色筛选
        if (activeTab.value !== 'all') {
          params.role = activeTab.value
        }
        
        // 添加搜索关键词
        if (searchKeyword.value) {
          params.keyword = searchKeyword.value
        }
        
        const response = await request.get('/admin/users', {
          params: params
        })
        
        const newUsers = response.content || response.data || []
          
          if (page.value === 0) {
            users.value = newUsers
          } else {
            users.value.push(...newUsers)
          }
          
        finished.value = newUsers.length < size.value
        page.value++
      } catch (error) {
        console.error('加载用户列表失败:', error)
        showToast('加载用户列表失败')
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }
    
    const onLoad = () => {
      loadUsers()
    }
    
    const onRefresh = () => {
      resetList()
      loadUsers()
    }
    
    const onSearch = () => {
      resetList()
      loadUsers()
    }
    
    const onClear = () => {
      searchKeyword.value = ''
      resetList()
      loadUsers()
    }
    
    const viewUserDetail = (user) => {
      selectedUser.value = user
      showUserDetail.value = true
    }
    
    const toggleUserStatus = async (user) => {
      const action = user.status === 'ACTIVE' ? '禁用' : '启用'
      
      try {
        await showConfirmDialog({
          title: `确认${action}`,
          message: `确定要${action}用户 ${user.nickname || user.username} 吗？`
        })
        
        const newStatus = user.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
        
        await request.put(`/admin/users/${user.id}/status`, { status: newStatus })
        
        showToast(`${action}成功`)
        onRefresh()
      } catch (error) {
        console.error(`${action}失败:`, error)
        showToast(`${action}失败`)
      }
    }
    
    const promoteToExpert = async (user) => {
      try {
        await showConfirmDialog({
          title: '确认设为达人',
          message: `确定要将用户 ${user.nickname || user.username} 设为旅游达人吗？`
        })
        
        await request.put(`/admin/users/${user.id}/role`, { role: 'EXPERT' })
        
        showToast('设为达人成功')
        onRefresh()
      } catch (error) {
        console.error('设为达人失败:', error)
        showToast('设为达人失败')
      }
    }
    
    const getRoleType = (role) => {
      switch (role) {
        case 'ADMIN': return 'danger'
        case 'EXPERT': return 'success'
        case 'TOURIST': return 'primary'
        default: return 'default'
      }
    }
    
    const getRoleText = (role) => {
      switch (role) {
        case 'ADMIN': return '管理员'
        case 'EXPERT': return '旅游达人'
        case 'TOURIST': return '游客'
        default: return '未知'
      }
    }
    
    const getStatusText = (status) => {
      switch (status) {
        case 'ACTIVE': return '活跃'
        case 'INACTIVE': return '非活跃'
        case 'BANNED': return '封禁'
        default: return '未知'
      }
    }
    
    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }
    
    onMounted(() => {
      if (checkAdminPermission()) {
        loadUsers()
      }
    })
    
    return {
      activeTab,
      users,
      loading,
      finished,
      refreshing,
      searchKeyword,
      showUserDetail,
      selectedUser,
      goBack,
      onTabChange,
      onLoad,
      onRefresh,
      onSearch,
      onClear,
      viewUserDetail,
      toggleUserStatus,
      promoteToExpert,
      getRoleType,
      getRoleText,
      getStatusText,
      formatTime
    }
  }
}
</script>

<style scoped>
.admin-user-management {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.search-section {
  padding: 16px;
  background: white;
  margin-bottom: 16px;
}

.user-list {
  padding: 16px;
}

.user-item {
  margin-bottom: 16px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.user-info {
  margin: 8px 0;
}

.user-basic {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.username {
  color: #666;
  font-size: 12px;
}

.user-stats {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  font-size: 12px;
  color: #666;
}

.user-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.user-actions {
  display: flex;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
  margin-top: 8px;
}

.user-detail {
  padding: 16px;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.detail-section p {
  margin: 4px 0;
  font-size: 14px;
  line-height: 1.5;
}
</style>
