#!/bin/bash

# 测试原始图片上传（不处理）
echo "Testing raw image upload..."
curl -X POST http://localhost:8080/api/upload/raw \
  -F "file=@/Users/ghchen/IdeaProjects/image-service/sample.jpg" \
  -v

echo -e "\n\nTesting processed image upload..."
curl -X POST http://localhost:8080/api/upload \
  -F "file=@/Users/ghchen/IdeaProjects/image-service/sample.jpg" \
  -F "width=200" \
  -F "height=200" \
  -F "quality=80" \
  -F "format=PNG" \
  -v