---
title: 城市报修与工单处理系统——团队分工与 Agent Skills 开发规范
version: 2.0
updated: 2026-06-25
owner: 侯舒扬
team_size: 6
status: frozen-for-development
---

# 开发前先看

本规范仍是总规则，但开发落地以以下文件为直接执行入口：

1. `docs/00-开发启动包索引.md`：组长和组员的启动顺序；
2. `sql/init_city_repair.sql`：数据库唯一初始化脚本；
3. `docs/数据库与接口定稿.md`：表、状态、接口、异常和模块边界；
4. `docs/GitHub协作与提交规范.md`：拉代码、开分支、提交、PR 和合并；
5. `.agents/skills/city-repair-project-rules/SKILL.md`：项目级业务 Skill；
6. `.agents/skills/city-repair-ui-style/SKILL.md`：项目级 UI Skill；
7. `templates/`：AI 日志、测试记录和 PR 自检模板。

组员开始写代码前，必须先完成 GitHub 分支、数据库导入、Skill 读取和个人任务确认。

# 城市报修与工单处理系统

## 六人团队分工、Agent Skills、统一视觉与交付规范

> 本文件同时面向团队成员和编程 AI。所有成员在开始开发前，必须把本文件交给自己使用的 AI 阅读，并要求 AI 明确回复“已理解项目范围、本人模块、禁止事项和验收标准”。
>
> **规则优先级：本文件与项目级 Skills > 组长最新通知 > 通用 GitHub Skill > AI 自行判断。** 发生冲突时，必须停止编码并向组长确认，不得自行改规则。

---

## 0. 执行摘要

### 0.1 最终题目

**城市报修与工单处理系统**

项目只实现一个清楚、可运行、可在 5 分钟内演示完的业务闭环，不扩展为综合应急指挥平台。

### 0.2 核心业务闭环

```text
居民提交报修
  → 管理员审核
  → 管理员分派维修人员
  → 维修人员接单并处理
  → 维修人员提交完成结果
  → 居民评价
  → 管理员查看统计看板
```

### 0.3 开发优先级

1. **P0：系统可启动、可登录、数据库可用。**
2. **P1：居民报修—审核—分派—处理—完成—评价闭环。**
3. **P2：筛选、分页、图片上传、状态日志、统计看板。**
4. **P3：AI 分类、摘要或建议等加分功能。**

> P0、P1 未全部通过前，禁止开发 P3。

### 0.4 六人分工总览

| 成员 | 主责模块 | Git 分支 | 主要交付 |
|---|---|---|---|
| 侯舒扬 | 组长、基础架构、权限、数据库、Skills、合并与发布 | `develop` / `main` | 可运行主分支、初始化 SQL、统一规范、最终 README 与演示 |
| 苟佳豪 | 居民报修 | `feat/resident-repair` | 报修页面、接口、图片、本人查询、取消 |
| 闫龙飞 | 管理员审核与分派 | `feat/admin-dispatch` | 审核、驳回、优先级、分派、状态日志 |
| 袁翊博 | 维修人员处理 | `feat/worker-processing` | 接单、开始处理、进度、完成、处理图片 |
| 孔祥洋 | 统计看板与类别管理 | `feat/dashboard-category` | 真实统计 API、ECharts、类别 CRUD |
| 张天童 | 评价、通知、测试与附录 | `feat/evaluation-quality` | 评价与通知代码、测试表、Bug 表、AI 日志和文档初稿 |

---

# 1. 全局业务规则

## 1.1 用户角色

### 居民 `RESIDENT`

允许：

- 提交报修；
- 查询本人报修；
- 查看本人报修详情、图片和处理时间线；
- 取消仍处于“待审核”的本人报修；
- 对本人“已完成”的工单进行一次评价。

禁止：

- 查看他人工单；
- 修改审核结果、优先级、分派人员或处理结果；
- 评价未完成工单；
- 对同一工单重复评价。

### 管理员 `ADMIN`

允许：

- 查询全部工单；
- 审核通过或驳回；
- 设置工单优先级；
- 分派维修人员；
- 管理报修类别；
- 查看状态日志、评价和统计看板。

禁止：

- 代替居民评价；
- 直接把“待审核”工单改为“已完成”；
- 绕过状态流转规则。

### 维修人员 `WORKER`

允许：

- 查询分派给自己的工单；
- 接单；
- 开始处理；
- 提交处理进度；
- 上传处理后图片；
- 填写处理说明并完成工单。

禁止：

- 查看或处理未分派给自己的工单；
- 审核或分派工单；
- 修改居民原始报修信息。

## 1.2 工单状态枚举

所有数据库、后端、前端、接口文档和测试必须使用下列唯一状态。禁止创建同义状态。

| 枚举值 | 中文显示 | 合法来源 | 合法去向 |
|---|---|---|---|
| `PENDING_REVIEW` | 待审核 | 居民新建 | `PENDING_ASSIGN`、`REJECTED`、`CANCELLED` |
| `PENDING_ASSIGN` | 待分派 | 管理员审核通过 | `PENDING_ACCEPT` |
| `PENDING_ACCEPT` | 待接单 | 管理员完成分派 | `PROCESSING` |
| `PROCESSING` | 处理中 | 维修人员接单或开始处理 | `COMPLETED` |
| `COMPLETED` | 已完成 | 维修人员提交完成结果 | `EVALUATED` |
| `EVALUATED` | 已评价 | 居民提交评价 | 终态 |
| `REJECTED` | 已驳回 | 管理员驳回 | 终态 |
| `CANCELLED` | 已取消 | 居民取消待审核工单 | 终态 |

禁止新增：`已受理`、`维修中`、`关闭`、`待处理`、`已解决` 等同义状态。

