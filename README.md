# 校园失物招领系统

基于 Spring Boot + Vue 3 的前后端分离校园失物招领系统。

## 项目地址

GitHub: https://github.com/317232/campus_lost_system.git

---

## 技术栈

### 后端

| 技术 | 版本 |
|------|------|
| Spring Boot | 3.3.5 |
| MyBatis-Plus | 3.5.7 |
| Spring Security + JWT | - |
| MySQL | 8.x |
| Java | 17 |

### 前端

| 技术 | 版本 |
|------|------|
| Vue 3 | - |
| Vite | - |
| Pinia | - |
| Vue Router | - |
| Axios | - |

---

## 项目结构

```
campus_lost_system/
├── backend/                    # Spring Boot 后端
│   └── src/main/java/com/campus/lostfound/
│       ├── LostFoundApplication.java   # 启动类
│       ├── admin/             # 管理员模块（审核、统计、用户管理）
│       ├── auth/              # 认证模块（登录、注册、找回密码）
│       ├── claim/             # 认领模块（草稿机制 + 正式申请）
│       ├── common/           # 通用模块（ApiResponse、PageResponse、OperationLog、ContactService）
│       ├── config/           # 配置模块（Security、CORS）
│       ├── domain/           # 领域模型（Entity、Mapper）
│       ├── file/             # 文件上传模块
│       ├── item/             # 统一物品模块（失物 + 招领）
│       ├── notice/           # 公告模块
│       ├── security/         # 安全模块（JWT 过滤器、Token 工具）
│       └── user/             # 用户模块（个人资料）
├── frontend/                  # Vue 3 前端
│   └── src/
│       ├── api/              # 接口封装
│       │   └── modules/     # 按业务模块拆分（admin, auth, categories, claims, items, notices, reports, user）
│       ├── components/       # 公共组件
│       ├── composables/      # 组合式 API 复用
│       ├── layouts/          # 页面布局（MainLayout、AuthLayout、AdminLayout）
│       ├── router/           # 路由配置（含路由守卫）
│       ├── stores/           # Pinia 状态管理
│       ├── utils/            # 工具函数
│       └── views/            # 页面视图
├── docx/                      # 需求文档
└── sql/                       # 数据库脚本（init.sql）
```

---

## 核心功能

| 模块 | 功能 |
|------|------|
| 用户 | 注册、登录、个人资料管理、密码修改 |
| 物品 | 统一物品发布（失物/招领）、搜索筛选、详情查看 |
| 认领 | 关键词验证草稿 → 正式申请 → 管理员审核 |
| 管理员 | 待审物品/认领列表、审核操作、用户管理、公告管理、举报管理、数据统计 |
| 联系方式 | 脱敏展示 + 查看日志记录 |
| 举报 | 用户举报反馈（物品/认领/公告/用户） |
| 操作日志 | 管理员关键操作自动记录 |

---

## 核心 API

### 认证 `/api/auth`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| POST | `/api/auth/send-email-code` | 发送邮箱验证码 |
| POST | `/api/auth/forgot-password` | 忘记密码 |
| POST | `/api/auth/reset-password` | 重置密码 |
| POST | `/api/auth/logout` | 登出 |

### 物品 `/api/items`（统一 API）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/items?scene=lost\|found` | 物品列表（支持 keyword/category/zone 筛选） |
| GET | `/api/items/{id}` | 物品详情 |
| POST | `/api/items` | 发布物品（需认证） |
| PUT | `/api/items/{id}` | 修改物品（需认证） |
| DELETE | `/api/items/{id}` | 删除物品（需认证） |
| GET | `/api/items/me` | 我的发布记录（需认证） |

### 认领 `/api/claims`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/claims/draft` | 创建认领草稿（关键词验证） |
| POST | `/api/claims` | 提交正式认领申请（需认证） |
| GET | `/api/claims` | 我的认领记录（需认证） |

### 管理员 `/api/admin`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/items/review` | 待审物品列表（分页） |
| POST | `/api/admin/items/audit` | 审核物品 |
| GET | `/api/admin/claims/review` | 待审认领列表（分页） |
| POST | `/api/admin/claims/audit` | 审核认领 |
| GET | `/api/admin/stats/dashboard` | 统计大盘 |
| GET | `/api/admin/statistics/trend` | 趋势统计 |
| GET | `/api/admin/users` | 用户列表（分页） |
| PUT | `/api/admin/users/{id}/status` | 禁用/启用用户 |
| GET | `/api/admin/notices` | 公告列表 |
| POST | `/api/admin/notices` | 发布公告 |
| GET | `/api/admin/reports` | 举报列表（分页） |

### 文件 `/api/files`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/files/upload` | 单文件上传 |
| POST | `/api/files/uploads` | 多文件上传 |

---

## 认领流程（两阶段）

```
用户提交关键词验证
       │
       ▼
  POST /api/claims/draft
  生成 draft_token（24小时有效）
       │
       ▼
  POST /api/claims（携带 draft_token）
  状态变为 REVIEW_PENDING
       │
       ▼
  管理员审核 POST /api/admin/claims/audit
  通过 → 物品状态改为 CLAIMED
  驳回 → 记录驳回原因
```

---

## 数据库设计

基于 `init.sql` 统一物品表设计：

- **`items`** 表通过 `scene`（lost/found）字段统一存储失物和招领信息
- **`item_contacts`** 支持联系方式脱敏（contact_visibility 控制）
- **`item_audits`** 记录物品审核历史
- **`claim_drafts`** 实现认领草稿机制
- **`operation_logs`** 记录管理员操作日志
- **`contact_unlock_logs`** 记录联系方式查看日志

---

## 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.x

---

## 快速启动

### 后端

```bash
cd backend
# 配置 application.yml 中的数据库连接
mvn spring-boot:run
```

### 前端

```bash
cd frontend
npm install
npm run dev
```
