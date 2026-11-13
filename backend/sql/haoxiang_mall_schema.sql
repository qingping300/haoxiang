-- haoxiang mall MySQL 8 schema and local development seed.
-- Run on a fresh haoxiang_mall database; inserts use fixed IDs and are not rerunnable.
-- Sample prices are illustrative. Existing image paths are retained for later replacement.
-- Local test admin: admin / 123456.

SET NAMES utf8mb4;

-- 01. 建表：cdk_info
CREATE TABLE IF NOT EXISTS `cdk_info`  (
  `cdk_id` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `cdk_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'cdk码',
  `create_time` datetime NOT NULL,
  `create_user` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`cdk_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- 02. 建表：haoxiang_mall_admin_user
CREATE TABLE IF NOT EXISTS `haoxiang_mall_admin_user`  (
  `admin_user_id` int NOT NULL AUTO_INCREMENT COMMENT '管理员id',
  `login_user_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '管理员登陆名称',
  `login_password` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '管理员登陆密码',
  `nick_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '管理员显示昵称',
  `locked` tinyint NULL DEFAULT 0 COMMENT '是否锁定 0未锁定 1已锁定无法登陆',
  PRIMARY KEY (`admin_user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 03. 建表：haoxiang_mall_carousel
CREATE TABLE IF NOT EXISTS `haoxiang_mall_carousel`  (
  `carousel_id` int NOT NULL AUTO_INCREMENT COMMENT '首页轮播图主键id',
  `carousel_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '轮播图',
  `redirect_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '\'##\'' COMMENT '点击后的跳转地址(默认不跳转)',
  `carousel_rank` int NOT NULL DEFAULT 0 COMMENT '排序值(字段越大越靠前)',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` int NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_user` int NOT NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`carousel_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 04. 建表：haoxiang_mall_coupon
CREATE TABLE IF NOT EXISTS `haoxiang_mall_coupon`  (
  `coupon_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '优惠券名称',
  `coupon_desc` varchar(127) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '优惠券介绍，通常是显示优惠券使用限制文字',
  `coupon_total` int NOT NULL DEFAULT 0 COMMENT '优惠券数量，如果是0，则是无限量',
  `discount` int NULL DEFAULT 0 COMMENT '优惠金额，',
  `min` int NULL DEFAULT 0 COMMENT '最少消费金额才能使用优惠券。',
  `coupon_limit` tinyint NULL DEFAULT 1 COMMENT '用户领券限制数量，如果是0，则是不限制；默认是1，限领一张.',
  `coupon_type` tinyint NULL DEFAULT 0 COMMENT '优惠券赠送类型，如果是0则通用券，用户领取；如果是1，则是注册赠券；如果是2，则是优惠券码兑换；',
  `status` tinyint NULL DEFAULT 0 COMMENT '优惠券状态，如果是0则是正常可用；如果是1则是过期; 如果是2则是下架。',
  `goods_type` tinyint NULL DEFAULT 0 COMMENT '商品限制类型，如果0则全商品，如果是1则是类目限制，如果是2则是商品限制。',
  `goods_value` varchar(1023) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '商品限制值，goods_type如果是0则空集合，如果是1则是类目集合，如果是2则是商品集合。',
  `code` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '优惠券兑换码',
  `days` smallint NULL DEFAULT 0 COMMENT '基于领取时间的有效天数days。',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`coupon_id`) USING BTREE,
  INDEX `code`(`code`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券信息及规则表' ROW_FORMAT = DYNAMIC;

-- 05. 建表：haoxiang_mall_coupon_user
CREATE TABLE IF NOT EXISTS `haoxiang_mall_coupon_user`  (
  `coupon_user_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `status` tinyint NULL DEFAULT 0 COMMENT '使用状态, 如果是0则未使用；如果是1则已使用；如果是2则已过期；',
  `used_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `start_time` datetime NULL DEFAULT NULL COMMENT '有效期开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '有效期截至时间',
  `order_id` bigint NULL DEFAULT NULL COMMENT '订单ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`coupon_user_id`) USING BTREE,
  INDEX `user_coupin_index`(`user_id`, `coupon_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '优惠券用户使用表' ROW_FORMAT = DYNAMIC;

-- 06. 建表：haoxiang_mall_goods_category
CREATE TABLE IF NOT EXISTS `haoxiang_mall_goods_category`  (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类id',
  `category_level` tinyint NOT NULL DEFAULT 0 COMMENT '分类级别(1-一级分类 2-二级分类 3-三级分类)',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父分类id',
  `category_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '分类名称',
  `category_rank` int NOT NULL DEFAULT 0 COMMENT '排序值(字段越大越靠前)',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` int NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `update_user` int NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`category_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 07. 建表：haoxiang_mall_goods_info
CREATE TABLE IF NOT EXISTS `haoxiang_mall_goods_info`  (
  `goods_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品表主键id',
  `goods_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商品名',
  `goods_intro` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商品简介',
  `goods_category_id` bigint NOT NULL DEFAULT 0 COMMENT '关联分类id',
  `goods_cover_img` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '/admin/dist/img/no-img.png' COMMENT '商品主图',
  `goods_carousel` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '/admin/dist/img/no-img.png' COMMENT '商品轮播图',
  `goods_detail_content` text CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '商品详情',
  `original_price` int NOT NULL DEFAULT 1 COMMENT '商品价格',
  `selling_price` int NOT NULL DEFAULT 1 COMMENT '商品实际售价',
  `stock_num` int NOT NULL DEFAULT 0 COMMENT '商品库存数量',
  `tag` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '商品标签',
  `goods_sell_status` tinyint NOT NULL DEFAULT 0 COMMENT '商品上架状态 0-上架 1-下架',
  `create_user` int NOT NULL DEFAULT 0 COMMENT '添加者主键id',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商品添加时间',
  `update_user` int NOT NULL DEFAULT 0 COMMENT '修改者主键id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '商品修改时间',
  PRIMARY KEY (`goods_id`) USING BTREE,
  INDEX `cate_index`(`goods_category_id`) USING BTREE,
  INDEX `sell_status_index`(`goods_sell_status`) USING BTREE,
  INDEX `create_time_index`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 08. 建表：haoxiang_mall_index_config
CREATE TABLE IF NOT EXISTS `haoxiang_mall_index_config`  (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '首页配置项主键id',
  `config_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '显示字符(配置搜索时不可为空，其他可为空)',
  `config_type` tinyint NOT NULL DEFAULT 0 COMMENT '1-搜索框热搜 2-搜索下拉框热搜 3-(首页)热销商品 4-(首页)新品上线 5-(首页)为你推荐',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '商品id 默认为0',
  `redirect_url` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '##' COMMENT '点击后的跳转地址(默认不跳转)',
  `config_rank` int NOT NULL DEFAULT 0 COMMENT '排序值(字段越大越靠前)',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user` int NOT NULL DEFAULT 0 COMMENT '创建者id',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新修改时间',
  `update_user` int NULL DEFAULT 0 COMMENT '修改者id',
  PRIMARY KEY (`config_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 09. 建表：haoxiang_mall_order
CREATE TABLE IF NOT EXISTS `haoxiang_mall_order`  (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单表主键id',
  `order_no` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '订单号',
  `user_id` bigint NOT NULL DEFAULT 0 COMMENT '用户主键id',
  `total_price` int NOT NULL DEFAULT 1 COMMENT '订单总价',
  `pay_status` tinyint NOT NULL DEFAULT 0 COMMENT '支付状态:0.未支付,1.支付成功,-1:支付失败',
  `pay_type` tinyint NOT NULL DEFAULT 0 COMMENT '支付方式标识：0.未支付 1.登录密码验证',
  `pay_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `order_status` tinyint NOT NULL DEFAULT 0 COMMENT '订单状态:0.待支付 1.已支付 2.配货完成 3:出库成功 4.交易成功 -1.手动关闭 -2.超时关闭 -3.商家关闭',
  `extra_info` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '订单body',
  `user_name` varchar(30) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人姓名',
  `user_phone` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人手机号',
  `user_address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货人收货地址',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新修改时间',
  `version` bigint NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`order_id`) USING BTREE,
  UNIQUE INDEX `order_no_uqi`(`order_no`) USING BTREE,
  INDEX `user_id_index`(`user_id`) USING BTREE,
  INDEX `create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 10. 建表：haoxiang_mall_order_item
CREATE TABLE IF NOT EXISTS `haoxiang_mall_order_item`  (
  `order_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单关联购物项主键id',
  `order_id` bigint NOT NULL DEFAULT 0 COMMENT '订单主键id',
  `seckill_id` bigint NULL DEFAULT NULL COMMENT '秒杀商品ID',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '关联商品id',
  `goods_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '下单时商品的名称(订单快照)',
  `goods_cover_img` varchar(200) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '下单时商品的主图(订单快照)',
  `selling_price` int NOT NULL DEFAULT 1 COMMENT '下单时商品的价格(订单快照)',
  `goods_count` int NOT NULL DEFAULT 1 COMMENT '数量(订单快照)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_item_id`) USING BTREE,
  INDEX `order_id_index`(`order_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 11. 建表：haoxiang_mall_seckill
CREATE TABLE IF NOT EXISTS `haoxiang_mall_seckill`  (
  `seckill_id` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `goods_id` int NOT NULL COMMENT '秒杀商品ID',
  `seckill_price` int NOT NULL COMMENT '秒杀价格',
  `seckill_num` int NOT NULL COMMENT '秒杀数量',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '秒杀商品状态（0下架，1上架）',
  `seckill_begin` datetime NULL DEFAULT NULL COMMENT '秒杀开始时间',
  `seckill_end` datetime NULL DEFAULT NULL COMMENT '秒杀结束时间',
  `seckill_rank` int NULL DEFAULT NULL COMMENT '秒杀商品排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `is_deleted` tinyint(1) NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  PRIMARY KEY (`seckill_id`) USING BTREE,
  INDEX `status_index`(`status`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- 12. 建表：haoxiang_mall_seckill_success
CREATE TABLE IF NOT EXISTS `haoxiang_mall_seckill_success`  (
  `sec_id` int NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `seckill_id` int NOT NULL COMMENT '商品商品id',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `status` tinyint NOT NULL DEFAULT -1 COMMENT '状态信息：-1无效，0成功',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`sec_id`) USING BTREE,
  INDEX `idx_create_time`(`create_time`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '秒杀库存表' ROW_FORMAT = DYNAMIC;

-- 13. 建表：haoxiang_mall_shopping_cart_item
CREATE TABLE IF NOT EXISTS `haoxiang_mall_shopping_cart_item`  (
  `cart_item_id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物项主键id',
  `user_id` bigint NOT NULL COMMENT '用户主键id',
  `goods_id` bigint NOT NULL DEFAULT 0 COMMENT '关联商品id',
  `goods_count` int NOT NULL DEFAULT 1 COMMENT '数量(最大为5)',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标识字段(0-未删除 1-已删除)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最新修改时间',
  PRIMARY KEY (`cart_item_id`) USING BTREE,
  INDEX `is_delete_index`(`is_deleted`) USING BTREE,
  INDEX `user_id_index`(`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 14. 建表：haoxiang_mall_user
CREATE TABLE IF NOT EXISTS `haoxiang_mall_user`  (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户主键id',
  `nick_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '用户昵称',
  `login_name` varchar(11) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '登陆名称(默认为手机号)',
  `password_md5` varchar(32) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT 'MD5加密后的密码',
  `introduce_sign` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '个性签名',
  `address` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL DEFAULT '' COMMENT '收货地址',
  `is_deleted` tinyint NOT NULL DEFAULT 0 COMMENT '注销标识字段(0-正常 1-已注销)',
  `locked_flag` tinyint NOT NULL DEFAULT 0 COMMENT '锁定标识字段(0-未锁定 1-已锁定)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

-- 15. 建表：haoxiang_mall_user_click
CREATE TABLE IF NOT EXISTS `haoxiang_mall_user_click`  (
  `id` bigint NOT NULL COMMENT '自增ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- 16. 建表：haoxiang_mall_user_search_record
CREATE TABLE IF NOT EXISTS `haoxiang_mall_user_search_record`  (
  `id` bigint NOT NULL COMMENT '自增ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `search_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '查询内容',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = DYNAMIC;

-- 初始化数据：沿用逐条 INSERT INTO ... VALUES (...) 的导入格式。

-- Records of haoxiang_mall_admin_user (1)
INSERT INTO `haoxiang_mall_admin_user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', '本地管理员', 0);

-- Records of haoxiang_mall_carousel (3)
INSERT INTO `haoxiang_mall_carousel` VALUES (1, '/mall/image/swiper/banner01.jpg', '/goods/detail/20001', 3, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_carousel` VALUES (2, '/mall/image/swiper/banner02.jpg', '/goods/detail/20002', 2, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_carousel` VALUES (3, '/mall/image/swiper/banner03.jpg', '/goods/detail/20003', 1, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);

-- Records of haoxiang_mall_coupon (6)
INSERT INTO `haoxiang_mall_coupon` VALUES (1, '满 1000 减 100', '全场通用', 100, 100, 1000, 1, 0, 0, 0, '', NULL, 30, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_coupon` VALUES (2, '满 500 减 50', '全场通用', 200, 50, 500, 1, 0, 0, 0, '', NULL, 30, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_coupon` VALUES (3, '满 200 减 20', '全场通用', 300, 20, 200, 1, 0, 0, 0, '', NULL, 30, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_coupon` VALUES (4, '新用户满 300 减 30', '注册赠券', 0, 30, 300, 1, 1, 0, 0, '', NULL, 30, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_coupon` VALUES (5, '小米手机满 3000 减 100', '限小米手机分类', 100, 100, 3000, 1, 0, 0, 1, '51', NULL, 30, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_coupon` VALUES (6, 'iPhone 18 Pro 满 5000 减 200', '限指定商品', 100, 200, 5000, 1, 0, 0, 2, '20001', NULL, 30, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);

-- Records of haoxiang_mall_goods_category (98)
INSERT INTO `haoxiang_mall_goods_category` VALUES (15, 1, 0, '家电 数码 手机', 100, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (16, 1, 0, '女装 男装 穿搭', 99, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (17, 2, 15, '家电', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (18, 2, 15, '数码', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (19, 2, 15, '手机', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (20, 3, 17, '生活电器', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (21, 3, 17, '厨房电器', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (22, 3, 17, '扫地机器人', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (23, 3, 17, '吸尘器', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (24, 3, 17, '取暖器', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (25, 3, 17, '豆浆机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (26, 3, 17, '暖风机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (27, 3, 17, '加湿器', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (28, 3, 17, '蓝牙音箱', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (29, 3, 17, '烤箱', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (30, 3, 17, '卷发器', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (31, 3, 17, '空气净化器', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (32, 3, 18, '游戏主机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (33, 3, 18, '耳机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (34, 3, 18, '平板电脑', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (35, 3, 18, '苹果 Apple', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (36, 3, 18, '电脑主机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (37, 3, 18, '数码相机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (38, 3, 18, '电玩动漫', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (39, 3, 18, '单反相机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (40, 3, 18, '键盘鼠标', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (41, 3, 18, '无人机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (42, 3, 18, '二手电脑', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (43, 3, 18, '二手手机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (44, 3, 19, '苹果电脑', 89, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (45, 3, 19, '荣耀手机', 99, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (46, 3, 19, '华为手机', 98, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (47, 3, 19, '苹果手机', 88, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (48, 3, 19, '华为 Mate 系列', 79, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (49, 3, 19, '华为 Pura 系列', 97, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (50, 3, 19, '华为 Pura 旗舰', 0, 1, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (51, 3, 19, '小米手机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (52, 3, 19, '红米', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (53, 3, 19, 'OPPO', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (54, 3, 19, '一加', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (55, 3, 19, '小米 MIX', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (56, 3, 19, 'Reno', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (57, 3, 19, 'vivo', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (58, 3, 19, '手机以旧换新', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (59, 1, 0, '运动 户外 乐器', 97, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (60, 1, 0, '游戏 动漫 影视', 96, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (61, 1, 0, '家具 家饰 家纺', 98, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (62, 1, 0, '美妆 清洁 宠物', 94, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (63, 1, 0, '工具 装修 建材', 93, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (64, 1, 0, '珠宝 金饰 眼镜', 92, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (65, 1, 0, '玩具 孕产 用品', 2, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (66, 1, 0, '鞋靴 箱包 配件', 91, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (67, 2, 16, '女装', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (68, 2, 16, '男装', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (69, 2, 16, '穿搭', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (70, 2, 61, '家具', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (71, 2, 61, '家饰', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (72, 2, 61, '家纺', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (73, 2, 59, '运动', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (74, 2, 59, '户外', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (75, 2, 59, '乐器', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (76, 3, 67, '外套', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (77, 3, 70, '沙发', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (78, 3, 73, '跑鞋', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (79, 2, 60, '游戏', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (80, 2, 60, '动漫', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (81, 2, 60, '影视', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (82, 3, 79, 'LOL', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (83, 2, 62, '美妆', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (84, 2, 62, '宠物', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (85, 2, 62, '清洁', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (86, 3, 83, '口红', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (87, 2, 63, '工具', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (88, 2, 63, '装修', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (89, 2, 63, '建材', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (90, 3, 87, '转换器', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (91, 2, 64, '珠宝', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (92, 2, 64, '金饰', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (93, 2, 64, '眼镜', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (94, 3, 91, '钻石', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (95, 2, 66, '鞋靴', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (96, 2, 66, '箱包', 9, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (97, 2, 66, '配件', 8, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (98, 3, 95, '休闲鞋', 10, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (99, 3, 83, '气垫', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (100, 3, 83, '美白', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (101, 3, 83, '隔离霜', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (102, 3, 83, '粉底', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (103, 3, 83, '腮红', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (104, 3, 83, '睫毛膏', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (105, 3, 83, '香水', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (106, 3, 83, '面膜', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (107, 3, 19, '手机配件', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (108, 3, 18, '耳机', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (109, 3, 83, '生活用品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (200, 3, 19, '三星手机', 87, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (201, 3, 18, '笔记本电脑', 86, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_goods_category` VALUES (202, 3, 18, '智能手表', 85, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);

-- Records of haoxiang_mall_goods_info (36)
INSERT INTO `haoxiang_mall_goods_info` VALUES (20001, 'Apple iPhone 18 Pro', '2026 款 Pro 系列手机', 47, '/mall/image/products/20001.jpg', '/mall/image/products/20001.jpg', '<p>Apple iPhone 18 Pro</p><p>2026 款 Pro 系列手机</p>', 8999, 8499, 103, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20002, 'Apple iPhone 18 Pro Max', '2026 款 Pro Max 系列手机', 47, '/mall/image/products/20002.png', '/mall/image/products/20002.png', '<p>Apple iPhone 18 Pro Max</p><p>2026 款 Pro Max 系列手机</p>', 9999, 9499, 106, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20003, 'Apple iPhone 17e', '2026 款 iPhone 17e', 47, '/mall/image/products/20003.png', '/mall/image/products/20003.png', '<p>Apple iPhone 17e</p><p>2026 款 iPhone 17e</p>', 4499, 4299, 109, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20004, 'Samsung Galaxy S26 Ultra', '2026 款 Galaxy S 系列旗舰手机', 200, '/mall/image/products/20004.png', '/mall/image/products/20004.png', '<p>Samsung Galaxy S26 Ultra</p><p>2026 款 Galaxy S 系列旗舰手机</p>', 9999, 9299, 112, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20005, 'Samsung Galaxy S26+', '2026 款 Galaxy S 系列大屏手机', 200, '/mall/image/products/20005.jpg', '/mall/image/products/20005.jpg', '<p>Samsung Galaxy S26+</p><p>2026 款 Galaxy S 系列大屏手机</p>', 7999, 7499, 115, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20006, 'Samsung Galaxy S26', '2026 款 Galaxy S 系列手机', 200, '/mall/image/products/20006.png', '/mall/image/products/20006.png', '<p>Samsung Galaxy S26</p><p>2026 款 Galaxy S 系列手机</p>', 6999, 6499, 118, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20007, 'Xiaomi 17 Ultra', '2026 款 Xiaomi 影像旗舰手机', 51, '/mall/image/products/20007.jpg', '/mall/image/products/20007.jpg', '<p>Xiaomi 17 Ultra</p><p>2026 款 Xiaomi 影像旗舰手机</p>', 6999, 6499, 121, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20008, 'Xiaomi 17T Pro', '2026 款 Xiaomi 17T 系列手机', 51, '/mall/image/products/20008.png', '/mall/image/products/20008.png', '<p>Xiaomi 17T Pro</p><p>2026 款 Xiaomi 17T 系列手机</p>', 4999, 4599, 124, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20009, 'HONOR Magic8 Pro', '2026 款 HONOR Magic 系列手机', 45, '/mall/image/products/20009.png', '/mall/image/products/20009.png', '<p>HONOR Magic8 Pro</p><p>2026 款 HONOR Magic 系列手机</p>', 5999, 5499, 127, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20010, 'HUAWEI Pura 90s Pro Max', '2026 款 HUAWEI Pura 系列手机', 46, '/mall/image/products/20010.png', '/mall/image/products/20010.png', '<p>HUAWEI Pura 90s Pro Max</p><p>2026 款 HUAWEI Pura 系列手机</p>', 8999, 8499, 130, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20011, 'HUAWEI Mate 80 Pro', '2026 年热门 Mate 系列手机', 46, '/mall/image/products/20011.png', '/mall/image/products/20011.png', '<p>HUAWEI Mate 80 Pro</p><p>2026 年热门 Mate 系列手机</p>', 7999, 7499, 133, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20012, 'Apple MacBook Neo', '2026 款轻便笔记本电脑', 44, '/mall/image/products/20012.png', '/mall/image/products/20012.png', '<p>Apple MacBook Neo</p><p>2026 款轻便笔记本电脑</p>', 5999, 5599, 136, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20013, 'Apple MacBook Air M5 13 英寸', '2026 款 M5 轻薄笔记本', 44, '/mall/image/products/20013.png', '/mall/image/products/20013.png', '<p>Apple MacBook Air M5 13 英寸</p><p>2026 款 M5 轻薄笔记本</p>', 8999, 8499, 139, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20014, 'Apple MacBook Air M5 15 英寸', '2026 款 M5 大屏轻薄笔记本', 44, '/mall/image/products/20014.jpg', '/mall/image/products/20014.jpg', '<p>Apple MacBook Air M5 15 英寸</p><p>2026 款 M5 大屏轻薄笔记本</p>', 10999, 10499, 142, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20015, 'Apple MacBook Pro M5 Pro', '2026 款专业笔记本电脑', 44, '/mall/image/products/20015.png', '/mall/image/products/20015.png', '<p>Apple MacBook Pro M5 Pro</p><p>2026 款专业笔记本电脑</p>', 16999, 15999, 145, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20016, 'Apple MacBook Pro M5 Max', '2026 款高性能专业笔记本', 44, '/mall/image/products/20016.png', '/mall/image/products/20016.png', '<p>Apple MacBook Pro M5 Max</p><p>2026 款高性能专业笔记本</p>', 26999, 25499, 148, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20017, 'Samsung Galaxy Book6 Pro', '2026 款 Galaxy Book 系列笔记本', 201, '/mall/image/products/20017.png', '/mall/image/products/20017.png', '<p>Samsung Galaxy Book6 Pro</p><p>2026 款 Galaxy Book 系列笔记本</p>', 9999, 9299, 151, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20018, 'Apple iPad Air M4 11 英寸', '2026 款 M4 平板电脑', 34, '/mall/image/products/20018.jpg', '/mall/image/products/20018.jpg', '<p>Apple iPad Air M4 11 英寸</p><p>2026 款 M4 平板电脑</p>', 4999, 4699, 154, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20019, 'Apple iPad Air M4 13 英寸', '2026 款 M4 大屏平板电脑', 34, '/mall/image/products/20019.png', '/mall/image/products/20019.png', '<p>Apple iPad Air M4 13 英寸</p><p>2026 款 M4 大屏平板电脑</p>', 6499, 6199, 157, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20020, 'Apple AirPods 5', '2026 款无线耳机', 33, '/mall/image/products/20020.jpg', '/mall/image/products/20020.jpg', '<p>Apple AirPods 5</p><p>2026 款无线耳机</p>', 1399, 1299, 160, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20021, 'Samsung Galaxy Buds4 Pro', '2026 款 Galaxy 无线耳机', 33, '/mall/image/products/20021.png', '/mall/image/products/20021.png', '<p>Samsung Galaxy Buds4 Pro</p><p>2026 款 Galaxy 无线耳机</p>', 1699, 1499, 163, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20022, 'Samsung Galaxy Buds4', '2026 款 Galaxy 无线耳机', 33, '/mall/image/products/20022.jpg', '/mall/image/products/20022.jpg', '<p>Samsung Galaxy Buds4</p><p>2026 款 Galaxy 无线耳机</p>', 1199, 999, 166, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20023, 'Xiaomi Buds 6', '2026 款 Xiaomi 无线耳机', 33, '/mall/image/products/20023.jpg', '/mall/image/products/20023.jpg', '<p>Xiaomi Buds 6</p><p>2026 款 Xiaomi 无线耳机</p>', 699, 599, 169, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20024, 'Apple Watch Series 12', '2026 款智能手表', 202, '/mall/image/products/20024.jpg', '/mall/image/products/20024.jpg', '<p>Apple Watch Series 12</p><p>2026 款智能手表</p>', 3299, 3099, 172, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20025, 'Apple Watch Ultra 4', '2026 款户外智能手表', 202, '/mall/image/products/20025.jpg', '/mall/image/products/20025.jpg', '<p>Apple Watch Ultra 4</p><p>2026 款户外智能手表</p>', 6499, 6199, 175, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20026, 'Xiaomi Watch 5', '2026 款 Xiaomi 智能手表', 202, '/mall/image/products/20026.jpg', '/mall/image/products/20026.jpg', '<p>Xiaomi Watch 5</p><p>2026 款 Xiaomi 智能手表</p>', 1599, 1399, 178, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20027, 'Samsung Galaxy Book6 Ultra', '2026 款 Galaxy Book 高性能笔记本', 201, '/mall/image/products/20027.png', '/mall/image/products/20027.png', '<p>Samsung Galaxy Book6 Ultra</p><p>2026 款 Galaxy Book 高性能笔记本</p>', 15999, 14999, 181, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20028, 'Xiaomi 17', '2026 年热门 Xiaomi 旗舰手机', 51, '/mall/image/products/20028.png', '/mall/image/products/20028.png', '<p>Xiaomi 17</p><p>2026 年热门 Xiaomi 旗舰手机</p>', 4499, 4199, 184, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20029, 'HUAWEI Pura 90', '2026 款 HUAWEI Pura 系列手机', 46, '/mall/image/products/20029.png', '/mall/image/products/20029.png', '<p>HUAWEI Pura 90</p><p>2026 款 HUAWEI Pura 系列手机</p>', 5999, 5599, 187, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20030, 'Dyson R1 Nurovi Dry', '2026 款扫地机器人', 22, '/mall/image/products/20030.png', '/mall/image/products/20030.png', '<p>Dyson R1 Nurovi Dry</p><p>2026 款扫地机器人</p>', 6999, 6499, 190, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20031, 'Dyson R2 Nurovi Wash+Dry', '2026 款扫拖一体机器人', 22, '/mall/image/products/20031.jpg', '/mall/image/products/20031.jpg', '<p>Dyson R2 Nurovi Wash+Dry</p><p>2026 款扫拖一体机器人</p>', 8999, 8499, 193, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20032, 'Dyson V10 Konical', '2026 款无线吸尘器', 23, '/mall/image/products/20032.png', '/mall/image/products/20032.png', '<p>Dyson V10 Konical</p><p>2026 款无线吸尘器</p>', 4999, 4599, 196, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20033, 'DJI Osmo Pocket 4P', '2026 款手持影像设备', 37, '/mall/image/products/20033.jpg', '/mall/image/products/20033.jpg', '<p>DJI Osmo Pocket 4P</p><p>2026 款手持影像设备</p>', 3999, 3699, 199, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20034, 'DJI Osmo 360 II', '2026 款全景相机', 37, '/mall/image/products/20034.jpg', '/mall/image/products/20034.jpg', '<p>DJI Osmo 360 II</p><p>2026 款全景相机</p>', 4299, 3999, 202, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20035, 'Nike Pegasus 42', '2026 款日常跑鞋', 78, '/mall/image/products/20035.png', '/mall/image/products/20035.png', '<p>Nike Pegasus 42</p><p>2026 款日常跑鞋</p>', 999, 899, 205, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');
INSERT INTO `haoxiang_mall_goods_info` VALUES (20036, 'Nike Pegasus Plus 2', '2026 款速度训练跑鞋', 78, '/mall/image/products/20036.jpg', '/mall/image/products/20036.jpg', '<p>Nike Pegasus Plus 2</p><p>2026 款速度训练跑鞋</p>', 1299, 1199, 208, '2026 新品', 0, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00');

-- Records of haoxiang_mall_index_config (34)
INSERT INTO `haoxiang_mall_index_config` VALUES (1, 'Apple iPhone 18 Pro', 1, 0, '##', 99, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (2, 'Samsung Galaxy S26 Ultra', 1, 0, '##', 98, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (3, 'Xiaomi 17 Ultra', 1, 0, '##', 97, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (4, 'Apple MacBook Neo', 1, 0, '##', 96, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (5, 'HUAWEI Pura 90s Pro Max', 2, 0, '##', 99, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (6, 'Apple iPad Air M4 11 英寸', 2, 0, '##', 98, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (7, 'Apple AirPods 5', 2, 0, '##', 97, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (8, 'Apple Watch Series 12', 2, 0, '##', 96, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (9, 'Apple iPhone 18 Pro', 3, 20001, '##', 99, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (10, 'Samsung Galaxy S26 Ultra', 3, 20004, '##', 98, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (11, 'Xiaomi 17 Ultra', 3, 20007, '##', 97, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (12, 'HONOR Magic8 Pro', 3, 20009, '##', 96, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (13, 'Apple MacBook Neo', 3, 20012, '##', 95, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (14, 'Apple AirPods 5', 3, 20020, '##', 94, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (15, 'Apple Watch Series 12', 3, 20024, '##', 93, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (16, 'Xiaomi 17', 3, 20028, '##', 92, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (17, 'Apple iPhone 18 Pro Max', 4, 20002, '##', 99, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (18, 'Apple iPhone 17e', 4, 20003, '##', 98, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (19, 'Xiaomi 17T Pro', 4, 20008, '##', 97, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (20, 'Apple MacBook Air M5 13 英寸', 4, 20013, '##', 96, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (21, 'Samsung Galaxy Book6 Pro', 4, 20017, '##', 95, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (22, 'Apple iPad Air M4 11 英寸', 4, 20018, '##', 94, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (23, 'Samsung Galaxy Buds4 Pro', 4, 20021, '##', 93, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (24, 'Apple Watch Ultra 4', 4, 20025, '##', 92, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (25, 'Samsung Galaxy S26+', 5, 20005, '##', 99, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (26, 'Samsung Galaxy S26', 5, 20006, '##', 98, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (27, 'HUAWEI Pura 90s Pro Max', 5, 20010, '##', 97, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (28, 'HUAWEI Mate 80 Pro', 5, 20011, '##', 96, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (29, 'Apple MacBook Air M5 15 英寸', 5, 20014, '##', 95, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (30, 'Apple MacBook Pro M5 Max', 5, 20016, '##', 94, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (31, 'Apple iPad Air M4 13 英寸', 5, 20019, '##', 93, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (32, 'Samsung Galaxy Buds4', 5, 20022, '##', 92, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (33, 'Xiaomi Buds 6', 5, 20023, '##', 91, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_index_config` VALUES (34, 'Xiaomi Watch 5', 5, 20026, '##', 90, 0, '2026-10-30 00:00:00', 0, '2026-10-30 00:00:00', 0);

-- Records of haoxiang_mall_seckill (6)
INSERT INTO `haoxiang_mall_seckill` VALUES (1, 20001, 8199, 25, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 99, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_seckill` VALUES (2, 20004, 8999, 30, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 98, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_seckill` VALUES (3, 20007, 6199, 35, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 97, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_seckill` VALUES (4, 20012, 5299, 40, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 96, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_seckill` VALUES (5, 20020, 999, 45, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 95, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
INSERT INTO `haoxiang_mall_seckill` VALUES (6, 20023, 299, 50, 1, '2026-10-01 00:00:00', '2026-12-31 23:59:59', 94, '2026-10-30 00:00:00', '2026-10-30 00:00:00', 0);
