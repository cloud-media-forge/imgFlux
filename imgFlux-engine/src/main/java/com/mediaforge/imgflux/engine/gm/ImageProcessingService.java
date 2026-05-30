package com.mediaforge.imgflux.engine.gm;

/**
 * Image processing service interface
 */
public interface ImageProcessingService {
    
    /**
     * Process image
     * @param imageData Original image binary data
     * @param width Target width
     * @param height Target height
     * @param quality Image quality (1-100)
     * @param extent Whether to extend image
     * @param trim Whether to trim image
     * @param format Target format
     * @return Processed image binary data
     */
    byte[] processImage(byte[] imageData, int width, int height, int quality, 
                       boolean extent, boolean trim, String format);
}