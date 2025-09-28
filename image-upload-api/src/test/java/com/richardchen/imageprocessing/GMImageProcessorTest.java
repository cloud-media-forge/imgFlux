package com.richardchen.imageprocessing;

import com.richardchen.imageupload.ImageUploadApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ImageUploadApplication.class)
public class GMImageProcessorTest {
    
    @Autowired
    private GMImageProcessor gmImageProcessor;
    
    @Test
    public void testGMImageProcessorInitialization() {
        assertNotNull(gmImageProcessor, "GMImageProcessor should be autowired");
    }
}