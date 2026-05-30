package com.mediaforge.imgflux.engine.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "img-flux.gm")
public class GMConfigProperties {
    
    /**
     * GM thread pool size, default value is 16
     */
    private int poolSize = 16;

    public int getPoolSize() {
        return poolSize;
    }

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }
}
