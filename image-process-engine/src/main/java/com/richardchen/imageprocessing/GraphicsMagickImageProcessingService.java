package com.richardchen.imageprocessing;

import org.gm4java.im4java.GMBatchCommand;
import org.im4java.core.IMOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;

@Service
public class GraphicsMagickImageProcessingService implements ImageProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphicsMagickImageProcessingService.class);
    
    @Autowired
    private Gm4JavaBatchCommand gmService;
    
    @Autowired
    private GMImageProcessor gmImageProcessor;
    
    @Override
    public byte[] processImage(byte[] imageData, int width, int height, int quality, 
                              boolean extent, boolean trim, String format) {
        try {
            // 使用GMBatchCommand处理图片
            return gmImageProcessor.processImageWithBatchCommand(imageData, width, height, quality);
        } catch (Exception e) {
            logger.error("Error processing image", e);
            throw new RuntimeException("Failed to process image", e);
        }
    }
}