package com.mediaforge.imgflux.engine.gm;

/**
 * Image processing service interface
 */
public interface ImageProcessingService {

    byte[] processImage(ThumbnailDefinition definition);
}
