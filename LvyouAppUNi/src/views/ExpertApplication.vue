<template>
  <div class="expert-application">
    <van-nav-bar title="申请成为旅游达人" left-arrow @click-left="goBack" />
    
    <!-- 申请表单 - 只在没有申请或申请被拒绝时显示 -->
    <div v-if="!applicationStatus || applicationStatus.status === 'REJECTED'" class="application-form">
      <van-form @submit="submitApplication">
        <van-cell-group>
          <van-field
            v-model="form.reason"
            label="申请理由"
            type="textarea"
            placeholder="请详细说明您为什么想成为旅游达人，您的旅游经验和专长等"
            :rules="[{ required: true, message: '请填写申请理由' }]"
            rows="4"
            maxlength="1000"
            show-word-limit
          />
          
          <van-field
            v-model="form.bio"
            label="个人简介"
            type="textarea"
            placeholder="请简要介绍您的个人背景、旅游经历等"
            :rules="[{ required: true, message: '请填写个人简介' }]"
            rows="3"
            maxlength="500"
            show-word-limit
          />
          
          <van-field
            v-model="form.expertise"
            label="专业领域"
            placeholder="如：户外徒步、摄影、美食、文化等"
            maxlength="200"
            show-word-limit
          />
          
          <van-field
            v-model="form.contactInfo"
            label="联系方式"
            placeholder="微信、QQ或其他联系方式"
            maxlength="200"
            show-word-limit
          />
        </van-cell-group>
        
        <div class="form-actions">
          <van-button 
            type="primary" 
            size="large" 
            native-type="submit"
            :loading="submitting"
            block
          >
            {{ applicationStatus && applicationStatus.status === 'REJECTED' ? '重新申请' : '提交申请' }}
          </van-button>
        </div>
      </van-form>
    </div>
    
    <!-- 申请状态 -->
    <div v-if="applicationStatus" class="application-status">
      <van-cell-group>
        <van-cell title="申请状态">
          <template #value>
            <van-tag :type="getStatusType(applicationStatus.status)">
              {{ getStatusText(applicationStatus.status) }}
            </van-tag>
          </template>
        </van-cell>
        
        <van-cell 
          v-if="applicationStatus.adminNote"
          title="管理员备注"
          :value="applicationStatus.adminNote"
        />
        
        <van-cell 
          v-if="applicationStatus.rejectReason"
          title="拒绝原因"
          :value="applicationStatus.rejectReason"
        />
        
        <van-cell 
          title="申请时间"
          :value="formatTime(applicationStatus.createdAt)"
        />
        
        <van-cell 
          v-if="applicationStatus.reviewedAt"
          title="审核时间"
          :value="formatTime(applicationStatus.reviewedAt)"
        />
      </van-cell-group>
      
      <div v-if="applicationStatus.status === 'PENDING'" class="cancel-action">
        <van-button 
          type="danger" 
          size="large" 
          @click="cancelApplication"
          block
        >
          取消申请
        </van-button>
      </div>
    </div>
    
    <!-- 达人权益说明 -->
    <div class="benefits-section">
      <h3>旅游达人权益</h3>
      <van-cell-group>
        <van-cell title="活动组织" value="可以发起和组织旅游活动" />
        <van-cell title="内容创作" value="发布优质攻略和动态" />
        <van-cell title="粉丝互动" value="与粉丝互动，建立个人品牌" />
        <van-cell title="平台支持" value="获得平台推广支持" />
      </van-cell-group>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'

export default {
  name: 'ExpertApplication',
  setup() {
    const router = useRouter()
    const form = ref({
      reason: '',
      bio: '',
      expertise: '',
      contactInfo: ''
    })
    const submitting = ref(false)
    const applicationStatus = ref(null)
    
    const goBack = () => {
      router.go(-1)
    }
    
    const submitApplication = async () => {
      if (submitting.value) return
      
      submitting.value = true
      
      try {
        const response = await fetch('/api/expert/apply', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          },
          body: JSON.stringify(form.value)
        })
        
        if (response.ok) {
          showToast('申请已提交，请等待审核')
          loadApplicationStatus()
        } else {
          const error = await response.json()
          showToast(error.message || '提交失败')
        }
      } catch (error) {
        console.error('提交申请失败:', error)
        showToast('提交失败')
      } finally {
        submitting.value = false
      }
    }
    
    const loadApplicationStatus = async () => {
      try {
        const response = await fetch('/api/expert/my-application', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.ok) {
          applicationStatus.value = await response.json()
        } else if (response.status === 404) {
          applicationStatus.value = null
        } else {
          console.error('加载申请状态失败')
        }
      } catch (error) {
        console.error('加载申请状态失败:', error)
      }
    }
    
    const cancelApplication = async () => {
      try {
        await showConfirmDialog({
          title: '确认取消',
          message: '确定要取消达人申请吗？'
        })
        
        const response = await fetch('/api/expert/my-application', {
          method: 'DELETE',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        })
        
        if (response.ok) {
          showToast('申请已取消')
          loadApplicationStatus()
        } else {
          showToast('取消失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消申请失败:', error)
          showToast('取消失败')
        }
      }
    }
    
    const getStatusType = (status) => {
      switch (status) {
        case 'APPROVED': return 'success'
        case 'REJECTED': return 'danger'
        case 'PENDING': return 'warning'
        default: return 'default'
      }
    }
    
    const getStatusText = (status) => {
      switch (status) {
        case 'APPROVED': return '已通过'
        case 'REJECTED': return '已拒绝'
        case 'PENDING': return '待审核'
        default: return '未知'
      }
    }
    
    const formatTime = (time) => {
      return new Date(time).toLocaleString()
    }
    
    onMounted(() => {
      loadApplicationStatus()
    })
    
    return {
      form,
      submitting,
      applicationStatus,
      goBack,
      submitApplication,
      cancelApplication,
      getStatusType,
      getStatusText,
      formatTime
    }
  }
}
</script>

<style scoped>
.expert-application {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding-top: 0;
}

/* 确保导航栏正确显示 */
:deep(.van-nav-bar) {
  position: relative;
  z-index: 1000;
}

:deep(.van-nav-bar__left) {
  display: flex;
  align-items: center;
}

.application-form {
  padding: 16px;
  background: white;
  margin-bottom: 16px;
}

.form-actions {
  padding: 16px;
}

.application-status {
  padding: 0 16px;
  margin-bottom: 16px;
}

.cancel-action {
  padding: 16px;
}

.benefits-section {
  padding: 16px;
  background: white;
}

.benefits-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
}
</style>
