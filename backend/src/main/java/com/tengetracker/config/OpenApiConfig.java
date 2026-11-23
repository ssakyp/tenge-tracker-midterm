package com.tengetracker.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI tengeTrackerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tenge Tracker REST API")
                        .description("""
                                ## Overview
                                Personal finance tracker: accounts, categories, tags, budgets and transactions, protected with JWT.

                                ## Authentication
                                1. **POST /api/auth/signup** or **POST /api/auth/login** returns `accessToken`.
                                2. Click **Authorize** and paste the token (without the word Bearer).
                                3. Every protected request then sends `Authorization: Bearer <token>`.

                                Public: signup, login, `GET /api/categories`, `GET /api/categories/{id}`, `GET /api/tags`, `GET /api/tags/{id}`.
                                Everything else (all POST/PUT/PATCH/DELETE and all personal data) returns **401** without a valid token,
                                and **403** when you touch another user's account, transaction or budget.

                                ## Data model (relationships)
                                - User 1 -- 1 UserProfile (one-to-one)
                                - User 1 -> N Account, User 1 -> N Budget (one-to-many)
                                - Account 1 -> N Transaction, Category 1 -> N Transaction, Category 1 -> N Budget
                                - Transaction N <-> N Tag through `transaction_tags` (many-to-many)

                                ## Error responses
                                400 validation/business rule, 401 not authenticated, 403 not your data, 404 not found, 409 conflict.
                                Bodies contain `timestamp`, `status` and `error` (or field-level `errors`).
                                """)
                        .version("2.0.0")
                        .contact(new Contact().name("Tenge Tracker Project"))
                        .license(new License().name("Educational project")))
                .servers(List.of(new Server().url("http://localhost:3000").description("Backend (Docker Compose)")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }
}
