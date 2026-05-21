<template>
  <div class="edit-post page-nav-only">
    <van-nav-bar title="编辑动态" fixed left-arrow @click-left="goBack">
      <template #right>
        <van-button type="primary" size="small" :loading="saving" @click="handleSave">
          保存
        </van-button>
      </template>
    </van-nav-bar>
    <div class="nav-spacer"></div>

    <div v-if="loading" class="loading-wrap">
      <van-loading vertical>加载动态信息中...</van-loading>
    </div>

    <div v-else class="post-form">
      <div class="status-tip" :class="originalStatus === 'REJECTED' ? 'status-tip-rejected' : 'status-tip-pending'">
        {{ statusTipText }}
      </div>

      <van-form @submit="handleSave">
        <van-cell-group>
          <van-field
            v-model="postForm.content"
            type="textarea"
            placeholder="分享你的旅行故事..."
            :rules="[{ required: true, message: '请输入动态内容' }]"
            maxlength="1000"
            show-word-limit
            autosize
            rows="4"
          />
        </van-cell-group>

        <van-cell-group title="位置信息">
          <van-field
            v-model="postForm.location"
            label="位置"
            placeholder="点击选择位置（可选）"
            left-icon="location-o"
            readonly
            is-link
            @click="openLocationPicker"
          />
        </van-cell-group>

        <van-cell-group title="当前图片">
          <div v-if="currentImages.length" class="current-images">
            <van-image
              v-for="(image, index) in currentImages"
              :key="`${image}-${index}`"
              :src="image"
              class="current-image"
              fit="cover"
              radius="10"
            />
          </div>
          <div v-else class="image-empty">当前没有图片</div>
        </van-cell-group>

        <van-cell-group title="替换图片">
          <div class="replace-hint">如需修改图片，请重新上传，保存后会覆盖原图片。</div>
          <van-uploader
            v-model="replacementFileList"
            multiple
            :max-count="9"
            :after-read="afterRead"
            :before-delete="beforeDelete"
            upload-text="重新选择图片"
          />
        </van-cell-group>

        <van-cell-group title="动态类型">
          <van-radio-group v-model="postForm.type" direction="horizontal">
            <van-radio name="DYNAMIC">实时动态</van-radio>
            <van-radio name="STRATEGY">攻略分享</van-radio>
            <van-radio name="ACTIVITY">活动相关</van-radio>
          </van-radio-group>
        </van-cell-group>
      </van-form>
    </div>

    <van-popup v-model:show="showLocationPicker" position="bottom" round>
      <div class="destination-popup">
        <div class="destination-header">
          <h3>选择位置</h3>
          <van-icon name="cross" @click="showLocationPicker = false" />
        </div>
        <div class="destination-search">
          <van-search
            v-model="locationKeyword"
            placeholder="输入城市/景点/地址关键词"
            show-action
            @search="handleLocationSearch"
          >
            <template #action>
              <span @click="handleLocationSearch">搜索</span>
            </template>
          </van-search>
        </div>
        <div class="destination-result">
          <van-list
            v-model:loading="locationLoading"
            :finished="locationFinished"
            finished-text="没有更多结果了"
            @load="loadMoreLocations"
          >
            <van-cell
              v-for="item in locationResults"
              :key="item.id"
              clickable
              :title="item.name"
              :label="formatPoiAddress(item)"
              @click="chooseLocation(item)"
            />
          </van-list>
          <div
            v-if="!locationLoading && locationResults.length === 0 && locationKeyword"
            class="destination-empty"
          >
            <van-empty image="search" description="暂未找到匹配地点" />
          </div>
          <div v-if="locationSearchError" class="manual-fill-box">
            <div class="manual-fill-title">地图服务异常，改为手动填写</div>
            <div class="manual-fill-hint">{{ locationSearchError }}</div>
            <van-field
              v-model="manualLocation"
              placeholder="请输入位置"
              maxlength="200"
              clearable
            />
            <van-button type="primary" block round class="manual-fill-btn" @click="confirmManualLocation">
              使用手动填写
            </van-button>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showConfirmDialog, showToast } from 'vant'
