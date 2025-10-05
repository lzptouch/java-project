# 基础镜像使用JDK 17
FROM openjdk:17-jdk-slim

# 作者信息
MAINTAINER Product Service Team

# 设置工作目录
WORKDIR /app

# 添加应用程序jar包
COPY target/product-service-1.0.0.jar app.jar

# 添加数据库脚本
COPY db/schema.sql /app/db/schema.sql

# 设置环境变量
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 暴露端口
EXPOSE 8081

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]