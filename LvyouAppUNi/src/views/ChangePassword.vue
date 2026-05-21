<template>
  <div class="change-password">
    <!-- 顶部导航栏 -->
    <van-nav-bar title="修改密码" fixed>
      <template #left>
        <van-icon name="cross" @click="goBack" />
      </template>
    </van-nav-bar>

    <!-- 修改密码表单 -->
    <div class="password-form">
      <van-form @submit="handleSubmit">
        <van-cell-group title="密码修改">
          <van-field
            v-model="formData.oldPassword"
            type="password"
            label="原密码"
            placeholder="请输入原密码"
            :rules="[{ required: true, message: '请输入原密码' }]"
          />
          
          <van-field
            v-model="formData.newPassword"
            type="password"
            label="新密码"
            placeholder="请输入新密码"
            :rules="[
              { required: true, message: '请输入新密码' },
              { min: 6, max: 20, message: '密码长度必须在6-20个字符之间' }
            ]"
          />
          
          <van-field
            v-model="formData.confirmPassword"
            type="password"
            label="确认密码"
            placeholder="请再次输入新密码"
            :rules="[
              { required: true, message: '请确认新密码' },
              { validator: validateConfirmPassword, message: '两次输入的密码不一致' }
            ]"
          />
        </van-cell-group>
        
        <div class="form-tips">
          <van-icon name="info-o" size="14" />
          <span>密码长度6-20个字符，建议包含字母、数字和特殊字符</span>
        </div>
      </van-form>
    </div>

    <!-- 提交按钮 -->
    <div class="submit-section">
      <van-button
        type="primary"
        size="large"
        block
        :loading="submitting"
        @click="handleSubmit"
      >
        确认修改
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { changePassword } from '@/api/user'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()

// 响应式数据
const submitting = ref(false)
const formData = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 方法
const goBack = () => {
  if (hasChanges()) {
    showConfirmDialog({
      title: '确认离开',
      message: '当前修改未保存，确定要离开吗？'
    }).then(() => {
      router.back()
    }).catch(() => {
      // 用户取消
    })
  } else {
    router.back()
  }
}

const hasChanges = () => {
  return formData.value.oldPassword || 
         formData.value.newPassword || 
         formData.value.confirmPassword
}

const validateConfirmPassword = (value) => {
  return value === formData.value.newPassword
}

const handleSubmit = async () => {
  if (submitting.value) return
  
  // 验证表单
  if (!formData.value.oldPassword.trim()) {
    showToast('请输入原密码')
    return
  }
  
  if (!formData.value.newPassword.trim()) {
    showToast('请输入新密码')
    return
  }
  
  if (formData.value.newPassword.length < 6 || formData.value.newPassword.length > 20) {
    showToast('密码长度必须在6-20个字符之间')
    return
  }
  
  if (formData.value.newPassword !== formData.value.confirmPassword) {
    showToast('两次输入的密码不一致')
    return
  }
  
  if (formData.value.oldPassword === formData.value.newPassword) {
    showToast('新密码不能与原密码相同')
    return
  }
  
  submitting.value = true
  
  try {
    await changePassword(formData.value)
    showToast('密码修改成功')
    router.back()
  } catch (error) {
    console.error('密码修改失败:', error)
    showToast(error.response?.data?.message || '密码修改失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.change-password {
  padding: 0rem;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.password-form {
  padding: 0rem;
  margin-top: 46px;
}

.form-tips {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 15px 16px;
  background-color: #f0f8ff;
  color: #1989fa;
  font-size: 12px;
  margin: 10px 0;
}

.submit-section {
  padding: 20px 16px;
  background: white;
  margin-top: 20px;
}

:deep(.van-cell-group__title) {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  padding: 15px 16px 10px;
}

:deep(.van-field__label) {
  font-size: 14px;
  color: #333;
  width: 80px;
}

:deep(.van-field__control) {
  font-size: 14px;
}
</style>
