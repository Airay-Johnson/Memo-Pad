package org.example.controller;

import jakarta.annotation.Resource;
import org.example.common.Result;
import org.example.entity.AiRequest;
import org.example.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private AiService aiService;

    @PostMapping("/chat")
    public Result chat(@RequestBody AiRequest req) {
        try {
            String rawJson = aiService.callAi(req.getText(), req.getAction());
            return Result.success(rawJson);
        } catch (Exception e) {
            return Result.error("AI 调用失败: " + e.getMessage());
        }
    }
}