## 1.3 优先级枚举

| 枚举值 | 中文显示 |
|---|---|
| `LOW` | 低 |
| `NORMAL` | 普通 |
| `HIGH` | 高 |
| `URGENT` | 紧急 |

管理员审核时可设置优先级；居民不得直接决定最终优先级。

## 1.4 核心数据表

若现有框架已有用户、角色、菜单、权限表，必须复用，不得新建第二套登录体系。

| 表名 | 用途 | 关键关系 |
|---|---|---|
| `sys_user` / `sys_role` | 用户与角色 | 复用现有权限体系 |
| `repair_category` | 报修类别 | 被 `repair_order.category_id` 引用 |
| `repair_order` | 工单主表 | 关联居民、类别、当前处理人和当前状态 |
| `order_assignment` | 分派记录 | 关联工单、管理员和维修人员 |
| `order_status_log` | 状态流转和操作日志 | 关联工单、操作者、前后状态 |
| `order_attachment` | 报修前、处理后图片 | 关联工单和附件类型 |
| `order_evaluation` | 居民评价 | 与工单一对一 |
| `user_notification` | 用户通知（可选） | 关联接收用户、工单和通知类型 |

## 1.5 数据一致性要求

以下规则必须由后端校验，不能只靠前端隐藏按钮：

- 居民只能读取和操作自己的工单；
- 维修人员只能读取和操作分派给自己的工单；
- 状态流转必须校验当前状态；
- 同一工单只能存在一条有效当前分派；
- 同一工单只能评价一次；
- 状态变化必须在同一事务中完成：更新工单主表 + 写入状态日志；
- 已被工单引用的报修类别不能直接物理删除，优先使用“停用”；
- 上传文件必须校验类型、大小和归属；
- 任何接口不得信任前端传入的当前用户 ID，应从登录上下文获取。

---

# 2. 统一工程规范

## 2.1 技术栈

优先沿用组长已经创建的现有项目。成员不得自行换框架、重建项目或增加第二套请求封装。

如项目尚未初始化，默认技术栈为：

```text
后端：Spring Boot 3 + MyBatis-Plus
前端：Vue 3 + Vite + Element Plus
数据库：MySQL 8
图表：ECharts
接口测试：Apifox 或 Postman
版本管理：Git
```

## 2.2 API 规范

### 路径

- 资源名使用复数名词；
- 路径统一使用小写与连字符；
- 不在路径中混用多种命名风格；
- 状态动作可使用明确的子路径，如 `/approve`、`/assign`、`/complete`。

### 返回结构

所有接口复用项目已有统一返回结构。若项目没有，则统一为：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": "2026-06-25T12:00:00"
}
```

错误返回必须包含稳定错误码和可读信息，不得把数据库异常堆栈直接返回前端。

### 分页参数

```text
page: 从 1 开始
pageSize: 默认 10，最大 100
keyword: 可选搜索词
status: 可选状态
```

### 时间格式

- 后端和数据库明确时区；
- 前端统一显示为 `YYYY-MM-DD HH:mm`；
- 禁止同一系统同时出现多种时间格式。

## 2.3 命名规范

| 类型 | 规范 | 示例 |
|---|---|---|
| 数据库表/字段 | `snake_case` | `repair_order`, `created_at` |
| Java 类 | `PascalCase` | `RepairOrderService` |
| Java/TypeScript 变量 | `camelCase` | `repairOrderId` |
| 常量/枚举 | `UPPER_SNAKE_CASE` | `PENDING_REVIEW` |
| Vue 组件文件 | 项目现有规范优先 | `RepairOrderList.vue` |
| API 文件 | 按模块组织 | `repairOrderApi.ts` |

同一概念只能有一个名称。例如统一使用“工单 `order`”，不得在不同模块混用 `ticket`、`case`、`task`。

## 2.4 Git 规范

### 分支

- `main`：最终稳定版本，只由组长维护；
- `develop`：集成分支，只由组长或组长授权合并；
- 成员功能分支：见各自分工。

### 操作规则

1. 开始开发前先拉取最新 `develop`；
2. 禁止直接向 `main` 提交；
3. 禁止修改其他成员主责文件；
4. 修改公共代码前必须先在群里说明；
5. 不提交 `node_modules`、`target`、`dist`、日志、IDE 临时文件和真实密钥；
6. 每次提交只解决一个明确问题；
7. 合并前必须运行项目并提交验收结果。

### 提交信息示例

```text
feat: 完成居民报修创建接口
feat: 完成管理员审核与分派联调
fix: 修复维修人员越权访问
fix: 修复非法状态跳转
test: 补充评价重复提交测试
docs: 补充模块接口与AI协作记录
```

每名成员至少保留 3 次有效提交，禁止最后一次把全部代码一次性提交。

## 2.5 AI 生成代码人工检查清单

每次接受 AI 代码前，开发者必须检查：

- 是否重复创建项目或用户体系；
- 是否修改了其他成员模块；
- 是否写死用户 ID、统计值、状态或测试数据；
- 是否只在前端做权限控制；
- 是否存在空值、越权、重复提交、并发、事务和异常处理问题；
- SQL 条件是否会查询到他人数据；
- 状态流转是否合法；
- 页面是否真实调用后端 API；
- 构建、启动和测试是否真实执行，而不是只声称“应该可用”。

---

# 3. Agent Skills 统一方案

## 3.1 为什么必须统一 Skills

团队成员可能使用 Claude Code、Codex 或其他编程代理。为了避免不同 AI 生成完全不同的目录、页面风格和业务规则，统一采用：

```text
通用 GitHub Skills
  + 项目级业务 Skill
  + 项目级 UI Skill
  + 本文件中的个人任务提示词
