<template>
  <div class="my-activities">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="我的活动" fixed>
      <template #left>
        <van-icon name="arrow-left" @click="goBack" />
      </template>
      <template #right>
        <van-icon name="plus" size="18" @click="goToCreateActivity" />
      </template>
    </van-nav-bar>

    <!-- 活动统计信息 -->
    <div class="stats-section">
      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-number">{{ totalActivities }}</div>
          <div class="stat-label">总活动数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ approvedActivities }}</div>
          <div class="stat-label">已通过</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ pendingActivities }}</div>
          <div class="stat-label">待审核</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ totalParticipants }}</div>
          <div class="stat-label">总参与人数</div>
        </div>
      </div>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <van-tabs v-model:active="activeTab" @change="handleTabChange">
        <van-tab title="我创建的" name="created" />
        <van-tab title="我参加的" name="participated" />
        <van-tab title="待审核" name="PENDING" />
        <van-tab title="已通过" name="APPROVED" />
        <van-tab title="进行中" name="ONGOING" />
        <van-tab title="已完成" name="COMPLETED" />
      </van-tabs>
    </div>

    <!-- 活动列表 -->
    <div class="activities-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadMyActivities"
        >
          <div
            v-for="activity in activities"
            :key="activity.id"
            class="activity-item"
            @click="goToActivityDetail(activity.id)"
          >
            <div class="activity-image">
              <van-image
                v-if="activity.coverImage"
                :src="activity.coverImage"
                class="cover-image"
                fit="cover"
              />
              <div v-else class="default-cover">
                <van-icon name="calendar-o" size="24" />
              </div>
              <div class="status-badge">
                <van-tag :type="getStatusType(activity.status)" size="small">
                  {{ getStatusText(activity.status) }}
                </van-tag>
              </div>
            </div>
            
            <div class="activity-content">
              <div class="activity-header">
                <h3 class="activity-title">{{ activity.title }}</h3>
                <div class="activity-type">
                  <van-tag type="primary" size="small">
                    {{ getTypeText(activity.type) }}
                  </van-tag>
                  <!-- 显示参与状态 -->
                  <van-tag 
                    v-if="activeTab === 'participated' && activity.participationStatus"
                    :type="getParticipationStatusType(activity.participationStatus)"
                    size="small"
                    style="margin-left: 8px;"
                  >
                    {{ getParticipationStatusText(activity.participationStatus) }}
                  </van-tag>
                </div>
              </div>
              
              <div class="activity-info">
                <div class="info-item">
                  <van-icon name="location-o" />
                  <span>{{ activity.destination }}</span>
                </div>
                <div class="info-item">
                  <van-icon name="clock-o" />
                  <span>{{ formatDate(activity.startTime) }}</span>
                </div>
                <div class="info-item" v-if="activity.cost">
                  <van-icon name="gold-coin-o" />
                  <span>¥{{ activity.cost }}</span>
                </div>
              </div>
              
              <div class="activity-stats">
                <div class="stat">
                  <van-icon name="friends-o" />
                  <span>{{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants }}</span>
                </div>
                <div class="stat">
                  <van-icon name="eye-o" />
                  <span>{{ activity.viewCount || 0 }}</span>
                </div>
                <div class="stat">
                  <van-icon name="like-o" />
                  <span>{{ activity.likeCount || 0 }}</span>
                </div>
              </div>
              
              <div class="activity-actions">
                <!-- 我参加的活动操作 -->
                <template v-if="activeTab === 'participated'">
                  <van-button
                    v-if="activity.participationStatus === 'PENDING'"
                    type="warning"
                    size="small"
                    @click.stop="showParticipationPendingTip"
                  >
                    等待审核
                  </van-button>
                  <van-button
                    v-else-if="activity.participationStatus === 'APPROVED'"
                    type="success"
                    size="small"
                    @click.stop="goToActivityDetail(activity.id)"
                  >
                    已通过
                  </van-button>
                  <van-button
                    v-else-if="activity.participationStatus === 'REJECTED'"
                    type="danger"
                    size="small"
                    @click.stop="showParticipationRejectedTip"
                  >
                    已拒绝
                  </van-button>
                  <van-button
                    v-else
                    type="primary"
                    size="small"
                    @click.stop="goToActivityDetail(activity.id)"
                  >
                    查看详情
                  </van-button>
                </template>
                
                <!-- 我创建的活动操作 -->
                <template v-else>
                  <van-button
                    v-if="activity.status === 'PENDING'"
                    type="warning"
                    size="small"
                    @click.stop="showPendingTip"
                  >
                    等待审核
                  </van-button>
                  <van-button
                    v-else-if="activity.status === 'APPROVED'"
                    type="success"
                    size="small"
                    @click.stop="showApprovedTip"
                  >
                    已通过
                  </van-button>
                  <van-button
                    v-else-if="activity.status === 'ONGOING'"
                    type="primary"
                    size="small"
                    @click.stop="goToActivityDetail(activity.id)"
                  >
                    查看详情
                  </van-button>
                  <van-button
                    v-else-if="activity.status === 'COMPLETED'"
                    type="default"
                    size="small"
                    @click.stop="goToActivityDetail(activity.id)"
                  >
                    已完成
                  </van-button>
                </template>
              </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 空状态 -->
    <div class="empty-state" v-if="!loading && activities.length === 0">
      <van-empty
        image="search"
        description="还没有发布活动"
      >
        <van-button type="primary" @click="goToCreateActivity">
          发布第一个活动
        </van-button>
      </van-empty>
    </div>

    <!-- 创建活动按钮 -->
    <van-floating-bubble
      axis="xy"
      icon="plus"
      magnetic="x"
      @click="goToCreateActivity"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getMyActivities, getMyParticipations } from '@/api/activities'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const activities = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const activeTab = ref('all')
