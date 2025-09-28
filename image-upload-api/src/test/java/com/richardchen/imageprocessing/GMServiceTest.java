package com.richardchen.imageprocessing;

import com.richardchen.imageservice.config.GMConfigProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {Gm4JavaBatchCommand.class, GMConfigProperties.class})
@Import(GMServiceTest.TestConfig.class)
public class GMServiceTest {
    
    @Autowired
    private Gm4JavaBatchCommand gmService;
    
    @MockBean
    private GMConfigProperties gmConfigProperties;
    
    @Test
    public void testGMServiceInitialization() {
        // 模拟配置属性
        when(gmConfigProperties.getPoolSize()).thenReturn(16);
        
        assertNotNull(gmService, "GMService should be autowired");
    }
    
    // 配置类用于测试
    static class TestConfig {
        // 可以在这里添加测试专用的配置
    }
}