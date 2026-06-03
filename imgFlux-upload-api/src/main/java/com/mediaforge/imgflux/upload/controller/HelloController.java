package com.mediaforge.imgflux.upload.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * default javadoc.
 * <pre>
 * created by @author Richard Chen
 * on @date 2026-06-03 18:54:00
 * </pre>
 **/
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

}
