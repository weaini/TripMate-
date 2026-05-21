<template>
  <div class="points-records page-nav-only">
    <van-nav-bar
      title="积分明细"
      fixed
      left-arrow
      @click-left="$router.back()"
    />
    <div class="nav-spacer"></div>

    <!-- 当前积分与等级 -->
    <div class="points-summary">
      <div class="summary-main">
        <div class="summary-left">
          <van-icon name="gold-coin" class="summary-icon" />
          <div class="summary-texts">
            <span class="summary-label">当前积分</span>
            <span class="summary-value">{{ currentPoints }}</span>
          </div>
        </div>
        <div class="level-badge">Lv.{{ currentLevel.level }}</div>
      </div>

      <div class="level-card">
        <div class="level-row">
          <div>
            <div class="level-title">{{ currentLevel.title }}</div>
            <div class="level-subtitle">
              {{ nextLevel ? `距离 Lv.${nextLevel.level} 还差 ${pointsToNextLevel} 分` : '已达到当前最高等级' }}
            </div>
          </div>
          <div class="level-side">
            <div class="level-range">{{ currentLevel.min }} - {{ currentLevel.maxText }}</div>
            <van-button plain size="mini" class="level-guide-btn" @click="showLevelGuide = true">
              等级说明
            </van-button>
          </div>
        </div>

        <div class="level-progress">
          <div class="level-progress-bar" :style="{ width: `${levelProgress}%` }"></div>
        </div>

        <div class="level-progress-meta">
          <span>成长进度 {{ levelProgress }}%</span>
          <span v-if="nextLevel">下一等级：Lv.{{ nextLevel.level }}</span>
          <span v-else>满级</span>
        </div>
      </div>
    </div>

    <!-- 积分记录列表 -->
    <div class="records-section">
      <div class="section-title">积分流水</div>
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多了"
          @load="loadRecords"
        >
          <div v-if="records.length === 0 && !loading" class="empty-state">
            <van-empty description="暂无积分记录">
              <template #description>
                <p>发布动态、攻略或活动可获得积分</p>
                <p>评论等操作会消耗积分</p>
              </template>
            </van-empty>
          </div>
          <div
            v-for="item in records"
            :key="item.id"
            class="record-item"
            :class="{ earn: item.points > 0, spend: item.points < 0 }"
          >
            <div class="record-main">
              <span class="record-desc">{{ typeText(item) }}</span>
              <span class="record-points" :class="{ earn: item.points > 0, spend: item.points < 0 }">
                {{ item.points > 0 ? '+' : '' }}{{ item.points }}
              </span>
            </div>
            <div class="record-meta">
              <span class="record-time">{{ formatTime(item.createdAt) }}</span>
            </div>
            <div v-if="item.description" class="record-detail">{{ item.description }}</div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <van-popup v-model:show="showLevelGuide" position="bottom" round>
      <div class="level-guide-popup">
        <div class="level-guide-header">
          <div class="level-guide-title">等级说明</div>
          <van-icon name="cross" size="18" @click="showLevelGuide = false" />
        </div>
        <div class="level-guide-desc">
          等级由当前积分实时计算，不需要单独存储。积分越高，等级越高。
        </div>
        <div class="level-guide-list">
          <div
            v-for="rule in LEVEL_RULES"
            :key="rule.level"
            class="level-guide-item"
            :class="{ active: rule.level === currentLevel.level }"
          >
            <div>
              <div class="level-guide-item-title">Lv.{{ rule.level }} {{ rule.title }}</div>
              <div class="level-guide-item-range">{{ rule.min }} - {{ Number.isFinite(rule.max) ? rule.max : '∞' }} 分</div>
            </div>
            <van-tag v-if="rule.level === currentLevel.level" type="primary" plain>当前</van-tag>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getPointsRecords } from '@/api/user'
import { LEVEL_RULES, getLevelInfo } from '@/utils/pointsLevel'

const userStore = useUserStore()
const records = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(0)
const pageSize = 10
const showLevelGuide = ref(false)

const currentPoints = computed(() => userStore.userInfo?.points ?? 0)
const levelInfo = computed(() => getLevelInfo(currentPoints.value))
const currentLevel = computed(() => levelInfo.value.currentLevel)
const nextLevel = computed(() => levelInfo.value.nextLevel)
const pointsToNextLevel = computed(() => levelInfo.value.pointsToNextLevel)
const levelProgress = computed(() => levelInfo.value.levelProgress)

