package com.mediaforge.imgflux.upload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.mediaforge.imgflux.upload", 
    "com.mediaforge.imgflux.engine",
})
@EntityScan(basePackages = "com.mediaforge.imgflux.upload.service.entity")
@EnableJpaRepositories(basePackages = "com.mediaforge.imgflux.upload.service.repository")
public class ImageUploadApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ImageUploadApplication.class, args);
    }
}