```

## 3.2 规则优先级

从高到低：

1. `city-repair-project-rules`：业务、状态、权限、接口、模块边界；
2. `city-repair-ui-style`：视觉、布局、组件和交互；
3. 本文件中对应成员的任务说明；
4. `frontend-design`、`webapp-testing` 等通用 Skill；
5. AI 默认习惯。

通用 Skill 与项目级 Skill 冲突时，必须以项目级 Skill 为准。

## 3.3 统一使用的 GitHub Skills

### 1. `skill-creator`

用途：由组长创建、检查和维护项目级 Skills。

链接：

- https://github.com/anthropics/skills/tree/main/skills/skill-creator

### 2. `frontend-design`

用途：所有前端页面的视觉设计、信息层级、细节和可用性。

链接：

- https://github.com/anthropics/skills/tree/main/skills/frontend-design

> `frontend-design` 只负责提高完成度。它不得覆盖本项目的颜色、布局、状态和组件规则。

### 3. `webapp-testing`

用途：使用 Playwright 等方式验证本地 Web 页面、交互、截图、控制台错误和核心流程。

链接：

- https://github.com/anthropics/skills/tree/main/skills/webapp-testing

### 4. `doc-coauthoring`

用途：整理 README、课程附录、测试记录、AI 协作日志和反思。

链接：

- https://github.com/anthropics/skills/tree/main/skills/doc-coauthoring

### 5. Agent Skills 开放规范

用途：检查 `SKILL.md` 的目录结构、YAML 头和命名是否合法。

链接：

- https://agentskills.io/specification
- https://github.com/agentskills/agentskills

### 6. Codex 官方 Skill 安装器说明

Codex 可通过内置 `$skill-installer` 从 GitHub 目录安装 Skill，安装后需要重启 Codex。

链接：

- https://github.com/openai/skills
- https://github.com/openai/skills/tree/main/skills/.system/skill-installer

## 3.4 项目中的 Skill 目录

项目仓库保存一份**唯一源版本**：

```text
<project-root>/
├─ .agents/
│  └─ skills/
│     ├─ vendor/
│     │  ├─ frontend-design/
│     │  ├─ webapp-testing/
│     │  ├─ skill-creator/
│     │  └─ doc-coauthoring/
│     ├─ city-repair-project-rules/
│     │  └─ SKILL.md
│     └─ city-repair-ui-style/
│        └─ SKILL.md
├─ backend/
├─ frontend/
└─ README.md
```

说明：

- `.agents/skills/` 是团队共享源目录；
- 不同客户端的自动发现目录可能不同；
- 成员必须让自己的 AI 检查当前客户端的真实安装目录，再复制或映射；
- 不得凭记忆硬写安装路径；
- 安装后必须让 AI 报告：Skill 名称、来源 URL、实际路径和发现结果。

## 3.5 发给 AI 的统一“安装 Skills”提示词

每位成员先把下面内容原样发给自己正在使用的 Claude Code、Codex 或其他编程 AI：

```text
你现在只执行 Agent Skills 安装与验证，不修改任何业务代码。

第一步：识别你当前运行的客户端和版本，例如 Claude Code、Codex CLI、Codex Desktop 或其他客户端。
第二步：检查该客户端当前版本支持的项目级和用户级 Skill 发现目录，不要凭记忆猜路径。
第三步：从以下 GitHub 目录下载完整 Skill 文件夹，不能只复制 SKILL.md，必须保留该 Skill 自带的 scripts、references、assets 和 LICENSE 文件：

1. https://github.com/anthropics/skills/tree/main/skills/frontend-design
2. https://github.com/anthropics/skills/tree/main/skills/webapp-testing
3. https://github.com/anthropics/skills/tree/main/skills/skill-creator
4. https://github.com/anthropics/skills/tree/main/skills/doc-coauthoring

将一份未修改的源副本保存在当前项目：
.agents/skills/vendor/<skill-name>/

然后根据当前客户端的真实要求，将 Skill 安装、复制或映射到可发现目录：
- 如果是 Codex，优先使用官方 $skill-installer 从 GitHub URL 安装；
- 如果是 Claude Code，先检查当前版本支持的项目级 Skill 目录，再安装；
- 如果当前客户端已能发现 .agents/skills，则不要重复安装；
- 不要删除或覆盖已有同名 Skill，发现冲突时先报告。

完成后只输出：
1. 当前客户端与版本；
2. 每个 Skill 的 GitHub 来源；
3. 源副本路径；
4. 实际发现路径；
5. 是否需要重启；
6. 重启后成功发现的 Skill 名称；
7. 任何失败项和原因。

在我确认前，不要修改项目业务代码。
```

## 3.6 Codex 用户可直接使用的安装提示词

```text
使用 Codex 内置的 $skill-installer，分别从下列 GitHub 目录安装完整 Skill：

https://github.com/anthropics/skills/tree/main/skills/frontend-design
https://github.com/anthropics/skills/tree/main/skills/webapp-testing
https://github.com/anthropics/skills/tree/main/skills/skill-creator
https://github.com/anthropics/skills/tree/main/skills/doc-coauthoring

安装完成后告诉我实际安装目录，并提醒我完全重启 Codex。重启后列出成功发现的 Skill。不要修改任何业务代码。
```

## 3.7 组长创建两个项目级 Skills 的提示词

```text
请使用 skill-creator，并严格遵守 https://agentskills.io/specification，在项目根目录创建以下两个项目级 Agent Skills：

1. .agents/skills/city-repair-project-rules/SKILL.md
2. .agents/skills/city-repair-ui-style/SKILL.md

