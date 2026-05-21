<template>
  <div class="guides page-with-tabbar">
    <!-- 导航栏 -->
    <van-nav-bar title="攻略" fixed />
    <div class="nav-spacer"></div>

    <!-- 搜索框 -->
    <van-search
      v-model="searchKeyword"
      placeholder="搜索攻略..."
      @search="onSearch"
      @cancel="onSearchCancel"
    />

    <!-- 分类筛选 -->
    <div class="filter-section">
      <van-tabs v-model:active="activeTab" @change="onTabChange">
        <van-tab title="全部" name="all" />
        <van-tab title="旅行贴士" name="TRAVEL_TIPS" />
        <van-tab title="目的地攻略" name="DESTINATION_GUIDE" />
        <van-tab title="交通指南" name="TRANSPORTATION" />
        <van-tab title="住宿推荐" name="ACCOMMODATION" />
        <van-tab title="美食攻略" name="FOOD_GUIDE" />
      </van-tabs>
    </div>

    <!-- 排序选项 -->
    <div class="sort-section">
      <van-dropdown-menu>
        <van-dropdown-item v-model="sortBy" :options="sortOptions" @change="onSortChange" />
      </van-dropdown-menu>
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
                  :src="guide.coverImage"
                  class="cover-image"
                  fit="cover"
                />
                <van-image
                  v-else
                  src="/default-guide.png"
                  class="cover-image"
                  fit="cover"
                />
                <div class="guide-type">{{ guide.typeDisplayName }}</div>
              </div>

              <!-- 攻略信息 -->
              <div class="guide-info">
                <h3 class="guide-title">{{ guide.title }}</h3>
                <p class="guide-summary">{{ guide.summary || guide.content.substring(0, 100) + '...' }}</p>
                
                <!-- 作者信息 -->
                <div class="guide-author">
                  <van-image
                    :src="guide.author.avatar || '/default-avatar.png'"
                    class="author-avatar"
                    round
                    fit="cover"
                  />
                  <span class="author-name">{{ guide.author.nickname }}</span>
                  <span class="publish-time">{{ formatTime(guide.createdAt) }}</span>
                </div>

                <!-- 统计信息 -->
                <div class="guide-stats">
                  <van-icon name="eye-o" />
                  <span>{{ guide.viewCount }}</span>
                  <van-icon name="good-job-o" />
                  <span>{{ guide.likeCount }}</span>
                  <van-icon name="chat-o" />
                  <span>{{ guide.commentCount }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </van-list>
    </van-pull-refresh>

    <!-- 底部导航栏 -->
    <van-tabbar v-model="activeTabbar" fixed>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/posts">动态</van-tabbar-item>
      <van-tabbar-item icon="calendar-o" to="/activities">活动</van-tabbar-item>
      <van-tabbar-item icon="bookmark-o" to="/guides">攻略</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>

    <!-- 创建攻略按钮 -->
    <div class="fixed-create-fab" @click="goToCreateGuide">
      <van-icon name="plus" />
    </div>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getGuides, searchGuides } from '@/api/guides'

