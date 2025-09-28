# Image Service

基于Spring Boot 3.3的图片服务，提供图片上传、下载和管理功能。

## 功能特性

1. **图片处理模块**：
   - 基于GraphicsMagick图片处理引擎
   - 使用im4java和gm4java库
   - 基于gm4java的GraphicsMagick进程池管理
   - 提供公共的图片处理方法

2. **图片上传模块**：
   - REST API接口
   - 支持JPG, JPEG, PNG, GIF, AVIF格式
   - 最大10M文件限制
   - 支持多种对象存储（MinIO, AWS S3, 阿里云OSS）
   - 基于哈希值的文件去重机制
   - 自动推送到CDN

3. **图片下载和Resize模块**：
   - REST API接口
   - 支持图片参数处理（宽度、高度、质量等）
   - CDN优先获取机制

4. **图片管理Admin UI**：
   - 用户注册和登录
   - 图片列表查看和预览
   - 团队/公司目录结构

## 技术栈

- Spring Boot 3.3
- Spring MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- GraphicsMagick + im4java + gm4java
- MinIO Client
- AWS S3 SDK
- Alibaba Cloud OSS SDK

## 项目结构

```
src/main/java/com/example/imageservice/
├── ImageServiceApplication.java    # 主应用类
├── config/                         # 配置类
├── controller/                     # 控制器
├── entity/                         # 实体类
├── repository/                     # 数据访问层
├── service/                        # 业务逻辑层
├── imageprocessing/                # 图片处理模块
├── storage/                        # 存储服务模块
├── cdn/                            # CDN服务模块
└── util/                           # 工具类
```

## 配置要求

1. **GraphicsMagick**：需要安装GraphicsMagick并配置到系统路径
2. **对象存储**：根据配置选择MinIO、AWS S3或阿里云OSS
3. **数据库**：默认使用H2内存数据库，可配置其他数据库

## 安装和运行

1. 克隆项目：
   ```
   git clone <repository-url>
   ```

2. 安装依赖：
   ```
   mvn clean install
   ```

3. 运行应用：
   ```
   mvn spring-boot:run
   ```

## API接口

### 图片上传
```
POST /api/upload
参数：
- file: 图片文件
- width: 目标宽度（可选）
- height: 目标高度（可选）
- quality: 图片质量（1-100，默认80）
- extent: 是否扩展图片（true/false，默认false）
- trim: 是否裁剪图片（true/false，默认false）
- format: 目标格式（JPG/JPEG/PNG/GIF/AVIF，默认保持原格式）
```

### 图片下载
```
GET /api/download/{hash}
参数：
- width: 目标宽度（可选）
- height: 目标高度（可选）
- quality: 图片质量（1-100，默认80）
- extent: 是否扩展图片（true/false，默认false）
- trim: 是否裁剪图片（true/false，默认false）
- format: 目标格式（JPG/JPEG/PNG/GIF/AVIF，默认JPG）
```

### 用户注册
```
POST /api/users/register
参数：
- username: 用户名
- password: 密码
- companyName: 公司名称
- teamName: 团队名称
```

## Admin UI

访问地址：`http://localhost:8080/admin/login`

功能：
- 用户登录/注册
- 图片列表查看
- 图片预览
- 图片上传

## 配置文件

配置文件位于：`src/main/resources/application.yml`

主要配置项：
- 服务器端口
- 数据库连接
- MinIO配置
- AWS S3配置
- 阿里云OSS配置
- CDN配置
- 图片处理配置
- GraphicsMagick线程池配置 (gm.pool-size，默认值16)

### GraphicsMagick线程池配置

```yaml
gm:
  pool-size: 16 # 线程池大小，默认值为16
```

该配置用于设置GraphicsMagick处理图片时使用的线程池大小。我们使用gm4java库的PooledGMService
来管理GraphicsMagick进程池，确保高效并发处理图片。

## 开发说明

1. 图片处理模块基于GraphicsMagick，确保系统已安装GraphicsMagick
2. 对象存储服务支持多种后端，通过Spring Profile切换
3. CDN服务使用简单的HTTP接口，可根据需要替换为专业CDN服务