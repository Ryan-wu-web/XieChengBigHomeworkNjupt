package com.yisu.server.ai.controller;

import com.yisu.server.ai.service.AIService;
import com.yisu.server.common.Result;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AIController {
    @Autowired
    private AIService aiService;

    @PostMapping("/chat")
    public Result<String> chat(@RequestBody AIChatRequest request) {
        try {
            String answer = aiService.chat(request.getMessage(), request.isHotelQuery());
            return Result.success(answer);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("对话失败: " + e.getMessage());
        }
    }

    @Data
    private static class AIChatRequest {
        private String message;
        private boolean hotelQuery = true;
    }
}