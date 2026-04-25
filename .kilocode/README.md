# Kilo Code Agent 配置说明

本目录包含 Kilo Code AI 编程助手的 Agent 配置文件。

## Agent 架构

采用 **PM 主代理 + 专业子代理** 架构：

```
┌─────────────────────────────────────────────────────┐
│              project-manager (主代理)                 │
│         mode: primary | 可直接选择                    │
│         负责：任务拆解、委派、协调、汇总                 │
└─────────────────┬───────────────────────────────────┘
                  │ 委派
        ┌─────────┼─────────┐
        ▼         ▼         ▼
┌──────────┐ ┌──────────┐ ┌──────────┐
│tech-lead │ │ backend- │ │frontend- │
│ 子代理   │ │engineer  │ │engineer  │
│mode:sub  │ │ 子代理   │ │ 子代理   │
└──────────┘ └──────────┘ └──────────┘
```

## Agent 角色

| Agent | 模式 | 职责 | 触发方式 |
|-------|------|------|----------|
| `project-manager` | primary | 项目经理主代理 | 直接选择 |
| `tech-lead` | subagent | 总工程师 | `@tech-lead` |
| `backend-engineer` | subagent | 后端工程师 | `@backend-engineer` |
| `frontend-engineer` | subagent | 前端工程师 | `@frontend-engineer` |

## 文件说明

```
.kilocode/
├── agents/
│   ├── project-manager.md     # 项目经理主代理
│   ├── tech-lead.md           # 总工程师子代理
│   ├── backend-engineer.md    # 后端工程师子代理
│   └── frontend-engineer.md   # 前端工程师子代理
├── mcp.json                   # MCP 服务器配置
└── README.md                  # 本说明文件
```

## 使用方法

### 1. 选择主代理

打开 Kilo VSCode 插件，在 Agent 选择器中选择 **project-manager**。

### 2. 输入需求

正常输入需求描述，例如：
```
帮我设计一个图书管理系统后台，从需求到任务拆解和接口草案。
```

### 3. PM 自动委派

project-manager 会根据需求自动分析并委派子代理：
- 涉及架构、技术路线 → `@tech-lead`
- 涉及接口、数据库、业务逻辑 → `@backend-engineer`
- 涉及页面、组件、交互 → `@frontend-engineer`

### 4. 手动调用子代理

也可以直接手动调用子代理：

```
@tech-lead 评审这个架构是否合理

@backend-engineer 设计登录模块接口草案

@frontend-engineer 设计后台管理页面结构
```

## 子代理职责

### tech-lead (总工程师)
- 系统架构设计
- 技术选型决策
- 模块边界定义
- 性能与安全风险控制
- 研发规范制定

### backend-engineer (后端工程师)
- 后端服务开发
- API 接口设计
- 数据库设计
- 业务逻辑实现
- 异常处理与权限控制

### frontend-engineer (前端工程师)
- 页面开发
- 组件设计
- 交互逻辑实现
- 状态管理
- 前端联调

## 权限控制

| Agent | task | edit | bash | read | grep | glob | list |
|-------|------|------|------|------|------|------|------|
| project-manager | 仅子代理 | ask | ask | allow | allow | allow | allow |
| tech-lead | deny | ask | ask | allow | allow | allow | allow |
| backend-engineer | deny | ask | ask | allow | allow | allow | allow |
| frontend-engineer | deny | ask | ask | allow | allow | allow | allow |

## 工作流程

```
用户需求 → PM 分析 → 委派子代理 → 子代理分析 → PM 汇总 → 输出计划
```

### PM 输出格式

【PM】
需求理解：
已确认信息：
合理假设：
待确认问题：
任务拆解：
风险清单：
验收标准：

### 子代理输出格式

【总工程师】
技术判断：
推荐方案：
系统边界：
关键风险：

【后端工程师】
后端任务：
接口草案：
数据结构草案：
业务逻辑说明：

【前端工程师】
前端任务：
页面结构：
组件拆分：
状态管理：

## 注意事项

1. 子代理不能继续委派（task: deny）
2. 所有重大决策由 PM 汇总
3. 建议的接口/数据结构必须标记为"草案"
4. 不要编造未确认的技术栈或接口
5. 上下文混乱时先做状态同步
