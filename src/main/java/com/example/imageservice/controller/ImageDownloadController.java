package com.example.imageservice.controller;

import com.example.imageservice.cdn.CdnService;
import com.example.imageservice.imageprocessing.ImageProcessingService;
import com.example.imageservice.storage.ObjectStorageService;
import com.example.imageservice.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/download")
public class ImageDownloadController {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private ObjectStorageService objectStorageService;
    
    @Autowired
    private CdnService cdnService;
    
    @Value("${minio.bucket}")
    private String bucketName;
    
    /**
     * 下载并处理图片
     */
    @GetMapping("/{hash}/**")
    public ResponseEntity<byte[]> downloadImage(
            @PathVariable String hash,
            @RequestParam(value = "width", required = false, defaultValue = "0") int width,
            @RequestParam(value = "height", required = false, defaultValue = "0") int height,
            @RequestParam(value = "quality", required = false, defaultValue = "80") int quality,
            @RequestParam(value = "extent", required = false, defaultValue = "false") boolean extent,
            @RequestParam(value = "trim", required = false, defaultValue = "false") boolean trim,
            @RequestParam(value = "format", required = false, defaultValue = "JPG") String format) {
        
        try {
            String folderName = hash.substring(0, 4);
            String fileName = hash.substring(4) + "." + format.toLowerCase();
            String filePath = folderName + "/" + fileName;
            
            // 首先尝试从CDN获取图片
            byte[] imageData = cdnService.getFromCdn(filePath);
            if (imageData != null && imageData.length > 0) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(getContentType(format)));
                return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
            }
            
            // 如果CDN没有，则从原始存储获取并处理
            if (!objectStorageService.fileExists(bucketName, filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] originalImageData = objectStorageService.downloadFile(bucketName, filePath);
            
            // 处理图片
            byte[] processedImageData = imageProcessingService.processImage(
                    originalImageData, width, height, quality, extent, trim, format);
            
            // 推送到CDN
            cdnService.pushToCdn(filePath, processedImageData, getContentType(format));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(getContentType(format)));
            return new ResponseEntity<>(processedImageData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 根据格式获取内容类型
     */
    private String getContentType(String format) {
        switch (format.toUpperCase()) {
            case "JPG":
            case "JPEG":
                return "image/jpeg";
            case "PNG":
                return "image/png";
            case "GIF":
                return "image/gif";
            case "AVIF":
                return "image/avif";
            default:
                return "image/jpeg";
        }
    }
}