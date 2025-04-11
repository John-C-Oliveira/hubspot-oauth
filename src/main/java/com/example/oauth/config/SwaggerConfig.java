package com.example.oauth.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Api MeeTime integração com HubSpot")
                        .version("v1")
                        .description("API de integração com HubSpot para criação de contatos e gerenciamento de autenticação OAuth2."));
    }
}
