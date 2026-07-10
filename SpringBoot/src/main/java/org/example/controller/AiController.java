package org.example.controller;

import dev.langchain4j.service.TokenStream;
import jakarta.annotation.Resource;
import org.example.agent.MemoAgent;
import org.example.common.Result;
import org.example.common.UserContext;
import org.example.entity.AiRequest;
import org.example.guard.SecurityGuard;
import org.example.rag.KnowledgeBaseService;
import org.example.service.AiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;

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

    // ===== 文字处理（保留旧版，功能简单不需要 Agent 循环）=====

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

    // ===== Agent 同步模式 =====

    @PostMapping("/agent")
    public Result agent(@RequestBody Map<String, String> req) {
        String message = req.get("message");
        if (message == null || message.trim().isEmpty()) {
            return Result.error("请输入需求");
        }

        String block = securityGuard.validateInput(message);
        if (block != null) return Result.error(block);

        try {
            String sessionId = req.getOrDefault("sessionId", "default");
            String answer = memoAgent.chat(sessionId, message);
            answer = securityGuard.sanitizeOutput(answer);
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error("Agent 执行失败: " + e.getMessage());
        }
    }

    // ===== Agent 流式模式 (SSE) =====

    @PostMapping(value = "/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter agentStream(@RequestBody Map<String, String> req) {
        String message = req.get("message");
        SseEmitter emitter = new SseEmitter(180000L);

        if (message == null || message.trim().isEmpty()) {
            try { emitter.send(SseEmitter.event().data("请输入需求")); emitter.complete(); } catch (IOException ignored) {}
            return emitter;
        }

        String block = securityGuard.validateInput(message);
        if (block != null) {
            try { emitter.send(SseEmitter.event().data(block)); emitter.complete(); } catch (IOException ignored) {}
            return emitter;
        }

        String sessionId = req.getOrDefault("sessionId", "default-stream");
        Integer userId = UserContext.getUserId();
        TokenStream tokenStream = memoAgent.chatStream(sessionId, message);

        tokenStream
            .beforeToolExecution(before -> {
                if (userId != null) UserContext.setUserId(userId);
            })
            .onToolExecuted(tool -> {
                try {
                    String name = tool.request().name();
                    emitter.send(SseEmitter.event().name("tool").data(name));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            })
            .onPartialResponse(token -> {
                try {
                    emitter.send(SseEmitter.event().data(securityGuard.sanitizeOutput(token)));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            })
            .onCompleteResponse(response -> emitter.complete())
            .onError(emitter::completeWithError)
            .start();

        return emitter;
    }

    // ===== 规划模式 =====

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

    // ===== RAG 知识库 =====

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
