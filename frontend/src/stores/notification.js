import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  // 通知列表
  const notifications = ref([])
  
  // 未读通知数量
  const unreadCount = computed(() => 
    notifications.value.filter(n => !n.read).length
  )
  
  // WebSocket 连接状态
  const connected = ref(false)

  /**
   * 添加新通知
   * @param {Object} notification - 通知对象
   */
  function addNotification(notification) {
    notifications.value.unshift({
      ...notification,
      id: Date.now(),
      read: false,
      timestamp: new Date().toISOString()
    })
  }

  /**
   * 标记通知为已读
   * @param {number} notificationId - 通知ID
   */
  function markAsRead(notificationId) {
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.read = true
    }
  }

  /**
   * 标记所有通知为已读
   */
  function markAllAsRead() {
    notifications.value.forEach(n => {
      n.read = true
    })
  }

  /**
   * 清空所有通知
   */
  function clearNotifications() {
    notifications.value = []
  }

  /**
   * 设置连接状态
   * @param {boolean} status 
   */
  function setConnected(status) {
    connected.value = status
  }

  return {
    notifications,
    unreadCount,
    connected,
    addNotification,
    markAsRead,
    markAllAsRead,
    clearNotifications,
    setConnected
  }
})