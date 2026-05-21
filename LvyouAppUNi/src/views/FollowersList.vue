<template>
  <div class="followers-list page-nav-only">
    <van-nav-bar title="粉丝预览" fixed left-arrow @click-left="$router.back()" />
    <div class="nav-spacer" />
    <div class="list-preview">
      <p class="list-preview-count">共 {{ totalCount }} 位粉丝</p>
      <div v-if="list.length === 0 && !loading" class="list-preview-empty">暂无粉丝，多发动态更容易被关注</div>
      <div v-else class="list-preview-row">
        <van-image
          v-for="u in list.slice(0, 10)"
          :key="'p-' + u.id"
          round
          width="36"
          height="36"
          fit="cover"
          :src="avatarUrl(u)"
          class="list-preview-avatar"
        />
        <span v-if="totalCount > 10" class="list-preview-more">+{{ totalCount - 10 }}</span>
      </div>
    </div>
    <div class="section-label">全部粉丝</div>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <van-empty v-if="!loading && list.length === 0" description="还没有粉丝" />
        <van-cell
          v-for="u in list"
          :key="u.id"
          :title="u.nickname || u.username"
          :label="u.bio || '暂无简介'"
          center
        >
          <template #icon>
            <van-image round width="44" height="44" fit="cover" :src="avatarUrl(u)" class="cell-avatar" />
          </template>
        </van-cell>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { useUserStore } from '@/stores/user'
import { getUserFollowers } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const list = ref([])
const page = ref(0)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const totalCount = ref(0)

const avatarUrl = (u) => {
  if (!u?.avatar) return '/default-avatar.png'
  if (String(u.avatar).startsWith('http')) return u.avatar
  return `http://localhost:8081${u.avatar.startsWith('/') ? '' : '/'}${u.avatar}`
}

const userId = () => userStore.userInfo?.id

const load = async (reset) => {
  if (!userId()) {
    showToast('请先登录')
    router.replace('/login')
    return
  }
  if (reset) {
    page.value = 0
    list.value = []
    finished.value = false
  }
  loading.value = true
  try {
    const res = await getUserFollowers(userId(), { page: page.value, size: 20 })
    const content = res?.content || []
    if (typeof res?.totalElements === 'number') totalCount.value = res.totalElements
    if (page.value === 0) list.value = content
    else list.value.push(...content)
    if (content.length < 20) finished.value = true
    else page.value++
  } catch (e) {
    showToast(e.response?.data?.message || '加载失败')
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => load(false)
const onRefresh = () => {
  refreshing.value = true
  load(true)
}

onMounted(() => {
  if (!userStore.isLoggedIn) {
    router.replace('/login')
    return
  }
  totalCount.value = userStore.userInfo?.followersCount ?? 0
  load(true)
})
</script>

<style scoped>
.followers-list { min-height: 100vh; background: var(--page-bg, #f5f5f5); }
.cell-avatar { margin-right: 12px; }
.list-preview {
  background: #fff;
  margin: 0 12px 8px;
  padding: 12px;
  border-radius: 12px;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.06);
}
.list-preview-count {
  margin: 0 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #333;
}
.list-preview-row {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 6px;
}
.list-preview-avatar {
  border: 2px solid #f0f0f0;
}
.list-preview-more {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f5f5f5;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: #666;
}
.list-preview-empty {
  font-size: 13px;
  color: #999;
  padding: 8px 0;
}
.section-label {
  margin: 8px 16px 4px;
  font-size: 13px;
  font-weight: 600;
  color: #666;
}
</style>
