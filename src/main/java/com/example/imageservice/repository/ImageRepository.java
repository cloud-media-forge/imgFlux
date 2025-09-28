package com.example.imageservice.repository;

import com.example.imageservice.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserId(Long userId);
    List<Image> findByUserIdAndFileHash(Long userId, String fileHash);
}