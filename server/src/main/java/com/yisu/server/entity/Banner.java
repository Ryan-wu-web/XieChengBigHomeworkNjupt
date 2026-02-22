package com.yisu.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("banner")
public class Banner {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title; // Banner标题
    private String imageUrl; // Banner图片URL
    private Integer hotelId; // 关联酒店ID
    private Integer sortOrder; // 排序顺序
    private Integer status; // 0:禁用, 1:启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
