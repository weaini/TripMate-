<template>
  <div class="info-page">
    <van-nav-bar title="帮助中心" fixed left-arrow @click-left="goBack" />
    <div class="nav-spacer"></div>

    <div class="content">
      <div class="hero-card">
        <div class="hero-title">常见问题与使用说明</div>
        <div class="hero-desc">
          以下内容为固定帮助信息，可帮助您快速了解平台主要功能的使用方式。
        </div>
      </div>

      <div class="faq-card" v-for="item in faqList" :key="item.title">
        <div class="faq-title">{{ item.title }}</div>
        <div class="faq-content">{{ item.content }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const faqList = [
  {
    title: '1. 如何发布动态？',
    content: '进入“动态”页面后点击发布入口，填写文字、图片和位置信息后即可提交发布。',
  },
  {
    title: '2. 如何报名参加活动？',
    content: '在活动详情页中点击报名按钮，提交后等待审核，通过后即可加入活动聊天室。',
  },
  {
    title: '3. 为什么无法进入聊天室？',
    content: '只有已报名且具备进入权限的用户才能访问活动聊天室，请先确认报名状态是否通过。',
  },
  {
    title: '4. 如何查看通知消息？',
    content: '您可以在通知页面查看系统通知、活动提醒、评论提醒和审核结果等内容。',
  },
  {
    title: '5. 遇到问题怎么办？',
    content: '如页面异常、数据加载失败或操作无响应，可重新进入页面后再次尝试，必要时联系平台管理员处理。',
  },
]

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

.hero-card,
.faq-card {
  background: #fff;
  border-radius: 18px;
  padding: 16px;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.06);
}

.hero-card {
  margin-bottom: 12px;
  background: linear-gradient(135deg, #eff6ff 0%, #ffffff 100%);
}

.faq-card + .faq-card {
  margin-top: 12px;
}

.hero-title,
.faq-title {
  color: #1e293b;
  font-weight: 600;
}

.hero-title {
  font-size: 18px;
  margin-bottom: 8px;
}

.hero-desc,
.faq-content {
  font-size: 14px;
  line-height: 1.8;
  color: #64748b;
}

.faq-title {
  font-size: 15px;
  margin-bottom: 8px;
}
</style>
