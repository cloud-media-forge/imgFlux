package com.richardchen.imageservice.cdn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SimpleCdnService implements CdnService {
    
    @Value("${cdn.base-url:}")
    private String cdnBaseUrl;
    
    @Value("${cdn.api-key:}")
    private String cdnApiKey;
    
    @Override
    public void pushToCdn(String filePath, byte[] data, String contentType) {
        // 简单实现，仅记录日志
        System.out.println("Pushing to CDN: " + filePath + " (Content-Type: " + contentType + ")");
        // 在实际应用中，这里会实现真正的CDN推送逻辑
    }
    
    @Override
    public byte[] getFromCdn(String filePath) {
        // 简单实现，返回null表示文件不存在于CDN
        System.out.println("Getting from CDN: " + filePath);
        return null;
    }
    
    @Override
    public boolean existsInCdn(String filePath) {
        // 简单实现，始终返回false
        System.out.println("Checking existence in CDN: " + filePath);
        return false;
    }
}