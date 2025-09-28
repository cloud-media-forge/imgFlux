package com.richardchen.imageupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.richardchen.imageupload", 
    "com.richardchen.imageprocessing",
    "com.richardchen.imageservice"
})
@EntityScan(basePackages = "com.richardchen.imageservice.entity")
@EnableJpaRepositories(basePackages = "com.richardchen.imageservice.repository")
public class ImageUploadApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ImageUploadApplication.class, args);
    }
}