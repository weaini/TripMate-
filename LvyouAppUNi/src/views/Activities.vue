<template>
  <div class="activities page-with-tabbar">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="活动" fixed>
      <template #right>
        <van-icon name="plus" size="18" @click="goToCreateActivity" />
      </template>
    </van-nav-bar>

    <!-- 搜索栏（与筛选、排序均为 fixed，由 .activities 的 padding-top 统一留出空间） -->
    <div class="search-bar">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索活动"
        @search="handleSearch"
        @clear="handleClear"
      />
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <van-tabs v-model:active="categoryTab" @change="handleCategoryTabChange">
        <van-tab title="全部" name="all" />
        <van-tab title="徒步" name="HIKING" />
        <van-tab title="摄影" name="PHOTOGRAPHY" />
        <van-tab title="露营" name="CAMPING" />
        <van-tab title="文化" name="CULTURAL" />
      </van-tabs>
      <div class="filter-actions">
        <van-icon name="filter-o" class="filter-icon" :class="{ active: hasExtraActivityFilters }" @click="showFilter = true" />
        <van-icon name="refresh" class="refresh-icon" @click="refreshActivities" />
      </div>
    </div>

    <!-- 排序栏 -->
    <div class="sort-bar">
      <van-dropdown-menu>
        <van-dropdown-item v-model="sortBy" :options="sortOptions" @change="handleSortChange" />
        <van-dropdown-item v-model="timeFilter" :options="timeOptions" @change="handleTimeChange" />
      </van-dropdown-menu>
    </div>

    <!-- 快速筛选标签 -->
    <div class="quick-filters" v-if="quickFilters.length > 0">
      <van-tag
        v-for="filter in quickFilters"
        :key="filter.key"
        :type="filter.active ? 'primary' : 'default'"
        size="medium"
        @click="toggleQuickFilter(filter)"
        class="quick-filter-tag"
      >
        {{ filter.label }}
      </van-tag>
    </div>

    <!-- 筛选弹窗 -->
    <van-popup v-model:show="showFilter" position="bottom">
      <div class="filter-popup">
        <div class="filter-header">
          <h3>筛选条件</h3>
          <van-icon name="cross" @click="showFilter = false" />
        </div>
        <div class="filter-content">
          <div class="filter-section">
            <h4>活动类型</h4>
            <van-radio-group v-model="filterType">
              <van-radio name="all">全部</van-radio>
              <van-radio name="HIKING">徒步</van-radio>
              <van-radio name="PHOTOGRAPHY">摄影</van-radio>
              <van-radio name="CAMPING">露营</van-radio>
              <van-radio name="CULTURAL">文化</van-radio>
              <van-radio name="ADVENTURE">探险</van-radio>
              <van-radio name="RELAXATION">休闲</van-radio>
            </van-radio-group>
          </div>
          <div class="filter-section">
            <h4>活动状态</h4>
            <van-radio-group v-model="filterStatus">
              <van-radio name="all">全部</van-radio>
              <van-radio name="APPROVED">已通过</van-radio>
              <van-radio name="ONGOING">进行中</van-radio>
              <van-radio name="PENDING">待审核</van-radio>
              <van-radio name="COMPLETED">已完成</van-radio>
              <van-radio name="CANCELLED">已取消</van-radio>
            </van-radio-group>
          </div>
          <div class="filter-section">
            <h4>费用范围</h4>
            <van-radio-group v-model="filterCost">
              <van-radio name="all">全部</van-radio>
              <van-radio name="free">免费</van-radio>
              <van-radio name="low">100元以下</van-radio>
              <van-radio name="medium">100-500元</van-radio>
              <van-radio name="high">500元以上</van-radio>
            </van-radio-group>
          </div>
        </div>
        <div class="filter-footer">
          <van-button @click="resetFilter">重置</van-button>
          <van-button type="primary" @click="applyFilter">确定</van-button>
        </div>
      </div>
    </van-popup>

    <!-- 结果区：统计摘要 + 列表/空状态二选一 -->
    <section class="activities-result">
      <!-- 有数据时展示统计栏，吸顶时贴在固定筛选区下方 -->
      <div class="stats-bar" v-if="activities.length > 0">
        <div class="stats-item">
          <span class="stats-label">共找到</span>
          <span class="stats-value">{{ totalElements || activities.length }}</span>
          <span class="stats-label">个活动</span>
        </div>
        <div class="stats-item stats-summary" v-if="activeFilterSummary">
          <span class="stats-label">筛选</span>
          <span class="stats-value">{{ activeFilterSummary }}</span>
        </div>
      </div>

      <!-- 有数据或加载中：显示可刷新列表 -->
      <div class="activities-list" v-if="activities.length > 0 || loading">
        <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            finished-text="没有更多了"
            @load="loadActivities"
          >
            <ActivityCard
              v-for="activity in activities"
              :key="activity.id"
              :activity="activity"
              @click="goToActivityDetail(activity.id)"
            />
          </van-list>
        </van-pull-refresh>
      </div>

      <!-- 无数据且未加载中：仅显示空状态，避免与列表的「没有更多了」重复 -->
      <div class="empty-state" v-else>
        <van-empty
          image="search"
          description="暂无活动"
        >
          <van-button type="primary" @click="goToCreateActivity" v-if="userStore.isExpert || userStore.isAdmin">
            创建第一个活动
          </van-button>
        </van-empty>
      </div>
    </section>

    <!-- 发布按钮 - 只有旅游达人才能创建活动 -->
    <div
      v-if="userStore.isExpert || userStore.isAdmin"
      class="fixed-create-fab"
      @click="goToCreateActivity"
    >
      <van-icon name="plus" />
    </div>

    <!-- 底部导航 -->
    <van-tabbar v-model="tabbarActive" fixed>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/posts">动态</van-tabbar-item>
      <van-tabbar-item icon="calendar-o" to="/activities">活动</van-tabbar-item>
      <van-tabbar-item icon="bookmark-o" to="/guides">攻略</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getActivities } from '@/api/activities'
