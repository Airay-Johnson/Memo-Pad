# Memo Pad

一个基于 Vue 3 + Spring Boot 的智能笔记应用，支持笔记管理、分组归类、回收站以及 AI 文字助手功能。

## 功能特性

- **用户系统** — 注册、登录、个人信息修改、密码修改
- **笔记管理** — 新建、编辑、自动保存、删除
- **分组管理** — 创建分组、按分组查看笔记、删除分组
- **回收站** — 软删除恢复、永久删除
- **AI 文字助手** — 润色、总结、扩写、翻译（基于通义千问 API）
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
│       ├── common/               # Result、CorsConfig、AiConfig
│       ├── controller/           # AdminController、NoteController、NoteGroupController、AiController
│       ├── entity/               # Admin、Note、NoteGroup、AiRequest
│       ├── mapper/               # MyBatis 接口
│       ├── service/              # 业务逻辑层
│       └── exception/            # 全局异常处理
├── vue/                          # 前端
│   └── src/
│       ├── views/                # Login、Register、Manage、Home、Group、Trash、404
│       ├── components/           # AiAssistant.vue
│       ├── router/               # Vue Router 配置
│       └── utils/request.js      # Axios 封装
└── sql/
    └── init.sql                  # 数据库初始化脚本
```

## 快速开始

### 环境要求

- JDK 17+
- Node.js 20.19+ 或 22.12+
- MySQL 8.0+

### 1. 初始化数据库

```bash
mysql -u root -p < sql/init.sql
```

默认用户：`admin` / `123456`

### 2. 启动后端

修改 `SpringBoot/src/main/resources/application.yml` 中的数据库密码和 AI 配置：

```yaml
spring:
  datasource:
    password: 你的MySQL密码

ai:
  api-key: 你的通义千问API Key
```

```bash
cd SpringBoot
mvn spring-boot:run
```

后端启动在 `http://localhost:9999`

### 3. 启动前端

```bash
cd vue
npm install
npm run dev
```

前端启动在 `http://localhost:5173`

### 4. 使用 AI 助手

在笔记编辑页面点击「AI 助手」按钮，选择操作（润色/总结/扩写/翻译），即可调用通义千问处理文字。

> AI 助手需要有效的通义千问 API Key，请在阿里云 Model Studio 获取。

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

## License

MIT
