import { useUserStore } from '@/stores/modules/user'
import { useNotificationStore } from '@/stores/modules/notification'

export function installRouterGuards(router, pinia) {
  router.beforeEach((to) => {
    const userStore = useUserStore(pinia)

    if (!userStore.initialized) {
      userStore.hydrate()
    }

    if (to.meta?.guestOnly && userStore.isAuthenticated) {
      return userStore.isAdmin ? { name: 'admin-dashboard' } : { name: 'home' }
    }

    if (to.meta?.requiresAuth && !userStore.isAuthenticated) {
      return {
        name: 'login',
        query: { redirect: to.fullPath },
      }
    }

    if (to.meta?.roles?.length && !to.meta.roles.includes(userStore.role)) {
      return userStore.isAuthenticated ? { name: 'home' } : { name: 'login' }
    }

    return true
  })

  // 路由变化后，检查用户登录状态变化，用于管理 WebSocket 连接
  router.afterEach((to, from) => {
    const userStore = useUserStore(pinia)
    const notificationStore = useNotificationStore(pinia)

    // 如果用户已登录且 WebSocket 未连接，则建立连接
    if (userStore.isAuthenticated && !notificationStore.wsConnected) {
      // 延迟初始化，确保应用完全加载
      setTimeout(() => {
        notificationStore.initWebSocket()
      }, 100)
    }

    // 如果用户未登录且 WebSocket 已连接，则断开连接
    if (!userStore.isAuthenticated && notificationStore.wsConnected) {
      notificationStore.disconnectWebSocket()
    }
  })
}
