# WebSocket 实时通知架构设计

> 更新时间：2026-04-25
> 功能：认领审核结果实时通知
> 状态：✅ 已实现

---

## 一、架构概览

### 1.1 技术选型

- **后端**：Spring Boot WebSocket (基于 STOMP 协议)
- **前端**：Vue 3 + stompjs (STOMP 协议客户端)
- **消息代理**：内置简单内存代理（适合单机部署）

### 1.2 WebSocket 端点

| 端点                     | 说明               | 权限     |
| ------------------------ | ------------------ | -------- |
| `/ws`                    | WebSocket 连接端点 | 需认证   |
| `/topic/notify/{userId}` | 用户私有通知主题   | 仅本人   |
| `/topic/public`          | 公共广播主题       | 所有用户 |

### 1.3 通知类型

| 类型             | 触发场景         | 接收人   |
| ---------------- | ---------------- | -------- |
| `CLAIM_APPROVED` | 认领申请通过     | 申请人   |
| `CLAIM_REJECTED` | 认领申请被拒绝   | 申请人   |
| `ITEM_AUDITED`   | 物品审核结果通知 | 发布人   |
| `SYSTEM_NOTICE`  | 系统公告推送     | 所有用户 |

---

## 二、后端实现

### 2.1 依赖

```xml
<!-- Spring Boot WebSocket -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2.2 核心组件

```
websocket/
├── config/
│   └── WebSocketConfig.java          # WebSocket 配置类
├── handler/
│   └── NotificationHandler.java       # 消息处理Handler
├── service/
│   └── NotificationService.java       # 通知服务接口
│   └── impl/
│       └── NotificationServiceImpl.java
└── dto/
    └── NotificationMessage.java       # 通知消息DTO
```

### 2.3 安全集成

- WebSocket 连接前需通过 JWT 获取 token
- 连接时携带 token 进行身份验证
- 心跳机制保持连接活跃

---

## 三、前端实现

### 3.1 依赖

```bash
npm install stompjs
```

### 3.2 核心组件

```
src/
├── websocket/
│   ├── stomp-client.js     # STOMP 客户端单例（已实现）
│   ├── socket.js          # socket.io 客户端（备用）
│   └── useNotification.js # 组合式函数
└── stores/
    └── modules/
        └── notification.js # 通知状态管理（Pinia）
└── components/
    └── common/
        └── NotificationBell.vue # 通知铃铛组件
```

---

## 四、通知流程

### 4.1 认领审核通知流程

```
1. 管理员审核认领申请 (ClaimAudit)
2. 调用 NotificationService.pushClaimResult()
3. WebSocket 推送通知到用户私有主题
4. 前端接收并展示通知
5. 用户可点击查看详情
```

### 4.2 物品审核通知流程

```
1. 管理员审核物品 (ItemAudit)
2. 调用 NotificationService.pushItemAuditResult()
3. WebSocket 推送通知到用户私有主题
4. 前端接收并展示通知
```

---

## 五、数据库变更

**无需数据库变更** - 复用现有 OperationLog 表记录通知日志

---

## 六、配置项

```yaml
websocket:
  allowed-origins: "*"
  heartbeat:
    interval: 25000 # 心跳间隔(ms)
    timeout: 60000 # 超时时间(ms)
```

---

## 七、风险与限制

| 风险       | 影响               | 缓解措施               |
| ---------- | ------------------ | ---------------------- |
| 多实例部署 | 消息无法跨实例推送 | 后续引入 Redis Pub/Sub |
| 连接数限制 | 高并发时连接受限   | 水平扩展 + 连接池      |
| 消息可靠性 | 断线可能丢失消息   | 消息持久化 + 重试机制  |
