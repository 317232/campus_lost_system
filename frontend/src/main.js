import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.js'
import { pinia } from './stores'
import { useUserStore } from './stores/modules/user'
import './style.css'

const app = createApp(App)

app.use(pinia)
useUserStore(pinia).hydrate()
app.use(router)

// WebSocket 初始化逻辑
// 在 Vue 应用挂载后，根据用户登录状态决定是否连接 WebSocket
app.mount('#app')

// 导出 router 和 pinia 实例供其他地方使用（如路由守卫）
export { router, pinia }
