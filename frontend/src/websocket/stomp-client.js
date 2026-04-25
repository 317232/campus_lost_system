/**
 * WebSocket STOMP 客户端（可选实现）
 * 
 * @description
 * 使用 stompjs 库连接 Spring WebSocket STOMP 端点
 * 如果需要完整的 STOMP 协议支持，请安装依赖：
 *   npm install stompjs
 * 
 * 此文件作为备用方案，当前默认使用 socket.io-client
 */

import { Client } from 'stompjs'
import { getAuthToken } from '@/utils/auth'

// WebSocket 服务地址
const WS_BASE_URL = import.meta.env.VITE_WS_BASE_URL || 
                    import.meta.env.VITE_API_BASE_URL?.replace('http', 'ws') || 
                    'ws://localhost:8080'

class StompClient {
  constructor() {
    this.client = null
    this.connected = false
    this.subscriptions = new Map()
    this.listeners = new Map()
  }

  /**
   * 连接到 WebSocket STOMP 服务器
   */
  connect() {
    const token = getAuthToken()
    
    if (!token) {
      console.warn('[STOMP] No token available, skipping connection')
      return Promise.reject(new Error('No authentication token'))
    }

    if (this.connected) {
      console.log('[STOMP] Already connected')
      return Promise.resolve()
    }

    return new Promise((resolve, reject) => {
      try {
        // 创建 STOMP 客户端
        // 注意：需要根据后端配置选择合适的 WebSocket 端点
        // 如果使用 SockJS：/ws -> SockJS 端点
        // 如果使用原生 WebSocket：/ws -> 原生 WebSocket 端点
        this.client = new Client({
          brokerURL: `${WS_BASE_URL}/ws`,
          // 或者使用 SockJS：
          // connectHeaders: { Authorization: `Bearer ${token}` },
          // webSocketFactory: () => new SockJS(`${WS_BASE_URL}/ws`),
        })

        this.client.connectHeaders = {
          Authorization: `Bearer ${token}`
        }

        this.client.onConnect = () => {
          console.log('[STOMP] Connected successfully')
          this.connected = true
          this.emit('connected')
          resolve()
        }

        this.client.onDisconnect = () => {
          console.log('[STOMP] Disconnected')
          this.connected = false
          this.emit('disconnected')
        }

        this.client.onStompError = (frame) => {
          console.error('[STOMP] STOMP error:', frame)
          this.emit('error', frame)
        }

        this.client.onWebSocketError = (error) => {
          console.error('[STOMP] WebSocket error:', error)
          this.emit('error', error)
        }

        // 激活连接
        this.client.activate()

      } catch (error) {
        console.error('[STOMP] Connection failed:', error)
        reject(error)
      }
    })
  }

  /**
   * 断开连接
   */
  disconnect() {
    if (this.client) {
      this.client.deactivate()
      this.client = null
    }
    this.connected = false
    this.subscriptions.clear()
  }

  /**
   * 订阅用户私有通知
   * 对应后端的 /user/queue/notify
   */
  subscribeToUserNotifications(userId) {
    if (!this.connected) {
      console.warn('[STOMP] Not connected, cannot subscribe')
      return
    }

    const subscriptionId = `user-notify-${userId}`
    
    if (this.subscriptions.has(subscriptionId)) {
      console.log('[STOMP] Already subscribed to user notifications')
      return
    }

    // 订阅用户私有通知
    // Spring 会自动将消息路由到对应用户
    const subscription = this.client.subscribe(
      `/user/queue/notify`,
      (message) => {
        const body = JSON.parse(message.body)
        this.handleMessage(body)
      }
    )

    this.subscriptions.set(subscriptionId, subscription)
    console.log('[STOMP] Subscribed to user notifications')
  }

  /**
   * 订阅公共广播
   * 对应后端的 /topic/public
   */
  subscribeToPublicBroadcast() {
    if (!this.connected) {
      console.warn('[STOMP] Not connected, cannot subscribe')
      return
    }

    const subscriptionId = 'public-broadcast'
    
    if (this.subscriptions.has(subscriptionId)) {
      console.log('[STOMP] Already subscribed to public broadcast')
      return
    }

    const subscription = this.client.subscribe(
      '/topic/public',
      (message) => {
        const body = JSON.parse(message.body)
        this.handleMessage(body)
      }
    )

    this.subscriptions.set(subscriptionId, subscription)
    console.log('[STOMP] Subscribed to public broadcast')
  }

  /**
   * 取消订阅
   */
  unsubscribe(subscriptionId) {
    const subscription = this.subscriptions.get(subscriptionId)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(subscriptionId)
      console.log('[STOMP] Unsubscribed:', subscriptionId)
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
   * 处理收到的消息
   */
  handleMessage(message) {
    console.log('[STOMP] Received message:', message)
    
    const { type } = message
    if (type) {
      this.emit(type.toLowerCase(), message)
    }
    this.emit('notification', message)
  }

  /**
   * 添加事件监听器
   */
  on(event, callback) {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set())
    }
    this.listeners.get(event).add(callback)
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
          console.error(`[STOMP] Error in listener for ${event}:`, error)
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
export const stompClient = new StompClient()

// 导出类型常量
export const NotificationType = {
  CLAIM_APPROVED: 'CLAIM_APPROVED',
  CLAIM_REJECTED: 'CLAIM_REJECTED',
  ITEM_AUDITED: 'ITEM_AUDITED',
  SYSTEM_NOTICE: 'SYSTEM_NOTICE'
}

export default stompClient
