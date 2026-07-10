package org.example.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import okhttp3.*;
import org.example.common.AiConfig;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class AiService {

    @Resource
    private AiConfig aiConfig;

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String callAi(String text, String action, String userRequest) throws IOException {
        if (aiConfig.getApiKey() == null || aiConfig.getApiKey().contains("your-api-key")) {
            throw new IOException("AI 功能尚未配置：请在 application.yml 中填写真实的 api-key");
        }
        String prompt = buildPrompt(text, action, userRequest);
        return callAiRaw("你是一个专业的文字助手。", prompt);
    }

    private String buildPrompt(String text, String action, String userRequest) {
        if (userRequest != null && !userRequest.trim().isEmpty()) {
            return "以下是用户写的原始文字：\n" + text
                   + "\n\n用户的需求：\n" + userRequest
                   + "\n\n请根据用户的需求处理上面的文字，直接返回处理结果，不要加任何解释。";
        }
        return switch (action) {
            case "polish" -> "请帮我润色以下文字，使其更流畅、更有文采，直接返回润色后的文字，不要加任何解释：\n\n" + text;
            case "summarize" -> "请帮我用几句话总结以下内容，直接返回总结，不要加任何解释：\n\n" + text;
            case "expand" -> "请帮我扩写以下文字，丰富内容和细节，直接返回扩写后的文字，不要加任何解释：\n\n" + text;
            case "translate" -> "请将以下文字翻译成英文，直接返回翻译结果，不要加任何解释：\n\n" + text;
            default -> "请帮我润色以下文字：\n\n" + text;
        };
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
