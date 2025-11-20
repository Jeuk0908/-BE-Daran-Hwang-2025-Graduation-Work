package org.example.greduatebe.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정 Configuration
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    /**
     * CORS 설정
     * 프론트엔드 애플리케이션에서 API 호출을 허용하기 위한 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("Configuring CORS mappings - allowedOrigins: {}", allowedOrigins);

        String[] origins = allowedOrigins.split(",");

        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // preflight 요청 캐시 시간 (초)
    }
}
