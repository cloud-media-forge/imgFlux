package com.mediaforge.imgflux.upload.controller.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.mediaforge.imgflux.engine.gm.ImageProcessingService;
import com.mediaforge.imgflux.engine.gm.ThumbnailDefinition;
import com.mediaforge.imgflux.engine.service.cdn.CdnService;
import com.mediaforge.imgflux.engine.service.storage.ObjectStorageService;
import com.mediaforge.imgflux.engine.service.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/upload")
public class ImageUploadController {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private ObjectStorageService objectStorageService;
    
    @Autowired
    private CdnService cdnService;
    
    @Value("${img-flux.image.max-size}")
    private long maxFileSize;
    
    @Value("${img-flux.image.supported-formats}")
    private String supportedFormats;
    
    @Value("${img-flux.storage.options.minio.bucket}")
    private String bucketName;

    // Supported image formats
    private List<String> getSupportedFormats() {
        return Arrays.asList(supportedFormats.split(","));
    }
    
    /**
     * Upload and process image
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
            // Validate file size
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
            }

            // Validate file format
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            
            String fileExtension = getFileExtension(originalFilename).toUpperCase();
            if (!getSupportedFormats().contains(fileExtension)) {
                return ResponseEntity.badRequest().body("Unsupported file format: " + fileExtension);
            }

            // If target format is not specified, use original format
            if (format == null || format.isEmpty()) {
                format = fileExtension;
            } else {
                format = format.toUpperCase();
            }

            // Validate target format
            if (!getSupportedFormats().contains(format)) {
                return ResponseEntity.badRequest().body("Unsupported target format: " + format);
            }

            // Read file data
            byte[] originalImageData = file.getBytes();

            // Process image
            ThumbnailDefinition definition = new ThumbnailDefinition();
            definition.setImageData(originalImageData);
            definition.setWidth(width);
            definition.setHeight(height);
            definition.setQuality(quality);
            definition.setExtent(extent);
            definition.setTrim(trim);
            definition.setToFormat(format);
            byte[] processedImageData = imageProcessingService.processImage(definition);

            // Calculate hash value
            String hash = HashUtil.calculateHash(processedImageData);
            String folderName = hash.substring(0, 4);
            String fileName = hash.substring(4) + "." + format.toLowerCase();
            String objectName = folderName + "/" + fileName;

            // Upload to object storage
            String contentType = getContentType(format);
            objectStorageService.uploadFile(bucketName, objectName, processedImageData, contentType);

            // Push to CDN server
            cdnService.pushToCdn(objectName, processedImageData, contentType);
            
            return ResponseEntity.ok("Image uploaded successfully. Object name: " + objectName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image: " + e.getMessage());
        }
    }
    
    /**
     * Upload original image directly without processing
     */
    @PostMapping("/raw")
    public ResponseEntity<String> uploadRawImage(
            @RequestParam("file") MultipartFile file) throws IOException {
        
        try {
            // Validate file size
            if (file.getSize() > maxFileSize) {
                return ResponseEntity.badRequest().body("File size exceeds maximum allowed size of " + maxFileSize + " bytes");
            }

            // Validate file format
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                return ResponseEntity.badRequest().body("Invalid file name");
            }
            
            String fileExtension = getFileExtension(originalFilename).toUpperCase();
            if (!getSupportedFormats().contains(fileExtension)) {
                return ResponseEntity.badRequest().body("Unsupported file format: " + fileExtension);
            }

            // Read file data
            byte[] originalImageData = file.getBytes();

            // Calculate hash value
            String hash = HashUtil.calculateHash(originalImageData);
            String folderName = hash.substring(0, 4);
            String fileName = hash.substring(4) + "." + fileExtension.toLowerCase();
            String objectName = folderName + "/" + fileName;

            // Upload to object storage
            String contentType = getContentType(fileExtension);
            objectStorageService.uploadFile(bucketName, objectName, originalImageData, contentType);

            // Push to CDN server
            cdnService.pushToCdn(objectName, originalImageData, contentType);
            
            return ResponseEntity.ok("Raw image uploaded successfully. Object name: " + objectName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload raw image: " + e.getMessage());
        }
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    
    /**
     * Get content type based on format
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