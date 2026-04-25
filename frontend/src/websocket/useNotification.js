/**
 * WebSocket 通知组合式函数
 * 
 * @description
 * 提供便捷的 WebSocket 通知功能接口
 * 可在任何 Vue 组件或组合式函数中使用
 */

import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useNotificationStore } from '@/stores/modules/notification'
import { wsClient, NotificationType } from '@/websocket/socket'

/**
 * 使用 WebSocket 通知功能
 * 
 * @param {Object} options 配置选项
 * @param {Function} options.onNotification - 新通知回调
 * @param {string[]} options.subscribeTypes - 要订阅的通知类型列表
 * @returns {Object} 通知相关的响应式状态和方法
 */
export function useNotification(options = {}) {
  const notificationStore = useNotificationStore()
  const { onNotification, subscribeTypes } = options

  // 响应式状态
  const isConnected = ref(false)
  const notifications = computed(() => notificationStore.notifications)
  const unreadCount = computed(() => notificationStore.unreadCount)
  const unreadNotifications = computed(() => notificationStore.unreadNotifications)

  // 监听器引用
  const listeners = []

  // 初始化
  const init = () => {
    // 监听连接状态变化
    const connectedListener = () => {
      isConnected.value = true
    }
    const disconnectedListener = () => {
      isConnected.value = false
    }

    wsClient.on('connected', connectedListener)
    wsClient.on('disconnected', disconnectedListener)
    wsClient.on('reconnected', connectedListener)

    listeners.push(
      { event: 'connected', callback: connectedListener },
      { event: 'disconnected', callback: disconnectedListener },
      { event: 'reconnected', callback: connectedListener }
    )

    // 设置初始连接状态
    isConnected.value = wsClient.isConnected()

    // 监听指定类型的通知
    if (subscribeTypes && subscribeTypes.length > 0) {
      subscribeTypes.forEach(type => {
        const listener = (message) => {
          if (onNotification) {
            onNotification(message)
          }
        }
        wsClient.on(type.toLowerCase(), listener)
        listeners.push({ event: type.toLowerCase(), callback: listener })
      })
    }

    // 监听所有通知
    if (onNotification) {
      const allListener = (message) => {
        onNotification(message)
      }
      wsClient.on('notification', allListener)
      listeners.push({ event: 'notification', callback: allListener })
    }
  }

  // 清理
  const cleanup = () => {
    listeners.forEach(({ event, callback }) => {
      wsClient.off(event, callback)
    })
    listeners.length = 0
  }

  // 方法
  const markAsRead = (notificationId) => {
    notificationStore.markAsRead(notificationId)
  }

  const markAllAsRead = () => {
    notificationStore.markAllAsRead()
  }

  const removeNotification = (notificationId) => {
    notificationStore.removeNotification(notificationId)
  }

  const clearAll = () => {
    notificationStore.clearAll()
  }

  // 生命周期钩子
  onMounted(() => {
    init()
  })

  onUnmounted(() => {
    cleanup()
  })

  return {
    // 状态
    notifications,
    unreadCount,
    unreadNotifications,
    isConnected,
    
    // 方法
    markAsRead,
    markAllAsRead,
    removeNotification,
    clearAll,
    
    // 通知类型常量
    NotificationType,
  }
}

/**
 * 监听特定类型通知的组合式函数
 * 
 * @param {string|string[]} types - 通知类型或类型数组
 * @param {Function} callback - 回调函数
 */
export function useNotificationListener(types, callback) {
  const normalizedTypes = Array.isArray(types) ? types : [types]
  const listeners = []

  onMounted(() => {
    normalizedTypes.forEach(type => {
      const listener = (message) => {
        callback(message)
      }
      wsClient.on(type.toLowerCase(), listener)
      listeners.push({ event: type.toLowerCase(), callback: listener })
    })
  })

  onUnmounted(() => {
    listeners.forEach(({ event, callback: cb }) => {
      wsClient.off(event, cb)
    })
  })
}

/**
 * 获取未读通知数量的组合式函数
 * 
 * @returns {Object} 包含未读计数的响应式对象
 */
export function useUnreadCount() {
  const notificationStore = useNotificationStore()
  
  return {
    unreadCount: computed(() => notificationStore.unreadCount),
    unreadNotifications: computed(() => notificationStore.unreadNotifications),
  }
}

export default useNotification
