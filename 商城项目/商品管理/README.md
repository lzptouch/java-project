# 商品管理微服务

## 项目介绍

本项目是一个基于Spring Boot的商品管理微服务，提供商品的CRUD操作、分类管理、库存管理等功能，采用微服务架构设计，支持独立部署。

## 技术栈

- **后端框架**：Spring Boot 2.7.x
- **ORM框架**：MyBatis-Plus 3.5.x
- **数据库**：MySQL 8.0
- **缓存**：Redis 7.0
- **服务发现与配置**：Nacos 2.1.x
- **消息队列**：RabbitMQ 3.9.x
- **API文档**：Knife4j(Swagger) 3.0.x
- **Docker**：支持容器化部署

## 项目结构

```
src/main/java/com/example/product/
├── config/           # 配置类
├── controller/       # 控制器层
├── dto/              # 数据传输对象
├── entity/           # 实体类
├── mapper/           # MyBatis映射器
├── service/          # 服务层
│   └── impl/         # 服务实现类
├── util/             # 工具类
└── ProductApplication.java  # 应用启动类
```

## 核心功能

1. **商品管理**：
   - 商品创建、更新、删除
   - 商品列表查询（支持分页、条件查询）
   - 商品详情查询
   - 商品上架/下架
   - 商品库存管理

2. **分类管理**：
   - 分类创建、更新、删除
   - 分类树结构获取
   - 分类启用/禁用

3. **规格管理**：
   - 商品规格创建、更新、删除
   - 规格库存管理

## 快速开始

### 本地开发环境

#### 前提条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7.0+
- Nacos 2.1.0+
- RabbitMQ 3.9+

#### 配置步骤

1. 克隆项目到本地

2. 配置数据库
   - 创建数据库：`product_service`
   - 导入数据库脚本：`db/schema.sql`

3. 修改配置文件
   - 编辑 `application.yml`，配置数据库、Redis、Nacos、RabbitMQ连接信息

4. 启动服务
   - 运行 `ProductApplication.java`
   - 服务启动后可访问：http://localhost:8081/doc.html 查看API文档

### Docker部署

#### 前提条件

- Docker 20.10+
- Docker Compose 1.29+

#### 部署步骤

1. 构建项目

```bash
mvn clean package -DskipTests
```

2. 使用Docker Compose启动

```bash
docker-compose up -d
```

3. 访问服务
   - 服务地址：http://localhost:8081
   - API文档：http://localhost:8081/doc.html

## Docker相关命令

### 构建镜像

```bash
docker build -t product-service:1.0.0 .
```

### 运行容器

```bash
docker run -d --name product-service -p 8081:8081 product-service:1.0.0
```

### 查看日志

```bash
docker logs -f product-service
```

## API文档

服务启动后可访问 Swagger API 文档：
http://localhost:8081/doc.html

## 注意事项

1. 首次启动时，数据库会自动初始化基础数据
2. 生产环境中请修改默认的数据库密码和Redis密码
3. 建议在生产环境中配置HTTPS
4. 请根据实际需求调整JVM参数以优化性能

## 许可证

MIT