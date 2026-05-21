<template>
  <div class="create-activity">
    <van-nav-bar title="创建活动" fixed>
      <template #left>
        <van-icon name="cross" @click="goBack" style="font-size: 18px; padding: 8px;" />
      </template>
      <template #right>
        <div class="nav-actions">
          <van-button
            plain
            type="primary"
            size="small"
            class="save-btn"
            @click="saveDraft"
          >
            保存
          </van-button>
          <van-button
            type="primary"
            size="small"
            :loading="creating"
            @click="handleCreate"
          >
            发布
          </van-button>
        </div>
      </template>
    </van-nav-bar>
  <div class="nav-spacer"></div>

    <!-- 活动表单 -->
    <div class="activity-form">
      <van-form @submit="handleCreate">
        <!-- 基本信息 -->
        <van-cell-group title="基本信息">
          <van-field
            v-model="activityForm.title"
            label="活动标题"
            placeholder="请输入活动标题"
            :rules="[{ required: true, message: '请输入活动标题' }]"
            maxlength="200"
            show-word-limit
          />
          <van-field
            v-model="activityForm.description"
            type="textarea"
            label="活动描述"
            placeholder="详细描述活动内容、行程安排等"
            :rules="[{ required: true, message: '请输入活动描述' }]"
            maxlength="2000"
            show-word-limit
            autosize
            rows="4"
          />
          <van-field
            v-model="activityForm.destination"
            label="目的地"
            placeholder="点击选择活动地点"
            readonly
            is-link
            @click="openDestinationPicker"
            :rules="[{ required: true, message: '请选择目的地' }]"
            maxlength="200"
            show-word-limit
          />
        </van-cell-group>

        <!-- 时间信息 -->
        <van-cell-group title="时间信息">
          <van-field
            v-model="activityForm.startTime"
            label="开始时间"
            placeholder="选择开始时间"
            readonly
            is-link
            @click="openStartTimePicker"
            :rules="[{ required: true, message: '请选择开始时间' }]"
          />
          <van-field
            v-model="activityForm.endTime"
            label="结束时间"
            placeholder="选择结束时间"
            readonly
            is-link
            @click="openEndTimePicker"
            :rules="[{ required: true, message: '请选择结束时间' }]"
          />
          <van-field
            v-model="activityForm.registrationDeadline"
            label="报名截止"
            placeholder="选择报名截止时间"
            readonly
            is-link
            @click="openDeadlinePicker"
            :rules="[{ required: true, message: '请选择报名截止时间' }]"
          />
        </van-cell-group>

        <!-- 活动详情 -->
        <van-cell-group title="活动详情">
          <van-field
            v-model="activityForm.maxParticipants"
            label="最大人数"
            placeholder="请输入最大参与人数"
            type="number"
            :rules="[{ required: true, message: '请输入最大参与人数' }]"
          />
          <van-field
            v-model="activityForm.cost"
            label="活动费用"
            placeholder="请输入活动费用（元）"
            type="number"
          />
          <van-field
            v-model="activityForm.type"
            label="活动类型"
            placeholder="选择活动类型"
            readonly
            is-link
            @click="showTypePicker = true"
            :rules="[{ required: true, message: '请选择活动类型' }]"
          />
          <van-field
            v-model="activityForm.level"
            label="难度等级"
            placeholder="选择难度等级"
            readonly
            is-link
            @click="showLevelPicker = true"
            :rules="[{ required: true, message: '请选择难度等级' }]"
          />
        </van-cell-group>

        <!-- 其他信息 -->
        <van-cell-group title="其他信息">
          <van-field
            v-model="activityForm.requirements"
            type="textarea"
            label="参与要求"
            placeholder="请说明参与活动的要求（可选）"
            maxlength="500"
            show-word-limit
            autosize
            rows="3"
          />
          <van-field
            v-model="activityForm.contactInfo"
            label="联系方式"
            placeholder="请输入联系方式（可选）"
            maxlength="200"
            show-word-limit
          />
        </van-cell-group>

        <!-- 封面图片 -->
        <van-cell-group title="封面图片">
          <van-uploader
            v-model="fileList"
            :max-count="1"
            :after-read="afterRead"
            :before-delete="beforeDelete"
            upload-text="选择封面"
          />
        </van-cell-group>
      </van-form>
    </div>

    <!-- 自定义时间选择器 -->
    <ActivityDatePicker
      v-model:show="showStartTimePicker"
      :value="activityForm.startTime"
      type="datetime"
      title="选择开始时间"
      :min-date="new Date()"
      :max-date="new Date(2030, 11, 31)"
      @confirm="onStartTimeConfirm"
      @cancel="showStartTimePicker = false"
    />

    <ActivityDatePicker
      v-model:show="showEndTimePicker"
      :value="activityForm.endTime"
      type="datetime"
      title="选择结束时间"
      :min-date="new Date()"
      :max-date="new Date(2030, 11, 31)"
      @confirm="onEndTimeConfirm"
      @cancel="showEndTimePicker = false"
    />

    <ActivityDatePicker
      v-model:show="showDeadlinePicker"
      :value="activityForm.registrationDeadline"
      type="datetime"
      title="选择报名截止时间"
      :min-date="new Date()"
      :max-date="new Date(2030, 11, 31)"
      @confirm="onDeadlineConfirm"
      @cancel="showDeadlinePicker = false"
    />

    <!-- 类型选择器 -->
    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- 难度选择器 -->
    <van-popup v-model:show="showLevelPicker" position="bottom">
      <van-picker
        :columns="levelColumns"
        @confirm="onLevelConfirm"
        @cancel="showLevelPicker = false"
      />
    </van-popup>

    <!-- 目的地选择（高德地点搜索） -->
    <van-popup v-model:show="showDestinationPicker" position="bottom" round>
      <div class="destination-popup">
        <div class="destination-header">
          <h3>选择目的地</h3>
          <van-icon name="cross" @click="showDestinationPicker = false" />
        </div>
        <div class="destination-search">
          <van-search
            v-model="destinationKeyword"
            placeholder="输入城市/景点/地址关键词"
            show-action
            @search="handleDestinationSearch"
          >
            <template #action>
              <span @click="handleDestinationSearch">搜索</span>
            </template>
          </van-search>
        </div>
        <div class="destination-result">
          <van-list
            v-model:loading="destinationLoading"
            :finished="destinationFinished"
            finished-text="没有更多结果了"
            @load="loadMoreDestinations"
          >
            <van-cell
              v-for="item in destinationResults"
              :key="item.id"
              clickable
              :title="item.name"
              :label="formatPoiAddress(item)"
              @click="chooseDestination(item)"
            />
          </van-list>
          <div
            v-if="!destinationLoading && destinationResults.length === 0 && destinationKeyword"
            class="destination-empty"
          >
            <van-empty image="search" description="暂未找到匹配地点" />
          </div>
          <div v-if="destinationSearchError" class="manual-fill-box">
            <div class="manual-fill-title">地图服务异常，改为手动填写</div>
            <div class="manual-fill-hint">{{ destinationSearchError }}</div>
            <van-field
              v-model="manualDestination"
              placeholder="请输入目的地"
              maxlength="200"
              clearable
            />
            <van-button type="primary" block round class="manual-fill-btn" @click="confirmManualDestination">
              使用手动填写
            </van-button>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { createActivity, uploadActivityImages } from '@/api/activities'
