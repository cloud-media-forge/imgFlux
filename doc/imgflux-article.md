# ImgFlux: 基于 GraphicsMagick 的高性能图片处理微服务

> 一个开箱即用的图片上传、实时缩放、OCR 文字翻译微服务，采用读写分离架构，支持 MinIO / AWS S3 / 阿里云 OSS 多种存储后端，Docker Compose 一键部署。

## 为什么做 ImgFlux?

在内容驱动的产品中，图片处理是一个看似简单却极其复杂的基础设施：

- 用户上传原图后，不同终端需要不同尺寸和格式的缩略图
- 跨国业务需要将图片上的文字实时翻译成目标语言
- 图片存储需要高可用、低成本，且随时可迁移
- 处理性能直接影响用户体验和服务器成本

现有的云服务（Cloudinary、Imgix）功能强大但价格高昂，且数据不在自己手中。**ImgFlux** 的目标是提供一个**可自托管、高性能、功能完整**的开源替代方案。

## 系统架构

ImgFlux 采用**读写分离**的微服务架构，将上传和下载拆分为独立服务，共享核心图片处理引擎：

```
                        ┌─────────────────────────────────────────┐
                        │           imgFlux-engine                │
                        │  ┌─────────────┐  ┌──────────────────┐  │
                        │  │ GM4Java     │  │ TextTranslation  │  │
                        │  │ Pool (GM)   │  │ (Tesseract OCR)  │  │
                        │  └─────────────┘  └──────────────────┘  │
                        └──────────┬──────────────┬───────────────┘
                                   │              │
              ┌────────────────────┘              └────────────────────┐
              │                                                        │
  ┌───────────┴────────────┐                        ┌─────────────────┴───────────┐
  │  Upload API (:8080)    │                        │  Download API (:8081)       │
  │  ┌──────────────────┐  │                        │  ┌───────────────────────┐  │
  │  │ REST Upload      │  │                        │  │ On-the-fly Resize    │  │
  │  │ Admin Dashboard  │  │                        │  │ Remote Image Fetch   │  │
  │  │ Raw Upload       │  │                        │  │ OCR Text Translation │  │
  │  └──────────────────┘  │                        │  └───────────────────────┘  │
  └───────────┬────────────┘                        └──────────────┬──────────────┘
              │                                                     │
              ▼                                                     ▼
  ┌──────────────────────────────────────────────────────────────────────────────┐
  │                    MinIO / AWS S3 / Alibaba OSS                             │
  └──────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                              ┌───────────┐
                              │    CDN    │
                              └───────────┘
```

### 模块职责

| 模块 | 职责 | 技术栈 |
|------|------|--------|
| **imgFlux-engine** | 核心图片处理引擎，GM 连接池，OCR 文字翻译 | GraphicsMagick, gm4java, Tesseract, Tess4J |
| **imgFlux-upload-api** | 图片上传、管理后台、文件管理 UI | Spring Boot 3.5, Thymeleaf, Spring Security |
| **imgFlux-download-api** | 实时缩略图生成、远程图片处理、URL 驱动 API | Spring Boot 3.5, RestTemplate |

## 核心特性

### 功能展示

