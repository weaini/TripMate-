<template>
  <div class="post-card" @click="$emit('click')">
    <div class="post-header">
      <div class="avatar">
        <van-image
          v-if="post.user?.avatar"
          :src="getUserAvatarUrl(post.user)"
          class="avatar-image"
          round
          fit="cover"
        />
        <van-icon v-else name="user-o" size="20" class="avatar-icon" />
      </div>
      <div class="user-info">
        <div class="username">{{ post.user?.nickname || post.user?.username }}</div>
        <div class="post-time">{{ formatTime(post.createdAt) }}</div>
      </div>
      <div class="post-actions-header">
        <van-tag
          v-if="showActions && post.status"
          :type="getStatusType(post.status)"
          size="medium"
          class="post-status-tag"
        >
          {{ getStatusText(post.status) }}
        </van-tag>
        <van-button
          v-if="!showActions && post.user?.id !== userStore.userInfo?.id"
          :type="post.user?.isFollowed ? 'default' : 'primary'"
          size="mini"
          @click.stop="toggleFollow"
          :loading="post.following"
        >
          {{ post.user?.isFollowed ? '已关注' : '关注' }}
        </van-button>
        
        <!-- 我的动态操作按钮 -->
        <div v-if="showActions" class="my-actions">
          <van-button
            v-if="canEdit"
            type="primary"
            size="mini"
            icon="edit"
            @click.stop="handleEdit"
          >
            编辑
          </van-button>
          <van-button
            type="danger"
            size="mini"
            icon="delete"
            @click.stop="handleDelete"
          >
            删除
          </van-button>
        </div>
        
        <van-icon v-if="!showActions" name="ellipsis" class="more-icon" />
      </div>
    </div>
    
    <div class="post-content">
      <p class="content-text">{{ post.content }}</p>
      <div v-if="post.images && post.images.length > 0" class="post-images">
        <van-image
          v-for="(image, index) in post.images.slice(0, 3)"
          :key="index"
          :src="image"
          class="post-image"
          fit="cover"
        />
        <div v-if="post.images.length > 3" class="more-images">
          +{{ post.images.length - 3 }}
        </div>
      </div>
    </div>
    
    <div class="post-footer">
      <div class="post-stats">
        <span class="location" v-if="post.location">
          <van-icon name="location-o" />
          {{ post.location }}
        </span>
      </div>
      <div class="post-actions">
        <van-button
          :icon="post.isLiked ? 'like' : 'like-o'"
          :class="post.isLiked ? 'like-button-liked' : 'like-button-unliked'"
          size="small"
          @click.stop="handleLike"
        >
          {{ post.likeCount || 0 }}
        </van-button>
        <van-button icon="chat-o" size="small" @click.stop="handleComment">
          {{ post.commentCount || 0 }}
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits, computed } from 'vue'
import { likePost, unlikePost } from '@/api/posts'
import { followUser, unfollowUser } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'
import { getUserAvatarUrl } from '@/utils/avatar'

const props = defineProps({
  post: {
    type: Object,
    required: true
  },
  showActions: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click', 'delete', 'edit'])

const userStore = useUserStore()
const canEdit = computed(() => !props.showActions || props.post.status !== 'APPROVED')

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已拒绝',
    DELETED: '已删除'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger',
    DELETED: 'default'
  }
  return map[status] || 'primary'
}

// 处理点赞
const handleLike = async () => {
  try {
    if (props.post.isLiked) {
      await unlikePost(props.post.id)
      props.post.isLiked = false
      props.post.likeCount = (props.post.likeCount || 1) - 1
    } else {
      await likePost(props.post.id)
      props.post.isLiked = true
      props.post.likeCount = (props.post.likeCount || 0) + 1
    }
  } catch (error) {
    showToast('操作失败')
  }
}

// 处理评论
const handleComment = () => {
  // 跳转到动态详情页面查看评论
  window.location.href = `/posts/${props.post.id}`
}

// 处理关注
const toggleFollow = async () => {
  if (props.post.following || !props.post.user) return
  
  props.post.following = true
  
  try {
    if (props.post.user.isFollowed) {
      await unfollowUser(props.post.user.id)
      props.post.user.isFollowed = false
      showToast('取消关注成功')
    } else {
      await followUser(props.post.user.id)
      props.post.user.isFollowed = true
      showToast('关注成功')
    }
    userStore.fetchUserInfo?.().catch(() => {})
  } catch (error) {
    console.error('操作失败:', error)
    showToast(error.response?.data?.message || '操作失败')
  } finally {
    props.post.following = false
  }
}

// 处理编辑
const handleEdit = () => {
  emit('edit', props.post.id)
}

// 处理删除
const handleDelete = () => {
  emit('delete', props.post.id)
}
</script>

<style scoped>
.post-card {
  background: white;
  margin-bottom: 10px;
  padding: 15px;
  border-radius: 8px;
}

.post-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.avatar {
  width: 40px;
  height: 40px;
  margin-right: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 50%;
}

.avatar-image {
  width: 100%;
  height: 100%;
}

.avatar-icon {
  color: #999;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}

.post-time {
  font-size: 12px;
  color: #999;
}

.post-actions-header {
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-status-tag {
  flex-shrink: 0;
}

.more-icon {
  color: #999;
  font-size: 16px;
}

.post-content {
  margin-bottom: 12px;
}

.content-text {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 10px;
}

.post-images {
  display: flex;
  gap: 5px;
  position: relative;
}

.post-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}

.more-images {
  position: absolute;
  top: 0;
  right: 0;
  width: 80px;
  height: 80px;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  font-size: 12px;
}

.post-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-stats {
  flex: 1;
}

.location {
  font-size: 12px;
  color: #999;
  display: flex;
  align-items: center;
  gap: 2px;
}

.post-actions {
  display: flex;
  gap: 10px;
}

.post-actions .van-button {
  padding: 0 8px;
  height: 28px;
  font-size: 12px;
}

.my-actions {
  display: flex;
  gap: 8px;
}

.my-actions .van-button {
  padding: 0 8px;
  height: 28px;
  font-size: 12px;
}
</style>
