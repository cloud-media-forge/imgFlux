package com.mediaforge.imgflux.engine.service.cdn;

public interface CdnService {
    
    /**
     * Push file to CDN
     * @param filePath File path
     * @param data File data
     * @param contentType Content type
     */
    void pushToCdn(String filePath, byte[] data, String contentType);
    
    /**
     * Get file from CDN
     * @param filePath File path
     * @return File data
     */
    byte[] getFromCdn(String filePath);
    
    /**
     * Check if file exists in CDN
     * @param filePath File path
     * @return Whether the file exists
     */
    boolean existsInCdn(String filePath);
}