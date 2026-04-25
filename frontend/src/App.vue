<template>
  <router-view />
  <!-- 全局通知铃铛组件 -->
  <NotificationGlobal />
</template>

<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/modules/user'
import { useNotificationStore } from '@/stores/modules/notification'
import NotificationGlobal from '@/components/common/NotificationBell.vue'

const userStore = useUserStore()
const notificationStore = useNotificationStore()

onMounted(() => {
  // 检查用户登录状态并初始化 WebSocket
  if (userStore.isAuthenticated) {
    notificationStore.initWebSocket()
  }
})
</script>

<style>
/* 全局样式由 style.css 提供 */
</style>
