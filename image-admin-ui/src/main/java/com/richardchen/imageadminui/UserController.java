package com.richardchen.imageadminui;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        // 简化实现，不依赖其他模块的实体和服务
        return ResponseEntity.ok(new RegisterResponse("User registered successfully", 1L));
    }
    
    // 注册请求DTO
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
    
    // 注册响应DTO
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