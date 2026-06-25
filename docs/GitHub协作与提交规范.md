# GitHub 协作与提交规范

## 仓库分支

固定分支：

| 分支 | 用途 | 谁能合并 |
|---|---|---|
| `main` | 最终稳定版和提交版 | 组长 |
| `develop` | 日常集成版 | 组长或授权成员 |
| `feat/resident-repair` | 居民报修 | 苟佳豪 |
| `feat/admin-dispatch` | 管理员审核与分派 | 闫龙飞 |
| `feat/worker-processing` | 维修人员处理 | 袁翊博 |
| `feat/dashboard-category` | 统计看板与类别管理 | 孔祥洋 |
| `feat/evaluation-quality` | 评价、通知、测试与附录 | 张天童 |

## 组长初始化仓库

```bash
git init
git add .
git commit -m "chore: 初始化开发规范、数据库和项目级Skills"
git branch -M main
git remote add origin <你的GitHub仓库地址>
git push -u origin main

git checkout -b develop
git push -u origin develop
```

在 GitHub 上设置：

- `main` 禁止直接 push；
- `develop` 建议通过 Pull Request 合并；
- 邀请所有成员加入仓库；
- 要求 PR 至少带测试结果和截图。

## 组员第一次拉代码

```bash
git clone <你的GitHub仓库地址>
cd <仓库目录>
git checkout develop
git pull origin develop
git checkout -b feat/<自己的分支名>
git push -u origin feat/<自己的分支名>
```

示例：

```bash
git checkout -b feat/resident-repair
git push -u origin feat/resident-repair
```

## 每天开始开发前

```bash
git checkout develop
git pull origin develop
git checkout feat/<自己的分支名>
git merge develop
```

如果有冲突，先停止开发，截图或复制冲突文件名发给组长，不要凭感觉乱删别人代码。

## 每次提交

```bash
git status
git add <本次修改的文件>
git commit -m "feat: 完成具体功能说明"
git push
```

提交信息类型：

- `feat`: 新功能
- `fix`: 修复问题
- `test`: 测试用例或测试记录
- `docs`: 文档
- `style`: 不影响逻辑的样式调整
- `refactor`: 不改行为的重构
- `chore`: 构建、配置、初始化

每名成员至少 3 次有效提交。禁止最后一次把全部代码一次性提交。

## 禁止提交

不要提交：

- `node_modules/`
- `target/`
- `dist/`
- `.idea/`、`.vscode/` 中的个人配置
- `.env` 中的真实密码
- 日志文件
- 真实密钥、真实个人账号密码
- 无关大文件

建议 `.gitignore` 至少包含：

```gitignore
node_modules/
target/
dist/
logs/
*.log
.env
.env.local
.idea/
.vscode/
```

## 提 Pull Request 前

每个 PR 必须写清：

- 本次完成什么；
- 改了哪些接口；
- 是否改数据库；
- 实际执行过哪些命令；
- 测试结果；
- 截图或接口测试证据；
- 剩余风险。

可以直接复制 `templates/PR自检模板.md`。

## 合并顺序建议

1. 组长基础架构、数据库、登录权限。
2. 居民报修。
3. 管理员审核与分派。
4. 维修人员处理。
5. 评价与通知。
6. 统计看板与类别管理。
7. 测试、文档、README、演示脚本。

每合并一个模块，组长都跑一次核心流程，避免最后一天才发现状态接不上。
