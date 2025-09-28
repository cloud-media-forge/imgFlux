package com.example.imageservice.imageprocessing;

import org.gm4java.engine.GMConnection;
import org.gm4java.engine.GMException;
import org.gm4java.engine.support.GMConnectionPoolConfig;
import org.gm4java.engine.support.PooledGMService;
import org.gm4java.im4java.GMBatchCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class Gm4JavaBatchCommand {
    
    private static final Logger logger = LoggerFactory.getLogger(Gm4JavaBatchCommand.class);
    
    @Autowired
    private com.example.imageservice.config.GMConfigProperties gmConfigProperties;
    
    private org.gm4java.engine.GMService gmService;
    private GMBatchCommand gmBatchCommand;
    
    @PostConstruct
    public void init() {
        try {
            // 创建GM连接池配置
            GMConnectionPoolConfig config = new GMConnectionPoolConfig();
            config.setMaxActive(gmConfigProperties.getPoolSize());
            config.setMaxIdle(gmConfigProperties.getPoolSize());
            config.setMinIdle(1);
            
            // 创建PooledGMService
            gmService = new PooledGMService(config);
            
            // 创建GMBatchCommand
            gmBatchCommand = new GMBatchCommand(gmService, "convert");
            
            logger.info("GM service initialized with pool size: {}", gmConfigProperties.getPoolSize());
        } catch (Exception e) {
            logger.error("Failed to initialize GM service", e);
            throw new RuntimeException("Failed to initialize GM service", e);
        }
    }
    
    @PreDestroy
    public void destroy() {
        // PooledGMService不需要特殊关闭处理
        logger.info("GM service destroyed");
    }
    
    /**
     * 执行GM命令
     * @param command GM命令
     * @param arguments 命令参数
     * @return 命令执行结果
     * @throws GMException
     */
    public String execute(String command, String... arguments) throws Exception {
        if (gmService == null) {
            throw new IllegalStateException("GM service not initialized");
        }
        return gmService.execute(command, arguments);
    }
    
    /**
     * 获取GM连接
     * @return GM连接
     * @throws Exception
     */
    public GMConnection getConnection() throws Exception {
        if (gmService == null) {
            throw new IllegalStateException("GM service not initialized");
        }
        return gmService.getConnection();
    }
    
    /**
     * 获取GMBatchCommand实例
     * @return GMBatchCommand实例
     */
    public GMBatchCommand getGMBatchCommand() {
        if (gmBatchCommand == null) {
            throw new IllegalStateException("GMBatchCommand not initialized");
        }
        return gmBatchCommand;
    }
}