要求：
- name 必须和父目录同名；
- YAML frontmatter 至少包含 name 和 description；
- description 必须明确说明何时触发；
- 指令使用明确的 MUST、MUST NOT、SHOULD；
- 主 SKILL.md 不堆积无关背景；
- 业务规则来自本开发规范第 1、2、5、6 章；
- UI 规则来自本开发规范第 4 章；
- 不自行改变技术栈、角色、状态、表名和接口；
- 创建后检查格式、列出文件，并用至少 3 个开发任务测试触发是否正确。

暂时不要修改业务代码。先把两个 SKILL.md 的完整内容输出给我审查。
```

## 3.8 每次开始开发时的统一启动提示词

```text
开始本次任务前：

1. 完整读取项目根目录的团队开发规范；
2. 激活并遵守 city-repair-project-rules；
3. 涉及页面时激活 city-repair-ui-style 和 frontend-design；
4. 完成后使用 webapp-testing 验证核心流程；
5. 先检查现有仓库、数据库和接口，不要重建项目；
6. 不创建第二套用户体系、权限体系、请求封装或状态枚举；
7. 不修改其他成员主责模块。

编码前先输出：
- 你理解的任务边界；
- 预计修改和新增的文件；
- 预计新增或调用的接口；
- 可能影响的数据库表；
- 风险和需要确认的问题。

得到确认后再编码。

编码完成后必须真实运行构建和测试，并输出：
- 修改文件清单；
- 接口清单；
- 数据库变更；
- 已执行命令；
- 测试结果；
- 截图或日志路径；
- 剩余风险；
- 需要人工复核的代码。
```

---

# 4. 全局 UI 与交互规范

## 4.1 统一使用哪些 Skill

所有涉及页面的任务必须同时使用：

```text
frontend-design
+ city-repair-ui-style
```

职责划分：

- `frontend-design`：负责页面完成度、信息层级、可用性和细节；
- `city-repair-ui-style`：负责统一颜色、间距、布局、组件、状态和禁止项；
- 如两者冲突，以 `city-repair-ui-style` 为准。

## 4.2 产品气质

```text
定位：面向居民、管理员和维修人员的城市公共服务管理工具
关键词：实用、清晰、克制、可信、效率优先
风格：浅色政务/企业后台，信息密度适中，状态明确，反馈及时
```

禁止：

- 营销落地页式大标题；
- 紫色渐变、霓虹、赛博朋克；
- 玻璃拟态、大面积毛玻璃；
- 复杂 3D 图标和 3D 饼图；
- 无业务意义的动画；
- 每个页面使用不同色彩和圆角；
- AI 自由发挥成“科技大屏”。

## 4.3 设计令牌

| 类别 | 统一值 | 用途 |
|---|---|---|
| 主色 | `#1F5FBF` | 主按钮、选中菜单、链接、关键指标 |
| 深蓝 | `#174A7E` | 页面标题、重点文字 |
| 页面背景 | `#F5F7FA` | 主内容区背景 |
| 卡片背景 | `#FFFFFF` | 表格、表单、统计卡片 |
| 正文 | `#1F2937` | 正文和表格主要内容 |
| 次级文字 | `#5F6B7A` | 时间、说明和辅助信息 |
| 边框 | `#D9E1EA` | 卡片、分隔线和输入框边框 |
| 成功 | `#2E7D5B` | 已完成、已评价 |
| 警告 | `#C97A10` | 待审核、待分派 |
| 危险 | `#B83A3A` | 驳回、取消、删除、错误 |
| 字体 | 微软雅黑 / 苹方 / Noto Sans CJK SC | 不混用多套字体 |
| 卡片圆角 | `8px` | 不使用超大圆角 |
| 按钮/输入框圆角 | `6px` | 全系统统一 |
| 阴影 | `0 2px 8px rgba(31,41,55,.08)` | 仅重点卡片和浮层 |

## 4.4 布局规则

### 全局后台布局

```text
左侧固定侧栏
+ 顶部栏
+ 主内容区
```

- 左侧菜单宽度统一；
- 顶栏高度统一；
- 主内容区保持一致内边距；
- 页面内容最大宽度和左右留白保持一致；
- 不同成员不得各写一套侧栏和顶栏。

### 列表管理页

固定结构：

```text
面包屑/标题
→ 筛选栏
→ 操作区
→ 表格
→ 分页
```

必须有：加载、空数据、请求失败、删除确认、分页。

### 详情页

固定结构：

```text
返回按钮/标题
→ 当前状态和基础信息
→ 工单时间线
→ 现场图片和处理结果
→ 合法操作区
```

### 表单页

固定结构：

```text
标题
→ 分组表单
→ 图片上传
→ 取消/提交操作区
```

必须有：必填标记、即时校验、重复提交防护、提交加载状态、成功和失败提示。

### 统计看板

固定结构：

```text
标题和时间范围
→ 4 个指标卡
→ 趋势图
→ 状态/类别分布图
→ 维修人员排行表
```

所有数据必须来自后端 API。禁止前端硬编码统计值。

## 4.5 组件规则

- 全项目只使用现有 UI 组件库；
- 全项目只使用一套图标库；
- 每页最多一个实心主按钮；
- 次要操作使用普通按钮或文字按钮；
- 删除、驳回、取消等危险操作必须二次确认；
- 所有请求必须有 loading；
- 所有操作必须有明确成功/失败反馈；
- 表格默认每页 10 条；
- 长文本使用省略和 Tooltip，不允许挤坏表格；
- 图片支持预览；
- 权限不足时不只隐藏按钮，后端仍需拒绝；
- 动画仅允许 150–250ms 的菜单、按钮和弹层过渡。

## 4.6 状态显示映射

