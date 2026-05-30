package com.mediaforge.imgflux.admin.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.mediaforge.imgflux.admin.ui", 
    "com.mediaforge.imgflux.upload", 
    "com.mediaforge.imgflux.download", 
    "com.mediaforge.imgflux.engine",
    "com.mediaforge.imgflux.imageservice"
})
@EntityScan(basePackages = "com.mediaforge.imgflux.imageservice.entity")
@EnableJpaRepositories(basePackages = "com.mediaforge.imgflux.admin.ui.repository")
public class AdminUiApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AdminUiApplication.class, args);
    }
}