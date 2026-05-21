<template>
  <div class="notifications page-nav-only">
    <van-nav-bar
      title="通知"
      fixed
      right-text="首页"
      @click-right="$router.push('/home')"
    >
      <template #left>
        <van-icon name="arrow-left" size="18" class="nav-back-icon" @click="$router.push('/profile')" />
      </template>
    </van-nav-bar>
    <div class="nav-spacer"></div>
    <div class="content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="store.loading"
          :finished="store.finished"
          finished-text="没有更多了"
          @load="() => store.load(false)"
        >
          <div v-for="n in store.items" :key="n.id" class="notice-item" :class="{ unread: n.status === 'UNREAD' }">
            <div class="notice-title">{{ n.title }}</div>
            <div class="notice-content">{{ n.content }}</div>
            <div class="notice-meta">
              <span class="type">{{ n.type }}</span>
              <span class="time">{{ formatTime(n.createdAt) }}</span>
              <van-button v-if="n.status === 'UNREAD'" size="small" type="primary" plain @click="markOne(n)">设为已读</van-button>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
      <div class="actions">
        <van-button block type="primary" @click="markAll">全部设为已读</van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useNotificationStore } from '@/stores/notifications'

const store = useNotificationStore()
const refreshing = ref(false)

const onRefresh = async () => {
  await store.load(true)
  refreshing.value = false
}

const markOne = async (n) => {
  await store.markSelectedRead([n.id])
}

const markAll = async () => {
  await store.markAllReadAction()
}

const formatTime = (t) => {
  if (!t) return ''
  const ts = new Date(t)
  return ts.toLocaleString()
}

onMounted(async () => {
  if (store.items.length === 0) {
    await store.init()
  }
})
</script>

<style scoped>
.notifications {
  min-height: 100vh;
  background-color: var(--page-bg);
}

.content {
  padding: var(--page-padding-lg);
}

.nav-back-icon {
  color: #333;
}

.notice-item {
  background: #fff;
  border-radius: 8px;
  padding: 12px 14px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.05);
}
.notice-item.unread { box-shadow: 0 0 0 2px #1989fa22 inset; }
.notice-title { font-weight: 600; color: #333; margin-bottom: 6px; }
.notice-content { color: #666; font-size: 14px; line-height: 1.5; margin-bottom: 8px; }
.notice-meta { display: flex; gap: 8px; align-items: center; font-size: 12px; color: #999; }
.actions { margin-top: 10px; }
</style>
