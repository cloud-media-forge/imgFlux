package com.richardchen.imageservice.storage;

import io.minio.*;
import io.minio.errors.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinIOStorageService implements ObjectStorageService {
    
    @Value("${minio.endpoint}")
    private String endpoint;
    
    @Value("${minio.access-key}")
    private String accessKey;
    
    @Value("${minio.secret-key}")
    private String secretKey;
    
    private MinioClient minioClient;
    
    private MinioClient getMinioClient() {
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