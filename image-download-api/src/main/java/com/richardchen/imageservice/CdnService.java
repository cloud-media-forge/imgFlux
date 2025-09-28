package com.example.imageservice.cdn;

/**
 * CDN服务接口
 */
public interface CdnService {
    
    /**
     * 推送文件到CDN
     * @param filePath 文件路径
     * @param data 文件数据
     * @param contentType 内容类型
     */
    void pushToCdn(String filePath, byte[] data, String contentType);
    
    /**
     * 从CDN获取文件
     * @param filePath 文件路径
     * @return 文件数据，如果不存在则返回null
     */
    byte[] getFromCdn(String filePath);
    
    /**
     * 检查文件是否存在于CDN
     * @param filePath 文件路径
     * @return 是否存在
     */
    boolean existsInCdn(String filePath);
}