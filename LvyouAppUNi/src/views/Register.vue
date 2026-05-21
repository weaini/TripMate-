<template>
  <div class="register">
    <van-nav-bar title="注册" left-arrow @click-left="goBack" />
    
    <div class="register-container">
      <!-- Logo区域 -->
      <div class="logo-section">
        <div class="logo-wrapper">
          <van-icon name="location-o" size="40" color="#1989fa" />
        </div>
        <h2>加入我们</h2>
        <p>开启你的旅行社交之旅</p>
      </div>

      <!-- 注册表单 -->
      <van-form @submit="handleRegister" ref="registerFormRef">
        <van-cell-group inset>
          <van-field
            v-model="registerForm.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名（3-20个字符）"
            :rules="usernameRules"
            left-icon="user-o"
            clearable
            @blur="checkUsername"
          />
          <van-field
            v-model="registerForm.email"
            name="email"
            label="邮箱"
            placeholder="请输入邮箱地址"
            :rules="emailRules"
            left-icon="envelop-o"
            clearable
            @blur="checkEmail"
          />
          <van-field
            v-model="registerForm.password"
            :type="showPassword ? 'text' : 'password'"
            name="password"
            label="密码"
            placeholder="请输入密码（6-20个字符）"
            :rules="passwordRules"
            left-icon="lock"
            clearable
          >
            <template #right-icon>
              <van-icon 
                :name="showPassword ? 'eye-o' : 'closed-eye'" 
                @click="togglePassword"
              />
            </template>
          </van-field>
          <van-field
            v-model="registerForm.confirmPassword"
            :type="showConfirmPassword ? 'text' : 'password'"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="confirmPasswordRules"
            left-icon="lock"
            clearable
          >
            <template #right-icon>
              <van-icon 
                :name="showConfirmPassword ? 'eye-o' : 'closed-eye'" 
                @click="toggleConfirmPassword"
              />
            </template>
          </van-field>
          <van-field
            v-model="registerForm.nickname"
            name="nickname"
            label="昵称"
            placeholder="请输入昵称（可选）"
            left-icon="smile-o"
            clearable
          />
          <van-field
            v-model="registerForm.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号（可选）"
            left-icon="phone-o"
            clearable
          />
        </van-cell-group>

        <!-- 用户协议 -->
        <div class="agreement">
          <van-checkbox v-model="agreeTerms" shape="square">
            <span class="agreement-text">
              我已阅读并同意
              <span class="link" @click="showTerms">《用户协议》</span>
              和
              <span class="link" @click="showPrivacy">《隐私政策》</span>
            </span>
          </van-checkbox>
        </div>

        <div class="form-actions">
          <van-button
            round
            block
            type="primary"
            native-type="submit"
            :loading="loading"
            loading-text="注册中..."
            :disabled="!agreeTerms || loading"
            class="register-btn"
          >
            注册
          </van-button>
        </div>
      </van-form>

      <!-- 登录链接 -->
      <div class="login-link">
        <span>已有账号？</span>
        <van-button type="default" size="small" @click="goToLogin" plain>
          立即登录
        </van-button>
      </div>
    </div>

    <!-- 用户协议弹窗 -->
    <van-popup v-model:show="showTermsPopup" position="bottom" :style="{ height: '70%' }">
      <div class="terms-content">
        <div class="terms-header">
          <h3>用户协议</h3>
          <van-icon name="cross" @click="showTermsPopup = false" />
        </div>
        <div class="terms-body">
          <p>欢迎使用旅游社交平台！</p>
          <p>1. 用户注册时需要提供真实、准确的信息</p>
          <p>2. 用户不得发布违法违规内容</p>
          <p>3. 平台有权对违规用户进行处罚</p>
          <p>4. 用户应保护好自己的账号安全</p>
          <p>5. 本协议最终解释权归平台所有</p>
        </div>
      </div>
    </van-popup>

    <!-- 隐私政策弹窗 -->
    <van-popup v-model:show="showPrivacyPopup" position="bottom" :style="{ height: '70%' }">
      <div class="privacy-content">
        <div class="privacy-header">
          <h3>隐私政策</h3>
          <van-icon name="cross" @click="showPrivacyPopup = false" />
        </div>
        <div class="privacy-body">
          <p>我们重视您的隐私保护！</p>
          <p>1. 我们仅收集必要的用户信息</p>
          <p>2. 您的个人信息不会被泄露给第三方</p>
          <p>3. 我们采用加密技术保护数据安全</p>
          <p>4. 您可以随时查看和修改个人信息</p>
          <p>5. 如有疑问请联系客服</p>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { register } from '@/api/auth'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const agreeTerms = ref(false)
