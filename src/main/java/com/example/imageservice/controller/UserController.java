package com.example.imageservice.controller;

import com.example.imageservice.entity.User;
import com.example.imageservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            User user = userService.registerUser(
                request.getUsername(), 
                request.getPassword(), 
                request.getCompanyName(), 
                request.getTeamName()
            );
            
            // 返回成功响应
            return ResponseEntity.ok(new RegisterResponse("User registered successfully", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
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
    
    // 错误响应DTO
    public static class ErrorResponse {
        private String error;
        
        public ErrorResponse(String error) {
            this.error = error;
        }
        
        public String getError() {
            return error;
        }
    }
}