import { showToast, showConfirmDialog } from 'vant'
import ActivityDatePicker from '@/components/ActivityDatePicker.vue'
import { searchPlacesByKeyword } from '@/api/amap'

const router = useRouter()
const ACTIVITY_DRAFT_KEY = 'create-activity-draft'
const EMPTY_ACTIVITY_FORM = {
  title: '',
  description: '',
  destination: '',
  startTime: '',
  endTime: '',
  registrationDeadline: '',
  maxParticipants: '',
  cost: '',
  type: '',
  level: '',
  requirements: '',
  contactInfo: ''
}

// 响应式数据
const creating = ref(false)
const fileList = ref([])
const lastSavedDraftSnapshot = ref(JSON.stringify(EMPTY_ACTIVITY_FORM))

// 表单数据
const activityForm = ref({ ...EMPTY_ACTIVITY_FORM })

// 选择器状态
const showStartTimePicker = ref(false)
const showEndTimePicker = ref(false)
const showDeadlinePicker = ref(false)
const showTypePicker = ref(false)
const showLevelPicker = ref(false)
// 目的地地点搜索状态（高德）
const showDestinationPicker = ref(false)
const destinationKeyword = ref('')
const destinationResults = ref([])
const destinationLoading = ref(false)
// 初始不自动触发加载
const destinationFinished = ref(true)
const destinationPage = ref(1)
const destinationSearchError = ref('')
const manualDestination = ref('')

