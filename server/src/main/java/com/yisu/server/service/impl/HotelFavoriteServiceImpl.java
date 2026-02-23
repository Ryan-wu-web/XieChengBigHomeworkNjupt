package com.yisu.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yisu.server.entity.HotelFavorite;
import com.yisu.server.mapper.HotelFavoriteMapper;
import com.yisu.server.service.HotelFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class HotelFavoriteServiceImpl extends ServiceImpl<HotelFavoriteMapper, HotelFavorite> implements HotelFavoriteService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean toggleFavorite(Integer hotelId, Integer userId) {
        QueryWrapper<HotelFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("hotel_id", hotelId);
        wrapper.eq("user_id", userId);
        HotelFavorite favorite = getOne(wrapper);
        
        if (favorite != null) {
            // 已收藏，取消收藏
            removeById(favorite.getId());
            return false;
        } else {
            // 未收藏，添加收藏
            HotelFavorite newFavorite = new HotelFavorite();
            newFavorite.setHotelId(hotelId);
            newFavorite.setUserId(userId);
            save(newFavorite);
            return true;
        }
    }

    @Override
    public boolean isFavorited(Integer hotelId, Integer userId) {
        QueryWrapper<HotelFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("hotel_id", hotelId);
        wrapper.eq("user_id", userId);
        return count(wrapper) > 0;
    }

    @Override
    public Integer getFavoriteCountByHotelId(Integer hotelId) {
        QueryWrapper<HotelFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("hotel_id", hotelId);
        return Math.toIntExact(count(wrapper));
    }

    @Override
    public List<Map<String, Object>> getFavoritesByUserId(Integer userId) {
        String sql = "SELECT h.* FROM hotel h " +
                     "INNER JOIN hotel_favorite f ON h.id = f.hotel_id " +
                     "WHERE f.user_id = ? AND h.deleted = 0";
        return jdbcTemplate.queryForList(sql, userId);
    }
}
