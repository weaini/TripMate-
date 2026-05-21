<template>
  <div class="home page-with-tabbar">
    <van-nav-bar title="旅游社交平台" fixed />
    <div class="nav-spacer"></div>

    <!-- 轮播 -->
    <div class="banner-section">
      <div
        class="banner-container"
        @touchstart.capture.passive="onBannerTouchStart"
        @touchmove.capture.passive="onBannerTouchMove"
        @mousedown.capture="onBannerMouseDown"
        @mousemove.capture="onBannerMouseMove"
        @mouseup.capture="onBannerMouseUp"
      >
        <van-swipe :autoplay="3500" indicator-color="white">
          <van-swipe-item v-for="(banner, index) in banners" :key="index">
            <div
              class="banner-item"
              @click="onBannerClick(banner)"
            >
              <img :src="banner.image" :alt="banner.title" class="banner-image" />
              <div class="banner-content">
                <span class="banner-badge">今日推荐</span>
                <h3>{{ banner.title }}</h3>
                <p>{{ banner.description }}</p>
                <div class="banner-cta">立即查看 <van-icon name="arrow" /></div>
              </div>
            </div>
          </van-swipe-item>
        </van-swipe>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="nav-section">
      <div class="section-title-row">
        <div>
          <div class="section-title">快捷入口</div>
          <div class="section-subtitle">快速进入常用功能</div>
        </div>
      </div>
      <van-grid :column-num="3" :border="false" :gutter="8">
        <van-grid-item
          v-for="nav in navItems"
          :key="nav.name"
          :icon="nav.icon"
          :text="nav.text"
          @click="handleNavClick(nav)"
        />
      </van-grid>
    </div>

    <!-- 热门动态 -->
    <div class="feed-section">
      <div class="section-header">
        <div>
          <h3>热门动态</h3>
          <p class="section-subtitle">看看大家最近都在分享什么</p>
        </div>
        <span class="more" @click="router.push('/posts')">更多 <van-icon name="arrow" /></span>
      </div>
      <div v-if="postsLoading && hotPosts.length === 0" class="feed-skeleton">
        <van-skeleton title :row="2" v-for="n in 3" :key="n" class="sk" />
      </div>
      <div v-else-if="hotPosts.length === 0" class="feed-empty">
        <van-empty description="暂无动态，去发布第一条吧" image="search">
          <van-button type="primary" size="small" round @click="router.push('/posts/create')">
            发布动态
          </van-button>
        </van-empty>
      </div>
      <div v-else class="feed-list">
        <div
          v-for="post in hotPosts"
          :key="post.id"
          class="feed-item"
          @click="router.push('/posts/' + post.id)"
        >
          <div class="feed-main">
            <div class="feed-user">
              <van-image
                round
                width="36"
                height="36"
                fit="cover"
                :src="post.user?.avatar || '/default-avatar.png'"
              />
              <div class="feed-user-text">
                <span class="nickname">{{ post.user?.nickname || post.user?.username || '用户' }}</span>
                <span class="time">{{ formatTime(post.createdAt) }}</span>
              </div>
            </div>
            <div class="feed-body">
              <p class="feed-content">{{ excerpt(post.content) }}</p>
              <div v-if="post.images?.length" class="feed-thumb-wrap">
                <van-image
                  :src="getFullImageUrl(post.images[0])"
                  class="feed-thumb"
                  fit="cover"
                />
              </div>
            </div>
            <div v-if="post.location" class="feed-location">
              <van-icon name="location-o" />
              <span>{{ post.location }}</span>
            </div>
            <div class="feed-meta">
              <span><van-icon name="good-job-o" /> {{ post.likeCount ?? 0 }}</span>
              <span><van-icon name="chat-o" /> {{ post.commentCount ?? 0 }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 推荐活动（条数少，仅展示） -->
    <div class="feed-section activities-preview">
      <div class="section-header">
        <div>
          <h3>精选活动</h3>
          <p class="section-subtitle">一起出发，发现更多同好</p>
        </div>
        <span class="more" @click="router.push('/activities')">更多 <van-icon name="arrow" /></span>
      </div>
      <div v-if="actsLoading && previewActivities.length === 0" class="feed-skeleton">
        <van-skeleton title :row="1" v-for="n in 2" :key="n" class="sk" />
      </div>
      <div v-else-if="previewActivities.length === 0" class="feed-empty compact">
        <p class="muted">暂无进行中的活动</p>
      </div>
      <div v-else class="act-list">
        <div
          v-for="a in previewActivities"
          :key="a.id"
          class="act-row"
          @click="router.push('/activities/' + a.id)"
        >
          <div class="act-top">
            <span class="act-badge">精选</span>
            <span class="act-title">{{ a.title }}</span>
          </div>
          <span class="act-meta">{{ a.destination || '目的地待定' }}</span>
          <div class="act-info-row">
            <span class="act-pill"><van-icon name="friends-o" /> {{ a.currentParticipants ?? 0 }}/{{ a.maxParticipants ?? 0 }} 人</span>
            <span v-if="a.type" class="act-pill">{{ a.type }}</span>
          </div>
        </div>
      </div>
    </div>

    <van-tabbar v-model="activeTab" fixed>
      <van-tabbar-item icon="home-o" to="/home">首页</van-tabbar-item>
      <van-tabbar-item icon="chat-o" to="/posts">动态</van-tabbar-item>
      <van-tabbar-item icon="calendar-o" to="/activities">活动</van-tabbar-item>
      <van-tabbar-item icon="bookmark-o" to="/guides">攻略</van-tabbar-item>
      <van-tabbar-item icon="user-o" to="/profile">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHotPosts, getPosts } from '@/api/posts'
