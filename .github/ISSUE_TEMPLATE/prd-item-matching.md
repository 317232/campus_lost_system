# PRD: 校园失物招领系统 - 物品智能匹配推荐

## Problem Statement

当前系统支持失物和招领分别发布，但用户需要手动查找匹配项。例如，用户丢失"黑色双肩包"需要自己去招领列表搜索"书包"来碰运气。系统缺少自动化的物品匹配推荐能力，用户可能错过快速找回失物的机会。

## Solution

当用户查看某条失物（招领）详情时，系统自动推荐可能匹配的招领（失物）信息，按匹配度排序展示，帮助失主和拾得者快速建立联系。

## User Stories

1. 作为丢失物品的用户，我希望在查看我的失物详情时看到可能匹配的招领信息，以便快速找回失物
2. 作为拾得物品的用户，我希望在查看我的招领详情时看到可能匹配的失物信息，以便确认物品归属
3. 作为系统管理员，我希望推荐算法基于物品名称、分类、地点、时间等多维度计算匹配度，提高推荐准确性
4. 作为普通访客，我希望推荐结果按匹配度排序，前3条最相关的推荐优先展示
5. 作为前端用户，我希望匹配推荐模块在无匹配结果时自动隐藏，不显示空内容
6. 作为前端用户，我希望点击推荐物品能直接跳转到对应详情页
7. 作为系统管理员，我希望推荐接口响应时间在500ms以内，不影响页面加载体验
8. 作为丢失物品的用户，我希望推荐列表包含物品名称、地点、时间、缩略图等关键信息，方便快速判断
9. 作为系统，我要求匹配只在PUBLISHED状态的物品之间进行，不显示待审核或已关闭的物品
10. 作为系统，我要求匹配只在相反场景之间进行（lost物品只推荐found物品，found物品只推荐lost物品）

## Implementation Decisions

### 后端模块

- `ItemController.java` — 已有 `/api/items/{id}/matches` 接口
- `ItemService.java` — 需实现 `getMatches(Long itemId, Integer limit)` 方法
- `ItemServiceImpl.java` — 实现匹配算法
- `MatchItem.java` / `MatchResp.java` — 已有DTO

### 前端模块

- `MatchRecommendations.vue` — 已有组件
- `LostDetailView.vue` / `FoundDetailView.vue` — 需集成

### 接口规格

**GET /api/items/{id}/matches**

匹配算法权重：分类相同+30，地点相同+25，名称包含+25，时间7天内+20

## Testing Decisions

- 后端：ItemServiceImpl.getMatches() 匹配算法测试
- 前端：MatchRecommendations.vue 渲染逻辑测试

## Out of Scope

- WebSocket实时推送、用户订阅、AI语义匹配、图片识别匹配
