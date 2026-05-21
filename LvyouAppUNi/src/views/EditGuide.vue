<template>
  <div class="edit-guide">
    <!-- 导航栏 -->
    <van-nav-bar title="编辑攻略" fixed>
      <template #left>
        <van-icon name="arrow-left" @click="goBack" />
      </template>
      <template #right>
        <van-button type="primary" size="small" @click="saveDraft">保存草稿</van-button>
      </template>
    </van-nav-bar>

    <div v-if="loading" class="loading">
      <van-loading type="spinner" />
    </div>

    <div v-else-if="guide" class="form-container">
      <!-- 攻略标题 -->
      <van-field
        v-model="form.title"
        label="标题"
        placeholder="请输入攻略标题"
        maxlength="200"
        show-word-limit
        required
      />

      <!-- 攻略类型 -->
      <van-field
        v-model="form.typeDisplayName"
        label="类型"
        placeholder="请选择攻略类型"
        readonly
        is-link
        @click="showTypePicker = true"
        required
      />

      <!-- 封面图片 -->
      <div class="cover-section">
        <div class="section-label">封面图片</div>
        <van-uploader
          v-model="fileList"
          :max-count="1"
          :after-read="afterRead"
          :before-delete="beforeDelete"
        >
          <div class="upload-placeholder">
            <van-icon name="plus" size="24" />
            <div>点击上传封面</div>
          </div>
        </van-uploader>
      </div>

      <!-- 攻略摘要 -->
      <van-field
        v-model="form.summary"
        label="摘要"
        type="textarea"
        placeholder="请输入攻略摘要（可选）"
        maxlength="500"
        show-word-limit
        rows="3"
      />

      <!-- 攻略内容 -->
      <div class="content-section">
        <div class="section-label">攻略内容</div>
        <van-field
          v-model="form.content"
          type="textarea"
          placeholder="请详细描述您的攻略内容..."
          maxlength="10000"
          show-word-limit
          rows="15"
          required
        />
      </div>

      <!-- 发布选项 -->
      <div class="publish-options">
        <van-cell-group>
          <van-cell title="立即发布" center>
            <template #right-icon>
              <van-switch v-model="publishNow" />
            </template>
          </van-cell>
        </van-cell-group>
      </div>

      <!-- 发布按钮 -->
      <div class="publish-section">
        <van-button
          type="primary"
          size="large"
          block
          @click="updateGuide"
          :loading="updating"
        >
          {{ publishNow ? '更新攻略' : '保存草稿' }}
        </van-button>
      </div>
    </div>

    <!-- 类型选择器 -->
    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="typeOptions"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>
  </div>
</template>

<script>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getGuideById, updateGuide as updateGuideAPI, uploadGuideCover } from '@/api/guides'

