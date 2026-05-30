package com.mediaforge.imgflux.upload.service.repository;

import com.mediaforge.imgflux.upload.service.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByUserId(Long userId);
    List<Image> findByUserIdAndFileHash(Long userId, String fileHash);
}