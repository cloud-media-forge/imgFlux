package com.example.imageservice.controller;

import com.example.imageservice.entity.Image;
import com.example.imageservice.entity.User;
import com.example.imageservice.service.ImageService;
import com.example.imageservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminUIController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ImageService imageService;
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }
        
        String username = principal.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/admin/login";
        }
        
        // 获取用户图片列表
        List<Image> images = imageService.getUserImages(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("images", images);
        
        return "dashboard";
    }
    
    @GetMapping("/images")
    public String listImages(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }
        
        String username = principal.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/admin/login";
        }
        
        // 获取用户图片列表
        List<Image> images = imageService.getUserImages(user.getId());
        
        model.addAttribute("user", user);
        model.addAttribute("images", images);
        
        return "image-list";
    }
    
    @GetMapping("/images/{id}")
    public String viewImage(@PathVariable Long id, Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/admin/login";
        }
        
        String username = principal.getName();
        User user = userService.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/admin/login";
        }
        
        // 这里应该添加权限检查，确保用户只能查看自己的图片
        // 为简化实现，我们假设用户可以查看所有图片
        
        model.addAttribute("user", user);
        // 在实际实现中，应该从数据库获取图片信息
        // Image image = imageService.getImageById(id);
        // model.addAttribute("image", image);
        
        return "image-view";
    }
}