export default {
  name: 'EditGuide',
  setup() {
    const route = useRoute()
    const router = useRouter()
    
    // 响应式数据
    const guide = ref(null)
    const loading = ref(true)
    const form = reactive({
      title: '',
      type: '',
      typeDisplayName: '',
      summary: '',
      content: ''
    })
    
    const fileList = ref([])
    const showTypePicker = ref(false)
    const publishNow = ref(true)
    const updating = ref(false)
    
    // 攻略类型选项
    const typeOptions = [
      { text: '旅行贴士', value: 'TRAVEL_TIPS' },
      { text: '目的地攻略', value: 'DESTINATION_GUIDE' },
      { text: '交通指南', value: 'TRANSPORTATION' },
      { text: '住宿推荐', value: 'ACCOMMODATION' },
      { text: '美食攻略', value: 'FOOD_GUIDE' },
      { text: '文化体验', value: 'CULTURE_GUIDE' },
      { text: '省钱攻略', value: 'BUDGET_TIPS' },
      { text: '安全贴士', value: 'SAFETY_TIPS' },
      { text: '摄影技巧', value: 'PHOTOGRAPHY' },
      { text: '其他', value: 'OTHER' }
    ]
    
    // 加载攻略详情
    const loadGuide = async () => {
      try {
        console.log('路由参数:', route.params)
        console.log('攻略ID:', route.params.id)
        if (!route.params.id) {
          showToast('攻略ID不存在')
          router.back()
          return
        }
        const response = await getGuideById(route.params.id)
        console.log('获取攻略详情响应:', response)
        guide.value = response
        
        // 填充表单
        form.title = guide.value.title
        form.type = guide.value.type
        form.typeDisplayName = guide.value.typeDisplayName
        form.summary = guide.value.summary || ''
        form.content = guide.value.content
        
        // 如果有封面图片，添加到文件列表
        if (guide.value.coverImage) {
          fileList.value = [{
            url: guide.value.coverImage,
            isImage: true
          }]
        }
        
        publishNow.value = guide.value.status === 'PUBLISHED'
        
      } catch (error) {
        console.error('加载攻略详情失败:', error)
        showToast('加载攻略详情失败')
        router.back()
      } finally {
        loading.value = false
      }
    }
    
    // 类型选择确认
    const onTypeConfirm = ({ selectedOptions }) => {
      const option = selectedOptions[0]
      form.type = option.value
      form.typeDisplayName = option.text
      showTypePicker.value = false
    }
    
    // 图片上传后处理
    const afterRead = (file) => {
      console.log('图片上传:', file)
    }
    
    // 删除图片前确认
    const beforeDelete = () => {
      return showConfirmDialog({
        title: '确认删除',
        message: '确定要删除这张图片吗？'
      })
    }
    
    // 保存草稿
    const saveDraft = async () => {
      if (!validateForm()) return
      
      try {
        updating.value = true
        
        const guideId = route.params.id
        if (!guideId) {
          showToast('攻略ID不存在')
          return
        }
        
        const guideData = {
          title: form.title,
          type: form.type,
          summary: form.summary,
          content: form.content,
          status: 'DRAFT'
        }
        
        await updateGuideAPI(guideId, guideData)
        
        // 如果有新图片，上传封面
        if (fileList.value.length > 0 && fileList.value[0].file) {
          try {
            await uploadGuideCover(guideId, fileList.value[0].file)
          } catch (uploadError) {
            console.error('封面上传失败:', uploadError)
            showToast('攻略保存成功，但封面上传失败')
          }
        }
        
        showToast('草稿保存成功')
        router.back()
        
      } catch (error) {
        console.error('保存草稿失败:', error)
        showToast('保存草稿失败')
      } finally {
        updating.value = false
      }
    }
    
    // 更新攻略
    const updateGuide = async () => {
      if (!validateForm()) return
      
      try {
        updating.value = true
        
        const guideId = route.params.id
        if (!guideId) {
          showToast('攻略ID不存在')
          return
        }
        
        const guideData = {
          title: form.title,
          type: form.type,
          summary: form.summary,
          content: form.content,
          status: publishNow.value ? 'PUBLISHED' : 'DRAFT'
        }
        
        await updateGuideAPI(guideId, guideData)
        
        // 如果有新图片，上传封面
        if (fileList.value.length > 0 && fileList.value[0].file) {
          try {
            await uploadGuideCover(guideId, fileList.value[0].file)
          } catch (uploadError) {
            console.error('封面上传失败:', uploadError)
            showToast('攻略更新成功，但封面上传失败')
          }
        }
        
        showToast(publishNow.value ? '攻略更新成功' : '草稿保存成功')
        router.back()
        
      } catch (error) {
        console.error('更新攻略失败:', error)
        showToast('更新攻略失败')
      } finally {
        updating.value = false
      }
    }
    
    // 表单验证
    const validateForm = () => {
      if (!form.title.trim()) {
        showToast('请输入攻略标题')
        return false
      }
      
      if (!form.type) {
        showToast('请选择攻略类型')
        return false
      }
      
      if (!form.content.trim()) {
        showToast('请输入攻略内容')
        return false
      }
      
      return true
    }
    
    // 返回
    const goBack = () => {
      if (form.title !== guide.value.title || form.content !== guide.value.content) {
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
    
    // 初始化
    onMounted(() => {
      loadGuide()
    })
    
    return {
      guide,
      loading,
      form,
      fileList,
      showTypePicker,
      publishNow,
      updating,
      typeOptions,
      onTypeConfirm,
      afterRead,
      beforeDelete,
      saveDraft,
      updateGuide,
      goBack
    }
  }
}
</script>

<style scoped>
.edit-guide {
  padding-top: 40vw; /* 为固定导航栏留出空间 */
  background: #f5f5f5;
  min-height: 100vh;
}

.loading {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.form-container {
  padding: 16px;
}

.cover-section,
.content-section {
  margin: 16px 0;
}

.section-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  border: 1px dashed #ddd;
  border-radius: 8px;
  color: #999;
  font-size: 12px;
}

.publish-options {
  margin: 16px 0;
}

.publish-section {
  margin-top: 32px;
  padding-bottom: 32px;
}
</style>
