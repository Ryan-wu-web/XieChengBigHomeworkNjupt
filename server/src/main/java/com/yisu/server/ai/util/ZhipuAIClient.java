package com.yisu.server.ai.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.yisu.server.ai.config.AIConfig;
import com.yisu.server.entity.Hotel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ZhipuAIClient {
    @Autowired
    private AIConfig aiConfig;
    private OkHttpClient okHttpClient;

    @PostConstruct
    public void init() {
        this.okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(aiConfig.getZhipu().getTimeout(), TimeUnit.MILLISECONDS)
                .readTimeout(aiConfig.getZhipu().getTimeout(), TimeUnit.MILLISECONDS)
                .writeTimeout(aiConfig.getZhipu().getTimeout(), TimeUnit.MILLISECONDS)
                .build();
    }

    public String chat(String userMessage, List<Hotel> hotels) {
        String systemPrompt = buildSystemPrompt(hotels);
        ZhipuRequest request = new ZhipuRequest();
        request.setModel(aiConfig.getZhipu().getModel());

        List<Message> messages = new ArrayList<>();
        messages.add(new Message("system", systemPrompt));
        messages.add(new Message("user", userMessage));
        request.setMessages(messages);

        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON.toJSONString(request), mediaType);
        Request httpRequest = new Request.Builder()
                .url(aiConfig.getZhipu().getEndpoint())
                .header("Authorization", "Bearer " + aiConfig.getZhipu().getApiKey())
                .post(body)
                .build();

        try (Response response = okHttpClient.newCall(httpRequest).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                ZhipuResponse zhipuResponse = JSON.parseObject(responseBody, ZhipuResponse.class);
                if (zhipuResponse.getChoices() != null && !zhipuResponse.getChoices().isEmpty()) {
                    return zhipuResponse.getChoices().get(0).getMessage().getContent();
                }
            }
            log.error("智谱AI API调用失败: " + response);
            if (response.body() != null) {
                log.error("智谱AI API响应内容: " + response.body().string());
            }
            return "抱歉，我暂时无法回答你的问题，请稍后再试。";
        } catch (Exception e) {
            log.error("智谱AI API调用异常", e);
            return "抱歉，系统出现异常，请稍后再试。";
        }
    }

    private String buildSystemPrompt(List<Hotel> hotels) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的酒店预订助手，负责回答用户关于酒店的问题。\n");
        prompt.append("请遵循以下规则：\n");
        prompt.append("1. 回答要简洁明了，不超过200字\n");
        prompt.append("2. 如果提到具体酒店，请确保信息准确\n");
        prompt.append("3. 如果不确定，请诚实地告诉用户\n");
        prompt.append("4. 使用友好、专业的语气\n");
        prompt.append("5. 只回答与酒店相关的问题，其他问题请礼貌拒绝\n");

        if (hotels != null && !hotels.isEmpty()) {
            prompt.append("\n当前可用的酒店信息：\n");
            for (Hotel hotel : hotels) {
                prompt.append("- 酒店名称：").append(hotel.getName()).append("\n");
                prompt.append("  地址：").append(hotel.getAddress()).append("\n");
                prompt.append("  星级：").append(hotel.getStarRating()).append("星\n");
                if (hotel.getMinPrice() != null) {
                    prompt.append("  最低价格：¥").append(hotel.getMinPrice()).append("起\n");
                }
                if (hotel.getFacilities() != null) {
                    prompt.append("  设施：").append(hotel.getFacilities()).append("\n");
                }
                if (hotel.getSurroundings() != null) {
                    prompt.append("  周边：").append(hotel.getSurroundings()).append("\n");
                }
                prompt.append("\n");
            }
        }

        return prompt.toString();
    }

    public static class ZhipuRequest {
        private String model;
        private List<Message> messages;
        private boolean stream = false;

        public ZhipuRequest() {
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }

        public boolean isStream() {
            return stream;
        }

        public void setStream(boolean stream) {
            this.stream = stream;
        }
    }

    public static class Message {
        private String role;
        private String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

    public static class ZhipuResponse {
        private List<Choice> choices;

        public List<Choice> getChoices() {
            return choices;
        }

        public void setChoices(List<Choice> choices) {
            this.choices = choices;
        }
    }

    public static class Choice {
        private Message message;

        public Message getMessage() {
            return message;
        }

        public void setMessage(Message message) {
            this.message = message;
        }
    }
}