@echo off

rem 商品秒杀系统启动脚本
rem 设置控制台编码为UTF-8
chcp 65001

echo =========================================
echo 商品秒杀系统启动脚本
echo =========================================

rem 检查Java环境
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请确保已安装JDK并配置JAVA_HOME环境变量
    pause
    exit /b 1
)

echo 已检测到Java环境

rem 检查MySQL服务状态
sc query MySQL >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: MySQL服务未运行，请确保MySQL服务已启动
)

echo 检查MySQL服务完成

rem 检查Redis服务状态
sc query Redis >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: Redis服务未运行，请确保Redis服务已启动
)

echo 检查Redis服务完成

rem 检查RabbitMQ服务状态
sc query RabbitMQ >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告: RabbitMQ服务未运行，请确保RabbitMQ服务已启动
)

echo 检查RabbitMQ服务完成

rem 检查JAR文件是否存在
if not exist "target\seckill-1.0.0.jar" (
    echo 警告: JAR文件不存在，请先执行构建命令
    echo 正在执行构建...
    call mvn clean package -DskipTests
    if %errorlevel% neq 0 (
        echo 错误: 构建失败
        pause
        exit /b 1
    )
)

rem 启动应用程序
echo 正在启动商品秒杀系统...
java -jar -Xms512m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8 target\seckill-1.0.0.jar

rem 检查启动状态
if %errorlevel% neq 0 (
    echo 错误: 应用启动失败
    pause
    exit /b 1
)

echo 应用启动成功
pause