// 选择器选项
const typeColumns = [
  { text: '徒步', value: 'HIKING' },
  { text: '摄影采风', value: 'PHOTOGRAPHY' },
  { text: '露营', value: 'CAMPING' },
  { text: '文化体验', value: 'CULTURAL' },
  { text: '探险', value: 'ADVENTURE' },
  { text: '休闲', value: 'RELAXATION' },
  { text: '其他', value: 'OTHER' }
]

const levelColumns = [
  { text: '简单', value: 'EASY' },
  { text: '中等', value: 'MEDIUM' },
  { text: '困难', value: 'HARD' },
  { text: '专家级', value: 'EXPERT' }
]

// 方法
const goBack = () => {
  console.log('点击退出按钮')
  if (hasUnsavedChanges()) {
    showConfirmDialog({
      title: '确认离开',
      message: '当前内容未保存，确定要离开吗？'
    }).then(() => {
      console.log('用户确认离开')
      // 尝试多种返回方式
      try {
        if (window.history.length > 1) {
          router.back()
        } else {
          router.push('/activities')
        }
      } catch (error) {
        console.error('路由返回失败:', error)
        router.push('/activities')
      }
    }).catch(() => {
      console.log('用户取消离开')
      // 用户取消
    })
  } else {
    console.log('直接返回')
    try {
      if (window.history.length > 1) {
        router.back()
      } else {
        router.push('/activities')
      }
    } catch (error) {
      console.error('路由返回失败:', error)
      router.push('/activities')
    }
  }
}

const saveDraft = () => {
  try {
    const payload = {
      form: { ...activityForm.value },
      savedAt: Date.now()
    }
    localStorage.setItem(ACTIVITY_DRAFT_KEY, JSON.stringify(payload))
    lastSavedDraftSnapshot.value = getFormSnapshot(activityForm.value)
    showToast('草稿已保存')
  } catch (error) {
    console.error('保存草稿失败:', error)
    showToast('保存草稿失败')
  }
}

const restoreDraft = async () => {
  const saved = localStorage.getItem(ACTIVITY_DRAFT_KEY)
  if (!saved) {
    lastSavedDraftSnapshot.value = getFormSnapshot(EMPTY_ACTIVITY_FORM)
    return
  }
  try {
    await showConfirmDialog({
      title: '恢复草稿',
      message: '检测到你上次未完成的活动草稿，是否恢复？'
    })
    const parsed = JSON.parse(saved)
    const savedForm = parsed?.form ? parsed.form : parsed
    activityForm.value = {
      ...activityForm.value,
      ...savedForm
    }
    lastSavedDraftSnapshot.value = getFormSnapshot(activityForm.value)
    showToast('草稿已恢复')
  } catch {
    // 用户取消恢复时，也记录当前基线，避免空白页直接误报“未保存”
    lastSavedDraftSnapshot.value = getFormSnapshot(activityForm.value)
  }
}

const clearDraft = () => {
  localStorage.removeItem(ACTIVITY_DRAFT_KEY)
  lastSavedDraftSnapshot.value = getFormSnapshot(EMPTY_ACTIVITY_FORM)
}

// 打开目的地选择弹窗
const openDestinationPicker = () => {
  showDestinationPicker.value = true
  if (activityForm.value.destination) {
    destinationKeyword.value = activityForm.value.destination
    manualDestination.value = activityForm.value.destination
  }
  destinationResults.value = []
  destinationLoading.value = false
  destinationFinished.value = true
  destinationSearchError.value = ''
}

// 高德地点搜索：首次/重新搜索
const handleDestinationSearch = async () => {
  if (!destinationKeyword.value.trim()) {
    showToast('请输入地点关键词')
    return
  }
  destinationPage.value = 1
  destinationFinished.value = false
  destinationResults.value = []
  await fetchDestinationList()
}

// 加载更多地点（翻页）
const loadMoreDestinations = async () => {
  if (destinationFinished.value || destinationLoading.value) return
  if (!destinationKeyword.value.trim()) return
  destinationPage.value += 1
  await fetchDestinationList(true)
}

