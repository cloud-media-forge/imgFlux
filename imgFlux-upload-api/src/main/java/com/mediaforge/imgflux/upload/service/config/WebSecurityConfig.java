package com.mediaforge.imgflux.upload.service.config;

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
            .requestMatchers("/api/upload/**").permitAll() // Ensure upload API is completely open
            .requestMatchers("/api/**").permitAll() // API endpoints are public for now
            .requestMatchers("/admin/**").permitAll() // Admin endpoints are also open
            .anyRequest().permitAll() // All other requests are allowed
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
        .csrf(csrf -> csrf.disable()); // Disable CSRF protection for easy API testing
        
    return http.build();
}
}