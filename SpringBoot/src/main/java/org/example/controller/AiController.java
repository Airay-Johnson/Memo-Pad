package org.example.controller;

import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.entity.AiRequest;
import org.example.service.AiService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    /** 第 1/2 层：单次文字处理 */
    @PostMapping("/chat")
    public Result chat(@RequestBody AiRequest req) {
        try {
            String rawJson = aiService.callAi(req.getText(), req.getAction(), req.getUserRequest());
            return Result.success(rawJson);
        } catch (Exception e) {
            return Result.error("AI 调用失败: " + e.getMessage());
        }
    }

    /** 第 3/4 层：Agent + 记忆 */
    @PostMapping("/agent")
    public Result agent(@RequestBody Map<String, String> req) {
        try {
            String message = req.get("message");
            if (message == null || message.trim().isEmpty()) {
                return Result.error("请输入需求");
            }
            String sessionId = req.getOrDefault("sessionId", "");
            String answer = aiService.agentRun(message, sessionId);
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error("Agent 执行失败: " + e.getMessage());
        }
    }

    /** 第 5 层：规划模式 */
    @PostMapping("/agent/plan")
    public Result agentPlan(@RequestBody Map<String, String> req) {
        try {
            String message = req.get("message");
            if (message == null || message.trim().isEmpty()) {
                return Result.error("请输入需求");
            }
            String sessionId = req.getOrDefault("sessionId", "");
            String answer = aiService.agentRunPlan(message, sessionId);
            return Result.success(answer);
        } catch (Exception e) {
            return Result.error("规划执行失败: " + e.getMessage());
        }
    }

    /** 清除会话记忆 */
    @DeleteMapping("/memory")
    public Result clearMemory(@RequestBody Map<String, String> req) {
        String sessionId = req.get("sessionId");
        aiService.clearMemory(sessionId);
        return Result.success("记忆已清除");
    }
}
