# Alice Learn 雅思学习网站

Vue 3 + TypeScript 前端，Java 21 + Spring Boot 3 后端，本地 MySQL，DeepSeek 用于写作范文和批改。

- 需求与表结构：[docs/需求准备文档.md](docs/需求准备文档.md)
- 第一阶段做了什么：[docs/阶段一开发记录.md](docs/阶段一开发记录.md)

## 环境

- JDK 21
- Maven 3.9+
- Node.js 18+
- 本地 MySQL（账号 `root` / 密码 `root`，库名 `alice_learn` 会自动创建）
- 可选：DeepSeek API Key（写作 AI 功能需要）

## 启动后端

在 `backend` 目录：

```bash
# 可选：配置 AI
set DEEPSEEK_API_KEY=sk-your-key

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

## 建议体验路径

1. 注册并登录
2. 写作题库选题，保存草稿或提交
3. 生成 AI 范文（需 API Key）
4. 查看批改报告（四维分数雷达图）
5. 词汇加入生词本，做记忆卡片
6. 阅读练习提交后查看解析

## 词库说明

第一版约 300 个词，以 Academic Word List 高频词为底，按话题分类写入 Flyway 种子数据。
