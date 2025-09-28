package com.richardchen.imageservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    
    @Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/api/upload/**").permitAll() // 确保上传API完全开放
            .requestMatchers("/api/**").permitAll() // API endpoints are public for now
            .requestMatchers("/admin/**").permitAll() // 管理端点也开放
            .anyRequest().permitAll() // 所有其他请求都允许
        )
        .formLogin(form -> form
            .loginPage("/admin/login")
            .defaultSuccessUrl("/admin/images", true)
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/admin/login?logout")
            .permitAll()
        )
        .csrf(csrf -> csrf.disable()); // 禁用CSRF保护，方便API测试
        
    return http.build();
}
}