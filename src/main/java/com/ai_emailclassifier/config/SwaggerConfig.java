package com.ai_emailclassifier.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI emailClassifierOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("AI Email Classification API")
                        .version("1.0.0")
                        .description("""
                                REST API for classifying emails using AI.
                                
                                Features:
                                • JWT Authentication
                                • AI-powered Email Classification
                                • PostgreSQL Persistence
                                • Role-based Authorization
                                """)
                        .contact(new Contact()
                                .name("Dev Adhikari")
                                .email("YOUR_EMAIL")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()

                        .addSecuritySchemes(
                                "Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}