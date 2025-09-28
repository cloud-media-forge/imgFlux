package com.example.imageservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "gm")
public class GMConfigProperties {
    
    /**
     * GM线程池大小，默认值为16
     */
    private int poolSize = 16;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}