import ActivityCard from '@/components/ActivityCard.vue'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const searchKeyword = ref('')
// 顶部分类与底部 TabBar 不能共用同一个 ref，否则会把数字 2 当成活动类型传给后端
const categoryTab = ref('all')
const tabbarActive = ref(2) // 底部「活动」为 2
const activities = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const page = ref(0) // 后端分页从0开始
const pageSize = ref(10)
const totalElements = ref(0)

// 筛选相关
const showFilter = ref(false)
const filterType = ref('all')
const filterStatus = ref('all')
const filterCost = ref('all')

// 排序相关
const sortBy = ref('latest')
const timeFilter = ref('all')

// 快速筛选
const quickFilters = ref([])

const sortOptions = [
  { text: '最新发布', value: 'latest' },
  { text: '最早开始', value: 'earliest' },
  { text: '最多报名', value: 'popular' },
  { text: '费用最低', value: 'cheapest' }
]

const timeOptions = [
  { text: '全部时间', value: 'all' },
  { text: '今天', value: 'today' },
  { text: '本周', value: 'week' },
  { text: '本月', value: 'month' }
]

const hasExtraActivityFilters = computed(
  () =>
    !!searchKeyword.value.trim() ||
    categoryTab.value !== 'all' ||
    filterType.value !== 'all' ||
    filterStatus.value !== 'all' ||
    filterCost.value !== 'all' ||
    timeFilter.value !== 'all'
)

const activeFilterSummary = computed(() => {
  const parts = []
  if (searchKeyword.value.trim()) {
    parts.push(`「${searchKeyword.value.trim()}」`)
  }
  const typeKey = filterType.value !== 'all' ? filterType.value : (categoryTab.value !== 'all' ? categoryTab.value : null)
  if (typeKey) {
    parts.push(getTypeText(typeKey))
  }
  if (filterStatus.value !== 'all') {
    parts.push(getStatusText(filterStatus.value))
  }
  if (filterCost.value !== 'all') {
    parts.push(getCostText(filterCost.value))
  }
  if (timeFilter.value !== 'all') {
    const t = timeOptions.find((o) => o.value === timeFilter.value)
    parts.push(t ? t.text : timeFilter.value)
  }
  return parts.length ? parts.join(' · ') : ''
})

// 方法
const goToCreateActivity = () => {
  router.push('/activities/create')
}

const goToActivityDetail = (id) => {
  router.push(`/activities/${id}`)
}

const handleSearch = () => {
  updateQuickFilters()
  resetActivities()
  loadActivities()
}

const handleClear = () => {
  searchKeyword.value = ''
  updateQuickFilters()
  resetActivities()
  loadActivities()
}

