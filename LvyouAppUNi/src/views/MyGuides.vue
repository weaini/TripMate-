<template>
  <div class="my-guides">
    <!-- 导航栏 -->
    <van-nav-bar title="我的攻略" fixed>
      <template #left>
        <van-icon name="arrow-left" @click="goBack" />
      </template>
      <template #right>
        <van-icon name="plus" @click="goToCreateGuide" />
      </template>
    </van-nav-bar>

    <!-- 统计信息 -->
    <div class="stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-number">{{ stats.totalGuides || 0 }}</div>
          <div class="stat-label">总攻略</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ stats.publishedGuides || 0 }}</div>
          <div class="stat-label">已发布</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ stats.draftGuides || 0 }}</div>
          <div class="stat-label">草稿</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ stats.totalViews || 0 }}</div>
          <div class="stat-label">总浏览</div>
        </div>
      </div>
    </div>

    <!-- 状态筛选 -->
    <div class="filter-section">
      <van-tabs v-model:active="activeTab" @change="onTabChange">
        <van-tab title="全部" name="all" />
        <van-tab title="已发布" name="PUBLISHED" />
        <van-tab title="草稿" name="DRAFT" />
        <van-tab title="隐藏" name="HIDDEN" />
      </van-tabs>
    </div>

    <!-- 攻略列表 -->
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadGuides"
      >
        <div v-if="guides.length === 0" class="empty-state">
          <van-empty description="暂无攻略" />
          <van-button type="primary" @click="goToCreateGuide">发布第一个攻略</van-button>
        </div>
        <div v-else>
          <div
            v-for="guide in guides"
            :key="guide.id"
            class="guide-item"
            @click="goToGuideDetail(guide.id)"
          >
            <div class="guide-card">
              <!-- 封面图片 -->
              <div class="guide-cover">
                <van-image
                  v-if="guide.coverImage"
                  :src="getImageUrl(guide.coverImage)"
                  class="cover-image"
                  fit="cover"
                  @error="handleImageError"
                />
                <div
                  v-else
                  class="cover-placeholder"
                >
                  <van-icon name="photo" size="40" color="#ccc" />
                  <div class="placeholder-text">暂无封面</div>
                </div>
                <div class="guide-status" :class="getStatusClass(guide.status)">
                  {{ guide.statusDisplayName }}
                </div>
              </div>

              <!-- 攻略信息 -->
              <div class="guide-info">
                <h3 class="guide-title">{{ guide.title }}</h3>
                <p class="guide-summary">{{ guide.summary || guide.content.substring(0, 100) + '...' }}</p>
                
                <!-- 统计信息 -->
                <div class="guide-stats">
                  <div class="stat">
                    <van-icon name="eye-o" />
                    <span>{{ guide.viewCount }}</span>
                  </div>
                  <div class="stat">
                    <van-icon name="good-job-o" />
                    <span>{{ guide.likeCount }}</span>
                  </div>
                  <div class="stat">
                    <van-icon name="chat-o" />
                    <span>{{ guide.commentCount }}</span>
                  </div>
                </div>

                <!-- 发布时间 -->
                <div class="publish-time">{{ formatTime(guide.createdAt) }}</div>

                <!-- 操作按钮 -->
                <div class="guide-actions">
                  <van-button
                    type="primary"
                    size="mini"
                    @click.stop="editGuide(guide.id)"
                  >
                    编辑
                  </van-button>
                  <van-button
                    type="danger"
                    size="mini"
                    @click.stop="deleteGuide(guide)"
                  >
                    删除
                  </van-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getMyGuides, deleteGuide } from '@/api/guides'

