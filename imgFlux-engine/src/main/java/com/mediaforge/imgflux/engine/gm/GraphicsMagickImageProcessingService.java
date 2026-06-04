package com.mediaforge.imgflux.engine.gm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GraphicsMagickImageProcessingService implements ImageProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(GraphicsMagickImageProcessingService.class);
    
    @Autowired
    private GMImageProcessor gmImageProcessor;
    
    @Override
    public byte[] processImage(ThumbnailDefinition definition) {
        try {
            return gmImageProcessor.processImageWithBatchCommand(definition);
        } catch (Exception e) {
            logger.error("Error processing image", e);
            throw new RuntimeException("Failed to process image", e);
        }
    }
}
