<template>
  <div class="activity-participants">
    <!-- 导航栏 -->
    <van-nav-bar
      title="参与者管理"
      left-text="返回"
      left-arrow
      @click-left="goBack"
    />
    
    <!-- 活动信息 -->
    <div class="activity-info">
      <h3>{{ activity.title }}</h3>
      <p class="activity-meta">
        <span>待审核：{{ activity.pendingParticipantCount ?? 0 }}</span>
      </p>
    </div>
    
    <!-- 状态筛选 -->
    <van-tabs v-model:active="activeTab" @change="onTabChange">
      <van-tab title="待审核" name="PENDING">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <div v-if="participants.length === 0" class="empty-state">
              <van-empty description="暂无待审核的报名" />
            </div>
            <div v-else>
              <div
                v-for="participant in participants"
                :key="participant.id"
                class="participant-card"
              >
                <div class="participant-info">
                  <van-image
                    :src="participant.user.avatar || '/default-avatar.png'"
                    class="avatar"
                    round
                    fit="cover"
                  />
                  <div class="info">
                    <h4>{{ participant.user.nickname }}</h4>
                    <p class="note">{{ participant.applicationNote }}</p>
                    <p class="time">申请时间：{{ formatTime(participant.appliedAt) }}</p>
                  </div>
                </div>
                <div class="actions">
                  <van-button
                    type="success"
                    size="small"
                    @click="approveParticipant(participant.id)"
                  >
                    通过
                  </van-button>
                  <van-button
                    type="danger"
                    size="small"
                    @click="showRejectDialog(participant)"
                  >
                    拒绝
                  </van-button>
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </van-tab>
      
      <van-tab title="已通过" name="APPROVED">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <div v-if="participants.length === 0" class="empty-state">
              <van-empty description="暂无已通过的报名" />
            </div>
            <div v-else>
              <div
                v-for="participant in participants"
                :key="participant.id"
                class="participant-card approved"
              >
                <div class="participant-info">
                  <van-image
                    :src="participant.user.avatar || '/default-avatar.png'"
                    class="avatar"
                    round
                    fit="cover"
                  />
                  <div class="info">
                    <h4>{{ participant.user.nickname }}</h4>
                    <p class="note">{{ participant.applicationNote }}</p>
                    <p class="time">通过时间：{{ formatTime(participant.approvedAt) }}</p>
                  </div>
                </div>
                <div class="status-badge approved">
                  <van-icon name="success" />
                  已通过
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </van-tab>
      
      <van-tab title="已拒绝" name="REJECTED">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="onLoad"
          >
            <div v-if="participants.length === 0" class="empty-state">
              <van-empty description="暂无已拒绝的报名" />
            </div>
            <div v-else>
              <div
                v-for="participant in participants"
                :key="participant.id"
                class="participant-card rejected"
              >
                <div class="participant-info">
                  <van-image
                    :src="participant.user.avatar || '/default-avatar.png'"
                    class="avatar"
                    round
                    fit="cover"
                  />
                  <div class="info">
                    <h4>{{ participant.user.nickname }}</h4>
                    <p class="note">{{ participant.applicationNote }}</p>
                    <p class="rejection-reason">拒绝原因：{{ participant.rejectionReason }}</p>
                    <p class="time">申请时间：{{ formatTime(participant.appliedAt) }}</p>
                  </div>
                </div>
                <div class="status-badge rejected">
                  <van-icon name="close" />
                  已拒绝
                </div>
              </div>
            </div>
          </van-list>
        </van-pull-refresh>
      </van-tab>
    </van-tabs>
    
    <!-- 拒绝原因对话框 -->
    <van-dialog
      v-model:show="showRejectDialogRef"
      title="拒绝报名"
      show-cancel-button
      @confirm="confirmReject"
    >
      <van-field
        v-model="rejectReason"
        type="textarea"
        placeholder="请输入拒绝原因"
        rows="3"
        maxlength="200"
        show-word-limit
      />
    </van-dialog>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getActivity, getActivityParticipants, approveParticipant as approveParticipantApi, rejectParticipant } from '@/api/activities'

