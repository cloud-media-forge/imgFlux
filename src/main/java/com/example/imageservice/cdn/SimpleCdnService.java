package com.example.imageservice.cdn;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SimpleCdnService implements CdnService {
    
    @Value("${cdn.base-url}")
    private String cdnBaseUrl;
    
    @Value("${cdn.api-key}")
    private String cdnApiKey;
    
    private final RestTemplate restTemplate;
    
    public SimpleCdnService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public void pushToCdn(String filePath, byte[] data, String contentType) {
        try {
            String url = cdnBaseUrl + "/" + filePath;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cdnApiKey);
            headers.set("Content-Type", contentType);
            
            HttpEntity<byte[]> entity = new HttpEntity<>(data, headers);
            
            restTemplate.exchange(url, HttpMethod.PUT, entity, String.class);
        } catch (Exception e) {
            // 记录日志但不抛出异常，因为CDN推送失败不应影响主要功能
            System.err.println("Failed to push to CDN: " + e.getMessage());
        }
    }
    
    @Override
    public byte[] getFromCdn(String filePath) {
        try {
            String url = cdnBaseUrl + "/" + filePath;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cdnApiKey);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, byte[].class);
            
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public boolean existsInCdn(String filePath) {
        try {
            String url = cdnBaseUrl + "/" + filePath;
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + cdnApiKey);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                url, HttpMethod.HEAD, entity, Void.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}