// 实际请求高德接口
const fetchDestinationList = async (append = false) => {
  try {
    destinationLoading.value = true
    destinationSearchError.value = ''
    const pois = await searchPlacesByKeyword(destinationKeyword.value, {
      page: destinationPage.value,
      offset: 20,
      city: '' // 如需限制城市，可在这里传入城市名称或代码
    })

    if (append) {
      destinationResults.value = destinationResults.value.concat(pois)
    } else {
      destinationResults.value = pois
    }

    destinationFinished.value = !pois || pois.length < 20
  } catch (error) {
    console.error('搜索目的地失败:', error)
    showToast(error.message || '搜索目的地失败，请稍后重试')
    destinationSearchError.value = error.message || '地图服务异常'
    destinationFinished.value = true
  } finally {
    destinationLoading.value = false
  }
}

// 选择某个地点
const chooseDestination = (poi) => {
  const fullName = poi.name || ''
  const addr = poi.address || poi.adname || ''
  activityForm.value.destination = addr ? `${fullName}（${addr}）` : fullName
  showDestinationPicker.value = false
}

const confirmManualDestination = () => {
  if (!manualDestination.value.trim()) {
    showToast('请输入目的地')
    return
  }
  activityForm.value.destination = manualDestination.value.trim()
  showDestinationPicker.value = false
}

// 格式化地点的展示地址
const formatPoiAddress = (poi) => {
  const parts = []
  if (poi.pname) parts.push(poi.pname)
  if (poi.cityname && poi.cityname !== poi.pname) parts.push(poi.cityname)
  if (poi.adname) parts.push(poi.adname)
  if (poi.address) parts.push(poi.address)
  return parts.filter(Boolean).join(' · ')
}

const getFormSnapshot = (form) => {
  return JSON.stringify({
    title: form.title || '',
    description: form.description || '',
    destination: form.destination || '',
    startTime: form.startTime || '',
    endTime: form.endTime || '',
    registrationDeadline: form.registrationDeadline || '',
    maxParticipants: form.maxParticipants || '',
    cost: form.cost || '',
    type: form.type || '',
    level: form.level || '',
    requirements: form.requirements || '',
    contactInfo: form.contactInfo || ''
  })
}

const hasFormData = () => {
  return getFormSnapshot(activityForm.value) !== getFormSnapshot(EMPTY_ACTIVITY_FORM)
}

const hasUnsavedChanges = () => {
  if (!hasFormData()) return false
  return getFormSnapshot(activityForm.value) !== lastSavedDraftSnapshot.value
}

