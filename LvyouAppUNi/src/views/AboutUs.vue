<template>
  <div class="info-page">
    <van-nav-bar title="关于我们" fixed left-arrow @click-left="goBack" />
    <div class="nav-spacer"></div>

    <div class="content">
      <div class="brand-card">
        <div class="brand-badge">Lvyou App</div>
        <div class="brand-title">一站式旅游社交与活动平台</div>
        <div class="brand-desc">
          为用户提供动态分享、活动组织、旅游攻略、即时交流等功能，帮助大家更方便地发现目的地、结伴出行与记录旅程。
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">产品定位</div>
        <div class="section-text">
          面向热爱旅行的用户，打造集内容分享、旅游活动、达人攻略和互动社区于一体的综合平台。
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">核心功能</div>
        <ul class="section-list">
          <li>浏览与发布旅游动态，分享旅途瞬间。</li>
          <li>创建并参与旅游活动，支持聊天室实时交流。</li>
          <li>查看旅游攻略与达人内容，获取行程灵感。</li>
          <li>接收通知消息，管理个人资料与积分成长。</li>
        </ul>
      </div>

      <div class="section-card">
        <div class="section-title">版本信息</div>
        <div class="info-row">
          <span>当前版本</span>
          <span>V1.0.0</span>
        </div>
        <div class="info-row">
          <span>服务对象</span>
          <span>旅游爱好者</span>
        </div>
        <div class="info-row">
          <span>内容类型</span>
          <span>动态 / 活动 / 攻略 / 通知</span>
        </div>
      </div>

      <div class="section-card">
        <div class="section-title">服务理念</div>
        <div class="section-text">
          让旅行信息更清晰、互动更高效、结伴更便捷，帮助每一位用户找到适合自己的出行方式。
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

.nav-spacer {
  height: 46px;
}

.content {
  padding: 12px;
}

.brand-card,
.section-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.brand-card {
  margin-bottom: 12px;
  background: linear-gradient(135deg, #eef8ff 0%, #ffffff 100%);
}

.section-card + .section-card {
  margin-top: 12px;
}

.brand-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(47, 119, 255, 0.12);
  color: #2f77ff;
  font-size: 12px;
  margin-bottom: 10px;
}

.brand-title,
.section-title {
  color: #1e293b;
  font-weight: 600;
}

.brand-title {
  font-size: 18px;
  margin-bottom: 8px;
}

.brand-desc,
.section-text,
.section-list,
.info-row {
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

.info-row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.info-row + .info-row {
  margin-top: 8px;
}
</style>
