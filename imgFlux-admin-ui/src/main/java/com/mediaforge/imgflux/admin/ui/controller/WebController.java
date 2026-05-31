package com.mediaforge.imgflux.admin.ui.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping({
        "/",
        "/dashboard",
        "/images",
        "/images/**",
        "/login",
        "/register",
        "/error"
    })
    public String index() {
        return "forward:/index.html";
    }
}