import { getPost, updatePost, uploadPostImages } from '@/api/posts'
import { searchPlacesByKeyword } from '@/api/amap'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const saving = ref(false)
const originalStatus = ref('PENDING')
const initialSnapshot = ref('')
const currentImages = ref([])
const replacementFileList = ref([])

const postForm = ref({
  content: '',
  location: '',
  type: 'DYNAMIC',
})

const showLocationPicker = ref(false)
const locationKeyword = ref('')
const locationResults = ref([])
const locationLoading = ref(false)
const locationFinished = ref(true)
const locationPage = ref(1)
const locationSearchError = ref('')
const manualLocation = ref('')

const statusTipText = computed(() => {
  if (originalStatus.value === 'REJECTED') {
    return '该动态当前为已拒绝状态，修改保存后会重新进入待审核。'
  }
  return '该动态当前为待审核状态，修改保存后将继续保持待审核。'
})

const getFormSnapshot = () => JSON.stringify({
  content: postForm.value.content || '',
  location: postForm.value.location || '',
  type: postForm.value.type || 'DYNAMIC',
})

const hasUnsavedChanges = () => {
  return getFormSnapshot() !== initialSnapshot.value || replacementFileList.value.length > 0
}

const loadPost = async () => {
  try {
    const post = await getPost(route.params.id)

    if (post.user?.id && userStore.userInfo?.id && post.user.id !== userStore.userInfo.id) {
      showToast('只能编辑自己的动态')
      router.replace('/my-posts')
      return
    }

    if (post.status === 'APPROVED') {
      showToast('审核通过的动态不能再编辑')
      router.replace('/my-posts')
      return
    }

    if (post.status === 'DELETED') {
      showToast('该动态已删除')
      router.replace('/my-posts')
      return
    }

    originalStatus.value = post.status || 'PENDING'
    postForm.value = {
      content: post.content || '',
      location: post.location || '',
      type: post.type || 'DYNAMIC',
    }
    currentImages.value = post.images || []
    initialSnapshot.value = getFormSnapshot()
  } catch (error) {
    console.error('加载动态详情失败:', error)
    showToast(error.message || '加载动态详情失败')
    router.replace('/my-posts')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  if (!hasUnsavedChanges()) {
    router.back()
    return
  }

  showConfirmDialog({
    title: '确认离开',
    message: '当前修改尚未保存，确定要离开吗？'
  }).then(() => {
    router.back()
  }).catch(() => {})
}

const openLocationPicker = () => {
  showLocationPicker.value = true
  locationResults.value = []
  locationLoading.value = false
  locationFinished.value = true
  locationSearchError.value = ''
  if (postForm.value.location) {
    locationKeyword.value = postForm.value.location
    manualLocation.value = postForm.value.location
  }
}

const handleLocationSearch = async () => {
  if (!locationKeyword.value.trim()) {
    showToast('请输入地点关键词')
    return
  }
  locationPage.value = 1
  locationResults.value = []
  locationFinished.value = false
  await fetchLocationList()
}

const loadMoreLocations = async () => {
  if (locationFinished.value || locationLoading.value) return
  if (!locationKeyword.value.trim()) return
  locationPage.value += 1
  await fetchLocationList(true)
}

const fetchLocationList = async (append = false) => {
  try {
    locationLoading.value = true
    locationSearchError.value = ''
    const pois = await searchPlacesByKeyword(locationKeyword.value, {
      page: locationPage.value,
      offset: 20,
      city: ''
    })

    if (append) {
      locationResults.value = locationResults.value.concat(pois)
    } else {
      locationResults.value = pois
    }

    locationFinished.value = !pois || pois.length < 20
  } catch (error) {
    console.error('搜索位置失败:', error)
    showToast(error.message || '搜索位置失败，请稍后重试')
    locationSearchError.value = error.message || '地图服务异常'
    locationFinished.value = true
  } finally {
    locationLoading.value = false
  }
}

const chooseLocation = (poi) => {
  const fullName = poi.name || ''
  const addr = poi.address || poi.adname || ''
  postForm.value.location = addr ? `${fullName}（${addr}）` : fullName
  showLocationPicker.value = false
}

const confirmManualLocation = () => {
  if (!manualLocation.value.trim()) {
    showToast('请输入位置')
    return
  }
  postForm.value.location = manualLocation.value.trim()
  showLocationPicker.value = false
}

const formatPoiAddress = (poi) => {
  const parts = []
  if (poi.pname) parts.push(poi.pname)
  if (poi.cityname && poi.cityname !== poi.pname) parts.push(poi.cityname)
  if (poi.adname) parts.push(poi.adname)
  if (poi.address) parts.push(poi.address)
  return parts.filter(Boolean).join(' · ')
}

const afterRead = (file) => {
  console.log('编辑动态上传图片:', file)
}

const beforeDelete = () => {
  return showConfirmDialog({
    title: '确认删除',
    message: '确定要删除这张图片吗？'
  })
}

const handleSave = async () => {
  if (saving.value || loading.value) return

  if (!postForm.value.content.trim()) {
    showToast('请输入动态内容')
    return
  }

  saving.value = true

  try {
    await updatePost(route.params.id, {
      content: postForm.value.content,
      location: postForm.value.location,
      type: postForm.value.type,
    })

    if (replacementFileList.value.length > 0) {
      const files = replacementFileList.value
        .map(item => item.file)
        .filter(Boolean)

      if (files.length > 0) {
        try {
          await uploadPostImages(route.params.id, files)
        } catch (uploadError) {
          console.error('编辑动态后上传图片失败:', uploadError)
          showToast(originalStatus.value === 'REJECTED' ? '内容已更新并重新提交审核，但图片替换失败' : '内容已更新，但图片替换失败')
          router.replace('/my-posts')
          return
        }
      }
    }

    showToast(originalStatus.value === 'REJECTED' ? '修改成功，已重新提交审核' : '修改成功，待审核中')
    router.replace('/my-posts')
  } catch (error) {
    console.error('编辑动态失败:', error)
    showToast(error.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

loadPost()
</script>

<style scoped>
.edit-post {
  min-height: 100vh;
  background: #f5f5f5;
}

.loading-wrap {
  min-height: calc(100vh - 46px);
  display: flex;
  align-items: center;
  justify-content: center;
}

.post-form {
  padding: 0;
}

.status-tip {
  margin: 12px 12px 0;
  padding: 12px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
}

.status-tip-pending {
  background: #fff7e8;
  color: #b26a00;
}

.status-tip-rejected {
  background: #fff2f0;
  color: #cf1322;
}

.current-images {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  padding: 10px 16px 16px;
}

.current-image {
  width: 100%;
  height: 110px;
  overflow: hidden;
  border-radius: 10px;
}

.image-empty {
  padding: 10px 16px 16px;
  font-size: 13px;
  color: #999;
}

.replace-hint {
  padding: 10px 16px 0;
  font-size: 12px;
  color: #999;
}

.manual-fill-box {
  margin-top: 12px;
  padding: 12px;
  border-radius: 12px;
  background: #fff7f0;
}

.manual-fill-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.manual-fill-hint {
  font-size: 12px;
  color: #999;
  margin-bottom: 10px;
}

.manual-fill-btn {
  margin-top: 10px;
}

:deep(.van-cell-group__title) {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding: 15px 16px 10px;
}

:deep(.van-field__control) {
  font-size: 14px;
  line-height: 1.5;
}

:deep(.van-uploader) {
  padding: 10px 16px 16px;
}

:deep(.van-radio-group) {
  padding: 10px 16px;
}
</style>