export default {
  name: 'Guides',
  setup() {
    const router = useRouter()
    
    // 响应式数据
    const guides = ref([])
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const searchKeyword = ref('')
    const activeTab = ref('all')
    const sortBy = ref('latest')
    const activeTabbar = ref(3) // 攻略页面在底部导航栏的索引
    
    // 分页参数
    const pageParams = reactive({
      page: 0,
      size: 10
    })
    
    // 排序选项
    const sortOptions = [
      { text: '最新发布', value: 'latest' },
      { text: '最多浏览', value: 'popular' },
      { text: '最多点赞', value: 'liked' }
    ]
    
    // 加载攻略列表
    const loadGuides = async () => {
      if (loading.value) return
      
      loading.value = true
      
      try {
        let response
        if (searchKeyword.value.trim()) {
          // 搜索模式
          response = await searchGuides({
            keyword: searchKeyword.value,
            page: pageParams.page,
            size: pageParams.size
          })
        } else {
          // 正常列表模式
          const params = {
            page: pageParams.page,
            size: pageParams.size
          }
          
          if (activeTab.value !== 'all') {
            params.type = activeTab.value
          }
          
          if (sortBy.value === 'popular') {
            params.sortBy = 'viewCount'
          } else if (sortBy.value === 'liked') {
            params.sortBy = 'likeCount'
          }
          
          response = await getGuides(params)
        }
        
        // 处理不同的响应数据结构
        let newGuides = []
        if (response.data && response.data.content) {
          // Spring Data Page 结构
          newGuides = response.data.content
        } else if (response.content) {
          // 直接返回的 content
          newGuides = response.content
        } else if (Array.isArray(response)) {
          // 直接返回数组
          newGuides = response
        } else if (Array.isArray(response.data)) {
          // 数组在 data 中
          newGuides = response.data
        } else {
          console.log('未识别的响应结构:', response)
          newGuides = []
        }
        
        if (pageParams.page === 0) {
          guides.value = newGuides
        } else {
          guides.value.push(...newGuides)
        }
        
        // 处理分页信息
        if (response.data && response.data.content) {
          // Spring Data Page 结构
          finished.value = response.data.last || newGuides.length < pageParams.size
        } else {
          // 其他结构，根据返回数据长度判断
          finished.value = newGuides.length < pageParams.size
        }
        pageParams.page++
        
      } catch (error) {
        console.error('加载攻略失败:', error)
        showToast('加载攻略失败')
      } finally {
        loading.value = false
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
    
    // 搜索
    const onSearch = () => {
      pageParams.page = 0
      finished.value = false
      loadGuides()
    }
    
    // 取消搜索
    const onSearchCancel = () => {
      searchKeyword.value = ''
      pageParams.page = 0
      finished.value = false
      loadGuides()
    }
    
    // 切换分类
    const onTabChange = () => {
      pageParams.page = 0
      finished.value = false
      loadGuides()
    }
    
    // 切换排序
    const onSortChange = () => {
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
    
    // 初始化
    onMounted(() => {
      loadGuides()
    })
    
    return {
      guides,
      loading,
      finished,
      refreshing,
      searchKeyword,
      activeTab,
      sortBy,
      activeTabbar,
      sortOptions,
      loadGuides,
      onRefresh,
      onSearch,
      onSearchCancel,
      onTabChange,
      onSortChange,
      goToGuideDetail,
      goToCreateGuide,
      formatTime
    }
  }
}
</script>

<style scoped>
.guides {
  padding-top: 0;
  background: var(--page-bg);
}

.filter-section {
  background: white;
  position: sticky;
  top: 46px;
  z-index: var(--z-header);
}

.sort-section {
  background: white;
  padding: 8px 16px;
  border-bottom: 1px solid #eee;
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
  height: 200px;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.guide-type {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0,0,0,0.6);
  color: white;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
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

.guide-author {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.author-avatar {
  width: 24px;
  height: 24px;
  margin-right: 8px;
}

.author-name {
  font-size: 12px;
  color: #999;
  margin-right: 8px;
}

.publish-time {
  font-size: 12px;
  color: #999;
}

.guide-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.guide-stats .van-icon {
  margin-right: 4px;
}

/* 创建攻略按钮样式 */
.create-guide-btn {
  position: fixed;
  bottom: 108px;
  right: 20px;
  z-index: 1000;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 50%;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.create-guide-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 16px rgba(102, 126, 234, 0.6);
}

.create-guide-btn .van-icon {
  font-size: 24px;
  color: white;
}

/* 底部导航栏由 .page-with-tabbar 统一留白 */

/* 空状态样式优化 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

.empty-state .van-empty {
  padding: 40px 0;
}

/* 筛选区域样式 */
.filter-section {
  background: white;
  border-bottom: 1px solid #f0f0f0;
}

.sort-section {
  background: white;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 16px;
}
</style>
