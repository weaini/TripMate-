<template>
  <div class="create-post page-nav-only">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="发布动态" fixed>
      <template #left>
        <van-icon name="cross" @click="goBack" />
      </template>
      <template #right>
        <van-button
          type="primary"
          size="small"
          :loading="publishing"
          @click="handlePublish"
        >
          发布
        </van-button>
      </template>
    </van-nav-bar>
    <div class="nav-spacer"></div>

    <!-- 发布表单 -->
    <div class="post-form">
      <van-form @submit="handlePublish">
        <!-- 内容输入 -->
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

        <!-- 位置信息 -->
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

        <!-- 图片上传 -->
        <van-cell-group title="添加图片">
          <van-uploader
            v-model="fileList"
            multiple
            :max-count="9"
            :after-read="afterRead"
            :before-delete="beforeDelete"
            upload-text="添加图片"
          />
        </van-cell-group>

        <!-- 动态类型 -->
        <van-cell-group title="动态类型">
          <van-radio-group v-model="postForm.type" direction="horizontal">
            <van-radio name="DYNAMIC">实时动态</van-radio>
            <van-radio name="STRATEGY">攻略分享</van-radio>
            <van-radio name="ACTIVITY">活动相关</van-radio>
          </van-radio-group>
        </van-cell-group>

        <!-- 隐私设置 -->
        <van-cell-group title="隐私设置">
          <van-switch
            v-model="postForm.isPublic"
            size="20px"
          />
          <span class="privacy-text">
            {{ postForm.isPublic ? '公开' : '仅好友可见' }}
          </span>
        </van-cell-group>
      </van-form>
    </div>
    <!-- 位置选择弹窗（高德地点搜索） -->
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
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { createPost, uploadPostImages } from '@/api/posts'
import { showToast, showConfirmDialog } from 'vant'
import { searchPlacesByKeyword } from '@/api/amap'

const router = useRouter()

// 响应式数据
const publishing = ref(false)
const fileList = ref([])
const postForm = ref({
  content: '',
  location: '',
  type: 'DYNAMIC',
  isPublic: true
})

// 位置选择（高德）
const showLocationPicker = ref(false)
const locationKeyword = ref('')
const locationResults = ref([])
const locationLoading = ref(false)
// 初始不自动触发加载，等待用户点击“搜索”
const locationFinished = ref(true)
const locationPage = ref(1)
const locationSearchError = ref('')
const manualLocation = ref('')

// 方法
const goBack = () => {
  if (postForm.value.content || fileList.value.length > 0) {
    showConfirmDialog({
      title: '确认离开',
      message: '当前内容未保存，确定要离开吗？'
    }).then(() => {
      router.back()
    }).catch(() => {
      // 用户取消
    })
  } else {
    router.back()
  }
}

// 打开位置选择弹窗
const openLocationPicker = () => {
  showLocationPicker.value = true
  // 打开时重置列表状态，避免上一次搜索的“没有更多结果了”残留
  locationResults.value = []
  locationLoading.value = false
  locationFinished.value = true
  locationSearchError.value = ''
  if (postForm.value.location) {
    locationKeyword.value = postForm.value.location
    manualLocation.value = postForm.value.location
  }
}

// 搜索位置
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
  // 这里可以添加图片压缩逻辑
  console.log('图片上传:', file)
}

const beforeDelete = (file, detail) => {
  return new Promise((resolve) => {
    showConfirmDialog({
      title: '确认删除',
      message: '确定要删除这张图片吗？'
    }).then(() => {
      resolve(true)
    }).catch(() => {
      resolve(false)
    })
  })
}

const handlePublish = async () => {
  if (publishing.value) return
  
  if (!postForm.value.content.trim()) {
    showToast('请输入动态内容')
    return
  }
  
  publishing.value = true
  
  try {
    // 创建动态
    const postData = {
      content: postForm.value.content,
      location: postForm.value.location,
      type: postForm.value.type
    }
    
    const response = await createPost(postData)
    
    // 如果有图片，上传图片
    if (fileList.value.length > 0) {
      const files = fileList.value.map(item => item.file)
      await uploadPostImages(response.id, files)
    }
    
    showToast('发布成功')
    router.back()
  } catch (error) {
    console.error('发布失败:', error)
    showToast('发布失败，请重试')
  } finally {
    publishing.value = false
  }
}
</script>

<style scoped>
.create-post {
  padding: 0rem;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.post-form {
  padding: 0rem;
}

.privacy-text {
  margin-left: 10px;
  font-size: 14px;
  color: #666;
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
  padding: 10px 16px;
}

:deep(.van-radio-group) {
  padding: 10px 16px;
}

:deep(.van-radio) {
  margin-right: 20px;
}

:deep(.van-switch) {
  margin-right: 10px;
}
</style>
