# 商品秒杀系统

## 项目概述
本项目是一个高性能、分布式的商品秒杀系统，基于Spring Cloud微服务架构实现，具有高并发处理能力和分布式事务支持。

## 技术栈
- **后端框架**：Spring Boot 2.7.13 + Spring Cloud 2021.0.8
- **微服务组件**：Spring Cloud Alibaba Nacos (服务注册发现)
- **分布式事务**：Seata 1.6.1
- **数据访问**：Spring Data JPA
- **缓存**：Redis + Caffeine
- **消息队列**：RabbitMQ
- **限流熔断**：Resilience4j
- **容器化**：Docker + Docker Compose
- **数据库**：MySQL 8.0

## 数据库设计

### 数据库结构概览
系统包含8个核心数据表：

1. **用户表(user)** - 存储用户信息
2. **商品表(product)** - 存储商品基本信息
3. **秒杀活动表(seckill_activity)** - 存储秒杀活动配置
4. **秒杀商品表(seckill_product)** - 存储参与秒杀的商品信息
5. **秒杀库存表(seckill_stock)** - 存储秒杀商品库存信息（乐观锁）
6. **秒杀订单表(seckill_order)** - 存储秒杀订单信息
7. **秒杀用户抢购记录表(seckill_user_record)** - 防止重复抢购
8. **系统配置表(system_config)** - 存储系统配置参数

### 数据库初始化
使用提供的 `seckill.sql` 文件初始化数据库：

```bash
# 在MySQL中执行以下命令
mysql -u root -p < seckill.sql
```

## 微服务架构

### 服务组成
- **seckill-service**：秒杀服务，负责商品秒杀的核心业务逻辑
- **Nacos**：服务注册发现中心
- **Seata**：分布式事务协调器
- **MySQL**：主数据库
- **Redis**：缓存层，用于库存预热和分布式锁
- **RabbitMQ**：消息队列，用于异步处理订单创建

### 核心功能模块

#### 1. 库存管理模块
- 库存预热：系统启动时将秒杀商品库存加载到Redis
- 乐观锁扣减：通过version字段实现库存扣减的并发控制
- 分布式锁：使用Redis实现分布式锁防止超卖

#### 2. 秒杀业务模块
- 接口限流：使用令牌桶算法限制接口访问频率
- 防重复抢购：记录用户抢购记录，防止重复下单
- 异步处理：订单创建异步化，提高系统吞吐量

#### 3. 分布式事务模块
- Seata AT模式：保证跨服务事务一致性
- 本地事务：关键业务操作使用本地事务保证数据一致性

## Docker部署

### 环境要求
- Docker 19.03+
- Docker Compose 1.28+

### 部署步骤

1. **构建Docker镜像**

```bash
cd d:\小米云盘\面试求职\项目\java\商城项目\商品秒杀
# 先构建项目
mvn clean package -DskipTests
# 构建Docker镜像
docker build -t seckill-service:1.0.0 .
```

2. **启动所有服务**

```bash
# 使用docker-compose启动所有服务
docker-compose up -d
```

3. **验证部署**

服务启动后，可以访问以下地址验证：
- Nacos控制台：http://localhost:8848/nacos
- 应用服务：http://localhost:8081

### 环境变量配置

主要配置参数通过 `application-docker.yml` 文件和Docker环境变量配置：

- 数据库连接：MySQL连接信息
- Redis配置：缓存服务器信息
- RabbitMQ配置：消息队列服务器信息
- Nacos配置：服务注册中心信息
- Seata配置：分布式事务配置

## 系统启动参数

### JVM参数配置
```
JAVA_OPTS="-Xms512m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
```

### 环境配置
```bash
# 启动本地环境
java -jar seckill-service-1.0.0.jar --spring.profiles.active=local

# 启动Docker环境
java -jar seckill-service-1.0.0.jar --spring.profiles.active=docker
```

## 项目结构说明

### 核心包结构
- `com.example.seckill` - 项目根包
  - `controller` - REST API接口层
  - `service` - 业务逻辑层
  - `repository` - 数据访问层
  - `entity` - 实体类
  - `config` - 配置类
  - `SeckillApplication.java` - 应用入口

### 数据库访问方式
本项目使用Spring Data JPA作为数据访问层框架，通过Repository接口定义查询方法。主要Repository包括：

- `SeckillStockRepository` - 秒杀库存数据访问
- `SeckillOrderRepository` - 秒杀订单数据访问
- `SeckillUserRecordRepository` - 用户抢购记录数据访问

## 常见问题与解决方案

### 问题1：高并发下库存超卖
**解决方案**：
- 使用Redis预减库存 + 数据库乐观锁双重验证
- 引入分布式锁控制并发访问

### 问题2：服务不可用
**解决方案**：
- 服务注册到Nacos实现自动发现和负载均衡
- 使用Resilience4j实现服务熔断和降级

### 问题3：分布式事务一致性
**解决方案**：
- 使用Seata AT模式管理分布式事务
- 关键操作使用@GlobalTransactional注解

## 安全注意事项

1. 接口限流：防止恶意请求导致系统崩溃
2. SQL注入防护：使用JPA参数化查询
3. 数据校验：所有输入数据进行严格校验
4. 敏感数据加密：用户密码等敏感信息加密存储

## 性能优化建议

1. 增加Redis缓存容量，优化缓存策略
2. 数据库读写分离，提高查询性能
3. 服务水平扩展，增加实例数量
4. 优化JVM参数，提高内存利用率

## 后续功能规划

1. 增加用户登录认证模块
2. 集成Spring Cloud Gateway实现API网关
3. 增加监控告警系统
4. 支持分布式部署的会话管理