# Memo Pad — 基于 LangChain4j 的智能笔记应用

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.4.5, JDK 17 |
| AI 框架 | LangChain4j 1.17.2 (AI Services, @Tool 注解, TokenStream 流式) |
| LLM | 通义千问 qwen-plus (DashScope, 通过 OpenAI 兼容接口调用) |
| 持久层 | MyBatis + MySQL 8.0 |
| 向量存储 | PGVector (PostgreSQL + pgvector 扩展) |
| 前端框架 | Vue 3 (Composition API) + Element Plus + Vite |
| 测试 | JUnit 5 (13 个单元测试) |

## 核心功能

### 1. LangChain4j Agent 智能体

- 实现 **ReAct 推理模式** (Thought→Action→Observation→Answer 思维链)
- 注册 **12 个 @Tool 工具**：笔记 CRUD、批量删除/移动、统计查询、Markdown 导出
- 同步/流式双模式，SSE 协议逐字实时展示

### 2. RAG 检索增强生成

- 文档上传 → 递归切分(500 字窗口/50 字重叠) → AllMiniLM 向量化 → PGVector 存储
- 语义向量检索(余弦相似度 ≥0.6)，将相关知识片段注入 LLM 上下文

### 3. 安全护栏

- **输入护栏**：8 条正则规则检测 Prompt 注入(中文/英文/越狱/角色扮演)
- **输出护栏**：PII 自动脱敏(手机号/身份证号/银行卡号)
- 基于 LangChain4j Guardrails 框架实现，挂载到 Agent 调用链路

### 4. 企业级特性

- Agent 审计日志：工具调用耗时/参数/结果追踪
- 会话记忆管理：MessageWindow 20 轮滑动窗口
- Docker Compose 一键部署 + 环境变量 API Key 脱敏

## API 端点

| 端点 | 说明 |
|------|------|
| POST /ai/chat | 文字润色/总结/扩写/翻译 |
| POST /ai/agent | Agent 同步模式 (ReAct 工具调用) |
| POST /ai/agent/stream | Agent 流式 SSE (逐字实时) |
| POST /ai/agent/plan | Agent 规划执行模式 |
| POST /ai/rag/upload | 知识库文档上传 |
| GET /ai/rag/search | 知识库语义搜索 |

## 项目结构

```
Memo-Pad/
├── SpringBoot/src/main/java/org/example/
│   ├── agent/
│   │   ├── MemoAgent.java           # Agent 接口 (ReAct + 同步/流式)
│   │   ├── NoteTools.java           # 8 个基础笔记工具
│   │   ├── AdvancedTools.java       # 批量/统计/导出工具
│   │   └── AgentAuditService.java   # 审计日志服务
│   ├── config/AgentConfig.java      # Bean 装配 (ChatModel + Streaming + Guardrails)
│   ├── controller/AiController.java # AI 端点 (/chat /agent /agent/stream /rag/*)
│   ├── guard/
│   │   ├── SecurityGuard.java       # 注入检测 + PII 脱敏
│   │   └── InputGuard.java          # LangChain4j Guardrail
│   ├── rag/KnowledgeBaseService.java # RAG 知识库服务
│   └── service/                     # 业务服务层
├── vue/src/
│   ├── components/
│   │   ├── AiAssistant.vue          # 流式 Agent 对话 UI
│   │   └── RagUploader.vue          # 知识库上传 UI
│   └── views/Home.vue               # 主界面
├── sql/init.sql                     # 6 张表建表脚本
└── docker-compose.yml               # MySQL + PostgreSQL 编排
```

## 快速开始

```bash
# 1. 启动基础设施
docker-compose up -d

# 2. 设置通义千问 API Key
export MEMOPAD_AI_API_KEY=sk-your-key

# 3. 启动后端
cd SpringBoot && mvn spring-boot:run

# 4. 启动前端
cd vue && npm install && npm run dev

# 5. 访问 http://localhost:5173
```

## 简历浓缩版

```
Memo Pad — 基于 LangChain4j 的智能笔记应用
Spring Boot + Vue 3 | LangChain4j 1.17 + 通义千问 | MySQL + PGVector

• 基于 LangChain4j AiServices 框架构建 ReAct 模式 AI Agent，通过 @Tool 注解注册
  12 个工具实现笔记全生命周期管理，支持 SSE 流式实时对话
• 实现 RAG 检索增强生成管线：文档递归切分 → AllMiniLM 向量化 → PGVector 语义检索
• 设计多层安全护栏：8 条 Prompt 注入检测规则 + PII 自动脱敏，基于 LangChain4j
  Guardrails 框架挂载到 Agent 调用链路
• 使用 Docker Compose 编排 MySQL + PostgreSQL 多服务，环境变量管理 API Key 实现配置安全
```