const typeText = (item) => {
  const typeMap = {
    EARN: '获得积分',
    SPEND: '消费积分',
    REFUND: '退还积分',
    ADMIN_ADJUST: '管理员调整',
  }
  if (item.description) return item.description
  return typeMap[item.type] || item.type || '积分变动'
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const sameDay = d.toDateString() === now.toDateString()
  if (sameDay) {
    return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  return d.toLocaleDateString('zh-CN') + ' ' + d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const loadRecords = async () => {
  if (loading.value) return
  loading.value = true
  try {
    const res = await getPointsRecords({
      page: page.value,
      size: pageSize,
      sort: 'createdAt,desc',
    })
    const content = res.content || []
    if (page.value === 0) {
      records.value = content
    } else {
      records.value.push(...content)
    }
    page.value++
    finished.value = !content.length || content.length < pageSize
  } catch (e) {
    console.error('加载积分记录失败:', e)
    finished.value = true
  } finally {
    loading.value = false
  }
}

const onRefresh = async () => {
  page.value = 0
  records.value = []
  finished.value = false
  await loadRecords()
  refreshing.value = false
}

// 进入页面主动加载第一页，避免 van-list 首屏不触发 @load 导致一直 loading
onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.points-records {
  min-height: 100vh;
  background: var(--page-bg);
}

.nav-spacer {
  height: 46px;
}

.points-summary {
  margin: var(--page-padding-lg);
  padding: 20px;
  background: linear-gradient(135deg, #1989fa 0%, #0d6efd 100%);
  border-radius: 16px;
  color: #fff;
  box-shadow: 0 10px 24px rgba(13, 110, 253, 0.22);
}

.summary-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.summary-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.summary-texts {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.summary-label {
  font-size: 14px;
  opacity: 0.9;
}

.summary-icon {
  font-size: 28px;
}

.summary-value {
  font-size: 24px;
  font-weight: 600;
}

.level-badge {
  flex-shrink: 0;
  padding: 6px 12px;
  border-radius: 999px;
  color: #fffdf2;
  background: linear-gradient(90deg, rgba(255, 224, 138, 0.26) 0%, rgba(255, 213, 79, 0.22) 100%);
  border: 1px solid rgba(255, 224, 138, 0.38);
  backdrop-filter: blur(8px);
  font-size: 14px;
  font-weight: 600;
}

.level-card {
  margin-top: 16px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.14);
  border: 1px solid rgba(255, 255, 255, 0.2);
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 24px rgba(0, 0, 0, 0.08);
}

.level-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
}

.level-title {
  font-size: 16px;
  font-weight: 600;
}

.level-subtitle {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.9;
}

.level-range {
  font-size: 12px;
  opacity: 0.85;
}

.level-side {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 8px;
}

.level-guide-btn {
  min-width: 68px;
  border-color: rgba(255, 224, 138, 0.38);
  color: #fffdf2;
  background: linear-gradient(90deg, rgba(255, 224, 138, 0.18) 0%, rgba(255, 213, 79, 0.14) 100%);
}

.level-progress {
  margin-top: 14px;
  height: 8px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  overflow: hidden;
}

.level-progress-bar {
  height: 100%;
  border-radius: 999px;
  background: linear-gradient(90deg, #ffe08a 0%, #ffd54f 100%);
  box-shadow: 0 0 12px rgba(255, 224, 138, 0.35);
}

.level-progress-meta {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  gap: 12px;
  font-size: 12px;
  opacity: 0.92;
}

.level-guide-popup {
  padding: 20px 16px calc(24px + env(safe-area-inset-bottom));
  max-height: 72vh;
  overflow-y: auto;
}

.level-guide-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.level-guide-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.level-guide-desc {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.6;
  color: #666;
}

.level-guide-list {
  margin-top: 14px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.level-guide-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px;
  border-radius: 16px;
  background: #f7f8fa;
}

.level-guide-item.active {
  background: linear-gradient(180deg, #f4f9ff 0%, #eef6ff 100%);
  border: 1px solid rgba(25, 137, 250, 0.18);
}

.level-guide-item-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.level-guide-item-range {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.records-section {
  margin: 0 var(--page-padding-lg);
  margin-bottom: var(--content-bottom-safe);
  background: #fff;
  border-radius: var(--section-radius);
  padding: var(--page-padding-lg);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.record-item {
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.record-item:last-child {
  border-bottom: none;
}

.record-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.record-desc {
  font-size: 15px;
  color: #333;
}

.record-points {
  font-size: 16px;
  font-weight: 600;
}

.record-points.earn {
  color: #07c160;
}

.record-points.spend {
  color: #ee0a24;
}

.record-meta {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.record-detail {
  margin-top: 4px;
  font-size: 13px;
  color: #666;
}

.empty-state {
  padding: 24px 0;
}
</style>
