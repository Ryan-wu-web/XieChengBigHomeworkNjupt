-- 数据库增量升级脚本（按顺序执行；若字段已存在可跳过对应语句）

-- 1) 图片字段
ALTER TABLE `hotel` ADD COLUMN `cover_image` varchar(255) NULL COMMENT '封面图' AFTER `images`;

-- 2) 多语言与展示信息字段
ALTER TABLE `hotel` ADD COLUMN `name_en` varchar(100) NULL COMMENT '英文名称' AFTER `name`;
ALTER TABLE `hotel` ADD COLUMN `surroundings` text NULL COMMENT '周边环境' AFTER `description`;
ALTER TABLE `hotel` ADD COLUMN `promotion_info` text NULL COMMENT '优惠信息' AFTER `surroundings`;

-- 3) 快捷标签（用于前台筛选）
ALTER TABLE `hotel` ADD COLUMN `tags` varchar(255) NULL COMMENT '快捷标签(逗号分隔)' AFTER `facilities`;

-- 4) 房型（room_type）字段补全：用于“房型详情”展示与上架管理
ALTER TABLE `room_type` ADD COLUMN `status` int NULL DEFAULT 1 COMMENT '0:下架 1:上架' AFTER `stock`;
ALTER TABLE `room_type` ADD COLUMN `tags` varchar(255) NULL COMMENT '快捷标签(逗号分隔)' AFTER `status`;
ALTER TABLE `room_type` ADD COLUMN `area_range` varchar(50) NULL COMMENT '面积范围(如35-40㎡)' AFTER `tags`;
ALTER TABLE `room_type` ADD COLUMN `floor_range` varchar(50) NULL COMMENT '楼层范围(如6-8层)' AFTER `area_range`;
ALTER TABLE `room_type` ADD COLUMN `wifi_free` tinyint NULL DEFAULT 1 COMMENT 'WiFi是否免费 0/1' AFTER `floor_range`;
ALTER TABLE `room_type` ADD COLUMN `window_flag` tinyint NULL DEFAULT 1 COMMENT '是否有窗 0/1' AFTER `wifi_free`;
ALTER TABLE `room_type` ADD COLUMN `no_smoking` tinyint NULL DEFAULT 1 COMMENT '是否禁烟 0/1' AFTER `window_flag`;
ALTER TABLE `room_type` ADD COLUMN `bed_count` int NULL DEFAULT 1 COMMENT '床数量' AFTER `no_smoking`;
ALTER TABLE `room_type` ADD COLUMN `bed_type` varchar(50) NULL COMMENT '床型(特大床/双床等)' AFTER `bed_count`;
ALTER TABLE `room_type` ADD COLUMN `bed_size` varchar(20) NULL COMMENT '床尺寸(如1.81米)' AFTER `bed_type`;
ALTER TABLE `room_type` ADD COLUMN `extra_bed_allowed` tinyint NULL DEFAULT 0 COMMENT '是否可加床 0/1' AFTER `bed_size`;
ALTER TABLE `room_type` ADD COLUMN `breakfast_count` int NULL DEFAULT 0 COMMENT '包含早餐份数' AFTER `extra_bed_allowed`;
ALTER TABLE `room_type` ADD COLUMN `breakfast_type` varchar(50) NULL COMMENT '早餐类型(自助餐等)' AFTER `breakfast_count`;
ALTER TABLE `room_type` ADD COLUMN `breakfast_dishes` varchar(255) NULL COMMENT '早餐菜品(逗号分隔)' AFTER `breakfast_type`;
ALTER TABLE `room_type` ADD COLUMN `breakfast_time` varchar(50) NULL COMMENT '早餐时间(如07:30-09:30)' AFTER `breakfast_dishes`;
ALTER TABLE `room_type` ADD COLUMN `breakfast_extra_price` decimal(10,2) NULL DEFAULT 0.00 COMMENT '早餐成人加价(元/人)' AFTER `breakfast_time`;
ALTER TABLE `room_type` ADD COLUMN `member_benefits` text NULL COMMENT '会员权益(JSON/文本)' AFTER `breakfast_extra_price`;
ALTER TABLE `room_type` ADD COLUMN `cancel_policy` text NULL COMMENT '政策与服务(JSON/文本)' AFTER `member_benefits`;

-- 5) 用户表添加邮箱字段
ALTER TABLE `sys_user` ADD COLUMN `email` varchar(100) NULL COMMENT '邮箱' AFTER `phone`;

-- 6) 创建酒店点评表
CREATE TABLE IF NOT EXISTS `hotel_review` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hotel_id` int NOT NULL COMMENT '酒店ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `rating` int NOT NULL DEFAULT 5 COMMENT '评分 1-5',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '点评内容',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '点评图片(JSON数组)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_hotel_id` (`hotel_id`) USING BTREE,
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic COMMENT = '酒店点评表';

-- 7) 创建酒店收藏表
CREATE TABLE IF NOT EXISTS `hotel_favorite` (
  `id` int NOT NULL AUTO_INCREMENT,
  `hotel_id` int NOT NULL COMMENT '酒店ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_hotel_user` (`hotel_id`, `user_id`) USING BTREE COMMENT '同一用户不能重复收藏同一酒店',
  INDEX `idx_user_id` (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic COMMENT = '酒店收藏表';

-- 8) 用户表添加状态字段
ALTER TABLE `sys_user` ADD COLUMN `status` int NULL DEFAULT 0 COMMENT '0:正常, 1:禁用' AFTER `role`;

-- 9) 酒店表添加经纬度字段（用于地图定位）
ALTER TABLE `hotel` ADD COLUMN `latitude` decimal(10, 7) NULL COMMENT '纬度' AFTER `address`;
ALTER TABLE `hotel` ADD COLUMN `longitude` decimal(10, 7) NULL COMMENT '经度' AFTER `latitude`;

-- 10) 创建首页Banner广告表
CREATE TABLE IF NOT EXISTS `banner` (
  `id` int NOT NULL AUTO_INCREMENT,
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Banner标题',
  `image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'Banner图片URL',
  `hotel_id` int NOT NULL COMMENT '关联酒店ID（点击跳转到该酒店详情）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序（数字越小越靠前）',
  `status` int NULL DEFAULT 1 COMMENT '0:禁用, 1:启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_hotel_id` (`hotel_id`) USING BTREE,
  INDEX `idx_status` (`status`) USING BTREE,
  INDEX `idx_sort_order` (`sort_order`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic COMMENT = '首页Banner广告表';
