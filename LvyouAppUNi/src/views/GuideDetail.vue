<template>
  <div class="guide-detail page-nav-only">
    <!-- 导航栏 -->
    <van-nav-bar :title="guide?.title || '攻略详情'" fixed left-arrow @click-left="goBack" />
    <div class="nav-spacer"></div>

    <div v-if="loading" class="loading">
      <van-loading type="spinner" />
    </div>

    <div v-else-if="guide" class="guide-content">
      <!-- 攻略头部 -->
      <div class="guide-header">
        <h1 class="guide-title">{{ guide.title }}</h1>
        
        <!-- 作者信息 -->
        <div class="author-info">
          <van-image
            :src="guide.author.avatar || '/default-avatar.png'"
            class="author-avatar"
            round
            fit="cover"
          />
          <div class="author-details">
            <div class="author-name">{{ guide.author.nickname }}</div>
            <div class="publish-time">{{ formatTime(guide.createdAt) }}</div>
          </div>
          <div class="guide-type">{{ guide.typeDisplayName }}</div>
        </div>

        <!-- 统计信息 -->
        <div class="guide-stats">
          <div class="stat-item">
            <van-icon name="eye-o" />
            <span>{{ guide.viewCount }}</span>
          </div>
          <div class="stat-item">
            <van-icon name="good-job-o" />
            <span>{{ guide.likeCount }}</span>
          </div>
          <div class="stat-item">
            <van-icon name="chat-o" />
            <span>{{ guide.commentCount }}</span>
          </div>
        </div>
      </div>

      <!-- 攻略内容 -->
      <div class="guide-body">
        <div v-if="guide.coverImage" class="guide-cover">
          <van-image
            :src="guide.coverImage"
            class="cover-image"
            fit="cover"
          />
        </div>
        
        <div class="guide-content-text" v-html="guide.content"></div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-section">
        <van-button
          type="primary"
          :icon="guide.isLiked ? 'good-job' : 'good-job-o'"
          @click="toggleLike"
        >
          {{ guide.isLiked ? '已点赞' : '点赞' }} ({{ guide.likeCount }})
        </van-button>
        
        <van-button
          v-if="guide.isAuthor"
          type="warning"
          @click="editGuide"
        >
          编辑攻略
        </van-button>
      </div>

      <!-- 评论区域 -->
      <div class="comments-section">
        <div class="comments-header">
          <h3>评论 ({{ guide.commentCount }})</h3>
        </div>

        <!-- 评论列表 -->
        <van-list
          v-model:loading="commentsLoading"
          :finished="commentsFinished"
          finished-text="没有更多评论了"
          @load="loadComments"
        >
          <div v-if="comments.length === 0" class="empty-comments">
            <van-empty description="暂无评论" />
          </div>
          <div v-else>
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <div class="comment-content">
                <van-image
                  :src="comment.user.avatar || '/default-avatar.png'"
                  class="user-avatar"
                  round
                  fit="cover"
                />
                <div class="comment-body">
                  <div class="comment-header">
                    <span class="username">{{ comment.user.nickname }}</span>
                    <span class="time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <div class="comment-text">{{ comment.content }}</div>
                  <div class="comment-actions">
                    <van-button
                      type="primary"
                      size="mini"
                      @click="replyToComment(comment)"
                    >
                      回复
                    </van-button>
                    <van-button
                      type="primary"
                      size="mini"
                      @click="likeComment(comment.id)"
                    >
                      👍 {{ comment.likeCount || 0 }}
                    </van-button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </van-list>
      </div>
    </div>

    <!-- 评论输入框 -->
    <div class="comment-input">
      <van-field
        v-model="commentText"
        placeholder="写个评论..."
        type="textarea"
        rows="2"
        maxlength="1000"
        show-word-limit
        @keyup.enter="submitComment"
      />
      <van-button
        type="primary"
        size="small"
        @click="submitComment"
        :disabled="!commentText.trim()"
      >
        发送
      </van-button>
    </div>

    <!-- 回复对话框 -->
    <van-dialog
      v-model:show="showReplyDialog"
      title="回复评论"
      show-cancel-button
      @confirm="confirmReply"
    >
      <van-field
        v-model="replyText"
        placeholder="输入回复内容..."
        type="textarea"
        rows="3"
        maxlength="500"
        show-word-limit
      />
    </van-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { getApiErrorMessage, isPointsInsufficientError } from '@/utils/apiError'
import { getGuideById, toggleGuideLike, getGuideComments, createGuideComment, toggleCommentLike } from '@/api/guides'

