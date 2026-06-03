package com.mediaforge.imgflux.engine.service.storage;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "img-flux.storage.type", havingValue = "minio", matchIfMissing = true)
public class MinIOStorageService implements ObjectStorageService {

    private static final Logger logger = LoggerFactory.getLogger(MinIOStorageService.class);

    @Value("${img-flux.storage.options.minio.endpoint}")
    private String endpoint;

    @Value("${img-flux.storage.options.minio.access-key}")
    private String accessKey;

    @Value("${img-flux.storage.options.minio.secret-key}")
    private String secretKey;

    @Value("${img-flux.storage.options.minio.bucket:original-image}")
    private String defaultBucket;

    private MinioClient minioClient;

    @PostConstruct
    public void init() {
        getMinioClient();
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(defaultBucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(defaultBucket).build()
                );
                logger.info("Created MinIO bucket: {}", defaultBucket);
            }
        } catch (Exception e) {
            logger.warn("Failed to ensure MinIO bucket '{}' exists: {}", defaultBucket, e.getMessage());
        }
    }

    public MinioClient getMinioClient() {
        if (minioClient == null) {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
        }
        return minioClient;
    }
    
    @Override
    public void uploadFile(String bucketName, String objectName, byte[] data, String contentType) {
        try {
            InputStream inputStream = new ByteArrayInputStream(data);
            getMinioClient().putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, data.length, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to MinIO", e);
        }
    }
    
    @Override
    public byte[] downloadFile(String bucketName, String objectName) {
        try {
            GetObjectResponse response = getMinioClient().getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            
            return response.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from MinIO", e);
        }
    }
    
    @Override
    public boolean fileExists(String bucketName, String objectName) {
        try {
            getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public void deleteFile(String bucketName, String objectName) {
        try {
            getMinioClient().removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from MinIO", e);
        }
    }
}