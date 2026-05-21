<template>
  <div class="info-page">
    <van-nav-bar title="隐私设置" fixed left-arrow @click-left="goBack" />
    <div class="nav-spacer"></div>

    <div class="content">
      <div class="hero-card">
        <div class="hero-title">保护您的个人隐私</div>
        <div class="hero-desc">
          我们仅在提供旅游动态、活动报名、消息通知等基础服务所需的范围内处理您的信息。
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">我们会收集哪些信息</div>
        <ul class="section-list">
          <li>账号基础信息：用户名、昵称、头像、手机号或邮箱。</li>
          <li>内容信息：您发布的动态、评论、攻略、活动报名记录。</li>
          <li>设备与日志信息：用于保障登录安全、定位故障与优化体验。</li>
        </ul>
      </div>

      <div class="section-card">
        <div class="section-title">信息会如何使用</div>
        <ul class="section-list">
          <li>用于完成登录、资料展示、互动聊天、活动参与等核心功能。</li>
          <li>用于向您推送系统通知、评论提醒和活动相关消息。</li>
          <li>用于提升内容推荐质量、维护平台安全和服务稳定性。</li>
        </ul>
      </div>

      <div class="section-card">
        <div class="section-title">隐私保护说明</div>
        <ul class="section-list">
          <li>未经您的授权，我们不会将个人敏感信息对外公开展示。</li>
          <li>我们会采用合理的技术与管理措施保护您的账号与数据安全。</li>
          <li>如您不再使用本服务，可联系平台申请账号相关信息处理支持。</li>
        </ul>
      </div>

      <div class="section-card">
        <div class="section-title">温馨提示</div>
        <div class="section-text">
          在发布动态、评论或参加活动时，请避免主动公开身份证号、家庭住址、银行卡等敏感信息。
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.info-page {
  min-height: 100vh;
  background: #f6f8fc;
}

.content {
  padding: 12px;
}

.nav-spacer {
  height: 46px;
}

.hero-card,
.section-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.hero-card {
  margin-bottom: 12px;
  background: linear-gradient(135deg, #f3f7ff 0%, #ffffff 100%);
}

.section-card + .section-card {
  margin-top: 12px;
}

.hero-title,
.section-title {
  color: #1e293b;
  font-weight: 600;
}

.hero-title {
  font-size: 18px;
  margin-bottom: 8px;
}

.hero-desc,
.section-text,
.section-list {
  font-size: 14px;
  line-height: 1.8;
  color: #64748b;
}

.section-title {
  font-size: 16px;
  margin-bottom: 10px;
}

.section-list {
  margin: 0;
  padding-left: 18px;
}

.section-list li + li {
  margin-top: 8px;
}
</style>
