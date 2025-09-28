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
    mvn spring-boot:run
else
    echo "Maven is not installed."
    echo "To run this application, please:"
    echo "1. Install Maven (https://maven.apache.org/install.html)"
    echo "2. Run 'mvn spring-boot:run' in the project directory"
    echo ""
    echo "Alternatively, you can build a JAR file and run it directly:"
    echo "1. Install Maven"
    echo "2. Run 'mvn clean package'"
    echo "3. Run 'java -jar target/image-service-0.0.1-SNAPSHOT.jar'"
fi