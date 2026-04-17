# campus_lost_system

基于 spring boot + vue 3 的前后端分离的校园失物招领系统。属于课设，毕设项目。

<br />

项目地址：github: <https://github.com/317232/campus_lost_system.git>



## 后端架构（目前）

框架: Spring Boot 3.3.5
持久层: MyBatis-Plus 3.5.7
数据库: MySQL
安全: Spring Security + JWT (Json Web Token)
密码加密: BCrypt
邮件服务: Spring Boot Mail
依赖管理: Maven
编程语言: Java 17

backend/
├── pom.xml # Maven 依赖配置
├── src/
│ ├── main/
│ │ ├── java/com/campus/lostfound/
│ │ │ ├── LostFoundApplication.java # 应用启动类
│ │ │ ├── admin/ # 管理员模块
│ │ │ ├── auth/ # 认证授权模块
│ │ │ ├── catalog/ # 分类管理模块
│ │ │ ├── claim/ # 认领管理模块
│ │ │ ├── common/ # 公共组件
│ │ │ ├── config/ # 配置类
│ │ │ ├── demo/ # 示例数据
│ │ │ ├── domain/ # 领域模型
│ │ │ ├── found/ # 招领模块
│ │ │ ├── lost/ # 失物模块
│ │ │ ├── mapper/ # 数据访问层接口
│ │ │ ├── notice/ # 公告模块
│ │ │ ├── report/ # 举报模块
│ │ │ ├── security/ # 安全相关
│ │ │ └── user/ # 用户模块
│ │ └── resources/
│ │ ├── application.yml # 应用配置
│ │ └── static/ # 静态资源
│ └── test/ # 测试代码
└── target/

（每个模块有 dto + service\&Impl + controller）

## 前端架构（目前）

技术选型：Vite + JavaScript + Pinia + Vue Router + Axios

frontend/
├── public/ # 静态资源（favicon.ico 等）
├── src/
│ ├── api/ # 所有接口封装（最重要！）
│ │ ├── index.js # axios 实例
│ │ ├── modules/ # 按业务模块拆分接口
│ │ │ ├── user.js
│ │ │ ├── project.js
│ │ │ └── ...
│ │ └── interceptors.js # 请求/响应拦截器
│ ├── assets/ # 图片、svg、字体等
│ ├── components/ # 全局公共组件
│ │ ├── common/ # 通用组件（Button、Table、Modal 等）
│ │ └── business/ # 业务组件（可复用）
│ ├── composables/ # 组合式 API 逻辑复用（useRequest、useAuth 等）
│ ├── layouts/ # 页面布局（MainLayout、AuthLayout）
│ ├── router/ # 路由配置
│ │ ├── index.js
│ │ ├── modules/ # 按模块拆分路由
│ │ └── guards.js # 路由守卫（登录、权限）
│ ├── stores/ # Pinia 状态管理（核心！）
│ │ ├── modules/ # 按模块拆分
│ │ │ ├── user.js
│ │ │ ├── project.js
│ │ │ └── ...
│ │ └── index.js # 统一导出所有 store
│ ├── utils/ # 工具函数（日期、验证、token 等）
│ ├── views/ # 页面视图（路由对应页面）
│ │ ├── dashboard/
│ │ ├── user/
│ │ ├── project/
│ │ └── ...
│ ├── App.vue
│ ├── main.js # 项目入口
├── vite.config.js
├── package.json
└── README.md

## 核心业务

失物招领系统的核心业务包括：

- 用户注册、登录、个人资料管理
- 物品发布、搜索、详情查看
- 认领物品、审核物品发布
- 管理员用户管理、物品管理、认领管理

## 模块分层架构

### 整体架构图

