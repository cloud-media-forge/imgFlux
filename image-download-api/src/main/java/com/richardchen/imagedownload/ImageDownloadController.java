package com.richardchen.imagedownload;

import com.richardchen.imageprocessing.ImageProcessingService;
import com.richardchen.imageservice.cdn.CdnService;
import com.richardchen.imageservice.storage.ObjectStorageService;
import com.richardchen.imageservice.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/download")
public class ImageDownloadController {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private ObjectStorageService objectStorageService;
    
    @Autowired
    private CdnService cdnService;
    
    @Value("${minio.bucket:original-image}")
    private String bucketName;
    
    /**
     * 查看图片 - 直接用图片路径作为参数从MinIO服务器获取原图
     */
    @GetMapping("/view/**")
    public ResponseEntity<byte[]> viewImage(HttpServletRequest request) {
        try {
            // 从请求路径中提取图片路径
            String requestUri = request.getRequestURI();
            String imagePath = requestUri.substring("/api/download/view/".length());
            
            // 从MinIO下载图片
            byte[] imageData = objectStorageService.downloadFile(bucketName, imagePath);
            
            // 确定内容类型
            String contentType = "image/jpeg"; // 默认值
            if (imagePath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (imagePath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageData.length);
            
            return new ResponseEntity<>(imageData, headers, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 下载图片 - 先用图片路径参数从MinIO服务器获取原图，
     * 存储在本地文件系统，再调用imageengine进行图片压缩处理等
     */
    @GetMapping("/download/**")
    public ResponseEntity<byte[]> downloadImage(
            HttpServletRequest request,
            @RequestParam(value = "width", required = false, defaultValue = "0") int width,
            @RequestParam(value = "height", required = false, defaultValue = "0") int height,
            @RequestParam(value = "quality", required = false, defaultValue = "80") int quality,
            @RequestParam(value = "extent", required = false, defaultValue = "false") boolean extent,
            @RequestParam(value = "trim", required = false, defaultValue = "false") boolean trim,
            @RequestParam(value = "format", required = false, defaultValue = "JPG") String format) {
        
        try {
            // 从请求路径中提取图片路径
            String requestUri = request.getRequestURI();
            String imagePath = requestUri.substring("/api/download/download/".length());
            
            // 从MinIO下载原图
            byte[] originalImageData = objectStorageService.downloadFile(bucketName, imagePath);
            
            // 如果没有指定处理参数，则直接返回原图
            if (width == 0 && height == 0 && quality == 80 && !extent && !trim && 
                ("JPG".equalsIgnoreCase(format) || "JPEG".equalsIgnoreCase(format))) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(getContentType(format)));
                headers.setContentLength(originalImageData.length);
                return new ResponseEntity<>(originalImageData, headers, HttpStatus.OK);
            }
            
            // 使用图片处理服务处理图片
            byte[] processedImageData = imageProcessingService.processImage(
                    originalImageData, width, height, quality, extent, trim, format);
            
            // 确定内容类型
            String contentType = getContentType(format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            if (processedImageData != null) {
                headers.setContentLength(processedImageData.length);
            }
            
            return new ResponseEntity<>(processedImageData, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            e.printStackTrace();
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