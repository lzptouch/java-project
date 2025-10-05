# 使用Java 8基础镜像
FROM openjdk:8-jdk-alpine

# 维护者信息
LABEL maintainer="order-service"

# 设置工作目录
WORKDIR /app

# 复制jar包到容器中
COPY target/order-service-1.0.0.jar app.jar

# 复制数据库脚本到容器中
COPY sql/init.sql /app/sql/init.sql

# 暴露服务端口
EXPOSE 8080

# 环境变量
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]