| 状态 | 文本 | 建议组件类型 |
|---|---|---|
| `PENDING_REVIEW` | 待审核 | `warning` |
| `PENDING_ASSIGN` | 待分派 | `warning` |
| `PENDING_ACCEPT` | 待接单 | `primary` |
| `PROCESSING` | 处理中 | `primary` |
| `COMPLETED` | 已完成 | `success` |
| `EVALUATED` | 已评价 | `success` |
| `REJECTED` | 已驳回 | `danger` |
| `CANCELLED` | 已取消 | `info` |

同一状态在所有页面必须使用完全相同的文本和颜色。

## 4.7 图表规则

- 使用 ECharts；
- 同一指标在不同图表保持同色；
- 颜色数量控制在 5–7 个以内；
- 禁止 3D 图表、随机渐变和炫光；
- 标题、图例、轴标签保持清晰；
- 无数据时显示“暂无数据”，不得显示报错或空白；
- 图表必须在窗口尺寸变化时正确 resize；
- 1280px 宽度下仍能正常操作。

## 4.8 页面验收清单

- [ ] 页面标题、按钮、标签和状态与本规范一致；
- [ ] 侧栏、顶栏、卡片、间距、按钮和表格与其他模块一致；
- [ ] 没有写死统计值、用户信息或状态；
- [ ] 无水平溢出、文字截断、按钮重叠和低对比度；
- [ ] 加载、空数据、失败、权限不足和二次确认可演示；
- [ ] 页面真实调用 API；
- [ ] 浏览器控制台无明显错误；
- [ ] 1440×900 和 1280 宽度均可用。

---

# 5. 六人详细分工与验收标准

## 5.1 侯舒扬：组长、基础架构与最终集成

### 分支

```text
develop
main
```

### 负责内容

- 创建和维护项目骨架；
- 数据库初始化与演示数据；
- 登录、角色、菜单和权限；
- 统一返回结构、异常处理、分页、文件上传等公共能力；
- 创建和维护项目级 Skills；
- 审核各成员数据库和接口变更；
- 合并分支和解决冲突；
- 核心流程回归测试；
- README、部署说明、演示账号、最终压缩包；
- 5 分钟演示脚本和最终提交。

### 禁止事项

- 不替其他成员重复写完整业务模块；
- 不在未通知成员时大范围重构其模块；
- 不在 6 月 28 日后新增大型功能。

### 验收标准

- [ ] 前后端可按 README 启动；
- [ ] 三种角色能登录且菜单正确；
- [ ] 数据库可从初始化 SQL 重建；
- [ ] 状态枚举、返回结构和异常提示统一；
- [ ] 所有成员分支已合并并通过核心回归；
- [ ] 最终代码、数据库、视频和附录齐全。

### 发给 AI 的个人任务提示词

```text
ROLE
你负责《城市报修与工单处理系统》的组长、基础架构与最终集成。

CONTEXT
先读取团队开发规范、city-repair-project-rules 和 city-repair-ui-style。涉及页面时使用 frontend-design，完成后使用 webapp-testing。

SCOPE
只负责项目骨架、数据库初始化、登录权限、公共能力、Skills、代码合并、回归测试、README 和最终发布。不要替其他成员重写居民报修、管理员审核、维修处理、统计或评价模块。

REQUIRED WORK
1. 检查现有仓库和技术栈，不重建项目；
2. 明确前后端启动方式和数据库配置；
3. 复用现有用户与权限体系；
4. 统一接口返回、异常处理、分页、文件上传和状态枚举；
5. 维护初始化 SQL 和演示账号；
6. 合并前运行构建与核心流程测试；
7. 记录冲突、风险和修复结果。

MUST NOT
- 不创建第二套用户体系；
- 不自行更换技术栈；
- 不覆盖其他成员业务模块；
- 不声称测试成功而不执行命令。

OUTPUT CONTRACT
编码前输出计划、修改文件、数据库影响和风险；完成后输出修改文件、执行命令、测试结果、剩余风险和人工复核点。
```

## 5.2 苟佳豪：居民报修模块

### 分支

```text
feat/resident-repair
```

### 页面

- 我要报修；
- 我的报修；
- 报修详情。

### 主要接口

```http
POST /api/repair-orders
GET  /api/repair-orders/my
GET  /api/repair-orders/{id}
PUT  /api/repair-orders/{id}/cancel
```

可按现有项目规范补充类别查询和图片上传接口，不得另建一套上传体系。

### 主要数据表

```text
repair_order
repair_category
order_attachment
order_status_log
```

### 功能要求

- 填写标题、类别、位置、详细描述、联系电话；
- 上传现场图片；
- 提交后初始状态为 `PENDING_REVIEW`；
- 列表支持关键词、状态、时间筛选和分页；
- 居民只能看到自己的工单；
- 仅本人且状态为 `PENDING_REVIEW` 时可取消；
- 详情展示状态、图片、处理结果和完整时间线。

### 验收标准

- [ ] 能创建真实数据库记录；
- [ ] 缺少必填字段时前后端均拒绝；
- [ ] 居民无法查看他人工单；
- [ ] 非待审核工单无法取消；
- [ ] 页面、接口和数据库真实联调；
- [ ] 至少有成功、参数错误和越权测试截图。

### 发给 AI 的个人任务提示词

```text
ROLE
你负责《城市报修与工单处理系统》的居民报修模块。

BRANCH
feat/resident-repair

CONTEXT
读取团队开发规范、city-repair-project-rules 和 city-repair-ui-style。页面使用 frontend-design；完成后使用 webapp-testing。

SCOPE
只实现“我要报修、我的报修、报修详情”及对应后端接口。复用现有用户、权限、请求封装、文件上传和 UI 布局。

REQUIRED WORK
1. 检查 repair_order、repair_category、order_attachment、order_status_log 的现有结构；
2. 实现创建工单，初始状态固定为 PENDING_REVIEW；
3. 从登录上下文获取居民 ID，不能信任前端传入用户 ID；
4. 实现本人列表、筛选、分页和详情；
5. 仅本人待审核工单可取消，并写入状态日志；
6. 接入真实 API 和图片上传；
7. 测试成功、参数错误、他人访问和非法取消。

MUST NOT
- 不创建第二套用户体系；
- 不修改管理员、维修、统计和评价模块；
- 不新增状态；
- 不写死用户、类别或统计数据；
- 不只靠前端限制权限。

OUTPUT CONTRACT
完成后列出修改文件、接口、数据库影响、测试命令、测试结果、截图路径和剩余风险。
```

