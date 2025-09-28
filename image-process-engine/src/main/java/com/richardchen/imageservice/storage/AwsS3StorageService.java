package com.richardchen.imageservice.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;

@Service
public class AwsS3StorageService implements ObjectStorageService {
    
    @Value("${aws.s3.access-key-id}")
    private String accessKeyId;
    
    @Value("${aws.s3.secret-access-key}")
    private String secretAccessKey;
    
    @Value("${aws.s3.region}")
    private String region;
    
    private S3Client s3Client;
    
    @PostConstruct
    public void init() {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
        s3Client = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
    
    @Override
    public void uploadFile(String bucketName, String objectName, byte[] data, String contentType) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .contentType(contentType)
                    .build();
            
            s3Client.putObject(request, RequestBody.fromBytes(data));
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to AWS S3", e);
        }
    }
    
    @Override
    public byte[] downloadFile(String bucketName, String objectName) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build();
            
            ResponseInputStream<GetObjectResponse> response = s3Client.getObject(request);
            return response.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from AWS S3", e);
        }
    }
    
    @Override
    public boolean fileExists(String bucketName, String objectName) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build();
            
            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check file existence in AWS S3", e);
        }
    }
    
    @Override
    public void deleteFile(String bucketName, String objectName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    .build();
            
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from AWS S3", e);
        }
    }
}