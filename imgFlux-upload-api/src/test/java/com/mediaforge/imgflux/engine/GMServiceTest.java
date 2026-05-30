package com.mediaforge.imgflux.engine;

import com.mediaforge.imgflux.engine.config.GMConfigProperties;
import com.mediaforge.imgflux.engine.gm.Gm4JavaBatchCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {Gm4JavaBatchCommand.class, GMConfigProperties.class})
@Import(GMServiceTest.TestConfig.class)
public class GMServiceTest {
    
    @Autowired
    private Gm4JavaBatchCommand gmService;
    
    @MockBean
    private GMConfigProperties gmConfigProperties;
    
    @Test
    public void testGMServiceInitialization() {
        // Mock configuration properties
        when(gmConfigProperties.getPoolSize()).thenReturn(16);
        
        assertNotNull(gmService, "GMService should be autowired");
    }

    // Configuration class for testing
    static class TestConfig {
        // Can add test-specific configurations here
    }
}