package com.mediaforge.imgflux.engine.service.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "img-flux.storage.type", havingValue = "aliyun")
public class AliyunOssStorageService implements ObjectStorageService {
    
    @Value("${img-flux.storage.options.alibaba.oss.access-key}")
    private String accessKeyId;
    
    @Value("${img-flux.storage.options.alibaba.oss.secret-key}")
    private String accessKeySecret;
    
    @Value("${img-flux.storage.options.alibaba.oss.endpoint}")
    private String endpoint;
    
    private OSS ossClient;
    
    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }
    
    @Override
    public void uploadFile(String bucketName, String objectName, byte[] data, String contentType) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
            metadata.setContentLength(data.length);
            
            InputStream inputStream = new ByteArrayInputStream(data);
            ossClient.putObject(bucketName, objectName, inputStream, metadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to Alibaba Cloud OSS", e);
        }
    }
    
    @Override
    public byte[] downloadFile(String bucketName, String objectName) {
        try {
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            return ossObject.getObjectContent().readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from Alibaba Cloud OSS", e);
        }
    }
    
    @Override
    public boolean fileExists(String bucketName, String objectName) {
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to check file existence in Alibaba Cloud OSS", e);
        }
    }
    
    @Override
    public void deleteFile(String bucketName, String objectName) {
        try {
            ossClient.deleteObject(bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from Alibaba Cloud OSS", e);
        }
    }
}