package com.richardchen.imageservice.config;

import com.richardchen.imageservice.storage.AliyunOssStorageService;
import com.richardchen.imageservice.storage.AwsS3StorageService;
import com.richardchen.imageservice.storage.MinIOStorageService;
import com.richardchen.imageservice.storage.ObjectStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageServiceConfig {
    
    @Autowired
    private StorageConfigProperties storageConfigProperties;
    
    @Bean
    public ObjectStorageService objectStorageService(
            MinIOStorageService minioStorageService,
            AwsS3StorageService awsS3StorageService,
            AliyunOssStorageService aliyunOssStorageService) {
        
        switch (storageConfigProperties.getType().toLowerCase()) {
            case "aws":
                return awsS3StorageService;
            case "aliyun":
                return aliyunOssStorageService;
            case "minio":
            default:
                return minioStorageService;
        }
    }
}