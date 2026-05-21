<template>
  <div class="activity-card" @click="$emit('click')">
    <div class="activity-image">
      <div class="cover-image">
        <van-image
          v-if="activity.coverImage"
          :src="activity.coverImage"
          class="activity-image"
          fit="cover"
        />
        <van-image
          v-else
          src="/default-activity.png"
          class="activity-image"
          fit="cover"
        />
      </div>
      <div class="activity-status">
        <van-tag :type="getStatusType(activity.status)">
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
      
      <div class="activity-description">
        {{ activity.description }}
      </div>
      
      <div class="activity-footer">
        <div class="participants">
          <van-icon name="friends-o" />
          <span>{{ activity.currentParticipants || 0 }}/{{ activity.maxParticipants }}</span>
        </div>
        <div class="activity-actions">
          <van-button
            v-if="!activity.isJoined"
            type="primary"
            size="small"
            @click.stop="handleJoin"
          >
            报名
          </van-button>
          <van-button
            v-else
            type="default"
            size="small"
            @click.stop="handleCancel"
          >
            已报名
          </van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { joinActivity, cancelJoinActivity } from '@/api/activities'
import { showToast } from 'vant'

const props = defineProps({
  activity: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

// 获取状态类型
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

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'PENDING': '待审核',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'ONGOING': '进行中',
    'COMPLETED': '已结束',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || '未知'
}

// 获取类型文本
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

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const month = d.getMonth() + 1
  const day = d.getDate()
  const hour = d.getHours().toString().padStart(2, '0')
  const minute = d.getMinutes().toString().padStart(2, '0')
  return `${month}月${day}日 ${hour}:${minute}`
}

// 处理报名
const handleJoin = async () => {
  try {
    await joinActivity(props.activity.id)
    props.activity.isJoined = true
    props.activity.currentParticipants = (props.activity.currentParticipants || 0) + 1
    showToast('报名成功')
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data
    showToast(typeof msg === 'string' ? msg : '报名失败')
  }
}

// 处理取消报名
const handleCancel = async () => {
  try {
    await cancelJoinActivity(props.activity.id)
    props.activity.isJoined = false
    props.activity.currentParticipants = Math.max((props.activity.currentParticipants || 1) - 1, 0)
    showToast('取消报名成功')
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data
    showToast(typeof msg === 'string' ? msg : '取消报名失败')
  }
}
</script>

<style scoped>
.activity-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 15px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;
}

.activity-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.activity-image {
  position: relative;
  height: 120px;
}

.cover-image {
  width: 100%;
  height: 100%;
  position: relative;
}

.activity-image {
  width: 100%;
  height: 100%;
}

.default-cover {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.cover-icon {
  margin-bottom: 8px;
  opacity: 0.8;
}

.cover-text {
  font-size: 12px;
  opacity: 0.9;
}

.activity-status {
  position: absolute;
  top: 10px;
  right: 10px;
}

.activity-content {
  padding: 15px;
}

.activity-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.activity-title {
  font-size: 16px;
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
  margin-right: 5px;
  font-size: 14px;
}

.activity-description {
  font-size: 13px;
  color: #666;
  line-height: 1.4;
  margin-bottom: 15px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.activity-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.participants {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.participants .van-icon {
  margin-right: 5px;
  font-size: 14px;
}

.activity-actions .van-button {
  height: 28px;
  padding: 0 12px;
  font-size: 12px;
}
</style>