package com.tengetracker.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tengeTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tenge Tracker REST API")
                        .description("API for managing financial accounts and transactions. Use the endpoints below to inspect request and response schemas and try requests against the running application.")
                        .version("1.0.0")
                        .contact(new Contact().name("Tenge Tracker Project")));
    }
}