| <div style="width:50px">功能</div>         | <div style="width:200px">预览</div>                                                               |
|------------------------------------------|-------------------------------------------------------------------------------------------------|
| **原图**                                   | ![origin](https://brand.github.com/_next/static/media/logo-03.cc5e5332.png)                     |
| **压缩到尺寸 200x200**                        | ![Resized](../doc/assets/logo-03.cc5e5332-500x500.png)                                          |
| **去掉主体四周的白色背景**                          | ![Trimmed](../doc/assets/logo-03.cc5e5332-trim.png)                                             |
| **扩展到指定尺寸**                              | ![Extended](../doc/assets/logo-03.cc5e5332-ex.png)                                              |
| **压缩质量到 5%**                             | ![Quality 5](../doc/assets/logo-03.cc5e5332-q5.png)                                             |
| **转换格式**                                 | ![Conver to WEBP format](../doc/assets/logo-03.cc5e5332-q5.webp)                                |
| **翻译图片上的文字** (Korean→English) |  ![Translated to English](../imgFlux-engine/src/test/resources/translate/C2-1BIG-en-result.jpg) |
| **翻译图片上的文字** (Korean→Chinese)            | ![Translated to Chinese](../imgFlux-engine/src/test/resources/translate/output/C2-1BIG-zh-CN-result.jpg) |



### 1. URL 驱动的实时缩放

Download API 提供两种风格的 URL 接口，适合 CDN 缓存和前端直接拼接：

**Query 参数风格：**
```
GET /api/v1/thumbnail/forge/local/abcd/image.jpg?width=300&height=200&quality=85&format=WEBP
```

**路径参数风格（CDN 友好）：**
```
GET /api/v1/thumbnail/resize/local/300x200q85/abcd/image.jpg
```

路径参数格式说明：

| 参数 | 格式 | 示例 | 说明 |
|------|------|------|------|
| 尺寸 | `WxH` | `300x200` | 宽x高，可省略其一 |
| 质量 | `q85` | `q80` | JPEG/WebP 压缩质量 |
| 扩展 | `ex` | `ex` | 强制扩展到指定尺寸 |
| 裁剪 | `trim` | `trim` | 去除空白边缘 |
| 翻译 | `rans:src:dst` | `rans:ko:en` | OCR 文字翻译 |

### 2. 远程图片处理

支持直接处理远程 URL 上的图片，无需先下载再上传：

```
GET /api/v1/thumbnail/forge/remote/https://example.com/photo.jpg?width=400&format=AVIF
```

这使得 ImgFlux 可以作为**图片代理**使用，对第三方图片进行实时处理和格式转换。

### 3. OCR 图片文字翻译

这是 ImgFlux 最独特的功能之一。通过 Tesseract OCR 检测图片上的文字，调用 Google Translate 翻译后，用双线性插值算法自然擦除原文并渲染译文：

```
GET /api/v1/thumbnail/forge/local/abcd/korean-banner.jpg?srcLang=ko&toLang=en
```

处理流程：

```
原图 → Tesseract OCR 检测文字区域
     → 按 Unicode 范围过滤非目标语言文字
     → 调用 Google Translate 翻译
     → 双线性插值采样周围像素，自然擦除原文
     → Java2D 自适应字号渲染译文
     → 输出处理后图片
```

支持的语言：韩语、日语、英语、西班牙语、法语、德语、简体中文、繁体中文。

### 4. 内容寻址存储

上传时使用 SHA-256 对处理后的图片内容计算哈希，前 4 位作为目录名，其余作为文件名：

```
SHA-256: a3f2b8c9d1e4...
存储路径: a3f2/b8c9d1e4...jpg
```

这种设计带来两个好处：
- **天然去重**：相同内容的图片自动复用存储路径
- **均匀分布**：哈希前缀作为目录，避免单目录文件过多

### 5. 多存储后端

通过 `ObjectStorageService` 接口统一抽象，只需修改配置文件即可切换存储后端：

```yaml
img-flux:
  storage:
    type: minio  # 可选: minio, aws, aliyun
    options:
      minio:
        endpoint: http://localhost:9000
        bucket: original-image
```

启动时自动检查并创建 Bucket，开箱即用。

## 快速部署

### Docker Compose 一键启动

```bash
docker-compose up -d
```

`docker-compose.yml` 包含三个服务：

| 服务 | 端口 | 说明 |
|------|------|------|
| MinIO | 9000/9001 | 对象存储 + 管理控制台 |
| Upload API | 8081 | 图片上传与管理后台 |
| Download API | 8082 | 实时缩略图服务 |

### API 使用示例

**上传原图：**
```bash
curl -X POST http://localhost:8081/api/v1/upload/raw \
  -F "file=@photo.jpg"
# 返回: Raw image uploaded successfully. Object name: a3f2/b8c9d1e4....jpg
```

**上传并处理：**
```bash
curl -X POST http://localhost:8081/api/v1/upload \
  -F "file=@photo.jpg" \
  -F "width=800" \
  -F "quality=85" \
  -F "format=WEBP"
```

**实时缩放：**
```
http://localhost:8082/api/v1/thumbnail/resize/local/400x300q85/a3f2/b8c9d1e4....jpg
```

**远程图片处理 + 文字翻译：**
```
http://localhost:8082/api/v1/thumbnail/forge/remote/https://example.com/banner.jpg?width=600&srcLang=ko&toLang=en
```

## 技术栈总览

| 层次 | 技术 |
|------|------|
| 语言 | Java 17 |
| 框架 | Spring Boot 3.5 |
| 图片处理 | GraphicsMagick + gm4java + im4java |
| OCR | Tesseract 5 + Tess4J |
| 对象存储 | MinIO / AWS S3 / 阿里云 OSS |
| 前端 | Thymeleaf + Spring Security |
| 容器化 | Docker + Docker Compose |
| 构建 | Maven 多模块 |

## 项目结构

```
imgFlux/
├── pom.xml                          # 父 POM，统一依赖版本管理
├── docker-compose.yml               # 一键部署
├── imgFlux-engine/                  # 核心引擎（共享模块）
│   ├── gm/                          # GraphicsMagick 处理
│   │   ├── Gm4JavaBatchCommand      # GM 连接池管理
│   │   ├── GMImageProcessor         # 图片处理器
│   │   └── ImageProcessingService   # 处理服务接口
│   ├── service/
│   │   ├── storage/                 # 存储抽象层
│   │   │   ├── ObjectStorageService # 统一存储接口
│   │   │   ├── MinIOStorageService  # MinIO 实现
│   │   │   ├── AwsS3StorageService  # AWS S3 实现
│   │   │   └── AliyunOssStorageService # 阿里云 OSS 实现
│   │   └── translate/
│   │       └── TextTranslationService # OCR 文字翻译
│   └── config/                      # 配置类
├── imgFlux-upload-api/              # 上传服务
│   ├── controller/api/              # REST API
│   ├── controller/web/              # 管理后台
│   └── resources/templates/         # Thymeleaf 页面
└── imgFlux-download-api/            # 下载/缩放服务
    ├── api/                         # 缩略图 API
    └── utils/                       # URL 参数解析
```

## 总结

ImgFlux 是一个面向实战的图片处理微服务，它解决了几个核心痛点：

- **性能**：通过 GM 连接池避免进程级开销，适合高并发场景
- **灵活性**：URL 驱动的 API 设计，前端按需拼接参数，CDN 友好
- **差异化**：内置 OCR 文字翻译，适合跨境电商和内容本地化场景
- **可部署性**：Docker Compose 一键启动，存储后端可插拔

项目仍在持续迭代中，欢迎 Star 和贡献代码。

---

**项目地址：** [GitHub - ImgFlux](https://github.com/cloud-media-forge/imgFlux)

**技术栈：** Java 17 / Spring Boot 3.5 / GraphicsMagick / Tesseract OCR / MinIO / Docker

**许可协议：** Open Source
