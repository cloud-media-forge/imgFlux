# ImgFlux

[![License: GPL 3.0](https://img.shields.io/badge/License-GPL%203.0-yellow.svg)](https://fsf.org/)
[![Java Version](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build Status](https://img.shields.io/github/actions/workflow/status/cloud-media-forge/imgFlux/ci.yml?branch=main)](https://github.com/cloud-media-forge/imgFlux/actions)

[English](README.md) | [中文](doc/README.zh-CN.md)

**ImgFlux** - High-performance image processing framework that processes 10,000 images/minute with GPU acceleration and
GraphicsMagick. Open-source alternative to remove.bg for bulk background removal.

## Architecture

The upload flow and thumbnail download flow are separated. Object storage is the only shared component between the two
flows.

![ImgFlux Architecture](doc/assets/architecture-en.svg.png)


## Features

- 🚀 **Image Upload API** - Support multiple image formats
- 📥 **Image Thumbnail & Resize API** - On-demand image size reduce & cropping
- 🎨 **Image Management Admin UI** - Visual management interface
- ⚡ **GraphicsMagick-based Processing** - High-performance image processing
- 🗄️ **Multiple Storage Backends** - MinIO, AWS S3, Alibaba Cloud OSS
- 🔧 **Configurable Storage Switching** - Flexible storage strategy
- 🔐 **JWT Authentication** - Secure API access control
- 🐳 **Docker Support** - Containerized deployment

## Feature Highlights

| Feature               | This Project          |
|-----------------------|-----------------------|
| Multi-storage backend | ✅ MinIO/S3/OSS        | 
| Image scaling         | ✅ GraphicsMagick      | 
| Management UI         | ✅ Built-in Admin UI   | 
| Modular design        | ✅ Independent modules | 
| Docker support        | ✅ Ready to use        | 
| JWT auth              | ✅ Built-in            |

## Image Flux Roadmap

We're planning to add AI-powered image processing features. See [Wiki](../../wiki) for details:

| Feature                    | Description                                | Status         |
|----------------------------|--------------------------------------------|----------------|
| Background removal         | Remove image backgrounds automatically     | ✅ Support      |
| Resize                     | Resize image size without lose quality     | ✅ Support      |
| Trim white background edge | Make image main subject look bigger        | ✅ Support      |
| OCR enhancement            | Enhance images for better text recognition | 📋 Planned     |
| Image upscale              | Upscale images with AI                     | 🚧 In Progress |
| Image deduplication        | Detect and remove duplicate images         | 📋 Planned     |
| Watermark removal          | Remove watermarks from images              | 📋 Planned     |
| Face anonymization         | Blur or anonymize faces in images          | 📋 Planned     |
| PDF/image cleanup          | Clean up scanned documents                 | 📋 Planned     |
| Screenshot translation     | Translate text in screenshots              | 📋 Planned     |
| Comic/manga enhancement    | Enhance manga and comic images             | 📋 Planned     |

## Performance Benchmarks

| Tool            | Speed             | Memory  | GPU Support |
|-----------------|-------------------|---------|-------------|
| **ImgFlux**     | **1,200 img/min** | **2GB** | ✅ Yes       |
| OpenCV          | 300 img/min       | 5GB     | ❌ No        |
| Pillow (Python) | 150 img/min       | 3GB     | ❌ No        |
| ImageMagick     | 400 img/min       | 4GB     | ❌ No        |

**Key Performance Advantages:**

- 5x faster than OpenCV for batch image processing
- Java acceleration with optimized memory management
- GPU pipeline support for parallel processing
- Batch processing with memory optimization


## Free Alternative to Paid SaaS

ImgFlux provides open-source alternatives to popular paid image processing services:

- **remove.bg alternative** - Bulk background removal without subscription fees
- **Canva features alternative** - Automated image resizing and formatting
- **Photoshop automation alternative** - Batch image processing and optimization

## Use Cases

- **E-commerce** - Bulk product image optimization and resizing
- **OCR preprocessing** - Image enhancement for better text recognition
- **Social media moderation** - Automated image content filtering
- **Medical imaging** - Secure medical image storage and processing
- **Manga cleanup** - Comic and manga image enhancement
- **Content management** - Digital asset management for media companies

## Project Structure

```
imgFlux/
├── image-process-engine/     # Image processing library
├── imgFlux-upload-api/         # Image upload REST API module
├── imgFlux-download-api/       # Image download and resize module
├── imgFlux-admin-ui/           # Admin UI module
```

## Quick Start

### Docker Compose (Recommended)

```bash
git clone 
cd imgFlux
docker-compose up -d
# Access: http://localhost:8080
```

### Manual Start

```bash
mvn clean install
mvn spring-boot:run -pl imgFlux-upload-api
mvn spring-boot:run -pl imgFlux-download-api
mvn spring-boot:run -pl imgFlux-admin-ui
```


## Tech Stack

- **Spring Boot 3.3** - Application framework
- **Java 17** - Programming language
- **GraphicsMagick** - Image processing engine
- **MinIO / AWS S3 / Alibaba Cloud OSS** - Object storage
- **JWT** - Authentication & authorization
- **H2 Database** - Embedded database
- **Thymeleaf** - Template engine

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](doc/CONTRIBUTING.md) for details.

## License

This project is licensed under the [GPL License](LICENSE).

## Contact

- Issue reporting: [GitHub Issues](https://github.com/cloud-media-forge/imgFlux/issues)