const handleCategoryTabChange = () => {
  // 用顶部分类筛选时，清空弹窗里的类型，避免与 categoryTab 冲突
  filterType.value = 'all'
  updateQuickFilters()
  resetActivities()
  loadActivities()
}

const resetActivities = () => {
  activities.value = []
  page.value = 0 // 重置为第0页
  finished.value = false
  totalElements.value = 0
}

// 刷新活动列表
const refreshActivities = () => {
  resetActivities()
  loadActivities()
}

// 下拉刷新
const onRefresh = () => {
  resetActivities()
  loadActivities().finally(() => {
    refreshing.value = false
  })
}

const loadActivities = async () => {
  if (loading.value) return
  
  loading.value = true
  
  try {
    const params = {
      page: page.value,
      size: pageSize.value
    }
    
    // 添加搜索关键词
    if (searchKeyword.value) {
      params.keyword = searchKeyword.value
    }
    
    // 活动类型：弹窗筛选优先；否则使用顶部分类（徒步/摄影/文化等）
    if (filterType.value !== 'all') {
      params.type = filterType.value
    } else if (categoryTab.value !== 'all') {
      params.type = categoryTab.value
    }
    
    if (filterStatus.value !== 'all') {
      params.status = filterStatus.value
    } else {
      // 默认只显示已批准的活动
      params.status = 'APPROVED'
    }
    
    if (filterCost.value !== 'all') {
      switch (filterCost.value) {
        case 'free':
          params.maxCost = 0
          break
        case 'low':
          params.maxCost = 100
          break
        case 'medium':
          params.minCost = 100
          params.maxCost = 500
          break
        case 'high':
          params.minCost = 500
          break
      }
    }
    
    // 添加排序
    switch (sortBy.value) {
      case 'latest':
        params.sort = 'createdAt,desc'
        break
      case 'earliest':
        params.sort = 'startTime,asc'
        break
      case 'popular':
        params.sort = 'currentParticipants,desc'
        break
      case 'cheapest':
        params.sort = 'cost,asc'
        break
    }
    
    // 添加时间筛选
    if (timeFilter.value !== 'all') {
      const now = new Date()
      switch (timeFilter.value) {
        case 'today':
          params.startDate = new Date(now.getFullYear(), now.getMonth(), now.getDate()).toISOString()
          break
        case 'week':
          const weekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000)
          params.startDate = weekAgo.toISOString()
          break
        case 'month':
          const monthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)
          params.startDate = monthAgo.toISOString()
          break
      }
    }
    
    console.log('加载活动参数:', params)
    const response = await getActivities(params)
    console.log('活动API响应:', response)
    
    if (page.value === 0) {
      activities.value = response.content || []
    } else {
      activities.value.push(...(response.content || []))
    }
    
    // 更新统计信息
    totalElements.value = response.totalElements || 0
    
    console.log('当前活动列表:', activities.value)
    page.value++
    finished.value = !response.content || response.content.length < pageSize.value
  } catch (error) {
    console.error('加载活动失败:', error)
  } finally {
    loading.value = false
  }
}

// 筛选相关方法
const resetFilter = () => {
  filterType.value = 'all'
  filterStatus.value = 'all'
  filterCost.value = 'all'
  categoryTab.value = 'all'
  updateQuickFilters()
}

const applyFilter = () => {
  showFilter.value = false
  updateQuickFilters()
  resetActivities()
  loadActivities()
}

// 排序相关方法
const handleSortChange = () => {
  resetActivities()
  loadActivities()
}

const handleTimeChange = () => {
  updateQuickFilters()
  resetActivities()
  loadActivities()
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    'all': '全部',
    'APPROVED': '已通过',
    'ONGOING': '进行中',
    'PENDING': '待审核',
    'REJECTED': '已拒绝',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || '未知'
}