const afterRead = (file) => {
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

const onStartTimeConfirm = (result) => {
  console.log('开始时间选择:', result)
  if (result && result.value) {
    activityForm.value.startTime = result.value
    showStartTimePicker.value = false
    console.log('设置开始时间:', result.value)
  } else {
    showToast('时间格式错误，请重新选择')
  }
}

const onEndTimeConfirm = (result) => {
  console.log('结束时间选择:', result)
  if (result && result.value) {
    activityForm.value.endTime = result.value
    showEndTimePicker.value = false
    console.log('设置结束时间:', result.value)
  } else {
    showToast('时间格式错误，请重新选择')
  }
}

const onDeadlineConfirm = (result) => {
  console.log('截止时间选择:', result)
  if (result && result.value) {
    activityForm.value.registrationDeadline = result.value
    showDeadlinePicker.value = false
    console.log('设置截止时间:', result.value)
  } else {
    showToast('时间格式错误，请重新选择')
  }
}

const onTypeConfirm = ({ selectedOptions }) => {
  activityForm.value.type = selectedOptions[0].text
  showTypePicker.value = false
}

const onLevelConfirm = ({ selectedOptions }) => {
  activityForm.value.level = selectedOptions[0].text
  showLevelPicker.value = false
}

// 时间选择器打开方法
const openStartTimePicker = () => {
  console.log('打开开始时间选择器')
  showStartTimePicker.value = true
}

const openEndTimePicker = () => {
  console.log('打开结束时间选择器')
  showEndTimePicker.value = true
}

const openDeadlinePicker = () => {
  console.log('打开截止时间选择器')
  showDeadlinePicker.value = true
}


const handleCreate = async () => {
  if (creating.value) return
  
  // 验证表单
  if (!activityForm.value.title.trim()) {
    showToast('请输入活动标题')
    return
  }
  
  if (!activityForm.value.description.trim()) {
    showToast('请输入活动描述')
    return
  }
  
  if (!activityForm.value.destination.trim()) {
    showToast('请输入目的地')
    return
  }
  
  if (!activityForm.value.startTime) {
    showToast('请选择开始时间')
    return
  }
  
  if (!activityForm.value.endTime) {
    showToast('请选择结束时间')
    return
  }
  
  if (!activityForm.value.registrationDeadline) {
    showToast('请选择报名截止时间')
    return
  }
  
  if (!activityForm.value.maxParticipants) {
    showToast('请输入最大参与人数')
    return
  }
  
  if (!activityForm.value.type) {
    showToast('请选择活动类型')
    return
  }
  
  if (!activityForm.value.level) {
    showToast('请选择难度等级')
    return
  }
  
  // 验证费用（如果填写了）
  if (activityForm.value.cost !== null && activityForm.value.cost !== undefined && activityForm.value.cost !== '') {
    const costValue = parseFloat(activityForm.value.cost)
    if (isNaN(costValue) || costValue < 0) {
      showToast('费用必须为有效的正数')
      return
    }
  }
  
  // 验证最大参与人数
  if (activityForm.value.maxParticipants <= 0) {
    showToast('最大参与人数必须大于0')
    return
  }
  
  // 验证时间逻辑
  const startTime = new Date(activityForm.value.startTime)
  const endTime = new Date(activityForm.value.endTime)
  const deadline = new Date(activityForm.value.registrationDeadline)
  
  if (startTime >= endTime) {
    showToast('结束时间必须晚于开始时间')
    return
  }
  
  if (deadline >= startTime) {
    showToast('报名截止时间必须早于活动开始时间')
    return
  }
  
  creating.value = true
  
  try {
    const activityData = {
      title: activityForm.value.title,
      description: activityForm.value.description,
      destination: activityForm.value.destination,
      startTime: activityForm.value.startTime,
      endTime: activityForm.value.endTime,
      registrationDeadline: activityForm.value.registrationDeadline,
      maxParticipants: parseInt(activityForm.value.maxParticipants),
      cost: activityForm.value.cost ? parseFloat(activityForm.value.cost) : null,
      type: getTypeValue(activityForm.value.type),
      level: getLevelValue(activityForm.value.level),
      requirements: activityForm.value.requirements,
      contactInfo: activityForm.value.contactInfo
    }
    
    // 创建活动
    const response = await createActivity(activityData)
    console.log('活动创建响应:', response)
    
    // 如果有上传的图片，上传图片
    if (fileList.value.length > 0) {
      try {
        // 提取文件对象
        const files = fileList.value.map(item => {
          if (item.file) {
            return item.file
          } else if (item.content) {
            // 如果是base64格式，需要转换为File对象
            return dataURLtoFile(item.content, item.name || 'image.jpg')
          }
          return item
        }).filter(file => file instanceof File)
        
        if (files.length > 0) {
          await uploadActivityImages(response.id, files)
          showToast('活动创建成功，图片上传成功，等待审核')
        } else {
          showToast('活动创建成功，等待审核')
        }
      } catch (uploadError) {
        console.error('图片上传失败:', uploadError)
        showToast('活动创建成功，但图片上传失败，请稍后重新上传')
      }
    } else {
      showToast('活动创建成功，等待审核')
    }

    clearDraft()
    router.back()
  } catch (error) {
    console.error('创建活动失败:', error)
    showToast('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

const getTypeValue = (text) => {
  const typeMap = {
    '徒步': 'HIKING',
    '摄影采风': 'PHOTOGRAPHY',
    '露营': 'CAMPING',
    '文化体验': 'CULTURAL',
    '探险': 'ADVENTURE',
    '休闲': 'RELAXATION',
    '其他': 'OTHER'
  }
  return typeMap[text] || 'OTHER'
}

const getLevelValue = (text) => {
  const levelMap = {
    '简单': 'EASY',
    '中等': 'MEDIUM',
    '困难': 'HARD',
    '专家级': 'EXPERT'
  }
  return levelMap[text] || 'EASY'
}

// 将base64数据URL转换为File对象
const dataURLtoFile = (dataurl, filename) => {
  const arr = dataurl.split(',')
  const mime = arr[0].match(/:(.*?);/)[1]
  const bstr = atob(arr[1])
  let n = bstr.length
  const u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new File([u8arr], filename, { type: mime })
}

// 组件挂载时初始化
onMounted(async () => {
  console.log('CreateActivity组件已挂载')
  await restoreDraft()
})
</script>

<style scoped>
.create-activity {
  padding-top: 0;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.activity-form {
  padding: 0;
}

.nav-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.save-btn {
  background: #fff;
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
</style>
