package com.example.imageservice.imageprocessing;

import com.example.imageservice.ImageServiceApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ImageServiceApplication.class)
public class GMImageProcessorTest {
    
    @Autowired
    private GMImageProcessor gmImageProcessor;
    
    @Test
    public void testGMImageProcessorInitialization() {
        assertNotNull(gmImageProcessor, "GMImageProcessor should be autowired");
    }
}