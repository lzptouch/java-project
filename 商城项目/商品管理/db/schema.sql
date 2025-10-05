CREATE DATABASE IF NOT EXISTS product_service DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE product_service;

-- 分类表
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '分类ID',
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    level TINYINT DEFAULT 1 COMMENT '分类层级',
    icon VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
    weight INT DEFAULT 0 COMMENT '排序权重，数字越大越靠前',
    status TINYINT DEFAULT 1 COMMENT '状态：0禁用，1启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    INDEX idx_parent_id (parent_id),
    INDEX idx_status (status),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '商品ID',
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    description VARCHAR(500) DEFAULT NULL COMMENT '商品描述',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '商品价格',
    original_price DECIMAL(10, 2) DEFAULT 0.00 COMMENT '原价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
    sales INT DEFAULT 0 COMMENT '销量',
    image VARCHAR(255) DEFAULT NULL COMMENT '商品主图',
    images VARCHAR(1000) DEFAULT NULL COMMENT '商品图片，逗号分隔',
    status TINYINT DEFAULT 0 COMMENT '状态：0下架，1上架',
    weight INT DEFAULT 0 COMMENT '排序权重，数字越大越靠前',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    INDEX idx_category_id (category_id),
    INDEX idx_status (status),
    INDEX idx_name (name),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB COMMENT='商品表';

-- 商品详情表
CREATE TABLE IF NOT EXISTS product_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '详情ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    detail_content LONGTEXT COMMENT '商品详情内容，富文本',
    params VARCHAR(1000) DEFAULT NULL COMMENT '商品参数，JSON格式',
    package_list VARCHAR(500) DEFAULT NULL COMMENT '包装清单',
    after_sale_desc VARCHAR(500) DEFAULT NULL COMMENT '售后服务描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    UNIQUE KEY uk_product_id (product_id),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB COMMENT='商品详情表';

-- 商品规格表
CREATE TABLE IF NOT EXISTS product_spec (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '规格ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    spec_name VARCHAR(50) NOT NULL COMMENT '规格名称',
    spec_value VARCHAR(50) NOT NULL COMMENT '规格值',
    price DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '规格价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '规格库存',
    sales INT DEFAULT 0 COMMENT '规格销量',
    image_url VARCHAR(255) DEFAULT NULL COMMENT '规格图片',
    spec_code VARCHAR(100) DEFAULT NULL COMMENT '规格编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
    INDEX idx_product_id (product_id),
    INDEX idx_spec_code (spec_code),
    INDEX idx_deleted (deleted)
) ENGINE=InnoDB COMMENT='商品规格表';

-- 初始化基础数据
INSERT INTO category (name, parent_id, level, icon, weight, status) VALUES
('电子产品', 0, 1, 'electronics.png', 100, 1),
('服装鞋帽', 0, 1, 'clothing.png', 90, 1),
('食品饮料', 0, 1, 'food.png', 80, 1),
('家用电器', 0, 1, 'appliance.png', 70, 1),
('计算机', 1, 2, 'computer.png', 95, 1),
('手机', 1, 2, 'phone.png', 90, 1),
('笔记本', 5, 3, 'laptop.png', 85, 1),
('台式机', 5, 3, 'desktop.png', 80, 1);