package com.mediaforge.imgflux.admin.ui.controller;

import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.mediaforge.imgflux.admin.ui.dto.ImageInfo;
import com.mediaforge.imgflux.engine.service.storage.MinIOStorageService;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/admin")
public class AdminUIController {
    @Value("${img-flux.storage.bucket.name:origin-image}")
    private String bucketName;
    
    @Autowired
    private MinIOStorageService minIOStorageService;

    @GetMapping("/")
    public String root() {
        return "redirect:/admin/images";
    }
    
    @GetMapping("")
    public String rootSlash() {
        return "redirect:/admin/images";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        // Add user information to model
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        // Add empty image list to model
        model.addAttribute("images", new ArrayList<>());
        return "dashboard";
    }
    
    @GetMapping("/images")
    public String listImages(Model model, Principal principal, @RequestParam(defaultValue = "") String path) {
        // Add user information to model
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        // Get image list from MinIO
        List<ImageInfo> images = new ArrayList<>();
        try {
            MinioClient minioClient = minIOStorageService.getMinioClient();
            Iterable<Result<Item>> results = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(path)
                    .recursive(false)
                    .build()
            );
            
            for (Result<Item> result : results) {
                Item item = result.get();
                String objectName = item.objectName();

                // Skip if current path is not empty and object name equals path (this is the current directory itself)
                if (!path.isEmpty() && objectName.equals(path)) {
                    continue;
                }

                // Check if it is a folder (ends with /)
                boolean isFolder = objectName.endsWith("/");

                // Get display name (remove path prefix)
                String displayName = objectName;
                if (!path.isEmpty() && objectName.startsWith(path)) {
                    displayName = objectName.substring(path.length());
                }

                // If it is a folder, only keep the first level directory name
                String finalObjectName = objectName; // Save original object name for subsequent processing
                if (isFolder && !displayName.isEmpty()) {
                    if (displayName.endsWith("/")) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }
                    // If there is still /, only take the first level
                    int slashIndex = displayName.indexOf('/');
                    if (slashIndex > 0) {
                        displayName = displayName.substring(0, slashIndex);
                        finalObjectName = path + displayName + "/";
                    }
                }
                
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.setFileName(displayName);
                // Handle case where lastModified might be null
                try {
                    if (item.lastModified() != null) {
                        imageInfo.setLastModified(item.lastModified().toLocalDateTime());
                    } else {
                        imageInfo.setLastModified(java.time.LocalDateTime.now());
                    }
                } catch (Exception e) {
                    // If conversion fails, use current time
                    imageInfo.setLastModified(java.time.LocalDateTime.now());
                }
                imageInfo.setStorageSpace(bucketName);
                imageInfo.setFileSize(item.size());
                imageInfo.setFolder(isFolder);
                imageInfo.setFullPath(finalObjectName); // Use processed object name
                images.add(imageInfo);
            }
        } catch (Exception e) {
            // If getting image list fails, log error and use empty list
            e.printStackTrace();
        }
        
        model.addAttribute("images", images);
        model.addAttribute("currentPath", path);
        return "image-list";
    }
    
    @PostMapping("/images/create-folder")
    @ResponseBody
    public ResponseEntity<String> createFolder(@RequestParam String folderName, @RequestParam(defaultValue = "") String path) {
        try {
            MinioClient minioClient = minIOStorageService.getMinioClient();
            String fullPath = path.isEmpty() ? folderName + "/" : path + folderName + "/";

            // Upload an empty object to create folder
            minioClient.putObject(
                io.minio.PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fullPath)
                    .stream(new java.io.ByteArrayInputStream(new byte[0]), 0, -1)
                    .build()
            );
            
            return ResponseEntity.ok("Folder created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to create folder: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/images/delete")
    @ResponseBody
    public ResponseEntity<String> deleteObject(@RequestParam String objectName) {
        try {
            MinioClient minioClient = minIOStorageService.getMinioClient();
            minioClient.removeObject(
                io.minio.RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            
            return ResponseEntity.ok("Object deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete object: " + e.getMessage());
        }
    }
    
    @GetMapping("/images/{id}")
    public String viewImage(@PathVariable Long id, Model model, Principal principal) {
        // Add user information to model
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        // Add empty image object to model (simplified implementation)
        model.addAttribute("image", new Object() {
            public String getFileHash() { return "sample-hash"; }
            public String getFileName() { return "sample.jpg"; }
            public Integer getWidth() { return 800; }
            public Integer getHeight() { return 600; }
            public Long getFileSize() { return 102400L; }
            public String getContentType() { return "image/jpeg"; }
            public String getCreatedAt() { return "2023-01-01 12:00:00"; }
            public String getFilePath() { return "/path/to/sample.jpg"; }
        });
        return "image-view";
    }

    // Add endpoint to view image directly
    @GetMapping("/image/**")
    public ResponseEntity<byte[]> viewImageDirect(HttpServletRequest request) {
        try {
            // Extract image path from request path
            String requestUri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String imagePath = requestUri.substring(contextPath.length() + "/admin/image/".length());

            // Download image from MinIO
            MinioClient minioClient = minIOStorageService.getMinioClient();
            InputStream stream = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(imagePath)
                    .build()
            );

            // Read image data
            byte[] imageData = stream.readAllBytes();
            stream.close();

            // Determine content type
            String contentType = "image/jpeg"; // Default value
            if (imagePath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (imagePath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }

            // Set response headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(imageData.length);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(imageData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/test")
    @ResponseBody
    public String testPageAccess() {
        return "All pages are accessible without login!";
    }
}