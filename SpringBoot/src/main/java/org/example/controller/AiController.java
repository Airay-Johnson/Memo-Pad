package org.example.controller;

import jakarta.annotation.Resource;
import org.example.agent.MemoAgent;
import org.example.common.Result;
import org.example.entity.AiRequest;
import org.example.guard.SecurityGuard;
import org.example.rag.KnowledgeBaseService;
import org.example.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AI Controller v3 — LangChain4j + RAG + 安全护栏。
 *
 * 变更摘要：
 * - /agent、/agent/plan 统一走 MemoAgent（LangChain4j）
 * - /chat 仍保留旧版 AiService（简单文字处理不需要 Agent 循环）
 * - 新增 /rag/upload 知识库上传
 * - 新增 /rag/search 知识库搜索
 * - 所有端点加 SecurityGuard 输入检验
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @Resource
    private MemoAgent memoAgent;

    @Resource
    private SecurityGuard securityGuard;

    @Resource
    private KnowledgeBaseService knowledgeBase;

    // ===== 第 1/2 层：文字处理（保留旧版，功能简单不需要 Agent 循环）=====

    @PostMapping("/chat")
    public Result chat(@RequestBody AiRequest req) {
        String block = securityGuard.validateInput(req.getText());
        if (block != null) return Result.error(block);

        try {
            String rawJson = aiService.callAi(req.getText(), req.getAction(), req.getUserRequest());
            return Result.success(rawJson);
        } catch (Exception e) {
            return Result.error("AI 调用失败: " + e.getMessage());
        }
    }

    // ===== 第 3/4 层：Agent + 记忆（LangChain4j 版）=====

    @PostMapping("/agent")
    public Result agent(@RequestBody Map<String, String> req) {
        String message = req.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("请输入需求");
        }

        // 安全护栏：输入检查
        String block = securityGuard.validateInput(message);
        if (block != null) return Result.error(block);

        try {
            String sessionId = req.getOrDefault("sessionId", "default");
            String answer = memoAgent.chat(sessionId, message);

            // 安全护栏：输出脱敏
            answer = securityGuard.sanitizeOutput(answer);

            return Result.success(answer);
        } catch (Exception e) {
            return Result.error("Agent 执行失败: " + e.getMessage());
        }
    }

    // ===== 第 5 层：规划模式 =====

    @PostMapping("/agent/plan")
    public Result agentPlan(@RequestBody Map<String, String> req) {
        String message = req.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("请输入需求");
        }

        String block = securityGuard.validateInput(message);
        if (block != null) return Result.error(block);

        try {
            String sessionId = req.getOrDefault("sessionId", "default-plan");
            String answer = memoAgent.chat(sessionId,
                "请按以下步骤处理用户需求。先分析需求，拆成执行步骤，再逐步执行，最后总结：\n" + message);
            answer = securityGuard.sanitizeOutput(answer);
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error("规划执行失败: " + e.getMessage());
        }
    }

    // ===== 记忆管理 =====

    @DeleteMapping("/memory")
    public Result clearMemory(@RequestBody Map<String, String> req) {
        String sessionId = req.get("sessionId");
        aiService.clearMemory(sessionId);
        return Result.success("记忆已清除");
    }

    // ===== RAG 知识库 =====

    /**
     * 上传文档到知识库。支持纯文本、Markdown。
     */
    @PostMapping("/rag/upload")
    public Result ragUpload(@RequestBody Map<String, String> req) {
        String content = req.get("content");
        String fileName = req.getOrDefault("fileName", "未命名文档");

        if (content == null || content.trim().isEmpty()) {
            return Result.error("文档内容不能为空");
        }

        try {
            knowledgeBase.ingest(content, Map.of("fileName", fileName));
            return Result.success("文档「" + fileName + "」已存入知识库，共 " + content.length() + " 字符");
        } catch (Exception e) {
            return Result.error("知识库存储失败: " + e.getMessage());
        }
    }

    /**
     * 搜索知识库，返回最相关的片段。
     */
    @GetMapping("/rag/search")
    public Result ragSearch(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return Result.error("搜索关键词不能为空");
        }

        try {
            var results = knowledgeBase.search(query, 5);
            return Result.success(results);
        } catch (Exception e) {
            return Result.error("知识库搜索失败: " + e.getMessage());
        }
    }
}
