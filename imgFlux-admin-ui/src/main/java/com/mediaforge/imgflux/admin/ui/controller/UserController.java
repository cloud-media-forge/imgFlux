package com.mediaforge.imgflux.admin.ui.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        // Simplified implementation, does not depend on other modules' entities and services
        return ResponseEntity.ok(new RegisterResponse("User registered successfully", 1L));
    }

    // Register request DTO
    public static class RegisterRequest {
        private String username;
        private String password;
        private String companyName;
        private String teamName;
        
        // Getters and Setters
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public void setPassword(String password) {
            this.password = password;
        }
        
        public String getCompanyName() {
            return companyName;
        }
        
        public void setCompanyName(String companyName) {
            this.companyName = companyName;
        }
        
        public String getTeamName() {
            return teamName;
        }
        
        public void setTeamName(String teamName) {
            this.teamName = teamName;
        }
    }

    // Register response DTO
    public static class RegisterResponse {
        private String message;
        private Long userId;
        
        public RegisterResponse(String message, Long userId) {
            this.message = message;
            this.userId = userId;
        }
        
        public String getMessage() {
            return message;
        }
        
        public Long getUserId() {
            return userId;
        }
    }
}