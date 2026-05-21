<template>
  <div class="activity-chat page-nav-only">
    <van-nav-bar title="活动聊天室" fixed left-arrow @click-left="goBack" />
    <div class="nav-spacer"></div>

    <div v-if="isCheckingPermission" class="state-block">
      <van-loading vertical>正在检查聊天室权限...</van-loading>
    </div>

    <div v-else-if="!canAccessChat" class="state-block">
      <van-empty description="您暂时无法进入此聊天室" />
      <van-button type="primary" round @click="goBack">返回活动详情</van-button>
    </div>

    <template v-else>
      <div v-if="activityInfo" class="chat-activity-card">
        <div class="activity-title">{{ activityInfo.title }}</div>
        <div class="activity-meta">
          <span><van-icon name="location-o" /> {{ activityInfo.destination || '地点待定' }}</span>
          <span><van-icon name="clock-o" /> {{ formatActivityTime(activityInfo.startTime) }}</span>
        </div>
        <div class="activity-meta">
          <span><van-icon name="friends-o" /> {{ activityInfo.currentParticipants || 0 }}/{{ activityInfo.maxParticipants || '-' }} 人</span>
          <span><van-icon name="manager-o" /> {{ activityInfo.organizer?.nickname || activityInfo.organizer?.username || '组织者' }}</span>
        </div>
      </div>

      <div v-if="organizerNotice" class="notice-card organizer-notice">
        <div class="notice-title">
          <van-icon name="volume-o" />
          <span>活动公告</span>
        </div>
        <div class="notice-content">{{ organizerNotice }}</div>
      </div>

      <div v-if="systemMessages.length" class="system-notice-list">
        <div
          v-for="notice in systemMessages"
          :key="notice.id"
          class="notice-card system-notice"
        >
          <div class="notice-title">
            <van-icon name="flag-o" />
            <span>系统通知</span>
          </div>
          <div class="notice-content">{{ notice.content }}</div>
          <div class="notice-time">{{ formatTime(notice.createdAt) }}</div>
        </div>
      </div>

      <div class="shortcut-row">
        <van-button
          v-for="shortcut in quickShortcuts"
          :key="shortcut"
          plain
          size="mini"
          class="shortcut-btn"
          @click="applyShortcut(shortcut)"
        >
          {{ shortcut }}
        </van-button>
      </div>

      <div ref="chatContainer" class="chat-container" @scroll="handleScroll">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <div v-if="loading && comments.length === 0" class="state-block compact">
            <van-loading vertical>正在加载聊天记录...</van-loading>
          </div>

          <div v-else-if="comments.length === 0" class="state-block compact">
            <van-empty description="暂无聊天记录">
              <template #description>
                <p>可以先发一句“我到了”</p>
              </template>
            </van-empty>
          </div>

          <div v-else class="messages-list">
            <div
              v-for="(comment, index) in comments"
              :key="comment.id"
              class="message-item"
            >
              <div v-if="shouldShowTimeDivider(comment, index)" class="time-divider">
                <span class="time-text">{{ formatDate(comment.createdAt) }}</span>
              </div>

              <div class="message-row" :class="{ self: isSelf(comment) }">
                <van-image
                  :src="getAvatar(comment.user)"
                  class="user-avatar"
                  round
                  fit="cover"
                />

                <div class="message-card" :class="{ self: isSelf(comment) }">
                  <div class="message-header">
                    <span class="username">{{ isSelf(comment) ? '我' : (comment.user?.nickname || comment.user?.username || '用户') }}</span>
                    <span class="message-time">{{ formatTime(comment.createdAt) }}</span>
                  </div>
                  <div class="message-content">{{ comment.content }}</div>
                  <div class="message-actions" :class="{ self: isSelf(comment) }">
                    <van-button plain size="mini" icon="chat-o" @click="replyToComment(comment)">回复</van-button>
                    <van-button
                      plain
                      size="mini"
                      :type="comment.isLiked ? 'primary' : 'default'"
                      :icon="comment.isLiked ? 'good-job' : 'good-job-o'"
                      @click="handleLike(comment.id)"
                    >
                      {{ comment.likeCount || 0 }}
                    </van-button>
                  </div>

                  <div v-if="comment.replies && comment.replies.length" class="replies-container">
                    <div
                      v-for="reply in comment.replies"
                      :key="reply.id"
                      class="reply-bubble"
                      :class="{ self: isSelf(reply) }"
                    >
                      <div class="reply-header">
                        <span class="reply-username">{{ isSelf(reply) ? '我' : (reply.user?.nickname || reply.user?.username || '用户') }}</span>
                        <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                      </div>
                      <div class="reply-content">{{ reply.content }}</div>
                      <div class="reply-actions">
                        <van-button
                          plain
                          size="mini"
                          :type="reply.isLiked ? 'primary' : 'default'"
                          :icon="reply.isLiked ? 'good-job' : 'good-job-o'"
                          @click="handleLike(reply.id)"
                        >
                          {{ reply.likeCount || 0 }}
                        </van-button>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </van-pull-refresh>

        <div v-if="unreadCount > 0" class="new-message-tip" @click="scrollToLatest">
          有 {{ unreadCount }} 条新消息，点击查看
        </div>
      </div>

      <div class="input-section">
        <div v-if="replyTarget" class="reply-preview">
          <div class="reply-preview-text">
            正在回复 {{ replyTarget.user?.nickname || replyTarget.user?.username || '用户' }}：{{ replyTarget.content }}
          </div>
          <van-icon name="cross" size="16" @click="cancelReply" />
        </div>

        <div class="input-box">
          <van-field
            v-model="inputMessage"
            :placeholder="replyTarget ? '输入回复内容...' : '说点什么...'"
            type="textarea"
            rows="1"
            maxlength="1000"
            autosize
            class="message-input"
          />
          <van-button
            type="primary"
            size="small"
            :loading="sending"
            :disabled="!inputMessage.trim()"
            class="send-button"
            @click="sendMessage"
          >
            发送
          </van-button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showDialog } from 'vant'