## 5.3 闫龙飞：管理员审核与分派模块

### 分支

```text
feat/admin-dispatch
```

### 页面

- 待审核工单；
- 工单审核；
- 工单分派；
- 状态日志。

### 主要接口

```http
GET /api/admin/orders
PUT /api/admin/orders/{id}/approve
PUT /api/admin/orders/{id}/reject
PUT /api/admin/orders/{id}/assign
GET /api/repair-orders/{id}/logs
```

### 主要数据表

```text
repair_order
order_assignment
order_status_log
sys_user / sys_role
```

### 功能要求

- 管理员按状态、类别、优先级、关键词和时间查询工单；
- 仅 `PENDING_REVIEW` 可审核；
- 审核通过后进入 `PENDING_ASSIGN`；
- 驳回必须填写原因并进入 `REJECTED`；
- 仅 `PENDING_ASSIGN` 可分派；
- 只能分派给有效的维修角色用户；
- 分派后创建分派记录并进入 `PENDING_ACCEPT`；
- 每次状态变化写入日志。

### 验收标准

- [ ] 非管理员无法调用管理接口；
- [ ] 驳回无原因时拒绝；
- [ ] 非维修角色不可被分派；
- [ ] 非法状态跳转被拒绝；
- [ ] 工单更新和日志写入在同一事务；
- [ ] 前端列表、审核、分派和日志真实联调。

### 发给 AI 的个人任务提示词

```text
ROLE
你负责《城市报修与工单处理系统》的管理员审核与分派模块。

BRANCH
feat/admin-dispatch

CONTEXT
读取团队开发规范、city-repair-project-rules 和 city-repair-ui-style。页面使用 frontend-design；完成后使用 webapp-testing。

SCOPE
只实现管理员工单查询、审核通过、驳回、设置优先级、分派维修人员和状态日志。

REQUIRED WORK
1. 复用现有管理员权限和维修角色；
2. 只允许 PENDING_REVIEW 审核；
3. 审核通过后进入 PENDING_ASSIGN；
4. 驳回必须有原因并进入 REJECTED；
5. 只允许 PENDING_ASSIGN 分派；
6. 校验被分派用户存在且具有 WORKER 角色；
7. 分派后写 order_assignment、更新 repair_order、写 order_status_log，并处于同一事务；
8. 测试参数错误、权限不足、重复审核、非法状态和无效维修人员。

MUST NOT
- 不跳过状态；
- 不直接完成工单；
- 不修改居民提交内容；
- 不只在前端做权限校验；
- 不修改其他成员模块。

OUTPUT CONTRACT
输出修改文件、接口、事务边界、测试命令、测试结果、截图和剩余风险。
```

## 5.4 袁翊博：维修人员工单处理模块

### 分支

```text
feat/worker-processing
```

### 页面

- 待接工单；
- 我的工单；
- 工单处理详情。

### 主要接口

```http
GET  /api/worker/orders
PUT  /api/worker/orders/{id}/accept
PUT  /api/worker/orders/{id}/start
POST /api/worker/orders/{id}/progress
PUT  /api/worker/orders/{id}/complete
```

### 主要数据表

```text
repair_order
order_assignment
order_status_log
order_attachment
```

### 功能要求

- 维修人员只查询分派给自己的工单；
- `PENDING_ACCEPT` 可接单；
- 接单或开始处理后进入 `PROCESSING`；
- 处理中可提交进度说明；
- 完成时必须填写处理说明，可上传处理后图片；
- 完成后进入 `COMPLETED`；
- 所有处理动作写入日志；
- 居民详情页可查看处理结果和时间线。

### 验收标准

- [ ] 无法查看或操作他人被分派工单；
- [ ] 非法状态无法接单、开始或完成；
- [ ] 完成时缺少处理说明会被拒绝；
- [ ] 上传的处理图片与工单正确关联；
- [ ] 状态和日志事务一致；
- [ ] 待接单到已完成完整路径可演示。

### 发给 AI 的个人任务提示词

```text
ROLE
你负责《城市报修与工单处理系统》的维修人员工单处理模块。

BRANCH
feat/worker-processing

CONTEXT
读取团队开发规范、city-repair-project-rules 和 city-repair-ui-style。页面使用 frontend-design；完成后使用 webapp-testing。

SCOPE
只实现维修人员的待接工单、我的工单、处理详情、接单、开始处理、进度和完成。

REQUIRED WORK
1. 从登录上下文获取维修人员 ID；
2. 校验当前用户就是该工单当前有效分派人；
3. 只允许 PENDING_ACCEPT 接单；
4. 进入处理后状态统一为 PROCESSING；
5. 处理中允许提交进度并写日志；
6. 完成必须有处理说明，可上传处理后图片；
7. 完成后进入 COMPLETED，并在同一事务写日志；
8. 测试他人工单、非法状态、缺少说明和重复完成。

MUST NOT
- 不允许操作未分派给自己的工单；
- 不修改审核、优先级和居民原始描述；
- 不新增状态；
- 不修改其他成员模块；
- 不只隐藏按钮代替后端权限校验。

OUTPUT CONTRACT
输出修改文件、接口、状态校验、测试命令、测试结果、截图和剩余风险。
```