import { getActivities } from '@/api/activities'

const router = useRouter()
const activeTab = ref(0)

// 轮播：避免滑动时误触发 click 跳转
const bannerTouch = ref({ x: 0, y: 0, moved: false })
const onBannerTouchStart = (e) => {
  const t = e.touches?.[0]
  bannerTouch.value = { x: t?.clientX || 0, y: t?.clientY || 0, moved: false }
}

const onBannerTouchMove = (e) => {
  const t = e.touches?.[0]
  if (!t) return
  const dx = Math.abs((t.clientX || 0) - bannerTouch.value.x)
  const dy = Math.abs((t.clientY || 0) - bannerTouch.value.y)
  if (dx > 8 || dy > 8) bannerTouch.value.moved = true
}

// 桌面端鼠标拖拽也会触发 click，这里同样做拦截
const bannerMouseDown = ref(null)
const onBannerMouseDown = (e) => {
  bannerMouseDown.value = { x: e.clientX || 0, y: e.clientY || 0, moved: false, down: true }
  bannerTouch.value.moved = false
}

const onBannerMouseMove = (e) => {
  if (!bannerMouseDown.value?.down) return
  const dx = Math.abs((e.clientX || 0) - bannerMouseDown.value.x)
  const dy = Math.abs((e.clientY || 0) - bannerMouseDown.value.y)
  if (dx > 6 || dy > 6) {
    bannerMouseDown.value.moved = true
    bannerTouch.value.moved = true
  }
}

const onBannerMouseUp = () => {
  if (bannerMouseDown.value) bannerMouseDown.value.down = false
}

const onBannerClick = (banner) => {
  if (bannerTouch.value.moved) return
  if (banner?.to) router.push(banner.to)
}

const banners = ref([
  {
    image: '/static/【哲风壁纸】动漫角色-卡通场景.png',
    title: '发现旅途',
    description: '动态 · 活动 · 攻略一站逛',
    to: '/posts',
  },
  {
    image: '/static/【哲风壁纸】天坛-天空-彩烟.png',
    title: '结伴出发',
    description: '报名活动，认识同好',
    to: '/activities',
  },
  {
    image: '/static/【哲风壁纸】党徽-共产主义接班人.png',
    title: '写好攻略',
    description: '分享路线与心得',
    to: '/guides',
  },
])

const navItems = ref([
  { name: 'posts', icon: 'chat-o', text: '动态', path: '/posts' },
  { name: 'activities', icon: 'calendar-o', text: '活动', path: '/activities' },
  { name: 'guides', icon: 'bookmark-o', text: '攻略', path: '/guides' },
])

const handleNavClick = (nav) => {
  router.push(nav.path)
}

const hotPosts = ref([])
const postsLoading = ref(false)
const previewActivities = ref([])
const actsLoading = ref(false)

const getFullImageUrl = (url) => {
  if (!url) return ''
  if (String(url).startsWith('http')) return url
  return `http://localhost:8081${url}`
}

const excerpt = (text) => {
  if (!text) return ''
  const t = String(text).replace(/\s+/g, ' ').trim()
  return t.length > 72 ? t.slice(0, 72) + '…' : t
}

const formatTime = (t) => {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const diff = now - d
  if (diff < 86400000) {
    const h = d.getHours().toString().padStart(2, '0')
    const m = d.getMinutes().toString().padStart(2, '0')
    return `${h}:${m}`
  }
  return `${d.getMonth() + 1}/${d.getDate()}`
}

