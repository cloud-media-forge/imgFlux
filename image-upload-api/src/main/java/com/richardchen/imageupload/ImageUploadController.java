package com.richardchen.imageupload;

import com.richardchen.imageprocessing.ImageProcessingService;
import com.richardchen.imageservice.cdn.CdnService;
import com.richardchen.imageservice.storage.ObjectStorageService;
import com.richardchen.imageservice.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private ObjectStorageService objectStorageService;
    
    @Autowired
    private CdnService cdnService;
    
    @Value("${image.max-size}")
    private long maxFileSize;
    
    @Value("${image.supported-formats}")
    private String supportedFormats;
    
    @Value("${minio.bucket}")
    private String bucketName;
    
    // 支持的图片格式
    private List<String> getSupportedFormats() {
        return Arrays.asList(supportedFormats.split(","));
    }
    
    /**
     * 上传并处理图片
     */
    @PostMapping
    public ResponseEntity<String> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "width", required = false, defaultValue = "0") int width,
            @RequestParam(value = "height", required = false, defaultValue = "0") int height,
            @RequestParam(value = "quality", required = false, defaultValue = "80") int quality,
            @RequestParam(value = "extent", required = false, defaultValue = "false") boolean extent,
            @RequestParam(value = "trim", required = false, defaultValue = "false") boolean trim,
            @RequestParam(value = "format", required = false) String format) throws IOException {
        
        try {
            // 验证文件大小
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
            }
            
            // 验证文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            
            String fileExtension = getFileExtension(originalFilename).toUpperCase();
            if (!getSupportedFormats().contains(fileExtension)) {
                return ResponseEntity.badRequest().body("Unsupported file format: " + fileExtension);
            }
            
            // 如果未指定目标格式，则使用原格式
            if (format == null || format.isEmpty()) {
                format = fileExtension;
            } else {
                format = format.toUpperCase();
            }
            
            // 验证目标格式
            if (!getSupportedFormats().contains(format)) {
                return ResponseEntity.badRequest().body("Unsupported target format: " + format);
            }
            
            // 读取文件数据
            byte[] originalImageData = file.getBytes();
            
            // 处理图片
            byte[] processedImageData = imageProcessingService.processImage(
                    originalImageData, width, height, quality, extent, trim, format);
            
            // 计算哈希值
            String hash = HashUtil.calculateHash(processedImageData);
            String folderName = hash.substring(0, 4);
            String fileName = hash.substring(4) + "." + format.toLowerCase();
            String objectName = folderName + "/" + fileName;
            
            // 上传到对象存储
            String contentType = getContentType(format);
            objectStorageService.uploadFile(bucketName, objectName, processedImageData, contentType);
            
            // 推送到CDN服务器
            cdnService.pushToCdn(objectName, processedImageData, contentType);
            
            return ResponseEntity.ok("Image uploaded successfully. Object name: " + objectName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image: " + e.getMessage());
        }
    }
    
    /**
     * 直接上传原图，不进行处理
     */
    @PostMapping("/raw")
    public ResponseEntity<String> uploadRawImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        try {
            // 验证文件大小
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
            }
            
            // 验证文件格式
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            
            String fileExtension = getFileExtension(originalFilename).toUpperCase();
            if (!getSupportedFormats().contains(fileExtension)) {
                return ResponseEntity.badRequest().body("Unsupported file format: " + fileExtension);
            }
            
            // 读取文件数据
            byte[] originalImageData = file.getBytes();
            
            // 计算哈希值
            String hash = HashUtil.calculateHash(originalImageData);
            String folderName = hash.substring(0, 4);
            String fileName = hash.substring(4) + "." + fileExtension.toLowerCase();
            String objectName = folderName + "/" + fileName;
            
            // 上传到对象存储
            String contentType = getContentType(fileExtension);
            objectStorageService.uploadFile(bucketName, objectName, originalImageData, contentType);
            
            // 推送到CDN服务器
            cdnService.pushToCdn(objectName, originalImageData, contentType);
            
            return ResponseEntity.ok("Raw image uploaded successfully. Object name: " + objectName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload raw image: " + e.getMessage());
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
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