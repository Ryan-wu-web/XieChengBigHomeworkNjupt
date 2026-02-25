package com.yisu.server.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yisu.server.ai.service.AIService;
import com.yisu.server.ai.util.ZhipuAIClient;
import com.yisu.server.entity.Hotel;
import com.yisu.server.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIServiceImpl implements AIService {
    @Autowired
    private ZhipuAIClient zhipuAIClient;
    @Autowired
    private HotelService hotelService;

    @Override
    public String chat(String userMessage, boolean hotelQuery) {
        List<Hotel> hotels = null;
        if (hotelQuery) {
            hotels = hotelService.list(new QueryWrapper<Hotel>().eq("status", 1));
        }
        return zhipuAIClient.chat(userMessage, hotels);
    }
}