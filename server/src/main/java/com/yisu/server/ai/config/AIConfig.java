package com.yisu.server.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {
    private Zhipu zhipu = new Zhipu();

    @Data
    public static class Zhipu {
        private String apiKey;
        private String model = "glm-4";
        private String endpoint = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
        private int timeout = 30000;
    }
}