<template>
  <div class="edit-activity">
    <van-nav-bar title="编辑活动" fixed>
      <template #left>
        <van-icon name="cross" @click="goBack" />
        <!-- 备用离开按钮（调试用） -->
        <van-icon name="arrow-left" @click="forceGoBack" style="margin-left: 10px;" />
        <!-- 简化离开按钮 -->
        <van-icon name="close" @click="simpleGoBack" style="margin-left: 10px;" />
      </template>
      <template #right>
        <van-button
          type="primary"
          size="small"
          :loading="saving"
          @click="handleSave"
        >
          保存
        </van-button>
        <!-- 调试按钮 -->
        <van-button
          type="default"
          size="small"
          @click="debugInfo"
          style="margin-left: 8px;"
        >
          调试
        </van-button>
      </template>
    </van-nav-bar>

    <!-- 加载状态 -->
    <van-loading v-if="loading" class="loading-container" vertical>
      加载活动信息中...
    </van-loading>

    <!-- 活动表单 -->
    <div v-else class="activity-form">
      <van-form @submit="handleSave">
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
            placeholder="请输入活动目的地"
            :rules="[{ required: true, message: '请输入目的地' }]"
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
          
          <!-- 备选方案：使用原生时间选择器 -->
          <div style="margin: 10px 0; padding: 10px; background: #f5f5f5; border-radius: 4px;">
            <p style="font-size: 12px; color: #666; margin-bottom: 10px;">如果上方时间选择器不工作，请使用下方备选方案：</p>
            <div style="display: flex; flex-direction: column; gap: 10px;">
              <div>
                <label style="font-size: 14px; color: #333;">开始时间：</label>
                <input 
                  type="datetime-local" 
                  v-model="activityForm.startTime"
                  style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 5px;"
                />
              </div>
              <div>
                <label style="font-size: 14px; color: #333;">结束时间：</label>
                <input 
                  type="datetime-local" 
                  v-model="activityForm.endTime"
                  style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 5px;"
                />
              </div>
              <div>
                <label style="font-size: 14px; color: #333;">报名截止：</label>
                <input 
                  type="datetime-local" 
                  v-model="activityForm.registrationDeadline"
                  style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px; margin-top: 5px;"
                />
              </div>
            </div>
          </div>
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
          <div v-if="originalImage" class="original-image">
            <p class="image-label">当前封面：</p>
            <van-image
              :src="originalImage"
              width="100"
              height="100"
              fit="cover"
              round
            />
          </div>
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
    <van-popup v-model="showTypePicker" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- 难度选择器 -->
    <van-popup v-model="showLevelPicker" position="bottom">
      <van-picker
        :columns="levelColumns"
        @confirm="onLevelConfirm"
        @cancel="showLevelPicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getActivity, updateActivity, uploadActivityImages } from '@/api/activities'
import { showToast, showConfirmDialog } from 'vant'
import ActivityDatePicker from '@/components/ActivityDatePicker.vue'

    const route = useRoute()
    const router = useRouter()
    
// 响应式数据
const loading = ref(true)
const saving = ref(false)
const activityId = ref(route.params.id)
const fileList = ref([])
const originalImage = ref('')
const originalData = ref(null) // 存储原始活动数据

// 表单数据
const activityForm = ref({
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
})

// 选择器状态
const showStartTimePicker = ref(false)
const showEndTimePicker = ref(false)
const showDeadlinePicker = ref(false)
const showTypePicker = ref(false)
const showLevelPicker = ref(false)


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
  console.log('点击返回按钮')
  console.log('是否有未保存更改:', hasUnsavedChanges())
  
  if (hasUnsavedChanges()) {
    console.log('检测到未保存的更改，显示确认对话框')
    showConfirmDialog({
      title: '确认离开',
      message: '当前修改未保存，确定要离开吗？'
    }).then(() => {
      console.log('用户确认离开')
      router.back()
    }).catch(() => {
      console.log('用户取消离开')
      // 用户取消
    })
  } else {
    console.log('没有未保存的更改，直接返回')
    router.back()
  }
}

// 强制离开方法（备用）
const forceGoBack = () => {
  console.log('强制离开页面')
  try {
    router.back()
  } catch (error) {
    console.error('router.back()失败:', error)
    // 如果router.back()失败，尝试其他方法
    try {
      window.history.back()
    } catch (historyError) {
      console.error('window.history.back()失败:', historyError)
      // 最后尝试跳转到首页
      window.location.href = '/'
    }
  }
}

// 简化离开方法（绕过所有检查）
const simpleGoBack = () => {
  console.log('简化离开页面（绕过所有检查）')
  try {
    router.back()
  } catch (error) {
    console.error('简化离开失败:', error)
    window.location.href = '/'
  }
}

// 调试信息方法
const debugInfo = () => {
  console.log('=== 编辑活动页面调试信息 ===')
  console.log('活动ID:', activityId.value)
  console.log('表单数据:', activityForm.value)
  console.log('原始数据:', originalData.value)
  console.log('是否有未保存更改:', hasUnsavedChanges())
  console.log('保存状态:', saving.value)
  console.log('路由信息:', router.currentRoute.value)
  console.log('浏览器历史记录长度:', window.history.length)
  console.log('当前URL:', window.location.href)
  
  // 显示调试信息给用户
  showToast(`调试信息已输出到控制台，请按F12查看`)
}

