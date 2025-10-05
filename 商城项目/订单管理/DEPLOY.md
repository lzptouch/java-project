# 订单管理微服务部署说明

## 项目概述

本项目是一个基于Spring Boot的订单管理微服务，支持订单的创建、查询、支付、发货、确认收货等全生命周期管理功能。项目采用微服务架构设计，支持Docker容器化部署。

## 技术栈

- **后端框架**: Spring Boot 2.7.x + Spring Cloud
- **ORM框架**: MyBatis-Plus 3.5.x
- **数据库**: MySQL 8.0
- **缓存**: Redis 6.2
- **消息队列**: RabbitMQ 3.8
- **服务发现**: Nacos
- **API文档**: Knife4j (Swagger)
- **容器化**: Docker + Docker Compose

## 项目结构

```
order-service/
├── src/
│   ├── main/
│   │   ├── java/com/example/order/    # 源代码目录
│   │   │   ├── controller/            # 控制器层
│   │   │   ├── service/               # 服务层
│   │   │   ├── mapper/                # Mapper层
│   │   │   ├── entity/                # 实体类
│   │   │   ├── dto/                   # 数据传输对象
│   │   │   ├── config/                # 配置类
│   │   │   ├── task/                  # 定时任务
│   │   │   └── OrderServiceApplication.java  # 启动类
│   │   └── resources/                 # 资源目录
│   │       ├── mapper/                # MyBatis映射文件
│   │       └── application.yml        # 应用配置
├── sql/
│   └── init.sql                       # 数据库初始化脚本
├── pom.xml                            # Maven配置
├── Dockerfile                         # Docker构建文件
└── docker-compose.yml                 # Docker Compose配置
```

## 环境要求

- **Docker**: 19.03+  
- **Docker Compose**: 1.25+
- **JDK**: 1.8+（本地开发需要）
- **Maven**: 3.6+（本地构建需要）

## Docker部署步骤

### 1. 克隆项目（如果有Git仓库）

```bash
git clone <项目仓库地址>
cd order-service
```

### 2. 本地构建（可选）

如果需要本地构建JAR包：

```bash
# 编译打包
mvn clean package -DskipTests
```

### 3. 使用Docker Compose启动

项目已配置好`docker-compose.yml`，可以一键启动所有服务：

```bash
# 启动所有服务
 docker-compose up -d

# 查看启动日志
docker-compose logs -f
```

### 4. 验证服务是否正常启动

```bash
# 查看容器状态
docker-compose ps

# 访问API文档（默认端口8080）
# http://localhost:8080/doc.html
```

## 手动构建Docker镜像（可选）

如果需要单独构建Docker镜像：

```bash
# 构建镜像
docker build -t order-service:1.0.0 .

# 运行容器
docker run -d --name order-service -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://<mysql-host>:3306/order_service \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=123456 \
  -e SPRING_REDIS_HOST=<redis-host> \
  order-service:1.0.0
```

## 数据库配置说明

- **数据库名**: order_service
- **用户名**: root
- **密码**: 123456
- **端口**: 3306

数据库初始化脚本会自动创建所需的表结构和测试数据。

## 服务访问地址

- **订单服务API**: http://localhost:8080/api/orders
- **Swagger文档**: http://localhost:8080/doc.html
- **MySQL**: localhost:3306
- **Redis**: localhost:6379
- **RabbitMQ管理界面**: http://localhost:15672 (guest/guest)

## 主要API接口

- `POST /api/orders` - 创建订单
- `GET /api/orders/{orderId}` - 获取订单详情
- `GET /api/orders` - 查询订单列表
- `PUT /api/orders/{orderId}/cancel` - 取消订单
- `POST /api/orders/{orderId}/pay-success` - 支付成功回调
- `PUT /api/orders/{orderId}/ship` - 订单发货
- `PUT /api/orders/{orderId}/confirm-receive` - 确认收货

## 常见问题排查

1. **服务无法启动**
   - 检查Docker和Docker Compose版本
   - 查看日志：`docker-compose logs order-service`
   - 确认端口未被占用

2. **数据库连接失败**
   - 检查MySQL容器是否正常运行
   - 确认数据库配置正确
   - 查看数据库连接日志

3. **Redis连接失败**
   - 检查Redis容器是否正常运行
   - 确认Redis密码配置正确

## 扩展说明

### 1. 多环境配置

可以通过修改`docker-compose.yml`中的环境变量来适应不同环境：

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod  # 切换为生产环境配置
```

### 2. 数据持久化

Docker Compose配置中已设置数据卷持久化，确保容器重启后数据不会丢失。

### 3. 服务扩展

可以通过修改Docker Compose配置扩展服务实例数量：

```bash
docker-compose up -d --scale order-service=3
```

## 注意事项

1. 生产环境部署时请修改默认密码
2. 建议配置合适的JVM参数以优化性能
3. 定期备份数据库数据
4. 建议配置监控和日志收集系统