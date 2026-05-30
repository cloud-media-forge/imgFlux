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
            // Create GM connection pool configuration
            GMConnectionPoolConfig config = new GMConnectionPoolConfig();
            config.setMaxActive(gmConfigProperties.getPoolSize());
            config.setMaxIdle(gmConfigProperties.getPoolSize());
            config.setMinIdle(1);

            // Create PooledGMService
            gmService = new PooledGMService(config);

            // Create GMBatchCommand
            gmBatchCommand = new GMBatchCommand(gmService, "convert");
            
            logger.info("GM service initialized with pool size: {}", gmConfigProperties.getPoolSize());
        } catch (Exception e) {
            logger.error("Failed to initialize GM service", e);
            throw new RuntimeException("Failed to initialize GM service", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        // PooledGMService does not require special shutdown handling
        logger.info("GM service destroyed");
    }
    
    /**
     * Execute GM command
     * @param command GM command
     * @param arguments Command arguments
     * @return Command execution result
     * @throws GMException
     */
    public String execute(String command, String... arguments) throws Exception {
        if (gmService == null) {
            throw new IllegalStateException("GM service not initialized");
        }
        return gmService.execute(command, arguments);
    }
    
    /**
     * Get GM connection
     * @return GM connection
     * @throws Exception
     */
    public GMConnection getConnection() throws Exception {
        if (gmService == null) {
            throw new IllegalStateException("GM service not initialized");
        }
        return gmService.getConnection();
    }
    
    /**
     * Get GMBatchCommand instance
     * @return GMBatchCommand instance
     */
    public GMBatchCommand getGMBatchCommand() {
        if (gmBatchCommand == null) {
            throw new IllegalStateException("GMBatchCommand not initialized");
        }
        return gmBatchCommand;
    }
}