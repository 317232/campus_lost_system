# WebSocket 依赖安装说明

## 当前依赖状态

项目 `package.json` 已包含 `socket.io-client`：

```json
{
  "dependencies": {
    "socket.io-client": "^4.8.3"
  }
}
```

## 已创建的文件

### 1. WebSocket 客户端 (socket.js)
- 位置：`src/websocket/socket.js`
- 功能：WebSocket 客户端单例，使用 socket.io-client

### 2. STOMP 客户端（可选）
- 位置：`src/websocket/stomp-client.js`
- 功能：基于 stompjs 的 STOMP 协议实现（可选，需要额外安装依赖）

### 3. 通知 Store
- 位置：`src/stores/modules/notification.js`
- 功能：管理通知状态，与 WebSocket 集成

### 4. 通知铃铛组件
- 位置：`src/components/common/NotificationBell.vue`
- 功能：全局通知展示组件

### 5. 组合式函数
- 位置：`src/websocket/useNotification.js`
- 功能：便捷的通知功能封装

## 依赖安装

### 方案一：使用 socket.io-client（推荐，已安装）

无需额外安装，直接使用 `socket.io-client`：

```bash
npm install
```

### 方案二：使用 stompjs（可选）

如果需要完整的 STOMP 协议支持，可以安装 stompjs：

```bash
npm install stompjs sockjs-client
```

然后将 `src/stores/modules/notification.js` 中的导入改为：

```javascript
// 从
import { wsClient, NotificationType } from '@/websocket/socket'

// 改为
import { stompClient as wsClient, NotificationType } from '@/websocket/stomp-client'
```

## 环境变量配置

在 `frontend/` 目录下创建 `.env` 文件：

```env
# WebSocket 服务地址
VITE_WS_BASE_URL=http://localhost:8080

# API 基础地址（用于回退）
VITE_API_BASE_URL=http://localhost:8080
```

## 验证安装

1. 启动后端服务
2. 启动前端：`npm run dev`
3. 登录用户
4. 打开浏览器开发者工具，查看控制台输出：
   - `[WebSocket] Connected successfully` 表示连接成功
   - `[NotificationStore] WebSocket connected` 表示通知 Store 已初始化

## 故障排除

### 问题：WebSocket 连接失败

1. 检查后端服务是否启动
2. 检查 `VITE_WS_BASE_URL` 配置是否正确
3. 检查 CORS 配置（后端 `WebSocketConfig.java` 中的 `setAllowedOriginPatterns("*")`）

### 问题：无法收到通知

1. 确认用户已登录且有有效 token
2. 检查 `NotificationService` 是否正确调用
3. 确认订阅路径与后端配置一致

### 问题：跨域问题

后端已配置允许所有来源：
```java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns("*")
    .withSockJS();
```

如需限制来源，请修改 `WebSocketConfig.java`。