const showPassword = ref(false)
const showConfirmPassword = ref(false)
const showTermsPopup = ref(false)
const showPrivacyPopup = ref(false)
const registerFormRef = ref(null)

const registerForm = ref({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: ''
})

// 表单验证规则
const usernameRules = [
  { required: true, message: '请输入用户名' },
  { min: 3, max: 20, message: '用户名长度为3-20个字符' },
  { pattern: /^[a-zA-Z0-9_]+$/, message: '用户名只能包含字母、数字和下划线' }
]

const emailRules = [
  { required: true, message: '请输入邮箱' },
  { pattern: /^[^\s@]+@[^\s@]+\.[^\s@]+$/, message: '请输入正确的邮箱格式' }
]

const passwordRules = [
  { required: true, message: '请输入密码' },
  { min: 6, max: 20, message: '密码长度为6-20个字符' },
  { 
    validator: (val) => {
      // 密码强度检查：至少包含字母和数字
      const hasLetter = /[a-zA-Z]/.test(val)
      const hasNumber = /\d/.test(val)
      return hasLetter && hasNumber
    }, 
    message: '密码必须包含字母和数字' 
  }
]

const confirmPasswordRules = [
  { required: true, message: '请确认密码' },
  { validator: (val) => val === registerForm.value.password, message: '两次输入的密码不一致' }
]

// 方法
const goBack = () => {
  router.back()
}

const goToLogin = () => {
  router.push('/login')
}

const togglePassword = () => {
  showPassword.value = !showPassword.value
}

const toggleConfirmPassword = () => {
  showConfirmPassword.value = !showConfirmPassword.value
}

const showTerms = () => {
  showTermsPopup.value = true
}

const showPrivacy = () => {
  showPrivacyPopup.value = true
}

// 检查用户名是否可用
const checkUsername = async () => {
  if (!registerForm.value.username || registerForm.value.username.length < 3) {
    return
  }
  
  try {
    // 这里可以调用API检查用户名是否已存在
    // 暂时用简单的本地检查
    const commonUsernames = ['admin', 'test', 'user', 'guest']
    if (commonUsernames.includes(registerForm.value.username.toLowerCase())) {
      showToast('该用户名已被使用')
    }
  } catch (error) {
    console.error('检查用户名失败:', error)
  }
}

// 检查邮箱是否可用
const checkEmail = async () => {
  if (!registerForm.value.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(registerForm.value.email)) {
    return
  }
  
  try {
    // 这里可以调用API检查邮箱是否已存在
    // 暂时用简单的本地检查
    const commonEmails = ['admin@test.com', 'test@test.com']
    if (commonEmails.includes(registerForm.value.email.toLowerCase())) {
      showToast('该邮箱已被使用')
    }
  } catch (error) {
    console.error('检查邮箱失败:', error)
  }
}

