# Alice Learn 雅思学习网站

Vue 3 + TypeScript 前端，Java 21 + Spring Boot 3 后端，本地 MySQL，DeepSeek 用于写作范文和批改。

- 需求与表结构：[docs/需求准备文档.md](docs/需求准备文档.md)
- 第一阶段做了什么：[docs/阶段一开发记录.md](docs/阶段一开发记录.md)
- 第二阶段（草稿更新、阅读历史、校验加固、页面状态）：[docs/阶段二开发记录.md](docs/阶段二开发记录.md)

## 环境

- JDK 21
- Maven 3.9+
- Node.js 18+
- 本地 MySQL（账号密码写在 `backend/.env.local`，库名 `alice_learn` 会自动创建）
- 可选：DeepSeek API Key（写作 AI 功能需要）

## 配置（.env）

后端的数据库密码、DeepSeek Key、JWT 密钥等都从 `backend/.env` 与 `backend/.env.local` 读取：

| 文件 | 是否提交 Git | 用途 |
|------|--------------|------|
| `backend/.env` | 是 | 模板，只放非敏感默认值和空占位 |
| `backend/.env.local` | **否**（已 gitignore） | 你的真实密码 / Key，覆盖 `.env` |

首次克隆后，在 `backend` 目录新建 `.env.local`：

```properties
DB_PASSWORD=你的MySQL密码
DEEPSEEK_API_KEY=sk-你的key
JWT_SECRET=随便一串至少32位的随机字符
```

可配置项及说明见 `backend/.env`。优先级：`.env` < `.env.local` < 操作系统环境变量（服务器上直接 `export` 即可覆盖）。

## 启动后端

在 `backend` 目录：

```bash
mvn spring-boot:run
```

- 接口文档（Knife4j）：http://localhost:8080/doc.html
- API 前缀：`/api`

没有 API Key 时，注册登录、题库、词汇、阅读仍可使用；AI 范文和批改会提示未配置密钥。

## 启动前端

在 `frontend` 目录：

```bash
npm install
npm run dev
```

浏览器打开 http://localhost:5173

生产构建：`npm run build`（先跑 `vue-tsc` 类型检查，再由 Vite 输出到 `dist/`）。

## 校验

- 后端单测：在 `backend` 目录执行 `mvn test`
- 前端类型检查：在 `frontend` 目录执行 `npx vue-tsc -b`

## 建议体验路径

1. 注册并登录
2. 写作题库选题，保存草稿（再次进入同一题会自动回填，继续改同一篇）
3. 生成 AI 范文（需 API Key，生成后随作文保存）
4. 提交后在报告页发起批改，查看四维分数雷达图和逐句建议
5. 词汇加入生词本，做记忆卡片
6. 阅读练习（带计时）提交后查看解析
7. 个人中心查看作文记录、阅读记录，可继续写 / 回顾解析 / 删除

## 词库说明

第一版约 300 个词，以 Academic Word List 高频词为底，按话题分类写入 Flyway 种子数据。
