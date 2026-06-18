package com.example.flowpay.configs;

import org.springframework.context.annotation.Configuration;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfigs implements WebMvcConfigurer {
    private final LocalValidatorFactoryBean validator;

    public WebConfigs(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    @Override
    public void addCorsMappings(@SuppressWarnings("null") CorsRegistry registry) {
        CorsRegistration registration = registry.addMapping("/**");
        registration.allowedOriginPatterns("*");
        registration.allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS");
        registration.allowedHeaders("*");
        registration.maxAge(3600);
    }

    @Override
    public Validator getValidator() {
        return validator;
    }
}
