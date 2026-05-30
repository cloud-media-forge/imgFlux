package com.mediaforge.imgflux.engine.service.storage;

/**
 * Object storage service interface
 */
public interface ObjectStorageService {
    
    /**
     * Upload file
     * @param bucketName Bucket name
     * @param objectName Object name
     * @param data File data
     * @param contentType Content type
     */
    void uploadFile(String bucketName, String objectName, byte[] data, String contentType);
    
    /**
     * Download file
     * @param bucketName Bucket name
     * @param objectName Object name
     * @return File data
     */
    byte[] downloadFile(String bucketName, String objectName);
    
    /**
     * Check if file exists
     * @param bucketName Bucket name
     * @param objectName Object name
     * @return Whether the file exists
     */
    boolean fileExists(String bucketName, String objectName);
    
    /**
     * Delete file
     * @param bucketName Bucket name
     * @param objectName Object name
     */
    void deleteFile(String bucketName, String objectName);
}