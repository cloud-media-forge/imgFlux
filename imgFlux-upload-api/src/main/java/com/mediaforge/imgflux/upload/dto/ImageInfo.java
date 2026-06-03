package com.mediaforge.imgflux.upload.dto;

import java.time.LocalDateTime;

public class ImageInfo {
    private String fileName;
    private LocalDateTime lastModified;
    private String storageSpace;
    private long fileSize;
    private boolean isFolder;
    private String fullPath;
    
    public ImageInfo() {
    }
    
    public ImageInfo(String fileName, LocalDateTime lastModified, String storageSpace, long fileSize, boolean isFolder, String fullPath) {
        this.fileName = fileName;
        this.lastModified = lastModified;
        this.storageSpace = storageSpace;
        this.fileSize = fileSize;
        this.isFolder = isFolder;
        this.fullPath = fullPath;
    }
    
    // Getters and setters
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public LocalDateTime getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
    
    public String getStorageSpace() {
        return storageSpace;
    }
    
    public void setStorageSpace(String storageSpace) {
        this.storageSpace = storageSpace;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    public boolean isFolder() {
        return isFolder;
    }
    
    public void setFolder(boolean folder) {
        isFolder = folder;
    }
    
    public String getFullPath() {
        return fullPath;
    }
    
    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }
}