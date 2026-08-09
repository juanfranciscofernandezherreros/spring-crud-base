package com.example.crudbase.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Results API")
                        .version("0.1.0")
                        .description("REST API for managing match results")
                        .contact(new Contact()
                                .name("Juan Francisco Fernandez Herreros")
                                .email("juanfranciscofernandezherreros@gmail.com"))
                        .license(new License()
                                .name("Unlicensed")));
    }
}
