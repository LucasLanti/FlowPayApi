package com.example.flowpay.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flowPayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FlowPay API")
                        .description("Documentação da API do FlowPay.")
                        .version("v1"));
    }
}
