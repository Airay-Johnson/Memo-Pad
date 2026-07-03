package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import okhttp3.*;
import org.example.common.AiConfig;
import org.example.common.ToolResult;
import org.example.entity.Conversation;
import org.example.entity.ToolType;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
public class AiService {

    @Resource
    private AiConfig aiConfig;

    @Resource
    private ToolService toolService;

    @Resource
    private ConversationService conversationService;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ===== 单次调用（第 1/2 层）=====

    public String callAi(String text, String action, String userRequest) throws IOException {
        if (aiConfig.getApiKey() == null || aiConfig.getApiKey().contains("your-api-key")) {
            throw new IOException("AI 功能尚未配置：请在 application.yml 中填写真实的 api-key");
        }
        String prompt = buildPrompt(text, action, userRequest);
        return callAiRaw("你是一个专业的文字助手。", prompt);
    }

    private String buildPrompt(String text, String action, String userRequest) {
        if (userRequest != null && !userRequest.trim().isEmpty()) {
            return "以下是用户写的原始文字：\n" + text +
                   "\n\n用户的需求：\n" + userRequest +
                   "\n\n请根据用户的需求处理上面的文字，直接返回处理结果，不要加任何解释。";
        }
        return switch (action) {
            case "polish" -> "请帮我润色以下文字，使其更流畅、更有文采，直接返回润色后的文字，不要加任何解释：\n\n" + text;
            case "summarize" -> "请帮我用几句话总结以下内容，直接返回总结，不要加任何解释：\n\n" + text;
            case "expand" -> "请帮我扩写以下文字，丰富内容和细节，直接返回扩写后的文字，不要加任何解释：\n\n" + text;
            case "translate" -> "请将以下文字翻译成英文，直接返回翻译结果，不要加任何解释：\n\n" + text;
            default -> "请帮我润色以下文字：\n\n" + text;
        };
    }

    // ===== 第 3/4 层：Agent 循环 + 持久化记忆 =====

    /**
     * Agent 模式（带记忆）。
     * @param userMessage 用户说的原始需求
     * @param sessionId   会话 ID（前端生成 UUID），传 null 则不使用记忆
     */
    public String agentRun(String userMessage, String sessionId) throws IOException {
        if (aiConfig.getApiKey() == null || aiConfig.getApiKey().contains("your-api-key")) {
            return "AI 功能尚未配置：请在 application.yml 中填写真实的 api-key。\n"
                 + "获取地址：https://dashscope.console.aliyun.com/apiKey";
        }

        String systemPrompt = buildSystemPrompt();
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // Layer 4：从数据库加载历史对话
        boolean useMemory = sessionId != null && !sessionId.isEmpty();
        if (useMemory) {
            List<Conversation> history = conversationService.loadHistory(sessionId);
            for (Conversation c : history) {
                messages.add(Map.of("role", c.getRole(), "content", c.getContent()));
            }
        }

        // 本次用户消息
        messages.add(Map.of("role", "user", "content", userMessage));
        if (useMemory) {
            conversationService.save(sessionId, "user", userMessage);
        }

        int maxSteps = 12;
        int step = 0;

        while (step < maxSteps) {
            step++;
            String aiResponse = callAiWithHistory(messages);
            messages.add(Map.of("role", "assistant", "content", aiResponse));
            if (useMemory) {
                conversationService.save(sessionId, "assistant", aiResponse);
            }

            try {
                Map<String, Object> parsed = objectMapper.readValue(aiResponse, Map.class);

                if (parsed.containsKey("done")) {
                    return (String) parsed.get("answer");
                }

                if (parsed.containsKey("tool")) {
                    String toolName = (String) parsed.get("tool");
                    @SuppressWarnings("unchecked")
                    Map<String, String> params = (Map<String, String>) parsed.getOrDefault("params", Map.of());

                    ToolResult result = toolService.execute(toolName, params);
                    String toolFeedback = result.isSuccess()
                        ? "工具执行成功，结果：\n" + result.getContent()
                        : "工具执行失败：" + result.getContent();

                    messages.add(Map.of("role", "user", "content", toolFeedback));
                    if (useMemory) {
                        conversationService.save(sessionId, "tool", toolFeedback);
                    }
                    continue;
                }

                messages.add(Map.of("role", "user",
                    "content", "请按照约定的格式返回：任务完成时返回 {\"done\":true,\"answer\":\"...\"}，需要工具时返回 {\"tool\":\"...\",\"params\":{...}}"));

            } catch (Exception e) {
                return aiResponse;
            }
        }

        return "任务步骤过多，已终止。请尝试更简单的需求。";
    }

    // ===== 第 5 层：先规划再执行 =====