export default {
  name: 'MyGuides',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const guides = ref([])
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const activeTab = ref('all')
    const stats = ref({
      totalGuides: 0,
      publishedGuides: 0,
      draftGuides: 0,
      totalViews: 0
    })
    
    // 分页参数
    const pageParams = reactive({
      page: 0,
      size: 10
    })
    
    // 加载我的攻略
    const loadGuides = async () => {
      if (loading.value) return
      
      loading.value = true
      
      try {
        const params = {
          page: pageParams.page,
          size: pageParams.size
        }
        
        if (activeTab.value !== 'all') {
          params.status = activeTab.value
        }
        
        const response = await getMyGuides(params)
        console.log('API响应数据结构:', response)
        
        // Spring Data Page对象的结构
        const newGuides = response.content || []
        
        if (pageParams.page === 0) {
          guides.value = newGuides
        } else {
          guides.value.push(...newGuides)
        }
        
        // Spring Data Page对象的分页信息
        finished.value = response.last || newGuides.length < pageParams.size
        pageParams.page++
        
        // 计算统计信息
        calculateStats()
        
      } catch (error) {
        console.error('加载我的攻略失败:', error)
        showToast('加载攻略失败')
      } finally {
        loading.value = false
      }
    }
    
    // 计算统计信息
    const calculateStats = () => {
      const totalGuides = guides.value.length
      const publishedGuides = guides.value.filter(g => g.status === 'PUBLISHED').length
      const draftGuides = guides.value.filter(g => g.status === 'DRAFT').length
      const totalViews = guides.value.reduce((sum, g) => sum + g.viewCount, 0)
      
      stats.value = {
        totalGuides,
        publishedGuides,
        draftGuides,
        totalViews
      }
    }
    
    // 下拉刷新
    const onRefresh = async () => {
      refreshing.value = true
      pageParams.page = 0
      finished.value = false
      await loadGuides()
      refreshing.value = false
    }
    
    // 切换状态筛选
    const onTabChange = () => {
      pageParams.page = 0
      finished.value = false
      loadGuides()
    }
    
    // 跳转到攻略详情
    const goToGuideDetail = (id) => {
      router.push(`/guides/${id}`)
    }
    
    // 跳转到创建攻略
    const goToCreateGuide = () => {
      router.push('/guides/create')
    }
    
    // 编辑攻略
    const editGuide = (id) => {
      router.push(`/guides/${id}/edit`)
    }
    
    // 删除攻略
    const deleteGuide = async (guide) => {
      try {
        await showConfirmDialog({
          title: '确认删除',
          message: `确定要删除攻略"${guide.title}"吗？删除后无法恢复。`
        })
        
        await deleteGuide(guide.id)
        showToast('攻略删除成功')
        
        // 刷新列表
        pageParams.page = 0
        finished.value = false
        loadGuides()
        
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除攻略失败:', error)
          showToast('删除攻略失败')
        }
      }
    }
    
    // 获取状态样式类
    const getStatusClass = (status) => {
      switch (status) {
        case 'PUBLISHED':
          return 'status-published'
        case 'DRAFT':
          return 'status-draft'
        case 'HIDDEN':
          return 'status-hidden'
        default:
          return ''
      }
    }
    
    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      const date = new Date(time)
      const now = new Date()
      const diff = now - date
      
      const minutes = Math.floor(diff / 60000)
      const hours = Math.floor(diff / 3600000)
      const days = Math.floor(diff / 86400000)
      
      if (minutes < 1) return '刚刚'
      if (minutes < 60) return `${minutes}分钟前`
      if (hours < 24) return `${hours}小时前`
      if (days < 7) return `${days}天前`
      
      return date.toLocaleDateString()
    }
    
    // 返回
    const goBack = () => {
      router.back()
    }
    
    // 处理图片URL
    const getImageUrl = (imagePath) => {
      // 如果已经是完整URL，直接返回
      if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
        return imagePath
      }
      
      // 如果是相对路径，确保以/开头
      if (!imagePath.startsWith('/')) {
        imagePath = '/' + imagePath
      }
      
      // 对于上传的图片，通过Vite代理到后端
      return imagePath
    }
    
    // 处理图片加载错误
    const handleImageError = (event) => {
      console.warn('图片加载失败:', event.target.src)
      // 隐藏加载失败的图片，显示占位符
      event.target.style.display = 'none'
    }
    
    // 初始化
    onMounted(() => {
      loadGuides()
    })
    
    return {
      guides,
      loading,
      finished,
      refreshing,
      activeTab,
      stats,
      loadGuides,
      onRefresh,
      onTabChange,
      goToGuideDetail,
      goToCreateGuide,
      editGuide,
      deleteGuide,
      getStatusClass,
      formatTime,
      goBack,
      getImageUrl,
      handleImageError
    }
  }
}
</script>

<style scoped>
.my-guides {
  padding-top: 46px; /* 为固定导航栏留出空间 */
  background: #f5f5f5;
  min-height: 100vh;
}

.stats-section {
  padding: 16px;
}

.stats-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.filter-section {
  background: white;
  position: sticky;
  top: 46px;
  z-index: 10;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
}

.guide-item {
  margin: 8px;
}

.guide-card {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.guide-cover {
  position: relative;
  height: 150px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
}

.placeholder-text {
  margin-top: 8px;
  font-size: 12px;
}

.guide-status {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
}

.status-published {
  background: #52c41a;
}

.status-draft {
  background: #faad14;
}

.status-hidden {
  background: #999;
}

.guide-info {
  padding: 16px;
}

.guide-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.5;
  margin: 0 0 12px 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.guide-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
}

.publish-time {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
}

.guide-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
