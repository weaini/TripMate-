<template>
  <div class="activity-chat">
    <!-- 导航栏 -->
    <van-nav-bar
      title="活动聊天室"
      left-text="返回"
      left-arrow
      @click-left="goBack"
    />
    
    <!-- 权限检查中 -->
    <div v-if="isCheckingPermission" class="permission-check">
      <van-loading type="spinner" size="24px" />
      <p>正在检查权限...</p>
    </div>
    
    <!-- 无权限访问 -->
    <div v-else-if="!canAccessChat" class="no-permission">
      <van-empty description="您没有权限访问此聊天室" />
      <van-button type="primary" @click="goBack">返回</van-button>
    </div>
    
    <!-- 聊天消息列表 -->
    <div v-else class="chat-container" ref="chatContainer">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadComments"
        >
          <div v-if="comments.length === 0" class="empty-state">
            <van-empty description="暂无聊天记录" />
          </div>
          <div v-else>
            <div
              v-for="comment in comments"
              :key="comment.id"
              class="comment-item"
            >
              <!-- 评论内容 -->
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
                      👍 {{ comment.likeCount }}
                    </van-button>
                  </div>
                </div>
              </div>
              
              <!-- 回复列表 -->
              <div v-if="comment.replies && comment.replies.length > 0" class="replies">
                <div
                  v-for="reply in comment.replies"
                  :key="reply.id"
                  class="reply-item"
                >
                  <van-image
                    :src="reply.user.avatar || '/default-avatar.png'"
                    class="reply-avatar"
                    round
                    fit="cover"
                  />
                  <div class="reply-body">
                    <div class="reply-header">
                      <span class="username">{{ reply.user.nickname }}</span>
                      <span class="time">{{ formatTime(reply.createdAt) }}</span>
                    </div>
                    <div class="reply-text">{{ reply.content }}</div>
                    <div class="reply-actions">
                      <van-button
                        type="primary"
                        size="mini"
                        @click="likeComment(reply.id)"
                      >
                        👍 {{ reply.likeCount }}
                      </van-button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
    
    <!-- 输入框 -->
    <div v-if="canAccessChat" class="input-section">
      <van-field
        v-model="inputMessage"
        placeholder="输入消息..."
        type="textarea"
        rows="2"
        maxlength="1000"
        show-word-limit
        @keyup.enter="sendMessage"
      />
      <van-button
        type="primary"
        size="small"
        @click="sendMessage"
        :disabled="!inputMessage.trim()"
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
        v-model="replyMessage"
        type="textarea"
        placeholder="输入回复内容"
        rows="3"
        maxlength="1000"
        show-word-limit
      />
    </van-dialog>
  </div>
</template>

<script>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { getApiErrorMessage, isPointsInsufficientError } from '@/utils/apiError'
import { getActivityComments, createComment, likeComment as likeCommentApi } from '@/api/comments'
import { checkParticipation } from '@/api/activities'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