    /**
     * 规划模式：先让 AI 把复杂任务拆成步骤列表，再逐步执行。
     */
    public String agentRunPlan(String userMessage, String sessionId) throws IOException {
        if (aiConfig.getApiKey() == null || aiConfig.getApiKey().contains("your-api-key")) {
            return "AI 功能尚未配置：请在 application.yml 中填写真实的 api-key。\n"
                 + "获取地址：https://dashscope.console.aliyun.com/apiKey";
        }

        String systemPrompt = buildSystemPrompt();
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // 加载历史
        boolean useMemory = sessionId != null && !sessionId.isEmpty();
        if (useMemory) {
            for (Conversation c : conversationService.loadHistory(sessionId)) {
                messages.add(Map.of("role", c.getRole(), "content", c.getContent()));
            }
        }

        // === 阶段 1：让 AI 出计划 ===
        String planPrompt = "用户的需求是：「" + userMessage + "」\n\n"
            + "请先分析这个需求，拆分成可执行的步骤，以 JSON 格式返回你的执行计划：\n"
            + "{\"plan\":[\"第1步的描述\",\"第2步的描述\",...],\"thinking\":\"你的分析思路\"}\n\n"
            + "例如用户说「搜索工作笔记并整理成汇总」：\n"
            + "{\"plan\":[\"SEARCH_NOTE 搜索'工作'\",\"READ_NOTE 逐个读取搜索结果\",\"CREATE_NOTE 创建汇总笔记\"],\"thinking\":\"需要先搜索、再读取、最后汇总\"}";

        messages.add(Map.of("role", "user", "content", planPrompt));
        if (useMemory) conversationService.save(sessionId, "user", planPrompt);

        String planResponse = callAiWithHistory(messages);
        messages.add(Map.of("role", "assistant", "content", planResponse));
        if (useMemory) conversationService.save(sessionId, "assistant", planResponse);

        // 解析计划
        String[] steps;
        try {
            Map<String, Object> planParsed = objectMapper.readValue(planResponse, Map.class);
            @SuppressWarnings("unchecked")
            List<String> planList = (List<String>) planParsed.get("plan");
            steps = planList.toArray(new String[0]);
        } catch (Exception e) {
            return "AI 规划失败，尝试用普通模式执行。\n\n规划响应：\n" + planResponse;
        }

        // === 阶段 2：逐步执行 ===
        StringBuilder stepLog = new StringBuilder("[执行计划] 共 " + steps.length + " 步：\n");
        for (int i = 0; i < steps.length; i++) {
            stepLog.append("  ").append(i + 1).append(". ").append(steps[i]).append("\n");
        }
        stepLog.append("\n--- 开始执行 ---\n");

        int maxSteps = steps.length + 3; // 每步可能还需子步骤
        int step = 0;
        int currentPlanStep = 0;

        while (step < maxSteps && currentPlanStep < steps.length) {
            step++;

            String execPrompt;
            if (step == 1) {
                execPrompt = "现在开始执行计划。第 1 步：" + steps[0];
            } else {
                execPrompt = "继续执行。当前应该执行第 " + (currentPlanStep + 1) + " 步：" + steps[currentPlanStep]
                    + "（如果已完成则跳到下一步）";
            }

            messages.add(Map.of("role", "user", "content", execPrompt));
            if (useMemory) conversationService.save(sessionId, "user", execPrompt);

            String aiResponse = callAiWithHistory(messages);
            messages.add(Map.of("role", "assistant", "content", aiResponse));
            if (useMemory) conversationService.save(sessionId, "assistant", aiResponse);

            try {
                Map<String, Object> parsed = objectMapper.readValue(aiResponse, Map.class);

                if (parsed.containsKey("done")) {
                    // AI 认为这一步完成了，进入下一步
                    stepLog.append("\n>> 第 ").append(currentPlanStep + 1).append(" 步完成：")
                           .append(parsed.get("answer")).append("\n");
                    currentPlanStep++;
                    continue;
                }

                if (parsed.containsKey("tool")) {
                    String toolName = (String) parsed.get("tool");
                    @SuppressWarnings("unchecked")
                    Map<String, String> params = (Map<String, String>) parsed.getOrDefault("params", Map.of());

                    ToolResult result = toolService.execute(toolName, params);
                    String toolFeedback = result.isSuccess()
                        ? "工具执行成功，结果：\n" + result.getContent()
                        : "工具执行失败：" + result.getContent();

                    stepLog.append("  [").append(toolName).append("] -> ")
                           .append(result.isSuccess() ? "成功" : "失败").append("\n");

                    messages.add(Map.of("role", "user", "content", toolFeedback));
                    if (useMemory) conversationService.save(sessionId, "tool", toolFeedback);
                    continue;
                }

                // 判断是否该进入下一步
                currentPlanStep++;

            } catch (Exception e) {
                stepLog.append("\n[!] 第 ") .append(currentPlanStep + 1).append(" 步异常，尝试继续\n");
                currentPlanStep++;
            }
        }

        // === 阶段 3：AI 总结 ===
        String summarizePrompt = "所有步骤已执行完毕。请根据以上执行结果，用自然语言向用户做一个简洁的总结汇报。\n"
            + "返回格式：{\"done\":true,\"answer\":\"总结内容\"}";

        messages.add(Map.of("role", "user", "content", summarizePrompt));
        if (useMemory) conversationService.save(sessionId, "user", summarizePrompt);

        String finalResponse = callAiWithHistory(messages);
        if (useMemory) conversationService.save(sessionId, "assistant", finalResponse);

        try {
            Map<String, Object> finalParsed = objectMapper.readValue(finalResponse, Map.class);
            if (finalParsed.containsKey("answer")) {
                return stepLog + "\n--- 最终结果 ---\n" + finalParsed.get("answer");
            }
        } catch (Exception ignored) {}

        return stepLog + "\n--- 最终结果 ---\n" + finalResponse;
    }