import { getApiErrorMessage, isPointsInsufficientError } from '@/utils/apiError'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'
import { useUserStore } from '@/stores/user'
import { getActivity, checkParticipation } from '@/api/activities'
import { createComment, getActivityComments, likeComment as likeCommentApi } from '@/api/comments'
import { getFullAvatarUrl } from '@/utils/avatar'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activityId = route.params.id
const WS_URL = 'http://localhost:8081/ws'

const activityInfo = ref(null)
const comments = ref([])
const loading = ref(false)
const refreshing = ref(false)
const sending = ref(false)
const canAccessChat = ref(false)
const isCheckingPermission = ref(true)
const inputMessage = ref('')
const replyTarget = ref(null)
const unreadCount = ref(0)
const chatContainer = ref(null)
const quickShortcuts = ['我到了', '我会晚到10分钟', '集合点在哪里？', '有人一起拼车吗？']
const systemMessages = ref([])

let stompClient = null

const currentUserId = computed(() => userStore.userInfo?.id ?? null)
const organizerNotice = computed(() => {
  if (!activityInfo.value) return ''

  const notices = []
  if (activityInfo.value.startTime) {
    notices.push(`请于 ${formatActivityTime(activityInfo.value.startTime)} 准时集合`)
  }
  if (activityInfo.value.destination) {
    notices.push(`集合地点：${activityInfo.value.destination}`)
  }
  if (activityInfo.value.requirements) {
    notices.push(`参与要求：${activityInfo.value.requirements}`)
  }
  if (activityInfo.value.contactInfo) {
    notices.push(`联系组织者：${activityInfo.value.contactInfo}`)
  }

  return notices.join('；')
})

const ensureUserInfo = async () => {
  if (!userStore.userInfo && userStore.isLoggedIn) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取当前用户信息失败:', error)
    }
  }
}

const getAvatar = (user) => {
  return getFullAvatarUrl(user?.avatar) || '/default-avatar.png'
}

const isSelf = (comment) => {
  return !!currentUserId.value && currentUserId.value === comment?.user?.id
}

