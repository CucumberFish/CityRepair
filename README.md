# 城市报修与工单处理系统

课程：《信息系统开发与实践实验》

目标：完成一个可运行、可在 5 分钟内演示的城市报修与工单处理原型，并留下数据库、API、前后端联调、测试、AI 协作和团队协作证据。

## 核心流程

```text
居民提交报修
-> 管理员审核
-> 管理员分派维修人员
-> 维修人员接单并处理
-> 维修人员提交完成结果
-> 居民评价
-> 管理员查看统计看板
```

## 开发前必读

1. `docs/00-开发启动包索引.md`
2. `城市报修与工单处理系统-团队分工与Agent-Skills开发规范.md`
3. `docs/数据库与接口定稿.md`
4. `docs/GitHub协作与提交规范.md`
5. `.agents/skills/city-repair-project-rules/SKILL.md`
6. 涉及页面时再读 `.agents/skills/city-repair-ui-style/SKILL.md`

## 固定技术栈

如果没有现成项目骨架，默认使用：

- 后端：Spring Boot 3 + MyBatis-Plus
- 前端：Vue 3 + Vite + Element Plus
- 数据库：MySQL 8
- 图表：ECharts
- 接口测试：Apifox 或 Postman
- 版本管理：Git + GitHub

## 数据库

初始化脚本：

```text
sql/init_city_repair.sql
```

数据库名固定为：

```text
city_repair
```

演示账号：

| 角色 | 用户名 | 密码 |
|---|---|---|
| 管理员 | `admin` | `123456` |
| 居民 | `resident1` | `123456` |
| 维修人员 | `worker1` | `123456` |
| 维修人员 | `worker2` | `123456` |

脚本中密码为 `{noop}123456`，如果后端使用其他加密方式，组长统一替换，不允许组员各自改。

## Git 分支

- `main`：最终稳定版；
- `develop`：集成分支；
- `feat/resident-repair`：居民报修；
- `feat/admin-dispatch`：管理员审核与分派；
- `feat/worker-processing`：维修人员处理；
- `feat/dashboard-category`：统计看板与类别管理；
- `feat/evaluation-quality`：评价、通知、测试与附录。

详细命令见 `docs/GitHub协作与提交规范.md`。

## 每人必须交的证据

- 至少 3 次有效 commit；
- 页面截图；
- 接口测试截图；
- AI 协作日志；
- 测试记录；
- 30 到 45 秒模块讲解稿。

模板在 `templates/`。

## 当前状态

当前仓库已经包含基础前后端骨架：

- 后端目录：`backend/`
- 前端目录：`frontend/`

后端默认端口为 `8080`，上下文路径为 `/api`；前端默认端口为 `5173`。

本地启动后端前，请准备 MySQL 数据库 `city_repair`，并按需设置环境变量：

```bash
DB_HOST=localhost
DB_PORT=3306
DB_NAME=city_repair
DB_USERNAME=root
DB_PASSWORD=你的密码
```

后端启动：

```bash
cd backend
mvn spring-boot:run
```

前端启动：

```bash
cd frontend
npm install
npm run dev
```
