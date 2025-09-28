package com.example.imageservice.config;

import com.example.imageservice.storage.MinIOStorageService;
import com.example.imageservice.storage.ObjectStorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfig {
    
    @Bean
    @Profile("minio")
    public ObjectStorageService minioStorageService() {
        return new MinIOStorageService();
    }
    
    // 可以添加其他存储服务的配置
    // @Bean
    // @Profile("aws")
    // public ObjectStorageService awsStorageService() {
    //     return new AwsS3StorageService();
    // }
    //
    // @Bean
    // @Profile("aliyun")
    // public ObjectStorageService aliyunStorageService() {
    //     return new AliyunOssStorageService();
    // }
}