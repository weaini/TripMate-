<template>
  <div class="edit-profile page-nav-only">
    <van-nav-bar title="编辑资料" fixed left-arrow @click-left="goBack">
      <template #right>
        <van-button type="primary" size="small" plain :loading="saving" @click="handleSave">
          保存
        </van-button>
      </template>
    </van-nav-bar>
    <div class="nav-spacer" />

    <div class="edit-body">
      <!-- 头像 -->
      <van-cell-group inset title="头像" class="block">
        <van-cell>
          <template #title>
            <div class="avatar-row">
              <div class="avatar-wrap" @click="showAvatarPicker = true">
                <van-image
                  v-if="getAvatarUrl()"
                  :src="getAvatarUrl()"
                  round
                  width="72"
                  height="72"
                  fit="cover"
                />
                <div v-else class="avatar-placeholder">
                  <van-icon name="user-o" size="32" />
                </div>
                <div class="avatar-badge">
                  <van-loading v-if="uploadingAvatar" size="16" color="#fff" />
                  <van-icon v-else name="photograph" size="18" color="#fff" />
                </div>
              </div>
              <div class="avatar-hint">
                <span class="hint-main">点击更换头像</span>
                <span class="hint-sub">JPG / PNG，不超过 10MB；保存时一并上传</span>
              </div>
            </div>
          </template>
        </van-cell>
      </van-cell-group>

      <van-form ref="formRef" @submit="handleSave">
        <van-cell-group inset title="基本资料" class="block">
          <van-field
            v-model="formData.nickname"
            name="nickname"
            label="昵称"
            placeholder="2–20 个字符"
            maxlength="20"
            show-word-limit
            :rules="[{ required: true, message: '请填写昵称' }]"
          />
          <van-field
            v-model="formData.bio"
            name="bio"
            label="简介"
            type="textarea"
            placeholder="一句话介绍自己"
            maxlength="100"
            show-word-limit
            rows="2"
            autosize
          />
          <van-field
            v-model="formData.email"
            name="email"
            label="邮箱"
            placeholder="用于登录与通知"
            :rules="[
              { required: true, message: '请填写邮箱' },
              { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '邮箱格式不正确' },
            ]"
          />
          <van-field v-model="formData.phone" name="phone" label="手机" placeholder="选填" maxlength="20" />
        </van-cell-group>

        <van-cell-group inset title="更多信息" class="block">
          <van-field
            :model-value="genderLabel"
            name="gender"
            label="性别"
            placeholder="请选择"
            readonly
            is-link
            @click="showGenderPicker = true"
          />
          <van-field
            :model-value="formData.birthday || '未设置'"
            name="birthday"
            label="生日"
            readonly
            is-link
            @click="showBirthdayPicker = true"
          />
          <van-field
            v-model="formData.location"
            name="location"
            label="所在地"
            placeholder="点击选择所在地（可选）"
            maxlength="50"
            readonly
            is-link
            @click="openLocationPicker"
          />
          <van-field
            v-model="formData.interests"
            name="interests"
            label="兴趣"
            placeholder="多个用逗号分隔"
            maxlength="100"
          />
        </van-cell-group>

        <div class="submit-wrap">
          <van-button round block type="primary" native-type="submit" :loading="saving">
            保存资料
          </van-button>
        </div>
      </van-form>
    </div>

    <van-popup v-model:show="showGenderPicker" position="bottom" round>
      <van-picker
        :columns="genderColumns"
        @confirm="onGenderConfirm"
        @cancel="showGenderPicker = false"
      />
    </van-popup>

    <ActivityDatePicker
      v-model:show="showBirthdayPicker"
      :value="formData.birthday"
      type="date"
      title="选择生日"
      :min-date="new Date(1940, 0, 1)"
      :max-date="new Date()"
      @confirm="onBirthdayConfirm"
      @cancel="showBirthdayPicker = false"
    />

    <van-popup v-model:show="showAvatarPicker" position="bottom" round>
      <div class="picker-sheet">
        <div class="sheet-title">更换头像</div>
        <van-uploader
          v-model="fileList"
          :max-count="1"
          accept="image/*"
          :after-read="afterRead"
          :preview-image="false"
        >
          <van-button block type="primary" plain>从相册选择</van-button>
        </van-uploader>
        <van-button block class="mt-8" @click="showAvatarPicker = false">取消</van-button>
      </div>
    </van-popup>

    <!-- 所在地选择弹窗（高德地点搜索） -->
    <van-popup v-model:show="showLocationPicker" position="bottom" round>
      <div class="destination-popup">
        <div class="destination-header">
          <h3>选择所在地</h3>
          <van-icon name="cross" @click="showLocationPicker = false" />
        </div>
        <div class="destination-search">
          <van-search
            v-model="locationKeyword"
            placeholder="输入城市/地区/地址关键词"
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
              placeholder="请输入所在地"
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
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog } from 'vant'
import { updateUserProfile, uploadAvatar } from '@/api/user'
import { getFullAvatarUrl } from '@/utils/avatar'
import ActivityDatePicker from '@/components/ActivityDatePicker.vue'
import { searchPlacesByKeyword } from '@/api/amap'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const saving = ref(false)
const uploadingAvatar = ref(false)
const showGenderPicker = ref(false)
const showBirthdayPicker = ref(false)
const showAvatarPicker = ref(false)
const fileList = ref([])
const avatarPreview = ref('')
const selectedAvatarFile = ref(null)
// 所在地选择（高德）
const showLocationPicker = ref(false)
const locationKeyword = ref('')
const locationResults = ref([])
const locationLoading = ref(false)
// 初始不触发加载
const locationFinished = ref(true)
const locationPage = ref(1)
const locationSearchError = ref('')
const manualLocation = ref('')
/** 进入页时的快照，用于未保存提示 */
const initialSnapshot = ref('')

