# Image Service

基于Spring Boot 3.3的图片服务，包含图片上传、下载API服务和图片管理Admin UI。

## 功能特性

- 图片上传API
- 图片下载和缩放API
- 图片管理Admin UI
- 基于GraphicsMagick的图片处理
- 支持多种对象存储服务（MinIO、AWS S3、阿里云OSS）
- 可配置的存储服务切换

## 项目结构

```
image-service/
├── image-process-engine/     # 图片处理公共库
├── image-upload-api/         # 图片上传REST API模块
├── image-download-api/       # 图片下载和resize模块
├── image-admin-ui/           # Admin UI模块
```

## 配置存储服务

在`application.yml`中配置存储服务类型：

```yaml
# Storage configuration
storage:
  type: minio # 可选值: minio, aws, aliyun

# MinIO configuration
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: original-image

# AWS S3 configuration
aws:
  s3:
    access-key-id: your-access-key-id
    secret-access-key: your-secret-access-key
    region: us-east-1
    bucket: your-bucket-name

# Alibaba Cloud OSS configuration
alibaba:
  oss:
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    endpoint: oss-cn-hangzhou.aliyuncs.com
    bucket: your-bucket-name
```

### MinIO配置示例

```yaml
storage:
  type: minio

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: original-image
```

### AWS S3配置示例

```yaml
storage:
  type: aws

aws:
  s3:
    access-key-id: your-access-key-id
    secret-access-key: your-secret-access-key
    region: us-east-1
    bucket: your-bucket-name
```

### 阿里云OSS配置示例

```yaml
storage:
  type: aliyun

alibaba:
  oss:
    access-key-id: your-access-key-id
    access-key-secret: your-access-key-secret
    endpoint: oss-cn-hangzhou.aliyuncs.com
    bucket: your-bucket-name
```

## 构建和运行

```bash
# 构建项目
mvn clean install

# 运行各个服务
mvn spring-boot:run -pl image-upload-api
mvn spring-boot:run -pl image-download-api
mvn spring-boot:run -pl image-admin-ui
```

## 使用说明

1. 根据需要配置`application.yml`中的存储服务类型和参数
2. 启动所有服务模块
3. 通过Admin UI管理图片或直接调用API服务