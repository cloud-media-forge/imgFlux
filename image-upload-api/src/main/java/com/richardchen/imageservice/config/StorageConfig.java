package com.richardchen.imageservice.config;

import com.richardchen.imageservice.storage.ObjectStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfig {
    
    // AWS S3存储服务配置
    @Bean
    @Profile("aws")
    public ObjectStorageService awsStorageService() {
        // 注意：需要实现AwsS3StorageService类
        // return new AwsS3StorageService();
        throw new UnsupportedOperationException("AWS S3 storage service not implemented yet");
    }
    
    // 阿里云OSS存储服务配置
    @Bean
    @Profile("aliyun")
    public ObjectStorageService aliyunStorageService() {
        // 注意：需要实现AliyunOssStorageService类
        // return new AliyunOssStorageService();
        throw new UnsupportedOperationException("Aliyun OSS storage service not implemented yet");
    }
}