export default {
  name: 'GuideDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // 响应式数据
    const guide = ref(null)
    const loading = ref(true)
    const comments = ref([])
    const commentsLoading = ref(false)
    const commentsFinished = ref(false)
    const commentText = ref('')
    const showReplyDialog = ref(false)
    const replyText = ref('')
    const replyingTo = ref(null)
    
    // 分页参数
    const pageParams = reactive({
      page: 0,
      size: 10
    })
    
    // 加载攻略详情
    const loadGuide = async () => {
      try {
        const response = await getGuideById(route.params.id)
        console.log('攻略详情响应:', response)
        guide.value = response
      } catch (error) {
        console.error('加载攻略详情失败:', error)
        showToast('加载攻略详情失败')
      } finally {
        loading.value = false
      }
    }
    
    // 加载评论
    const loadComments = async () => {
      if (commentsLoading.value) return
      
      commentsLoading.value = true
      
      try {
        const response = await getGuideComments(route.params.id, {
          page: pageParams.page,
          size: pageParams.size
        })
        console.log('评论API响应数据结构:', response)
        
        // Spring Data Page对象的结构
        const newComments = response.content || []
        
        if (pageParams.page === 0) {
          comments.value = newComments
        } else {
          comments.value.push(...newComments)
        }
        
        // Spring Data Page对象的分页信息
        commentsFinished.value = response.last || newComments.length < pageParams.size
        pageParams.page++
        
      } catch (error) {
        console.error('加载评论失败:', error)
        showToast('加载评论失败')
      } finally {
        commentsLoading.value = false
      }
    }
    
    // 点赞/取消点赞
    const toggleLike = async () => {
      try {
        await toggleGuideLike(guide.value.id)
        guide.value.isLiked = !guide.value.isLiked
        guide.value.likeCount += guide.value.isLiked ? 1 : -1
        showToast(guide.value.isLiked ? '点赞成功' : '取消点赞')
      } catch (error) {
        console.error('点赞失败:', error)
        showToast('点赞失败')
      }
    }
    
    // 提交评论
    const submitComment = async () => {
      if (!commentText.value.trim()) return
      
      try {
        await createGuideComment(guide.value.id, {
          content: commentText.value.trim()
        })
        
        commentText.value = ''
        showToast('评论成功')
        
        // 刷新评论列表
        pageParams.page = 0
        commentsFinished.value = false
        loadComments()
        
        // 更新评论数
        guide.value.commentCount++
        
      } catch (error) {
        console.error('评论失败:', error)
        const msg = getApiErrorMessage(error, '评论失败')
        if (isPointsInsufficientError(error)) {
          showDialog({
            title: '积分不足',
            message: msg,
            confirmButtonText: '知道了',
          })
        } else {
          showToast(msg)
        }
      }
    }
    
    // 回复评论
    const replyToComment = (comment) => {
      replyingTo.value = comment
      showReplyDialog.value = true
    }
    
    // 确认回复
    const confirmReply = async () => {
      if (!replyText.value.trim()) return
      
      try {
        await createGuideComment(guide.value.id, {
          content: replyText.value.trim(),
          parentId: replyingTo.value.id
        })
        
        replyText.value = ''
        showReplyDialog.value = false
        replyingTo.value = null
        showToast('回复成功')
        
        // 刷新评论列表
        pageParams.page = 0
        commentsFinished.value = false
        loadComments()
        
      } catch (error) {
        console.error('回复失败:', error)
        const msg = getApiErrorMessage(error, '回复失败')
        if (isPointsInsufficientError(error)) {
          showDialog({
            title: '积分不足',
            message: msg,
            confirmButtonText: '知道了',
          })
        } else {
          showToast(msg)
        }
      }
    }
    
    // 点赞评论
    const likeComment = async (commentId) => {
      try {
        await toggleCommentLike(commentId)
        showToast('操作成功')
        
        // 刷新评论列表
        pageParams.page = 0
        commentsFinished.value = false
        loadComments()
        
      } catch (error) {
        console.error('点赞评论失败:', error)
        showToast('操作失败')
      }
    }
    
    // 编辑攻略
    const editGuide = () => {
      router.push(`/guides/${guide.value.id}/edit`)
    }
    
    // 返回
    const goBack = () => {
      router.back()
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
      loadGuide()
      loadComments()
    })
    
    return {
      guide,
      loading,
      comments,
      commentsLoading,
      commentsFinished,
      commentText,
      showReplyDialog,
      replyText,
      loadComments,
      toggleLike,
      submitComment,
      replyToComment,
      confirmReply,
      likeComment,
      editGuide,
      goBack,
      formatTime
    }
  }
}
</script>

<style scoped>
.guide-detail {
  padding-top: 0;
  background: var(--page-bg);
  min-height: 100vh;
  padding-bottom: 80px; /* 为底部输入框留出空间 */
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.guide-content {
  background: white;
  margin: 8px;
  border-radius: 8px;
  overflow: hidden;
}

.guide-header {
  padding: 20px;
  border-bottom: 1px solid #eee;
}

.guide-title {
  font-size: 20px;
  font-weight: bold;
  color: #333;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.author-info {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.author-avatar {
  width: 40px;
  height: 40px;
  margin-right: 12px;
}

.author-details {
  flex: 1;
}

.author-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.publish-time {
  font-size: 12px;
  color: #999;
}

.guide-type {
  background: #f0f0f0;
  color: #666;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.guide-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
}

.guide-body {
  padding: 20px;
}

.guide-cover {
  margin-bottom: 16px;
}

.cover-image {
  width: 100%;
  border-radius: 8px;
}

.guide-content-text {
  font-size: 16px;
  line-height: 1.6;
  color: #333;
}

.action-section {
  padding: 16px 20px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 12px;
}

.comments-section {
  margin-top: 8px;
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.comments-header {
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.comments-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.empty-comments {
  padding: 40px 20px;
}

.comment-item {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-content {
  display: flex;
  gap: 12px;
}

.user-avatar {
  width: 32px;
  height: 32px;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  color: #333;
  font-size: 14px;
}

.time {
  color: #999;
  font-size: 12px;
}

.comment-text {
  color: #333;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

.comment-input {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  padding: 12px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 8px;
  align-items: flex-end;
  z-index: 100;
}

.comment-input .van-field {
  flex: 1;
}
</style>
