package com.mediaforge.imgflux.download;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.mediaforge.imgflux.download", 
    "com.mediaforge.imgflux.engine",
})
public class ImageDownloadApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ImageDownloadApplication.class, args);
    }
}