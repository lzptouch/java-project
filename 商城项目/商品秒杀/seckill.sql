-- 商品秒杀系统数据库脚本

-- 1. 用户表（简化版）
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码（加密存储）',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-禁用，1-正常）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 商品表
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `original_price` decimal(10,2) NOT NULL COMMENT '原价',
  `stock` int DEFAULT 0 COMMENT '普通库存',
  `description` text COMMENT '商品描述',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-下架，1-上架）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 3. 秒杀活动表
CREATE TABLE `seckill_activity` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '活动ID',
  `activity_name` varchar(100) NOT NULL COMMENT '活动名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-未开始，1-进行中，2-已结束，3-已取消）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 4. 秒杀商品表（关联活动和商品）
CREATE TABLE `seckill_product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '秒杀商品ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价',
  `seckill_stock` int NOT NULL COMMENT '秒杀库存数量',
  `status` tinyint DEFAULT 1 COMMENT '状态（0-无效，1-有效）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_activity_product` (`activity_id`,`product_id`),
  KEY `fk_product_id` (`product_id`),
  CONSTRAINT `fk_seckill_activity_id` FOREIGN KEY (`activity_id`) REFERENCES `seckill_activity` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_seckill_product_id` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 5. 秒杀库存表（独立表，用于并发扣减）
CREATE TABLE `seckill_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '库存ID',
  `seckill_product_id` bigint NOT NULL COMMENT '秒杀商品ID',
  `quantity` int NOT NULL DEFAULT 0 COMMENT '剩余库存数量',
  `version` int DEFAULT 0 COMMENT '版本号（乐观锁）',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seckill_product` (`seckill_product_id`),
  CONSTRAINT `fk_seckill_product_stock` FOREIGN KEY (`seckill_product_id`) REFERENCES `seckill_product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀库存表';

-- 6. 秒杀订单表
CREATE TABLE `seckill_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `seckill_product_id` bigint NOT NULL COMMENT '秒杀商品ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `seckill_price` decimal(10,2) NOT NULL COMMENT '秒杀价格',
  `status` tinyint DEFAULT 0 COMMENT '订单状态（0-待支付，1-已支付，2-已取消，3-已完成）',
  `pay_deadline` datetime DEFAULT NULL COMMENT '支付截止时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  CONSTRAINT `fk_order_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_product_id` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_seckill_product_id` FOREIGN KEY (`seckill_product_id`) REFERENCES `seckill_product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_activity_id` FOREIGN KEY (`activity_id`) REFERENCES `seckill_activity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 7. 秒杀用户抢购记录（用于快速校验重复抢购）
CREATE TABLE `seckill_user_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '抢购时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product_activity` (`user_id`,`product_id`,`activity_id`),
  CONSTRAINT `fk_record_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_record_product_id` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_record_activity_id` FOREIGN KEY (`activity_id`) REFERENCES `seckill_activity` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀用户抢购记录表';

-- 8. 系统配置表（用于活动预热、限流等配置）
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(50) NOT NULL COMMENT '配置键',
  `config_value` varchar(255) DEFAULT NULL COMMENT '配置值',
  `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 初始化系统配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('seckill.prewarm.time', '600000', '活动预热时间（毫秒）'),
('seckill.rate.limit.ip', '10', 'IP限流（每秒请求数）'),
('seckill.rate.limit.user', '5', '用户限流（每秒请求数）'),
('seckill.pay.timeout', '300', '订单支付超时时间（秒）'),
('seckill.captcha.enable', '1', '是否启用验证码（0-禁用，1-启用）');