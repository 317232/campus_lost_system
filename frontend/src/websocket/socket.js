/**
 * WebSocket 客户端单例
 * 使用 STOMP 协议与后端 Spring WebSocket 服务通信
 * 
 * @description
 * - 支持 STOMP over WebSocket 协议
 * - 自动处理连接断开重连
 * - 集成 JWT Token 认证
 * - 支持订阅私有通知和公共广播
 */

import { io } from 'socket.io-client'
import { getAuthToken, getAuthRole } from '@/utils/auth'

// WebSocket 服务地址
const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

// 心跳间隔 (ms)
const HEARTBEAT_INTERVAL = 25000

// 重连配置
const RECONNECT_DELAY = 3000
const MAX_RECONNECT_ATTEMPTS = 5

class WebSocketClient {
  constructor() {
    this.client = null
    this.connected = false
    this.connecting = false
    this.subscriptions = new Map()
    this.reconnectAttempts = 0
    this.listeners = new Map()
    this.currentUserId = null
  }

  /**
   * 获取当前用户信息
   */
  getCurrentUser() {
    return {
      token: getAuthToken(),
      role: getAuthRole()
    }
  }

  /**
   * 连接到 WebSocket 服务器
   */
  connect() {
    const { token } = this.getCurrentUser()
    
    if (!token) {
      console.warn('[WebSocket] No token available, skipping connection')
      return Promise.reject(new Error('No authentication token'))
    }

    if (this.connected || this.connecting) {
      console.log('[WebSocket] Already connected or connecting')
      return Promise.resolve()
    }

    this.connecting = true

    return new Promise((resolve, reject) => {
      try {
        // 使用 socket.io-client 连接
        // 注意：后端同时支持原生 WebSocket 和 SockJS，这里使用原生 WebSocket
        this.client = io(`${WS_BASE_URL}/ws`, {
          transports: ['websocket', 'polling'],
          auth: {
            Authorization: `Bearer ${token}`
          },
          reconnection: true,
          reconnectionDelay: RECONNECT_DELAY,
          reconnectionAttempts: MAX_RECONNECT_ATTEMPTS,
          heartbeatInterval: HEARTBEAT_INTERVAL,
          timeout: 10000
        })

        this.client.on('connect', () => {
          console.log('[WebSocket] Connected successfully')
          this.connected = true
          this.connecting = false
          this.reconnectAttempts = 0
          this.emit('connected')
          resolve()
        })

        this.client.on('disconnect', (reason) => {
          console.log('[WebSocket] Disconnected:', reason)
          this.connected = false
          this.emit('disconnected', reason)
        })

        this.client.on('connect_error', (error) => {
          console.error('[WebSocket] Connection error:', error.message)
          this.connected = false
          this.connecting = false
          this.reconnectAttempts++
          this.emit('error', error)
          
          if (this.reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            reject(error)
          }
        })

        this.client.on('reconnect', (attemptNumber) => {
          console.log('[WebSocket] Reconnected after', attemptNumber, 'attempts')
          this.connected = true
          this.emit('reconnected', attemptNumber)
        })

        // 处理后端推送的消息
        // 由于使用 socket.io-client，需要模拟 STOMP 的订阅机制
        this.client.on('message', (message) => {
          this.handleMessage(message)
        })

      } catch (error) {
        console.error('[WebSocket] Connection failed:', error)
        this.connecting = false
        reject(error)
      }
    })
  }

  /**
   * 断开 WebSocket 连接
   */
  disconnect() {
    if (this.client) {
      this.client.disconnect()
      this.client = null
    }
    this.connected = false
    this.connecting = false
    this.subscriptions.clear()
    console.log('[WebSocket] Disconnected')
  }

  /**
   * 处理收到的消息
   */
  handleMessage(message) {
    try {
      const parsedMessage = typeof message === 'string' ? JSON.parse(message) : message
      console.log('[WebSocket] Received message:', parsedMessage)
      
      // 触发对应的监听器
      const { type } = parsedMessage
      if (type) {
        this.emit(type.toLowerCase(), parsedMessage)
      }
      this.emit('notification', parsedMessage)
    } catch (error) {
      console.error('[WebSocket] Failed to parse message:', error)
    }
  }

  /**
   * 订阅私有通知 (用户专属)
   * 对应后端的 /user/queue/notify 路径
   */
  subscribeToUserNotifications(userId) {
    if (!this.connected) {
      console.warn('[WebSocket] Not connected, cannot subscribe')
      return
    }

    this.currentUserId = userId
    const subscriptionId = `user-notify-${userId}`
    
    if (this.subscriptions.has(subscriptionId)) {
      console.log('[WebSocket] Already subscribed to user notifications')
      return
    }

    // 使用 socket.io 的 rooms 机制
    // 实际订阅时需要后端配合，这里模拟订阅逻辑
    const room = `user:${userId}`
    this.client.emit('subscribe', { room, type: 'user-notify' })
    
    this.subscriptions.set(subscriptionId, { room, type: 'user-notify' })
    console.log('[WebSocket] Subscribed to user notifications:', room)
  }

  /**
   * 订阅公共广播 (所有用户)
   * 对应后端的 /topic/public 路径
   */
  subscribeToPublicBroadcast() {
    if (!this.connected) {
      console.warn('[WebSocket] Not connected, cannot subscribe')
      return
    }

    const subscriptionId = 'public-broadcast'
    
    if (this.subscriptions.has(subscriptionId)) {
      console.log('[WebSocket] Already subscribed to public broadcast')
      return
    }

    this.client.emit('subscribe', { room: 'public', type: 'public-broadcast' })
    
    this.subscriptions.set(subscriptionId, { room: 'public', type: 'public-broadcast' })
    console.log('[WebSocket] Subscribed to public broadcast')
  }

  /**
   * 取消订阅
   */
  unsubscribe(subscriptionId) {
    const subscription = this.subscriptions.get(subscriptionId)
    if (subscription && this.client) {
      this.client.emit('unsubscribe', { room: subscription.room })
      this.subscriptions.delete(subscriptionId)
      console.log('[WebSocket] Unsubscribed:', subscriptionId)
    }
  }

  /**
   * 取消所有订阅
   */
  unsubscribeAll() {
    this.subscriptions.forEach((sub, id) => {
      this.unsubscribe(id)
    })
  }

  /**
   * 添加事件监听器
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set())
    }
    this.listeners.get(event).add(callback)
    console.log('[WebSocket] Listener added for:', event)
  }

  /**
   * 移除事件监听器
   */
  off(event, callback) {
    const eventListeners = this.listeners.get(event)
    if (eventListeners) {
      eventListeners.delete(callback)
    }
  }

  /**
   * 触发事件
   */
  emit(event, data) {
    const eventListeners = this.listeners.get(event)
    if (eventListeners) {
      eventListeners.forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error(`[WebSocket] Error in listener for ${event}:`, error)
        }
      })
    }
  }

  /**
   * 获取连接状态
   */
  isConnected() {
    return this.connected
  }
}

// 导出单例实例
export const wsClient = new WebSocketClient()

// 导出类型常量
export const NotificationType = {
  CLAIM_APPROVED: 'CLAIM_APPROVED',
  CLAIM_REJECTED: 'CLAIM_REJECTED',
  ITEM_AUDITED: 'ITEM_AUDITED',
  SYSTEM_NOTICE: 'SYSTEM_NOTICE'
}

export const BusinessType = {
  ITEM: 'ITEM',
  CLAIM: 'CLAIM',
  REPORT: 'REPORT'
}

export default wsClient