onMounted(async () => {
  postsLoading.value = true
  actsLoading.value = true
  try {
    const postsRes = await getHotPosts({ page: 0, size: 8 })
    const content = postsRes?.content || postsRes?.data?.content || []
    hotPosts.value = Array.isArray(content) ? content : []
  } catch {
    try {
      const fallback = await getPosts({ page: 0, size: 8 })
      const c = fallback?.content || fallback?.data?.content || []
      hotPosts.value = Array.isArray(c) ? c : []
    } catch {
      hotPosts.value = []
    }
  } finally {
    postsLoading.value = false
  }
  try {
    const actRes = await getActivities({ page: 0, size: 5, status: 'APPROVED' })
    const ac = actRes?.content || actRes?.data?.content || []
    previewActivities.value = Array.isArray(ac) ? ac.slice(0, 5) : []
  } catch {
    previewActivities.value = []
  } finally {
    actsLoading.value = false
  }
})
</script>

<style scoped>
.home {
  background-color: var(--page-bg);
  padding-bottom: env(safe-area-inset-bottom);
}

.banner-section {
  margin: var(--page-padding);
  height: 168px;
  border-radius: 18px;
  overflow: hidden;
  box-shadow: 0 10px 28px rgba(25, 137, 250, 0.16);
}

.banner-container,
.banner-container .van-swipe,
.banner-container .van-swipe-item {
  height: 168px;
}

.banner-item {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  cursor: pointer;
}

.banner-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: 50% 35%;
  display: block;
  transform: scale(1.02);
}

.banner-content {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0, 0, 0, 0.72));
  color: #fff;
  padding: 16px 14px 14px;
}

.banner-badge {
  display: inline-flex;
  align-items: center;
  margin-bottom: 8px;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.18);
  backdrop-filter: blur(6px);
  font-size: 11px;
  font-weight: 600;
}

.banner-content h3 {
  font-size: 17px;
  font-weight: 600;
  margin: 0 0 4px;
}

.banner-content p {
  font-size: 13px;
  margin: 0;
  opacity: 0.92;
}

.banner-cta {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-top: 10px;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  font-size: 12px;
  font-weight: 500;
}

.nav-section {
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  margin: 0 var(--page-padding) 12px;
  border-radius: 16px;
  padding: 14px 10px 18px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.section-title-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px 10px;
}

.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
}

.section-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #9aa4b2;
}

.feed-section {
  margin: 0 var(--page-padding) 16px;
  background: linear-gradient(180deg, #ffffff 0%, #fcfdff 100%);
  border-radius: 16px;
  padding: 16px 14px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.section-header .section-subtitle {
  margin-top: 4px;
}

.section-header .more {
  font-size: 13px;
  color: #1989fa;
  display: inline-flex;
  align-items: center;
  gap: 2px;
}

.feed-skeleton .sk {
  margin-bottom: 12px;
}

.feed-empty {
  padding: 8px 0 16px;
}

.feed-empty.compact {
  padding: 8px 0;
}

.feed-empty .muted {
  margin: 0;
  font-size: 13px;
  color: #999;
  text-align: center;
}

.feed-item {
  border-bottom: 1px solid #f1f5f9;
  padding: 12px 0;
}

.feed-item:last-child {
  border-bottom: none;
}

.feed-user {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.feed-main {
  display: flex;
  flex-direction: column;
}

.feed-body {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.feed-user-text {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.feed-user-text .nickname {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.feed-user-text .time {
  font-size: 12px;
  color: #999;
}

.feed-content {
  margin: 0 0 8px;
  font-size: 14px;
  color: #444;
  line-height: 1.5;
  flex: 1;
}

.feed-thumb-wrap {
  flex-shrink: 0;
}

.feed-thumb {
  width: 84px;
  height: 84px;
  border-radius: 12px;
  overflow: hidden;
}

.feed-location {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 8px;
  color: #7c8a9a;
  font-size: 12px;
}

.feed-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #999;
}

.feed-meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.act-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.act-row {
  padding: 14px 12px;
  background: linear-gradient(135deg, #f8fbff 0%, #f3f8ff 100%);
  border: 1px solid #e8f1ff;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.act-row:active {
  opacity: 0.85;
}

.act-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.act-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 3px 8px;
  border-radius: 999px;
  background: #1989fa;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
}

.act-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.act-meta {
  font-size: 12px;
  color: #888;
}

.act-info-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.act-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 999px;
  background: rgba(25, 137, 250, 0.08);
  color: #4b5b70;
  font-size: 12px;
}

.activities-preview {
  margin-bottom: calc(56px + env(safe-area-inset-bottom));
}

:deep(.van-grid-item__content) {
  border-radius: 14px;
  background: linear-gradient(180deg, #f8fbff 0%, #ffffff 100%);
  box-shadow: inset 0 0 0 1px #edf2f7;
  padding: 18px 8px;
}

:deep(.van-grid-item__icon) {
  margin-bottom: 8px;
  color: #1989fa;
}

:deep(.van-grid-item__text) {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

:deep(.van-swipe__indicators) {
  bottom: 10px;
}
</style>
