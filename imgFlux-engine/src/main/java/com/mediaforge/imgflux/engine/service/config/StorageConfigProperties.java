package com.mediaforge.imgflux.engine.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "img-flux.storage")
public class StorageConfigProperties {
    
    /**
     * Storage type: minio, aws, aliyun
     */
    private String type = "minio";
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
}