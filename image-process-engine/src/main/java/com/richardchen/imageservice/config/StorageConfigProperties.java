package com.richardchen.imageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
public class StorageConfigProperties {
    
    /**
     * 存储类型: minio, aws, aliyun
     */
    private String type = "minio";
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}