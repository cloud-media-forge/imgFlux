#!/bin/bash

# Image Service启动脚本

# 检查Java是否已安装
if ! command -v java &> /dev/null
then
    echo "Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# 检查Maven是否已安装
if command -v mvn &> /dev/null
then
    echo "Building with Maven..."
    mvn clean install
    
    # 启动各个服务模块
    echo "Starting Image Upload API service..."
    mvn spring-boot:run -pl image-upload-api &
    
    echo "Starting Image Download API service..."
    mvn spring-boot:run -pl image-download-api &
    
    echo "Starting Image Admin UI service..."
    mvn spring-boot:run -pl image-admin-ui &
    
    echo "All services started. Press Ctrl+C to stop."
    wait
else
    echo "Maven is not installed."
    echo "To run this application, please:"
    echo "1. Install Maven (https://maven.apache.org/install.html)"
    echo "2. Run 'mvn clean install' in the project directory"
    echo "3. Run each service separately:"
    echo "   mvn spring-boot:run -pl image-upload-api"
    echo "   mvn spring-boot:run -pl image-download-api"
    echo "   mvn spring-boot:run -pl image-admin-ui"
fi