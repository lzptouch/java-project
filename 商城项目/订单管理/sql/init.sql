-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `order_service` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `order_service`;

-- 订单主表
CREATE TABLE `order_main` (
  `order_id` bigint NOT NULL COMMENT '订单号（雪花算法生成）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额（扣除优惠后）',
  `status` tinyint NOT NULL COMMENT '订单状态（0-草稿，1-待支付，2-已支付，3-已发货，4-已完成，5-已取消）',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '签收时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`order_id`),
  KEY `idx_user_id` (`user_id`) COMMENT '用户ID索引，支持用户查订单'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 订单明细表
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单号',
  `sku_id` bigint NOT NULL COMMENT '商品SKU ID',
  `product_name` varchar(255) NOT NULL COMMENT '商品名称（下单时快照，避免商品改名影响订单）',
  `sku_specs` varchar(255) DEFAULT NULL COMMENT 'SKU规格（如颜色:红;尺码:L）',
  `price` decimal(10,2) NOT NULL COMMENT '下单时单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '明细总金额（price*quantity）',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`) COMMENT '订单号索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 支付记录表
CREATE TABLE `order_payment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '关联订单号',
  `pay_no` varchar(64) DEFAULT NULL COMMENT '支付渠道流水号（如微信支付的transaction_id）',
  `pay_type` tinyint NOT NULL COMMENT '支付方式（1-微信，2-支付宝）',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `status` tinyint NOT NULL COMMENT '支付状态（0-待支付，1-支付成功，2-支付失败）',
  `callback_time` datetime DEFAULT NULL COMMENT '支付回调时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`) COMMENT '订单号唯一，一个订单对应一个支付记录'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 售后记录表
CREATE TABLE `order_after_sale` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `after_sale_no` varchar(64) NOT NULL COMMENT '售后单号',
  `order_id` bigint NOT NULL COMMENT '关联订单号',
  `type` tinyint NOT NULL COMMENT '售后类型（1-退款，2-退货退款，3-换货）',
  `status` tinyint NOT NULL COMMENT '售后状态（0-申请中，1-审核通过，2-审核拒绝，3-处理中，4-已完成）',
  `refund_amount` decimal(10,2) DEFAULT NULL COMMENT '退款金额',
  `reason` varchar(500) DEFAULT NULL COMMENT '申请原因',
  `apply_time` datetime DEFAULT NULL COMMENT '申请时间',
  `process_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建测试数据
INSERT INTO `order_main` (`order_id`, `user_id`, `total_amount`, `pay_amount`, `status`, `create_time`, `update_time`) 
VALUES (10000001, 1001, 199.99, 199.99, 1, NOW(), NOW());

INSERT INTO `order_item` (`order_id`, `sku_id`, `product_name`, `sku_specs`, `price`, `quantity`, `total_price`) 
VALUES (10000001, 10001, '测试商品', '颜色:红;尺码:M', 199.99, 1, 199.99);

INSERT INTO `order_payment` (`order_id`, `pay_type`, `pay_amount`, `status`, `create_time`, `update_time`) 
VALUES (10000001, 1, 199.99, 0, NOW(), NOW());