const formData = ref({
  nickname: '',
  bio: '',
  email: '',
  phone: '',
  gender: '',
  birthday: '',
  location: '',
  interests: '',
  avatar: '',
})

const genderColumns = [
  { text: '男', value: 'MALE' },
  { text: '女', value: 'FEMALE' },
  { text: '保密', value: 'SECRET' },
]

const genderLabel = computed(() => {
  const g = formData.value.gender
  const row = genderColumns.find((c) => c.value === g)
  return row ? row.text : ''
})

function normalizeBirthday(v) {
  if (!v) return ''
  if (typeof v === 'string') {
    if (/^\d{4}-\d{2}-\d{2}/.test(v)) return v.slice(0, 10)
    return v
  }
  if (Array.isArray(v) && v.length >= 3) {
    const [y, m, d] = v
    return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
  }
  return ''
}

function snapshot() {
  return JSON.stringify(formData.value)
}

function hasChanges() {
  return snapshot() !== initialSnapshot.value || selectedAvatarFile.value != null
}

const goBack = async () => {
  if (!hasChanges()) {
    router.back()
    return
  }
  try {
    await showConfirmDialog({
      title: '未保存的修改',
      message: '确定离开？已编辑内容将丢失。',
    })
    router.back()
  } catch {
    /* cancel */
  }
}

const getAvatarUrl = () => {
  if (avatarPreview.value) return avatarPreview.value
  if (formData.value.avatar) return getFullAvatarUrl(formData.value.avatar)
  return ''
}

const onGenderConfirm = ({ selectedOptions }) => {
  const opt = selectedOptions?.[0]
  formData.value.gender = opt?.value || ''
  showGenderPicker.value = false
}

const onBirthdayConfirm = (payload) => {
  if (payload?.value) formData.value.birthday = String(payload.value).slice(0, 10)
  showBirthdayPicker.value = false
}

const afterRead = (file) => {
  const f = file.file
  if (!f?.type?.startsWith('image/')) {
    showToast('请选择图片')
    return
  }
  if (f.size > 10 * 1024 * 1024) {
    showToast('图片不能超过 10MB')
    return
  }
  const reader = new FileReader()
  reader.onload = (e) => {
    avatarPreview.value = e.target.result
    selectedAvatarFile.value = f
  }
  reader.readAsDataURL(f)
  showAvatarPicker.value = false
  fileList.value = []
  showToast('头像已选择，点保存上传')
}

