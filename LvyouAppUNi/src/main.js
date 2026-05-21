import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'

// 引入Vant样式
import 'vant/lib/index.css'

// 引入全局样式
import './style.css'

const app = createApp(App)

// 确保Pinia在Router之前初始化
const pinia = createPinia()
app.use(pinia)
app.use(router)

// 等待所有插件初始化完成后再挂载
app.mount('#app')