export default {
  name: 'ActivityChat',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const activityId = route.params.id
    const comments = ref([])
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const page = ref(0)
    const size = ref(20)
    const canAccessChat = ref(false)
    const isCheckingPermission = ref(true)
    
    // 输入相关
    const inputMessage = ref('')
    const showReplyDialog = ref(false)
    const replyMessage = ref('')
    const currentReplyComment = ref(null)
    
    // WebSocket相关
    let stompClient = null
    let chatContainer = null
    
    // 检查聊天室访问权限
    const checkChatAccess = async () => {
      try {
        isCheckingPermission.value = true
        const response = await checkParticipation(activityId)
        canAccessChat.value = response.canAccessChat
        
        if (!canAccessChat.value) {
          showToast('您没有权限访问此聊天室，请先报名参加活动')
          router.back()
          return false
        }
        return true
      } catch (error) {
        console.error('检查权限失败:', error)
        showToast('检查权限失败')
        router.back()
        return false
      } finally {
        isCheckingPermission.value = false
      }
    }
    
    // 获取评论列表
    const loadComments = async (reset = false) => {
      if (reset) {
        page.value = 0
        comments.value = []
        finished.value = false
      }
      
      try {
        loading.value = true
        const response = await getActivityComments(activityId, {
          page: page.value,
          size: size.value
        })
        
        if (reset) {
          comments.value = response.content
        } else {
          comments.value.push(...response.content)
        }
        
        if (response.content.length < size.value) {
          finished.value = true
        }
        
        page.value++
        
        // 滚动到底部
        if (reset) {
          nextTick(() => {
            scrollToBottom()
          })
        }
      } catch (error) {
        console.error('获取评论失败:', error)
        showToast('获取评论失败')
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }
    
    // 下拉刷新
    const onRefresh = () => {
      loadComments(true)
    }
    
    // 发送消息
    const sendMessage = async () => {
      if (!inputMessage.value.trim()) {
        showToast('请输入消息内容')
        return
      }
      
      try {
        await createComment(activityId, {
          content: inputMessage.value.trim()
        })
        inputMessage.value = ''
        showToast('发送成功')
      } catch (error) {
        console.error('发送消息失败:', error)
        const msg = getApiErrorMessage(error, '发送失败')
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
      currentReplyComment.value = comment
      replyMessage.value = ''
      showReplyDialog.value = true
    }
    
    // 确认回复
    const confirmReply = async () => {
      if (!replyMessage.value.trim()) {
        showToast('请输入回复内容')
        return
      }
      
      try {
        await createComment(activityId, {
          content: replyMessage.value.trim(),
          parentId: currentReplyComment.value.id
        })
        showReplyDialog.value = false
        showToast('回复成功')
        loadComments(true)
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
        await likeCommentApi(commentId)
        showToast('点赞成功')
        loadComments(true)
      } catch (error) {
        console.error('点赞失败:', error)
        showToast('点赞失败')
      }
    }
    
    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString()
    }
    
    // 滚动到底部
    const scrollToBottom = () => {
      if (chatContainer) {
        chatContainer.scrollTop = chatContainer.scrollHeight
      }
    }
    
    // 初始化WebSocket
    const initWebSocket = () => {
      try {
        const socket = new SockJS('/ws')
        stompClient = new Client({
          webSocketFactory: () => socket,
          debug: (str) => {
            console.log('STOMP Debug:', str)
          },
          onConnect: () => {
            console.log('WebSocket连接成功')
            
            // 订阅活动评论消息
            stompClient.subscribe(`/topic/activity/${activityId}/comments`, (message) => {
              try {
                const comment = JSON.parse(message.body)
                comments.value.unshift(comment)
                nextTick(() => {
                  scrollToBottom()
                })
              } catch (error) {
                console.error('解析消息失败:', error)
              }
            })
            
            // 订阅系统消息
            stompClient.subscribe(`/topic/activity/${activityId}/system`, (message) => {
              showToast(message.body)
            })
          },
          onStompError: (error) => {
            console.error('STOMP错误:', error)
          },
          onWebSocketError: (error) => {
            console.error('WebSocket错误:', error)
          }
        })
        
        stompClient.activate()
      } catch (error) {
        console.error('WebSocket初始化失败:', error)
      }
    }
    
    // 断开WebSocket连接
    const disconnectWebSocket = () => {
      if (stompClient) {
        stompClient.deactivate()
        stompClient = null
      }
    }
    
    // 返回
    const goBack = () => {
      router.back()
    }
    
    onMounted(async () => {
      // 先检查权限
      const hasAccess = await checkChatAccess()
      if (hasAccess) {
        loadComments(true)
        initWebSocket()
      }
    })
    
    onUnmounted(() => {
      disconnectWebSocket()
    })
    
    return {
      comments,
      loading,
      finished,
      refreshing,
      inputMessage,
      showReplyDialog,
      replyMessage,
      canAccessChat,
      isCheckingPermission,
      onRefresh,
      loadComments,
      sendMessage,
      replyToComment,
      confirmReply,
      likeComment,
      formatTime,
      goBack
    }
  }
}
</script>

<style scoped>
.activity-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5f5f5;
}

.chat-container {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
}

.empty-state {
  padding: 40px 20px;
}

.comment-item {
  background: white;
  margin-bottom: 10px;
  border-radius: 8px;
  padding: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.1);
}

.comment-content {
  display: flex;
  gap: 10px;
}

.user-avatar {
  width: 40px;
  height: 40px;
  flex-shrink: 0;
}

.comment-body {
  flex: 1;
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.username {
  font-weight: bold;
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

.replies {
  margin-top: 10px;
  padding-left: 20px;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-avatar {
  width: 30px;
  height: 30px;
  flex-shrink: 0;
}

.reply-body {
  flex: 1;
}

.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3px;
}

.reply-text {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

.reply-actions {
  margin-top: 5px;
}

.input-section {
  background: white;
  padding: 10px;
  border-top: 1px solid #eee;
  display: flex;
  gap: 10px;
  align-items: flex-end;
}

.input-section .van-field {
  flex: 1;
}

.permission-check {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #666;
}

.permission-check p {
  margin-top: 10px;
  font-size: 14px;
}

.no-permission {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  padding: 20px;
}

.no-permission .van-button {
  margin-top: 20px;
}
</style>
