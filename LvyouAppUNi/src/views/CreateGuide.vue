<template>
  <div class="create-guide">
    <!-- 导航栏 -->
    <van-nav-bar title="发布攻略" fixed>
      <template #left>
        <van-icon name="arrow-left" @click="goBack" />
      </template>
      <template #right>
        <van-button type="primary" size="small" @click="saveDraft">保存草稿</van-button>
      </template>
    </van-nav-bar>

    <div class="form-container">
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
          @click="publishGuide"
          :loading="publishing"
        >
          {{ publishNow ? '发布攻略' : '保存草稿' }}
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
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { createGuide, uploadGuideCover } from '@/api/guides'

export default {
  name: 'CreateGuide',
  setup() {
    const router = useRouter()
    
    // 响应式数据
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
    const publishing = ref(false)
    
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
        publishing.value = true
        
        const guideData = {
          title: form.title,
          type: form.type,
          summary: form.summary,
          content: form.content,
          status: 'DRAFT'
        }
        
        const response = await createGuide(guideData)
        console.log('创建攻略响应:', response)
        
        // 如果有图片，上传封面
        if (fileList.value.length > 0) {
          try {
            await uploadGuideCover(response.id, fileList.value[0].file)
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
        publishing.value = false
      }
    }
    
    // 发布攻略
    const publishGuide = async () => {
      if (!validateForm()) return
      
      try {
        publishing.value = true
        
        const guideData = {
          title: form.title,
          type: form.type,
          summary: form.summary,
          content: form.content,
          status: publishNow.value ? 'PUBLISHED' : 'DRAFT'
        }
        
        const response = await createGuide(guideData)
        console.log('创建攻略响应:', response)
        
        // 如果有图片，上传封面
        if (fileList.value.length > 0) {
          try {
            await uploadGuideCover(response.id, fileList.value[0].file)
          } catch (uploadError) {
            console.error('封面上传失败:', uploadError)
            showToast('攻略发布成功，但封面上传失败')
          }
        }
        
        showToast(publishNow.value ? '攻略发布成功，获得10积分！' : '草稿保存成功')
        router.back()
        
      } catch (error) {
        console.error('发布攻略失败:', error)
        showToast('发布攻略失败')
      } finally {
        publishing.value = false
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
      if (form.title || form.content) {
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
    
    return {
      form,
      fileList,
      showTypePicker,
      publishNow,
      publishing,
      typeOptions,
      onTypeConfirm,
      afterRead,
      beforeDelete,
      saveDraft,
      publishGuide,
      goBack
    }
  }
}
</script>

<style scoped>
.create-guide {
  padding-top: 46vw; /* 为固定导航栏留出空间 */
  background: #f5f5f5;
  min-height: 100vh;
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
