package com.yisu.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yisu.server.entity.Banner;
import java.util.List;

public interface BannerService extends IService<Banner> {
    List<Banner> getActiveBanners(); // 获取启用的Banner列表（供Android端使用）
}
