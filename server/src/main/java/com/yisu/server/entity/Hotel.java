package com.yisu.server.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@TableName("hotel")
public class Hotel {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String nameEn; // 英文名称
    private String address;
    private BigDecimal latitude; // 纬度
    private BigDecimal longitude; // 经度
    private String starRating; // 1-5星
    private String description;
    private String surroundings; // 周边环境：景点、交通、商场
    private String promotionInfo; // 优惠信息
    private String facilities; // 逗号分隔，如：Wifi,Parking
    private String tags; // 逗号分隔，快捷标签：亲子,豪华,免费停车
    private LocalDate openDate;
    private Integer status; // 0:审核中, 1:已发布, 2:下线, 3:审核不通过
    private Integer merchantId;
    private String images; // JSON or comma separated
    private String coverImage; // 封面图
    private BigDecimal minPrice;
    private String rejectReason; // 审核不通过原因
    private String city; // 城市，用于筛选
}
