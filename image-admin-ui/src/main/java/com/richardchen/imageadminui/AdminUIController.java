package com.richardchen.imageadminui;

import com.richardchen.imageadminui.dto.ImageInfo;
import com.richardchen.imageservice.storage.MinIOStorageService;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
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
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminUIController {
    
    @Autowired
    private MinIOStorageService minIOStorageService;
    
    @Value("${minio.bucket}")
    private String bucketName;
    
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
        // 添加用户信息到模型
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        // 添加空的图片列表到模型
        model.addAttribute("images", new ArrayList<>());
        return "dashboard";
    }
    
    @GetMapping("/images")
    public String listImages(Model model, Principal principal, @RequestParam(defaultValue = "") String path) {
        // 添加用户信息到模型
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        
        // 从MinIO获取图片列表
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
                
                // 如果当前路径不为空且对象名等于路径，则跳过（这是当前目录本身）
                if (!path.isEmpty() && objectName.equals(path)) {
                    continue;
                }
                
                // 判断是否为文件夹（以/结尾）
                boolean isFolder = objectName.endsWith("/");
                
                // 获取显示名称（去掉路径前缀）
                String displayName = objectName;
                if (!path.isEmpty() && objectName.startsWith(path)) {
                    displayName = objectName.substring(path.length());
                }
                
                // 如果是文件夹，只保留第一级目录名
                String finalObjectName = objectName; // 保存原始对象名用于后续处理
                if (isFolder && !displayName.isEmpty()) {
                    if (displayName.endsWith("/")) {
                        displayName = displayName.substring(0, displayName.length() - 1);
                    }
                    // 如果还有/，只取第一级
                    int slashIndex = displayName.indexOf('/');
                    if (slashIndex > 0) {
                        displayName = displayName.substring(0, slashIndex);
                        finalObjectName = path + displayName + "/";
                    }
                }
                
                ImageInfo imageInfo = new ImageInfo();
                imageInfo.setFileName(displayName);
                // 处理lastModified可能为null的情况
                try {
                    if (item.lastModified() != null) {
                        imageInfo.setLastModified(item.lastModified().toLocalDateTime());
                    } else {
                        imageInfo.setLastModified(java.time.LocalDateTime.now());
                    }
                } catch (Exception e) {
                    // 如果转换失败，使用当前时间
                    imageInfo.setLastModified(java.time.LocalDateTime.now());
                }
                imageInfo.setStorageSpace(bucketName);
                imageInfo.setFileSize(item.size());
                imageInfo.setFolder(isFolder);
                imageInfo.setFullPath(finalObjectName); // 使用处理后的对象名
                images.add(imageInfo);
            }
        } catch (Exception e) {
            // 如果获取图片列表失败，记录错误并使用空列表
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
            
            // 上传一个空对象来创建文件夹
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
        // 添加用户信息到模型
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        // 添加空的图片对象到模型（简化实现）
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
    
    // 添加直接查看图片的端点
    @GetMapping("/image/**")
    public ResponseEntity<byte[]> viewImageDirect(HttpServletRequest request) {
        try {
            // 从请求路径中提取图片路径
            String requestUri = request.getRequestURI();
            String contextPath = request.getContextPath();
            String imagePath = requestUri.substring(contextPath.length() + "/admin/image/".length());
            
            // 从MinIO下载图片
            MinioClient minioClient = minIOStorageService.getMinioClient();
            InputStream stream = minioClient.getObject(
                io.minio.GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(imagePath)
                    .build()
            );
            
            // 读取图片数据
            byte[] imageData = stream.readAllBytes();
            stream.close();
            
            // 确定内容类型
            String contentType = "image/jpeg"; // 默认值
            if (imagePath.toLowerCase().endsWith(".png")) {
                contentType = "image/png";
            } else if (imagePath.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            }
            
            // 设置响应头
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