package com.ventsim.ventsim.webConfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "http://127.0.0.0:3000", "https://miniature-space-happiness-46w5q5vxg5r24w6-3001.app.github.dev", "https://miniature-space-happiness-46w5q5vxg5r24w6-3000.app.github.dev") // adjust to your frontend port
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");

    }


}