// 时间选择器打开函数
const openStartTimePicker = () => {
  console.log('点击开始时间')
  console.log('当前showStartTimePicker值:', showStartTimePicker.value)
  showStartTimePicker.value = true
  console.log('设置后showStartTimePicker值:', showStartTimePicker.value)
}

const openEndTimePicker = () => {
  console.log('点击结束时间')
  console.log('当前showEndTimePicker值:', showEndTimePicker.value)
  showEndTimePicker.value = true
  console.log('设置后showEndTimePicker值:', showEndTimePicker.value)
}

const openDeadlinePicker = () => {
  console.log('点击截止时间')
  console.log('当前showDeadlinePicker值:', showDeadlinePicker.value)
  showDeadlinePicker.value = true
  console.log('设置后showDeadlinePicker值:', showDeadlinePicker.value)
}

// 检查是否有未保存的更改
const hasUnsavedChanges = () => {
  if (!originalData.value) return false
  
  const current = activityForm.value
  const original = originalData.value
  
  return current.title !== original.title ||
         current.description !== original.description ||
         current.destination !== original.destination ||
         current.startTime !== original.startTime ||
         current.endTime !== original.endTime ||
         current.registrationDeadline !== original.registrationDeadline ||
         current.maxParticipants !== original.maxParticipants ||
         current.cost !== original.cost ||
         current.type !== original.type ||
         current.level !== original.level ||
         current.requirements !== original.requirements ||
         current.contactInfo !== original.contactInfo ||
         fileList.value.length > 0
}

// 加载活动数据
const loadActivity = async () => {
  try {
    loading.value = true
    const response = await getActivity(activityId.value)
    console.log('活动详情:', response)
    
    // 填充表单数据
    const formData = {
      title: response.title || '',
      description: response.description || '',
      destination: response.destination || '',
      startTime: response.startTime || '',
      endTime: response.endTime || '',
      registrationDeadline: response.registrationDeadline || '',
      maxParticipants: response.maxParticipants || '',
      cost: response.cost || '',
      type: getTypeText(response.type) || '',
      level: getLevelText(response.level) || '',
      requirements: response.requirements || '',
      contactInfo: response.contactInfo || ''
    }
    
    activityForm.value = formData
    
    // 保存原始数据用于比较
    originalData.value = { ...formData }
    
    // 设置原始图片
    if (response.coverImage) {
      originalImage.value = response.coverImage
    }
    
  } catch (error) {
    console.error('加载活动失败:', error)
    showToast('加载活动信息失败')
  } finally {
    loading.value = false
  }
}

// 图片处理
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

// 时间选择器处理
const onStartTimeConfirm = (result) => {
  console.log('开始时间选择:', result)
  if (result && result.value) {
    activityForm.value.startTime = result.value
    console.log('设置开始时间:', result.value)
  } else {
    showToast('时间格式错误，请重新选择')
  }
}

const onEndTimeConfirm = (result) => {
  console.log('结束时间选择:', result)
  if (result && result.value) {
    activityForm.value.endTime = result.value
    console.log('设置结束时间:', result.value)
  } else {
    showToast('时间格式错误，请重新选择')
  }
}

const onDeadlineConfirm = (result) => {
  console.log('截止时间选择:', result)
  if (result && result.value) {
    activityForm.value.registrationDeadline = result.value
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

// 保存活动
const handleSave = async () => {
  if (saving.value) return
  
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
  
  saving.value = true
  
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
    
    // 更新活动
    const response = await updateActivity(activityId.value, activityData)
    console.log('活动更新响应:', response)
    
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
          await uploadActivityImages(activityId.value, files)
          showToast('活动更新成功，图片上传成功')
        } else {
          showToast('活动更新成功')
        }
      } catch (uploadError) {
        console.error('图片上传失败:', uploadError)
        showToast('活动更新成功，但图片上传失败，请稍后重新上传')
      }
    } else {
      showToast('活动更新成功')
    }
    
    router.back()
  } catch (error) {
    console.error('更新活动失败:', error)
    showToast('更新失败，请重试')
  } finally {
    saving.value = false
  }
}

// 工具函数
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

const getTypeText = (value) => {
  const valueMap = {
    'HIKING': '徒步',
    'PHOTOGRAPHY': '摄影采风',
    'CAMPING': '露营',
    'CULTURAL': '文化体验',
    'ADVENTURE': '探险',
    'RELAXATION': '休闲',
    'OTHER': '其他'
  }
  return valueMap[value] || ''
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

const getLevelText = (value) => {
  const valueMap = {
    'EASY': '简单',
    'MEDIUM': '中等',
    'HARD': '困难',
    'EXPERT': '专家级'
  }
  return valueMap[value] || ''
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

onMounted(() => {
  console.log('EditActivity页面已加载，活动ID:', activityId.value)
  loadActivity()
})
</script>

<style scoped>
.edit-activity {
  padding-top: 40vw;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.loading-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 50vh;
}

.activity-form {
  padding: 0;
}

.original-image {
  padding: 10px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.image-label {
  font-size: 14px;
  color: #666;
  margin: 0;
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

:deep(.van-loading__text) {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}
</style>