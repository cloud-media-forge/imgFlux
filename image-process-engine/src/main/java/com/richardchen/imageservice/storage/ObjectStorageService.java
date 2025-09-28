package com.richardchen.imageservice.storage;

/**
 * 对象存储服务接口
 */
public interface ObjectStorageService {
    
    /**
     * 上传文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param data 文件数据
     * @param contentType 内容类型
     */
    void uploadFile(String bucketName, String objectName, byte[] data, String contentType);
    
    /**
     * 下载文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件数据
     */
    byte[] downloadFile(String bucketName, String objectName);
    
    /**
     * 检查文件是否存在
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String bucketName, String objectName);
    
    /**
     * 删除文件
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    void deleteFile(String bucketName, String objectName);
}