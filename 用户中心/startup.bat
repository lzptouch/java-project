@echo off

rem 用户中心启动脚本

echo ===================================
echo      用户中心 - 单点登录与权限验证系统

echo 正在启动应用程序...

rem 检查Java是否安装
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未找到Java环境，请先安装JDK 1.8或更高版本
    pause
    exit /b 1
)

rem 检查MySQL是否运行
netstat -ano | findstr 3306 >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告：未检测到MySQL服务运行，请确保MySQL已启动
)

rem 检查Redis是否运行
netstat -ano | findstr 6379 >nul 2>&1
if %errorlevel% neq 0 (
    echo 警告：未检测到Redis服务运行，请确保Redis已启动
)

rem 启动应用程序
set JAR_FILE=target/user-center-1.0.0.jar

if exist %JAR_FILE% (
    java -jar %JAR_FILE%
) else (
    echo 错误：未找到可执行JAR文件。请先运行 'mvn clean package' 构建项目
    pause
    exit /b 1
)

pause