<template>
  <div class="comment-list">
    <!-- 积分显示 -->
    <PointsDisplay
      v-if="showPointsInfo"
      :show-details="false"
      :low-points-threshold="1"
      @points-insufficient="handlePointsInsufficient"
      @earn-points="handleEarnPoints"
      ref="pointsDisplayRef"
    />

    <!-- 评论输入框 -->
    <div class="comment-input-section">
      <div class="comment-input-wrapper">
        <div class="input-header">
          <span class="input-title">发表评论</span>
          <span class="comment-count">{{ totalComments }} 条评论</span>
        </div>
        <van-field
          v-model="newComment"
          placeholder="说点什么..."
          type="textarea"
          autosize
          maxlength="200"
          show-word-limit
          class="comment-field"
          :border="false"
        />
        <div class="input-actions">
          <van-button
            type="primary"
            size="small"
            :loading="submitting"
            :disabled="!newComment.trim()"
            @click="submitComment"
            class="submit-btn"
          >
            发送
          </van-button>
        </div>
      </div>
    </div>

    <!-- 评论列表 -->
    <div class="comments-container">
      <div v-if="loading" class="loading-wrapper">
        <van-loading size="20" />
        <span>加载评论中...</span>
      </div>
      
      <div v-else-if="comments.length === 0" class="empty-comments">
        <van-icon name="chat-o" size="40" />
        <p>还没有评论，来抢沙发吧~</p>
      </div>
      
      <div v-else class="comments-list">
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
          :class="{ 'comment-liked': comment.isLiked }"
        >
          <div class="comment-avatar">
            <van-image
              v-if="comment.user?.avatar"
              :src="getUserAvatarUrl(comment.user)"
              class="avatar-image"
              round
              fit="cover"
            />
            <van-icon v-else name="user-o" size="20" class="avatar-icon" />
          </div>
          
          <div class="comment-content">
            <div class="comment-header">
              <div class="author-info">
                <span class="comment-author">{{ comment.user?.nickname || comment.user?.username }}</span>
                <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
              </div>
              <van-button
                v-if="canDeleteComment(comment)"
                icon="delete-o"
                size="mini"
                type="danger"
                plain
                @click="handleDeleteComment(comment)"
                :loading="comment.deleting"
                class="delete-btn"
              >
                删除
              </van-button>
            </div>
            
            <div class="comment-text">{{ comment.content }}</div>
            
            <div class="comment-actions">
              <van-button
                :icon="comment.isLiked ? 'like' : 'like-o'"
                :type="comment.isLiked ? 'danger' : 'default'"
                size="mini"
                plain
                @click="toggleCommentLike(comment)"
                :loading="comment.liking"
                class="like-btn"
              >
                <span v-if="comment.likeCount > 0">{{ comment.likeCount }}</span>
              </van-button>
              
              <van-button
                icon="chat-o"
                size="mini"
                type="default"
                plain
                class="reply-btn"
                @click="showReplyInput(comment)"
              >
                回复
              </van-button>
            </div>
            
            <!-- 二级评论区域 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies-section">
              <div class="replies-toggle" @click="toggleReplies(comment)">
                <van-icon :name="comment.showReplies ? 'arrow-up' : 'arrow-down'" />
                <span>{{ comment.showReplies ? '收起' : '展开' }} {{ comment.replies.length }} 条回复</span>
              </div>
              
              <div v-if="comment.showReplies" class="replies-list">
                <div
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  class="reply-item"
                  :class="{ 'comment-liked': reply.isLiked }"
                >
                  <div class="reply-avatar">
                    <van-image
                      v-if="reply.user?.avatar"
                      :src="getUserAvatarUrl(reply.user)"
                      class="avatar-image"
                      round
                      fit="cover"
                    />
                    <van-icon v-else name="user-o" size="16" class="avatar-icon" />
                  </div>
                  
                  <div class="reply-content">
                    <div class="reply-header">
                      <div class="author-info">
                        <span class="reply-author">{{ reply.user?.nickname || reply.user?.username }}</span>
                        <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                      </div>
                      <van-button
                        v-if="canDeleteComment(reply)"
                        icon="delete-o"
                        size="mini"
                        type="danger"
                        plain
                        @click="handleDeleteComment(reply)"
                        :loading="reply.deleting"
                        class="delete-btn"
                      >
                        删除
                      </van-button>
                    </div>
                    
                    <div class="reply-text">{{ reply.content }}</div>
                    
                    <div class="reply-actions">
                      <van-button
                        :icon="reply.isLiked ? 'like' : 'like-o'"
                        :type="reply.isLiked ? 'danger' : 'default'"
                        size="mini"
                        plain
                        @click="toggleCommentLike(reply)"
                        :loading="reply.liking"
                        class="like-btn"
                      >
                        <span v-if="reply.likeCount > 0">{{ reply.likeCount }}</span>
                      </van-button>
                      
                      <van-button
                        icon="chat-o"
                        size="mini"
                        type="default"
                        plain
                        class="reply-btn"
                        @click="showReplyInput(comment, reply)"
                      >
                        回复
                      </van-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
            
            <!-- 回复输入框 -->
            <div v-if="comment.showReplyInput" class="reply-input-section">
              <van-field
                v-model="comment.replyText"
                placeholder="回复 @{{ comment.replyToUser }}..."
                type="textarea"
                autosize
                maxlength="200"
                show-word-limit
                class="reply-field"
                :border="false"
              />
              <div class="reply-input-actions">
                <van-button
                  size="mini"
                  @click="cancelReply(comment)"
                >
                  取消
                </van-button>
                <van-button
                  type="primary"
                  size="mini"
                  :loading="comment.replying"
                  :disabled="!comment.replyText.trim()"
                  @click="submitReply(comment)"
                >
                  发送
                </van-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore && !loading" class="load-more">
      <van-button
        type="default"
        size="small"
        @click="loadMoreComments"
        :loading="loadingMore"
      >
        加载更多评论
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { getActivityComments, createComment, deleteComment, likeComment, unlikeComment, getTopLevelComments, getReplies, addComment, getPostCommentsWithStats } from '@/api/comments'
import { getUserAvatarUrl } from '@/utils/avatar'
import { showToast, showDialog } from 'vant'
import { getApiErrorMessage, isPointsInsufficientError } from '@/utils/apiError'
import PointsDisplay from './PointsDisplay.vue'

