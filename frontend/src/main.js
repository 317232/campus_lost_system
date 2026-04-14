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
app.mount('#app')