const page = ref(0)
const pageSize = ref(10)

// 统计信息
const totalActivities = ref(0)
const approvedActivities = ref(0)
const pendingActivities = ref(0)
const totalParticipants = ref(0)

// 计算属性
const filteredActivities = computed(() => {
  if (activeTab.value === 'all') {
    return activities.value
  }
  return activities.value.filter(activity => activity.status === activeTab.value)
})

// 方法
const goBack = () => {
  router.back()
}

const goToCreateActivity = () => {
  router.push('/activities/create')
}

const goToActivityDetail = (id) => {
  router.push(`/activities/${id}`)
}

const handleTabChange = (name) => {
  activeTab.value = name
  resetActivities()
  loadMyActivities()
}

const resetActivities = () => {
  activities.value = []
  page.value = 0
  finished.value = false
}

const onRefresh = () => {
  resetActivities()
  loadMyActivities().finally(() => {
    refreshing.value = false
  })
}

const loadMyActivities = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    
    let response
    
    if (activeTab.value === 'participated') {
      // 加载我参加的活动
      console.log('加载我参加的活动参数:', params)
      response = await getMyParticipations(params)
      console.log('我参加的活动API响应:', response)
    } else {
      // 加载我创建的活动
      // 添加状态筛选
      if (activeTab.value !== 'created' && activeTab.value !== 'all') {
        params.status = activeTab.value
      }
      
      console.log('加载我创建的活动参数:', params)
      response = await getMyActivities(params)
      console.log('我创建的活动API响应:', response)
    }
    
    if (page.value === 0) {
      activities.value = response.content || []
    } else {
      activities.value.push(...(response.content || []))
    }
    
    // 更新统计信息
    updateStats()
    
    page.value++
    finished.value = !response.content || response.content.length < pageSize.value
  } catch (error) {
    console.error('加载我的活动失败:', error)
    showToast('加载失败，请重试')
  } finally {
    loading.value = false
  }
}

const updateStats = () => {
  totalActivities.value = activities.value.length
  approvedActivities.value = activities.value.filter(a => a.status === 'APPROVED').length
  pendingActivities.value = activities.value.filter(a => a.status === 'PENDING').length
  totalParticipants.value = activities.value.reduce((sum, a) => sum + (a.currentParticipants || 0), 0)
}

// 状态相关方法
const getStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'ONGOING': 'primary',
    'COMPLETED': 'default',
    'CANCELLED': 'danger'
  }
  return statusMap[status] || 'default'
}

const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'ONGOING': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || '未知'
}

// 参与状态相关方法
const getParticipationStatusType = (status) => {
  const statusMap = {
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return statusMap[status] || 'default'
}

const getParticipationStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getTypeText = (type) => {
  const typeMap = {
    'HIKING': '徒步',
    'PHOTOGRAPHY': '摄影',
    'CAMPING': '露营',
    'CULTURAL': '文化',
    'ADVENTURE': '探险',
    'RELAXATION': '休闲',
    'OTHER': '其他'
  }
  return typeMap[type] || '其他'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = d.getHours().toString().padStart(2, '0')
  const minute = d.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

const showPendingTip = () => {
  showToast('活动正在审核中，请耐心等待')
}

const showApprovedTip = () => {
  showToast('活动已通过审核，可以开始报名了')
}

const showParticipationPendingTip = () => {
  showToast('您的参与申请正在审核中，请耐心等待')
}

const showParticipationRejectedTip = () => {
  showToast('您的参与申请已被拒绝')
}

onMounted(() => {
  loadMyActivities()
})
</script>

<style scoped>
.my-activities {
  padding: 0;
  padding-top: 0vw;
  background-color: #f5f5f5;
  min-height: 100vh;
}

/* 统计信息卡片 */
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
.filter-section {
  background: white;
  position: sticky;
  top: 46px;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 8px;
}

/* 活动列表 */
.activities-list {
  padding: 10px;
}

.activity-item {
  background: white;
  border-radius: 12px;
  margin-bottom: 10px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
  cursor: pointer;
}

.activity-item:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.08);
}

.activity-image {
  position: relative;
  height: 112px;
}

.cover-image {
  width: 100%;
  height: 100%;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.status-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.activity-content {
  padding: 14px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.activity-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0;
  flex: 1;
  margin-right: 10px;
  line-height: 1.4;
}

.activity-info {
  margin-bottom: 10px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
  font-size: 12px;
  color: #666;
}

.info-item .van-icon {
  margin-right: 6px;
  font-size: 14px;
}

.activity-stats {
  display: flex;
  gap: 16px;
  margin-bottom: 10px;
}

.stat {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.stat .van-icon {
  margin-right: 4px;
  font-size: 14px;
}

.activity-actions {
  display: flex;
  justify-content: flex-end;
}

/* 空状态 */
.empty-state {
  padding: 60px 20px;
  text-align: center;
}

/* 浮动按钮 */
:deep(.van-floating-bubble) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* 标签样式 */
:deep(.van-tabs__line) {
  background-color: #1989fa;
}

:deep(.van-tag--primary) {
  background-color: #667eea;
}

:deep(.van-tag--success) {
  background-color: #52c41a;
}

:deep(.van-tag--warning) {
  background-color: #faad14;
}

:deep(.van-tag--danger) {
  background-color: #ff4d4f;
}
</style>
