<template>
  <div class="post-detail page-nav-only">
    <!-- 导航栏 -->
    <van-nav-bar title="动态详情" fixed left-arrow @click-left="goBack" />
    <div class="nav-spacer"></div>

    <!-- 内容区域 -->
    <div class="content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-wrapper">
        <van-loading size="24" />
        <span>加载中...</span>
      </div>

      <!-- 动态内容 -->
      <div v-else-if="post" class="post-content">
        <!-- 用户信息 -->
        <div class="post-header">
          <div class="user-info">
            <div class="avatar">
              <van-image
                v-if="post.user?.avatar"
                :src="getUserAvatarUrl(post.user)"
                class="avatar-image"
                round
                fit="cover"
              />
              <van-icon v-else name="user-o" size="24" class="avatar-icon" />
            </div>
            <div class="user-details">
              <div class="username">{{ post.user?.nickname || post.user?.username }}</div>
              <div class="post-time">{{ formatTime(post.createdAt) }}</div>
            </div>
          </div>
          <van-button
            v-if="post.user?.id !== userStore.userInfo?.id"
            :type="post.user?.isFollowed ? 'default' : 'primary'"
            size="small"
            @click="toggleFollow"
            :loading="following"
          >
            {{ post.user?.isFollowed ? '已关注' : '关注' }}
          </van-button>
        </div>

        <!-- 动态内容 -->
        <div class="post-body">
          <p class="content-text">{{ post.content }}</p>
          
          <!-- 图片展示 -->
          <div v-if="post.images && post.images.length > 0" class="post-images">
            <van-image
              v-for="(image, index) in post.images"
              :key="index"
              :src="getFullImageUrl(image)"
              class="post-image"
              fit="cover"
              @click="previewImage(index)"
            />
          </div>

          <!-- 位置信息 -->
          <div v-if="post.location" class="location-info">
            <van-icon name="location-o" />
            <span>{{ post.location }}</span>
          </div>
        </div>

        <!-- 互动数据 -->
        <div class="post-stats">
          <div class="stat-item">
            <van-icon name="like-o" />
            <span>{{ post.likeCount || 0 }} 赞</span>
          </div>
          <div class="stat-item">
            <van-icon name="chat-o" />
            <span>{{ post.commentCount || 0 }} 评论</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="post-actions two">
          <van-button
            :icon="post.isLiked ? 'like' : 'like-o'"
            :type="post.isLiked ? 'danger' : 'default'"
            size="large"
            @click="toggleLike"
            :loading="liking"
          >
            {{ post.isLiked ? '已赞' : '点赞' }}
          </van-button>
          <van-button
            icon="chat-o"
            size="large"
            @click="scrollToComments"
          >
            评论
          </van-button>
        </div>
      </div>

      <!-- 错误状态 -->
      <div v-else class="error-wrapper">
        <van-empty description="动态不存在或已删除" />
        <van-button type="primary" @click="goBack">返回</van-button>
      </div>
    </div>

    <!-- 评论区域 -->
    <div v-if="post" class="comments-section">
      <CommentList
        :post-id="postId"
        @comment-added="onCommentAdded"
        @comment-deleted="onCommentDeleted"
        ref="commentListRef"
      />
    </div>

    <!-- 图片预览 -->
    <van-image-preview
      v-model:show="showImagePreview"
      :images="previewImages"
      :start-position="previewIndex"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getPost, likePost, unlikePost } from '@/api/posts'
import { followUser, unfollowUser, getFollowStatus } from '@/api/user'
import { getUserAvatarUrl } from '@/utils/avatar'
import { showToast } from 'vant'
import CommentList from '@/components/CommentList.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const post = ref(null)
const loading = ref(false)
const liking = ref(false)
const following = ref(false)
const showImagePreview = ref(false)
const previewImages = ref([])
const previewIndex = ref(0)
const commentListRef = ref(null)

// 计算属性
const postId = computed(() => route.params.id)

// 方法
const goBack = () => {
  router.back()
}

