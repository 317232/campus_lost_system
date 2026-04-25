# Project overview

- 这是一个基于 Spring Boot + Vue 3 的前后端分离的校园失物招领系统
- 项目结构：
  - backend/: Spring Boot 后端服务
    - 框架: Spring Boot 3.3.5, MyBatis-Plus 3.5.7
    - 数据库: MySQL
    - 安全: Spring Security + JWT
    - 邮件服务: Spring Boot Mail
  - frontend/: Vue 3 前端应用
    - 技术栈: Vite + JavaScript + Pinia + Vue Router + Axios
  - docx/: 项目文档
    - 包含软件可行性报告、需求分析报告等
- 核心业务模块：用户管理、失物管理、招领管理、认领管理、公告管理、举报管理、管理员功能

# Hard rules

- 首先读取README.md文件了解详细内容，次读取docx目录下的文件
- 不允许修改 public API contract，除非任务文件明确允许
- 不允许改数据库 schema，除非任务文件明确允许
- 不允许做测试，除非执行任务明确要求测试

# Configuration

- 后端端口: 8080
- 数据库连接: MySQL (localhost:3306)
- 邮件服务: QQ 邮箱 SMTP (smtp.qq.com:465)
- JWT 配置: 使用 HS256 算法，有效期 2 小时

# Commands

- 后端启动: cd backend && mvn spring-boot:run
- 前端启动: cd frontend && npm install && npm run dev
- 后端打包: cd backend && mvn clean package
- 前端构建: cd frontend && npm run build

# Commands

- 后端启动: cd backend && mvn spring-boot:run
- 前端启动: cd frontend && npm install && npm run dev
- 后端打包: cd backend && mvn clean package
- 前端构建: cd frontend && npm run build

# Coding conventions

- 后端采用 Spring Boot + MyBatis-Plus 架构风格
- 前端采用 Vue 3 + Composition API 风格 
- 错误处理统一使用 ApiResponse 格式
- 日志使用 SLF4J + Logback 进行记录
- 代码注释遵循 JavaDoc 和 JSDoc 规范


# Delivery

- 输出：代码 + 测试 + 变更说明
- 如果发现任务描述与代码现状冲突，先在结果里说明冲突点再实施
