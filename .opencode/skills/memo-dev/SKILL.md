---
name: memo-dev
description: Use ONLY when the user explicitly requests new feature development, adds new capabilities, or creates new components for the Memo-Pad project. Covers the full 7-phase development workflow with mandatory review gate.
---

# Memo-Pad 开发流程 v2.0

## 核心原则

1. **企业级对标** — 开发前检索 ≥2 个成熟产品/案例作为参照
2. **一次规划、不再回头** — 评审门禁通过后方案冻结
3. **渐进式验证** — 每完成一个文件立即编译检查
4. **零返工设计** — 修改前先读代码，变更仅限目标范围
5. **Token 效率优先** — 批量并行读文件，最小化轮次

## 7 阶段流程

### Phase 1: 需求分析
- 明确功能边界：用户故事 → 验收条件
- 可行性判断：技术栈限制、资源评估
- **输出**：功能需求清单（≤10 条）

### Phase 2: 对标研究
- 检索 ≥2 个行业最佳案例/开源项目
- 提取可复用的架构模式和设计决策
- **输出**：对标摘要 + 技术方案草案

### Phase 3: 技术设计
- 技术栈选定（优先复用现有依赖）
- 接口契约定义（REST API / 类接口签名）
- 文件级变更清单（新增/修改/删除）
- **输出**：完整设计文档

### Phase 4: 风险评审
- 识别 ≥3 个潜在风险 + 缓解措施
- 评估对现有功能的影响范围
- **输出**：风险评估矩阵

### ★ Phase 5: 评审门禁 (HARD GATE)
- **用户确认计划** — 关键决策点，不可跳过
- 门禁通过后方案冻结，禁止架构级修改
- 若用户提出方案级修改，重新进入 Phase 3

### Phase 6: 增量开发
- 按文件逐个实现：读→改→编译验证
- 每 3-5 个文件完成一个子功能后自测
- 每模块完成后 Git commit

### Phase 7: 验证交付
- 全量编译 + Lint 检查
- 边界场景测试(空输入/超长/并发/注入)
- 变更总结报告

## 禁止事项

- 禁止 Phase 5 门禁通过后推翻方案
- 禁止未读代码就修改文件
- 禁止一次性修改 >5 个文件不编译验证
- 禁止添加不必要的代码注释
- 禁止循环讨论已确认的决策

## 必须事项

- 每个文件修改前必须 Read 读取
- 每次修改后必须编译/类型检查
- 使用批量并行工具调用减少轮次
- 代码风格严格跟随现有文件模式

## 技术栈背景

- 后端：Spring Boot 3.4.5 + LangChain4j 1.17.2 + MyBatis + MySQL
- 前端：Vue 3 + Element Plus + Vite
- AI：通义千问(DashScope) 通过 OpenAI 兼容接口
- 关键包：`dev.langchain4j.*`, `org.example.*`
