package com.mediaforge.imgflux.download.api;

import com.mediaforge.imgflux.engine.gm.ImageProcessingService;
import com.mediaforge.imgflux.engine.service.cdn.CdnService;
import com.mediaforge.imgflux.engine.service.storage.ObjectStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/thumbnail")
public class ImageThumbnailController {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private ObjectStorageService objectStorageService;
    
    @Autowired
    private CdnService cdnService;
    
    @Value("${img-flux.storage.bucket.name:original-image}")
    private String bucketName;
    
    /**
     * View image - Get original image from MinIO server using image path as parameter
     */
    @GetMapping("/view/**")
    public ResponseEntity<byte[]> viewImage(HttpServletRequest request) {
        try {
            // Extract image path from request path
            String requestUri = request.getRequestURI();
            String imagePath = requestUri.substring("/api/v1/thumbnail/view/".length());

            // Download image from MinIO
            byte[] imageData = objectStorageService.downloadFile(bucketName, imagePath);

            // Determine content type
            String contentType = "image/jpeg"; // Default value
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
     * Download image - First get original image from MinIO server using image path parameter,
     * then call image engine for image compression processing
     */
    @GetMapping("/forge/**")
    public ResponseEntity<byte[]> downloadImage(
            HttpServletRequest request,
            @RequestParam(value = "width", required = false, defaultValue = "0") int width,
            @RequestParam(value = "height", required = false, defaultValue = "0") int height,
            @RequestParam(value = "quality", required = false, defaultValue = "80") int quality,
            @RequestParam(value = "extent", required = false, defaultValue = "false") boolean extent,
            @RequestParam(value = "trim", required = false, defaultValue = "false") boolean trim,
            @RequestParam(value = "format", required = false, defaultValue = "JPG") String format) {
        
        try {
            // Extract image path from request path
            String requestUri = request.getRequestURI();
            String imagePath = requestUri.substring("/api/v1/thumbnail/forge/".length());

            // Download original image from MinIO
            byte[] originalImageData = objectStorageService.downloadFile(bucketName, imagePath);

            // If no processing parameters are specified, return original image directly
            if (width == 0 && height == 0 && quality == 80 && !extent && !trim && 
                ("JPG".equalsIgnoreCase(format) || "JPEG".equalsIgnoreCase(format))) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(getContentType(format)));
                headers.setContentLength(originalImageData.length);
                return new ResponseEntity<>(originalImageData, headers, HttpStatus.OK);
            }

            // Use image processing service to process image
            byte[] processedImageData = imageProcessingService.processImage(
                    originalImageData, width, height, quality, extent, trim, format);

            // Determine content type
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