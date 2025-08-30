package org.fxtravel.fxspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://192.168.184.131:30080") // 允许前端地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许 OPTIONS 方法
                .allowedHeaders("*")
                .allowCredentials(true); // 允许携带 Cookie
    }
}