const loadPost = async () => {
  if (!postId.value) return
  
  loading.value = true
  
  try {
    const response = await getPost(postId.value)
    post.value = response.data || response
    if (post.value.user?.id && userStore.isLoggedIn && post.value.user.id !== userStore.userInfo?.id) {
      try {
        const st = await getFollowStatus(post.value.user.id)
        post.value.user.isFollowed = !!st?.following
      } catch {
        post.value.user.isFollowed = false
      }
    }
    // 准备图片预览数据
    if (post.value.images && post.value.images.length > 0) {
      previewImages.value = post.value.images.map(img => getFullImageUrl(img))
    }
    
  } catch (error) {
    console.error('加载动态失败:', error)
    showToast('加载动态失败')
  } finally {
    loading.value = false
  }
}

const toggleLike = async () => {
  if (liking.value) return
  
  liking.value = true
  
  try {
    if (post.value.isLiked) {
      await unlikePost(postId.value)
      post.value.isLiked = false
      post.value.likeCount = Math.max(0, (post.value.likeCount || 0) - 1)
    } else {
      await likePost(postId.value)
      post.value.isLiked = true
      post.value.likeCount = (post.value.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('操作失败:', error)
    showToast('操作失败')
  } finally {
    liking.value = false
  }
}

const toggleFollow = async () => {
  if (following.value || !post.value.user) return
  
  following.value = true
  
  try {
    if (post.value.user.isFollowed) {
      await unfollowUser(post.value.user.id)
      post.value.user.isFollowed = false
      showToast('取消关注成功')
    } else {
      await followUser(post.value.user.id)
      post.value.user.isFollowed = true
      showToast('关注成功')
    }
    userStore.fetchUserInfo?.().catch(() => {})
  } catch (error) {
    console.error('操作失败:', error)
    showToast(error.response?.data?.message || '操作失败')
  } finally {
    following.value = false
  }
}

const previewImage = (index) => {
  previewIndex.value = index
  showImagePreview.value = true
}

const scrollToComments = () => {
  const commentsSection = document.querySelector('.comments-section')
  if (commentsSection) {
    commentsSection.scrollIntoView({ behavior: 'smooth' })
  }
}

const getFullImageUrl = (imageUrl) => {
  if (!imageUrl) return ''
  if (imageUrl.startsWith('http')) return imageUrl
  return `http://localhost:8081${imageUrl}`
}

const formatTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const postTime = new Date(time)
  const diff = now - postTime
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return postTime.toLocaleDateString()
}

const onCommentAdded = (comment) => {
  // 更新评论数
  post.value.commentCount = (post.value.commentCount || 0) + 1
}

const onCommentDeleted = (comment) => {
  // 更新评论数
  post.value.commentCount = Math.max(0, (post.value.commentCount || 0) - 1)
}

onMounted(() => {
  loadPost()
})
</script>

<style scoped>
.post-detail {
  min-height: 100vh;
  background-color: var(--page-bg);
}

.content {
  padding: 0 var(--page-padding-lg);
  padding-bottom: var(--content-bottom-safe);
}

.loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 60px 20px;
  color: #999;
}

.post-content {
  background: white;
  margin-bottom: 8px;
}

.post-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.avatar-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 50%;
  color: #999;
}

.user-details {
  flex: 1;
}

.username {
  font-weight: 500;
  font-size: 16px;
  color: #333;
  margin-bottom: 2px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-body {
  padding: 16px;
}

.content-text {
  font-size: 16px;
  line-height: 1.6;
  color: #333;
  margin-bottom: 16px;
  word-break: break-word;
}

.post-images {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 8px;
  margin-bottom: 16px;
}

.post-image {
  width: 100%;
  height: 120px;
  border-radius: 8px;
  cursor: pointer;
}

.location-info {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.post-stats {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 0 16px 12px;
  font-size: 14px;
  color: #666;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.post-actions.two {
  display: flex;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  gap: 8px;
}
.post-actions {
  display: flex;
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  gap: 8px;
}

.post-actions .van-button,
.post-actions.two .van-button {
  flex: 1;
}

.comments-section {
  background: white;
  margin-top: 8px;
}

.error-wrapper {
  padding: 60px 20px;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .post-header {
    padding: 12px;
  }
  
  .post-body {
    padding: 12px;
  }
  
  .post-actions {
    padding: 8px 12px;
  }
  
  .post-images {
    grid-template-columns: repeat(auto-fit, minmax(80px, 1fr));
  }
  
  .post-image {
    height: 100px;
  }
}
</style>
