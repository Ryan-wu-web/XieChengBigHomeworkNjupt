package com.yisu.server.controller;

import com.yisu.server.common.Result;
import com.yisu.server.service.HotelFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorite")
@CrossOrigin
public class HotelFavoriteController {

    @Autowired
    private HotelFavoriteService favoriteService;

    // 收藏/取消收藏
    @PostMapping("/toggle")
    public Result<Map<String, Object>> toggleFavorite(@RequestParam Integer hotelId,
                                                       @RequestParam Integer userId) {
        boolean isFavorited = favoriteService.toggleFavorite(hotelId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorited", isFavorited);
        result.put("message", isFavorited ? "收藏成功" : "取消收藏成功");
        return Result.success(result);
    }

    // 检查是否已收藏
    @GetMapping("/check")
    public Result<Boolean> checkFavorite(@RequestParam Integer hotelId,
                                         @RequestParam Integer userId) {
        boolean isFavorited = favoriteService.isFavorited(hotelId, userId);
        return Result.success(isFavorited);
    }

    // 获取酒店收藏数量
    @GetMapping("/count")
    public Result<Integer> getFavoriteCount(@RequestParam Integer hotelId) {
        Integer count = favoriteService.getFavoriteCountByHotelId(hotelId);
        return Result.success(count);
    }

    // 获取用户收藏列表
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> getFavorites(@RequestParam Integer userId) {
        List<Map<String, Object>> list = favoriteService.getFavoritesByUserId(userId);
        return Result.success(list);
    }
}
