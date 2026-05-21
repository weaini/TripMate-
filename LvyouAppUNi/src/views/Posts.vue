<template>
  <div class="posts page-with-tabbar">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="动态" fixed>
      <template #left>
        <van-icon name="chat-o" size="20" color="#1989fa" />
      </template>
      <template #right>
        <van-icon name="plus" size="18" color="#1989fa" @click="goToCreatePost" />
      </template>
    </van-nav-bar>
    <div class="nav-spacer"></div>

    <div class="posts-search">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索内容或地点"
        @search="onSearchKeyword"
        @clear="onSearchKeyword"
      />
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <van-tabs v-model:active="feedTab" @change="handleFeedTabChange">
        <van-tab name="all">
          <template #title>
            <van-icon name="apps-o" size="16" style="margin-right: 4px;" />
            全部
          </template>
        </van-tab>
        <van-tab name="following">
          <template #title>
            <van-icon name="friends-o" size="16" style="margin-right: 4px;" />
            关注
          </template>
        </van-tab>
        <van-tab name="hot">
          <template #title>
            <van-icon name="fire-o" size="16" style="margin-right: 4px;" />
            热门
          </template>
        </van-tab>
        <van-tab name="latest">
          <template #title>
            <van-icon name="clock-o" size="16" style="margin-right: 4px;" />
            最新
          </template>
        </van-tab>
      </van-tabs>
      <van-icon name="filter-o" class="filter-icon" :class="{ active: hasExtraFilters }" @click="showFilter = true" />
    </div>

    <div v-if="hasExtraFilters" class="filter-chips">
      <van-tag v-if="searchKeyword.trim()" closeable size="medium" type="primary" @close="clearSearch">
        搜索: {{ searchKeyword.trim() }}
      </van-tag>
      <van-tag v-if="filterType !== 'all'" closeable size="medium" type="primary" @close="filterType = 'all'; applyFilterQuiet()">
        {{ postTypeLabel(filterType) }}
      </van-tag>
      <van-tag v-if="filterTime !== 'all'" closeable size="medium" type="primary" @close="filterTime = 'all'; applyFilterQuiet()">
        {{ timeRangeLabel(filterTime) }}
      </van-tag>
      <van-button v-if="hasExtraFilters" size="mini" plain type="primary" @click="clearAllExtraFilters">清空筛选</van-button>
    </div>

    <!-- 筛选弹窗 -->
    <van-popup v-model:show="showFilter" position="bottom">
      <div class="filter-popup">
        <div class="filter-header">
          <h3>筛选条件</h3>
          <van-icon name="cross" @click="showFilter = false" />
        </div>
        <div class="filter-content">
          <div class="filter-section">
            <h4>动态类型</h4>
            <van-radio-group v-model="filterType">
              <van-radio name="all">全部</van-radio>
              <van-radio name="DYNAMIC">实时动态</van-radio>
              <van-radio name="STRATEGY">攻略分享</van-radio>
              <van-radio name="ACTIVITY">活动相关</van-radio>
            </van-radio-group>
          </div>
          <div class="filter-section">
            <h4>时间范围</h4>
            <van-radio-group v-model="filterTime">
              <van-radio name="all">全部时间</van-radio>
              <van-radio name="today">今天</van-radio>
              <van-radio name="week">本周</van-radio>
              <van-radio name="month">本月</van-radio>
            </van-radio-group>
          </div>
        </div>
        <div class="filter-footer">
          <van-button @click="resetFilter">重置</van-button>
          <van-button type="primary" @click="applyFilter">确定</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 动态列表 -->
    <div class="posts-list" :class="{ 'with-chips': hasExtraFilters }">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadPosts"
      >
        <PostCard
          v-for="post in posts"
          :key="post.id"
          :post="post"
          @click="goToPostDetail(post.id)"
        />
      </van-list>
    </div>

    <!-- 发布按钮 -->
    <div class="fixed-create-fab" @click="goToCreatePost">
      <van-icon name="plus" />
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="tabbarActive" fixed>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/posts">动态</van-tabbar-item>
      <van-tabbar-item icon="calendar-o" to="/activities">活动</van-tabbar-item>
      <van-tabbar-item icon="bookmark-o" to="/guides">攻略</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPosts } from '@/api/posts'