// 更新快速筛选
const updateQuickFilters = () => {
  const filters = []

  if (searchKeyword.value.trim()) {
    filters.push({
      key: 'search',
      label: `搜索:${searchKeyword.value.trim()}`,
      active: true
    })
  }

  if (filterType.value !== 'all') {
    filters.push({
      key: 'type',
      label: getTypeText(filterType.value),
      active: true
    })
  } else if (categoryTab.value !== 'all') {
    filters.push({
      key: 'category',
      label: getTypeText(categoryTab.value),
      active: true
    })
  }
  
  if (filterStatus.value !== 'all') {
    filters.push({
      key: 'status',
      label: getStatusText(filterStatus.value),
      active: true
    })
  }
  
  if (filterCost.value !== 'all') {
    filters.push({
      key: 'cost',
      label: getCostText(filterCost.value),
      active: true
    })
  }

  if (timeFilter.value !== 'all') {
    const t = timeOptions.find((o) => o.value === timeFilter.value)
    filters.push({
      key: 'time',
      label: t ? t.text : timeFilter.value,
      active: true
    })
  }
  
  quickFilters.value = filters
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

// 获取费用文本
const getCostText = (cost) => {
  const costMap = {
    'free': '免费',
    'low': '100元以下',
    'medium': '100-500元',
    'high': '500元以上'
  }
  return costMap[cost] || '全部'
}

// 切换快速筛选
const toggleQuickFilter = (filter) => {
  filter.active = !filter.active
  
  if (!filter.active) {
    // 重置对应的筛选条件
    switch (filter.key) {
      case 'search':
        searchKeyword.value = ''
        break
      case 'type':
        filterType.value = 'all'
        break
      case 'category':
        categoryTab.value = 'all'
        break
      case 'status':
        filterStatus.value = 'all'
        break
      case 'cost':
        filterCost.value = 'all'
        break
      case 'time':
        timeFilter.value = 'all'
        break
    }
    
    // 重新加载数据
    updateQuickFilters()
    resetActivities()
    loadActivities()
  }
}

onMounted(() => {
  updateQuickFilters()
  loadActivities()
})
</script>

<style scoped>
/* 顶部固定区域总高度：nav(46)+search(~54)+filter(~44)+sort(~48)=192，取 200 避免内容与筛选/排序栏重叠 */
.activities {
  padding: 0;
  padding-top: 200px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf1 100%);
}

.search-bar {
  background: white;
  padding: 12px 16px;
  position: fixed;
  top: var(--nav-height);
  left: 0;
  right: 0;
  z-index: var(--z-header);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #f0f0f0;
}

.filter-bar {
  background: white;
  position: fixed;
  top: calc(var(--nav-height) + 54px);
  left: 0;
  right: 0;
  z-index: var(--z-header);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 16px 8px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid #f0f0f0;
}

.filter-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-icon, .refresh-icon {
  font-size: 18px;
  color: #666;
  cursor: pointer;
  padding: 5px;
  border-radius: 4px;
  transition: all 0.3s;
}

.filter-icon:hover, .refresh-icon:hover {
  background-color: #f0f0f0;
  color: #1989fa;
}

.filter-icon.active {
  color: #1989fa;
}

.sort-bar {
  background: white;
  position: fixed;
  top: calc(var(--nav-height) + 54px + 44px);
  left: 0;
  right: 0;
  z-index: var(--z-header);
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 统计信息栏：吸顶时紧贴固定区域下方 */
.stats-bar {
  background: white;
  padding: 12px 16px;
  margin-top: 0;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  position: sticky;
  top: 200px;
  z-index: 4;
}

.stats-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stats-summary .stats-value {
  max-width: 52vw;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.stats-label {
  color: #666;
}

.stats-value {
  color: #1989fa;
  font-weight: 600;
}

.activities-list {
  padding: 10px;
  margin-top: 0;
  padding-top: 20px;
}

/* 快速筛选标签：吸顶时在统计栏下方 */
.quick-filters {
  background: white;
  padding: 10px 16px;
  margin-top: 0;
  border-bottom: 1px solid #eee;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  position: sticky;
  top: calc(200px + 44px); /* 固定区高度 + 统计栏约高 */
  z-index: 4;
}

.quick-filter-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.quick-filter-tag:hover {
  transform: scale(1.05);
}

/* 空状态：仅在无数据且未加载时显示，与列表二选一 */
.empty-state {
  margin-top: 0;
  padding: 60px 20px 40px;
}

:deep(.van-tabs__line) {
  background-color: #1989fa;
}

:deep(.van-tab) {
  font-size: 12px;
}

:deep(.van-floating-bubble) {
  background-color: #1989fa;
  bottom: 108px !important;
}

/* 筛选弹窗样式 */
.filter-popup {
  padding: 20px;
  max-height: 70vh;
}

.filter-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.filter-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.filter-content {
  margin-bottom: 20px;
}

.filter-section {
  margin-bottom: 20px;
}

.filter-section h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.filter-footer {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

:deep(.van-radio-group) {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

:deep(.van-radio) {
  margin-right: 0;
}
</style>