export default {
  name: 'ActivityParticipants',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    const activityId = route.params.id
    const activity = ref({})
    const participants = ref([])
    const activeTab = ref('PENDING')
    const loading = ref(false)
    const finished = ref(false)
    const refreshing = ref(false)
    const page = ref(0)
    const size = ref(10)
    
    // 拒绝相关
    const showRejectDialogRef = ref(false)
    const rejectReason = ref('')
    const currentRejectParticipant = ref(null)
    
    // 加载活动详情（用于标题与报名人数）
    const loadActivity = async () => {
      try {
        const res = await getActivity(activityId)
        activity.value = res
      } catch (e) {
        console.error('获取活动详情失败:', e)
        showToast('获取活动详情失败')
      }
    }
    
    // 获取参与者列表
    const loadParticipants = async (reset = false) => {
      if (reset) {
        page.value = 0
        participants.value = []
        finished.value = false
      }
      
      try {
        loading.value = true
        const response = await getActivityParticipants(activityId, {
          status: activeTab.value,
          page: page.value,
          size: size.value
        })
        
        if (reset) {
          participants.value = response.content
        } else {
          participants.value.push(...response.content)
        }
        
        if (response.content.length < size.value) {
          finished.value = true
        }
        
        page.value++
      } catch (error) {
        console.error('获取参与者列表失败:', error)
        showToast('获取参与者列表失败')
      } finally {
        loading.value = false
        refreshing.value = false
      }
    }
    
    // 下拉刷新
    const onRefresh = () => {
      loadActivity()
      loadParticipants(true)
    }
    
    // 上拉加载
    const onLoad = () => {
      loadParticipants()
    }
    
    // 切换标签
    const onTabChange = () => {
      loadParticipants(true)
    }
    
    // 通过报名
    const approveParticipant = async (participantId) => {
      try {
        await showConfirmDialog({
          title: '确认通过',
          message: '确定要通过这个报名申请吗？'
        })
        
        await approveParticipantApi(activityId, participantId)
        showToast('已通过报名')
        loadActivity()
        loadParticipants(true)
      } catch (error) {
        if (error !== 'cancel') {
          console.error('通过报名失败:', error)
          showToast('通过报名失败')
        }
      }
    }
    
    // 显示拒绝对话框
    const showRejectDialog = (participant) => {
      currentRejectParticipant.value = participant
      rejectReason.value = ''
      showRejectDialogRef.value = true
    }
    
    // 确认拒绝
    const confirmReject = async () => {
      if (!rejectReason.value.trim()) {
        showToast('请输入拒绝原因')
        return
      }
      
      try {
        await rejectParticipant(activityId, currentRejectParticipant.value.id, rejectReason.value)
        showToast('已拒绝报名')
        showRejectDialogRef.value = false
        loadActivity()
        loadParticipants(true)
      } catch (error) {
        console.error('拒绝报名失败:', error)
        showToast('拒绝报名失败')
      }
    }
    
    // 格式化时间
    const formatTime = (time) => {
      if (!time) return ''
      return new Date(time).toLocaleString()
    }
    
    // 返回
    const goBack = () => {
      router.back()
    }
    
    onMounted(() => {
      loadActivity()
      loadParticipants(true)
    })
    
    return {
      activity,
      participants,
      activeTab,
      loading,
      finished,
      refreshing,
      showRejectDialogRef,
      rejectReason,
      onRefresh,
      onLoad,
      onTabChange,
      approveParticipant,
      showRejectDialog,
      confirmReject,
      formatTime,
      goBack
    }
  }
}
</script>

<style scoped>
.activity-participants {
  background: #f5f5f5;
  min-height: 100vh;
}

.activity-info {
  background: white;
  padding: 16px;
  margin-bottom: 8px;
}

.activity-info h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: bold;
}

.activity-meta {
  display: flex;
  gap: 16px;
  margin: 0;
  color: #666;
  font-size: 14px;
}

.participant-card {
  background: white;
  margin: 8px 16px;
  padding: 16px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.participant-card.approved {
  border-left: 4px solid #52c41a;
}

.participant-card.rejected {
  border-left: 4px solid #ff4d4f;
}

.participant-info {
  display: flex;
  align-items: center;
  flex: 1;
}

.avatar {
  width: 48px;
  height: 48px;
  margin-right: 12px;
}

.info h4 {
  margin: 0 0 4px 0;
  font-size: 16px;
  font-weight: bold;
}

.info p {
  margin: 2px 0;
  font-size: 14px;
  color: #666;
}

.note {
  color: #333 !important;
  font-weight: 500;
}

.rejection-reason {
  color: #ff4d4f !important;
}

.actions {
  display: flex;
  gap: 8px;
}

.status-badge {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: bold;
}

.status-badge.approved {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.status-badge.rejected {
  background: #fff2f0;
  color: #ff4d4f;
  border: 1px solid #ffccc7;
}

.empty-state {
  padding: 40px 20px;
}
</style>