## 5.5 孔祥洋：统计看板与类别管理

### 分支

```text
feat/dashboard-category
```

### 页面

- 数据统计看板；
- 报修类别管理。

### 主要接口

```http
GET    /api/statistics/overview
GET    /api/statistics/status
GET    /api/statistics/category
GET    /api/statistics/trend
GET    /api/statistics/worker-rank
GET    /api/categories
POST   /api/categories
PUT    /api/categories/{id}
PUT    /api/categories/{id}/status
DELETE /api/categories/{id}
```

若项目不允许物理删除已引用类别，删除接口应返回明确错误，并引导停用。

### 主要数据表

```text
repair_order
repair_category
order_assignment
order_evaluation
```

### 看板必须包含

- 工单总数；
- 待处理数量；
- 已完成数量；
- 完成率；
- 状态分布；
- 类别分布；
- 最近 7 天工单趋势；
- 维修人员处理数量排行。

### 验收标准

- [ ] 所有统计来自后端 API 和真实 SQL；
- [ ] 新增或完成工单后统计变化；
- [ ] 时间范围和完成率计算正确；
- [ ] 类别支持新增、修改、启停；
- [ ] 已被引用类别不能被错误删除；
- [ ] 图表有加载、空数据和失败状态；
- [ ] ECharts 无控制台错误并能 resize。

### 发给 AI 的个人任务提示词

```text
ROLE
你负责《城市报修与工单处理系统》的统计看板与报修类别管理。

BRANCH
feat/dashboard-category

CONTEXT
读取团队开发规范、city-repair-project-rules 和 city-repair-ui-style。页面必须使用 frontend-design；完成后使用 webapp-testing。

SCOPE
只实现真实统计接口、ECharts 看板和报修类别 CRUD/启停。

REQUIRED WORK
1. 先确认 repair_order 的状态、时间和类别字段；
2. 编写真实聚合 SQL，禁止前端硬编码；
3. 实现总量、待处理、已完成、完成率、状态、类别、7 日趋势和人员排行；
4. 统一时间范围和完成率口径；
5. 实现类别新增、修改、启停；
6. 已被引用类别不得直接物理删除；
7. 页面处理 loading、空数据、错误和 resize；
8. 使用真实初始化数据验证统计变化。

MUST NOT
- 不写死图表数据；
- 不使用 3D 图表或随机渐变；
- 不新增工单状态；
- 不修改其他成员模块；
- 不用前端计算代替核心后端统计。

OUTPUT CONTRACT
输出统计口径、SQL/接口、修改文件、测试数据、测试命令、截图和剩余风险。
```

## 5.6 张天童：评价、通知、测试与附录材料

### 分支

```text
feat/evaluation-quality
```

### 页面

- 居民评价；
- 评价管理；
- 我的通知。

### 主要接口

```http
POST /api/repair-orders/{id}/evaluation
GET  /api/evaluations/my
GET  /api/admin/evaluations
GET  /api/notifications/my
```

### 主要数据表

```text
order_evaluation
repair_order
order_status_log
user_notification（若实现）
```

### 功能要求

- 只有本人且状态为 `COMPLETED` 的工单可评价；
- 评分范围 1–5；
- 同一工单只能评价一次；
- 评价成功后工单进入 `EVALUATED`；
- 管理员可查询评价；
- 通知至少覆盖审核结果、分派/接单、处理完成；
- 负责统一测试用例、Bug 表、AI 协作日志和文档初稿。

### 验收标准

- [ ] 未完成、他人和重复评价均被拒绝；
- [ ] 评价创建和工单状态更新在同一事务；
- [ ] 通知只展示给正确用户；
- [ ] 至少整理正常、异常、权限和回归测试；
- [ ] 文档内容能和代码、页面、接口及截图逐项对应；
- [ ] 不凭空补写未实现功能。

### 发给 AI 的个人任务提示词

```text
ROLE
你负责《城市报修与工单处理系统》的评价、通知、测试与附录材料。

BRANCH
feat/evaluation-quality

CONTEXT
读取团队开发规范、city-repair-project-rules 和 city-repair-ui-style。页面使用 frontend-design；完成后使用 webapp-testing；整理文档时使用 doc-coauthoring。

SCOPE
实现居民评价、管理员评价查询、本人通知，并负责测试和课程附录初稿。

REQUIRED WORK
1. 只有本人 COMPLETED 工单可评价；
2. 评分必须为 1–5，同一工单只允许一条评价；
3. 评价创建和工单改为 EVALUATED 必须同一事务；
4. 通知至少覆盖审核结果、工单分派/接单和完成；
5. 通知只能被对应接收人查询；
6. 使用 webapp-testing 验证正常、异常、权限和核心回归流程；
7. 整理 AI 原始提示词、人工修改、测试结果、Bug 修复和反思；
8. 文档中的每项功能必须有代码或截图证据。

MUST NOT
- 不允许管理员代替居民评价；
- 不允许重复评价；
- 不编造未实现功能和测试结果；
- 不修改其他成员模块；
- 不只写文档而不完成主责代码。

OUTPUT CONTRACT
输出修改文件、接口、测试用例、Bug 表、截图路径、AI 协作记录和剩余风险。
```

---

# 6. 统一测试与验收

## 6.1 每个模块最低测试集合

每名成员至少提供：

1. 正常流程测试；
2. 必填参数或格式错误测试；
3. 权限越界测试；
4. 非法状态测试；
5. 页面加载与接口失败测试；
6. 本模块回归测试。

## 6.2 核心端到端流程

最终必须按以下顺序完整演示：

