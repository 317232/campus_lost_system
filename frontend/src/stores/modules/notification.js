/**
 * 通知状态管理 Store
 * 
 * @description
 * - 管理通知列表（未读/已读/全部）
 * - 处理新通知的添加
 * - 维护通知计数
 * - 与 WebSocket 集成实现实时通知
 */

import { defineStore } from 'pinia'
import { stompClient, NotificationType } from '@/websocket/stomp-client'

// WebSocket 客户端实例（使用 STOMP 协议）
const wsClient = stompClient

// 通知存储键名
const NOTIFICATION_STORAGE_KEY = 'campus-notifications'

// 最大存储通知数量
const MAX_NOTIFICATIONS = 100

/**
 * 从 localStorage 加载通知
 */
function loadNotifications() {
  try {
    const stored = localStorage.getItem(NOTIFICATION_STORAGE_KEY)
    return stored ? JSON.parse(stored) : []
  } catch {
    return []
  }
}

/**
 * 保存通知到 localStorage
 */
function saveNotifications(notifications) {
  try {
    localStorage.setItem(NOTIFICATION_STORAGE_KEY, JSON.stringify(notifications))
  } catch (error) {
    console.error('Failed to save notifications:', error)
  }
}

/**
 * 创建默认状态
 */
function defaultState() {
  return {
    /** @type {Array} 通知列表 */
    notifications: [],
    /** @type {Number} 未读通知数量 */
    unreadCount: 0,
    /** @type {Boolean} 是否有新通知（用于触发动画） */
    hasNewNotification: false,
    /** @type {Boolean} 通知面板是否展开 */
    panelVisible: false,
    /** @type {Boolean} WebSocket 连接状态 */
    wsConnected: false,
    /** @type {Number} 重连尝试次数 */
    reconnectAttempts: 0,
  }
}

export const useNotificationStore = defineStore('notification', {
  state: () => ({
    ...defaultState(),
    notifications: loadNotifications(),
  }),

  getters: {
    /** 是否已登录 */
    isAuthenticated: (state, getters, rootState) => {
      try {
        const { useUserStore } = require('@/stores/modules/user')
        const userStore = useUserStore()
        return userStore.isAuthenticated
      } catch {
        return false
      }
    },

    /** 获取用户ID */
    currentUserId: (state, getters, rootState) => {
      try {
        const { useUserStore } = require('@/stores/modules/user')
        const userStore = useUserStore()
        return userStore.profile?.id || null
      } catch {
        return null
      }
    },

    /** 未读通知列表 */
    unreadNotifications: (state) => {
      return state.notifications.filter(n => !n.read)
    },

    /** 已读通知列表 */
    readNotifications: (state) => {
      return state.notifications.filter(n => n.read)
    },

    /** 获取指定类型通知 */
    getByType: (state) => (type) => {
      return state.notifications.filter(n => n.type === type)
    },

    /** 获取指定业务ID的通知 */
    getByBusinessId: (state) => (businessId) => {
      return state.notifications.filter(n => n.businessId === businessId)
    },
  },

  actions: {
    /**
     * 初始化 WebSocket 连接
     * 应在用户登录后调用
     */
    initWebSocket() {
      // 监听连接事件
      wsClient.on('connected', () => {
        this.wsConnected = true
        this.reconnectAttempts = 0
        console.log('[NotificationStore] WebSocket connected')
        
        // 订阅用户私有通知
        if (this.currentUserId) {
          wsClient.subscribeToUserNotifications(this.currentUserId)
        }
        
        // 订阅公共广播
        wsClient.subscribeToPublicBroadcast()
      })

      wsClient.on('disconnected', (reason) => {
        this.wsConnected = false
        console.log('[NotificationStore] WebSocket disconnected:', reason)
      })

      wsClient.on('reconnected', (attemptNumber) => {
        this.wsConnected = true
        console.log('[NotificationStore] WebSocket reconnected after', attemptNumber, 'attempts')
      })

      wsClient.on('error', (error) => {
        console.error('[NotificationStore] WebSocket error:', error)
        this.reconnectAttempts++
      })

      // 监听所有通知类型
      this.setupNotificationListeners()

      // 连接到 WebSocket
      wsClient.connect().catch(error => {
        console.error('[NotificationStore] Failed to connect:', error)
      })
    },

    /**
     * 设置通知监听器
     */
    setupNotificationListeners() {
      // 通用通知监听
      wsClient.on('notification', (message) => {
        this.handleIncomingNotification(message)
      })

      // 各类型通知监听
      Object.values(NotificationType).forEach(type => {
        wsClient.on(type.toLowerCase(), (message) => {
          this.handleIncomingNotification(message)
        })
      })
    },

    /**
     * 处理收到的通知
     */
    handleIncomingNotification(message) {
      const notification = {
        ...message,
        id: this.generateId(),
        read: false,
        receivedAt: new Date().toISOString(),
      }

      // 添加到列表头部
      this.notifications.unshift(notification)
      
      // 限制最大数量
      if (this.notifications.length > MAX_NOTIFICATIONS) {
        this.notifications = this.notifications.slice(0, MAX_NOTIFICATIONS)
      }

      // 更新未读计数
      this.unreadCount = this.notifications.filter(n => !n.read).length

      // 触发新通知动画
      this.hasNewNotification = true
      setTimeout(() => {
        this.hasNewNotification = false
      }, 1000)

      // 保存到 localStorage
      saveNotifications(this.notifications)

      console.log('[NotificationStore] New notification:', notification)
    },

    /**
     * 生成唯一ID
     */
    generateId() {
      return `notif-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    },

    /**
     * 标记通知为已读
     */
    markAsRead(notificationId) {
      const notification = this.notifications.find(n => n.id === notificationId)
      if (notification && !notification.read) {
        notification.read = true
        notification.readAt = new Date().toISOString()
        this.unreadCount = this.notifications.filter(n => !n.read).length
        saveNotifications(this.notifications)
      }
    },

    /**
     * 标记所有通知为已读
     */
    markAllAsRead() {
      const now = new Date().toISOString()
      this.notifications.forEach(n => {
        if (!n.read) {
          n.read = true
          n.readAt = now
        }
      })
      this.unreadCount = 0
      saveNotifications(this.notifications)
    },

    /**
     * 删除通知
     */
    removeNotification(notificationId) {
      const index = this.notifications.findIndex(n => n.id === notificationId)
      if (index > -1) {
        const wasUnread = !this.notifications[index].read
        this.notifications.splice(index, 1)
        if (wasUnread) {
          this.unreadCount = this.notifications.filter(n => !n.read).length
        }
        saveNotifications(this.notifications)
      }
    },

    /**
     * 清空所有通知
     */
    clearAll() {
      this.notifications = []
      this.unreadCount = 0
      saveNotifications([])
    },

    /**
     * 切换通知面板
     */
    togglePanel() {
      this.panelVisible = !this.panelVisible
    },

    /**
     * 打开通知面板
     */
    openPanel() {
      this.panelVisible = true
    },

    /**
     * 关闭通知面板
     */
    closePanel() {
      this.panelVisible = false
    },

    /**
     * 断开 WebSocket 连接
     * 应在用户登出时调用
     */
    disconnectWebSocket() {
      wsClient.disconnect()
      this.wsConnected = false
      this.reconnectAttempts = 0
    },

    /**
     * 重置状态
     */
    $reset() {
      const defaults = defaultState()
      Object.assign(this, defaults, {
        notifications: loadNotifications(),
      })
    },
  },
})
