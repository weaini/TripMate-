<template>
  <div class="points-display">
    <van-cell-group v-if="showDetails" :title="title">
      <van-cell>
        <template #title>
          <div class="points-info">
            <van-icon name="gold-coin" class="points-icon" />
            <span class="points-label">当前积分</span>
          </div>
        </template>
        <template #value>
          <div class="points-value">
            <span class="current-points">{{ currentPoints }}</span>
            <van-tag v-if="isLowPoints" type="warning" size="small" class="low-points-tag">
              积分不足
            </van-tag>
          </div>
        </template>
      </van-cell>
      
      <van-cell v-if="showEarnPoints" title="获取积分">
        <template #value>
          <div class="earn-points-info">
            <div class="earn-item">
              <span class="earn-action">发布帖子</span>
              <span class="earn-points">+10</span>
            </div>
            <div class="earn-item">
              <span class="earn-action">发布攻略</span>
              <span class="earn-points">+10</span>
            </div>
            <div class="earn-item">
              <span class="earn-action">发布活动</span>
              <span class="earn-points">+10</span>
            </div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>
    
    <!-- 简化显示模式 -->
    <div v-else class="points-simple">
      <van-icon name="gold-coin" class="points-icon" />
      <span class="points-text">{{ currentPoints }}</span>
      <van-tag v-if="isLowPoints" type="warning" size="small" class="low-points-tag">
        不足
      </van-tag>
    </div>
    
    <!-- 积分不足提醒 -->
    <van-dialog
      v-model:show="showInsufficientDialog"
      title="积分不足"
      :message="insufficientMessage"
      show-cancel-button
      confirm-button-text="去获取积分"
      cancel-button-text="取消"
      @confirm="handleEarnPoints"
      @cancel="showInsufficientDialog = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  // 是否显示详细信息
  showDetails: {
    type: Boolean,
    default: false
  },
  // 标题
  title: {
    type: String,
    default: '积分信息'
  },
  // 是否显示获取积分方式
  showEarnPoints: {
    type: Boolean,
    default: true
  },
  // 积分不足阈值
  lowPointsThreshold: {
    type: Number,
    default: 5
  }
})

const emit = defineEmits(['earn-points', 'points-insufficient'])

const userStore = useUserStore()

// 响应式数据
const showInsufficientDialog = ref(false)
const insufficientMessage = ref('')

// 计算属性
const currentPoints = computed(() => {
  return userStore.userInfo?.points || 0
})

const isLowPoints = computed(() => {
  return currentPoints.value < props.lowPointsThreshold
})

// 监听积分变化
watch(currentPoints, (newPoints, oldPoints) => {
  if (newPoints < props.lowPointsThreshold && oldPoints >= props.lowPointsThreshold) {
    // 积分从足够变为不足
    showPointsInsufficientDialog('您的积分不足，无法进行某些操作')
  }
}, { immediate: false })

// 显示积分不足对话框
const showPointsInsufficientDialog = (message) => {
  insufficientMessage.value = message
  showInsufficientDialog.value = true
  emit('points-insufficient', {
    message,
    currentPoints: currentPoints.value,
    threshold: props.lowPointsThreshold
  })
}

// 处理获取积分
const handleEarnPoints = () => {
  showInsufficientDialog.value = false
  emit('earn-points')
}

// 暴露方法给父组件
defineExpose({
  showPointsInsufficientDialog,
  currentPoints: currentPoints.value,
  isLowPoints: isLowPoints.value
})
</script>

<style scoped>
.points-display {
  width: 100%;
}

.points-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.points-icon {
  color: #ffd700;
  font-size: 16px;
}

.points-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.points-value {
  display: flex;
  align-items: center;
  gap: 8px;
}

.current-points {
  font-size: 16px;
  font-weight: 600;
  color: #1989fa;
}

.low-points-tag {
  margin-left: 4px;
}

.earn-points-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.earn-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
}

.earn-action {
  color: #666;
}

.earn-points {
  color: #4caf50;
  font-weight: 600;
}

/* 简化显示模式 */
.points-simple {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  background: #f5f5f5;
  border-radius: 12px;
  font-size: 12px;
}

.points-text {
  color: #333;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 480px) {
  .points-simple {
    font-size: 11px;
    padding: 3px 6px;
  }
  
  .points-icon {
    font-size: 14px;
  }
}
</style>