const handleSave = async () => {
  if (saving.value) return
  if (!userStore.isLoggedIn) {
    router.push('/login')
    return
  }
  try {
    await formRef.value?.validate?.()
  } catch {
    return
  }

  saving.value = true
  try {
    if (selectedAvatarFile.value) {
      uploadingAvatar.value = true
      try {
        const res = await uploadAvatar(selectedAvatarFile.value)
        const url = res?.avatarUrl ?? res?.data?.avatarUrl
        if (url) formData.value.avatar = url
        else throw new Error('no url')
      } finally {
        uploadingAvatar.value = false
      }
    }

    const payload = {
      nickname: formData.value.nickname.trim(),
      bio: formData.value.bio || '',
      email: formData.value.email.trim(),
      phone: formData.value.phone || '',
      gender: formData.value.gender || '',
      birthday: formData.value.birthday || '',
      location: formData.value.location || '',
      interests: formData.value.interests || '',
      avatar: formData.value.avatar || '',
    }

    const updated = await updateUserProfile(payload)
    userStore.setUserInfo(updated)
    avatarPreview.value = ''
    selectedAvatarFile.value = null
    initialSnapshot.value = snapshot()
    showToast('已保存')
    router.back()
  } catch (e) {
    const msg = e.response?.data?.message || e.message || '保存失败'
    showToast(typeof msg === 'string' ? msg : '保存失败')
  } finally {
    saving.value = false
  }
}

// 打开所在地选择弹窗
const openLocationPicker = () => {
  showLocationPicker.value = true
  if (formData.value.location) {
    locationKeyword.value = formData.value.location
    manualLocation.value = formData.value.location
  }
  locationResults.value = []
  locationLoading.value = false
  locationFinished.value = true
  locationSearchError.value = ''
}

// 搜索所在地
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
    console.error('搜索所在地失败:', error)
    showToast(error.message || '搜索所在地失败，请稍后重试')
    locationSearchError.value = error.message || '地图服务异常'
    locationFinished.value = true
  } finally {
    locationLoading.value = false
  }
}

const chooseLocation = (poi) => {
  const fullName = poi.name || ''
  const addr = poi.address || poi.adname || ''
  formData.value.location = addr ? `${fullName}（${addr}）` : fullName
  showLocationPicker.value = false
}

const confirmManualLocation = () => {
  if (!manualLocation.value.trim()) {
    showToast('请输入所在地')
    return
  }
  formData.value.location = manualLocation.value.trim()
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

const initFormData = async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch {
      showToast('请重新登录')
      router.replace('/login')
      return
    }
  }
  const u = userStore.userInfo
  if (!u) {
    router.replace('/login')
    return
  }
  formData.value = {
    nickname: u.nickname || '',
    bio: u.bio || '',
    email: u.email || '',
    phone: u.phone || '',
    gender: u.gender || '',
    birthday: normalizeBirthday(u.birthday),
    location: u.location || '',
    interests: u.interests || '',
    avatar: u.avatar || '',
  }
  initialSnapshot.value = snapshot()
}

onMounted(() => initFormData())
</script>

<style scoped>
.edit-profile {
  min-height: 100vh;
  background: var(--page-bg, #f5f6f7);
}
.edit-body {
  padding: 12px 12px calc(24px + env(safe-area-inset-bottom));
}
.block {
  margin-bottom: 12px;
}
.avatar-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 8px 0;
}
.avatar-wrap {
  position: relative;
  width: 72px;
  height: 72px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
}
.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
.avatar-badge {
  position: absolute;
  right: 0;
  bottom: 0;
  width: 28px;
  height: 28px;
  background: rgba(0, 0, 0, 0.55);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-hint {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.hint-main {
  font-size: 15px;
  font-weight: 500;
  color: #333;
}
.hint-sub {
  font-size: 12px;
  color: #999;
}
.submit-wrap {
  margin: 20px 0 32px;
  padding: 0 4px;
}
.picker-sheet {
  padding: 16px 16px calc(16px + env(safe-area-inset-bottom));
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
.sheet-title {
  text-align: center;
  font-weight: 600;
  margin-bottom: 16px;
}
.mt-8 {
  margin-top: 8px;
}
:deep(.van-cell-group__title) {
  color: #646566;
  font-size: 13px;
}
</style>
