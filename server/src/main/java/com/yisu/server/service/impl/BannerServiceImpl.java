package com.yisu.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yisu.server.entity.Banner;
import com.yisu.server.mapper.BannerMapper;
import com.yisu.server.service.BannerService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Override
    public List<Banner> getActiveBanners() {
        QueryWrapper<Banner> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1); // 只获取启用的
        wrapper.orderByAsc("sort_order"); // 按排序顺序升序
        return list(wrapper);
    }
}