const sortByCreatedAtAsc = (list = []) => {
  return [...list].sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
}

const normalizeComment = (comment) => {
  return {
    ...comment,
    likeCount: comment.likeCount || 0,
    isLiked: !!comment.isLiked,
    replies: sortByCreatedAtAsc((comment.replies || []).map(item => normalizeComment(item))),
  }
}

const isNearBottom = () => {
  const el = chatContainer.value
  if (!el) return true
  return el.scrollHeight - el.scrollTop - el.clientHeight < 120
}

const scrollToBottom = (smooth = true) => {
  const el = chatContainer.value
  if (!el) return
  el.scrollTo({
    top: el.scrollHeight,
    behavior: smooth ? 'smooth' : 'auto',
  })
  unreadCount.value = 0
}

const scrollToLatest = () => {
  scrollToBottom()
}

const pushSystemMessage = (content) => {
  if (!content) return
  const notice = {
    id: `system-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
    content,
    createdAt: new Date().toISOString(),
  }
  systemMessages.value = [notice, ...systemMessages.value].slice(0, 8)
}

const replaceReplyInList = (list, reply) => {
  const index = list.findIndex(item => item.id === reply.id)
  if (index > -1) {
    const cloned = [...list]
    cloned[index] = { ...cloned[index], ...reply }
    return sortByCreatedAtAsc(cloned)
  }
  return sortByCreatedAtAsc([...list, reply])
}

const mergeIncomingComment = (incoming, options = {}) => {
  if (!incoming?.id) return
  const normalized = normalizeComment(incoming)
  const keepBottom = options.forceScroll || isNearBottom()

  if (normalized.parentId) {
    let parentFound = false
    comments.value = comments.value.map(comment => {
      if (comment.id !== normalized.parentId) return comment
      parentFound = true
      return {
        ...comment,
        replies: replaceReplyInList(comment.replies || [], normalized),
      }
    })
    if (!parentFound) return
  } else {
    const existingIndex = comments.value.findIndex(item => item.id === normalized.id)
    if (existingIndex > -1) {
      const next = [...comments.value]
      const existing = next[existingIndex]
      next[existingIndex] = {
        ...existing,
        ...normalized,
        replies: normalized.replies?.length
          ? sortByCreatedAtAsc(normalized.replies)
          : (existing.replies || []),
      }
      comments.value = sortByCreatedAtAsc(next)
    } else {
      comments.value = sortByCreatedAtAsc([...comments.value, normalized])
    }
  }

  nextTick(() => {
    if (keepBottom) {
      scrollToBottom(options.forceScroll)
    } else if (!options.fromSelf) {
      unreadCount.value += 1
    }
  })
}

const updateCommentLikeLocally = (commentId) => {
  comments.value = comments.value.map(comment => {
    if (comment.id === commentId) {
      return {
        ...comment,
        likeCount: (comment.likeCount || 0) + 1,
        isLiked: true,
      }
    }

    const replies = (comment.replies || []).map(reply => {
      if (reply.id !== commentId) return reply
      return {
        ...reply,
        likeCount: (reply.likeCount || 0) + 1,
        isLiked: true,
      }
    })

    return {
      ...comment,
      replies,
    }
  })
}

const loadActivityInfo = async () => {
  try {
    activityInfo.value = await getActivity(activityId)
  } catch (error) {
    console.error('加载活动信息失败:', error)
  }
}

const loadComments = async () => {
  try {
    loading.value = true
    const response = await getActivityComments(activityId, {
      page: 0,
      size: 50,
    })
    comments.value = sortByCreatedAtAsc((response.content || []).map(item => normalizeComment(item)))
    nextTick(() => {
      scrollToBottom(false)
    })
  } catch (error) {
    console.error('获取评论失败:', error)
    showToast(error.message || '获取评论失败')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = async () => {
  await loadComments()
}

const checkChatAccess = async () => {
  try {
    isCheckingPermission.value = true
    const response = await checkParticipation(activityId)
    canAccessChat.value = !!response?.canAccessChat
    if (!canAccessChat.value) {
      showToast('您没有权限访问此聊天室，请先报名并通过审核')
    }
  } catch (error) {
    console.error('检查聊天室权限失败:', error)
    showToast(error.message || '检查聊天室权限失败')
    canAccessChat.value = false
  } finally {
    isCheckingPermission.value = false
  }
}

const applyShortcut = (shortcut) => {
  inputMessage.value = inputMessage.value.trim()
    ? `${inputMessage.value}\n${shortcut}`
    : shortcut
}

const replyToComment = (comment) => {
  replyTarget.value = {
    id: comment.id,
    content: comment.content,
    user: comment.user,
  }
}

const cancelReply = () => {
  replyTarget.value = null
}

const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content) {
    showToast('请输入消息内容')
    return
  }

  const isReply = !!replyTarget.value

  try {
    sending.value = true
    const created = await createComment(activityId, {
      content,
      parentId: replyTarget.value?.id,
    })
    mergeIncomingComment(created, { fromSelf: true, forceScroll: true })
    inputMessage.value = ''
    replyTarget.value = null
    showToast(isReply ? '回复成功' : '发送成功')
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
  } finally {
    sending.value = false
  }
}

const handleLike = async (commentId) => {
  try {
    await likeCommentApi(commentId)
    updateCommentLikeLocally(commentId)
  } catch (error) {
    console.error('点赞失败:', error)
    showToast(error.message || '点赞失败')
  }
}

const initWebSocket = () => {
  try {
    stompClient = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      reconnectDelay: 5000,
      debug: () => {},
      onConnect: () => {
        stompClient.subscribe(`/topic/activity/${activityId}/comments`, (message) => {
          try {
            const incoming = JSON.parse(message.body)
            mergeIncomingComment(incoming)
          } catch (error) {
            console.error('解析实时消息失败:', error)
          }
        })

        stompClient.subscribe(`/topic/activity/${activityId}/system`, (message) => {
          if (message.body) {
            pushSystemMessage(message.body)
            showToast(message.body)
          }
        })
      },
      onStompError: (error) => {
        console.error('STOMP错误:', error)
      },
      onWebSocketError: (error) => {
        console.error('WebSocket错误:', error)
      },
    })
    stompClient.activate()
  } catch (error) {
    console.error('初始化WebSocket失败:', error)
  }
}

const disconnectWebSocket = () => {
  if (stompClient) {
    stompClient.deactivate()
    stompClient = null
  }
}

const handleScroll = () => {
  if (isNearBottom()) {
    unreadCount.value = 0
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const isToday = date.toDateString() === now.toDateString()
  if (isToday) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return `${date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })} ${date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })}`
}

const formatDate = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const today = new Date()
  const yesterday = new Date(today)
  yesterday.setDate(yesterday.getDate() - 1)

  if (date.toDateString() === today.toDateString()) return '今天'
  if (date.toDateString() === yesterday.toDateString()) return '昨天'
  return date.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
}

const formatActivityTime = (time) => {
  if (!time) return '时间待定'
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

const shouldShowTimeDivider = (comment, index) => {
  if (index === 0) return true
  return new Date(comment.createdAt).toDateString() !== new Date(comments.value[index - 1].createdAt).toDateString()
}

const goBack = () => {
  router.back()
}

onMounted(async () => {
  await ensureUserInfo()
  await checkChatAccess()
  if (!canAccessChat.value) return
  await Promise.all([loadActivityInfo(), loadComments()])
  initWebSocket()
})

onUnmounted(() => {
  disconnectWebSocket()
})
</script>

<style scoped>
.activity-chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  overflow: hidden;
  background: #f6f7fb;
}

.state-block {
  flex: 1;
  min-height: 50vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 20px;
}

.state-block.compact {
  min-height: 260px;
}

.chat-activity-card {
  margin: 12px 12px 0;
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, #2f77ff 0%, #4f8dff 100%);
  color: #fff;
  box-shadow: 0 10px 24px rgba(47, 119, 255, 0.16);
}

.notice-card {
  margin: 12px 12px 0;
  padding: 12px 14px;
  border-radius: 14px;
  box-shadow: 0 8px 20px rgba(15, 23, 42, 0.06);
}

.organizer-notice {
  background: linear-gradient(180deg, #fff8e7 0%, #fffdf7 100%);
  border: 1px solid rgba(255, 196, 87, 0.35);
}

.system-notice-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.system-notice {
  background: linear-gradient(180deg, #eef6ff 0%, #f8fbff 100%);
  border: 1px solid rgba(47, 119, 255, 0.2);
}

.notice-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #9a6700;
}

.system-notice .notice-title {
  color: #2f77ff;
}

.notice-content {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.7;
  color: #475569;
  white-space: pre-wrap;
}

.notice-time {
  margin-top: 8px;
  font-size: 11px;
  color: #94a3b8;
}

.activity-title {
  font-size: 17px;
  font-weight: 600;
  line-height: 1.5;
}

.activity-meta {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.9);
}

.activity-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.shortcut-row {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 12px 12px 4px;
}

.shortcut-btn {
  flex-shrink: 0;
  border-radius: 999px;
}

.chat-container {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px 12px calc(120px + env(safe-area-inset-bottom));
}

.messages-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.time-divider {
  text-align: center;
  margin: 14px 0 4px;
}

.time-text {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.14);
  color: #94a3b8;
  font-size: 12px;
}

.message-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.message-row.self {
  flex-direction: row-reverse;
}

.user-avatar {
  width: 38px;
  height: 38px;
  flex-shrink: 0;
}

.message-card {
  max-width: calc(100% - 56px);
  padding: 12px;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 2px 10px rgba(15, 23, 42, 0.06);
}

.message-card.self {
  background: linear-gradient(135deg, #e9f3ff 0%, #f2f8ff 100%);
  border: 1px solid rgba(47, 119, 255, 0.08);
}

.message-header,
.reply-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.username,
.reply-username {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
}

.message-time,
.reply-time {
  font-size: 11px;
  color: #94a3b8;
  white-space: nowrap;
}

.message-content,
.reply-content {
  margin-top: 8px;
  font-size: 14px;
  line-height: 1.7;
  color: #1e293b;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-actions,
.reply-actions {
  display: flex;
  gap: 8px;
  margin-top: 10px;
}

.message-actions.self {
  justify-content: flex-end;
}

.replies-container {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid #eef2f7;
}

.reply-bubble {
  padding: 10px;
  border-radius: 12px;
  background: #f8fafc;
}

.reply-bubble.self {
  background: #edf5ff;
}

.new-message-tip {
  position: sticky;
  bottom: 12px;
  margin: 0 auto;
  width: fit-content;
  padding: 8px 14px;
  border-radius: 999px;
  background: #2f77ff;
  color: #fff;
  font-size: 12px;
  box-shadow: 0 8px 20px rgba(47, 119, 255, 0.24);
}

.input-section {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 3;
  background: #fff;
  border-top: 1px solid #eef2f7;
  padding: 10px 12px calc(10px + env(safe-area-inset-bottom));
  box-shadow: 0 -6px 20px rgba(15, 23, 42, 0.08);
}

.reply-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
  padding: 8px 10px;
  border-radius: 12px;
  background: #f5f8ff;
  color: #3b82f6;
}

.reply-preview-text {
  flex: 1;
  font-size: 12px;
  line-height: 1.5;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.input-box {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}

.message-input {
  flex: 1;
  border-radius: 18px;
  background: #f8fafc;
  padding: 8px 12px;
}

.message-input :deep(.van-field__control) {
  min-height: 22px;
  font-size: 14px;
  line-height: 1.5;
}

.send-button {
  height: 38px;
  padding: 0 16px;
  border-radius: 19px;
}

.van-button--mini {
  height: 26px;
  padding: 0 10px;
  border-radius: 999px;
}

.chat-container::-webkit-scrollbar {
  width: 4px;
}

.chat-container::-webkit-scrollbar-thumb {
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.45);
}
</style>