const handleRegister = async () => {
  if (loading.value) return
  
  // 表单验证
  if (!registerFormRef.value) return
  
  try {
    await registerFormRef.value.validate()
  } catch (error) {
    showToast('请检查表单信息')
    return
  }
  
  if (!agreeTerms.value) {
    showToast('请先同意用户协议和隐私政策')
    return
  }
  
  // 确认注册
  try {
    await showConfirmDialog({
      title: '确认注册',
      message: '确定要注册账号吗？',
    })
  } catch {
    return
  }
  
  loading.value = true
  
  try {
    console.log('注册请求数据:', registerForm.value)
    
    // 准备注册数据，移除确认密码字段
    const { confirmPassword, ...registerData } = registerForm.value
    
    const response = await register(registerData)
    console.log('注册响应:', response)
    
    showToast('注册成功！请登录')
    router.push('/login')
  } catch (error) {
    console.error('注册失败:', error)
    
    let errorMessage = '注册失败，请重试'
    
    if (error.response) {
      const { data } = error.response
      if (data && data.message) {
        errorMessage = data.message
      } else if (data && typeof data === 'string') {
        errorMessage = data
      }
    } else if (error.message) {
      errorMessage = error.message
    }
    
    showToast(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-container {
  padding: 20px;
  min-height: calc(100vh - 46px);
  display: flex;
  flex-direction: column;
}

.logo-section {
  text-align: center;
  margin: 20px 0 40px;
  color: white;
}

.logo-wrapper {
  width: 80px;
  height: 80px;
  margin: 0 auto 15px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(255, 255, 255, 0.3);
}

.logo-section h2 {
  font-size: 24px;
  margin-bottom: 8px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.logo-section p {
  font-size: 14px;
  opacity: 0.9;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.form-actions {
  margin: 20px 0;
}

.register-btn {
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(45deg, #1989fa, #36a3f7);
  border: none;
  box-shadow: 0 4px 12px rgba(25, 137, 250, 0.3);
}

.register-btn:disabled {
  background: #ccc;
  box-shadow: none;
}

.agreement {
  margin: 15px 0;
  padding: 0 15px;
}

.agreement-text {
  color: white;
  font-size: 13px;
  line-height: 1.4;
}

.agreement .link {
  color: #1989fa;
  text-decoration: underline;
  cursor: pointer;
  transition: color 0.3s;
}

.agreement .link:hover {
  color: #36a3f7;
}

.login-link {
  text-align: center;
  margin: 20px 0;
  color: white;
}

.login-link span {
  margin-right: 8px;
  opacity: 0.9;
}

/* 弹窗样式 */
.terms-content, .privacy-content {
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.terms-header, .privacy-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #eee;
}

.terms-header h3, .privacy-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}

.terms-body, .privacy-body {
  flex: 1;
  overflow-y: auto;
  line-height: 1.6;
}

.terms-body p, .privacy-body p {
  margin-bottom: 12px;
  color: #666;
  font-size: 14px;
}

/* 表单样式优化 */
:deep(.van-cell-group) {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

:deep(.van-field__control) {
  color: #333;
  font-size: 15px;
}

:deep(.van-field__label) {
  color: #666;
  font-weight: 500;
}

:deep(.van-field__placeholder) {
  color: #999;
}

:deep(.van-checkbox) {
  align-items: flex-start;
}

:deep(.van-checkbox__label) {
  color: white;
  font-size: 13px;
  line-height: 1.4;
}

:deep(.van-checkbox__icon) {
  margin-top: 2px;
}

:deep(.van-button--small) {
  height: 32px;
  padding: 0 16px;
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 375px) {
  .register-container {
    padding: 15px;
  }
  
  .logo-section {
    margin: 15px 0 30px;
  }
  
  .logo-wrapper {
    width: 70px;
    height: 70px;
  }
  
  .logo-section h2 {
    font-size: 20px;
  }
  
  .logo-section p {
    font-size: 12px;
  }
}

/* 动画效果 */
.register-container {
  animation: fadeInUp 0.6s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-wrapper {
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 输入框焦点效果 */
:deep(.van-field--focused .van-field__control) {
  color: #1989fa;
}

:deep(.van-field--focused .van-field__label) {
  color: #1989fa;
}

/* 按钮悬停效果 */
.register-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(25, 137, 250, 0.4);
  transition: all 0.3s ease;
}

/* 加载状态优化 */
:deep(.van-button--loading) {
  opacity: 0.8;
}
</style>
