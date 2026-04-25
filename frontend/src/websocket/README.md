# WebSocket 实时通知使用指南

## 一、依赖说明

项目已包含 `socket.io-client`，无需额外安装：

```json
{
  "dependencies": {
    "socket.io-client": "^4.8.3"
  }
}
```

## 二、文件结构

```
frontend/src/
├── websocket/
│   ├── socket.js              # WebSocket 客户端单例（核心）
│   ├── useNotification.js     # 组合式函数封装
│   └── README.md              # 本文档
├── stores/
│   └── modules/
│       └── notification.js    # 通知状态管理 Store
└── components/
    └── common/
        └── NotificationBell.vue # 通知铃铛组件
```

## 三、快速开始

### 3.1 在导航组件中使用通知铃铛

在页头导航组件（如 `SiteNav.vue`）中添加通知铃铛：

```vue
<template>
  <header>
    <!-- 其他导航内容 -->
    <NotificationBell />
  </header>
</template>

<script setup>
import NotificationBell from '@/components/common/NotificationBell.vue'
</script>
```

### 3.2 在特定页面监听通知

使用组合式函数监听特定类型的通知：

```vue
<template>
  <div>
    <h1>物品详情</h1>
    <!-- 页面内容 -->
  </div>
</template>

<script setup>
import { useNotification } from '@/websocket/useNotification'

// 监听物品审核结果通知
useNotification({
  subscribeTypes: ['ITEM_AUDITED'],
  onNotification: (message) => {
    console.log('收到物品审核通知:', message)
    // 根据 businessId 判断是否是当前物品
    if (message.businessId === currentItemId.value) {
      // 刷新页面或更新状态
      refreshItem()
    }
  }
})
</script>
```

## 四、API 参考

### 4.1 WebSocket 客户端 (socket.js)

```javascript
import { wsClient, NotificationType } from '@/websocket/socket'

// 连接状态
wsClient.isConnected()  // => boolean

// 手动连接
await wsClient.connect()

// 断开连接
wsClient.disconnect()

// 订阅私有通知（用户专属）
wsClient.subscribeToUserNotifications(userId)

// 订阅公共广播
wsClient.subscribeToPublicBroadcast()

// 添加事件监听
wsClient.on('notification', (message) => { /* ... */ })
wsClient.on('claim_approved', (message) => { /* ... */ })

// 移除事件监听
wsClient.off('notification', callback)
```

### 4.2 通知 Store (notification.js)

```javascript
import { useNotificationStore } from '@/stores/modules/notification'
import { storeToRefs } from 'pinia'

const store = useNotificationStore()

// 响应式数据
store.notifications      // 通知列表
store.unreadCount        // 未读数量
store.panelVisible       // 面板是否显示
store.wsConnected        // WebSocket 连接状态

// 方法
store.initWebSocket()         // 初始化连接
store.disconnectWebSocket()   // 断开连接
store.markAsRead(id)          // 标记已读
store.markAllAsRead()         // 全部已读
store.removeNotification(id)  // 删除通知
store.clearAll()              // 清空全部
```

### 4.3 组合式函数 (useNotification.js)

```javascript
import { useNotification, useUnreadCount } from '@/websocket/useNotification'

// 完整用法
const {
  notifications,      // 通知列表
  unreadCount,       // 未读数量
  unreadNotifications, // 未读通知列表
  isConnected,       // 连接状态
  markAsRead,        // 标记已读
  markAllAsRead,     // 全部已读
  removeNotification, // 删除通知
  clearAll,          // 清空全部
  NotificationType,  // 通知类型常量
} = useNotification({
  subscribeTypes: ['CLAIM_APPROVED', 'CLAIM_REJECTED'],
  onNotification: (msg) => console.log('收到通知:', msg)
})

// 仅获取未读数量
const { unreadCount, unreadNotifications } = useUnreadCount()
```

## 五、通知类型

| 类型 | 说明 | 触发场景 |
|------|------|----------|
| `CLAIM_APPROVED` | 认领申请通过 | 管理员审核通过认领申请 |
| `CLAIM_REJECTED` | 认领申请被拒绝 | 管理员拒绝认领申请 |
| `ITEM_AUDITED` | 物品审核结果 | 管理员审核物品发布 |
| `SYSTEM_NOTICE` | 系统公告 | 系统推送公告 |

## 六、通知消息结构

```typescript
interface NotificationMessage {
  type: string           // 通知类型
  title: string           // 通知标题
  content: string        // 通知内容
  businessId: number     // 关联业务ID
  businessType: string   // 关联业务类型 (ITEM/CLAIM/REPORT)
  sentAt: string         // 发送时间 (ISO格式)
  fromUserId?: number    // 发送人ID
  toUserId?: number       // 目标用户ID
}
```

## 七、注意事项

1. **自动连接管理**：WebSocket 连接由路由守卫自动管理，用户登录后自动连接，登出后自动断开。

2. **Token 刷新**：如果使用 JWT，建议在 Token 刷新时重新建立连接。

3. **多标签页**：多个标签页会各自建立连接，后端会向每个标签页推送通知。

4. **重连机制**：默认最多重连 5 次，每次间隔 3 秒。

5. **消息持久化**：通知会保存到 localStorage，刷新页面后仍可查看历史通知。

## 八、环境变量

创建 `.env` 文件配置 WebSocket 地址：

```env
# 开发环境
VITE_WS_BASE_URL=http://localhost:8080
VITE_API_BASE_URL=http://localhost:8080

# 生产环境
VITE_WS_BASE_URL=https://your-api-domain.com
VITE_API_BASE_URL=https://your-api-domain.com
```
