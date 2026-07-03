# Memo Pad v3 — LangChain4j + RAG + 安全护栏

基于 Vue 3 + Spring Boot 的智能笔记应用，**从裸写 Agent 升级为 LangChain4j 企业级框架**。

## v3.0 升级内容

- **LangChain4j 框架化** — 用 `@Tool` 注解替代手写 ToolService switch 路由和 while 循环
- **RAG 知识库** — 上传文档 → 自动切分向量化 → 语义检索，回答基于真实文档
- **安全护栏** — Prompt 注入检测 + PII（身份证/手机号）自动脱敏
- **Agent 配置可视化** — `AgentConfig.java` 像搭乐高一样组装 AI 组件

## 技术栈

| 层级 | v2.0（裸写） | v3.0（框架） |
|------|------------|------------|
| AI 调用 | OkHttp 手拼 JSON | LangChain4j DashScope 适配器 |
| 工具调度 | ToolService switch 路由 | `@Tool` 注解自动路由 |
| 记忆管理 | ConversationService + MySQL | `@MemoryId` + ChatMemory |
| 文档检索 | 无 | RAG (Embedding + 向量检索) |
| 安全防护 | 无 | Prompt 注入检测 + PII 脱敏 |

## 项目结构

```
SpringBoot/src/main/java/org/example/
├── agent/
│   ├── MemoAgent.java          ← LangChain4j Agent 接口
│   └── NoteTools.java          ← @Tool 注解工具类
├── rag/
│   └── KnowledgeBaseService.java  ← RAG 知识库
├── guard/
│   └── SecurityGuard.java      ← 输入/输出安全护栏
└── config/
    └── AgentConfig.java        ← Agent 组件装配
```

## 快速开始

1. JDK 17+, Node.js 18+, MySQL 8.0
2. 初始化数据库: `mysql -u root -p123456 < sql/init.sql`
3. 后端: `cd SpringBoot && mvn spring-boot:run`
4. 前端: `cd vue && npm install && npm run dev`
5. 访问 http://localhost:5173

## API 端点

| 端点 | 功能 |
|------|------|
| POST /ai/chat | 文字润色/总结/翻译 |
| POST /ai/agent | LangChain4j Agent 自动工具调用 |
| POST /ai/agent/plan | Agent 规划执行模式 |
| DELETE /ai/memory | 清除会话记忆 |
| POST /ai/rag/upload | 上传文档到知识库 |
| GET /ai/rag/search | 知识库语义搜索 |
