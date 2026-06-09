package com.mediaforge.imgflux.engine.gm;

import com.mediaforge.imgflux.engine.config.GMConfigProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMBatchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class Gm4JavaBatchCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(Gm4JavaBatchCommand.class);
    
    @Autowired
    private GMConfigProperties gmConfigProperties;
    
    private org.gm4java.engine.GMService gmService;
    private GMBatchCommand gmBatchCommand;
    
    @PostConstruct
    public void init() {
        try {
            GMConnectionPoolConfig config = new GMConnectionPoolConfig();
            config.setMaxActive(gmConfigProperties.getPoolSize());
            config.setMaxIdle(gmConfigProperties.getPoolSize());
            config.setMinIdle(1);
            gmService = new PooledGMService(config);
            gmBatchCommand = new GMBatchCommand(gmService, "convert");
            logger.info("GM service initialized with pool size: {}", gmConfigProperties.getPoolSize());
        } catch (Exception e) {
            logger.warn("GM service not available — image processing will use Java2D fallbacks. Cause: {}", e.getMessage());
            gmService = null;
            gmBatchCommand = null;
        }
    }
    
    @PreDestroy
    public void destroy() {
        // PooledGMService does not require special shutdown handling
        logger.info("GM service destroyed");
    }

    
    /**
     * Get GMBatchCommand instance
     * @return GMBatchCommand instance
     */
    @Bean
    public GMBatchCommand getGMBatchCommand() {
        return gmBatchCommand;
    }

    public boolean isAvailable() {
        return gmBatchCommand != null;
    }
}