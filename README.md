# Memo Pad v4.0 — 企业级 AI 智能笔记

基于 **Vue 3 + Spring Boot + LangChain4j 1.17** 的多用户智能笔记应用。

## v4.0 更新

- **多用户数据隔离** — ThreadLocal + SQL `user_id` 过滤
- **邮箱注册登录** — BCrypt 加密，自动昵称
- **流式 Agent** — SSE 逐字输出 + 工具调用可视化
- **Agent 工具链** — ReAct 推理 + 14 个 @Tool + RAG 知识库检索
- **思维链可视化** — Thought/Action/Observation 彩色渲染
- **安全护栏** — 8 条注入检测 + PII 脱敏
- **UI 重构** — 浮动按钮 + 侧边栏聊天 + 渐变动效

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.4.5, JDK 17, MyBatis |
| AI 引擎 | LangChain4j 1.17.2 (OpenAI 适配器 → DashScope 通义千问) |
| 数据库 | MySQL 8.0 + PostgreSQL 16 (PGVector) |
| 前端 | Vue 3 + Element Plus + Vite 8 |
| 安全 | BCrypt + Guardrails + ThreadLocal 隔离 |

## 项目结构

```
Memo-Pad/
├── SpringBoot/src/main/java/org/example/
│   ├── agent/           # MemoAgent 接口 + 14个 @Tool 工具
│   ├── common/           # UserContext(ThreadLocal) + UserFilter
│   ├── config/           # AgentConfig (ChatModel + Streaming + Guardrails)
│   ├── controller/       # REST API
│   ├── guard/            # InputGuard + SecurityGuard
│   ├── rag/              # KnowledgeBaseService (PGVector)
│   └── service/          # 业务逻辑
├── vue/src/
│   ├── components/       # AiSidebar(侧边栏) + FloatingButton + RagUploader
│   └── views/            # Home/Group/Trash/Login/Register/Manage
├── sql/init.sql          # 6 表建表脚本 (含 user_id 隔离)
└── docker-compose.yml    # MySQL + PostgreSQL 一键部署
```

## 快速开始

```bash
# 1. 初始化数据库
mysql -u root < sql/init.sql

# 2. 复制配置模板
cp SpringBoot/src/main/resources/application-template.yml \
   SpringBoot/src/main/resources/application.yml
# 编辑 application.yml，填入通义千问 API Key

# 3. 启动 (可选 PostgreSQL for RAG)
docker-compose up -d mysql    # 仅 MySQL
# docker-compose up -d         # 全部服务

# 4. 后端
cd SpringBoot && mvn spring-boot:run     # http://localhost:9999

# 5. 前端
cd vue && npm install && npm run dev     # http://localhost:5173
```

## API

| 端点 | 功能 |
|------|------|
| POST /admin/register | 邮箱注册 |
| POST /admin/login | 邮箱登录 |
| POST /ai/chat | 文字润色/总结/扩写/翻译 |
| POST /ai/agent | Agent 同步模式 (14个工具) |
| POST /ai/agent/stream | Agent 流式 SSE (逐字 + 工具卡片) |
| POST /ai/agent/plan | Agent 规划执行 |
| POST /ai/rag/upload | 上传文档到知识库 |
| GET /ai/rag/search | 知识库语义搜索 |
| POST /note/* | 笔记 CRUD |
| POST /noteGroup/* | 分组管理 |

## License

MIT