![img](https://file+.vscode-resource.vscode-cdn.net/Users/m9570/.vscode/extensions/marscode.marscode-extension-1.2.54/resource/images/languageIcon/plaintext.svg)

plainText







```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端 (Vue.js)                                    │
│  发起登录请求 → { account, password }                                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         AuthController (控制器层)                            │
│  @RestController                                                             │
│  @RequestMapping("/api/auth")                                                │
│  - login()     → 接收请求，调用 AuthService.login()                         │
│  - register()  → 处理注册                                                    │
│  - forgotPassword() → 发送验证码                                            │
│  - resetPassword() → 重置密码                                               │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         AuthService (服务接口层)                              │
│  - register(RegisterReq)     → 定义注册业务逻辑                              │
│  - login(LoginReq)           → 定义登录业务逻辑                              │
│  - logout(token)             → 定义登出业务逻辑                              │
│  - refreshToken()            → 定义刷新令牌逻辑                              │
│  - forgotPassword()          → 定义忘记密码逻辑                              │
│  - resetPassword()           → 定义重置密码逻辑                              │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      AuthServiceImpl (服务实现层)                             │
│                                                                             │
│  依赖注入:                                                                   │
│  - UserMapper (数据库操作)                                                  │
│  - JwtUtils (JWT令牌生成)                                                  │
│  - EmailUtils (邮件发送)                                                   │
│                                                                             │
│  核心逻辑:                                                                   │
│  1. register(): 检查学号 → BCrypt加密密码 → 插入用户                        │
│  2. login():    查询用户 → 校验密码 → 生成双Token返回                       │
│  3. logout():   记录日志                                                   │
│  4. refreshToken(): 解析Token → 重新签发                                   │
│  5. forgotPassword(): 验证账号 → 生成验证码 → 发送邮件                      │
│  6. resetPassword(): 验证验证码 → 更新密码                                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                    ┌───────────────┼───────────────┐
                    ▼               ▼               ▼
┌─────────────────────────┐ ┌─────────────────┐ ┌─────────────────────────┐
│     UserMapper          │ │    JwtUtils      │ │     EmailUtils          │
│  (数据访问层)           │ │  (JWT工具类)      │ │    (邮件工具类)          │
│                         │ │                  │ │                         │
│ 继承 BaseMapper<User>  │ │ generateAccess   │ │ sendVerifyCode()       │
│ - selectOne()          │ │ generateRefresh  │ │   → 发送验证码邮件      │
│ - insert()             │ │ parseToken       │ │                         │
│ - updateById()         │ │ getUserIdFrom    │ │                         │
│ - exists()             │ │   Token          │ │                         │
└─────────────────────────┘ └─────────────────┘ └─────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         MySQL 数据库                                          │
│  表: user                                                                   │
│  - id (主键)                                                                │
│  - student_no (学号)                                                        │
│  - name (姓名)                                                              │
│  - password_hash (加密后的密码)                                            │
│  - email / phone / avatar_url / status                                      │
└─────────────────────────────────────────────────────────────────────────────┘
```

------

### 各层详细说明

### 示例 ： 登录模块

#### 1. Controller 层 (AuthController)

**文件位置**: `auth/AuthController.java`

- 负责接收 HTTP 请求
- 使用 `@Valid` 注解进行参数校验
- 调用 Service 层处理业务逻辑
- 返回统一的 `ApiResponse` 响应格式

| 方法 | 路径 | 功能 | |------|------|------| | POST | `/api/auth/register` | 用户注册 | | POST | `/api/auth/login` | 用户登录 | | POST | `/api/auth/forgot-password` | 忘记密码(发送验证码) | | POST | `/api/auth/reset-password` | 重置密码 |

------

#### 2. Service 接口层 (AuthService)

**文件位置**: `auth/service/AuthService.java`

定义了认证模块的业务接口，封装业务流程：

```
public interface AuthService {
    void register(AuthDTO.RegisterReq req);        // 注册
    AuthDTO.LoginResp login(AuthDTO.LoginReq req); // 登录
    void logout(String token);                     // 登出
    AuthDTO.LoginResp refreshToken(String refreshToken); // 刷新令牌
    void forgotPassword(AuthDTO.ForgotPasswordReq req); // 忘记密码
    void resetPassword(AuthDTO.ResetPasswordReq req);   // 重置密码
}
```

------

#### 3. Service 实现层 (AuthServiceImpl)

**文件位置**: `auth/service/impl/AuthServiceImpl.java`

实现具体的业务逻辑：

| 业务方法 | 核心逻辑 | |---------|---------| | **register()** | ① 检查学号是否已存在 ② 使用 BCrypt 加密密码 ③ 插入用户到数据库 | | **login()** | ① 根据账号查询用户(学号/邮箱/手机号) ② 校验密码 ③ 生成 Access Token 和 Refresh Token ④ 返回用户信息和 Token | | **logout()** | 记录登出日志(实际需要 Redis 黑名单) | | **refreshToken()** | ① 解析 Refresh Token ② 验证用户状态 ③ 重新签发双 Token | | **forgotPassword()** | ① 验证账号是否存在 ② 生成6位验证码 ③ 发送邮件 | | **resetPassword()** | ① 验证验证码 ② 查询用户 ③ 使用 BCrypt 加密新密码 ④ 更新数据库 |

------

#### 4. DTO 层 (AuthDTO)

**文件位置**: `auth/dto/AuthDTO.java`

数据传输对象，用于请求和响应：

| 类名 | 用途 | |------|------| | `RegisterReq` | 注册请求: studentNo, name, password, phone, email | | `LoginReq` | 登录请求: account, password | | `LoginResp` | 登录响应: accessToken, refreshToken, userId, name, avatarUrl | | `ForgotPasswordReq` | 忘记密码请求: account | | `ResetPasswordReq` | 重置密码请求: account, verifyCode, newPassword |

------

#### 5. Mapper 层 (UserMapper)

**文件位置**: `mapper/UserMapper.java`

继承 MyBatis-Plus 的 `BaseMapper`，提供数据库操作：

- `selectOne()` - 查询单条记录
- `insert()` - 插入用户
- `updateById()` - 更新用户
- `exists()` - 检查记录是否存在

------

#### 6. 工具类

| 工具类 | 位置 | 功能 | |--------|------|------| | **JwtUtils** | `common/utils/JwtUtils.java` | 生成和解析 JWT Token | | **JwtTokenProvider** | `security/JwtTokenProvider.java` | Spring Security 使用的 Token 提供者 | | **EmailUtils** | `common/utils/EmailUtils.java` | 发送验证码邮件 | | **JwtAuthenticationFilter** | `security/JwtAuthenticationFilter.java` | JWT 认证过滤器，验证请求中的 Token |



## 流程

提交审核流程
提交草稿 (Pre-check):
接口: POST /api/claims/drafts
逻辑: 校验 keyword_proof。如果通过，生成 draft_token 并存入 claim_drafts。

转为正式申请 (Formal Apply):
接口: POST /api/claims
逻辑: 携带 draft_id，提交 formal_proof。此时 claims 表状态设为 REVIEW_PENDING。

管理员审核 (Final Audit):
接口: PUT /api/admin/claims/{id}/audit
逻辑: 插入 claim_audits 记录，并将 claims.status 设为 APPROVED，同时将 items.status 设为 CLAIMED。

意义： 1.初筛机制：如果任何人都能直接发起正式申请，管理员将被淹没在海量的无效申请中。 2.保护物主与拾取者的隐私：在提交正式申请前，需要校验 keyword_proof 是否真实，防止恶意申请。 3.实现“认领权”的限时锁定：防止并发冲突，当一个用户提交了正确的关键词并生成草稿后，该物品在短时间内可以被锁定或标记为“有人正在认领”，防止同一时间多个用户发起正式流程导致数据混乱。 4.规范化业务流转
两阶段验证：
草稿阶段：系统级/初步逻辑校验（通过 keyword_proof）。
正式阶段：管理员级/人工最终审定（通过 formal_proof）。
状态闭环：通过 status (ACTIVE/USED/EXPIRED) 管理草稿生命周期，确保一个 draft_token 只能对应一个正式的 claim 记录。