import PostCard from '@/components/PostCard.vue'

const router = useRouter()

// 顶部分类（全部/关注/热门/最新）与底部 TabBar 必须分开，否则会互相覆盖 v-model
const feedTab = ref('all')
const tabbarActive = ref(1) // 底部「动态」为 1
const posts = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 筛选相关
const showFilter = ref(false)
const filterType = ref('all')
const filterTime = ref('all')
const searchKeyword = ref('')

const hasExtraFilters = computed(
  () => !!searchKeyword.value.trim() || filterType.value !== 'all' || filterTime.value !== 'all'
)

// 方法
const goToCreatePost = () => {
  router.push('/posts/create')
}

const goToPostDetail = (id) => {
  router.push(`/posts/${id}`)
}

const handleFeedTabChange = () => {
  resetPosts()
  loadPosts()
}

const onSearchKeyword = () => {
  resetPosts()
  loadPosts()
}

const clearSearch = () => {
  searchKeyword.value = ''
  resetPosts()
  loadPosts()
}

const applyFilterQuiet = () => {
  resetPosts()
  loadPosts()
}

const clearAllExtraFilters = () => {
  searchKeyword.value = ''
  filterType.value = 'all'
  filterTime.value = 'all'
  resetPosts()
  loadPosts()
}

const postTypeLabel = (t) => {
  const m = { DYNAMIC: '实时动态', STRATEGY: '攻略分享', ACTIVITY: '活动相关' }
  return m[t] || t
}

const timeRangeLabel = (t) => {
  const m = { today: '今天', week: '本周', month: '本月' }
  return m[t] || t
}

const resetPosts = () => {
  posts.value = []
  page.value = 1
  finished.value = false
}

const loadPosts = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const params = {
      page: page.value - 1, // Spring Boot分页从0开始
      size: pageSize.value
    }
    
    // 根据顶部分类 tab 添加不同参数（勿与底部 tabbar 混用）
    switch (feedTab.value) {
      case 'following':
        params.following = true
        break
      case 'hot':
        params.sort = 'popular'
        break
      case 'latest':
        params.sort = 'latest'
        break
    }
    
    // 添加筛选条件
    if (filterType.value !== 'all') {
      params.type = filterType.value
    }
    
    const kw = searchKeyword.value.trim()
    if (kw) {
      params.keyword = kw
    }

    if (filterTime.value !== 'all') {
      const now = new Date()
      switch (filterTime.value) {
        case 'today':
          params.startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate()).toISOString()
          break
        case 'week':
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          params.startDate = weekAgo.toISOString()
          break
        case 'month':
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
          params.startDate = monthAgo.toISOString()
          break
      }
    }
    
    // 尝试从API获取数据
    let response
    try {
      console.log('发送API请求，参数:', params)
      response = await getPosts(params)
      console.log('API响应:', response)
    } catch (apiError) {
      console.warn('API请求失败，使用模拟数据:', apiError)
      // 如果API请求失败，使用模拟数据
      response = getMockPosts()
    }
    
    if (page.value === 1) {
      posts.value = response.content || []
    } else {
      posts.value.push(...(response.content || []))
    }
    
    page.value++
    finished.value = !response.content || response.content.length < pageSize.value
  } catch (error) {
    console.error('加载动态失败:', error)
    // 如果完全失败，使用模拟数据
    if (page.value === 1) {
      posts.value = getMockPosts()
    }
  } finally {
    loading.value = false
  }
}