const props = defineProps({
  postId: {
    type: [String, Number],
    required: true
  },
  showPointsInfo: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['comment-added', 'comment-deleted', 'points-insufficient'])

const userStore = useUserStore()

// 响应式数据
const comments = ref([])
const newComment = ref('')
const loading = ref(false)
const loadingMore = ref(false)
const submitting = ref(false)
const page = ref(1)
const pageSize = ref(10)
const hasMore = ref(true)
const totalComments = ref(0)
const topLevelComments = ref(0)
const secondLevelComments = ref(0)

// 计算属性
const canDeleteComment = (comment) => {
  return userStore.userInfo?.id === comment.user?.id || userStore.isAdmin
}

// 方法
const loadComments = async (reset = false) => {
  if (reset) {
    page.value = 1
    comments.value = []
    hasMore.value = true
  }
  
  if (loading.value || loadingMore.value) return
  
  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }
  
  try {
    console.log('开始加载评论，postId:', props.postId, 'page:', page.value)
    const response = await getPostCommentsWithStats(props.postId, {
      page: page.value - 1, // 前端从1开始，后端从0开始
      size: pageSize.value
    })
    
    console.log('评论API响应:', response)
    const responseData = response.data || response
    
    // 更新统计信息
    totalComments.value = responseData.totalComments || 0
    topLevelComments.value = responseData.topLevelComments || 0
    secondLevelComments.value = responseData.secondLevelComments || 0
    
    const newComments = responseData.comments || []
    console.log('解析的评论数据:', newComments)
    
    // 为每个评论设置UI状态
    for (const comment of newComments) {
      comment.showReplies = false
      comment.showReplyInput = false
      comment.replyText = ''
      comment.replying = false
      comment.replyToUser = ''
      
      // 为每个回复也设置UI状态
      if (comment.replies) {
        for (const reply of comment.replies) {
          reply.showReplies = false
          reply.showReplyInput = false
          reply.replyText = ''
          reply.replying = false
          reply.replyToUser = ''
        }
      }
    }
    
    if (reset) {
      comments.value = newComments
    } else {
      comments.value.push(...newComments)
    }
    
    console.log('最终评论列表:', comments.value)
    hasMore.value = responseData.hasMore || false
    page.value++
    
  } catch (error) {
    console.error('加载评论失败:', error)
    showToast('加载评论失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMoreComments = () => {
  loadComments(false)
}

const submitComment = async () => {
  if (!newComment.value.trim()) {
    showToast('请输入评论内容')
    return
  }
  
  submitting.value = true
  
  try {
    const response = await addComment(props.postId, {
      content: newComment.value.trim()
    })
    
    // 将新评论添加到列表顶部
    comments.value.unshift(response.data || response)
    newComment.value = ''
    
    showToast('评论成功')
    emit('comment-added', response.data || response)
    
  } catch (error) {
    console.error('评论失败:', error)
    const msg = getApiErrorMessage(error, '评论失败')
    if (isPointsInsufficientError(error)) {
      const extra = error.response?.data?.data
      showDialog({
        title: '积分不足',
        message: msg,
        confirmButtonText: '知道了',
      })
      emit('points-insufficient', {
        message: msg,
        requiredPoints: extra?.requiredPoints ?? 1,
        currentPoints: extra?.currentPoints,
        shortage: extra?.shortage,
      })
    } else {
      showToast(msg)
    }
  } finally {
    submitting.value = false
  }
}

// 处理积分不足
const handlePointsInsufficient = (data) => {
  console.log('积分不足:', data)
  emit('points-insufficient', data)
}

// 处理获取积分
const handleEarnPoints = () => {
  console.log('用户选择获取积分')
  // 可以跳转到积分获取页面或显示积分获取方式
  showToast('请通过发布内容来获取积分')
}

const handleDeleteComment = async (comment) => {
  try {
    await deleteComment(comment.id)
    
    // 从列表中移除
    const index = comments.value.findIndex(c => c.id === comment.id)
    if (index > -1) {
      comments.value.splice(index, 1)
    }
    
    showToast('删除成功')
    emit('comment-deleted', comment)
    
  } catch (error) {
    console.error('删除评论失败:', error)
    showToast(error.response?.data?.message || '删除失败')
  }
}

const toggleCommentLike = async (comment) => {
  if (comment.liking) return
  
  comment.liking = true
  
  try {
    if (comment.isLiked) {
      await unlikeComment(comment.id)
      comment.isLiked = false
      comment.likeCount = Math.max(0, (comment.likeCount || 0) - 1)
    } else {
      await likeComment(comment.id)
      comment.isLiked = true
      comment.likeCount = (comment.likeCount || 0) + 1
    }
  } catch (error) {
    console.error('操作失败:', error)
    showToast('操作失败')
  } finally {
    comment.liking = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  
  const now = new Date()
  const commentTime = new Date(time)
  const diff = now - commentTime
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return commentTime.toLocaleDateString()
}

// 层级评论相关方法
const toggleReplies = (comment) => {
  comment.showReplies = !comment.showReplies
}

const showReplyInput = (comment, replyTo = null) => {
  // 关闭其他评论的回复输入框
  comments.value.forEach(c => {
    if (c.id !== comment.id) {
      c.showReplyInput = false
    }
  })
  
  comment.showReplyInput = true
  comment.replyText = ''
  comment.replying = false
  
  if (replyTo) {
    comment.replyToUser = replyTo.user?.nickname || replyTo.user?.username
  } else {
    comment.replyToUser = comment.user?.nickname || comment.user?.username
  }
}

const cancelReply = (comment) => {
  comment.showReplyInput = false
  comment.replyText = ''
  comment.replyToUser = ''
}

const submitReply = async (comment) => {
  if (!comment.replyText.trim()) {
    showToast('请输入回复内容')
    return
  }
  
  comment.replying = true
  
  try {
    const response = await addComment(props.postId, {
      content: comment.replyText,
      parentId: comment.id
    })
    
    // 添加到回复列表
    const newReply = response.data || response
    if (!comment.replies) {
      comment.replies = []
    }
    comment.replies.push(newReply)
    
    // 重置输入框
    comment.replyText = ''
    comment.showReplyInput = false
    comment.replyToUser = ''
    
    showToast('回复成功')
    emit('comment-added', newReply)
    
  } catch (error) {
    console.error('回复失败:', error)
    const msg = getApiErrorMessage(error, '回复失败')
    if (isPointsInsufficientError(error)) {
      const extra = error.response?.data?.data
      showDialog({
        title: '积分不足',
        message: msg,
        confirmButtonText: '知道了',
      })
      emit('points-insufficient', {
        message: msg,
        requiredPoints: extra?.requiredPoints ?? 1,
        currentPoints: extra?.currentPoints,
        shortage: extra?.shortage,
      })
    } else {
      showToast(msg)
    }
  } finally {
    comment.replying = false
  }
}

onMounted(() => {
  loadComments(true)
})
</script>

<style scoped>
.comment-list {
  background: white;
}

.comment-input-section {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-input-wrapper {
  background: #f8f9fa;
  border-radius: 12px;
  margin-bottom: 16px;
  overflow: hidden;
}

.input-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px 8px;
  border-bottom: 1px solid #e9ecef;
}

.input-title {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.comment-count {
  font-size: 12px;
  color: #999;
}

.comment-field {
  padding: 12px 16px;
  border: none;
  background: transparent;
}

.input-actions {
  display: flex;
  justify-content: flex-end;
  padding: 8px 16px 12px;
  border-top: 1px solid #e9ecef;
}

.submit-btn {
  height: 32px;
  padding: 0 20px;
  border-radius: 16px;
}

.comments-container {
  min-height: 200px;
}

.loading-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 40px;
  color: #999;
}

.empty-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-comments p {
  margin: 12px 0 0;
  font-size: 14px;
}

.comments-list {
  padding: 0;
}

.comment-item {
  display: flex;
  padding: 16px;
  border-bottom: 1px solid #f8f9fa;
  gap: 12px;
  transition: all 0.2s ease;
  position: relative;
}

.comment-item:hover {
  background: #f8f9fa;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-liked {
  background: #fff5f5;
}

.comment-avatar {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
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

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.author-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.comment-author {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.delete-btn {
  opacity: 0.7;
  transition: opacity 0.2s ease;
}

.delete-btn:hover {
  opacity: 1;
}

.comment-text {
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  margin-bottom: 8px;
  word-break: break-word;
}

.comment-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.like-btn {
  transition: all 0.2s ease;
}

.like-btn:hover {
  transform: scale(1.05);
}

.reply-btn {
  transition: all 0.2s ease;
}

.reply-btn:hover {
  transform: scale(1.05);
}

/* 二级评论样式 */
.replies-section {
  margin-top: 8px;
  border-left: 2px solid #f0f0f0;
  padding-left: 12px;
}

.replies-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 0;
  color: #666;
  font-size: 12px;
  cursor: pointer;
  transition: color 0.2s ease;
}

.replies-toggle:hover {
  color: #333;
}

.replies-list {
  margin-top: 8px;
}

.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
  border-bottom: 1px solid #f8f9fa;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-avatar {
  flex-shrink: 0;
  width: 24px;
  height: 24px;
}

.reply-content {
  flex: 1;
  min-width: 0;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 4px;
}

.reply-author {
  font-weight: 500;
  font-size: 13px;
  color: #333;
}

.reply-time {
  font-size: 11px;
  color: #999;
}

.reply-text {
  font-size: 13px;
  line-height: 1.4;
  color: #333;
  margin-bottom: 6px;
  word-break: break-word;
}

.reply-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.reply-input-section {
  margin-top: 8px;
  padding: 8px;
  background: #f8f9fa;
  border-radius: 8px;
}

.reply-field {
  margin-bottom: 8px;
}

.reply-input-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.load-more {
  padding: 16px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-item {
    padding: 10px 12px;
  }
  
  .comment-avatar {
    width: 28px;
    height: 28px;
  }
  
  .comment-text {
    font-size: 13px;
  }
}
</style>
