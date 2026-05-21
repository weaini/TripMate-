<template>
  <div class="settings">
    <van-nav-bar title="通知设置" fixed left-arrow @click-left="goBackToProfile" />
    <div class="nav-spacer"></div>
    <div class="content">
      <van-cell-group inset>
        <van-cell title="系统通知">
          <template #right-icon>
            <van-switch v-model="model.enableSystem" />
          </template>
        </van-cell>
        <van-cell title="活动相关">
          <template #right-icon>
            <van-switch v-model="model.enableActivity" />
          </template>
        </van-cell>
        <van-cell title="评论提醒">
          <template #right-icon>
            <van-switch v-model="model.enableComment" />
          </template>
        </van-cell>
        <van-cell title="审核结果">
          <template #right-icon>
            <van-switch v-model="model.enableReview" />
          </template>
        </van-cell>
      </van-cell-group>
      <div style="padding:16px">
        <van-button type="primary" block @click="save">保存</van-button>
      </div>
    </div>
  </div>
  
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '@/stores/notifications'
import { showToast } from 'vant'

const router = useRouter()
const store = useNotificationStore()
const model = reactive({ enableSystem: true, enableActivity: true, enableComment: true, enableReview: true })

onMounted(async () => {
  if (!store.preference) await store.loadPreference()
  Object.assign(model, store.preference || {})
})

const save = async () => {
  await store.updatePreference(model)
  showToast('已保存')
}

const goBackToProfile = () => {
  router.push('/profile')
}
</script>

<style scoped>
.settings { min-height: 100vh; background: #f5f5f5; }
.content { padding: 12px; }
</style>

