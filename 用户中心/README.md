# 用户中心 - 单点登录与权限验证系统

本项目实现了完整的单点登录(SSO)和权限验证功能，采用Spring Boot + Spring Security + JWT + Redis技术栈，基于RBAC(基于角色的访问控制)模型设计。

## 功能特性

### 单点登录功能
- 基于JWT的令牌认证机制
- 支持令牌刷新
- 支持用户登出和令牌吊销
- 使用Redis存储令牌黑名单

### 权限验证功能
- 基于RBAC模型的权限管理
- 自定义注解实现方法级权限控制
- 支持角色和权限的灵活配置
- 提供权限验证API接口

## 技术栈

- **后端框架**：Spring Boot 2.7.13
- **安全框架**：Spring Security
- **持久层**：Spring Data JPA
- **数据库**：MySQL
- **缓存**：Redis
- **认证**：JWT (JSON Web Token)
- **构建工具**：Maven

## 项目结构

```
src/main/java/com/example/usercenter/
├── UserCenterApplication.java       # 应用主入口
├── annotation/                      # 自定义注解
│   └── RequirePermission.java       # 权限验证注解
├── aspect/                          # AOP切面
│   └── PermissionAspect.java        # 权限验证切面
├── config/                          # 配置类
│   └── SecurityConfig.java          # Spring Security配置
├── controller/                      # 控制器
│   ├── AuthController.java          # 认证控制器
│   ├── UserController.java          # 用户控制器
│   └── UserAdminController.java     # 用户管理控制器
├── entity/                          # 实体类
│   ├── SysUser.java                 # 用户实体
│   ├── SysRole.java                 # 角色实体
│   └── SysPermission.java           # 权限实体
├── exception/                       # 异常处理
│   └── GlobalExceptionHandler.java  # 全局异常处理器
├── filter/                          # 过滤器
│   └── JwtAuthenticationFilter.java # JWT认证过滤器
├── init/                            # 初始化
│   └── DataInitializer.java         # 数据初始化类
├── repository/                      # 数据访问层
│   ├── SysUserRepository.java       # 用户数据访问接口
│   ├── SysRoleRepository.java       # 角色数据访问接口
│   └── SysPermissionRepository.java # 权限数据访问接口
└── service/                         # 业务逻辑层
    ├── SSOService.java              # 单点登录服务接口
    ├── SysUserService.java          # 用户服务接口
    ├── PermissionService.java       # 权限服务接口
    └── impl/                        # 服务实现类
        ├── SSOServiceImpl.java      # 单点登录服务实现
        ├── SysUserServiceImpl.java  # 用户服务实现
        └── PermissionServiceImpl.java # 权限服务实现
```

## 数据库设计

项目使用了RBAC模型的数据库设计，包含以下表：

- **sys_user**: 用户表
- **sys_role**: 角色表
- **sys_permission**: 权限表
- **sys_user_role**: 用户-角色关联表
- **sys_role_permission**: 角色-权限关联表

详细的表结构可以参考项目中的`rbac.sql`文件。

## API接口说明

### 认证相关接口

#### 用户登录
- **URL**: `/api/auth/login`
- **方法**: POST
- **请求体**: `{"username": "用户名", "password": "密码"}`
- **返回**: 包含访问令牌和刷新令牌的JSON对象

#### 刷新令牌
- **URL**: `/api/auth/refresh`
- **方法**: POST
- **请求体**: `{"refresh_token": "刷新令牌"}`
- **返回**: 新的访问令牌

#### 用户登出
- **URL**: `/api/auth/logout`
- **方法**: POST
- **请求头**: `Authorization: Bearer 访问令牌`

#### 验证令牌
- **URL**: `/api/auth/validate`
- **方法**: GET
- **请求头**: `Authorization: Bearer 访问令牌`
- **返回**: 令牌是否有效

### 用户相关接口

#### 获取当前用户信息
- **URL**: `/api/users/current`
- **方法**: GET
- **请求头**: `Authorization: Bearer 访问令牌`
- **返回**: 当前登录用户信息

#### 获取当前用户权限列表
- **URL**: `/api/users/permissions`
- **方法**: GET
- **请求头**: `Authorization: Bearer 访问令牌`
- **权限**: 需要`user:view`权限
- **返回**: 用户拥有的权限列表

#### 获取当前用户角色列表
- **URL**: `/api/users/roles`
- **方法**: GET
- **请求头**: `Authorization: Bearer 访问令牌`
- **权限**: 需要`user:view`权限
- **返回**: 用户拥有的角色列表

### 用户管理接口

#### 创建用户
- **URL**: `/api/admin/users`
- **方法**: POST
- **请求头**: `Authorization: Bearer 访问令牌`
- **权限**: 需要`user:create`权限
- **请求体**: 用户信息
- **返回**: 创建的用户信息

#### 更新用户
- **URL**: `/api/admin/users/{id}`
- **方法**: PUT
- **请求头**: `Authorization: Bearer 访问令牌`
- **权限**: 需要`user:update`权限
- **请求体**: 更新的用户信息
- **返回**: 更新后的用户信息

#### 删除用户
- **URL**: `/api/admin/users/{id}`
- **方法**: DELETE
- **请求头**: `Authorization: Bearer 访问令牌`
- **权限**: 需要`user:delete`权限

## 使用说明

### 前置条件

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+

### 配置修改

1. 修改`application.yml`中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_center?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
```

2. 修改Redis连接信息：
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    password:
```

3. 可选：修改JWT相关配置：
```yaml
jwt:
  secret: sso-server-secret-key-1234567890abcdefghijklmnopqrstuvwxyz
  expiration: 7200000  # 2小时
  refresh-expiration: 2592000000  # 30天
```

### 项目构建和运行

1. 编译项目：
```bash
mvn clean package
```

2. 运行项目：
```bash
java -jar target/user-center-1.0.0.jar
```

### 初始数据

系统启动时会自动初始化以下测试数据：

- **管理员账户**：
  - 用户名：admin
  - 密码：admin123
  - 角色：ADMIN（拥有所有权限）

- **普通用户账户**：
  - 用户名：user
  - 密码：user123
  - 角色：USER（仅拥有查看权限）

## 安全说明

1. 密码使用BCrypt算法加密存储
2. JWT令牌设置了合理的过期时间
3. 实现了令牌黑名单机制，支持强制下线
4. 使用Redis存储敏感数据，提升安全性和性能
5. 实现了细粒度的权限控制，可以精确到方法级别

## 扩展建议

1. 添加更多的权限管理功能，如动态权限分配
2. 集成第三方登录，如OAuth2.0
3. 添加用户操作日志记录
4. 实现多租户支持
5. 添加API文档生成（如Swagger）