    // ===== 会话管理（Layer 4 配套）=====

    /** 清除某个会话的记忆 */
    public void clearMemory(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            conversationService.clear(sessionId);
        }
    }

    // ===== System Prompt =====

    private String buildSystemPrompt() {
        StringBuilder toolsDesc = new StringBuilder();
        for (ToolType tool : ToolType.values()) {
            toolsDesc.append("- ").append(tool.name()).append(": ").append(tool.getDescription());
            if (tool.getParamNames() != null && tool.getParamNames().length > 0) {
                toolsDesc.append("，参数：").append(String.join("、", tool.getParamNames()));
            }
            toolsDesc.append("\n");
        }

        return """
            你是 Memo Pad 的智能笔记助手。用户通过自然语言和你对话，你负责帮他们管理笔记和分组。

            ## 你的能力（工具）
            %s
            ## 工具调用格式
            需要调用工具时，返回纯 JSON，不要加任何 markdown 标记：
            {"tool":"工具名","params":{"参数名":"参数值"}}

            示例：
            - 用户说"看看我有哪些笔记" → {"tool":"LIST_NOTES"}
            - 用户说"帮我新建一篇日记" → {"tool":"CREATE_NOTE","params":{"title":"日记","content":""}}
            - 用户说"搜索关于会议的内容" → {"tool":"SEARCH_NOTE","params":{"keyword":"会议"}}
            - 用户说"删掉第三条笔记" → {"tool":"DELETE_NOTE","params":{"noteId":"3"}}
            - 用户说"把第一条笔记改一下内容" → {"tool":"UPDATE_NOTE","params":{"noteId":"1","content":"新内容"}}
            - 用户说"把笔记移到学习分组" → {"tool":"MOVE_NOTE","params":{"noteId":"1","groupId":"2"}}

            ## 任务完成格式
            任务完成后，返回纯 JSON：
            {"done":true,"answer":"给用户的最终回复"}

            ## 回答规则
            1. 每次只调一个工具，拿到结果后再决定下一步
            2. 需要先查看再操作时（如"删掉第三条"），先 LIST_NOTES 拿到 ID，再执行删除
            3. 回答简洁友好，用中文，适当使用 emoji
            4. 对不确定的操作（如删除），先展示内容让用户确认，再执行
            5. 如果用户的需求模糊（如"整理一下笔记"），主动询问具体想要怎么做
            """.formatted(toolsDesc);
    }

    // ===== API 调用 =====

    private String callAiWithHistory(List<Map<String, String>> messages) throws IOException {
        try {
            String messagesJson = objectMapper.writeValueAsString(messages);
            String jsonBody = "{\"model\":\"" + aiConfig.getModel() + "\",\"messages\":" + messagesJson + "}";

            Request request = new Request.Builder()
                    .url(aiConfig.getApiUrl())
                    .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new IOException("API 请求失败，HTTP " + response.code() + "：" + response.body().string());
                }
                String raw = response.body().string();
                return extractContent(raw);
            }
        } catch (IOException e) {
            throw e;
        }
    }

    private String callAiRaw(String systemPrompt, String userPrompt) throws IOException {
        String jsonBody = """
            {
                "model": "%s",
                "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"}
                ]
            }
            """.formatted(
                aiConfig.getModel(),
                systemPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"),
                userPrompt.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
        );

        Request request = new Request.Builder()
                .url(aiConfig.getApiUrl())
                .addHeader("Authorization", "Bearer " + aiConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(jsonBody, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API 请求失败，HTTP " + response.code() + "：" + response.body().string());
            }
            return extractContent(response.body().string());
        }
    }

    private String extractContent(String rawJson) {
        try {
            Map<String, Object> map = objectMapper.readValue(rawJson, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) map.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            return rawJson;
        }
    }
}