```text
1. 居民登录并提交报修，上传现场图片；
2. 管理员登录，审核通过并设置优先级；
3. 管理员分派维修人员；
4. 维修人员登录，接单、开始处理并提交完成结果；
5. 居民查看处理时间线并评价；
6. 管理员打开统计看板，数据随工单变化。
```

## 6.3 必测异常

- 居民访问他人工单；
- 居民取消非待审核工单；
- 非管理员调用审核接口；
- 重复审核；
- 分派给非维修人员；
- 维修人员处理他人工单；
- 重复完成工单；
- 未完成工单评价；
- 重复评价；
- 删除已被引用类别。

## 6.4 AI 不得伪造测试

AI 必须给出实际执行的命令、输出或截图路径。下列表述不能视为通过：

```text
“代码看起来没有问题”
“理论上可以运行”
“应该已经修复”
“未执行测试，但预计通过”
```

---

# 7. 开发时间计划

| 日期 | 团队目标 | 必须完成 |
|---|---|---|
| 6 月 25 日 | 初始化与规范冻结 | 仓库、数据库、角色、分支、Skills、开发环境全部确认 |
| 6 月 26 日 | 后端主流程 | 各模块接口、状态校验和基础接口测试 |
| 6 月 27 日 | 前端与真实联调 | 页面、接口接入、第一轮运行截图 |
| 6 月 28 日 | 模块验收与合并 | 分支自测、依次合并、回归测试和 Bug 表 |
| 6 月 29 日 | 修复、文档与视频 | 只修 Bug、补证据、整理 README/附录、录制视频 |
| 6 月 30 日 | 最终提交 | 全新环境部署验证、检查命名和在线链接权限 |

> **6 月 28 日后停止新增大型功能。6 月 29 日只允许修 Bug、补证据、整理文档和录制视频。**

---

# 8. 每个人必须提交的证据

| 证据 | 最低要求 | 命名示例 |
|---|---|---|
| 代码 | 本人分支可运行，至少 3 次有效提交 | `苟佳豪-resident-commits.png` |
| 页面截图 | 核心页面、正常结果、异常提示 | `闫龙飞-审核-成功.png` |
| 接口截图 | 成功、参数错误、权限错误 | `袁翊博-complete-403.png` |
| AI 协作日志 | 工具、原提示词、输出摘要、人工修改、验证 | `孔祥洋-AI协作日志.md` |
| 测试记录 | 正常、异常、权限、回归 | `张天童-测试用例.md` |
| 讲解稿 | 30–45 秒讲清模块、技术点和人工审查 | `成员名-讲解稿.md` |

## 8.1 AI 协作日志模板

```markdown
## 日期与工具
2026-06-26，Claude Code / Codex

## 任务目标
本次只解决一个具体问题。

## 原始提示词
保留完整提示词，不得只写“让 AI 帮我写代码”。

## AI 输出摘要
AI 新增或修改了哪些文件、接口、SQL 或页面。

## 人工检查
发现了哪些业务、权限、状态、命名、异常或视觉问题。

## 人工修改
具体修改了什么，以及为什么不能直接采用 AI 原结果。

## 验证结果
实际执行的构建、接口测试、页面测试、截图或 Bug 修复对比。

## 最终证据
commit、PR、文件或截图路径。
```

---

# 9. 最终提交检查表

- [ ] 前后端完整代码可按 README 启动；
- [ ] 完整数据库导出和初始化 SQL；
- [ ] 演示账号和角色说明；
- [ ] README 包含环境、端口、数据库配置、启动方式和常见问题；
- [ ] 5 分钟视频完整跑通业务闭环；
- [ ] 附录包含系统设计、模块结构、流程图、接口设计、数据库设计；
- [ ] 附录包含 AI 协作日志、团队分工、测试、Bug、反思；
- [ ] 视频和在线文档链接可访问；
- [ ] 压缩包命名符合课程要求；
- [ ] 不包含真实密码、密钥、`node_modules`、`target`、`dist` 和无关大文件；
- [ ] 在一台干净环境上完成最后一次部署验证。

---

# 10. 给所有成员的最简操作步骤

1. 拉取组长提供的最新仓库；
2. 切换到自己的功能分支；
3. 把本文件发给正在使用的 AI；
4. 发第 3.5 节“安装 Skills”提示词；
5. 重启 AI 客户端并确认 Skills 已发现；
6. 发第 3.8 节“统一启动提示词”；
7. 再发第 5 章中属于自己的个人任务提示词；
8. 先让 AI 输出计划，确认后才允许编码；
9. 完成后真实运行测试并保存截图；
10. 至少提交 3 次 Git commit；
11. 把代码、截图、AI 日志和测试记录交给组长；
12. 未经组长允许，不合并 `develop`，不修改项目级 Skills。

---

# 11. 参考链接

- Anthropic Agent Skills 仓库：<https://github.com/anthropics/skills>
- Agent Skills 开放规范：<https://agentskills.io/specification>
- Agent Skills 规范仓库：<https://github.com/agentskills/agentskills>
- `frontend-design`：<https://github.com/anthropics/skills/tree/main/skills/frontend-design>
- `webapp-testing`：<https://github.com/anthropics/skills/tree/main/skills/webapp-testing>
- `skill-creator`：<https://github.com/anthropics/skills/tree/main/skills/skill-creator>
- `doc-coauthoring`：<https://github.com/anthropics/skills/tree/main/skills/doc-coauthoring>
- OpenAI Codex Skills 仓库：<https://github.com/openai/skills>
- Codex `$skill-installer`：<https://github.com/openai/skills/tree/main/skills/.system/skill-installer>

> GitHub Skill 可能更新。组长在项目启动时固定一次版本并提交到项目仓库；开发期间成员不得私自自动升级，以免不同人的规则和脚本版本不一致。