// 模拟数据函数
const getMockPosts = () => {
  return [
    {
      id: 1,
      content: '今天去了美丽的西湖，风景如画！推荐大家一定要来看看，特别是夕阳西下的时候，真的太美了！',
      location: '杭州西湖',
      likeCount: 15,
      commentCount: 3,
      shareCount: 2,
      viewCount: 128,
      isLiked: false,
      createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
      user: {
        id: 1,
        username: 'traveler001',
        nickname: '旅行达人小王',
        avatar: '/static/【哲风壁纸】动漫角色-卡通场景.png'
      },
      images: ['/static/【哲风壁纸】天坛-天空-彩烟.png']
    },
    {
      id: 2,
      content: '分享一个超棒的徒步路线！从黄山脚下到山顶，全程约8公里，风景绝美，体力好的朋友可以挑战一下！',
      location: '安徽黄山',
      likeCount: 28,
      commentCount: 7,
      shareCount: 5,
      viewCount: 256,
      isLiked: true,
      createdAt: new Date(Date.now() - 5 * 60 * 60 * 1000).toISOString(),
      user: {
        id: 2,
        username: 'hiker_mike',
        nickname: '登山爱好者Mike',
        avatar: '/static/【哲风壁纸】党徽-共产主义接班人.png'
      },
      images: []
    },
    {
      id: 3,
      content: '刚参加完一个摄影活动，学到了很多拍照技巧！和大家分享几张作品，欢迎点评～',
      location: '北京798艺术区',
      likeCount: 42,
      commentCount: 12,
      shareCount: 8,
      viewCount: 389,
      isLiked: false,
      createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
      user: {
        id: 3,
        username: 'photo_lover',
        nickname: '摄影爱好者',
        avatar: '/static/【哲风壁纸】动漫角色-卡通场景.png'
      },
      images: ['/static/【哲风壁纸】天坛-天空-彩烟.png', '/static/【哲风壁纸】党徽-共产主义接班人.png']
    }
  ]
}

// 筛选相关方法
const resetFilter = () => {
  filterType.value = 'all'
  filterTime.value = 'all'
  searchKeyword.value = ''
}

const applyFilter = () => {
  showFilter.value = false
  resetPosts()
  loadPosts()
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.posts {
  padding-top: 0;
  background-color: var(--page-bg);
}

.posts-search {
  position: fixed;
  top: var(--nav-height, 46px);
  left: 0;
  right: 0;
  z-index: calc(var(--z-header, 100) - 1);
  background: #fff;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.06);
}

.filter-bar {
  background: white;
  position: fixed;
  top: calc(var(--nav-height, 46px) + 54px);
  left: 0;
  right: 0;
  z-index: var(--z-header);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #f0f0f0;
}

.filter-icon {
  font-size: 18px;
  color: #1989fa;
  cursor: pointer;
  padding: 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.filter-icon:hover {
  background-color: #f0f8ff;
  color: #1976d2;
}

.filter-icon.active {
  color: #1989fa;
}

.filter-chips {
  position: fixed;
  top: calc(var(--nav-height, 46px) + 54px + 44px);
  left: 0;
  right: 0;
  z-index: calc(var(--z-header, 100) - 2);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f7f8fa;
  border-bottom: 1px solid #eee;
}

.posts-list {
  padding: var(--page-padding);
  padding-bottom: var(--content-bottom-safe);
  margin-top: calc(var(--nav-height, 46px) + 54px + 44px + 8px);
}

.posts-list.with-chips {
  margin-top: calc(var(--nav-height, 46px) + 54px + 44px + 44px + 8px);
}

:deep(.van-tabs__line) {
  background-color: #1989fa;
}

:deep(.van-tab) {
  font-size: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.van-tab__title) {
  display: flex;
  align-items: center;
  justify-content: center;
}

:deep(.van-floating-bubble) {
  background-color: #1989fa;
  bottom: 108px !important;
}

/* 筛选弹窗样式 */
.filter-popup {
  padding: 20px;
  max-height: 60vh;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.filter-content {
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.filter-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

:deep(.van-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.van-radio) {
  margin-right: 0;
}
</style>
