package com.mediaforge.imgflux.download.api;

import com.mediaforge.imgflux.download.utils.ResizeParamParser;
import com.mediaforge.imgflux.download.utils.ResizePathUtils;
import com.mediaforge.imgflux.engine.gm.ImageProcessingService;
import com.mediaforge.imgflux.engine.gm.ThumbnailDefinition;
import com.mediaforge.imgflux.engine.service.cdn.CdnService;
import com.mediaforge.imgflux.engine.service.storage.ObjectStorageService;
import jakarta.servlet.http.HttpServletRequest;
import com.mediaforge.imgflux.download.utils.ResizePathUtils.ResizePath;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/thumbnail")
@Slf4j
public class ImageThumbnailController {
    
    @Autowired
    private ImageProcessingService imageProcessingService;
    
    @Autowired
    private ObjectStorageService objectStorageService;
    
    @Autowired
    private CdnService cdnService;

    @Autowired
    private RestTemplate restTemplate;

    private static final Set<String> VALID_MODES = Set.of("local", "remote");

    @Value("${img-flux.storage.options.minio.bucket:original-image}")
    private String bucketName;

    @Value("${img-flux.image.supported-formats:JPG,JPEG,PNG,GIF,AVIF,WEBP}")
    private String supportedFormats;
    
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
            log.error("viewImage error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Download image - First get original image from MinIO server using image path parameter,
     * then call image engine for image compression processing
     */
    @GetMapping("/forge/{mode}/**")
    public ResponseEntity<byte[]> resize(
            HttpServletRequest request,
            @PathVariable("mode") String mode,
            @RequestParam(value = "width", required = false, defaultValue = "0") int width,
            @RequestParam(value = "height", required = false, defaultValue = "0") int height,
            @RequestParam(value = "quality", required = false, defaultValue = "80") int quality,
            @RequestParam(value = "extent", required = false, defaultValue = "false") boolean extent,
            @RequestParam(value = "trim", required = false, defaultValue = "false") boolean trim,
            @RequestParam(value = "format", required = false, defaultValue = "JPG") String format,
            @RequestParam(value = "srcLang", required = false, defaultValue = "") String srcLang,
            @RequestParam(value = "toLang", required = false, defaultValue = "") String toLang) {

        try {
            validateMode(mode);

            // Extract image path from request path
            String requestUri = request.getRequestURI();
            String prefix = "/api/v1/thumbnail/forge/" + mode + "/";
            String imagePath = requestUri.substring(prefix.length());

            // Download original image
            byte[] originalImageData = downloadImage(mode, imagePath);

            // If no processing parameters are specified, return original image directly
            if (width == 0 && height == 0 && quality == 80 && !extent && !trim && 
                ("JPG".equalsIgnoreCase(format) || "JPEG".equalsIgnoreCase(format)) &&
                !hasTranslationRequest(srcLang, toLang)) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(getContentType(format)));
                headers.setContentLength(originalImageData.length);
                return new ResponseEntity<>(originalImageData, headers, HttpStatus.OK);
            }

            // Use image processing service to process image
            ThumbnailDefinition definition = new ThumbnailDefinition();
            definition.setImageData(originalImageData);
            definition.setWidth(width);
            definition.setHeight(height);
            definition.setQuality(quality);
            definition.setExtent(extent);
            definition.setTrim(trim);
            definition.setFormat(format);
            definition.setSrcLang(srcLang);
            definition.setToLang(toLang);
            byte[] processedImageData = imageProcessingService.processImage(definition);

            // Determine content type
            String contentType = getContentType(format);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            if (processedImageData != null) {
                headers.setContentLength(processedImageData.length);
            }
            
            return new ResponseEntity<>(processedImageData, headers, HttpStatus.OK);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("resize error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping({"/resize/{mode}/{resize_param}/{*imagePath}"})
    public ResponseEntity<byte[]> resizeImage(
            @PathVariable("mode") String mode,
            @PathVariable("resize_param") String resizeParam,
            @PathVariable("imagePath") String imagePath) {
        try {
            validateMode(mode);

            ThumbnailDefinition definition = ResizeParamParser.parseResizeParam(resizeParam);
            String normalizedImagePath = ResizePathUtils.normalizeDecodedImagePath(imagePath);
            ResizePath resizePath = ResizePathUtils.parseResizePath(normalizedImagePath, supportedFormats);

            byte[] originalImageData = downloadImage(mode, resizePath.sourcePath());
            definition.setImageData(originalImageData);
            definition.setFormat(resizePath.targetFormat());

            byte[] processedImageData = imageProcessingService.processImage(definition);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(getContentType(definition.getFormat())));
            if (processedImageData != null) {
                headers.setContentLength(processedImageData.length);
            }

            return new ResponseEntity<>(processedImageData, headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing image", e);
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
            case "WEBP":
                return "image/webp";
            default:
                return "image/jpeg";
        }
    }

    private boolean hasTranslationRequest(String srcLang, String toLang) {
        return srcLang != null && !srcLang.isBlank()
                && toLang != null && !toLang.isBlank()
                && !srcLang.equalsIgnoreCase(toLang);
    }

    private byte[] downloadImage(String mode, String path) {
        if ("remote".equals(mode)) {
            // Normalize single-slash form (https:/example.com) back to double-slash.
            // Callers may hit either form depending on URL normalization in transit.
            String fixedPath = path.replaceFirst("^https:/([^/])", "https://$1");
            return restTemplate.getForObject(fixedPath, byte[].class);
        }
        return objectStorageService.downloadFile(bucketName, path);
    }

    private void validateMode(String mode) {
        if (!VALID_MODES.contains(mode)) {
            throw new IllegalArgumentException("Invalid mode: " + mode + ". Must be 'local' or 'remote'.");
        }
    }

}
