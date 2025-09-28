# 使用官方OpenJDK运行时作为基础镜像
FROM openjdk:17-jdk-slim

# 设置维护者信息
LABEL maintainer="image-service@example.com"

# 设置工作目录
WORKDIR /app

# 复制Maven依赖
COPY pom.xml .
COPY src ./src

# 安装Maven并构建应用程序
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 运行应用程序
ENTRYPOINT ["java", "-jar", "target/image-service-0.0.1-SNAPSHOT.jar"]