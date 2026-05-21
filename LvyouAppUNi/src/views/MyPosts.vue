<template>
  <div class="my-posts page-with-tabbar">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="我的动态" fixed>
      <template #left>
        <van-icon name="arrow-left" size="20" @click="goBack" />
      </template>
      <template #right>
        <van-icon name="plus" size="18" color="#1989fa" @click="goToCreatePost" />
      </template>
    </van-nav-bar>
    <div class="nav-spacer"></div>

    <!-- 统计信息 -->
    <div class="stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-number">{{ totalPosts }}</div>
          <div class="stat-label">总动态</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ totalLikes }}</div>
          <div class="stat-label">总点赞</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ totalComments }}</div>
          <div class="stat-label">总评论</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <van-tabs v-model:active="activeFilter" @change="handleFilterChange">
        <van-tab name="all">
          <template #title>
            <van-icon name="apps-o" size="16" style="margin-right: 4px;" />
            全部
          </template>
        </van-tab>
        <van-tab name="PENDING">
          <template #title>
            <van-icon name="underway-o" size="16" style="margin-right: 4px;" />
            待审核
          </template>
        </van-tab>
        <van-tab name="APPROVED">
          <template #title>
            <van-icon name="checked" size="16" style="margin-right: 4px;" />
            已通过
          </template>
        </van-tab>
        <van-tab name="REJECTED">
          <template #title>
            <van-icon name="close" size="16" style="margin-right: 4px;" />
            已拒绝
          </template>
        </van-tab>
      </van-tabs>
    </div>

    <!-- 动态列表 -->
    <div class="posts-list">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="loadMyPosts"
      >
        <div v-if="posts.length === 0 && !loading" class="empty-state">
          <van-empty
            image="https://fastly.jsdelivr.net/npm/@vant/assets/custom-empty-image.png"
            :description="getEmptyDescription()"
          >
            <van-button type="primary" @click="goToCreatePost">发布第一条动态</van-button>
          </van-empty>
        </div>
        
        <div v-for="post in posts" :key="post.id" class="post-item">
          <PostCard
            :post="post"
            :show-actions="true"
            @click="goToPostDetail(post.id)"
            @delete="handleDeletePost(post.id)"
            @edit="handleEditPost(post)"
          />
        </div>
      </van-list>
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" fixed>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/posts">动态</van-tabbar-item>
      <van-tabbar-item icon="calendar-o" to="/activities">活动</van-tabbar-item>
      <van-tabbar-item icon="bookmark-o" to="/guides">攻略</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyPosts, deletePost } from '@/api/posts'
import { showConfirmDialog, showToast } from 'vant'
import PostCard from '@/components/PostCard.vue'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const activeTab = ref(4) // 当前在"我的"页面
const activeFilter = ref('all')
const posts = ref([])
const loading = ref(false)
const finished = ref(false)
const page = ref(1)
const pageSize = ref(10)

// 统计数据
const totalPosts = ref(0)
const totalLikes = ref(0)
const totalComments = ref(0)

// 方法
const goBack = () => {
  router.back()
}

const goToCreatePost = () => {
  router.push('/posts/create')
}

const goToPostDetail = (id) => {
  router.push(`/posts/${id}`)
}

const handleFilterChange = (name) => {
  activeFilter.value = name
  resetPosts()
  loadMyPosts()
}

const resetPosts = () => {
  posts.value = []
  page.value = 1
  finished.value = false
}

const loadMyPosts = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const params = {
      page: page.value - 1, // Spring Boot分页从0开始
      size: pageSize.value
    }
    
    if (activeFilter.value !== 'all') {
      params.status = activeFilter.value
    }
    
    console.log('🔍 获取我的动态，参数:', params)
    const response = await getMyPosts(params)
    console.log('📊 我的动态响应:', response)
    
    if (page.value === 1) {
      posts.value = response.content || []
    } else {
      posts.value.push(...(response.content || []))
    }
    
    // 更新统计数据
    if (page.value === 1) {
      totalPosts.value = response.totalElements || 0
      totalLikes.value = posts.value.reduce((sum, post) => sum + (post.likeCount || 0), 0)
      totalComments.value = posts.value.reduce((sum, post) => sum + (post.commentCount || 0), 0)
    }
    
    page.value++
    finished.value = !response.content || response.content.length < pageSize.value
  } catch (error) {
    console.error('加载我的动态失败:', error)
    showToast('加载失败，请重试')
    
    // 如果API失败，使用模拟数据
    if (page.value === 1) {
      posts.value = getMockMyPosts()
      totalPosts.value = posts.value.length
      totalLikes.value = posts.value.reduce((sum, post) => sum + (post.likeCount || 0), 0)
      totalComments.value = posts.value.reduce((sum, post) => sum + (post.commentCount || 0), 0)
    }
  } finally {
    loading.value = false
  }
}

// 模拟数据
const getMockMyPosts = () => {
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
        id: userStore.userInfo?.id,
        username: userStore.userInfo?.username,
        nickname: userStore.userInfo?.nickname,
        avatar: userStore.userInfo?.avatar
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
        id: userStore.userInfo?.id,
        username: userStore.userInfo?.username,
        nickname: userStore.userInfo?.nickname,
        avatar: userStore.userInfo?.avatar
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
        id: userStore.userInfo?.id,
        username: userStore.userInfo?.username,
        nickname: userStore.userInfo?.nickname,
        avatar: userStore.userInfo?.avatar
      },
      images: ['/static/【哲风壁纸】天坛-天空-彩烟.png', '/static/【哲风壁纸】党徽-共产主义接班人.png']
    }
  ]
}

// 删除动态
const handleDeletePost = async (postId) => {
  try {
    await showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这条动态吗？删除后无法恢复。'
    })
    
    await deletePost(postId)
    showToast('删除成功')
    
    // 重新加载动态列表
    resetPosts()
    loadMyPosts()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除动态失败:', error)
      showToast('删除失败，请重试')
    }
  }
}

// 编辑动态
const handleEditPost = (post) => {
  if (post.status === 'APPROVED') {
    showToast('审核通过的动态不能再编辑')
    return
  }
  router.push(`/posts/${post.id}/edit`)
}

const getEmptyDescription = () => {
  const map = {
    all: '还没有发布任何动态',
    PENDING: '当前没有待审核的动态',
    APPROVED: '当前没有已通过的动态',
    REJECTED: '当前没有已拒绝的动态'
  }
  return map[activeFilter.value] || '暂无动态'
}

onMounted(async () => {
  if (!userStore.isLoggedIn) {
    showToast('请先登录')
    router.push('/login')
    return
  }
  
  // 确保用户信息已加载
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      showToast('获取用户信息失败')
      router.push('/login')
      return
    }
  }
  
  loadMyPosts()
})
</script>

<style scoped>
.my-posts {
  padding-top: 0vw;
  padding-bottom: 50px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

/* 统计信息 */
.stats-section {
  padding: 16px;
  background: white;
  margin-bottom: 8px;
}

.stats-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: space-around;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.2);
}

.stat-item {
  text-align: center;
  color: white;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

/* 筛选栏 */
.filter-bar {
  background: white;
  position: sticky;
  top: 46px;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #f0f0f0;
}

/* 动态列表 */
.posts-list {
  padding: 10px;
}

.post-item {
  margin-bottom: 10px;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
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

</style>
