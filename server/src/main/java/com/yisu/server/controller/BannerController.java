package com.yisu.server.controller;

import com.yisu.server.common.Result;
import com.yisu.server.entity.Banner;
import com.yisu.server.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banner")
@CrossOrigin
public class BannerController {

    @Autowired
    private BannerService bannerService;

    // Android端：获取启用的Banner列表
    @GetMapping("/list")
    public Result<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return Result.success(banners);
    }

    // 管理员：获取所有Banner列表（包括禁用的）
    @GetMapping("/admin/list")
    public Result<List<Banner>> getAllBanners() {
        List<Banner> banners = bannerService.list();
        banners.sort((a, b) -> {
            if (a.getSortOrder() == null) return 1;
            if (b.getSortOrder() == null) return -1;
            return a.getSortOrder().compareTo(b.getSortOrder());
        });
        return Result.success(banners);
    }

    // 管理员：添加Banner
    @PostMapping("/admin/add")
    public Result<Object> addBanner(@RequestBody Banner banner) {
        bannerService.save(banner);
        return Result.success();
    }

    // 管理员：更新Banner
    @PutMapping("/admin/update")
    public Result<Object> updateBanner(@RequestBody Banner banner) {
        bannerService.updateById(banner);
        return Result.success();
    }

    // 管理员：删除Banner
    @DeleteMapping("/admin/{id}")
    public Result<Object> deleteBanner(@PathVariable Integer id) {
        bannerService.removeById(id);
        return Result.success();
    }
}
