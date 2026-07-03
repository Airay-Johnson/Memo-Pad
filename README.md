# Memo Pad v2.0

一个基于 Vue 3 + Spring Boot 的智能笔记应用，支持笔记管理、分组归类、回收站以及 **五层 AI Agent** 功能。

## v2.0 新特性

- **AI Agent 自主操作** — AI 自动调用工具：查看笔记、搜索、创建、修改、删除、移动分组
- **持久化记忆** — AI 记住上下文，跨轮对话引用历史
- **规划执行模式** — AI 先出计划再逐步执行，完成复杂多步任务

## 功能特性

- **用户系统** — 注册、登录、个人信息修改、密码修改
- **笔记管理** — 新建、编辑、自动保存、删除
- **分组管理** — 创建分组、按分组查看笔记、移动笔记到分组
- **回收站** — 软删除恢复、永久删除
- **AI 文字助手** — 润色、总结、扩写、翻译（基于通义千问 API）
- **AI Agent 模式** — AI 自主调用工具管理笔记（Layer 3-5）
- **响应式布局** — 侧边栏导航 + 内容区编辑

## 技术栈

| 层级 | 技术 |
|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Vue Router 5 |
| 后端 | Spring Boot 3.4.5 + MyBatis 3 + OkHttp3 |
| 数据库 | MySQL 8 |
| AI | 通义千问（qwen-plus） |

## 项目结构

```
Memo-Pad/
├── SpringBoot/                   # 后端
│   └── src/main/java/org/example/
│       ├── Application.java      # 启动类
│       ├── common/               # Result、CorsConfig、AiConfig、ToolResult
│       ├── controller/           # AdminController、NoteController、NoteGroupController、AiController
│       ├── entity/               # Admin、Note、NoteGroup、AiRequest、ToolType、Conversation
│       ├── mapper/               # MyBatis 接口（Admin/Note/NoteGroup/Conversation）
│       ├── service/              # AiService（五层 Agent）、ToolService、ConversationService 等
│       └── exception/            # 全局异常处理
├── vue/                          # 前端
│   └── src/
│       ├── views/                # Login、Register、Manage、Home、Group、Trash、404
│       ├── components/           # AiAssistant.vue（三种模式切换）
│       ├── router/               # Vue Router 配置
│       └── utils/request.js      # Axios 封装
├── sql/
│   └── init.sql                  # 数据库初始化脚本（含 conversation 表）
├── .github/workflows/
│   └── release.yml               # GitHub Actions CI/CD
├── Dockerfile                    # 多阶段构建
└── docker-compose.yml            # MySQL + 后端
```

## AI Agent 五层架构

| 层 | 名称 | 说明 |
|----|------|------|
| 1 | AI 接口 | 调通通义千问 API，基础文字处理 |
| 2 | 意图理解 | 用户自由输入需求，AI 理解意图 |
| 3 | 工具调用 | AI 决定用什么工具，后端执行，循环往复 |
| 4 | 持久记忆 | 对话历史存 MySQL，跨轮记忆上下文 |
| 5 | 自主规划 | AI 先出计划再执行，完成复杂多步任务 |

### Agent 可用工具

| 工具 | 说明 |
|------|------|
| LIST_NOTES | 查看所有笔记 |
| READ_NOTE | 读取指定笔记内容 |
| SEARCH_NOTE | 按关键词搜索笔记 |
| CREATE_NOTE | 创建新笔记 |
| UPDATE_NOTE | 修改笔记标题或内容 |
| DELETE_NOTE | 删除笔记（移至回收站） |
| MOVE_NOTE | 移动笔记到分组 |
| LIST_GROUPS | 查看所有分组 |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 20.19+ 或 22.12+
- MySQL 8.0+

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

> 导入后确保 conversation 表已创建（Layer 4 记忆所需）。如单独建表：
> ```sql
> CREATE TABLE IF NOT EXISTS conversation (
>   id          INT PRIMARY KEY AUTO_INCREMENT,
>   session_id  VARCHAR(36)  NOT NULL,
>   role        VARCHAR(20)  NOT NULL,
>   content     TEXT         NOT NULL,
>   create_time DATETIME     DEFAULT now(),
>   INDEX idx_session (session_id, create_time)
> ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
> ```

默认用户：`admin` / `123456`

### 2. 配置并启动后端

修改 `SpringBoot/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码

ai:
  api-key: 你的通义千问API Key  # https://dashscope.console.aliyun.com/apiKey
```

```bash
cd SpringBoot
mvn spring-boot:run
```

### 3. 启动前端

```bash
cd vue
npm install
npm run dev
```

### 4. 使用 AI 功能

| 模式 | 访问方式 | 用途 |
|------|---------|------|
| 文字处理 | AI 助手 → 文字处理 | 润色、总结、扩写、翻译 |
| Agent 模式 | AI 助手 → Agent | 对话式笔记管理 |
| 规划模式 | AI 助手 → Agent → 开启规划 | 复杂多步任务自动执行 |

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/login` | 用户登录 |
| POST | `/admin/register` | 用户注册 |
| PUT | `/admin/updateProfile` | 修改个人信息 |
| PUT | `/admin/updatePassword` | 修改密码 |
| GET | `/note/selectAll` | 获取所有笔记 |
| GET | `/note/selectByGroupId` | 按分组获取笔记 |
| POST | `/note/insertNewNote` | 新建笔记 |
| PUT | `/note/updateNote` | 更新笔记 |
| PUT | `/note/moveToTrash` | 移至回收站 |
| GET | `/note/selectTrash` | 获取回收站笔记 |
| PUT | `/note/restoreNote` | 恢复笔记 |
| DELETE | `/note/deleteForever` | 永久删除 |
| GET | `/noteGroup/selectAll` | 获取所有分组 |
| POST | `/noteGroup/insert` | 新建分组 |
| DELETE | `/noteGroup/deleteGroup` | 删除分组 |
| POST | `/ai/chat` | AI 文字处理 |
| POST | `/ai/agent` | AI Agent 对话（含记忆） |
| POST | `/ai/agent/plan` | AI 规划执行模式 |
| DELETE | `/ai/memory` | 清除会话记忆 |

## License

MIT
