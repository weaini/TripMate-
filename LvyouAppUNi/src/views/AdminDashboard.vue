<template>
  <div class="admin-dashboard">
    <van-nav-bar title="管理后台" left-arrow @click-left="goBack" />
    
    <!-- 统计概览 -->
    <div class="stats-section">
      <h3>数据概览</h3>
      <div class="stats-grid">
        <div class="stat-item">
          <div class="stat-number">{{ statistics.totalUsers || 0 }}</div>
          <div class="stat-label">总用户数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ statistics.pendingPosts || 0 }}</div>
          <div class="stat-label">待审核动态</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ statistics.pendingActivities || 0 }}</div>
          <div class="stat-label">待审核活动</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ statistics.pendingExpertApplications || 0 }}</div>
          <div class="stat-label">待审核达人申请</div>
        </div>
      </div>
    </div>

    <!-- 功能菜单 -->
    <div class="menu-section">
      <van-cell-group>
        <van-cell 
          title="动态审核" 
          icon="photo-o" 
          is-link 
          @click="goToPostReview"
          :badge="statistics.pendingPosts"
        />
        <van-cell 
          title="活动审核" 
          icon="calendar-o" 
          is-link 
          @click="goToActivityReview"
          :badge="statistics.pendingActivities"
        />
        <van-cell 
          title="达人申请审核" 
          icon="user-o" 
          is-link 
          @click="goToExpertReview"
          :badge="statistics.pendingExpertApplications"
        />
        <van-cell 
          title="用户管理" 
          icon="friends-o" 
          is-link 
          @click="goToUserManagement"
        />
      </van-cell-group>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import request from '@/api/request'

export default {
  name: 'AdminDashboard',
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    const statistics = ref({})
    
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
    
    const goToPostReview = () => {
      router.push('/admin/posts')
    }
    
    const goToActivityReview = () => {
      router.push('/admin/activities')
    }
    
    const goToExpertReview = () => {
      router.push('/admin/experts')
    }
    
    const goToUserManagement = () => {
      router.push('/admin/users')
    }
    
    const loadStatistics = async () => {
      try {
        const response = await request.get('/admin/statistics')
        statistics.value = response
      } catch (error) {
        console.error('加载统计数据失败:', error)
        showToast('加载统计数据失败')
      }
    }
    
    onMounted(() => {
      if (checkAdminPermission()) {
        loadStatistics()
      }
    })
    
    return {
      statistics,
      goBack,
      goToPostReview,
      goToActivityReview,
      goToExpertReview,
      goToUserManagement
    }
  }
}
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background-color: #f5f5f5;
}

.stats-section {
  padding: 16px;
  background: white;
  margin-bottom: 16px;
}

.stats-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  color: #1989fa;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #666;
}

.menu-section {
  padding: 0 16px;
}
</style>
