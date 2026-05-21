import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/Home.vue'),
    meta: { title: '首页' },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册', requiresAuth: false },
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('@/views/Profile.vue'),
    meta: { title: '个人中心', requiresAuth: true },
  },
  {
    path: '/profile/edit',
    name: 'EditProfile',
    component: () => import('@/views/EditProfile.vue'),
    meta: { title: '编辑资料', requiresAuth: true },
  },
  {
    path: '/profile/settings',
    name: 'ProfileSettings',
    component: () => import('@/views/ProfileSettings.vue'),
    meta: { title: '设置', requiresAuth: true },
  },
  {
    path: '/profile/privacy',
    name: 'PrivacySettingsInfo',
    component: () => import('@/views/PrivacySettingsInfo.vue'),
    meta: { title: '隐私设置', requiresAuth: true },
  },
  {
    path: '/help-center',
    name: 'HelpCenter',
    component: () => import('@/views/HelpCenter.vue'),
    meta: { title: '帮助中心', requiresAuth: true },
  },
  {
    path: '/about',
    name: 'AboutUs',
    component: () => import('@/views/AboutUs.vue'),
    meta: { title: '关于我们', requiresAuth: true },
  },
  {
    path: '/profile/change-password',
    name: 'ChangePassword',
    component: () => import('@/views/ChangePassword.vue'),
    meta: { title: '修改密码', requiresAuth: true },
  },
  {
    path: '/profile/points-records',
    name: 'PointsRecords',
    component: () => import('@/views/PointsRecords.vue'),
    meta: { title: '积分明细', requiresAuth: true },
  },
  {
    path: '/profile/followers',
    name: 'FollowersList',
    component: () => import('@/views/FollowersList.vue'),
    meta: { title: '粉丝预览', requiresAuth: true },
  },
  {
    path: '/profile/following',
    name: 'FollowingList',
    component: () => import('@/views/FollowingList.vue'),
    meta: { title: '关注预览', requiresAuth: true },
  },
  {
    path: '/posts',
    name: 'Posts',
    component: () => import('@/views/Posts.vue'),
    meta: { title: '动态' },
  },
  {
    path: '/posts/create',
    name: 'CreatePost',
    component: () => import('@/views/CreatePost.vue'),
    meta: { title: '发布动态', requiresAuth: true },
  },
  {
    path: '/posts/:id/edit',
    name: 'EditPost',
    component: () => import('@/views/EditPost.vue'),
    meta: { title: '编辑动态', requiresAuth: true },
  },
  {
    path: '/posts/:id',
    name: 'PostDetail',
    component: () => import('@/views/PostDetail.vue'),
    meta: { title: '动态详情' },
  },
  {
    path: '/my-posts',
    name: 'MyPosts',
    component: () => import('@/views/MyPosts.vue'),
    meta: { title: '我的动态', requiresAuth: true },
  },
  {
    path: '/my-activities',
    name: 'MyActivities',
    component: () => import('@/views/MyActivities.vue'),
    meta: { title: '我的活动', requiresAuth: true },
  },
  {
    path: '/activities',
    name: 'Activities',
    component: () => import('@/views/Activities.vue'),
    meta: { title: '活动' },
  },
  {
    path: '/activities/create',
    name: 'CreateActivity',
    component: () => import('@/views/CreateActivity.vue'),
    meta: { title: '创建活动', requiresAuth: true, requiresExpert: true },
  },
  {
    path: '/activities/:id',
    name: 'ActivityDetail',
    component: () => import('@/views/ActivityDetail.vue'),
    meta: { title: '活动详情' },
  },
  {
    path: '/activities/:id/edit',
    name: 'EditActivity',
    component: () => {
      console.log('加载EditActivity组件')
      return import('@/views/EditActivity.vue').then(module => {
        console.log('EditActivity组件加载成功')
        return module
      }).catch(error => {
        console.error('EditActivity组件加载失败:', error)
        throw error
      })
    },
    meta: { title: '编辑活动', requiresAuth: true },
  },
  {
    path: '/activities/:id/participants',
    name: 'ActivityParticipants',
    component: () => import('@/views/ActivityParticipants.vue'),
    meta: { title: '参与者管理', requiresAuth: true },
  },
  {
    path: '/activities/:id/chat',
    name: 'ActivityChat',
    component: () => import('@/views/ActivityChatSimple.vue'),
    meta: { title: '活动聊天室', requiresAuth: true },
  },
  {
    path: '/strategies',
    redirect: '/guides'
  },
  {
    path: '/guides',
    name: 'Guides',
    component: () => import('@/views/Guides.vue'),
    meta: { title: '攻略' },
  },
  {
    path: '/guides/create',
    name: 'CreateGuide',
    component: () => import('@/views/CreateGuide.vue'),
    meta: { title: '发布攻略', requiresAuth: true },
  },
  {
    path: '/guides/:id',
    name: 'GuideDetail',
    component: () => import('@/views/GuideDetail.vue'),
    meta: { title: '攻略详情' },
  },
  {
    path: '/guides/:id/edit',
    name: 'EditGuide',
    component: () => import('@/views/EditGuide.vue'),
    meta: { title: '编辑攻略', requiresAuth: true },
  },
  {
    path: '/my-guides',
    name: 'MyGuides',
    component: () => import('@/views/MyGuides.vue'),
    meta: { title: '我的攻略', requiresAuth: true },
  },
  {
    path: '/notifications',
    name: 'Notifications',
    component: () => import('@/views/Notifications.vue'),
    meta: { title: '通知', requiresAuth: true },
  },
  {
    path: '/notification-settings',
    name: 'NotificationSettings',
    component: () => import('@/views/NotificationSettings.vue'),
    meta: { title: '通知设置', requiresAuth: true },
  },
  // 管理员路由
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('@/views/AdminDashboard.vue'),
    meta: { title: '管理后台', requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/posts',
    name: 'AdminPostReview',
    component: () => import('@/views/AdminPostReview.vue'),
    meta: { title: '动态审核', requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/activities',
    name: 'AdminActivityReview',
    component: () => import('@/views/AdminActivityReview.vue'),
    meta: { title: '活动审核', requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/experts',
    name: 'AdminExpertReview',
    component: () => import('@/views/AdminExpertReview.vue'),
    meta: { title: '达人申请审核', requiresAuth: true, requiresAdmin: true },
  },
  {
    path: '/admin/users',
    name: 'AdminUserManagement',
    component: () => import('@/views/AdminUserManagement.vue'),
    meta: { title: '用户管理', requiresAuth: true, requiresAdmin: true },
  },
  // 达人申请路由
  {
    path: '/expert/apply',
    name: 'ExpertApplication',
    component: () => import('@/views/ExpertApplication.vue'),
    meta: { title: '申请成为旅游达人', requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  try {
    console.log('路由跳转:', { from: from.path, to: to.path, name: to.name })
    const userStore = useUserStore()
    
    // 设置页面标题
    if (to.meta.title) {
      document.title = to.meta.title
    }
    
    // 如果用户已登录但没有用户信息，先获取用户信息
    if (userStore.isLoggedIn && !userStore.userInfo) {
      try {
        await userStore.fetchUserInfo()
      } catch (error) {
        console.error('获取用户信息失败:', error)
        // 如果获取用户信息失败，清除登录状态
        userStore.logout()
      }
    }
    
    // 检查是否需要登录
    if (to.meta.requiresAuth && !userStore.isLoggedIn) {
      console.log('需要登录，重定向到登录页面')
      next('/login')
      return
    }
    
    // 检查管理员权限
    if (to.meta.requiresAdmin && userStore.getUserRole !== 'ADMIN') {
      console.log('权限检查失败:', { 
        requiresAdmin: to.meta.requiresAdmin, 
        userRole: userStore.getUserRole,
        userInfo: userStore.userInfo,
        isLoggedIn: userStore.isLoggedIn
      })
      next('/home')
      return
    }
    
    // 检查达人权限
    if (to.meta.requiresExpert && userStore.getUserRole !== 'EXPERT' && userStore.getUserRole !== 'ADMIN') {
      console.log('需要达人权限，重定向到申请页面')
      next('/expert/apply')
      return
    }
    
    // 如果已登录且访问登录/注册页面，重定向到首页
    if ((to.name === 'Login' || to.name === 'Register') && userStore.isLoggedIn) {
      console.log('已登录用户访问登录/注册页面，重定向到首页')
      next('/home')
      return
    }
    
    console.log('路由守卫通过，允许跳转')
    next()
  } catch (error) {
    console.error('路由守卫错误:', error)
    console.log('路由守卫出错，直接放行')
    // 如果出现错误，直接放行
    next()
  }
})

export default router
