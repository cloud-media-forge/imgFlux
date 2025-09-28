package com.example.imageservice.service;

import com.example.imageservice.entity.Image;
import com.example.imageservice.repository.ImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ImageService {
    
    @Autowired
    private ImageRepository imageRepository;
    
    public Image saveImage(Image image) {
        return imageRepository.save(image);
    }
    
    public List<Image> getUserImages(Long userId) {
        return imageRepository.findByUserId(userId);
    }
    
    public List<Image> getUserImagesByHash(Long userId, String fileHash) {
        return imageRepository.findByUserIdAndFileHash(userId, fileHash);
    }
}